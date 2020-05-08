 /**
  * Copyright (C) 2000 Maynard Demmon, maynard@organic.com
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
  
 package com.organic.maynard.outliner;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Window;
 import java.awt.datatransfer.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 
 import com.organic.maynard.util.string.*;
 
 public class IconKeyListener implements KeyListener, MouseListener {
 
 	// Constants for setting cursor position.
 	private static final int POSITION_FIRST = 0;
 	private static final int POSITION_CURRENT = 1;
 	private static final int POSITION_LAST = 2;
 
 
 	// Instance Fields
 	private OutlinerCellRendererImpl textArea = null;
 
 
 	// The Constructors
 	public IconKeyListener() {}
 	
 	public void destroy() {
 		textArea = null;
 	}
 
 	
 	private void recordRenderer(Component c) {
 		if (c instanceof OutlineButton) {
 			textArea = ((OutlineButton) c).renderer;
 		} else if (c instanceof OutlineLineNumber) {
 			textArea = ((OutlineLineNumber) c).renderer;
 		} else if (c instanceof OutlineCommentIndicator) {
 			textArea = ((OutlineCommentIndicator) c).renderer;
 		}
 	}
 
 	// MouseListener Interface
 	public void mouseEntered(MouseEvent e) {}
 	
 	public void mouseExited(MouseEvent e) {}
 	
 	public void mousePressed(MouseEvent e) {
 		recordRenderer(e.getComponent());
 		
 		// This is detection for Solaris
 		if (e.isPopupTrigger() && textArea.node.isSelected()) {
 			Outliner.macroPopup.show(e.getComponent(),e.getX(), e.getY());
 		}
 		
 		// Handle clicks. The modulo is to deal with rapid clicks that would register as a triple click or more.
 		if ((e.getClickCount() % 2) == 1) {
 			processSingleClick(e);
 		} else if ((e.getClickCount() % 2) == 0){
 			processDoubleClick(e);
 		}
 		
 		// Record the EditingNode and CursorPosition and ComponentFocus
  		TreeContext tree = textArea.node.getTree();
 		tree.setEditingNode(textArea.node);
 		tree.setComponentFocus(OutlineLayoutManager.ICON);
 		
 		// Store the node since it may get lost by the time we want to throw the new mouse event.
 		Node node = textArea.node;
 		
 		// Redraw and set focus
		tree.doc.panel.layout.draw(node, OutlineLayoutManager.ICON);
 		
 		// Consume the current event and then propogate a new event to
 		// the DnD listener since if a drawUp() happened, the old event
 		// will most likely have an invalid component.
 		e.consume();
 
 		MouseEvent eNew = new MouseEvent(
 			tree.doc.panel.layout.getUIComponent(node).button, 
 			e.getID(), 
 			e.getWhen(), 
 			e.getModifiers(), 
 			e.getX(), 
 			e.getY(), 
 			e.getClickCount(), 
 			e.isPopupTrigger()
 		);
 		tree.doc.panel.layout.dndListener.mousePressed(eNew);
 	}
 	
 	public void mouseReleased(MouseEvent e) {
 		recordRenderer(e.getComponent());
 		
 		// This is detection for Windows
 		if (e.isPopupTrigger() && textArea.node.isSelected()) {
 			Outliner.macroPopup.show(e.getComponent(),e.getX(), e.getY());
 		}
 	}
 	
 	public void mouseClicked(MouseEvent e) {}
 	    
 	protected void processSingleClick(MouseEvent e) {
 		Node node = textArea.node;
 		TreeContext tree = node.getTree();
 		
 		if (e.isShiftDown()) {
 			tree.selectRangeFromMostRecentNodeTouched(node);
 		
 		} else if (e.isControlDown()) {
 			if (node.isSelected() && (tree.selectedNodes.size() != 1)) {
 				tree.removeNodeFromSelection(node);
 			} else if (tree.getSelectedNodesParent() == node.getParent()) {
 				tree.addNodeToSelection(node);
 			}
 			
 		} else if (!node.isSelected()) {
 			tree.setSelectedNodesParent(node.getParent());
 			tree.addNodeToSelection(node);
 		}	
 	}
 	
 	protected void processDoubleClick(MouseEvent e) {
 		if (textArea.node.isExpanded()) {
 			textArea.node.setExpanded(false);
 		} else {
 			textArea.node.setExpanded(true);
 		}
 		
 		if (textArea.node.isSelected()) {
 			textArea.node.getTree().setSelectedNodesParent(textArea.node.getParent());
 			textArea.node.getTree().addNodeToSelection(textArea.node);
 		}		
 	}
 	
 	
 	// KeyListener Interface
 	public void keyPressed(KeyEvent e) {
 		recordRenderer(e.getComponent());
 	
 		// Create some short names for convienence
 		TreeContext tree = textArea.node.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 
 		switch(e.getKeyCode()) {
 			case KeyEvent.VK_PAGE_DOWN:
 				toggleExpansion(tree,layout);
 				break;
 
 			case KeyEvent.VK_PAGE_UP:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						clearComment(tree, layout);
 					} else {
 						toggleCommentInheritance(tree,layout);
 					}
 				} else if (e.isShiftDown()) {
 					toggleComment(tree,layout);
 				} else {
 					toggleCommentAndClear(tree, layout);
 				}
 				break;
 
 			case KeyEvent.VK_F11:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						clearEditable(tree, layout);
 					} else {
 						toggleEditableInheritance(tree,layout);
 					}
 				} else if (e.isShiftDown()) {
 					toggleEditable(tree,layout);
 				} else {
 					toggleEditableAndClear(tree, layout);
 				}
 				break;
 
 			case KeyEvent.VK_F12:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						clearMoveable(tree, layout);
 					} else {
 						toggleMoveableInheritance(tree,layout);
 					}
 				} else if (e.isShiftDown()) {
 					toggleMoveable(tree,layout);
 				} else {
 					toggleMoveableAndClear(tree, layout);
 				}
 				break;			
 			case KeyEvent.VK_DELETE:
 				delete(tree,layout,true);
 				break;
 
 			case KeyEvent.VK_BACK_SPACE:
 				delete(tree,layout,false);
 				break;
 				
 			case KeyEvent.VK_UP:
 				if (e.isShiftDown()) {
 					moveUp(tree,layout);
 				} else {
 					navigate(tree, layout, UP);
 				}
 				break;
 
 			case KeyEvent.VK_DOWN:
 				if (e.isShiftDown()) {
 					moveDown(tree,layout);
 				} else {
 					navigate(tree,layout, DOWN);
 				}
 				break;
 
 			case KeyEvent.VK_LEFT:
 				if (e.isShiftDown()) {
 					moveLeft(tree,layout);
 				} else {
 					navigate(tree,layout, LEFT);
 				}
 				break;
 
 			case KeyEvent.VK_RIGHT:
 				if (e.isShiftDown()) {
 					moveRight(tree,layout);
 				} else {
 					navigate(tree,layout, RIGHT);
 				}
 				break;
 
 			case KeyEvent.VK_ENTER:
 				insert(tree,layout);
 				break;
 
 			case KeyEvent.VK_INSERT:
 				if (e.isShiftDown()) {
 					changeToParent(tree, layout);
 				} else {
 					changeFocusToTextArea(tree, layout, POSITION_CURRENT);
 				}
 				break;
 
 			case KeyEvent.VK_HOME:
 				if (tree.selectedNodes.size() > 1) {
 					changeSelectionToNode(tree, layout, POSITION_FIRST);
 				} else {
 					changeFocusToTextArea(tree, layout, POSITION_FIRST);
 				}
 				break;
 
 			case KeyEvent.VK_END:
 				if (tree.selectedNodes.size() > 1) {
 					changeSelectionToNode(tree, layout, POSITION_LAST);
 				} else {
 					changeFocusToTextArea(tree, layout, POSITION_LAST);
 				}
 				break;
 
 			case KeyEvent.VK_TAB:
 				if (e.isShiftDown()) {
 					promote(tree,layout);
 				} else {
 					demote(tree,layout);
 				}
 				break;
 
 			case KeyEvent.VK_C:
 				if (e.isControlDown()) {
 					copy(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_X:
 				if (e.isControlDown()) {
 					cut(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_V:
 				if (e.isControlDown()) {
 					paste(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_I:
 				if (e.isControlDown()) {
 					selectInverse(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_A:
 				if (e.isControlDown() && !e.isShiftDown()) {
 					selectAll(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_D:
 				if (e.isControlDown() && !e.isShiftDown()) {
 					selectNone(tree,layout);
 					break;
 				} else {
 					return;
 				}
 
 			case KeyEvent.VK_M:
 				if (e.isControlDown()) {
 					if (e.isShiftDown()) {
 						merge(tree,layout,true);
 					} else {
 						merge(tree,layout,false);
 					}
 					break;
 				} else {
 					return;
 				}
 
 			default:
 				return;
 		}
 		
 		e.consume();
 		return;
 	}
 	
 	public void keyTyped(KeyEvent e) {
 		recordRenderer(e.getComponent());
 
 		// Create some short names for convienence
 		Node currentNode = textArea.node;
 		TreeContext tree = currentNode.getTree();
 		OutlineLayoutManager layout = tree.doc.panel.layout;
 		Node youngestNode = tree.getYoungestInSelection();
 
 		// If we're read-only then abort
 		if (!currentNode.isEditable()) {
 			return;
 		}
 				
 		// Catch any unwanted chars that slip through
 		if (e.isControlDown() ||
 			(e.getKeyChar() == KeyEvent.VK_BACK_SPACE) ||
 			(e.getKeyChar() == KeyEvent.VK_TAB) ||
 			(e.getKeyChar() == KeyEvent.VK_ENTER) ||
 			(e.getKeyChar() == KeyEvent.VK_INSERT)
 		) {
 			return;
 		}
 
 		// Clear the selection since focus will change to the textarea.
 		tree.clearSelection();
 		
 		// Replace the text with the character that was typed
 		String oldText = youngestNode.getValue();
 		String newText = String.valueOf(e.getKeyChar());
 		youngestNode.setValue(newText);
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setCursorPosition(1);
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 
 		// Put the Undoable onto the UndoQueue
 		tree.doc.undoQueue.add(new UndoableEdit(youngestNode,oldText,newText,0,1,0,1));
 		
 		// Redraw and Set Focus
 		layout.draw(youngestNode,OutlineLayoutManager.TEXT);
 
 		e.consume();
 		return;
 	}
 	public void keyReleased(KeyEvent e) {}
 
 
 
 	// Key Handlers
 	private void toggleExpansion(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			Node node = (Node) tree.selectedNodes.get(i);
 			if (node.isExpanded()) {
 				node.setExpanded(false);
 			} else {
 				node.setExpanded(true);
 			}
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	// Comments
 	private void clearComment(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			clearCommentForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void clearCommentForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getCommentState();
 		int newValue = Node.COMMENT_INHERITED;
 		
 		if (oldValue != Node.COMMENT_INHERITED) {
 			node.setCommentState(Node.COMMENT_INHERITED);
 			undoable.addPrimitive(new PrimitiveUndoableCommentChange(node, oldValue, newValue));
 		}
 				
 		for (int i = 0; i < node.numOfChildren(); i++) {
 			clearCommentForSingleNode(node.getChild(i), undoable);
 		}
 	}
 	
 	private void toggleCommentAndClear(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleCommentAndClearForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void toggleCommentAndClearForSingleNode(Node node, CompoundUndoable undoable) {
 		toggleCommentForSingleNode(node, undoable);
 		
 		for (int i = 0; i < node.numOfChildren(); i++) {
 			clearCommentForSingleNode(node.getChild(i), undoable);
 		}		
 	}
 			
 	private void toggleComment(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleCommentForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void toggleCommentForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getCommentState();
 		int newValue = Node.COMMENT_INHERITED;
 		boolean isComment = node.isComment();
 		
 		if (oldValue == Node.COMMENT_FALSE) {
 			node.setCommentState(Node.COMMENT_TRUE);
 			newValue = Node.COMMENT_TRUE;
 					
 		} else if (oldValue == Node.COMMENT_TRUE) {
 			node.setCommentState(Node.COMMENT_FALSE);
 			newValue = Node.COMMENT_FALSE;
 		
 		} else {
 			if (isComment) {
 				node.setCommentState(Node.COMMENT_FALSE);
 				newValue = Node.COMMENT_FALSE;
 			} else {
 				node.setCommentState(Node.COMMENT_TRUE);
 				newValue = Node.COMMENT_TRUE;
 			}
 		}
 				
 		undoable.addPrimitive(new PrimitiveUndoableCommentChange(node, oldValue, newValue));
 	}
 	
 	private void toggleCommentInheritance(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleCommentInheritanceForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 	
 	protected static void toggleCommentInheritanceForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getCommentState();
 		int newValue = Node.COMMENT_INHERITED;
 		boolean isComment = node.isComment();
 		
 		if (oldValue == Node.COMMENT_INHERITED) {
 			if (isComment) {
 				node.setCommentState(Node.COMMENT_TRUE);
 				newValue = Node.COMMENT_TRUE;
 			} else {
 				node.setCommentState(Node.COMMENT_FALSE);
 				newValue = Node.COMMENT_FALSE;
 			}
 								
 		} else {
 			node.setCommentState(Node.COMMENT_INHERITED);
 		}
 				
 		undoable.addPrimitive(new PrimitiveUndoableCommentChange(node, oldValue, newValue));
 	}
 	
 	private void changeFocusToTextArea(TreeContext tree, OutlineLayoutManager layout, int positionType) {
 		Node currentNode = textArea.node;
 		
 		if (positionType == POSITION_FIRST) {
 			tree.setCursorPosition(0);
 			tree.doc.setPreferredCaretPosition(0);
 		} else if (positionType == POSITION_LAST) {
 			int index = textArea.getText().length();
 			tree.setCursorPosition(index);
 			tree.doc.setPreferredCaretPosition(index);		
 		}
 		
 		tree.setComponentFocus(OutlineLayoutManager.TEXT);
 		tree.clearSelection();
 		layout.draw(currentNode,OutlineLayoutManager.TEXT);
 	}
 
 	private void changeSelectionToNode(TreeContext tree, OutlineLayoutManager layout, int positionType) {
 		Node selectedNode = null;
 		
 		if (positionType == POSITION_FIRST) {
 			selectedNode = tree.getYoungestInSelection();
 		} else if (positionType == POSITION_LAST) {
 			selectedNode = tree.getOldestInSelection();
 		}
 		
 		// Update Selection
 		tree.clearSelection();
 		tree.addNodeToSelection(selectedNode);
 
 		// Record State
 		tree.setEditingNode(selectedNode);
 		tree.setCursorPosition(0);
 		tree.doc.setPreferredCaretPosition(0);
 		
 		// Redraw and Set Focus	
 		layout.draw(selectedNode, OutlineLayoutManager.ICON);
 	}
 
 	private void moveUp(TreeContext tree, OutlineLayoutManager layout) {
 		Node youngestNode = tree.getYoungestInSelection();
 		Node node = youngestNode.prevSibling();
 		if (node == youngestNode) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable = new CompoundUndoableMove(node.getParent(),node.getParent());
 		int targetIndex = node.currentIndex();
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 
 			// Abort if node is not moveable
 			if (!nodeToMove.isMoveable()) {
 				continue;
 			}
 		
 			int currentIndex = nodeToMove.currentIndex();
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			targetIndex++;
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}
 	}
 	
 	private void moveDown(TreeContext tree, OutlineLayoutManager layout) {
 		Node oldestNode = tree.getOldestInSelection();
 		Node node = oldestNode.nextSibling();
 		if (node == oldestNode) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable = new CompoundUndoableMove(node.getParent(),node.getParent());
 		int targetIndex = node.currentIndex();
 		
 		// Do the move
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 
 			// Abort if node is not moveable
 			if (!nodeToMove.isMoveable()) {
 				continue;
 			}
 		
 			int currentIndex = nodeToMove.currentIndex();
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			targetIndex--;
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}
 	}
 
 	private void moveLeft(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		Node youngestNode = tree.getYoungestInSelection();
 		Node node = tree.getPrevNode(youngestNode);
 		if (node == null) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(), node.getParent());
 		int targetIndex = node.currentIndex();
 		int currentIndexAdj = 0;
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 
 			// Abort if node is not moveable
 			if (!nodeToMove.isMoveable()) {
 				continue;
 			}
 		
 			int currentIndex = nodeToMove.currentIndex() + currentIndexAdj;
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 			
 			if (nodeToMove.getParent() != node.getParent()) {
 				currentIndexAdj--;
 			}
 			targetIndex++;
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}		
 	}
 
 
 	private void moveRight(TreeContext tree, OutlineLayoutManager layout) {
 		Node oldestNode = tree.getOldestInSelection();
 		Node node = tree.getNextNode(oldestNode.getLastViewableDecendent());
 		if (node == null) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableMove undoable;
 		int targetIndex = -1;
 		if ((!node.isLeaf() && node.isExpanded())) {
 			undoable = new CompoundUndoableMove(oldestNode.getParent(),node);
 			targetIndex = 0;
 		} else if (tree.getSelectedNodesParent() != node.getParent()) {
 			undoable = new CompoundUndoableMove(oldestNode.getParent(),node.getParent());
 			targetIndex = node.currentIndex() + 1;
 		} else {
 			undoable = new CompoundUndoableMove(oldestNode.getParent(),node.getParent());
 			targetIndex = node.currentIndex();
 		}
 	
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 
 			// Abort if node is not moveable
 			if (!nodeToMove.isMoveable()) {
 				continue;
 			}
 		
 			int currentIndex = nodeToMove.currentIndex();
 			
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, currentIndex, targetIndex));
 
 			if ((!node.isLeaf() && node.isExpanded()) || (nodeToMove.getParent() != node.getParent())) {
 				// Do Nothing.
 			} else {
 				targetIndex--;
 			}
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}
 	}
 
 	private void insert(TreeContext tree, OutlineLayoutManager layout) {
 		Node node = tree.getOldestInSelection();
 
 		// Abort if node is not editable
 		if (!tree.getOldestInSelection().isEditable()) {
 			return;
 		}
 		
 		tree.clearSelection();
 		TextKeyListener.doInsert(node, tree, layout);
 	}
 
 	private void promote(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		if (currentNode.getParent().isRoot()) {
 			return;
 		}
 
 		// Put the Undoable onto the UndoQueue
 		Node targetNode = currentNode.getParent().getParent();
 		int targetIndex = currentNode.getParent().currentIndex() + 1;
 		
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(),targetNode);
 
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 
 			// Abort if node is not moveable
 			if (!nodeToMove.isMoveable()) {
 				continue;
 			}
 		
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, nodeToMove.currentIndex(), targetIndex));
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}
 	}
 
 	private void demote(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		if (tree.getYoungestInSelection().isFirstChild()) {
 			return;
 		}
 
 		// Abort if node is not moveable
 		if (!currentNode.isMoveable()) {
 			return;
 		}
 				
 		// Put the Undoable onto the UndoQueue
 		Node targetNode = tree.getYoungestInSelection().prevSibling();
 
 		CompoundUndoableMove undoable = new CompoundUndoableMove(currentNode.getParent(),targetNode);
 		
 		int existingChildren = targetNode.numOfChildren();
 		for (int i = tree.selectedNodes.size() - 1; i >= 0; i--) {
 			// Record the Insert in the undoable
 			Node nodeToMove = (Node) tree.selectedNodes.get(i);
 
 			// Abort if node is not moveable
 			if (!nodeToMove.isMoveable()) {
 				continue;
 			}
 
 			undoable.addPrimitive(new PrimitiveUndoableMove(undoable, nodeToMove, nodeToMove.currentIndex(), existingChildren));
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}
 		
 
 	}
 
 	private void copy(TreeContext tree, OutlineLayoutManager layout) {
 		NodeSet nodeSet = new NodeSet();
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			nodeSet.addNode(((Node) tree.selectedNodes.get(i)).cloneClean());
 		}
 		
 		// [md] This conditional is here since StringSelection subclassing seems to be broken in Java 1.3.1.
 		if (Outliner.isJava131()) {
 			java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(nodeSet.toString()), null);
 		} else {
 			Outliner.clipboard.setContents(new NodeSetTransferable(nodeSet), Outliner.outliner);
 		}
 	}
 
 	private void cut(TreeContext tree, OutlineLayoutManager layout) {
 		NodeSet nodeSet = new NodeSet();
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			Node node = (Node) tree.selectedNodes.get(i);
 			
 			// Abort if node is not editable
 			if (!node.isEditable()) {
 				continue;
 			}
 				
 			nodeSet.addNode(node.cloneClean());
 		}
 		
 		if (!nodeSet.isEmpty()) {
 			// [md] This conditional is here since StringSelection subclassing seems to be broken in Java 1.3.1.
 			if (Outliner.isJava131()) {
 				java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(nodeSet.toString()), null);
 			} else {
 				Outliner.clipboard.setContents(new NodeSetTransferable(nodeSet), Outliner.outliner);
 			}
 		}
 		
 		// Delete selection
 		delete(tree,layout,false);
 	}
 
 	private void paste(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// Abort if node is not editable
 		if (!tree.getOldestInSelection().isEditable()) {
 			return;
 		}
 			
 		// Put the Undoable onto the UndoQueue
 		CompoundUndoableInsert undoable = new CompoundUndoableInsert(currentNode.getParent());
 		tree.doc.undoQueue.add(undoable);
 
 		// Get the text from the clipboard and turn it into a tree
 		boolean isNodeSet = false;
 		String text = "";
 		NodeSet nodeSet = new NodeSet();
 		try {
 			Transferable selection = (Transferable) Outliner.clipboard.getContents(this);
 			if (selection != null) {
 				if (selection instanceof NodeSetTransferable) {
 					nodeSet = (NodeSet) selection.getTransferData(NodeSetTransferable.nsFlavor);
 					isNodeSet = true;
 				} else {
 					text = (String) selection.getTransferData(DataFlavor.stringFlavor);
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		Node oldestNode = tree.getOldestInSelection();
 		Node parentForNewNode = oldestNode.getParent();
 		int indexForNewNode = parentForNewNode.getChildIndex(oldestNode);
 			
 		tree.clearSelection();
 		
 		if (isNodeSet) {
 			for (int i = nodeSet.getSize() - 1; i >= 0; i--) {
 				Node node = nodeSet.getNode(i);
 				node.setTree(tree, true);
 				parentForNewNode.insertChild(node, indexForNewNode + 1);
 				node.setDepthRecursively(parentForNewNode.getDepth() + 1);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode,node,index));
 
 				tree.addNodeToSelection(node);
 			}
 		} else {
 			Node tempRoot = PadSelection.pad(text, tree, oldestNode.getDepth(), Preferences.LINE_END_STRING);
 		
 			for (int i = tempRoot.numOfChildren() - 1; i >= 0; i--) {
 				Node node = tempRoot.getChild(i);
 				parentForNewNode.insertChild(node, indexForNewNode + 1);
 				tree.insertNode(node);
 
 				// Record the Insert in the undoable
 				int index = node.currentIndex() + i;
 				undoable.addPrimitive(new PrimitiveUndoableInsert(parentForNewNode,node,index));
 
 				tree.addNodeToSelection(node);
 			}
 		}
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(tree.getYoungestInSelection());
 
 		// Redraw and Set Focus
 		layout.draw(tree.getYoungestInSelection(),OutlineLayoutManager.ICON);
 	}
 
 	private void selectAll(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// select all siblings
 		Node parent = currentNode.getParent();
 		
 		tree.addNodeToSelection(parent.getChild(0));
 		tree.selectRangeFromMostRecentNodeTouched(parent.getChild(parent.numOfChildren() - 1));
 
 		// Redraw and Set Focus
 		layout.draw(currentNode,OutlineLayoutManager.ICON);
 	}
 
 	private void selectInverse(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 
 		// select all siblings
 		Node parent = currentNode.getParent();
 		
 		for (int i = 0; i < parent.numOfChildren(); i++) {
 			Node child = parent.getChild(i);
 			
 			if (child.isSelected()) {
 				tree.removeNodeFromSelection(child);
 			} else {
 				tree.addNodeToSelection(child);
 			}
 		}
 		
 		if (tree.getNumberOfSelectedNodes() == 0) {
 			// Change to text node if all nodes were deselected.
 			changeFocusToTextArea(tree, layout, POSITION_FIRST);
 		} else {
 			// Redraw and Set Focus
 			layout.draw(currentNode,OutlineLayoutManager.ICON);
 		}
 	}
 
 	private void selectNone(TreeContext tree, OutlineLayoutManager layout) {
 		changeFocusToTextArea(tree, layout, POSITION_FIRST);
 	}
 
 	private void changeToParent(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		Node newSelectedNode = currentNode.getParent();
 		if (newSelectedNode.isRoot()) {return;}
 		
 		tree.setSelectedNodesParent(newSelectedNode.getParent());
 		tree.addNodeToSelection(newSelectedNode);
 		
 		tree.setEditingNode(newSelectedNode);
 		
 		// Redraw and Set Focus
 		layout.draw(newSelectedNode, OutlineLayoutManager.ICON);		
 	}
 	
 	private static final int UP = 1;
 	private static final int DOWN = 2;
 	private static final int LEFT = 3;
 	private static final int RIGHT = 4;
 	
 	private void navigate(TreeContext tree, OutlineLayoutManager layout, int type) {
 		Node node = null;
 		Node youngestNode = null;
 		Node oldestNode = null;
 		
 		switch(type) {
 			case UP:
 				youngestNode = tree.getYoungestInSelection();
 				node = youngestNode.prevSibling();
 				if (node == youngestNode) {return;}
 				tree.clearSelection();
 				break;
 
 			case DOWN:
 				oldestNode = tree.getOldestInSelection();
 				node = oldestNode.nextSibling();
 				if (node == oldestNode) {return;}
 				tree.clearSelection();
 				break;
 
 			case LEFT:
 				youngestNode = tree.getYoungestInSelection();
 				node = tree.getPrevNode(youngestNode);
 				if (node == null) {return;}
 				tree.setSelectedNodesParent(node.getParent());
 				break;
 
 			case RIGHT:
 				oldestNode = tree.getOldestInSelection();
 				node = tree.getNextNode(oldestNode);
 				if (node == null) {return;}
 				tree.setSelectedNodesParent(node.getParent());
 				break;
 				
 			default:
 				return;
 		}
 		
 		tree.addNodeToSelection(node);
 
 		// Record the EditingNode and CursorPosition and ComponentFocus
 		tree.setEditingNode(node);
 
 		// Redraw and Set Focus
 		layout.draw(node,OutlineLayoutManager.ICON);
 	}
 	
 	protected void delete (TreeContext tree, OutlineLayoutManager layout, boolean deleteMode) {
 		Node youngestNode = tree.getYoungestInSelection();
 		Node parent = tree.getEditingNode().getParent();
 		CompoundUndoableReplace undoable = new CompoundUndoableReplace(parent, deleteMode);
 
 		if (tree.isWholeDocumentSelected()) {
 			if (tree.isDocumentEmpty()) {return;}
 			
 			Node newNode = new NodeImpl(tree,"");
 			newNode.setDepth(0);
 			undoable.addPrimitive(new PrimitiveUndoableReplace(parent,youngestNode,newNode));
 
 			// Iterate over the remaining selected nodes deleting each one
 			for (int i = 1; i < tree.getNumberOfSelectedNodes(); i++) {
 				Node node = (Node) tree.selectedNodes.get(i);
 
 				// Abort if node is not editable
 				if (!node.isEditable()) {
 					continue;
 				}
 				
 				undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,null));
 			}
 		} else {
 			// Iterate over the remaining selected nodes deleting each one
 			for (int i = 0; i < tree.getNumberOfSelectedNodes(); i++) {
 				Node node = (Node) tree.selectedNodes.get(i);
 
 				// Abort if node is not editable
 				if (!node.isEditable()) {
 					continue;
 				}
 				
 				undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,null));
 			}
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();
 		}
 		
 		return;
 	}
 
 	private void merge(TreeContext tree, OutlineLayoutManager layout, boolean withSpaces) {
 		Node youngestNode = tree.getYoungestInSelection();
 		int youngestNodeDepth = youngestNode.getDepth();
 		int youngestNodeIsComment = youngestNode.getCommentState();
 		Node parent = tree.getEditingNode().getParent();
 		CompoundUndoableReplace undoable = new CompoundUndoableReplace(parent);
 
 		// Get merged text
 		StringBuffer buf = new StringBuffer();
 		boolean didMerge = false;
 		
 		if (withSpaces) {
 			for (int i = 0; i < tree.selectedNodes.size(); i++) {
 				Node node = (Node) tree.selectedNodes.get(i);
 				
 				// Abort if node is not editable
 				if (!node.isEditable()) {
 					continue;
 				}
 				
 				didMerge = true;
 				node.getMergedValueWithSpaces(buf);
 			}
 		} else {
 			for (int i = 0; i < tree.selectedNodes.size(); i++) {
 				Node node = (Node) tree.selectedNodes.get(i);
 				
 				// Abort if node is not editable
 				if (!node.isEditable()) {
 					continue;
 				}
 				
 				didMerge = true;
 				node.getMergedValue(buf);
 			}		
 		}
 		
 		// It's possible all nodes were read-only. If so then abort.
 		if (!didMerge) {
 			return;
 		}
 		
 		Node newNode = new NodeImpl(tree,buf.toString());
 		newNode.setDepth(youngestNodeDepth);
 		newNode.setCommentState(youngestNodeIsComment);
 		undoable.addPrimitive(new PrimitiveUndoableReplace(parent, youngestNode, newNode));
 
 		// Iterate over the remaining selected nodes deleting each one
 		for (int i = 1; i < tree.getNumberOfSelectedNodes(); i++) {
 			Node node = (Node) tree.selectedNodes.get(i);
 			
 			// Abort if node is not editable
 			if (!node.isEditable()) {
 				continue;
 			}
 
 			undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,null));
 		}
 
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 			undoable.redo();		
 		}
 		
 		return;
 	}
 
 
 	// Additional Outline Methods
 	public static void hoist(TreeContext tree) {
 		if (tree.selectedNodes.size() != 1) {
 			return;
 		}
 		
 		TextKeyListener.hoist(tree.getYoungestInSelection());
 		return;
 	}
 
 	public static void expandAllSubheads(TreeContext tree) {
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			((Node) tree.selectedNodes.get(i)).ExpandAllSubheads();
 		}
 		tree.doc.panel.layout.draw();
 		return;
 	}
 
 	public static void expandEverything(TreeContext tree) {
 		TextKeyListener.expandEverything(tree);
 		return;
 	}
 
 	public static void collapseToParent(TreeContext tree) {
 		TextKeyListener.collapseToParent(tree.getEditingNode());
 		return;
 	}
 
 	public static void collapseEverything(TreeContext tree) {
 		TextKeyListener.collapseEverything(tree);
 		return;
 	}
 
 
 	// Editable
 	private void clearEditable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			clearEditableForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void clearEditableForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getEditableState();
 		int newValue = Node.EDITABLE_INHERITED;
 		
 		if (oldValue != Node.EDITABLE_INHERITED) {
 			node.setEditableState(Node.EDITABLE_INHERITED);
 			undoable.addPrimitive(new PrimitiveUndoableEditableChange(node, oldValue, newValue));
 		}
 				
 		for (int i = 0; i < node.numOfChildren(); i++) {
 			clearEditableForSingleNode(node.getChild(i), undoable);
 		}
 	}
 	
 	private void toggleEditableAndClear(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleEditableAndClearForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void toggleEditableAndClearForSingleNode(Node node, CompoundUndoable undoable) {
 		toggleEditableForSingleNode(node, undoable);
 		
 		for (int i = 0; i < node.numOfChildren(); i++) {
 			clearEditableForSingleNode(node.getChild(i), undoable);
 		}		
 	}
 			
 	private void toggleEditable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleEditableForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void toggleEditableForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getEditableState();
 		int newValue = Node.EDITABLE_INHERITED;
 		boolean isEditable = node.isEditable();
 		
 		if (oldValue == Node.EDITABLE_FALSE) {
 			node.setEditableState(Node.EDITABLE_TRUE);
 			newValue = Node.EDITABLE_TRUE;
 					
 		} else if (oldValue == Node.EDITABLE_TRUE) {
 			node.setEditableState(Node.EDITABLE_FALSE);
 			newValue = Node.EDITABLE_FALSE;
 		
 		} else {
 			if (isEditable) {
 				node.setEditableState(Node.EDITABLE_FALSE);
 				newValue = Node.EDITABLE_FALSE;
 			} else {
 				node.setEditableState(Node.EDITABLE_TRUE);
 				newValue = Node.EDITABLE_TRUE;
 			}
 		}
 				
 		undoable.addPrimitive(new PrimitiveUndoableEditableChange(node, oldValue, newValue));
 	}
 	
 	private void toggleEditableInheritance(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleEditableInheritanceForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 	
 	protected static void toggleEditableInheritanceForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getEditableState();
 		int newValue = Node.EDITABLE_INHERITED;
 		boolean isEditable = node.isEditable();
 		
 		if (oldValue == Node.EDITABLE_INHERITED) {
 			if (isEditable) {
 				node.setEditableState(Node.EDITABLE_TRUE);
 				newValue = Node.EDITABLE_TRUE;
 			} else {
 				node.setEditableState(Node.EDITABLE_FALSE);
 				newValue = Node.EDITABLE_FALSE;
 			}
 								
 		} else {
 			node.setEditableState(Node.EDITABLE_INHERITED);
 		}
 				
 		undoable.addPrimitive(new PrimitiveUndoableEditableChange(node, oldValue, newValue));
 	}
 
 
 	// Moveable
 	private void clearMoveable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			clearMoveableForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void clearMoveableForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getMoveableState();
 		int newValue = Node.MOVEABLE_INHERITED;
 		
 		if (oldValue != Node.MOVEABLE_INHERITED) {
 			node.setMoveableState(Node.MOVEABLE_INHERITED);
 			undoable.addPrimitive(new PrimitiveUndoableMoveableChange(node, oldValue, newValue));
 		}
 				
 		for (int i = 0; i < node.numOfChildren(); i++) {
 			clearMoveableForSingleNode(node.getChild(i), undoable);
 		}
 	}
 	
 	private void toggleMoveableAndClear(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleMoveableAndClearForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void toggleMoveableAndClearForSingleNode(Node node, CompoundUndoable undoable) {
 		toggleMoveableForSingleNode(node, undoable);
 		
 		for (int i = 0; i < node.numOfChildren(); i++) {
 			clearMoveableForSingleNode(node.getChild(i), undoable);
 		}		
 	}
 			
 	private void toggleMoveable(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleMoveableForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 
 	protected static void toggleMoveableForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getMoveableState();
 		int newValue = Node.MOVEABLE_INHERITED;
 		boolean isMoveable = node.isMoveable();
 		
 		if (oldValue == Node.MOVEABLE_FALSE) {
 			node.setMoveableState(Node.MOVEABLE_TRUE);
 			newValue = Node.MOVEABLE_TRUE;
 					
 		} else if (oldValue == Node.MOVEABLE_TRUE) {
 			node.setMoveableState(Node.MOVEABLE_FALSE);
 			newValue = Node.MOVEABLE_FALSE;
 		
 		} else {
 			if (isMoveable) {
 				node.setMoveableState(Node.MOVEABLE_FALSE);
 				newValue = Node.MOVEABLE_FALSE;
 			} else {
 				node.setMoveableState(Node.MOVEABLE_TRUE);
 				newValue = Node.MOVEABLE_TRUE;
 			}
 		}
 				
 		undoable.addPrimitive(new PrimitiveUndoableMoveableChange(node, oldValue, newValue));
 	}
 	
 	private void toggleMoveableInheritance(TreeContext tree, OutlineLayoutManager layout) {
 		Node currentNode = textArea.node;
 		
 		CompoundUndoablePropertyChange undoable = new CompoundUndoablePropertyChange(tree);
 		
 		for (int i = 0; i < tree.selectedNodes.size(); i++) {
 			toggleMoveableInheritanceForSingleNode((Node) tree.selectedNodes.get(i), undoable);
 		}
 		
 		if (!undoable.isEmpty()) {
 			tree.doc.undoQueue.add(undoable);
 		}
 
 		layout.draw(currentNode, OutlineLayoutManager.ICON);
 	}
 	
 	protected static void toggleMoveableInheritanceForSingleNode(Node node, CompoundUndoable undoable) {
 		int oldValue = node.getMoveableState();
 		int newValue = Node.MOVEABLE_INHERITED;
 		boolean isMoveable = node.isMoveable();
 		
 		if (oldValue == Node.MOVEABLE_INHERITED) {
 			if (isMoveable) {
 				node.setMoveableState(Node.MOVEABLE_TRUE);
 				newValue = Node.MOVEABLE_TRUE;
 			} else {
 				node.setMoveableState(Node.MOVEABLE_FALSE);
 				newValue = Node.MOVEABLE_FALSE;
 			}
 								
 		} else {
 			node.setMoveableState(Node.MOVEABLE_INHERITED);
 		}
 				
 		undoable.addPrimitive(new PrimitiveUndoableMoveableChange(node, oldValue, newValue));
 	}
 }
