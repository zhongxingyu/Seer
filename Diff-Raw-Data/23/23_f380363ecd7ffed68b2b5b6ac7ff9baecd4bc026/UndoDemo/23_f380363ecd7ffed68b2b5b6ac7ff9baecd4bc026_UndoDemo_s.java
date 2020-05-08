 package org.iugonet.www;
 
 /*
  * An example of how to implement undo in Java.
  * Michael Terry, mterry@cs.uwaterloo.ca
  * 
  * The code is rather dense with inner classes, but hopefully you can 
  * follow the logic. You can break the inner classes out into separate
  * methods if it makes it easier to follow.
  */
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.undo.AbstractUndoableEdit;
 import javax.swing.undo.UndoManager;
 import javax.swing.undo.UndoableEdit;
 
 public class UndoDemo {
 
 	// Undo manager
 	private UndoManager undoManager;
 
 	// Our user interface components
 	private JCheckBox fullUndoCheckBox;
 
 	private JSlider slider;
 
 	// Menu items
 	private JMenuItem undoMenuItem;
 
 	private JMenuItem redoMenuItem;
 
 	// Keeps track of the last slider value prior to the user changing it
 	private int lastSliderValue = 0;
 
 	// Tracks whether we are currently undoing or redoing a slider change
 	private boolean undoRedoInProgress = false;
 
 	public UndoDemo() {
 
 		// Create our undo manager
 		undoManager = new UndoManager();
 
 		// Create our user interface controls
 		fullUndoCheckBox = new JCheckBox("Fully Undoable Button");
 		slider = new JSlider();
 
 		// Create a simple box to hold all of our controls
 		Box box = new Box(BoxLayout.Y_AXIS);
 		box.add(fullUndoCheckBox);
 		box.add(slider);
 
 		// Create a frame and add the box of controls to it
 		JFrame frame = new JFrame();
 		frame.getContentPane().add(box);
 
 		// Create a menu and menu items
 		JMenuBar menuBar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 		JMenu editMenu = new JMenu("Edit");
 		menuBar.add(fileMenu);
 		menuBar.add(editMenu);
 		frame.setJMenuBar(menuBar);
 
 		// Create a "quit" menu item and add it to the file menu
 		JMenuItem quitMenuItem = new JMenuItem("Quit");
 		quitMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				System.exit(0);
 			}
 		});
 		fileMenu.add(quitMenuItem);
 
 		// Create our edit menu items
 		undoMenuItem = new JMenuItem("Undo");
 		redoMenuItem = new JMenuItem("Redo");
 		editMenu.add(undoMenuItem);
 		editMenu.add(redoMenuItem);
 
 		// Set undo/redo to be disabled, initially
 		undoMenuItem.setEnabled(false);
 		redoMenuItem.setEnabled(false);
 
 		/*
 		 * Create our action event listeners for undo/redo For these, we simply
 		 * call undoManager's undo or redo command, then update the menu items'
 		 * enabled state based on whether the undo manager says we can undo or
 		 * redo at the present time
 		 */
 		undoMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				if (undoManager.canUndo()) {
 					undoManager.undo();
 				}
 				undoMenuItem.setEnabled(undoManager.canUndo());
 				redoMenuItem.setEnabled(undoManager.canRedo());
 			}
 		});
 		redoMenuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				if (undoManager.canRedo()) {
 					undoManager.redo();
 				}
 				undoMenuItem.setEnabled(undoManager.canUndo());
 				redoMenuItem.setEnabled(undoManager.canRedo());
 			}
 		});
 
 		/*
 		 * Add an action listener to the fully-undoable check box. Whenever an
 		 * action is performed, create a new UndoableEdit item that has methods
 		 * to undo and redo the action. Add this edit to the undo manager, and
 		 * update the menu items accordingly
 		 */
 		fullUndoCheckBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 
 				// Create a new undo action
 				// Save our current state in a final boolean so it is accessible
 				// in the inner class below
 				final boolean curState = fullUndoCheckBox.isSelected();
 				UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 
 					/**
 					 * 
 					 */
 					private static final long serialVersionUID = 1L;
 
 					// Method that is called when we must redo the undone action
 					public void redo()
 							throws javax.swing.undo.CannotRedoException {
 						super.redo();
 
 						// Set the checkbox to its pre-undone state
 						fullUndoCheckBox.setSelected(curState);
 
 						// Update our menu items
 						undoMenuItem.setEnabled(undoManager.canUndo());
 						redoMenuItem.setEnabled(undoManager.canRedo());
 					}
 
 					public void undo()
 							throws javax.swing.undo.CannotUndoException {
 						super.undo();
 
 						// Set the checkbox to its previous state
 						fullUndoCheckBox.setSelected(!curState);
 
 						// Update our menu items
 						undoMenuItem.setEnabled(undoManager.canUndo());
 						redoMenuItem.setEnabled(undoManager.canRedo());
 					}
 				};
 
 				// Add this undoable edit to the undo manager
 				undoManager.addEdit(undoableEdit);
 
 				// Update the state of the menu items
 				undoMenuItem.setEnabled(undoManager.canUndo());
 				redoMenuItem.setEnabled(undoManager.canRedo());
 			}
 		});
 
 		// Save the initial value of the slider
 		lastSliderValue = slider.getValue();
 
 		// Add a change listener so we can undo changes to the slider
 		slider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent event) {
 
 				/*
 				 * We only want to record a change to the slider if the user is
 				 * done adjusting it. Also, we need to make sure we don't add a
 				 * new event to the undo manager if we are currently changing
 				 * the slider's value as a result of undoing an action. Thus, we
 				 * keep track of whether an undo/redo is in progress or not
 				 */
 				if (!slider.getValueIsAdjusting()) {
 
 					if (!undoRedoInProgress) {
 						// Create an undoable action
 						// Save our old and new slider values in final int's
 						// that will be accessible in
 						// the inner class below
 						final int newValue = slider.getValue();
 						final int oldValue = lastSliderValue;
 						UndoableEdit undoableEdit = new AbstractUndoableEdit() {
 							/**
 							 * 
 							 */
 							private static final long serialVersionUID = 1L;
 
 							public void redo()
 									throws javax.swing.undo.CannotRedoException {
 								super.redo();
 
 								// Flag that we are in a redo, then change the
 								// slider value to the new value
 								// If we don't do this, we will create a new
 								// undoable edit, which we don't want to do...
 								// This is because when we call "setValue" then
 								// the stateChanged event will be called again
 								undoRedoInProgress = true;
 								slider.setValue(newValue);
 								undoRedoInProgress = false;
 
 								// Update our menu items
 								undoMenuItem.setEnabled(undoManager.canUndo());
 								redoMenuItem.setEnabled(undoManager.canRedo());
 							}
 
 							public void undo()
 									throws javax.swing.undo.CannotUndoException {
 								super.undo();
 
 								// Flag that we are in an undo, then change the
 								// slider value to the new value
 								// If we don't do this, we will create a new
 								// undoable edit, which we don't want to do...
 								// This is because when we call "setValue" then
 								// the stateChanged event will be called again
 								undoRedoInProgress = true;
 								slider.setValue(oldValue);
 								undoRedoInProgress = false;
 
 								// Update our menu items
 								undoMenuItem.setEnabled(undoManager.canUndo());
 								redoMenuItem.setEnabled(undoManager.canRedo());
 							}
 						};
 
 						// Add the undo action
 						undoManager.addEdit(undoableEdit);
 
 						// Update the state of our menu items
 						undoMenuItem.setEnabled(undoManager.canUndo());
 						redoMenuItem.setEnabled(undoManager.canRedo());
 					}
 
 					// Save the lastSliderValue for the next undoable action
 					lastSliderValue = slider.getValue();
 				}
 			}
 		});
 
 		/*
 		 * Add a window listener to our frame to quit the app if the main frame
 		 * is closed.
 		 */
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(java.awt.event.WindowEvent event) {
 				System.exit(0);
 			};
 		});
 
 		// Pack and show our frame
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) {
 		UndoDemo demo = new UndoDemo();
 	}
 
 }
