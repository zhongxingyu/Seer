 package ch.sfdr.fractals.fractals;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ch.sfdr.fractals.math.ComplexNumber;
 
 /**
  * Finds cycles in orbits of escape time fractal functions.
  * Uses a Newton iterator with a Jacobi matrix to find the root for a given
  * start point. The basic iteration:
  *   z = z - jacobian(z)^{-1} * f(z)
  * @author D.Ritz
  */
public strictfp class ComplexOrbitCycleFinder
 {
 	// the tolerance in the newton finding
 	private static final double TOL = 1e-13;
 
 	// the tolerance comparing cycle start points
 	private static final double POINT_TOL = 1e-4;
 
 	// the "h" used in diff. Smaller values would cause NaN problems
 	private static final double DIFF_H = 1e-8;
 
	private static final double DIVIDE = 5.0;

 	private StepFractalFunction function;
 	private int cycleLength;
 	private long delay;
 
 	// the maximum number of iterations
 	private int maxIter = 400;
 
 	private ComplexOrbitCycleListener listener;
 	private Thread thread;
 	private List<ComplexNumber> cycleStartPoints;
 
 	/**
 	 * constructs a cycle finder for the given fractal function
 	 * @param listener the listener
 	 */
 	public ComplexOrbitCycleFinder(ComplexOrbitCycleListener listener)
 	{
 		this.listener = listener;
 		cycleStartPoints = new ArrayList<ComplexNumber>();
 	}
 
 	/**
 	 * returns the maximum number of iterations allowed
 	 * @return the max number of iterations
 	 */
 	public int getMaxIter()
 	{
 		return maxIter;
 	}
 
 	/**
 	 * sets the maximum number of iterations allowed
 	 * @param maxIter max number of iterations
 	 */
 	public void setMaxIter(int maxIter)
 	{
 		this.maxIter = maxIter;
 	}
 
 
 	/**
 	 * finds a cycle of given length, starting the Newton iteration with the
 	 * given start value
 	 * @param cycleLenght length of the cycle to find
 	 * @param start the start value for the Newton iteration
 	 * @return One possible point as ComplexNumber where a cycle occurs
 	 */
 	private ComplexNumber findCycle(int cycleLenght, ComplexNumber start)
 	{
 		this.cycleLength = cycleLenght;
 
 		// the value that should converge to the solution
 		ComplexNumber z = start.clone();
 
 		// the function value, i.e. the difference to 0
 		ComplexNumber f = new ComplexNumber(1, 1);
 
 		// the delta from the jacobian multiplied by the function value
 		ComplexNumber delta = new ComplexNumber(0, 0);
 
 		// the jacobian (and it's inverse)
 		Jacobian2x2 jac = new Jacobian2x2();
 
 		int i = 0;
 		while ((Math.abs(f.getReal()) > TOL ||
 				Math.abs(f.getImaginary()) > TOL) &&
 				i++ < maxIter)
 		{
 			// get the inverse of the jacobian
 			jac.jacobian(z).invert();
 
 			// get the function value
 			callFunction(z, f);
 
 			// multiply inverse of the jacobian with the current function value
 			delta.set(
 				jac.get(0, 0) * f.getReal() +
 					jac.get(0, 1) * f.getImaginary(),
 				jac.get(1, 0) * f.getReal() +
 					jac.get(1, 1) * f.getImaginary());
 
 			// subtract the delta form the current value
 			z.subtract(delta);
 		}
 
 		if (i >= maxIter || z.isNaN())
 			return null;
 
 		return z;
 	}
 
 
 	/**
 	 * Tries to find all cycles of a given length. The Listener will be called
 	 * with the results
 	 * @param function the fractal step function
 	 * @param length the length of the cycles
 	 * @param delay the delay in milliseconds between each cycle found
 	 */
 	public synchronized void findAllCycles(StepFractalFunction function,
 			int length, long delay)
 	{
 		stop();
 
 		this.function = function;
 		this.cycleLength = length;
 		this.delay = delay;
 		cycleStartPoints.clear();
 
 		thread = new Thread() {
 			@Override
 			public void run()
 			{
 				doFindAllCycles();
 			};
 		};
 		thread.start();
 	}
 
 	/**
 	 * stop any running calculation
 	 */
 	public synchronized void stop()
 	{
 		if (thread != null) {
 			thread.interrupt();
 			try {
 				thread.join();
 			} catch (InterruptedException e) {
 			}
 			thread = null;
 		}
 	}
 
 	/**
 	 * waits for completion
 	 * @throws InterruptedException
 	 */
 	public void waitForCompletion()
 		throws InterruptedException
 	{
 		Thread t;
 		synchronized (this) {
 			t = thread;
 		}
 		if (t != null)
			t.join();
 	}
 
 	/**
 	 * Scans over the whole boundary area. The area is divided into
 	 * (5 * cycleLength) pieces in both directions.
 	 */
 	private void doFindAllCycles()
 	{
 		double xmin = function.getLowerBounds().getReal();
 		double ymin = function.getLowerBounds().getImaginary();
 		double xmax = function.getUpperBounds().getReal();
 		double ymax = function.getUpperBounds().getImaginary();
 		double boundary = function.getBoundarySqr();
 
		double stepSizeX = Math.abs(xmax - xmin) / (DIVIDE * cycleLength);
		double stepSizeY = Math.abs(ymax - ymin) / (DIVIDE * cycleLength);
 
 		ComplexNumber start = new ComplexNumber(0, 0);
 
 		long lastCycleTs = System.currentTimeMillis();
 
 		for (double x = xmin; x < xmax; x += stepSizeX) {
 			for (double y = ymin; y < ymax; y += stepSizeY) {
 				// skip points outside the boundary circle
 				if (x * x + y * y > boundary)
 					continue;
 
 				start.set(x, y);
 				ComplexNumber z = findCycle(cycleLength, start);
 
 				if (Thread.interrupted())
 					return;
 
 				// check list if this (very similar) point has already been found
 				boolean newCycle = addNewCycle(z);
 				if (newCycle) {
 					boolean cont = listener.cycleFound(z, cycleLength);
 					if (!cont)
 						return;
 
 					// delay the next cycle
 					long dur = System.currentTimeMillis() - lastCycleTs;
 					long sleepDelay = delay - dur;
 					if (sleepDelay > 0) {
 						try {
 							Thread.sleep(sleepDelay);
 						} catch (InterruptedException e) {
 							return;
 						}
 					}
 					lastCycleTs = System.currentTimeMillis();
 				}
 			}
 		}
 	}
 
 	/**
 	 * checks if a cycle is new by looking it up in the list
 	 * @param z the start point of the cycle
 	 * @return true if the cycle is new, false if already found before
 	 */
 	private boolean addNewCycle(ComplexNumber z)
 	{
 		// max iterations count reached or NaN
 		if (z == null)
 			return false;
 
 		// compare against points found previously
 		for (ComplexNumber c : cycleStartPoints) {
 			if (Math.abs(c.getReal() - z.getReal()) < POINT_TOL &&
 				Math.abs(c.getImaginary() - z.getImaginary()) < POINT_TOL)
 			{
 				return false;
 			}
 		}
 
 		// new...add it to the list
 		cycleStartPoints.add(z);
 
 		return true;
 	}
 
 	/**
 	 * calls the fractal function cycleLength times
 	 * @param start the start value
 	 * @param res the result
 	 * @return the value after cycleCount iterations
 	 */
 	private ComplexNumber callFunction(ComplexNumber start, ComplexNumber res)
 	{
 		res.set(start);
 		for (int i = 0; i < cycleLength; i++)
 			function.step(start, res);
 		// bring it to the f(z) = 0 form
 		res = res.subtract(start);
 		return res;
 	}
 
 	/**
 	 * A 2x2 matrix jacobian, with the ability to invert itself.
 	 * All objects are pre-allocated.
 	 */
 	private class Jacobian2x2
 	{
 		private double[][] elem = new double[2][2];
 		private double[][] inv = new double[2][2];
 		private ComplexNumber start = new ComplexNumber(0, 0);
 		private ComplexNumber zd = new ComplexNumber(0, 0);
 		private ComplexNumber zd2 = new ComplexNumber(0, 0);
 
 		public double get(int x, int y)
 		{
 			return elem[x][y];
 		}
 
 		public void set(int x, int y, double val)
 		{
 			elem[x][y] = val;
 		}
 
 		/**
 		 * inverts this jacobian
 		 * @return this
 		 */
 		public Jacobian2x2 invert()
 		{
			double det = elem[0][0] * elem[1][1] - elem[0][1] * elem[1][0];
 
 			inv[0][0] =  elem[1][1] / det;
 			inv[0][1] = -elem[0][1] / det;
 			inv[1][0] = -elem[1][0] / det;
 			inv[1][1] =  elem[0][0] / det;
 
 			// just swap the two buffers - no instantiations
 			double[][] tmp = elem;
 			elem = inv;
 			inv = tmp;
 
 			return this;
 		}
 
 		/**
 		 * builds the jacobian matrix with numerical differentiation (central diff')
 		 * @param z the complex number
 		 * @return this
 		 */
 		public Jacobian2x2 jacobian(ComplexNumber z)
 		{
 			// x part
 			start.set(z.getReal() + DIFF_H, z.getImaginary());
 			callFunction(start, zd);
 
 			start.set(z.getReal() - DIFF_H, z.getImaginary());
 			callFunction(start, zd2);
 
 			zd.subtract(zd2);
 			set(0, 0, zd.getReal() / (2 * DIFF_H));
 			set(1, 0, zd.getImaginary() / (2 * DIFF_H));
 
 			// y part:
 			start.set(z.getReal(), z.getImaginary() + DIFF_H);
 			callFunction(start, zd);
 
 			start.set(z.getReal(), z.getImaginary() - DIFF_H);
 			callFunction(start, zd2);
 			zd.subtract(zd2);
 			set(0, 1, zd.getReal() / (2 * DIFF_H));
 			set(1, 1, zd.getImaginary() / (2 * DIFF_H));
 
 			return this;
 		}
 	}
 }
