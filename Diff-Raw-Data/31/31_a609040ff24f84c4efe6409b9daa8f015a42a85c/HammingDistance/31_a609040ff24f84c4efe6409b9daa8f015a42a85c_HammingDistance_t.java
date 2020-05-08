 package org.lilian.util.distance;
 
 import java.util.List;
 
 import org.lilian.util.Functions;
 
 public class HammingDistance<T> implements Distance<List<T>>
 {
 	private static final long serialVersionUID = 1L;
 
 	@Override
 	public double distance(List<T> a, List<T> b)
 	{
 		// compute the penalty for inequal size
 		int penalty = Math.abs(a.size() - b.size());
 		
 		int min = Math.min(a.size(), b.size());
 		
 		int diffs = 0;
 		for(int i = 0; i < min; i++)
			if(! Functions.equals(a.get(i), b.get(i)))
 				diffs ++;
 		
 		return diffs + penalty;
 	}
 }
