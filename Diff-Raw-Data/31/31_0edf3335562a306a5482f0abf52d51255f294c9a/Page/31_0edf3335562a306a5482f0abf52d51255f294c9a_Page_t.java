 package views;
 
 import classes.Word;
 import javafx.beans.property.ReadOnlyDoubleProperty;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.control.Tab;
 import javafx.scene.effect.BlendMode;
 import javafx.scene.layout.Pane;
 import javafx.scene.paint.Color;
 import javafx.scene.shape.Rectangle;
 import skrivfx.TestController;
 
 public class Page extends Tab {
 
     private static double scale = .65;
     private final double mmScale = .1727;
     private Canvas mmCanvas; //Minimap
     private GraphicsContext mmGC; // Minimap
     private Canvas canvas;
     private GraphicsContext gc;
     private double curX, curY; // Cursor
     private boolean isFirst = true; // For scaling
     private TestController tc;
     private double viewportTranslatePostion = 0.0;
     private Pane pane;
 
 
     public Page(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height, TestController tc) {
         this(width, height, "Untitled " + tc.newPageUpdateCount(), tc);
     }
 
     public Page(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height, String title, TestController tc) {
         super();
         this.setText(title);
         makeCanvas(width, height);
         //this.setContent(canvas);
         curX = 20;
         curY = 10;
         this.tc = tc;
         
         //added pane
         pane = new Pane();
         this.setContent(pane);
         pane.setPrefSize(width.doubleValue(), height.doubleValue());
         pane.getChildren().add(canvas);
     }
 
     private void makeCanvas(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height) {
         canvas = new Canvas();
 //        canvas.widthProperty().bind(width);
 //        canvas.heightProperty().bind(height);
         canvas.setWidth(width.doubleValue());
         canvas.setHeight(height.doubleValue());
         
         // 106 X 717
         mmCanvas = new Canvas();
         mmCanvas.setWidth(106);
         mmCanvas.setHeight(717);
         
 
         gc = canvas.getGraphicsContext2D();
         mmGC = mmCanvas.getGraphicsContext2D();
     }
     
     public void addPageHandlers(EventHandler<Event> e){
         canvas.setOnMouseClicked(e);
         canvas.setOnMouseDragged(e);
         canvas.setOnMouseReleased(e);
         canvas.setOnKeyPressed(e);
     }
     
     public Canvas getCanvas(){
         return canvas;
     }
     
     public Canvas getMiniMap(){
         return mmCanvas;
     }
 
     public void drawWord(Word w) {
         if (canvas.getWidth() - curX < w.getWidth()) {
             curX = 20;
             curY = curY + 50;
             tc.moveViewportDown();
         }
         // Change stuff around, maybe keep it within bounds?
         if(isFirst && w.getHeight() > 1 || scale*w.getHeight() > 40){ 
             System.out.println("in here");
             scale = 40/w.getHeight(); 
             isFirst = false;
             w.setLocation(curX, curY);
             gc.drawImage(w.getImage(), curX, curY, scale * w.getWidth(), 40);
             mmGC.drawImage(w.getImage(), mmScale*curX, mmScale*curY, mmScale*w.getWidth(), mmScale*40);
         }
         else{
             w.setLocation(curX, curY);
             gc.drawImage(w.getImage(), curX, curY, scale * w.getWidth(), scale * w.getHeight());
             //gc.drawImage(w.getImage(), curX, curY + 50-w.getHeight(), scale * w.getWidth(), scale * w.getHeight());
             mmGC.drawImage(w.getImage(), mmScale*curX, mmScale*curY, mmScale*w.getWidth(), mmScale*w.getHeight());
         }
         
         curX = curX + 40 + scale * w.getWidth();
         w.setWidth(scale*w.getWidth());
     }
     
     public void eraseWord(Word w){
//        javafx.scene.paint.Paint temp = gc.getFill();
//        gc.setFill(Color.WHITE);
//        // Just a little extra wiggle room
//        gc.fillRect(w.getX()-1, w.getY()-10, w.getWidth()+2, 50);
//        gc.setFill(temp);
        Rectangle r = new Rectangle(w.getX()-1, w.getY()-10, w.getWidth()+2, 50);
        r.setFill(Color.WHITE);
        //r.setOpacity(1);
     
        pane.getChildren().add(r);
     }
     
     public void carriageReturn(){
         curX = 20;
         curY = curY + 50;
     }
     
     public void setViewportTranslatePosition(double y){
         this.viewportTranslatePostion = y;
     }
     
     public double getViewportTranslatePosition(){
         return this.viewportTranslatePostion;
     }
 
     void highlightWord(Word w) {
         Rectangle r = new Rectangle(w.getX()-1, w.getY()-10, w.getWidth()+2, 50);
         r.setFill(Color.YELLOW);
         r.setBlendMode(BlendMode.SRC_OVER);
         r.setOpacity(0.2);
         
         
         //r.setOpacity(0.3);
         pane.getChildren().add(r);
         //javafx.scene.paint.Paint temp = gc.getFill();
         //gc.setFill(Color.YELLOW);
         // Just a little extra wiggle room
         //gc.fillRect(w.getX()-1, w.getY()-10, w.getWidth()+2, 50);
         //gc.setFill(temp);
     }
     
     
 }
