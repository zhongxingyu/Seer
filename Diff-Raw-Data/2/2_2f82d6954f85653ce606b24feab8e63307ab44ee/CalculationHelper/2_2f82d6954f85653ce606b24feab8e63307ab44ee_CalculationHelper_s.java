 package helper;
 
 public class CalculationHelper {
 	
 	ImageHelper ih;
 	
 	public CalculationHelper() {
 		ih = new ImageHelper();
 	}
 
 	public double[] getTargetOutput(int label) {
 		double[] targetOutput = new double[10];
 		for (int i = 0; i < targetOutput.length; i++) {
 			targetOutput[i] = -1.0;
 		}
 		targetOutput[label] = 1.0;
 		return targetOutput;
 	}
 	
 	public double calculateError(double[] actualOuput, double[] targetOutput) {
         double error = 0.0;
         for (int i = 0; i < targetOutput.length; i++) {
            error += Math.pow(actualOuput[i] - targetOutput[i], 2);
         }
         return error / 2;
     }
 	
 	public int[] randomizeTrainingDataSequence() {
 		int [] trainingDataSequence = new int[ih.lengthOfTrainingSet() + 1];
 		
 		// Initialize array
 		for (int i = 1; i <= ih.lengthOfTrainingSet(); i++) {
 			trainingDataSequence[i] = i;
 		}
 		
 		// Swap at each position with a random other position
 		for (int i = 1; i <= ih.lengthOfTrainingSet(); i++) {
 			int j = 1 + (int) (Math.random() * ((ih.lengthOfTrainingSet() - 1) + 1));
 			int temp = trainingDataSequence[i];
 			trainingDataSequence[i] = trainingDataSequence[j];
 			trainingDataSequence[j] = temp;
 		}
 		
 		return trainingDataSequence;
 	}
 }
