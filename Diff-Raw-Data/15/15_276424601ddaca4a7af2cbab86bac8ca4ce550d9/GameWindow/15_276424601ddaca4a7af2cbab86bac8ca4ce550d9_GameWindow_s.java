 package main;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 public class GameWindow extends JFrame {
 
     private static final int FRAME_REDRAW_DELAY = 16;
     private static final Color BACKGROUND_COLOUR = Color.black;
 
     private Timer frameRedrawer;
     private BufferStrategy drawBuffer;
     
     private List<Displayable> drawList;
     private List<MouseRespondable> mouseActionList;
     private List<KeyboardRespondable> keyboardActionList;
     private List<LogicTicker> logicTickList;
     
     
     public GameWindow () {
         
         super("Protosaurus");
         this.drawList = new ArrayList<Displayable>();
         this.mouseActionList = new ArrayList<MouseRespondable>();
         this.keyboardActionList = new ArrayList<KeyboardRespondable>();
         this.logicTickList = new ArrayList<LogicTicker>();
 
     }
     
     public void init () {
         
         this.setSize(800, 600);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setVisible(true);
         
         initGraphics();
         initMouseListeners();
         initKeyboardListeners();
         
     }
     
     private void initGraphics() {
         
         this.frameRedrawer = new Timer(FRAME_REDRAW_DELAY, new ActionListener() {
             
             @Override
             public void actionPerformed(ActionEvent e) {
                 tickLogic(FRAME_REDRAW_DELAY);
                 repaint();
             }
             
         });
         
         this.createBufferStrategy(2);
         drawBuffer = this.getBufferStrategy();
         
         this.frameRedrawer.start();
         
     }
 
     private void initMouseListeners() {
         
         this.addMouseListener(new MouseListener() {
             
             @Override
             public void mouseReleased(MouseEvent e) {
                 
                 Point mousePosition = new Point(MouseInfo.getPointerInfo().getLocation());
                 SwingUtilities.convertPointFromScreen(mousePosition, GameWindow.this);
                 
                 for (int i = 0; i < mouseActionList.size(); i++) {
                     if(mouseActionList.get(i).inBounds(mousePosition.x, mousePosition.y) && mouseActionList.get(i).mouseUp(mousePosition.x, mousePosition.y, e.getButton()))
                         return;
                 }
                 
             }
             
             @Override
             public void mousePressed(MouseEvent e) {
                 
                 Point mousePosition = new Point(MouseInfo.getPointerInfo().getLocation());
                 SwingUtilities.convertPointFromScreen(mousePosition, GameWindow.this);
                 
                 for (int i = 0; i < mouseActionList.size(); i++) {
                     if(mouseActionList.get(i).inBounds(mousePosition.x, mousePosition.y) && mouseActionList.get(i).mouseDown(mousePosition.x, mousePosition.y, e.getButton()))
                         return;
                 }
                 
             }
             
             @Override
             public void mouseExited(MouseEvent e) {}
             
             @Override
             public void mouseEntered(MouseEvent e) {
                 
                 Point mousePosition = new Point(MouseInfo.getPointerInfo().getLocation());
                 SwingUtilities.convertPointFromScreen(mousePosition, GameWindow.this);
                 
                 for (int i = 0; i < mouseActionList.size(); i++) {
                     
                     boolean inBounds = mouseActionList.get(i).inBounds(mousePosition.x, mousePosition.y);
                     
                     if(inBounds && mouseActionList.get(i).mouseIn(mousePosition.x, mousePosition.y))
                         return;
                     else if (!inBounds)
                         mouseActionList.get(i).mouseOut();
                 }
                 
             }
             
             @Override
             public void mouseClicked(MouseEvent e) {}
             
         });
         
     }
     
     private void initKeyboardListeners() {
         
         this.addKeyListener(new KeyListener() {
             
             @Override
             public void keyTyped(KeyEvent e) {}
             
             @Override
             public void keyReleased(KeyEvent e) {
                 
                 for (int i = 0; i < keyboardActionList.size(); i++) {
                     keyboardActionList.get(i).keyUp(e.getKeyCode());
                 }
                 
             }
             
             @Override
             public void keyPressed(KeyEvent e) {
                 
                 for (int i = 0; i < keyboardActionList.size(); i++) {
                     keyboardActionList.get(i).keyDown(e.getKeyCode());
                 }
 
             }
             
         });
         
     }
 
     private void tickLogic (int delta) {
         
         for (int i = 0; i < this.logicTickList.size(); i++) {
             this.logicTickList.get(i).tick(delta);
         }
         
     }
     
     @Override
     public void paint(Graphics g) {
 
         Graphics2D dBuffer = (Graphics2D) drawBuffer.getDrawGraphics();
 
         dBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
         dBuffer.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 
         dBuffer.setColor(BACKGROUND_COLOUR);
         dBuffer.fillRect(0, 0, this.getWidth(), this.getHeight());
         
         for (int i = 0; i < drawList.size(); i++) {
             drawList.get(i).draw(dBuffer);
         }
         
         drawBuffer.show();
         
         g.dispose();
         dBuffer.dispose();
         
     }
     
     public void addDisplayable (Displayable displayable) {
         this.drawList.add(displayable);
     }
     
     public void removeDisplayable (Displayable displayable) {
         this.drawList.remove(displayable);
     }
     
     public void addMouseActionListener (MouseRespondable mouseRespondable) {
         this.mouseActionList.add(mouseRespondable);
     }
     
     public void removeMouseActionListener (MouseRespondable mouseRespondable) {
         this.mouseActionList.remove(mouseRespondable);
     }
 
     public void addKeyboardActionListener (KeyboardRespondable keyboardRespondable) {
         this.keyboardActionList.add(keyboardRespondable);
     }
     
     public void removeKeyboardActionListener (KeyboardRespondable keyboardRespondable) {
         this.keyboardActionList.remove(keyboardRespondable);
     }
     
     public void addLogicTicker (LogicTicker logicTicker) {
         this.logicTickList.add(logicTicker);
     }
     
     public void removeLogicTicker (LogicTicker logicTicker) {
         this.logicTickList.remove(logicTicker);
     }
     
 }
