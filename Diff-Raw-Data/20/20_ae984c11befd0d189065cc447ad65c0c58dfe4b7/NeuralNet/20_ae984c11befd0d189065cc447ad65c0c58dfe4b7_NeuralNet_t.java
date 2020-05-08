 package NerualNet;
 
 import java.util.Scanner;
 import java.io.File; 
 import java.io.IOException;
 
 public class NeuralNet implements Runnable{
 	Network net;
 	double[][] trainSet;
 	int numSamples;
 	int epochs;
 	char[] classes;
 	
 	public NeuralNet(Network net, double[][] trainSet, int numSamples, int epochs, char[] classes){
 		this.net = net;
 		this.trainSet = trainSet;
 		this.numSamples = numSamples;
 		this.epochs = epochs;
 		this.classes = classes;
 	}
 	public void run(){
 		net.train(trainSet, numSamples, epochs, classes);
 	}
 	
 	public static void main(String[] args) throws IOException, InterruptedException {
 		int type, inputs,samples, epochs;
 		
 		int outputs = 26;
 		int layers = 0;
 		int centers = 0;
 		
 		char[] expected;
 		char[] classes;
 		
 		double rate;
 		
 		double[][] set1;
 		double[][] set2;
 		
 		String fileName1;
 		String fileName2;
 		
 		Scanner keyscan;
 		Scanner filescan1;
 		Scanner filescan2;
 		
 		Thread[] threads;
 		
 		Network[] net;
 		
 		File file1;
 		File file2;
 		
		rate = 0.001;
 		
 		keyscan = new Scanner(System.in);
 		
 		
 		System.out.println("Select network type: ");
 		System.out.println("1. MLP");
 		System.out.println("2. RBF");
 		System.out.println("3. ANFIS");
 		
 		type = keyscan.nextInt();
 		
 		if(type == 1){
 			System.out.println("Enter the number of hidden layers: ");
 			layers = keyscan.nextInt();
 		}
 		else if (type == 2){
 			System.out.println("Enter the number of centers: ");
 			centers = keyscan.nextInt();
 		}
 		else if (type == 3){
 			System.out.println("Test ANFIS: ");
 		}
 		
 		System.out.println("Enter number of training epochs: ");
 		epochs = keyscan.nextInt();
 		
 		System.out.println("Enter number of samples: ");
 		samples = keyscan.nextInt();
 		
 		System.out.println("Enter a value for n: ");
 		inputs = keyscan.nextInt();
 		
 		//System.out.println("Enter input file name: ");
 		//fileName1 = keyscan.next();
 		
 		////////
 		fileName1 = "data/letter-recognition.data";
 		////////
 		
 		file1 = new File(fileName1);
 		
 		threads = new Thread[8];
 		
 		filescan1 = new Scanner(file1).useDelimiter(",|\\n");
 		
 		set1 = new double[inputs][samples];
 		
 		classes = new char[outputs];
 		
 		expected = new char[samples];
 		
 		// Read in list of possible classes
 		for (int i = 0; i < outputs; i++){
 			classes[i] = filescan1.next().charAt(0);
 		}
 		
 		// Read in set vectors
 		for (int i = 0; i < samples; i++){
 			expected[i] = filescan1.next().charAt(0);
 			
 			for (int j = 0; j < inputs; j++){
 				set1[j][i] = Integer.parseInt(filescan1.next());
 			}
 		}
 		
 		net = new Network[outputs];
 		
 		int t = 0;
 		
 		for (int i = 0; i < outputs; i++){
 			if(type == 1)
 				net[i] = new MLPNet(inputs, layers, 1, rate, classes[i]);
 			else if (type == 2)
 				net[i] = new RBFNet(inputs, centers, 1, rate, classes[i], samples, set1);
 			
 			threads[t] = new Thread(new NeuralNet(net[i], set1, samples, epochs, expected));
 			threads[t].start();
 			
 			if(t == 7){
 				for (Thread thread : threads) {
 					  thread.join();
 				}
 				t = 0;				
 			}
 			else
 				t++;
 		}
 		
		for (Thread thread : threads) {
			  thread.join();
		}
 		
 		
 		char out;
 		for (int a = 0; a < 100; a++){
 			int responses = outputs;
 			for (int i = 0; i < outputs; i++){
 				out = net[i].process(set1, a, expected[a]);
 				if(out != '!'){
 					if(out == expected[a])
 						System.out.print(out + " ");
 					else						
 						System.out.print("Error(" + out + ") ");
 				}
 				else
 					responses--;
 			}
 			if(responses == 0)
 				System.out.print("Failed to classify");
 			System.out.println();
 		}
 		
			
 		
 		/*
 		for (int a = 0; a < 4; a++){
 			System.out.println("\n**************************************************************************");
 			System.out.println("Train/Validate/Test Round " + (a + 1));
 			fileName1 = "data/n" + inputs + "/" + a;
 			System.out.println("Using file " + fileName1 + " for training");
 			
 			file1 = new File(fileName1);
 			
 			filescan1 = new Scanner(file1);
 			
 			set1 = new double[inputs+outputs][samples];
 			
 			for (int i = 0; i < samples; i++){
 				for (int j = 0; j < inputs+outputs; j++){
 					set1[j][i] = filescan1.nextDouble();
 				}
 			}
 			
 			fileName2 = "data/n" + inputs + "/" + ((a + 1)%4);
 			System.out.println("Using file " + fileName2 + " for validation");
 			
 			file2 = new File(fileName2);
 			
 			filescan2 = new Scanner(file2);
 			
 			set2 = new double[inputs+outputs][samples];
 	
 			for (int i = 0; i < samples; i++){
 				for (int j = 0; j < inputs+outputs; j++){
 					set2[j][i] = filescan2.nextDouble();
 				}
 			}
 			
 			if(type == 1){
 				net = new MLPNet(inputs, layers, outputs, rate);
 			}
 			else if(type == 2){
 				net = new RBFNet(inputs, centers, outputs, rate, samples, set1);
 			}
 			else if(type == 3){
 				net = new ANFISNet(inputs, centers, outputs, rate, samples, set1);
 			}
 			
 			for(int i = 0; i < 20; i++){
 				net.train(set1, samples, (epochs/20));				
 				System.out.println("Average validation error: " + String.valueOf(net.process(set2, samples)) + "%");
 			}
 			
 			filescan1.close();
 			filescan2.close();
 			
 		
 			fileName1 = "data/n" + inputs + "/" + ((a + 2)%4);
 			System.out.println("\nUsing file " + fileName1 + " for testing");
 			
 			file1 = new File(fileName1);
 			
 			filescan1 = new Scanner(file1);
 	
 			for (int i = 0; i < samples; i++){
 				for (int j = 0; j < inputs+outputs; j++){
 					set1[j][i] = filescan1.nextDouble();
 				}
 			}
 			
 			System.out.println("Average test error: " + String.valueOf(net.process(set1, samples)) + "%");
 			
 			fileName1 = "data/n" + inputs + "/" + ((a + 3)%4);
 			System.out.println("\nLeaving out file " + fileName1 + " for this round");
 						
 			filescan1.close();
 		}*/
 		
 		filescan1.close();
 		
 		keyscan.close();		
 	}
 
 }
