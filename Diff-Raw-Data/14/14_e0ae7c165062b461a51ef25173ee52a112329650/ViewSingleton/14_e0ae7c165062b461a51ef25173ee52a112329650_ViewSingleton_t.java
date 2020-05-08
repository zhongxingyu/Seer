 package views;
 
 import classes.Word;
 import javafx.beans.property.ReadOnlyDoubleProperty;
 import javafx.event.EventHandler;
 import javafx.scene.SnapshotParameters;
 import javafx.scene.image.Image;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import skrivfx.TestController;
 
 
 public class ViewSingleton {
     
     private java.util.List<views.Page> tabs;
     private javafx.scene.canvas.Canvas writingCanvas; // Writing box
     private javafx.scene.canvas.GraphicsContext writingGC;
         private javafx.scene.canvas.Canvas minimapCanvas;
     private javafx.scene.canvas.GraphicsContext minimapGC; 
     private int currentIndex; // Current tab
     private static ViewSingleton instance;
     
     private ViewSingleton(){
         tabs = new java.util.ArrayList<>();
     }
     
     public static ViewSingleton getInstance(){
         if(instance == null){
             instance = new ViewSingleton();
         }
         return instance;
     }
    
     public void makeWritingCanvas(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height){
         writingCanvas = new javafx.scene.canvas.Canvas();
         writingCanvas.widthProperty().bind(width);
         writingCanvas.heightProperty().bind(height);
         writingGC = writingCanvas.getGraphicsContext2D();
     }
     
     public void addHandlers(EventHandler<MouseEvent> e){
         writingCanvas.setOnMousePressed(e);
         writingCanvas.setOnMouseDragged(e);
         writingCanvas.setOnMouseReleased(e);
     }
     
     // Must return a page so that it can be added to the TabPane
     public Page addTab(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height, TestController tc){
         tabs.add(new Page(width, height, tc));
         currentIndex = tabs.size()-1;
         return tabs.get(currentIndex);
     }
     
    public void carriageReturn(){
        tabs.get(currentIndex).carriageReturn();
    }
    
     public void setLineWidth(double d){
         writingGC.setLineWidth(d);
     }
     
     public void setStrokeColor(Color c){
         writingGC.setStroke(c);
     }
     
     public void setCurrentIndex(int i){
         currentIndex = i;
     }
     
     public void drawWord(Word w){
         tabs.get(currentIndex).drawWord(w);
     }
     
     public Image getSnapshot(int x, int y, int width, int height){
         SnapshotParameters sp = new SnapshotParameters();
         sp.setViewport(new javafx.geometry.Rectangle2D(x, y, width, height));
         return writingCanvas.snapshot(sp, null);
     }
     
     public void startLine(double x, double y){
         writingGC.beginPath();
         writingGC.moveTo(x, y);
     }
     
     public void updateLine(double x, double y){
         writingGC.lineTo(x, y);
         writingGC.stroke();
     }
     
     public javafx.scene.canvas.Canvas getWritingCanvas(){
         return writingCanvas;
     }
     
     public void clearWritingCanvas(){
         writingGC.clearRect(0, 0, writingCanvas.getWidth(), writingCanvas.getHeight());
     }
         public void makeMinimapCanvas(ReadOnlyDoubleProperty width, ReadOnlyDoubleProperty height) {
         minimapCanvas = new javafx.scene.canvas.Canvas();
         minimapCanvas.widthProperty().bind(width);
         minimapCanvas.heightProperty().bind(height);
         minimapGC = writingCanvas.getGraphicsContext2D();
     }
     public void updateMinimapCanvas()
     {
         
     }
 
 }
