 package hps.mint;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.ArrayList;
 import java.lang.StringBuilder;
 
 public class ExactChange {
 		// !!! Assumes sorted (in decreasing order) cachedDenoms
 	static public ArrayList<Integer> compute(List<Integer> denoms, float N) {
 		
 		// Initialize map (sorted assumption)
 		ArrayList<Integer> counts = new ArrayList<Integer>(Collections.nCopies(100 + denoms.get(0), Integer.MAX_VALUE));
 		for( Integer i : denoms )
 			if( i < counts.size() )  counts.set(i, 1);
 		counts.set(0, 0);
 		if( counts.size() > 100 )
 			counts.set(100, 0);
 		
 		// Fill map
 		for( int i=0; i<counts.size(); i++ ) {
 			for( Integer j : denoms) {
 				if( j <= i ) {
 					int possibleMin = counts.get(i-j) + 1;
 					if( counts.get(i) > possibleMin )
 						counts.set(i, possibleMin);
 				}
 			}
 		}
 		
 		return counts;
 	}
 	
 	static public List<Integer> getCoins(List<Integer> counts, List<Integer> denoms, int sum) {
 		List<Integer> coins = new ArrayList<Integer>(10);
 		
 		while( sum % 100 > 0 )
 		{
 			for( Integer d : denoms )
 			{
 				if( sum < d )  { continue; }
 				
				System.out.println("value 1="+counts.get(sum-d));
				System.out.println("value 2="+(counts.get(sum)-1));
				
 				//edited by kunal balani
 				//special case when denomination is by 100
 				if(d == 100){
 					if( counts.get(sum-d) == counts.get(sum) )
 					{
 						coins.add(d);
 						sum -= d;
 						break;
 					}
 				}
 				
 				if( counts.get(sum-d) == counts.get(sum)-1 )
 				{
 					coins.add(d);
 					sum -= d;
 					break;
 				}
 			}
 		}
 		
 		return coins;
 	}
 	
 	static public String coinsToString(List<Integer> coins) {
 		if( coins.size() == 0 )  return "";
 		StringBuilder sb = new StringBuilder();
 		for( Integer c : coins ) {
 			sb.append(c+",");
 		}
 		sb.setLength(sb.length()-1);
 		return sb.toString();
 	}
 }
