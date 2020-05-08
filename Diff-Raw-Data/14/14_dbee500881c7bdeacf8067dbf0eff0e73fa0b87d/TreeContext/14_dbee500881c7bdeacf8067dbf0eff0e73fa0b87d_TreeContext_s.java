 /**
  * Portions Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
  * Portions Copyright (C) 2002 Stan Krute <Stan@StanKrute.com>
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or 
  * without modification, are permitted provided that the 
  * following conditions are met:
  * 
  *  - Redistributions of source code must retain the above copyright 
  *    notice, this list of conditions and the following disclaimer. 
  * 
  *  - Redistributions in binary form must reproduce the above 
  *    copyright notice, this list of conditions and the following 
  *    disclaimer in the documentation and/or other materials provided 
  *    with the distribution. 
  * 
  *  - Neither the names "Java Outline Editor", "JOE" nor the names of its 
  *    contributors may be used to endorse or promote products derived 
  *    from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
  * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
  * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
  * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  */
  
 package com.organic.maynard.outliner;
 
 import java.util.*;
 import javax.swing.*;
 import javax.swing.event.*;
 
 public class TreeContext extends AttributeContainerImpl implements JoeTree {
 
 	// Private Instance Variables
 	private OutlinerDocument document = null;
 	
 	private JoeNodeList visibleNodes = Outliner.newNodeList(1000);
 	private JoeNodeList selectedNodes = Outliner.newNodeList(100);
 	private Node rootNode = null;
 
 	private HashMap attributes = null;
 
 
 	// The Constructors
 	public TreeContext(OutlinerDocument document) {
 		this.document = document;
 
 		resetStructure();
 		
 		document.panel.layout.setNodeToDrawFrom(getEditingNode(),0);
 	}
 
 	public TreeContext() {
 		resetStructure();
 	}
 	
 	
 	
 	// Comments
 	private boolean comment = false;
 
 	// these two methods are part of JoeTree interface
 	public void setRootNodeCommentState(boolean comment) {this.comment = comment;}
 	public boolean getRootNodeCommentState() {return this.comment;}
 
 	// Editablity
 	private boolean editable = true;
 
 	// these two methods are part of JoeTree interface
 	public void setRootNodeEditableState(boolean editable) {this.editable = editable;}
 	public boolean getRootNodeEditableState() {return this.editable;}
 
 	// Moveability
 	private boolean moveable = true;
 
 	// these two methods are part of JoeTree interface
 	public void setRootNodeMoveableState(boolean moveable) {this.moveable = moveable;}
 	public boolean getRootNodeMoveableState() {return this.moveable;}
 
 
 	// Line Count Control
 	private int lineCountKey = 0;
 	
 	
 	public int getLineCountKey() {
 		return lineCountKey;
 	}
 	
 	public void incrementLineCountKey() {
 		// Lets not grow forever since it could be possible to 
 		// exceed max int although very very very unlikely, but still, better to be safe.
 		if (lineCountKey > 1000000) {
 			lineCountKey = 0;
 		} else {
 			lineCountKey++;
 		}
 	}
 
 	
 	// Accessors
 
 	// Statistics
 	public int getLineCount() {
 		return rootNode.getDecendantCount();
 	}
 	
 	public int getCharCount() {
 		return rootNode.getDecendantCharCount();
 	}
 	
 	
 	// Tracking the Editing Location
 	private Node editingNode = null;
 	private int cursorPosition = 0;
 	private int cursorMarkPosition = 0;
 	private int componentFocus = OutlineLayoutManager.TEXT;
 
 	public void setEditingNode(Node editingNode) {
 		setEditingNode(editingNode, true);
 	}
 
 	public void setEditingNode(Node editingNode, boolean updateAttPanel) {
 		this.editingNode = editingNode;
 		
 		if (updateAttPanel) {
 			document.attPanel.update();
 		}
 	}
 	
 	public Node getEditingNode() {
 		return editingNode;
 	}
 
 	public void setCursorMarkPosition(int cursorMarkPosition) {
 		this.cursorMarkPosition = cursorMarkPosition;
 	}
 	public int getCursorMarkPosition() {
 		return cursorMarkPosition;
 	}
 
 	public void setComponentFocus(int componentFocus) {
 		this.componentFocus = componentFocus;
 		updateEditMenu();
 	}
 	
 	public int getComponentFocus() {
 		return componentFocus;
 	}
 
 	public void setCursorPosition(int cursorPosition) {
 		setCursorPosition(cursorPosition,true);
 	}
 	
 	public void setCursorPosition(int cursorPosition, boolean setMark) {
 		this.cursorPosition = cursorPosition;
 		if (setMark) {
 			setCursorMarkPosition(cursorPosition);
 		}
 		updateEditMenu();
 	}
 	
 	public int getCursorPosition() {
 		return cursorPosition;
 	}
 
 	private static JMenuItem cutItem = null;
 	private static JMenuItem copyItem = null;
 	private static JMenuItem deleteItem = null;
 	private static JMenuItem selectInverseItem = null;
 	private static JMenuItem exportSelectionItem = null;
 	private static JMenuItem hoistItem = null;
 
 	public void updateEditMenu() {
 		if (cutItem == null) {
 			cutItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.CUT_MENU_ITEM);
 			copyItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.COPY_MENU_ITEM);
 			deleteItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.DELETE_MENU_ITEM);
 			selectInverseItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.SELECT_INVERSE_MENU_ITEM);
 			exportSelectionItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.EXPORT_SELECTION_MENU_ITEM);
 			hoistItem = (JMenuItem) GUITreeLoader.reg.get(GUITreeComponentRegistry.OUTLINE_HOIST_MENU_ITEM);
 		}
 		
 		Node node = getEditingNode();
 
 		if (getComponentFocus() == OutlineLayoutManager.TEXT) {
 			selectInverseItem.setEnabled(false);
 			
 			if (getCursorPosition() == getCursorMarkPosition()) {
 				copyItem.setEnabled(false);
 				cutItem.setEnabled(false);
 				deleteItem.setEnabled(false);
 				exportSelectionItem.setEnabled(false);
 			} else {
 				copyItem.setEnabled(true);
 				cutItem.setEnabled(true);
 				deleteItem.setEnabled(true);
 				exportSelectionItem.setEnabled(true);
 			}
 
 			if (node.isLeaf()) {
 				hoistItem.setEnabled(false);
 			} else {
 				hoistItem.setEnabled(true);
 			}
 		} else if (getComponentFocus() == OutlineLayoutManager.ICON) {
 			selectInverseItem.setEnabled(true);
 			
 			if (selectedNodes.size() == 0) {
 				// Can this really ever happen? Seems not unless something has gone horribly wrong.
 				copyItem.setEnabled(false);
 				cutItem.setEnabled(false);
 				deleteItem.setEnabled(false);
 				exportSelectionItem.setEnabled(false);
			} else if (selectedNodes.size() == 1) {
				if (node.isLeaf()) {
					hoistItem.setEnabled(false);
				} else {
					hoistItem.setEnabled(true);
				}
 			} else {
 				copyItem.setEnabled(true);
 				cutItem.setEnabled(true);
 				deleteItem.setEnabled(true);
 				exportSelectionItem.setEnabled(true);
 			}			
 		}	
 	}
 
 
 	// Tree Methods
 	public Node getPrevNode(Node existingNode) {
 		int prevNodeIndex = visibleNodes.indexOf(existingNode) - 1;
 		if (prevNodeIndex < 0) {
 			return null;
 		}
 		return visibleNodes.get(prevNodeIndex);
 	}
 
 	public Node getNextNode(Node existingNode) {
 		int nextNodeIndex = visibleNodes.indexOf(existingNode) + 1;
 		if (nextNodeIndex >= visibleNodes.size()) {
 			return null;
 		}
 		return visibleNodes.get(nextNodeIndex);
 	}
 	
 	public void addNode(Node node) {
 		visibleNodes.add(node);
 	}
 	
 	public void removeNode(Node node) {
 		int index = visibleNodes.indexOf(node);
 		
 		if (index != -1) {
 			int lastIndex = visibleNodes.indexOf(node.getLastViewableDecendent());
 			visibleNodes.removeRange(index, lastIndex + 1);
 		}
 	}
 
 	public void insertNode(Node node) {
 		// Find the first Ancestor that is in the cache or Root
 		Node ancestor = node.getYoungestVisibleAncestor();
 		
 		// Expand all nodes in the path down to the node
 		node.expandAllAncestors();
 		
 		// Walk the tree Downwards inserting all expanded nodes and their children
 		ancestor.insertChildrenIntoVisibleNodesCache(visibleNodes.indexOf(ancestor));
 	}
 
 	public int insertNodeAfter(Node existingNode, Node newNode) {
 		int nodeIndex = visibleNodes.indexOf(existingNode) + 1;
 		if (nodeIndex >= 0) {
 			visibleNodes.add(nodeIndex, newNode);
 		}
 		return nodeIndex;
 	}
 
 	public void insertNode(Node node, int index) {
 		visibleNodes.add(index, node);
 	}
 	
 	// Handling Node Selection
 	private Node mostRecentNodeTouched = null;
 	private Node selectedNodesParent = null;
 	
 	public int getNumberOfSelectedNodes() {
 		return selectedNodes.size();
 	}
 	
 	public void setSelectedNodesParent(Node node) {
 		setSelectedNodesParent(node,true);
 	}
 	
 	public void setSelectedNodesParent(Node node, boolean doClear) {
 		if (doClear) {
 			clearSelection();
 		}
 		this.selectedNodesParent = node;	
 	}
 	
 	public Node getSelectedNodesParent() {return selectedNodesParent;}
 	
 	public void clearSelection() {
 		for (int i = selectedNodes.size() - 1; i >= 0; i--) {
 			selectedNodes.get(i).setSelected(false);
 		}
 		
 		selectedNodes.clear();
 	}
 	
 	public void addNodeToSelection(Node node) {
 		if (node.isSelected()) {
 			return; // Don't add a node if it is already selected.
 		} else if (node.getParent() == getSelectedNodesParent()) {			
 			node.setSelected(true);
 			mostRecentNodeTouched = node;
 			
 			// Maintain the selected nodes in order from youngest to oldest
 			if (selectedNodes.size() > 0) {
 				int nodeIndex = node.currentIndex();
 				
 				NodeImpl parent = (NodeImpl) selectedNodesParent;
 				int searchStartIndex = 0;
 				int childCount = parent.children.size() - 1;
 				
 				for (int i = 0; i < selectedNodes.size(); i++) {
 					searchStartIndex = parent.children.indexOf(selectedNodes.get(i), searchStartIndex, childCount);
 					if (searchStartIndex > nodeIndex) {
 						selectedNodes.add(i, node);
 						return;
 					}
 				}
 			}
 			
 			selectedNodes.add(node);
 		}
 	}
 	
 	public void removeNodeFromSelection(Node node) {
 		if (node.getParent() == getSelectedNodesParent()) {
 			node.setSelected(false);
 			int index = selectedNodes.indexOf(node);
 			if (index != -1) {
 				selectedNodes.remove(index);
 			}
 		}	
 	}
 	
 	public void selectRangeFromMostRecentNodeTouched(Node node) {
 		if (node.getParent() == getSelectedNodesParent()) {
 			int indexA = mostRecentNodeTouched.currentIndex();
 			int indexB = node.currentIndex();
 			
 			int start = Math.min(indexA,indexB);
 			int end = Math.max(indexA,indexB);
 			
 			clearSelection();
 			
 			for (int i = start; i <= end; i++) {
 				Node theNode = getSelectedNodesParent().getChild(i);
 				theNode.setSelected(true);
 				selectedNodes.add(theNode);
 			}
 		}	
 	}
 
 	public Node getYoungestInSelection() {
 		try {
 			return selectedNodes.get(0);
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 	
 	public Node getOldestInSelection() {
 		try {
 			return selectedNodes.get(selectedNodes.size() - 1);
 		} catch (IndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 
 
 	// Tree Manipulation
 	public void promoteNode(Node currentNode, int currentNodeIndex) {
 		if (currentNode.getParent().isRoot()) {
 			// Our parent is root. Since we can't be promoted to root level, Abort.
 			return;
 		}
 
 		Node targetNode = currentNode.getParent().getParent();
 		int insertIndex = currentNode.getParent().currentIndex() + 1;
 		
 		// Remove the selected node from the current parent node.
 		currentNode.getParent().removeChild(currentNode, currentNodeIndex);
 			
 		// Append the selected node to the target node.
 		targetNode.insertChild(currentNode, insertIndex);
 		currentNode.setDepthRecursively(targetNode.getDepth() + 1);
 		
 		// Now Remove and Insert into the VisibleNodes Cache
 		removeNode(currentNode);
 		insertNode(currentNode);
 	}
 	
 	public void demoteNode(Node currentNode, Node targetNode, int currentNodeIndex) {
 		if (targetNode == currentNode) {
 			// We have no previous sibling, so Abort.
 			return;
 		}
 
 		// Remove the selected node from the current parent node.
 		currentNode.getParent().removeChild(currentNode, currentNodeIndex);
 			
 		// Append the selected node to the target node.
 		targetNode.insertChild(currentNode,targetNode.numOfChildren());
 		currentNode.setDepthRecursively(targetNode.getDepth() + 1);
 
 		// Now Remove and Insert into the VisibleNodes Cache
 		removeNode(currentNode);
 		insertNode(currentNode);
 	}
 	
 	
 	// Misc Methods
 	public boolean isWholeDocumentSelected() {
 		if ((selectedNodesParent != null) &&
 			selectedNodesParent.isRoot() && 
 			(getNumberOfSelectedNodes() == selectedNodesParent.numOfChildren()))
 		{
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public boolean isDocumentEmpty() {
 		if ((rootNode.numOfChildren() == 1) &&
 			rootNode.getFirstChild().isLeaf() &&
 			rootNode.getFirstChild().getValue().equals(""))
 		{
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	// ----------------------- JoeTree interface ------------------------
 	
 	public OutlinerDocument getDocument () {
 		return document ;
 	} // end method getVisibleNodes
 	
 	
 	public void setDocument (OutlinerDocument someDocument) {
 		document = someDocument ;
 	} // end method getVisibleNodes
 	
 	
 	public JoeNodeList getVisibleNodes () {
 		return visibleNodes ;
 	} // end method getVisibleNodes
 	
 	
 	public JoeNodeList getSelectedNodes () {
 		return selectedNodes ;
 	} // end method getSelectedNodes
 	
 	
 	public Node getRootNode () {
 		return rootNode ;
 	} // end method getSelectedNodes
 	
 	
 	public void setRootNode(Node someNode) {
 		rootNode = someNode;
 		rootNode.setExpandedClean(true);
 	}
 	
 	
 	public void reset() {
 		document = null;
 		visibleNodes.clear();
 		selectedNodes.clear();
 		rootNode = null;
 		attributes = null;
 		
 		resetStructure();	
 	}
 
 	private void resetStructure() {
 		// Create an empty Tree
 		setRootNode(new NodeImpl(this,"ROOT"));
 		rootNode.setHoisted(true);
 	
 		NodeImpl child = new NodeImpl(this,"");
 		child.setDepth(0);
 		rootNode.insertChild(child, 0);
 		insertNode(child);
 		
 		// Record the current location
 		setEditingNode(child, false);	
 	}
 	
 	
 	public void destroy() {
 		visibleNodes = null;
 		selectedNodes = null;
 		rootNode.destroy();
 		rootNode = null;
 		editingNode = null;
 		mostRecentNodeTouched = null;
 		selectedNodesParent = null;
 		document = null;
 	}
 	
 	
 }
