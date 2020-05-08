 package com.bananity.util;
 
 /**
  *  This class encapsulates many methods to calculate the Jaccard Distance
  *
  *  @author Alberto Rubio Muñoz
  *  @see <a href="http://en.wikipedia.org/wiki/Jaccard_index">Jaccard Index (Wikipedia Article)</a> 
  */
 public class Jaccard {
 
 	/**
 	 *  @param simmetric 	This param specifies if you want a typical Jaccard distance or if you want to use the cardinal of `b` as a divisor
 	 *
 	 *  @return Jaccard distance between `a` and `b`
 	 */
 	public static double distance(IBag<String> a, IBag<String> b) {
 		IBag<String> i = a.intersection( b );
 		if ( i.size() == 0 ) {
 			return 1.0d;
 		}
 		
 		return 1.0d - ((double)i.size() / (double)a.union(b).size());
 	}
 }
