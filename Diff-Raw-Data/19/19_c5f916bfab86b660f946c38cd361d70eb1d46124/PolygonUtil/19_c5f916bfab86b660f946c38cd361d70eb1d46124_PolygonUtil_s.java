 	package com.secondhand.math;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import com.badlogic.gdx.math.Vector2;
 
 public final class PolygonUtil {
 
 	private PolygonUtil() {}	
 	
 	/**
 	 * See wikipedia for the definition of a convex polygon.
 	 * @param polygon
 	 * @return whether the poly is convex.
 	 */
 	public static boolean  isConvex(final List<Vector2> polygon) {
 		
 		if ( polygon.size() < 3 ) {
 			throw new IllegalArgumentException("A polygon with less than 3 points is not a polygon!");
 		}
 		
 		boolean firstPairChecked = false;
 		boolean ensureAllPositive = false;
 		boolean ensureAllNegative = false;
 		
 		// go through every consequtive pair of edges. 
 		for(int i = 0; i < polygon.size(); ++i) {
 			// these 2 edges are defined by these 3 points:
 			final Vector2 p1 = polygon.get(i);
 			final Vector2 p2 = polygon.get((i + 1) % polygon.size());
 			final Vector2 p3 = polygon.get((i + 2) % polygon.size());
 			
 			// this vector represents the first edge
 			final float dx1 = p2.x - p1.x;
 			final float dy1 = p2.y - p1.y;
 			
 			// this vector represents the second edge
 			final float dx2 = p3.x - p2.x;
 			final float dy2 = p3.y - p2.y;
 			
 			//
 			final float zcrossproduct = dx1*dy2 - dy1*dx2;
 						
 			/* from the definition of the cross product(if we set the z-component of 
 			 * both vectors to 0), we find that the given polygon is convex only if
 			 * all the computed z-components(zcrossproduct) are all positive or all negative.
 			 * The proof of this is trivial and left as an excecise to the reader :-)
 			 */
 			if(!firstPairChecked) {
 				
 				if(zcrossproduct >= 0) {
 					ensureAllPositive = true;
 				} else {
 					ensureAllNegative = true;
 				}
 				
 				firstPairChecked = true;
 			} else {
 				if(ensureAllPositive && zcrossproduct < 0) {
 					return false;
 				} else if(ensureAllNegative && zcrossproduct >= 0) {
 					return false;
 				}
 			}
 			
 		}
 
 		// now we've checked all the adjacent pairs of edges, so it must be convex.
 		return true;
 	}
 	
 	/**
      * Random float in range -1 to 1
      * @param rng
      * @return
      */
     private static float nextFloat(final Random rng) {
     	return 1.0f / (float)rng.nextInt();
     }
     
     private static List<Vector2> getRandomStartingPolygon(final Random rng) {
     	
     	
     	final List<Vector2> polygonEdges = new ArrayList<Vector2>();
         
     	final int choice = rng.nextInt(3);
     	
     	if(choice == 2) {
     		// 6 edges
     		polygonEdges.add(new Vector2(50,-50));
     		polygonEdges.add(new Vector2(100,0));
     		polygonEdges.add(new Vector2(100,100));
     		polygonEdges.add(new Vector2(50,150));
     		polygonEdges.add(new Vector2(0,100));
     		polygonEdges.add(new Vector2(0,0));
     	} else if(choice == 1) {
     		// 7 edges
     		polygonEdges.add(new Vector2(50,-50));
     		polygonEdges.add(new Vector2(100,0));
     		polygonEdges.add(new Vector2(150,50));
     		polygonEdges.add(new Vector2(100,100));
     		polygonEdges.add(new Vector2(50,150));
     		polygonEdges.add(new Vector2(0,100));
     		polygonEdges.add(new Vector2(0,0));
     	}  else if(choice == 0) {
     		// 8 edges
     		polygonEdges.add(new Vector2(50,-50));
     		polygonEdges.add(new Vector2(100,0));
     		polygonEdges.add(new Vector2(150,50));
     		polygonEdges.add(new Vector2(100,100));
     		polygonEdges.add(new Vector2(50,150));
     		polygonEdges.add(new Vector2(0,100));
     		polygonEdges.add(new Vector2(-50,50));
     		polygonEdges.add(new Vector2(0,0));
     	}
         
         return polygonEdges;
     }
     
     public static List<Vector2> getRandomPolygon() {
     	
     	final int TRANSFORMATIONS = 200;
     	final float MAX_TRANSFORMATION = 20;
     	
     	final Random rng = new Random();
         
    	List<Vector2> polygonEdges = getRandomStartingPolygon(rng);
     	
         for(int i = 0; i < TRANSFORMATIONS; ++i) {
         	// get a random edge point of the polygon
         	final Vector2 v = polygonEdges.get(rng.nextInt(polygonEdges.size()));
         	
         	// randomly tranform it by some random vector.
         	
         	Vector2 transformation;
         	if(i < TRANSFORMATIONS / 2) {
         		transformation = new Vector2((float)rng.nextDouble() * MAX_TRANSFORMATION, (float)rng.nextDouble() * MAX_TRANSFORMATION);
         	}else {
         		transformation = new Vector2((float)nextFloat(rng) * MAX_TRANSFORMATION, (float)nextFloat(rng) * MAX_TRANSFORMATION);
         	}
         	
         	v.add(transformation);
         	
         	/*
         	 * If this transformation the polygon non-convex, we'll have to undo it,
         	 * since Box2D does not support non-convex polygons.
         	 */
         	if(!PolygonUtil.isConvex(polygonEdges)) {
         		v.sub(transformation);
         	}
         }
         
         // now we have to make sure that the polygon ends up approximately at the touched position.
        Vector2 firstV = polygonEdges.get(0);
         for(int i = 1; i < polygonEdges.size(); ++i) {
         	polygonEdges.get(i).sub(firstV);
         }
         // set the zero vector.
         firstV.x = 0;
         firstV.y = 0;
         
         
         return polygonEdges;
     }
     
 }
