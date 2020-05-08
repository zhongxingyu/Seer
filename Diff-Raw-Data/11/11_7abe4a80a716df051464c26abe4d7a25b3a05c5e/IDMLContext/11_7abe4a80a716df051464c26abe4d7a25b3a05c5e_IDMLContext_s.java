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
 
 import java.util.List;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 import net.sf.okapi.common.Event;
 import net.sf.okapi.common.EventType;
 import net.sf.okapi.common.Util;
 import net.sf.okapi.common.resource.Code;
 import net.sf.okapi.common.resource.TextFragment;
 import net.sf.okapi.common.resource.ITextUnit;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextFragment.TagType;
 
 public class IDMLContext {
 	
 	private boolean inScope;
 	private Node topNode;
 	private Node scopeNode;
 //	private Node contentNode;
 	private TextFragment tf;
 	private int status;
 	private boolean isReferent;
 	private IDMLSkeleton skel;
 	private String tuId;
 	
 	private final static boolean phOnly = true;
 	
 	/**
 	 * Create a new context.
 	 * @param rootNode Node when starting embedded context. Should be null for the top-level context.
 	 */
 	public IDMLContext (boolean isReferent,
 		Node topNode)
 	{
 		this.isReferent = isReferent;
 		this.topNode = topNode;
 	}
 	
 	public void enterScope (Node scopeNode,
 		String tuId)
 	{
 		this.scopeNode = scopeNode;
 		this.tuId = tuId;
 		tf = new TextFragment();
 		status = 0;
 		inScope = true;
 	}
 
 	public Node getTopNode () {
 		return topNode;
 	}
 	
 	public Node getScopeNode () {
 		return scopeNode;
 	}
 	
 	public void leaveScope () {
 		inScope = false;
 		skel = null;
 	}
 	
 	public boolean inScope () {
 		return inScope;
 	}
 	
 	/**
 	 * Adds the text unit to the given queue.
 	 * @param queue the event queue where to add the event.
 	 * @return true if a text unit with possibly inline codes was added, false otherwise.
 	 */
 	public boolean addToQueue (List<Event> queue) {
 		if ( tf.isEmpty() ) return false; // Skip empty entries
 		
 //		if ( status == 1 ) {
 //			// Only one content: no need for inline codes
 //			// Reset the fragment to just the text
 //			tf = new TextFragment(TextFragment.getText(tf.getCodedText()));
 //			// Make the Content the top node
 //			scopeNode = contentNode;
 //		}
 		
 		// Create the text unit
 		ITextUnit tu = new TextUnit(tuId, null, isReferent);
 		tu.setSourceContent(tf);
 		if ( skel == null ) {
 			skel = new IDMLSkeleton(topNode, scopeNode);
 		}
 		tu.setSkeleton(skel);
 		// And add the new event to the queue
 		queue.add(new Event(EventType.TEXT_UNIT, tu));
 		// This object should not be called again
 		
 //		return (status != 1);
 		return true;
 	}
 	
 	/**
 	 * Adds a Content element to the text unit.
 	 * @param elem the Content element node.
 	 */
 	public void addContent (Element elem) {
 		if ( phOnly ) {
 			tf.append(TagType.PLACEHOLDER, "code", buildStartTag(elem));
 			IDMLFilter.processContent(elem, tf);
 			tf.append(TagType.PLACEHOLDER, "code", buildEndTag(elem));
 		}
 		else {
 			tf.append(TagType.OPENING, "code", buildStartTag(elem));
 			IDMLFilter.processContent(elem, tf);
 			tf.append(TagType.CLOSING, "code", buildEndTag(elem));
 		}
 		status++;
 //		contentNode = elem;
 	}
 	
 	public void addCode (Code code) {
 		tf.append(code);
 	}
 
 	public void addCode (Node node) {
 		if ( node.getNodeType() == Node.TEXT_NODE ) {
 			String text = node.getNodeValue();
 			for ( int i=0; i<text.length(); i++ ) {
 				if ( !Character.isWhitespace(text.charAt(i)) ) {
 					tf.append(TagType.PLACEHOLDER, "text", text);
 					return;
 				}
 			}
 			// Otherwise: just white spaces: no output
 		}
 		else if ( node.getNodeType() == Node.CDATA_SECTION_NODE ) {
 			tf.append(TagType.PLACEHOLDER, "cdata", "<![CDATA["+node.getNodeValue()+"]]>");
 		}
 	}
 	
 	public void addReference (String key,
 		NodeReference ref)
 	{
 		if ( skel == null ) {
 			skel = new IDMLSkeleton(topNode, scopeNode);
 		}
 		// Clone the node, so it's not deleted when merging the surrounding content
 		skel.addReferenceNode(key, ref);
 	}
 	
 	public void addStartTag (Element elem) {
 		if ( phOnly ) {
 			tf.append(TagType.PLACEHOLDER,
 				elem.getNodeName(), buildStartTag(elem));
 		}
 		else {
 			tf.append(elem.hasChildNodes() ? TagType.OPENING : TagType.PLACEHOLDER,
 				elem.getNodeName(), buildStartTag(elem));
 		}
 	}
 	
 	public void addEndTag (Element elem) {
 		if ( elem.hasChildNodes() ) {
 			if ( phOnly ) {
 				tf.append(TagType.PLACEHOLDER, elem.getNodeName(), buildEndTag(elem));
 			}
 			else {
 				tf.append(TagType.CLOSING, elem.getNodeName(), buildEndTag(elem));
 			}
 		}
 	}
 	
 	public String buildStartTag (Element elem) {
 		StringBuilder sb = new StringBuilder("<"+elem.getNodeName());
 		NamedNodeMap attrNames = elem.getAttributes();
 		for ( int i=0; i<attrNames.getLength(); i++ ) {
 			Attr attr = (Attr)attrNames.item(i);
 			sb.append(" " + attr.getName() + "=\"");
 			sb.append(Util.escapeToXML(attr.getValue(), 3, false, null));
 			sb.append("\"");
 		}
 		// Make it an empty element if possible
 		if ( elem.hasChildNodes() ) {
 			sb.append(">");
 		}
 		else {
 			sb.append("/>");
 		}
 		return sb.toString();
 	}
 	
 	public String buildEndTag (Element elem) {
		return "</"+elem.getNodeName()+">";
 	}
 
 }
