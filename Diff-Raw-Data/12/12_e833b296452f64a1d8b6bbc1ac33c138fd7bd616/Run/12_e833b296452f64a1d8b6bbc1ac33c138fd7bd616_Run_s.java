 package test;
 
 import java.util.Scanner;
 
 public class Run {
 
 	private int generationPool = 40;
 	private int adultPool = 20;
 	private int bitSize = 50;
 	private int adultProtocol = 3;
 	private int parentProtocol = 2;
 	private int problemId = 3;
	private int fitnessEvaluationMethod = 2;
 	private double mutateRate = 0.05;
 	static boolean finished = false;
 	static double BESTOVERALLFITNESS = 0;
 	
 
 	public Run() {
 		// init();
 		simulateEA();
 	}
 
 	public void init() {
 		Scanner sc = new Scanner(System.in);
 		System.out.println("Welcome");
 		System.out.println("Enter problem id\nOneMax = 0\nAdvencedOneMax = 1\nColonelBlotto = 2");
 		problemId = sc.nextInt();
 		if (problemId == 2) {
 			System.out.println("Enter bitSize, have to be dividable by 4:");
 			bitSize = sc.nextInt();
 			while (bitSize % 4 != 0) {
 				System.out.println("Enter bitSize, have to be dividable by 4:");
 				bitSize = sc.nextInt();
 			}
 		} else {
 			System.out.println("Enter bitSize:");
 			bitSize = sc.nextInt();
 		}
 		System.out.println("Enter childern size, have to be dividable by 2:");
 		generationPool = sc.nextInt();
 		while (generationPool % 2 != 0) {
 			System.out.println("Enter childern size, have to be dividable by 2:");
 			generationPool = sc.nextInt();
 		}
 		System.out.println("Enter adult size:");
 		adultPool = sc.nextInt();
 		System.out.println("Enter adult protocol\nRandom = 0\nOver production = 1\nFull generational replacement = 2\nGenerational mixing = 3");
 		adultProtocol = sc.nextInt();
 		System.out.println("Enter parent protocol\nRandom = 0\nFitness proportionate = 1\nSigma-scaling = 2\nTournament selection = 3\nStochastic uniform selection = 4");
 		parentProtocol = sc.nextInt();
 		System.out.println("Enter mutate rate, eks 0,01:");
 		mutateRate = sc.nextDouble();
 	}
 
 	public void simulateEA() {
 
 		// Declare populations
 		PopulationChildren populationChildren = new PopulationChildren();
 		PopulationAdults populationAdults = new PopulationAdults(adultPool);
 		PopulationParent populationParent = new PopulationParent(generationPool);
 		// Declare helper classes
 		Development development = new Development(populationChildren, generationPool, bitSize, mutateRate, problemId);
 		FitnessTesting fitnessTesting = new FitnessTesting(populationChildren, problemId,fitnessEvaluationMethod);
 		Reproduction reproduction = new Reproduction(populationParent);
 
 		int i = 0;
 		while (!finished && i< 2000) {
 
 			i++;
 			System.out.println("\nGeneration: " + i);
 			if (i == 1) {
 				development.createFirstGeneration();
 			} else {
 				development.develop(reproduction.getGenotypes());
 			}
 
 			fitnessTesting.evaluateFitness();
 
 			populationAdults.adultSelection(populationChildren, adultProtocol);
 
 			populationParent.parentSelection(populationAdults, parentProtocol);
 
 			reproduction.run();
 
 			System.out.println(populationAdults);
 
 		}
 		System.out.println("\nDone at generation: " + i);
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		new Run();
 
 	}
 
 }
