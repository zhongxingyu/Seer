 
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
        //this will make sure left has no duplicates
		Set<Integer> srcSet = new HashSet<Integer>();
		for(Pair src: srcPairs){
			Set<Integer> destSet = new HashSet<Integer>();
			if(srcSet.contains(num[src.left]))
				continue;
 			for(Pair dest:destPairs){
                //this will make sure left has no duplicates
				if(src.right < dest.left && !destSet.contains(num[dest.left])){
 					ArrayList<Integer> al = new ArrayList<Integer>();
 					al.add(num[src.left]);
 					al.add(num[src.right]);
 					al.add(num[dest.left]);
 					al.add(num[dest.right]);
					destSet.add(num[dest.left]);
					srcSet.add(num[src.left]);
 					all.add(al);
 				}
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
		for(int i = 0; i < num.length; ++i) {
            //cannot do short cut-here as (a,c) (b,d) might be useful when val[a]==val[b]
            //and in the four (a, x) (b, y) 
 			for(int j = i+1; j< num.length; ++j) {
                //this will make sure no duplicates from (a b) (a c) where val[b]== val[c]
				if(num[j] == num[j-1] && num[j] != num [i])
					continue;
 				int sum = num[i] + num [j];
 				Pair pair = new Pair(i, j);
 				if(!pairSumMap.containsKey(sum)){
 					pairSumMap.put(sum, new LinkedList<Pair>());
 				}
 				List<Pair> pairList = pairSumMap.get(sum);
 				pairList.add(pair);
 			}
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
