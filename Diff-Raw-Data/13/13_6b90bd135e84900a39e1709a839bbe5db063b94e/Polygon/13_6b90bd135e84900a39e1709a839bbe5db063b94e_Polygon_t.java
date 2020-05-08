 /*
  *  Flingbox - An OpenSource physics sandbox for Google's Android
  *  Copyright (C) 2009  Jon Ander Peñalba & Endika Gutiérrez
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.eside.flingbox.objects;
 
 import java.util.Random;
 
 import edu.eside.flingbox.graphics.RenderPolygon;
 import edu.eside.flingbox.math.PolygonUtils;
 import edu.eside.flingbox.math.Vector2D;
 import edu.eside.flingbox.physics.PhysicPolygon;
 import edu.eside.flingbox.physics.PhysicBody.OnMovementListener;
 
 /**
  * Polygon is a general class with handles Physics and render from 
  * a Polygonal Body
  *
  */
 public final class Polygon extends Body implements OnMovementListener {
 	private final Vector2D[] mPoints;
 	private final short mPointsCount;
 
 	/**
 	 * Default Constructor for a Polygon
 	 * @param points	Array of Polygon's point
 	 * @throws IllegalArgumentException		If not enough points
 	 */
 	public Polygon(final Vector2D[] polygonPoints) throws IllegalArgumentException {
 		super(null, null);
 		
 		// Get passed points count
 		final int pointsCount = polygonPoints.length;
 		
 		// If not points enough to build a polygon.
 		if (pointsCount < 3)
 			throw new IllegalArgumentException("Not points enough to build a polygon.");
 	
 		
 		float polygonArea = PolygonUtils.polygonArea(polygonPoints);
 		// Set points in Clock-wise order
		if (polygonArea > 0) {
 			/* If points are in anti-Clock-wise order
 			 * returned arre will be positive, else, it 
 			 * will be negative.
 			 */
			Vector2D temp;
			for (int i = 0, j = polygonPoints.length - 1; i<j; --j, ++i) {
				temp = polygonPoints[i];
 				polygonPoints[i] = polygonPoints[j];
 				polygonPoints[j] = temp;
 			}	
		} else {
 			polygonArea = -polygonArea;
		}
 		
 		
 		short[] triangulationIndexes = PolygonUtils.triangulatePolygon(polygonPoints);
 		Vector2D centroid = PolygonUtils.polygonCentroid(polygonPoints);
 		
 		// Relocate polygon to fin centroid with point (0, 0)
 		for (Vector2D p : polygonPoints) {
 			p.i -= centroid.i;
 			p.j -= centroid.j;
 		}
 		
 		mPoints = polygonPoints;
 		mPointsCount = (short) (polygonPoints.length);
 		
 		mRender = new RenderPolygon(polygonPoints, triangulationIndexes);
 		mPhysics = new PhysicPolygon(polygonPoints, polygonArea, centroid, this);
 	}
 	
 	/**
 	 * @return Polygon points
 	 * NOTE: THIS COULD NOT MATCH WITH points IN CONSTRUCTOR!!
 	 */
 	public Vector2D[] getPoints() {
 		return mPoints;
 	}
 
 	/**
 	 * @return Polygons total points. 
 	 * NOTE: THIS COULD NOT MATCH WITH points IN CONSTRUCTOR!!
 	 */
 	public int getPointsCount() {
 		return mPointsCount;
 	}
 	
 	/**
 	 * Sets random color to polygon
 	 */
 	public void setRandomColor() {
 		Random rnd = new Random();
 		((RenderPolygon) mRender).setColor(rnd.nextFloat() ,rnd.nextFloat() ,rnd.nextFloat() , 1.0f);
 	}
 
 	/**
 	 * Called when movement occurs.
 	 */
 	public void onMovement(Vector2D position, float rotation) {
 		if (mRender != null && mRender instanceof RenderPolygon)
 			((RenderPolygon) mRender).setPosition(
 					new Vector2D(position.i, position.j), rotation);
 	}
 
 }
