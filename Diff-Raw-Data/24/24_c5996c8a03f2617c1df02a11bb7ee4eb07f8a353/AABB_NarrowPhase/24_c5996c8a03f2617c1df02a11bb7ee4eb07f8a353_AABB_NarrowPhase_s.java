 package edu.ncsu.uhp.escape.engine.collision;
 
 import edu.ncsu.uhp.escape.engine.utilities.math.Point;
 import edu.ncsu.uhp.escape.engine.utilities.*;
 
 /**
  * Collision Detection
  * 
  * @author Tim Mervine
  * 
  */
 public class AABB_NarrowPhase implements INarrowCollision {
 	private Point dimension;
 	private Point offsets;
 
 	/**
 	 * Constructs a Collision Box from the Points dimension and offsets.
 	 * 
 	 * @param dimension
 	 * @param offsets
 	 */
 	public AABB_NarrowPhase(Point dimension, Point offsets) {
 		this.dimension = dimension;
 		this.offsets = offsets;
 	}
 
 	/**
 	 * gets dimension of collision box
 	 * 
 	 * @return dimension
 	 */
 	public Point getDimension() {
 		return dimension;
 	}
 
 	/**
 	 * gets offset of collision box
 	 * 
 	 * @return offsets
 	 */
 	public Point getOffsets() {
 		return offsets;
 	}
 
 	/**
 	 * Creates points for the collision box in a 2D system
 	 * 
 	 * @param position
 	 * @param rotation
 	 * @return The vertices of the 2D box
 	 */
 	public Point[] getPoints(Point position, IRotation rotation) {
 		Point[] vertices = new Point[4];
 		ZAxisRotation angle = new ZAxisRotation(rotation.toGlMatrix());
 		vertices[0] = Point.getOrigin().add(this.offsets);
 		vertices[1] = new Point(0, this.dimension.getY(), 0).add(this.offsets);
 		vertices[2] = new Point(this.dimension.getX(), this.dimension.getY(), 0)
 				.add(this.offsets);
 		vertices[3] = new Point(this.dimension.getX(), 0, 0).add(this.offsets);
 		for (int i = 0; i < vertices.length; i++) {
 			angle.apply(vertices[i]);
 			vertices[i] = vertices[i].add(position);
 		}
 		return vertices;
 	}
 
 	/**
 	 * Finds the slope of the edge of the rotated box
 	 * 
 	 * @param point1
 	 * @param point2
 	 * @return The slope of the edge
 	 */
 
 	public float getSlope(Point point1, Point point2) {
 		float slope = 0;
 		if (point1.getX() >= point2.getX()) {
 			slope = (point1.getY() + (-point2.getY()))
 					/ (point1.getX() - point2.getX());
 		} else {
 			slope = (point2.getY() - point1.getY())
 					/ (point2.getX() - point1.getX());
 		}
 		return slope;
 	}
 
 	/**
 	 * 
 	 * @param slope
 	 * @param point
 	 * @param point1
 	 * @param point2
 	 * @return
 	 */
 
 	public boolean checkSlope(float slope, Point checkPoint, Point point1,
 			Point point2) {
 		boolean intersect = false;
 		float expectedY1 = 0;
 		float expectedY2 = 0;
 		if (point1.getY() <= point2.getY()) {
 			expectedY1 = point1.getY()
 					+ (slope * (checkPoint.getX() - point1.getX()));
 			expectedY2 = point2.getY()
 					+ (slope * (checkPoint.getX() - point2.getX()));
 		} else {
 			expectedY1 = point2.getY()
 					+ (slope * (checkPoint.getX() - point2.getX()));
 			expectedY2 = point1.getY()
 					+ (slope * (checkPoint.getX() - point1.getX()));
 		}
 		if (((expectedY1 <= checkPoint.getY()) && (checkPoint.getY() <= expectedY2))
 				|| ((expectedY1 >= checkPoint.getY() && (checkPoint.getY() >= expectedY2)))) {
 			intersect = true;
 		}
 		return intersect;
 	}
 
 	/**
 	 * Checks for collision
 	 */
 	public boolean doesCollideNarrow(Point thisPosition,
 			IRotation thisRotation, ICollision checkCollide,
 			Point checkPosition, IRotation checkRotation) {
 		boolean collide = false;
 		Point[] box1 = getPoints(thisPosition, thisRotation);
 		ZAxisRotation thisAngle = new ZAxisRotation(thisRotation.toGlMatrix());
 		ZAxisRotation checkAngle = new ZAxisRotation(checkRotation.toGlMatrix());
 		ZAxisRotation betweenAngle;
 		if (thisAngle.getAngle() >= checkAngle.getAngle()) {
 			betweenAngle = new ZAxisRotation(thisAngle.getAngle()
 					+ -checkAngle.getAngle());
 		} else {
 			betweenAngle = new ZAxisRotation(checkAngle.getAngle()
 					- thisAngle.getAngle());
 		}
 		AABB_NarrowPhase boxCheckCollision = null;
 		if (checkCollide instanceof MultiPhaseCollision) {
 			boxCheckCollision = (AABB_NarrowPhase) ((MultiPhaseCollision) checkCollide).narrow;
 		} else if (checkCollide instanceof AABB_NarrowPhase) {
 			boxCheckCollision = (AABB_NarrowPhase) checkCollide;
 		}
 		if (boxCheckCollision != null) {
 			Point[] box2 = boxCheckCollision.getPoints(checkPosition,
 					checkRotation);
 			for (int i = 0; i < box1.length; i++) {
 				thisAngle.negative().apply(box1[i]);
 				thisAngle.negative().apply(box2[i]);
 			}
			float box1x1 = box1[3].getX() - offsets.getX(); // Left x value
			float box1x2 = box1[0].getX() - offsets.getX(); // Right x value
			float box1y1 = box1[0].getY() - offsets.getY(); // Bottom y value
			float box1y2 = box1[1].getY() - offsets.getY(); // Top y value
			float box1z1 = box1[0].getZ() - offsets.getZ(); // Lower z value
			float box1z2 = box1[0].getZ() + this.dimension.getZ()
					- offsets.getZ(); // Higher
 			// z
 			// value
 			float box2x1, box2x2, box2y1, box2y2;
 			float box2z1 = box2[0].getZ() - offsets.getZ(); // Lower z value
 			float box2z2 = boxCheckCollision.getDimension().getZ()
 					+ box2[0].getZ() - offsets.getZ(); // Higher z value
 			if (betweenAngle.getAngle() <= Math.PI / 2) {
 				box2x1 = Math.abs(box2[2].getX()); // Left x value
 				box2x2 = Math.abs(box2[0].getX()); // Right x value
 				box2y1 = Math.abs(box2[3].getY()); // Bottom y value
 				box2y2 = Math.abs(box2[1].getY()); // Top y value
 			} else if (betweenAngle.getAngle() <= Math.PI) {
 				box2x1 = Math.abs(box2[1].getX()); // Left x value
 				box2x2 = Math.abs(box2[3].getX()); // Right x value
 				box2y1 = Math.abs(box2[2].getY()); // Bottom y value
 				box2y2 = Math.abs(box2[0].getY()); // Top y value
 			} else if (betweenAngle.getAngle() <= Math.PI + Math.PI / 2) {
 				box2x1 = Math.abs(box2[0].getX()); // Left x value
 				box2x2 = Math.abs(box2[2].getX()); // Right x value
 				box2y1 = Math.abs(box2[3].getY()); // Bottom y value
 				box2y2 = Math.abs(box2[1].getY()); // Top y value
 			} else {
 				box2x1 = Math.abs(box2[3].getX()); // Left x value
 				box2x2 = Math.abs(box2[1].getX()); // Right x value
 				box2y1 = Math.abs(box2[0].getY()); // Bottom y value
 				box2y2 = Math.abs(box2[2].getY()); // Top y value
 			}
 			double offset = Math.cos(betweenAngle.getAngle()) * offsets.getX()
 					+ Math.sin(betweenAngle.getAngle()) * offsets.getY();
 			box2x1 -= offset;
 			box2x2 -= offset;
 			box2y1 -= offset;
 			box2y2 -= offset;
 			if ((box1z1 <= box2z1 && box2z1 <= box1z2)
 					|| (box1z1 <= box2z2 && box2z2 <= box1z2)) {
 				if ((box1x2 <= box2x1 && box2x1 <= box1x1)
 						|| (box1x2 <= box2x2 && box2x2 <= box1x1)) {
 					if ((box1y1 <= box2y1 && box2y1 <= box1y2)
 							|| (box1y1 <= box2y2 && box2y2 <= box1y2)) {
 						if (betweenAngle.getAngle() % ((float) Math.PI / 2) == 0) {
 							collide = true;
 						} else {
 							float slope1 = getSlope(box1[0], box1[3]);
 							float slope2 = getSlope(box1[1], box1[0]);
 							for (int i = 0; i < 3; i++) {
 								if (checkSlope(slope1, box2[i], box1[0],
 										box1[2])) {
 									collide = true;
 									break;
 								}
 								if (checkSlope(slope2, box2[i], box1[1],
 										box1[3])) {
 									collide = true;
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return collide;
 	}
 }
