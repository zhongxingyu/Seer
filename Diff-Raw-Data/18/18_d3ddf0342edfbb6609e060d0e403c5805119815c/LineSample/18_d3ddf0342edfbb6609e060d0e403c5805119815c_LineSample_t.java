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
 
 package uk.ac.diamond.scisoft.analysis.dataset.function;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 
 /**
  * Sample along line and return list of one 1D dataset
  *
  */
 public class LineSample implements DatasetToDatasetFunction {
 	int sx, sy, ex, ey;
 	double step;
 	private double rad;
 	private double sp;
 	private double cp;
 
 	/**
 	 * Set up line sampling
 	 * 
 	 * @param sx
 	 * @param sy
 	 * @param ex
 	 * @param ey
 	 * @param step 
 	 */
 	public LineSample(int sx, int sy, int ex, int ey, double step) {
 		this.sx = sx;
 		this.sy = sy;
 		this.ex = ex;
 		this.ey = ey;
 		this.step = step;
 		rad = Math.hypot(ex - sx, ey - sy);
 		double phi = Math.atan2(ey - sy, ex - sx);
 		cp = Math.cos(phi);
 		sp = Math.sin(phi);
 	}
 
 	/**
 	 * @param i
 	 * @return position of indexed point on line
 	 */
 	public int[] getPoint(int i) {
 		final double r = step * i;
 		return new int[] {(int) (sy + r * sp), (int) (sx + r * cp)};
 	}
 
 	/**
 	 * The class implements pointwise sampling along a given line
 	 * 
 	 * @param datasets input 2D datasets
 	 * @return one 1D dataset
 	 */
 	@Override
 	public List<AbstractDataset> value(IDataset... datasets) {
 		if (datasets.length == 0)
 			return null;
 
 		List<AbstractDataset> result = new ArrayList<AbstractDataset>();
 
 		for (IDataset ids : datasets) {
			if (ids == null || ids.getRank() != 2)
 				return null;
 
 			int nr = ((int) Math.floor(rad / step)) + 1;
 
 			AbstractDataset linsample = AbstractDataset.zeros(new int[] { nr },
 					AbstractDataset.getBestFloatDType(ids.elementClass()));
 
 			double x, y;
 			for (int i = 0; i < nr; i++) {
 				final double r = step * i;
 				x = sx + r * cp;
 				y = sy + r * sp;
 				linsample.setObjectAbs(i, Maths.getBilinear(ids, y, x));
 			}
 
 			result.add(linsample);
 		}
 		return result;
 	}
 }
