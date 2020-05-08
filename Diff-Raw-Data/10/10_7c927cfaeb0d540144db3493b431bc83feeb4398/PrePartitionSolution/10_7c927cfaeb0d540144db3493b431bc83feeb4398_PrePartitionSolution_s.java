 import java.util.Arrays;
 import java.util.Random;
 
 
 public class PrePartitionSolution extends AbstractSolution  {
 	private long[] sequence;
 	private int[] partitions;
 	private KK solution;
 	private Random rand;
 	
 	public PrePartitionSolution (long[] nums_init){
 		sequence = nums_init;
 		int n = sequence.length;
 		partitions = new int[n];
 		
 		rand = new Random();
 		for(int i = 0; i < n; i++){
 			partitions[i] = rand.nextInt(n);
 		}
 		
 		long[] mod_sequence = getNewSequence();
 		solution = new KK(mod_sequence);
 	}
 	
 	public PrePartitionSolution (long[] nums_init, int[] parts){
 		sequence = nums_init;
 		partitions = parts;
 		
 		rand = new Random();
 		long[] mod_sequence = getNewSequence();
 		solution = new KK(mod_sequence);
 	}
 	
 
 	
 	private long[] getNewSequence(){
 		int n = sequence.length;
 		int[] partition_index = new int[n];
 		Arrays.fill(partition_index,-1);
 		
 		long[] new_sequence = new long[n];
 		for (int i = 0; i < n; i++){
 			int p = partitions[i];
 			if (partition_index[p] == -1){
 				new_sequence[i] = sequence[i];
 				partition_index[p] = i;
 			}
 			else{
 				new_sequence[partition_index[p]] += sequence[i];
 				new_sequence[i]= 0;
 			}		
 		}
 		return new_sequence;
 	}
 	
 	public long getResidue(){
 		return solution.getResidue();
 	}
 	
 	public Solution getRandNeigbor(){
 		int n = sequence.length;
 		int i = rand.nextInt(n);
 		int j = rand.nextInt(n);
 		
 		while(partitions[i]==j)
 			j = rand.nextInt(n);
		int[] new_partitions = partitions;
 		new_partitions[i] = j;
 		
 		return new PrePartitionSolution(sequence,new_partitions);
 		
 	}
 	
 	public Solution randSolution(){
 		return new PrePartitionSolution(sequence);
 	}
 	
 	
 }
