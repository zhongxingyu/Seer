package com.d.app;
 
 import android.opengl.GLES20;
 import android.util.Log;
 
 /**
  * Tiny helper class that uploads GL ES 2.0 shaders.
  */
 public class Programs {
   
     private static String kLogTag = "GDC11";
 
     private static int getShader(String source, int type) {
         int shader = GLES20.glCreateShader(type);
         if (shader == 0) return 0;
         
         GLES20.glShaderSource(shader, source);
         GLES20.glCompileShader(shader);
         int[] compiled = { 0 };
         GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
         if (compiled[0] == 0) {
             Log.e(kLogTag, GLES20.glGetShaderInfoLog(shader));
         }
         return shader;
     }
 
     public static int loadProgram(String vertexShader,
             String fragmentShader) {
         int vs = getShader(vertexShader, GLES20.GL_VERTEX_SHADER);
         int fs = getShader(fragmentShader, GLES20.GL_FRAGMENT_SHADER);
         if (vs == 0 || fs == 0) return 0;
 
         int program = GLES20.glCreateProgram();
         GLES20.glAttachShader(program, vs);
         GLES20.glAttachShader(program, fs);
         GLES20.glLinkProgram(program);
 
         int[] linked = { 0 };
         GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linked, 0);
         if (linked[0] == 0) {
             Log.e(kLogTag, GLES20.glGetProgramInfoLog(program));
             return 0;
         }
         return program;
     }
 }
