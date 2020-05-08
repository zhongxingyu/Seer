 package NerualNet;
 
 import java.util.Scanner;
 import java.io.File; 
 import java.io.IOException;
 
 // Main driver class for all network types
 public class NeuralNet{
 	
 //// Main Program Entry	////
 	public static void main(String[] args) throws IOException, InterruptedException {
 		int type, trainingStrategy, inputs, samples, epochs, correct, error, fail;
 	
 		int outputs = 1;
 		int layers = 0;
 		int centers = 0;
 		int numLabels = 0;
 		int numClasses = 26;
 		double rate = 0.01;
 		boolean classFirst = true;
 		
 	/** Set number of threads here **/
 		int numThreads = 8 ;
 	/********************************/
 		
 		char[] expected1;
 		char[] expected2;
 		char[] classes;
 		
 		double[][] set1;
 		double[][] set2;
 		
 		String fileName1;
 		
 		Scanner keyscan;
 		Scanner filescan1;
 		
 		TrainingThread[] trainThreads;
 		TestThread[] testThreads;
 		
 		Network[] net;
 		
 		File file1;
 		
 		keyscan = new Scanner(System.in);
 		
 	
 		
 		System.out.println("Enter the number of hidden layers: ");
 		layers = keyscan.nextInt();
 		
 		
 		System.out.println("Enter number of training epochs: ");
 		epochs = keyscan.nextInt();
 		
 		System.out.println("Enter number of samples: ");
 		samples = keyscan.nextInt();
 		
 		System.out.println("Enter a value for n: ");
 		inputs = keyscan.nextInt();
 		
 		System.out.println("Enter a training strategy: ");
		System.out.println("Select network type: ");
 		System.out.println("1. Backpropagation");
 		System.out.println("2. Genetic Algorithm");
 		System.out.println("3. Evolutionary Strategy");
 		System.out.println("4. Differential Evolution");
 		trainingStrategy = keyscan.nextInt();
 		
 		//System.out.println("Enter input file name: ");
 		//fileName1 = keyscan.next();
 		
 		//////// Hardcode filename for now
 		fileName1 = "data/letter-recognition.data";
 //		fileName1 = "data/pendigits.tra";
 		//fileName1 = "data/optdigits.tra";
 		////////
 
 		file1 = new File(fileName1);
 		
 		filescan1 = new Scanner(file1);
 		filescan1.useDelimiter(",|\\n|\\r\\n");
 		
 		set1 = new double[inputs][samples];
 		set2 = new double[inputs][samples];
 		
 		classes = new char[numClasses];
 		
 		expected1 = new char[samples];
 		expected2 = new char[samples];
 		
 		trainThreads = new TrainingThread[numThreads];
 				
 		// Read in list of possible classes
 		for (int i = 0; i < numClasses; i++){
 			classes[i] = filescan1.next().trim().charAt(0);
 		}
 		
 		// Read in training set vectors
 		for (int i = 0; i < samples; i++){
 			if(classFirst)
 				expected1[i] = filescan1.next().charAt(0);
 			
 			for (int j = 0; j < inputs; j++){
 				set1[j][i] = Integer.parseInt(filescan1.next().trim());
 			}
 			
 			if(!classFirst)
 				expected1[i] = filescan1.next().charAt(0);
 		}
 		
 		// Read in test set vectors
 		for (int i = 0; i < samples; i++){
 			if(classFirst)				
 				expected2[i] = filescan1.next().charAt(0);
 			
 			for (int j = 0; j < inputs; j++){
 				set2[j][i] = Integer.parseInt(filescan1.next().trim());
 			}
 			
 			if(!classFirst)				
 				expected2[i] = filescan1.next().charAt(0);
 		}			
 		
 		// Create one network for each possible class
 		net = new MLPNet[numClasses];	
 		
 		// Fill up the thread array with training networks
 		int t = 0;		
 		for (int i = 0; i < numClasses; i++){
 			net[i] = new MLPNet(inputs, layers, outputs, rate, classes[i]);
 			
 			if(trainingStrategy == 1)
 				net[i].setTrainingStrategy(new BPTrainingStrategy());
 			
 			else if(trainingStrategy == 2)
 				net[i].setTrainingStrategy(new GATrainingStrategy());
 			
 			else if(trainingStrategy == 3)
 				net[i].setTrainingStrategy(new ESTrainingStrategy());
 			
 			else if(trainingStrategy == 4)
 				net[i].setTrainingStrategy(new DETrainingStrategy());
 			
 			
 			trainThreads[t] = new TrainingThread(net[i], set1, samples, epochs, expected1);
 			trainThreads[t].start();
 			
 			// If max number of threads is reached, wait for them to finish before starting a new batch
 			if(t == numThreads - 1){
 				for (TrainingThread thread : trainThreads) {
 					  thread.join();
 				}
 				t = 0;				
 			}
 			else
 				t++;
 		}
 		
 		// Make sure last batch of threads finished before moving on
 		for (TrainingThread thread : trainThreads) {
 			  thread.join();
 		}
 		
 		// Begin testing phase ///////////////////////////////////////
 		correct = error = fail = 0;		
 		
 		// Check each test vector against each class network
 		
 		testThreads = new TestThread[numThreads];
 		t = 0;
 		int[] result = new int[3];
 		for (int a = 0; a < samples; a++){	
 			
 			testThreads[t] = new TestThread(net, set2, expected2, a, numClasses);
 			testThreads[t].start();
 			
 			// If max number of threads is reached, wait for them to finish before starting a new batch
 			if(t == numThreads - 1){
 				for (TestThread thread : testThreads) {
 					  thread.join();
 				}
 				t = 0;
 				for(int k = 0; k < numThreads; k++){
 					result = testThreads[k].getResult();
 					correct += result[0];
 					error += result[1];
 					fail += result[2];
 					testThreads[k] = null;
 				}
 			}
 			else
 				t++;
 			
 		}
 		for (TestThread thread : testThreads) {
 			if(thread != null)
 				thread.join();
 		}
 		for(int k = 0; k < numThreads; k++){
 			if(testThreads[k] != null){
 				result = testThreads[k].getResult();
 				correct += result[0];
 				error += result[1];
 				fail += result[2];
 			}
 		}
 		
 		System.out.println("Correct: " + correct + ", Error: " + error + ", Failed to Class: " + fail);
 		System.out.println("Percent incorrect: " + (((double)(error + fail))/((double)(error + fail + correct)))*100.00 + "%");
 		
 		filescan1.close();
 		keyscan.close();		
 	}
 
 }
