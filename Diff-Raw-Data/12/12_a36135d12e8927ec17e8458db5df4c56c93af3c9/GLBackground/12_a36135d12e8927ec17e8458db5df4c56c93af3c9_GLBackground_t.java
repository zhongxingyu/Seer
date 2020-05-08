 package com.slsw.fiarworks.firework;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import android.graphics.Bitmap;
 import android.opengl.GLES20;
 import android.opengl.GLUtils;
 
 public class GLBackground
 {
 	private final String vertexShaderCode =
         "attribute vec2 a_Position;" +
         "attribute vec2 a_TexCoordinate;" +
         "varying vec2 v_TexCoordinate;" +
         "void main() {" +
         "  vec4 position = vec4(a_Position, 0.0, 1.0);" +
         "  v_TexCoordinate = a_TexCoordinate;" +
         "  gl_Position = position;" +
         "}";
 
     private final String fragmentShaderCode =
         "precision mediump float;" +
         "uniform sampler2D u_Texture;" +
         "varying vec2 v_TexCoordinate;" +
         "void main() {" +
         "  gl_FragColor = texture2D(u_Texture, v_TexCoordinate);" +
         "}";
 
     static final float[] screenPos = {	-1.0f, -1.0f,
 	   									-1.0f,  1.0f,
 	   									 1.0f, -1.0f,
 	   									 1.0f,  1.0f,
 	   									 1.0f, -1.0f,
 	   									-1.0f,  1.0f,
 	   									};
 
	static final float[] screenTex = {	0.0f, 1.0f,
	   									0.0f,  0.0f,
	   									 1.0f, 1.0f,
	   									 1.0f,  0.0f,
	   									 1.0f, 1.0f,
	   									0.0f,  0.0f,
 	   									};
 
     static int[] mfloatBufferPosHandle = new int[1];
     final int[] textureHandle = new int[1];
 	static int mPosShaderLoc;
     static int mTexShaderLoc;
     static int mTextureUniformLoc;
 
 
     static int mNumGeometryFloats; 
 	static int mProgram;
 
 
 	ByteBuffer mByteBufferPos;
 	ByteBuffer mByteBufferTex;
 
 	GLBackground()
 	{
 		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
         int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
 
 		mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
         GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
         GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
 
         //error checking
         {
 			mPosShaderLoc = GLES20.glGetAttribLocation(mProgram, "a_Position");
 			if(mPosShaderLoc == -1)
 			{
 				System.out.println("mPosShaderLoc is -1. This is bad.");
 			}
 			mTexShaderLoc = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
 			if(mTexShaderLoc == -1)
 			{
 				System.out.println("mTexShaderLoc is -1. This is bad.");
 			}
 			mTextureUniformLoc = GLES20.glGetUniformLocation(mProgram, "u_Texture");
 			if(mTextureUniformLoc == -1)
 			{
 				System.out.println("mTextureUniformLoc is -1. This is bad.");
 			}
 
 		}
 
 		GLES20.glGenTextures(1, textureHandle, 0);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
 		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
 		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
 
 		mByteBufferPos = ByteBuffer.allocateDirect(12 * 4);
 		mByteBufferPos.order(ByteOrder.nativeOrder());
 		mByteBufferTex = ByteBuffer.allocateDirect(12 * 4);
 		mByteBufferTex.order(ByteOrder.nativeOrder());
 
 
 	}
 
 	public void draw(Bitmap camera_image)
 	{
 		
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
 		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
         GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
  		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, camera_image, 0);
 
  		mNumGeometryFloats = screenPos.length;
 		int numGeometryBytes = mNumGeometryFloats * 4;
 
 		FloatBuffer floatBufferPos = mByteBufferPos.asFloatBuffer();
 		floatBufferPos.put(screenPos);
 		floatBufferPos.position(0);
 
 		FloatBuffer floatBufferTex = mByteBufferTex.asFloatBuffer();
 		floatBufferTex.put(screenTex);
 		floatBufferTex.position(0);
 
 		GLES20.glUseProgram(mProgram);
 
 		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
 		GLES20.glUniform1i(mTextureUniformLoc, 0);
 
 		GLES20.glEnableVertexAttribArray(mPosShaderLoc);
 		GLES20.glVertexAttribPointer(	mPosShaderLoc, 2,
 										GLES20.GL_FLOAT, false,
 										2*4, floatBufferPos);
 
 		GLES20.glEnableVertexAttribArray(mTexShaderLoc);
 		GLES20.glVertexAttribPointer(	mTexShaderLoc, 2,
 										GLES20.GL_FLOAT, false,
 										2*4, floatBufferTex);
 
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumGeometryFloats / 2);
 
 		GLES20.glDisableVertexAttribArray(mPosShaderLoc);
 		GLES20.glDisableVertexAttribArray(mTexShaderLoc);
 
 	}
 
 
 
 	public static int loadShader(int type, String shaderCode){
 
         // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
         // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         int shader = GLES20.glCreateShader(type);
 
         int[] isCompiled = new int[1];
 
 
         // add the source code to the shader and compile it
         GLES20.glShaderSource(shader, shaderCode);
         GLES20.glCompileShader(shader);
 
         GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, isCompiled, 0);
         if(isCompiled[0] == 0)
 		{
 			System.out.println("Shader did not compile");
         }
         
         return shader;
     }
 }
