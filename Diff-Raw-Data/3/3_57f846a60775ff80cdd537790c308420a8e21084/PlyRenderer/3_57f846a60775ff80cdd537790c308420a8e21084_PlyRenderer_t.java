 package org.plyrenderer.client;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.i18n.client.NumberFormat;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.*;
 
 import java.util.logging.Logger;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>
  */
 public class PlyRenderer implements EntryPoint {
 
     private static final int canvasHeight = 500;
     private static final int canvasWidth = 500;
     private Renderer renderer;
     private PointCloud cloud;
 
     private PlyRendererServiceAsync service;
 
     private final Logger logger = Logger.getLogger("PlyRenderer");
     private Label percentage;
 
     private RadioButton b1, b2, b3;
     private Label fps;
 
     private Canvas canvas;
 
     private static NumberFormat format = NumberFormat.getFormat(".00");
 
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
 
         service = PlyRendererService.App.getInstance();
 
         canvas = Canvas.createIfSupported();
 
         if (canvas == null) {
             RootPanel.get("canvas").add(new Label("Sorry, your browser doesn't support the HTML5 Canvas element"));
             return;
         }
 
 
         //canvas.setStyleName("mainCanvas");
         canvas.setWidth(canvasWidth + "px");
         canvas.setCoordinateSpaceWidth(canvasWidth);
 
         canvas.setHeight(canvasHeight + "px");
         canvas.setCoordinateSpaceHeight(canvasHeight);
 
         canvas.setVisible(false);
         RootPanel.get("canvas").add(canvas);
         renderer = new Renderer(canvas);
 
         createControl();
 
         service.getInfo(new AsyncCallback<PlyInfo>() {
             public void onFailure(Throwable caught) {
                 logger.warning("Impossible to get the PLY information: " + caught);
 
             }
 
             public void onSuccess(PlyInfo result) {
                 renderer.setBoundingBox(result.getBoundingBox());
                 renderer.initialiseScene();
                canvas.setVisible(true);
 
                 int chunkSize = result.getChunkSize();
                 final int numPoints = result.getNumPoints();
                 cloud = new PointCloud(numPoints);
 
                 renderer.setPointCloud(cloud);
                 for (int offset = 0; offset < numPoints; offset += chunkSize) {
                     service.getPoints(offset, new AsyncCallback<Point[]>() {
                         public void onFailure(Throwable caught) {
                             percentage.setText("Error!");
                             logger.warning("Impossible to get the points");
                         }
 
                         public void onSuccess(Point[] result) {
                             cloud.addPoints(result);
                             double percent = cloud.getNumberOfPoints() * 1. / numPoints * 100;
 
                             percentage.setText(format.format(percent) + "%");
                             renderer.render();
 
                             if (cloud.getNumberOfPoints() == numPoints) {
                                 enable();
                             }
 
                         }
                     });
                 }
 
 
             }
         });
 
     }
 
     private void enable() {
         b1.setVisible(true);
         b2.setVisible(true);
         b3.setVisible(true);
         fps.setVisible(true);
         percentage.setVisible(false);
         RootPanel.setVisible(RootPanel.get("loading").getElement(), false);
         renderer.enable();
     }
 
     private void createControl() {
         Panel panel = new FlowPanel();
 
 
         b1 = new RadioButton("controlGroup", "Rotate");
         b1.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
             public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                 if (booleanValueChangeEvent.getValue()) renderer.setInteraction(Renderer.Interaction.ROTATE);
             }
         });
         b1.setVisible(false);
 
         b2 = new RadioButton("controlGroup", "Translate");
         b2.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
             public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                 if (booleanValueChangeEvent.getValue()) renderer.setInteraction(Renderer.Interaction.TRANSLATE);
             }
         });
         b2.setVisible(false);
 
         b3 = new RadioButton("controlGroup", "Zoom");
         b3.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
             public void onValueChange(ValueChangeEvent<Boolean> booleanValueChangeEvent) {
                 if (booleanValueChangeEvent.getValue()) renderer.setInteraction(Renderer.Interaction.ZOOM);
             }
         });
         b3.setVisible(false);
         b1.setValue(true);
 
         panel.add(b1);
         panel.add(b2);
         panel.add(b3);
 
         RootPanel.get("control").add(panel);
 
         fps = new Label("---");
         renderer.addListener(new Renderer.RendererListener() {
 
             public void event() {
                 fps.setVisible(true);
                 fps.setText(format.format(renderer.getFps()) + " fps");
             }
         });
         RootPanel.get("fps").add(fps);
 
         percentage = new Label("---");
         RootPanel.get("percent").add(percentage);
 
 
     }
 }
