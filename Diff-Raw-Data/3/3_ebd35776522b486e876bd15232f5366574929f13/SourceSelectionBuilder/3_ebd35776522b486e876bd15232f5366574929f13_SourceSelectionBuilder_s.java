 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.jsp.selection;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.wst.sse.core.internal.provisional.IndexedRegion;
 import org.eclipse.wst.sse.core.internal.provisional.text.ITextRegion;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.eclipse.wst.xml.core.internal.document.AttrImpl;
 import org.eclipse.wst.xml.core.internal.document.ElementImpl;
 import org.eclipse.wst.xml.core.internal.provisional.document.IDOMElement;
 import org.w3c.dom.Attr;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 public class SourceSelectionBuilder {
 	StructuredTextEditor sourceEditor;
 
 	public SourceSelectionBuilder(StructuredTextEditor sourceEditor) {
 		this.sourceEditor = sourceEditor;
 	}
 
 	public SourceSelection getSelection() {
 		Point range = sourceEditor.getTextViewer().getSelectedRange();
 		int anchorPosition = range.x;
 		int focusPosition = range.x + range.y;
 		boolean extendFlag = range.y != 0;
 		boolean reversionFlag = extendFlag && anchorPosition == SelectionHelper.getCaretOffset(sourceEditor);
 		if (reversionFlag) {
 			anchorPosition = focusPosition;
 			focusPosition = range.x;
 		}
 		Node focusNode = getSourceNodeAt(this.sourceEditor,focusPosition);
 		if (focusNode == null) {
 			return null;
 		}
 
 		int focusOffset = getSourceNodeOffset(focusNode, focusPosition, !reversionFlag);
 		Node anchorNode = null;
 		int anchorOffset = 0;
 		if (extendFlag) {
 			anchorNode = getSourceNodeAt(this.sourceEditor,anchorPosition);
 			anchorOffset = getSourceNodeOffset(anchorNode, anchorPosition, reversionFlag);
 		} else {
 			anchorNode = focusNode;
 			anchorOffset = focusOffset;
 		}
 
 		SourceSelection selection = null;
 		if (reversionFlag) {
 			selection = new SourceSelection(focusNode, focusOffset, anchorNode, anchorOffset);
 		} else {
 			selection = new SourceSelection(anchorNode, anchorOffset, focusNode, focusOffset);
 		}
 		List selectedNodes = null;
 		if (focusNode instanceof IDOMElement) {
 			IDOMElement element = (IDOMElement)focusNode;
 			if (focusPosition < element.getEndStartOffset()) {
 				NamedNodeMap attrs = focusNode.getAttributes();
 				for (int i = 0; i < attrs.getLength(); i++) {
 					AttrImpl attr = (AttrImpl)attrs.item(i);
 					ITextRegion region = attr.getValueRegion();
 					if(region==null) {
 						break;
 					}
 					int attrStart = region.getStart();
 					int attrEnd = attrStart + attr.getValue().length();
 					if (range.x - ((ElementImpl)attr.getOwnerElement()).getStartOffset() - 1 >= attrStart && range.x - ((ElementImpl)attr.getOwnerElement()).getStartOffset() - 1 <= attrEnd) {
 						selectedNodes = new ArrayList();
 						selectedNodes.add(attr);
 						break;
 					}
 				}
 			}
 		}
 
 		if (selectedNodes == null) {
 			selectedNodes = SelectionHelper.getTextWidgetSelectedNodes(sourceEditor.getModel(), sourceEditor.getSelectionProvider());
 		}
 		if (selectedNodes != null && selectedNodes.size() == 1) {
 			Object node = selectedNodes.get(0);
 			if (node instanceof AttrImpl) {
 				AttrImpl attr = (AttrImpl)node;
 				int attrStart = attr.getValueRegion().getStart();
 				int attrEnd = attrStart + attr.getValue().length();
 				if (range.x - ((ElementImpl)attr.getOwnerElement()).getStartOffset() - 1 >= attrStart && range.x - ((ElementImpl)attr.getOwnerElement()).getStartOffset() - 1 <= attrEnd) {
 					selection.setFocusAttribute((Attr)node);
 					Point attrRange = new Point(range.x - (((ElementImpl)attr.getOwnerElement()).getStartOffset() + attr.getValueRegion().getStart()) - 1, range.y);
 					selection.setFocusAttributeRange(attrRange);
 				}
 			}
 		}
 		return selection;
 	}
 
 	public static Node getSourceNodeAt(StructuredTextEditor srcEditor,int offset) {
 		if (srcEditor != null && srcEditor.getModel() != null) {
 			IndexedRegion node = srcEditor.getModel().getIndexedRegion(offset);
 			if (node == null) {
 				node = srcEditor.getModel().getIndexedRegion(offset - 1);
 			}
 			if (node instanceof Node) {
 				return (Node) node;
 			}
 		}
 		return null;
 	}
 		 
 	private int getSourceNodeOffset(Node node, int pos, boolean endFlag) {
 		if (node == null) return 0;
 		int start = ((IndexedRegion)node).getStartOffset();
 		int end = ((IndexedRegion)node).getEndOffset();
 		
 		if (node.getNodeType() == Node.TEXT_NODE) {
 			if (pos < start) {
 				return 0;
 			} else if (pos > end) {
 				return end - start;
 			} else {
 				return pos - start;
 			}
 		} else if (node.getNodeType() == Node.ELEMENT_NODE) {
 			ElementImpl element = (ElementImpl)node;
 			if (element.isContainer()) {
 				if (pos < element.getStartEndOffset()) {
 					return 0;
 				} else {
 					if (element.hasEndTag() && pos <= element.getEndStartOffset()) {
 						return 2;
 					} else {
 						return 1;
 					}
 				}
 			} else {
 				return endFlag ? 1 : 0;
 			}
 		} else if (node.getNodeType() == Node.COMMENT_NODE) {
 			if (pos > end) {
 				pos = end;
 			}
 			int offset = pos - start - 4;
 			return offset < 0 ? 0 : offset;
 		} else {
 			return endFlag ? 1 : 0;
 		}
 	}
 }
