 package agent;
 
 import java.util.Random;
 
 class Psyche {
 	
 	private final static double MIN_REACTION_T = 3;
 	private final static double MAX_REACTION_T = 10;
 	
 	/**Czas reakcji na  zagroenie = detekcja + decyzja*/
 	double reaction_t;
 	
 	/**Referencja do agenta*/
 	private Agent agent;
 	
 	/**Random number generator*/
 	private Random rand;
 	
 	
	Psyche(Agent _agent){
		rand = new Random();
		this.agent = _agent;
 		reaction_t = (MAX_REACTION_T - MIN_REACTION_T) * rand.nextDouble() + MIN_REACTION_T;
 	}
 
 }
