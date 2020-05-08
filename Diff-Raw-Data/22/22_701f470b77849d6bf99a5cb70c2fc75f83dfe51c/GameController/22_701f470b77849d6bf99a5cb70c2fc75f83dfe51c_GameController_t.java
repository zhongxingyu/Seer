 package se.chalmers.kangaroo.controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import se.chalmers.kangaroo.model.GameModel;
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.view.ChangeView;
 import se.chalmers.kangaroo.view.GameView;
 
 /**
  * A class for handling and running the game.
  * 
  * @author simonal
  * @modifiedby pavlov
  * 
  */
 public class GameController implements KeyListener {
 
 	private GameModel gm;
 	private GameView gv;
 
 	private CustomKeys ck;
 
 	public GameController(ChangeView cv) {
 		ck = CustomKeys.getInstance();
 		gm = new GameModel();
 		gv = new GameView("resources/images/background.gif", gm, cv);
 	}
 
 	public GameView getGameView() {
 		return gv;
 	}
 
 	public void start() {
 		gm.start();
 		new Thread(new PlayModel()).start();
 	}
 
 	private void pauseGame() {
 		gv.togglePause();
 		gv.repaint();
 	}
 
 	private void resumeGame() {
 		gv.togglePause();
 	}
 
 	class PlayModel implements Runnable {
 		private long diff;
 
 		public void run() {
 			while (true) {
				if (!gv.getIsPaused()) {
 					long time = System.currentTimeMillis();
 					gm.update();
 					if(gm.isLevelFinished()){
 						setVictoryView();
 					}
 					gv.repaint();
 					gv.revalidate();
 					try {
 						diff = System.currentTimeMillis() - time;
 						if (diff < 1000 / 60)
 							Thread.sleep(1000 / 60 - diff);
 					} catch (InterruptedException e) {
 					}
 				}
 			}
 		}
 
 		private void setVictoryView() {
 			gv.showVictoryView();
 			while(!gv.startNewLevel())
 				try{
 					Thread.sleep(100);
 				}catch(InterruptedException e){
 				}
 			gv.removeVictoryView();
 			gm.nextLevel();
 		}
 
 	}
 
 	private void pressedKey(KeyEvent e) {
 		int code = e.getKeyCode();
 		// Jump
 		if (code == ck.getJumpKey()) {
 			if (gm.getKangaroo().getStillJumping() == false) {
 				gm.getKangaroo().setStillJumping(true);
 				gm.getKangaroo().jump();
 			}
 
 			// Left
 		} else if (code == ck.getLeftKey()) {
 			gm.getKangaroo().setDirection(Direction.DIRECTION_WEST);
 
 			// Right
 		} else if (code == ck.getRightKey()) {
 			gm.getKangaroo().setDirection(Direction.DIRECTION_EAST);
 		} else if (code == ck.getItemKey()) {
 			if (gm.getKangaroo().getItem() != null)
 				gm.getKangaroo().getItem().onUse(gm.getKangaroo());
 
 		} else if (code == KeyEvent.VK_ESCAPE) {
			if (!gv.getIsPaused() == true) {
 				pauseGame();
 			} else {
 				resumeGame();
 			}
 		} else {
 			// If any other keys are pressed, restarts the level.
 			gm.restartLevel();
 		}
 
 	}
 
 	public void releaseKey(KeyEvent e) {
 		int code = e.getKeyCode();
 		// TODO Fix so jump is key sensitive
 		if (code == ck.getJumpKey()) {
 			gm.getKangaroo().setStillJumping(false);
 		}
 		if (code == ck.getLeftKey()) {
 			gm.getKangaroo().setDirection(Direction.DIRECTION_NONE);
 		} else if (code == ck.getRightKey()) {
 			gm.getKangaroo().setDirection(Direction.DIRECTION_NONE);
 		}
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		pressedKey(e);
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		releaseKey(e);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// Nothing to do here.
 	}
 
 }
