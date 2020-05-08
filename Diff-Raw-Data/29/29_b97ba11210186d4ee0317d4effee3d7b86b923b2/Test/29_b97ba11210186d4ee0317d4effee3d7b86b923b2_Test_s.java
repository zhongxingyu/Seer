 package com.shsrobotics.reinforcementlearning.estimators;
 
 import com.shsrobotics.reinforcementlearning.util.DataPoint;
 
 public class Test {
 
 	/**
 	 * @param args the command line arguments
 	 */
 	public static void main(String[] args) {
		int n = 100;
 		DataPoint[] data = new DataPoint[n];
 		String[] inputKeys = {"A", "B"};
 		String[] outputKeys = {"Z"};
 		double[] minimums = {0, 0};
 		double[] maximums = {100, 100};
 		for (int i = 0; i < n; i++) {
 			double randA = Math.round(Math.random() * 100);
 			double randB = Math.round(Math.random() * 100);
 			double[] input = {randA, randB};
 			double[] output = {randA + randB};
 			data[i] = new DataPoint(inputKeys, input, outputKeys, output);
 		}
 		RandomForestQEstimator forest = new RandomForestQEstimator(50, minimums, maximums);
 		double[] variableImportance = forest.addInitialData(data);
 		double[] input = {30, 50};
 		System.out.println("output: " + forest.run(input));
 	}
 }
