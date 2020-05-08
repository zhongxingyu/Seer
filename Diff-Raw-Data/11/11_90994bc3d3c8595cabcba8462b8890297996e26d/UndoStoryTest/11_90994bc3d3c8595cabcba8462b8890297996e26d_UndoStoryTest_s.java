 package org.jpacman.test.framework.accept;
 
 import org.jpacman.framework.ui.MainUI;
 import org.jpacman.framework.ui.UndoablePacman;
 
 /**
  * @author Rick van Hattem <Rick.van.Hattem@Fawo.nl>
  * 
  */
 public class UndoStoryTest extends MovePlayerStoryTest {
 	@Override
 	public MainUI makeUI() {
		return new UndoablePacman();
 	}
 }
