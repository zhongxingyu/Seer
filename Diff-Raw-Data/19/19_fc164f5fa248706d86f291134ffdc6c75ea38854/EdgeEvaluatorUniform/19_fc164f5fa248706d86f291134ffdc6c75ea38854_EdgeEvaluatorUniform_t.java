 package com.tinkerpop.bench.evaluators;
 
import com.tinkerpop.bench.cache.Cache;
 import com.tinkerpop.blueprints.pgm.Edge;
 
 /**
  * @author Alex Averbuch (alex.averbuch@gmail.com)
  * @author Martin Neumann (m.neumann.1980@gmail.com)
  */
 public class EdgeEvaluatorUniform extends EdgeEvaluator {
 
 	@Override
 	public double evaluate(Cache cache, int index) {
 		return 1d;
 	}
 }
