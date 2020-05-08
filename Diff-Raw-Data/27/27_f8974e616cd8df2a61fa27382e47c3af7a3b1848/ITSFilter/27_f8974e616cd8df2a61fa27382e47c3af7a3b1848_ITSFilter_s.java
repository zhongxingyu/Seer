 /*===========================================================================
   Copyright (C) 2008-2012 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.its;
 
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Stack;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IdGenerator;
 import net.sf.okapi.common.ListUtil;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.annotation.TermsAnnotation;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filters.SubFilter;
 import net.sf.okapi.common.filterwriter.GenericFilterWriter;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.DocumentPart;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.Property;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.common.skeleton.GenericSkeletonWriter;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.its.ITSEngine;
 import org.w3c.its.ITraversal;
 import org.w3c.its.TargetPointerEntry;
 
 public abstract class ITSFilter implements IFilter {
 
 	private final Logger logger = LoggerFactory.getLogger(getClass());
 
 	protected Parameters params;
 	protected EncoderManager encoderManager;
 	protected Document doc;
 	protected RawDocument input;
 	protected String encoding;
 	protected String docName;
 	protected LocaleId srcLang;
 	protected String lineBreak;
 	protected boolean hasUTF8BOM;
 	protected GenericSkeleton skel;
 	protected IFilterConfigurationMapper fcMapper;
 	
 	private final String mimeType;
 	private final boolean isHTML5;
 	
 	private String trgLangCode; // can be null
 	private ITSEngine trav;
 	private LinkedList<Event> queue;
 	private int tuId;
 	private IdGenerator otherId;
 	private TextFragment frag;
 	private Stack<ContextItem> context;
 	private boolean canceled;
 //	private IEncoder cfEncoder;
 	private TermsAnnotation terms;
 	private Map<String, String> variables;
 
 	public ITSFilter (boolean isHTML5,
 		String mimeType)
 	{
 		this.isHTML5 = isHTML5;
 		this.mimeType = mimeType;
 		this.params = new Parameters();
 	}
 	
 	/**
 	 * Sets the ITS variables to pass to the ITS parameters feature.
 	 * This method should be called before {@link #open(RawDocument, boolean)}.
 	 * Those variables overwrite the default values set in the <code>its;params</code> elements.
 	 * @param map the map of variables to pass. Can be null or empty.
 	 */
 	public void setITSVariables (Map<String, String> map) {
 		variables = map;
 	}
 
 	public void cancel () {
 		canceled = true;
 	}
 
 	public void close () {
 		if (input != null) {
 			input.close();
 		}
 	}
 
 	public ISkeletonWriter createSkeletonWriter () {
 		return new GenericSkeletonWriter();
 	}
 
 	public IFilterWriter createFilterWriter () {
 		return new GenericFilterWriter(createSkeletonWriter(), getEncoderManager());
 	}
 
 	public String getMimeType () {
 		return mimeType;
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
 		this.fcMapper = fcMapper;
 	}
 
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 	
 	abstract protected void initializeDocument ();
 	
 	abstract protected void applyRules (ITSEngine itsEng);
 	
 	abstract protected void createStartDocumentSkeleton (StartDocument startDoc);
 
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		close();
 		
 		// Initializes the variables
 		this.input = input;
 		canceled = false;
 		tuId = 0;
 		otherId = new IdGenerator(null, "o");
 
 		initializeDocument();
 		
 		// Allow no target locale too
 		trgLangCode = null;
 		if ( !Util.isNullOrEmpty(input.getTargetLocale()) ) {
 			trgLangCode = input.getTargetLocale().toString().toLowerCase();
 		}
 		
 		if ( params.useCodeFinder ) {
 			params.codeFinder.compile();
 		}
 
 		// Create the ITS engine
 		trav = new ITSEngine(doc, input.getInputURI(), isHTML5, variables);
 		// Load the parameters file if there is one
 		if ( params != null ) {
 			if ( params.getDocument() != null ) {
 				trav.addExternalRules(params.getDocument(), params.getURI());
 			}
 		}
 		
 		applyRules(trav);
 
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
 		if ( !trav.getTranslatableAttributeRuleTriggered() ) {
 			// Allow to not escape quotes only if there is no translatable attributes
 			if ( !params.escapeQuotes ) {
 				params.quoteModeDefined = true;
 				params.quoteMode = 0; // quote and apos not escaped
 			}
 		}
 		startDoc.setFilterParameters(params);
 		
 		startDoc.setFilterWriter(createFilterWriter());
 		startDoc.setType(mimeType);
 		startDoc.setMimeType(mimeType);
 
 		createStartDocumentSkeleton(startDoc);
 		startDoc.setSkeleton(skel);
 		
 		// Put the start document in the queue
 		queue.add(new Event(EventType.START_DOCUMENT, startDoc));
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
 					if ( extract() ) {
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
 					if ( extract() ) {
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
 		
 		boolean checkEncoding = (isHTML5 && node.getLocalName().equals("meta"));
 		
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
 				if (( trav.getTranslate(attr) ) && ( attr.getValue().length() > 0 )) {
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
 				else if ( isHTML5 && attr.getName().equals("lang") ) {
 					//String x = attr.getValue();
 					//TODO: handle xml:lang
 					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
 						+ "\"");
 				}
 				else if ( checkEncoding && attr.getName().equals("charset") ) {
 					// For HTML5 documents: set the property placeholder for the encoding
 					DocumentPart dp = new DocumentPart(otherId.createId(), false);
 					dp.setProperty(new Property(Property.ENCODING, encoding));
 					skel.append(tmp.toString());
 					skel.addValuePlaceholder(dp, Property.ENCODING, LocaleId.EMPTY);
 					skel.append("\"");
 					queue.add(new Event(EventType.DOCUMENT_PART, dp, skel));
 					// Reset the skleeton for next event
 					skel = new GenericSkeleton();
 					tmp.setLength(0);
 				}
 				else { //TODO: escape unsupported chars
 					tmp.append(Util.escapeToXML(attr.getNodeValue(), 3, false, null)
 						+ "\"");
 				}
 			}
 		}
 		if ( !isHTML5 && !node.hasChildNodes() ) tmp.append("/");
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
 				if (( trav.getTranslate(attr) ) && ( attr.getValue().length() > 0 )) {
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
 		if ( !isHTML5 && !node.hasChildNodes() ) tmp.append("/");
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
 		ITextUnit tu = new TextUnit(id, attr.getValue(), true, mimeType);
 		
 		// Deal with inline codes if needed
 		if ( params.useCodeFinder ) {
 			TextFragment tf = tu.getSource().getFirstContent();
 			params.codeFinder.process(tf);
 //			// Escape inline code content
 //			List<Code> codes = tf.getCodes();
 //			for ( Code code : codes ) {
 //				// Escape the data of the new inline code (and only them)
 //				if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) {
 //					if ( cfEncoder == null ) {
 //						cfEncoder = getEncoderManager().getEncoder();
 //						//TODO: We should use the proper output encoding here, not force UTF-8, but we do not know it
 //						cfEncoder.setOptions(params, "utf-8", lineBreak);
 //					}
 //					code.setData(cfEncoder.encode(code.getData(), EncoderContext.TEXT));
 //				}
 //			}
 		}
 
 		// Set the ITS context for this attribute and set the relevant properties
		ContextItem ci = new ContextItem(context.peek().node, trav, attr);
 		// ITS Localization Note
 		if ( !Util.isEmpty(ci.locNote) ) {
 			tu.setProperty(new Property(Property.NOTE, ci.locNote));
 		}
 		// ITS Domain
 		if ( !Util.isEmpty(ci.domains) ) {
 			tu.setProperty(new Property(Property.ITS_DOMAIN, ci.domains));
 		}
 		// ITS External resources Reference
 		if ( !Util.isEmpty(ci.externalRes) ) {
 			tu.setProperty(new Property(Property.ITS_EXTERNALRESREF, ci.externalRes));
 		}
 		// ITS Storage Size
 		if ( ci.storageSize != null ) {
 			tu.setProperty(new Property(Property.ITS_STORAGESIZE, ci.storageSize));
 		}
 		// ITS Allowed characters
 		if ( ci.allowedChars != null ) {
 			tu.setProperty(new Property(Property.ITS_ALLOWEDCHARACTERS, ci.allowedChars));
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
 
 			if ( trav.getTerm(null) ) {
 				if ( terms == null ) {
 					terms = new TermsAnnotation();
 				}
 				terms.add(node.getTextContent(), trav.getTermInfo(null));
 			}
 
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
 			// Test if this node is involved in a source/target pair
 			if ( trav.getHasTargetPointer() ) {
 				TargetPointerEntry tpe = trav.getTargetPointerEntry(node);
 				if ( tpe != null ) {
 					if ( tpe.getTargetNode() == node ) {
 						// This node is a target location
 						tpe.toString();
 						//TODO
 					}
 					else {
 						// This node is a source with a target location
 						// TODO
 						tpe.toString();
 					}
 				}
 			}
 			
 			if ( trav.getSubFilter(null) != null ) {
 				processSubFilterContent(node, ((ITSEngine)trav).getSubFilter(null));
 				moveToEnd(node); // Move to the end of this node
 				return true; // Send the events
 			}
 			
 			// Otherwise, treat the tag
 			switch ( trav.getWithinText() ) {
 			case ITraversal.WITHINTEXT_NESTED: //TODO: deal with nested elements
 				// For now treat them as inline
 			case ITraversal.WITHINTEXT_YES:
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
 					if ( extract() ) {
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
 					if ( extract() ) {
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
 	
 	private void moveToEnd (Node start) {
 		Node node = null;
 		while ( (node = trav.nextNode()) != null ) {
 			if ( node == start ) return;
 		}
 	}
 	
 	/**
 	 * Process a content with a given sub-filter.
 	 * @param node the node to process.
 	 * @param fltConfig the sub-filter configuration identifier.
 	 */
 	private void processSubFilterContent (Node node,
 		String configId)
 	{
 		// Create the skeleton for the start tag
 		// This will be used later
 		addStartTagToSkeleton(node);
 
 		// Instantiate the filter to use as sub-filter
 		IFilter sf = fcMapper.createFilter(configId, null);
 		if ( sf == null ) {
 			throw new OkapiBadFilterInputException(String.format("Could not instantiate subfilter '%s'.", configId));
 		}
 		
 		// Create the sub-filter wrapper
 		// First, make sure we have defaults and set the default encoder
 		//TODO: Issue: the encoding is the input encoding, not the output one
 		encoderManager.setDefaultOptions(getParameters(), this.encoding, this.lineBreak);
 		encoderManager.updateEncoder(getMimeType());
 		// Then create the sub-filter
 		SubFilter subfilter = new SubFilter(sf, 
 			getEncoderManager().getEncoder(),
 			1, // sectionIndex
 			"parentId",
 			null // Parent name
 			);
 
 		// Process the content
 		String content = node.getTextContent();
 		subfilter.open(new RawDocument(content, srcLang));
 		while (subfilter.hasNext()) {
 			queue.add(subfilter.next());
 		}
 		subfilter.close();
 		
 		// Create the skeleton for the end tag
 		GenericSkeleton skelAfter = new GenericSkeleton();
 		skelAfter.add(buildEndTag(node));
 		
 		// Create the document part holding the re-writing mechanism
 		queue.add(subfilter.createRefEvent(skel, skelAfter));
 		
 		// Just make sure this skeleton is reset for next time
 		skel = new GenericSkeleton();
 	}
 
 	/**
 	 * Indicates if the current node is to be extracted according its ITS state.
 	 * @return true if it is to be extracted, false otherwise.
 	 */
 	private boolean extract () {
 		// Check ITS translate
 		if ( !trav.getTranslate(null) ) return false;
 		
 		// Check ITS locale filter
 		String list = trav.getLocaleFilter();
 		// null is none-defined, so default is '*'
 		if (( list == null ) || list.equals("*") ) return true;
 		if ( list.length() == 0 ) return false; // Empty list is 'none'
 		
 		// More info for extended language range/filtering here:
 		// http://www.rfc-editor.org/rfc/bcp/bcp47.txt
 		if ( trgLangCode == null ) {
 			// Log a warning that the data category cannot be used
 			logger.warn("No target locale specified: Cannot use the provided ITS Locale Filter data category.");
 			return true;
 		}
 		// Now check with one or more codes
 		return extendedMatch(list, trgLangCode);
 	}
 	
 	/**
 	 * Indicates if a given language tag matches at least one item of a list of extended language ranges.
 	 * <p>Based on the algorithm described at: http://tools.ietf.org/html/rfc4647#section-3.3.2
 	 * @param langRanges the list of extended language ranges
 	 * @param langTag the language tag.
 	 * @return true if the language tag matches at least one item of a list of extended language ranges.
 	 */
 	public boolean extendedMatch (String langRanges,
 		String langTag)
 	{
 		for ( String langRange : ListUtil.stringAsArray(langRanges.toLowerCase()) ) {
 			if ( doesLangTagMacthesLangRange(langRange, langTag) ) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Compares an extended language range with a language tag.
 	 * @param langRange the extended language range.
 	 * @param langTag the language tag.
 	 * @return true if the language tag matches the language range.
 	 */
 	private boolean doesLangTagMacthesLangRange (String langRange,
 		String langTag)
 	{
 		String[] lrParts = langRange.toLowerCase().split("-", 0);
 		String[] ltParts = langTag.toLowerCase().split("-", 0);
 		
 		int i = 0;
 		int j = 0;
 		String lrst = lrParts[i];
 		String ltst = ltParts[j]; j++;
 		if ( !lrst.equals(ltst) && !lrst.equals("*") ) return false;
 
 		i = 1;
 		j = 1;
 		while ( i<lrParts.length) {
 			lrst = lrParts[i];
 			if ( lrst.equals("*") ) {
 				i++;
 				continue;
 			}
 			else if ( j >= ltParts.length ) {
 				return false;
 			}
 			else if ( ltParts[j].equals(lrst) ) {
 				i++; j++;
 				continue;
 			}
 			else if ( ltParts[j].length() == 1 ) {
 				return false;
 			}
 			else {
 				j++;
 			}
 		}
 	
 		return true;
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
 //				List<Code> codes = frag.getCodes();
 //				for ( Code code : codes ) {
 //					// Escape the data of the new inline code (and only them)
 //					if ( code.getType().equals(InlineCodeFinder.TAGTYPE) ) {
 //						if ( cfEncoder == null ) {
 //							cfEncoder = getEncoderManager().getEncoder();
 //							//TODO: We should use the proper output encoding here, not force UTF-8, but we do not know it
 //							cfEncoder.setOptions(params, "utf-8", lineBreak);
 //						}
 //						code.setData(cfEncoder.encode(code.getData(), EncoderContext.TEXT));
 //					}
 //				}
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
 				if ( isContextTranslatable() ) {
 					frag = new TextFragment();
 				}
 			}
 			return false;
 		}
 
 		// Create the unit
 		ITextUnit tu = new TextUnit(String.valueOf(++tuId));
 		tu.setMimeType(mimeType);
 		
 		tu.setSourceContent(frag);
 		
 		// ITS Localization Note
 		if ( !Util.isEmpty(context.peek().locNote) ) {
 			tu.setProperty(new Property(Property.NOTE, context.peek().locNote));
 		}
 		// ITS Domain
 		if ( !Util.isEmpty(context.peek().domains) ) {
 			tu.setProperty(new Property(Property.ITS_DOMAIN, context.peek().domains));
 		}
 		// ITS External resources Reference
 		if ( !Util.isEmpty(context.peek().externalRes) ) {
 			tu.setProperty(new Property(Property.ITS_EXTERNALRESREF, context.peek().externalRes));
 		}
 		// ITS Storage Size
 		if ( context.peek().storageSize != null ) {
 			tu.setProperty(new Property(Property.ITS_STORAGESIZE, context.peek().storageSize));
 		}
 		// ITS Allowed characters
 		if ( context.peek().allowedChars != null ) {
 			tu.setProperty(new Property(Property.ITS_ALLOWEDCHARACTERS, context.peek().allowedChars));
 		}
 		
 		// Set term info
 		if ( terms != null ) {
 			tu.getSource().setAnnotation(terms);
 			terms = null; // Reset for next time
 		}
 
 		// Set the resname value (or null, which is fine)
 		tu.setName(context.peek().idValue);
 		
 		// Set the information about preserving or not white-spaces
 		if ( isHTML5 ) {
 			if ( node.getNodeName().equals("pre") ) {
 				context.peek().preserveWS = true;
 			}
 		}
 		if ( context.peek().preserveWS ) {
 			tu.setPreserveWhitespaces(true);
 		}
 		else { // We laso unwrap if we don't have to preserve
 			tu.setPreserveWhitespaces(false);
 			tu.getSource().unwrap(false, true);
 		}
 		
 		skel.addContentPlaceholder(tu);
 		if ( popStack ) {
 			context.pop();
 			skel.add(buildEndTag(node));
 		}
 		tu.setSkeleton(skel);
 		queue.add(new Event(EventType.TEXT_UNIT, tu));
 		frag = null;
 		if ( popStack && isContextTranslatable() ) {
 			frag = new TextFragment();
 		}
 		skel = new GenericSkeleton();
 		return true;
 	}
 
 }
