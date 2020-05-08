 package com.bernerbits.util.geom;
 
 import java.util.Iterator;
import java.util.List;
 
import javax.vecmath.Vector2d;
import javax.vecmath.Vector2f;
 
 public final class Polygon2D {
 	
 	private boolean clockwise;
 	private double maxX;
 	private double minX;
 	private double maxY;
 	private double minY;
 	
 	public double getMaxX() {
 		return maxX;
 	}
 	public double getMinX() {
 		return minX;
 	}
 	public double getMaxY() {
 		return maxY;
 	}
 	public double getMinY() {
 		return minY;
 	}
 	
 	private Polygon2DNode first;
 	
 	public Polygon2D(Point2D... points) {
 		if(points.length < 3) {
 			throw new IllegalArgumentException("Cannot create a polygon with fewer than 3 points.");
 		}
 		Polygon2DNode prev = null;
 		Polygon2DNode current = null;
 		for(Point2D point : points) {
 			current = new Polygon2DNode(point);
 			if(first == null) {
 				first = current;
 			}
 			if(prev != null) {
 				prev.setNext(current);
 				current.setPrev(prev);
 			}
 			prev = current;
 		}
 		current.setNext(first);
 		first.setPrev(current);
 		
 		calculateExtents();
 		calculateWinding();
 		calculateTextureAndNormals();
 	}
 	
 	public Iterable<Polygon2DNode> getNodes() {
 		return new Iterable<Polygon2DNode>() {
 			@Override
 			public Iterator<Polygon2DNode> iterator() {
 				return new Iterator<Polygon2DNode>() {
 					private Polygon2DNode current = null;
 
 					@Override
 					public boolean hasNext() {
 						if(current == null) {
 							return true;
 						}
 						return current.next() != first;
 					}
 
 					@Override
 					public Polygon2DNode next() {
 						if(current == null) {
 							current = first;
 							return current;
 						}
 						current = current.next();
 						return current;
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException("Removing poly node with iterator is not supported");
 					}
 					
 				};
 			}
 		};
 	}
 	
 	public Iterable<Polygon2DNode> getClosedNodes() {
 		return new Iterable<Polygon2DNode>() {
 			@Override
 			public Iterator<Polygon2DNode> iterator() {
 				return new Iterator<Polygon2DNode>() {
 					private Polygon2DNode current = null;
 					private boolean start = true;
 					
 					@Override
 					public boolean hasNext() {
 						if(current == null) {
 							return true;
 						}
 						return start || current != first;
 					}
 
 					@Override
 					public Polygon2DNode next() {
 						if(current == null) {
 							current = first;
 							return current;
 						}
 						start = false;
 						current = current.next();
 						return current;
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException("Removing poly node with iterator is not supported");
 					}
 					
 				};
 			}
 		};
 	}
 
 	private void calculateTextureAndNormals() {
 		double texX = 0;
 		for(Polygon2DNode node : getNodes()) {
 			Polygon2DNode prev = node.prev();
 			Polygon2DNode next = node.next();
 			
 			double normalX = next.getY() - prev.getY();
 			double normalZ = -(next.getX() - prev.getX());
 			
 			node.setNormalAndNormalize(normalX,normalZ);
 			node.setTextureCoordinate(texX);
 			
 			double dist = Math.sqrt((next.getY() - node.getY())*(next.getY() - node.getY()) + (next.getX() - node.getX())*(next.getX() - node.getX()));
 			texX += dist;
 		}
 	}
 
 	private void calculateExtents() {
 		maxX = Double.NEGATIVE_INFINITY;
 		minX = Double.POSITIVE_INFINITY;
 		maxY = Double.NEGATIVE_INFINITY;
 		minY = Double.POSITIVE_INFINITY;
 		
 		for(Polygon2DNode node : getNodes()) {
 			if(node.getX() > maxX) {
 				maxX = node.getX();
 			}
 			if(node.getX() < minX) {
 				minX = node.getX();
 			}
 			if(node.getY() > maxY) {
 				maxY = node.getY();
 			}
 			if(node.getY() < minY) {
 				minY = node.getY();
 			}
 		}
 	}
 	
 	private void calculateWinding() {
 		// Adapted from algorithm here: http://www.gamedev.net/topic/447552-determining-winding-order-in-2d-polygon/
 		int insideCount = 0;
 		
 		for(Polygon2DNode node : getNodes()) {
 		
 			// Consider the two adjacent edges as edges of a triangle and get the midpoint of this triangle
 			Point2D midpoint = GeometryFunctions.triangleBarycenter(
 					node.prev().toPoint(),
 					node.toPoint(),
 					node.next().toPoint()
 			);
 			
 			// Get the normals to the two edge vectors: in the case of a clockwise
 			// polygon they will point towards the inside of the polygon.
			Vector2d normal1 = new Vector2d(
					- (node.getY() - node.prev().getY()),
					node.getX() - node.prev().getX()
 			);
 			
			Vector2d normal2 = new Vector2d(
					- (node.next().getY() - node.getY()),
					node.next().getX() - node.getX()
 			);
 			
 			// Now get a vector going from the shared point of the two edges to the midpoint of the triangle
			Vector2d testVector = new Vector2d(
				midpoint.getX() - node.getX(),
				midpoint.getY() - node.getY()
 			);
 			
 			// Now test if this point is inside the 'triangle' mentioned previously by
 			// dotting the testVector with the two normals we got. A true point inside
 			// triangle test would check against the normals of all three triangle edges. 
 			// But for this test we only use two: if either of the dot products is zero
 			// then consider the point to be 'outside' the triangle.
 			if(testVector.dot(normal1) < 0 || testVector.dot(normal2) < 0) {
 				insideCount++;
 			} else {
 				insideCount--;
 			}
 			
 		}
 		clockwise = (insideCount >= 0);
 	}
 	
 	public boolean isClockwise() {
 		return clockwise;
 	}
 	
 }
