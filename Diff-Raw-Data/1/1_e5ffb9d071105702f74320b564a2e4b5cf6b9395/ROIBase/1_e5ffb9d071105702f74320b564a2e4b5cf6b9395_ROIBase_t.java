 /*
  * Copyright 2011 Diamond Light Source Ltd.
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
 
 /**
  * Base class for general region of interest
  */
 public class ROIBase implements Serializable {
 	protected double spt[]; // start or centre coordinates
 	protected boolean plot;
 
 	/**
 	 * @param point The start (or centre) point to set
 	 */
 	public void setPoint(double[] point) {
 		spt = point;
 	}
 
 	/**
 	 * @param point The start (or centre) point to set
 	 */
 	public void setPoint(int[] point) {
 		spt[0] = point[0];
 		spt[1] = point[1];
 	}
 
 	/**
 	 * @param x
 	 * @param y
 	 */
 	public void setPoint(int x, int y) {
 		spt[0] = x;
 		spt[1] = y;
 	}
 
 	/**
 	 * @param x 
 	 * @param y 
 	 */
 	public void setPoint(double x, double y) {
		if (spt==null) spt = new double[2];
 		spt[0] = x;
 		spt[1] = y;
 	}
 
 	/**
 	 * @return Returns the start (or centre) point
 	 */
 	public double[] getPoint() {
 		return spt;
 	}
 
 	/**
 	 * @return Returns the start (or centre) point's x value
 	 */
 	public double getPointX() {
 		return spt[0];
 	}
 
 	/**
 	 * @return Returns the start (or centre) point's y value
 	 */
 	public double getPointY() {
 		return spt[1];
 	}
 
 	/**
 	 * @return Returns the start (or centre) point
 	 */
 	public int[] getIntPoint() {
 		return new int[] { (int) spt[0], (int) spt[1] };
 	}
 
 	/**
 	 * Add an offset to start (or centre) point
 	 * 
 	 * @param pt
 	 */
 	public void addPoint(int[] pt) {
 		spt[0] += pt[0];
 		spt[1] += pt[1];
 	}
 
 	/**
 	 * @return a copy
 	 */
 	public ROIBase copy() {
 		ROIBase c = new ROIBase();
 		c.spt = spt.clone();
 		c.plot = plot;
 		return c;
 	}
 
 	/**
 	 * To account for a down-sampling of the dataset, change ROI
 	 * @param subFactor
 	 */
 	public void downsample(double subFactor) {
 		spt[0] /= subFactor;
 		spt[1] /= subFactor;
 	}
 
 	/**
 	 * @param require set true if plot required 
 	 */
 	public void setPlot(boolean require) {
 		plot = require;
 	}
 
 	/**
 	 * @return true if plot is enabled
 	 */
 	public boolean isPlot() {
 		return plot;
 	}
 }
