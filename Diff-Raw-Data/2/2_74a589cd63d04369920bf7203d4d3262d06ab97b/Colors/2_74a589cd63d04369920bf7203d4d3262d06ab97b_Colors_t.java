 /*
  * Copyright (C) 2012 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.android.dreams.basic;
 
 import android.animation.Animator;
 import android.opengl.GLSurfaceView;
 import android.animation.AnimatorSet;
 import android.animation.ObjectAnimator;
 import android.animation.TimeInterpolator;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.res.Configuration;
 import android.graphics.drawable.Drawable;
 import android.graphics.Color;
 import android.graphics.PorterDuff;
 import android.net.Uri;
 import android.os.BatteryManager;
 import android.os.Handler;
 import android.provider.Settings;
 import android.service.dreams.Dream;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.animation.AccelerateInterpolator;
 import android.view.animation.DecelerateInterpolator;
 import android.webkit.WebSettings;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.widget.TextView;
 import android.os.SystemClock;
 
 import javax.microedition.khronos.egl.EGLConfig;
 import javax.microedition.khronos.opengles.GL10;
 
 import android.opengl.GLES20;
 import android.opengl.GLSurfaceView;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 public class Colors extends Dream {
     // It's so easy to use OpenGLES 2.0!
     GLSurfaceView gl;
     Square mSquare;
 
     class Square {
         // Straight from the API guide
         private final String vertexShaderCode =
             "attribute vec4 a_position;" +
             "attribute vec4 a_color;" +
             "varying vec4 v_color;" +
             "void main() {" +
             "  gl_Position = a_position;" +
             "  v_color = a_color;" +
             "}";
 
         private final String fragmentShaderCode =
             "precision mediump float;" +
             "varying vec4 v_color;" +
             "void main() {" +
             "  gl_FragColor = v_color;" +
             "}";
 
         private final FloatBuffer vertexBuffer;
         private final FloatBuffer colorBuffer;
         private final int mProgram;
         private int mPositionHandle;
         private int mColorHandle;
 
         private ShortBuffer drawListBuffer;
 
 
         // number of coordinates per vertex in this array
         final int COORDS_PER_VERTEX = 3;
         float squareCoords[] = { -1f,  1f, 0f,   // top left
                                  -1f, -1f, 0f,   // bottom left
                                   1f, -1f, 0f,   // bottom right
                                   1f,  1f, 0f }; // top right
 
         private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices (CCW)
         
         private final float HUES[] = { // reverse order due to CCW winding
                 60,  // yellow
                 120, // green
                 343, // red
                 200, // blue
         };
         
         private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
         private final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
 
         private float cornerFrequencies[] = new float[vertexCount];
         private int cornerRotation;
         
         final int COLOR_PLANES_PER_VERTEX = 4;
         private final int colorStride = COLOR_PLANES_PER_VERTEX * 4; // bytes per vertex
 
         // Set color with red, green, blue and alpha (opacity) values
         float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
 
         public Square() {
             for (int i=0; i<vertexCount; i++) {
                 cornerFrequencies[i] = 1f + (float)(Math.random() * 5); 
             }
             cornerRotation = (int)(Math.random() * vertexCount);
             // initialize vertex byte buffer for shape coordinates
             ByteBuffer bb = ByteBuffer.allocateDirect(
             // (# of coordinate values * 4 bytes per float)
                     squareCoords.length * 4);
             bb.order(ByteOrder.nativeOrder());
             vertexBuffer = bb.asFloatBuffer();
             vertexBuffer.put(squareCoords);
             vertexBuffer.position(0);
             
             bb = ByteBuffer.allocateDirect(vertexCount * colorStride);
             bb.order(ByteOrder.nativeOrder());
             colorBuffer = bb.asFloatBuffer();
 
             // initialize byte buffer for the draw list
             ByteBuffer dlb = ByteBuffer.allocateDirect(
             // (# of coordinate values * 2 bytes per short)
                     drawOrder.length * 2);
             dlb.order(ByteOrder.nativeOrder());
             drawListBuffer = dlb.asShortBuffer();
             drawListBuffer.put(drawOrder);
             drawListBuffer.position(0);
 
             // prepare shaders and OpenGL program
             int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER,
                                                        vertexShaderCode);
             int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                          fragmentShaderCode);
 
             mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
             GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
             GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
             GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
         }
 
         final float[] _tmphsv = new float[3];
         public void draw() {
             // Add program to OpenGL environment
             GLES20.glUseProgram(mProgram);
 
             // get handle to vertex shader's a_position member
             mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
 
             // Enable a handle to the triangle vertices
             GLES20.glEnableVertexAttribArray(mPositionHandle);
 
             // Prepare the triangle coordinate data
             GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                                          GLES20.GL_FLOAT, false,
                                          vertexStride, vertexBuffer);
 
             // same thing for colors
             long now = SystemClock.uptimeMillis();
             colorBuffer.clear();
             final float t = (float)now / 4000f; // set the base period to 4sec
 //            android.util.Slog.v("Colors", "t=" + t);
             for(int i=0; i<vertexCount; i++) {
                 final float freq = (float) Math.sin(2 * Math.PI * t / cornerFrequencies[i]);
                 _tmphsv[0] = HUES[(i + cornerRotation) % vertexCount];
                 _tmphsv[1] = 1f;
                 _tmphsv[2] = freq * 0.25f + 0.75f;
                 final int c = Color.HSVToColor(_tmphsv);
                 colorBuffer.put((float)((c & 0xFF0000) >> 16) / 0xFF);
                 colorBuffer.put((float)((c & 0x00FF00) >> 8) / 0xFF);
                 colorBuffer.put((float)(c & 0x0000FF) / 0xFF);
                 colorBuffer.put(/*a*/ 1f);
             }
             colorBuffer.position(0);
             mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_color");
             checkGlError("glGetAttribLocation");
             GLES20.glEnableVertexAttribArray(mColorHandle);
             checkGlError("glEnableVertexAttribArray");
             GLES20.glVertexAttribPointer(mColorHandle, COLOR_PLANES_PER_VERTEX,
                     GLES20.GL_FLOAT, false,
                     colorStride, colorBuffer);
             checkGlError("glVertexAttribPointer");
 
             // Draw the triangle
             GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
 
             // Disable vertex array
             GLES20.glDisableVertexAttribArray(mPositionHandle);
             GLES20.glDisableVertexAttribArray(mColorHandle);
         }
 
         public int loadShader(int type, String shaderCode){
 
             // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
             // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
             int shader = GLES20.glCreateShader(type);
 
             // add the source code to the shader and compile it
             GLES20.glShaderSource(shader, shaderCode);
             GLES20.glCompileShader(shader);
 
             return shader;
         }
 
         /**
          * Utility method for debugging OpenGL calls. Provide the name of the call
          * just after making it:
          *
          * <pre>
          * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
          * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
          *
          * If the operation is not successful, the check throws an error.
          *
          * @param glOperation - Name of the OpenGL call to check.
          */
         public void checkGlError(String glOperation) {
             int error;
             while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                 Log.e("GL", glOperation + ": glError " + error);
                 throw new RuntimeException(String.format("%s: glError 0x%04x", glOperation, error));
             }
         }
     }
 
     private class ColorsRenderer implements GLSurfaceView.Renderer {
         public void onSurfaceCreated(GL10 unused, EGLConfig config) {
             mSquare = new Square();
             GLES20.glClearColor(0f, 0f, 0f, 1.0f);
         }
 
         public void onDrawFrame(GL10 unused) {
             GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
 
             mSquare.draw();
         }
 
         public void onSurfaceChanged(GL10 unused, int width, int height) {
             GLES20.glViewport(0, 0, width, height);
         }
     }
 
     @Override
     public void onStart() {
         super.onStart();
     }
 
     class TheySaidIHadToHaveAGLSurfaceView extends GLSurfaceView {
 
         public TheySaidIHadToHaveAGLSurfaceView(Context context){
             super(context);
 
             setEGLContextClientVersion(2);
 
             setRenderer(new ColorsRenderer());
         }
     }
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
         
        setFullscreen(true);
         setInteractive(false);
         
         gl = new TheySaidIHadToHaveAGLSurfaceView(Colors.this);
         gl.postDelayed(new Runnable() {
             public void run() {
                 Colors.this.setContentView(gl);
             }
         }, 1000);
     }
 }
