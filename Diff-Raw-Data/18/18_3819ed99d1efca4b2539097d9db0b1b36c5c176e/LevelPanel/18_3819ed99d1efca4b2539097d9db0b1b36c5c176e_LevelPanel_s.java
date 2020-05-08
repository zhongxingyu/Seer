 package org.sankozi.rogueland.gui;
 
 import com.google.inject.Inject;
 import java.awt.Cursor;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import javax.swing.JComponent;
 import org.apache.log4j.Logger;
 import org.sankozi.rogueland.control.Game;
 import org.sankozi.rogueland.control.LogListener;
 import org.sankozi.rogueland.model.Controls;
 import org.sankozi.rogueland.model.Direction;
 import org.sankozi.rogueland.model.Level;
 import org.sankozi.rogueland.model.Move;
 
 /**
  * Panel that renders current level state
  * @author sankozi
  */
 public class LevelPanel extends JComponent{
     private final static Logger LOG = Logger.getLogger(LevelPanel.class);
 
     GameSupport gameSupport;
     Rectangle levelSize = new Rectangle(0, 0, Level.WIDTH, Level.HEIGHT);
 
     Rectangle playerLocation = new Rectangle(50, 50, 10, 10);
     Direction cursorDirection;
 
     GuiControls gc = new GuiControls();
 
     ComponentListener componentListener = new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent e) { gameSupport.resize(getSize()); }
        @Override public void componentShown  (ComponentEvent e) { gameSupport.resize(getSize()); }
     };
 
     {
         this.setFocusable(true);
         this.addKeyListener(gc);
         this.addMouseMotionListener(new MoveCursor());
         this.addMouseListener(gc);
         this.addComponentListener(componentListener);
     }
 
     public KeyListener getKeyListener(){
         return gc;
     }
 
     @Override
     public void paint(Graphics g) {
         g.drawImage(gameSupport.getLevelImage(), 0, 0, this);
     }
 
     @Inject
     public void setGame(Game game){
         LOG.info("set game");
         gameSupport = new GameSupport(game, gc);
         gameSupport.addListener(new GameListener(){
             @Override public void onEvent(GameEvent event) { refreshGameState(); }
         });
         gameSupport.gameStart();
     }
 
     public void addLogListener(LogListener logListener){
         gameSupport.addLogListener(logListener);
     }
 
     private void setDirectionCursor(Direction dir){
         cursorDirection = dir;
         switch(dir){
             case NW:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                 break;
             case N:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                 break;
             case NE:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                 break;
             case W:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.W_RESIZE_CURSOR));
                 break;
             case C:
                 LevelPanel.this.setCursor(Cursor.getDefaultCursor());
                 break;
             case E:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
                 break;
             case SW:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                 break;
             case S:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
                 break;
             case SE:
                 LevelPanel.this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                 break;
         }
     }
 
     /* must be called in game thread */
     private void refreshGameState(){
         LOG.info("refreshGameState");
         repaint();//submits repaint event to EDT
         playerLocation = gameSupport.getPlayerLocation();
     }
 
     private class MoveCursor implements MouseMotionListener {
         final double first = Math.tan(Math.PI / 8.0);
         final double second = Math.tan(Math.PI / 2.0  - Math.PI / 8.0);
 
 //        {
 //            LOG.info("first = " + first + " second = " + second);
 //        }
 
         @Override
         public void mouseDragged(MouseEvent e) {
         }
 
         @Override
         public void mouseMoved(MouseEvent e) {
             int x = e.getX();
             int y = e.getY();
             int px = playerLocation.x + playerLocation.width / 2;
             int py = playerLocation.y + playerLocation.height / 2;
             double dy = Math.abs(y-py);
             double dx = Math.abs(x-px);
 //            LOG.info("x = " + x + " y = " + y + " px = " + px + " py = " + py + " dx = " + dx + " dy = " + dy);
             if(dx + dy < 20){//cursor close to the player
                 setDirectionCursor(Direction.C);
             } else if(x < px){
                 if(y < py) { //NW region
                     double tan = dx / dy;
                     if(tan < first){
                         setDirectionCursor(Direction.N);
                     } else if (tan < second) {
                         setDirectionCursor(Direction.NW);
                     } else {
                         setDirectionCursor(Direction.W);
                     }
                 } else { //SW region
                     double tan = dy / dx;
                     if(tan < first){
                         setDirectionCursor(Direction.W);
                     } else if (tan < second) {
                         setDirectionCursor(Direction.SW);
                     } else {
                         setDirectionCursor(Direction.S);
                     }
                 }
             } else {
                 if(y < py) { //NE region
                     double tan = dx / dy;
                     if(tan < first){
                         setDirectionCursor(Direction.N);
                     } else if (tan < second) {
                         setDirectionCursor(Direction.NE);
                     } else {
                         setDirectionCursor(Direction.E);
                     }
                 } else { //SE region
                     double tan = dy / dx;
                     if(tan < first){
                         setDirectionCursor(Direction.E);
                     } else if (tan < second) {
                         setDirectionCursor(Direction.SE);
                     } else {
                         setDirectionCursor(Direction.S);
                     }
                 }
             }
         }
     }
 
     private class GuiControls implements Controls, KeyListener, MouseListener {
 
         BlockingQueue<Move> keysPressed = new ArrayBlockingQueue<Move>(5);
 
         @Override
         public Move waitForMove() throws InterruptedException {
             LOG.info("waitForMove");
             Move move = keysPressed.take();
             return move;
         }
 
         @Override
         public void keyTyped(KeyEvent e) {
         }
 
         public Move fromKeyCode(int code){
             switch(code){
                 case KeyEvent.VK_UP:
                     return Move.Go.NORTH;
                 case KeyEvent.VK_LEFT:
                     return Move.Go.WEST;
                 case KeyEvent.VK_DOWN:
                     return Move.Go.SOUTH;
                 case KeyEvent.VK_RIGHT:
                     return Move.Go.EAST;
                 default:
                     return null;
             }
         }
 
 
         @Override
         public void keyPressed(KeyEvent e) {
             try {
                 Move move = fromKeyCode(e.getKeyCode());
                 if(move != null){
                     keysPressed.offer(move, 1, TimeUnit.SECONDS);
                 }
             } catch (InterruptedException ex) {
                 LOG.error(ex.getMessage(), ex);
             }
         }
 
         @Override
         public void keyReleased(KeyEvent e) {
         }
 
         @Override
         public void mouseClicked(MouseEvent e) {
             try {
                 keysPressed.offer(cursorDirection.toSingleMove(), 1, TimeUnit.SECONDS);
             } catch (InterruptedException ex) {
                 LOG.error(ex.getMessage(), ex);
             }
         }
 
         @Override
         public void mousePressed(MouseEvent e) {
         }
 
         @Override
         public void mouseReleased(MouseEvent e) {
         }
 
         @Override
         public void mouseEntered(MouseEvent e) {
         }
 
         @Override
         public void mouseExited(MouseEvent e) {
         }
     }
 }
