 package JNeuralNet;
 
 import java.util.ArrayList;
 
 public class RobotPopulation implements Population {
 	Robot[] individuals;
 	int ppg;
 	
 	RobotPopulation(int size, int parentsPerGeneration) {
 		if(parentsPerGeneration > size) {
 			System.err.println("More parents than population!! Cannot proceed.");
 			System.exit(-1);
 		}
 		if((1.0*size) / (1.0*parentsPerGeneration)%1.0 != 0) {
 			System.err.println("Population size is not evenly divisible among parents. Cannot proceed.");
 			System.exit(-1);
 		}
 		individuals = new Robot[size];
 		ppg = parentsPerGeneration;
 		for(int i=0; i<size; i++) {
 			individuals[i] = new Robot(new RobotGenome());
 		}
 	}
 	
 	public void run(int cyclesPerGeneration, int generations) throws Exception {
 		for(int i=0; i<generations; i++) {
 			for(int j=0; j<cyclesPerGeneration; j++) {
 				for(int k=0; k<individuals.length; k++) {
					System.out.println("Calling individuals[i].tick()");
 					individuals[k].tick();
 				}
 			}
 			nextGeneration();
 		}
 	}
 	
 	public Robot[] getIndividuals() {
 		return individuals;
 	}
 
 	void nextGeneration() {
 		Robot[] sorted = sortIndividuals();
 		RobotGenome[] children = new RobotGenome[individuals.length];
 		for(int i=0, j=ppg-1; i<ppg; i++, j--) {
 			RobotGenome[] subchldrn = sorted[i].mate(sorted[j].getGenome(), individuals.length/ppg);
 			for(int k=0; k<individuals.length/ppg; k++) {
 				children[i*(individuals.length/ppg)+k] = subchldrn[k];
 			}
 		}
 	}
 	
 	Robot[] sortIndividuals() {
 		ArrayList<Robot> rv = new ArrayList<Robot>();
 		for(int i=0; i<individuals.length; i++) {
 			int index = 0;
 			while(rv.size() > index && rv.get(index).getScore() > individuals[i].getScore()) {
 				index++;
 			}
 			rv.add(index, individuals[i]);
 		}
 		return (Robot[])rv.toArray();
 	}
 }
