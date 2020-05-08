 package com.example.android.opengl;
 
 import java.util.Arrays;
 import java.util.Stack;
 
 import android.opengl.Matrix;
 
 public class GLState {
 
 	public static final float[] pMatrix = new float[16];
 	public static final float[] mvMatrix = new float[16];
 	public static final float[] nMatrix = new float[9];
     private static final float[] nInvertMatrix = new float[16];
     private static final float[] nInvertTransposeMatrix = new float[16];
     private static final float[] tmpMatrix = new float[16];
     
     private static final Stack<float[]> projectionStack = new Stack<float[]>();
     private static final Stack<float[]> modelviewStack = new Stack<float[]>();
 	
 	public GLState() {
 		
 	}
 	
 	public static void setMVIdentity(){
 		Matrix.setIdentityM(mvMatrix, 0);
 	}
 	
 	public static void translate(float x, float y, float z){
 		Matrix.translateM(mvMatrix, 0, x, y, z);
 	}
 	
 	public static void rotate(float a, float x, float y, float z){
 		Matrix.rotateM(mvMatrix, 0, a, x, y, z);
 	}
 	
 	public static void scale(float x, float y, float z){
 		Matrix.scaleM(mvMatrix, 0, x, y, z);
 		
 	}
 	
 	public static void pushMVMatrix(){
        modelviewStack.push(mvMatrix);
     }
 
 	public static void popMVMatrix(){
 		float popped[] = modelviewStack.pop();
 		for (int i = 0; i < 16; i++){
 			mvMatrix[i] = popped[i];
 		}
 		
     }
 
 	public static void pushPMatrix(){
        projectionStack.push(pMatrix);
     }
 	
 	public static void popPMatrix(){
		float popped[] = new float[16];
 		for (int i = 0; i < 16; i++){
 			mvMatrix[i] = popped[i];
 		}
     }
 	
 	public static void setNMatrix(){
 
         Matrix.invertM(nInvertMatrix, 0, mvMatrix, 0);
         Matrix.transposeM(nInvertTransposeMatrix, 0,nInvertMatrix, 0);
 
         nMatrix[0] = nInvertTransposeMatrix[0];
         nMatrix[1] = nInvertTransposeMatrix[1];
         nMatrix[2] = nInvertTransposeMatrix[2];
 
         nMatrix[3] = nInvertTransposeMatrix[4];
         nMatrix[4] = nInvertTransposeMatrix[5];
         nMatrix[5] = nInvertTransposeMatrix[6];
 
         nMatrix[6] = nInvertTransposeMatrix[8];
         nMatrix[7] = nInvertTransposeMatrix[9];
         nMatrix[8] = nInvertTransposeMatrix[10];
     }
 
 }
