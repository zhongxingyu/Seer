 package com.shsrobotics.reinforcementlearning.optimizers;
 
 /**
  * Optimize coordinates based on a Pattern Search algorithm.
  * To use this class, classes must extend it and provide 
  * a {@code double f(double[] input)} method. Method calls should be
  * made to {@code minimize()} and {@code maximize()} only.  Other members
  * are public only for extension and implementation purposes.
  * @author Team 2412.
  */
 public abstract class Optimizer {
 	/**
 	 * The number of variables to optimize.
 	 * Data dimensions.
 	 */
 	private final int n; 
 	/**
 	 * How many times to run the algorithm
 	 */
 	private final int iterations;
 	
 	/**
 	 * Minimum variable values.
 	 * Used to choose random starting points.
 	 */
 	private final double[] minimums;
 	/**
 	 * Maximum Variable values.
 	 * Used to choose random starting points.
 	 */
 	private final double[] maximums;
 	
 		
 	/**
 	 * Step size for Pattern Search algorithm.
 	 * Defaults to ten percent of average variable range.
 	 */
	private double PatternSearchStep;	
	/**
	 * Stores the default search step size.
	 * Defaults to ten percent of average variable range.
	 */
	private double InitialStep;
 	
 	/**
 	 * Create an optimizer.
 	 * @param dimension the number of variables.
 	 * @param iterations how precise to maximize.
 	 * @param minimums the minimum domain values.
 	 * @param maximums the maximum domain values.
 	 */
 	public Optimizer(int iterations, double[] minimums, double[] maximums) {
 		this.n = minimums.length;
 		this.iterations = iterations;
 		this.minimums = minimums;
 		this.maximums = maximums;
 		
 		//find average step size
 		double sum = 0;
 		for (int variable = 0; variable < n; variable++) {
 			sum += 0.1 * (maximums[variable] - minimums[variable]); // ten percent of the range
 		}
		InitialStep = sum / n;
 	}
 	
 	
 	
 	/**
 	 * Maximize the fitness function output for a set of coordinates.
 	 * @return the maximized coordinates.
 	 */
 	public double[] maximize() {
		PatternSearchStep = InitialStep;
 		return psOptimize(true);
 	}
 	
 	/**
 	 * Minimize the fitness function output for a set of coordinates.
 	 * @return the minimized coordinates.
 	 */
 	public double[] minimize() {
		PatternSearchStep = InitialStep;
 		return psOptimize(false);
 	}
 
 	
 	/**
 	 * Uses a Pattern Search algorithm to find the coordinates that maximize
 	 * the function.
 	 * @param maximize whether or not to maximize or minimize. If set to
 	 * true, the method will maximize.
 	 * @return the maximized coordinates.
 	 */
 	private double[] psOptimize(boolean maximize) {
 		/*
 		 * Pattern vertices.  Center is stored in 0, Left(k) is stored in k + 1,
 		 * and Right(k) is stored in k + n.
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
 			vertices[leftPoint].increment(k, -PatternSearchStep);
 			vertices[rightPoint].increment(k, PatternSearchStep);	
 		}
 		
 		for (int i = 0; i < iterations; i++) {
 			double best = vertices[0].value; // best value
 			int bestIndex = -1; // index of best value
 			for (int vertex = 1; vertex < length; vertex++) {
 				if (better(vertices[vertex].value, best, maximize)) {
 					best = vertices[vertex].value;
 					bestIndex = vertex;
 				}
 			}
 			if (bestIndex == -1) { // center was best
 				// scale pattern
 				PatternSearchStep /= 2; // halve search size.
 				for (int k = 0; k < n; k++) {
 					int leftPoint = k + 1;
 					int rightPoint = k + 1 + n;
 					vertices[leftPoint].increment(k, PatternSearchStep);
 					vertices[rightPoint].increment(k, -PatternSearchStep);
 				}
 			} else {
 				// move pattern
 				double[] changes = new double[n];
 				for (int k = 0; k < n; k++) {
 					changes[k] = vertices[bestIndex].coordinates[k]
 							- vertices[0].coordinates[k];
 				}
 				for (int vertex = 0; vertex < length; vertex++) {
 					for (int k = 0; k < n; k++) {
 						vertices[vertex].increment(k, changes[k]);			
 					}
 				}
 			}
 		}
 		
 		return vertices[0].coordinates;
 	}
 	
 	
 	
 	/**
 	 * A data point.  Used for optimization.
 	 */
 	public class Point {
 		/**
 		 * The data coordinates.
 		 */
 		public double[] coordinates;
 		/**
 		 * The data dependent variable (output).
 		 */
 		public double value;
 		
 		/**
 		 * Create a point.
 		 * @param coordinates the action coordinates.
 		 * @param value the Q-Value from the coordinates.
 		 */
 		public Point(double[] coordinates, double value)	{
 			this.coordinates = coordinates;
 			this.value = value;
 		}
 		
 		/**
 		 * Update the {@code value} variable.
 		 */
 		public void update() {
 			this.value = f(this.coordinates);
 		}
 		
 		/**
 		 * Increment a coordinate by a specified amount.
 		 * @param k the coordinate variable to change.
 		 * @param amount the amount to increment by.
 		 */
 		public void increment(int k, double amount) {
 			double[] newCoordinates = this.coordinates.clone();
 			newCoordinates[k] += amount;
 			this.coordinates = newCoordinates.clone();
 			update();
 		}
 	}
 
 	/**
 	 * Find which double is better.
 	 * Whether or not a variable is better is determined by 
 	 * the maximize parameter.
 	 * @param a the first value to test.
 	 * @param b the second value to test.
 	 * @param maximize whether or not to test if {@code a > b}.  If false, tests for
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
      * @return The array.
      */
     private double[] rands() {
         double[] toReturn = new double[n];
         for (int i = 0; i < n; i++) {
 			double range = maximums[i] - minimums[i];
 			toReturn[i] = Math.random() * range + minimums[i]; // generate random number in range
 		}
         return toReturn;
     }
 	
 	
 	/**
 	 * Run the fitness function.
 	 * @param input the input values (domain).
 	 * @return the result (range).
 	 */
 	public abstract double f(double[] input);
 }
