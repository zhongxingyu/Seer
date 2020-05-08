 //TODO: Maybe add transforms?
 package engine.render;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import java.util.ArrayList;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.ARBVertexBufferObject;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 import org.lwjgl.opengl.GL20;
 
 import com.bulletphysics.collision.dispatch.CollisionObject;
 import com.bulletphysics.collision.shapes.BoxShape;
 import com.bulletphysics.collision.shapes.CollisionShape;
 import com.bulletphysics.collision.shapes.ConvexHullShape;
 import com.bulletphysics.linearmath.Transform;
 import com.bulletphysics.util.ObjectArrayList;
 
 import engine.entity.Entity;
 import engine.importing.FileLoader;
 
 import engine.render.model_pieces.Face;
 import engine.render.model_pieces.Mesh;
 import engine.render.model_pieces.Texture;
 import engine.resource.Resource;
 import engine.resource.ResourceManager;
 
 public class Model implements RenderObject, Resource {
 	private ArrayList<Mesh> meshes;
 	private volatile Vector3f max, min, center;
 	private boolean hasVBO = false;
 	private int modelVBOID;
 	private int modelVBOindexID;
 	private FloatBuffer vertex_buffer;
 	private IntBuffer index_buffer;
 	//private Integer pointIndex = 0;
 	private FloatBuffer buf;
 	private Shader shader;
 	private CollisionShape shape;
 	private int total_vertices=0;
 	private Texture opengl_tex;
 	private boolean wire_frame = false;
 
 	//private Boolean immediate_scale_rotate = false;
 
 	public Model() {
 		meshes = new ArrayList<Mesh>();
 		init();
 	}
 	
 	public Model(Shader shader) {
 		meshes = new ArrayList<Mesh>();
 		init();
 		this.shader = shader;
 	}
 
 	public Model(Mesh[] mesh_array) {
 		meshes = new ArrayList<Mesh>();
 		for (Mesh m : mesh_array) {
 			meshes.add(m);
 		}
 		init();
 	}
 
 	public Model(ArrayList<Mesh> mesh_array) {
 		meshes = new ArrayList<Mesh>();
 		for (Mesh m : mesh_array) {
 			meshes.add(m);
 		}
 		init();
 	}
 
 	// Copy Constructor
 	public Model(Model model) {
 		init();
 		this.meshes = new ArrayList<Mesh>();
 		for (Mesh m : model.meshes) {
 			this.meshes.add(new Mesh(m));
 		}
 		//modelVBOID = model.getVBOID();
 		//modelVBOindexID = model.getVBOindexID();
 		this.shape = model.getCollisionShape();
 		this.shader = model.shader;
		//verify();
 	}
 
 	public void init() {
 		max = new Vector3f();
 		min = new Vector3f();
 		center = new Vector3f();
 	}
 
 	/* Setters */
 	public void addMesh(Mesh m) {
 		meshes.add(m);
 		//reduceHull();
 	}
 	
 	public void deleteMeshes() {
 		meshes.clear();
 	}
 
 	/* Getters */
 	public Mesh getMesh(int i) {
 		return meshes.get(i);
 	}
 
 	public Vector3f getCenter() {
 		return center;
 	}
 
 	public ArrayList<Mesh> getMeshes() {
 		return this.meshes;
 	}
 	
 	public int getMeshCount() {
 		return meshes.size();
 	}
 
 	public boolean getHasVBO() {
 		return hasVBO;
 	}
 
 	public int getVBOID() {
 		return modelVBOID;
 	}
 
 	public int getVBOindexID() {
 		return modelVBOindexID;
 	};
 
 	/* Draw Methods */
 	private void rotateAndScaleImmediate(CollisionObject collision_object) {
 		// Retrieve the current motionstate to get the transform
 		// versus the world
 		Transform transform_matrix = new Transform();
 		
 		transform_matrix.set(collision_object.getWorldTransform(new Transform()));
 
 		// Adjust the position and rotation of the object from physics
 		float[] body_matrix = new float[16];
 		buf = BufferUtils.createFloatBuffer(16);
 		
 		transform_matrix.getOpenGLMatrix(body_matrix);
 		buf.put(body_matrix);
 		buf.flip();
 		GL11.glMultMatrix(buf);
 		buf.clear();
 
 		// Scaling code (testing)
 		Vector3f halfExtent = new Vector3f();
 		collision_object.getCollisionShape().getLocalScaling(halfExtent);
 		GL11.glScalef(
 			1.0f * halfExtent.x, 
 			1.0f * halfExtent.y,
 			1.0f * halfExtent.z
 		);
 	}
 
 	/* Verify */
 	// This function will verify whether the file had normals defined
 	// and also check for shading groups and normalize if possible
 	public void verify() {
 		ArrayList<Vector3f> maxes = new ArrayList<Vector3f>();
 		ArrayList<Vector3f> mins = new ArrayList<Vector3f>();
 
 		// for each mesh we get the max and min width,height,depth
 		// we also calculate normals since we're going through the
 		// whole list anyway
 		for (Mesh m : meshes) {
 			maxes.add(m.getMaximums());
 			mins.add(m.getMinimums());
 			m.calcNormals(true);
 		}
 
 		max = new Vector3f();
 		min = new Vector3f();
 
 		// create a vector for making a surrounding shape that
 		// matches the largest and smallest points exactly
 		max = maxes.get(0);
 		min = mins.get(0);
 		for (int i = 1; i < maxes.size(); i++) {
 			if (maxes.get(i).x > max.x) {
 				max.x = maxes.get(i).x;
 			}
 			if (maxes.get(i).y > max.y) {
 				max.y = maxes.get(i).y;
 			}
 			if (maxes.get(i).z > max.z) {
 				max.z = maxes.get(i).z;
 			}
 
 			if (mins.get(i).x < min.x) {
 				min.x = mins.get(i).x;
 			}
 			if (mins.get(i).y < min.y) {
 				min.y = mins.get(i).y;
 			}
 			if (mins.get(i).z < min.z) {
 				min.z = mins.get(i).z;
 			}
 		}
 		// find the center of the model using our min/max values
 		center.add(max, min);
 		center.scale(0.5f);
 
 		// TODO: check to see if the max/min are working
 		// System.out.println("MAX: " + String.valueOf(max));
 		// System.out.println("MIN: " + String.valueOf(min));
 	}
 
 	/* Export */
 	public void saveXGL(String filename) {
 		StringBuffer data = new StringBuffer();
 
 		data.append("<WORLD>\n");
 		for (int i = 0; i < meshes.size(); i++) {
 			data.append(meshes.get(i).toXGLString(i));
 		}
 		data.append("</WORLD>\n");
 
 		try {
 			BufferedWriter out = new BufferedWriter(new FileWriter(filename));
 			out.write(data.toString());
 			out.close();
 			System.out.println("Export succeeded");
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.out.println("EXPORT FAILED\n\n");
 		}
 	}
 	
 	public StringBuffer toXGLString() {
 		StringBuffer data = new StringBuffer();
 
 		data.append("<WORLD>\n");
 		for (int i = 0; i < meshes.size(); i++) {
 			data.append(meshes.get(i).toXGLString(i));
 		}
 		data.append("</WORLD>\n");
 
 		return data;
 	}
 
 	/* Debug */
 	public String toString() {
 		String ret = "Model{\n";
 		ret += "	# of Meshes:" + meshes.size() + "\n";
 		for (Integer i = 0; i < meshes.size(); i++) {
 			ret += "	Mesh " + i.toString() + "{\n";
 			ret += meshes.get(i).toString();
 			ret += "	}\n";
 		}
 		ret += "}\n";
 		return ret;
 	}
 
 	public CollisionShape createCollisionShape() {
 		Vector3f shape = new Vector3f();
 		shape.sub(max, min);
 		// shape.scale(0.5f);
 		return new BoxShape(shape);
 	}
 
 	public ArrayList<Byte> getColor() {
 		return meshes.get(0).getMaterial().getColor();
 	}
 
 	// *******************VBO METHODS**************************
 	public int createVBOID(int i) {
 		IntBuffer buffer = BufferUtils.createIntBuffer(i);
 		ARBVertexBufferObject.glGenBuffersARB(buffer);
 		return buffer.get(0);
 	}
 
 	public void bufferData(int id, FloatBuffer buffer) {
 		ARBVertexBufferObject.glBindBufferARB(
 			ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, id);
 		ARBVertexBufferObject.glBufferDataARB(
 			ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, buffer,
 			ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
 		ARBVertexBufferObject.glBindBufferARB(
 			ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
 	}
 
 	public void bufferElementData(int id, IntBuffer buffer) {
 		ARBVertexBufferObject.glBindBufferARB(
 			ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, id);
 		ARBVertexBufferObject.glBufferDataARB(
 			ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, buffer,
 			ARBVertexBufferObject.GL_STATIC_DRAW_ARB);
 		ARBVertexBufferObject.glBindBufferARB(
 			ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
 	}
 
 	public void createVBO() {
 		verify();
 		
 		// if we support VBOs we need to precompute the thing now
 		// that we have normals and the model is fully loaded
 		int num_faces_all_meshes = 0;
 		int num_vertices = 0;
 		int pointIndex = 0;
 		if (meshes.size() != 0) {
 			if (hasVBO) {
 				//System.out.println("DestroyingVBO: " + modelVBOID + ":" + modelVBOindexID);
 				destroyVBO();
 			} 
 			modelVBOID = createVBOID(1);
 			modelVBOindexID = createVBOID(1);
 			//System.out.println("CreatingVBO: " + modelVBOID + ":" + modelVBOindexID);
 			
 			for (Mesh m : meshes) {
 				num_faces_all_meshes += m.getFaceCount();
 				for(Face face: m.getFaces()) {
 					if(face.getVertexCount() > num_vertices) {
 						num_vertices = face.getVertexCount();
 					}
 				}
 			}
 			vertex_buffer = BufferUtils.createFloatBuffer(num_faces_all_meshes
 				* num_vertices * 12);
 			index_buffer = BufferUtils.createIntBuffer(num_faces_all_meshes
 				* num_vertices);
 			for (Mesh m : meshes) {
 				for (Face f : m.getFaces()) {
 					vertex_buffer.put(f.createFaceBufferVNTC(m));
 					index_buffer.put(f.createIndexBufferVNTC(pointIndex));
 					pointIndex += num_vertices;
 				}
 			}
 
 			//System.out.println("NumVerts: " + index_buffer.limit() + ":" + vertex_buffer.limit());
 			
 			// NEVER FLIP AGAIN PAST THIS POINT UNLESS YOU'RE LOADING IN
 			// COMPLETELY NEW DATA
 			vertex_buffer.flip();
 			index_buffer.flip();
 
 			// Put data in allocated buffers
 			bufferData(modelVBOID, vertex_buffer);
 			bufferElementData(modelVBOindexID, index_buffer);
 
 			// Set the notifier
 			hasVBO = true;
 			// buf = BufferUtils.createFloatBuffer(16);
 			total_vertices = getVertexCount();			
 		} else {
 			System.out.println("WARNING: Tried to create VBO with no available meshes.");
 		}
 	}
 	
 	public void drawFixedPipe(Entity ent) {
 		//get number of vertices in a face
 		int num_verts = meshes.get(0).getFace(0).getVertices().size();
 		
 		//Non VBO drawing
 		/*
 		 * GL11.glPushMatrix();
 		rotateAndScaleImmediate(ent.getCollisionObject());
 
 		for (Mesh m : meshes) {
 			m.draw();
 		}
 		GL11.glPopMatrix();
 		 */	
 		
 		// http://www.solariad.com/blog/8-posts/37-preparing-an-lwjgl-application-for-opengl-core-spec
 		GL11.glPushMatrix();
 		//TODO: Uncheck cast
 		rotateAndScaleImmediate((CollisionObject)ent.getProperty("collision_object"));
 	
 		GL11.glEnable(GL11.GL_VERTEX_ARRAY);
 		GL11.glEnable(GL11.GL_NORMAL_ARRAY);
 		GL11.glEnable(GL11.GL_TEXTURE_COORD_ARRAY);
 		GL11.glEnable(GL11.GL_COLOR_ARRAY);
 
 		// Bind the index of the object
 		ARBVertexBufferObject.glBindBufferARB(
 			ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, modelVBOID);
 
 		ARBVertexBufferObject.glBindBufferARB(
 			ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, modelVBOindexID);
 
 		// vertices
 		int offset = 0 * 4; // 0 as its the first in the chunk, i.e. no offset.
 							// * 4 to convert to bytes.
 		GL11.glVertexPointer(num_verts, GL11.GL_FLOAT, Face.VERTEX_STRIDE, offset);
 
 		// normals
 		offset = 3 * 4; // 3 components is the initial offset from 0, then
 						// convert to bytes
 		GL11.glNormalPointer(GL11.GL_FLOAT, Face.VERTEX_STRIDE, offset);
 
 		// texture coordinates
 		offset = (3 + 3) * 4;
 		GL11.glTexCoordPointer(2, GL11.GL_FLOAT, Face.VERTEX_STRIDE, offset);
 
 		// colors
 		offset = (3 + 3 + 2) * 4; // (6*4) is the number of byte to skip to get
 									// to the colour chunk
 		GL11.glColorPointer(4, GL11.GL_FLOAT, Face.VERTEX_STRIDE, offset);
 
 		int first = index_buffer.get(0);
 		int last = index_buffer.get(index_buffer.limit() - 1);
 
 		/*
 		//Use this call instead of the one below if the one below isn't working
 		GL12.glDrawRangeElements(
 			GL11.GL_TRIANGLES, 
 			first, 
 			last,
 			index_buffer
 		);
 		*/
 		GL12.glDrawRangeElements(GL11.GL_TRIANGLES, first, last, total_vertices, GL11.GL_UNSIGNED_INT, 0);
 
 		GL11.glDisable(GL11.GL_VERTEX_ARRAY);
 		GL11.glDisable(GL11.GL_NORMAL_ARRAY);
 		GL11.glDisable(GL11.GL_TEXTURE_COORD_ARRAY);
 		GL11.glDisable(GL11.GL_COLOR_ARRAY);
 
 		GL11.glPopMatrix();
 	}
 
 	public void drawProgrammablePipe(Entity ent, Shader shader) {
 		//get number of vertices in a face
 		int num_verts = meshes.get(0).getFace(0).getVertices().size();
 		
 		if(shader == null) {
 			shader = this.shader;
 		}
 		if(shader != null) {
 			int vertex = GL20.glGetAttribLocation(
 				shader.getShaderID(),
 				new String("vertex")
 			);
 			int normal = GL20.glGetAttribLocation(
 				shader.getShaderID(),
 				new String("normal")
 			);
 			int texture = GL20.glGetAttribLocation(
 				shader.getShaderID(),
 				new String("texture")
 			);	
 			int color = GL20.glGetAttribLocation(
 				shader.getShaderID(),
 				new String("color")
 			);
 			
 			GL20.glEnableVertexAttribArray(vertex);
 			GL20.glEnableVertexAttribArray(normal);
 			GL20.glEnableVertexAttribArray(texture);
 			if(color != -1 ) GL20.glEnableVertexAttribArray(color);
 	
 			// Bind the index of the object
 			ARBVertexBufferObject.glBindBufferARB(
 				ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, modelVBOID);
 	
 			ARBVertexBufferObject.glBindBufferARB(
 				ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, modelVBOindexID);
 	
 			// vertices
 			long offset = 0 * 4; // 0 as its the first in the chunk, i.e. no offset.
 								// * 4 to convert to bytes.
 			GL20.glVertexAttribPointer(
 				vertex, 
 				3,
 				GL11.GL_FLOAT,
 				false,
 				Face.VERTEX_STRIDE, 
 				offset
 			);
 	
 			// normals
 			offset = 3 * 4; // 3 components is the initial offset from 0, then
 							// convert to bytes
 			GL20.glVertexAttribPointer(
 				normal, 
 				3,
 				GL11.GL_FLOAT,
 				false,
 				Face.VERTEX_STRIDE, 
 				offset
 			);
 	
 			// texture coordinates
 			offset = (3 + 3) * 4;
 			GL20.glVertexAttribPointer(
 				texture, 
 				2,
 				GL11.GL_FLOAT,
 				false,
 				Face.VERTEX_STRIDE, 
 				offset
 			);
 	
 			// colors
 			if(color != -1 ) {
 				offset = (3 + 3 + 2) * 4; // (6*4) skip to color bytes
 				GL20.glVertexAttribPointer(
 					color, 
 					4,
 					GL11.GL_FLOAT,
 					false,
 					Face.VERTEX_STRIDE, 
 					offset		
 				);
 			}	
 
 			int first = index_buffer.get(0);
 			int last = index_buffer.get(index_buffer.limit() - 1);
 	
 			shader.startShader(modelVBOID, ent);
 				if(opengl_tex != null)
 					opengl_tex.draw(shader);
 				
 				//check for wireframe rendering
 				if(wire_frame ) {
 					GL12.glDrawRangeElements(GL11.GL_LINES, first, last, total_vertices, GL11.GL_UNSIGNED_INT, 0);
 				} else {
 					if(num_verts == 3) 
 						GL12.glDrawRangeElements(GL11.GL_TRIANGLES, first, last, total_vertices, GL11.GL_UNSIGNED_INT, 0);
 					else if(num_verts == 4)
 						GL12.glDrawRangeElements(GL11.GL_QUADS, first, last, total_vertices, GL11.GL_UNSIGNED_INT, 0);
 				}
 				if(opengl_tex != null)
 					opengl_tex.unbind();
 			shader.stopShader();
 		
 			ARBVertexBufferObject.glBindBufferARB(
 				ARBVertexBufferObject.GL_ARRAY_BUFFER_ARB, 0);
 			ARBVertexBufferObject.glBindBufferARB(
 				ARBVertexBufferObject.GL_ELEMENT_ARRAY_BUFFER_ARB, 0);
 			
 			if(color != -1 ) GL20.glDisableVertexAttribArray(color);
 			GL20.glDisableVertexAttribArray(texture);
 			GL20.glDisableVertexAttribArray(normal);
 			GL20.glDisableVertexAttribArray(vertex);
 			
 			//if there is an error
 			if(GL20.glGetProgrami(shader.getShaderID(), GL20.GL_LINK_STATUS)!=GL11.GL_TRUE) {
 				//find out how large it is and print
 				int maxLength = GL20.glGetProgrami(shader.getShaderID(), GL20.GL_LINK_STATUS) + 1;
 				System.out.println("length: " + maxLength + " " + GL20.glGetProgramInfoLog(shader.getShaderID(), maxLength));
 			}
 		} else {
 			System.out.println("Missing shader [rendering direct]:\nmodelVBOID:"+modelVBOID+" modelVBOIDXID:"+modelVBOindexID+"\n");
 		}
 	}
 	
 	public void drawProgrammablePipe(Entity ent) {
 		drawProgrammablePipe(ent,null);
 	}
 
 	public int getVertexCount() {
 		int count = 0;
 		for(Mesh m : this.getMeshes())
 			for(Face f : m.getFaces())
 				count += f.getVertexCount();
 		return count;
 	}
 
 	public void destroyVBO() {
 		ARBVertexBufferObject.glDeleteBuffersARB(modelVBOID);
 		ARBVertexBufferObject.glDeleteBuffersARB(modelVBOindexID);
 		hasVBO = false;
 	}
 	// *****************END VBO METHODS***********************
 	
 	public void reduceHull() {
 		ObjectArrayList<Vector3f> vertices = new ObjectArrayList<Vector3f>();
 		
 		//System.out.println("\n\n\nHURR");
 		for(Mesh m : this.getMeshes()){
 			for(Face f : m.getFaces()){
 				for(Vector3f v : f.getVertices()){
 					Vector3f translated_verts = new Vector3f();
 					translated_verts.add(v, m.getTransform());
 					vertices.add(translated_verts);
 					//System.out.println(translated_verts);
 				}
 			}
 		}
 		//System.out.println("###HURR\n\n\n");
 		ConvexHullShape cvs = new ConvexHullShape(vertices);
 		shape = cvs;
 	}
 	
 	public CollisionShape getCollisionShape() {
 		if(null == shape){
 			this.reduceHull();
 		}
 		return shape;
 	}
 	
 	public void setTransparent(){
 		//Vector3f zero = new Vector3f(0,0,0);
 		for(Mesh m : this.getMeshes()){
 			//m.setMaterial(new Material(zero,zero,zero,zero,0f,0f));
 			m.getMaterial().setAlpha(0.0f);
 		}
 	}
 	
 	public void setTexture(Texture texture) {
 		this.opengl_tex = texture;  
 	}
 	
 	public void setCollisionShape(CollisionShape shape) {
 		this.shape = shape;		
 	}
 
 	public Shader getShader() {
 		return shader;
 	}
 	
 	public void setShader(Shader shader) {
 		this.shader = shader;
 	}
 	
 	public void combineModels(Model model) {
 		for(Mesh mesh: model.getMeshes()) {
 			meshes.add(mesh);
 		}
 		//num_vertices=getVertexCount();
 		if(hasVBO){
 			destroyVBO();
 		}
 		createVBO();
 	}
 	
 	public void setWireFrame(boolean enabled) {
 		wire_frame = enabled;
 	}
 
 	@Override
 	public String toXML() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void loadFromFile(ResourceManager resource_manager, InputStream is, String extension) throws Exception {
 		this.combineModels(FileLoader.loadFile(is, extension));
 		Shader shader = (Shader)resource_manager.getResource("default", "shaders");
 		this.setShader(shader);
 	}
 }
