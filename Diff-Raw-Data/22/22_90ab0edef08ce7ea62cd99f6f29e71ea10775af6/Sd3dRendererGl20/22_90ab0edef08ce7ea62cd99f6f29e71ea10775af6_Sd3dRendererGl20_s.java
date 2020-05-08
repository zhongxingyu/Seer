 package org.nicolasmy.sd3d.gfx.renderer;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.IntBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.microedition.khronos.egl.EGL11;
 import javax.microedition.khronos.opengles.GL11;
 
 import org.nicolasmy.sd3d.GameHolder;
 import org.nicolasmy.sd3d.Sd3dConfig;
 import org.nicolasmy.sd3d.gfx.Sd3dLight;
 import org.nicolasmy.sd3d.gfx.Sd3dLight.LightType;
 import org.nicolasmy.sd3d.gfx.Sd3dMaterial;
 import org.nicolasmy.sd3d.gfx.Sd3dMesh;
 import org.nicolasmy.sd3d.gfx.Sd3dObject;
 import org.nicolasmy.sd3d.gfx.Sd3dRessourceManager;
 import org.nicolasmy.sd3d.gfx.Sd3dScene;
 
 
 import org.nicolasmy.sd3d.math.Sd3dMatrix;
 
 import android.opengl.GLES20;
 import android.opengl.GLU;
 import android.opengl.Matrix;
 import android.util.Log;
 
 
 
 public class Sd3dRendererGl20 implements Sd3dRendererInterface
 {
 	public enum ALIGN
 	{
 		TOP,
 		LEFT,
 		RIGHT,
 		CENTER
 	}
 	private HashMap<String, Sd3dShader> shaderMap = new HashMap<String, Sd3dShader>();
 	private Sd3dBitmapQuadFont mBitmapQuadFont = new Sd3dBitmapQuadFont();
 	private boolean useShadowVolume = false;
 	private boolean useShadowMapping = false;
 	private boolean useMultisampling = false;
 	private boolean clearScreen = true;
 	private Sd3dShadowMapping shadowMapping;
 	private Sd3dSampling sampling;
 	private String vertexShader;
 	private String fragmentShader;
 	private Sd3dShader defaultShader;
 	private Sd3dShader textureOnlyShader;
 	private Sd3dShader colorOnlyShader;
 	private Sd3dShader pickingShader = null;
 	private Sd3dFrameBuffer pickingFrameBuffer = null;
 	private float[] mProjectionOrthoMatrix = new float[16];
     
     //private boolean mTranslucentBackground;
 	private int screenWidth;
 	public int getScreenWidth() {
 		return screenWidth;
 	}
 
 
 	public void setScreenWidth(int screenWidth) {
 		this.screenWidth = screenWidth;
 	}
 
 
 	public int getScreenHeight() {
 		return screenHeight;
 	}
 
 
 	public void setScreenHeight(int screenHeight) {
 		this.screenHeight = screenHeight;
 	}
 
 	private int screenHeight;
 	private int top;
 	private float m_Frustum[][] = {new float[4],new float[4],new float[4],new float[4],new float[4],new float[4]};
 	
    
 	private Sd3dRendererElement mRenderList[];
 	private int mMaxRenderElement;
 	private int mCountRenderElement;
 	public Sd3dBmpFont mBmpFont;
 	public float mLightVector[];
 	
 	
 	public void setGL11Context(GL11 gl)
 	{
 	}
 	
 	private void loadConfiguration()
 	{
 		if ((""+Sd3dConfig.getString("use_shadow_mapping")).equals("true"))
 			this.useShadowMapping = true;
 		
 		if ((""+Sd3dConfig.getString("use_shadow_mapping")).equals("false"))
 			this.useShadowMapping = false;
 
 		if ((""+Sd3dConfig.getString("use_shadow_volume")).equals("true"))
 			this.useShadowVolume = true;
 		
 		if ((""+Sd3dConfig.getString("use_shadow_volume")).equals("false"))
 			this.useShadowVolume = false;		
 		
 		if ((""+Sd3dConfig.getString("clear_screen")).equals("true"))
 			this.clearScreen = true;
 		
 		if ((""+Sd3dConfig.getString("clear_screen")).equals("false"))
 			this.clearScreen = false;				
 		
 		if (! Sd3dConfig.getString("sampling_ratio").equals("1.0"))
 			this.useMultisampling = true;
 	}
 	
 	
 	public void regiserMesh(Sd3dRendererElement element, Sd3dMesh mesh)
 	{
 		if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
 		{
 			//throw new RuntimeException("regiserMesh() ENTERING: ERROR ON OPENGL ES CALL");
 		}			
 		
 		//OpenGL stuffs
 		IntBuffer buffer = IntBuffer.allocate(1);
 		
 		if (mesh.mVertices != null)
 		{
 			GLES20.glGenBuffers(1,buffer);
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffer.get(0));
 			GLES20.glBufferData(GL11.GL_ARRAY_BUFFER,mesh.mVertices.capacity() * 4,mesh.mVertices,GL11.GL_STATIC_DRAW);
 			element.mVertexBufferName = buffer.get(0);
 		}
 		else element.mVertexBufferName = 0;
 	
 		if (mesh.mTangentsBinormals != null)
 		{
 			GLES20.glGenBuffers(1,buffer);
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffer.get(0));
 			GLES20.glBufferData(GL11.GL_ARRAY_BUFFER,mesh.mTangentsBinormals.capacity() * 4,mesh.mTangentsBinormals,GL11.GL_STATIC_DRAW);
 			element.mTangentBinormalBufferName = buffer.get(0);
 		} else element.mTangentBinormalBufferName = 0;			
 		
 		/*
 		if (mesh.mNormals != null)
 		{
 			GLES20.glGenBuffers(1,buffer);
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffer.get(0));
 			GLES20.glBufferData(GL11.GL_ARRAY_BUFFER,mesh.mNormals.capacity() * 4,mesh.mNormals,GL11.GL_STATIC_DRAW);
 			element.mNormalBufferName = buffer.get(0);
 		} else element.mNormalBufferName = 0;			
 		*/
 		
 		if (mesh.mIndices != null)
 		{
 			GLES20.glGenBuffers(1,buffer);
 			GLES20.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, buffer.get(0));
 			GLES20.glBufferData(GL11.GL_ELEMENT_ARRAY_BUFFER,mesh.mIndices.capacity()*2,mesh.mIndices,GL11.GL_STATIC_DRAW);
 			element.mIndiceBufferName = buffer.get(0);
 			element.mIndiceCount = mesh.mIndices.capacity();
 		} else element.mIndiceBufferName = 0;
 		
 		/*
 		if (mesh.mTexCoords != null)
 		{
 			GLES20.glGenBuffers(1,buffer);
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffer.get(0));
 			GLES20.glBufferData(GL11.GL_ARRAY_BUFFER,mesh.mTexCoords.capacity() * 4,mesh.mTexCoords,GL11.GL_STATIC_DRAW);
 			element.mTexCoordBufferName = buffer.get(0);
 		} else element.mTexCoordBufferName = 0;		
 		*/
 		if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
 		{
 			throw new RuntimeException("regiserMesh() EXITING: ERROR ON OPENGL ES CALL");
 		}		
 		
 		element.mIsShadowVolume = mesh.mIsShadowVolume;
 		element.mIsBillboard = mesh.mIsBillboard;
 		element.mIsInScreenSpace = mesh.mIsInScreenSpace;
 	}	
 	
 	public int registerMaterial(Sd3dRendererElement element, Sd3dMaterial material)
 	{
 		if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
 		{
 			throw new RuntimeException("registerMaterial() ENTERING: ERROR ON OPENGL ES CALL");
 		}				
 		
 		//OpenGL stuffs	
 		IntBuffer buffer = IntBuffer.allocate(1);		
 		
 		if (material.mColors != null)
 		{
 			GLES20.glGenBuffers(1,buffer);
 			
 			
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, buffer.get(0));
 			GLES20.glBufferData(GL11.GL_ARRAY_BUFFER,material.mColors.capacity() * 4,material.mColors,GL11.GL_STATIC_DRAW);
 			//mColorBufferName = buffer.get(0);
 			//material.mColorName = mColorBufferName;
 			material.mColorName =  buffer.get(0);
 		//} else mColorBufferName = 0;	
 		} else material.mColorName = 0;
 	
 		if (material.mTextureName[0] == 0) {
 			if (material.mTextureData != null) {
 				GLES20.glGenTextures(1, buffer); 
 				GLES20.glBindTexture(GL11.GL_TEXTURE_2D, buffer.get(0)); 
 				//GLES20.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
 				GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,material.mWidth, material.mHeight, 0, 
 						GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, material.mTextureData);
 				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
 				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); 
 				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,GL11.GL_CLAMP_TO_EDGE);
 				
 				element.mTextureName[0] = buffer.get(0);
 				
 				
 				material.mTextureName[0] = buffer.get(0);
 			}
 		} else {
 			element.mTextureName[0] = material.mTextureName[0];
 		}
 		
 		if (material.mTextureName[1] == 0) {
 			if (material.mSecondaryTextureData != null) {
 				GLES20.glGenTextures(1, buffer); 
 				GLES20.glBindTexture(GL11.GL_TEXTURE_2D, buffer.get(0)); 
 				//GLES20.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
 				GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,
 						//material.mWidth, material.mHeight, 
 						//TODO remove that shit !
 						2048, 1024,
 						0, 
 						GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, material.mSecondaryTextureData);
 				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
 				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); 
 				GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,GL11.GL_CLAMP_TO_EDGE);
 				
 				
 				element.mTextureName[1] = buffer.get(0);
 				material.mTextureName[1] = buffer.get(0);
 			}
 		} else {
 			element.mTextureName[1] = material.mTextureName[1];
 		}		
 		
 		if (material.getShaderName() != null) {
 			element.mShaderName = material.getShaderName();
 		}
 		
 		if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
 		{
 			throw new RuntimeException("registerMaterial() EXITING: ERROR ON OPENGL ES CALL");
 		}		
 		
 		return 0;
 	}	
 	
 
 	
 	public Sd3dRendererElement[] createRenderElement(Sd3dObject object)
 	{
 		Sd3dRendererElement element[] = new Sd3dRendererElement[object.mMesh.length];
 		for (int i = 0; i < object.mMesh.length;i++)
 		{
 			if (object.mMesh[i] != null)
 			{	
 				element[i] = new Sd3dRendererElement();
 				
 				if (object.mMesh[i].mRendererElementInterface != null)
 				{
 					element[i].mRendererInterface = object.mMesh[i].mRendererElementInterface;
 					element[i].mRendererInterface.register();
 				}
 
 					
 				if (object.mMesh[i] != null)
 					regiserMesh(element[i],object.mMesh[i]);
 				if (i < object.mMaterial.length)
 				  if (object.mMaterial[i] != null)
 				  {
 					  registerMaterial(element[i],object.mMaterial[i]);	
 				  }
 		
 				element[i].mObject = object;
 				
 
 				element[i].mIsPickable = object.mIsPickable;
 				if (element[i].mIsPickable)
 				{
 					generatePickingColor(element[i]);
 				}
 				
 				if (object.mTransformMatrix != null)
 				{
 					element[i].mTransformMatrix = object.mTransformMatrix;
 				}
 				
 				if (object.mPosition != null)
 				{
 					element[i].mPosition = object.mPosition;
 				}
 				
 				if (object.mMesh[i].mMeshPosition != null)
 				{
 					element[i].mPosition = object.mMesh[i].mMeshPosition;	
 				}
 				
 				if (i < object.mMaterial.length)
 				if (object.mMaterial[i].useAlphaBlending())
 				{
 					element[i].mAlphaBlending = true;
 				} else element[i].mAlphaBlending = false;
 		
 				if (i < object.mMaterial.length)
 				if (object.mMaterial[i].isAlphaTest())
 				{
 					element[i].mAlphaTest = true;
 				} else element[i].mAlphaTest = false;			
 	
 				if (i < object.mMaterial.length)
 				if (object.mMaterial[i].isRenderLight())
 				{
 					element[i].mRenderLight = true;
 				} else element[i].mRenderLight = false;					
 				
 				if (object.mRotation != null)
 				{
 					element[i].mOrientation = object.mRotation;
 				} 
 				
 				//Hierarchical objects managment
 				if (object.mChild != null)
 				{
 					Log.d("sd3d","Hierarchical objects managment allocation");
 					if (object.mChild.mRenderElement == null)
 						object.mChild.mRenderElement = createRenderElement(object.mChild);
 				}
 				
 			}
 		}
 		return element;
 	}
 	
 	public void destroyRenderElement(Sd3dObject object)
 	{
 		for (int i = 0;i < object.mRenderElement.length;i++)
 		{
 			if (object.mRenderElement[i] != null)					
 			{
 				if (object.mRenderElement[i].mRendererInterface != null)
 				{
 					object.mRenderElement[i].mRendererInterface.unregister();
 					object.mRenderElement[i].mRendererInterface = null;
 				}
 				
 				unregisterMesh(object.mRenderElement[i]);
 				unregisterMaterial(object.mRenderElement[i]);			
 			}
 		}
 	}
 	
 	public void addObjectToRenderList(Sd3dObject object)
 	{
 		if (mCountRenderElement < mMaxRenderElement)
 		{
 			if (object.mRenderElement == null)
 			{
 				object.mRenderElement = createRenderElement(object);
 			}
 			
 			for (int i = 0;i < object.mRenderElement.length;i++)
 			{
 				if (object.mRenderElement[i] != null)					
 					if (object.mRenderElement[i].mDisable != true)
 				{
 					mRenderList[mCountRenderElement] = object.mRenderElement[i];
 					mCountRenderElement++;
 				}
 			}
 		}
 	}
 	
 	public void unregisterMaterial(Sd3dMaterial material)
 	{
 		int buffer[] = new int[1];
 		
 		if (material.mColorName != 0)
 		{
 			buffer[0] = material.mColorName;
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			material.mColorName = 0;
 		}	
 	}
 	
 	public void unregisterMaterial(Sd3dRendererElement element)
 	{
 		int buffer[] = new int[1];
 		
 		if (element.mTextureName[0] != 0)
 		{
 			buffer[0] = element.mTextureName[0];
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			element.mTextureName[0] = 0;
 		}
 		
 		if (element.mTextureName[1] != 0)
 		{
 			buffer[0] = element.mTextureName[1];
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			element.mTextureName[1] = 0;
 		}
 				
 		
 		
 		if (element.mObject != null)
 		{
 			for (int i = 0;i < element.mObject.mMaterial.length;i++)
 			{
 				unregisterMaterial(element.mObject.mMaterial[i]);
 			}
 		}
 	}
 
 	public void unregisterMesh(Sd3dRendererElement element)
 	{
 		int buffer[] = new int[1];
 		
 		if (element.mVertexBufferName != 0)
 		{
 			buffer[0] = element.mVertexBufferName;
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			element.mVertexBufferName = 0;
 		}	
 		
 		/*
 		if (element.mNormalBufferName != 0)
 		{
 			buffer[0] = element.mNormalBufferName;
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			element.mNormalBufferName = 0;
 		}	
 		*/
 		if (element.mIndiceBufferName != 0)
 		{
 			buffer[0] = element.mIndiceBufferName;
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			element.mIndiceBufferName = 0;
 		}		
 		
 		/*
 		if (element.mTexCoordBufferName != 0)
 		{
 			buffer[0] = element.mTexCoordBufferName;
 			GLES20.glDeleteBuffers(1, buffer, 0);
 			element.mTexCoordBufferName = 0;
 		}
 		
 		*/
 	}
 	
 	FloatBuffer vertexBuffer;
 	private void drawShadowVolumeQuad(Sd3dShader shader)
 	{		
 		
 		float vertices[] = {
 				//0f, (float)this.screenHeight, 0f,        					  // V1 - first vertex (x,y,z)
 				//(float)this.screenWidth, (float)this.screenHeight, 0f,        // V2 - second vertex
 				0f,  0f,  0f,                                                 // V3 - third vertex		
 				(float)this.screenWidth, (float)this.screenHeight, 0f,        // V2 - second vertex	
 				0f, (float)this.screenHeight, 0f,        					  // V1 - first vertex (x,y,z)
 				
 				//(float)this.screenWidth, (float)this.screenHeight, 0f,        // V1 - first vertex (x,y,z)
 				//(float)this.screenWidth, 0f,  0f,                             // V2 - second vertex
 				0f,  0f,  0f,                                                  // V3 - third vertex			
 				(float)this.screenWidth, 0f,  0f,                             // V2 - second vertex		
 				(float)this.screenWidth, (float)this.screenHeight, 0f        // V1 - first vertex (x,y,z)				
 		};		
 		
 		
 		if (vertexBuffer == null)
 		{
 		  ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
 		  vertexByteBuffer.order(ByteOrder.nativeOrder());
 		
           // allocates the memory from the byte buffer
 	      vertexBuffer = vertexByteBuffer.asFloatBuffer();
 	    
 		  // fill the vertexBuffer with the vertices
 	      vertexBuffer.put(vertices);
 	    
 	      // set the cursor position to the beginning of the buffer	
 	      vertexBuffer.position(0);		
 		}
 		
 		//OpenGL stuffs	
 		Matrix.setIdentityM(shader.modelMatrix, 0);
 		//Matrix.setIdentityM(mViewMatrix, 0);
 		
 		shader.renderStateVector.put(2, 0);//NO LIGHT	
 		shader.renderStateVector.put(1, 2);//HAS COLOR UNIFORM
 		shader.renderStateVector.put(0, 0);//NO TEXTURE
 		
 		//Colors 
 		shader.colorVector.put(0,0f);
 		shader.colorVector.put(1,0f);
 		shader.colorVector.put(2,0f);
 		shader.colorVector.put(3,0.3f);
 		
 	    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
 	    // (which currently contains model * view).
 	    //Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
 	    //Matrix.multiplyMM(mMVMatrix, 0, mModelMatrix, 0,mViewMatrix, 0);
 	 
 	    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
 	    // (which now contains model * view * projection).
 		Matrix.orthoM(shader.projectionOrthoMatrix, 0, 0, this.screenWidth, this.screenHeight, 0, -1, 1);
 	    Matrix.multiplyMM(shader.MVPMatrix, 0, shader.projectionOrthoMatrix, 0, shader.modelMatrix, 0);			
 	  
 
 	    shader.bind();
 	    
 
 	    GLES20.glUniformMatrix4fv(shader.getMVMatrixHandle(), 1, false, shader.MVMatrix, 0);
 	    GLES20.glUniformMatrix4fv(shader.getMVPMatrixHandle(), 1, false, shader.MVPMatrix, 0);
 	    GLES20.glUniform4iv(shader.getRenderStateVectorHandle(), 1, shader.renderStateVector);		
 	    GLES20.glUniform4fv(shader.getColorVectorHandle(), 1, shader.colorVector);	
 	    
 		
 		GLES20.glEnable(GL11.GL_BLEND);
 		GLES20.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		
 		GLES20.glVertexAttribPointer(Sd3dShader.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
 		GLES20.glEnableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		
 		
 		GLES20.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
 		
 		GLES20.glDisableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		
 		GLES20.glDisable(GL11.GL_BLEND);
 		
 	}
 	
 	
 	FloatBuffer quadVertexBuffer;
 	FloatBuffer quadTexcoordsBuffer;
 	private void drawShadowTexturedQuad(Sd3dShader shader, int[] texture)
 	{		
 		
 		float vertices[] = {
 				//0f, (float)this.screenHeight, 0f,        					  // V1 - first vertex (x,y,z)
 				//(float)this.screenWidth, (float)this.screenHeight, 0f,        // V2 - second vertex
 				0f,  0f,  0f,                                                 // V3 - third vertex	
 				(float)this.screenWidth, (float)this.screenHeight, 0f,        // V2 - second vertex	
 				0f, (float)this.screenHeight, 0f,        					  // V1 - first vertex (x,y,z)
 				//(float)this.screenWidth, (float)this.screenHeight, 0f,        // V1 - first vertex (x,y,z)
 				//(float)this.screenWidth, 0f,  0f,                             // V2 - second vertex
 				0f,  0f,  0f,                                                  // V3 - third vertex	
 				(float)this.screenWidth, 0f,  0f,                             // V2 - second vertex		
 				(float)this.screenWidth, (float)this.screenHeight, 0f,        // V1 - first vertex (x,y,z)	
 			
 		};		
 		
 		float texcoords[] = {
 				0f,  0f,
 				1f,  1f,
 				0f,  1f,
 				0f,  0f,		
 				1f,  0f,
 				1f,  1f,					
 		};
 		
 		
 		IntBuffer pixelBuffer = IntBuffer.wrap(texture);
 		IntBuffer buffer = IntBuffer.allocate(1);	
 		GLES20.glGenTextures(1, buffer); 
 		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, buffer.get(0)); 
 		GLES20.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_FALSE);
 		GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,this.screenWidth, this.screenHeight, 0, 
 			GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixelBuffer);
 		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
 		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); 
 		GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S,GL11.GL_CLAMP_TO_EDGE);		
 		
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, buffer.get(0));
 		if (quadVertexBuffer == null)
 		{
 		  ByteBuffer quadVertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
 		  quadVertexByteBuffer.order(ByteOrder.nativeOrder());
 		
           // allocates the memory from the byte buffer
 		  quadVertexBuffer = quadVertexByteBuffer.asFloatBuffer();
 	    
 
 	      
 		  ByteBuffer texcoordsByteBuffer = ByteBuffer.allocateDirect(texcoords.length * 4);
 		  texcoordsByteBuffer.order(ByteOrder.nativeOrder());
 		
           // allocates the memory from the byte buffer
 		  quadTexcoordsBuffer = texcoordsByteBuffer.asFloatBuffer();
 	    
 		  // fill the vertexBuffer with the vertices
 		  quadTexcoordsBuffer.put(texcoords);
 	    
 	      // set the cursor position to the beginning of the buffer	
 		  quadTexcoordsBuffer.position(0);			      
 		}
 		
 		  // fill the vertexBuffer with the vertices
 		  quadVertexBuffer.put(vertices);
 	    
 	      // set the cursor position to the beginning of the buffer	
 		  quadVertexBuffer.position(0);		
 		  
 		
 		//OpenGL stuffs	
 		Matrix.setIdentityM(shader.modelMatrix, 0);
 		//Matrix.setIdentityM(mViewMatrix, 0);
 		
 		shader.renderStateVector.put(2, 0);//NO LIGHT	
 		shader.renderStateVector.put(1, 2);//HAS COLOR UNIFORM
 		shader.renderStateVector.put(0, 0);//NO TEXTURE
 		
 		//Colors 
 		shader.colorVector.put(0,0f);
 		shader.colorVector.put(1,0f);
 		shader.colorVector.put(2,0f);
 		shader.colorVector.put(3,0.3f);
 		
 	    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
 	    // (which currently contains model * view).
 	    //Matrix.multiplyMM(mMVMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
 	    //Matrix.multiplyMM(mMVMatrix, 0, mModelMatrix, 0,mViewMatrix, 0);
 	 
 	    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
 	    // (which now contains model * view * projection).
 		Matrix.orthoM(shader.projectionOrthoMatrix, 0, 0, this.screenWidth, this.screenHeight, 0, -1, 1);
 	    Matrix.multiplyMM(shader.MVPMatrix, 0, shader.projectionOrthoMatrix , 0, shader.modelMatrix, 0);			
 	  
 
 	    shader.bind();
 	    
 
 	    GLES20.glUniformMatrix4fv(shader.getMVMatrixHandle(), 1, false, shader.MVMatrix, 0);
 	    GLES20.glUniformMatrix4fv(shader.getMVPMatrixHandle(), 1, false, shader.MVPMatrix, 0);
 	    GLES20.glUniform4iv(shader.getRenderStateVectorHandle(), 1, shader.renderStateVector);		
 	    GLES20.glUniform4fv(shader.getColorVectorHandle(), 1, shader.colorVector);	
 	    
 		
 		GLES20.glEnable(GL11.GL_BLEND);
 		GLES20.glBlendFunc(GL11.GL_SRC_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR);
 		
 		GLES20.glVertexAttribPointer(Sd3dShader.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, 0, quadVertexBuffer);
 		GLES20.glEnableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		
 		GLES20.glVertexAttribPointer(Sd3dShader.vertexTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, quadTexcoordsBuffer);
 	    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);				
 		
 		GLES20.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
 		
 		GLES20.glDisableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		GLES20.glDisableVertexAttribArray(Sd3dShader.vertexNormalHandle);
 		GLES20.glDisableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);
 		GLES20.glDisable(GL11.GL_BLEND);
 		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);
 		GLES20.glDeleteTextures(1, buffer);
 		//GLES20.glDeleteBuffers(1, buffer.array(), 0);
 	}	
 	
 	
 	private void drawHorizontalPlane(Sd3dScene scene,Sd3dShader shader)
 	{		
 		float height = -5f;
 		float size = 100f;
 		float vertices[] = {	
 				-size,  height,  -size,                             		
 				size, height, size, 
 				-size, height, size,
 				-size,  height,  -size,
 				size, height,  -size,
 				size, height, size,		};		
 		
 		/*
 		float vertices[] = {	
  
 				-10f, height, 10f,				
 				10f, height, 10f, 
 				-10f,  height,  -10f,  
 				-10f,  height,  -10f,
 				10f, height,  -10f,
 				10f, height, 10f,		};				
 		*/
 		if (vertexBuffer == null)
 		{
 		  ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
 		  vertexByteBuffer.order(ByteOrder.nativeOrder());
 		
           // allocates the memory from the byte buffer
 	      vertexBuffer = vertexByteBuffer.asFloatBuffer();
 	    
 		  // fill the vertexBuffer with the vertices
 	      vertexBuffer.put(vertices);
 	    
 	      // set the cursor position to the beginning of the buffer	
 	      vertexBuffer.position(0);		
 		}
 		
 		//OpenGL stuffs	
 		Matrix.setIdentityM(shader.modelMatrix, 0);
 		//Matrix.setIdentityM(mViewMatrix, 0);
 		Matrix.translateM(shader.modelMatrix, 0, 
 				scene.getCamera().getPosition()[0], 
 				scene.getCamera().getPosition()[1], 
 				scene.getCamera().getPosition()[2]);
 		
 		Log.d("BallJump",scene.getCamera().getPosition()[0]+" - "+scene.getCamera().getPosition()[2]);
 		shader.renderStateVector.put(2, 0);//NO LIGHT	
 		shader.renderStateVector.put(1, 2);//HAS COLOR UNIFORM
 		shader.renderStateVector.put(0, 0);//NO TEXTURE
 		
 		//Colors 
 		shader.colorVector.put(0,1f);
 		shader.colorVector.put(1,1f);
 		shader.colorVector.put(2,1f);
 		shader.colorVector.put(3,1.0f);
 		
 	    Matrix.multiplyMM(shader.MVPMatrix, 0, shader.projectionMatrix, 0, shader.modelMatrix, 0);			
 		
 	    
 	    shader.bind();
 	    
 
 	    GLES20.glUniformMatrix4fv(shader.getMVMatrixHandle(), 1, false, shader.MVMatrix, 0);
 	    GLES20.glUniformMatrix4fv(shader.getMVPMatrixHandle(), 1, false, shader.MVPMatrix, 0);
 	    GLES20.glUniform4iv(shader.getRenderStateVectorHandle(), 1, shader.renderStateVector);		
 	    GLES20.glUniform4fv(shader.getColorVectorHandle(), 1, shader.colorVector);	
 	    
 		
 		//GLES20.glEnable(GL11.GL_BLEND);
 		//GLES20.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		
 		GLES20.glVertexAttribPointer(Sd3dShader.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer);
 		GLES20.glEnableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		
 		GLES20.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
 		
 		GLES20.glDisableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		
 		//GLES20.glDisable(GL11.GL_BLEND);		
 	}	
 	
 	
 	private float matrix[] = new float[16];
 
 	
 	public void renderRenderElementToDepth(Sd3dRendererElement element,Sd3dShader shader)
 	{	
 		
 		Matrix.setIdentityM(mLocalTransform, 0);
 		
 		Matrix.invertM(mTmpMatrix, 0, shader.modelMatrix, 0);
 		System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 		
 		if (element.mRenderLight)
 		{				
 			shader.renderStateVector.put(2, 1);//LIGHT	
 		}		
 		{
 			shader.renderStateVector.put(2, 0);//NO LIGHT	
 		}
 
 		if (!element.mIsShadowVolume)
 			if (element.mOrientation != null)
 			{
 				Sd3dMatrix.setRotateEulerM(shader.normalMatrix, 0, element.mOrientation[0], element.mOrientation[1], element.mOrientation[2]);
 				Matrix.multiplyMM(mTmpMatrix, 0, shader.normalMatrix, 0, mLocalTransform,0);
 				System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);
 			}	
 			
 		if (element.mTransformMatrix != null)
 		{
 			//System.arraycopy(element.mTransformMatrix, 0, shader.normalMatrix, 0, 16);
 			Matrix.multiplyMM(mTmpMatrix, 0, shader.normalMatrix, 0, element.mTransformMatrix,0);
 			System.arraycopy(mTmpMatrix, 0, shader.normalMatrix, 0, 16);
 			//System.arraycopy(element.mTransformMatrix, 0, shader.modelMatrix, 0, 16);	
 			
 			Matrix.multiplyMM(mTmpMatrix, 0, element.mTransformMatrix,0, mLocalTransform, 0);
 			System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);
 		}			
 		
 		if (element.mPosition != null)
 		{
 			Matrix.setIdentityM(matrix, 0);
 			Matrix.translateM(matrix, 0, element.mPosition[0], element.mPosition[1], element.mPosition[2]);	
 			
 			//Matrix.multiplyMM(mTmpMatrix, 0, matrix, 0, shader.normalMatrix,0);
 			//System.arraycopy(mTmpMatrix, 0, matrix, 0, 16);
 			
 			Matrix.multiplyMM(mTmpMatrix, 0, matrix, 0, mLocalTransform,0);
 			System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);
 		}			
 		
 		Matrix.invertM(mTmpMatrix, 0, mLocalTransform, 0);
 		System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);			
 		
 		//Matrix.multiplyMM(mTmpMatrix, 0, shader.modelMatrix, 0, mLocalTransform,0);
 		Matrix.multiplyMM(mTmpMatrix, 0, mLocalTransform,0, shader.modelMatrix, 0);
 		System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);		
 		
 		Matrix.invertM(mTmpMatrix, 0, shader.modelMatrix, 0);
 		System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);						
 	
 		if (element.mIsBillboard)
 		{
 			//this.billboardCheatSphericalBegin();
 		}
 		
         // draw using hardware buffers             
 		if (element.mVertexBufferName != 0)
 		{
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, element.mVertexBufferName);    
 			//GLES20.glVertexAttribPointer(this.mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, Sd3dMesh.nbFloatPerVertex*4, 0);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		    
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexNormalHandle, 3, GLES20.GL_FLOAT, false, Sd3dMesh.nbFloatPerVertex*4, 3*4);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexNormalHandle);	
 		    
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexTexCoordHandle, 2, GLES20.GL_FLOAT, false, Sd3dMesh.nbFloatPerVertex*4, 6*4);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);		    
 		    
 		}
 		
 		if (element.mAlphaTest)
 		{		
 			GLES20.glEnable(GL11.GL_ALPHA_TEST);
 		}
 
 		if (element.mAlphaBlending)
 		{		
 			GLES20.glDepthMask(false);
 			GLES20.glEnable (GL11.GL_BLEND);
 			GLES20.glBlendFunc( GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR );
 		}
 		
 		
 	    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
 	    // (which currently contains model * view).    
 		Matrix.multiplyMM(shader.MVMatrix, 0, shader.viewMatrix, 0, shader.modelMatrix, 0);
 	    				
 		
 	    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
 	    // (which now contains model * view * projection).
 		//System.arraycopy(shader.projectionMatrix, 0, this.defaultShader.projectionMatrix, 0, 16);
 		System.arraycopy(this.defaultShader.projectionMatrix, 0, shader.projectionMatrix, 0, 16);
 		
 	    Matrix.multiplyMM(shader.MVPMatrix, 0, shader.projectionMatrix, 0, shader.MVMatrix, 0);	
 	    
 	    GLES20.glUniformMatrix4fv(shader.getMVPMatrixHandle(), 1, false, shader.MVPMatrix, 0);
 	    	
 		
 	    if (element.mIndiceBufferName != 0)
 		{		
 			GLES20.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, element.mIndiceBufferName);             
 			GLES20.glDrawElements(GL11.GL_TRIANGLES, element.mIndiceCount,GL11.GL_UNSIGNED_SHORT, 0);
 		}	
 		
 		if (element.mAlphaTest)
 		{		
 			GLES20.glDisable(GL11.GL_ALPHA_TEST);						
 		}
 	
 
 
     	GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);
     	GLES20.glDisableVertexAttribArray(Sd3dShader.vertexNormalHandle);
     	GLES20.glDisableVertexAttribArray(Sd3dShader.vertexColorHandle);
 
 	    	    
 	    
 	    
 		GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);             
 		GLES20.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0); 	
 		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);	
 		
 		if (element.mAlphaBlending)
 		{		
 			GLES20.glDisable (GL11.GL_BLEND);
 			GLES20.glDepthMask(true);
 		}
 				
 		if (element.mIsBillboard)
 		{
 			//this.billboardEnd();
 		}			
 					
 		//Hierachical objects management
 		if ((element.mObject != null)&&(element.mObject.mChild != null)&&(element.mObject.mChild.mRenderElement != null))
 		{
 			Sd3dRendererElement elements[] = element.mObject.mChild.mRenderElement;
 			for (int i = 0; i < elements.length;i++)
 			{
 				float tmpModel[] = new float[16];
 				float tmpNormal[] = new float[16];
 				System.arraycopy(shader.modelMatrix, 0, tmpModel, 0, 16);
 				System.arraycopy(shader.normalMatrix, 0, tmpNormal, 0, 16);
 				
 				//Matrix.transposeM(mTmpMatrix, 0, shader.modelMatrix, 0);
 				//System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 				
 				this.renderRenderElementToDepth(element.mObject.mChild.mRenderElement[i],shader);
 				System.arraycopy(tmpModel, 0, shader.modelMatrix, 0, 16);
 				System.arraycopy(tmpNormal, 0, shader.normalMatrix, 0, 16);				
 			}
 		}
 							
 	}	
 	
 	private float mTmpMatrix[] = new float[16];
 	private float mLocalTransform[] = new float[16];
 	public void renderRenderElement(Sd3dRendererElement element,Sd3dShader shader)
 	{
 		Matrix.setIdentityM(mLocalTransform, 0);
 		
 		Matrix.invertM(mTmpMatrix, 0, shader.modelMatrix, 0);
 		System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 		
 
 		
 		if (!element.mIsShadowVolume)
 		/*
 		if (element.mOrientation != null)
 		{
 			Sd3dRendererGl20.setRotateEulerM(shader.normalMatrix, 0, element.mOrientation[0], element.mOrientation[1], element.mOrientation[2]);
 			Matrix.multiplyMM(shader.modelMatrix, 0, shader.normalMatrix, 0, shader.modelMatrix,0);
 			//System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 		}
 		*/
 			if (element.mOrientation != null)
 			{
 				Sd3dMatrix.setRotateEulerM(shader.normalMatrix, 0, element.mOrientation[0], element.mOrientation[1], element.mOrientation[2]);
 				//Log.d("EULERM",element.mOrientation[0] + " " + element.mOrientation[1] + " " + element.mOrientation[2]);
 				Matrix.multiplyMM(mTmpMatrix, 0, shader.normalMatrix, 0, mLocalTransform,0);
 				System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);
 				
 				
 			}	
 			
 		if (element.mTransformMatrix != null)
 		{
 			//System.arraycopy(element.mTransformMatrix, 0, shader.normalMatrix, 0, 16);
 			Matrix.multiplyMM(mTmpMatrix, 0, shader.normalMatrix, 0, element.mTransformMatrix,0);
 			System.arraycopy(mTmpMatrix, 0, shader.normalMatrix, 0, 16);
 			//System.arraycopy(element.mTransformMatrix, 0, shader.modelMatrix, 0, 16);	
 			
 			Matrix.multiplyMM(mTmpMatrix, 0, element.mTransformMatrix,0, mLocalTransform, 0);
 			System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);
 		}			
 		
 		if (element.mPosition != null)
 		{
 			Matrix.setIdentityM(matrix, 0);
 			Matrix.translateM(matrix, 0, element.mPosition[0], element.mPosition[1], element.mPosition[2]);	
 			
 			//Matrix.multiplyMM(mTmpMatrix, 0, matrix, 0, shader.normalMatrix,0);
 			//System.arraycopy(mTmpMatrix, 0, matrix, 0, 16);
 			
 			Matrix.multiplyMM(mTmpMatrix, 0, matrix, 0, mLocalTransform,0);
 			System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);
 		}	
 		/*
 		if (element.mTransformMatrix != null)
 		{
 			//System.arraycopy(element.mTransformMatrix, 0, shader.normalMatrix, 0, 16);
 			Matrix.multiplyMM(mTmpMatrix, 0, shader.normalMatrix, 0, element.mTransformMatrix,0);
 			System.arraycopy(mTmpMatrix, 0, shader.normalMatrix, 0, 16);
 			//System.arraycopy(element.mTransformMatrix, 0, shader.modelMatrix, 0, 16);	
 			
 			Matrix.multiplyMM(mTmpMatrix, 0, shader.modelMatrix, 0, element.mTransformMatrix,0);
 			System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 		}			
 		
 		if (element.mPosition != null)
 		{
 			Matrix.setIdentityM(matrix, 0);
 			Matrix.translateM(matrix, 0, element.mPosition[0], element.mPosition[1], element.mPosition[2]);	
 			
 			//Matrix.multiplyMM(mTmpMatrix, 0, matrix, 0, shader.normalMatrix,0);
 			//System.arraycopy(mTmpMatrix, 0, matrix, 0, 16);
 			
 			Matrix.multiplyMM(mTmpMatrix, 0, matrix, 0, shader.modelMatrix,0);
 			System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 		}				
 		*/
 		
 		
 		
 		Matrix.invertM(mTmpMatrix, 0, mLocalTransform, 0);
 		System.arraycopy(mTmpMatrix, 0, mLocalTransform, 0, 16);			
 
 		Matrix.multiplyMM(mTmpMatrix, 0, mLocalTransform,0, shader.modelMatrix, 0);
 		System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);		
 		
 		Matrix.invertM(mTmpMatrix, 0, shader.modelMatrix, 0);
 		System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);		
 	
 		
 	
 		
 		
 
 		if (element.mIsBillboard)
 		{
 			//this.billboardCheatSphericalBegin();
 		}
 		
         // draw using hardware buffers             
 		if (element.mVertexBufferName != 0)
 		{
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, element.mVertexBufferName);    
 			//GLES20.glVertexAttribPointer(this.mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, Sd3dMesh.nbFloatPerVertex*4, 0);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 		    
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexNormalHandle, 3, GLES20.GL_FLOAT, false, Sd3dMesh.nbFloatPerVertex*4, 3*4);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexNormalHandle);	
 		    
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexTexCoordHandle, 2, GLES20.GL_FLOAT, false, Sd3dMesh.nbFloatPerVertex*4, 6*4);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);		    
 		    
 		}
 
 		if (element.mTangentBinormalBufferName != 0)
 		{
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, element.mTangentBinormalBufferName);    
 			//GLES20.glVertexAttribPointer(this.mPositionHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexTangentHandle, 3, GLES20.GL_FLOAT, false, 6*4, 0);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTangentHandle);
 		    
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexBinormalHandle, 3, GLES20.GL_FLOAT, false, 6*4, 3*4);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexBinormalHandle);	
 		}		
 		
 		
 		/*
 		if (element.mNormalBufferName != 0)
 		{
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, element.mNormalBufferName);            	
 			//GLES20.glVertexAttribPointer(this.mNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
 			GLES20.glVertexAttribPointer(this.vertexNormalHandle, 3, GLES20.GL_FLOAT, false, 0, 0);
 		    GLES20.glEnableVertexAttribArray(vertexNormalHandle);	
 		} 
 		else GLES20.glDisableVertexAttribArray(vertexNormalHandle);	 
 		
 		if (element.mTexCoordBufferName != 0)
 		{
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, element.mTexCoordBufferName);             
 			GLES20.glVertexAttribPointer(this.vertexTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, 0);
 		    GLES20.glEnableVertexAttribArray(vertexTexCoordHandle);
 		}
 		else GLES20.glDisableVertexAttribArray(vertexTexCoordHandle);	 
 		*/
 		if (element.mObject.mMaterial[0].mColorName != 0)
 		{		
 			GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, element.mObject.mMaterial[0].mColorName);  
 			//GLES20.glVertexAttribPointer(this.mColorHandle, 4, GLES20.GL_FLOAT, false, 0, 0);
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexColorHandle, 4, GLES20.GL_FLOAT, false, 0, 0);
 		    GLES20.glEnableVertexAttribArray(Sd3dShader.vertexColorHandle);
 		    shader.renderStateVector.put(1, 1);
 		}
 		else
 		{	
 			if (element.mObject.mMaterial[0].mColors != null)
 			{
 				Log.d("renderRenderElement()","HAS COLOR POINTER BUT NO COLOR NAME");
 				registerMaterial(element,element.mObject.mMaterial[0]);
 			}
 			else
 			{
 				GLES20.glDisableVertexAttribArray(Sd3dShader.vertexColorHandle);
 			}
 			shader.renderStateVector.put(1, 0);
 		}
 	
 		if (element.mTextureName[0] != 0)
 		{
 			GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 			GLES20.glBindTexture(GL11.GL_TEXTURE_2D, element.mTextureName[0]);
 			//GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,shadowMapping.renderTex[0]);
 			shader.renderStateVector.put(0, 1);
 		} 	
 		else 
 		{
 			shader.renderStateVector.put(0, 0);
 		}
 		
 		if (element.mTextureName[1] != 0)
 		{
 			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
 			GLES20.glBindTexture(GL11.GL_TEXTURE_2D, element.mTextureName[1]);
 			GLES20.glUniform1i(GLES20.glGetUniformLocation(shader.getProgramHandle(), "secondary_texture"), 1);			
 			//GLES20.glBindTexture(GLES20.GL_TEXTURE_2D,shadowMapping.renderTex[0]);
 			shader.renderStateVector.put(0, 1);
 		} 	
 		else 
 		{
 			shader.renderStateVector.put(0, 0);
 		}
 				
 		
 		if (this.useShadowMapping)
 		{
 			// send the depth texture
 			GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
 			GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowMapping.renderTex[0]);
 			GLES20.glUniform1i(GLES20.glGetUniformLocation(shader.getProgramHandle(), "shadow_texture"), 1);
 		}
 		
 		if (element.mAlphaTest)
 		{		
 			GLES20.glEnable(GL11.GL_ALPHA_TEST);
 		}
 
 		if (element.mAlphaBlending)
 		{		
 			GLES20.glDepthMask(false);
 			GLES20.glEnable (GL11.GL_BLEND);
 			GLES20.glBlendFunc( GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_COLOR );
 		}
 		
 		
 		
 		
 	    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
 	    // (which currently contains model * view).    
 		Matrix.multiplyMM(shader.MVMatrix, 0, shader.viewMatrix, 0, shader.modelMatrix, 0);
 		
 	    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
 	    // (which now contains model * view * projection).
 	    Matrix.multiplyMM(shader.MVPMatrix, 0, shader.projectionMatrix, 0, shader.MVMatrix, 0);	
 	    
 		if (this.useShadowMapping)
 		{
 			//Computing shadow projection matrix
 			Matrix.multiplyMM(shadowMapping.shader.MVMatrix,0, shadowMapping.shader.viewMatrix,0,shader.modelMatrix,0);	 
 			Matrix.multiplyMM(shader.shadowTextureMatrix,0,shader.projectionMatrix,0, shadowMapping.shader.MVMatrix, 0);
 		}
 		
 		if (element.mRenderLight)
 		{			
 			shader.renderStateVector.put(2, 1);			
 		}
 		else
 		{
 			shader.renderStateVector.put(2, 0);	
 		}	 	    
 	    
 		if (element.mPickingColor != null){
 			//Log.d("","PICKING "+shader.getColorVectorHandle());
 			shader.colorVector.put(0, (float)element.mPickingColor[0]/256.0f);
 			shader.colorVector.put(1, (float)element.mPickingColor[1]/256.0f);
 			shader.colorVector.put(2, (float)element.mPickingColor[2]/256.0f);
 		} else {
 			//Log.d("","PICKING COLOR IS NULL");
 		}
 	
 		
 	    if (element.mRendererInterface != null)
 	    {
 	    	element.mRendererInterface.prerender(shader.camPos, shader.MVMatrix,shader.MVPMatrix,shader.projectionMatrix,shader.normalMatrix,shader.renderStateVector);
 	    }
 	    else
 	    {
 
 	    	//Matrix.transposeM(mTmpMatrix, 0, shader.MVMatrix, 0);
 		    GLES20.glUniformMatrix4fv(shader.getMVMatrixHandle(), 1, false, shader.MVMatrix, 0);
 		    
 		    GLES20.glUniformMatrix4fv(shader.getMVPMatrixHandle(), 1, false, shader.MVPMatrix, 0);
 		    //GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mShadowTextureMatrix, 0);
 		    
 		    GLES20.glUniformMatrix4fv(shader.getNormalMatrixHandle(), 1, false, shader.normalMatrix, 0);
 		    
 		    //GLES20.glUniformMatrix4fv(mShadowTextureMatrixHandle, 1, false, mShadowTextureMatrix, 0);
 		    GLES20.glUniformMatrix4fv(shader.getShadowTextureMatrixHandle(), 1, false,shader.shadowTextureMatrix,0);
 		    GLES20.glUniform4iv(shader.getRenderStateVectorHandle(), 1, shader.renderStateVector);	
 		  
 	    	GLES20.glUniform4fv(shader.getColorVectorHandle(), 1, shader.colorVector);		    
 		        	
 	    }
 	    	
 		
 	    if (element.mIndiceBufferName != 0)
 		{		
 			GLES20.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, element.mIndiceBufferName);             
 			GLES20.glDrawElements(GL11.GL_TRIANGLES, element.mIndiceCount,GL11.GL_UNSIGNED_SHORT, 0);
 		}	
 	    
 	    
 		if (element.mAlphaTest)
 		{		
 			GLES20.glDisable(GL11.GL_ALPHA_TEST);						
 		}
 	
 	    if (element.mRendererInterface != null)
 	    {
 	    	element.mRendererInterface.postrender();
 	    }
 	    else
 	    {
 	    	GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);
 	    	GLES20.glDisableVertexAttribArray(Sd3dShader.vertexNormalHandle);
 	    	GLES20.glDisableVertexAttribArray(Sd3dShader.vertexColorHandle);
 	    }
 	    	    
 	    
 	    
 		GLES20.glBindBuffer(GL11.GL_ARRAY_BUFFER, 0);             
 		GLES20.glBindBuffer(GL11.GL_ELEMENT_ARRAY_BUFFER, 0); 	
 		GLES20.glBindTexture(GL11.GL_TEXTURE_2D, 0);	
 		
 		if (element.mAlphaBlending)
 		{		
 			GLES20.glDisable (GL11.GL_BLEND);
 			GLES20.glDepthMask(true);
 		}
 				
 		if (element.mIsBillboard)
 		{
 			//this.billboardEnd();
 		}			
 					
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);	
 		
 		//Hierachical objects management
 		if ((element.mObject != null)&&(element.mObject.mChild != null)&&(element.mObject.mChild.mRenderElement != null))
 		{
 			Sd3dRendererElement elements[] = element.mObject.mChild.mRenderElement;
 			for (int i = 0; i < elements.length;i++)
 			{
 				float tmpModel[] = new float[16];
 				float tmpNormal[] = new float[16];
 				System.arraycopy(shader.modelMatrix, 0, tmpModel, 0, 16);
 				System.arraycopy(shader.normalMatrix, 0, tmpNormal, 0, 16);
 				
 				//Matrix.transposeM(mTmpMatrix, 0, shader.modelMatrix, 0);
 				//System.arraycopy(mTmpMatrix, 0, shader.modelMatrix, 0, 16);
 				
 				this.renderRenderElement(element.mObject.mChild.mRenderElement[i],shader);
 				System.arraycopy(tmpModel, 0, shader.modelMatrix, 0, 16);
 				System.arraycopy(tmpNormal, 0, shader.normalMatrix, 0, 16);				
 			}
 		}	
 	}
 	
 	
 	public int registerFontMaterial()
 	{
 		if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
 		{
 			throw new RuntimeException("registerMaterial() ENTERING: ERROR ON OPENGL ES CALL");
 		}				
 		
 		//OpenGL stuffs	
 		IntBuffer buffer = IntBuffer.allocate(1);		
 		
 		if (this.mBmpFont.mMaterial[0].mTextureData != null)
 		{
 			GLES20.glGenTextures(1, buffer); 
 			GLES20.glBindTexture(GL11.GL_TEXTURE_2D, buffer.get(0)); 
 			//GLES20.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
 			GLES20.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA,this.mBmpFont.mMaterial[0].mWidth, this.mBmpFont.mMaterial[0].mHeight, 0, 
 					GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, this.mBmpFont.mMaterial[0].mTextureData);
 			GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR); 
 			GLES20.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR); 
 			
 			
 			this.mBmpFont.mTextureName = buffer.get(0);
 		}
 		
 		
 		if (GLES20.glGetError() != GLES20.GL_NO_ERROR)
 		{
 			throw new RuntimeException("registerMaterial() EXITING: ERROR ON OPENGL ES CALL");
 		}		
 		
 		return 0;
 	}	
 	
 
 
 	/*
 	public void renderRenderElementPickable(Sd3dRendererElement element)
 	{
 	}
 	
 	
 	public void billboardCheatSphericalBegin()
 	{	
 	}
 	
 	public void billboardEnd()
 	{
 		// restore the previously 
 		// stored modelview matrix
 		//mGl.glPopMatrix();		
 	}
 	*/
 	private void renderRenderListShadowVolume(Sd3dShader shader)
 	{
 		shader.bind();
 		
 		for (int i = 0; i < mCountRenderElement;i++)
 		{
 			if (mRenderList[i].mIsShadowVolume){
 				
 				Matrix.setIdentityM(shader.modelMatrix, 0);
 				Matrix.setIdentityM(shader.normalMatrix, 0);				
 				
 			  renderRenderElement(mRenderList[i],shader);
 			}
 		}	
 		
 		shader.unbind();
 	}
 	
 	/*
 	public void renderRenderListPickable()
 	{
 		for (int i = 0; i < mCountRenderElement;i++)
 		{
 			if (mRenderList[i].mIsPickable){
 			  renderRenderElementPickable(mRenderList[i]);
 			}
 		}		
 	}*/
 	
 	private void renderRenderInScreenSpaceList(Sd3dShader shader)
 	{
 		
 		shader.bind();		
 		for (int i = 0; i < mCountRenderElement;i++)
 		{
 			if ((!mRenderList[i].mIsShadowVolume)&&(mRenderList[i].mIsInScreenSpace))
 			{
 			    Matrix.setIdentityM(shader.modelMatrix, 0);
 				Matrix.setIdentityM(shader.normalMatrix, 0);				
 				
 				renderRenderElement(mRenderList[i],shader);
 			}
 		}	
 		shader.unbind();		
 	}
 	
 	public void renderRenderListToDepth(Sd3dShader shader)
 	{
 		for (int i = 0; i < mCountRenderElement;i++)
 		{
 			if (mRenderList[i].mRenderLight)
 			if ((!mRenderList[i].mIsShadowVolume)&&(!mRenderList[i].mIsInScreenSpace))
 			{
 		      Matrix.setIdentityM(shader.modelMatrix, 0);
 			  Matrix.setIdentityM(shader.normalMatrix, 0);
 				
 			  renderRenderElementToDepth(mRenderList[i],shader);
 			}
 		}
 	}	
 	
 	public void renderRenderList(Sd3dShader shader,int lightPass)
 	{
 		Sd3dShader currentShader = shader;
 		for (int i = 0; i < mCountRenderElement;i++)
 		{
 			//if ((lightPass > 0)&&(!mRenderList[i].mRenderLight)) 
 			//		continue;
 			
 			if ((!mRenderList[i].mIsShadowVolume)&&(!mRenderList[i].mIsInScreenSpace))
 			{
 				if (mRenderList[i].mShaderName != null) {
 					currentShader = this.shaderMap.get(mRenderList[i].mShaderName);
 					if (currentShader != null) {
 						System.arraycopy(shader.viewMatrix, 0, currentShader.viewMatrix, 0, 16);
 						shader.unbind();
 						currentShader.bind();
 					}
 				}
 				
 				Matrix.setIdentityM(currentShader.modelMatrix, 0);
 				Matrix.setIdentityM(currentShader.normalMatrix, 0);
 				
 				if (mRenderList[i].mRenderLight)
 				{				
 					currentShader.renderStateVector.put(2, 1);//LIGHT	
 				}		
 				{
 					currentShader.renderStateVector.put(2, 0);//NO LIGHT	
 				}				
 				
 				renderRenderElement(mRenderList[i],currentShader);
 				
 				if (mRenderList[i].mShaderName != null) {
 					currentShader.unbind();
 					shader.bind();
 				}
 			}
 		}
 	}
 	
 	
 	public void renderShadowVolumeScene(Sd3dScene scene,Sd3dShader shader)
 	{
 		
 		Matrix.setIdentityM(shader.viewMatrix, 0);
 		
 		if (scene.getCamera().getRotationMatrix() == null)
 		{
 			float rot[] = scene.getCamera().getOrientation();           
 			Matrix.rotateM(shader.viewMatrix, 0, rot[0], 1.0f, 0.0f, 0.0f);
 			Matrix.rotateM(shader.viewMatrix, 0, rot[1], 0.0f, 1.0f, 0.0f);
 			Matrix.rotateM(shader.viewMatrix, 0, rot[2], 0.0f, 0.0f, 1.0f);
 		}
 		else
 		{
 			//mGl.glLoadMatrixf(scene.getCamera().getRotationMatrix(), 0);
 			
 			//GLU.gluLookAt(mGl, 0, 0, 2, 0, 0, 0, 0, 1, 0);
 			Matrix.setLookAtM(matrix, 0, 0, 0, 2, 0, 0, 0, 0, 1, 0);
 			Matrix.multiplyMM(shader.viewMatrix, 0, scene.getCamera().getRotationMatrix(), 0, matrix, 0);
 		}
 			
         float pos[] = scene.getCamera().getPosition();
 
         Matrix.translateM(shader.viewMatrix, 0, -pos[0], -pos[1], -pos[2]);
 		
 		
 		
 		
         //Copy camera position
         shader.camPos.position(0);
         shader.camPos.put(pos[0]);
         shader.camPos.put(pos[1]);
         shader.camPos.put(pos[2]);
         shader.camPos.put(0.f);
         
        
         this.setupShadowVolumeStep1();
 		this.setupShadowVolumeStep2();
 		this.renderRenderListShadowVolume(shader);
 		this.setupShadowVolumeStep3();
 		
 		this.renderRenderListShadowVolume(shader);        
 
 		this.setupShadowVolumeStep4();
 		
 		drawShadowVolumeQuad(colorOnlyShader);
 
 		this.setupShadowVolumeStep5();
 
         
         
 	}	
 	
 	public void renderPickableScene(Sd3dScene scene)
 	{
 
 		System.arraycopy(defaultShader.projectionMatrix, 0, pickingShader.projectionMatrix, 0, 16);
 		
 		Matrix.setIdentityM(pickingShader.viewMatrix, 0);
 		
 		if (scene.getCamera().getRotationMatrix() == null)
 		{
 			float rot[] = scene.getCamera().getOrientation();           
 			Matrix.rotateM(pickingShader.viewMatrix, 0, rot[0], 1.0f, 0.0f, 0.0f);
 			Matrix.rotateM(pickingShader.viewMatrix, 0, rot[1], 0.0f, 1.0f, 0.0f);
 			Matrix.rotateM(pickingShader.viewMatrix, 0, rot[2], 0.0f, 0.0f, 1.0f);
 		}
 			
         float pos[] = scene.getCamera().getPosition();
 
         Matrix.translateM(pickingShader.viewMatrix, 0, -pos[0], -pos[1], -pos[2]);
 				
         
         pickingShader.bind();
         Log.d("PICKING SHADER =",""+pickingShader.getColorVectorHandle());
 		for (int i = 0; i < mCountRenderElement;i++)
 		{
 			if (mRenderList[i].mIsPickable){
 				Matrix.setIdentityM(pickingShader.modelMatrix, 0);
 				Matrix.setIdentityM(pickingShader.normalMatrix, 0);				
 				
 				renderRenderElement(mRenderList[i],pickingShader);
 			}
 		}	
 		
 		pickingShader.unbind();
         
 	}
 	
 
 	private void renderSceneFromLightPOV(Sd3dLight light, Sd3dScene scene,Sd3dShader shader)
 	{
 		GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		/*
 		if (this.clearScreen)
 			GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		else
 			GLES20.glClear(GL11.GL_DEPTH_BUFFER_BIT);
 			*/
         GLES20.glFrontFace(GL11.GL_CW);
  
         if (light.getLighType() == LightType.DIRECTION)
         {
 			Matrix.setLookAtM(shader.viewMatrix, 0, 
 					scene.getCamera().getPosition()[0] - light.getPosition()[0], scene.getCamera().getPosition()[1] - light.getPosition()[1], scene.getCamera().getPosition()[2] - light.getPosition()[2], 
 					scene.getCamera().getPosition()[0], scene.getCamera().getPosition()[1], scene.getCamera().getPosition()[2], 
 					0, 1, 0);
         }
         else
         {
 			Matrix.setLookAtM(shader.viewMatrix, 0, 
 					light.getPosition()[0], light.getPosition()[1], light.getPosition()[2], 
 					scene.getCamera().getPosition()[0], scene.getCamera().getPosition()[1], scene.getCamera().getPosition()[2], 
 					0, 1, 0);        	
         }
         renderRenderListToDepth(shader);	
 	}
 	
 	private ArrayList<Sd3dLight> lightList = new ArrayList<Sd3dLight>();
 	public void renderScene(Sd3dScene scene)
 	{
 		
 		
 		if ((shadowMapping == null)&&(this.useShadowMapping))
 		{
 			shadowMapping = new Sd3dShadowMapping();
 			shadowMapping.init(this.screenWidth,this.screenHeight);
 		}
 		if ((sampling == null)&&(this.useMultisampling))
 		{
 			sampling = new Sd3dSampling();
 			sampling.init(screenWidth, screenHeight);
 		}
 		
 		mCountRenderElement = 0;
 		for (int i = 0;i < scene.mCountObject;i++)
 		{
 			this.addObjectToRenderList(scene.mObjectList[i]);
 		}		
 		
 		if (this.sampling != null)
 			this.sampling.onRenderScene();			
 		
 		
 		if (this.clearScreen)
 			GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		else
 			GLES20.glClear(GL11.GL_DEPTH_BUFFER_BIT);		
 		
 		lightList.clear();
 		lightList.addAll(scene.getDirectionLight());
 		lightList.addAll(scene.getPositionLight());
 		int lightPass = 0;
 		//Log.d("sd3d","toto:"+lightList.size());
 			
 		
 		// Light pass count
 		defaultShader.renderStateVector.put(3,lightList.size());
 		
 		for (Sd3dLight activeLight : lightList)
 		{
 			/*
 			if (lightPass == 0)
 			{
 				lightPass++;
 				continue;
 			}
 			*/
 			//Log.d("sd3d","lightPass:"+lightPass);
 			if (this.useShadowMapping)
 			{
 				shadowMapping.onRenderToDepthTexture();
 				this.renderSceneFromLightPOV(activeLight,scene,this.shadowMapping.shader);
 				shadowMapping.onRenderToScreen(screenWidth,screenHeight);
 			}
 	
 			if (this.sampling != null)
 				this.sampling.onRenderScene();	
 			
 			defaultShader.bind();
 			
 			//Only Ambient in the first pass
 			if ((scene.getAmbientLight() != null))
 			{
 				float color[] = scene.getAmbientLight().getRGBA();
 				GLES20.glUniform4f(defaultShader.getLightAmbientHandle(), color[0], color[1], color[2], color[3]);
 			}
 			else
 			{
 				GLES20.glUniform4f(defaultShader.getLightAmbientHandle(), 0f, 0f, 0f, 0f);
 			}
 			
 			if (defaultShader.getLightDirHandle() != -1)
 			{
 				if (activeLight.getLighType() == LightType.DIRECTION)
 				{
 					float lightp[] = activeLight.getPosition();
 					GLES20.glUniform4f(defaultShader.getLightDirHandle(), lightp[0], lightp[1], lightp[2], 0f);
 				}
 				else
 				{
 					GLES20.glUniform4f(defaultShader.getLightDirHandle(), 0f, 0f, 0f, 0f);
 				}
 			}
 			
 			if (defaultShader.getLightPosHandle() != -1)
 			{
 				if (activeLight.getLighType() == LightType.POINT)
 				{
 					float lightp[] = activeLight.getPosition();
 					GLES20.glUniform4f(defaultShader.getLightPosHandle(), lightp[0], lightp[1], lightp[2], 0f);
 				}
 				else
 				{
 					GLES20.glUniform4f(defaultShader.getLightPosHandle(), 0f, 0f, 0f, 0f);
 				}
 			}
 			
 			
 
 			
 			//GLES20.glClearColor(0.4f, 0.4f, 0.0f, 1.0f);
 			
 
 	        GLES20.glFrontFace(GL11.GL_CW);
 	        GLES20.glCullFace(GL11.GL_BACK);
 	
 			Matrix.setIdentityM(defaultShader.viewMatrix, 0);
 	 
 		
 			
 			if (scene.getCamera().getRotationMatrix() == null)
 			{
 				
 				float rot[] = scene.getCamera().getOrientation();           
 				Matrix.rotateM(defaultShader.viewMatrix, 0, rot[0], 1.0f, 0.0f, 0.0f);
 				Matrix.rotateM(defaultShader.viewMatrix, 0, rot[1], 0.0f, 1.0f, 0.0f);
 				Matrix.rotateM(defaultShader.viewMatrix, 0, rot[2], 0.0f, 0.0f, 1.0f);
 				
 //				Sd3dRendererGl20.setRotateEulerM(defaultShader.viewMatrix, 0, rot[0], rot[1], rot[2]);
 				
 			}
 			else
 			{
 				//mGl.glLoadMatrixf(scene.getCamera().getRotationMatrix(), 0);
 				
 				//GLU.gluLookAt(mGl, 0, 0, 2, 0, 0, 0, 0, 1, 0);
 				Matrix.setLookAtM(matrix, 0, 0, 0, 2, 0, 0, 0, 0, 1, 0);
 				//Matrix.multiplyMM(mViewMatrix, 0, scene.getCamera().getRotationMatrix(), 0, matrix, 0);
 				Matrix.multiplyMM(defaultShader.viewMatrix, 0, scene.getCamera().getRotationMatrix(), 0, matrix, 0);
 			}
 				
 
 			float pos[] = scene.getCamera().getPosition();
 			Matrix.translateM(defaultShader.viewMatrix, 0, -pos[0], -pos[1], -pos[2]);		
 			
 	        updateFrustumFaster(defaultShader);        
	
	        
 	        if (lightPass > 0)
 	        {
 	        	//GLES20.glClear(GL11.GL_DEPTH_BUFFER_BIT);
 	        	GLES20.glEnable(GL11.GL_DEPTH_TEST);
 	        	GLES20.glDepthFunc(GL11.GL_LEQUAL);
 	        	GLES20.glEnable(GLES20.GL_BLEND);
 	        	GLES20.glBlendFunc(GLES20.GL_ONE,GLES20.GL_ONE);
 	        }
 	        this.renderRenderList(defaultShader,lightPass); 
 	        if (lightPass > 0)
 	        {
 	        	GLES20.glDisable(GLES20.GL_BLEND);
 	        }
 	        lightPass++;
 		}
         
 		//renderPickableScene(scene);
 		
         if (this.sampling != null)
         {
         	this.sampling.onRenderToScreen(screenWidth, screenHeight);
         	this.sampling.drawSampler(screenWidth, screenHeight, textureOnlyShader);
         }
         
         
         float[] tmp = textureOnlyShader.projectionMatrix;
         textureOnlyShader.projectionMatrix = this.mProjectionOrthoMatrix;
         Matrix.setIdentityM(textureOnlyShader.viewMatrix, 0);
         this.renderRenderInScreenSpaceList(textureOnlyShader); 
         
         textureOnlyShader.projectionMatrix = tmp;
        	
         this.renderText(this.textureOnlyShader);
         
         this.mBmpFont.resetTextBuffer();    
         
         drawShadowTexturedQuad(textureOnlyShader,mBitmapQuadFont.getPixels());
         
         if (this.useShadowVolume)
         	renderShadowVolumeScene(scene,defaultShader);
         
         mBitmapQuadFont.frameEnd();
         
 	}
 	
 	public void renderText(Sd3dShader shader)
 	{
 		if (this.mBmpFont.mTextureName == 0){
 			this.registerFontMaterial();
 		}
 			//this.mBmpFont.registerTexture();
 			
 		
 		if (this.mBmpFont.mMesh[0].mVertices.position() != 0)
 		{			
 			
 			//OpenGL stuffs	
 			Matrix.setIdentityM(shader.modelMatrix, 0);
 			Matrix.setIdentityM(shader.viewMatrix, 0);
 			
 			shader.renderStateVector.put(2, 0);//NO LIGHT	
 			shader.renderStateVector.put(1, 1);//HAS COLOR
 			shader.renderStateVector.put(0, 1);//HAS TEXTURE
 			
 		    // This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
 		    // (which currently contains model * view).
 		    Matrix.multiplyMM(shader.MVMatrix, 0, shader.viewMatrix, 0, shader.modelMatrix, 0);
 		 
 		    // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
 		    // (which now contains model * view * projection).
 		    Matrix.multiplyMM(shader.MVPMatrix, 0, mProjectionOrthoMatrix, 0, shader.MVMatrix, 0);		
 		    
 		    shader.bind();
 		    
 		    GLES20.glUniformMatrix4fv(shader.getMVMatrixHandle(), 1, false, shader.MVMatrix, 0);
 		    GLES20.glUniformMatrix4fv(shader.getMVPMatrixHandle(), 1, false, shader.MVPMatrix, 0);
 		    GLES20.glUniform4iv(shader.getRenderStateVectorHandle(), 1, shader.renderStateVector);			
 			
 			//GLES20.glEnable (GL11.GL_BLEND);
 			//GLES20.glBlendFunc (GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);			
 		    GLES20.glDisable(GLES20.GL_DEPTH_TEST);
 			
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, 0, this.mBmpFont.getBbVertices());
 			GLES20.glEnableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 			
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexTexCoordHandle, 2, GLES20.GL_FLOAT, false, 0, this.mBmpFont.getTexCoords());
 			GLES20.glEnableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);
 
 			GLES20.glVertexAttribPointer(Sd3dShader.vertexColorHandle, 4, GLES20.GL_FLOAT, false, 0, this.mBmpFont.getColors());
 			GLES20.glEnableVertexAttribArray(Sd3dShader.vertexColorHandle);
 			
 			GLES20.glBindTexture(GL11.GL_TEXTURE_2D, this.mBmpFont.mTextureName);
 			
 			GLES20.glDrawElements(GL11.GL_TRIANGLES, this.mBmpFont.mMesh[0].mIndices.position(),GL11.GL_UNSIGNED_SHORT, this.mBmpFont.getIndices());	
 			
 			GLES20.glDisableVertexAttribArray(Sd3dShader.vertexPositionHandle);
 			GLES20.glDisableVertexAttribArray(Sd3dShader.vertexTexCoordHandle);
 			GLES20.glDisableVertexAttribArray(Sd3dShader.vertexColorHandle);
 			GLES20.glEnable(GLES20.GL_DEPTH_TEST);
 			//GLES20.glDisable (GL11.GL_BLEND);
 		}		
 	}
 	
 	//@Overides
 	public void updateScreenResolution(int width, int height)
 	{
 		Log.d("updateScreenResolution()","RECEIVE HEIGHT="+height+" WIDTH="+width);
 		this.screenHeight = height;
 		this.screenWidth = width;		
 		mBmpFont.setScreenProperties(width, height);
 		mBitmapQuadFont.init(width,height);
 		//this.pickingFrameBuffer.updateScreen(width, height);
 		
 		/*
     	if (pickingFrameBuffer != null) {
     		pickingFrameBuffer.release();
     	}*/
     	
 
 	}
 	
     public Sd3dRendererGl20(boolean useTranslucentBackground,int maxRenderElement,int width,int height) {
         //mTranslucentBackground = useTranslucentBackground;      
     	loadConfiguration();
     	
     	this.mMaxRenderElement = maxRenderElement;
 		
 		this.screenHeight = height;
 		this.screenWidth = width;
 		mRenderList = new Sd3dRendererElement[mMaxRenderElement];
 		
 		mBmpFont = new Sd3dBmpFont("font.png",null);
 		mBmpFont.setScreenProperties(width, height);
 		mLightVector = new float[3];
     }		
     private static final int EGL_OPENGL_ES2_BIT = 4;
     public int[] getConfigSpec() {
         // 32bpp + 16bit Z-buffer + 8bit stencil buffer
     	if (this.useShadowVolume == true)
     	{
 	        int[] configSpec = {
 	                EGL11.EGL_RED_SIZE,      5,
 	                EGL11.EGL_GREEN_SIZE,    6,
 	                EGL11.EGL_BLUE_SIZE,     5,
 	                EGL11.EGL_ALPHA_SIZE,    0,                		
 	        		EGL11.EGL_DEPTH_SIZE,   16,
 	        		EGL11.EGL_STENCIL_SIZE, 8,
 	        		EGL11.EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,
 	        		EGL11.EGL_NONE
 	        };
 	        return configSpec;
     	}
     	else
     	{
 	        int[] configSpec = {
 	                EGL11.EGL_RED_SIZE,      5,
 	                EGL11.EGL_GREEN_SIZE,    6,
 	                EGL11.EGL_BLUE_SIZE,     5,
 	                EGL11.EGL_ALPHA_SIZE,    0,                		
 	        		EGL11.EGL_DEPTH_SIZE,   16,
 	        		EGL11.EGL_STENCIL_SIZE, 0,
 	        		EGL11.EGL_RENDERABLE_TYPE,EGL_OPENGL_ES2_BIT,
 	        		EGL11.EGL_NONE
 	        };
 	        return configSpec;    		
     	}
     }
 
     public void setupShadowVolumeStep1()
     {
     	//Do not test against depth
     	GLES20.glDepthMask(false);
     	
     	//Do not write in the color buffer
     	GLES20.glColorMask(false, false, false, false);
     	
     	//Enable stencil buffer
     	GLES20.glEnable(GL11.GL_STENCIL_TEST);
     	
     	GLES20.glClear(GL11.GL_STENCIL_BUFFER_BIT);
     	
     	GLES20.glStencilFunc(GL11.GL_ALWAYS, 0, ~0);	
     }
 
     public void setupShadowVolumeStep2()
     {
     	GLES20.glFrontFace(GL11.GL_CW);
     	
     	GLES20.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_INCR);
     }    
     
     public void setupShadowVolumeStep3()
     {
     	GLES20.glFrontFace(GL11.GL_CCW);
     	
     	GLES20.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_DECR);
     }       
     
     public void setupShadowVolumeStep4()
     {
     	GLES20.glColorMask(true, true, true, true);
     	
     	GLES20.glFrontFace(GL11.GL_CW);
     	
     	GLES20.glStencilFunc(GL11.GL_NOTEQUAL, 0, ~0);
 
     	GLES20.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);    	
     }
     
     public void setupShadowVolumeStep5()
     {
     	GLES20.glDepthMask(true);    	
     	
     	GLES20.glDisable(GL11.GL_STENCIL_TEST);
     }
         
     
     public void sizeChanged(GL11 gl, int width, int height) {  	
          //gl.glViewport(0, 0, width, height);
 	    		
          /*
           * Set our projection matrix. This doesn't have to be done
           * each time we draw, but usually a new projection needs to
           * be set when the viewport is resized.
           */
          /*
          float ratio = (float) width / height;
          gl.glMatrixMode(GL11.GL_PROJECTION);
          gl.glLoadIdentity();
          gl.glFrustumf(-ratio, ratio, -1, 1, 1.5f, 1000);
          */
          this.updateScreenResolution(width, height);
     	
     	 // Set the OpenGL viewport to the same size as the surface.
         GLES20.glViewport(0, 0, width, height);
      
         
         float f = 1.0f;
         
         // Create a new perspective projection matrix. The height will stay the same
         // while the width will vary as per aspect ratio.
         final float ratio = (float) width / height;
         final float left = -ratio * f;
         final float right = ratio * f;
         final float bottom = -1.0f * f;
         final float top = 1.0f * f;
         final float near = 1.0f;
         final float far = 1000.0f;
      
         Matrix.frustumM(defaultShader.projectionMatrix, 0, left, right, bottom, top, near, far);    	
         //Matrix.frustumM(colorOnlyShader.projectionMatrix, 0, left, right, bottom, top, near, far);  
         Matrix.frustumM(textureOnlyShader.projectionMatrix, 0, left, right, bottom, top, near, far);  
         
         Log.d("Sd3dRendererGl20",this.screenWidth+" "+this.screenHeight);
         Matrix.orthoM(mProjectionOrthoMatrix, 0, 0, this.screenWidth, this.screenHeight, 0, -1, 1);
               
     }
 
     public void surfaceCreated(GL11 gl) {
 
     	GLES20.glEnable(GL11.GL_DEPTH_TEST);
     	GLES20.glEnable(GL11.GL_CULL_FACE);
     	
     	if (Sd3dConfig.getString("default_shader_vs") != null)
     		this.vertexShader = Sd3dRessourceManager.getManager().getText("shaders/"+Sd3dConfig.getString("default_shader_vs"));
     	else
     		this.vertexShader = Sd3dRessourceManager.getManager().getText("shaders/default_vs.gl");
     	
     	if (vertexShader == null)
     	{
     		Log.e("Sd3d","Warning expected shader file : "+"shaders/default_vs.gl"+" not present in <asset> folder.");
     	}
     	
 
     	if (Sd3dConfig.getString("default_shader_fs") != null)
     		this.fragmentShader = Sd3dRessourceManager.getManager().getText("shaders/"+Sd3dConfig.getString("default_shader_fs"));
     	else
     		this.fragmentShader = Sd3dRessourceManager.getManager().getText("shaders/default_fs.gl");
 	    
 
     	if (fragmentShader == null)
     	{
     		Log.e("Sd3d","Warning expected shader file : "+"shaders/default_fs.gl"+" not present in <asset> folder.");    		
     	}
     	    	
     	defaultShader = new Sd3dShader(this.vertexShader,this.fragmentShader);
     	defaultShader.register();    
     	
     	
     	textureOnlyShader = new Sd3dShader(Sd3dRessourceManager.getManager().getText("shaders/textureonly_vs.gl"),
     			Sd3dRessourceManager.getManager().getText("shaders/textureonly_fs.gl"));
     	textureOnlyShader.register();      	
     	
     	colorOnlyShader = new Sd3dShader(Sd3dRessourceManager.getManager().getText("shaders/coloronly_vs.gl"),
     			Sd3dRessourceManager.getManager().getText("shaders/coloronly_fs.gl"));
     	colorOnlyShader.register();    	    	
     	
     	pickingShader = new Sd3dShader(Sd3dRessourceManager.getManager().getText("shaders/coloronly_vs.gl"),
 				Sd3dRessourceManager.getManager().getText("shaders/coloronly_fs.gl"));
 		pickingShader.register();  
 	
 		shaderMap.put("textureonly", textureOnlyShader);
 		shaderMap.put("coloronly", textureOnlyShader);   
 		
     	if (pickingFrameBuffer != null) {
     		Log.d("Sd3dRendererGL20","Deleting picking framebuffer");
     		pickingFrameBuffer.release();
     		pickingFrameBuffer = null;
     	}    	
 
  		GameHolder.mGame.invalidateRenderElements = true;
     }	
     
     public Sd3dObject getObjectFromPickingColor(int r,int g,int b)
     {		
     	for (int i = 0; i < mCountRenderElement;i++)
     	{
     		if (mRenderList[i].mIsPickable)
     		{
     			if (mRenderList[i].mPickingColor != null)
     				if (mRenderList[i].mPickingColor[0] == r)
         				if (mRenderList[i].mPickingColor[1] == g)
             				if (mRenderList[i].mPickingColor[2] == b)
             				{
             					
             					return mRenderList[i].mObject;
             				}
     		}
     	}
     	return null;
     }
     
     
     private byte pickColorOffset = 1;
     public void generatePickingColor(Sd3dRendererElement element)
     {
     	element.mPickingColor = new byte[3];
     	pickColorOffset += 1;
     	element.mPickingColor[0] = pickColorOffset;
     	element.mPickingColor[1] = 0;    	
     	element.mPickingColor[2] = 0;      	
     }
     
 	public int getTop() {
 		return top;
 	}
 
 	public void setTop(int top) {
 		this.top = top;
 	}
 	
     private float clip[] = new float[16];
     //private float proj[] = new float[16];
     //private float modl[] = new float[16];
     private int[] mViewport = new int[16];	
 	private void updateFrustumFaster(Sd3dShader shader)
 	{
 
 	    float t;
 	 
 	    // Get The Current PROJECTION Matrix From OpenGL
 	    //GLES20.glGetFloatv( GL11.GL_PROJECTION_MATRIX, proj,0 );
 	 
 	    // Get The Current MODELVIEW Matrix From OpenGL
 	    //GLES20.glGetFloatv( GL11.GL_MODELVIEW_MATRIX, modl,0 );
 	    
 	    GLES20.glGetIntegerv( GL11.GL_VIEWPORT, mViewport,0 );    
 	   
 	    // Combine The Two Matrices (Multiply Projection By Modelview)
 	    // But Keep In Mind This Function Will Only Work If You Do NOT
 	    // Rotate Or Translate Your Projection Matrix
 	    clip[ 0] = shader.viewMatrix[ 0] * shader.projectionMatrix[ 0];
 	    clip[ 1] = shader.viewMatrix[ 1] * shader.projectionMatrix[ 5];
 	    clip[ 2] = shader.viewMatrix[ 2] * shader.projectionMatrix[10] + shader.viewMatrix[ 3] * shader.projectionMatrix[14];
 	    clip[ 3] = shader.viewMatrix[ 2] * shader.projectionMatrix[11];
 	 
 	    clip[ 4] = shader.viewMatrix[ 4] * shader.projectionMatrix[ 0];
 	    clip[ 5] = shader.viewMatrix[ 5] * shader.projectionMatrix[ 5];
 	    clip[ 6] = shader.viewMatrix[ 6] * shader.projectionMatrix[10] + shader.viewMatrix[ 7] * shader.projectionMatrix[14];
 	    clip[ 7] = shader.viewMatrix[ 6] * shader.projectionMatrix[11];
 	 
 	    clip[ 8] = shader.viewMatrix[ 8] * shader.projectionMatrix[ 0];
 	    clip[ 9] = shader.viewMatrix[ 9] * shader.projectionMatrix[ 5];
 	    clip[10] = shader.viewMatrix[10] * shader.projectionMatrix[10] + shader.viewMatrix[11] * shader.projectionMatrix[14];
 	    clip[11] = shader.viewMatrix[10] * shader.projectionMatrix[11];
 	 
 	    clip[12] = shader.viewMatrix[12] * shader.projectionMatrix[ 0];
 	    clip[13] = shader.viewMatrix[13] * shader.projectionMatrix[ 5];
 	    clip[14] = shader.viewMatrix[14] * shader.projectionMatrix[10] + shader.viewMatrix[15] * shader.projectionMatrix[14];
 	    clip[15] = shader.viewMatrix[14] * shader.projectionMatrix[11];
 	 
 	    // Extract The Numbers For The RIGHT Plane
 	    m_Frustum[0][0] = clip[ 3] - clip[ 0];
 	    m_Frustum[0][1] = clip[ 7] - clip[ 4];
 	    m_Frustum[0][2] = clip[11] - clip[ 8];
 	    m_Frustum[0][3] = clip[15] - clip[12];
 	 
 	    // Normalize The Result
 	    t = (float) (Math.sqrt( m_Frustum[0][0] * m_Frustum[0][0] + m_Frustum[0][1] * m_Frustum[0][1] + m_Frustum[0][2] * m_Frustum[0][2] ));
 	    m_Frustum[0][0] /= t;
 	    m_Frustum[0][1] /= t;
 	    m_Frustum[0][2] /= t;
 	    m_Frustum[0][3] /= t;
 	 
 	    // Extract The Numbers For The LEFT Plane
 	    m_Frustum[1][0] = clip[ 3] + clip[ 0];
 	    m_Frustum[1][1] = clip[ 7] + clip[ 4];
 	    m_Frustum[1][2] = clip[11] + clip[ 8];
 	    m_Frustum[1][3] = clip[15] + clip[12];
 	 
 	    // Normalize The Result
 	    t = (float)(Math.sqrt( m_Frustum[1][0] * m_Frustum[1][0] + m_Frustum[1][1] * m_Frustum[1][1] + m_Frustum[1][2] * m_Frustum[1][2] ));
 	    m_Frustum[1][0] /= t;
 	    m_Frustum[1][1] /= t;
 	    m_Frustum[1][2] /= t;
 	    m_Frustum[1][3] /= t;
 	 
 	    // Extract The BOTTOM Plane
 	    m_Frustum[2][0] = clip[ 3] + clip[ 1];
 	    m_Frustum[2][1] = clip[ 7] + clip[ 5];
 	    m_Frustum[2][2] = clip[11] + clip[ 9];
 	    m_Frustum[2][3] = clip[15] + clip[13];
 	 
 	    // Normalize The Result
 	    t = (float)(Math.sqrt( m_Frustum[2][0] * m_Frustum[2][0] + m_Frustum[2][1] * m_Frustum[2][1] + m_Frustum[2][2] * m_Frustum[2][2] ));
 	    m_Frustum[2][0] /= t;
 	    m_Frustum[2][1] /= t;
 	    m_Frustum[2][2] /= t;
 	    m_Frustum[2][3] /= t;
 	 
 	    // Extract The TOP Plane
 	    m_Frustum[3][0] = clip[ 3] - clip[ 1];
 	    m_Frustum[3][1] = clip[ 7] - clip[ 5];
 	    m_Frustum[3][2] = clip[11] - clip[ 9];
 	    m_Frustum[3][3] = clip[15] - clip[13];
 	 
 	    // Normalize The Result
 	    t = (float)(Math.sqrt( m_Frustum[3][0] * m_Frustum[3][0] + m_Frustum[3][1] * m_Frustum[3][1] + m_Frustum[3][2] * m_Frustum[3][2] ));
 	    m_Frustum[3][0] /= t;
 	    m_Frustum[3][1] /= t;
 	    m_Frustum[3][2] /= t;
 	    m_Frustum[3][3] /= t;
 	 
 	    // Extract The FAR Plane
 	    m_Frustum[4][0] = clip[ 3] - clip[ 2];
 	    m_Frustum[4][1] = clip[ 7] - clip[ 6];
 	    m_Frustum[4][2] = clip[11] - clip[10];
 	    m_Frustum[4][3] = clip[15] - clip[14];
 	 
 	    // Normalize The Result
 	    t = (float)(Math.sqrt( m_Frustum[4][0] * m_Frustum[4][0] + m_Frustum[4][1] * m_Frustum[4][1] + m_Frustum[4][2] * m_Frustum[4][2] ));
 	    m_Frustum[4][0] /= t;
 	    m_Frustum[4][1] /= t;
 	    m_Frustum[4][2] /= t;
 	    m_Frustum[4][3] /= t;
 	 
 	    // Extract The NEAR Plane
 	    m_Frustum[5][0] = clip[ 3] + clip[ 2];
 	    m_Frustum[5][1] = clip[ 7] + clip[ 6];
 	    m_Frustum[5][2] = clip[11] + clip[10];
 	    m_Frustum[5][3] = clip[15] + clip[14];
 	 
 	    // Normalize The Result
 	    t = (float)(Math.sqrt( m_Frustum[5][0] * m_Frustum[5][0] + m_Frustum[5][1] * m_Frustum[5][1] + m_Frustum[5][2] * m_Frustum[5][2] ));
 	    m_Frustum[5][0] /= t;
 	    m_Frustum[5][1] /= t;
 	    m_Frustum[5][2] /= t;
 	    m_Frustum[5][3] /= t;
 	        
 	}	
 	
 	public boolean pointInFrustum(float x, float y, float z)
 	{
 	    int i;
 	    // The Idea Behind This Algorithum Is That If The Point
 	    // Is Inside All 6 Clipping Planes Then It Is Inside Our
 	    // Viewing Volume So We Can Return True.
 	    for(i = 0; i < 6; i++)
 	    {
 	        if(m_Frustum[i][0] * x + m_Frustum[i][1] * y + m_Frustum[i][2] * z + m_Frustum[i][3] <= 0)
 	        {
 	            return(false);
 	        }
 	    }
 	    return(true);
 	}	
 	
 	public void pointToScreen(float x,float y,float z,float res[])
 	{
 		GLU.gluProject(x, y, z, defaultShader.viewMatrix, 0, defaultShader.projectionMatrix, 0, this.mViewport, 0, res, 0);
 		// (0,0) is top left, not bottom left corner.
 		res[1] = this.screenHeight - res[1]; 
 	}	
 	
 	public void pointToScreen(float x,float y,float z,float res[], float modelmatrix[])
 	{
 		//Matrix.setIdentityM(defaultShader.viewMatrix, 0);
 		Matrix.multiplyMM(defaultShader.MVMatrix, 0, defaultShader.viewMatrix, 0,modelmatrix, 0);
 		GLU.gluProject(x, y, z, defaultShader.MVMatrix, 0, defaultShader.projectionMatrix, 0, this.mViewport, 0, res, 0);
 		// (0,0) is top left, not bottom left corner.
 		res[1] = this.screenHeight - res[1]; 
 	}
 	
 	public void displayText(String text,Sd3dRenderer.ALIGN halign, Sd3dRenderer.ALIGN valign, float size)
 	{
 		this.mBitmapQuadFont.drawText(text, 0, 0);
 		this.mBmpFont.addTextToBuffer(text, halign, valign, size);
 	}	
 	
 	float tmpP[] = new float[3]; 
 	public void displayText(String text,float x, float y, float z, float modelmatrix[])
 	{
 		
 		pointToScreen((float)x,(float)y,(float)z,tmpP,modelmatrix);
 		this.mBitmapQuadFont.drawText(text, (int)tmpP[0], (int)tmpP[1]);
 		//this.mBitmapQuadFont.drawText("Drawing text at : "+(int)tmpP[0]+" - "+(int)tmpP[1],0, 100);
 		
 		//displayMatrix(defaultShader.normalMatrix,10);
 		//displayMatrix(modelmatrix,130);		
 	}		
 	
 	private void displayMatrix(float[] matrix, int offsetEcran) {
 		String m = "";
 		for (int i = 0;i < 16;i++) {
 			m = m + " " + matrix[i];
 			if ((i+1)%4 == 0) {
 				this.mBitmapQuadFont.drawText(m, 0, offsetEcran + i * 8);
 				m = "";
 			}
 		}
 		
 		
 	}
 	
 	public void RendererElements()
 	{
 		this.mBmpFont.mTextureName = 0;
 	}
 
 
 	public void invalidateRendererElements() {
 		this.mBmpFont.mTextureName = 0;
 	}	
 	
 	public Sd3dObject pickAt(int x, int y, Sd3dScene scene){	
     	if (pickingFrameBuffer == null){
 			pickingFrameBuffer = new Sd3dFrameBuffer();
 			pickingFrameBuffer.init(screenWidth, screenHeight);
     	}    	
 				
 		pickingFrameBuffer.bind();
 		
 		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 		GLES20.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);		
 		
 		renderPickableScene(scene);
 		
 		GLES20.glFlush();
 		
 		IntBuffer buffer = IntBuffer.allocate(1);
 		GLES20.glReadPixels(x,screenHeight - y - top, 1, 1, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE  , buffer);
 			
 		Log.d("Sd3dRendererGl20","Picked color [ "+x+" , "+y+" ] = "+buffer.get(0)+" ERROR="+GLES20.glGetError() );
 		
 		pickingFrameBuffer.unbind(this.screenWidth, this.screenHeight);
 		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		
 		
 		int r = buffer.get(0) & 0xff;
 		int g = (buffer.get(0) & 0xff00) >> 8;
 		int b = (buffer.get(0) & 0xff0000) >> 16;
 		//Log.d("Sd3dRendererGl20","R="+r+" G="+g+" B="+b);
 		return getObjectFromPickingColor(r,g,b);
 	}
 
 
 	@Override
 	public void displayText(String text, float x, float y) {
 		this.mBitmapQuadFont.drawText(text, (int)x, (int)y);	
 	}
 	
 	
 }
