 import java.util.ArrayList;
 
 public class Main {
 	
 	public static void main(String[] args){
 		try {
 			// OK let's do this.
 			
 			///////////////////////////////////////////////////////////////////
 			//// Data used across various parts.
 			ArrayList<Entry> bcan_test = DataReader.read("bcan/bcan.test");
 			ArrayList<Entry> bcan_train = DataReader.read("bcan/bcan.train");
 			ArrayList<Entry> bcan_train30 = DataReader.read("bcan/bcan.train30");
 			ArrayList<Entry> bcan_train90 = DataReader.read("bcan/bcan.train90");
 			ArrayList<Entry> bcan_validate = DataReader.read("bcan/bcan.validate");
 			
 			int[] stoppingParamList = new int[] {1,5,9,17,25,37,43,49};
 			/*
 			int[] stoppingParamList = new int[80];
 			for (int i = 0; i < 80; ++i) 
 				stoppingParamList[i] = i+1;
 			*/
 			///////////////////////////////////////////////////////////////////
 			
 			// Part a)
 			System.out.printf("\n\nPart a)\n");
 			NodeFactory.changeCriterion(new Criterion_MaxGain());
 			
 			ArrayList<Integer> numberNodes1 = new ArrayList<Integer>();
 			ArrayList<Double> testingError1 = new ArrayList<Double>();
 			ArrayList<Double> validatingError1 = new ArrayList<Double>();
 			ArrayList<Double> trainingError1 = new ArrayList<Double>();
 			
 			for (int i : stoppingParamList) {
 				NodeFactory.changeStoppingParam(i);
 				
 				Tree t1 = new Tree();
 				// Grow tree using bcan_train90
 				t1. growTree(bcan_train90);
 				
 				//t1.printTree();
 				
 				numberNodes1.add(t1.nodeCount());
 				testingError1.add(t1.getError(bcan_test));
 				validatingError1.add(t1.getError(bcan_validate));
 				trainingError1.add(t1.getError(bcan_train90));
 				
 				// Print the testing, validate, and training errors
 				System.out.printf("For stopping param = %d, Testing Error " +
 						"= %f, Validate Error = %f, Training Error = %f, Number" +
 						" of nodes = %d\n", 
 						i, t1.getError(bcan_test), t1.getError(bcan_validate),
 						t1.getError(bcan_train90), t1.nodeCount());
 			}
 			
 			System.out.println("%% For MATLAB plotting purposes only %%"); 
 			String xs1 = "";
 			String testings1 = "";
 			String trainings1 = "";
 			for (int k = 0; k < numberNodes1.size(); ++k) {
 				xs1 += (numberNodes1.get(k) + ", ");
 				testings1 += (testingError1.get(k) + ", ");
 				trainings1 += (trainingError1.get(k) + ", ");
 			}
 				
 			xs1 = xs1.substring(0, xs1.length()-2);
 			testings1 = testings1.substring(0, testings1.length()-2);
 			trainings1 = trainings1.substring(0, trainings1.length()-2);
 			
 			System.out.println("figure\nhold on");
 			System.out.println("node_size = [" + xs1 + "]");
 			System.out.println("testing_error = [" + testings1 + "]");
 			System.out.println("training_error = [" + trainings1 + "]");  
 			System.out.println("plot(node_size, testing_error)\n" +
 					"plot(node_size, training_error)\n" +
 					"xlabel('node number')\nylabel('error')\n" +
 					"title('Problem 2a) error vs node')\n" +
 					"legend('testing','training')\n\n"); 
 
 			System.out.println("## For table generation purpose only: copy to excel ##"); 
 			System.out.println("Table of number of nodes Vs. Validation Error");
 			
 			System.out.printf("Stopping Parameter");
 			for (int k = 0; k < numberNodes1.size(); ++k) {
 				System.out.printf("\t%d", stoppingParamList[k]);
 			}
 			System.out.printf("\n");
 			
 			System.out.printf("Number of Nodes");
 			for (int k = 0; k < numberNodes1.size(); ++k) {
 				System.out.printf("\t%d", numberNodes1.get(k));
 			}
 			System.out.printf("\n");
 			
 			System.out.printf("Validation Set Misclassification");
 			for (int k = 0; k < numberNodes1.size(); ++k) {
 				System.out.printf("\t%f", validatingError1.get(k));
 			}
 			System.out.printf("\n");
 			
 			///////////////////////////////////////////////////////////////////
 			// Part b)
 			System.out.printf("\n\nPart b)\n");
 			NodeFactory.changeCriterion(new Criterion_MinError());
 			
 			ArrayList<Integer> numberNodes2 = new ArrayList<Integer>();
 			ArrayList<Double> testingError2 = new ArrayList<Double>();
 			ArrayList<Double> validatingError2 = new ArrayList<Double>();
 			ArrayList<Double> trainingError2 = new ArrayList<Double>();
 			
 			for (int i : stoppingParamList) {
 				NodeFactory.changeStoppingParam(i);
 				
 				Tree t2 = new Tree();
 				// Grow tree using bcan_train90
 				t2.growTree(bcan_train90);
 				
 				numberNodes2.add(t2.nodeCount());
 				testingError2.add(t2.getError(bcan_test));
 				validatingError2.add(t2.getError(bcan_validate));
 				trainingError2.add(t2.getError(bcan_train90));
 				
 				// Print the testing, validate, and training errors
 				System.out.printf("For stopping param = %d, Testing Error " +
 						"= %f, Validate Error = %f, Training Error = %f, Number" +
 						" of nodes = %d\n", 
 						i, t2.getError(bcan_test), t2.getError(bcan_validate),
 						t2.getError(bcan_train90), t2.nodeCount());
 			}
 			
 			System.out.println("%% For MATLAB plotting purposes only %%"); 
 			String xs2 = "";
 			String testings2 = "";
 			String trainings2 = "";
 			for (int k = 0; k < numberNodes2.size(); ++k) {
 				xs2 += (numberNodes2.get(k) + ", ");
 				testings2 += (testingError2.get(k) + ", ");
 				trainings2 += (trainingError2.get(k) + ", ");
 			}
 				
 			xs2 = xs2.substring(0, xs2.length()-2);
 			testings2 = testings2.substring(0, testings2.length()-2);
 			trainings2 = trainings2.substring(0, trainings2.length()-2);
 			
 			System.out.println("figure\nhold on");
 			System.out.println("node_size = [" + xs2 + "]");
 			System.out.println("testing_error = [" + testings2 + "]");
 			System.out.println("training_error = [" + trainings2 + "]");  
 			System.out.println("plot(node_size, testing_error)\n" +
 					"plot(node_size, training_error)\n" +
 					"xlabel('node number')\nylabel('error')\n" +
 					"title('Problem 2b) error vs node')\n" +
 					"legend('testing','training')\n\n"); 
 
 			System.out.println("## For table generation purpose only: copy to excel ##"); 
 			System.out.println("Table of number of nodes Vs. Validation Error");
 			
 			System.out.printf("Stopping Parameter");
 			for (int k = 0; k < numberNodes2.size(); ++k) {
 				System.out.printf("\t%d", stoppingParamList[k]);
 			}
 			System.out.printf("\n");
 			
 			System.out.printf("Number of Nodes");
 			for (int k = 0; k < numberNodes2.size(); ++k) {
 				System.out.printf("\t%d", numberNodes2.get(k));
 			}
 			System.out.printf("\n");
 			
 			System.out.printf("Validation Set Misclassification");
 			for (int k = 0; k < numberNodes2.size(); ++k) {
 				System.out.printf("\t%f", validatingError2.get(k));
 			}
 			System.out.printf("\n");
 			
 			///////////////////////////////////////////////////////////////////
 			// Part c)
 			System.out.printf("\n\nPart c)\n");
 			NodeFactory.changeCriterion(new Criterion_MaxGain());
 			NodeFactory.changeStoppingParam(1);
 			// Grow tree using bcan_train90
 			Tree t3 = new Tree();
 			t3.growTree(bcan_train90);
 			
 			// Prune the tree using bcan_validate
 			t3.Prune(bcan_validate);
 			
 			// Print the test errors, and total number of nodes
 			System.out.printf("The Test Error = %f\nTotal number of nodes = %d", 
 					t3.getError(bcan_test), t3.nodeCount());
 			
 			///////////////////////////////////////////////////////////////////
 			// Part d)
 			// Please refer to the function.
			for (int i = 0; i < 1000; ++i) {
 			System.out.printf("\n\nPart d)\n");
 			crossValidation(bcan_train, bcan_test, stoppingParamList);
			}
 			
 			///////////////////////////////////////////////////////////////////
 			// Part e)
 			System.out.printf("\n\nPart e)\n");
 			NodeFactory.changeCriterion(new Criterion_MaxGain());
 			
 			ArrayList<Integer> numberNodes5 = new ArrayList<Integer>();
 			ArrayList<Double> testingError5 = new ArrayList<Double>();
 			ArrayList<Double> validatingError5 = new ArrayList<Double>();
 			ArrayList<Double> trainingError5 = new ArrayList<Double>();
 			
 			for (int i : stoppingParamList) {
 				NodeFactory.changeStoppingParam(i);
 				
 				Tree t5 = new Tree();
 				// Grow tree using bcan_train30
 				t5.growTree(bcan_train30);
 				
 				numberNodes5.add(t5.nodeCount());
 				testingError5.add(t5.getError(bcan_test));
 				validatingError5.add(t5.getError(bcan_validate));
 				trainingError5.add(t5.getError(bcan_train90));
 				
 				// Print the testing, validate, and training errors
 				System.out.printf("For stopping param = %d, Testing Error " +
 						"= %f, Validate Error = %f, Training Error = %f\n", 
 						i, t5.getError(bcan_test), t5.getError(bcan_validate),
 						t5.getError(bcan_train30));
 			}
 			
 			
 			System.out.println("%% For MATLAB plotting purposes only %%"); 
 			String xs5 = "";
 			String testings5 = "";
 			for (int k = 0; k < numberNodes5.size(); ++k) {
 				xs5 += (numberNodes5.get(k) + ", ");
 				testings5 += (testingError5.get(k) + ", ");
 			}
 				
 			xs5 = xs5.substring(0, xs5.length()-2);
 			testings5 = testings5.substring(0, testings5.length()-2);
 			
 			System.out.println("figure\nhold on");
 			System.out.println("node_size = [" + xs5 + "]");
 			System.out.println("testing_error = [" + testings5 + "]");
 			System.out.println("plot(node_size, testing_error)\n" +
 					"xlabel('node number')\nylabel('error')\n" +
 					"title('Problem 2e) training error vs node')\n\n"); 
 			
 		} catch (Exception e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		
 	}
 
 	private static void crossValidation(ArrayList<Entry> bcan_train, 
 			ArrayList<Entry> bcan_test, int[] stoppingParamList) throws Exception {
 		
 		NodeFactory.changeCriterion(new Criterion_MaxGain());
 		// Make a copy
 		ArrayList<Entry> bcan_train_cp = new ArrayList<Entry>(bcan_train);
 		
 		// Knuth Shuffle. 
 		for (int i = 0; i < bcan_train_cp.size(); ++i) {
 			int loc = MathUtilities.genRandomIntRange(i, bcan_train_cp.size());
 			if (loc != i) {
 				Entry e = bcan_train_cp.get(i);
 				bcan_train_cp.set(i, bcan_train_cp.get(loc));
 				bcan_train_cp.set(loc, e);
 			}
 		}
 		
		/*
		System.out.printf("First few entries:");
		for (int i = 0; i < 10; ++i) {
			System.out.printf("%s ", bcan_train_cp.get(i).toString());
		}
		System.out.printf("\n");
		*/
		
 		// Generate errors for all cases
 		double[][] bestStoppingParamList = new double[10][stoppingParamList.length];
 		for (int i = 0; i < 10; ++i) {
 			ArrayList<Entry> training_p4 = new ArrayList<Entry>();
 			ArrayList<Entry> validating_p4 = new ArrayList<Entry>();
 			
 			// Build training and validating set with train
 			for (int j = 0; j < i*55; ++j) {
 				if (j < i*55 || j >= (i+1)*55)
 					training_p4.add(bcan_train_cp.get(j));
 				else
 					validating_p4.add(bcan_train_cp.get(j));
 			}
 			
 			for (int j= 0; j < stoppingParamList.length; ++j) {
 				NodeFactory.changeStoppingParam(stoppingParamList[j]);
 				
 				Tree t4 = new Tree();
 				// Grow tree using training_p4
 				t4.growTree(training_p4);
 				
 				// Validate the tree using validating_p4
 				// Record the error at bestStoppingParamList[i][j]
 				bestStoppingParamList[i][j] = t4.getError(validating_p4);
 			}
 		}
 		
 		int bestStoppingParam = -1;
 		int bestError = Integer.MAX_VALUE;
 		for (int j = 0; j < stoppingParamList.length; ++j) {
 			int currError = 0;
 			for (int i = 0; i < 10; ++i) {
 				currError += bestStoppingParamList[i][j];
 			}
 			
 			if (currError < bestError) {
 				bestError = currError;
 				bestStoppingParam = stoppingParamList[j];
 			}
 		}
 		
 		// Report bestStoppingParam as best stopping param.
 		System.out.printf("The best stopping parameter = %d\n", bestStoppingParam);
 		
 		NodeFactory.changeStoppingParam(bestStoppingParam);
 		Tree t4best = new Tree();
 		// Grow tree using bcan_train
 		t4best.growTree(bcan_train);
 		
 		// Print the testing errors
 		System.out.printf("The Test Error = %f\n", 
 				t4best.getError(bcan_test));
 	}
 }
