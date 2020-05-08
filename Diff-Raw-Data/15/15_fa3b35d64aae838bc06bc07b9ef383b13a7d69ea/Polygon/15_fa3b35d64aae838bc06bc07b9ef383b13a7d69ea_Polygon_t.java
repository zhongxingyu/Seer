 package com.receiver2d.engine.geometry;
 
 import java.util.*;
 
 import com.receiver2d.engine.Vector2D;
import com.receiver2d.engine.Console;
 
 /**
  * 
  */
 public class Polygon implements Iterable<Point2D> {
 	/**
 	 * The vertices of the Polygon.
 	 */
 	protected Point2D[] verts;
 	
 	/**
 	 * The number of vertices in the Polygon.
 	 */
 	protected int length;
 	
 	/**
 	 * Creates a new Polygon from Vector2D points.
 	 * @param pnts The points to use.
 	 */
 	public Polygon(Vector2D... pnts) {
 		verts = new Point2D[pnts.length];
 		for (int i=0; i<pnts.length; i++) {
 			verts[i] = new Point2D(pnts[i]);
 			if (i > 0) verts[i].pointFrom(verts[i-1]); // connect to previous
 		}
 		length = verts.length;
 	}
 	
 	/**
 	 * Creates a new Polygon from numerous (x,y) coordinates. For this method,
 	 * the parameters are read in pairs, so (n,n+1) will be point n. Any
 	 * parameter n+2 that doesn't have its corresponding n+3 coordinate will
 	 * not be used.
 	 * @param ds The (x,y) coordinates to use.
 	 */
 	public Polygon(float... fs) {
 		Point2D[] fpnts = new Point2D[fs.length/2];
		for (int i=0,f=0; i+1<fs.length; i+=2,f=i/2) {
			fpnts[f] = new Point2D(fs[i], fs[i+1]);
			if (f > 0) fpnts[f].pointFrom(fpnts[f-1]);
 		}
 		verts = fpnts;
 		length = verts.length;
 	}
 	
 	/**
 	 * Creates a new Polygon from numerous (x,y) coordinates. For this method,
 	 * the parameters are read in pairs, so (n,n+1) will be point n. Any
 	 * parameter n+2 that doesn't have its corresponding n+3 coordinate will
 	 * not be used.
 	 * @param ds The (x,y) coordinates to use.
 	 */
 	public Polygon(double... ds) {
 		Point2D[] dpnts = new Point2D[ds.length/2];
		for (int i=0,d=0; i+1<ds.length; i+=2,d=0) {
			dpnts[d] = new Point2D((float)ds[i], (float)ds[i+1]);
			if (d > 0) dpnts[d].pointFrom(dpnts[d-1]);
 		}
 		verts = dpnts;
 		length = verts.length;
 	}
 	
 	/**
 	 * Gets a vertex of the Polygon at a specified index.
 	 * @param p The index of the vertex.
 	 * @return The vertex located at index p.
 	 */
 	public Point2D get(int p) {
 		if (p > 0)
 			verts[p].pointFrom(verts[p-1]);
 		return verts[p];
 	}
 	
 	/**
 	 * @return The vertices in the Polygon.
 	 */
 	public Vector2D[] vertices() {
 		return verts;
 	}
 	
 	/**
 	 * @return The number of vertices in the Polygon.
 	 */
 	public int length() {
 		return length;
 	}
 	
 	/**
 	 * Allows for {@code for (Vector2D vec : poly)}
 	 */
 	@Override
 	public Iterator<Point2D> iterator() {
 		return new Iterator<Point2D>() {
 
 			private int index = 0;
 			
 			@Override
 			public boolean hasNext() {
 				return index<verts.length && verts[index] != null;
 			}
 
 			@Override
 			public Point2D next() {
 				if (index > 0) // set our points to update only when accessed
 					verts[index].pointFrom(verts[index-1]);
 				return verts[index++];
 			}
 
 			@Override
 			public void remove() {
 				Point2D[] newArr = new Point2D[verts.length-1];
 				
 				System.arraycopy(verts, 0, newArr, 0, index);
 				System.arraycopy(verts, index+1, newArr, 
 											index, verts.length-index-1);
 				for (int i=1; i<newArr.length; i++)
 					newArr[i].pointFrom(newArr[i-1]);
 				
 				verts = newArr; // reset array
 				length = verts.length;
 			}
 			
 		};
 	}
 }
