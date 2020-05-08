 /*-
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
 
 package uk.ac.diamond.scisoft.analysis.fitting.functions;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 
 
 /**
  * Class which contains all the information about a particular function which is made up out of several other
  * functions
  */
 public class CompositeFunction extends Add {
 
 	/**
 	 * This constructor is simply to start an empty composite function.
 	 */
 	public CompositeFunction() {
 		super();
 	}
 
 	/**
 	 * Create a set of datasets each containing the composite function and its constituent
 	 * parts evaluated over the values
 	 * 
 	 * @param values
 	 *            datasets containing all the values to evaluate the function at
 	 * @return an array of datasets
 	 */
 	public DoubleDataset[] display(DoubleDataset... values) {
 		if (values == null || values.length == 0) {
 			
 		}
 		int noOfFunctions = getNoOfFunctions();
 		
 		DoubleDataset[] outputs = new DoubleDataset[noOfFunctions + 1];
 
 		outputs[0] = calculateValues(values);
 		outputs[0].setName("Composite function");
 
 		// now add the data for each bit in turn
 		int j = 1;
 		for (IFunction f : functions) {
 			outputs[j] = (DoubleDataset) DatasetUtils.cast(f.calculateValues(values), AbstractDataset.FLOAT64);
 			outputs[j++].setName(f.getName());
 		}
 
 		return outputs;
 	}
 
 	/**
 	 * Create a set of datasets each containing the composite function and its constituent
 	 * parts evaluated over the values
 	 * 
 	 * @param XValues
 	 *            A dataset containing all the X values to calculate the data at
 	 * @param DataValues
 	 *            The data that is being fitted too, for visual help.
 	 * @return an array of datasets 
 	 */
 	public DoubleDataset[] display(DoubleDataset XValues, DoubleDataset DataValues) {
 		int noOfFunctions = getNoOfFunctions();
 
 		DoubleDataset[] outputs = new DoubleDataset[noOfFunctions + 4];
 
 		outputs[0] = new DoubleDataset(DataValues);
 
 		// now add the data
 		outputs[1] = calculateValues(XValues);
 		outputs[1].setName("Composite function");
 
 		// now add the errors to the graph, this should provide a good view to
 		// how good the fit is quite nicely.
 		outputs[2] = (DoubleDataset) Maths.subtract(outputs[1], DataValues);
 		outputs[2].setName("Error Value");
 		double offset = DataValues.min().doubleValue() - ((DataValues.max().doubleValue() - DataValues.min().doubleValue()) / 5.0);
 		outputs[2].isubtract(offset);
 
 		outputs[3] = new DoubleDataset(XValues.getShapeRef());
 		outputs[3].setName("Error Offset");
 		outputs[3].fill(offset);
 
 		// now add the data for each bit in turn
 		int j = 4;
 		for (IFunction f : functions) {
 			outputs[j] = (DoubleDataset) DatasetUtils.cast(f.calculateValues(XValues), AbstractDataset.FLOAT64);
 			outputs[j++].setName(f.getName());
 		}
 
 		return outputs;
 
 	}
 
 	/**
 	 * Attempts to cast and return the function at i as a Peak
 	 * @param i
 	 * @return IPeak
 	 * @throws ClassCastException
 	 */
 	public IPeak getPeak(int i) {
 		return (IPeak) getFunction(i);
 	}
 
 	@Override
 	public CompositeFunction copy() throws Exception {
		CompositeFunction copy = new CompositeFunction();
		for (IFunction f : functions) {
			copy.addFunction(f.copy());
		}
		return copy;
 	}
 }
