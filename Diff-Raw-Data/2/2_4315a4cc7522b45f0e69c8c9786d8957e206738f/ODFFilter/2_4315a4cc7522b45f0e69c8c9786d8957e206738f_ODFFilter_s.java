 /*===========================================================================
   Copyright (C) 2008-2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.filters.openoffice;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiIllegalFilterOperationException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.codehaus.stax2.XMLInputFactory2;
 
 /**
  * This class implements IFilter for XML documents in Open-Document format (ODF).
  * The expected input is the XML document itself. It can be used on ODF documents
  * that are not in Open-Office.org files (i.e. directly on the content.xml of the .odt).
  * For processing ODT, ODS, etc. documents, use the OpenDocumentFilter class,
  * which calls this filter as needed.
  */
 public class ODFFilter implements IFilter {
 
 	private static final String MIMETYPE = "text/x-odf";
 	
 	protected static final String NSURI_TEXT = "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
 	protected static final String NSURI_XLINK = "http://www.w3.org/1999/xlink";
 	
 	protected static final String TEXT_BOOKMARK_REF = "text:bookmark-ref";
 	protected static final String OFFICE_ANNOTATION = "office:annotation";
 
 	private Hashtable<String, ElementRule> toExtract;
 	private Hashtable<String, AttributeRule> attrbutesToExtract;
 	private ArrayList<String> toProtect;
 	private ArrayList<String> subFlow;
 	private LinkedList<Event> queue;
 	private String docName;
 	private XMLStreamReader reader;
 	private int otherId;
 	private int tuId;
 	private String language;
 	private Parameters params;
 	private GenericSkeleton skel;
 	private TextFragment tf;
 	private TextUnit tu;
 	private boolean canceled;
 	private boolean hasNext;
 	private Stack<Context> context;
 	private String lineBreak = "\n";
 	private String containerMimeType;
 
 	public ODFFilter () {
 		toExtract = new Hashtable<String, ElementRule>();
 		toExtract.put("text:p", new ElementRule("text:p", null));
 		toExtract.put("text:h", new ElementRule("text:h", null));
 		toExtract.put("dc:title", new ElementRule("dc:title", null));
 		toExtract.put("dc:description", new ElementRule("dc:description", null));
 		toExtract.put("dc:subject", new ElementRule("dc:subject", null));
 		toExtract.put("meta:keyword", new ElementRule("meta:keyword", null));
 		toExtract.put("meta:user-defined", new ElementRule("meta:user-defined", "meta:name"));
 		toExtract.put("text:index-title-template", new ElementRule("text:index-title-template", null));
 		
 		attrbutesToExtract = new Hashtable<String, AttributeRule>();
 		attrbutesToExtract.put("style:num-prefix", new AttributeRule("style:num-prefix", null));
 		attrbutesToExtract.put("style:num-suffix", new AttributeRule("style:num-suffix", null));
 		attrbutesToExtract.put("table:name", new AttributeRule("table:name", "application/vnd.oasis.opendocument.spreadsheet"));
 
 		subFlow = new ArrayList<String>();
 		subFlow.add("text:note");
 		subFlow.add("office:annotation");
 		
 		toProtect = new ArrayList<String>();
 		toProtect.add("text:initial-creator");
 		toProtect.add("text:creation-date");
 		toProtect.add("text:creation-time");
 		toProtect.add("text:description");
 		toProtect.add("text:user-defined");
 		toProtect.add("text:print-time");
 		toProtect.add("text:print-date");
 		toProtect.add("text:printed-by");
 		toProtect.add("text:editing-cycles");
 		toProtect.add("text:editing-duration");
 		toProtect.add("text:modification-time");
 		toProtect.add("text:modification-date");
 		toProtect.add("text:creator");
 		toProtect.add("text:page-count");
 		toProtect.add("text:paragraph-count");
 		toProtect.add("text:word-count");
 		toProtect.add("text:character-count");
 		toProtect.add("text:table-count");
 		toProtect.add("text:image-count");
 		toProtect.add("text:object-count");
 		toProtect.add("dc:date");
 		toProtect.add("dc:creator");
 		toProtect.add("text:note-citation");
 
 		toProtect.add("text:tracked-changes");
 
 		toProtect.add("text:title"); // Content is defined elsewhere
 		toProtect.add("text:subject"); // Content is defined elsewhere
 		toProtect.add("text:keywords"); // Content is defined elsewhere
 
 		toProtect.add(TEXT_BOOKMARK_REF); // Content is defined elsewhere
 
 		// Do it last to update the defaults if needed
 		params = new Parameters();
 		applyParameters();
 	}
 
 	public void close () {
 		try {
 			if ( reader != null ) {
 				reader.close();
 				reader = null;
 			}
 			hasNext = false;
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public void cancel () {
 		canceled = true;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close();
 		applyParameters();
 		canceled = false;
 		containerMimeType = "";
 		
 		XMLInputFactory fact = XMLInputFactory.newInstance();
 		fact.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
 		fact.setProperty(XMLInputFactory.IS_COALESCING, true);
 		fact.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, true);
 		fact.setProperty(XMLInputFactory2.P_AUTO_CLOSE_INPUT, true);
 
 		try {
 			input.setEncoding("UTF-8"); // Force UTF-8 as the default encoding
 			reader = fact.createXMLStreamReader(input.getStream());
 		}
 		catch ( XMLStreamException e ) {
 			throw new OkapiIOException("Cannot create the XML stream.", e);
 		}
 		language = input.getSourceLanguage();
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
 		context = new Stack<Context>();
 		context.push(new Context("", false));
 		otherId = 0;
 		tuId = 0;
 
 		queue = new LinkedList<Event>();
 		StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 		startDoc.setLanguage(language);
 		startDoc.setName(docName);
 		startDoc.setMimeType(MIMETYPE);
 		startDoc.setType(startDoc.getMimeType());
 		//TODO: Fix the encoding as it is  not necessarily correct as the encoding is not retrieve from XMLStreamReader
 		// We should use reader.getEncoding() when it's set
 		startDoc.setEncoding("UTF-8", false);
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setFilterParameters(params);
 		startDoc.setFilterWriter(createFilterWriter());
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 		hasNext = true;
 	}
 
 	/**
 	 * Sets the MIME type of the file containing this document. This is the MIME type found
 	 * in the mimetype file of the zip file.
 	 * @param mimeType the MIME type to set.
 	 */
 	public void setContainerMimeType (String mimeType) {
 		containerMimeType = mimeType;
 	}
 	
 	public String getName () {
 		return "okf_odf";
 	}
 
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MIMETYPE,
 			getClass().getName(),
 			"OpenDocument",
 			"XML OpenDocument files (e.g. use inside OpenOffice.org documents)."));
 		return list;
 	}
 	
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public Event next () {
 		// Treat cancel
 		if ( canceled ) {
 			queue.clear();
 			queue.add(new Event(EventType.CANCELED));
 			hasNext = false;
 		}
 		// Fill the queue if it's empty
 		if ( queue.isEmpty() ) {
 			read();
 		}
 		// Update hasNext flag on the FINISHED event
 		if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 			hasNext = false;
 		}
 		// Return the head of the queue
 		return queue.poll();
 	}
 	
 	public void setParameters (IParameters newParams) {
 		params = (Parameters)newParams;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	private void read () {
 		skel = new GenericSkeleton();
 		tf = new TextFragment();
 		try {
 			while ( reader.hasNext() ) {
 				switch ( reader.next() ) {
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					if ( context.peek().extract ) {
 						tf.append(reader.getText());
 					}
 					else { // UTF-8 element content: no escape of quote nor extended chars
 						skel.append(Util.escapeToXML(reader.getText(), 0, false, null));
 					}
 					break;
 					
 				case XMLStreamConstants.START_DOCUMENT:
 					//TODO set resource.setTargetEncoding(SET REAL ENCODING);
 					skel.append("<?xml version=\"1.0\" "
 						+ ((reader.getEncoding()==null) ? "" : "encoding=\""+reader.getEncoding()+"\"")
 						+ "?>");
 					break;
 				
 				case XMLStreamConstants.END_DOCUMENT:
 					Ending ending = new Ending(String.valueOf(++otherId));
 					ending.setSkeleton(skel);
 					queue.add(new Event(EventType.END_DOCUMENT, ending));
 					return;
 				
 				case XMLStreamConstants.START_ELEMENT:
 					processStartElement();
 					break;
 				
 				case XMLStreamConstants.END_ELEMENT:
 					if ( processEndElement() ) return; // Send an event
 					break;
 				
 				case XMLStreamConstants.COMMENT:
 					if ( context.peek().extract ) {
 						tf.append(TagType.PLACEHOLDER, null, "<!--" + reader.getText() + "-->");
 					}
 					else {
 						skel.append("<!--" + reader.getText() + "-->");
 					}
 					break;
 
 				case XMLStreamConstants.PROCESSING_INSTRUCTION:
 					if ( context.peek().extract ) {
 						tf.append(TagType.PLACEHOLDER, null,
 							"<?" + reader.getPITarget() + " " + reader.getPIData() + "?>");
 					}
 					else {
 						skel.append("<?" + reader.getPITarget() + " " + reader.getPIData() + "?>");
 					}
 					break;
 				}
 			} // End of main while		
 
 		}
 		catch ( XMLStreamException e ) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	private void setTUInfo (String name) {
 		tu.setType("x-"+name);
 		//lang?? 
 		//id???
 	}
 	
 	// Build the start tag name, and store it in skel if inSkeleton==true
 	private String buildStartTag (String name,
 		boolean inSkeleton )
 	{
 		StringBuilder tmp = new StringBuilder();
 		// Tag name
 		tmp.append("<" + name);
 		
 		// Namespaces
 		String prefix;
 		int count = reader.getNamespaceCount();
 		for ( int i=0; i<count; i++ ) {
 			prefix = reader.getNamespacePrefix(i);
 			tmp.append(String.format(" xmlns%s=\"%s\"",
 				((prefix!=null) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 
 		// Attributes
 		String qualName;
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			
 			prefix = reader.getAttributePrefix(i);
 			if ( prefix == null ) {
 				qualName = reader.getAttributeLocalName(i);
 			}
 			else {
 				qualName = prefix+":"+reader.getAttributeLocalName(i);
 			}
 			// Is this attribute translatable?
 			if ( attrbutesToExtract.containsKey(qualName) ) {
 				AttributeRule rule = attrbutesToExtract.get(qualName);
 				// Use indexOf to handle both normal non-template and template cases
 				if (( rule.mimeType == null ) || ( containerMimeType.indexOf(rule.mimeType) == 0 )) {
 					// This is translatable, should we extract?
 					String text = reader.getAttributeValue(i);
 					if ( hasTrueText(text) ) {
 						// Create a text unit
 						TextUnit tu = new TextUnit(String.valueOf(++tuId));
 						tu.setSourceContent(new TextFragment(text));
 						tu.setIsReferent(true);
 						tu.setMimeType(MIMETYPE);
 						tu.setType("x-"+qualName);
 						queue.add(new Event(EventType.TEXT_UNIT, tu));
 						tmp.append(String.format(" %s=\"", qualName));
 						skel.append(tmp.toString());
 						skel.addReference(tu);
 						tmp.setLength(0); // Reset buffer
 						tmp.append("\""); // End of attribute value
 						continue; // Next attribute
 					}
 					// Else: fall thru
 				}
 				// Else: fall thru
 			}
 			// Not translatable
 			tmp.append(String.format(" %s=\"%s\"", qualName,
 				Util.escapeToXML(reader.getAttributeValue(i), 3, false, null)));
 		}
 
 		tmp.append(">");
 		if ( inSkeleton ) {
 			skel.append(tmp.toString());
 		}
 		return tmp.toString();
 	}
 	
 	private boolean hasTrueText (String text) {
 		if ( Util.isEmpty(text) ) return false;
 		for ( int i=0; i<text.length(); i++ ) {
 			if ( Character.isLetter(text.charAt(i)) ) return true;
 		}
 		return false;
 	}
 	
 	private String buildEndTag (String name) {
 		return "</" + name + ">";
 	}
 	
 	private String makePrintName () {
 		String prefix = reader.getPrefix();
 		if (( prefix == null ) || ( prefix.length()==0 )) {
 			return reader.getLocalName();
 		}
 		// Else: with a prefix
 		return prefix + ":" + reader.getLocalName();
 	}
 	
 	private void processStartElement () throws XMLStreamException {
 		String name = makePrintName();
 		if ( toExtract.containsKey(name) ) {
 			if (( context.size() > 1 ) || ( subFlow.contains(name) )) { // Use nested mode
 				// Create the new id for the new sub-flow
 				String id = String.valueOf(++tuId);
 				// Add the reference to the current context
 				if ( context.peek().extract ) {
 					Code code = tf.append(TagType.PLACEHOLDER, name, TextFragment.makeRefMarker(id));
 					code.setReferenceFlag(true);
 				}
 				else { // Or in the skeleton
 					skel.addReference(tu);
 				}
 				// Create the new text unit
 				tu = new TextUnit(id);
 				// Set it as a referent, and set the info
 				tu.setIsReferent(true);
 				setTUInfo(name);
 				// Create the new fragment and skeleton
 				// And add the start tag of the sub-flow to the new skeleton
 				tf = new TextFragment();
 				skel = new GenericSkeleton();
 				buildStartTag(name, true);
 				// Set the new variables are the new context
 				context.push(new Context(name, true));
 				context.peek().setVariables(tf, skel, tu);
 			}
 			else { // Not nested
 				// Send document-part if there is a non-whitespace skeleton
 				if ( !skel.isEmpty(true) ) {
 					DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
 					dp.setSkeleton(skel);
 					queue.add(new Event(EventType.DOCUMENT_PART, dp));
 					skel = new GenericSkeleton(); // Start new skeleton 
 				}
 				// Start the new text-unit (append it to skel)
 				buildStartTag(name, true);
 				//TODO: need a way to set the TextUnit's name/id/restype/etc.
 				tu = new TextUnit(null); // ID set only if needed
 				setTUInfo(name);
 				context.push(new Context(name, true));
 				context.peek().setVariables(tf, skel, tu);
 			}
 		}
 		else if ( subFlow.contains(name) ) { // Is it a sub-flow (not extractable)
 			// Create the new id for the new sub-flow
 			String id = String.valueOf(++tuId);
 			// Add the reference to the current context
 			if ( context.peek().extract ) {
 				Code code = tf.append(TagType.PLACEHOLDER, name, TextFragment.makeRefMarker(id));
 				code.setReferenceFlag(true);
 				// Create the new text unit
 				tu = new TextUnit(id);
 				// Set it as a referent, and set the info
 				tu.setIsReferent(true);
 				setTUInfo(name);
 				// Create the new fragment and skeleton
 				tf = new TextFragment();
 				// Create the skeleton and add the start tag
 				skel = new GenericSkeleton();
 				buildStartTag(name, true);
 				// Add the start-tag to the new context 
 				// Set the new variables are the new context
 				context.push(new Context(name, true));
 				context.peek().setVariables(tf, skel, tu);
 			}
 			else { // Or in the skeleton
 				// Send the exeisting skeleton if needed
 				if ( !skel.isEmpty(true) ) {
 					DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
 					dp.setSkeleton(skel);
 					queue.add(new Event(EventType.DOCUMENT_PART, dp));
 					skel = new GenericSkeleton(); // Start new skeleton 
 				}
 				// Create the new text unit
 				tu = new TextUnit(id);
 				// Set it as a referent, and set the info
 				//tu.setIsReferent(true);
 				setTUInfo(name);
 				// Create the new fragment and skeleton
 				tf = new TextFragment();
 				// Create the skeleton and add the start tag
 				skel = new GenericSkeleton();
 				buildStartTag(name, true);
 				// Add the start-tag to the new context 
 				// Set the new variables are the new context
 				context.push(new Context(name, true));
 				context.peek().setVariables(tf, skel, tu);
 			}
 		}
 		else if ( context.peek().extract && name.equals("text:s") ) {
 			String tmp = reader.getAttributeValue(NSURI_TEXT, "c");
 			if ( tmp != null ) {
 				int count = Integer.valueOf(tmp);
 				for ( int i=0; i<count; i++ ) {
 					tf.append(" ");
 				}
 			}
 			else tf.append(" "); // Default=1
 			reader.nextTag(); // Eat the end-element event
 		}
 		else if ( context.peek().extract && name.equals("text:tab") ) {
 			tf.append("\t");
 			reader.nextTag(); // Eat the end-element event
 		}
 		else if ( context.peek().extract && name.equals("text:line-break") ) {
 			tf.append(new Code(TagType.PLACEHOLDER, "lb", "<text:line-break/>"));
 			reader.nextTag(); // Eat the end-element event
 		}
 		else {
 			if ( context.peek().extract ) {
 				if ( name.equals("text:a") ) processStartALink(name);
 				else if ( toProtect.contains(name) ) processReadOnlyInlineElement(name);
 				else tf.append(new Code(TagType.OPENING, name, buildStartTag(name, false)));
 			}
 			else { // Append to skel
 				buildStartTag(name, true);
 			}
 		}
 	}
 
 	private void processStartALink (String name) {
 		String data = buildStartTag(name, false);
 		String href = reader.getAttributeValue(NSURI_XLINK, "href");
 		if ( href != null ) {
 			//TODO: set the property, but where???
 		}
 		tf.append(new Code(TagType.OPENING, name, data));
 	}
 	
 	private void processReadOnlyInlineElement (String name) throws XMLStreamException {
 		StringBuilder tmp = new StringBuilder(buildStartTag(name, false));
 		while ( true ) {
 			switch ( reader.next() ) {
 			case XMLStreamConstants.CHARACTERS:
 				tmp.append(reader.getText());
 				break;
 			case XMLStreamConstants.START_ELEMENT:
 				tmp.append(buildStartTag(makePrintName(), false));
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				String tmpName = makePrintName();
 				tmp.append(buildEndTag(tmpName));
 				if ( tmpName.equals(name) ) {
 					tf.append(new Code(TagType.PLACEHOLDER, name, tmp.toString()));
 					return;
 				}
 				break;
 			case XMLStreamConstants.COMMENT:
 				tmp.append("<!--" + reader.getText() + "-->");
 				break;
 			case XMLStreamConstants.PROCESSING_INSTRUCTION:
 				tmp.append("<?" + reader.getPITarget() + " "
 					+ reader.getPIData() + "?>");
 				break;
 			case XMLStreamConstants.START_DOCUMENT:
 			case XMLStreamConstants.END_DOCUMENT:
 				// Should not occur
 				throw new OkapiIllegalFilterOperationException("Invalid start or end document detected while processing inline element.");
 			}
 		}		
 	}
 
 	//TODO
 /*	private int optimizeFront (TextFragment frag) {
 		int trace = 0;
 		String text = frag.getCodedText();
 		char ch;
 		for ( int i=0; i<text.length(); i++ ) {
 			ch = text.charAt(i);
 			if ( TextFragment.isMarker(ch) ) {
 				
 				i++;
 			}
 			else if ( !Character.isWhitespace(ch) ) {
 				if ( trace == 0 ) {
 					if ( i > 0 ) return i;
 					else return -1; // Can't optimize
 				}
 				else return -1;
 			}
 		}
 		return text.length()+1; // No text, only codes and/or whitespace
 	}
 */	
 	private void addTU (String name) {
 		// Send a document part if there is no content
 		// But if it's in nested context, the parent is already referring to tu, and
 		// changing that reference to dp is hard, so we just send an empty tu
 		if ( tf.isEmpty() && ( context.size() < 3 )) {
 			DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false);
 			skel.append(buildEndTag(name)+lineBreak);
 			dp.setSkeleton(skel);
 			queue.add(new Event(EventType.DOCUMENT_PART, dp));
 		}
 		else { // Else: Send a text unit
 			// Optimize the content to remove extra code that could be outside the text
 			//TODO
 			
 			skel.addContentPlaceholder(tu);
 			if ( tu.getId() == null ) tu.setId(String.valueOf(++tuId));
 			tu.setSourceContent(tf);
 			tu.setSkeleton(skel);
 			tu.setMimeType(MIMETYPE);
 			// Add closing tag to the fragment or skeleton
 			// Add line break because ODF files don't have any
 			// They are needed for example in RTF output
 			if ( tu.isReferent() ) skel.append(buildEndTag(name));
 			else skel.append(buildEndTag(name)+lineBreak);
 			queue.add(new Event(EventType.TEXT_UNIT, tu));
 		}
 	}
 	
 	// Return true when it's ready to send an event
 	private boolean processEndElement () {
 		String name = makePrintName();
 		if ( context.peek().extract && name.equals(context.peek().name) ) {
 			if ( context.size() > 2 ) { // Is it a nested TU
 				// Add it to the queue
 				addTU(name);
 				context.pop();
 				// Reset the current variable to the correct context
 				tf = context.peek().tf;
 				tu = context.peek().tu;
 				skel = context.peek().skel;
 				// No trigger of the events yet
 				return false;
 			}
 			else { // Not embedded, pop first
 				context.pop();
 				addTU(name);
 				// Trigger the events to be sent
 				return true;
 			}
 		}
 		else {
 			if ( context.peek().extract ) {
 				tf.append(new Code(TagType.CLOSING, name, buildEndTag(name)));
 			}
 			else {
 				skel.append(buildEndTag(name));
 				// Add extra line for some element to make things
 				// more readable in plain-text format
 				if ( name.equals("style:style")
 					|| ( name.equals("text:list-style"))
 					|| ( name.equals("draw:frame"))
 					|| ( name.equals("text:list"))
 					|| ( name.equals("text:list-item")) ) {
 					skel.append(lineBreak);
 				}
 			}
 		}
 		return false;
 	}
 
 	private void applyParameters () {
 		// Update the driver lists if needed
 		if ( toProtect.contains(TEXT_BOOKMARK_REF) ) {
 			if ( params.extractReferences ) {
 				toProtect.remove(TEXT_BOOKMARK_REF);
 			}
 		}
 		else { // Not protected 
 			if ( !params.extractReferences ) {
 				toProtect.add(TEXT_BOOKMARK_REF);
 			}
 		}
 		if ( toProtect.contains(OFFICE_ANNOTATION) ) {
 			if ( params.extractReferences ) {
 				toProtect.remove(OFFICE_ANNOTATION);
 			}
 		}
 		else { // Not protected 
 			if ( !params.extractReferences ) {
 				toProtect.add(OFFICE_ANNOTATION);
 			}
 		}
 		
 	}
 
 }
