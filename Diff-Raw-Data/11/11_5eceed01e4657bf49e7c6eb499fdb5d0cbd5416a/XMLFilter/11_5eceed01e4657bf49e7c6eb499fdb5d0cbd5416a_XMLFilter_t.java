 /*===========================================================================
   Copyright (C) 2008-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.xml;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.DefaultEntityResolver;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IdGenerator;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filters.InlineCodeFinder;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentType;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.its.IProcessor;
 import org.w3c.its.ITSEngine;
 import org.w3c.its.ITraversal;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 @UsingParameters(Parameters.class)
 public class XMLFilter implements IFilter {
 
 	private String docName;
 	private String encoding;
 	private LocaleId srcLang;
 	private String lineBreak;
 	private Document doc;
 	private ITraversal trav;
 	private LinkedList<Event> queue;
 	private int tuId;
 	private IdGenerator otherId;
 	private TextFragment frag;
 	private GenericSkeleton skel;
 	private Stack<ContextItem> context;
 	private boolean canceled;
 	private Parameters params;
 	private boolean hasUTF8BOM;
 	private EncoderManager encoderManager;
 
 	public XMLFilter () {
 		params = new Parameters();
 	}
 	
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 	}
 
 	public ISkeletonWriter createSkeletonWriter () {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	public String getName () {
 		return "okf_xml";
 	}
 	
 	public String getDisplayName () {
 		return "XML Filter";
 	}
 
 	public String getMimeType () {
 		return MimeTypeMapper.XML_MIME_TYPE;
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MimeTypeMapper.XML_MIME_TYPE,
 			getClass().getName(),
 			"Generic XML",
 			"Configuration for generic XML documents (default ITS rules).",
 			null,
 			".xml;"));
 		list.add(new FilterConfiguration(getName()+"-resx",
 			MimeTypeMapper.XML_MIME_TYPE,
 			getClass().getName(),
 			"RESX",
 			"Configuration for Microsoft RESX documents (without binary data).",
 			"resx.fprm",
 			".resx;"));
 		list.add(new FilterConfiguration(getName()+"-MozillaRDF",
 			MimeTypeMapper.XML_MIME_TYPE,
 			getClass().getName(),
 			"Mozilla RDF",
 			"Configuration for Mozilla RDF documents.",
 			"MozillaRDF.fprm",
 			".rdf;"));
 		list.add(new FilterConfiguration(getName()+"-JavaProperties",
 			MimeTypeMapper.XML_MIME_TYPE,
 			getClass().getName(),
 			"Java Properties XML",
 			"Configuration for Java Properties files in XML.",
 			"JavaProperties.fprm"));
 		list.add(new FilterConfiguration(getName()+"-AndroidStrings",
 			MimeTypeMapper.XML_MIME_TYPE,
 			getClass().getName(),
 			"Android Strings",
 			"Configuration for Android Strings XML documents.",
 			"AndroidStrings.fprm"));
 		list.add(new FilterConfiguration(getName()+"-WixLocalization",
 			MimeTypeMapper.XML_MIME_TYPE,
 			getClass().getName(),
 			"WiX Localization",
 			"Configuration for WiX (Windows Installer XML) Localization files.",
 			"WixLocalization.fprm",
 			".wxl;"));
 		return list;
 	}
 	
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
 		}
 		return encoderManager;
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return (queue != null);
 	}
 
 	public Event next () {
 		if ( canceled ) {
 			queue = null;
 			return new Event(EventType.CANCELED);
 		}
 		if ( queue == null ) return null;
 
 		// Process queue if it's not empty yet
 		while ( true ) {
 			if ( queue.size() > 0 ) {
 				Event event = queue.poll();
 				if ( event.getEventType() == EventType.END_DOCUMENT ) {
 					queue = null;
 				}
 				return event;
 			}
 
 			// Process the next item, filling the queue
 			process();
 			// Ensure no infinite loop
 			if ( queue.size() == 0 ) return null;
 		}
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close();
 		// Initializes the variables
 		canceled = false;
 		tuId = 0;
 		otherId = new IdGenerator(null, "o");
 
 		// Create the document builder factory
 		DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
 		fact.setNamespaceAware(true);
 		fact.setValidating(false);
 		// Expand entity references only if we do not protect them
 		// "Expand entity" means don't have ENTITY_REFERENCE
 		fact.setExpandEntityReferences(!params.protectEntityRef);
 		
 		// Create the document builder
 		DocumentBuilder docBuilder;
 		try {
 			docBuilder = fact.newDocumentBuilder();
 		}
 		catch (ParserConfigurationException e) {
 			throw new OkapiIOException(e);
 		}
 		//TODO: Do this only as an option
 		// Avoid DTD declaration
 		docBuilder.setEntityResolver(new DefaultEntityResolver());
 
 		input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
 		BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 		detector.detectBom();
 		
 		if ( detector.isAutodetected() ) {
 			encoding = detector.getEncoding();
 			//--Start workaround issue with XML Parser
 			// "UTF-16xx" are not handled as expected, using "UTF-16" alone 
 			// seems to resolve the issue.
 			if (( encoding.equals("UTF-16LE") ) || ( encoding.equals("UTF-16BE") )) {
 				encoding = "UTF-16";
 			}
 			//--End workaround
 			input.setEncoding(encoding);
 		}
 		
 		try {
 			InputSource is = new InputSource(input.getStream());
 			//is.setEncoding(input.getEncoding());
 			doc = docBuilder.parse(is);
 		}
 		catch ( SAXException e ) {
 			throw new OkapiIOException("Error when parsing the document.\n"+e.getMessage(), e);
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException("Error when reading the document.\n"+e.getMessage(), e);
 		}
 
 		encoding = doc.getXmlEncoding();
 		if ( encoding == null ) {
 			encoding = detector.getEncoding();
 		}
 		srcLang = input.getSourceLocale();
 		if ( srcLang == null ) throw new NullPointerException("Source language not set.");
 		hasUTF8BOM = detector.hasUtf8Bom();
 		lineBreak = detector.getNewlineType().toString();
 		if ( input.getInputURI() != null ) {
 			docName = input.getInputURI().getPath();
 		}
 		
 		if ( params.useCodeFinder ) {
 			params.codeFinder.compile();
 		}
 
 		// Create the ITS engine
 		ITSEngine itsEng;
 		itsEng = new ITSEngine(doc, input.getInputURI());
 		// Load the parameters file if there is one
 		if ( params != null ) {
 			if ( params.getDocument() != null ) {
 				itsEng.addExternalRules(params.getDocument(), params.getURI());
 			}
 		}
 		
 		// Apply the all rules (external and internal) to the document
 		itsEng.applyRules(IProcessor.DC_TRANSLATE | IProcessor.DC_LANGINFO 
 			| IProcessor.DC_LOCNOTE | IProcessor.DC_WITHINTEXT);
 		
 		trav = itsEng;
 		trav.startTraversal();
 		context = new Stack<ContextItem>();
 		
 		// Set the start event
 		queue = new LinkedList<Event>();
 
 		StartDocument startDoc = new StartDocument(otherId.createId());
 		startDoc.setName(docName);
 		String realEnc = doc.getInputEncoding();
 		if ( realEnc != null ) encoding = realEnc;
 		startDoc.setEncoding(encoding, hasUTF8BOM);
 		startDoc.setLineBreak(lineBreak);
 		startDoc.setLocale(srcLang);
 
 		// Default quote mode == 3
 		params.quoteModeDefined = true;
 		params.quoteMode = 3; // quote is escaped, apos is not
 		// Change the escapeQuotes option depending on whether translatable attributes rule
 		// was triggered or not
 		if ( !itsEng.getTranslatableAttributeRuleTriggered() ) {
 			// Allow to not escape quotes only if there is no translatable attributes
 			if ( !params.escapeQuotes ) {
 				params.quoteModeDefined = true;
 				params.quoteMode = 0; // quote and apos not escaped
 			}
 		}
 		startDoc.setFilterParameters(params);
 		
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setType(MimeTypeMapper.XML_MIME_TYPE);
 		startDoc.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
 
 		// Add the XML declaration
 		skel = new GenericSkeleton();
 		if ( !params.omitXMLDeclaration ) {
 			skel.add("<?xml version=\"" + doc.getXmlVersion() + "\"");
 			skel.add(" encoding=\"");
 			skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
 			skel.add("\"");
 			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
 			if ( doc.getXmlStandalone() ) skel.add(" standalone=\"yes\"");
 			skel.add("?>"+lineBreak);
 		}
 
 		// Add the DTD if needed
 		DocumentType dt = doc.getDoctype();
 		if ( dt != null ) {
 			rebuildDocTypeSection(dt);
 		}
 		
 		startDoc.setSkeleton(skel);
 		// Put the start document in the queue
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 	}
 
 	private void rebuildDocTypeSection (DocumentType dt) {
 		StringBuilder tmp = new StringBuilder();
 		// Set the start syntax
 		if ( dt.getPublicId() != null ) {
 			tmp.append(String.format("<!DOCTYPE %s PUBLIC \"%s\" \"%s\"",
 				dt.getName(),
 				dt.getPublicId(),
 				dt.getSystemId()));
 		}
 		else if ( dt.getSystemId() != null ) {
 			tmp.append(String.format("<!DOCTYPE %s SYSTEM \"%s\"",
 				dt.getName(),
 				dt.getSystemId()));
 		}
 		else if ( dt.getInternalSubset() != null ) {
 			tmp.append(String.format("<!DOCTYPE %s",
 				dt.getName()));
 		}
 		
 		// Add the internal sub-set if there is any
 		if ( dt.getInternalSubset() != null ) {
 			tmp.append(" [");
 			tmp.append(dt.getInternalSubset().replace("\n", lineBreak));
 			tmp.append("]");
 		}
 		
 		if ( tmp.length() > 0 ) {
 			tmp.append(">"+lineBreak);
 			skel.add(tmp.toString());
 		}
 	}
 	
 	private void process () {
 		Node node;
 		frag = null;
 		skel = new GenericSkeleton();
 		
 		if ( context.size() > 0 ) {
 			// If we are within an element, reset the fragment to append to it
 			if ( context.peek().translate ) { // The stack is up-to-date already
 				frag = new TextFragment();
 			}
 		}
 		
 		while ( true ) {
 			node = trav.nextNode();
 			if ( node == null ) { // No more node: we stop
 				Ending ending = new Ending(otherId.createId());
 				if (( skel != null ) && ( !skel.isEmpty() )) {
 					ending.setSkeleton(skel);
 				}
 				queue.add(new Event(EventType.END_DOCUMENT, ending));
 				return;
 			}
 			
 			// Else: valid node
 			switch ( node.getNodeType() ) {
 			case Node.CDATA_SECTION_NODE:
 				if ( frag == null ) {
 					skel.append(buildCDATA(node));
 				}
 				else {
 					if ( trav.translate() ) {
 						frag.append(node.getNodeValue());
 					}
 					else {
 						frag.append(TagType.PLACEHOLDER, null, buildCDATA(node));
 					}
 				}
 				break;
 
 			case Node.TEXT_NODE:
 				if ( frag == null ) {//TODO: escape unsupported chars
 					skel.append(Util.escapeToXML(
 						node.getNodeValue().replace("\n", lineBreak), 0, false, null));
 				}
 				else {
 					if ( trav.translate() ) {
 						if ( params.lineBreakAsCode ) escapeAndAppend(frag, node.getNodeValue());
 						else frag.append(node.getNodeValue());
 					}
 					else {//TODO: escape unsupported chars
 						frag.append(TagType.PLACEHOLDER, null, Util.escapeToXML(
 							node.getNodeValue().replace("\n", (params.escapeLineBreak ? "&#10;" : lineBreak)), 0, false, null));
 					}
 				}
 				break;
 				
 			case Node.ELEMENT_NODE:
 				if ( processElementTag(node) ) return;
 				break;
 				
 			case Node.PROCESSING_INSTRUCTION_NODE:
 				if ( frag == null ) {
 					skel.add(buildPI(node));
 				}
 				else {
 					frag.append(TagType.PLACEHOLDER, null, buildPI(node));
 				}
 				break;
 				
 			case Node.COMMENT_NODE:
 				if ( frag == null ) {
 					skel.add(buildComment(node));
 				}
 				else {
 					frag.append(TagType.PLACEHOLDER, null, buildComment(node));
 				}
 				break;
 				
 			case Node.ENTITY_REFERENCE_NODE:
 				// This get called only if params.protectEntityRef is true
 				// Note: &lt;, etc. are not reported as entity references 
 				if ( !trav.backTracking() ) {
 					if ( frag == null ) {
 						skel.add("&"+node.getNodeName()+";");
 					}
 					else {
 						frag.append(TagType.PLACEHOLDER, Code.TYPE_REFERENCE, "&"+node.getNodeName()+";");
 					}
 					// Some parsers provide the expanded content along with the reference node
 					// If so, this needs to be swallowed (note that it can be nested)
 					if ( node.hasChildNodes() ) {
 						Node thisNode = node;
 						do { // Read all the way down and back
 							node = trav.nextNode();
 						} while ( node != thisNode );
 					}
 				}
 				// Else: Do not save the entity reference
 				// Nothing to do, the content will be handled by TEXT_NODE	
 				break;
 
 			case Node.DOCUMENT_TYPE_NODE:
 				// Handled in the start document process
 				break;
 			case Node.NOTATION_NODE:
 				//TODO: handle notation nodes
 				break;
 			case Node.ENTITY_NODE:
 				//TODO: handle entity nodes
 				break;
 			}
 		}
 	}
 
 	private void escapeAndAppend (TextFragment frag,
 		String text) 
 	{
 		for ( int i=0; i<text.length(); i++ ) {
 			if ( text.charAt(i) == '\n' ) {
 				frag.append(TagType.PLACEHOLDER, "lb", "&#10;");
 			}
 			else {
 				frag.append(text.charAt(i)); 
 			}
 		}
 	}
 	
 	private void addStartTagToSkeleton (Node node) {
 		StringBuilder tmp = new StringBuilder();
 		tmp.append("<"
 			+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
 			+ node.getLocalName());
 		if ( node.hasAttributes() ) {
 			NamedNodeMap list = node.getAttributes();
 			Attr attr;
 			for ( int i=0; i<list.getLength(); i++ ) {
 				attr = (Attr)list.item(i);
 				if ( !attr.getSpecified() ) continue; // Skip auto-attributes
 				tmp.append(" "
 					+ ((attr.getPrefix()==null) ? "" : attr.getPrefix()+":")
 					+ attr.getLocalName() + "=\"");
 				// Extract if needed
 				if (( trav.translate(attr) ) && ( attr.getValue().length() > 0 )) {
 					// Store the text part and reset the buffer
 					skel.append(tmp.toString());
 					tmp.setLength(0);
 					// Create the TU
 					addAttributeTextUnit(attr, true);
 					tmp.append("\"");
 				}
 				else if ( attr.getName().equals("xml:lang") ) {
 					//String x = attr.getValue();
 					//TODO: handle xml:lang
 					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
 						+ "\"");
 				}
 				else { //TODO: escape unsupported chars
 					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
 						+ "\"");
 				}
 			}
 		}
 		if ( !node.hasChildNodes() ) tmp.append("/");
 		tmp.append(">");
 		skel.append(tmp.toString());
 	}
 
 	private void addStartTagToFragment (Node node) {
 		StringBuilder tmp = new StringBuilder();
 		String id = null;
 		tmp.append("<"
 			+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
 			+ node.getLocalName());
 		if ( node.hasAttributes() ) {
 			NamedNodeMap list = node.getAttributes();
 			Attr attr;
 			for ( int i=0; i<list.getLength(); i++ ) {
 				attr = (Attr)list.item(i);
 				if ( !attr.getSpecified() ) continue; // Skip auto-attributes
 				tmp.append(" "
 					+ ((attr.getPrefix()==null) ? "" : attr.getPrefix()+":")
 					+ attr.getLocalName() + "=\"");
 				// Extract if needed
 				if (( trav.translate(attr) ) && ( attr.getValue().length() > 0 )) {
 					id = addAttributeTextUnit(attr, false);
 					tmp.append(TextFragment.makeRefMarker(id));
 					tmp.append("\"");
 				}
 				else if ( attr.getName().equals("xml:lang") ) { // xml:lang
 					//TODO
 					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
 						+ "\"");
 				}
 				else { //TODO: escape unsupported chars
 					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
 						+ "\"");
 				}
 			}
 		}
 		if ( !node.hasChildNodes() ) tmp.append("/");
 		tmp.append(">");
 		// Set the inline code
 		Code code = frag.append((node.hasChildNodes() ? TagType.OPENING : TagType.PLACEHOLDER),
 			node.getLocalName(), tmp.toString());
 		code.setReferenceFlag(id!=null); // Set reference flag if we created TU(s)
 	}
 
 	private String addAttributeTextUnit (Attr attr,
 		boolean addToSkeleton)
 	{
 		String id = String.valueOf(++tuId);
 		ITextUnit tu = new TextUnit(id, attr.getValue(), true, MimeTypeMapper.XML_MIME_TYPE);
 
 		// Deal with inline codes if needed
 		if ( params.useCodeFinder ) {
 			TextFragment tf = tu.getSource().getFirstContent();
 			params.codeFinder.process(tf);
 			// Escape inline code content
 			List<Code> codes = tf.getCodes();
 			for ( Code code : codes ) {
 				// Escape the data of the new inline code (and only them)
 				if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) { 
 					code.setData(Util.escapeToXML(code.getData(), 0, params.escapeGT, null));
 				}
 			}
 		}
 		
 		queue.add(new Event(EventType.TEXT_UNIT, tu));
 		if ( addToSkeleton ) skel.addReference(tu);
 		return id;
 	}
 	
 	private String buildEndTag (Node node) {
 		if ( node.hasChildNodes() ) {
 			return "</"
 				+ ((node.getPrefix()==null) ? "" : node.getPrefix()+":")
 				+ node.getLocalName() + ">";
 		}
 		else { // Start tag was set as an empty element
 			return "";
 		}
 	}
 	
 	private String buildPI (Node node) {
 		// Do not escape PI content
 		return "<?" + node.getNodeName() + " " + node.getNodeValue() + "?>";
 	}
 	
 	private String buildCDATA (Node node) {
 		// Do not escape CDATA content
 		return "<![CDATA[" + node.getNodeValue().replace("\n", lineBreak) + "]]>";
 	}
 	
 	private String buildComment (Node node) {
 		// Do not escape comments
 		return "<!--" + node.getNodeValue().replace("\n", lineBreak) + "-->";
 	}
 
 	/**
 	 * Processes the start or end tag of an element node.
 	 * @param node Node to process.
 	 * @return True if we need to return, false to continue processing.
 	 */
 	private boolean processElementTag (Node node) {
 		if ( trav.backTracking() ) {
 			if ( frag == null ) { // Not an extraction: in skeleton
 				skel.add(buildEndTag(node));
 				if ( node.isSameNode(context.peek().node) ) context.pop();
 				if ( isContextTranslatable() ) { // We are after non-translatable withinText='no', check parent again.
 					frag = new TextFragment();
 				}
 			}
 			else { // Else we are within an extraction
 				if ( node.isSameNode(context.peek().node) ) { // End of possible text-unit
 					return addTextUnit(node, true);
 				}
 				else { // Within text
 					frag.append(TagType.CLOSING, node.getLocalName(), buildEndTag(node));
 				}
 			}
 		}
 		else { // Else: Start tag
 			switch ( trav.getWithinText() ) {
 			case ITraversal.WITHINTEXT_YES:
 			case ITraversal.WITHINTEXT_NESTED: //TODO: deal with nested elements
 				if ( frag == null ) { // Not yet in extraction
					//// Strange case: inline without parent???
					////TODO: do something about this, warning?
					//assert(false);
					// case of inline within non-translatable
					addStartTagToSkeleton(node);
					if ( node.hasChildNodes() ) {
						context.push(new ContextItem(node, trav));
					}
 				}
 				else { // Already in extraction
 					//frag.append(TagType.OPENING, node.getLocalName(), buildStartTag(node));
 					addStartTagToFragment(node);
 				}
 				break;
 			default: // Not within text
 				if ( frag == null ) { // Not yet in extraction
 					addStartTagToSkeleton(node);
 					if ( trav.translate() ) {
 						frag = new TextFragment();
 					}
 					if ( node.hasChildNodes() ) {
 						context.push(new ContextItem(node, trav));
 					}
 				}
 				else { // Already in extraction
 					// Queue the current item
 					addTextUnit(node, false);
 					addStartTagToSkeleton(node);
 					// And create a new one
 					if ( trav.translate() ) {
 						frag = new TextFragment();
 					}
 					if ( node.hasChildNodes() ) {
 						context.push(new ContextItem(node, trav));
 					}
 				}
 				break;
 			}
 		}
 		return false;
 	}
 
 	private boolean isContextTranslatable () {
 		if ( context.size() == 0 ) return false;
 		return context.peek().translate;
 	}
 	
 	/**
 	 * Adds a text unit to the queue if needed.
 	 * @param node The current node.
 	 * @param popStack True to pop the stack, false to leave the stack alone.
 	 * @return True if a text unit was added to the queue, false otherwise.
 	 */
 	private boolean addTextUnit (Node node,
 		boolean popStack)
 	{
 		// Extract if there is some text, or if there is code and we always extract codes
 		boolean extract = frag.hasText(false)
 			|| ( params.extractIfOnlyCodes && frag.hasCode() );
 		
 		if ( extract ) {
 			// Deal with inline codes if needed
 			if ( params.useCodeFinder ) {
 				params.codeFinder.process(frag);
 				// Escape inline code content
 				List<Code> codes = frag.getCodes();
 				for ( Code code : codes ) {
 					// Escape the data of the new inline code (and only them)
 					if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) { 
 						code.setData(Util.escapeToXML(code.getData(), 0, params.escapeGT, null));
 					}
 				}
 			}
 		
 			// Update the flag after the new codes
 			extract = frag.hasText(false)
 				|| ( params.extractIfOnlyCodes && frag.hasCode() );
 		}
 		
 		if ( !extract ) {
 			if ( !frag.isEmpty() ) { // Nothing but white spaces
 				skel.add(frag.toText().replace("\n", (params.escapeLineBreak ? "&#10;" : lineBreak))); // Pass them as skeleton
 			}
 			frag = null;
 			if ( popStack ) {
 				context.pop();
 				skel.add(buildEndTag(node));
 			}
 			return false;
 		}
 
 		// Create the unit
 		ITextUnit tu = new TextUnit(String.valueOf(++tuId));
 		tu.setMimeType(MimeTypeMapper.XML_MIME_TYPE);
 		
 		tu.setSourceContent(frag);
 		
 		String locNote = context.peek().locNote;
 		if ( locNote != null ) {
 			//TODO: implement real notes
 			tu.setProperty(new Property(Property.NOTE, locNote));
 		}
 		
 		String id = context.peek().idPointer;
 		if ( id != null ) {
 			tu.setName(id);
 		}
 		
 		if ( context.peek().preserveWS ) {
 			tu.setPreserveWhitespaces(true);
 		}
 		else {
 			tu.setPreserveWhitespaces(false);
 			tu.getSource().unwrap(true, true);
 		}
 		
 		skel.addContentPlaceholder(tu);
 		if ( popStack ) {
 			context.pop();
 			skel.add(buildEndTag(node));
 		}
 		tu.setSkeleton(skel);
 		queue.add(new Event(EventType.TEXT_UNIT, tu));
 		frag = null;
 		skel = new GenericSkeleton();
 		return true;
 	}
 
 	//TODO: use sub-filter to extract a content
 //	private void processSubContent () {
 //		// The variable frag contains the content
 //		
 //	}
 
 }
