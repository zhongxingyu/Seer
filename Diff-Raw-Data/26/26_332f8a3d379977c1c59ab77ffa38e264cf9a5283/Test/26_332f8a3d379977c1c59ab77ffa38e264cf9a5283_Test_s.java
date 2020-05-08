 package org.fit.cvut.mvi.evaluator;
 
 public class Test {
 
 	/**
 	 * Demonstrates a sample use of the FitnessEvaluator on the Windows platform.
 	 */
 	public static void main(String[] args) {
 		
 		/* Instantiate the evaluator. */
 		FitnessEvaluator myEvaluator = new FitnessEvaluator(
				"C:\\Program Files\\NetLogo 5.0.2\\NetLogo.jar",
 				"sablona.nlogo",
 				"sablona.xml");
 		
 		/* Calculate fitness of random sheep against random wolves. */
 		System.out.println(
 				"Average sheep fitness: " +
 				myEvaluator.evaluateSheepFitness(FitnessEvaluator.RANDOM_MOVE, FitnessEvaluator.RANDOM_MOVE));
 		
 		/* Calculate fitness of random wolves against random sheep. */
 		System.out.println(
 				"Average wolves fitness: " +
 				myEvaluator.evaluateWolvesFitness(FitnessEvaluator.RANDOM_MOVE, FitnessEvaluator.RANDOM_MOVE));
 
 	}
 
 }
