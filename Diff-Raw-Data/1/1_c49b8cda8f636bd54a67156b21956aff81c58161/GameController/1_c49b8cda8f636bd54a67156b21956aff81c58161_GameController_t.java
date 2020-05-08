 package se.chalmers.kangaroo.controller;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import se.chalmers.kangaroo.model.GameModel;
 import se.chalmers.kangaroo.model.utils.Direction;
 import se.chalmers.kangaroo.utils.CustomKeys;
 import se.chalmers.kangaroo.view.ChangeView;
 import se.chalmers.kangaroo.view.GameView;
 
 /**
  * A class for handling and running the game.
  * 
  * @author simonal
  * @modifiedby pavlov
  * 
  */
 public class GameController implements KeyListener, PropertyChangeListener {
 
 	private GameModel gm;
 	private GameView gv;
 	private ChangeView cv;
 	private CustomKeys ck;
 
 	public GameController(ChangeView cv) {
 		ck = CustomKeys.getInstance();
 		this.cv = cv;
 		gm = cv.getGameModel();
 		gv = new GameView("resources/images/background.gif", gm, cv);
 		cv.setGameView(gv);
 		gv.getObserver().addPropertyChangeListener(this);
 		gv.addKeyListener(this);
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
 				long time = System.currentTimeMillis();
 				if (gv.isRunning()) {
 					
 					gm.update();
 					if(gm.isLevelFinished()){
 						if(gm.isGameFinished()){
 							cv.finishedView();
 						}else
 							setVictoryView();
 					}
 					gv.repaint();
 					gv.revalidate();
 					
 				}
 				try {
 					diff = System.currentTimeMillis() - time;
 					if (diff < 1000 / 60)
 						Thread.sleep(1000 / 60 - diff);
 				} catch (InterruptedException e) {
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
 			gv.initAnimations();
 		}
 
 	}
 
 	private void pressedKey(KeyEvent e) {
 		int code = e.getKeyCode();
 		if (code == ck.getJumpKey()) {
 			if (!gm.getKangaroo().getStillJumping() && gv.isRunning()) {
 				gm.getKangaroo().setStillJumping(true);
 				gm.getKangaroo().jump();
 			}
 		} else if (code == ck.getLeftKey()) {
 			gm.getKangaroo().setDirection(Direction.DIRECTION_WEST);
 		} else if (code == ck.getRightKey()) {
 			gm.getKangaroo().setDirection(Direction.DIRECTION_EAST);
 		} else if (code == ck.getItemKey() && gv.isRunning()) {
 			if (gm.getKangaroo().getItem() != null)
 				gm.getKangaroo().getItem().onUse(gm.getKangaroo());
 
 		} else if (code == KeyEvent.VK_ESCAPE) {
 			if (!gv.isRunning()) {
 				pauseGame();
 			} else {
 				resumeGame();
 			}
 		} else {
 			// If any other keys are pressed, restart the level.
 			if(gv.isRunning())
 				gm.restartLevel();
 		}
 
 	}
 
 	public void releaseKey(KeyEvent e) {
 		int code = e.getKeyCode();
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
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName().equals("start") )
 				start();
 	}
 
 }
