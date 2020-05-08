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
 
 package net.sf.okapi.filters.xliff;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.util.LinkedList;
 import java.util.Stack;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.IEncoder;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.StartSubDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.codehaus.stax2.XMLInputFactory2;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class XLIFFFilter implements IFilter {
 
 	public static final String    XML_NS_URI   = "http://www.w3.org/XML/1998/namespace";
 	
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 	
 	private boolean hasNext;
 	private XMLStreamReader reader;
 	private String docName;
 	private int tuId;
 	private int otherId;
 	private int groupId;
 	private String srcLang;
 	private String trgLang;
 	private LinkedList<Event> queue;
 	private boolean canceled;
 	private GenericSkeleton skel;
 	private TextUnit tu;
 	private Parameters params;
 	private boolean sourceDone;
 	private boolean targetDone;
 	private TextContainer content;
 	private String encoding;
 	private Stack<Integer> parentIds;
 	private AltTransAnnotation altTrans;
 	private Stack<Boolean> preserveSpaces;
 	
 	public XLIFFFilter () {
 		params = new Parameters();
 	}
 	
 	public void cancel () {
 		canceled = true;
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
 			throw new RuntimeException(e);
 		}
 	}
 
 	public String getName () {
 		return "XLIFFFilter";
 	}
 
 	public String getMimeType () {
 		return "text/x-xliff";
 	}
 
 	public IParameters getParameters () {
 		return params;
 	}
 
 	public boolean hasNext () {
 		return hasNext;
 	}
 
 	public Event next () {
 		try {
 			// Check for cancellation first
 			if ( canceled ) {
 				queue.clear();
 				queue.add(new Event(EventType.CANCELED));
 				hasNext = false;
 			}
 			
 			// Parse next if nothing in the queue
 			if ( queue.isEmpty() ) {
 				if ( !read() ) {
 					Ending ending = new Ending(String.valueOf(++otherId));
 					ending.setSkeleton(skel);
 					queue.add(new Event(EventType.END_DOCUMENT, ending));
 				}
 			}
 			
 			// Return the head of the queue
 			if ( queue.peek().getEventType() == EventType.END_DOCUMENT ) {
 				hasNext = false;
 			}
 			return queue.poll();
 		}
 		catch ( XMLStreamException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void open (InputStream input) {
 		try {
 			if ( srcLang == null ) throw new RuntimeException("Source language not set.");
 			if ( trgLang == null ) throw new RuntimeException("Target language not set.");
 			close();
 			canceled = false;
 
 			XMLInputFactory fact = XMLInputFactory.newInstance();
 			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
 			fact.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, true);
 			reader = fact.createXMLStreamReader(input);
 
 			//TODO: Need to auto-detect the encoding and update 'encoding' variable
 			// use reader.getCharacterEncodingScheme() ??? but start doc not reported
 			
 			preserveSpaces = new Stack<Boolean>();
 			preserveSpaces.push(false);
 			parentIds = new Stack<Integer>();
 			parentIds.push(0);
 			groupId = 0;
 			tuId = 0;
 			otherId = 0;
 			// Set the start event
 			hasNext = true;
 			queue = new LinkedList<Event>();
 			
 			StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 			startDoc.setName(docName);
 			startDoc.setEncoding(encoding, false); //TODO: UTF8 BOM detection
 			startDoc.setLanguage(srcLang);
 			startDoc.setFilterParameters(getParameters());
 			startDoc.setType("text/x-xliff");
 			startDoc.setMimeType("text/x-xliff");
 			startDoc.setIsMultilingual(true);
 			queue.add(new Event(EventType.START_DOCUMENT, startDoc));
 
 			// The XML declaration is not reported by the parser, so we need to
 			// create it as a document part when starting
 			skel = new GenericSkeleton();
 			startDoc.setProperty(new Property(IEncoder.PROP_ENCODING, encoding, false));
 			skel.append("<?xml version=\"1.0\" encoding=\"");
 			skel.addValuePlaceholder(startDoc, IEncoder.PROP_ENCODING, "");
 			skel.append("\"?>");
 			startDoc.setSkeleton(skel);
 		}
 		catch ( XMLStreamException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void open (CharSequence inputText) {
 		//TODO: Check for better solution, going from char to byte to read char is just not good
 		try {
 			open(new ByteArrayInputStream(inputText.toString().getBytes("UTF-8")));
 		}
 		catch (UnsupportedEncodingException e) {
 			throw new RuntimeException(e);
 		} 
 	}
 
 	public void open (URI inputURI) {
 		try {
 			docName = inputURI.getPath();
 			open(inputURI.toURL().openStream());
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void setOptions(String sourceLanguage,
 		String targetLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		srcLang = sourceLanguage;
 		trgLang = targetLanguage;
 		encoding = defaultEncoding;
 	}
 
 	public void setOptions(String sourceLanguage,
 		String defaultEncoding,
 		boolean generateSkeleton)
 	{
 		setOptions(sourceLanguage, null, defaultEncoding, generateSkeleton);
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	public ISkeletonWriter createSkeletonWriter() {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter());
 	}
 
 	private boolean read () throws XMLStreamException {
 		skel = new GenericSkeleton();
 		int eventType;
 		
 		while ( reader.hasNext() ) {
 			eventType = reader.next();
 			switch ( eventType ) {
 			case XMLStreamConstants.START_ELEMENT:
 				String name = reader.getLocalName();
 				if ( "trans-unit".equals(name) ) {
 					return processTransUnit();
 				}
 				else if ( "file".equals(name) ) {
 					return processStartFile();
 				}
 				else if ( "group".equals(name) ) {
 					if ( processStartGroup() ) return true;
 				}
 				else storeStartElement();
 				break;
 				
 			case XMLStreamConstants.END_ELEMENT:
 				storeEndElement();
 				if ( "file".equals(reader.getLocalName()) ) {
 					return processEndFile();
 				}
 				else if ( "group".equals(reader.getLocalName()) ) {
 					return processEndGroup();
 				}
 				break;
 				
 			case XMLStreamConstants.SPACE:
 			case XMLStreamConstants.CDATA:
 			case XMLStreamConstants.CHARACTERS: //TODO: escape unsupported chars
 				skel.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 				break;
 				
 			case XMLStreamConstants.COMMENT:
 				skel.append("<!--"+ reader.getText() + "-->");
 				break;
 				
 			case XMLStreamConstants.PROCESSING_INSTRUCTION:
 				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 				break;
 				
 			case XMLStreamConstants.DTD:
				//TODO: processDTD();
 				break;
 				
 			case XMLStreamConstants.ENTITY_REFERENCE:
 			case XMLStreamConstants.ENTITY_DECLARATION:
 			case XMLStreamConstants.NAMESPACE:
 			case XMLStreamConstants.NOTATION_DECLARATION:
 			case XMLStreamConstants.ATTRIBUTE:
 				break;
 			case XMLStreamConstants.START_DOCUMENT:
 				break;
 			case XMLStreamConstants.END_DOCUMENT:
 				break;
 			}
 		}
 		return false;
 	}
 
 	private boolean processStartFile () {
 		// Make a document part with skeleton between the previous event and now.
 		// Spaces can go with the file element to reduce the number of events.
 		// This allows to have only the file skeleton parts with the sub-document event
 		if ( !skel.isEmpty(true) ) {
 			DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 			skel = new GenericSkeleton(); // And create a new skeleton for the next event
 			queue.add(new Event(EventType.DOCUMENT_PART, dp));
 		}
 		
 		storeStartElement();
 		StartSubDocument startSubDoc = new StartSubDocument(String.valueOf(++otherId));
 		String tmp = reader.getAttributeValue("", "original");
 		if ( tmp == null ) throw new RuntimeException("Missing attribute 'original'.");
 		else startSubDoc.setName(tmp);
 		
 		// Check the source language
 		tmp = reader.getAttributeValue("", "source-language");
 		if ( tmp == null ) throw new RuntimeException("Missing attribute 'source-language'.");
 		if ( tmp.compareTo(srcLang) != 0 ) { // Warn about source language
 			logger.warn(String.format("The source language declared in <file> is '%s'.", tmp));
 		}
 		
 		// Check the target language
 		tmp = reader.getAttributeValue("", "target-language");
 		if ( tmp != null ) {
 			if ( tmp.equalsIgnoreCase(trgLang) ) { // Warn about target language
 				logger.warn(String.format("The target language declared in <file> is '%s'.", tmp));
 			}
 		}
 		
 		startSubDoc.setSkeleton(skel);
 		queue.add(new Event(EventType.START_SUBDOCUMENT, startSubDoc));
 		return true;
 	}
 
 	private boolean processEndFile () {
 		Ending ending = new Ending(String.valueOf(+otherId));
 		ending.setSkeleton(skel);
 		queue.add(new Event(EventType.END_SUBDOCUMENT, ending));
 		return true;
 	}
 	
 	private void storeStartElement () {
 		String prefix = reader.getPrefix();
 		if (( prefix == null ) || ( prefix.length()==0 )) {
 			skel.append("<"+reader.getLocalName());
 		}
 		else {
 			skel.append("<"+prefix+":"+reader.getLocalName());
 		}
 
 		int count = reader.getNamespaceCount();
 		for ( int i=0; i<count; i++ ) {
 			prefix = reader.getNamespacePrefix(i);
 			skel.append(String.format(" xmlns%s=\"%s\"",
 				((prefix!=null) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 		String attrName;
 		boolean ps = preserveSpaces.peek();
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			prefix = reader.getAttributePrefix(i);
 			attrName = String.format("%s%s",
 				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 				reader.getAttributeLocalName(i));
 			skel.append(String.format(" %s=\"%s\"", attrName,
 				reader.getAttributeValue(i)));
 			if ( attrName.equals("xml:space") ) {
 				ps = reader.getAttributeValue(i).equals("preserve");
 			}
 		}
 		skel.append(">");
 		preserveSpaces.push(ps);
 	}
 	
 	private void storeEndElement () {
 		String prefix = reader.getPrefix();
 		if (( prefix != null ) && ( prefix.length()>0 )) {
 			skel.append("</"+prefix+":"+reader.getLocalName()+">");
 		}
 		else {
 			skel.append("</"+reader.getLocalName()+">");
 		}
 		preserveSpaces.pop();
 	}
 
 	private boolean processTransUnit () {
 		try {
 			// Make a document part with skeleton between the previous event and now.
 			// Spaces can go with trans-unit to reduce the number of events.
 			// This allows to have only the trans-unit skeleton parts with the TextUnit event
 			if ( !skel.isEmpty(true) ) {
 				DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 				skel = new GenericSkeleton(); // And create a new skeleton for the next event
 				queue.add(new Event(EventType.DOCUMENT_PART, dp));
 			}
 			
 			// Process trans-unit
 			sourceDone = false;
 			targetDone = false;
 			altTrans = null;
 			tu = new TextUnit(String.valueOf(++tuId));
 			storeStartElement();
 
 			String tmp = reader.getAttributeValue("", "translate");
 			if ( tmp != null ) tu.setIsTranslatable(tmp.equals("yes"));
 
 			tmp = reader.getAttributeValue("", "id");
 			if ( tmp == null ) throw new RuntimeException("Missing attribute 'id'.");
 			tu.setId(tmp);
 			
 			tmp = reader.getAttributeValue("", "resname");
 			if ( tmp != null ) tu.setName(tmp);
 			else if ( params.fallbackToID ) {
 				tu.setName(tu.getId());
 			}
 
 			// Set restype (can be null)
 			tu.setType(reader.getAttributeValue("", "restype"));
 			
 			// Get the content
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				String name;
 				switch ( eventType ) {
 				case XMLStreamConstants.START_ELEMENT:
 					name = reader.getLocalName();
 					if ( "source".equals(name) ) {
 						storeStartElement();
 						processSource(false);
 						storeEndElement();
 					}
 					else if ( "target".equals(name) ) {
 						storeStartElement();
 						processTarget();
 						storeEndElement();
 					}
 					else if ( "seg-source".equals(name) ) {
 						storeStartElement();
 						processSource(true);
 						storeEndElement();
 					}
 					else if ( "note".equals(name) ) {
 						addTargetIfNeeded();
 						storeStartElement();
 						processNote();
 						storeEndElement();
 					}
 					else if ( "alt-trans".equals(name) ) {
 						addTargetIfNeeded();
 						storeStartElement();
 						processStartAltTrans();
 					}
 					else {
 						addTargetIfNeeded();
 						storeStartElement();
 					}
 					break;
 				
 				case XMLStreamConstants.END_ELEMENT:
 					name = reader.getLocalName();
 					addTargetIfNeeded();
 					if ( "trans-unit".equals(name) ) {
 						storeEndElement();
 						tu.setSkeleton(skel);
 						tu.setMimeType("text/xml");
 						queue.add(new Event(EventType.TEXT_UNIT, tu));
 						return true;
 					}
 					// Else: just store the end
 					storeEndElement();
 					break;
 				
 				case XMLStreamConstants.SPACE:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.CHARACTERS:
 					if ( !targetDone ) {
 						// Faster that separating XMLStreamConstants.SPACE
 						// from other data in the all process
 						tmp = reader.getText();
 						for ( int i=0; i<tmp.length(); i++ ) {
 							if ( !Character.isWhitespace(tmp.charAt(i)) ) {
 								addTargetIfNeeded();
 								break;
 							}
 						}
 					}
 					//TODO: escape unsupported chars
 					skel.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					break;
 					
 				case XMLStreamConstants.COMMENT:
 					addTargetIfNeeded();
 					skel.append("<!--"+ reader.getText() + "-->");
 					break;
 				
 				case XMLStreamConstants.PROCESSING_INSTRUCTION:
 					addTargetIfNeeded();
 					skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new RuntimeException(e);
 		}
 		return false;
 	}
 	
 	private void processSource (boolean isSegSource) {
 		TextContainer tc;
 		if ( sourceDone ) { // Case of an alt-trans entry
 			// Get the language
 			String lang = reader.getAttributeValue(XML_NS_URI, "lang");
 			if ( lang == null ) lang = srcLang; // Use default
 			// Get the text content
 			tc = processContent(isSegSource ? "seg-source" : "source", true);
 			// Put the source in the alt-trans annotation
 			if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 			// Store in altTrans only when we are witnin alt-trans
 			if ( altTrans != null ) {
 				if ( isSegSource ) {
 					// TODO: handle seg-source
 					//TODO: content of seg-source should be the one to use???
 					//TODO: what if they are different?
 				}
 				else {
 					altTrans.addNew(lang, tc);
 					altTrans.getEntry().setPreserveWhitespaces(preserveSpaces.peek());
 				}
 			}
 			else { // It's seg-source just after a <source> (not in alt-trans)
 				//TODO: Handle segmented content
 			}
 		}
 		else {
 			// Get the coord attribute if available
 			String tmp = reader.getAttributeValue("", "coord");
 			if ( tmp != null ) {
 				//TODO: Need a way to store and make modifiable property
 				tu.setSourceProperty(new Property("coord", tmp, true));
 			}
 
 			skel.addContentPlaceholder(tu);
 			tc = processContent(isSegSource ? "seg-source" : "source", false);
 			if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 			tu.setPreserveWhitespaces(preserveSpaces.peek());
 			tu.setSource(tc);
 			sourceDone = true;
 		}
 	}
 	
 	private void processTarget () {
 		TextContainer tc;
 		if ( targetDone ) { // Case of an alt-trans entry
 			// Get the language
 			String lang = reader.getAttributeValue(XML_NS_URI, "lang");
 			if ( lang == null ) lang = trgLang; // Use default
 			// Get the text content
 			tc = processContent("target", true);
 			// Put the target in the alt-trans annotation
 			if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 			altTrans.setTarget(lang, tc);
 			altTrans.getEntry().setPreserveWhitespaces(preserveSpaces.peek());
 		}
 		else {
 			// Get the state attribute if available
 			String tmp = reader.getAttributeValue("", "state");
 			if ( tmp != null ) {
 				//TODO: Need a way to store and make modifiable property
 				tu.setTargetProperty(trgLang, new Property("state", tmp, true));
 			}
 		
 			// Get the coord attribute if available
 			tmp = reader.getAttributeValue("", "coord");
 			if ( tmp != null ) {
 				//TODO: Need a way to store and make modifiable property
 				tu.setTargetProperty(trgLang, new Property("coord", tmp, true));
 			}
 
 			skel.addContentPlaceholder(tu, trgLang);
 			tc = processContent("target", false);
 			if ( !tc.isEmpty() ) {
 				//resource.needTargetElement = false;
 				if ( !preserveSpaces.peek() ) TextFragment.unwrap(tc.getContent());
 				tu.setPreserveWhitespaces(preserveSpaces.peek());
 				tu.setTarget(trgLang, tc);
 			}
 			targetDone = true;
 		}
 	}
 	
 	private void processStartAltTrans () {
 		// Creates an annotation for the alt-trans if there is none yet.
 		if ( altTrans == null ) {
 			altTrans = new AltTransAnnotation();
 			tu.setAnnotation(altTrans);
 		}
 	}
 	
 	private void addTargetIfNeeded () {
 		if ( targetDone ) return; // Nothing to add
 		// If the target language is the same as the source, we should not create new <target>
 		if ( srcLang.equalsIgnoreCase(trgLang) ) return;
 		//Else: this trans-unit has no target, we add it here in the skeleton
 		// so we can merge target data in it when writing out the skeleton
 		skel.append(String.format("<target xml:lang=\"%s\">", trgLang));
 		skel.addContentPlaceholder(tu, trgLang);
 		skel.append("</target>");
 		skel.append("\n"); // TODO: use the line-break type of the original file
 		targetDone = true;
 	}
 	
 	/**
 	 * Processes a segment content.
 	 * @param tagName The name of the element content that is being processed.
 	 * @param store True if the data must be stored in the skeleton.
 	 * This is used to merge later on.
 	 * @param inlineCodes Array where to save the in-line codes. Do not save if this parameter
 	 * is set to null.
 	 * @return A new TextContainer object with the parsed content.
 	 */
 	private TextContainer processContent (String tagName,
 		boolean store)
 	{
 		try {
 			content = new TextContainer();
 			int id = 0;
 			Stack<Integer> idStack = new Stack<Integer>();
 			idStack.push(id);
 			int eventType;
 			String name;
 			Code code;
 			
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					content.append(reader.getText());
 					if ( store ) { //TODO: escape unsupported chars
 						skel.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					}
 					break;
 		
 				case XMLStreamConstants.END_ELEMENT:
 					name = reader.getLocalName();
 					if ( name.equals(tagName) ) {
 						return content;
 					}
 					else if ( name.equals("g") || name.equals("mrk") ) { //TODO: || name.equals("sub") ) {
 						if ( store ) storeEndElement();
 						code = content.append(TagType.CLOSING, name, name); 
 						idStack.pop();
 						String tmp = reader.getPrefix();
 						if (( tmp != null ) && ( tmp.length()>0 )) {
 							code.setOuterData("</"+tmp+":"+name+">");
 						}
 						else {
 							code.setOuterData("</"+name+">");
 						}
 					}
 					break;
 					
 				case XMLStreamConstants.START_ELEMENT:
 					if ( store ) storeStartElement();
 					name = reader.getLocalName();
 					if ( name.equals("g") || name.equals("mrk") ) { //TODO: || name.equals("sub") ) {
 						idStack.push(++id);
 						code = content.append(TagType.OPENING, name, name);
 						// Get the outer code
 						String prefix = reader.getPrefix();
 						StringBuilder tmpg = new StringBuilder();
 						if (( prefix != null ) && ( prefix.length()>0 )) {
 							tmpg.append("<"+prefix+":"+reader.getLocalName());
 						}
 						else {
 							tmpg.append("<"+reader.getLocalName());
 						}
 						int count = reader.getNamespaceCount();
 						for ( int i=0; i<count; i++ ) {
 							prefix = reader.getNamespacePrefix(i);
 							tmpg.append(String.format(" xmlns:%s=\"%s\"",
 								((prefix!=null) ? ":"+prefix : ""),
 								reader.getNamespaceURI(i)));
 						}
 						count = reader.getAttributeCount();
 						for ( int i=0; i<count; i++ ) {
 							if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 							prefix = reader.getAttributePrefix(i); 
 							tmpg.append(String.format(" %s%s=\"%s\"",
 								(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 								reader.getAttributeLocalName(i),
 								reader.getAttributeValue(i)));
 						}
 						tmpg.append(">");
 						code.setOuterData(tmpg.toString());
 					}
 					else if ( name.equals("x") ) {
 						appendCode(TagType.PLACEHOLDER, ++id, name, store, content);
 					}
 					else if ( name.equals("bpt") ) {
 						idStack.push(++id);
 						appendCode(TagType.OPENING, id, name, store, content);
 					}
 					else if ( name.equals("ept") ) {
 						appendCode(TagType.CLOSING, idStack.pop(), name, store, content);
 					}
 					else if ( name.equals("ph") ) {
 						appendCode(TagType.PLACEHOLDER, ++id, name, store, content);
 					}
 					else if ( name.equals("it") ) {
 						appendCode(TagType.PLACEHOLDER, ++id, name, store, content);
 					}
 					break;
 				}
 			}
 			return content;
 		}
 		catch ( XMLStreamException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * Appends a code, using the content of the node. Do not use for <g>-type tags.
 	 * @param type The type of in-line code.
 	 * @param id The id of the code to add.
 	 * @param tagName The tag name of the in-line element to process.
 	 * @param store True if we need to store the data in the skeleton.
 	 * @param content The object where to put the code.
 	 * @param inlineCodes Array where to save the original in-line code.
 	 * Do not save if this parameter is null.
 	 */
 	private void appendCode (TagType tagType,
 		int id,
 		String tagName,
 		boolean store,
 		TextContainer content)
 	{
 		try {
 			StringBuilder innerCode = new StringBuilder();
 			StringBuilder outerCode = null;
 			outerCode = new StringBuilder();
 			outerCode.append("<"+tagName);
 			int count = reader.getAttributeCount();
 			String prefix;
 			for ( int i=0; i<count; i++ ) {
 				if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 				prefix = reader.getAttributePrefix(i); 
 				outerCode.append(String.format(" %s%s=\"%s\"",
 					(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 					reader.getAttributeLocalName(i),
 					reader.getAttributeValue(i)));
 			}
 			outerCode.append(">");
 			
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.START_ELEMENT:
 					if ( store ) storeStartElement();
 					prefix = reader.getPrefix();
 					StringBuilder tmpg = new StringBuilder();
 					if (( prefix == null ) || ( prefix.length()==0 )) {
 						tmpg.append("<"+reader.getLocalName());
 					}
 					else {
 						tmpg.append("<"+prefix+":"+reader.getLocalName());
 					}
 					count = reader.getNamespaceCount();
 					for ( int i=0; i<count; i++ ) {
 						prefix = reader.getNamespacePrefix(i);
 						tmpg.append(String.format(" xmlns:%s=\"%s\"",
 							((prefix!=null) ? ":"+prefix : ""),
 							reader.getNamespaceURI(i)));
 					}
 					count = reader.getAttributeCount();
 					for ( int i=0; i<count; i++ ) {
 						if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 						prefix = reader.getAttributePrefix(i); 
 						tmpg.append(String.format(" %s%s=\"%s\"",
 							(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 							reader.getAttributeLocalName(i),
 							reader.getAttributeValue(i)));
 					}
 					tmpg.append(">");
 					outerCode.append(tmpg.toString());
 					break;
 					
 				case XMLStreamConstants.END_ELEMENT:
 					if ( store ) storeEndElement();
 					if ( tagName.equals(reader.getLocalName()) ) {
 						Code code = content.append(tagType, tagName, innerCode.toString());
 						outerCode.append("</"+tagName+">");
 						code.setOuterData(outerCode.toString());
 						return;	
 					}
 					break;
 
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					innerCode.append(reader.getText());//TODO: escape unsupported chars
 					outerCode.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					if ( store ) //TODO: escape unsupported chars
 						skel.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new RuntimeException(e);
 		}
 	}
 	
 	private void processNote () {
 		try {
 			StringBuilder tmp = new StringBuilder();
 			if ( tu.hasProperty("note") ) {
 				tmp.append(tu.getProperty("note"));
 				tmp.append("\n---\n");
 			}
 			int eventType;
 			while ( reader.hasNext() ) {
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE: //TODO: escape unsupported chars
 					skel.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					tmp.append(reader.getText());
 					break;
 				case XMLStreamConstants.END_ELEMENT:
 					String name = reader.getLocalName();
 					if ( name.equals("note") ) {
 						//TODO: Handle 'annotates', etc.
 						tu.setProperty(new Property("note", tmp.toString(), true));
 						return;
 					}
 					// Else: This should be an error as note are text only.
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private boolean processStartGroup () {
 		storeStartElement();
 		// Check if it's a 'merge-trans' group (v1.2)
 		String tmp = reader.getAttributeValue("", "merge-trans");
 		if ( tmp != null ) {
 			// If it's a 'merge-trans' group we do not treat it as a normal group.
 			// The group element was not generated by the extractor.
 			if ( tmp.compareTo("yes") == 0 ) {
 				parentIds.push(-1);
 				return false;
 			}
 		}
 		
 		// Else: it's a structural group
 		StartGroup group = new StartGroup(parentIds.peek().toString(),
 			String.valueOf(++groupId));
 		group.setSkeleton(skel);
 		parentIds.push(groupId);
 		queue.add(new Event(EventType.START_GROUP, group));
 
 		// Get resname (can be null)
 		tmp = reader.getAttributeValue("", "resname");
 		if ( tmp != null ) group.setName(tmp);
 		else if ( params.fallbackToID ) {
 			group.setName(reader.getAttributeValue("", "id"));
 		}
 
 		// Get restype (can be null)
 		group.setType(reader.getAttributeValue("", "restype"));
 		return true;
 	}
 	
 	private boolean processEndGroup () {
 		// Pop and checks the value for this group
 		int id = parentIds.pop();
 		if ( id == -1 ) {
 			// This closes a 'merge-trans' non-structural group
 			return false;
 		}
 
 		// Else: it's a structural group
 		Ending ending = new Ending(String.valueOf(id));
 		ending.setSkeleton(skel);
 		queue.add(new Event(EventType.END_GROUP, ending));
 		return true;
 	}
 
 }
