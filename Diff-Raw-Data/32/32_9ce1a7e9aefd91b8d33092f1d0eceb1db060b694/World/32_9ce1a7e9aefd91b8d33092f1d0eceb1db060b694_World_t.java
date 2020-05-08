 import java.util.Random;
 
 
 public class World {
 
 	Population agents;
 	Random randGen;
 
 	public World() {
 		agents = new Population( 10, 3, new int[] {2,1} );
 		randGen = new Random();
 	}
 
 	public void trainSum() {
 		for ( int i = 0; i < 1000; i++ ) {
 			for ( int id : agents.IDs() ) {
 				double[] question = {randGen.nextInt(10), randGen.nextInt(10), randGen.nextInt(10)};
 				double agentsAnswer = agents.askDenizen(id, question)[0];
 				double realAnswer = question[0]+question[1]+question[2];
				agents.giveReward(id, 1/Math.pow(2, Math.abs(agentsAnswer-realAnswer)));
 			}
 			agents.generation();
 		}
 	}
 
 
 
 }
