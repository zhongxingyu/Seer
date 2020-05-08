 package navtreeWheat;
 
 import processing.core.*;
 
 public class Edge {
 	public boolean removed=false;
 	public Node from, to;
 	public float length;
 	public int fromId, toId, index;
 	private Navtree navtree;
 	public boolean draw = true;
 
 	Edge(Navtree _navtree, int _fromId, int _toId) {
 		navtree = _navtree;
 		fromId = _fromId;
 		toId = _toId;
 	}
 
 	public void pointToNodes() {
 		try {
 			from = navtree.findNode(fromId);
 			to = navtree.findNode(toId);
 		} catch (Exception e) {
 			removed=true;
 			navtree.removeEdge(this);
 		}
 		length = 15; // This will be changed dinamically according to the date
 						// distance of the nodes
 
 	}
 
 
 	public void draw() {
 		if (draw) {
 			navtree.applet.stroke(200);
 			navtree.applet.strokeWeight((float) 0.35);
 			if(to.draw && from.draw) navtree.applet.line(from.x, from.y, to.x, to.y);
 		}
 	}
 
 	public void relax() {
 		float vx = to.x - from.x;
 		float vy = to.y - from.y;
 		float d = PApplet.mag(vx, vy);
 		if (d > 0) {
 			float f = (length - d) / (d * 3);
 			float dx = f * vx;
 			float dy = f * vy;
 			to.dx += dx;
 			to.dy += dy;
 			from.dx -= dx;
 			from.dy -= dy;
 		}
 	}
 
 }
