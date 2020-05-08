 package JNeuralNet;
 public class RobotMain {
 	static final int DEFAULT_POP_SIZE = 12;
 	static final int DEFAULT_PPG = 4;
 	static final int DEFAULT_TICKS_PER_GEN = 1000;
 	static final int DEFAULT_NUM_GENERATIONS = 50;
 	public static void main (String args[]) {
 		int size = DEFAULT_POP_SIZE, ppg = DEFAULT_PPG, 
 			numGenerations = DEFAULT_NUM_GENERATIONS, ticksPerGeneration = DEFAULT_TICKS_PER_GEN;
 
 		switch(args.length) {
 		case 4:
 			ticksPerGeneration = Integer.parseInt(args[3]);
 		case 3:
 			numGenerations = Integer.parseInt(args[2]);
 		case 2:
 			size = Integer.parseInt(args[1]);
 		case 1:
 			ppg = Integer.parseInt(args[0]);
 			break;
 		case 0:
 			System.err.println("USAGE: not done yet.");
 		default:
 		}
 
 		if(args.length > 4) {
 			System.err.println("Expected at most 4 arguments, ignored the rest.");
 		}
 		RobotPopulation pop = new RobotPopulation(size, ppg);
 		try {
 			pop.run(ticksPerGeneration,numGenerations);
 		} catch (Exception e) {
 			System.err.println("Exception thrown by pop.run(): "+e.toString());
 		}
 	}
 }
 
