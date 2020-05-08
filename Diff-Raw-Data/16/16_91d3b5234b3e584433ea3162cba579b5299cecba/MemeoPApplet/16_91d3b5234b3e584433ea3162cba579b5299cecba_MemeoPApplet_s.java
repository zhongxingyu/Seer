 package memeograph.ui;
 
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import memeograph.DiGraph;
 import memeograph.TreeChangeListener;
 import processing.core.PApplet;
 import processing.core.PFont;
 import processing.core.PVector;
 
 /**
  * Does the drawing and layout. Originally made by extending the moving eye example from processing.
  */
 public class MemeoPApplet extends PApplet implements TreeChangeListener, MouseWheelListener{
     static int PADDING = 20;
     static float K = 0.01f; //Spring constant (Along the Y)
     static float M = 0.95f; //Magnet contents (Along the X)
     static float FRICTION = .95f;
     static int MOVE_TICK = 50;
 
     private Map<DiGraph, Node> positions = new HashMap<DiGraph, Node>();
     private Grid rails  = new Grid();
 
     private List<DiGraph> stacks;
     private boolean treechanged = false;
     private boolean laidout = false;
 
     private int wanted_width;
     private int wanted_height;
 
     PFont font;
 
     //Camera Info
     PVector pos;
     PVector dir;
     PVector camNorth = new PVector(0,1,0);
    PVector camEast = new PVector(1,0,0);
 
     public MemeoPApplet(List<DiGraph> tree, int width, int height){
         this.stacks = tree;
         this.wanted_height = height;
         this.wanted_width = width;
         addMouseWheelListener(this);
     }
 
 
     @Override
     public void setup(){
         //Full screen, go big or go home!
         size(wanted_width, wanted_height, OPENGL);
         background(102);
 
         font = createFont("SansSerif.bold", 18);
         textFont(font);
         textAlign(CENTER, CENTER);
 
         //Lets see if we can slow down the stacks rendering to 30fps
         frameRate(20);
         pos = new PVector(width/2.0f, height/2.0f, (height/2.0f) / tan(PI*60.0f / 360.0f));
         dir = new PVector(width/2.0f, height/2.0f, 0);
 
         camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, camNorth.x, camNorth.y, camNorth.z);
         smooth();
     }
 
 
     @Override
     public void draw(){
         background(102);
         camera(pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, 0, 1, 0);
 
         //First check if we have to layout this stuff out
         if (!laidout) {
             treechanged = false;
             layout(stacks);
             System.out.println(rails);
         }
 
         //jiggle our layout
         adjust();
 
         //Now draw the lines between the nodes
         for (Node n : positions.values()) {
             for (DiGraph kid : n.data.getChildren()) {
                 Node knode = positions.get(kid);
                 if (n != null && knode != null) 
                     drawLine(n, knode);
             }
         }
 
         //Draw the nodes ontop of the lines. Awesome.
         for (Node n : positions.values()) {
             drawNode(n);
         }
     }
 
     private void drawLine(Node from, Node to){
         strokeWeight(5);
         line((float)from.x, (float)from.y, (float)from.z,
                 (float)to.x, (float)to.y, (float)to.z);
     }
 
     private void drawNode(Node n){
         translate((float)n.x, (float)n.y, (float)n.z);
 
         fill(n.r, n.g, n.b);
         strokeWeight(1);
         box((float)n.width, 20f, 20f);
 
         translate(0f, 0f, 11f);
         fill(5);
         text(n.data.getTreeName(), 0f, 0f);
 
         translate(0f, 0f, -22f);
 
         rotateY(PI);
         text(n.data.getTreeName(), 0f, 0f);
         rotateY(-PI);
         translate(0f, 0f, 11f);
 
         translate(-(float)n.x, -(float)n.y, -(float)n.z);
     }
 
     private void layout(List<DiGraph> t)
     {
         for (DiGraph stack : t) {
             layout(stack, -10, 0);
             
             DiGraph sf = stack;
             int y = 0;
             while (sf.getSoftwareChildren().size() == 1){
                 sf = sf.getSoftwareChildren().firstElement();
                 y+=50;
                 layout(sf, -10, y);
             }
 
             for (Vector<Node> rail : rails) {
                 double x = 0;
                 for (Node n : rail) {
                     n.x = x;
                     x += n.width + 50;
                 }
             }
         }
 
         laidout = true; 
     }
 
     private void layout(DiGraph t, int z, int y)
     {
         if (positions.get(t) != null) return;
         Node n = new Node(t, 0, y*50, z*50);
         n.width = textWidth(t.getTreeName());
 
         positions.put(t, n);
         rails.add(z, y, n);
 
         for (DiGraph kid : t.getDataChildren()) {
             layout(kid, z-1, y);
         }
 
         for (DiGraph kid : t.getSoftwareChildren()) {
             layout(kid, z, y+1);
         }
     }
 
     private double adjust(){
         if (!laidout) return -1;
 
         double total = 0;
 
         for (Node n : positions.values()) {
             n.fx = 0;
         }
 
         //magnets
         
         for (Vector<Node> layer : rails) {
             for(int j = 0; j < layer.size(); j++){
                 if (j > 0){
                     Node r = layer.get(j);
                     Node l = layer.get(j-1);
                     double d = (l.x + l.width/2) - (r.x - r.width/2);
                     layer.get(j).fx += 1000.0 / (d*d + 1);
                 }
 
                 if (j < layer.size() - 1){
                     Node l = layer.get(j);
                     Node r = layer.get(j+1);
                     double d = l.x + l.width/2 - (r.x - r.width/2);
                     layer.get(j).fx -= 1000.0 / (d*d + 1);
                 }
             }
 
         }
 
         //springs
         for (Node n : positions.values()) {
             for (DiGraph kidt : n.data.getChildren()) {
                 Node kid = positions.get(kidt);
                 if (kid == null) continue;
 
                 // F = -k*d
                 double dx = n.x - kid.x;
                 double dy = n.y - kid.y;
                 double dz = n.z - kid.z;
 
                 double d = Math.sqrt(dx*dx + dy*dy + dz * dz);
                 double F = K * d;
                 kid.fx += F*dx/d;
             }
         }
 
         
         for (Vector<Node> layer : rails) {
             for(int j = 0; j < layer.size(); j++){
                 Node n = layer.get(j);
                 n.vx = n.vx*FRICTION + 1*n.fx;
                 double newx = n.x + 0.1*n.vx;
                 total+= Math.abs(n.fx);
                 n.x = newx;
             }
 
             // Not too close, okay...
             for(int j = 1; j < layer.size(); j++){
                 Node l = layer.get(j-1);
                 Node n = layer.get(j);
                 if (l.x + l.width/2 + n.width/2 + PADDING > n.x) {
                     n.x = l.x + l.width/2 + n.width/2 + PADDING;
                 }
             }
         }
         
 
         //System.out.println(total);
         return total;
     }
 
     public void kidAdded(DiGraph parent, DiGraph addedNode) {
         throw new UnsupportedOperationException();
     }
 
     public void childrenRemoved(DiGraph parent) {
         throw new UnsupportedOperationException();
     }
 
     public void dataChanged(DiGraph parent) {
         throw new UnsupportedOperationException();
     }
 
 
     float dtheta = .03f;
     @Override
     public void mouseDragged()
     {
         float dy = pmouseY - mouseY;
         if (dy != 0) {
             float y = (pos.y-dir.y);
             float z = (pos.z-dir.z);
             float r = sqrt(y*y + z*z);
             float theta = atan2(y, z);
 
            float theta_new = theta + ((dy < 0) ? dtheta : (-1*dtheta));
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
             default: break;
         }
     }
 
     private void translateCameraY(float amount){
         PVector camera = PVector.sub(dir,pos);
        PVector cross = camera.cross(camEast);
        cross.normalize();
        cross.mult(-amount);
        pos.add(cross);
        dir.add(cross);
     }
 
     private void translateCameraX(float amount){
         PVector camera = PVector.sub(dir,pos);
         PVector cross = camera.cross(camNorth);
         cross.normalize();
         cross.mult(amount);
         pos.add(cross);
         dir.add(cross);
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
