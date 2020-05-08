public class Solution {
 
 class Pair{
 	int left;
 	int right;
 	public Pair(int left, int right){
 		this.left = left;
 		this.right =right;
 	}
 }
 	private ArrayList<ArrayList<Integer> >getFours(int [] num, List<Pair> srcPairs,
 			List<Pair> destPairs){
 		ArrayList<ArrayList<Integer>> all = new ArrayList<ArrayList<Integer>>();
 		//this will make sure a<b<c<d
		for(Pair src: srcPairs)
 			for(Pair dest:destPairs){
				if(src.right < dest.left){
 					ArrayList<Integer> al = new ArrayList<Integer>();
 					al.add(num[src.left]);
 					al.add(num[src.right]);
 					al.add(num[dest.left]);
 					al.add(num[dest.right]);
 					all.add(al);
 				}
 			}
 		return all;
 	}
 	public ArrayList<ArrayList<Integer>> fourSum(int[] num, int target) {
 		//if input is invalid, return empty results
 		ArrayList<ArrayList<Integer>> all = new ArrayList<ArrayList<Integer>> ();
 		if(num == null || num.length < 4)
 			return all;
 		//sort first for removing duplicates in the future
 		Arrays.sort(num);
 		//building pair sum index map
 		Map<Integer, List<Pair>> pairSumMap = new HashMap<Integer, List<Pair>>();
		for(int i = 0; i < num.length; ++i)
 			for(int j = i+1; j< num.length; ++j) {
 				int sum = num[i] + num [j];
 				Pair pair = new Pair(i, j);
 				if(!pairSumMap.containsKey(sum)){
 					pairSumMap.put(sum, new LinkedList<Pair>());
 				}
 				List<Pair> pairList = pairSumMap.get(sum);
 				pairList.add(pair);
 			}
 		//Finding two pairs sum to target
 		for(Integer val : pairSumMap.keySet()){
 			//otherwise may cause duplicates
 			if(val > target/2)
 				continue;
 			//no the other pair match
 			if(!pairSumMap.containsKey(target- val))
 				continue;
 			//src and dest
 			List<Pair> srcPairs = pairSumMap.get(val);
 			List<Pair> destPairs = pairSumMap.get(target-val);
 			ArrayList<ArrayList<Integer>> al = getFours(num,srcPairs, destPairs);
 			all.addAll(al);
 		}
 		return all;
 	}
}
