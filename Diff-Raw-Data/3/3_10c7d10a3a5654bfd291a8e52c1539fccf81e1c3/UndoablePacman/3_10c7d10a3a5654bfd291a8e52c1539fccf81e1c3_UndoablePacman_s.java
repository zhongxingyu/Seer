 /**
  * 
  */
 package org.jpacman.framework.ui;
 
 import org.jpacman.framework.factory.FactoryException;
 
 /**
  * @author Rick van Hattem <Rick.van.Hattem@Fawo.nl>
  * 
  */
 public class UndoablePacman extends MainUI {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1938866969221252775L;
 
 	/**
 	 * Main tarting point of the undoable pacman game.
 	 * 
 	 * @param args
 	 *            Ignored
 	 * @throws FactoryException
 	 *             If reading game map fails.
 	 */
 	public static void main(String[] args) throws FactoryException {
 		new UndoablePacman().main();
 	}
 
 	@Override
 	public MainUI initialize() throws FactoryException {
 		withButtonPanel(new UndoButtonPanel());
 		return super.initialize();
 	}
 
 	/**
 	 * Redo the last move.
 	 */
 	void redo() {
 
 	}
 
 	/**
 	 * Undo the last move.
 	 */
 	void undo() {
 
 	}
 }
