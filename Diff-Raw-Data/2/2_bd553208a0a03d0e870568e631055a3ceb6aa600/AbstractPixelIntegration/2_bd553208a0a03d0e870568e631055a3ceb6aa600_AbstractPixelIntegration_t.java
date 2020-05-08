 /*-
  * Copyright 2014 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.dataset.function;
 
 import java.util.List;
 
 import javax.vecmath.Vector3d;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
 import uk.ac.diamond.scisoft.analysis.diffraction.QSpace;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
 import uk.ac.diamond.scisoft.analysis.roi.ROIProfile.XAxis;
 
 public abstract class AbstractPixelIntegration implements DatasetToDatasetFunction{
 	int nbins;
 	Double min = null;
 	Double max = null;
 	DoubleDataset bins = null;
 	AbstractDataset axisArray;
 	QSpace qSpace = null;
 	ROIProfile.XAxis xAxis = XAxis.Q;
 	
 	public AbstractPixelIntegration(QSpace qSpace, int numBins) {
 		this.qSpace = qSpace;
 		this.nbins = numBins;
 	}
 	
 	public AbstractPixelIntegration(QSpace qSpace, int numBins, double lower, double upper)
 	{
 		this(qSpace, numBins);
 		min = lower;
 		max = upper;
 		if (min > max) {
 			throw new IllegalArgumentException("Given lower bound was higher than upper bound");
 		}
 
 		bins = (DoubleDataset) DatasetUtils.linSpace(min, max, nbins + 1, AbstractDataset.FLOAT64);
 	}
 
 	@Override
 	public abstract List<AbstractDataset> value(IDataset... datasets);
 	
 	/**
 	 * Set minimum and maximum edges of histogram bins
 	 * @param min
 	 * @param max
 	 */
 	public void setMinMax(double min, double max) {
 		this.min = min;
 		this.max = max;
 		bins = (DoubleDataset) DatasetUtils.linSpace(min, max, nbins + 1, AbstractDataset.FLOAT64);		
 	}
 	
 	public void setAxisType(ROIProfile.XAxis axis) {
 		this.xAxis = axis;
 	}
 	
 	protected void generateAxisArray(int[] shape, boolean centre) {
 		
 		if (qSpace == null) return;
 
 		axisArray = AbstractDataset.zeros(shape, AbstractDataset.FLOAT64);
 
 		PositionIterator iter = axisArray.getPositionIterator();
 		int[] pos = iter.getPos();
 
 		while (iter.hasNext()) {
 			
 			Vector3d q;
 			double value = 0;
 			
 			if (centre) q = qSpace.qFromPixelPosition(pos[1]+0.5, pos[0]+0.5);
 			else q = qSpace.qFromPixelPosition(pos[1], pos[0]);
 			
 			if (xAxis == XAxis.ANGLE) {
         		value = Math.toDegrees(qSpace.scatteringAngle(q));
 			} else {
 				value = q.length();
 			}
 			
 			axisArray.set(value, pos);
 		}
 	}
 	
 	protected void processAndAddToResult(AbstractDataset intensity, AbstractDataset histo, List<AbstractDataset> result, String name) {
 		AbstractDataset axis = Maths.add(bins.getSlice(new int[]{1}, null ,null), bins.getSlice(null, new int[]{-1},null));
 		axis.idivide(2);
 		
 		if (xAxis == XAxis.Q) axis.setName("q");
 		else axis.setName("2 Theta");
 		
 		result.add(axis);
		AbstractDataset out = Maths.dividez(intensity, DatasetUtils.cast(histo,AbstractDataset.FLOAT64));
 		out.setName(name + "_integrated");
 		result.add(out);
 	}
 }
