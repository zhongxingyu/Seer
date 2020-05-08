 
 package vooga.scroller.level_editor;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import vooga.scroller.viewUtil.IView;
 import vooga.scroller.viewUtil.Renderable;
 import vooga.scroller.viewUtil.WindowComponent;
 
 public class LEGridView extends WindowComponent{
     /**
      * 
      */
     private static final long serialVersionUID = 8266835201464623542L;
     private Dimension mySize;
     private Renderable myGrid;
 
     public LEGridView (IView parent, double d, double e) {
         // TODO Auto-generated constructor stub
         super(parent, d, e);
     }
 
     @Override
     protected void initializeVariables () {
         // TODO Auto-generated method stub
 //        mySize = ViewConstants.DEFAULT_ROOM_SIZE;
 //        this.setPreferredSize(mySize);
 //        this.setMinimumSize(mySize);
         
     }
 
     @Override
     protected void addComponents () {
         this.addMouseListener(new GridPositionListener());
     }
 
     @Override
     public void render (Renderable r) {
         myGrid = r;
         repaint();
     }
     
     /**
      * Paint the contents of the canvas.
      * 
      * Never called by you directly, instead called by Java runtime
      * when area of screen covered by this container needs to be
      * displayed (i.e., creation, uncovering, change in status)
      * 
      * @param pen used to paint shape on the screen
      */
     @Override
     public void paintComponent (Graphics pen) {
         pen.setColor(Color.WHITE);
         pen.fillRect(0, 0, getSize().width, getSize().height);
         if (myGrid != null) {
             myGrid.paint((Graphics2D) pen);
         }
     }
     
     private void createSprite (Point loc) {
         // TODO Auto-generated method stub
         process(loc);
     }
     
     private class GridPositionListener implements MouseListener {
 
 
         @Override
         public void mouseClicked (MouseEvent e) {
             // TODO Auto-generated method stub
            createSprite(e.getLocationOnScreen());
             System.out.println(e.getLocationOnScreen());
         }
 
         @Override
         public void mousePressed (MouseEvent e) {
             // TODO Auto-generated method stub
             
         }
 
         @Override
         public void mouseReleased (MouseEvent e) {
             // TODO Auto-generated method stub
             
         }
 
         @Override
         public void mouseEntered (MouseEvent e) {
             // TODO Auto-generated method stub
             
         }
 
         @Override
         public void mouseExited (MouseEvent e) {
             // TODO Auto-generated method stub
             
         }
         
     }
 
 }
