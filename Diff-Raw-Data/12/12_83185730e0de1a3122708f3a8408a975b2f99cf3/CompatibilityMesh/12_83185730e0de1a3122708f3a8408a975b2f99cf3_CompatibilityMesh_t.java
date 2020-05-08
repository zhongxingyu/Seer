 package graphics.compatibility;
 
 import static org.lwjgl.opengl.GL11.GL_FLOAT;
 import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
 import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
 import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
 import static org.lwjgl.opengl.GL11.glBindTexture;
 import static org.lwjgl.opengl.GL11.glDeleteTextures;
 import static org.lwjgl.opengl.GL11.glDrawElements;
 import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
 import static org.lwjgl.opengl.GL13.glActiveTexture;
 import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
 import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
 import static org.lwjgl.opengl.GL15.glBindBuffer;
 import static org.lwjgl.opengl.GL15.glBufferData;
 import static org.lwjgl.opengl.GL15.glDeleteBuffers;
 import static org.lwjgl.opengl.GL15.glGenBuffers;
 import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
 import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
 import static org.lwjgl.opengl.GL30.glBindVertexArray;
 import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
 import static org.lwjgl.opengl.GL30.glGenVertexArrays;
 import graphics.GLOperations;
 import graphics.Shader;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.HashMap;
 
 import loader.ColladaLoader;
 
 public class CompatibilityMesh{
 
 	private int vao;
 	
 	private int elements;
 	private int elementCount;
 	
 	
 	private int positionVbo;
 	private int normalVbo;
 	private int texCoordVbo;
 	
 	private int textureId;
 	
 	private int positionAttribute;
 	private int normalAttribute;
 	private int texCoordAttribute;
 	
 	public CompatibilityMesh(Shader shader)
 	{
 		
 		this.positionAttribute = shader.getAttributes().get("position");
 		this.normalAttribute = shader.getAttributes().get("normal");
 		this.texCoordAttribute = shader.getAttributes().get("texCoord");
 		
 		textureId = GLOperations.loadTexture("temp/debug.png");
 		
 		vao = glGenVertexArrays();
 		positionVbo = glGenBuffers();
 		normalVbo = glGenBuffers();
 		texCoordVbo = glGenBuffers();
 		elements = glGenBuffers();
 		
 		glBindVertexArray(vao);
 		
		HashMap<String, float[]> modelData = ColladaLoader.load("temp/werm.dae");
 		//ObjectLoader.load("temp/object.obj");
 		
 		
 		FloatBuffer vertexBuff = GLOperations.generateFloatBuffer(modelData.get("positions"));
 		FloatBuffer normalBuff = GLOperations.generateFloatBuffer(modelData.get("normals"));
 		FloatBuffer texCoordBuff = GLOperations.generateFloatBuffer(modelData.get("texCoords"));
 		
 //		FloatBuffer vertexBuff = GLOperations.generateFloatBuffer(modelData.get("Cylinder-mesh-positions-array"));
 //		FloatBuffer normalBuff = GLOperations.generateFloatBuffer(modelData.get("Cylinder-mesh-normals-array"));
 //		FloatBuffer texCoordBuff = GLOperations.generateFloatBuffer(modelData.get("Cylinder-mesh-map-0-array"));
 		
 		
 		
 		//get elements array
 		float[] fElems = modelData.get("elements");
 		int[] elems = new int[fElems.length];
 		
 		for(int i = 0; i < fElems.length; i++){
 			elems[i] = (int)fElems[i];
 		}
 		
 		this.elementCount = elems.length;
 		IntBuffer elementBuff = GLOperations.generateIntBuffer(elems);
 		
 		
 		//bind and buffer data
 		glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
         glBufferData(GL_ARRAY_BUFFER, vertexBuff , GL_STATIC_DRAW);
         
 		glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
         glBufferData(GL_ARRAY_BUFFER, normalBuff , GL_STATIC_DRAW);
         
         
         glBindBuffer(GL_ARRAY_BUFFER, texCoordVbo);
         glBufferData(GL_ARRAY_BUFFER, texCoordBuff, GL_STATIC_DRAW);
         
         glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, elements);
         glBufferData(GL_ELEMENT_ARRAY_BUFFER, elementBuff, GL_STATIC_DRAW);
 		
 		
         //enable vertex attrib ptr
 		glBindBuffer(GL_ARRAY_BUFFER, positionVbo);
         glVertexAttribPointer( positionAttribute, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(positionAttribute);
 		
         glBindBuffer(GL_ARRAY_BUFFER, normalVbo);
         glVertexAttribPointer( normalAttribute, 3, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(normalAttribute);
 
         glBindBuffer(GL_ARRAY_BUFFER, texCoordVbo);
         glVertexAttribPointer( texCoordAttribute, 2, GL_FLOAT, false, 0, 0);
         glEnableVertexAttribArray(texCoordAttribute);
 
         //glunbind buffer
 	}
 	
 	public void draw() {
 	
 		glActiveTexture(GL_TEXTURE0);
         glBindTexture(GL_TEXTURE_2D, 1);
 		//bind
 		glBindVertexArray(vao);
 
         glDrawElements(GL_TRIANGLES, elementCount, GL_UNSIGNED_INT, 0);
 
         glBindVertexArray(0);
 	}
 	
 	public void delete() {
 		
 		glDeleteBuffers(this.elements);
 		glDeleteBuffers(this.positionVbo);
 		glDeleteBuffers(this.normalVbo);
 		glDeleteBuffers(this.texCoordVbo);
 		
 		glDeleteTextures(this.textureId);
 		
 		
 		glDeleteVertexArrays(this.vao);
 		
 		glBindVertexArray(0);
 		
 	}
 
 	
 	
 }
