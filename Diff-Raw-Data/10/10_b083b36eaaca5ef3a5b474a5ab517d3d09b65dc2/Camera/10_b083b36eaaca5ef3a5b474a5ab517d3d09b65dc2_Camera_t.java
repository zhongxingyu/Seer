 package grige;
 
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.Arrays;
 
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
 			1f, 1f, 1f, 1f,
 			1f, 1f, 1f, 1f,
 	};
 	
 	private final int[] quadIndices = {
 			0, 1, 2, 3,
 	};
 	
 	//Camera attributes
 	private Vector2 position;
 	private Vector2I size;
 	private float depth;
 	
 	private Color clearColour;
 	private float ambientLightAlpha;
 	
 	//Current transformation matrices
 	private float[] projectionMatrix;
 	private float[] viewingMatrix;
 	
 	//Vertex Buffers
 	private int screenCanvasVAO;
 	private int geometryVAO;
 	private int lightingVAO;
 	
 	private int shadowVertexBuffer;
 	
 	//Frame Buffers
 	private FBObject geometryFBO;
 	private FBObject lightingFBO;
 	
 	//Shader Data
 	private ShaderProgram screenCanvasShader;
 	private ShaderProgram geometryShader;
 	private ShaderProgram lightingShader;
 	private ShaderProgram shadowGeometryShader;
 	
 	//GL context
 	private GL2 gl;
 	
 	public Camera(int startWidth, int startHeight, int startDepth)
 	{
 		position = new Vector2(0, 0);
 		size = new Vector2I(startWidth, startHeight);
 		depth = startDepth;
 		
 		ambientLightAlpha = 0;
 		clearColour = new Color(0,0,0,1);
 	}
 	
 	public void setPosition(float newX, float newY)
 	{
 		position.x = newX;
 		position.y = newY;
 		
 		viewingMatrix = new float[]{
 				1,0,0,0,
 				0,1,0,0,
 				0,0,1,0,
 				-position.x-size.x/2f,-position.y-size.y/2f,0,1f
 		};
 	}
 	
 	public void setPosition(Vector2 newPosition)
 	{
 		setPosition(newPosition.x, newPosition.y);
 	}
 	
 	public void setSize(int newWidth, int newHeight, float newDepth)
 	{
 		size.x = newWidth;
 		size.y = newHeight;
 		depth = newDepth;
 		
 		projectionMatrix = new float[]{
 				2f/size.x,0,0,0,
 				0,2f/size.y,0,0,
 				0,0,-2f/depth,0,
 				0,0,-1,1
 		};
 		setPosition(position.x,position.y); //Update the viewing matrix as well, because the size has changed (so we need to translate (0,0) differently)
 	}
 	
 	public void setAmbientLightAlpha(float newAlpha) { ambientLightAlpha = newAlpha; }
 	public void setClearColor(float r, float g, float b){ clearColour.setValues(r, g, b, 1); }
 	
 	public float getX() { return position.x; }
 	public float getY() { return position.y; }
 	public float getWidth() { return size.x; }
 	public float getHeight() { return size.y; }
 	public float getDepth() { return depth; }
 	public float getAmbientLightAlpha() { return ambientLightAlpha; }
 	public Color getClearColor() { return clearColour; }
 	
 	public Vector2 screenToWorldLoc(Vector2 screenLoc)
 	{
 		return new Vector2(screenToWorldLoc(screenLoc.x, screenLoc.y, 0));
 	}
 	
 	public Vector2 screenToWorldLoc(float x, float y)
 	{
 		return new Vector2(screenToWorldLoc(x,y,0));
 	}
 	
 	public Vector3 screenToWorldLoc(float x, float y, float z)
 	{
 		float[] screenLoc = new float[]{2*x/getWidth()-1, 2*y/getHeight()-1, 2*z/getDepth()-1, 1}; //Transform from pixel space to OpenGL screen space
 		float[] worldLoc = new float[4];
 		
 		float[] inverseViewingMatrix = new float[]{
 				1,0,0,0,
 				0,1,0,0,
 				0,0,1,0,
 				position.x+size.x/2f,position.y+size.y/2f,0,1f
 		};
 		float[] inverseProjectionMatrix = new float[]{
 				size.x/2f,0,0,0,
 				0,size.y/2f,0,0,
 				0,0,-depth/2f,0,
 				0,0,-depth/2f,1
 		};
 		
 		float[] transformMatrix = inverseViewingMatrix;
 		FloatUtil.multMatrixf(transformMatrix, 0, inverseProjectionMatrix, 0);
 		FloatUtil.multMatrixVecf(transformMatrix, screenLoc, worldLoc);
 		
 		Vector3 result = new Vector3(worldLoc[0], worldLoc[1], worldLoc[2]); //This is in OpenGL screen space, so we need to change it into pixel space
 		
 		return result;
 	}
 	
 	public Vector2 worldToScreenLoc(Vector2 worldLoc)
 	{
 		return new Vector2(worldToScreenLoc(worldLoc.x, worldLoc.y, 0));
 	}
 	
 	public Vector2 worldToScreenLoc(float x, float y)
 	{
 		return new Vector2(worldToScreenLoc(x,y,0));
 	}
 	
 	public Vector3 worldToScreenLoc(float x, float y, float z)
 	{
 		float[] worldLoc = new float[]{x, y, z, 1};
 		float[] screenLoc = new float[4];
 		float[] transformMatrix = Arrays.copyOf(projectionMatrix, 16);
 		FloatUtil.multMatrixf(transformMatrix, 0, viewingMatrix, 0);
 		FloatUtil.multMatrixVecf(transformMatrix, worldLoc, screenLoc);
 		
 		Vector3 result = new Vector3(screenLoc[0], screenLoc[1], screenLoc[2]); //This is in OpenGL screen space, so we need to change it into pixel space
 		
 		result.x += 1;
 		result.y += 1;
 		result.z += 1;
 		
 		result.x *= getWidth()/2;
 		result.y *= getHeight()/2;
 		result.z *= getDepth()/2;
 		
 		return result;
 	}
 	
 	protected void initialize(GL glContext)
 	{
 		gl = glContext.getGL2();
 		
 		//Set rendering properties
 		gl.glDisable(GL.GL_CULL_FACE);
 		gl.glEnable(GL.GL_STENCIL_TEST);
 		
 		gl.glEnable(GL.GL_DEPTH_TEST);
 		gl.glDepthFunc(GL.GL_LEQUAL);
 		
 		//Initialize the various components and data stores
 		initializeGeometryData();
 		initializeLightingData();
 		initializeShadowData();
 		initializeScreenCanvas();
 		
 		//Initialize and buffer the projection and viewing matrices
 		setSize(size.x, size.y, depth);
 	}
 
 	private void initializeGeometryData()
 	{
 		//Create the framebuffer
 		geometryFBO = new FBObject();
 		geometryFBO.reset(gl, size.x, size.y);
 		geometryFBO.attachTexture2D(gl, 0, true);
 		geometryFBO.attachRenderbuffer(gl, FBObject.Attachment.Type.DEPTH, 6);
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
 		gl.glGenBuffers(4, buffers,0);
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
 		
 		gl.glBindVertexArray(0);
 		geometryShader.useProgram(gl, false);
 	}
 	
 	private void initializeLightingData()
 	{
 		//Create the framebuffer
 		lightingFBO = new FBObject();
 		lightingFBO.reset(gl, size.x, size.y);
 		lightingFBO.attachTexture2D(gl, 0, true);
 		lightingFBO.attachRenderbuffer(gl, FBObject.Attachment.Type.DEPTH_STENCIL, 6);
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
 		int indexBuffer = buffers[0];
 		int vertexBuffer = buffers[1];
 		
 		//Buffer the vertex indices
 		gl.glBindBuffer(GL.GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
 		gl.glBufferData(GL.GL_ELEMENT_ARRAY_BUFFER, screenCanvasIndices.length*(Integer.SIZE/8), IntBuffer.wrap(screenCanvasIndices), GL.GL_STATIC_DRAW);
 		
 		//Buffer the vertex locations
 		int positionIndex = gl.glGetAttribLocation(lightingShader.program(), "position");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vertexBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, screenCanvasVertices.length*(Float.SIZE/8), FloatBuffer.wrap(screenCanvasVertices), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(positionIndex);
 		gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 0, 0);
 		
 		gl.glBindVertexArray(0);
 		lightingShader.useProgram(gl, false);
 	}
 
 	private void initializeShadowData()
 	{
 		//Load the shaders
 		shadowGeometryShader = loadShader("ShadowGeometry.vsh", "ShadowGeometry.fsh");
 		
 		int[] buffers = new int[1];
 		
 		gl.glGenBuffers(1, buffers, 0);
 		shadowVertexBuffer = buffers[0];
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
 		
 		//Assign to the samplers, the textures used by the geometry and lighting respectively
 		int geometryTextureSamplerIndex = gl.glGetUniformLocation(screenCanvasShader.program(), "geometryTextureUnit");
 		gl.glUniform1i(geometryTextureSamplerIndex, 0);
 		int lightingTextureSamplerIndex = gl.glGetUniformLocation(screenCanvasShader.program(), "lightingTextureUnit");
 		gl.glUniform1i(lightingTextureSamplerIndex, 1);
 		
 		gl.glBindVertexArray(0);
 		screenCanvasShader.useProgram(gl, false);
 	}
 	
 	private void rebufferViewingMatrix()
 	{
 		int viewMatrixIndex;
 		
 		//Geometry shader
 		geometryShader.useProgram(gl, true);
 		viewMatrixIndex = gl.glGetUniformLocation(geometryShader.program(), "viewingMatrix");
 		gl.glUniformMatrix4fv(viewMatrixIndex, 1, false, viewingMatrix, 0);
 		geometryShader.useProgram(gl, false);
 		
 		//Lighting shader
 		lightingShader.useProgram(gl, true);
 		viewMatrixIndex = gl.glGetUniformLocation(lightingShader.program(), "viewingMatrix");
 		gl.glUniformMatrix4fv(viewMatrixIndex, 1, false, viewingMatrix, 0);		
 		lightingShader.useProgram(gl, false);
 		
 		//Shadow Geometry Shader
 		shadowGeometryShader.useProgram(gl, true);
 		viewMatrixIndex = gl.glGetUniformLocation(shadowGeometryShader.program(), "viewingMatrix");
 		gl.glUniformMatrix4fv(viewMatrixIndex, 1, false, viewingMatrix, 0);
 		shadowGeometryShader.useProgram(gl, false);
 	}
 	
 	private void rebufferProjectionMatrix()
 	{
 		int projMatrixIndex;
 		
 		//Geometry shader
 		geometryShader.useProgram(gl, true);
 		projMatrixIndex = gl.glGetUniformLocation(geometryShader.program(), "projectionMatrix");
 		gl.glUniformMatrix4fv(projMatrixIndex, 1, false, projectionMatrix, 0);
 		geometryShader.useProgram(gl, false);
 		
 		//Lighting shader
 		lightingShader.useProgram(gl, true);
 		projMatrixIndex = gl.glGetUniformLocation(lightingShader.program(), "projectionMatrix");
 		gl.glUniformMatrix4fv(projMatrixIndex, 1, false, projectionMatrix, 0);
 		lightingShader.useProgram(gl, false);
 		
 		//Shadow Geometry shader
 		shadowGeometryShader.useProgram(gl, true);
 		projMatrixIndex = gl.glGetUniformLocation(shadowGeometryShader.program(), "projectionMatrix");
 		gl.glUniformMatrix4fv(projMatrixIndex, 1, false, projectionMatrix, 0);
 		shadowGeometryShader.useProgram(gl, false);
 	}
 	
 	protected void refresh(GL glContext)
 	{
 		gl = glContext.getGL2();
 		gl.glDepthMask(true);
 		
 		//Clear the screen
 		gl.glClearColor(0, 0, 0, 1);
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 		
 		//Clear the geometry buffer
 		geometryFBO.bind(gl);
 		gl.glClearColor(clearColour.getRed(), clearColour.getGreen(), clearColour.getBlue(), clearColour.getAlpha());
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
 		geometryFBO.unbind(gl);
 		
 		//Clear the lighting buffer
 		lightingFBO.bind(gl);
 		gl.glClearColor(0, 0, 0, ambientLightAlpha);
 		gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT | GL.GL_STENCIL_BUFFER_BIT);
 		lightingFBO.unbind(gl);
 		
 		rebufferViewingMatrix();
 		rebufferProjectionMatrix();
 		
 		gl.glDepthMask(false);
 	}
 	
 	public void drawGeometryStart()
 	{
 		geometryFBO.bind(gl);
 	}
 	public void drawGeometryEnd()
 	{
 		geometryFBO.unbind(gl);
 	}
 	public void drawLightingStart()
 	{
 		lightingFBO.bind(gl);
 	}
 	public void drawLightingEnd()
 	{
 		lightingFBO.unbind(gl);
 	}
 	
 	protected void drawObject(Drawable object)
 	{
 		//Compute the object transform matrix
 		float objWidth = object.width();
 		float objHeight = object.height();
 		float rotationSin = FloatUtil.sin(object.rotation());
 		float rotationCos = FloatUtil.cos(object.rotation());
 		
 		float[] objectTransformMatrix = new float[]{
 				 objWidth*rotationCos, objHeight*rotationSin,0,0,
 				-objWidth*rotationSin, objHeight*rotationCos,0,0,
 				0,0,1,0,
 				object.x(), object.y(), -object.depth(), 1
 		};
 		
 		//Draw geometry
 		if(object.getTexture() == null)
 			return;
 		
 		geometryShader.useProgram(gl, true);
 		gl.glActiveTexture(GL.GL_TEXTURE0);
 		gl.glBindVertexArray(geometryVAO);
 		gl.glDepthMask(true);
 		
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
 		
 		//Texture specification
 		int textureSamplerIndex = gl.glGetUniformLocation(geometryShader.program(), "textureUnit");
 		gl.glUniform1i(textureSamplerIndex, 0);
 		
 		int geometryObjTransformIndex = gl.glGetUniformLocation(geometryShader.program(), "objectTransform");
 		gl.glUniformMatrix4fv(geometryObjTransformIndex, 1, false, objectTransformMatrix, 0);
 		
 		Texture objTex = object.getTexture();
 		objTex.enable(gl);
 		objTex.bind(gl);
 		
 		gl.glDrawElements(GL.GL_TRIANGLE_STRIP, quadIndices.length, GL.GL_UNSIGNED_INT, 0);
 		
 		objTex.disable(gl);
 		
 		gl.glDisable(GL.GL_BLEND);
 		
 		gl.glDepthMask(false);
 		gl.glBindVertexArray(0);
 		geometryShader.useProgram(gl, false);
 	}
 	
 	protected void drawObjectDepthToLighting(Drawable object)
 	{
 		//Compute the object transform matrix
 		float objWidth = object.width();
 		float objHeight = object.height();
 		float rotationSin = FloatUtil.sin(object.rotation());
 		float rotationCos = FloatUtil.cos(object.rotation());
 		
 		float[] objectTransformMatrix = new float[]{
 				 objWidth*rotationCos, objHeight*rotationSin,0,0,
 				-objWidth*rotationSin, objHeight*rotationCos,0,0,
 				0,0,1,0,
 				object.x(), object.y(), -object.depth(), 1
 		};
 		
 		//Draw geometry		
 		geometryShader.useProgram(gl, true);
 		gl.glBindVertexArray(geometryVAO);
 		gl.glDepthMask(true);
 		gl.glColorMask(false, false, false, false);
 		
 		int objTransformIndex = gl.glGetUniformLocation(geometryShader.program(), "objectTransform");
 		gl.glUniformMatrix4fv(objTransformIndex, 1, false, objectTransformMatrix, 0);
 		
 		gl.glDrawElements(GL.GL_TRIANGLE_STRIP, quadIndices.length, GL.GL_UNSIGNED_INT, 0);
 		
 		gl.glColorMask(true, true, true, true);
 		gl.glDepthMask(false);
 		gl.glBindVertexArray(0);
 		geometryShader.useProgram(gl, false);
 	}
 
 	protected void drawLight(Light light)
 	{
 		//Compute the transformed light location (for lighting)
 		Vector3 transformedLightLoc = worldToScreenLoc(light.x(), light.y(), light.depth());
 
 		float[] lightVertices = {
 				-1.0f, -1.0f, -light.depth(),
 				-1.0f, 1.0f, -light.depth(),
 				1.0f, -1.0f, -light.depth(),
 				1.0f,  1.0f, -light.depth(),
 		};
 		
 		//Draw the light
 		lightingShader.useProgram(gl, true);
 		gl.glBindVertexArray(lightingVAO);
 		
 		gl.glEnable(GL.GL_BLEND);
 		gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE);
 		
 		int[] buffer = new int[1];
 		gl.glGenBuffers(1, buffer, 0);
 		
 		int positionIndex = gl.glGetAttribLocation(lightingShader.program(), "position");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, buffer[0]);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, lightVertices.length*(Float.SIZE/8), FloatBuffer.wrap(lightVertices), GL.GL_STATIC_DRAW);
 		gl.glEnableVertexAttribArray(positionIndex);
 		gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 0, 0);
 		
 		
 		int lightLocIndex = gl.glGetUniformLocation(lightingShader.program(), "lightLoc");
 		gl.glUniform3f(lightLocIndex, transformedLightLoc.x, transformedLightLoc.y, transformedLightLoc.z);
 		
 		int radiusIndex = gl.glGetUniformLocation(lightingShader.program(), "radius");
 		gl.glUniform1f(radiusIndex, light.getRadius());
 		
 		int colourIndex = gl.glGetUniformLocation(lightingShader.program(), "lightColour");
 		gl.glUniform3fv(colourIndex, 1, light.getColour().toColorFloatArray(), 0);
 		
 		int intensityIndex = gl.glGetUniformLocation(lightingShader.program(), "intensity");
 		gl.glUniform1f(intensityIndex, 1f);
 		
 		gl.glDrawElements(GL.GL_TRIANGLE_STRIP, screenCanvasIndices.length, GL.GL_UNSIGNED_INT, 0);
 		
 		gl.glDisable(GL.GL_BLEND);
 		
 		gl.glBindVertexArray(0);
 		lightingShader.useProgram(gl, false);
 	}
 	
 	protected void clearShadowStencil()
 	{
 		lightingFBO.bind(gl);
 		
 		gl.glClear(GL.GL_STENCIL_BUFFER_BIT);
 		
 		lightingFBO.unbind(gl);
 	}
 	
 	private void drawShadow(float[] shadowVerts)
 	{
 		shadowGeometryShader.useProgram(gl, true);
 		
 		int positionIndex = gl.glGetAttribLocation(shadowGeometryShader.program(), "position");
 		gl.glBindBuffer(GL.GL_ARRAY_BUFFER, shadowVertexBuffer);
 		gl.glBufferData(GL.GL_ARRAY_BUFFER, shadowVerts.length*(Float.SIZE/8), FloatBuffer.wrap(shadowVerts), GL.GL_DYNAMIC_DRAW);
 		gl.glEnableVertexAttribArray(positionIndex);
 		gl.glVertexAttribPointer(positionIndex, 3, GL.GL_FLOAT, false, 0, 0);
 		
 		gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, shadowVerts.length/3);
 		
 		shadowGeometryShader.useProgram(gl, false);
 	}
 	
 	protected void drawShadowsToStencil(ArrayList<float[]> vertexArrays)
 	{
 		//Set to draw only to the stencil buffer (no colour/alpha)
 		gl.glColorMask(false, false, false, false);
 		gl.glStencilFunc(GL.GL_ALWAYS, 1, 1);
 		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_REPLACE);
 		
 		//Render all shadows from this light into the stencil buffer
 		//This is so that when we render the actual light, it doesn't light up the shadows
 		for(int i=0; i<vertexArrays.size(); i++)
 			drawShadow(vertexArrays.get(i));
 		
 		//Reset drawing to standard colour/alpha
 		gl.glStencilOp(GL.GL_KEEP, GL.GL_KEEP, GL.GL_KEEP);
 		gl.glStencilFunc(GL.GL_EQUAL, 0, 1);
 		gl.glColorMask(true, true, true, true);
 	}
 	
 	protected void commitDraw()
 	{
 		screenCanvasShader.useProgram(gl, true);
 		gl.glBindVertexArray(screenCanvasVAO);
 		
 		//Bind the different buffer textures to the relevant GL textures so we can use it in our canvas shader
 		TextureAttachment geometryTexture = (TextureAttachment)geometryFBO.getColorbuffer(0);
 		gl.glActiveTexture(GL.GL_TEXTURE0);
 		gl.glBindTexture(GL.GL_TEXTURE_2D, geometryTexture.getName());
 		
 		TextureAttachment lightingTexture = (TextureAttachment)lightingFBO.getColorbuffer(0);
 		gl.glActiveTexture(GL.GL_TEXTURE1);
 		gl.glBindTexture(GL.GL_TEXTURE_2D, lightingTexture.getName());
 		
 		gl.glDrawElements(GL.GL_TRIANGLE_STRIP, screenCanvasIndices.length, GL.GL_UNSIGNED_INT, 0);
 		
 		gl.glBindVertexArray(0);
 		screenCanvasShader.useProgram(gl, false);
 	}
 	
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
 	
 	/*
 	 * Compute the vertices for the shadow cast by the given GameObject when lit by the given Light
 	 */
 	protected float[] generateShadowVertices(Light l, GameObject obj)
 	{
 		ArrayList<Float> vertexList = new ArrayList<Float>();
 		int currentVertexIndex = 0;
 		
 		float[] vertices = obj.getVertices();
 		
 		Vector2 vertexNormal = new Vector2(0,0);
 		Vector2 projectedLightVertex = new Vector2(0, 0);
 		
 		Vector2 currentVert = new Vector2(0,0);
 		Vector2 currentLightOffsetDir = new Vector2(0,0);
 		
 		Vector2 previousVert = new Vector2(vertices[6], vertices[7]);
 		Vector2 previousLightOffsetDir = new Vector2(l.x()-previousVert.x, l.y()-previousVert.y);
 		
 		for(int index=0; index<8; index+=2)
 		{
 			currentVert.x = vertices[index];
 			currentVert.y = vertices[index+1];
 			
 			currentLightOffsetDir.x = l.x() - currentVert.x;
 			currentLightOffsetDir.y = l.y() - currentVert.y;
 			currentLightOffsetDir.normalise(); //We can normalise here because the magnitude has no effect on the sign of any dot products
 			
 			//Because we know we're traversing vertices in a counter-clockwise order
 			//we know that the normal for an edge is (dy, -dx)
 			vertexNormal.x = currentVert.y - previousVert.y;
 			vertexNormal.y = -(currentVert.x - previousVert.x);
 			
 			//Check if the normal and the light are facing the same general direction
 			float dotProduct = vertexNormal.x*currentLightOffsetDir.x + vertexNormal.y*currentLightOffsetDir.y; 
 			if(dotProduct <= 0)
 			{
 				if(currentVertexIndex == -1 || index == 0) //If its the first index then we would have never had a chance to set currentVertexIndex to -1
 				{ 	//If the current index has been set to -1 then we moved from light into shadow
 					//so we need to add shadow for both the current and previous vertices
 					currentVertexIndex = 0;
 					vertexList.add(currentVertexIndex, previousVert.x);
 					vertexList.add(currentVertexIndex+1, previousVert.y);
 					vertexList.add(currentVertexIndex+2, -obj.depth()-0.5f); //Z-value for depth testing and for ease of use in vertex buffers, negative because we don't use object transform matrices for shadow geometry
 																			 //We subtract 0.5 so that the shadow lies just below the current depth, this prevents odd effects (e.g when objects overlap) and I guess is more physically accurate
 					
 					projectedLightVertex.set(previousLightOffsetDir);
 					projectedLightVertex.multiply(-1000); //lightOffset is the vector: "vertex -> light", we use (-) because we need to offset "light vertex ->"
 					projectedLightVertex.add(previousVert);
 					
 					vertexList.add(currentVertexIndex+3, projectedLightVertex.x);
 					vertexList.add(currentVertexIndex+4, projectedLightVertex.y);
 					vertexList.add(currentVertexIndex+5, -obj.depth()-0.5f);
 					
 					currentVertexIndex += 6;
 				}
 				
 				//Add the current vertex and its projection away from the light
 				vertexList.add(currentVertexIndex, currentVert.x);
 				vertexList.add(currentVertexIndex+1, currentVert.y);
 				vertexList.add(currentVertexIndex+2, -obj.depth()-0.5f);
 				
 				projectedLightVertex.set(currentLightOffsetDir);
 				projectedLightVertex.multiply(-1000); //lightOffset is the vector: "vertex -> light", we use (-) because we need to offset "light vertex ->"
 				projectedLightVertex.add(currentVert);
 				
 				vertexList.add(currentVertexIndex+3, projectedLightVertex.x);
 				vertexList.add(currentVertexIndex+4, projectedLightVertex.y);
 				vertexList.add(currentVertexIndex+5, -obj.depth()-0.5f);
 				
 				currentVertexIndex += 6;
 			}
 			else
 				currentVertexIndex = -1;
 			
 			//Shift our data backwards, so what used to be our "current" data, is now our "previous" data
 			previousVert.set(currentVert);
 			previousLightOffsetDir.set(currentLightOffsetDir);
 		}
 		
 		float[] vertArray = new float[vertexList.size()];
 		for(int i=0; i<vertArray.length; i++)
 			vertArray[i] = vertexList.get(i);
 		
 		return vertArray;
 	}
 }
