 package net.joesoft.andoria.utils;
 
 import com.badlogic.gdx.math.Vector3;
 
 public class Math3D {
 
 	public static Vector3 calcNormal(final Vector3 p1, final Vector3 p2, final Vector3 p3) {
 		final Vector3 u = p2.cpy().sub(p1);
 		final Vector3 v = p3.cpy().sub(p1);
 
		float nx = u.x * v.z - u.z * v.y;
		float ny = u.z * v.x - u.x * v.z;
		float nz = u.x * v.y - u.y * v.x;
 
		return new Vector3(nx, ny, nz).nor();
 	}
 }
