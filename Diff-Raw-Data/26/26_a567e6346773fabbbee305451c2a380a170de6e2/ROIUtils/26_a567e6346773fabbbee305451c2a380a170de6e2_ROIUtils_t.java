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
 	 * @throws UnsupportedOperationException when there is no list class
 	 */
 	public static ROIList<?> createNewROIList(Class<? extends IROI> clazz) {
 		if (clazz == null)
 			return null;
 
 		// be aware that order of tests is important as must be determined by class hierarchy
		if (PolygonalROI.class.isAssignableFrom(clazz))
 			return new PolygonalROIList();
 		// to-do add free draw
		else if (PolylineROI.class.isAssignableFrom(clazz))
 			return new PolylineROIList();
		else if (PointROI.class.isAssignableFrom(clazz))
 			return new PointROIList();
 		// to-do add line 3D
		else if (LinearROI.class.isAssignableFrom(clazz))
 			return new LinearROIList();
 		// to-do add grid, perimeter, x-axis box, x-axis line box, etc
		else if (RectangularROI.class.isAssignableFrom(clazz))
 			return new RectangularROIList();
 		// to-do add ring
		else if (SectorROI.class.isAssignableFrom(clazz))
 			return new SectorROIList();
 		// to-do add circular fit
		else if (CircularROI.class.isAssignableFrom(clazz))
 			return new CircularROIList();
		else if (EllipticalFitROI.class.isAssignableFrom(clazz))
 			return new EllipticalFitROIList();
		else if (EllipticalROI.class.isAssignableFrom(clazz))
 			return new EllipticalROIList();
 		// to-do add surface plot
 
 		throw new UnsupportedOperationException("No corresponding ROI list class");
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
