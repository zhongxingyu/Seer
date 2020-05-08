 package com.sirolf2009.syringe.client.models;
 
 import java.util.ArrayList;
 
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.opengl.Texture;
 
 public class Model3D {
 
 	public ArrayList<float[]> vertexsets = new ArrayList<float[]>(); // Vertex Coordinates
 	public ArrayList<float[]> vertexsetsnorms = new ArrayList<float[]>(); // Vertex Coordinates Normals
 	public ArrayList<float[]> vertexsetstexs = new ArrayList<float[]>(); // Vertex Coordinates Textures
 	public ArrayList<int[]> faces = new ArrayList<int[]>(); // Array of Faces (vertex sets)
 	public ArrayList<int[]> facestexs = new ArrayList<int[]>(); // Array of of Faces textures
 	public ArrayList<int[]> facesnorms = new ArrayList<int[]>(); // Array of Faces normals
 
 	public int objectlist;
 	public int numpolys = 0;
 
 	////Statisitcs for drawing ////
 	public float toppoint = 0;		// y+
 	public float bottompoint = 0;	// y-
 	public float leftpoint = 0;		// x-
 	public float rightpoint = 0;	// x+
 	public float farpoint = 0;		// z-
 	public float nearpoint = 0;		// z+
 	
 	////Statisitcs for collision ////
 	public float posX1 = 0;		// y+
 	public float posY1 = 0;		// y-
 	public float posZ1 = 0;		// x-
 	public float posX2 = 0;		// x+
 	public float posY2 = 0;		// z-
 	public float posZ2 = 0;		// z+
 
 	public Texture texture;
 	
 	public AABB AABB;
 
 	public void cleanUp() {
 		vertexsets.clear();
 		vertexsetsnorms.clear();
 		vertexsetstexs.clear();
 		faces.clear();
 		facestexs.clear();
 		facesnorms.clear();
 	}
 
 	public float getXWidth() {
 		float returnval = 0;
 		returnval = rightpoint - leftpoint;
 		return returnval;
 	}
 
 	public float getYHeight() {
 		float returnval = 0;
 		returnval = toppoint - bottompoint;
 		return returnval;
 	}
 
 	public float getZDepth() {
 		float returnval = 0;
 		returnval = nearpoint - farpoint;
 		return returnval;
 	}
 
 	public int numpolygons() {
 		return numpolys;
 	}
 
 	public void opengldraw() {
		texture.bind();
 		GL11.glCallList(objectlist);
 	}
 
 }
