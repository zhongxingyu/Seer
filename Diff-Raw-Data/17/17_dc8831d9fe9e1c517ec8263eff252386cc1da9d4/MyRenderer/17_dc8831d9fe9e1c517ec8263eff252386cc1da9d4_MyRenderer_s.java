 package com.example.opengles;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 
 import javax.microedition.khronos.opengles.GL10;
 import javax.microedition.khronos.egl.EGLConfig;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 
 public class MyRenderer implements GLSurfaceView.Renderer {
 	
 	private Triangle mTriangle;
 	
 	@Override
 	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
 		GLES20.glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
 		mTriangle = new Triangle();
 	}
 	@Override	
 	public void onDrawFrame(GL10 unused) {
 		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 		mTriangle.draw();
 	}
 	@Override	
 	public void onSurfaceChanged(GL10 unused, int width, int height) {
 		GLES20.glViewport(0, 0, width, height);
 	}
 	
 	public static int loadShader(int type, String shaderCode) {
 		int shader = GLES20.glCreateShader(type);
 		
 		GLES20.glShaderSource(shader, shaderCode);
 		GLES20.glCompileShader(shader);
 		
 		return shader;
 		
 	}
 }
 
 class Triangle {
 	
     private final String vertexShaderCode =
             "attribute vec4 vPosition;" +
             "void main() {" +
             "  gl_Position = vPosition;" +
             "}";
 
         private final String fragmentShaderCode =
             "precision mediump float;" +
             "uniform vec4 vColor;" +
             "void main() {" +
            "  gl_FragColor = vColor;" +
             "}";
 	
 	private final FloatBuffer vertexBuffer;
 	private final int mProgram;
 	private int mPositionHandle;
 	private int mColorHandle;
 	
 
 	
 	static final int COORDS_PER_VERTEX = 3;
 	static float triangleCoords[] = {
 		0.0f, -0.5f, 0.0f, //
 		0.5f, 0.0f, 0.0f, // oikea piste
 		-0.5f, 0.0f, 0.0f // vasen piste
 	};
 	float color[] = { 1.0f, 0.98f, 0.0f, 1.0f}; // R, G, B, A = lpinkyvyys. f = float eli desimaali
 
 	private int vertexStride = COORDS_PER_VERTEX * 4;
 	private int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
 	
 	public Triangle() {
 		ByteBuffer bb = ByteBuffer.allocateDirect (triangleCoords.length * 4);
 		
 		bb.order(ByteOrder.nativeOrder());
 		
 		vertexBuffer = bb.asFloatBuffer();
 		
 		vertexBuffer.put(triangleCoords);
 		
 		vertexBuffer.position(0);
 		
 		int vertexShader = MyRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
 		int fragmentShader = MyRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
 		
 		mProgram = GLES20.glCreateProgram();
 		GLES20.glAttachShader(mProgram, vertexShader);
 		GLES20.glAttachShader(mProgram, fragmentShader);
 		GLES20.glLinkProgram(mProgram);
 }
 public void draw() {
 	GLES20.glUseProgram(mProgram);
 	
 	mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
 	
 	GLES20.glEnableVertexAttribArray(mPositionHandle);
 	
 	GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
 	
 	mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
 	
 	GLES20.glUniform4fv(mColorHandle, 1, color, 0);
 	
 	GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
 	
 	GLES20.glDisableVertexAttribArray(mPositionHandle);
 	
 }
 }
