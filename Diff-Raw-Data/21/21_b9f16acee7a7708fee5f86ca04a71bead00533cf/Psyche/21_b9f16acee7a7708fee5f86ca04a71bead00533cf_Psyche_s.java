 package edu.agh.tunev.model.kkm;
 
 import java.util.Random;
 
 class Psyche {
 
	private final static double MIN_REACTION_T = 0.1 * 1000; // [ms]
	private final static double MAX_REACTION_T = 1 * 1000; // [ms]
 
 	private final static double ANXIETY_COEFF_SIGNIF = 0.2;
 	private final static double MIN_THREAT_COMP = 35 * Agent.TEMP_THREAT_COEFF;
 
 	/** Czas reakcji na zagrożenie = detekcja + decyzja */
 	double reaction_t;
 
 	/**
 	 * Poddenerwowanie agenta. Standardowo 1, jeśli nie ma zagrożenia;
 	 */
 	double anxiety;
 
 	/** Wsp. poddenerowawania, cecha osobnicza */
 	private double anxiety_coeff;
 
 	/** Random number generator */
 	private Random rand;
 
 	Psyche(Agent _agent) {
 		rand = new Random();
 		reaction_t = (MAX_REACTION_T - MIN_REACTION_T) * rand.nextDouble()
 				+ MIN_REACTION_T;
 		anxiety = 1;
 		anxiety_coeff = 1 + rand.nextDouble() * ANXIETY_COEFF_SIGNIF;
 	}
 
 	void expAnxiety(double threat_comp) {
 		if (threat_comp > MIN_THREAT_COMP)
 			anxiety = anxiety_coeff * threat_comp;
 		else
 			anxiety = 1;
 	}
 }
