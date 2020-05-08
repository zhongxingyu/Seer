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
 
 import java.io.Serializable;
 
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 
 /**
  * @deprecated use {@link uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction}
  */
 @Deprecated
 public abstract class AFunction extends uk.ac.diamond.scisoft.analysis.fitting.functions.AFunction implements IFunction, Serializable {
 
 	public AFunction(double[] values) {
 		super(values);
 	}
 
 	@Override
 	public IFunction getFunction(int index) {
 		return this;
 	}
 
 	/**
 	 * Added by mark to get round problems in Jython
 	 * @param value
 	 * @return A Dataset!
 	 */
 	public DataSet makeDataSet(DoubleDataset value) {
 		return DataSet.convertToDataSet(super.makeDataset(value));
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
