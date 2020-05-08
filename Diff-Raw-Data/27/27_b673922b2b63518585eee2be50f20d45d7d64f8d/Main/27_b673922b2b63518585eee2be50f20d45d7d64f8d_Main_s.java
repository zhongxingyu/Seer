 package anfis;
 
 import util.*;
 //import nodes.*;
 import data.*;
 
 public class Main {
 	public static void main(String[] args) {
 
 		// get statistics
 
 		DataReader input = new DataReader();
 		double [][] data = input.readData();
 		double[][] statistics = input.getStatistics();
 		InputVariable[] inputVariables = new InputVariable[statistics[0].length-1];
 
 		// i = 0 == expectedOutput
 		
 		for (int i = 0; i < inputVariables.length; i++) {
 			inputVariables[i] = new InputVariable(i + 1, statistics[0][i+1],
 					statistics[1][i+1]);
 		}
 
 		
 		
 		// set Settings
 		
 		Settings.numberOfInputVaribles = statistics[0].length - 1;
 		// TODO do we need this ??
 		Settings.trainingDataSize = 0;
 		Settings.numberOfShapes = 4;
 		Settings.bellSlope = 2;
 		Settings.inputVariables = inputVariables;
 
 		// Creating ANFIS network
 		ANFIS anfis = new ANFIS();
 		anfis.generateNetwork();
 		
 		System.out.println("--generation finished");
 		
 		/*double [] trainingData = new double [5];
 		trainingData[0] = data [0][1];
 		trainingData[1] = data [0][2];
 		trainingData[2] = data [0][3];
 		trainingData[3] = data [0][4];
 		trainingData[4] = data [0][5];
 		double expectedOutput = data[0][0]; */
 		
 		//anfis.training(trainingData);
 		
 		//System.out.println("Finished! Expected Output was: " + expectedOutput);
 		
 		
 		// create the training data
 		int trainigDataLength = 50;
 		
 		double [][] trainingData = new double [trainigDataLength][5];
 		double [] expectedOutput = new double [trainigDataLength];
 		
 		for (int i = 0; i < trainigDataLength; i++){
 			expectedOutput [i] = data [i][0];
 			trainingData[i][0] = data [i][1];
 			trainingData[i][1] = data [i][2];
 			trainingData[i][2] = data [i][3];
 			trainingData[i][3] = data [i][4];
 			trainingData[i][4] = data [i][5];
 			
 		}
 		
 		
 		// Ausfuehren von Anfis
 		
 		double errorSum;
 		
 		errorSum = anfis.test(trainingData, expectedOutput);
 		
 		System.out.println("Anfaengliche Fehlerrate: " + errorSum);
 		
 		errorSum = anfis.training(trainingData, expectedOutput);
 		System.out.println("Fehlerrate nach 1. Training (nur Consequent Parameter): " + errorSum);
 		errorSum = anfis.test(trainingData, expectedOutput);
 		
 		System.out.println("Fehlerrate nach 1. Training: " + errorSum);
 		
 		
 //		errorSum = anfis.training(trainingData, expectedOutput);
 //		System.out.println("Fehlerrate nach 2. Training (nur Consequent Parameter): " + errorSum);
 //		errorSum = anfis.test(trainingData, expectedOutput);
 //		
 //		System.out.println("Fehlerrate nach 2. Training: " + errorSum);
 		
 
 	}
 
 }
