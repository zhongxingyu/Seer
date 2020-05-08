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
 
 public class ROIUtils {
 
 	/**
 	 * @param clazz
 	 * @return new list to hold ROIs of same class
 	 */
 	public static ROIList<?> createNewROIList(Class<? extends IROI> clazz) {
 		if (clazz == null)
 			return null;
 
 		// be aware that order of tests is important as must be determined by class hierarchy
		if (clazz.equals(PolygonalROI.class))
 			return new PolygonalROIList();
 		// to-do add free draw
		if (clazz.equals(PolylineROI.class))
 			return new PolylineROIList();
		else if (clazz.equals(PointROI.class))
 			return new PointROIList();
 		// to-do add line 3D
		else if (clazz.equals(LinearROI.class))
 			return new LinearROIList();
 		// to-do add grid, perimeter, x-axis box, x-axis line box, etc
		else if (clazz.equals(RectangularROI.class))
 			return new RectangularROIList();
 		// to-do add ring
		else if (clazz.equals(SectorROI.class))
 			return new SectorROIList();
 		// to-do add circular fit
		else if (clazz.equals(CircularROI.class))
 			return new CircularROIList();
		else if (clazz.equals(EllipticalFitROI.class))
 			return new EllipticalFitROIList();
		else if (clazz.equals(EllipticalROI.class))
 			return new EllipticalROIList();
 		// to-do add surface plot
 
		return null;
 	}
 
 	/**
 	 * @param roi
 	 * @return new list to hold ROIs of same type
 	 */
 	public static ROIList<?> createNewROIList(IROI roi) {
 		if (roi == null)
 			return null;
 		return createNewROIList(roi.getClass());
 	}
 }
