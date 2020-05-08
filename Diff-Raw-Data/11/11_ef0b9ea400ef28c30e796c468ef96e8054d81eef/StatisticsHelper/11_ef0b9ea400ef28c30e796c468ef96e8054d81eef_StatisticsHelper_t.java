 package com.tinkerpop.bench;
 
 import java.util.Random;
 
 import com.tinkerpop.bench.cache.Cache;
 import com.tinkerpop.bench.evaluators.EdgeEvaluator;
 import com.tinkerpop.bench.evaluators.Evaluator;
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.Graph;
//import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.hollow.HollowGraph;
 
 /**
  * @author Marko A. Rodriguez (http://markorodriguez.com)
  * @author Alex Averbuch (alex.averbuch@gmail.com)
  * @author Martin Neumann (m.neumann.1980@gmail.com)
  */
 public class StatisticsHelper {
 
 	private static Random rand = new Random(42);
 	private static long time = -1l;
 	private static long memory = -1l;
 
 	public static Object[] getSampleVertexIds(Graph db, Evaluator evaluator,
 			int sampleSize) {
 
 		Object[] samples = new Object[sampleSize];
		if (db instanceof HollowGraph) {
			for (int i = 0; i < samples.length; i++) {
				samples[i] = new Long(0);
			}
			return samples;
		}
		
 		Double[] sampleVals = new Double[sampleSize];
 
 		double totalVal = evaluator.evaluateTotal(db);
 
 		for (int i = 0; i < sampleVals.length; i++) {
 			sampleVals[i] = rand.nextDouble() * totalVal;
 			samples[i] = null;
 		}
 
 		boolean finished = true;
 		Cache cache = Cache.getInstance(db);
 		int max = cache.getVertexIndexRange();
 		
 		for (int index = 0; index < max; index++) {
 
 			Object id = cache.getVertexID(index);
 			if (id == null) continue;
 			
 			double currentVal = evaluator.evaluate(cache, index);
 
 			finished = true;
 
 			for (int i = 0; i < sampleVals.length; i++) {
 				if (samples[i] == null) {
 					sampleVals[i] -= currentVal;
 					if (sampleVals[i] <= 0)
 						samples[i] = id;
 					else
 						finished = false;
 				}
 			}
 
 			if (finished == true)
 				break;
 		}
 		
 		if (!finished) throw new RuntimeException("getSampleVertexIds() did not finish");
 		for (int i = 0; i < sampleVals.length; i++) {
 			if (samples[i] == null)
 				throw new RuntimeException("getSampleVertexIds() did not finish - some elements are null");
 		}
 
 		return samples;
 	}
 	
 	public static Object[] getSampleEdgeIds(Graph db, EdgeEvaluator evaluator,
 			int sampleSize) {
 
 		Object[] samples = new Object[sampleSize];
 		Double[] sampleVals = new Double[sampleSize];
 
 		double totalVal = evaluator.evaluateTotal(db);
 
 		for (int i = 0; i < sampleVals.length; i++) {
 			sampleVals[i] = rand.nextDouble() * totalVal;
 			samples[i] = null;
 		}
 
 		boolean finished = true;
 
 		for (Edge currentEdge : db.getEdges()) {
 
 			double currentVal = evaluator.evaluate(currentEdge);
 
 			finished = true;
 
 			for (int i = 0; i < sampleVals.length; i++) {
 				if (samples[i] == null) {
 					sampleVals[i] -= currentVal;
 					if (sampleVals[i] <= 0)
 						samples[i] = currentEdge.getId();
 					else
 						finished = false;
 				}
 			}
 
 			if (finished == true)
 				break;
 		}
 
 		return samples;
 	}
 
 	public static long stopWatch() {
 		if (time == -1l) {
 			time = System.nanoTime();
 			return time;
 		} else {
 			long temp = System.nanoTime() - time;
 			time = -1l;
 			return temp;
 		}
 	}
 	
 	public static long stopMemory() {
 		final Runtime rt = Runtime.getRuntime();
 		if (memory == -1) {
 			memory = rt.totalMemory() - rt.freeMemory();
 			return memory;
 		} else {
 			long temp = rt.totalMemory() - rt.freeMemory() - memory;
 			memory = 1l;
 			return temp;
 		}
 	}
 }
