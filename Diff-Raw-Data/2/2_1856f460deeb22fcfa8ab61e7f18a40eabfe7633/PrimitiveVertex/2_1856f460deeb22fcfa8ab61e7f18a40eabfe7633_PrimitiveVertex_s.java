 package edu.washington.cs.games.ktuite.pointcraft;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 /* these primitives built out of pellets...
  * keep a list of pellets and then draw lines or polygons between them.
  */
 public class PrimitiveVertex {
 	private int gl_type;
 	private List<Vector3f> vertices;
 	private float line_width = 5f;
 	private Vector3f pt_1;
 	private Vector3f pt_2;
 	private float a, b, c, d;
 
 	public PrimitiveVertex(int _gl_type, List<Vector3f> _vertices) {
 		gl_type = _gl_type;
 		vertices = _vertices;
 	}
 
 	public PrimitiveVertex(int _gl_type, List<Vector3f> _vertices,
 			float _line_width) {
 		gl_type = _gl_type;
 		vertices = _vertices;
 		line_width = _line_width;
 	}
 
 	public boolean isPolygon() {
 		if (gl_type == GL_POLYGON)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean isLine() {
 		if (pt_1 != null && pt_2 != null)
 			return true;
 		else
 			return false;
 	}
 
 	public boolean isPlane() {
 		if (a == 0 && b == 0 && c == 0 && d == 0)
 			return false;
 		else
 			return true;
 	}
 	
 	public void setLine(Vector3f a, Vector3f b){
 		pt_1 = new Vector3f(a);
 		pt_2 = new Vector3f(b);
 	}
 	
 	public void setPlane(float _a, float _b, float _c, float _d){
 		a = _a;
 		b = _b;
 		c = _c;
 		d = _d;
 	}
 
 	public void draw() {
 		if (gl_type == GL_LINES) {
 			glColor3f(0f, .1f, .3f);
 			glLineWidth(line_width);
 		} else if (gl_type == GL_POLYGON) {
 			glColor4f(.9f, .9f, 0, .5f);
 		}
 
 		glBegin(gl_type);
 		for (Vector3f vertex : vertices) {
 			glVertex3f(vertex.x, vertex.y, vertex.z);
 		}
 		glEnd();
 	}
 
 	public float distanceToPoint(Vector3f pos) {
 		float dist = Float.MAX_VALUE;
 		if (isLine()){
 			// OH GOD THIS LOOKS SO SLOW
 			// TODO: make faster
 			Vector3f temp = new Vector3f();
 			Vector3f sub1 = new Vector3f();
 			Vector3f sub2 = new Vector3f();
 			Vector3f sub3 = new Vector3f();
 			Vector3f.sub(pos,pt_1,sub1);
 			Vector3f.sub(pos,pt_2,sub2);
 			Vector3f.sub(pt_2, pt_1, sub3);
 			Vector3f.cross(sub1, sub2, temp);
 			dist = temp.length()/sub3.length();
 		}
 		else if (isPlane()){
 			dist = (float) ((a*pos.x + b*pos.y + c*pos.z + d)/Math.sqrt(a*a + b*b + d*d));
 		}
 		System.out.println("distancE: " + dist);
		return dist;
 	}
 
 }
