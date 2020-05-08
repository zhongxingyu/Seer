 /*===========================================================================*/
 /* Copyright (C) 2008 Yves Savourel                                          */
 /*---------------------------------------------------------------------------*/
 /* This library is free software; you can redistribute it and/or modify it   */
 /* under the terms of the GNU Lesser General Public License as published by  */
 /* the Free Software Foundation; either version 2.1 of the License, or (at   */
 /* your option) any later version.                                           */
 /*                                                                           */
 /* This library is distributed in the hope that it will be useful, but       */
 /* WITHOUT ANY WARRANTY; without even the implied warranty of                */
 /* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   */
 /* General Public License for more details.                                  */
 /*                                                                           */
 /* You should have received a copy of the GNU Lesser General Public License  */
 /* along with this library; if not, write to the Free Software Foundation,   */
 /* Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA               */
 /*                                                                           */
 /* See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html */
 /*===========================================================================*/
 
 package net.sf.okapi.applications.rainbow.packages.ttx;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Reads a TTX file, where okapiTTX is the underlying format.
  */
 class TTXReader {
 
 	// Same as in Borneo database TSTATUS_* values
 	//TODO: need to define this list in a generic class, no Borneo
 	public static final int       STATUS_NOTRANS      = 0;
 	public static final int       STATUS_UNUSED       = 1;
 	public static final int       STATUS_TOTRANS      = 2;
 	public static final int       STATUS_TOEDIT       = 3;
 	public static final int       STATUS_TOREVIEW     = 4;
 	public static final int       STATUS_OK           = 5;
 	
 	private static final int      SOURCE              = 0x01;
 	private static final int      TARGET              = 0x02;
 	private static final int      BOTH                = 0x03;
 
 	protected TextUnit       item;
 	
 	private String           sourceLang;
 	private TextContainer    srcCont;
 	private TextContainer    trgCont;
 	private int              textType;
 	private NodeList         nodeList = null;
 	private Node             node;
 	private boolean          backTrack;
 	private Stack<Boolean>   m_stkFirstChildDone;
 	private Pattern          idPattern;
 	private int              inline;
 	private boolean          inText;
 
 	//TODO: Implement case for multiple file in single doc
 	public TTXReader () {
		// pattern to match: id = 'ID'
		idPattern = Pattern.compile("id\\s*?=\\s*?[\\\"\\'](.*?)[\\\"\\']");
 	}
 	
 	protected void finalize ()
 		throws Throwable
 	{
 	    try {
 	    	close();
 	    } finally {
 	        super.finalize();
 	    }
 	}
 
 	public void open (String p_sPath) {
 		try {
 			close();
 			DocumentBuilderFactory Fact = DocumentBuilderFactory.newInstance();
 			Fact.setValidating(false);
 			Document Doc = Fact.newDocumentBuilder().parse(new File(p_sPath));
 			m_stkFirstChildDone = new Stack<Boolean>();
 			m_stkFirstChildDone.push(true); // For #document root
 			node = Doc.getDocumentElement();
 			m_stkFirstChildDone.push(false);
 		}
 		catch ( SAXException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( ParserConfigurationException e ) {
 			throw new RuntimeException(e);
 		}
 		catch ( IOException e ) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void close () {
 		// Make list available for GC
 		if ( nodeList != null ) nodeList = null;
 		inText = false;
 	}
 
 	public boolean readItem () {
 		resetItem();
 		while ( true ) {
 			if ( !nextNode() ) {
 				return false; // Document is done
 			}
 			String name = getName();
 			if ( name.equals("ut") ) {
 				if ( processOuterUT() ) return true;
 			}
 			else if ( name.equals("Tuv") ) {
 				processTUV();
 			}
 			else if ( name.equals("Tu") ) {
 				processTU();
 			}
 			else if ( name.equals("UserSettings") ) {
 				processUserSettings();
 			}
 			else if ( inText ) {
 				switch ( node.getNodeType() ) {
 				case Node.TEXT_NODE:
 				case Node.CDATA_SECTION_NODE:
 					String tmp = node.getTextContent();
 					if ( (textType & SOURCE) == SOURCE ) srcCont.append(tmp);
 					if ( (textType & TARGET) == TARGET ) trgCont.append(tmp);
 					break;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Gets the name of the current node or an empty string.
 	 * @return The name, or an empty string if the node is null or not an element.
 	 */
 	private String getName () {
 		if ( node == null ) return "";
 		if ( node.getNodeType() != Node.ELEMENT_NODE ) return "";
 		return node.getNodeName();
 	}
 	
 	private boolean nextNode () {
 		if ( node != null ) {
 			backTrack = false;
 			if ( !m_stkFirstChildDone.peek() && node.hasChildNodes() ) {
 				// Change the flag for the current node
 				m_stkFirstChildDone.push(!m_stkFirstChildDone.pop());
 				// Get the new node and push its flag
 				node = node.getFirstChild();
 				m_stkFirstChildDone.push(false);
 			}
 			else {
 				Node TmpNode = node.getNextSibling();
 				if ( TmpNode == null ) {
 					node = node.getParentNode();
 					m_stkFirstChildDone.pop();
 					backTrack = true;
 				}
 				else {
 					node = TmpNode;
 					m_stkFirstChildDone.pop(); // Remove flag for previous sibling 
 					m_stkFirstChildDone.push(false); // Set new flag for new sibling
 				}
 			}
 		}
 		return (node != null);
 	}
 	
 	private void resetItem () {
 		item = new TextUnit();
 	}
 	
 	private void processUserSettings () {
 		sourceLang = ((Element)node).getAttribute("SourceLanguage");
 	}
 
 	private boolean processOuterUT () {
 		boolean result = false;
 		String text = node.getTextContent().trim();
 		if ( text.indexOf("<u ") == 0 ) {
 			inText = true;
 			textType = SOURCE;
 			srcCont = new TextContainer(item);
 			trgCont = new TextContainer(item);
			Matcher m = idPattern.matcher(text);
			if ( m.find() ) {
 				item.setIsTranslatable(false);
				item.setID(m.group(1));
 			}
 			else throw new RuntimeException("ID value not found for <u> element: "+text);
 		}
 		else if ( text.equals("</u>") ) {
 			inText = false;
 			item.setSourceContent(srcCont);
 			if ( !trgCont.isEmpty() ) {
 				item.setTargetContent(trgCont);
 			}
 			// If <ut> contains a </u> tag, that's the end of the item
 			result = true;
 		}
 		// Then move to the closing tag
 		while ( true ) {
 			if ( !nextNode() || getName().equals("ut") ) 
 				return result;
 		}
 	}
 
 	private void processTU () {
 		textType = BOTH;
 	}
 	
 	private void processTUV () {
 		String lang = ((Element)node).getAttribute("Lang");
 		if ( lang.equals(sourceLang) ) textType = SOURCE;
 		else textType = TARGET;
 		inline = 0;
 		processContent("Tuv");
 	}
 	
 	private void processContent (String container) {
 		// Check if it's empty
 		if ( !node.hasChildNodes() ) {
 			return;
 		}
 		
 		while ( nextNode() ) {
 			switch ( node.getNodeType() ) {
 			case Node.TEXT_NODE:
 			case Node.CDATA_SECTION_NODE:
 				if ( inline == 0 ) {
 					String tmp = node.getTextContent();
 					if ( (textType & SOURCE) == SOURCE ) srcCont.append(tmp);
 					if ( (textType & TARGET) == TARGET ) trgCont.append(tmp);
 				}
 				break;
 
 			case Node.ELEMENT_NODE:
 				String name = node.getNodeName();
 				if ( name.equals(container) ) {
 					// End return
 					return;
 				}
 				if ( backTrack ) {
 					if ( name.equals("ut") ) inline--;
 					continue;
 				}
 				// Else: It's a start of element
 				if ( name.equals("ut") ) {
 					//TODO: Handle open/close tags
 					appendCode(TagType.PLACEHOLDER, name);
 					inline++;
 				}
 				break;
 			}
 		}
 	}
 	
 	private void appendCode (TagType tagType,
 		String code)
 	{
 		if ( (textType & SOURCE) == SOURCE )
 			srcCont.append(tagType, code, code);
 		if ( (textType & TARGET) == TARGET )
 			trgCont.append(tagType, code, code);
 	}
 }
