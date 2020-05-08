 package org.newdawn.opengltest;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
import java.util.Arrays;
 
 import android.opengl.GLES20;
 
 
 public class Mesh {
 	public static final int DIMENSIONS = 3;
 	public static final int BYTES_PER_FLOAT = 4;
 	public static final int BYTES_PER_SHORT = 2;
 	private static final int VERTEX_STRIDE = DIMENSIONS * 4; // 4 bytes per vertex
 	
 	private final String vertexShaderCode =
 		// This matrix member variable provides a hook to manipulate
         // the coordinates of the objects that use this vertex shader
         "uniform mat4 uMVPMatrix;" +
         "uniform vec4 vWorldPosition;" + 
         "attribute vec4 vPosition;" +
         "void main() {" +
         // the matrix must be included as a modifier of gl_Position
         "  gl_Position = (vPosition + vWorldPosition) * uMVPMatrix;" +
         "}";
 
     private final String fragmentShaderCode =
         "uniform vec4 vColor;" +
         "void main() {" +
         "  gl_FragColor = vColor;" +
         "}";
 		
 	private FloatBuffer vertexBuffer;
 	private ShortBuffer vertexOrderBuffer;
 	private int numVertecies;
 	private int shaderProgram;
 	private float[] worldPosition = {0f,0f,0f,0f};
 	
 	private float[] colour;
 
 	public Mesh(float[] vertecies, float[] colour, short[] vertexOrder) {
 		numVertecies = vertexOrder.length;
 		
 		this.colour = colour;
 		
 		vertexBuffer = ByteBuffer.allocateDirect(BYTES_PER_FLOAT * vertecies.length).order(ByteOrder.nativeOrder()).asFloatBuffer();
 		vertexOrderBuffer = ByteBuffer.allocateDirect(BYTES_PER_SHORT * vertexOrder.length).order(ByteOrder.nativeOrder()).asShortBuffer();		
 		
 		vertexBuffer.put(vertecies).position(0);
 		vertexOrderBuffer.put(vertexOrder).position(0);
 
 		int vertexShader = MyGL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
 		int fragmentShader = MyGL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
 
 		shaderProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
 	    GLES20.glAttachShader(shaderProgram, vertexShader);   // add the vertex shader to program
 	    GLES20.glAttachShader(shaderProgram, fragmentShader); // add the fragment shader to program
 	    GLES20.glLinkProgram(shaderProgram);                  // creates OpenGL ES program executables
 	}
 
 	public void setPosition(float[] newPosition) {
 		for(int i=0;i<3;i++) {
 			worldPosition[i] = newPosition[i];
 		}
 	}
 	
 	public void getPosition(float[] retVal) {
 		for(int i=0;i<3;i++) {
 			retVal[i] = worldPosition[i];
 		}
 	}
 	
 	public void draw(float[] mvpMatrix) {
 		// Add program to OpenGL environment
         GLES20.glUseProgram(shaderProgram);
 
         // get handle to vertex shader's vPosition member
         int mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "vPosition");
 
         // Enable a handle to the triangle vertices
         GLES20.glEnableVertexAttribArray(mPositionHandle);
 
         // Prepare the triangle coordinate data
         GLES20.glVertexAttribPointer(mPositionHandle, DIMENSIONS,
                                      GLES20.GL_FLOAT, false,
                                      VERTEX_STRIDE, vertexBuffer);
 
         // get handle to fragment shader's vColor member
         int mColorHandle = GLES20.glGetUniformLocation(shaderProgram, "vColor");
 
         // Set color for drawing the triangle
         GLES20.glUniform4fv(mColorHandle, 1, colour, 0);
         
         int vWorldPositionHandle = GLES20.glGetUniformLocation(shaderProgram, "vWorldPosition");
         GLES20.glUniform4fv(vWorldPositionHandle, 1, worldPosition, 0);
 
         // get handle to shape's transformation matrix
         int mMVPMatrixHandle = GLES20.glGetUniformLocation(shaderProgram, "uMVPMatrix");
 
         // Apply the projection and view transformation
         GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
 
         // Draw the shape
         GLES20.glDrawElements(GLES20.GL_TRIANGLE_STRIP, numVertecies,
                 GLES20.GL_UNSIGNED_SHORT, vertexOrderBuffer);
         
         // Disable vertex array
         GLES20.glDisableVertexAttribArray(mPositionHandle);
        }
 }
