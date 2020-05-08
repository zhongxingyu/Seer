 /*******************************************************************************
  * Copyright (c) 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.pagedesigner.dom;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.gef.EditPart;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.core.internal.tld.IJSFConstants;
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.css2.ICSSStyle;
 import org.eclipse.jst.pagedesigner.css2.property.ICSSPropertyID;
 import org.eclipse.jst.pagedesigner.parts.ElementEditPart;
 import org.eclipse.jst.pagedesigner.parts.HTMLEditPartsFactory;
 import org.eclipse.jst.pagedesigner.parts.NodeEditPart;
 import org.eclipse.jst.pagedesigner.utils.HTMLUtil;
 import org.eclipse.jst.pagedesigner.validation.caret.Target;
 import org.eclipse.jst.pagedesigner.viewer.DesignPosition;
 import org.eclipse.jst.pagedesigner.viewer.DesignRange;
 import org.eclipse.swt.SWT;
 import org.eclipse.wst.sse.core.internal.provisional.INodeNotifier;
 import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * @author mengbo
  */
 public final class EditModelQuery {
 	private static Logger _log = PDPlugin.getLogger(EditModelQuery.class);
 
 	private static EditModelQuery _instance;
 
 	private static final int START_INDEX_BEFORE_TAG = 1;
 
 	private static final int END_INDEX_WITHIN_TAG = 2;
 
 	private static final HashSet SPECIAL_EMPTY_CHARS = new HashSet();
 
 	/**
 	 * Maps unicode Characters to html equivalents
 	 */
 	public static final HashMap CHAR_NODE_MAP = new HashMap();
 
 	// Cursor can't go outside of these container.
 	private static final HashSet HTML_CONSTRAINED_CONTAINERS = new HashSet();
 
 	/**
 	 * HTML tags that control style
 	 */
 	public static final HashSet HTML_STYLE_NODES = new HashSet();
 
 	static final HashSet UNREMOVEBLE_TAGS = new HashSet();
 
 	// Nodes that can hold other nodes.
 	static final String[] HTML_CONTAINER_NODES = {
 	//
 			IHTMLConstants.TAG_BODY, //
 			IHTMLConstants.TAG_HTML, //
 			IHTMLConstants.TAG_SPAN, //
 			IHTMLConstants.TAG_FORM, //
 			IHTMLConstants.TAG_P,//
 			IHTMLConstants.TAG_SPAN,//
 			IHTMLConstants.TAG_DIV,//
 			IHTMLConstants.TAG_LI,//
 			IHTMLConstants.TAG_OL,//
 			IHTMLConstants.TAG_UL //
 	};
 
 	static final String[] NON_HTML_CONTAINER_NODES = {
 			IJSFConstants.TAG_VIEW, //
 			IJSFConstants.TAG_PANELGRID, //
 			IJSFConstants.TAG_PANELGROUP, //
 			IJSFConstants.TAG_SUBVIEW };
 	static {
 		UNREMOVEBLE_TAGS.add(IHTMLConstants.TAG_HTML);
 		UNREMOVEBLE_TAGS.add(IHTMLConstants.TAG_HEAD);
 		UNREMOVEBLE_TAGS.add(IHTMLConstants.TAG_BODY);
 		EditModelQuery.CHAR_NODE_MAP.put(new Character(SWT.CR),
 				IHTMLConstants.TAG_BR);
 		EditModelQuery.CHAR_NODE_MAP.put(new Character(SWT.LF),
 				IHTMLConstants.TAG_BR);
 		EditModelQuery.SPECIAL_EMPTY_CHARS.add(" "); //$NON-NLS-1$
 		EditModelQuery.SPECIAL_EMPTY_CHARS.add("\t"); //$NON-NLS-1$
 		EditModelQuery.SPECIAL_EMPTY_CHARS.add("\r"); //$NON-NLS-1$
 		EditModelQuery.SPECIAL_EMPTY_CHARS.add("\n"); //$NON-NLS-1$
 		EditModelQuery.HTML_CONSTRAINED_CONTAINERS.add(IHTMLConstants.TAG_TD);
 		EditModelQuery.HTML_CONSTRAINED_CONTAINERS.add(IHTMLConstants.TAG_TR);
 		EditModelQuery.HTML_CONSTRAINED_CONTAINERS
 				.add(IHTMLConstants.TAG_TABLE);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_B);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_EM);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_H1);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_H2);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_H3);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_H4);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_H5);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_H6);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_A);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_U);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_I);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_S);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_STRONG);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_TT);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_BIG);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_SMALL);
 		EditModelQuery.HTML_STYLE_NODES.add(IHTMLConstants.TAG_FONT);
 	}
 
 	private EditModelQuery() {
         // no  external instantiation
 	}
 
 	/**
 	 * @return the singleton instance
 	 */
 	public static EditModelQuery getInstance() {
 		if (_instance == null) {
 			_instance = new EditModelQuery();
 		}
 		return _instance;
 	}
 
 	/**
 	 * Get previous sibling, or if sibling is null then get previous neighbor.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	public Node getPreviousNeighbor(Node node) {
 		if (!EditValidateUtil.validNode(node)) {
 			return null;
 		}
 		while (node != null && node.getNodeType() != Node.DOCUMENT_NODE
 				&& node.getPreviousSibling() == null) {
 			node = node.getParentNode();
 		}
 		return (node != null && node.getNodeType() != Node.DOCUMENT_NODE) ? node
 				.getPreviousSibling()
 				: null;
 	}
 
 	/**
 	 * Get privous sibling, or if sibling is null then get previous neighor's
 	 * rightmost child, which is adjacent to 'node'.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	public Node getPreviousLeafNeighbor(Node node) {
 		return getLastLeafChild(getPreviousNeighbor(node));
 	}
 
 	/**
 	 * Get next sibling, or if sibling is null get next neighbor.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	public Node getNextNeighbor(Node node) {
 		if (!EditValidateUtil.validNode(node)) {
 			return null;
 		}
 
 		while (node != null && node.getNodeType() != Node.DOCUMENT_NODE
 				&& node.getNextSibling() == null) {
 			node = node.getParentNode();
 		}
 		return (node != null && node.getNodeType() != Node.DOCUMENT_NODE) ? node
 				.getNextSibling()
 				: null;
 	}
 
 	/**
 	 * Get next sibling, or if sibling is null get next neighbor's leftmost leaf
 	 * child which will be adjacent to 'node'.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	public Node getNextLeafNeighbor(Node node) {
 		return getFirstLeafChild(getNextNeighbor(node));
 	}
 
 	/**
 	 * Get node's rightmost leaf child.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	private Node getLastLeafChild(Node node) {
 		if (node == null) {
 			return null;
 		}
 		if (node.getLastChild() != null) {
 			return getLastLeafChild(node.getLastChild());
 		}
         return node;
 	}
 
 	/**
 	 * Get node's leftmost leaf child.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	private Node getFirstLeafChild(Node node) {
 		if (node == null) {
 			return null;
 		}
 
 		if (node.getFirstChild() != null) {
 			return getFirstLeafChild(node.getFirstChild());
 		}
         return node;
 	}
 
 	/**
 	 * To see if node is within a indexed region that is started from 'start',
 	 * ended at 'end'
 	 */
 	static boolean within(int start, int end, Node theNode) {
 		return getNodeStartIndex(theNode) >= start
 				&& getNodeEndIndex(theNode) <= end;
 	}
 
 	/**
 	 * To see whether the 'position' is within indexed location (start, end)
 	 * 
 	 * @param start
 	 * @param end
 	 * @param position
 	 * @return
 	 */
 	 boolean within(int start, int end, IDOMPosition position) {
 		int pos = getIndexedRegionLocation(position);
 		return start <= pos && pos <= end;
 	}
 
 	/**
 	 * To see whether the 'theNode' is not within indexed region (start, end)
 	 * 
 	 * @param start
 	 * @param end
 	 * @param theNode
 	 * @return
 	 */
 	static boolean outOf(int start, int end, Node theNode) {
 		if (getNodeLenth(theNode) > 0) {
 			return getNodeStartIndex(theNode) >= end
 					|| getNodeEndIndex(theNode) <= start;
 		}
         return !((getNodeStartIndex(theNode) >= start && getNodeEndIndex(theNode) <= end));
 	}
 
 	/**
 	 * Determine whether the position is at node's edge. When the offset is at
 	 * edge, it is in the leftmost or rightmost offset of node's region.
 	 * 
 	 * @param position 
 	 * @param forward 
 	 * @return true if at edge
 	 */
 	boolean atEdge(IDOMPosition position, boolean forward) {
 		Node node = position.getContainerNode();
 		int offset = position.getOffset();
 		if (forward) {
 			if (EditModelQuery.isText(node)) {
 				return offset == node.getNodeValue().length();
 			}
             return offset == node.getChildNodes().getLength();
 		}
         return offset == 0;
 	}
 
 	/**
 	 * Get node's neighbor on the node tree, if forward, then get next,
 	 * otherwise go backward.
 	 * 
 	 * @param node
 	 * @param forward
 	 * @return the node
 	 */
 	Node getNeighbor(Node node, boolean forward) {
 		if (forward) {
 			return getNextNeighbor(node);
 		}
         return getPreviousNeighbor(node);
 	}
 
 	/**
 	 * Get neighbor which is descendent of root.
 	 * 
 	 * @param node
 	 * @param root
 	 * @return the node
 	 */
 	Node getPreviousNeighbor(Node node, Node root) {
 		if (!EditValidateUtil.validNode(node)) {
 			return null;
 		}
 		while (node != null && node != root
 				&& node.getNodeType() != Node.DOCUMENT_NODE
 				&& node.getPreviousSibling() == null) {
 			node = node.getParentNode();
 		}
 		return (node != null && node != root && node.getNodeType() != Node.DOCUMENT_NODE) ? node
 				.getPreviousSibling()
 				: null;
 	}
 
 	/**
 	 * Get neighbor which is descendent of root.
 	 * 
 	 * @param node
 	 * @param root
 	 * @return the node
 	 */
 	Node getNextNeighbor(Node node, Node root) {
 		if (!EditValidateUtil.validNode(node)) {
 			return null;
 		}
 
 		while (node != null && node != root
 				&& node.getNodeType() != Node.DOCUMENT_NODE
 				&& node.getNextSibling() == null) {
 			node = node.getParentNode();
 		}
 		return (node != null && node != root && node.getNodeType() != Node.DOCUMENT_NODE) ? node
 				.getNextSibling()
 				: null;
 	}
 
 	/**
 	 * Get neighbor which is descendent of root.
 	 * 
 	 * @param node
 	 * @param forward
 	 * @param root
 	 * @return the node
 	 */
 	 Node getNeighbor(Node node, boolean forward, Node root) {
 		Assert.isTrue(root != null);
 		if (forward) {
 			return getNextNeighbor(node, root);
 		}
         return getPreviousNeighbor(node, root);
 	}
 
 	/**
 	 * Get node's leaf child which is adjacent to 'node', according to
 	 * 'forward', it will search backward or forward.
 	 * 
 	 * @param node
 	 * @param forward
 	 * @return the node
 	 */
 	Node getLeafNeighbor(Node node, boolean forward) {
 		if (node == null) {
 			return null;
 		}
 		if (forward) {
 			return getNextLeafNeighbor(node);
 		}
         return getPreviousLeafNeighbor(node);
 	}
 
 	/**
 	 * Get neighbor's leaf child, which is adjacent to 'node'
 	 * 
 	 * @param node
 	 * @param childIndex
 	 * @param forward
 	 * @return the node
 	 */
 	 Node getLeafNeighbor(Node node, int childIndex, boolean forward) {
 		if (node == null) {
 			return null;
 		}
 		Node neighbor = getNeighbor(node, childIndex, forward);
 		if (neighbor != null) {
 			if (forward) {
 				return getFirstLeafChild(neighbor);
 			}
             return getLastLeafChild(neighbor);
 		}
 		return null;
 	}
 
 	/**
 	 * First try sibling, if it retruns null, try search neighbor.
 	 * 
 	 * @param parent
 	 * @param childIndex
 	 * @param forward
 	 * @return the node
 	 */
 	 Node getNeighbor(Node parent, int childIndex, boolean forward) {
 		if (!EditValidateUtil.validNode(parent)) {
 			return null;
 		}
 
 		NodeList nodeList = parent.getChildNodes();
 		if (nodeList != null && nodeList.getLength() > 0) {
 			if (nodeList.getLength() < childIndex) {
 				return null;
 			}
 			Node childNode = null;
 			if (!forward) {
 				--childIndex;
 			}
 			childNode = nodeList.item(childIndex);
 			if (childNode != null) {
 				return childNode;
 			}
             return getNeighbor(parent, forward);
 		}
         if (parent.getNodeType() == Node.TEXT_NODE) {
         	return getNeighbor(parent, forward);
         }
         return null;
 	}
 
 	/**
 	 * To see whether the textSelection start and end are on the same.
 	 * 
 	 * @param model
 	 * @param textSelection
 	 * @return the node
 	 */
 	 static boolean isSame(IStructuredModel model,
 			TextSelection textSelection) {
 		if (model != null && textSelection != null) {
 			int t1 = textSelection.getOffset();
 			int t2 = textSelection.getLength() + t1;
 			return model.getIndexedRegion(t1) == model.getIndexedRegion(t2);
 		}
 		return false;
 	}
 
 	/**
 	 * To see if the range and text selection covered the same range.
 	 * 
 	 * @param model
 	 * @param range
 	 * @param textSelection
 	 * @return true if same
 	 */
 	static boolean isSame(IStructuredModel model, DesignRange range,
 			TextSelection textSelection) {
 		if (model != null && range != null && textSelection != null) {
 			int t1 = textSelection.getOffset();
 			int t2 = textSelection.getLength() + t1;
 			int r1 = getIndexedRegionLocation(DOMRangeHelper.toDOMRange(range)
 					.getStartPosition());
 			int r2 = getIndexedRegionLocation(DOMRangeHelper.toDOMRange(range)
 					.getEndPosition());
 			return (model.getIndexedRegion(t1) == model.getIndexedRegion(r1) && //
 					model.getIndexedRegion(t2) == model.getIndexedRegion(r2))
 					|| (model.getIndexedRegion(t2) == model
 							.getIndexedRegion(r1) && //
 					model.getIndexedRegion(t1) == model.getIndexedRegion(r2));
 		}
 		return false;
 	}
 
 	/**
 	 * To see whether the selection is single point.
 	 * 
 	 * @param textSelection
 	 * @return true if same point
 	 */
 	 static boolean isSamePoint(TextSelection textSelection) {
 		return textSelection.getLength() == 0;
 	}
 
 	/**
 	 * To see whether two IDOMPosition are pointed to a same location.
 	 * 
 	 * @param p1
 	 * @param p2
 	 * @return true if same
 	 */
 	public static boolean isSame(IDOMPosition p1, IDOMPosition p2) {
 		if (p1 == p2
 				|| (p1.getContainerNode() == p2.getContainerNode() && p1
 						.getOffset() == p2.getOffset())) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * To see whether the range's start and end position are pointed to a same
 	 * location.
 	 * 
 	 * @param range
 	 * @return true if is same
 	 */
 	public static boolean isSame(DOMRange range) {
 		EditValidateUtil.validRange(range);
 		return isSame(range.getStartPosition(), range.getEndPosition());
 	}
 
 	/**
 	 * @param range
 	 * @return true if same
 	 */
 	public static boolean isSame(DesignRange range) {
 		return isSame(range.getStartPosition(), range.getEndPosition());
 	}
 
 	/**
 	 * @param p1
 	 * @param p2
 	 * @return true if same
 	 */
 	public static boolean isSame(DesignPosition p1, DesignPosition p2) {
 		if (p1 == p2) {
 			return true;
 		}
 		if (p1.getContainerNode() == p2.getContainerNode()
 				&& p1.getOffset() == p2.getOffset()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @param p1
 	 * @param p2
 	 * @return true if p1 and p2 are within same text node
 	 */
 	final boolean isWithinSameText(IDOMPosition p1, IDOMPosition p2) {
 		if (p1 == null || p2 == null) {
 			return false;
 		}
 		return p1.isText() && p2.isText()
 				&& p1.getContainerNode() == p2.getContainerNode();
 	}
 
 	/**
 	 * Get the node absolute start location in its residing IStructuredModel.
 	 * 
 	 * @param p
 	 * @return the location
 	 */
 	public static int getIndexedRegionLocation(IDOMPosition p) {
 		if (!EditValidateUtil.validPosition(p)) {
 			return -1;
 		}
 		Node parent = p.getContainerNode();
 		if (p.isText()) {
 			return ((IndexedRegion) parent).getStartOffset() + p.getOffset();
 		}
         int index = p.getOffset();
         if (!parent.hasChildNodes()) {
         	// Element:
         	if (!isDocument(parent)) {
         		IStructuredDocumentRegion region = ((IDOMNode) parent)
         				.getStartStructuredDocumentRegion();
         		return region.getEnd();
         	}
             // Document node:
             int offset = ((IndexedRegion) parent).getStartOffset();
             return offset;
         }
         NodeList children = parent.getChildNodes();
         // After rightmost child
         if (children.getLength() == index) {
         	if (!isDocument(parent)) {
         		int pos = getNodeEndNameStartIndex(parent);
         		return pos;
         	}
             int offset = ((IndexedRegion) parent).getEndOffset();
             return offset;
         }
         Node node = children.item(index);
         return ((IndexedRegion) node).getStartOffset();
 	}
 
 	/**
 	 * To determine whether the position is at the edge of a node. TODO: temp
 	 * func for later combination
 	 * @param nodePos 
 	 * 
 	 * @param position
 	 * @param left 
 	 * @return true if linked
 	 */
 	boolean isLinked(IDOMPosition nodePos, IDOMPosition position,
 			boolean left) {
 		int index = getIndexedRegionLocation(position);
 		if (left) {
 			int pos = getIndexedRegionLocation(nodePos);
 			return pos == index;
 		}
         Node node = null;
         int end;
         if (nodePos.isText()) {
         	node = nodePos.getContainerNode();
         	end = ((IndexedRegion) node).getEndOffset();
         } else {
         	node = nodePos.getNextSiblingNode();
         	Assert.isTrue(node != null);
         	end = ((IndexedRegion) node).getEndOffset();
         }
         return end == index;
 	}
 
 	/**
 	 * To see if the location is at the node's indexed pos, posType can be
 	 * START_INDEX_BEFORE_TAG, END_INDEX_WITHIN_TAG When location is at these
 	 * two position, webtools returns the container tag name, so we need to know
 	 * these.
 	 * 
 	 * @param location
 	 * @param node
 	 * @return true if at edge
 	 */
 	boolean isAtNodeNameEdge(int location, Node node, int posType) {
 		int start = getNodeEndNameStartIndex(node);
 		return location == start;
 	}
 
 	/**
 	 * @param location
 	 * @param node
 	 * @return true if at edge
 	 */
 	public boolean isAtNodeNameEdge(int location, Node node) {
 		return isAtNodeNameEdge(location, node, START_INDEX_BEFORE_TAG)
 				|| isAtNodeNameEdge(location, node, END_INDEX_WITHIN_TAG);
 	}
 
 	/**
 	 * If text only contains chars '\r' or '\n', it is considered to be
 	 * transparent.
 	 * 
 	 * @param node
 	 * @return true if transparent text
 	 */
 	 public static boolean isTransparentText(Node node) {
 		// should valid non null?
 		Assert.isTrue(node != null);
 		if (node == null || !isText(node)) {
 			return false;
 		}
 		if (!EditValidateUtil.validText(node)) {
 			return false;
 		}
 
 		Text text = (Text) node;
 		if (text.getLength() == 0) {
 			return true;
 		}
 		String value = text.getNodeValue();
 		int i = 0;
 		while (i < value.length() && HTMLUtil.isHTMLWhitespace(value.charAt(i))) {
 			i++;
 		}
 		return i == value.length();
 	}
 
 	/**
 	 * Get node index in its parent's children.
 	 * 
 	 * @param node
 	 * @return the node index or -1 if not found
 	 */
 	 static int getNodeIndex(Node node) {
 		EditValidateUtil.validNode(node);
 		Node parent = node.getParentNode();
 		int index = 0;
 		for (Node child = parent.getFirstChild(); child != null; child = child
 				.getNextSibling()) {
 			if (child == node) {
 				return index;
 			}
 			index++;
 		}
 		return -1; // error
 	}
 
 	/**
 	 * If parent has more than one children of which each node's localName is
 	 * the same as of 'node', return the index of 'node' in the same type
 	 * children list.
 	 * 
 	 * @param node
 	 * @return the index
 	 */
 	 public int getSameTypeNodeIndex(Node node) {
 		EditValidateUtil.validNode(node);
 		int i = 0;
 		while (node != null) {
 			Node sibling = node.getPreviousSibling();
 			if (sibling != null && sibling.getLocalName() != null
 					&& sibling.getLocalName().equals(node.getLocalName())) {
 				i++;
 			}
 			node = sibling;
 		}
 		return i; // error
 	}
 
 	/**
 	 * Start from position, skip transparent chars, and returns the first
 	 * non-transparent char's index. based on 'forward', go previously or next .
 	 * 
 	 * @param value
 	 * @param position
 	 * @param forward
 	 * @return the position
 	 */
 	 int getNextConcretePosition(String value, int position,
 			boolean forward) {
 		if (value == null) {
 			return -1;
 		}
 		if (value.length() == 0) {
 			return 0;
 		}
 		// validate
 		Assert.isTrue(position >= 0 && position <= value.length());
 		int i = -1;
 		if (forward) {
 			i = position;
 			while (i < value.length()
 					&& (value.charAt(i) == SWT.CR || value.charAt(i) == SWT.LF)) {
 				i++;
 			}
 			return i;
 		}
         i = position - 1;
         while (i >= 0
         		&& (value.charAt(i) == SWT.CR || value.charAt(i) == SWT.LF)) {
         	i--;
         }
         return i + 1;
 	}
 
 	/**
 	 * Get two nodes' lowest common ancestor.
 	 * 
 	 * @param node1
 	 * @param node2
 	 * @return the node
 	 */
 	 public Node getCommonAncestor(Node node1, Node node2) {
 		if (node1 == null || node2 == null) {
 			return null;
 		}
 
 		for (Node na = node1; na != null; na = na.getParentNode()) {
 			for (Node ta = node2; ta != null; ta = ta.getParentNode()) {
 				if (ta == na)
 					return ta;
 			}
 		}
 		return null; // not found
 	}
 
 	/**
 	 * Get lowest common ancestor of two IDOMPositions' container nodes..
 	 * 
 	 * @param p1
 	 * @param p2
 	 * @return the nodeh
 	 */
 	public Node getCommonAncestor(IDOMPosition p1, IDOMPosition p2) {
 		Node n1 = p1.getContainerNode();
 		Node n2 = p2.getContainerNode();
 		return getCommonAncestor(n1, n2);
 	}
 
 	/**
 	 * Get lowest ancestor of a 'node' which is block type.
 	 * 
 	 * @param node
 	 * @return the node
 	 */
 	 Node getBlockAncestor(Node node) {
 		if (!EditValidateUtil.validNode(node)) {
 			return null;
 		}
 		while (node != null && isChild(IHTMLConstants.TAG_BODY, node, true)) {
 			if (isBlockNode(node)) {
 				return node;
 			}
 			node = node.getParentNode();
 		}
 		return null;
 	}
 
 	/**
 	 * To see whether a node is block type.
 	 * 
 	 * @param node
 	 * @return true if is a block node
 	 */
 	public static boolean isBlockNode(Node node) {
 		return !isInline(node);
 	}
 
 	/**
 	 * @param node
 	 * @return true if is table cell
 	 */
 	static boolean isTableCell(Node node) {
 		if (node instanceof INodeNotifier) {
 			Object adapter = ((INodeNotifier) node)
 					.getAdapterFor(ICSSStyle.class);
 			if (adapter != null) {
 				ICSSStyle style = (ICSSStyle) adapter;
 				String display = style.getDisplay();
 				return display.equalsIgnoreCase(ICSSPropertyID.VAL_TABLE_CELL);
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * To see if a node's display type is inline.
 	 * 
 	 * @param refNode 
 	 * @return true if is inline
 	 */
 	public static boolean isInline(Node refNode) {
 		Node node = refNode;
 		EditPart part = Target.resolvePart(node);
 		if (part instanceof ElementEditPart) {
 			node = ((ElementEditPart) part).getTagConvert().getResultElement();
 		}
 		if (isText(node)) {
 			return true;
 		} else if (node instanceof INodeNotifier) {
 			Object adapter = ((INodeNotifier) node)
 					.getAdapterFor(ICSSStyle.class);
 			if (adapter != null) {
 				ICSSStyle style = (ICSSStyle) adapter;
 				String display = style.getDisplay();
 				return (display.equalsIgnoreCase(ICSSPropertyID.VAL_INLINE)
 						|| //
 						display
 								.equalsIgnoreCase(ICSSPropertyID.VAL_INLINE_TABLE)
 						|| //
 						display.equalsIgnoreCase(ICSSPropertyID.VAL_COMPACT) || //
 				display.equalsIgnoreCase(ICSSPropertyID.VAL_RUN_IN));
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param node
 	 * @return true if is list item
 	 */
 	public static boolean isListItem(Node node) {
 		if (node instanceof INodeNotifier) {
 			Object adapter = ((INodeNotifier) node)
 					.getAdapterFor(ICSSStyle.class);
 			if (adapter != null) {
 				ICSSStyle style = (ICSSStyle) adapter;
 				String display = style.getDisplay();
 				return (display.equalsIgnoreCase(ICSSPropertyID.VAL_LIST_ITEM));
 			}
 		}
 		return false;
 	}
 
 	/**
      * The net effect of this method is that (subject to flags), true
      * is returned if 'node' is the descendant (not really a child)
      * of a tag with a local name in the list of 'names'.
      * 
      * TODO C.B: I hate method.  Need to rename and possibly rewrite
      * 
 	 * @param names -- check this list for valid local names
 	 * @param node -- the node to check
 	 * @param ignoreCase -- if true, each node name has toLowerCase applied to
      * it before checking for it names.  NOTE: this assumes that names is already
      * in lowercase.  TODO: this is crappy assumption
 	 * @param noSame -- if true, then node is skipped and only its parent nodes are
      * checked
 	 * @return true if the local name of node or one of its parents
      * is in the array of Strings called names.
 	 */
 	public static boolean isChild(final String names[], Node node,
 			boolean ignoreCase, boolean noSame) {
 		if (node == null) {
 			return false;
 		}
 		if (noSame) {
 			node = node.getParentNode();
 		}
         
         final List namesAsList = Arrays.asList(names);
         
 		while (node != null && !isDocument(node)) {
 			String nodeName = node.getLocalName();
 
 			if (nodeName != null)
             {
 			    if (ignoreCase)
                 {
 			        nodeName = nodeName.toLowerCase();
                 }
                 
                 if (namesAsList.contains(nodeName))
                 {
                     return true;
                 }
 			}
             Node oldNode = node;
 			node = node.getParentNode();
             if (oldNode == node)
             {
                 throw new IllegalStateException("Infinite loop discovered in DOM tree"); //$NON-NLS-1$
             }
 		}
 		return false;
 	}
 
 	/**
 	 * Determine whether a node is a child of node that is named as 'name', if
 	 * the node itself is named as 'name' return true also.
 	 * 
 	 * @param name
 	 * @param node
 	 * @param ignoreCase 
 	 * @return true if is child
 	 */
 	public static boolean isChild(String name, Node node, boolean ignoreCase) {
 		if (node == null) {
 			return false;
 		}
 
 		while (node != null && node.getNodeType() != Node.DOCUMENT_NODE) {
 			String nodeName = node.getLocalName();
 			if (nodeName != null
 					&& (ignoreCase && name.equalsIgnoreCase(nodeName) || !ignoreCase
 							&& name.equalsIgnoreCase(nodeName))) {
 				return true;
 			}
 			node = node.getParentNode();
 		}
 		return false;
 	}
 
 	/**
 	 * To see whether 'node' is 'ancestor's child.
 	 * 
 	 * @param ancestor
 	 * @param node
 	 * @return true if is child
 	 */
 	public static boolean isChild(Node ancestor, Node node) {
 		if (node == null || ancestor == null) {
 			return false;
 		}
 
 		if (isDocument(ancestor)) {
 			return true;
 		}
 		while (node != null && !isDocument(ancestor)) {
			if (node == null) {
				break;
			}
 			if (node == ancestor) {
 				return true;
 			}
 			node = node.getParentNode();
 		}
 		return false;
 	}
 
 	/**
 	 * Get next sibling node to position's container node.
 	 * 
 	 * @param position
 	 * @return the node
 	 */
 	Node getNextSibling(IDOMPosition position) {
 		if (position.isText()) {
 			return position.getContainerNode().getNextSibling();
 		}
         return position.getNextSiblingNode();
 	}
 
 	/**
 	 * Get previous sibling node to position's container node.
 	 * 
 	 * @param position
 	 * @return the node
 	 */
 	Node getPreviousSibling(IDOMPosition position) {
 		if (position.isText()) {
 			return position.getContainerNode().getPreviousSibling();
 		}
         return position.getPreviousSiblingNode();
 	}
 
 	/**
 	 * Get position's container node's parent.
 	 * 
 	 * @param position
 	 * @return the parent node
 	 */
 	Node getParent(IDOMPosition position) {
 		if (position.isText()) {
 			return position.getContainerNode().getParentNode();
 		}
         return position.getContainerNode();
 	}
 
 	/**
 	 * Get node's sibling according to 'forward' direction
 	 * 
 	 * @param node
 	 * @param forward
 	 * @return the node
 	 */
 	public Node getSibling(Node node, boolean forward) {
 		EditValidateUtil.validNode(node);
 		if (forward) {
 			return node.getNextSibling();
 		}
         return node.getPreviousSibling();
 	}
 
 	/**
 	 * Get position's container node's sibling.
 	 * 
 	 * @param position
 	 * @param forward
 	 * @return the node
 	 */
 	public Node getSibling(IDOMPosition position, boolean forward) {
 		if (forward) {
 			return getNextSibling(position);
 		}
         return getPreviousSibling(position);
 	}
 
 	/**
 	 * Get position's container node's editable items number. this is temp
 	 * functions for future use.
 	 * 
 	 * @param position
 	 * @return the size
 	 */
 	int getSize(IDOMPosition position) {
 		EditValidateUtil.validPosition(position);
 		if (position.isText()) {
 			return ((Text) position.getContainerNode()).getLength();
 		}
         if (position.getContainerNode().hasChildNodes()) {
         	return position.getContainerNode().getChildNodes().getLength();
         }
         return 0;
 	}
 
 	/**
 	 * Valid position and return text, if it contains text node.
 	 * 
 	 * @param position
 	 * @return the text
 	 */
 	public Text getText(IDOMPosition position) {
 		if (position.isText()) {
 			if (position.getContainerNode() != null) {
 				return (Text) position.getContainerNode();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param node
 	 * @return the document for  node
 	 */
 	public static Document getDocumentNode(Node node) {
 		if (node != null) {
 			return isDocument(node) ? (Document) node : node.getOwnerDocument();
 		}
 		return null;
 	}
 
 	/**
 	 * To see whether a node is empty, here we can insert rules to see whether
 	 * it is empty, for delete operation, it could be deleted.
 	 * 
 	 * @param node
 	 * @return true if node empty
 	 */
 	static boolean isEmptyNode(Node node) {
 		if (node.getNodeType() == Node.TEXT_NODE) {
 			return isTransparentText(node);
 		}
 		if (node.getChildNodes() == null
 				|| node.getChildNodes().getLength() == 0) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * To see whther a node is text node.
 	 * 
 	 * @param node
 	 * @return true if the node is a text node
 	 */
 	public static boolean isText(Node node) {
 		return node != null && node.getNodeType() == Node.TEXT_NODE;
 	}
 
 	/**
 	 * To see whether a node is Document node.
 	 * 
 	 * @param node
 	 * @return true if the node is a doc node
 	 */
 	public static boolean isDocument(Node node) {
 		return node != null && node.getNodeType() == Node.DOCUMENT_NODE;
 	}
 
 	/**
 	 * Get style from parent node. from first paret 'firstF', we will traverse
 	 * the tree up untile reaching Document node, get all style node's, we may
 	 * insert rules here to stop the search at a before paricular node. Style
 	 * nodes could <b>, <u>...
 	 * 
 	 * @param children
 	 * @param firstF
 	 */
 	void assignFather(Vector children, Node firstF) {
 		if (children.size() == 0) {
 			return;
 		}
 		if (firstF != null && !isDocument(firstF)) {
 			String name = firstF.getNodeName();
 			// To see whether it is a style node that is our anticipated node.
 			if (name != null && HTML_STYLE_NODES.contains(name.toLowerCase())) {
 				Node newParent = firstF.cloneNode(false);
 				while (children.size() > 0) {
 					newParent.appendChild((Node) children.remove(0));
 				}
 				children.add(newParent);
 			}
 			assignFather(children, firstF.getParentNode());
 		}
 	}
 
 	/**
 	 * Get a node that is at Indexed position 'pos' in 'model'.
 	 * 
 	 * @param model
 	 * @param pos
 	 * @return the
 	 */
 	IndexedRegion getPosNode(IStructuredModel model, int pos) {
 		IndexedRegion inode = model.getIndexedRegion(pos);
 		return inode;
 	}
 
 	/**
 	 * If the pos is at right edge within container.
 	 * 
 	 * @param node
 	 * @param pos
 	 * @return true is at right edge
 	 */
 	boolean isAtRightMostWithin(Node node, int pos) {
 		return getNodeEndNameStartIndex(node) == pos;
 	}
 
 	/**
 	 * Create the node, if 'refNode' is null, then position is at the edge of
 	 * 'container'. otherwize calculate refNode's related index in its parent's
 	 * children list and return DOMPosition.
 	 * 
 	 * @param container
 	 * @param refNode
 	 * @param forward  
 	 * @return the dom position
 	 */
 	IDOMPosition createDomposition(Node container, Node refNode,
 			boolean forward) {
 		if (refNode == null) {
 			if (forward && container.hasChildNodes()) {
 				return new DOMPosition(container, container.getChildNodes()
 						.getLength());
 			}
             return new DOMPosition(container, 0);
 		}
         Assert.isTrue(refNode.getParentNode() == container);
         int index = getNodeIndex(refNode);
         if (!forward) {
         	index++;
         }
         return new DOMPosition(container, index);
 	}
 
 	/**
 	 * @param fModel
 	 * @param textSelection
 	 * @return the design range
 	 */
 	static DesignRange convertToDesignRange(IStructuredModel fModel,
 			TextSelection textSelection) {
 		int start = textSelection.getOffset();
 		int end = textSelection.getLength() + start;
 		IDOMPosition startDomPos = EditModelQuery.getInstance()
 				.createDomposition((IDOMModel) fModel, start, false);
 		IDOMPosition endDomPos = EditModelQuery.getInstance()
 				.createDomposition((IDOMModel) fModel, end, false);
 		if (startDomPos == null) {
 			startDomPos = endDomPos;
 		} else if (endDomPos == null) {
 			endDomPos = startDomPos;
 		}
 		if (startDomPos != null) {
 			DesignPosition startPos = null, endPos = null;
 			startPos = DOMPositionHelper.toDesignPosition(startDomPos);
 			endPos = DOMPositionHelper.toDesignPosition(endDomPos);
 			if (startPos != null) {
 				return new DesignRange(startPos, endPos);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Create IDOMPosition based on Indexed 'position' in model. If node at
 	 * position is text, use position to calculate DOMPosition offset,
 	 * otherwize, simply create position pointed to container's children list's
 	 * edge.
 	 * 
 	 * @param model
 	 * @param position
 	 * @param adjust 
 	 * @return the dom position
 	 */
 	public IDOMPosition createDomposition(IDOMModel model, int position,
 			boolean adjust) {
 		return createDomposition1(model, position, adjust);
 	}
 
 	/**
 	 * Create IDOMPosition based on Indexed 'position' in model. If node at
 	 * position is text, use position to calculate DOMPosition offset,
 	 * otherwize, simply create position pointed to container's children list's
 	 * edge.
 	 * 
 	 * @param model
 	 * @param position
 	 * @param adjust 
 	 * @return the dom position
 	 */
 	public IDOMPosition createDomposition1(IDOMModel model, int position,
 			boolean adjust) {
 		try {
 			// get the container
 			Object object = getPosNode(model, position);
 			if (object == null && position > 0) {
 				// The end of file?
 				object = getPosNode(model, position - 1);
 			}
 			Node container = null;
 			if (object == null) {
 				// empty file?
 				return new DOMPosition(model.getDocument(), 0);
 			}
 			container = (Node) object;
 			Object oppNode = getPosNode(model, position - 1);
 			if (oppNode != null
 					&& !EditModelQuery.isChild((Node) oppNode, container)
 					&& //
 					!EditModelQuery.isInline(container)
 					&& EditModelQuery.isInline((Node) oppNode)) {
 				container = (Node) oppNode;
 			}
 			int location = EditHelper.getInstance().getLocation(container,
 					position, false);
 			IDOMPosition result = null;
 			switch (location) {
 			case 1:
 			case 2:
 				result = new DOMRefPosition(container, false);
 				break;
 			case 4:
 			case 5:
 				result = new DOMRefPosition(container, true);
 				break;
 			case 3:
 				if (EditModelQuery.isText(container)) {
 					result = new DOMPosition(container, position
 							- EditModelQuery.getNodeStartIndex(container));
 				} else {
 					result = new DOMPosition(container, container
 							.getChildNodes().getLength());
 				}
 			}
 			return result;
 		} catch (Exception e) {
 			// "Error in position creation"
 			_log.error("Error.EditModelQuery.0" + e); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	/**
 	 * Calculate node's Indexed length in model.
 	 * 
 	 * @param node
 	 * @return the node length
 	 */
 	public static int getNodeLenth(Node node) {
 		if (node != null
                && EditValidateUtil.validNode(node)) {
 			return ((IndexedRegion) node).getEndOffset()
 					- ((IndexedRegion) node).getStartOffset();
 		}
         return 0;
 	}
 
 	/**
 	 * Return 'node' indexed start position Example: |<a></a>, the position is
 	 * indicated by '|'
 	 * 
 	 * @param node
 	 * @return the start index
 	 */
 	public static int getNodeStartIndex(Node node) {
 		if (EditValidateUtil.validNode(node) && node instanceof IndexedRegion) {
 			return ((IndexedRegion) node).getStartOffset();
 		}
 		return -1;
 	}
 
 	/**
 	 * Return 'node' indexed end position Example: <a></a>|, the position is
 	 * indicated by '|'
 	 * 
 	 * @param node
 	 * @return the end index
 	 */
 	public static int getNodeEndIndex(Node node) {
 		if (EditValidateUtil.validNode(node) && node instanceof IndexedRegion) {
 			return ((IndexedRegion) node).getEndOffset();
 		}
 		return -1;
 	}
 
 	/**
 	 * Get node at indexed position.
 	 * 
 	 * @param model 
 	 * @param position
 	 * @return the node at position
 	 */
 	static Node getNodeAt(IStructuredModel model, int position) {
 		try {
 			IndexedRegion region = model.getIndexedRegion(position);
 			if (region instanceof Node) {
 				return (Node) region;
 			}
 			return null;
 		} catch (Exception e) {
 			// "Error in region node creation"
 			_log.error("Error.EditModelQuery.1", e); //$NON-NLS-1$
 			return null;
 		}
 	}
 
 	/**
 	 * Return 'node' indexed start name's end position Example: <a>|aaa </a>,
 	 * the position is indicated by '|'
 	 * 
 	 * @param node
 	 * @return the index
 	 */
 	public static int getNodeStartNameEndIndex(Node node) {
 		if (isText(node)) {
 			return getNodeStartIndex(node);
 		}
 		if (EditValidateUtil.validNode(node) && node instanceof IDOMNode) {
 			IStructuredDocumentRegion region = ((IDOMNode) node)
 					.getStartStructuredDocumentRegion();
 			if (region != null) {
 				return region.getEndOffset();
 			}
 			// else
 			// {
 			// // if (node.hasChildNodes())
 			// // {
 			// // // Node should always have start name, so this part should
 			// never reach,
 			// // // the assert is for inner debug.
 			// // Assert.isTrue(false);
 			// // return getNodeStartIndex(node);
 			// // }
 			// }
 		}
 		// This should never happen.
 		return getNodeStartIndex(node);
 	}
 
 	/**
 	 * Return 'node' indexed end name' start position Example: <a>aaa| </a>, the
 	 * position is indicated by '|' If node is <a /> style or there is no </a>
 	 * to pair with <a>, the function return -1.
 	 * 
 	 * @param node
 	 * @return the start index
 	 */
 	public static int getNodeEndNameStartIndex(Node node) {
 		if (isText(node)) {
 			return getNodeEndIndex(node);
 		}
 		if (EditValidateUtil.validNode(node) && node instanceof IDOMNode) {
 			IStructuredDocumentRegion region = ((IDOMNode) node)
 					.getEndStructuredDocumentRegion();
 			if (region != null) {
 				return region.getStartOffset();
 			}
 			// else
 			// {
 			// if (node.hasChildNodes())
 			// {
 			// return getNodeEndIndex(node);
 			// }
 			// }
 		}
 		return getNodeEndIndex(node);
 	}
 
 	/**
 	 * To see if a node is <a/>style.
 	 * 
 	 * @param node
 	 * @return the single region node
 	 */
 	public boolean isSingleRegionNode(Node node) {
 		if (getNodeEndNameStartIndex(node) == getNodeEndIndex(node)
 				&& !node.hasChildNodes()) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * To see if a node has child that is not transparent child only.
 	 * 
 	 * @param node
 	 * @return true if node has transparent children
 	 */
 	boolean hasNonTransparentChild(Node node) {
         NodeList children = node.getChildNodes();
         for (int i = 0, n = children.getLength(); i < n; i++) {
         	Object child = children.item(i);
         	if (isText((Node) child)) {
         		if (!isTransparentText((Node) child)) {
         			return true;
         		}
         	} else {
         		return true;
         	}
         }
 		return false;
 	}
 
 	/**
 	 * To see if a node has child that is not transparent child only.
 	 * 
 	 * @param node
 	 * @param excludes 
 	 * @return true if has transparent child
 	 */
 	public boolean hasNonTransparentChild(Node node, String[] excludes) {
 		if (!node.hasChildNodes()) {
 			return false;
 		}
         NodeList children = node.getChildNodes();
         for (int i = 0, n = children.getLength(); i < n; i++) {
         	Object child = children.item(i);
         	if (isText((Node) child)) {
         		if (!isTransparentText((Node) child)) {
         			return true;
         		}
         	} else if (!Arrays.asList(excludes).contains(
         			((Node) child).getLocalName())) {
         		return true;
         	}
         }
 		return false;
 	}
 
 	/**
 	 * To see whether tag has whitespace char.
 	 * 
 	 * @param node
 	 * @return true if has whitespace neighbor
 	 */
 	public boolean hasWhitespaceNeighbor(Node node) {
 		node = getNeighbor(node, true);
 		if (isWidget(node)) {
 			return false;
 		}
         node = getFirstLeafChild(node);
         return isTransparentText(node);
 
 	}
 
 	/**
 	 * @param host
 	 * @return true if host is a widget
 	 */
 	public static boolean isWidget(Object host) {
 		boolean result = false;
 		EditPart part = null;
 		if (host instanceof EditPart) {
 			part = (EditPart) host;
 		} else if (host instanceof Node) {
 			part = Target.resolvePart((Node) host);
 			if (part == null) {
 				part = new HTMLEditPartsFactory(
 						(IDOMDocument) getDocumentNode((Node) host))
 						.createEditPart(null, host);
 			}
 		}
 		if (part instanceof NodeEditPart) {
 			result = ((NodeEditPart) part).isWidget();
 		}
 		return result;
 	}
 
 	/**
 	 * To combind whitespace chars, only one whitespace string should be create.
 	 * 
 	 * @param node
 	 * @return true if node is  redundant whitespace
 	 */
 	boolean isRedundantWightspaces(Node node) {
 		if (isTransparentText(node) && hasWhitespaceNeighbor(node)) {
 			return true;
 		}
         return false;
 	}
 
 	/**
 	 * @param node
 	 * @param names
 	 * @param ignoreCase
 	 * @return true if node has ancestors in the name list
 	 * TODO: bad practice
 	 */
 	public static boolean hasAncestor(Node node, String names[],
 			boolean ignoreCase) {
 		Assert.isTrue(names != null);
 		while (node != null && !EditModelQuery.isDocument(node)) {
 			if (isElement(node))
 				if (containItem(names, node, ignoreCase)) {
 					return true;
 				}
 			node = node.getParentNode();
 		}
 		return false;
 	}
 
 	/**
 	 * To see if 'node' has ancestor that has name as 'name'
 	 * 
 	 * @param node
 	 * @param name
 	 * @param ignoreCase
 	 * @return true if node has the named ancestor
 	 */
 	public static boolean hasAncestor(Node node, String name, boolean ignoreCase) {
 		Assert.isTrue(name != null);
 		while (node != null && !EditModelQuery.isDocument(node)) {
 			if (node.getNodeName() != null)
 				if ((ignoreCase && name.equalsIgnoreCase(node.getNodeName())) || //
 						(!ignoreCase && name.equals(node.getNodeName()))) {
 					return true;
 				}
 			node = node.getParentNode();
 		}
 		return false;
 	}
 
 	/**
 	 * To see if 'node' has direct ancestors that has names listed in 'name[]'
 	 * 
 	 * @param node
 	 * @param top
 	 * @param ignoreCase
 	 * @return the list of ancestors
 	 */
 	public static List getAncestors(Node node, String top, boolean ignoreCase) {
 		List result = new ArrayList();
 		Assert.isTrue(node != null);
 		while (node != null && !EditModelQuery.isDocument(node)) {
 			result.add(node);
 			String name = node.getLocalName();
 			if (ignoreCase && top.equalsIgnoreCase(name) || //
 					(!ignoreCase && top.equals(name))) {
 				break;
 			}
 			node = node.getParentNode();
 		}
 		return result;
 	}
 
 	/**
 	 * Copy old node's children to newNode.If the newNode is the father of the
 	 * old node,then the old node's children will be inserted before the old
 	 * node,otherwise,the old node's children just append to the newNode.
 	 * 
 	 * @param old
 	 * @param newNode
 	 */
 	public static void copyChildren(Node old, Node newNode) {
 		Node child = old.getFirstChild();
 		while (child != null) {
 			Node next = child.getNextSibling();
 			child = old.removeChild(child);
 			if (old.getParentNode() == newNode) {
 				newNode.insertBefore(child, old);
 			} else {
 				newNode.appendChild(child);
 			}
 			child = next;
 		}
 	}
 
 	/**
 	 * @param node
 	 * @return true if node is an element
 	 */
 	private static boolean isElement(Node node) {
 		return node.getNodeType() == Node.ELEMENT_NODE;
 	}
 
 	/**
 	 * Return a offspring of ancestor, the offsprint has a name listed in
 	 * childrenNames.
 	 * 
 	 * @param ancestor
 	 * @param childrenNames
 	 * @param maxLevelToSearch
 	 *            the max level from ancestor to the offspring in family tree.
 	 * @param ignoreCase
 	 * @return the node
 	 */
 	public static Node getChild(Node ancestor, String childrenNames[],
 			int maxLevelToSearch, boolean ignoreCase) {
 		if (ancestor == null || maxLevelToSearch < 0) {
 			return null;
 		}
         if (ancestor.getLocalName() != null
         		&& ignoreCase
         		&& Arrays.asList(childrenNames).contains(
         				ancestor.getLocalName().toLowerCase())
         		|| !ignoreCase
         		&& Arrays.asList(childrenNames).contains(
         				ancestor.getLocalName())) {
         	return ancestor;
         }
 		NodeList children = ancestor.getChildNodes();
 		for (int i = 0, n = children.getLength(); i < n; i++) {
 			Node result = getChild(children.item(i), childrenNames,
 					maxLevelToSearch - 1, ignoreCase);
 			if (result != null) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Return a offspring of ancestor, the nodes on the tree are type of
 	 * DeferredElementImpl, the offsprint has a name listed in childrenNames.
 	 * 
 	 * @param ancestor
 	 * @param childrenNames
 	 * @param maxLevelToSearch
 	 *            the max level from ancestor to the offspring in family tree.
 	 * @param ignoreCase
 	 * @return the node
 	 */
 	static Node getChildDeferredNode(Node ancestor,
 			String childrenNames[], int maxLevelToSearch, boolean ignoreCase) {
 		if (ancestor == null || maxLevelToSearch < 0) {
 			return null;
 		}
         String nodeName = ancestor.getNodeName();
         if (nodeName != null && ignoreCase
         		&& Arrays.asList(childrenNames).contains(nodeName)
         		|| !ignoreCase
         		&& Arrays.asList(childrenNames).contains(nodeName)) {
         	return ancestor;
         }
 		NodeList children = ancestor.getChildNodes();
 		for (int i = 0, n = children.getLength(); i < n; i++) {
 			Node result = getChildDeferredNode(children.item(i), childrenNames,
 					maxLevelToSearch - 1, ignoreCase);
 			if (result != null) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param node
 	 * @return if has tranparent node only
 	 */
 	public static boolean hasTransparentNodeOnly(Node node) {
 		NodeList children = node.getChildNodes();
 		for (int i = 0, n = children.getLength(); i < n; i++) {
 			if (!EditModelQuery.isTransparentText(children.item(i))) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * @param name
 	 * @param node
 	 * @param ignoreCase
 	 * @return the node
 	 */
 	public static Node getParent(String name, Node node, boolean ignoreCase) {
 		if (node == null) {
 			return null;
 		}
 
 		while (node != null && node.getNodeType() != Node.DOCUMENT_NODE) {
 			String nodeName = node.getLocalName();
 			if (nodeName != null
 					&& (ignoreCase && name.equalsIgnoreCase(nodeName) || !ignoreCase
 							&& name.equalsIgnoreCase(nodeName))) {
 				return node;
 			}
 			node = node.getParentNode();
 		}
 		return null;
 	}
 
 	/**
 	 * get Elements with the same localName as the input localName under the
 	 * rootNode,it is a recursive computation.
 	 * 
 	 * @param rootNode
 	 * @param localName
 	 * @param caseSensitive
 	 * @param list
 	 *            The input list to hold the matched elements.
 	 */
 	public static void getElementByLocalName(Node rootNode, String localName,
 			boolean caseSensitive, List list) {
 		if (list == null) {
 			return;
 		}
 		NodeList nodeList = rootNode.getChildNodes();
 		if (nodeList != null && nodeList.getLength() > 0) {
 			for (int i = 0, size = nodeList.getLength(); i < size; i++) {
 				Node node = nodeList.item(i);
 				if (node.getNodeType() == Node.ELEMENT_NODE) {
 					String nodeLocalName = node.getLocalName();
 					if (caseSensitive && localName.equals(nodeLocalName)) {
 						list.add(node);
 					} else if (!caseSensitive
 							&& localName.equalsIgnoreCase(nodeLocalName)) {
 						list.add(node);
 					}
 					getElementByLocalName(node, localName, true, list);
 				}
 
 			}
 		}
 	}
 
 	/**
 	 * @param tags
 	 * @param node
 	 * @param ignoreCase
 	 * @return the true if contains item
 	 */
 	public static boolean containItem(String[] tags, Node node,
 			boolean ignoreCase) {
 		if (ignoreCase) {
 			for (int i = 0, size = tags.length; i < size; i++) {
 				if (tags[i] == null) {
 					continue;
 				}
 				if (tags[i].equalsIgnoreCase(node.getNodeName())) {
 					return true;
 				}
 			}
 		} else {
 			for (int i = 0, size = tags.length; i < size; i++) {
 				if (tags[i] == null) {
 					continue;
 				}
 				if (tags[i].equals(node.getNodeName())) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
