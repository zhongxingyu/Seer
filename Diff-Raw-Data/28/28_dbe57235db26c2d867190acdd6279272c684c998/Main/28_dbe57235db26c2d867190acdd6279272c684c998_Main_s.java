 /**
  * 
  */
 package edu.pw.gis.gui;
 
 import java.awt.Container;
 import java.util.Scanner;
 
 import javax.swing.JFrame;
 
 import edu.pw.gis.algorithm.Genetic;
 import edu.pw.gis.parser.SNDParser;
 
 /**
  * @author ymir
  * 
  */
 public class Main {
 
 
 	static String FILE_PATH = "xml/big_test.xml";
 	static int MAX_WEIGHT = 65535;
 	static int INITIAL_POPULATION = 64;
 	static double ALFA_RATE = 0.2;
 	static double BETA_RATE = 0.7;
 	static double MUTATION_RATE = 0.01;
	static double CROSS_RATE = 0.2;
	static double MAX_USAGE = 0.5;
 	static int MAX_ITERATION_NO = 65535;
 	static JFrame frame = new JFrame();
 
 	public static void main_interface() {
 		Scanner in = new Scanner(System.in);
 		int decision;
 		do {
 			System.out.println("Choose: ");
 			System.out.println("1. Set file path (actual = " + FILE_PATH + ")");
 			System.out.println("2. Set max weight (actual = " + MAX_WEIGHT
 					+ ")");
 			System.out.println("3. Set initial population (actual = "
 					+ INITIAL_POPULATION + ")");
 			System.out.println("4. Set alfa rate (actual = " + ALFA_RATE + ")");
 			System.out.println("5. Set beta rate (actual = " + BETA_RATE + ")");
 			System.out.println("6. Set mutation rate (actual = "
 					+ MUTATION_RATE + ")");
 			System.out.println("7. Set cross rate (actual = " + CROSS_RATE
 					+ ")");
 			System.out.println("8. Set happy max usage (actual = " + MAX_USAGE
 					+ ")");
 			System.out.println("9. Set max iteration number (actual = "
 					+ MAX_ITERATION_NO + ")");
 			System.out.println("10. Run algorithm");
 			System.out.println("0. End program");
 
 			decision = in.nextInt();
 
 			switch (decision) {
 			case 1:
 				System.out.println("File path = ");
 				FILE_PATH = in.next();
 				break;
 			case 2:
 				System.out.println("Max weight (integer value <1,65535>) = ");
 				MAX_WEIGHT = in.nextInt();
 				break;
 			case 3:
 				System.out.println("Initial population (integer value (0,65535>) = ");
 				INITIAL_POPULATION = in.nextInt();
 				break;
 			case 4:
 				System.out.println("Alfa rate (double value <0,1>) = ");
 				ALFA_RATE = in.nextDouble();
 				break;
 			case 5:
 				System.out.println("Beta rate (double value <0,1>) = ");
 				BETA_RATE = in.nextDouble();
 				break;
 			case 6:
 				System.out.println("Mutation rate (double value <0,1>) = ");
 				MUTATION_RATE = in.nextDouble();
 				break;
 			case 7:
 				System.out.println("Cross rate (double value <0,1>) = ");
 				CROSS_RATE = in.nextDouble();
 				break;
 			case 8:
 				System.out.println("Happy max usage (double value) (0,1> = ");
 				MAX_USAGE = in.nextDouble();
 				break;
 			case 9:
 				System.out.println("Max iteration number (integer value (0,65535>) = ");
 				MAX_ITERATION_NO = in.nextInt();
 				break;
 			case 10:
 				run_algorithm();
 				break;
 			case 0:
 				frame.dispose();
 				break;
 			default:
 				System.out
 						.println("-------------------------------------------");
 				System.out.println("Bad choose");
 				System.out
 						.println("-------------------------------------------");
 				break;
 			}
 
 		} while (decision != 0);
 
 		System.out.println("-------------------------------------------");
 		System.out.println("Program ended");
 		System.out.println("-------------------------------------------");
 	}
 
 	public static void run_algorithm() {
 		SNDParser snd_parser = new SNDParser(FILE_PATH);
 		Genetic genetic = new Genetic(MAX_WEIGHT, INITIAL_POPULATION,
 				ALFA_RATE, BETA_RATE, MUTATION_RATE, CROSS_RATE, MAX_USAGE, MAX_ITERATION_NO);
 
 		genetic.createInitialPopulation(snd_parser.graph);
 
 		genetic.go(System.currentTimeMillis());
 
 		edu.pw.gis.graph.Graph top = genetic.graph_list.get(0);
 		genetic.printHighestUsage();
 		System.out.println("Graf wynikowy: ");
 		top.printAdjList();
 		
 //		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Container content = frame.getContentPane();
 		content.add(new PrettyGraph(top));
 		frame.pack();
 		frame.setVisible(true);
 	}
 
 	public static void main(String[] args) throws CloneNotSupportedException {
 		main_interface();
 	}
 
 }
