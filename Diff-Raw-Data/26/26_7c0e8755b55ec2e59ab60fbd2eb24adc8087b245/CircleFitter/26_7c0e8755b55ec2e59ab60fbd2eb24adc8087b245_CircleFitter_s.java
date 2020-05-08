 /*
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
 
 import java.util.Arrays;
 
 import org.apache.commons.math.ConvergenceException;
 import org.apache.commons.math.FunctionEvaluationException;
 import org.apache.commons.math.analysis.MultivariateMatrixFunction;
 import org.apache.commons.math.optimization.VectorialPointValuePair;
 import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 import Jama.EigenvalueDecomposition;
 import Jama.Matrix;
 import Jama.SingularValueDecomposition;
 
 /**
  * This function returns the coordinates (interleaved) for the points specified by the
  * geometric parameters and an array of angles
  */
class CircleCoordinatesFunction implements IConicSectionFitFunction {
 	private static final int PARAMETERS = CircleFitter.PARAMETERS;
 	private AbstractDataset X;
 	private AbstractDataset Y;
 	private DoubleDataset v;
 	private double[][] j;
 	private int n; // number of points
 	private int m; // number of equations
 	private double[] ca;
 	private double[] sa;
 
 	public CircleCoordinatesFunction(AbstractDataset x, AbstractDataset y) {
 		setPoints(x, y);
 	}
 
 	@Override
 	public void setPoints(AbstractDataset x, AbstractDataset y) {
 		X = x;
 		Y = y;
 		n = X.getSize();
 		m = 2*n;
 		v = new DoubleDataset(m);
 		j = new double[m][PARAMETERS+n];
 		for (int i = 0; i < m; i++) {
 			j[i][1] = 1;
 			i++;
 			j[i][2] = 1;
 		}
 		ca = new double[n];
 		sa = new double[n];
 	}
 
 	@Override
 	public double[] getTarget() {
 		double[] target = new double[m];
 		final IndexIterator itx = X.getIterator();
 		final IndexIterator ity = Y.getIterator();
 		int i = 0;
 		while (itx.hasNext() && ity.hasNext()) {
 			target[i++] = X.getElementDoubleAbs(itx.index);
 			target[i++] = Y.getElementDoubleAbs(ity.index);
 		}
 		return target;
 	}
 
 	@Override
 	public double[] getWeight() {
 		double[] weight = new double[m];
 		Arrays.fill(weight, 1.0);
 		return weight;
 	}
 
 	/**
 	 * Calculate angles of closest points on circle to targets
 	 * @param initParameters geometric parameters
 	 * @return array of all initial parameters
 	 */
 	double[] calcAllInitValues(double[] initParameters) {
 		double[] init = new double[n+PARAMETERS];
 		for (int i = 0; i < initParameters.length; i++) {
 			init[i] = initParameters[i];
 		}
 		final double x = initParameters[1];
 		final double y = initParameters[2];
 
 		// work out the angle values for the closest points on circle
 		final IndexIterator itx = X.getIterator();
 		final IndexIterator ity = Y.getIterator();
 		int i = PARAMETERS;
 		while (itx.hasNext() && ity.hasNext()) {
 			final double Xc = X.getElementDoubleAbs(itx.index) - x;
 			final double Yc = Y.getElementDoubleAbs(ity.index) - y;
 
 			init[i++] = Math.atan2(Yc, Xc);
 		}
 		return init;
 	}
 
 	@Override
 	public double[] value(double[] p) throws FunctionEvaluationException, IllegalArgumentException {
 		final double[] values = v.getData();
 		final double r = p[0];
 		final double x = p[1];
 		final double y = p[2];
 
 		for (int i = 0; i < n; i++) {
 			final double t = p[i+PARAMETERS];
 			final double cost = Math.cos(t);
 			final double sint = Math.sin(t);
 			ca[i] = cost;
 			sa[i] = sint;
 			final int ti = 2*i;
 			values[ti] = x + r*cost;
 			values[ti+1] = y + r*sint;
 		}
 
 		return values;
 	}
 
 	@Override
 	public AbstractDataset calcDistanceSquared(double[] parameters) throws IllegalArgumentException {
 		final double[] p = calcAllInitValues(parameters);
 
 		final DoubleDataset v = new DoubleDataset(n);
 		final double[] values = v.getData();
 		final double r = p[0];
 		final double x = p[1];
 		final double y = p[2];
 
 		for (int i = 0; i < n; i++) {
 			final double t = p[i+PARAMETERS];
 			final double cost = Math.cos(t);
 			final double sint = Math.sin(t);
 			double px = x + r*cost - X.getElementDoubleAbs(i);
 			double py = y + r*sint - Y.getElementDoubleAbs(i);
 			values[i] = px*px + py*py;
 		}
 
 		return v;
 	}
 
