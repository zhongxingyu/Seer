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
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.PositionIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.Stats;
 
 public class Median implements DatasetToDatasetFunction {
 	
 	private final int window;
 
 	/**
 	 * This class applies median filter to the input datasets
 	 * 
 	 * @param window 
 	 */
 	public Median(int window) {
 		if (window <= 0) {
 			throw new IllegalArgumentException("Non-positive window parameter not allowed");
 		}
 		
 		this.window = window / 2;
 	}
 	
 	@Override
 	public List<AbstractDataset> value(IDataset... datasets) {
 		
 		if (datasets.length == 0)
 			return null;
 
 		List<AbstractDataset> result = new ArrayList<AbstractDataset>();
 		
 		for (IDataset idataset : datasets) {
 			AbstractDataset dataset = DatasetUtils.convertToAbstractDataset(idataset);
 			final int dt = dataset.getDtype();
 			final int is = dataset.getElementsPerItem();
 			final int[] ishape = dataset.getShape();
 			
 			if (ishape.length > 1)
 				throw new IllegalArgumentException("Only 1D input datasets are supported");
 			
			AbstractDataset filterFunction = AbstractDataset.zeros(is , ishape, dt);
 			
			final PositionIterator iterPos = filterFunction.getPositionIterator();
 			while (iterPos.hasNext()) {
				int idx = iterPos.getPos()[0];
				int idxStart = Math.max(idx - this.window, 0);
				int idxStop = Math.min(idx + this.window, dataset.getSize()-1);
				AbstractDataset windowSlice = dataset.getSlice(new int[] {idxStart}, new int[] {idxStop}, new int[] {1});
 				
				filterFunction.set(Stats.median(windowSlice), iterPos.getPos());
 			}
 			
			result.add(filterFunction);
 		}
 		return result;
 	}
 }
