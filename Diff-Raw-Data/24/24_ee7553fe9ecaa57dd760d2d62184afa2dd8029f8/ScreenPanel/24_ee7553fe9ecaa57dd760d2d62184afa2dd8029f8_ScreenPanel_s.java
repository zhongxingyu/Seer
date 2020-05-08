 package Screen;
 
 import java.util.*;
 import javax.swing.*;
 import javax.swing.Timer;
 import java.awt.*;
 import java.awt.event.*;
 import cells.Map;
 import pxlJam.*;
 import cells.*;
 
 @SuppressWarnings("serial")
 public class ScreenPanel extends JPanel implements ActionListener, KeyListener{
 
 	Graphics2D bufferGraphics;
 	Map theMap = new Map("levels/level3.txt");
 
 	
     public ScreenPanel() {
          setFocusable(true);
          requestFocusInWindow();
          MListener mlist = new MListener();
          addMouseListener (mlist);
          addMouseMotionListener (mlist);
          addKeyListener(this);
     }
    
     public void paintComponent(Graphics g){
    	System.out.println("paint component called");
         requestFocusInWindow();
         Graphics2D g2d = (Graphics2D) g;
         Image offscreen = createImage(getWidth(),getHeight()); 
         bufferGraphics = (Graphics2D)offscreen.getGraphics(); 
         bufferGraphics.setColor(Color.white); 
         bufferGraphics.fillRect(0,0,getWidth(),getHeight());
         theMap.draw(bufferGraphics);
         
         g2d.drawImage(offscreen,0,0,this);
     }
     
     public void drawIntroScreen (Graphics2D g2d){
      
         
     }
     
     public void drawBalance(Graphics2D bf){
         
     }
 
     /** Respond to the buttons */
     
     public void drawButtons (Graphics2D bf){
         
         
     }
 
 
     /** Create a new order and put it on the queue to be processed */
     public void generateOrder() {
       
     }
 
     public void addItem(String item) {
       
     }
 
     public void deliverOrder(Graphics2D bf) {
        
     }
     
     public void newLevel(Graphics2D bf){
        
         
     }
    
 
     public void drawOrders(Graphics bf) {
   
    
     }
     
     public void rotateOrders (Graphics bf){
       
     }
     
     public void startTimer(int time){
      
     
     }
     
     public void bonusTimer(int time){
 
     }
     
     public void run() {
       while (true){
     	  repaint();
       }
 
     }
     
     
     public void sleep(double millis){
         try { Thread.sleep((long)millis); }
         catch (InterruptedException e) { }
     }
     
     
     private class MListener implements MouseListener, MouseMotionListener {
         int xChange, yChange;
         
         public void mousePressed (MouseEvent event){
           double x = event.getX();
           double y = event.getY();
           System.out.println(x);
           System.out.println(y);
           int xPos = (int) (x/Cell.CELL_WIDTH);
           int yPos = (int) (y/Cell.CELL_HEIGHT);
           theMap.setCell(yPos,xPos,new Wall(xPos*Cell.CELL_WIDTH,yPos*Cell.CELL_HEIGHT));
         }
         
         public void mouseDragged (MouseEvent event){
         	double x = event.getX();
             double y = event.getY();
             System.out.println(x);
             System.out.println(y);
             int xPos = (int) (x/Cell.CELL_WIDTH);
             int yPos = (int) (y/Cell.CELL_HEIGHT);
             theMap.setCell(yPos,xPos,new Wall(xPos*Cell.CELL_WIDTH,yPos*Cell.CELL_HEIGHT));
         }
         public void mouseReleased (MouseEvent event) {
         	
         }
         public void mouseClicked (MouseEvent event) {
         	
         }
         public void mouseEntered (MouseEvent event) {
         	
         }
         public void mouseExited (MouseEvent event) {
         	
         }
         public void mouseMoved (MouseEvent event) {
         	
         }
     
 
     }
     
      /** Handle the key typed event from the text field. */
     public void keyTyped(KeyEvent e) {
         
     }
 
     /** Handle the key-pressed event from the text field. */
     public void keyPressed(KeyEvent e) {
         if (e.getKeyChar() == 'a' || e.getKeyChar() == 'A'){
         	theMap.getCharacter().move(-5);
         }
         else if (e.getKeyChar() == 'd' ||e.getKeyChar() == 'D'){
        	theMap.getCharacter().move(-5);
         }
         
         repaint();
     }
 
     /** Handle the key-released event from the text field. */
     public void keyReleased(KeyEvent e) {
     }
 
     
     public void actionPerformed (ActionEvent e){
     	run();
 
     }
 }
