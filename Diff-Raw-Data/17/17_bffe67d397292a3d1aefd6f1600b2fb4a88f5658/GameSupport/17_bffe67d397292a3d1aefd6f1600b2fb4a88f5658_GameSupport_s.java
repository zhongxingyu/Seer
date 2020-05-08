 package org.sankozi.rogueland.gui;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.control.Game;
 import org.sankozi.rogueland.control.LogListener;
 import org.sankozi.rogueland.model.Controls;
 import org.sankozi.rogueland.model.Move;
 
 /**
  * Class supporting interaction between GameState and GUI
  * @author sankozi
  */
 @Singleton
 class GameSupport {
     private final static Logger LOG = Logger.getLogger(GameSupport.class);
 
     private final List<GameListener> listeners = Lists.newArrayList();
 
     private final Game game;
     private final GameEvent gameEvent;
 
     /**
      * Lock that protects Game object during move processing
      *
      * Write lock is locked before game is started and is released when player is moving 
 	 * (i.e. game is waiting for human player move)
      */
     private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
 
     private final Thread gameThread = new Thread(new GameStart());
 
     private final TilePainter painter;
 
     private Dimension paintedLevelFragment;
     private Rectangle levelSize;
     private BufferedImage levelImage = new BufferedImage(1,1, BufferedImage.TYPE_4BYTE_ABGR);
 
 	@Inject
     public GameSupport(Game game) {
         Preconditions.checkNotNull(game, "game cannot be null");
         this.game = game;
 		this.levelSize = new Rectangle(0, 0,
 				game.getLevel().getWidth(), game.getLevel().getHeight());
         this.gameEvent = new GameEvent(game);
         //TODO Guice
         this.painter = new FontPainter();
     }
 
 	public void setControls(Controls controls){
 		this.game.setControls(new SynchronizedControls(controls));
 	}
 
     public void resize(Dimension newDim) {
 //        LOG.info("component resized : new size : " + newDim);
         BufferedImage newImage = new BufferedImage(newDim.width, newDim.height, BufferedImage.TYPE_4BYTE_ABGR);
         Graphics2D g = null;
         try {
             g = newImage.createGraphics();
             if(readWriteLock.readLock().tryLock()){
                 try {
                     painter.paint(game, g, newDim.width, newDim.height);
                 } finally {
                     readWriteLock.readLock().unlock();
                 }
             }
         } finally {
             g.dispose();
         }
         levelImage = newImage;
     }
 
     public Image getLevelImage(){
         return levelImage;
     }
 
     public void addListener(GameListener listener){
 //		LOG.info(this.toString() + " adding " + listener.getClass());
         listeners.add(listener);
     }
 
     public void removeListener(GameListener listener){
         listeners.remove(listener);
     }
 
     public void clearListeners(){
         this.listeners.clear();
     }
 
     public Rectangle getPlayerLocation(){
         return painter.getPixelLocation(game, 
 				levelImage.getWidth(), levelImage.getHeight(),
 				game.getPlayer().getLocation());
     }
 
     public void fireGameEvent(GameEvent ge){
         for(GameListener listener : listeners){
             listener.onEvent(ge);
         }
     }
 
     void addLogListener(LogListener logListener) {
         game.addLogListener(logListener);
     }
 
     public void gameStart() {
         Preconditions.checkState(!gameThread.isAlive());
         gameThread.start();
     }
 
 	void paintLevelImage(Graphics g, JComponent comp) {
 		try {
 			readWriteLock.readLock().lock();
         	g.drawImage(levelImage, 0, 0, comp.getWidth(), comp.getHeight(), comp);
 		} finally {
 			readWriteLock.readLock().unlock();
 		}
 	}
 
     private class GameStart implements Runnable {
         @Override
         public void run() {
             readWriteLock.writeLock().lock();
            game.provideRunnable().run();
         }
     }
 
     private class SynchronizedControls implements Controls {
 
         private final Controls base;
 
         public SynchronizedControls(Controls base) {
             this.base = base;
         }
 
         @Override
         public Move waitForMove() throws InterruptedException {
             readWriteLock.writeLock().unlock();
 			drawLevel();
             fireEvents();
             //allow level read while waiting for player move
             Move ret = base.waitForMove();
             readWriteLock.writeLock().lock();
             return ret;
         }
 
         private void fireEvents() {
             for (final GameListener gl : listeners) {
 				SwingUtilities.invokeLater(new Runnable(){
 					@Override public void run() {
                 		gl.onEvent(gameEvent);
 					}
 				});
             }
         }
 
         private void drawLevel() {
 			Graphics2D g = null;
             try {
 				readWriteLock.writeLock().lock();
                 g = levelImage.createGraphics();
                 painter.paint(game, g, levelImage.getWidth(), levelImage.getHeight());
             } finally {
 				readWriteLock.writeLock().unlock();
                 if (g != null) {
                     g.dispose();
                 }
             }
         }
     }
 }
