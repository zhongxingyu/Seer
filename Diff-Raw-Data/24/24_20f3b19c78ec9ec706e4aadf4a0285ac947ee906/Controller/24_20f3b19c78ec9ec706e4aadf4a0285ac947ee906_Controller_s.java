 package test.writing.box;
 
 import classes.View;
 import classes.SmartPoint;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import javax.swing.SwingUtilities;
 
 
 public class Controller implements ActionListener, MouseListener, MouseMotionListener{
     
     private Model model;
     private View view;
     
     public Controller(){
         model = new Model();
         view = new View();
         view.setVisible(true);
     }   
     
     // Implementation for Button Listeners
     @Override
     public void actionPerformed(ActionEvent e) {
         System.out.println(String.format("%s button pressed", e.getActionCommand()));
     }
     
     // Implementation for Mouse Listeners
     @Override
     public void mouseClicked(MouseEvent e) {
         // NO need for functionality
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         // Add Point and see if it's a new Word
         if(e.isShiftDown()){
             System.out.println("Strikethrough attempted.");
         } else if(e.isAltDown()){
             System.out.println("Spread attempted");
         } else{
            // Add poin to "live" Word in Model
             model.addPoint(new SmartPoint(e.getPoint(), Boolean.FALSE));
             // update View
             view.updateUI(model.getViewData());
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         // Add "end" Point to denote the pen has been lifted
         model.addPointDirect(new SmartPoint(e.getPoint(), Boolean.TRUE));
         // Update View
         view.updateUI(model.getViewData());
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         // NO need for functionality
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         // NO need for functionality
     }
 
     // Implementation for Mouse Motion Listener
     @Override
     public void mouseDragged(MouseEvent e) {
         // Add dragged point
         // Dragged point doesn't check for type of input
         model.addPointDirect(new SmartPoint(e.getPoint(), Boolean.FALSE));
         // Update View
         view.updateUI(model.getViewData());
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
         // NO need for functionality
     }
     
     // Add button and mouse listeners to the View
     private void addListeners(){
         view.addButtonListeners(this);
         view.addMouseListeners(this, this);
     }
     
     
     // Launch the application
     public static void launch(){
         SwingUtilities.invokeLater(new Runnable(){
             @Override
             public void run(){
                 Controller c = new Controller();
                 c.addListeners();
             }
         });
     }
     
     // MAIN
     public static void main(String[] args){
         launch();
     }
 }
