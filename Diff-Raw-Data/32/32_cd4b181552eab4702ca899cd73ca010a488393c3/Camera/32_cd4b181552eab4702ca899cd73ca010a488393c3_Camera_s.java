 package grige;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL2;
 import javax.media.opengl.GL2ES2;
 
 import com.jogamp.opengl.FBObject;
 
 import com.jogamp.opengl.FBObject.TextureAttachment;
 import com.jogamp.opengl.math.FloatUtil;
 
 import com.jogamp.opengl.util.texture.Texture;
 
 import com.jogamp.opengl.util.glsl.ShaderCode;
 import com.jogamp.opengl.util.glsl.ShaderProgram;
 
 public class Camera {	
 	
 	private final float[] screenCanvasVertices = {
 			-1.0f, -1.0f, 0.0f,
 			-1.0f, 1.0f, 0.0f,
 			1.0f, -1.0f, 0.0f,
 			1.0f,  1.0f, 0.0f,
 	};
 	
 	private final float[] screenCanvasTextureCoords = {
 			0.0f, 0.0f,
 			0.0f, 1.0f,
 			1.0f, 0.0f,
 			1.0f, 1.0f,
 	};
 	
 	private final int[] screenCanvasIndices = {
 			0, 1, 2, 3,
 	};
 	
 	private final float[] quadVertices = {
 			-0.5f, -0.5f, 0.0f,
 			-0.5f, 0.5f, 0.0f,
 			0.5f, -0.5f, 0.0f,
 			0.5f,  0.5f, 0.0f,	
 	};
 	
 	private final float[] quadTextureCoords = {
 			0.0f, 0.0f,
 			0.0f, 1.0f,
 			1.0f, 0.0f,
 			1.0f, 1.0f,
 	};
 	
 	private final float[] quadTintColours = {
 			1f, 1f, 1f, 1f,
 			1f, 1f, 1f, 1f,
 			1f, 1f, 1f, 0f,
 			1f, 1f, 1f, 0f,
 	};
 	
 	private final int[] quadIndices = {
 			0, 1, 2, 3,
 	};
 	
 	private final float[] fanVertices = generateTriangleFanVertices(32);
 	private final float[] fanColours = generateTriangleFanColours(fanVertices.length);
 	
 	//Camera attributes
 	private int x;
 	private int y;
 	private int width;
 	private int height;
 	private int depth;
 	
 	//Current transformation matrices
 	private float[] projectionMatrix;
 	private float[] viewingMatrix;
 	
 	//Vertex Buffers
 	private int screenCanvasVAO;
 	private int geometryVAO;
 	private int lightingVAO;
 	
 	//Frame Buffers
 	private FBObject geometryFBO;
 	private int geometryFBOTexture;
 	
 	private FBObject lightingFBO;
 	private int lightingFBOTexture;
 	
 	//Shader Data
 	private ShaderProgram screenCanvasShader;
 	private ShaderProgram geometryShader;
 	private ShaderProgram lightingShader;
 	
 	//GL context
 	private GL2 gl;
 	
 	public Camera(int startWidth, int startHeight, int startDepth)
 	{
 		setSize(startWidth,startHeight,startDepth);
 		setPosition(0,0);
 	}
 	
 	public void setPosition(int newX, int newY)
 	{
 		x = newX;
 		y = newY;
 		
 		viewingMatrix = new float[]{1,0,0,0, 0,1,0,0, 0,0,1,0, -x-width/2f,-y-height/2f,0,1f};
 	}
 	
 	public void setSize(int newWidth, int newHeight, int newDepth)
 	{
 		width = newWidth;
 		height = newHeight;
 		depth = newDepth;
 		
 		projectionMatrix = new float[]{2f/width,0,0,0, 0,2f/height,0,0, 0,0,-2f/depth,0, 0,0,-1,1};
 		setPosition(x,y); //Update the viewing matrix as well, because the size has changed (so we need to translate (0,0) differently)
 	}
 	
 	public int getX() { return x; }
 	public int getY() { return y; }
 	public int getWidth() { return width; }
 	public int getHeight() { return height; }
 	public int getDepth() { return depth; }
 	
 	protected void initialize(GL glContext)
 	{
 		gl = glContext.getGL2();
 		
 		//Set rendering properties
 		gl.glDisable(GL.GL_CULL_FACE);
 		//gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 		
 		initializeGeometryData();
 		initializeLightingData();
 		initializeScreenCanvas();
 	}
 
 	private void initializeGeometryData()
 	{
 		//Create the framebuffer
 		geometryFBO = new FBObject();
 		geometryFBO.reset(gl, width, height);
 		geometryFBO.attachTexture2D(gl, 0, true);
 		geometryFBO.unbind(gl);
 		
 		//Load the shader
 		geometryShader = loadShader("SimpleVertexShader.vsh", "SimpleFragmentShader.fsh");
 		geometryShader.useProgram(gl, true);
 		
 		int[] buffers = new int[4];
 		
 		//Create the vertex array
 		gl.glGenVertexArrays(1, buffers, 0);
 		geometryVAO = buffers[0];
 		gl.glBindVertexArray(geometryVAO);
 		
 		//Generate and store the required buffers
 		gl.glGenBuffers(4, buffers,0); //Indices, VertexLocations, TextureCoordinates
 		int indexBuffer = buffers[0];
 		int vertexBuffer = buffers[1];
 		int texCoordBuffer = buffers[2];
 		int colourBuffer = buffers[3];
 		
 		//Buffer the vertex indices
 		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, quadIndices.length*(Integer.SIZE/8), IntBuffer.wrap(quadIndices), GL.GL_STATIC_DRAW);
 		
 		//Buffer the vertex locations
 		int positionIndex = gl.glGetAttribLocation(geometryShader.program(), "position");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, quadVertices.length*(Float.SIZE/8), FloatBuffer.wrap(quadVertices), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(positionIndex);
 		gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 0, 0);
 		
 		//Buffer the vertex texture coordinates
 		int texCoordIndex = gl.glGetAttribLocation(geometryShader.program(), "texCoord");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texCoordBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, quadTextureCoords.length*(Float.SIZE/8), FloatBuffer.wrap(quadTextureCoords), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(texCoordIndex);
 		gl.glVertexAttribPointer(texCoordIndex, 2, GL.GL_FLOAT, false, 0, 0);
 		
 		//Buffer the tint colour
 		int colourIndex = gl.glGetAttribLocation(geometryShader.program(), "tintColour");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, colourBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, quadTintColours.length*(Float.SIZE/8), FloatBuffer.wrap(quadTintColours), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(colourIndex);
 		gl.glVertexAttribPointer(colourIndex, 4, GL.GL_FLOAT, false, 0, 0);
 		
 		//Projection matrices
 		int projMatrixIndex = gl.glGetUniformLocation(geometryShader.program(), "projectionMatrix");
 		gl.glUniformMatrix4fv(projMatrixIndex, 1, false, projectionMatrix, 0);
 		
 		int viewMatrixIndex = gl.glGetUniformLocation(geometryShader.program(), "viewingMatrix");
 		gl.glUniformMatrix4fv(viewMatrixIndex, 1, false, viewingMatrix, 0);
 		
 		geometryShader.useProgram(gl, false);
 	}
 	
 	private void initializeLightingData()
 	{
 		//Create the framebuffer
 		lightingFBO = new FBObject();
 		lightingFBO.reset(gl, width, height);
 		lightingFBO.attachTexture2D(gl, 0, true);
 		lightingFBO.unbind(gl);
 		
 		//Load and bind the shader
 		lightingShader = loadShader("LightVertexShader.vsh", "LightFragmentShader.fsh");
 		lightingShader.useProgram(gl, true);
 		
 		int[] buffers = new int[2];
 		//Create the vertex array
 		gl.glGenVertexArrays(1, buffers, 0);
 		lightingVAO = buffers[0];
 		gl.glBindVertexArray(lightingVAO);
 		
 		gl.glGenBuffers(2, buffers ,0);
 		int vertexBuffer = buffers[0];
 		int colourBuffer = buffers[1];
 		
 		int positionIndex = gl.glGetAttribLocation(lightingShader.program(), "position");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, fanVertices.length*(Float.SIZE/8), FloatBuffer.wrap(fanVertices),GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(positionIndex);
 		gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 0, 0);
 		
 		int colourIndex = gl.glGetAttribLocation(lightingShader.program(), "vertColour");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, colourBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, fanColours.length*(Float.SIZE/8), FloatBuffer.wrap(fanColours), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(colourIndex);
 		gl.glVertexAttribPointer(colourIndex, 4, GL.GL_FLOAT, false, 0, 0);
 		
 		//Projection matrices
 		int projMatrixIndex = gl.glGetUniformLocation(lightingShader.program(), "projectionMatrix");
 		gl.glUniformMatrix4fv(projMatrixIndex, 1, false, projectionMatrix, 0);
 		
 		int viewMatrixIndex = gl.glGetUniformLocation(lightingShader.program(), "viewingMatrix");
 		gl.glUniformMatrix4fv(viewMatrixIndex, 1, false, viewingMatrix, 0);
 		
 		lightingShader.useProgram(gl, false);
 	}
 
 	private void initializeScreenCanvas()
 	{
 		//Load the shader
 		screenCanvasShader = loadShader("Canvas.vsh", "Canvas.fsh");
 		screenCanvasShader.useProgram(gl, true);
 		
 		int[] buffers = new int[4];
 		
 		//Create the vertex array
 		gl.glGenVertexArrays(1, buffers, 0);
 		screenCanvasVAO = buffers[0];
 		gl.glBindVertexArray(screenCanvasVAO);
 		
 		//Generate and store the required buffers
 		gl.glGenBuffers(4, buffers,0); //Indices, VertexLocations, TextureCoordinates
 		int indexBuffer = buffers[0];
 		int vertexBuffer = buffers[1];
 		int texCoordBuffer = buffers[2];
 		
 		//Buffer the vertex indices
 		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, screenCanvasIndices.length*(Integer.SIZE/8), IntBuffer.wrap(screenCanvasIndices), GL.GL_STATIC_DRAW);
 		
 		//Buffer the vertex locations
 		int positionIndex = gl.glGetAttribLocation(screenCanvasShader.program(), "position");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, screenCanvasVertices.length*(Float.SIZE/8), FloatBuffer.wrap(screenCanvasVertices), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(positionIndex);
 		gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 0, 0);
 		
 		//Buffer the vertex texture coordinates
 		int texCoordIndex = gl.glGetAttribLocation(screenCanvasShader.program(), "texCoord");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, texCoordBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, screenCanvasTextureCoords.length*(Float.SIZE/8), FloatBuffer.wrap(screenCanvasTextureCoords), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(texCoordIndex);
 		gl.glVertexAttribPointer(texCoordIndex, 2, GL.GL_FLOAT, false, 0, 0);
 		
 		//Get the textures used by the geometry and lighting individually
 		TextureAttachment geometryTexture = (TextureAttachment) geometryFBO.getColorbuffer(0);
 		geometryFBOTexture = geometryTexture.getName();
 		
 		TextureAttachment lightingTexture = (TextureAttachment) lightingFBO.getColorbuffer(0);
 		geometryFBOTexture = lightingTexture.getName();
 		
         int geometryTextureSamplerIndex = gl.glGetUniformLocation(screenCanvasShader.program(), "geometryTextureUnit");
 		gl.glUniform1f(geometryTextureSamplerIndex, 0);
 		int lightingTextureSamplerIndex = gl.glGetUniformLocation(screenCanvasShader.program(), "lightingTextureUnit");
 		gl.glUniform1f(lightingTextureSamplerIndex, 1);
 		
 		screenCanvasShader.useProgram(gl, false);
 	}
 	
 	public void refresh()
 	{
 		//Clear the screen
 		//gl.glDepthMask(true);
 		//gl.glClearDepth(1);
		gl.glClearColor(1, 0f, 0, 1);
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 	}
 	
 	public void drawLight(Light light)
 	{
 		//Compute the object transform matrix
 		float lightSize = 32*light.scale;
 		
 		float[] objectTransformMatrix = new float[]{
 				lightSize,0,0,0,
 				0,lightSize,0,0,
 				0,0,1,0,
 				light.x, light.y, -light.depth, 1
 		};
 		
 		//Draw the light
 		//lightingShader.useProgram(gl, true);
 		gl.glBindVertexArray(lightingVAO);
 		
 		int lightObjTransformIndex = gl.glGetUniformLocation(lightingShader.program(), "objectTransform");
 		gl.glUniformMatrix4fv(lightObjTransformIndex, 1, false, objectTransformMatrix, 0);
 		
 		gl.glDrawArrays(GL.GL_TRIANGLE_FAN, 0, fanVertices.length);
 		//lightingShader.useProgram(gl, false);
 	}
 	
 	public void drawObject(Drawable object)
 	{
 		//Compute the object transform matrix
 		float objWidth = object.getTexture().getWidth()*object.scale;
 		float objHeight = object.getTexture().getHeight()*object.scale;
 		float rotationRadians = object.rotation*FloatUtil.PI/180;
 		
 		float[] objectTransformMatrix = new float[]{
 				objWidth*FloatUtil.cos(rotationRadians),-objHeight*FloatUtil.sin(rotationRadians),0,0,
 				objWidth*FloatUtil.sin(rotationRadians), objHeight*FloatUtil.cos(rotationRadians),0,0,
 				0,0,1,0,
 				object.x, object.y, -object.depth, 1
 		};
 		
 		//Draw geometry
 		if(object.getTexture() == null)
 			return;
 		
 		geometryFBO.bind(gl);
 		geometryShader.useProgram(gl, true);
 		gl.glActiveTexture(GL.GL_TEXTURE0);
 		gl.glBindVertexArray(geometryVAO);
 		
 		//Texture specification
 		int textureSamplerIndex = gl.glGetUniformLocation(geometryShader.program(), "textureUnit");
 		gl.glUniform1f(textureSamplerIndex, 0);
 		
 		int geometryObjTransformIndex = gl.glGetUniformLocation(geometryShader.program(), "objectTransform");
 		gl.glUniformMatrix4fv(geometryObjTransformIndex, 1, false, objectTransformMatrix, 0);
 		
 		Texture objTex = object.getTexture();
 		objTex.enable(gl);
 		objTex.bind(gl);
 		
 		gl.glDrawElements(GL.GL_TRIANGLE_STRIP, quadIndices.length, GL.GL_UNSIGNED_INT, 0);
 		
 		objTex.disable(gl);
 		geometryShader.useProgram(gl, false);
 		geometryFBO.unbind(gl);
 	}
 	
 	public void commitDraw()
 	{
 		screenCanvasShader.useProgram(gl, true);
		
 		gl.glBindVertexArray(screenCanvasVAO);
 		
		/*gl.glActiveTexture(GL.GL_TEXTURE0);
		gl.glBindTexture(GL.GL_TEXTURE_2D, geometryFBOTexture);
 		
 		gl.glActiveTexture(GL.GL_TEXTURE1);
		gl.glBindTexture(GL.GL_TEXTURE_2D, lightingFBOTexture);*/
 		
 		gl.glDrawElements(GL.GL_TRIANGLE_STRIP, screenCanvasIndices.length, GL.GL_UNSIGNED_INT, 0);
 		
 		screenCanvasShader.useProgram(gl, false);
 	}
 	
 	//Initialization utility functions
 	private ShaderProgram loadShader(String vertexShader, String fragmentShader)
 	{
 		GL2ES2 gl = this.gl.getGL2ES2();
 		
 		ShaderCode vertShader = ShaderCode.create(gl, GL2ES2.GL_VERTEX_SHADER, 1, getClass(), new String[]{"/shaders/"+vertexShader},false);
 		vertShader.compile(gl);
 		
 		ShaderCode fragShader = ShaderCode.create(gl, GL2ES2.GL_FRAGMENT_SHADER, 1, getClass(), new String[]{"/shaders/"+fragmentShader},false);
 		fragShader.compile(gl);
 		
 		ShaderProgram newShader = new ShaderProgram();
 		newShader.init(gl);
 		newShader.add(vertShader);
 		newShader.add(fragShader);
 		
 		newShader.link(gl, System.out);
 		
 		vertShader.destroy(gl);
 		fragShader.destroy(gl);
 
 		return newShader;
 	}
 	
 	private float[] generateTriangleFanVertices(int edgeVertexCount)
 	{
 		float angleIncrement = 2*FloatUtil.PI/edgeVertexCount;
 		float[] resultVerts = new float[3*(edgeVertexCount+1+1)];
 		
 		//Define the origin of the fan
 		resultVerts[0] = 0f;
 		resultVerts[1] = 0f;
 		resultVerts[2] = 0f;
 		
 		//Define all the edge vertices of the fan
 		for(int i=0; i<=edgeVertexCount; i++)
 		{
 			int startIndex = (i+1)*3;
 			
 			resultVerts[startIndex]   = 0.6f*FloatUtil.cos(i*angleIncrement);// - FloatUtil.sin(i*angleIncrement); //X-value
 			resultVerts[startIndex+1] = 0.6f*FloatUtil.sin(i*angleIncrement);// + FloatUtil.cos(i*angleIncrement); //Y-value
 			resultVerts[startIndex+2] = 0; //Z-value
 		}
 		
 		return resultVerts;
 	}
 	
 	private float[] generateTriangleFanColours(int edgeVertexCount)
 	{
 		float[] resultColours = new float[4*(edgeVertexCount+1+1)];
 		
 		//Set the colour at the centre
 		resultColours[0] = 1;
 		resultColours[1] = 1;
 		resultColours[2] = 1;
 		resultColours[3] = 1;
 		
 		//Set the colour at the edge
 		for(int i=0; i<=edgeVertexCount; i++)
 		{
 			int startIndex = (i+1)*4;
 			
 			resultColours[startIndex]   = 1;
 			resultColours[startIndex+1] = 1;
 			resultColours[startIndex+2] = 1;
 			resultColours[startIndex+3] = 0;
 		}
 		
 		return resultColours;
 	}
 }
