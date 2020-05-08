 /*
 * currently, existing edges will only be prefixed when 
  * their labels are updated upon addition of triples.
  */
 package nodes;
 
 import com.hp.hpl.jena.rdf.model.Statement;
 
 import controlP5.ControllerView;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import processing.core.PApplet;
 import processing.core.PVector;
 
 /**
  *
  * @author kdbanman
  */
 public class Edge extends GraphElement<Edge>  {
 
     // edge shouldn't be long enough to touch its nodes. (cleaner looking)
     // NOTE: using this to directly scale theta in inside() causes a bit
     //       of scaling error, since theta is an angle, not a length
     private float lengthScale;
 
     private Node src;
     private Node dst;
 
     private HashSet<Statement> triples;
 
     Edge(Graph parentGraph, String name, Node s, Node d) {
       super(parentGraph, name);
 
       src = s;
       dst = d;
 
       triples = new HashSet<>();
 
       lengthScale = 0.9f;
 
       setView(new ControllerView() {
           @Override
           public void display(PApplet p, Object e) {
             Edge edge = (Edge) e;
 
             edge.updatePosition();
 
             // get vector between the source and the destination nodes
             PVector between = edge.dst.getPosition().get();
             between.sub(edge.src.getPosition());
 
             p.pushMatrix();
             
             // Translate(x,y,0) called already in Controller, but nodes are in 3D
             p.translate(0,0,edge.getPosition().z);
             
             if (displayLabel) {
                 displayLabel();
             }
             
             // Rotate towards the destination node to orient the edge
             PVector  target = between.get();
 
             PVector up = new PVector(0,1,0);
             PVector axis = target.cross(up);
             float angle = PVector.angleBetween(target, up);
 
             p.rotate(-angle, axis.x, axis.y, axis.z);
 
             float len = between.mag() - edge.src.size - edge.dst.size;
             
             if (selected() && !inside()) {
                 p.fill(pApp.selectColor);
             } else {
                 p.fill(currentCol);
             }
             
             p.box(edge.size, edge.lengthScale*len, edge.size); 
             
             p.popMatrix();
           }
         }
       );
     }
 
     @Override
     public boolean inside() {
       // NOTE:  steps 1** and 2** switched give +8% framerate @ 5000 nodes, ~250K edges
 
       proj.calculatePickPoints(pApp.mouseX, pApp.mouseY);
 
       // vector mouse is from cursor inward orthogonally from the screen
       PVector mouse = proj.ptEndPos.get();
       mouse.sub(proj.ptStartPos);
 
       // get edge vector between the source and the destination nodes
       PVector between = dst.getPosition().get();
       between.sub(src.getPosition());
 
       //  find shortest distance between mouse and edge vectors (lines)
       //  1 **
 
       // get normalized vector orthogonal to both.
       // (p,q,r) of this vector defines the coefficients of all planes
       // orthogonal to both vectors: px + qy + rz = C
       PVector orthogonal = mouse.cross(between);
       orthogonal.normalize();
 
       // find constant C for plane containing mouse and edge lines.
       // because |orthogonal| == 1, C represents the displacement of
       // the plane along the orthogonal vector defining it
       float mouseC = orthogonal.x * proj.ptStartPos.x +
                       orthogonal.y * proj.ptStartPos.y +
                       orthogonal.z * proj.ptStartPos.z;
 
       float edgeC = orthogonal.x * src.getPosition().x +
                       orthogonal.y * src.getPosition().y +
                       orthogonal.z * src.getPosition().z;
 
       // hooray!
       float dist = Nodes.abs(mouseC - edgeC);
 
       // test if shortest distance between edge and mouse lines is within
       // the size constraint of the edge
       if (dist < size) {
 
         //  determine whether or not the mouse vector is within the ongular
         //  sweep of the edge vector
         //  2 **
 
         // get angular displacement of edge
         PVector toSource = src.getPosition().get();
         toSource.sub(proj.ptStartPos);
 
         PVector toDest = dst.getPosition().get();
         toDest.sub(proj.ptStartPos);
 
         float theta = lengthScale * PVector.angleBetween(toSource, toDest);
 
         // get angles from mouse to source and from mouse to destination
         float phi1 = PVector.angleBetween(toSource, mouse);
         float phi2 = PVector.angleBetween(toDest, mouse);
 
         return phi1 < theta && phi2 < theta;
       } 
 
       return false;
     }
 
     public Edge updatePosition() {
       PVector sPos = src.getPosition();
       PVector dPos = dst.getPosition();
 
       float dist = PVector.dist(sPos, dPos);
 
       // offset in midpoint between the nodes w.r.t their radii
       float offset = 0.5f * (1 + (src.size - dst.size) / dist);
 
       // get the (offset) midpoint between the source and the destination
       PVector midpoint = PVector.lerp(sPos, dPos, offset);
       return setPosition(midpoint);
     }
     
     @Override
     protected void moveIfNotSelected(PVector horiz, PVector vert) {
         if (!src.selected()) {
             src.getPosition().add(horiz);
             src.getPosition().add(vert);
         }
         if (!dst.selected()) {
             dst.getPosition().add(horiz);
             dst.getPosition().add(vert);
         }
     }
     
     /**
      * prefixes and assembles each line of the labelText
      */
     @Override
     public void prefixLabel() {
         labelText.clear();
         // iterate through triples
         for (Statement t : triples) {
             //  prefix each member of the triple
             labelText.add("<" + graph.prefixed(t.getSubject().toString()) 
                     + "> <" + graph.prefixed(t.getPredicate().toString())
                     + "> <" + graph.prefixed(t.getObject().toString()) + ">");
         }
     }
     
     public Node getSourceNode() {
         return src;
     }
     
     public Node getDestinationNode() {
         return dst;
     }
     
     public Set<Statement> getTriples() {
         return triples;
     }
     
     public void addTriple(Statement t) {
         String sub = t.getSubject().toString();
         String pred = t.getPredicate().toString();
         String obj = t.getObject().toString();
         
         if (src.getName().equals(sub) && dst.getName().equals(obj) 
               || src.getName().equals(obj) && dst.getName().equals(sub)) {
             
             if (!triples.contains(t)) triples.add(t);
             
             // with overridden prefixLabel(), updateLabel() will work
             updateLabel();
         } else {
             Nodes.println("ERROR:  triple\n  " + sub + "\n  " + pred + "\n  " + obj
                     + "\ndoes not belong to edge\n  " + getName());
         }
     }
 }
