 /**
  * Copyright (C) 2000, 2001 Maynard Demmon, maynard@organic.com
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
 
 public class CompoundUndoableInsert extends AbstractCompoundUndoable {
 
 	private Node parent = null;
 	
 	// The Constructors
 	public CompoundUndoableInsert(Node parent) {
 		this(true, parent);
 	}
 	
 	public CompoundUndoableInsert(boolean isUpdatingGui, Node parent) {
 		super(isUpdatingGui);
 		this.parent = parent;
 	}
 	
 	// Accessors
 	public Node getParent() {return parent;}
 	
 	
 	// Undoable Interface
 	public void destroy() {
 		super.destroy();
 		parent = null;
 	}
 	
 	public void undo() {
 		// Find the node we will change focus too, note may end up null
 		Node youngestNode = ((PrimitiveUndoableInsert) primitives.get(primitives.size() - 1)).getNode();
 		Node newSelectedNode = youngestNode.prev();
 		
 		// Shorthand
 		JoeTree tree = youngestNode.getTree();
 		OutlineLayoutManager layout = tree.getDocument().panel.layout;
 		
 		// Delete Everything
 		for (int i = 0; i < primitives.size(); i++) {
 			((PrimitiveUndoableInsert) primitives.get(i)).undo();
 		}
 
 		if ((newSelectedNode == youngestNode.getParent()) && !newSelectedNode.isLeaf()) {
 			newSelectedNode = newSelectedNode.getFirstChild();
 		}
 		
 		// If the newSelectedNode is null, then select the first node in the tree
		if (newSelectedNode == null || newSelectedNode.isRoot()) {
 			tree.setSelectedNodesParent(tree.getRootNode());
 			newSelectedNode = tree.getRootNode().getFirstChild();
 			tree.addNodeToSelection(newSelectedNode);
 		} else {
 			tree.setSelectedNodesParent(newSelectedNode.getParent());
 			tree.addNodeToSelection(newSelectedNode);
 		}
 
 		// Record the EditingNode
 		tree.setEditingNode(newSelectedNode);
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 
 		// Redraw and Set Focus
 		// First make sure the node to draw from wasn't removed, it will be root since it is orphaned. 
 		// If so, we need to set the new one before trying to redraw.
 		if (layout.getNodeToDrawFrom().isRoot()) {
 			layout.setNodeToDrawFrom(newSelectedNode, tree.getVisibleNodes().indexOf(newSelectedNode));
 		}
 		tree.insertNode(newSelectedNode); // Just to make it visible
 		layout.draw(newSelectedNode,OutlineLayoutManager.ICON);	
 	}
 	
 	public void redo() {
 		Node youngestNode = ((PrimitiveUndoableInsert) primitives.get(primitives.size() - 1)).getNode();
 		JoeTree tree = youngestNode.getTree();
 
 		// Do all the Inserts
 		tree.setSelectedNodesParent(parent);
 		
 		for (int i = primitives.size() - 1; i >= 0; i--) {
 			((PrimitiveUndoableInsert) primitives.get(i)).redo();
 		}
 
 		// Record the EditingNode
 		tree.setEditingNode(youngestNode);
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 		
 		// Redraw and Set Focus
 		tree.getDocument().panel.layout.draw(youngestNode,OutlineLayoutManager.ICON);		
 	}
 }
