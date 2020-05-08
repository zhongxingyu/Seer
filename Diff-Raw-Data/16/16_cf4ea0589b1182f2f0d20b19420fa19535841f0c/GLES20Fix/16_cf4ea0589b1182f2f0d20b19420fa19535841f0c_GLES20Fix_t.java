package com.d.app;
 
 public class GLES20Fix {
     native public static void glDrawElements(
             int mode, int count, int type, int offset);
     native public static void glVertexAttribPointer(
             int index, int size, int type,
             boolean normalized, int stride, int offset);
  
     private GLES20Fix() {}
     static {
         System.loadLibrary("GLES20Fix");
     }
 }
