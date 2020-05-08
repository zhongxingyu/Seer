 package edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.screens;
 
 import controlP5.Button;
 import controlP5.ControlP5;
 import controlP5.Slider;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.ClusterDendrogramNode;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.Dendrogram;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.clustering.DendrogramNode;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.node.Node;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.viewing.DendrogramSlice;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.viewing.DendrogramSlicer;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.viewing.DendrogramSlicerImpl;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.ApplicationState;
 import edu.tufts.eecs.graphtheory.collapsiblegraph.windowing.Fonts;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import processing.core.PApplet;
 
 /**
  *
  * @author Jeremy
  */
 public class GraphRendererScreen {
 
     private static GraphRendererScreen instance;
     private PApplet papplet;
     private ControlP5 guiController;
     private Slider slider;
     private Button zoom;
     private Dendrogram dendrogram;
     private DendrogramSlicer dendrogramSlicer;
     double partitionDistance = 0.0F;
     private List<ViewableDendrogramNode> viewableDNodes;
     private boolean recentlyRendered = false;
     private ViewableDendrogramNode selectedNode;
     private Set<Node> selectedDataNodes;
 
     private GraphRendererScreen(PApplet papplet, ControlP5 guiController) {
         this.papplet = papplet;
         this.guiController = guiController;
         viewableDNodes = new ArrayList<ViewableDendrogramNode>();
         dendrogramSlicer = new DendrogramSlicerImpl();
     }
 
     private void setup() {
         slider = guiController.addSlider("zoomSlider", 0f, (float) ((ClusterDendrogramNode) dendrogram.getRootNode()).getDistance(), 30, 550, 200, 30);
         slider.alignValueLabel(ControlP5.TOP);
         slider.showTickMarks(true);
         slider.setCaptionLabel("");
         slider.hide();
 
         zoom = guiController.addButton("zoomButton", 1, 250, 550, 80, 30);
         zoom.setLabel("Zoom!");
         zoom.hide();
         guiController.draw();
 
 
     }
 
     public static GraphRendererScreen getGraphRendererScreen(PApplet papplet, ControlP5 guiController) {
         instance = new GraphRendererScreen(papplet, guiController);
         return instance;
     }
 
     public static GraphRendererScreen getGraphRendererScreen() {
         return instance;
     }
 
     public void setDendrogram(Dendrogram dendrogram) {
         this.dendrogram = dendrogram;
         setup();
     }
 
     public ApplicationState draw() {
         papplet.background(0);
         slider.show();
         zoom.show();
         guiController.draw();
 
         for (ViewableDendrogramNode viewableNode : viewableDNodes) {
 
             if (papplet.mousePressed) {
                 if (Math.pow((papplet.mouseX - viewableNode.getXCoordinate()), 2) + Math.pow((papplet.mouseY - viewableNode.getYCoordinate()), 2) < Math.pow(25, 2)) {
                     selectedNode = viewableNode;
                     selectedDataNodes = viewableNode.getDendrogramNode().getNodes();
                 }
             }
             
             if (viewableNode == selectedNode) {
                 papplet.fill(0, 0, 255);
             } else {
                 papplet.fill(255, 255, 255);
             }
             papplet.ellipseMode(papplet.CENTER);
             papplet.ellipse(viewableNode.getXCoordinate(), viewableNode.getYCoordinate(), 50, 50);
         }
         if(selectedDataNodes!=null && !selectedDataNodes.isEmpty()) {
         String nodes = "";
         for(Node selectedDataNode : selectedDataNodes) {
             nodes = nodes + selectedDataNode.toString() + ",";
         }
         papplet.fill(0, 0, 255);
        papplet.line(0, 500, 1024, 500);
         papplet.textFont(Fonts.getMediumFont());
         papplet.textAlign(papplet.LEFT);
        papplet.text(nodes, 400 , 520);
         }
         return ApplicationState.GRAPH_RENDERER_SCREEN;
     }
 
     public void drawViewableDendrogramNodes(DendrogramNode[] nodesToShow) {
     }
 
     public void setZoomLevel(double zoomLevel) {
         partitionDistance = zoomLevel;
     }
 
     public void redraw() {
         DendrogramSlice currentSlice = dendrogramSlicer.partitionByDistance(partitionDistance, dendrogram);
         DendrogramNode[] nodesToShow = currentSlice.getVisibleDendrogramNodes();
 
         viewableDNodes.clear();
 
 
         int maxX = 1024;
         int maxY = 500;
         int x = 25;
         int y = 25;
 
         papplet.ellipseMode(papplet.CENTER);
         for (DendrogramNode nodeToShow : nodesToShow) {
             ViewableDendrogramNode nextNode = new ViewableDendrogramNode(nodeToShow, 50, x, y);
             x += 60;
             if (x > maxX - 25) {
                 x = 25;
                 y += 60;
             }
             if (y > maxY - 25) {
                 System.out.println("Problem! Too many DNodes!");
                 return;
             }
             viewableDNodes.add(nextNode);
         }
         draw();
     }
 
     private static class ViewableDendrogramNode {
 
         DendrogramNode dendrogramNode;
         int diameter;
         int xCoordinate;
         int yCoordinate;
 
         public ViewableDendrogramNode(DendrogramNode dendrogramNode, int diameter, int xCoordinate, int yCoordinate) {
             this.dendrogramNode = dendrogramNode;
             this.diameter = diameter;
             this.xCoordinate = xCoordinate;
             this.yCoordinate = yCoordinate;
         }
 
         public DendrogramNode getDendrogramNode() {
             return dendrogramNode;
         }
 
         public int getDiameter() {
             return diameter;
         }
 
         public int getXCoordinate() {
             return xCoordinate;
         }
 
         public int getYCoordinate() {
             return yCoordinate;
         }
     }
 }
