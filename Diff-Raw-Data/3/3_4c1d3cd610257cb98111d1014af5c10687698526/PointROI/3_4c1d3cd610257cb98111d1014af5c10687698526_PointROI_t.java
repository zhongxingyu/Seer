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
 
 import java.util.Arrays;
 
 /**
  * Class for point region of interest
  */
 public class PointROI extends ROIBase {
 
 	public PointROI() {
 		spt = new double[2];
 	}
 
 	public PointROI(double[] point) {
 		setPoint(point);
 	}
 
 	public PointROI(double x, double y) {
		this();
 		setPoint(x, y);
 	}
 
 	public PointROI(int[] point) {
		this();
 		setPoint(point);
 	}
 
 	@Override
 	public String toString() {
 		return Arrays.toString(spt);
 	}
 }
