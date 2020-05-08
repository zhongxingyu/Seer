 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package skrivfx;
 
 import javafx.event.EventHandler;
 import javafx.scene.input.MouseEvent;
 
 /**
  *
  * @author Tyler
  */
 public class PageEvent implements EventHandler<MouseEvent>{
 
     private static final double howFar = .9; // Wow I suck with var names...
     private views.ViewSingleton view;
     private models.ModelSingleton model;
     private classes.ClearThread t;
     private javafx.scene.image.Image image;
     private boolean hasReachedEnd = false;
     
     public PageEvent(){
         view = views.ViewSingleton.getInstance();
         model = models.ModelSingleton.getInstance();
         t = classes.ClearThread.getInstance();
     }
     
     @Override
     public void handle(MouseEvent e) {
         if(model.getCurrentIndex() == -1){ System.out.println("Cannot write!!"); }
         else if(e.getEventType().equals(MouseEvent.MOUSE_PRESSED)){ mousePressedEvent(e); }
         else if(e.getEventType().equals(MouseEvent.MOUSE_DRAGGED)){ mouseDraggedEvent(e); }
         else{ mouseReleased(e); }
     }
     
     public void mousePressedEvent(MouseEvent e){
         view.startLine(e.getX(), e.getY());
         t.reset();
         if(model.isNewWord(e.getX(), e.getY()) && image != null){
             classes.Word w = new classes.Word(image);
             model.addWord(w);
             view.drawWord(w);
         }
     }
     
     public void mouseDraggedEvent(MouseEvent e){
         if(e.getX() > view.getWritingCanvas().widthProperty().doubleValue()*howFar){ hasReachedEnd = true; }
         model.addPoint(e.getX(), e.getY());
         view.updateLine(e.getX(), e.getY());
     }
     
     public void mouseReleased(MouseEvent e){
         image = view.getSnapshot(model.left(), model.top(), model.getWidth(), model.getHeight());
         if(hasReachedEnd){
            t.restart();
             hasReachedEnd = false;
         }
     }
 }