 	private void calculateJacobian(double[] p) {
 		final double r = p[0];
 
 		for (int i = 0; i < n; i++) {
 			final double ct = ca[i];
 			final double st = sa[i];
 
 			final int ti = 2*i;
 			final int tj = ti + 1;
 			j[ti][0] = ct;
 			j[ti][PARAMETERS+i] = - r*st;
 
 			j[tj][0] = st;
 			j[tj][PARAMETERS+i] = r*ct;
 		}
 	}
 
 	@Override
 	public MultivariateMatrixFunction jacobian() {
 		return new MultivariateMatrixFunction() {
 			@Override
 			public double[][] value(double[] p) throws FunctionEvaluationException, IllegalArgumentException {
 				calculateJacobian(p);
 				return j;
 			}
 		};
 	}
 }
 
 /**
  * Fit a circle whose geometric parameters are
  *  radius, centre coordinates
  */
public class CircleFitter implements IConicSectionFitter {
 	/**
 	 * Setup the logging facilities
 	 */
 	private static transient final Logger logger = LoggerFactory.getLogger(CircleFitter.class);
 
 	private double[] parameters;
 
 	private CircleCoordinatesFunction fitFunction;
 
 	private double residual;
 
 	final static int PARAMETERS = 3;
 
 	public CircleFitter() {
 		parameters = new double[PARAMETERS];
 	}
 
 	@Override
 	public double[] getParameters() {
 		return parameters;
 	}
 
 	@Override
 	public IConicSectionFitFunction getFitFunction(AbstractDataset x, AbstractDataset y) {
 		if (fitFunction == null) {
 			if (x == null || y == null)
 				throw new IllegalArgumentException("Fitter uninitialized so coordinate datasets are needed");
 			fitFunction = new CircleCoordinatesFunction(x, y);
 		} else if (x != null && y != null) {
 			fitFunction.setPoints(x, y);
 		}
 		return fitFunction;
 	}
 
 	@Override
 	public double getRMS() {
 		return residual;
 	}
 
 	/**
 	 * Fit points given by x, y datasets to a circle. If no initial parameters are given, then
 	 * an algebraic fit is performed then a non-linear least squares fitting routine is used to
 	 * provide the best geometric fit.
 	 * @param x
 	 * @param y
 	 * @param init parameters (can be null)
 	 */
 	@Override
 	public void geometricFit(AbstractDataset x, AbstractDataset y, double[] init) {
 		residual = Double.NaN;
 		if (x.getSize() < PARAMETERS || y.getSize() < PARAMETERS) {
 			throw new IllegalArgumentException("Need " + PARAMETERS + " or more points");
 		}
 
 		if (init == null)
 			init = quickfit(x, y);
 		else if (init.length < PARAMETERS)
 			throw new IllegalArgumentException("Need " + PARAMETERS + " parameters");
 
 		CircleCoordinatesFunction f = (CircleCoordinatesFunction) getFitFunction(x, y);
 		if (x.getSize() == PARAMETERS) {
 			for (int i = 0; i < PARAMETERS; i++)
 				parameters[i] = init[i];
 			return;
 		}
 		LevenbergMarquardtOptimizer opt = new LevenbergMarquardtOptimizer();
 
 		try {
 			VectorialPointValuePair result = opt.optimize(f, f.getTarget(), f.getWeight(), f.calcAllInitValues(init));
 
 			double[] point = result.getPointRef(); 
 			for (int i = 0; i < PARAMETERS; i++)
 				parameters[i] = point[i];
 
 			residual = opt.getRMS();
 			logger.info("Circle fit: rms = {}, x^2 = {}", opt.getRMS(), opt.getChiSquare());
 		} catch (FunctionEvaluationException e) {
 			// cannot happen
 		} catch (IllegalArgumentException e) {
 			// should not happen!
 		} catch (ConvergenceException e) {
 			throw new IllegalArgumentException("Problem with optimizer converging");
 		}
 	}
 
 	/**
 	 * Fit points given by x, y datasets to a circle. 
 	 * @param x
 	 * @param y
 	 */
 	@Override
 	public void algebraicFit(AbstractDataset x, AbstractDataset y) {
 		residual = Double.NaN;
 		if (x.getSize() < PARAMETERS || y.getSize() < PARAMETERS) {
 			throw new IllegalArgumentException("Need " + PARAMETERS + " or more points");
 		}
 
 		double[] quick = quickfit(x, y);
 		IConicSectionFitFunction f = getFitFunction(x, y);
 		residual = Math.sqrt((Double) f.calcDistanceSquared(quick).mean());
 
 		for (int i = 0; i < PARAMETERS; i++)
 			parameters[i] = quick[i];
 	}
 
