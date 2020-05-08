 package com.scarytom;
 
 import java.util.Map;
 import java.util.Set;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.scarytom.pronet.Programmer;
 
 public class UndirectedGraphBuilder {
 	private final Map<Programmer, Set<Programmer>> recommendees = Maps.newHashMap();
 	
 	public UndirectedGraphBuilder(Set<Programmer> programmers) {
 		for (Programmer recommender : programmers) {
 			Set<Programmer> recommendations = recommender.recommendations();
 			for (Programmer recommendee : recommendations) {
 				Set<Programmer> current = recommendees.get(recommendee);
 				if (current == null) {
 					current = Sets.newHashSet();
 					recommendees.put(recommendee, current);
 				}
 				current.add(recommender);
 			}
 		}
 	}
 
 	public Set<Programmer> whoRecommends(Programmer programmer) {
		Set<Programmer> result = recommendees.get(programmer);
		if(result==null){
			return Sets.newHashSet();
		}
		return result;
 	}
 }
