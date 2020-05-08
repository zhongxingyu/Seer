 package srp.dbgraph;
 
 import java.util.ArrayList;
<<<<<<< HEAD
=======
 import java.util.Arrays;
 import java.util.HashMap;
>>>>>>> developSW
 import java.util.HashSet;
 
 import srp.evolution.haplotypes.Haplotype;
 import srp.evolution.haplotypes.HaplotypeModel;
 
 public class DeBruijnGraphLikelihood {
 	
 	private DeBruijnGraph dbGraph;
 	private CompatibleSets compSets;
 //	private int maxNodeIndex;
 	private double[][] length_diff;
 	private int totalCount;
 	private int nodeCount;
 	private int[] pairedNode;
 	
 	HashMap<Character, Character> DNAComplement; 
 	
 	public DeBruijnGraphLikelihood(DeBruijnGraph dbGraph, CompatibleSets compSets) {
 		this.dbGraph = dbGraph;
 		this.compSets = compSets;
 		
 		preprocess();
 	}
 	
 	
 	private void preprocess() {
 		dbGraph.preprocess();
 		compSets.preprocess();
 		this.nodeCount = dbGraph.getSize();
 		
 		ArrayList<Integer> allLength = dbGraph.getAllLength();
 		pairedNode = new int[nodeCount];
 		length_diff = new double[nodeCount][nodeCount];
 		totalCount = 0;
 		for (CompatibleNode k1CNode : compSets) {
 			int k1 = k1CNode.getNodeIndex();
 			int k1LengthMinusDepth = allLength.get(k1) - k1CNode.getNodeDepth();
 			int[] cNodeList = k1CNode.getCNodeArray();
 			for (int k2 : cNodeList) {
 				if(k1 != k2){
 					totalCount += k1CNode.getCNodeCount(k2);
 					length_diff[k1][k2] = k1CNode.getCNodeDepth(k2) + allLength.get(k2) + k1LengthMinusDepth;
 				}
 			}
 				
 		}
 
 		DNAComplement = new HashMap<>();
 
 		DNAComplement.put('A', 'T');
 		DNAComplement.put('T', 'A');
 		DNAComplement.put('C', 'G');
 		DNAComplement.put('G', 'C');
 		
 
 
 		pairComplementNodes();
 		
 	}
 
 
 	
 	private void pairComplementNodes() {
 		boolean[] temp_hash2 = new boolean[nodeCount]; 
 		HashMap<Integer, Integer> temp_hash = new HashMap<Integer, Integer>();
 		HashMap<String, Integer> inverse_hash = new HashMap<String, Integer>();
 //		for $k( keys %cond_ver) {
 		ArrayList<String> allNodes = dbGraph.getAllNodes();
 		for (int i = 0; i < nodeCount; i++) {
 			
 		
 //		for (String string : allNodes) {
 //			
 //		}
 			int nodeIndex = i;//cNode.getNodeIndex();
 			temp_hash.put(i, 0);
 			temp_hash2[i] = true;
 			inverse_hash.put(allNodes.get(i), i);
 //		}
 //			temp_hash{$k} = 0;
 //			$inverse_hash{$cond_ver{$k}} = $k;
 		} 
 		
 //		#Link nodes which are reverse complements of each other. 
 		for (int i = 0; i < nodeCount; i++) {
 			if(temp_hash2[i]){
 //			if($temp_hash{$k}==0) {
 				String revComp = revcomplement(allNodes.get(i));
 				inverse_hash.get(revComp);
 //				System.out.println("aoeu\t"+inverse_hash.get(revComp));
 				Integer revIndex = inverse_hash.get(revComp);
 				if(revIndex!= null){
 					temp_hash2[i] = false;
 					temp_hash2[revIndex] = false;
 					pairedNode[i] = revIndex;
 					pairedNode[revIndex] = i;
 				}
 //				my($temp) = revcomplement($cond_ver{$k}); //get revComp
 //				$paired_nodes{$k} = $inverse_hash{$temp}; //get nodeIndex and paired them
 //				$paired_nodes{$inverse_hash{$temp}} = $k ; 
 //				$temp_hash{$k} = 1; 						//true
 //				$temp_hash{$inverse_hash{$temp}} = 1;
 			}
 		}
 //		undef %inverse_hash;
 //		undef %temp_hash;
 	}
 	private String revcomplement(String string) {
 
 		int l = string.length();
 		char[] revString = new char[l];
 		for (int i = 0; i < l; i++) {
 			revString[l-i-1] = (char) DNAComplement.get( string.charAt(i) );
 		}
 		
 		return String.valueOf(revString);
 	}
 
 
 	public double[][] getLengthDiff(){
 		return length_diff;
 	}
 
 	public void test(){
 //		HashMap<Integer, Integer> allEdges = dbGraph.getAllEdges();
 //		HashMap<Integer, String> allNodes = dbGraph.getAllNodes();
 //		for (int i = 0; i < maxNodeIndex; i++) {
 ////			System.out.println(i +"\t"+ allEdges.get(i) +"\t"+ allNodes.get(i));
 //			
 //		}
 	}
 	public double calculateLikelihood(PathSet pathSet){
 		
 		
 		double likelihood = 0;
 		double[][] d_hashTable = computeDHashTable(pathSet);
 		boolean[][] visited = new boolean[nodeCount][nodeCount];
 		//		
 ////		#print "In compute set \n";
 ////		my($llik) = 0; my($min) = 0; my($max) = -9**9	**9; 
 		double min = 0;
 		int pathCount = pathSet.getPathCount();
 		double Scale = pathCount*1200;
 //		
 //		HashMap<Integer, Integer> allLength = dbGraph.getAllLength();
 
 //		double[][] length_diff = new double[maxNodeIndex][maxNodeIndex];
 //		int totalCount = 0;
 //		for (CompatibleNode k1CSet : compSets) {
 //			int k1 = k1CSet.getNode();
 //			ArrayList<Integer> cNodeList = k1CSet.getCNodeList();
 //			for (Integer ik2 : cNodeList) {
 //				int k2 = ik2;
 //				totalCount += k1CSet.getCNodeCount(k2);
 //				length_diff[k1][k2] = k1CSet.getCNodeDepth(k2) - k1CSet.getDepth() + allLength.get(k1)+ allLength.get(k2);
 //			}
 //				
 //		}
 //}{$v2} = $v2_depth - $v1_depth + $length_ver{$v2} + $length_ver{$v1};
 		for (CompatibleNode k1CNode : compSets) {
 			int k1 = k1CNode.getNodeIndex();
 			int[] cNodeList = k1CNode.getCNodeArray();
 
 			for (int k2 : cNodeList) {
 			
 				if(d_hashTable[k1][k2]>0){
 					
 					if(!visited[k1][k2]){
 						int cNodeCount = k1CNode.getCNodeCount(k2);
 						double temp0 = d_hashTable[k1][k2]/(Scale-length_diff[k1][k2]);
 						double temp1 = totalCount - cNodeCount;
 						double temp2 = 1 - temp0;
 						double val = temp1*Math.log(temp2) + cNodeCount * Math.log(temp0);
 						likelihood += val;
 						System.out.println(k1 +"\t"+ k2 +"\t"+ temp0 +"\t"+ temp1 +"\t"+ temp2 +"\t"+ val);
 						visited[k1][k2] = true;
 						visited[pairedNode[k2]][pairedNode[k1]] = true;
 						
 					}
 				}else				{
 					likelihood += min;
 				}
 			}
 		}
 //		for $k1( keys %compatible_set ) 
 //		{
 //		#	print "Processing $k1 compatible set \n";
 //			for $k2(keys %{$compatible_set{$k1}}) 
 //			{
 //				if(exists($d_hashtable{$k1}{$k2}))
 //				{
 //					double temp0 = $d_hashtable{$k1}{$k2}/($Scale-$length_diff{$k1}{$k2});
 //					double temp1 = $total_count - $compatible_set{$k1}{$k2};
 //					double temp2 = 1 - ($d_hashtable{$k1}{$k2}/($Scale-$length_diff{$k1}{$k2}));
 //					double val = temp1*Math.log(temp2) + $compatible_set{$k1}{$k2} * Math.log(temp0);
 //					$llik += $val;
 //				}else				{
 //					likelihood += min;
 //				}
 //			}
 //		}
 
 		
 		return likelihood;
 	}
 	
 	
 /*	
 	sub compute_paired_likelihood
 	{
 		# Compute the likelihood of a set of paths based on the paired k-mers that support it 
 		# Need to know d_ijk, 
 		# Break the node paths into compatible sets 
 		# See if some compatible sets are shared across two or more haplotypes 
 		# %set_paths contains the set of haplotypes for which likelihood is to be computed... 
 		%d_hashtable = ();
 		$num_haps = scalar(keys %set_paths);
 		$genome_length = 1200;
 	#	print "Number of haplotypes is $num_haps \n";
 	#	print "Computing d-hasttable \n";
 		compute_d_hashtable();
 	#	print "D hash table done,Starting on likelihood \n";
 		$like = compute_set_likelihood_using_d();
 	#	print "Likelihood of set $like \n";
 		return $like;
 	}
 
 	sub compute_set_likelihood_using_d
 	{
 		#print "In compute set \n";
 		my($llik) = 0; my($min) = 0; my($max) = -9**9**9; 
 		$Scale = $num_haps*$genome_length; 
 		for $k1( keys %compatible_set ) 
 		{
 		#	print "Processing $k1 compatible set \n";
 			for $k2(keys %{$compatible_set{$k1}}) 
 			{
 				if(exists($d_hashtable{$k1}{$k2}))
 				{
 					$temp0 = $d_hashtable{$k1}{$k2}/($Scale-$length_diff{$k1}{$k2});
 					$temp1 = $total_count - $compatible_set{$k1}{$k2};
 					$temp2 = 1 - ($d_hashtable{$k1}{$k2}/($Scale-$length_diff{$k1}{$k2}));
 					$val = $temp1*log($temp2) + $compatible_set{$k1}{$k2} * log($temp0);
 					$llik += $val;
 				}else 
 				{
 					#compatible set contains a haplotype which is not supported by the set of haplotypes given, reject the set of haplotypes 	
 	#				print "Set of haplotypes not compatible based on data $k1 $k2 $compatible_set{$k1}{$k2} \n";
 					return $min;
 				}
 			}
 		}
 		#print "Max $max and Min $min $t1 $t2 $compatible_set{$t1}{$t2}\n";
 		#print "Likelihood is $llik \n";
 		return $llik;
 	}
 	*/
 
 	public double[][] computeDHashTable(PathSet pathSet) {
 		
 		double[][] d_hashTable = new double[nodeCount][nodeCount];
 		boolean offSet = pathSet.getOffSet();
 		int i = 0;
 		if(offSet){
 			i=1;
 		}
 		for (; i < pathSet.getPathCount(); i++) {
 			Path path = pathSet.getPath(i);
 //			ArrayList<Integer> path = new ArrayList<Integer>();
 			//path contains series of node??
 			HashSet<Integer> tempSet = new HashSet<>();
 			ArrayList<Integer> nodeList = path.getNodeList();
 			
 			for (Integer k1 : nodeList) {
 				tempSet.add(k1);
 				//TODO: redo this part, need create PATH class
 //				my(%temp_set) = ();
 //				for $k(@one_path) 
 //				{	
 //					$temp_set{$k} = 1;
 //				}
 			}
 			System.out.println(tempSet);
 			for (Integer k1 : nodeList) {
 				CompatibleNode compatibleSet = compSets.getCompatibleNode(k1);	
 //				int nodeIndex = compatibleSet.getNode();
 				int[] cNodeList = compatibleSet.getCNodeArray();
 //				System.out.println(Arrays.toString(cNodeList));
 				for (int nodeIndex : cNodeList) {
 	
 				
 					if (tempSet.contains(nodeIndex)){
 //						System.out.println(k1 +"\t"+ nodeIndex);
 						d_hashTable[k1][nodeIndex]++;
 					}
 				}
 			}
 //			System.out.println();
 		}
 //			for $n1( keys %set_paths ) 
 //			{
 //				#pick a path in the haplotype set
 //				my(@one_path) = @{$set_paths{$n1}};
 //				#print "Operating on $n1 ".scalar(@one_path)." nodes in it \n";
 //				my(%temp_set) = ();
 //				for $k(@one_path) 
 //				{	
 //					$temp_set{$k} = 1;
 //				}
 //				for (my($i)=0;$i<scalar(@one_path);$i++)
 //				{
 //					for $k(keys %{$compatible_set{$one_path[$i]}})
 //					{
 //						if(exists($temp_set{$k})) 
 //						{
 //							$d_hashtable{$one_path[$i]}{$k}++;
 //						}
 //					}
 //				}
 //			}
 //		}
 		return d_hashTable;
 	}
 
 
 	public int getTotalCount() {
 		return totalCount;
 	}
 }
