 package anfis;
 
 import util.*;
 //import nodes.*;
 import data.*;
 
 public class Main {
 	public static void main(String[] args) {
 
 			
 		DataInterface xor_test = new DataInterface("input1.csv", 2,4,0,2,2);
 		
 		Settings.numberOfInputVaribles = xor_test.getNumberOfInputVariables();
 		Settings.numberOfShapes = xor_test.getNumberOfShapes();
 		Settings.bellSlope = xor_test.getBellSlope();
 		Settings.inputVariables = xor_test.getInputVariables();
 		
 		ANFIS anfis = new ANFIS();
 		anfis.generateNetwork();
 		
 		System.out.println("--generation finished");
 		
 		
 		double [][] trainingData = xor_test.getTrainigData();
		double [] expectedOutput = xor_test.getExpectedTrainingOutput();
 		
 		
 		
 		
 		// Ausfuehren von Anfis
 		
 		double[][] result;
 		
 		result = anfis.test(trainingData, expectedOutput);
 		
 		System.out.println("Anfaengliche Fehlerrate: " + result[0][0]);
 		
 		anfis.trainConsequent(trainingData, expectedOutput);
 		result = anfis.test(trainingData, expectedOutput);
 		System.out.println("Fehlerrate nach 1. Training (nur Consequent Parameter): " + result[0][0]);
 		anfis.trainPremise(trainingData, expectedOutput);
 		result = anfis.test(trainingData, expectedOutput);
 		
 		System.out.println("Fehlerrate nach 1. Training: " + result[0][0]);
 		
 		
 		anfis.trainConsequent(trainingData, expectedOutput);
 		result = anfis.test(trainingData, expectedOutput);
 		System.out.println("Fehlerrate nach 2. Training (nur Consequent Parameter): " + result[0][0]);
 		anfis.trainPremise(trainingData, expectedOutput);
 		result = anfis.test(trainingData, expectedOutput);
 		
 		System.out.println("Fehlerrate nach 2. Training: " + result[0][0]);
 		
 		
		
 	}
 	}
 
 
