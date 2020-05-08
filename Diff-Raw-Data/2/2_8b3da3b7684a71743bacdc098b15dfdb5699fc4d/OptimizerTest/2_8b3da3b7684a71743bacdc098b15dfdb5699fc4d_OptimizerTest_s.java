 /*-
  * Copyright 2013 Diamond Light Source Ltd.
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
 
 package uk.ac.diamond.scisoft.analysis.fitting;
 
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.APeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Add;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Gaussian;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IOperator;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.IdentifiedPeak;
 import uk.ac.diamond.scisoft.analysis.fitting.functions.Offset;
 import uk.ac.diamond.scisoft.analysis.optimize.AbstractOptimizer;
 import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer;
 import uk.ac.diamond.scisoft.analysis.optimize.ApacheOptimizer.Optimizer;
 
 public class OptimizerTest  {
 
 	static final long SEED = 12357L;
 
 	public AbstractOptimizer createOptimizer(Optimizer o) {
 		ApacheOptimizer opt = new ApacheOptimizer(o);
 		opt.seed = SEED;
 		return opt;
 	}
 
 	@Test
 	public void testOptimizer() {
 		DoubleDataset gaussian = Generic1DDatasetCreator.createGaussianDataset();
 
 		List<IdentifiedPeak> peaks = Generic1DFitter.parseDataDerivative(Generic1DDatasetCreator.xAxis, gaussian, Generic1DDatasetCreator.smoothing);
 
 		IdentifiedPeak iniPeak = peaks.get(0);
 		int[] start = { iniPeak .getIndexOfDatasetAtMinPos() };
 		int[] stop = { iniPeak.getIndexOfDatasetAtMaxPos() + 1 };
 		int[] step = { 1 };
 		AbstractDataset y = gaussian.getSlice(start, stop, step);
 		AbstractDataset x = Generic1DDatasetCreator.xAxis.getSlice(start, stop, step);
 		double lowOffset = y.min().doubleValue();
 		double highOffset = (Double) y.mean();
 		Offset baseline = new Offset(lowOffset, highOffset);
 		APeak localPeak = new Gaussian(iniPeak);
 		IOperator comp = new Add();
 		comp.addFunction(localPeak);
 		comp.addFunction(baseline);
 
 		AbstractOptimizer opt = createOptimizer(Optimizer.SIMPLEX_MD);
 		try {
 			opt.optimize(new IDataset[] {x}, y, comp);
 		} catch (Exception e) {
 			System.err.println("Problem: " + e);
 		}
 
 		double[] parameters = opt.getParameterValues();
 		for (int ind = 0; ind < parameters.length; ind++) {
 			double v = parameters[ind];
 			double dv = v * 1e-5;
 			double od = evalDiff(parameters, ind, v, dv, opt);
 			double nd = 0;
 			System.err.printf("Difference is %g for %g\n", od, dv);
 			dv *= 0.25;
 
 			for (int i = 0; i < 20; i++) {
 				// System.err.println(Arrays.toString(parameters));
 				nd = evalDiff(parameters, ind, v, dv, opt);
 				System.err.printf("Difference is %g for %g\n", nd, dv);
 				if (Math.abs(nd - od) < 1e-15*Math.max(1, Math.abs(od))) {
 					break;
 				}
 				od = nd;
 				dv *= 0.25;
 			}
 
 			parameters[ind] = v;
 			double pd = opt.calculateResidualDerivative(opt.getParameters().get(ind), parameters);
 			System.err.println(nd + " cf " + pd);
			Assert.assertEquals(nd,  pd, 1e-3*Math.abs(pd));
 		}
 	}
 
 	private double evalDiff(double[] parameters, int ind, double v, double dv, AbstractOptimizer opt) {
 		parameters[ind] = v + dv;
 		opt.setParameterValues(parameters);
 		double r = opt.calculateResidual();
 
 		parameters[ind] = v - dv;
 		opt.setParameterValues(parameters);
 		r -= opt.calculateResidual();
 
 		return (r * 0.5) / dv;
 	}
 }
