 package graphics.compatibility;
 
 import static org.lwjgl.opengl.GL11.GL_FLOAT;
 import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
 import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
 import static org.lwjgl.opengl.GL11.glDrawElements;
 import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
 import static org.lwjgl.opengl.GL15.glBindBuffer;
 import static org.lwjgl.opengl.GL15.glBufferData;
 import static org.lwjgl.opengl.GL15.glGenBuffers;
 
 import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
 import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
 import static org.lwjgl.opengl.GL20.glUniform3f;
 import static org.lwjgl.opengl.GL20.glUniformMatrix4;
 import static org.lwjgl.opengl.GL30.glBindVertexArray;
 import static org.lwjgl.opengl.GL30.glGenVertexArrays;
 import graphics.GLOperations;
 import graphics.Mesh;
 import graphics.Shader;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import org.lwjgl.util.vector.Matrix4f;
 import org.lwjgl.util.vector.Vector3f;
 
 public class CompatibilityMesh implements Mesh {
 
 	private int vao;
 	
 	private int elements;
 	private int elementCount;
 	
 	
 	private int positionVbo;
 	private int normalVbo;
 	
 	private int colorUniform;
 	
 	private int positionAttribute;
 	private int normalAttribute;
 	
 	private Matrix4f model;
 	private int modelUniform;
 	
 	private float[] col;
 	
 	
 	private static float offset = 0.0f;
 
 	public CompatibilityMesh(Shader shader)
 	{
 		
 		this.colorUniform = shader.getUniforms().get("color");
 		this.modelUniform = shader.getUniforms().get("model");
 		this.positionAttribute = shader.getAttributes().get("position");
 		this.normalAttribute = shader.getAttributes().get("normal");
 		
 		this.col = new float[3];
 		
 		col[0] = (offset / 0.2f) % 4 * 0.25f;
 		col[1] = (1 + offset / 0.2f) % 4 * 0.25f;
 		col[2] = (2 + offset / 0.2f) % 4 * 0.25f;
 		
 		
 		vao = glGenVertexArrays();
 		positionVbo = glGenBuffers();
 		normalVbo = glGenBuffers();
 		elements = glGenBuffers();
 		
 		glBindVertexArray(vao);
 		
 		this.model = new Matrix4f();
 		model.translate(new Vector3f(0.0f, 0.0f, -40 + offset));
 		offset += 0.2f;
 		
 		float verts[] = {
 				-1.0f, -1.0f, 0.f,
 				-1.0f, 1.0f, 0.f,
 				1.0f, 1.0f, 0.f,
 				1.0f, -1.0f, 0.f,
 		};
 		FloatBuffer vertexBuff = GLOperations.generateFloatBuffer(verts);
 		glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
         glBufferData(GL_ARRAY_BUFFER, vertexBuff , GL_STATIC_DRAW);
         
         float normals[] = {
				0.f, 1.0f, 0.f,
				0.f, 1.0f, 0.f,
				0.f, 1.0f, 0.f,
				0.f, 1.0f, 0.f,
 		};
 		FloatBuffer normalBuff = GLOperations.generateFloatBuffer(normals);
 		glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
         glBufferData(GL_ARRAY_BUFFER, normalBuff , GL_STATIC_DRAW);
        
         
         
         //element buffer
         int elems[] = {0, 1, 2, 0, 2, 3};
 		IntBuffer elementBuff = GLOperations.generateIntBuffer(elems);
 		this.elementCount = elems.length;
 		
 		
 		//bind and buffer data
 		glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
         glVertexAttribPointer( positionAttribute, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(positionAttribute);
 		
         glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
         glVertexAttribPointer( normalAttribute, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(normalAttribute);
 		
        
 
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elements);
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuff, GL_STATIC_DRAW);
 
         
 	}
 	
 	public void draw() {
 		//set uniforms
 		float temp = col[0];
 		col[0] = col[0] * 0.9999f + col[1] * 0.0001f;
 		col[1] = col[1] * 0.9999f + col[2] * 0.0001f;
 		col[2] = col[2] * 0.9999f + temp * 0.0001f;
 		
 		
 		glUniform3f(colorUniform, col[0], col[1], col[2]);
 		
 		glUniformMatrix4(modelUniform, false, GLOperations.generateFloatBuffer(model));		
 		//bind
 		glBindVertexArray(vao);
 
         glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0);
 
         glBindVertexArray(0);
 	}
 	
 
 	
 	
 }
