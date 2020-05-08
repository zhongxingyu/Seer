 /*
  * 
  */
 package mandelbrotset;
 
 import javafx.application.Application;
 import javafx.event.Event;
 import javafx.event.EventHandler;
 import javafx.stage.Stage;
 
 import Model.Model;
 import View.View;
 import javafx.beans.property.DoubleProperty;
 import javafx.beans.property.SimpleDoubleProperty;
 import javafx.concurrent.Service;
 import javafx.geometry.Point2D;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.input.DragEvent;
 import javafx.scene.input.MouseDragEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import javafx.scene.paint.Paint;
 
 /**
  *
  * @author Brooke Smith brooke@tintuna.org
  */
 public class MandelbrotSet extends Application {
 
     Model model;
     View view;
 
     public MandelbrotSet() {
     }
 
     @Override
     public void start(final Stage primaryStage) {
         model = new Model();
         view = new View(model);
 
         primaryStage.setTitle("Mandelbrot");
         primaryStage.setWidth(600);
         primaryStage.setHeight(800);
         hookupEvents();
         primaryStage.setScene(view.scene);
         primaryStage.setResizable(true);
         primaryStage.show();
         runUpdateTaskAndHandleNewCanvas();
     }
 
     private void hookupEvents() {
         view.updateButton.setOnAction(new EventHandler() {
             double newX;
             double newY;
             double newFactor;
             double newMSetHeight;
             double newMSetWidth;
             int newIters;
 
             @Override
             public void handle(Event t) {
                 if (changedValues()) {
                     setChangedValues();
                     runUpdateTaskAndHandleNewCanvas();
                 }
             }
 
             private boolean changedValues() {
                 newX = Double.parseDouble(view.textx.getText());
                 newY = Double.parseDouble(view.texty.getText());
                 newFactor = Double.parseDouble(view.textfactor.getText());
                 newMSetHeight = Double.parseDouble(view.textMSetHeight.getText());
                 newMSetWidth = Double.parseDouble(view.textMSetWidth.getText());
                 newIters = Integer.parseInt(view.textIters.getText());
 
                 System.out.format("model.changedValues - x:%f, y:%f, mSetWidth:%f, MSetHeight:%f, factor:%f, iters:%d\n",
                         newX, newY, newMSetWidth, newMSetHeight, newFactor, newIters);
                 return (newX != model.getX() || newY != model.getY() || newFactor != model.getFactor() || newIters != model.getIters()
                         || newMSetHeight != model.getMSet_height() || newMSetWidth != model.getMSet_width());
 
             }
 
             private void setChangedValues() {
                 model.setX(newX);
                 model.setY(newY);
                 model.setFactor(newFactor);
                 model.setIters(newIters);
                 model.setMSet_height(newMSetHeight);
                 model.setMSet_width(newMSetWidth);
                 //TODO can gt rid of this
                 model.sceneChanged = true;
             }
         });
 
         view.transparentcanvas.setOnDragDetected(
                 new EventHandler() {
             // Activate the Drag Event
             @Override
             public void handle(Event t) {
                 System.out.println("DragDetected ");
                 view.transparentcanvas.startFullDrag();
                 t.consume();
             }
         });
 
         view.transparentcanvas.setOnMouseDragEntered(new EventHandler<MouseDragEvent>() {
            // Where the drag starts
             @Override
             public void handle(MouseDragEvent t) {
                 System.out.println("OnMouseDragEntered - event: " + t.getEventType() + ", x:" + t.getX() + ", y:" + t.getY());
                model.mouseDragFirstPoint = new Point2D(t.getX(), t.getY());
             }
         });
        view.transparentcanvas.setOnMouseDragged(new EventHandler<MouseEvent>(){

             @Override
             public void handle(MouseEvent t) {
              System.out.println("OnMouseDragged x:" + t.getX() + ", y:" + t.getY());
//                view.transparentcanvas.getGraphicsContext2D().setFill((Paint) Color.RED);
//                view.transparentcanvas.getGraphicsContext2D().fillRect(model.mouseDragFirstPoint.getX(), model.mouseDragFirstPoint.getX(),
//                        t.getX(), t.getY());
             }
            
         });
         view.transparentcanvas.setOnMouseDragReleased(new EventHandler<MouseDragEvent>() {
             // Where the drag ends
             @Override
             public void handle(MouseDragEvent t) {
                 System.out.println("OnMouseDragReleased - event: " + t.getEventType() + ", x:" + t.getX() + ", y:" + t.getY());
                 // Calculate the new x and y points:
                 // Previous Mandelbrot Set's width and height / canvas' width and height * (left/upper-most) mouse drag x/y + last x/y
                 model.setX(((model.getMSet_width() / model.getStageWIDTH()) * model.mouseDragFirstPoint.getX()) + model.getX());
                 model.setX(((model.getMSet_height() / model.getStageHEIGHT()) * model.mouseDragFirstPoint.getY()) + model.getY());
                 // Calculate the new width of the Mandelbrot Set to draw.  Its the width / height selected over the
                 // canvas' width and height proportion of the previous Mandelbrot Set's width and height
                 model.setMSet_height(model.getMSet_height() * (Math.abs(t.getY() - model.mouseDragFirstPoint.getY()) / (double) model.getStageHEIGHT()));
                 model.setMSet_width(model.getMSet_width() * (Math.abs(t.getX() - model.mouseDragFirstPoint.getX()) / (double) model.getStageWIDTH()));
                 model.sceneChanged = true;
                 runUpdateTaskAndHandleNewCanvas();
             }
         });
     }
 
     private void runUpdateTaskAndHandleNewCanvas() {
         final Service service = (Service) model.worker;
         service.setOnSucceeded(new EventHandler() {
             @Override
             public void handle(Event t) {
                 Canvas newCanvas = (Canvas) service.getValue();
                 System.out.println("Service (updateButton) succeeded - value is:" + newCanvas + ", and from worker its:" + model.worker.getValue());
                 view.replaceCanvas(newCanvas);
             }
         });
         service.restart();
     }
 
     public static void main(String[] args) {
         launch(args);
     }
 }
