 package org.jpacman.test.framework.accept;
 
 import org.jpacman.framework.ui.MainUI;
 import org.jpacman.framework.ui.UndoablePacman;
 
 /**
  * @author Rick van Hattem <Rick.van.Hattem@Fawo.nl>
  * 
  */
 public class UndoStoryTest extends MovePlayerStoryTest {
	private UndoablePacman pacman;

 	@Override
 	public MainUI makeUI() {
		pacman = new UndoablePacman();
		return pacman;
	}
	
	@Override
	protected MainUI getUI() {
		return pacman;
 	}
 }
