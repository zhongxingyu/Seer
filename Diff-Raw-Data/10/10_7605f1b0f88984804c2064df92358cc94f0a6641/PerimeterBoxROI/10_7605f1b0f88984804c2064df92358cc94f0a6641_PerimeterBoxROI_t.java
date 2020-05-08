 /*-
  * Copyright 2013 Diamond Light Source Ltd.
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
  * Class for rectangular region of interest : perimeter type (with coloured edges)
  */
 public class PerimeterBoxROI extends RectangularROI implements Serializable {
 
 	/**
 	 * Default square of 10 pixels
 	 */
 	public PerimeterBoxROI() {
 		super(10, 0.0);
 	}
 
 	/**
 	 * Square constructor
 	 * 
 	 * @param width
 	 * @param angle
 	 */
 	public PerimeterBoxROI(double width, double angle) {
 		super(width, angle);
 	}
 
 	/**
 	 * @param width
 	 * @param height
 	 * @param angle
 	 */
 	public PerimeterBoxROI(double width, double height, double angle) {
 		super(width, height, angle);
 	}
 
 	/**
 	 * @param ptx
 	 * @param pty
 	 * @param width
 	 * @param height
 	 * @param angle
 	 */
 	public PerimeterBoxROI(double ptx, double pty, double width, double height, double angle) {
 		super(ptx, pty, width, height, angle);
 	}
 
 	/**
 	 * @param ptx
 	 * @param pty
 	 * @param width
 	 * @param height
 	 * @param angle
 	 * @param clip 
 	 */
 	public PerimeterBoxROI(double ptx, double pty, double width, double height, double angle, boolean clip) {
 		super(ptx, pty, width, height, angle, clip);
 	}
 
 	/**
 	 * @return a copy
 	 */
 	@Override
 	public PerimeterBoxROI copy() {
		PerimeterBoxROI c = new PerimeterBoxROI(spt[0], spt[1], len[0], len[1], getAngle(), isClippingCompensation());
 		c.name = name;
 		c.plot = plot;
 		return c;
 	}
 }
