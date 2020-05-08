 package com.example.opengl;
 
 public class Vector3 {
 	float x;
 	float y;
	float z; 
 
 	public Vector3(float pX, float pY, float pZ) {
 		x = pX;
 		y = pY;
 		z = pZ;
 	}
 	
 	public static float[] mat4(float x) {
 		float[] result = new float[16];
 		for (int i = 0; i < 16; i++) {
 			if (i % 5 == 0) {
 				result[i] = 1;
 			} else {
 				result[i] = 0;
 			}
 		}
 		return result;
 	}
 	
 	public Vector3 sum(Vector3 p){
 		return new Vector3(p.x + x, p.y + y, p.z + z);
 	}
 	public void add(Vector3 p){
 		x += p.x;
 		y += p.y;
 		z += p.z;
 	}
 	public Vector3 mult(float f){
 		x*=f;
 		y*=f;
 		z*=f;
 		return new Vector3(x * f, y * f, z * f);
 	}
 	public void sub(Vector3 p){
 		x -= p.x;
 		y -= p.y;
 		z -= p.z;		
 	}
 }