 	/**
 	 * least-squares using algebraic cost function
 	 * <p>
 	 * This uses the Pratt method as mentioned in "Error analysis for circle fitting algorithms"
 	 * by A. Al-Sharadqah and N. Chernov, Electronic Journal of Statistics, v3, pp886-991 (2009)
 	 * <p>
 	 * @param x
 	 * @param y
 	 * @return geometric parameters
 	 */
 	private static double[] quickfit(AbstractDataset x, AbstractDataset y) {
 		double mx = (Double) x.mean();
 		double my = (Double) y.mean();
 		x = Maths.subtract(x.cast(AbstractDataset.FLOAT64), mx);
 		y = Maths.subtract(y.cast(AbstractDataset.FLOAT64), my);
 		final DoubleDataset z = (DoubleDataset) Maths.square(x).iadd(Maths.square(y));
 		final DoubleDataset o = DoubleDataset.ones(x.getShape());
 
 		double ca, cd, ce, cf;
 		if (x.getSize() == PARAMETERS) { // exact case
 			double[][] mz = {(double[]) x.getBuffer(), (double[]) y.getBuffer(), o.getData()};
 			Matrix Z = new Matrix(mz);
 			Matrix V = new Matrix(z.getData(), 1);
 			try {
 				V = V.times(Z.inverse());
 			} catch (RuntimeException e) {
 				throw new IllegalArgumentException("Given points are collinear");
 			}
 			ca = 1;
 			cd = -V.get(0, 0);
 			ce = -V.get(0, 1);
 			cf = -V.get(0, 2);
 		} else {
 			double[][] mz = { z.getData(), (double[]) x.getBuffer(), (double[]) y.getBuffer(), o.getData() };
 			Matrix Z = new Matrix(mz);
 
 			SingularValueDecomposition svd = Z.transpose().svd();
 			Matrix S = svd.getS();
 			// System.err.println("S:");
 			// S.print(12, 6);
 			Matrix V = svd.getV();
 			// System.err.println("V:");
 			// V.print(12, 6);
 
 			if (S.get(3, 3) < S.get(0, 0) * 1e-12) {
 				ca = V.get(0, 3);
 				cd = V.get(1, 3);
 				ce = V.get(2, 3);
 				cf = V.get(3, 3);
 			} else {
 				Matrix W = V.times(S);
 				// System.err.println("W:");
 				// W.print(12, 6);
 				Matrix Cinv = new Matrix(new double[] { 0, 0, 0, -0.5, 0, 1, 0, 0, 0, 0, 1, 0, -0.5, 0, 0, 0 }, 4);
 				Matrix T = W.transpose().times(Cinv.times(W));
 				// System.err.println("T:");
 				// T.print(12, 6);
 				EigenvalueDecomposition decomp = T.eig();
 				double[] e = decomp.getRealEigenvalues();
 				// System.err.println("Eigenvalues: " + Arrays.toString(e));
 				// find minimal positive eigenvalue
 				double emin = Double.POSITIVE_INFINITY;
 				int j = 0;
 				for (int i = 0; i < 4; i++) {
 					double ei = e[i];
 					if (ei > 0 && ei < emin) {
 						emin = ei;
 						j = i;
 					}
 					S.set(i, i, 1. / S.get(i, i));
 				}
 				Matrix A = decomp.getV();
 				A = V.times(S).times(A.getMatrix(0, 3, j, j));
 				// A.print(12, 6);
 				ca = A.get(0, 0);
 				cd = A.get(1, 0);
 				ce = A.get(2, 0);
 				cf = A.get(3, 0);
 			}
 		}
 
 		final double disc = cd*cd + ce*ce - 4.*ca*cf;
 //		System.err.println(String.format("Algebraic: %g, %g, %g, %g (%g)", ca, cd, ce, cf, disc));
 		if (disc < 0) {
 			throw new IllegalArgumentException("No solution!");
 		}
 
 		double[] qparameters = new double[PARAMETERS];
 		double f = 0.5/ca;
 		qparameters[0] = Math.abs(f)*Math.sqrt(disc);
 		qparameters[1] = -f*cd + mx;
 		qparameters[2] = -f*ce + my;
 //		System.err.println(String.format("Algebraic: %g, %g, %g", qparameters[0], qparameters[1], qparameters[2]));
 		return qparameters;
 	}
 
 	/**
 	 * Compute coordinates from an array of angles
 	 * @param angles
 	 * @param geometricParameters
 	 * @return x and y datasets
 	 */
 	public static AbstractDataset[] generateCoordinates(AbstractDataset angles, final double[] geometricParameters) {
 		if (geometricParameters.length != PARAMETERS)
 			throw new IllegalArgumentException("Need " + PARAMETERS + " parameters");
 
 		AbstractDataset[] coords = new AbstractDataset[2];
 
 		DoubleDataset x = new DoubleDataset(angles.getShape());
 		DoubleDataset y = new DoubleDataset(angles.getShape());
 		coords[0] = x;
 		coords[1] = y;
 
 		final double r = geometricParameters[0];
 		final double cx = geometricParameters[1];
 		final double cy = geometricParameters[2];
 		final IndexIterator it = angles.getIterator();
 
 		int i = 0;
 		while (it.hasNext()) {
 			final double t = angles.getElementDoubleAbs(it.index);
 			x.setAbs(i, cx + r*Math.cos(t));
 			y.setAbs(i, cy + r*Math.sin(t));
 			i++;
 		}
 		return coords;
 	}
 }
