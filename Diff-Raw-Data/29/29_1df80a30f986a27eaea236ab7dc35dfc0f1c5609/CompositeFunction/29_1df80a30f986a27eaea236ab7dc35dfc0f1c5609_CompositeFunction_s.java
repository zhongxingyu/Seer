 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.analysis.functions;
 
 import gda.analysis.DataSet;
 import gda.analysis.TerminalPrinter;
 
 import java.io.Serializable;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 
 
 /**
  * Class which contains all the information about a particular function which is made up out of several other
  * functions
  * @deprecated Use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction}
  */
 @Deprecated
 public class CompositeFunction extends uk.ac.diamond.scisoft.analysis.fitting.functions.CompositeFunction implements IFunction, Serializable {
 
 	/**
 	 * This constructor is simply to start an empty composite function.
 	 */
 	public CompositeFunction() {
 		super();
 	}
 
 	@Override
 	public DataSet makeDataSet(DoubleDataset... values) {
 
 		// the parallel functionality will stay in here, but is not being used
 		// as the threading overheads in java slow the performance
 		// significantly.
 		// return makeParallelDataSet(XValues);
 		return DataSet.convertToDataSet(makeSerialDataset(values));
 	}
 
 	class PFunction extends AFunction {
 		uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction function;
 
 		public PFunction(uk.ac.diamond.scisoft.analysis.fitting.functions.IFunction fn) {
			super(fn.getParameterValues());
 			function = fn;
			setName(function.getName());
 		}
 
 		@Override
 		public DoubleDataset makeDataset(IDataset... values) {
 			return (DoubleDataset) DatasetUtils.cast(function.makeDataset(values), AbstractDataset.FLOAT64);
 		}
 
 		@Override
 		public double partialDeriv(int parameter, double... position) {
 			return function.partialDeriv(parameter, position);
 		}
 
 		@Override
 		public double residual(boolean allValues, IDataset data, IDataset... values) {
 			return function.residual(allValues, data, values);
 		}
 
 		@Override
 		public double val(double... values) {
 			return function.val(values);
 		}
 
 		@Override
 		public void setParameterValues(double... params) {
			super.setParameterValues(params);
 			function.setParameterValues(params);
 		}
 
 		@Override
 		public String toString() {
 			return function.toString();
 		}
 
 		@Override
 		public void disp() {
 			TerminalPrinter.print(function.toString());
 		}
 
 		@Override
 		public DataSet makeDataSet(DoubleDataset... values) {
 			return DataSet.convertToDataSet(function.makeDataset(values));
 		}
 

 		@Override
 		public IFunction getFunction(int index) {
 			return this;
 		}
 	}
 
 	@Override
 	public AFunction getFunction(int index) {
 		return new PFunction(super.getFunction(index));
 	}
 
 	@Override
 	public void disp() {
 		TerminalPrinter.print(toString());
 	}
 
 	@Override
 	public String getParameterName(int index) {
 		return getParameter(index).getName();
 	}
 
 	@Override
 	public void setParameterName(String name, int index) {
 		getParameter(index).setName(name);
 	}
 }
