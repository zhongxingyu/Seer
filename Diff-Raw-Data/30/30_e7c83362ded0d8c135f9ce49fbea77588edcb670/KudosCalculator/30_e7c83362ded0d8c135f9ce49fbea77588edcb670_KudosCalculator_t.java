 package com.jax.teamcraft.pronet;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 
 public class KudosCalculator {
 
 	private static double d = 0.85;
 	private static double delta = 0.00001;
 
 	public Network calculate(Network network) {
 
		while(true) {
 			double highestDelta = 0;
 			// foreach programmer
			for (Programmer p : network.getProgrammers()) {
 				// calculate kudos from his followers
 				double newKudos = calculateScore(p);
 				double oldKudos = p.getKudos();
 				p.setKudos(newKudos);
 				double kudosDelta = Math.abs(oldKudos - newKudos);
 				if (kudosDelta > highestDelta)
 					highestDelta = kudosDelta;
 			}
			if (highestDelta < delta)
 				break;
 		}
 
 		// save the value
 		// repeat until avg ~= 1
 		return network;
 	}
 
 	private double calculateScore(Programmer p) {
 		Collection<Programmer> recomends = p.getRecommendedBy();
 
 		double score = 0;
 		for (Programmer p2 : recomends) {
 			double k = p2.getKudos();
 			double count = p2.getRecommendations().size();
 			score += k / count;
 		}
 		score = d * score + (1 - d);
 
 		return score;
 	}
 
 }
