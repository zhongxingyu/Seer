 package com.tinkerpop.bench.evaluators;
 
import com.tinkerpop.bench.cache.Cache;
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.Graph;
 
 /**
  * @author Alex Averbuch (alex.averbuch@gmail.com)
  * @author Martin Neumann (m.neumann.1980@gmail.com)
  */
 public abstract class EdgeEvaluator {
 
 	private double total = -1;
 
 	public double evaluateTotal(Graph db) {
 		if (total != -1)
 			return total;
 
 		total = 0d;
 		Cache cache = Cache.getInstance(db);
 		int max = cache.getEdgeIndexRange();
 		
 		for (int index = 0; index < max; index++) {
 			if (cache.getEdgeID(index) == null) continue;
 			total += evaluate(cache, index);
 		}
 
 		return total;
 	}
 
 	public abstract double evaluate(Cache cache, int index);
 }
