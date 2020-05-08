 /*-
  * Copyright 2012 Diamond Light Source Ltd.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package uk.ac.diamond.scisoft.analysis.roi;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Class for a polygonal ROI (really a list of point ROIs)
  */
 public class PolygonalROI extends PointROI implements Serializable, Iterable<PointROI> {
 	private List<PointROI> pts;
 
 	public PolygonalROI() {
 		super();
 		pts = new ArrayList<PointROI>();
 	}
 
 	public PolygonalROI(double[] start) {
 		super(start);
 		pts = new ArrayList<PointROI>();
 		pts.add(this);
 	}
 
 	@Override
 	public void setPoint(double[] point) {
 		super.setPoint(point);
		if (pts.size() == 0) {
 			pts.add(this);
 		}
 	}
 
 	/**
 	 * Set point of polygon at index
 	 * @param i index
 	 * @param point
 	 */
 	public void setPoint(int i, double[] point) {
 		if (i == 0)
 			setPoint(point);
 
 		pts.set(i, new PointROI(point));
 	}
 
 	/**
 	 * Add point to polygon
 	 * @param point
 	 */
 	public void insertPoint(double[] point) {
 		pts.add(new PointROI(point));
 	}
 
 	/**
 	 * Add point to polygon
 	 * @param point
 	 */
 	public void insertPoint(int[] point) {
 		insertPoint(new double[] { point[0], point[1] });
 	}
 
 	/**
 	 * Add point to polygon
 	 * @param x
 	 * @param y
 	 */
 	public void insertPoint(double x, double y) {
 		insertPoint(new double[] {x, y});
 	}
 
 	/**
 	 * Insert point to polygon at index
 	 * @param i index
 	 * @param point
 	 */
 	public void insertPoint(int i, double[] point) {
 		if (i == 0) {
 			if (pts.size() == 0) {
 				setPoint(point);
 			} else { // copy current and then shift
 				pts.set(0, new PointROI(spt));
 				spt = point;
 				pts.add(0, this);
 			}
 		} else {
 			pts.add(i, new PointROI(point));
 		}
 	}
 
 	/**
 	 * Insert point to polygon at index
 	 * @param i index
 	 * @param x
 	 * @param y
 	 */
 	public void insertPoint(int i, double x, double y) {
 		insertPoint(i, new double[] {x, y});
 	}
 
 	/**
 	 * @return number of sides
 	 */
 	public int getSides() {
 		return pts.size();
 	}
 
 	/**
 	 * @param i
 	 * @return x value of i-th point
 	 */
 	public double getPointX(int i) {
 		return pts.get(i).spt[0];
 	}
 
 	/**
 	 * @param i
 	 * @return y value of i-th point
 	 */
 	public double getPointY(int i) {
 		return pts.get(i).spt[1];
 	}
 
 	/**
 	 * @param i
 	 * @return i-th point as point ROI
 	 */
 	public PointROI getPoint(int i) {
 		return pts.get(i);
 	}
 
 	/**
 	 * @return iterator over points
 	 */
 	@Override
 	public Iterator<PointROI> iterator() {
 		return pts.iterator();
 	}
 
 	@Override
 	public PolygonalROI copy() {
 		PolygonalROI croi = new PolygonalROI(spt.clone());
 		for (int i = 1, imax = pts.size(); i < imax; i++)
 			croi.insertPoint(pts.get(i).spt.clone());
 
 		return croi;
 	}
 
 	@Override
 	public String toString() {
 		/**
 		 * You cannot have pts.toString() if the pts contains this! It
 		 * is a recursive call. Fix to defect https://trac/scientific_software/ticket/1943
 		 */
 		if (pts.contains(this)) return super.toString();
 		return pts.toString();
 	}
 }
