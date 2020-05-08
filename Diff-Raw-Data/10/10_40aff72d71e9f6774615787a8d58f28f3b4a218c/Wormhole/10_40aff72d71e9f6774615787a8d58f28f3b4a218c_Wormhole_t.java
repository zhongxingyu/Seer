 import java.awt.List;
 import java.io.*;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Random;
 
 /**
  * class Wormhole
  * Main class
  */
 public class Wormhole {
 	public static final String DEFAULT_DIRNAME_M = "Male/";		//default directory names
 	public static final String DEFAULT_DIRNAME_F = "Female/";
 	public static final String DEFAULT_DIRNAME_T = "Test/";
 	public static final double MAX_ERROR = 4.0;					//10female.txt works at 4.0
 	public static final int MAX_ITERATIONS = 1000;				//it would take 2 hours for this to happen... 
 	public static final int NUM_FOLDS = 5;						//number of folds to use in cross validation
 	
 	private static NeuralNet NN;
 	private static LinkedList<Face> trainSet;
 	private static LinkedList<Face> testSet;
 	
 	private static int countMale;
 	private static int countFemale;
 	private static int countTest;
 	
 	/**
 	 * Prints usage instructions.
 	 */
 	public static void helpPrinter() {
 		System.out.println("  Command Line Parameters are as follows:");
 		System.out.println("    \"--help\" : You're looking at it");
 		System.out.println("    \"-train\" : Train the network with data");
 		System.out.println("      Example: -train");
 		System.out.println("    \"-test\" : Test on an set of unknown data");
 		System.out.println("      Example: -test");
 		System.out.println("    \"-m [directory]\" : Set male image directory (default: Male/)");
 		System.out.println("      Example: -m male_imgs");
 		System.out.println("    \"-f [directory]\" : Set female image directory (default: Female/)");
 		System.out.println("      Example: -f female_imgs");
 		System.out.println("    \"-t [directory]\" : Set test image directory (default Test/)");
 		System.out.println("      Example: -t test_imgs");
 		System.out.println("Note: Later command-line options override earlier ones if they are incompatable\n");
 	}
 	
 	/**
 	 * Main function. Checks parameters and initializes program.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// Default folder names
 		String dirNameMale = DEFAULT_DIRNAME_M;
 		String dirNameFemale = DEFAULT_DIRNAME_F;
 		String dirNameTest = DEFAULT_DIRNAME_T;
 		boolean train = false;
 		boolean test = false;
 		boolean fold = false;
 		
 		countMale = 0;
 		countFemale = 0;
 		countTest = 0;
 		
 		// Parse through the command line arguments
 		try
 		{
 			int i = 0;
 			while(i < args.length)
 			{
 				if(args[i].equalsIgnoreCase("-m")) {
 					//override the default male directory name
 					dirNameMale = args[i + 1];
 					if(getContents(dirNameMale) == null) {
 						throw new IllegalArgumentException(dirNameMale + " is not a valid directory.");
 					}
 					i++;
 				} else if(args[i].equalsIgnoreCase("-f")) {
 					//override the default female directory name
 					dirNameFemale = args[i + 1];
 					if(getContents(dirNameFemale) == null) {
 						throw new IllegalArgumentException(dirNameFemale + " is not a valid directory.");
 					}
 					i++;
 				} else if(args[i].equalsIgnoreCase("-t")) {
 					//override the default test directory name
 					dirNameTest = args[i + 1];
 					if(getContents(dirNameTest) == null) {
 						throw new IllegalArgumentException(dirNameTest + " is not a valid directory.");
 					}
 					i++;
 				} else if(args[i].equalsIgnoreCase("-fold")) {
 					train = true;
 					fold = true;
 				} else if(args[i].equalsIgnoreCase("-train")) {
 					train = true;
 				} else if(args[i].equalsIgnoreCase("-test")) {
 					test = true;
 				} else if(args[i].equalsIgnoreCase("--help")){
 					helpPrinter();
 					System.exit(0);
 				} else {
 					throw new IllegalArgumentException();
 				}
 				
 				i++;
 			}
 		} catch(IllegalArgumentException ia) {
 			System.err.println("Invalid Arguments: " + ia.getMessage());
 			System.exit(4);
 		}
 		
 		try {
 			NN = new NeuralNet();
 			
 			if(train) {
 				trainSet = new LinkedList<Face>();
 				read(dirNameMale,Face.Facetype.male,trainSet);
 				read(dirNameFemale,Face.Facetype.female,trainSet);
 				trainSet = shuffle(trainSet); // interleave males and females
 				NN.initialize();
 				
 				if(fold) {
 					fold();
 				} else {
 					train();
 				}
 			}
 			
 			if(test) {
 				testSet = new LinkedList<Face>();
 				read(dirNameTest,Face.Facetype.test,testSet);
 				test();
 			}
 		} catch(IOException e) {
 			System.err.println("Invalid Image Directory: " + e.getMessage());
 			System.exit(4);
 		}
 	}
 	
 	/**
 	 * Gets and returns the valid face files of the given directory name.
 	 * @param dirName
 	 */
 	public static String[] getContents(String dirName) {
 		File dir = new File(dirName);
 
 		FilenameFilter filter = new FilenameFilter() {
 			public boolean accept(File dir, String name) {
 				//only list non-hidden .txt files
 				return !name.startsWith(".") && name.endsWith(".txt");
 			}
 		};
 		
 		return dir.list(filter);
 	}
 	
 	/**
 	 * Reads and stores the valid face files of the given directory name.
 	 * @param dirName
 	 * @throws IOException
 	 */
 	public static void read(String dirName, Face.Facetype type, LinkedList<Face> set) throws IOException {
 		String[] images = getContents(dirName);
 		
 		if(images == null) {
 			throw new IOException(dirName);
 		}
 		
 		for(String img : images) {
 			img = dirName + "/" + img;
 			try {
 				set.add(new Face(img,readFile(img),type));
 				switch(type) {
 					case female:
 						countFemale++;
 						break;
 					case male:
 						countMale++;
 						break;
 					case test:
 						countTest++;
 						break;
 					default:
 						break;
 				}
 			} catch(Exception e) {
 				System.err.println("Invalid Image File: " + e.getMessage());
 				System.exit(4);
 			}
 		}
 	}
 	
 	/**
 	 * Efficiently read file and return string.
 	 * http://stackoverflow.com/questions/326390/how-to-create-a-java-string-from-the-contents-of-a-file
 	 * @param path
 	 * @throws IOException
 	 */
 	public static String readFile(String path) throws IOException {
 		FileInputStream stream = new FileInputStream(new File(path));
 		try {
 			FileChannel fc = stream.getChannel();
 			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
 			/* Instead of using default, pass in a decoder. */
 			return Charset.defaultCharset().decode(bb).toString();
 		}
 		finally {
 			stream.close();
 		}
 	}
 	
 	/**
 	 * Train the network with the trainSet.
 	 */
 	private static void train() {
 		double error = 0.0;
 		int i = 0;
 		
 		System.out.println("Training neural network on " +
 				countMale +" male faces and " +
 				countFemale + " female faces:");
 		System.out.println("\ti\terror");
 		
 		do {
 			error = 0.0;
 			for(int j = 0; j < trainSet.size(); j++) {
 				NN.forwardPropagate(trainSet.get(j));
 				NN.backPropagate();
 				error += NN.getError();
 			}
 			i++;
 			
 			System.out.println("\t"+i+"\t"+error);
 		} while(error > MAX_ERROR && i < MAX_ITERATIONS);
 	}
 	
 	/**
 	 * Use 5-fold cross evaluation to train the network with the trainSet.
 	 */
 	private static void fold() {
 		int i = 0;
 		int partSize = trainSet.size() / NUM_FOLDS;
 		int testSize = partSize + trainSet.size() % NUM_FOLDS;
 		LinkedList<Double> errorT = new LinkedList<Double>();
 		LinkedList<Double> errorV = new LinkedList<Double>();
 		
 		System.out.println("Training neural network (with " + NUM_FOLDS + "-fold cross-validation) on " +
 				countMale +" male faces and " + countFemale + " female faces:");
 		System.out.println("\ti\terror");
 		
 		while(true) {
 			double et = 0.0;
 			double ev = 0.0;
 			
 			for(int fold = 0; fold < NUM_FOLDS; fold++){
 				LinkedList<Face> train = new LinkedList<Face>();
 				LinkedList<Face> test = new LinkedList<Face>();
 				partition(trainSet,train,test,fold,partSize,testSize);
 				
 				for(int j = 0; j < train.size(); j++) {
 					NN.forwardPropagate(train.get(j));
 					NN.backPropagate();
 					et += NN.getError();
 				}
 				
 				for(int j = 0; j < test.size(); j++) {
 					NN.test(test.get(j));
 					ev += NN.getError();
 				}
 			}
 			
 			errorT.add(i,et);
 			errorV.add(i,ev);
 			
 			if(et < MAX_ERROR || i > MAX_ITERATIONS) {
 				
 			}
 			
 			i++;
 		}
 	}
 	
	/**
	 * Partition set into a test set and a training set.
	 */
	@SuppressWarnings("unchecked") //Java complains when you use a cast on a clone()'d object.
 	private static void partition(LinkedList<Face> set,LinkedList<Face> train,LinkedList<Face> test,int fold,int partSize, int testSize) {
 		train = (LinkedList<Face>) set.clone();
 		for(int i = fold * partSize; i < fold * partSize + testSize; i++) {
 			test.add(set.get(i));
 			train.remove(i);
 		}
 	}
 	
 	/**
 	 * Test the network on the testSet.
 	 */
 	private static void test() {
 		System.out.println("Testing " + countTest + " faces:");
		System.out.println("\ti\tname\t\t\t\t\t\t\t\t\t\test. sex\terror");
 		for(int i = 0; i < testSet.size(); i++) {
			System.out.println("\t"+i+"\t"+testSet.get(i)+"\t"+NN.test(testSet.get(i))+"\t"+NN.getError());
 		}
 	}
 	
 	/**
 	 * Fisher-Yates Shuffle 
 	 */
 	private static LinkedList<Face> shuffle(LinkedList<Face> set) {
 		Random rand = new Random();
 		Face temp;
 		for(int i = set.size() - 1; i > 0; i--) {
 			int j = rand.nextInt(i);
 			temp = set.get(j);
 			set.set(j,set.get(i));
 			set.set(i,temp);
 		}
 		return set;
 	}
 	
 	
 }
