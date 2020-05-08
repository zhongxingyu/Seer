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
 package org.eclipse.jst.pagedesigner.commands.range;
 
 import java.util.Stack;
 import java.util.Vector;
 
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.jst.jsf.common.ui.internal.logging.Logger;
 import org.eclipse.jst.jsf.core.internal.tld.IJSFConstants;
 import org.eclipse.jst.pagedesigner.IHTMLConstants;
 import org.eclipse.jst.pagedesigner.PDPlugin;
 import org.eclipse.jst.pagedesigner.dom.DOMPosition;
 import org.eclipse.jst.pagedesigner.dom.DOMPositionHelper;
 import org.eclipse.jst.pagedesigner.dom.DOMRange;
 import org.eclipse.jst.pagedesigner.dom.DOMRefPosition;
 import org.eclipse.jst.pagedesigner.dom.EditHelper;
 import org.eclipse.jst.pagedesigner.dom.EditModelQuery;
 import org.eclipse.jst.pagedesigner.dom.IDOMPosition;
 import org.eclipse.jst.pagedesigner.validation.caret.ActionData;
 import org.eclipse.jst.pagedesigner.validation.caret.IETablePositionRule;
 import org.eclipse.jst.pagedesigner.validation.caret.InlineEditingNavigationMediator;
 import org.eclipse.jst.pagedesigner.viewer.LayoutPart;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 /**
  * @author mengbo
  */
 public class DeleteEdit extends DesignEdit {
 	private static final Logger _log = PDPlugin.getLogger(DeleteEdit.class);
 
 	private static final boolean INNER_DEBUG = false;
 
 	private boolean _forward;
 
 	Vector deleted = new Vector();
 
 	/**
 	 * @param range
 	 * @param viewer
	 * @param forward 
 	 */
 	public DeleteEdit(DOMRange range, GraphicalViewer viewer, boolean forward) {
 		super(range, viewer);
 		_forward = forward;
 	}
 
	/**
	 * @param range
	 * @param viewer
	 */
 	public DeleteEdit(DOMRange range, GraphicalViewer viewer) {
 		super(range, viewer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.commands.range.DesignEdit#operate()
 	 */
 	protected boolean operate() {
 		try {
 			boolean result = true;
 			if (EditModelQuery.isSame(getRange())) {
 				deleteSingle();
 			} else {
 				deleteRange();
 			}
 			return result;
 		} catch (Exception e) {
 			_log.error("Exception", e);
 			return false;
 		}
 	}
 
 	/**
 	 * Delete one node or char at operation position.
 	 */
 	private void deleteSingle() {
 		IDOMPosition opPosition = getOperationPosition();
 		opPosition = EditHelper.moveToNextEditPosition(getOperationPosition(),
 				_forward, new InlineEditingNavigationMediator(new ActionData(
 						ActionData.INLINE_EDIT, null)));
 		// for inner debug
 		if (INNER_DEBUG) {
 			_log.info("EditHelper, now we are at:" + opPosition);
 		}
 		if (EditModelQuery.isSame(opPosition, getOperationPosition())) {
 			return;
 		}
         setRange(new DOMRange(opPosition, getOperationPosition()));
         deleteRange();
 	}
 
	/**
	 * @return the stack
	 */
 	protected Stack deleteRange() {
 		WorkNode root = getRootWorkNode();
 		Node rootNode = root.getNode();
 		Stack result = getProcessedResult();
 		collectOtherStyles(rootNode, result);
 		return result;
 	}
 
 	private boolean isTableComponents(WorkNode node) {
 		String name = node.getNode().getNodeName();
 		return (IHTMLConstants.TAG_TD.equalsIgnoreCase(name) || //
 				IHTMLConstants.TAG_TH.equalsIgnoreCase(name) || //
 				IHTMLConstants.TAG_TR.equalsIgnoreCase(name) || // 
 				IHTMLConstants.TAG_THEAD.equalsIgnoreCase(name) || //
 				IHTMLConstants.TAG_TBODY.equalsIgnoreCase(name) || //
 		IHTMLConstants.TAG_TFOOT.equalsIgnoreCase(name));
 	}
 
 	private Node processContainerTable(WorkNode node) {
 		Node result = null;
 		if ((isTableComponents(node) || IHTMLConstants.TAG_TABLE
 				.equalsIgnoreCase(node.getNode().getNodeName())) //
				&& new IETablePositionRule(null).isInValidTable(node
 						.getNode())) {
 			result = node.getNode().cloneNode(false);
 		}
 		return result;
 	}
 
 	private Node processContainerStyleNodes(WorkNode node) {
 		String name = node.getNode().getNodeName();
 		Node result = null;
 		if (IHTMLConstants.TAG_LI.equalsIgnoreCase(name) || // 
 				EditModelQuery.HTML_STYLE_NODES.contains(node.getNode()
 						.getLocalName())) {
 			if (node.getNode().hasChildNodes()) {
 				result = node.getNode().cloneNode(false);
 			}
 		}
 		return result;
 	}
 
 	private Node processContainerView(WorkNode node) {
 		Node result = null;
 		if (IJSFConstants.TAG_VIEW.equalsIgnoreCase(node.getNode()
 				.getLocalName())) {
 			result = EditModelQuery.getDocumentNode(node.getNode())
 					.createElement(IJSFConstants.TAG_SUBVIEW);
 			result.setPrefix(node.getNode().getPrefix());
 		} else if (IHTMLConstants.TAG_BODY.equalsIgnoreCase(node.getNode()
 				.getNodeName())
 				|| IHTMLConstants.TAG_HTML.equalsIgnoreCase(node.getNode()
 						.getNodeName())) {
 			result = EditModelQuery.getDocumentNode(node.getNode())
 					.createElement(node.getNode().getNodeName());
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.commands.range.AbstractCopyEdit#processContainer(org.eclipse.jst.pagedesigner.commands.range.WorkNode)
 	 */
 	protected Node processContainer(WorkNode node) {
 		Node result = processContainerView(node);
 		if (result == null) {
 			result = processContainerTable(node);
 		}
 		if (result == null) {
 			result = processContainerStyleNodes(node);
 		}
 		if (result == null) {
 			// strip out container
 			if (LayoutPart.getConcreteNode(node.getNode()) != null) {
 				Node parent = node.getNode().getParentNode();
 				Node refNode = node.getNode();
 				Node child = node.getNode().getFirstChild();
 				Node first = null, last = null;
 				int index = 0;
 				NodeList children = node.getNode().getChildNodes();
 				int size = children.getLength();
 				while (child != null) {
 					Node next = child.getNextSibling();
 					Node n = EditHelper.deleteNode(child);
 					parent.insertBefore(n, refNode);
 					if (index == 0) {
 						if (refNode != null) {
 							first = refNode.getPreviousSibling();
 						} else {
 							first = parent.getLastChild();
 						}
 					}
 					if (index == size - 1) {
 						if (refNode != null) {
 							last = refNode.getPreviousSibling();
 						} else {
 							last = parent.getLastChild();
 						}
 					}
 					index++;
 					child = next;
 				}
 				if (node.getPosOffsets()[0] <= 0) {
 					setOperationPosition(new DOMRefPosition(first, false));
 				} else {
 					setOperationPosition(new DOMRefPosition(last, true));
 				}
 			} else {
 				setOperationPosition(new DOMRefPosition(node.getNode(), false));
 			}
 			result = EditHelper.deleteNode(node.getNode());
 		}
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.commands.range.AbstractCopyEdit#processNode(org.w3c.dom.Node,
 	 *      int[])
 	 */
 	protected Node processNode(WorkNode node) {
 		Node result = null;
 		if (!isTableComponents(node)
				|| !new IETablePositionRule(null).isInValidTable(node
 						.getNode())) {
 			// it's not table components.
 			setOperationPosition(new DOMRefPosition(node.getNode(), false));
 			result = EditHelper.deleteNode(node.getNode());
 		}
 		return result;
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.pagedesigner.commands.range.AbstractCopyEdit#processText(org.w3c.dom.Text,
 	 *      int[])
 	 */
 	protected Text processText(WorkNode node) {
 		Text currentNode = (Text) node.getNode();
 		int[] offsets = node.getPosOffsets();
 		Node second = null;
 		int location1 = EditHelper.getInstance().getLocation(currentNode,
 				node.getPosOffsets()[0], true);
 		int location2 = EditHelper.getInstance().getLocation(currentNode,
 				node.getPosOffsets()[1], true);
 		// left index
 		if (currentNode.getData().length() > 0) {
 			if (location1 == EditHelper.IN_MIDDLE) {
 				IDOMPosition position = new DOMPosition(currentNode, node
 						.getPosOffsets()[0]);
 				setOperationPosition(position);
 				position = DOMPositionHelper.splitText(position);
 				Node nnode = position.getNextSiblingNode();
 				if (nnode instanceof Text) {
 					currentNode = (Text) nnode;
 					offsets[1] -= offsets[0] > 0 ? offsets[0] : 0;
 				}
 			} else {
 				// setOperationPosition(new DOMRefPosition(currentNode, false));
 				if (currentNode.getPreviousSibling() != null) {
 					setOperationPosition(new DOMRefPosition(currentNode
 							.getPreviousSibling(), true));
 				} else {
 					setOperationPosition(new DOMPosition(currentNode
 							.getParentNode(), 0));
 				}
 			}
 			// right index
 			if (location2 >= EditHelper.IN_MIDDLE) {
 				IDOMPosition position = new DOMPosition(currentNode, offsets[1]);
 				position = DOMPositionHelper.splitText(position);
 				second = position.getPreviousSiblingNode();
 			}
 			return second != null ? (Text) EditHelper.deleteNode(second) : null;
 		}
         setOperationPosition(new DOMRefPosition(currentNode, false));
         return (Text) EditHelper.deleteNode(currentNode);
 	}
 }
