 package com.ahmet.polyshaping;
 
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.math.Vector2;
 
 public class Geometry {
 	public static Vector2 calculate_closest_point(Vector2 p1, Vector2 p2,
 			Vector2 point) {
 		double xDelta = p2.x - p1.x;
 		double yDelta = p2.y - p1.y;
 
		double u = ((point.x - p1.x) * xDelta + (point.y - p1.y) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
		 Gdx.app.log("u", Double.toString(u));
 		if (u > 1) {
 			u = 1;
 		} else if (u < 0) {
 			u = 0;
 		}
 
 		int pointx = (int) (p1.x + u * xDelta);
 		int pointy = (int) (p1.y + u * yDelta);
 
 		return new Vector2(pointx, pointy);
 	}
 
 	public static boolean is_orthogonal(Vector2 p1, Vector2 p2, Vector2 point) {
 		double xDelta = p2.x - p1.x;
 		double yDelta = p2.y - p1.y;
 		
 		double plus = (xDelta * xDelta + yDelta * yDelta);
 		double u;
 		
 		if(plus==0)
 			u = 2;
 		else
 			u = ((point.x - p1.x) * xDelta + (point.y - p1.y) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
 		
		//Gdx.app.log("is_orthogonal", Double.toString((xDelta * xDelta + yDelta * yDelta)));
		
 		return (u > 1 || u < 0);
 	}
 
 	public static int[] find_closest_point_in_vertex(Vector2[] vertex,
 			Vector2 point) {
 		TreeMap<Integer, Float> sorted_list = Geometry
 				.get_Sorted_Closest_Vertex(vertex, point.x, point.y);
 		int[] is_border = null;
 		int[] keys = new int[2];
 		keys[1] = -1;
 		int z = 0;
 		int order = -1;
 		for (Integer key : sorted_list.keySet()) {
 			if (z > 1)
 				break;
 			if (Math.abs(key - order) == 1
 					|| Math.abs(key - order) == sorted_list.size() - 1
 					|| order == -1) {
 				// Gdx.app.log("vertex", "key/value: " + key +
 				// "/"+sorted_distance.get(key));
 				if (order != -1
 						&& Geometry.is_orthogonal(vertex[key], vertex[order],
 								point)) {
 					is_border = new int[] { order, key };
 					continue;
 				}
 				keys[z] = key;
 				z++;
 				order = key;
 			}
 		}
 		if (is_border != null && keys[1] != -1) {
 			Vector2 check_border = Geometry.calculate_closest_point(
 					vertex[is_border[0]], vertex[is_border[1]], point);
 			Vector2 check_normal = Geometry.calculate_closest_point(
 					vertex[keys[0]], vertex[keys[1]], point);
 			return (Math.hypot(check_border.x - point.x, check_border.y
 					- point.y) < Math.hypot(check_normal.x - point.x,
 					check_normal.y - point.y)) ? is_border : keys;
 		} else if (keys[1] == -1)
 			return is_border;
 		else
 			return keys;
 
 	}
 
 	public static TreeMap<Integer, Float> get_Sorted_Closest_Vertex(
 			Vector2 vertex[], float x, float y) {
 		HashMap<Integer, Float> map = new HashMap<Integer, Float>();
 		for (int i = 0; i < vertex.length; i++) {
 			map.put(i, (float) Math.hypot(vertex[i].x - x, vertex[i].y - y));
 		}
 		ValueComparator bvc = new ValueComparator(map);
 		TreeMap<Integer, Float> sorted_map = new TreeMap<Integer, Float>(bvc);
 		sorted_map.putAll(map);
 
 		return sorted_map;
 	}
 }
 
 class ValueComparator implements Comparator {
 
 	Map base;
 
 	public ValueComparator(Map base) {
 		this.base = base;
 	}
 
 	public int compare(Object a, Object b) {
 
 		if ((Float) base.get(a) > (Float) base.get(b)) {
 			return 1;
 		} else if ((Float) base.get(a) == (Float) base.get(b)) {
 			return 0;
 		} else {
 			return -1;
 		}
 	}
 }
