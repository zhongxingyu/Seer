 package com.cse454.nel;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.cse454.nel.features.FeatureWeights;
 
 public class FeatureWeightScorer {
 
 	private Map<FeatureWeights, Double> scores;
 
 	public void addFeatureWeightScore(FeatureWeights weights, double score) {
 		scores.put(weights, score);
 	}
 
 	public static double score(String[] gold, String[] entities) {
 		List<String> goldEnts = new ArrayList<>();
 		List<String> ents = new ArrayList<>();
 		for (String g : gold) {
 			if (!g.equals("0")) {
 				goldEnts.add(g);
 			}
 		}
 		for (String ent : entities) {
 			if (!ent.equals("0") && goldEnts.contains(ent)) {
 				ents.add(ent);
 			}
 		}
 		double numerator = ents.size(); // the entities we get right
 		double denominator = goldEnts.size(); // the total amount of entities
		return numerator / denominator;
 	}
 }
