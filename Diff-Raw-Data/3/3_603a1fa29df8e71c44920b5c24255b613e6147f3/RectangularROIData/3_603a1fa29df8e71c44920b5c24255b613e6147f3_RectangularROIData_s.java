 /*
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
 
 package org.dawb.common.ui.plot.roi.data;
 
 import uk.ac.diamond.scisoft.analysis.axis.AxisValues;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractCompoundDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;
 
 /**
  * Class to aggregate information associated with a ROI
  */
 public class RectangularROIData extends ROIData {
 	/**
 	 * Construct new object from given roi and data
 	 * @param rroi
 	 * @param data
 	 */
 	public RectangularROIData(RectangularROI rroi, AbstractDataset data) {
 		this(rroi, data, null, 1.);
 	}
 
 	public RectangularROIData(RectangularROI rroi, AbstractDataset data, AbstractDataset mask) {
 		this(rroi, data, mask, 1.);
 	}
 	
 	/**
 	 * Construct new object from given roi and data
 	 * 
 	 * @param rroi
 	 * @param data
 	 * @param subFactor
 	 */
 	public RectangularROIData(RectangularROI rroi, AbstractDataset data, AbstractDataset mask, double subFactor) {
 		super();
 
 		setROI(rroi.copy());
 		roi.downsample(subFactor);
		profileData = ROIProfile.box(data, mask, (RectangularROI) roi);
 		if (profileData != null && profileData[0].getShape()[0] > 1 && profileData[1].getShape()[0] > 1) {
 			AbstractDataset pdata;
 			for (int i = 0; i < 2; i++) {
 				pdata = profileData[i];
 				if (pdata instanceof AbstractCompoundDataset) // use first element
 					profileData[i] = ((AbstractCompoundDataset) pdata).getElements(0);
 			}
 			Number sum = (Number) profileData[0].sum();
 			profileSum = sum.doubleValue();
 
 			xAxes = new AxisValues[] { null, null };
 			xAxes[0] = new AxisValues();
 			xAxes[1] = new AxisValues();
 
 			AbstractDataset axis;
 			axis = profileData[0].getIndices().squeeze();
 			axis.imultiply(subFactor);
 			xAxes[0].setValues(axis);
 			axis = profileData[1].getIndices().squeeze();
 			axis.imultiply(subFactor);
 			xAxes[1].setValues(axis);
 		} else {
 			setPlot(false);
 		}
 	}
 
 	/**
 	 * Aggregate a copy of ROI data to this object
 	 * @param roi
 	 * @param profileData
 	 * @param axes
 	 * @param profileSum
 	 */
 	public RectangularROIData(RectangularROI roi, AbstractDataset[] profileData, AxisValues[] axes, double profileSum) {
 		super();
 		setROI(roi.copy());
 		this.profileData = profileData.clone();
 		for (int i = 0; i < profileData.length; i++) {
 			this.profileData[i] = profileData[i].clone();
 		}
 		xAxes = axes.clone();
 		for (int i = 0; i < axes.length; i++) {
 			xAxes[i] = axes[i].clone();
 		}
 		this.profileSum = profileSum;
 		setPlot(false);
 	}
 
 	/**
 	 * @return linear region of interest
 	 */
 	@Override
 	public RectangularROI getROI() {
 		return (RectangularROI) roi;
 	}
 }
