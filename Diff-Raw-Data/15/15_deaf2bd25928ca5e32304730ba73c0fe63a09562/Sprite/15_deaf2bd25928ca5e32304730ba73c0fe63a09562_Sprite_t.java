 package com.hruivo.voidframework;
 
 public class Sprite extends Mesh {
 
 	public Sprite() {
 		float[] vertices = new float[] {
 				-0.0f,  10.0f, 0.0f,
 				-0.0f, -0.0f, 0.0f,
 				 10.0f, -0.0f, 0.0f,
 				 10.0f,  10.0f, 0.0f
 				 };
 		
		short[] indices = new short[] { 0, 2, 1, 0, 3, 2 };
 		
 		float[] texCoords = new float[] {
 			1.0f, 1.0f,
 			1.0f, 0.0f,
 			0.0f, 0.0f,
 			0.0f, 1.0f
 		};
 		
 		setIndices(indices);
 		setVertices(vertices);
 		setTextureCoordinates(texCoords);
 	}
 
 }
