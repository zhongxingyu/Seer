 package sets.lines;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.Scanner;
 
 import library.GPConfig;
 import library.GeneticProgram;
 import library.NodeFactory;
import library.ParallelFitness;
 import library.Population;
import library.ProgramGenerator;
 
 public class Main {
 
 	private static void enlarge(String args[], GPConfig c) {
 		if (args.length != 2) {
 			System.err
 					.println("Incorrect Usage\nTakes either a population archive, or a pnm output file and a new size\n");
 			return;
 		}
 		String file = args[0];
 		int size = Integer.parseInt(args[1]);
 		Scanner scan;
 		try {
 			scan = new Scanner(new File(file));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new RuntimeException("File cannot be found to enlarge");
 		}
 		String line;
 		scan.nextLine();// getline(image, line);
 		scan.nextLine();// ()getline(image,line);
 		line = scan.nextLine().substring(1);
 		GeneticProgram p = new GeneticProgram(c);
 		p.parseProgram(line);
 		((ImageFitness) (c.fitnessObject)).getResult(p, size);
 	}
 
 	public static void main(String[] args) {
 
 		//Create the setting for a new GP run
 		//1 root node, min program depth 1, and max depth 8
 		GPConfig symConfig = new GPConfig(1,1,8,0.7,0.28,0.02);
 	
 		// Declare a population, giving the size and a log file name
 		Population pop = new Population(100, 100, "run-log.txt", symConfig);
 
 		// Set the return type for our programs
 		pop.setReturnType(ReturnImage.TYPENUM);
 
 
 		// Write out the population every N generations
 		pop.setLogFrequency(100000);
 
 		// Add the terminals we need
 		symConfig.termSet.addNodeToSet(ReturnImage.TYPENUM, new Null(symConfig));
 		symConfig.termSet.addNodeToSet(ReturnColor.TYPENUM, new SetColor(symConfig));
 
 		// Add the functions we need
 		symConfig.funcSet.addNodeToSet(ReturnImage.TYPENUM, new Line(symConfig));
 		
 		// Set the fitness class to be used
 
 //		symConfig.fitnessObject = new ParallelFitness<ImageFitness>(symConfig,new ImageFitness(symConfig));
 		 symConfig.fitnessObject = new ImageFitness(symConfig);
 		// Initialise the fitness
 		symConfig.fitnessObject.initFitness();
 
 		// symConfig.selectionOperator = new TournamentSelection(5);
 
 		if (args.length == 2) {
 			symConfig.fitnessObject = new ImageFitness(symConfig);
 			enlarge(args, symConfig);
 			System.exit(0);
 		}
 
 		if (args.length == 1) {
 			// If there is one command line argument assume its a pop
 			// file name
 			// Reading a population from a file
 			try {
 				pop.readFromFile(args[0]);
 			} catch (Exception err) {
 				err.printStackTrace();
 				System.exit(1);
 			}
 		} else {
 			pop.generateInitialPopulation();
 		}
 
 		try {
 			StringBuffer str1 = new StringBuffer();
 
 			/* Do 20 generations, returns true if solution is found */
 
 			if (pop.evolve(20)) {
 				System.out.println("Found solution");
 			} else {
 				System.out.println("Didn't find solution");
 			}
 			NodeFactory.report();
 			pop.getBest().print(str1);
 			System.out.println("Best program");
 			System.out.println("Fitness " + pop.getBest().getFitness());
 			System.out.println(str1);
 
 			// cout << "Writing results to out.txt..." << endl;
 
 //			((ParallelFitness<ImageFitness>)(symConfig.fitnessObject)).fitness.getResult(pop.getBest(),100);
 //			((ParallelFitness<ImageFitness>)(symConfig.fitnessObject)).finish();
 			((ImageFitness)(symConfig.fitnessObject)).getResult(pop.getBest(),100);
 			// cout <<"Results written" <<endl;
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Exiting");
 			System.exit(1);
 		}
 
 		// The following code executes the best program on the training data
 		// and outputs the results of the execution to the file results.txt
 
 		System.out.println("Finished");
 	}
 
 }
