 /*
   File: UndoAction.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.util.undo;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.undo.*;
 
 import cytoscape.util.CytoscapeAction;
 
 /**
  * An action that calls undo for the most recent edit in the
  * undoable edit stack.
  */
 public class UndoAction extends CytoscapeAction {
 
 	/**
 	 * Constructs the action.
 	 */
 	public UndoAction() {
 		super("Undo");
 		setAcceleratorCombo(KeyEvent.VK_Z, ActionEvent.CTRL_MASK);
 		setPreferredMenu("Edit");
		setEnabled(true);
 	}
 
     /**
      * Tries to run undo() on the top edit of the edit stack.
      * @param e The action event that triggers this method call.
      */
 	public void actionPerformed(ActionEvent e) {
 		try {
 			if ( CyUndo.undoManager.canUndo() )
 				CyUndo.undoManager.undo();
 		} catch (CannotUndoException ex) {
 			System.out.println("Unable to undo: " + ex);
 			ex.printStackTrace();
 		}
 	}
 
     /**
      * Called when the menu that contains this action is clicked on.
      * @param e The menu event that triggers this method call.
      */
 	public void menuSelected(MenuEvent e) {
 		if (CyUndo.undoManager.canUndo()) {
 			setEnabled(true);
 			putValue(Action.NAME, CyUndo.undoManager.getUndoPresentationName());
 		} else {
 			setEnabled(false);
 			putValue(Action.NAME, "Undo");
 		}
 	}

	/**
	 * Called when a menu is hidden once you click on a menu item or elsewhere.
	 * This is a hack to make sure that undo is available via the accelerator
	 * keys at all times.  
     * @param e The menu event that triggers this method call.
	 */
	public void menuDeselected(MenuEvent e) {
		setEnabled(true);
	}
 }
