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
 ============================================================================*/
 
 package net.sf.okapi.filters.tmx;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import javax.xml.XMLConstants;
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 
 import net.sf.okapi.common.BOMNewlineEncodingDetector;
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.codehaus.stax2.XMLInputFactory2;
 
 public class TmxFilter implements IFilter {
 
 	private final Logger logger = Logger.getLogger(getClass().getName());
 	
 	private boolean hasNext;
 	private XMLStreamReader reader;	
 	private String docName;
 	private int tuId;
 	private int otherId; 
 	private LocaleId srcLang;
 	private LocaleId trgLang;
 	private LinkedList<Event> queue;	
 	private boolean canceled;
 	private GenericSkeleton skel;	
 	//private TextUnit tu;
 	private String encoding;	
 	private Parameters params;
 	private Stack<Boolean> preserveSpaces;
 	private String lineBreak;
 	private boolean hasUTF8BOM;
 	
 	private boolean skipUtWarning;
 	
 	
 	public enum TuvXmlLang {UNDEFINED,SOURCE,TARGET,OTHER}
 	private TuvXmlLang tuvTrgType = TuvXmlLang.UNDEFINED;
 
 	
 	private HashMap<String,String> rulesMap = new HashMap<String,String>();
 	private Stack<String> elemStack=new Stack<String>();
 	
 	public TmxFilter () {
 		params = new Parameters();
 		
 		rulesMap.put("<seg>", "<bpt><ept><it><ph><hi><ut>");
 		rulesMap.put("<sub>", "<bpt><ept><it><ph><hi><ut>");
 		rulesMap.put("<hi>", "<bpt><ept><it><ph><hi><ut>");
 		rulesMap.put("<bpt>","<sub>");
 		rulesMap.put("<ept>","<sub>");
 		rulesMap.put("<it>","<sub>");
 		rulesMap.put("<ph>","<sub>");		
 	}
 	
 	public void cancel() {
 		canceled = true;
 	}
 
 	public void close() {
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
 
 	public String getName() {
 		return "okf_tmx";
 	}
 	
 	public String getDisplayName () {
 		return "TMX Filter";
 	}
 
 	public String getMimeType () {
 		return "text/x-tmx";
 	}
 
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			"text/x-tmx",
 			getClass().getName(),
 			"TMX",
 			"Configuration for Translation Memory eXchange (TMX) documents."));
 		return list;
 	}
 	
 	public IParameters getParameters () {
 		return params;
 	}
 	
 	public boolean hasNext() {
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
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 /*	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		setOptions(input.getSourceLanguage(), input.getTargetLanguage(),
 			input.getEncoding(), generateSkeleton);
 		
 		if ( input.getInputCharSequence() != null ) {
 			open(input.getInputCharSequence());
 		}
 		else if ( input.getInputURI() != null ) {
 			open(input.getInputURI());
 		}
 		else if ( input.getInputStream() != null ) {
 			open(input.getInputStream());
 		}
 		else {
 			throw new OkapiBadFilterInputException("RawDocument has no input defined.");
 		}
 	}
 	
 	private void open (InputStream input) {
 		commonOpen(0, input);
 	}
 	
 	private void open (CharSequence inputText) {
 		docName = null;
 		encoding = "UTF-16";
 		hasUTF8BOM = false;
 		lineBreak = BOMNewlineEncodingDetector.getNewlineType(inputText).toString();
 		commonOpen(1, new StringReader(inputText.toString()));
 	}
 
 	private void open (URI inputURI) {
 		try {
 			docName = inputURI.getPath();
 			commonOpen(0, inputURI.toURL().openStream());
 		}
 		catch ( IOException e ) {
 			throw new OkapiIOException(e);
 		}
 	}*/	
 	
 	
 	public void open (RawDocument input,
 			boolean generateSkeleton)
 	{
 		try {
 			close();
 			canceled = false;			
 			
 			XMLInputFactory fact = XMLInputFactory.newInstance();
 			fact.setProperty(XMLInputFactory.IS_COALESCING, true);
//     		fact.setProperty(XMLInputFactory2.P_REPORT_PROLOG_WHITESPACE, true);
// DWH an error message says Property org.codehaus.stax2.reportPrologWhitespace is not supported
 			
 			//fact.setXMLResolver(new DefaultXMLResolver());
 			//TODO: Resolve the re-construction of the DTD, for now just skip it
 			fact.setProperty(XMLInputFactory.SUPPORT_DTD, false);			
 
 			input.setEncoding("UTF-8"); // Default for XML, other should be auto-detected
 			
 			// determine encoding based on BOM, if any
 			BOMNewlineEncodingDetector detector = new BOMNewlineEncodingDetector(input.getStream(), input.getEncoding());
 			detector.detectBom();
 			input.setEncoding(detector.getEncoding());
 			
 			//TODO: How does this filter auto detect the encoding??
 			reader = fact.createXMLStreamReader(input.getReader());
 
 			encoding = input.getEncoding(); // Real encoding
 			srcLang = input.getSourceLocale();
 			if ( Util.isNullOrEmpty(srcLang) ) throw new NullPointerException("Source language not set.");
 			trgLang = input.getTargetLocale();
 			if ( Util.isNullOrEmpty(trgLang) ) throw new NullPointerException("Target language not set.");
 			hasUTF8BOM = detector.hasUtf8Bom();
 			lineBreak = detector.getNewlineType().toString();
 			if ( input.getInputURI() != null ) {
 				docName = input.getInputURI().getPath();
 			}
 
 			preserveSpaces = new Stack<Boolean>();
 			preserveSpaces.push(false);
 			tuId = 0;
 			otherId = 0;			
 			hasNext=true;
 			queue = new LinkedList<Event>();
 			skipUtWarning = false;
 			
 			//--attempt encoding detection--
 			//if(reader.getEncoding()!=null){
 			//	encoding = reader.getEncoding();
 			//}
 			
 			StartDocument startDoc = new StartDocument(String.valueOf(++otherId));
 			startDoc.setName(docName);
 			String realEnc = reader.getCharacterEncodingScheme();
 			if ( realEnc != null ) encoding = realEnc;
 			startDoc.setEncoding(encoding, hasUTF8BOM); //TODO: UTF8 BOM detection
 			startDoc.setLocale(srcLang);
 			startDoc.setFilterParameters(getParameters());
 			startDoc.setFilterWriter(createFilterWriter());
 			startDoc.setType("text/x-tmx");
 			startDoc.setMimeType("text/x-tmx");
 			startDoc.setMultilingual(true);
 			startDoc.setLineBreak(lineBreak);			
 			queue.add(new Event(EventType.START_DOCUMENT, startDoc));			
 			
 			// The XML declaration is not reported by the parser, so we need to
 			// create it as a document part when starting			
 			skel = new GenericSkeleton();
 			startDoc.setProperty(new Property(Property.ENCODING, encoding, false));
 			skel.append("<?xml version=\"1.0\" encoding=\"");
 			skel.addValuePlaceholder(startDoc, Property.ENCODING, LocaleId.EMPTY);
 			skel.append("\"?>");
 			startDoc.setSkeleton(skel);
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 
 	public void setParameters(IParameters params) {
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
 				if (reader.getLocalName().equals("tu")){
 					
 					// Make a document part with skeleton between the previous event and now.
 					// Spaces can go with trans-unit to reduce the number of events.
 					// This allows to have only the trans-unit skeleton parts with the TextUnit event
 					if ( !skel.isEmpty(true) ) {
 						DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 						skel = new GenericSkeleton(); // And create a new skeleton for the next event
 						queue.add(new Event(EventType.DOCUMENT_PART, dp));
 					}
 					
 					return processTranslationUnit();
 				}else{
 					storeStartElement();
 					if (!params.consolidateDpSkeleton) {
 						DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 						skel = new GenericSkeleton();
 						queue.add(new Event(EventType.DOCUMENT_PART, dp));
 					}
 					break;
 				}
 			
 			case XMLStreamConstants.END_ELEMENT:
 				storeEndElement();
 				if (!params.consolidateDpSkeleton) {
 					DocumentPart dp = new DocumentPart(String.valueOf(++otherId), false, skel);
 					skel = new GenericSkeleton();
 					queue.add(new Event(EventType.DOCUMENT_PART, dp));
 				}
 				break;				
 			
 			case XMLStreamConstants.SPACE:
 			case XMLStreamConstants.CDATA:
 				skel.append(reader.getText().replace("\n", lineBreak));
 				break;				
 			case XMLStreamConstants.CHARACTERS: //TODO: Check if it's ok to not check for unsupported chars
 				skel.append(Util.escapeToXML(reader.getText().replace("\n", lineBreak), 0, params.escapeGT, null));
 				break;
 				
 			case XMLStreamConstants.COMMENT:
 				skel.append("<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
 				break;				
 
 			case XMLStreamConstants.PROCESSING_INSTRUCTION:
 				skel.append("<?"+ reader.getPITarget() + " " + reader.getPIData() + "?>");
 				break;				
 				
 			case XMLStreamConstants.DTD:
 				//TODO: Reconstruct the DTD declaration
 				// but how? nothing is available to do that
 				break;				
 				
 			case XMLStreamConstants.ENTITY_REFERENCE:
 			case XMLStreamConstants.ENTITY_DECLARATION:
 			case XMLStreamConstants.NAMESPACE:
 			case XMLStreamConstants.NOTATION_DECLARATION:
 				break;
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
 				((prefix.length()>0) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			prefix = reader.getAttributePrefix(i); 
 			skel.append(String.format(" %s%s=\"%s\"",
 				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 				reader.getAttributeLocalName(i),
 				reader.getAttributeValue(i)));
 		}
 		skel.append(">");
 	}
 	
 /*	private void storeTuStartElement () {
 		
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
 				((prefix.length()>0) ? ":"+prefix : ""),
 				reader.getNamespaceURI(i)));
 		}
 		count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( !reader.isAttributeSpecified(i) ) continue; // Skip defaults
 			prefix = reader.getAttributePrefix(i); 
 			skel.append(String.format(" %s%s=\"%s\"",
 				(((prefix==null)||(prefix.length()==0)) ? "" : prefix+":"),
 				reader.getAttributeLocalName(i),
 				reader.getAttributeValue(i)));
 			
 			//--set the properties depending on the tuvTrgType--
 			if(tuvTrgType == TuvXmlLang.UNDEFINED){
 				tu.setProperty(new Property(reader.getAttributeLocalName(i),reader.getAttributeValue(i), true));				
 			}else if(tuvTrgType == TuvXmlLang.SOURCE){
 				tu.setSourceProperty(new Property(reader.getAttributeLocalName(i), reader.getAttributeValue(i), true));
 			}else if(tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 				tu.setTargetProperty(currentLang, new Property(reader.getAttributeLocalName(i),reader.getAttributeValue(i), true));
 			}			
 		}
 		skel.append(">");
 	}*/	
 	
 	private void storeEndElement () {
 		String ns = reader.getPrefix();
 		if (( ns == null ) || ( ns.length()==0 )) {
 			skel.append("</"+reader.getLocalName()+">");
 		}
 		else {
 			skel.append("</"+ns+":"+reader.getLocalName()+">");
 		}
 	}
 	
 
 	/**
 	 * Processes notes or properties skeletizing start, content, and end element  
 	 * @param tmxTu The TmxTu helper for the current tu.
 	 * @return true for success and false for failure.
 	 */	
 	private boolean processTuDocumentPart(TmxTu tmxTu){
 		
 		String propName = "";							//used for <prop> elements to get the value of type to be used as prop name
 		String startElement = reader.getLocalName();	//prop or note
 		
 		if(tuvTrgType == TuvXmlLang.UNDEFINED){
 			//determine the property name and add skel to TmxTu
 			propName = tmxTu.parseStartElement(reader,startElement);
 		}else if (tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 			//determine the property name and add skel to TmxTuv
 			propName = tmxTu.curTuv.parseStartElement(reader, tuvTrgType, params.processAllTargets, startElement);
 		}
 		
 		try {
 			while(reader.hasNext()){
 				int eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.CHARACTERS:
 					 //TODO: Check if it's ok to not check for unsupported chars
 					//--append skel and set the properties depending on the tuvTrgType--
 					if(tuvTrgType == TuvXmlLang.UNDEFINED){
 						tmxTu.appendToSkel(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 						tmxTu.addProp(new Property(propName, reader.getText(), true));
 					}else if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 						tmxTu.curTuv.skelBefore.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 						tmxTu.curTuv.setProperty(new Property(propName, reader.getText(), true));
 					}else if(tuvTrgType == TuvXmlLang.OTHER){
 						tmxTu.curTuv.skelBefore.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					}
 					break;
 				case XMLStreamConstants.END_ELEMENT:
 					if(reader.getLocalName().equalsIgnoreCase(startElement)){
 						//--append skel depending on the tuvTrgType--
 						if(tuvTrgType == TuvXmlLang.UNDEFINED){
 							tmxTu.parseEndElement(reader, true);
 						}else if (tuvTrgType == TuvXmlLang.UNDEFINED || tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 							tmxTu.curTuv.parseEndElement(reader,true);
 						}
 						return true;
 					}
 					break;
 				}
 			}
 			return false;
 		} catch (XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}	
 	
 	
 	/**
 	 * Process a segment <seg>*</seg>, appending the skeleton to skel and adding the properties to nameable and reference to tu 
 	 */			
 	private boolean processSeg(TmxTu tmxTu){
 		
 		int id = 0;
 		//Stack<Integer> idStack = new Stack<Integer>();
 		//idStack.push(id);		
 		
 		String curLocalName;
 		
 		//--determine which container to use--
 		TextContainer tc;
 		if(tuvTrgType == TuvXmlLang.SOURCE){
 			//tc = pTu.getSource();
 			tc = tmxTu.curTuv.tc;
 		}else if(tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 			//tc = pTu.setTarget(currentLang, new TextContainer());
 			tc = tmxTu.curTuv.tc;
 		}else{
 			tc=null;
 		}
 		
 		//storeTuStartElement();							//store the <seg> element with it's properties
 		tmxTu.curTuv.parseStartElement(reader, tuvTrgType, params.processAllTargets);
 		
 		try {
 			while(reader.hasNext()){					//loop through the <seg> content
 				int eventType;
 				eventType = reader.next();
 				switch ( eventType ) {
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 					if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 						//TODO: Check if it's ok to not check for unsupported chars
 						tc.append(reader.getText());	//add to source or target container
 					}else{ 			
 						//TODO: Check if it's ok to not check for unsupported chars
 						tmxTu.curTuv.appendToSkel(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					}
 					break;
 					
 				case XMLStreamConstants.START_ELEMENT:		
 
 					curLocalName = reader.getLocalName().toLowerCase();
 					if(!isValidElement(elemStack.peek(),curLocalName,true)){
 						//--throws OkapiBadFilterInputException if not valid--
 					}
 					
 					if(curLocalName.equals("ut") && !skipUtWarning){
 						logger.warning("<ut> is been deprecated in tmx 1.4.");
 						skipUtWarning=true;
 					}
 					elemStack.push(curLocalName);
 										
 					if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 						if(curLocalName.equals("hi")){
 							String typeAttr = getTypeAttribute();
 							tc.append(TagType.OPENING, ((typeAttr!=null) ? typeAttr : "hi"),"<hi>");	
 						}else if(curLocalName.equals("ph") || curLocalName.equals("it") || curLocalName.equals("ut")){
 							appendCode(TagType.PLACEHOLDER, ++id, curLocalName, curLocalName, tc);
 						}else if(curLocalName.equals("bpt")){
 							appendCode(TagType.OPENING, ++id, curLocalName,"Xpt", tc);
 						}else if(curLocalName.equals("ept")){
 							appendCode(TagType.CLOSING, -1, curLocalName,"Xpt", tc);
 						}
 						break;
 					}else{
 						tmxTu.curTuv.parseStartElement(reader, tuvTrgType, params.processAllTargets);
 					}
 					break;
 					
 				case XMLStreamConstants.END_ELEMENT:
 					
 					curLocalName = reader.getLocalName();		//current element
 					elemStack.pop();						//pop one element
 					
 					if(curLocalName.equalsIgnoreCase("seg")){	//end of seg
 										
 						tmxTu.curTuv.finishedSegSection=true;
 						tmxTu.curTuv.parseEndElement(reader);
 
 						return true;
 					}else{
 						if(tuvTrgType == TuvXmlLang.SOURCE || tuvTrgType == TuvXmlLang.TARGET || params.processAllTargets){
 							if(curLocalName.equals("hi")){
 								tc.append(TagType.CLOSING, "hi","</hi>");	
 							}						
 						}else{
 							tmxTu.curTuv.parseEndElement(reader, true);
 							break;
 						}
 					}
 				}
 			}
 			return false;
 		} catch (XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}
 	
 	/**
 	 * Process an entire tu element
 	 * @return FilterEvent
 	 */		
 	private boolean processTranslationUnit(){
 		
 		LocaleId currentLang;
 		TmxTu tmxTu = new TmxTu(srcLang, trgLang, lineBreak);	//create the TmxTu helper
 		tmxTu.parseStartElement(reader);						//add to TmxTu skelBefore
 
 		String curLocalName;
 		
 		try {
 			while(reader.hasNext()){
 				
 				int eventType = reader.next();
 				switch ( eventType ) {
 				
 				case XMLStreamConstants.COMMENT:
 					//appends to either TmxTu or TmxTuv depending on tuvTrgType and skelBefore or skelAfter depending on flags
 					tmxTu.smartAppendToSkel(tuvTrgType, "<!--"+ reader.getText().replace("\n", lineBreak) + "-->");
 					break;
 				case XMLStreamConstants.CHARACTERS: 
 					//TODO: Check if it's ok to not check for unsupported chars
 					//appends to either TmxTu or TmxTuv depending on tuvTrgType and skelBefore or skelAfter depending on flags					
 					tmxTu.smartAppendToSkel(tuvTrgType, Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));					
 					break;
 				case XMLStreamConstants.START_ELEMENT:
 
 					curLocalName = reader.getLocalName(); 
 					if(curLocalName.equalsIgnoreCase("note") || curLocalName.equalsIgnoreCase("prop")){
 						//Todo: handle true/false
 						processTuDocumentPart(tmxTu);
 					}else if(reader.getLocalName().equals("tuv")){
 						
 						currentLang = getXmlLangFromCurTuv();
 						tuvTrgType = getTuvTrgType(currentLang);
 						
 						TmxTuv tmxTuv = tmxTu.addTmxTuv(currentLang,tuvTrgType);
 						tmxTuv.parseStartElement(reader,tuvTrgType, params.processAllTargets);
 						
 					}else if(reader.getLocalName().equals("seg")){
 						elemStack.push("seg");
 						processSeg(tmxTu);
 					}
 					break;
 					
 				case XMLStreamConstants.END_ELEMENT:
 
 					curLocalName = reader.getLocalName(); 
 					if(curLocalName.equalsIgnoreCase("tu")){
 						
 						tmxTu.parseEndElement(reader);
 						
 						tuId = tmxTu.addPrimaryTextUnitEvent(tuId, params.processAllTargets, queue);
 						tuId = tmxTu.addDuplicateTextUnitEvents(tuId, params.processAllTargets, queue);
 						
 						//--reset--
 						tuvTrgType = TuvXmlLang.UNDEFINED;
 						return true;
 						
 					}else if(curLocalName.equals("tuv")){
 
 						tmxTu.curTuv.parseEndElement(reader);
 
 					}else{
 						//--TMX RULE: Entering here would mean content other than <note>, <prop>, or <tuv> inside the <tu> which is invalid.
 						throw new OkapiBadFilterInputException("Only <note>, <prop>, and <tuv> elements are allowed inside <tu>");
 					} 	
 					break;
 				}
 			}
 		} catch (XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 		return false;		
 	}
 	
 	
 	/**
 	 * Appends a code, using the content of the node. Do not use for <g>-type tags.
 	 * @param type The type of in-line code.
 	 * @param id The id of the code to add.
 	 * @param tagName The tag name of the in-line element to process.
 	 * @param type The tag name of the in-line element to process. 
 	 * @param content The object where to put the code.
 	 * Do not save if this parameter is null.
 	 */
 	private void appendCode (TagType tagType,
 		int id,
 		String tagName,
 		String type,
 		TextContainer content)
 	{
 		
 		String localName;
 		
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
 					
 					localName = reader.getLocalName().toLowerCase();
 					if(!isValidElement(elemStack.peek(),localName, true)){
 						//--throws OkapiBadFilterInputException if not valid--
 					}
 					
 					if(localName.equals("ut") && !skipUtWarning){
 						logger.warning("<ut> is been deprecated in tmx 1.4.");
 						skipUtWarning=true;
 					}
 					elemStack.push(localName);
 
 					//--warn about subflow--
 					if("sub".equals(reader.getLocalName())){
 						logger.warning("A <sub> element was detected. It will be included in its parent code as <sub> is currently not supported.");
 					}
 					
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
 				
 					innerCode.append(tmpg.toString());
 					outerCode.append(tmpg.toString());
 					
 					break;
 					
 				case XMLStreamConstants.END_ELEMENT:
 					
 					elemStack.pop();
 					
 					//--completed the original placeholder/code and back up to the <seg> level--
 					if ( tagName.equals(reader.getLocalName()) && ((elemStack.peek().equals("seg"))|| (elemStack.peek().equals("hi")) )) {
 
 						Code code = content.append(tagType, type, innerCode.toString(), id);
 						outerCode.append("</"+tagName+">");
 						code.setOuterData(outerCode.toString());
 						return;							
 					}else{
 						
 						String ns = reader.getPrefix();
 						if (( ns == null ) || ( ns.length()==0 )) {
 							innerCode.append("</"+reader.getLocalName()+">");
 							outerCode.append("</"+reader.getLocalName()+">");
 						}
 						else {
 							innerCode.append("</"+ns+":"+reader.getLocalName()+">");
 							outerCode.append("</"+ns+":"+reader.getLocalName()+">");
 						}						
 					}
 					break;
 
 				case XMLStreamConstants.CHARACTERS:
 				case XMLStreamConstants.CDATA:
 				case XMLStreamConstants.SPACE:
 
 					innerCode.append(reader.getText());//TODO: escape unsupported chars
 					outerCode.append(Util.escapeToXML(reader.getText(), 0, params.escapeGT, null));
 					break;
 				}
 			}
 		}
 		catch ( XMLStreamException e) {
 			throw new OkapiIOException(e);
 		}
 	}	
 	
 
 	/**
 	 * Gets the TuvXmlLang based on current language and source and specified target lang
 	 * @return 	TuvXmlLang.SOURCE, TuvXmlLang.TARGET, and TuvXmlLang.OTHER
 	 */		
 	private TuvXmlLang getTuvTrgType(LocaleId lang){
 		if ( lang.equals(srcLang) ) {
 			return TuvXmlLang.SOURCE; 
 		}
 		else if ( lang.equals(trgLang) ) {
 			return TuvXmlLang.TARGET;
 		}
 		else { 
 			return TuvXmlLang.OTHER;
 		}
 	}
 	
 	
 	/**
 	 * Gets the value of the xml:lang or lang attribute from the current <tuv> element
 	 * @return the language value
 	 * @throws OkapiBadFilterInputException if xml:Lang or lang is missing
 	 */		
 	private LocaleId getXmlLangFromCurTuv(){
 		String tmp = reader.getAttributeValue(XMLConstants.XML_NS_URI, "lang");
 		if ( tmp != null ) {
 			return LocaleId.fromString(tmp);
 		}
 		// If xml:lang not found, fall back to lang (old TMX versions)
 		int count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if ( reader.getAttributeLocalName(i).equals("lang")){
 				return LocaleId.fromString(reader.getAttributeValue(i));
 			}
 		}
 		throw new OkapiBadFilterInputException("The required xml:lang or lang attribute is missing in <tuv>. The file is not valid TMX.");
 	}
 
 	
 	/**
 	 * Gets the type attribute from the current element
 	 * @return 	returns the type or null if it's missing
 	 */		
 	private String getTypeAttribute(){
 
 		int count = reader.getAttributeCount();
 		for ( int i=0; i<count; i++ ) {
 			if(reader.getAttributeLocalName(i).equals("lang")){
 				return reader.getAttributeValue(i);
 			}
 		}
 		return null;
 	}
 	
 	
 	private boolean isValidElement(String curElem, String newElem, boolean throwException){
 		String rules = rulesMap.get("<"+curElem+">");
 
 		if(rules!=null && rules.contains("<"+newElem+">")){
 			return true;
 		}else{
 			if(throwException){
 				throw new OkapiBadFilterInputException("<"+newElem+"> not allowed in <"+curElem+">. Only "+rules+" allowed.");
 			}else{
 				logger.warning("<"+newElem+"> not allowed in <"+curElem+">. Only "+rules+" allowed.");
 				return false;		
 			}
 		}
 	}
 }
