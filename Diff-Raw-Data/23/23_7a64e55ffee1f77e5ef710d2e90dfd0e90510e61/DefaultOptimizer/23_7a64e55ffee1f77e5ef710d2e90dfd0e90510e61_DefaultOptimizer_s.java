 package com.shsrobotics.reinforcementlearning.optimizers;
 
 import com.shsrobotics.reinforcementlearning.util.DataPoint;
 
 /**
  * Optimize coordinates based on a Pattern Search algorithm. To use this class,
  * classes must extend it and provide a {@code double f(double[] input)} method.
  * Method calls should be made to {@code minimize()} and {@code maximize()}
  * only. Other members are public only for extension and implementation
  * purposes.
  * <p/>
  * @author Team 2412.
  */
 public abstract class DefaultOptimizer implements Optimizer {
 
 	/**
 	 * The number of variables to optimize. Data dimensions.
 	 */
 	private final int n;
 	/**
 	 * How many times to run the algorithm
 	 */
 	private final int iterations;
 	/**
 	 * Minimum variable values. Used to choose random starting points.
 	 */
 	private final double[] minimums;
 	/**
 	 * Maximum Variable values. Used to choose random starting points.
 	 */
 	private final double[] maximums;
 	/**
 	 * Step size for Pattern Search algorithm. Defaults to ten percent of
 	 * average variable range.
 	 */
 	private double[] PatternSearchStep;
 	/**
 	 * Stores the default search step size. Defaults to a quarter of average
 	 * variable range, for a total range of a quarter of the search size.
 	 */
 	private double[] InitialStep;
 	/**
 	 * Default step size fraction.
 	 */
 	private double stepSize = 0.25;
 
 	/**
 	 * Create an optimizer.
 	 * <p/>
 	 * @param dimension the number of variables.
 	 * @param iterations how precise to maximize.
 	 * @param minimums the minimum domain values.
 	 * @param maximums the maximum domain values.
 	 */
 	public DefaultOptimizer(int iterations, double[] minimums, double[] maximums) {
 		this.n = minimums.length;
 		this.iterations = iterations;
 		this.minimums = minimums;
 		this.maximums = maximums;
 
 		PatternSearchStep = new double[n];
 		InitialStep = new double[n];
 
 		//find step size
 		for (int variable = 0; variable < n; variable++) {
 			InitialStep[variable] = stepSize * (maximums[variable] - minimums[variable]); // twenty-five percent of the range
 		}
 	}
 
 	@Override
 	public final double[] maximize() {
 		PatternSearchStep = InitialStep.clone();
 		return psOptimize(true);
 	}
 
 	@Override
 	public final double[] minimize() {
 		PatternSearchStep = InitialStep.clone();
 		return psOptimize(false);
 	}
 
 	/**
 	 * Uses a Pattern Search algorithm to find the coordinates that maximize the
 	 * function.
 	 * <p/>
 	 * @param maximize whether or not to maximize or minimize. If set to true,
 	 * the method will maximize.
 	 * @return the maximized coordinates.
 	 */
 	private double[] psOptimize(boolean maximize) {
 		/*
 		 * Pattern vertices.  Center is stored in 0, Left(k) is stored in k + 1,
 		 * and Right(k) is stored in (k + 1) + n.
 		 */
 		Point[] vertices = new Point[2 * n + 1];
 		int length = vertices.length;
 
 		// center point placed randomly
 		double[] center = rands();
 		for (int vertex = 0; vertex < length; vertex++) {
 			if (vertex == 0) {
 				vertices[vertex] = new Point(center, f(center));
 			} else {
 				vertices[vertex] = new Point(center, 0.0); // saves computation time, as these are recalculated immediately
 			}
 		}
 
 		// each variable
 		for (int k = 0; k < n; k++) {
 			int leftPoint = k + 1;
 			int rightPoint = k + 1 + n;
 			vertices[leftPoint].increment(k, -PatternSearchStep[k]);
 			vertices[leftPoint].update();
 			vertices[rightPoint].increment(k, PatternSearchStep[k]);
 			vertices[rightPoint].update();
 		}
 
 		for (int i = 0; i < iterations; i++) {
 			double best = vertices[0].value(); // best value
 			int bestIndex = 0; // index of best value (currently center)
 			for (int vertex = 1; vertex < length; vertex++) {
 				if (better(vertices[vertex].value(), best, maximize)) {
 					best = vertices[vertex].value();
 					bestIndex = vertex;
 				}
 			}
 			if (bestIndex == 0) {
 				// scale pattern
 				for (int k = 0; k < n; k++) {
 					PatternSearchStep[k] /= 2; // halve search size.
 					int leftPoint = k + 1;
 					int rightPoint = k + 1 + n;
 					vertices[leftPoint].increment(k, PatternSearchStep[k]); // opposite direction
 					vertices[leftPoint].update();
 					vertices[rightPoint].increment(k, -PatternSearchStep[k]); // opposite direction
 					vertices[rightPoint].update();
 				}
 			} else {
 				// move pattern
 				int variable = (bestIndex - 1) % n; // the variable to change
 				int direction = (bestIndex - 1 - n >= 0) ? 1 : -1; // which way to move
 				
 				int oppositeVertex; // find which vertex index corresponds to the opposite vertex
 				if (direction == 1) {
 					oppositeVertex = variable + 1;
 				} else {
 					oppositeVertex = variable + 1 + n;
 				}
 				
 				double change = direction * PatternSearchStep[variable]; // difference between best and center
 				for (int vertex = 0; vertex < length; vertex++) {
 					vertices[vertex].increment(variable, change);
 					
 					// save re-evaluation of function
 					if (vertex == 0) { 
 						vertices[vertex].setValue(vertices[bestIndex].value()); 
 					} else if (vertex == oppositeVertex) {
 						vertices[vertex].setValue(vertices[0].value()); 
 					} else {
 						vertices[vertex].update(); // for new values
 					}
 				}
 			}
 		}
 
 		return vertices[0].coordinates();
 	}
 
 	/**
 	 * A data point. Used for optimization.
 	 */
 	public class Point extends DataPoint {
 		/**
 		 * Create a point.
 		 * <p/>
 		 * @param coordinates the action coordinates.
 		 * @param value the Q-Value from the coordinates.
 		 */
 		public Point(double[] coordinates, double value) {			
 			super(coordinates, value);
 		}
 		
 		/**
 		 * The data coordinates.
 		 */
 		public double[] coordinates() {
 			return super.getInputs();
 		}
 		
 		/**
 		 * The data dependent variable (output).
 		 */
 		public double value() {
 			return super.getOutputs()[0];
 		}
 
 		/**
 		 * Update the {@code value} variable.
 		 */
 		public void update() {
 			super.setOutput(0, f(super.getInputs()));
 		}
 
 		/**
 		 * Set the {@code value} variable. If the value is known, save the time
 		 * updating it.
 		 */
 		public void setValue(double value) {
 			super.setOutput(0, value);
 		}
 
 		/**
 		 * Increment a coordinate by a specified amount. A call to
 		 * {@code update} is recommended afterwards.
 		 * <p/>
 		 * @param k the coordinate variable to change.
 		 * @param amount the amount to increment by.
 		 */
 		public void increment(int k, double amount) {
			double[] newCoordinates = super.getInputs();
 			double newCoordinate = newCoordinates[k] + amount;
 			if (newCoordinate > minimums[k] && newCoordinate < maximums[k]) { //in bounds				
 				setInput(k, newCoordinate);
 			}
 		}
 	}
 
 	/**
 	 * Find which double is better. Whether or not a variable is better is
 	 * determined by the maximize parameter.
 	 * <p/>
 	 * @param a the first value to test.
 	 * @param b the second value to test.
 	 * @param maximize whether or not to test if {@code a > b}. If false, tests
 	 * for
 	 * {@code a < b}.
 	 * @return True if {@code a} is better than {@code b}.
 	 */
 	private boolean better(double a, double b, boolean maximize) {
 		if (maximize) {
 			return a > b;
 		} else {
 			return a < b;
 		}
 	}
 
 	/**
 	 * Fill an array with random values.
 	 * <p/>
 	 * @return The array.
 	 */
 	double[] rands() {
 		double[] toReturn = new double[n];
 		double rangeScale = 2 * stepSize;
 		for (int i = 0; i < n; i++) {
 			double range = (maximums[i] - minimums[i]) * rangeScale;
 			toReturn[i] = Math.random() * range
 				+ minimums[i] + range / 2; // generate random number in range
 		}
 		return toReturn;
 	}
 
 	@Override
 	public abstract double f(double[] input);
 }
