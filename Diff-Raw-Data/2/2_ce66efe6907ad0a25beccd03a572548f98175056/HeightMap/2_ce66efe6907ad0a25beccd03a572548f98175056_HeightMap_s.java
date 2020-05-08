 package de.redlion.civilwar.collision;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.g3d.model.still.StillModel;
 import com.badlogic.gdx.graphics.g3d.model.still.StillSubMesh;
 import com.badlogic.gdx.math.Intersector;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.math.collision.BoundingBox;
 import com.badlogic.gdx.math.collision.Ray;
 
 import de.redlion.civilwar.Helper;
 
 public class HeightMap {
 
 	final int HEIGHTMAPSIZE = 256;
 
 	public float[] map;
 	public float[][] heightMap;
 
 	BoundingBox boundingBox;
 
 	public HeightMap(StillModel plane) {
 		boundingBox = new BoundingBox();
 
 		// get max bounds from model
 		int len = plane.subMeshes.length;
 		for (int i = 0; i < len; i++) {
 			StillSubMesh subMesh = plane.subMeshes[i];
 			BoundingBox bb = new BoundingBox();
 			subMesh.getBoundingBox(bb);
 			boundingBox.ext(bb);
 		}
 
 		System.out.println(boundingBox);
 
 		heightMap = new float[HEIGHTMAPSIZE][HEIGHTMAPSIZE];
 
 		// iterate through model and generate grid from vertices height
 		len = plane.subMeshes.length;
 		for (int i = 0; i < len; i++) {
 			StillSubMesh subMesh = plane.subMeshes[i];
 
 			map = new float[subMesh.mesh.getNumVertices() * 3];
 
 			int j = 0;
 			for (int n = 0; n < subMesh.mesh.getNumVertices() * 8; n = n + 8) {
 				// y is height
 				float x = subMesh.mesh.getVerticesBuffer().get(n);
 				float y = subMesh.mesh.getVerticesBuffer().get(n + 1);
 				float z = subMesh.mesh.getVerticesBuffer().get(n + 2);
 
 				map[j] = x;
 				map[j + 1] = y;
 				map[j + 2] = z;
 
 				j = j + 3;
 			}
 		}
 
		FileHandle fileIn = Gdx.files.internal("data/heightMap.dat");
 		System.out.println(fileIn.exists());
 		if (fileIn.exists()) {
 			try {
 				ObjectInputStream iis = new ObjectInputStream(fileIn.read());
 				heightMap = (float[][]) iis.readObject();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		} else {
 
 			// ray collision test for each point of the height map
 			Ray ray = new Ray(Vector3.X, Vector3.Y);
 			Vector3 localIntersection = new Vector3();
 			for (int x = 0; x < HEIGHTMAPSIZE; x++) {
 				for (int z = 0; z < HEIGHTMAPSIZE; z++) {
 
 					float x1 = Helper.map(x, 0, HEIGHTMAPSIZE, boundingBox.min.x, boundingBox.max.x);
 					float z1 = Helper.map(z, 0, HEIGHTMAPSIZE, boundingBox.min.z, boundingBox.max.z);
 
 					ray.set(new Vector3(x1, -100, z1), Vector3.Y);
 
 					Intersector.intersectRayTriangles(ray, map, localIntersection);
 					heightMap[x][z] = localIntersection.y;
 				}
 			}
 
 			try {
 				FileHandle fileOut = Gdx.files.external("heightMap.dat");
 				fileOut.file().createNewFile();
 				ObjectOutputStream oos = new ObjectOutputStream(fileOut.write(false));
 				oos.writeObject(heightMap);
 			} catch (Exception e) {
 				System.out.println(e);
 			}
 		}
 
 	}
 
 	public float getHeight(float x, float z) {
 		// TODO do fancy interpolation stuff or move this to GPU?
 		int x1 = (int) Math.floor(Helper.map(x, boundingBox.min.x, boundingBox.max.x, 0, HEIGHTMAPSIZE));
 		int z1 = (int) Math.floor(Helper.map(z, boundingBox.min.z, boundingBox.max.z, 0, HEIGHTMAPSIZE));
 
 		return heightMap[x1][z1];
 	}
 
 }
