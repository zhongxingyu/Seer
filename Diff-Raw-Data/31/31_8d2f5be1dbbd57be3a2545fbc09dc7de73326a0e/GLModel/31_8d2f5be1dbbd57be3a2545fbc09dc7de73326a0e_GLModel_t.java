 package com.slsw.fiarworks.firework;
 
import java.nio.IntBuffer;

 import android.opengl.GLES20;
 import android.opengl.Matrix;
 
 public class GLModel
 {
 	static final int COORDS_PER_VERTEX = 3;
 	static final int TEX_COORDS_PER_VERTEX = 2;
 	static final int NORMAL_COORDS_PER_VERTEX = 3;
 
 	static final int tex_offset = (COORDS_PER_VERTEX + NORMAL_COORDS_PER_VERTEX) * 4;
 	static final int normal_offset = COORDS_PER_VERTEX * 4;
 	static final int vertex_offset = 0 * 4;
 	static final int stride = (COORDS_PER_VERTEX + TEX_COORDS_PER_VERTEX + NORMAL_COORDS_PER_VERTEX) * 4;
 	
 	float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
 
 	private final String vertexShaderCode =
         "attribute vec4 vPosition;" +
         //for now we ignore texture coords
         "attribute vec2 vTexture;" +
         //and ignore color
         "attribute vec4 vColor;" +
         "uniform mat4 uMVPMatrix;" +
         "void main() {" +
         "  gl_Position = uMVPMatrix * vPosition;" +
         "}";
 
     private final String fragmentShaderCode =
         "precision mediump float;" +
         "uniform vec4 vColor;" +
         "void main() {" +
         "  gl_FragColor = vColor;" +
         "}";
 
     final int[] mGeometryBufferHandle = new int[1];
     final int mPosShaderLoc;
     final int mTexShaderLoc;
     final int mColShaderLoc;
     final int mMVPMatrixLoc;
     final int mColFShaderLoc;
 
     final int mNumGeometry; 
 	final int mProgram;
 
 	
 	GLModel(Model m)
 	{
 		mNumGeometry = m.num_geometry;
 
 		GLES20.glGenBuffers(1, mGeometryBufferHandle, 0);
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGeometryBufferHandle[0]);
 		GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, m.num_geometry * 4, m.geometryBuffer, GLES20.GL_STATIC_DRAW);
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
 
 		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
         int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
 
 		mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
         GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
         GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
         GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
 		mPosShaderLoc = GLES20.glGetAttribLocation(mProgram, "vPosition");
 		mTexShaderLoc = GLES20.glGetAttribLocation(mProgram, "vTexture");
 		mColShaderLoc = GLES20.glGetAttribLocation(mProgram, "vColor");
 		mMVPMatrixLoc = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
 		mColFShaderLoc = GLES20.glGetUniformLocation(mProgram, "vColor");
 	}
 
 	public void draw(MatrixStack stackMV, float[] projection)
 	{
 		//multiply tansforms onto the stack
 		//we'll skip this for now
 
 		float[] mvpMatrix = new float[16];
 		float[] mvMatrix = new float[16];
 		stackMV.getMatrix(mvMatrix, 0);
 		Matrix.multiplyMM(mvpMatrix, 0, mvMatrix, 0, projection, 0);
 
 		GLES20.glUseProgram(mProgram);
 
 		GLES20.glUniformMatrix4fv(mMVPMatrixLoc, 1, false, mvpMatrix, 0);
 		GLES20.glUniform4fv(mColFShaderLoc, 1, color, 0);
 
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, mGeometryBufferHandle[0]);
 
 		GLES20.glEnableVertexAttribArray(mPosShaderLoc);
 		GLES20.glVertexAttribPointer(mPosShaderLoc, 3, GLES20.GL_FLOAT, false, 8*4, 0*4);
 
 		GLES20.glEnableVertexAttribArray(mTexShaderLoc);
 		GLES20.glVertexAttribPointer(mTexShaderLoc, 2, GLES20.GL_FLOAT, false, 8*4, 3*4);
 
 		GLES20.glEnableVertexAttribArray(mColShaderLoc);
 		GLES20.glVertexAttribPointer(mColShaderLoc, 3, GLES20.GL_FLOAT, false, 8*4, 5*4);
 
 		GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, mNumGeometry);
 
 		GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
 
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
