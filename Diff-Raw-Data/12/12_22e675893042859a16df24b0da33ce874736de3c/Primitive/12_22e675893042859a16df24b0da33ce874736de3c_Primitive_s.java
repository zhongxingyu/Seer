 package edu.washington.cs.games.ktuite.pointcraft;
 
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Vector2f;
 import org.lwjgl.util.vector.Vector3f;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 
 /* these primitives built out of pellets...
  * keep a list of pellets and then draw lines or polygons between them.
  */
 public class Primitive {
 
 	private int gl_type;
 	private List<Pellet> vertices;
 	private float line_width = 5f;
 	private byte[] texture_data = null;
 	private transient Texture texture = null;
 	private Vector3f player_position;
 	private Vector3f player_viewing_direction;
 	private String texture_url = null;
 
 	private void readObject(ObjectInputStream ois)
 			throws ClassNotFoundException, IOException {
 		ois.defaultReadObject();
 		if (texture_url != null){
 			//texture_url = "dont_texture-" + texture_url;
 			startDownloadingTexture();
 		}
 	}
 
 	public Primitive(int _gl_type, List<Pellet> _vertices) {
 		gl_type = _gl_type;
 		vertices = _vertices;
 		if (gl_type == GL_POLYGON) {
 			System.out.println("making new polygon Primitive");
 			System.out.println("number of vertices: " + vertices.size());
 		}
 	}
 
 	public Primitive(int _gl_type, List<Pellet> _vertices, float _line_width) {
 		gl_type = _gl_type;
 		vertices = _vertices;
 		line_width = _line_width;
 	}
 
 	public void setPlayerPositionAndViewingDirection(Vector3f pos, Vector3f view) {
 		player_position = new Vector3f(pos);
 		player_viewing_direction = new Vector3f(view);
 		player_viewing_direction.normalise();
 		if (vertices.size() > 4)
 			startDownloadingTexture();
 	}
 
 	public boolean isPolygon() {
 		if (gl_type == GL_POLYGON)
 			return true;
 		else
 			return false;
 	}
 
 	public int numVertices() {
 		return vertices.size();
 	}
 	
 	public List<Pellet> getVertices(){
 		return vertices;
 	}
 
 	public void draw() {
 		if (gl_type == GL_LINES) {
 			glColor3f(0, 0, 0);
 			glLineWidth(line_width);
 		} else if (gl_type == GL_POLYGON) {
 			glColor4f(.9f, .9f, .9f, .9f);
 			if (texture != null) {
 				glEnable(GL_TEXTURE_2D);
 				texture.bind();
 			} else {
 				glDisable(GL_TEXTURE_2D);
 				if (texture_data != null) {
 					try {
 						texture = TextureLoader.getTexture("PNG",
 								new ByteArrayInputStream(texture_data));
 						texture_data = null;
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 		}
 
 		Vector2f[] tex_coords = new Vector2f[] { new Vector2f(0, 0),
 				new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1) };
 
 		glBegin(gl_type);
 		for (int i = 0; i < vertices.size() && i < tex_coords.length; i++) {
 			Pellet pellet = vertices.get(i);
 			Vector3f vertex = pellet.pos;
 			glTexCoord2f(tex_coords[i].x, tex_coords[i].y);
 			glVertex3f(vertex.x, vertex.y, vertex.z);
 		}
 		glEnd();
 
 		glDisable(GL_TEXTURE_2D);
 		
 		// draw a border around the polygons
 		glColor3f(0, 0, 0);
 		glLineWidth(3);
 		glBegin(GL_LINE_LOOP);
 		for (int i = 0; i < vertices.size() && i < tex_coords.length; i++) {
 			Pellet pellet = vertices.get(i);
 			Vector3f vertex = pellet.pos;
 			glTexCoord2f(tex_coords[i].x, tex_coords[i].y);
 			glVertex3f(vertex.x, vertex.y, vertex.z);
 		}
 		glEnd();
 	}
 
 	public void startDownloadingTexture() {
 		if (texture_url == null) {
 			texture_url = "http://excelso.cs.washington.edu:8081/texture.png?&v=";
 			for (Pellet p : vertices) {
 				Vector3f v = p.pos;
 				texture_url += v.x + "," + v.y + "," + v.z + ",";
 			}
 			texture_url += "garbage,&w=128,&h=128,";
 			if (player_position != null && player_viewing_direction != null) {
 				texture_url += "&p=" + player_position.x + ","
 						+ player_position.y + "," + player_position.z + ",";
 				texture_url += "&e=" + player_viewing_direction.x + ","
 						+ player_viewing_direction.y + ","
 						+ player_viewing_direction.z + ",";
 			}
 		}
 		System.out.println(texture_url);
 		final String final_url_string = texture_url;
 		new Thread() {
 			public void run() {
 
 				try {
 					URL url = new URL(final_url_string);
 					InputStream is = url.openStream();
 					ByteArrayOutputStream baos = new ByteArrayOutputStream();
 					byte[] bytes = new byte[4096];
 					int n;
 					while ((n = is.read(bytes)) != -1) {
 						baos.write(bytes, 0, n);
 					}
 					texture_data = baos.toByteArray();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}.start();
 	}
 
 	public String plyFace() {
 		String s = vertices.size() - 1 + "";
 		for (int i = 0; i < vertices.size() - 1; i++) {
 			Pellet pellet = vertices.get(i);
 			s += " " + Main.all_pellets_in_world.indexOf(pellet);
 		}
 		s += "\n";
 		System.out.println("PLYFACE:" + s);
 		return s;
 	}
 
 	public void printTriangleVertices() {
 		for (int i = 0; i < vertices.size() - 1; i++) {
 			Pellet pellet = vertices.get(i);
 			System.out.println(pellet.pos.x + " " + pellet.pos.y + " "
 					+ pellet.pos.z);
 		}
 		System.out.println("");
 	}
 	
 	public float distanceToPolygonPlane(Vector3f pos){
 		Vector3f v1 = new Vector3f();
 		Vector3f.sub(vertices.get(0).pos, vertices.get(1).pos, v1);
 		Vector3f v2 = new Vector3f();
 		Vector3f.sub(vertices.get(0).pos, vertices.get(2).pos, v2);
 		Vector3f norm = new Vector3f();
 		Vector3f.cross(v1, v2, norm);
 		norm.normalise();
 
 		float a = norm.x;
 		float b = norm.y;
 		float c = norm.z;
 		float d = -1
 				* (a * vertices.get(0).pos.x + b
 						* vertices.get(0).pos.y + c
 						* vertices.get(0).pos.z);
 
 		float distance = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
 				.sqrt(a * a + b * b + c * c));
 		
 		return Math.abs(distance);
 	}
 
 	public static void addBackDeletedPrimitive(Primitive primitive) {
 		// TODO: add the lines
 		/*
 		for (int i = 0; i < primitive.vertices.size() - 1; i++){
 			List<Pellet> last_two = new LinkedList<Pellet>();
 			last_two.add(primitive.getVertices().get(i));
 			last_two.add(primitive.getVertices().get(i+1));
 
 			Primitive line = new Primitive(GL_LINES, last_two);
 			Main.geometry.add(line);
 		}
 		*/
 		
 		Main.geometry.add(primitive);
 	}
 }
