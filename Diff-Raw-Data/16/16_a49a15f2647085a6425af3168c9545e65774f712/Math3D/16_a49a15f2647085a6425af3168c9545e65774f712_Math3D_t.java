 package net.joesoft.andoria.utils;
 
 import com.badlogic.gdx.math.Vector3;
 
 public class Math3D {
 
 	public static Vector3 calcNormal(final Vector3 p1, final Vector3 p2, final Vector3 p3) {
 		final Vector3 u = p2.cpy().sub(p1);
 		final Vector3 v = p3.cpy().sub(p1);
 
		return u.crs(v);
	}

	public static Vector3 sum(Vector3... vectors) {
		final int numVectors = vectors.length;
		final Vector3 result = new Vector3(vectors[0]);

		for(int i = 1; i < numVectors; ++i) {
			result.add(vectors[i]);
		}
 
		return result;
 	}
 }
