 package skrivfx;
 
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.shape.Path;
 
 
 public class PageEvent implements EventHandler<Event>{
 
     
     private views.ViewSingleton view;
     private models.ModelSingleton model;
     private TestController tc;
     private Path path;
     private double x, y;
     private double beginX, beginY, endX, endY;
     public PageEvent(TestController t){
         tc = t;
         view = views.ViewSingleton.getInstance();
         model = models.ModelSingleton.getInstance();
     }
     
     @Override
     public void handle(Event e) {        
          if (e.getEventType() == MouseEvent.MOUSE_PRESSED){
              mousePressed((MouseEvent)e);
          }
          if (e.getEventType() == MouseEvent.MOUSE_DRAGGED){
              mouseDragged((MouseEvent)e);
          }
          if (e.getEventType() == MouseEvent.MOUSE_RELEASED){
              mouseReleased((MouseEvent)e);
          } 
     }
     
     // We need clever ways of identifying what the user is doing
     public void mousePressed(MouseEvent e){
         if(e.isShiftDown()){
             beginX = e.getX();
             beginY = e.getY();
             //System.out.println(x + " " + y);
         }
     }
     
     // No real need for this
     private void mouseDragged(MouseEvent e) {
         if(e.isShiftDown()){          
             
         }
     }
 
     private void mouseReleased(MouseEvent e) {
         if(e.isShiftDown()){
             endX = e.getX();
             endY = e.getY();
             
             java.util.List<classes.Word> words = model.getWords();
             for(classes.Word w : words){
                 // Check x-coords
                if(endX < w.getX()+w.getWidth() && w.getX()> beginX && Math.abs(w.getY()-endY) < 50){
 //                if(w.getX() < endX && w.getX()+w.getWidth() > beginX && Math.abs(w.getY()-endY) < 50 ){
                     System.out.println("Word should be removed");
 //                    model.deleteWord(w);
                     view.eraseWord(w);
                    ;
                 }
             }
             
         }
         else if (tc.getHighlightButton().isSelected()){
             endX = e.getX();
             endY = e.getY();
             
             java.util.List<classes.Word> words = model.getWords();
             for(classes.Word w : words){
                 // Check x-coords
                if(endX < w.getX()+w.getWidth() && w.getX()> beginX && Math.abs(w.getY()-endY) < 50){
 //                if(w.getX() < endX && w.getX()+w.getWidth() > beginX && Math.abs(w.getY()-endY) < 50 ){
                     System.out.println("Word should be highlighted");
 //                    model.deleteWord(w);
                     view.highlightWord(w);
                    ;
                 }
             }
         }
         //System.out.println("inside mouse released");
         //do word function, x and y in here are initial mouse places
         //use getX anD getY to search the words for which things it passes through
         //erase what it passes through. 
     }
     
 }
