 package view;
 
 import javafx.beans.property.ReadOnlyDoubleProperty;
 import javafx.event.EventHandler;
 import javafx.scene.SnapshotParameters;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 
 
 public class ViewImage {
     
     private Canvas writingCanvas;
     private Canvas pageCanvas;
     private GraphicsContext writingGC;
     private GraphicsContext pageGC;
     // Maybe?
     //private Hashmap<WordImage, Point2D> words;
     private double cursorX, cursorY;
     
     public ViewImage(){
         cursorX = 20;
         cursorY = 20;
     }
     
     public void makeWritingCanvas(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height){
         writingCanvas = new Canvas();
         writingCanvas.widthProperty().bind(width);
         writingCanvas.heightProperty().bind(height);
         
         writingGC = writingCanvas.getGraphicsContext2D();
         writingGC.setFill(Color.BLACK);
         writingGC.setLineWidth(2);
     }
     
     public void makePageCanvas(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height){
         pageCanvas = new Canvas();
         pageCanvas.widthProperty().bind(width);
         pageCanvas.heightProperty().bind(height);
         
         pageGC = pageCanvas.getGraphicsContext2D();       
         pageGC.setFill(Color.BLACK);
         pageGC.setLineWidth(1);
     }
     
     public void addHandlers(EventHandler<MouseEvent> e){
         writingCanvas.setOnMousePressed(e);
         writingCanvas.setOnMouseDragged(e);
         writingCanvas.setOnMouseReleased(e);
     }
     
     public javafx.scene.image.Image getSnapshot(int x, int y, int width, int height){
         SnapshotParameters sp = new SnapshotParameters();
         sp.setViewport(new javafx.geometry.Rectangle2D(x, y, width, height));
         return writingCanvas.snapshot(sp, null);
     }
     
     public void drawWord(classes.WordImage w){
         if(pageCanvas.getWidth()-cursorX < w.getWidth()){
            cursorX = 20;
             cursorY = cursorY + w.getHeight() + 20;
         } 
         pageGC.drawImage(w.getImage(), cursorX, cursorY, .65*w.getWidth(), .65*w.getHeight());
         cursorX = cursorX + 20 + w.getWidth();
     }
     
     public Canvas getWritingCanvas(){
         return writingCanvas;
     }
     
     public Canvas getPageCanvas(){
         return pageCanvas;
     }
     
     public void startLine(double x, double y){
         writingGC.beginPath();
         writingGC.moveTo(x, y);
     }
     
     public void updateLine(double x, double y){
         writingGC.lineTo(x, y);
         writingGC.stroke();
     }
     
     public GraphicsContext getWritingGraphics(){
         return writingGC;
     }
 }
 
 
 
