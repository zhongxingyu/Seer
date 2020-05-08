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
 
 public class UndoableEdit implements Undoable {
 
 	// Fields
 	private Node node = null;
 
 	private String newText = "";
 	private String oldText = "";
 
 	private int newPosition = 0;
 	private int oldPosition = 0;
 
 	private int newMarkPosition = 0;
 	private int oldMarkPosition = 0;
 	
 	private boolean frozen = false;
 
 
 	// The Constructors
 	public UndoableEdit(
 		Node node, 
 		String oldText, 
 		String newText, 
 		int oldPosition, 
 		int newPosition, 
 		int oldMarkPosition, 
 		int newMarkPosition) 
 	{
 		this.node = node;
 		this.newText = newText;
 		this.oldText = oldText;
 		this.newPosition = newPosition;
 		this.oldPosition = oldPosition;
 		this.newMarkPosition = newMarkPosition;
 		this.oldMarkPosition = oldMarkPosition;
 	}
 	
 	public void destroy() {
 		node = null;
 		newText = null;
 		oldText = null;
 	}
 	
 	// Accessors
 	public void setNode(Node node) {this.node = node;}
 	public Node getNode() {return this.node;}
 
 	public void setNewText(String newText) {this.newText = newText;}
 	public String getNewText() {return this.newText;}
 
 	public void setNewPosition(int newPosition) {this.newPosition = newPosition;}
 	public int getNewPosition() {return this.newPosition;}
 
 	public void setNewMarkPosition(int newMarkPosition) {this.newMarkPosition = newMarkPosition;}
 	public int getNewMarkPosition() {return this.newMarkPosition;}
 	
 	public void setFrozen(boolean frozen) {this.frozen = frozen;}
 	public boolean isFrozen() {return frozen;}
 	
 	// Undoable Interface
 	public void undo() {
 		JoeTree tree = node.getTree();
 		
 		node.setValue(oldText);
 		tree.setCursorPosition(oldPosition);
 		tree.getDocument().setPreferredCaretPosition(oldPosition);
 		tree.setCursorMarkPosition(oldMarkPosition);
		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 		tree.setEditingNode(node);
 		tree.clearSelection();
 		tree.insertNode(node); // Used for visibility
 		
 		tree.getDocument().panel.layout.draw(node, OutlineLayoutManager.TEXT);
 	}
 	
 	public void redo() {
 		JoeTree tree = node.getTree();
 
 		node.setValue(newText);
 		tree.setCursorPosition(newPosition);
 		tree.getDocument().setPreferredCaretPosition(newPosition);
 		tree.setCursorMarkPosition(newMarkPosition);
		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 		tree.setEditingNode(node);
 		tree.clearSelection();
 		tree.insertNode(node); // Used for visibility
 		
 		tree.getDocument().panel.layout.draw(node, OutlineLayoutManager.TEXT);
 	}
 
 
 	// Static Methods
 	public static void freezeUndoEdit(Node currentNode) {
 		UndoableEdit undoable = currentNode.getTree().getDocument().undoQueue.getIfEdit();
 		if ((undoable != null) && (undoable.getNode() == currentNode)) {
 			undoable.setFrozen(true);
 		}
 	}
 }
