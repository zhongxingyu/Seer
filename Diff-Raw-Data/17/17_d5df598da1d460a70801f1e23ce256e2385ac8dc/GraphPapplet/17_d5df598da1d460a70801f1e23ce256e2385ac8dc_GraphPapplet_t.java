 package edu.tufts.eecs.graphtheory.collapsiblegraph.windowing;
 
 import controlP5.ControlEvent;
 import controlP5.ControlP5;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.Dendrogram;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.screens.GraphRendererScreen;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.screens.IntroScreen;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.screens.MainScreen;
 import processing.core.PApplet;
 
 /**
  *
  * @author Jeremy
  */
 public class GraphPapplet extends PApplet {
 
     private ApplicationState currentState;
     private MainScreen mainScreen;
     private IntroScreen introScreen;
     private GraphRendererScreen graphRendererScreen;
     private ControlP5 guiController;
     private Dendrogram dendrogram;
     
 
 
     @Override
     public void setup() {
         size(1024, 600);
         guiController = new ControlP5(this);
         guiController.setAutoDraw(false);
         
         Fonts.initialize(this);
         mainScreen = MainScreen.getMainScreen(this, guiController);
         introScreen = IntroScreen.getIntroScreen(this, guiController);
         graphRendererScreen = GraphRendererScreen.getGraphRendererScreen(this, guiController);
         
         currentState = ApplicationState.INTRO_SCREEN;
 
         background(0);
         smooth();
     }
 
     @Override
     public void draw() {
         switch (currentState) {
             case INTRO_SCREEN: {
                 currentState = introScreen.draw();
                 break;
             }
             
             case MAIN_SCREEN: {
                 currentState = mainScreen.draw();
                 break;
             }
             
             case GRAPH_RENDERER_SCREEN: {
                 currentState = graphRendererScreen.draw();
                 break;
             }
            
         }
     }
     
     
     public void controlEvent(ControlEvent theEvent) {
         String eventName = theEvent.controller().name();
         if(eventName.equals("loadGraph")) {
             mainScreen.loadGraph();
        }
        
         if(eventName.equals("submit")) {
             graphRendererScreen.setDendrogram(mainScreen.processGraph());
            graphRendererScreen.redraw();
            graphRendererScreen.draw();
             currentState = ApplicationState.GRAPH_RENDERER_SCREEN;
         }
        
         if(eventName.equals("zoomSlider")) {
             graphRendererScreen.setZoomLevel((double)theEvent.controller().value());
         }
         if(eventName.equals("zoomButton")) {
             graphRendererScreen.redraw();
         }
     }
     
 
 
     public static void main(String[] argv) {
         PApplet.main(new String[]{"edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.GraphPapplet"});
     }
     
     
 }
