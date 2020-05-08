 import java.util.ArrayList;
 import java.util.BitSet;
 
 /**
  * Contains useful utility methods
  * @author Jervis
  *
  */
 public class Util {
 
 	public static void getAllSubsets(LogicalAndTerm terms) throws Exception {
 		int setSize = terms.size();  
 		long maxLimit = 1 << setSize;  
 		BitVector bv = new BitVector(setSize);		
 		ArrayList<LogicalAndTerm> result = new ArrayList<LogicalAndTerm>(); 
 		for (long i = 0; i <= maxLimit; ++i) {
 			bv.increment(); 
 			BitSet bs = bv.getBitsSet(); 
 			LogicalAndTerm subset = new LogicalAndTerm();
 			for (int j = 0; j < bs.size(); ++j) {
 				if(bs.get(j)) {
 					subset.add(terms.get(j));
 				}
 			}
 			result.add(subset);
 		}
 	}
 	
 	/**s
 	 * Gets a list of of selection queries/conditions returned in a 
 	 * format of LogicalAndTerms.
 	 * @param queries - list of queries to process
 	 * @return
 	 */
 	public static ArrayList<LogicalAndTerm> getBasicTerms(
 			ArrayList<double[]> queries) {
 		int count = 1;
 		ArrayList<LogicalAndTerm> result = new ArrayList<LogicalAndTerm>();
 		for (double[] query : queries) {
 			LogicalAndTerm lat = new LogicalAndTerm();
 			for (double selectivity : query) {
 				String functionName = String.format("t%d", count);
 				String arg = String.format("o%d[i]", count);
 				BasicTerm term = new BasicTerm(functionName, arg, selectivity);
 				lat.add(term);
				++count;
 			}
 			if (lat.size() > 0)
 				result.add(lat);
 		}
 		return result;
 	}
 }
