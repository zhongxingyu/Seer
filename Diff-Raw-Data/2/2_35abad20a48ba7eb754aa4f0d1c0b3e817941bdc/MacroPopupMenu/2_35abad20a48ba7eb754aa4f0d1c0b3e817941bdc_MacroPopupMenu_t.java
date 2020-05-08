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
 import java.io.*;
 import javax.swing.*;
 import java.util.*;
 
 /**
  * @author  $Author$
  * @version $Revision$, $Date$
  */
 
 public class MacroPopupMenu extends JPopupMenu implements ActionListener, MouseListener {
 
 	// Constants
 	private static final int UPPER_BUFFER_SIZE = 30;
 	private static final int LOWER_BUFFER_SIZE = 50;
 	
 	// Sort Menu
 	private static String SORT = null;
 	private static String SORT_SHALLOW = null;
 	private static String SORT_DEEP = null;
 	
 	private static JMenu SORT_MENU = null;
 	private static JMenu SORT_SHALLOW_MENU = null;
 	private static JMenu SORT_DEEP_MENU = null;
 
 	private static String SORT_ASCENDING = null;
 	private static String SORT_DECENDING = null;
 
 	private static JMenu SORT_SHALLOW_ASCENDING_MENU = null;
 	private static JMenu SORT_SHALLOW_DECENDING_MENU = null;
 	private static JMenu SORT_DEEP_ASCENDING_MENU = null;
 	private static JMenu SORT_DEEP_DECENDING_MENU = null;
 
 
 	// Class Fields
 	public static ArrayList macros = new ArrayList();
 
 	public static ArrayList sortMacros = new ArrayList();
 
 
 	// The Constructors
 	public MacroPopupMenu() {
 		super();
 
 		SORT = GUITreeLoader.reg.getText("sort");
 		SORT_SHALLOW = GUITreeLoader.reg.getText("sort_shallow");
 		SORT_DEEP = GUITreeLoader.reg.getText("sort_deep");
 		SORT_ASCENDING = GUITreeLoader.reg.getText("sort_ascending");
 		SORT_DECENDING = GUITreeLoader.reg.getText("sort_descending");
 
 
 		SORT_MENU = new OutlinerSubMenuItem();
 		SORT_MENU.setText(SORT);
 		SORT_SHALLOW_MENU = new OutlinerSubMenuItem();
 		SORT_SHALLOW_MENU.setText(SORT_SHALLOW);
 		SORT_DEEP_MENU = new OutlinerSubMenuItem();
 		SORT_DEEP_MENU.setText(SORT_DEEP);
 
 		SORT_SHALLOW_ASCENDING_MENU = new OutlinerSubMenuItem();
 		SORT_SHALLOW_ASCENDING_MENU.setText(SORT_ASCENDING);
 		SORT_SHALLOW_DECENDING_MENU = new OutlinerSubMenuItem();
 		SORT_SHALLOW_DECENDING_MENU.setText(SORT_DECENDING);
 		SORT_DEEP_ASCENDING_MENU = new OutlinerSubMenuItem();
 		SORT_DEEP_ASCENDING_MENU.setText(SORT_ASCENDING);
 		SORT_DEEP_DECENDING_MENU = new OutlinerSubMenuItem();
 		SORT_DEEP_DECENDING_MENU.setText(SORT_DECENDING);
 
 
 		SORT_SHALLOW_MENU.insert(SORT_SHALLOW_ASCENDING_MENU, 0);
 		SORT_SHALLOW_MENU.insert(SORT_SHALLOW_DECENDING_MENU, 1);
 
 		SORT_DEEP_MENU.insert(SORT_DEEP_ASCENDING_MENU, 0);
 		SORT_DEEP_MENU.insert(SORT_DEEP_DECENDING_MENU, 1);
 		
 		SORT_MENU.insert(SORT_SHALLOW_MENU, 0);
 		SORT_MENU.insert(SORT_DEEP_MENU, 1);
 		
 		this.insert(SORT_MENU, 0);
 		this.insert(new JPopupMenu.Separator(), 1);
 	}
 
 
 	// Overrides show in JPopup.
 	public void show(Component invoker, int x, int y) {
 		if (macros.size() > 0) {
 			Point p = getPopupMenuOrigin(invoker, x, y);
     		super.show(invoker,p.x,p.y);
 		}
 	}
 
 	// This code is a start, until we can find a better way to popup long menus.
 	protected Point getPopupMenuOrigin(Component invoker, int x, int y){
 		//Figure out the sizes needed to calculate the menu position
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension pmSize = this.getSize();
 		// For the first time the menu is popped up
 		// the size has not yet been initialised
 		if(pmSize.width == 0){
 			pmSize = this.getPreferredSize();
 		}
 		
 		Point absp = new Point(x,y);
 		SwingUtilities.convertPointToScreen(absp, invoker);
 		
 		int aleft = absp.x + pmSize.width;
 		int abottom = absp.y + pmSize.height;
 		
 		if(aleft > screenSize.width) {
 			x -= aleft - screenSize.width;
 		}
 		
 		if(abottom > screenSize.height) {
 			y -= abottom - screenSize.height;
 		}
 		
 		return new Point(x,y);
 	}
 	
 	public void insert(Component item, int i) {
 		item.addMouseListener(this);
 		super.insert(item,i);
 	}
 
 
 	// MouseListener Interface
 	public void mouseEntered(MouseEvent e) {
 		JComponent item = (JComponent) e.getSource();
 		//JMenuItem item = (JMenuItem) e.getSource();
 		int itemHeight = item.getHeight();
 		Point p = new Point(0,itemHeight/2);
 		
 		SwingUtilities.convertPointToScreen(p, item);
 		
 		Point location = this.getLocationOnScreen();
 		
 		int lowerBound = getLowerScreenBoundary();
 		int upperBound = getUpperScreenBoundary();
 		
 		if (p.y < lowerBound) {
 			location.y += itemHeight;
 			this.setLocation(location);
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException ie) {
 				System.out.println("Interrupted Exception: " + ie);
  			}
 		} else if (p.y > upperBound) {
 			location.y -= itemHeight;
 			this.setLocation(location);
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException ie) {
 				System.out.println("Interrupted Exception: " + ie);
  			}
 		}
  	}
  	public void mouseExited(MouseEvent e) {}
  	public void mousePressed(MouseEvent e) {}
  	public void mouseReleased(MouseEvent e) {}
  	public void mouseClicked(MouseEvent e) {}
 
 
 	private int getLowerScreenBoundary() {
 		return UPPER_BUFFER_SIZE;
 	}
 	
 	private int getUpperScreenBoundary() {
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		return screenSize.height - LOWER_BUFFER_SIZE;
 	}
 
 	public boolean isNameUnique(String name) {
 		for (int i = 0; i < macros.size(); i++) {
 			if (name.equals(getMacro(i).getName())) {
 				return false;
 			}
 		}
 		return true;	
 	}
 	
 	public int addMacro(Macro macro) {
 		if (macro instanceof SortMacro) {
 			// Find the correct spot to add it alphabetically
 			int i;
 			for (i = 0; i < sortMacros.size(); i++) {
 				Macro macroTemp = (Macro) sortMacros.get(i);
 				if (macroTemp.getName().compareTo(macro.getName()) >= 0) {
 					break;
 				}
 			}
 			
 			sortMacros.add(i, macro);
 			
 			JMenuItem shallowAscendingItem = new JMenuItem(macro.getName());
 			JMenuItem shallowDecendingItem = new JMenuItem(macro.getName());
 			JMenuItem deepAscendingItem = new JMenuItem(macro.getName());
 			JMenuItem deepDecendingItem = new JMenuItem(macro.getName());
 			
 			shallowAscendingItem.addActionListener(this);
 			shallowDecendingItem.addActionListener(this);
 			deepAscendingItem.addActionListener(this);
 			deepDecendingItem.addActionListener(this);
 			
 			SORT_SHALLOW_ASCENDING_MENU.insert(shallowAscendingItem,i);
 			SORT_SHALLOW_DECENDING_MENU.insert(shallowDecendingItem,i);
 			SORT_DEEP_ASCENDING_MENU.insert(deepAscendingItem,i);
 			SORT_DEEP_DECENDING_MENU.insert(deepDecendingItem,i);
 			
 			return i;		
 		} else {
 			// Find the correct spot to add it alphabetically
 			int i;
 			for (i = 0; i < macros.size(); i++) {
 				Macro macroTemp = (Macro) macros.get(i);
 				if (macroTemp.getName().compareTo(macro.getName()) >= 0) {
 					break;
 				}
 			}
 			
 			macros.add(i, macro);
 			JMenuItem item = new JMenuItem(macro.getName());
 			item.addActionListener(this);
 			this.insert(item,i + 2);
 			return i;
 		}
 	}
 		
 	public int removeMacro(Macro macro) {
 		if (macro instanceof SortMacro) {
 			int index = sortMacros.indexOf(macro);
 			sortMacros.remove(index);
 			
 			SORT_SHALLOW_ASCENDING_MENU.remove(index);
 			SORT_SHALLOW_DECENDING_MENU.remove(index);
 			SORT_DEEP_ASCENDING_MENU.remove(index);
 			SORT_DEEP_DECENDING_MENU.remove(index);
 			
 			return index;
 		} else {
 			int index = macros.indexOf(macro);
 			macros.remove(index);
 			this.remove(index + 2);
 			return index;
 		}
 	}
 	
 	public Macro getMacro(int i) {
 		return (Macro) macros.get(i);	
 	}
 	
 	public Macro getMacro(String name) {
 		// First check Macros
 		for (int i = 0; i < macros.size(); i++) {
 			Macro macro = getMacro(i);
 			if (macro.getName().equals(name)) {
 				return macro;
 			}
 		}
 		
 		// Then check SortMacros
 		for (int i = 0; i < sortMacros.size(); i++) {
 			SortMacro macro = getSortMacro(i);
 			if (macro.getName().equals(name)) {
 				return macro;
 			}
 		}
 
 		return null;
 	}
 
 	public SortMacro getSortMacro(int i) {
 		return (SortMacro) sortMacros.get(i);	
 	}
 
 	// ActionListener Interface
 	public void actionPerformed(ActionEvent e) {
 		startWaitCursor();
 
 		// Get the Macro
 		Macro macro = getMacro(e.getActionCommand());
 
 		// Shorthand
 		OutlinerDocument document = Outliner.getMostRecentDocumentTouched();
 		TreeContext tree = document.tree;
 		
 		// Handle Undoability Confirmation
 		if (!macro.isUndoable()) {
 			String msg = GUITreeLoader.reg.getText("confirmation_operation_not_undoable");
 
 			int result = JOptionPane.showConfirmDialog(document, msg,"",JOptionPane.OK_CANCEL_OPTION);
 			if (result == JOptionPane.YES_OPTION) {
 				// Proceed, do nothing
 			} else if (result == JOptionPane.CANCEL_OPTION) {
 				return;
 			}			
 		}
 		
 		if (macro.getUndoableType() == Macro.SIMPLE_UNDOABLE) {
 			doSimpleUndoableMacro(document, tree, macro);
 			
 		} else if (macro.getUndoableType() == Macro.COMPLEX_UNDOABLE) {
 			doComplexUndoableMacro(document, tree, macro);
 			
 		} else if (macro.getUndoableType() == Macro.RAW_MACRO_UNDOABLE) {
 			if (macro instanceof SortMacro) {
 				JMenu parentMenu = (JMenu) ((JPopupMenu) ((JComponent) e.getSource()).getParent()).getInvoker();
 				String parentMenuText = parentMenu.getText();
 				String parentParentMenuText = ((JMenu) ((JPopupMenu) (parentMenu).getParent()).getInvoker()).getText();
 				
 				if (SORT_SHALLOW.equals(parentParentMenuText)) {
 					if (SORT_ASCENDING.equals(parentMenuText)) {
 						((SortMacro) macro).process(SortMacro.MODE_SHALLOW + SortMacro.MODE_ASCENDING);
 					} else if (SORT_DECENDING.equals(parentMenuText)) {
 						((SortMacro) macro).process(SortMacro.MODE_SHALLOW + SortMacro.MODE_DECENDING);
 					}
 				} else if (SORT_DEEP.equals(parentParentMenuText)) {
 					if (SORT_ASCENDING.equals(parentMenuText)) {
 						((SortMacro) macro).process(SortMacro.MODE_DEEP + SortMacro.MODE_ASCENDING);
 					} else if (SORT_DECENDING.equals(parentMenuText)) {
 						((SortMacro) macro).process(SortMacro.MODE_DEEP + SortMacro.MODE_DECENDING);
 					}
 				} else {
 					((SortMacro) macro).process();
 				}
 			} else {
 				((RawMacro) macro).process();
 			}
 			
 		} else {
 			// TODO: Need code for when it is not undoable.
 		}
 		
 		// Redraw
 		tree.doc.panel.layout.redraw();
 		
 		endWaitCursor();
 	}
 	
 	private void doSimpleUndoableMacro(OutlinerDocument document, TreeContext tree, Macro macro) {
 		CompoundUndoableEdit undoable = new CompoundUndoableEdit(tree);
 		
 		if (tree.getComponentFocus() == OutlineLayoutManager.TEXT) {
 			// Create a nodeRangePair
 			Node node = tree.getEditingNode();
 			
 			// Abort if not editable
 			if (!node.isEditable()) {
 				return;
 			}
 			
 			int cursor = tree.getCursorPosition();
 			int mark = tree.getCursorMarkPosition();
 			int startIndex = Math.min(cursor,mark);
 			int endIndex = Math.max(cursor,mark);
 			
 			NodeRangePair nodeRangePair = new NodeRangePair(node, startIndex, endIndex);
 			
 			// Process the macro and create undoable
 			String oldText = nodeRangePair.node.getValue();
 			macro.process(nodeRangePair);
 			String newText = nodeRangePair.node.getValue();
 			
 			if (macro.isUndoable()) {
 				undoable.addPrimitive(new PrimitiveUndoableEdit(nodeRangePair.node,oldText, newText));
 			}
 			
 			tree.setCursorPosition(nodeRangePair.endIndex);
 			tree.setCursorMarkPosition(nodeRangePair.startIndex);
 		} else {
 			for (int i = 0; i < tree.selectedNodes.size(); i++) {
				Node node = tree.selectedNodes.get(i);
 				
 				// Abort if not editable
 				if (!node.isEditable()) {
 					continue;
 				}
 
 				// Create a nodeRangePair
 				NodeRangePair nodeRangePair = new NodeRangePair(node,-1,-1);
 				
 				// Process the macro and create undoable
 				String oldText = nodeRangePair.node.getValue();
 				macro.process(nodeRangePair);
 				String newText = nodeRangePair.node.getValue();
 				
 				if (macro.isUndoable()) {
 					undoable.addPrimitive(new PrimitiveUndoableEdit(nodeRangePair.node, oldText, newText));
 				}				
 			}
 		}
 
 		if (macro.isUndoable()) {
 			if (!undoable.isEmpty()) {
 				document.undoQueue.add(undoable);
 			}
 		} else {
 			document.undoQueue.clear();
 		}	
 	}
 
 	private void doComplexUndoableMacro(OutlinerDocument document, TreeContext tree, Macro macro) {
 		Node parent = tree.getEditingNode().getParent();
 		CompoundUndoableReplace undoable = new CompoundUndoableReplace(parent);
 		
 		if (tree.getComponentFocus() == OutlineLayoutManager.TEXT) {
 			// Create a nodeRangePair
 			Node node = tree.getEditingNode();
 
 			// Abort if not editable
 			if (!node.isEditable()) {
 				return;
 			}
 
 			int cursor = tree.getCursorPosition();
 			int mark = tree.getCursorMarkPosition();
 			int startIndex = Math.min(cursor,mark);
 			int endIndex = Math.max(cursor,mark);
 			
 			Node clonedNode = node.cloneClean();
 			NodeRangePair nodeRangePair = new NodeRangePair(clonedNode,startIndex,endIndex);
 			
 			// Process the macro and create undoable
 			Object obj = macro.process(nodeRangePair);
 			if (obj != null) {
 				undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,nodeRangePair.node));
 			}
 		} else {
 			for (int i = 0; i < tree.selectedNodes.size(); i++) {
 				// Create a nodeRangePair
 				Node node = tree.selectedNodes.get(i);
 
 				// Abort if not editable
 				if (!node.isEditable()) {
 					continue;
 				}
 				
 				Node clonedNode = node.cloneClean();
 				NodeRangePair nodeRangePair = new NodeRangePair(clonedNode,-1,-1);
 
 				// Process the macro
 				Object obj = macro.process(nodeRangePair);
 				if (obj != null) {
 					undoable.addPrimitive(new PrimitiveUndoableReplace(parent,node,nodeRangePair.node));
 				}
 			}
 		}
 
 		if (macro.isUndoable()) {
 			if (!undoable.isEmpty()) {
 				document.undoQueue.add(undoable);
 				undoable.redo();
 			}
 		} else {
 			document.undoQueue.clear();
 		}
 	}
 
 	
 	// Class Methods
 	private static Cursor normalCursor = null;
 	private static Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
 	
 	protected static void startWaitCursor() {
 		Component comp = Outliner.outliner.getGlassPane();
 		
 		// Store the normal cursor
 		normalCursor = comp.getCursor();
 		
 		// Set the cursor to the wait cursor
 		comp.setVisible(true);
 		comp.setCursor(waitCursor);
 	}
 	
 	protected static void endWaitCursor() {
 		Component comp = Outliner.outliner.getGlassPane();
 		
 		if (normalCursor != null) {
 			comp.setCursor(normalCursor);
 		}
 		comp.setVisible(false);
 	}
 
 	public static boolean validateExistence(String name) {
 		if (name.equals("")) {
 			return false;
 		} else {
 			return true;
 		}	
 	}
 
 	public static boolean validateUniqueness(String name) {
 		if (Outliner.macroPopup.isNameUnique(name)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static boolean validateRestrictedChars(String name) {
 		// This set shouldn't exist in filenames so we need to check for them.
 		if (name.indexOf("\\") != -1) {
 			return false;
 		} else if (name.indexOf("/") != -1) {
 			return false;
 		} else if (name.indexOf(":") != -1) {
 			return false;
 		} else if (name.indexOf("*") != -1) {
 			return false;
 		} else if (name.indexOf("?") != -1) {
 			return false;
 		} else if (name.indexOf("\"") != -1) {
 			return false;
 		} else if (name.indexOf("<") != -1) {
 			return false;
 		} else if (name.indexOf(">") != -1) {
 			return false;
 		} else if (name.indexOf("|") != -1) {
 			return false;
 			
 		// This set shouldn't happen, but let's check anyway.
 		} else if (name.indexOf("\r") != -1) {
 			return false;
 		} else if (name.indexOf("\n") != -1) {
 			return false;
 		} else if (name.indexOf("\t") != -1) {
 			return false;
 		
 		// Looks good.
 		} else {
 			return true;
 		}
 	}
 }
