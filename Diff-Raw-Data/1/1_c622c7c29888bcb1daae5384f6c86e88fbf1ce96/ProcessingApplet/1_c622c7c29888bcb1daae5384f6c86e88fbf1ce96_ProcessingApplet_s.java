 package memeograph.renderer.processing;
 
 import java.util.*;
 import processing.core.*;
 import memeograph.generator.jdb.nodes.*;
 
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import javax.media.opengl.GLException;
 import memeograph.graph.Graph;
 import memeograph.graph.Node;
 import memeograph.util.ACyclicIterator;
 
 /**
  * Does the actual drawing of graphs and nodes. No layout calculation code is
  * in here, checkout GrapLayoutHandler for that.
  * We also do the user input handling here.
  */
 public class ProcessingApplet extends PApplet implements MouseWheelListener{
     static int MOVE_TICK = 50;
 
 
     //Just to have something and avoid the dreaded null
     Iterator<Graph> graphs = new ArrayList<Graph>().iterator();
     Graph currentgraph = null;
 
     PFont font;
 
     //Camera Info
     PVector pos;
     PVector dir;
     PVector camNorth = new PVector(0,1,0);
 
     //Text Rendering info
     private final int renderfrontback = 1;
     private final int rendertopbottom = 2;
     private int rendermode = renderfrontback;
 
     private final Object lock = new Object();
     private boolean isSetup = false;
 
     public ProcessingApplet(){
         addMouseWheelListener(this);
     }
 
 
     @Override
     public void setup(){
         //Full screen, go big or go home!
         try{
             size(1024, 768, P3D);
             //size(1024, 768, OPENGL);
         }catch(GLException gle){
             gle.printStackTrace();
             System.exit(1);
         }
         background(102);
 
         font = createFont("SansSerif.bold", 18);
         textFont(font);
         textAlign(CENTER, CENTER);
 
         //Lets see if we can slow down the stacks rendering to 30fps
         frameRate(25);
         pos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*60.0f / 360.0f));
         dir = new PVector(width/2.0f, height/2.0f, 0);
 
         camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, camNorth.x, camNorth.y, camNorth.z);
         smooth();
 
         //Absolutely insane, but we need to make sure this runs before setting
         //the graph...
         synchronized(lock){ isSetup = true; lock.notifyAll(); }
     }
 
 
 
     @Override
     public void draw(){
         background(102);
         camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, 0, 1, 0);
 
         //Now draw the lines between the nodes
         ACyclicIterator<Node> i = new ACyclicIterator<Node>(currentgraph.preorderTraversal());
         while( i.hasNext()){
           Node parent = i.next();
           for (Node kid : parent.getChildren()) {
            if(kid == null) continue;
             drawLine(parent, kid);
           }
         }
 
         //And now to actually draw the nodes
         ACyclicIterator<Node> j = new ACyclicIterator<Node>(currentgraph.preorderTraversal());
         while(j.hasNext()) {
           drawNode(j.next());
         }
     }
 
 
     private void drawLine(Node f, Node t){
         if (f.lookup(GraphNodeType.class) instanceof ObjectGraphRoot) { return; }
         NodeGraphicsInfo from = f.lookup(NodeGraphicsInfo.class);
         NodeGraphicsInfo to = t.lookup(NodeGraphicsInfo.class);
 
         strokeWeight(5);
         stroke(1f,Math.min(from.opacity, to.opacity));
         line(from.x, from.y, from.z, to.x, to.y, to.z);
     }
 
     private void drawNode(Node node){
         if (node.lookup(GraphNodeType.class) instanceof ObjectGraphRoot) { return; }
         pushMatrix();
         NodeGraphicsInfo n = node.lookup(NodeGraphicsInfo.class);
         GraphNodeType t = node.lookup(GraphNodeType.class);
 
         translate(n.x, n.y, n.z);
 
         fill(n.r, n.g, n.b, n.opacity);
         strokeWeight(1);
         box(n.width, 20f, 20f);
 
         float size = 0;
         String data = null;
 
         if ((rendermode & renderfrontback) != 0) {
             data = t.toString();
             size = textWidth(data);
 
             pushMatrix();
             translate(0f, 0f, 11f);
             fill(5);
             text(data, 0, 0f);
 
             translate(0f, 0f, -22f);
 
             rotateY(PI);
             text(data, 0f, 0f);
             popMatrix();
         }
 
         if ((rendermode & rendertopbottom) != 0) {
             if (data == null){data = t.toString(); size = textWidth(data);}
 
             translate(0f, 11f, 0f);
             fill(5);
             rotateX(-PI/2);
             text(t.toString(), -size/2, 0f);
             rotateX(PI/2);
 
             translate(0f, -22f, 0f);
 
             rotateX(-PI/2);
             rotateY(PI);
             textAlign(LEFT);
             text(t.toString(), -size/2, 0f);
         }
         popMatrix();
     }
 
     public void setGraphs(Iterator<Graph> graphs ){
       //Make sure that setup has run first, then we can start to set the graphs
       synchronized(lock){
         while(!isSetup){
           try {
             lock.wait();
           } catch (InterruptedException ex) {
             ex.printStackTrace();
           }
         }
       }
 
       this.graphs = graphs;
       this.currentgraph = graphs.next();
       GraphLayoutHandler layout = new GraphLayoutHandler(currentgraph, this);
       currentgraph.getRoot().store(GraphLayoutHandler.class, layout);
       layout.doLayout();
   }
 
     private void nextGraph(){
       if (graphs.hasNext() == false) {
         System.err.println("No more to show you...");
         return;
       }
       Graph nextGraph = graphs.next();
       GraphLayoutHandler layout = nextGraph.getRoot().lookup(GraphLayoutHandler.class);
       if (layout == null) {
           layout = new GraphLayoutHandler(nextGraph, this);
           layout.doLayout();
           nextGraph.getRoot().store(GraphLayoutHandler.class, layout);
       }
       currentgraph = nextGraph;
     }
 
 
     private float dtheta = .03f;
     @Override
     public void mouseDragged()
     {
         float dy = pmouseY - mouseY;
         if (dy != 0) {
             float y = (pos.y-dir.y);
             float z = (pos.z-dir.z);
             float r = sqrt(y*y + z*z);
             float theta = atan2(y, z);
 
             float theta_new = theta + ((dy > 0) ? dtheta : (-1*dtheta));
             y = sin(theta_new) * r;
             z = cos(theta_new) * r;
             pos.y = dir.y + y;
             pos.z = dir.z + z;
         }
 
         float dx = pmouseX - mouseX;
         if (dx != 0) {
             float x = (pos.x-dir.x);
             float z = (pos.z-dir.z);
             float r = sqrt(x*x + z*z);
             float theta = atan2(z, x);
 
             float theta_new = theta + ((dx < 0) ? dtheta : (-1*dtheta));
             x = cos(theta_new) * r;
             z = sin(theta_new) * r;
             pos.x = dir.x + x;
             pos.z = dir.z + z;
         }
     }
 
     @Override
     public void keyPressed(){
         char k = (char)key;
         switch(k){
             case 'w':
             case 'W': translateCameraY(-MOVE_TICK); break;
             case 's':
             case 'S': translateCameraY(MOVE_TICK); break;
             case 'a':
             case 'A': translateCameraX(-MOVE_TICK); break;
             case 'd':
             case 'D': translateCameraX(MOVE_TICK); break;
             case 't':
             case 'T': toggleRenderMode(); break;
             case 'n':
             case 'N': nextGraph(); break;
             default: break;
         }
     }
 
     private void translateCameraY(float amount){
         PVector camera = PVector.sub(dir,pos);
         PVector cross = camera.cross(camNorth);
         PVector up = cross.cross(camera);
         up.normalize();
         up.mult(amount);
         pos.add(up);
         dir.add(up);
     }
 
     private void translateCameraX(float amount){
         PVector camera = PVector.sub(dir,pos);
         PVector cross = camera.cross(camNorth);
         cross.normalize();
         cross.mult(amount);
         pos.add(cross);
         dir.add(cross);
     }
 
     private void toggleRenderMode(){
         rendermode = (rendermode + 1) % ((renderfrontback|rendertopbottom) + 1);
     }
 
     @Override
     public void mouseMoved(){
     }
 
     public void mouseWheelMoved(MouseWheelEvent e) {
         int notches = -1*e.getWheelRotation(); //notches goes negative if the
                                             //wheel is scrolled up.
              
         PVector camera = PVector.sub(dir,pos);
         camera.normalize();
         
         pos.add(PVector.mult(camera, (float)notches * 100f));
         dir.add(PVector.mult(camera, (float)notches * 100f));
     }
 
 
 }
