 /*===========================================================================
   Copyright (C) 2010-2011 by the Okapi Framework contributors
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
 
 package net.sf.okapi.filters.idml;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.IParameters;
 import net.sf.okapi.common.IdGenerator;
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.MimeTypeMapper;
 import net.sf.okapi.common.UsingParameters;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.encoder.EncoderManager;
 import net.sf.okapi.common.exceptions.OkapiBadFilterInputException;
 import net.sf.okapi.common.exceptions.OkapiIOException;
 import net.sf.okapi.common.filters.FilterConfiguration;
 import net.sf.okapi.common.filters.IFilter;
 import net.sf.okapi.common.filters.IFilterConfigurationMapper;
 import net.sf.okapi.common.filterwriter.IFilterWriter;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.CodeSimplifier;
 import net.sf.okapi.common.resource.Ending;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.RawDocument;
 import net.sf.okapi.common.resource.StartDocument;
 import net.sf.okapi.common.resource.StartGroup;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.skeleton.ISkeletonWriter;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.ProcessingInstruction;
 import org.xml.sax.SAXException;
 
 @UsingParameters(Parameters.class)
 public class IDMLFilter implements IFilter {
 
 	private final static String MIMETYPE = "application/vnd.adobe.indesign-idml-package";
 	private final static String DOCID = "sd";
 	private final static String ENDID = "end";
 	private final static String SPREADTYPE = "spread";
 	private final static String STORYTYPE = "story";
 	private final static String EMBEDDEDSTORIES = "embedded-stories";
 	private final static CodeSimplifier SIMPLIFIER = new CodeSimplifier();
 	
 	final private DocumentBuilder docBuilder;
 	private URI docURI;
 	private LinkedList<Event> queue;
 	private LocaleId srcLoc;
 	private Parameters params;
 	private EncoderManager encoderManager;
 	private HashMap<String, ZipEntry> stories;
 	private LinkedHashMap<String, ArrayList<String>> spreads;
 	private ArrayList<String> storiesDone;
 	private Iterator<String> storyIter;
 	private Iterator<String> spreadIter;
 	private ZipFile zipFile;
 	private IdGenerator spreadIdGen;
 	private IdGenerator storyIdGen;
 	private int spreadStack;
 	private String tuIdPrefix;
 	private Stack<IDMLContext> ctx;
 	private HashMap<String, Boolean> embeddedElements;
 	private HashMap<String, Integer> embeddedElementsPos;
 	private IdGenerator refGen;
 	private IdGenerator tuIdGen;
 	
 	public IDMLFilter () {
 		try {
 			params = new Parameters();
 			DocumentBuilderFactory docFact = DocumentBuilderFactory.newInstance();
 			docFact.setValidating(false);
 			docBuilder = docFact.newDocumentBuilder();
 			
 			embeddedElements = new HashMap<String, Boolean>();
 			embeddedElements.put("Table", true);
 			embeddedElements.put("Footnote", true);
 			embeddedElements.put("Note", true);
 			// Create position holder for each
 			embeddedElementsPos = new HashMap<String, Integer>();
 			for ( String name : embeddedElements.keySet() ) {
 				embeddedElementsPos.put(name, -1);
 			}
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error initializing.\n"+e.getMessage(), e);
 		}
 	}
 
 	@Override
 	public void cancel () {
 		// TODO
 	}
 
 	@Override
 	public void close () {
 		if ( zipFile != null ) {
 			try {
 				zipFile.close();
 			}
 			catch ( IOException e ) {
 				// Swallow it
 			}
 			zipFile = null;
 		}
 	}
 
 	@Override
 	public ISkeletonWriter createSkeletonWriter () {
 		return null; // There is no corresponding skeleton writer
 	}
 	
 	@Override
 	public IFilterWriter createFilterWriter () {
 		return new IDMLFilterWriter();
 	}
 
 	@Override
 	public EncoderManager getEncoderManager () {
 		if ( encoderManager == null ) {
 			encoderManager = new EncoderManager();
 			encoderManager.setMapping(MimeTypeMapper.XML_MIME_TYPE, "net.sf.okapi.common.encoder.XMLEncoder");
 		}
 		return encoderManager;
 	}
 
 	@Override
 	public String getName () {
 		return "okf_idml";
 	}
 
 	@Override
 	public String getDisplayName () {
 		return "IDML Filter";
 	}
 
 	@Override
 	public String getMimeType () {
 		return MIMETYPE;
 	}
 
 	@Override
 	public List<FilterConfiguration> getConfigurations () {
 		List<FilterConfiguration> list = new ArrayList<FilterConfiguration>();
 		list.add(new FilterConfiguration(getName(),
 			MIMETYPE,
 			getClass().getName(),
 			"IDML",
 			"Adobe InDesign IDML documents",
 			null,
 			".idml;"));
 		return list;
 	}
 	
 	@Override
 	public IParameters getParameters () {
 		return params;
 	}
 
 	@Override
 	public boolean hasNext () {
 		return ( queue != null );
 	}
 
 	@Override
 	public Event next () {
 		if ( queue == null ) return null;
 		if ( queue.size() > 0 ) {
 			return queue.poll();
 		}
 
 		// Get the next event
 		read();
 		// End process if needed
 		if ( queue.size() == 0 ) {
 			queue = null; // No more
 			Ending ending = new Ending("ed");
 			return new Event(EventType.END_DOCUMENT, ending);
 		}
 		// Else, return the next event that was read
 		return queue.poll();
 	}
 
 	@Override
 	public void open (RawDocument input) {
 		open(input, true);
 	}
 	
 	@Override
 	public void open (RawDocument input,
 		boolean generateSkeleton)
 	{
 		queue = null;
 		close();
 		docURI = input.getInputURI();
 		if ( docURI == null ) {
 			throw new OkapiBadFilterInputException("This filter supports only URI input.");
 		}
 		srcLoc = input.getSourceLocale();
 		spreadIdGen = new IdGenerator(null, "spr");
 		storyIdGen = new IdGenerator(null, "sto");
 
 		// Adjust options
 		embeddedElements.put("Note", this.params.getExtractNotes());
 		embeddedElementsPos.put("Note", -1);
 		
 		// Gather the spreads
 		gatherStories();
 
 		// Add the start document event
 		StartDocument sd = new StartDocument(DOCID);
 		sd.setEncoding("UTF-8", false);
 		sd.setName(docURI.getPath());
 		sd.setLocale(srcLoc);
 		sd.setMimeType(MIMETYPE);
 		sd.setLineBreak("\n");
 		sd.setFilterParameters(params);
 		sd.setFilterWriter(createFilterWriter());
 		// Add the skeleton
 		sd.setSkeleton(new IDMLSkeleton(zipFile));
 		// Create the start document event
 		queue = new LinkedList<Event>();
 		queue.add(new Event(EventType.START_DOCUMENT, sd));
 		
 		// Point to the first spread
 		if ( spreads.size() > 0 ) {
 			spreadIter = spreads.keySet().iterator();
 			if ( spreadIter.hasNext() ) {
 				String spreadName = spreadIter.next();
 				storyIter = spreads.get(spreadName).iterator();
 				StartGroup sg = new StartGroup(DOCID, spreadIdGen.createId());
 				queue.add(new Event(EventType.START_GROUP, sg));
 				sg.setName(spreadName);
 				if ( spreadName.equals(EMBEDDEDSTORIES) ) {
 					sg.setId(EMBEDDEDSTORIES);
 				}
 				else {
 					sg.setType(SPREADTYPE);
 				}
 				spreadStack++;
 			}
 		}
 	}
 	
 	@Override
 	public void setFilterConfigurationMapper (IFilterConfigurationMapper fcMapper) {
 		// TODO (if needed)
 	}
 
 	@Override
 	public void setParameters (IParameters params) {
 		this.params = (Parameters)params;
 	}
 
 	private void read () {
 		if ( spreadIter == null ) return; // No content
 		while ( true ) {
 			// Check for next story in the current spread
 			if ( storyIter.hasNext() ) {
 				// At least one more story to process
 				processStory(storyIter.next());
 				return;
 			}
 			
 			// Otherwise: close the previous spread if needed
 			if ( spreadStack > 0 ) {
 				Ending ending = new Ending(spreadIdGen.getLastId()+ENDID);
 				queue.add(new Event(EventType.END_GROUP, ending));
 				spreadStack--;
 			}
 			
 			// Then try the next spread
 			if ( spreadIter.hasNext() ) {
 				String spreadName = spreadIter.next();
 				storyIter = spreads.get(spreadName).iterator();
 				StartGroup sg = new StartGroup(DOCID, spreadIdGen.createId());
 				sg.setName(spreadName);
 				if ( spreadName.equals(EMBEDDEDSTORIES) ) {
 					sg.setId(EMBEDDEDSTORIES);
 				}
 				else {
 					sg.setType(SPREADTYPE);
 				}
 				queue.add(new Event(EventType.START_GROUP, sg));
 				spreadStack++;
 			}	
 			else {
 				// Else: nothing else
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * Gathers all the stories to process, for each spread.
 	 */
 	private void gatherStories () {
 		spreadIter = null;
 		storyIter = null;
 		spreads = new LinkedHashMap<String, ArrayList<String>>();
 		storiesDone = new ArrayList<String>();
 		stories = new HashMap<String, ZipEntry>();
 		try {
 			zipFile = new ZipFile(new File(docURI));
 			Enumeration<? extends ZipEntry> entries = zipFile.entries();
 			while( entries.hasMoreElements() ) {
 				ZipEntry entry = entries.nextElement();
 				if ( entry.getName().endsWith(".xml") ) {
 					if ( entry.getName().startsWith("Spreads/") ||
 						( entry.getName().startsWith("MasterSpreads/") && params.getExtractMasterSpreads() )) {
 						// Gather stories from the spread
 						gatherStoriesInSpread(entry);
 					}
 					else if ( entry.getName().startsWith("Stories/") ) {
 						// Gather stories from the story itself (embedded in TextFrame)
 						gatherStoriesInStory(entry);
 						// Add the entry to the lookup list
 						String storyId = Util.getFilename(entry.getName(), false);
 						int p = storyId.indexOf('_');
 						if ( p > -1 ) storyId = storyId.substring(p+1);
 						stories.put(storyId, entry);
 					}
 				}
 			}
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException("Error while gathering stories.", e);
 		}
 	}
 
 	/**
 	 * Gather all the stories used in this spread.
 	 * @param entry the zip entry for the spread.
 	 * @return the total number of stories in the given spread.
 	 */
 	private int gatherStoriesInSpread (ZipEntry entry)
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		ArrayList<String> storyList = new ArrayList<String>();
 		Document doc = docBuilder.parse(zipFile.getInputStream(entry));
 
 		String name = entry.getName();
 		
 		NodeList list = doc.getElementsByTagName("TextFrame");
 		for ( int i=0; i<list.getLength(); i++ ) {
 			Element tf = (Element)list.item(i);
 			String tmp = tf.getAttribute("ParentStory");
 			if ( Util.isEmpty(tmp) ) {
 				throw new IOException("Missing value for parentStory.");
 			}
 			// Add the the story to the lookup list
 			if ( !storiesDone.contains(tmp) ) {
 				storyList.add(tmp);
 				storiesDone.add(tmp);
 			}
 		}
 		
		list = doc.getElementsByTagName("TextPath");
		for ( int i=0; i<list.getLength(); i++ ) {
			Element tf = (Element)list.item(i);
			String tmp = tf.getAttribute("ParentStory");
			if ( Util.isEmpty(tmp) ) {
				throw new IOException("Missing value for parentStory.");
			}
			// Add the the story to the lookup list
			if ( !storiesDone.contains(tmp) ) {
				storyList.add(tmp);
				storiesDone.add(tmp);
			}
		}

 		// Add the stories for this spread to the overall list of stories to process
 		spreads.put(name, storyList);
 		// Return the number of stories in this spread
 		return storyList.size();
 	}
 
 	/**
 	 * Gather all the stories used in this story.
 	 * @param entry the zip entry for the story.
 	 */
 	private void gatherStoriesInStory (ZipEntry entry)
 		throws SAXException, IOException, ParserConfigurationException
 	{
 		ArrayList<String> storyList = new ArrayList<String>();
 		Document doc = docBuilder.parse(zipFile.getInputStream(entry));
 		
 		NodeList list = doc.getElementsByTagName("TextFrame");
 		for ( int i=0; i<list.getLength(); i++ ) {
 			Element tf = (Element)list.item(i);
 			String tmp = tf.getAttribute("ParentStory");
 			if ( Util.isEmpty(tmp) ) {
 				throw new IOException("Missing value for parentStory.");
 			}
 			// Add the the story to the lookup list
 			if ( !storiesDone.contains(tmp) ) {
 				storyList.add(tmp);
 				storiesDone.add(tmp);
 			}
 		}
 		
 		// If needed, add the stories for this story to the overall list of stories to process
 		if ( !storyList.isEmpty() ) {
 			ArrayList<String> existingList = spreads.get(EMBEDDEDSTORIES);
 			if ( existingList == null ) {
 				spreads.put(EMBEDDEDSTORIES, storyList);
 			}
 			else {
 				existingList.addAll(storyList);
 				spreads.put(EMBEDDEDSTORIES, existingList);
 			}
 		}
 	}
 
 	private void processStory (String storyId) {
 		ZipEntry entry = stories.get(storyId);
 		if ( entry == null ) {
 			throw new OkapiIOException("No story entry found for "+storyId);
 		}
 		try {
 			// Read the document in memory
 			Document doc = docBuilder.parse(zipFile.getInputStream(entry));
 			
 			// Start the story group
 			StartGroup sg = new StartGroup(spreadIdGen.getLastId(), storyIdGen.createId());
 			sg.setName(storyId);
 			sg.setType(STORYTYPE);
 			sg.setSkeleton(new IDMLSkeleton(entry, doc));
 			queue.add(new Event(EventType.START_GROUP, sg));
 			
 			// Prepare for traversal
 			tuIdPrefix = storyId+"-";
 			ctx = new Stack<IDMLContext>();
 			refGen = new IdGenerator(null);
 			tuIdGen = new IdGenerator(null);
 			Node topNode = doc.getDocumentElement();
 			ctx.push(new IDMLContext(false, topNode));
 
 			// Reset the embedded elements position
 			for ( String name : embeddedElementsPos.keySet() ) {
 				embeddedElementsPos.put(name, -1);
 			}
 			
 			// Traverse the story
 			processNodes(topNode);
 			
 			// End the story group
 			Ending ending = new Ending(storyIdGen.getLastId()+ENDID);
 			queue.add(new Event(EventType.END_GROUP, ending));
 		}
 		catch ( Throwable e ) {
 			throw new OkapiIOException(String.format("Error processing story file '%s'.\n"+e.getMessage(), storyId), e);
 		}
 	}
 	
 	private void processNodes (Node node) {
 		while ( node != null ) {
 			
 			if ( node.getNodeType() != Node.ELEMENT_NODE ) {
 				if ( ctx.peek().inScope() ) {
 					// Add to current entry if needed
 					switch ( node.getNodeType() ) {
 					case Node.TEXT_NODE:
 					case Node.CDATA_SECTION_NODE:
 						ctx.peek().addCode(node);
 						break;
 					default:
 						throw new OkapiIOException("Unexpected node type: "+node.getNodeType());
 					}
 				}
 				node = node.getNextSibling();
 				continue;
 			}
 			
 			// Else: it's an element
 			Element elem = (Element)node;
 			String name = elem.getNodeName();
 			
 			// Process before the children
 			if ( name.equals("Content") ) {
 				ctx.peek().addContent(elem);
 				node = elem.getNextSibling();
 				continue;
 			}
 			else if ( name.equals("ParagraphStyleRange") ) {
 				// Process the start, and continue or move on depending on the return
 				if ( doStartPSR(elem) ) {
 					node = elem.getNextSibling();
 					continue;
 				}
 			}
 			else if ( embeddedElements.containsKey(name) ) {
 				// Update the count for that element
 				embeddedElementsPos.put(name, embeddedElementsPos.get(name) + 1);
 				// Process the element
 				if ( ctx.peek().inScope() ) {
 					if ( embeddedElements.get(name) ) {
 						// Create the inline code that holds the reference
 						String key = refGen.createId();
 						ctx.peek().addCode(new Code(TagType.PLACEHOLDER, name,
 							String.format("<%s id=\"%s\"/>", IDMLSkeleton.NODEREMARKER, key)));
 						ctx.peek().addReference(key, makeNodeReference(node));
 						// Create the new context
 						ctx.push(new IDMLContext(true, node));
 					}
 					else { // Do not extract: Just use a node reference
 						String key = refGen.createId();
 						ctx.peek().addCode(new Code(TagType.PLACEHOLDER, name,
 							String.format("<%s id=\"%s\"/>", IDMLSkeleton.NODEREMARKER, key)));
 						ctx.peek().addReference(key, makeNodeReference(node));
 						// Moves to the next sibling
 						node = elem.getNextSibling();
 						continue;
 					}
 				}
 				else { // Not in scope
 					// Move to the next sibling
 					node = elem.getNextSibling();
 					continue;
 				}
 			}
 			else {
 				if ( ctx.peek().inScope() ) {
 					ctx.peek().addStartTag(elem);
 				}
 			}
 		
 			// Process the children (if any)
 			if ( elem.hasChildNodes() ) {
 				processNodes(elem.getFirstChild());
 			}
 			
 			// When coming back from the children
 			if ( name.equals("ParagraphStyleRange") ) {
 				// Trigger the text unit
 				if ( ctx.peek().addToQueue(queue) && params.getSimplifyCodes() ) {
 					// Try to simplify the inline codes if possible
 					// We can access the text this way because it's not segmented yet
 					ITextUnit tu = queue.getLast().getTextUnit();
 					TextFragment tf = tu.getSource().getFirstContent();
 					String[] res = SIMPLIFIER.simplifyAll(tf, true);
 					// Move the native data into the skeleton if needed
 					if ( res != null ) {
 						// Check if the new fragment is empty
 						if ( tu.getSource().isEmpty() ) {
 							// Remove from queue
 							queue.removeLast();
 						}
 						else {
 							IDMLSkeleton skel = (IDMLSkeleton)tu.getSkeleton();
 							skel.addMovedParts(res);
 						}
 					}
 				}
 				ctx.peek().leaveScope();
 			}
 			else if ( embeddedElements.containsKey(name) ) {
 				if ( ctx.peek().inScope() ) {
 					//TODO
 				}
 				ctx.pop();
 			}
 			else {
 				if ( ctx.peek().inScope() ) {
 					ctx.peek().addEndTag(elem);
 				}
 			}
 			
 			// Then move on to the next sibling
 			node = elem.getNextSibling();
 		}
 	}
 	
 	private NodeReference makeNodeReference (Node targetNode) {
 		String name = targetNode.getNodeName();
 		return new NodeReference(name, embeddedElementsPos.get(name));
 	}
 	
 	private String makeTuId () {
 		return tuIdPrefix+tuIdGen.createId();
 	}
 
 	/**
 	 * Processes the start of a ParagraphStyleRange
 	 * @param node the node of the current element.
 	 * @return true if the element has been dealt with, and the caller method should continue the loop with the next sibling,
 	 * false if the caller need to just continue down. 
 	 */
 	private boolean doStartPSR (Node node) {
 		NodeList list = ((Element)node).getElementsByTagName("Content");
 		if ( list.getLength() > 1 ) {
 			// Several content: no shortcut
 			ctx.peek().enterScope(node, makeTuId());
 			return false;
 		}
 		if ( list.getLength() == 1 ) {
 			// We have a single Content element
 			Element cnt = (Element)list.item(0);
 			// Create the text unit
 			ITextUnit tu = new TextUnit(makeTuId());
 			tu.setSourceContent(processContent(cnt, null));
 			tu.setSkeleton(new IDMLSkeleton(ctx.peek().getTopNode(), cnt)); // Merge directly on Content
 			// And add the new event to the queue
 			queue.add(new Event(EventType.TEXT_UNIT, tu));
 		}
 		// Else: we have no content
 		// In both case: move on to the next node
 		return true;
 	}
 
 	/**
 	 * Processes the content of a Content element.
 	 * @param content the Content node.
 	 * @param tf the text fragment where to put the content. Use null to create one.
 	 * @return the modified text fragment (may be a new one).
 	 */
 	static TextFragment processContent (Element content,
 		TextFragment tf)
 	{
 		if ( tf == null ) tf = new TextFragment();
 		// We assume only TEXT and PI nodes, no inner elements!
 		Node node = content.getFirstChild();
 		while ( node != null ) {
 			switch ( node.getNodeType() ) {
 			case Node.TEXT_NODE:
 				processText(tf, node.getNodeValue());
 				break;
 			case Node.PROCESSING_INSTRUCTION_NODE:
 				ProcessingInstruction pi = (ProcessingInstruction)node;
 				tf.append(TagType.PLACEHOLDER, "pi", String.format("<?%s %s?>", pi.getTarget(), pi.getTextContent()));
 				break;
 			default:
 				throw new OkapiIOException("Unexpected content in <Content>: "+node.getNodeType());
 			}
 			node = node.getNextSibling();
 		}
 		return tf;
 	}
 
 	static void processText (TextFragment dest,
 		String text)
 	{
 		for ( int i=0; i<text.length(); i++ ) {
 			char ch = text.charAt(i);
 			switch ( ch ) {
 			case '\u2028': // Forced line-break
 				dest.append(TagType.PLACEHOLDER, "lb", String.valueOf(ch));
 				break;
 			case '\u200b': // Discretionary line-break
 				dest.append(TagType.PLACEHOLDER, "lb-disc", String.valueOf(ch));
 				break;
 			case '\u2011': // Non-breaking hyphen
 				dest.append(TagType.PLACEHOLDER, "nb-hyph", String.valueOf(ch));
 				break;
 			case '\u202f': // Fixed-width non-breaking space
 				dest.append(TagType.PLACEHOLDER, "nbsp-fw", String.valueOf(ch));
 				break;
 			case '\u200a': // Hair space
 				dest.append(TagType.PLACEHOLDER, "sp-hair", String.valueOf(ch));
 				break;
 			case '\u2006': // Sixth space
 				dest.append(TagType.PLACEHOLDER, "sp-6th", String.valueOf(ch));
 				break;
 			case '\u2005': // Thin space
 				dest.append(TagType.PLACEHOLDER, "sp-4th", String.valueOf(ch));
 				break;
 			case '\u2004': // Quarter space
 				dest.append(TagType.PLACEHOLDER, "sp-3rd", String.valueOf(ch));
 				break;
 			case '\u2008': // Punctuation space
 				dest.append(TagType.PLACEHOLDER, "sp-punc", String.valueOf(ch));
 				break;
 			case '\u2009': // Thin space
 				dest.append(TagType.PLACEHOLDER, "sp-thin", String.valueOf(ch));
 				break;
 			case '\u2007': // Figure space
 				dest.append(TagType.PLACEHOLDER, "sp-fig", String.valueOf(ch));
 				break;
 			case '\u2001': // Flush space
 				dest.append(TagType.PLACEHOLDER, "sp-flush", String.valueOf(ch));
 				break;
 			case '\ufeff': // Text anchor (Not sure about this one, but for sure we don't want it in the text)
 				dest.append(TagType.PLACEHOLDER, "tx-anch", String.valueOf(ch));
 				break;
 			default:
 				dest.append(ch);
 				break;
 			}
 			
 		}
 	}
 
 }
