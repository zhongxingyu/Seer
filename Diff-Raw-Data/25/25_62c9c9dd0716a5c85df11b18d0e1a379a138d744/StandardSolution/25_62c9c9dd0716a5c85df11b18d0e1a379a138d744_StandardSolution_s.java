 import java.util.Random;
 
 
 public class StandardSolution extends AbstractSolution {
 	private long[] sequence;
 	private boolean[] partitions;
 	
 	// constructor without partitions creates random solution
 	public StandardSolution (long[] nums){
 		sequence = nums;
 		partitions = new boolean[sequence.length];
 		
 		Random rand = new Random();
 		for (int i = 0; i < partitions.length; i++){
 			// worry about seeing?
 			partitions[i] = rand.nextBoolean();
 		}
 	}
 
 	public StandardSolution (long[] nums, boolean[] parts){
 		sequence = nums;
 		partitions = parts;
 	}
 	
 	public long getResidue(){
 		long residue = 0;
 		for (int i = 0; i < sequence.length; i++){
 			if (partitions[i])
 				residue += sequence[i];
 			else
 				residue -= sequence[i];
 		}
 		return Math.abs(residue);
 	}
 	
 	public Solution getRandNeigbor(){
		boolean[] neighbor_partitions = partitions;
 		// do we need to worry about seeding???
 		Random rand = new Random();
 		int i = rand.nextInt(sequence.length);
 		int j = rand.nextInt(sequence.length);
 		while (i==j){
 			j = rand.nextInt(sequence.length);
 		}
 		int p = rand.nextInt(2);
 		
 		neighbor_partitions[i] = !neighbor_partitions[i];
 		if (p==0){
 			neighbor_partitions[j] = !neighbor_partitions[j];
 		}
 			
 		return new StandardSolution(sequence, neighbor_partitions);
 	}
 	
 	public Solution randSolution(){
 		return new StandardSolution(sequence);
 	}
 	
 
 	
 }
