 package org.voxeltech.game;
 
 import java.util.ArrayList;
 
 import org.voxeltech.graphics.*;
 import org.voxeltech.utils.*;
 import org.voxeltech.noise.*;
 
 public class WorldChunk {
 	
 	private final static int SIZE = 20;
 
 	private ProgramClock clock = ProgramClock.getInstance();	
 	private ArrayList< ArrayList< ArrayList<Voxel> > > terrian;
 	/** 
 	 * <em>boundaries</em> holds the min and max values of the chunk. 
 	 * <em>Boundaries</em>[0] is the x-axis, <em>boundaries</em>[1]
 	 * is the y-axis, and <em>boundaries</em>[2] is the z-axis.
 	 */
 	private float[][] boundaries; 
 	
 	/**
 	 * <em>coordinates</em> refer to the chunk coordinates. (does not correspond
 	 * to actual world coordinates)
 	 */
 	public int[] coordinates;
 	
 	/**
 	 * <em>origin</em> refers to the top-left actual coordinates. (this corresponds
 	 * to actual world coordinates, i.e. where the chunk is drawn) 
 	 */
 	public float[] origin;
 
 	public WorldChunk() {
 		terrian = new ArrayList<ArrayList<ArrayList<Voxel>>>(SIZE);
 		for(ArrayList<ArrayList<Voxel>> a1 : terrian) {
 			a1 = new ArrayList<ArrayList<Voxel>>(SIZE);
 			for(ArrayList<Voxel> a2 : a1) {
 				a2 = new ArrayList<Voxel>(SIZE);
 			}
 		}
 	}
 	
 	public WorldChunk(float x, float y, float z) {
 		this();
 		
 		origin[0] = x;
 		origin[1] = y;
 		origin[2] = z;
 		
 		coordinates[0] = (int)(x / SIZE);
 		coordinates[1] = (int)(y / SIZE);
 		coordinates[2] = (int)(z / SIZE);
 		
 		boundaries[0][0] = x;
 		boundaries[0][1] = x+(SIZE*Voxel.SIZE);

		boundaries[1][0] = y;
		boundaries[1][1] = y+(SIZE*Voxel.SIZE);
 		
		boundaries[2][0] = z;
		boundaries[2][1] = z+(SIZE*Voxel.SIZE);
 		
 		generateChunk();
 	}
 	
 	private void generateChunk() {
 		for(int i = 0; i < SIZE; i++) {
 			for(int j = 0; j < SIZE; j++) {
 				for(int k = 0; k < SIZE; k++) {
 					Voxel v = new Voxel(i*Voxel.SIZE, j*Voxel.SIZE, k*Voxel.SIZE);
 					
 					double sn = SimplexNoise.noise(v.position.x, v.position.y, v.position.z);
 			        if(sn <= 0.3)
 			            v.turnOff();
 			        if(sn > 0.3)
 			            v.turnOn();
 					
 					terrian.get(i).get(j).add(v);
 				}
 			}
 		}
 	}
 	
 }
