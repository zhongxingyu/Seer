 package com.mtt;
 
 import java.nio.*;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.*;
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 
 public class MttGameRenderer implements GLSurfaceView.Renderer
 {
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
 
     public static int loadShader(int type, String shaderCode){
 
         // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
         // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
         int shader = GLES20.glCreateShader(type);
 
         // add the source code to the shader and compile it
         GLES20.glShaderSource(shader, shaderCode);
         GLES20.glCompileShader(shader);
 
         return shader;
     }
 
     MttGame game;
 
     public MttGameRenderer(MttGame game)
     {
         this.game = game;
     }
 
 
 
     void drawTriangle()
     {
         ByteBuffer bb = ByteBuffer.allocateDirect(3*3*4);
         bb.order(ByteOrder.nativeOrder());
 
         FloatBuffer vertexBuffer = bb.asFloatBuffer();
 
         // number of coordinates per vertex in this array
         int COORDS_PER_VERTEX = 3;
         float triangleCoords[] = { // in counterclockwise order:
             0.0f,  0.622008459f, 0.0f,   // top
             -0.5f, -0.311004243f, 0.0f,   // bottom left
             0.5f, -0.311004243f, 0.0f    // bottom right
         };
 
         // Set color with red, green, blue and alpha (opacity) values
        float color[] = { 0.5f, 0.3f, 0.1f, 1.0f };
 
         vertexBuffer.put(triangleCoords);
         vertexBuffer.position(0);
 
         int vertID = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
         int fragID = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
         int programID = GLES20.glCreateProgram();
         GLES20.glAttachShader(programID, vertID);
         GLES20.glAttachShader(programID, fragID);
         GLES20.glLinkProgram(programID);
 
 
         GLES20.glUseProgram(programID);
 
         int vertexCount = triangleCoords.length / COORDS_PER_VERTEX;
         int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
 
         int posAttr = GLES20.glGetAttribLocation(programID, "vPosition");
         GLES20.glEnableVertexAttribArray(posAttr);
         GLES20.glVertexAttribPointer(posAttr, 3, GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);
 
         int colorUnif = GLES20.glGetUniformLocation(programID, "vColor");
         GLES20.glUniform4fv(colorUnif, 1, color, 0);
 
         GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
 
         GLES20.glDisableVertexAttribArray(posAttr);
 
     }
 
     @Override
     public void onSurfaceCreated(GL10 unused, EGLConfig config) {
 
         // Set the background frame color
         GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
 
     }
 
     @Override
     public void onDrawFrame(GL10 unused) {
 
         // Draw background color
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
 
         drawTriangle();
 
         System.out.println("onDrawFrame()");
 
     }
 
     @Override
     public void onSurfaceChanged(GL10 unused, int width, int height) {
         // Adjust the viewport based on geometry changes,
         // such as screen rotation
         GLES20.glViewport(0, 0, width, height);
     }
 }
