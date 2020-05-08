 package Online;
 
 import ilog.concert.IloException;
 import ilog.concert.IloNumExpr;
 import ilog.concert.IloNumVar;
 import ilog.cplex.IloCplex;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 public class Count {
 	public static long process(ResultSet result, int sample_size, int db_size,
 			int target_value) throws SQLException {
 
 		HashMap<Integer, Integer> frequencies = new HashMap<Integer, Integer>();
 		Integer k;
 		Integer v;
 
 		long count = 0;
 
 		while (result.next()) {
 			k = new Integer(result.getInt("value"));// sum first column
 			if (frequencies.containsKey(k)) {
 				v = frequencies.get(k);
 				frequencies.put(k, new Integer(++v));
 			} else
 				frequencies.put(k, new Integer(1));
 
 		}
 
 		Integer frequency = frequencies.get(target_value);
 		// estimate
 		if (frequency != null)
 			count = frequency * db_size / sample_size;
 		return count;
 
 	}
 
 	
	// select count(value) from bigdata where value=10
	public static double[] calculateSumConfidenceInterval(ResultSet resultSet,
 			int sampleSize, int dbSize, double epsilon, int target_value) {
 		double[] bounds = new double[2];
 		CplexSolution solution_min = null;
 		CplexSolution solution_max = null;
 
 		HashMap<Integer, Integer> frequencies = new HashMap<Integer, Integer>();
 		HashMap<Integer, Float> selectivities = new HashMap<Integer, Float>();
 
 		double eta = (double) dbSize / ((double) sampleSize);
 		double query_selectivity = 0;
 
 		Integer k;
 		Integer v;
 
 		int min = 10000, max = 0;
 
 		try {
 			while (resultSet.next()) {
 				k = new Integer(resultSet.getInt("value"));
 				// update max and min
 				if (k.intValue() > max)
 					max = k.intValue();
 				else if (k.intValue() < min)
 					min = k.intValue();
 
 				if (frequencies.containsKey(k)) // already in the list
 				{
 					v = frequencies.get(k);
 					v += 1;
 
 					frequencies.put(k, new Integer(v));
 				} else {
 					frequencies.put(k, new Integer(1));
 				}
 			}
 
 			Iterator<Map.Entry<Integer, Integer>> it = frequencies.entrySet()
 					.iterator();
 			while (it.hasNext()) {
 				Entry<Integer, Integer> entry = (Entry<Integer, Integer>) it
 						.next();
 				selectivities
 						.put(entry.getKey(), new Float((float) entry.getValue()
 								/ (float) sampleSize));
 				query_selectivity += (float) entry.getValue()
 						/ (float) sampleSize;
 			}
 
 			double lowerBound, upperBound;
 			Float target_selectivity = selectivities.get(target_value);
 			if (target_selectivity == null) {
 				bounds[0] = 0;
 				bounds[1] = 0;
 				return bounds;
 			}


 			solution_max = CountSolver.solveCount(target_value, query_selectivity, selectivities, target_selectivity, eta, epsilon, true);
 			solution_min = CountSolver.solveCount(target_value, query_selectivity, selectivities, target_selectivity, eta, epsilon, false);
 			
 		
 			bounds[0] = solution_min.objective_value * dbSize;
 			bounds[1] = solution_max.objective_value * dbSize;
 
 		} catch (Exception e) {
 			e.printStackTrace(System.out);
 			// System.out.println(e.getMessage());
 		}
 
 		return bounds;
 
 	}
 }
