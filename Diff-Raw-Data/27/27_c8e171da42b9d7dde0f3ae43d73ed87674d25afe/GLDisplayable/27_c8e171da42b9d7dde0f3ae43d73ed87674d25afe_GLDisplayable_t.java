 package openGL.gl;
 
 import java.io.File;
 import java.util.LinkedList;
 
 import openGL.objects.Transformation;
 
 
 /**
  * A GLDisplayable manages all the object specific data OpenGL needs for rendering.
  * 
  * <p>This includes: <br>
  * <li>Handle for the memory on the GPU (Vertex Array Object) </li>
  * <li>Handle for the compiled GLSL shader programm </li>
  * <li> A set of Data arrays that are sent to the gpu, with a description 
  * how they are laid out (indices and Vertex Elements)</li>
  *  
  * </p>
  * 
  * <p>To render an object stored in some arbitrary datastructure,
  * write a wrapper for this data structure which extends this class, see GLWireframeMesh for an example.
  * <li> Create data arrays </li>
  * <li> Use the addElement(...) and addIndices(...) methods to pass data arrays to the gpu </li>
  * <li> Overwrite glRenderFlag() which is a callback function called to get 
  * the appropriate Render flag for the wrapped data(GL.GL_POINTS /GL.GL_TRIANGLES etc)</li>
  * <li> Overwrite addUniform(..) which is a callback function called in each render pass
  * and can be used to load data dependent uniform variables </li>
  * <li> Write /reuse appropriate GLSL shader files and configure opengl to use them using
  *  the configurePreferredShader method</li>
  * </p>
  * 
  * 
  */
 public abstract class GLDisplayable {
 
 	/**
 	 * The number of vertices
 	 */
 	private int n;
 
 	/**
 	 * Indices into the vertex data to specify the triangles / lines / points passed
 	 * to the shader. For example for triangles,  the first three indices define the three vertices of the first
 	 * triangle, the second three indices the second triangle, etc.
 	 * For lines, the indices describe the sequence in which the vertices should
 	 * be traversed.
 	 * For points, typically the index array is just [0,1,2,3,...,numberofpoints].
 	 */
 	private int[] indices;
 
 	/**
 	 * A list of the data arrays that store the vertex attributes.
 	 * The Vertex elements store the semantic, layout and name of the
 	 * data arrays that are going to be sent to the GPU.
 	 * 
 	 * VertexElements are mapped to OpenGL buffers.
 	 */
 	private LinkedList<VertexElement> vertexElements;
 
 	/**
 	 * The handle to the OpenGL VAO of this data. The VAO
 	 * is the handle for the data on the GPU.
 	 */
 	protected GLVertexArrayObject vertexArrayObject;
 
 	/**
 	 * The GLShader manages the compiled, loaded Shader programm
 	 * that runs on the GPU.
 	 */
 	GLShader shader = null;
 	
 	/**
 	 * The Files this object is configure to load the GLSL shader from.
 	 */
 	private String vert_shader_file;
 	private String frag_shader_file;
 	private String geom_shader_file;
 	
 
 	/**
 	 * Vertex data consists of a list of vertex elements, and an index array.
 	 * The index array contains indices into the vertex data. The indices
 	 * specify how vertices are connected elementary OpenGL types 
 	 * (lines, triangles, etc). 
 	 * 
 	 * @param n
 	 *            the number of vertices.
 	 */
 	public GLDisplayable(int n) {
 		this.n = n;
 		vertexArrayObject = null;
 		indices = new int[0];
 		vertexElements = new LinkedList<VertexElement>();
 	}
 
 	public int getNumberOfVertices() {
 		return n;
 	}
 
 	public void addElement(float[] f, Semantic s, int i) {
 		if (f.length == n * i) {
 			VertexElement vertexElement = new VertexElement();
 			vertexElement.data = f;
 			vertexElement.semantic = s;
 			vertexElement.nComponents = i;
 
 			// Make sure POSITION is the last element in the list. This
 			// guarantees
 			// that rendering works as expected (i.e., vertex attributes are set
 			// before the vertex is rendered).
 			if (s == Semantic.POSITION) {
 				vertexElements.addLast(vertexElement);
 			} else {
 				vertexElements.addFirst(vertexElement);
 			}
 		} else {
 			System.err
 					.println("Array of '"
 							+ s.name()
 							+ "' has not the correct dimension (must be number of vertices times i).\n"
 							+ "No elements for " + s.name()
 							+ " have been added so far.");
 		}
 	}
 	
 	public void addElement(float[] f, Semantic s, int i, String name) {
 		if (f.length == n * i) {
 			VertexElement vertexElement = new VertexElement();
 			vertexElement.data = f;
 			vertexElement.semantic = s;
 			vertexElement.nComponents = i;
 			vertexElement.glName = name;
 
 			// Make sure POSITION is the last element in the list. This
 			// guarantees
 			// that rendering works as expected (i.e., vertex attributes are set
 			// before the vertex is rendered).
 			if (s == Semantic.POSITION) {
 				vertexElements.addLast(vertexElement);
 			} else {
 				vertexElements.addFirst(vertexElement);
 			}
 		} else {
 			System.err
 					.println("Array of '"
 							+ s.name()
 							+ "' has not the correct dimension (must be number of vertices times i).\n"
 							+ "No elements for " + s.name()
 							+ " have been added so far.");
 		}
 	}
 
 	public void addIndices(int[] indices) {
 		this.indices = indices;
 	}
 
 	public LinkedList<VertexElement> getElements() {
 		return vertexElements;
 	}
 
 	public int[] getIndices() {
 		return indices;
 	}
 
 	public GLVertexArrayObject getVAO() {
 		return vertexArrayObject;
 	}
 
 	public void setVAO(GLVertexArrayObject vertexArrayObject) {
 		this.vertexArrayObject = vertexArrayObject;
 	}
 
 	/**
 	 * Set what glsl shaders should be used for rendering
 	 * @param vert_shader
 	 * @param frag_shader
 	 */
 	public void configurePreferredShader(String vert_shader, String frag_shader,
 			String geom_shader) {
 				this.vert_shader_file = vert_shader;
 				this.frag_shader_file = frag_shader;
 				this.geom_shader_file = geom_shader;
 			}
 
 	/**
 	 * This method will be called during rendering.
 	 * @param gl3
 	 */
 	public void loadPreferredShader(GLRenderer gl3) {
 		if(frag_shader_file == null){
 			gl3.useDefaultShader();
 		}
 		else if(shader == null){
 			shader = (GLShader) gl3.makeShader();
 			try {
 				if(geom_shader_file != null){
 					shader.load(vert_shader_file, frag_shader_file, geom_shader_file);
 				}
 				else{
 					shader.load(vert_shader_file, frag_shader_file);
 				}
 				
 			} catch (Exception e) {
 				System.out.print("Problem with shader:\n");
 				shader = null;
 				System.out.print(e.getMessage());
 			}
 		}
 		
 		if(shader == null){
 			gl3.useDefaultShader();
 		}
 		else{
 			gl3.useShader(shader);
 		}
 	}
 
 	/**
 	 * Overwrite this method to specify the glRender flag. This decides
 	 * if the Vertex and geometry shader receive triangles (return GL.GL_TRIANGLES),
 	 * lines (return GL.GL_LINES) or points (return GL.GL_POINTS)
 	 * @return
 	 */
 	public abstract int glRenderFlag();
 
 	/**
 	 * Overwrite this to pass additional uniforms to the shaders, 
 	 * using the method GLRenderContext.loadUniform(...).
 	 * @param glRenderContext
 	 */
 	public abstract void loadAdditionalUniforms(GLRenderer glRenderContext, Transformation mvMat);
 
 	/**
 	 * A vertex element is an array of floats that stores vertex attributes,
 	 * like positions, normals, or texture coordinates. The element stores the
 	 * data values, its semantic, and the number of components per item (for
 	 * example, a homogeneous vector has four components, or RGB colors have
 	 * three components).
 	 */
 	public class VertexElement {
 
 		private float[] data;
 		private Semantic semantic;
 		private int nComponents;
 		String glName;
 
 		public float[] getData() {
 			return data;
 		}
 
 		public Semantic getSemantic() {
 			return semantic;
 		}
 
 		public int getNumberOfComponents() {
 			return nComponents;
 		}
 		
 		public String getGLName(){
 			return glName;
 		}
 
 	}
 
 	/**
 	 * Vertex data semantic can be position, normal, texture or color
 	 * coordinates.
 	 */
 	public enum Semantic {
 		POSITION, USERSPECIFIED
 	}
 	
 	public String toString() {
		String name = new File(vert_shader_file).getName() + " " +
 				new File(frag_shader_file).getName();
		if (geom_shader_file != null)
			name += " " + new File(geom_shader_file).getName();
		return name;
 	}
 	
 
 }
