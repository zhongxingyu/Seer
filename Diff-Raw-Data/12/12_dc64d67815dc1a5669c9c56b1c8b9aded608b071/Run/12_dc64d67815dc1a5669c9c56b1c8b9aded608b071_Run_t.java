 import helper.CalculationHelper;
 import helper.ImageHelper;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import neuraalnetwork.NeuralNetwork;
 
 public class Run {
 
 	public static void main(String[] args) {
 		Date timeStarted = Calendar.getInstance().getTime();
 		System.out.println("--- Started at " + timeStarted + " ---");
 		
 		ImageHelper ih = new ImageHelper();
 		CalculationHelper ch = new CalculationHelper();
 		
 		// Parameters
 		int numberNeuronsHiddenLayer = 50;
 		double learningRate = 0.008;
 		int sizeValidationSet = 50;
		double desiredError = 0.85;
 
 		// Initialize network
 		NeuralNetwork nn = new NeuralNetwork(numberNeuronsHiddenLayer);
 
 		// Get validation set		
 		List<Integer> indicesValidationSet = ih.getIndicesValidationSet(sizeValidationSet);	
 		
 		// Start with 'normal' sequence 
 		int [] trainingDataSequence = new int[ih.lengthOfTrainingSet() + 1];
 		for (int i = 1; i <= ih.lengthOfTrainingSet(); i++) {
 			trainingDataSequence[i] = i;
 		}
 
 		// Train and validate network
 		double meanError = 10.0;
 		int counter = 0;
		while (desiredError < meanError) {
 			counter++;
 			
 			// Train
 			Date trainingsRoundStarted = Calendar.getInstance().getTime();
 			for (int i = 1; i <= ih.lengthOfTrainingSet(); i++) {
 				int[] image = ih.readImage(trainingDataSequence[i], ih.getTrainingSet());
 				int label = ih.readLabel(trainingDataSequence[i], ih.getTrainingSet());
 				double[] actualOutput = nn.forwardPropagate(image);
 				nn.backPropagate(actualOutput, ch.getTargetOutput(label),
 						learningRate);
 			}
 			Date trainingsRoundEnded = Calendar.getInstance().getTime();
 			
 			// Validate
 			double sumError = 0.0;
 			for (int i = 0; i < indicesValidationSet.size(); i++) {
 				int[] image = ih.readImage(indicesValidationSet.get(i), ih.getTrainingSet());
 				int label = ih.readLabel(indicesValidationSet.get(i), ih.getTrainingSet());
 				double[] actualOutput = nn.forwardPropagate(image);
 				sumError += ch.calculateError(actualOutput, ch.getTargetOutput(label));		
 			}
 			
 			meanError = sumError / indicesValidationSet.size();			
 			
 			System.out.println("Error: " + meanError +"; Round: " + counter + "; Time: " 
 					+ ((trainingsRoundEnded.getTime() - trainingsRoundStarted.getTime()) / 1000) + " seconds");
 
 			ch.randomizeTrainingDataSequence();
 		}
 
 		// Test network
 		int numberOfRightAnswers = 0;
 		for (int i = 1; i <= ih.lengthOfTestSet(); i++) {
 			int[] image = ih.readImage(i, ih.getTestSet());
 			int label = ih.readLabel(i, ih.getTestSet());
 			double[] actualOutput = nn.forwardPropagate(image);
 
 			double bestOutput = -100.0;
 			int bestIndex = 0;
 			for (int j = 0; j < actualOutput.length; j++) {
 				if (actualOutput[j] > bestOutput) {
 					bestOutput = actualOutput[j];
 					bestIndex = j;
 				}
 			}
 
 			if (label == bestIndex)
 				numberOfRightAnswers++;
 		}
 
 		System.out.println("Found " + numberOfRightAnswers + " right answers in " + ih.lengthOfTestSet() + " tests");
 		System.out.println("Accuracy: " + (double) numberOfRightAnswers	/ (double) ih.lengthOfTestSet() * 100 + "%");
 		
 		Date timeFinished = Calendar.getInstance().getTime();
 		System.out.println("--- Ended at " + timeFinished + " --- " +
 				"Runtime: " + ((timeFinished.getTime() - timeStarted.getTime()) / 1000) + " seconds");
 	}
 
 }
