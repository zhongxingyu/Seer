 package bloom;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.UUID;
 
 import bloom.dd.StrataEstimator;
 import bloom.filters.InvertibleBloomFilter;
 
 public class Main {
 
 
 	public static Set<String> createFileNames(int number){
 		return createFileNames(number, new HashSet<String>(number));
 	}
 	
 	public static Set<String> createFileNames(int additional, Set<String> files){
 		int start = files.size();
 		while (files.size() < (start+additional) ){
 			files.add( UUID.randomUUID().toString().substring(0,10) );	//TODO allow for generic size, not just 10
 		}	
 		return files;
 	}
 	
 	
 	public static void tuningIBF(int num_hash_functions, int num_cells){
 		int trials = 100;
 		int[] keys_sizes = {100,1000,10000,100000,1000000};
 		int[] deltas = {0,5,10,15,20,25,30,35,40,45,50};
 		
		Set<String> files_base, files_different;
 		InvertibleBloomFilter ibf1, ibf2, ibf_sub;
		ArrayList<String> difference;
 		int realDiff, calcDiff;
 		float success, success_total;
 		
 		for(int i=0; i < keys_sizes.length; ++i){			
 			for(int j=0; j < deltas.length; ++j){
 				
 				success_total = 0;
 				for (int t=0; t < trials; ++t){
 					
 					files_base = createFileNames(keys_sizes[i]);
 					files_different = new HashSet<String>(files_base);
 					ibf1 = new InvertibleBloomFilter(num_hash_functions, num_cells, files_base);
 					
 					files_different = new HashSet<String>(files_base);
 					files_different = createFileNames(deltas[j], files_different);
 					ibf2 = new InvertibleBloomFilter(num_hash_functions, num_cells, files_different);
 					
 					ibf_sub = InvertibleBloomFilter.subtract(ibf1, ibf2);
 	
 					difference = ibf_sub.getPureKeys();
 					realDiff = files_different.size() - files_base.size();
 					calcDiff = difference.size();
 					success = 1 - Math.abs(realDiff-calcDiff)/(float)realDiff;
 					//System.out.println("Decoded a set of "+keys_sizes[i]+" with a difference of "+realDiff+". Estimate "+calcDiff + ". % Success "+success);
 					//System.out.println(success);
 					if(success == Double.NaN)
 						success = 1;
 					success_total += success;
 				}
 				System.out.println(success_total/trials);
 			}
 			
 			System.out.println("---------------------------");
 		}	
 		
 	}
 	
 	//num keys shouldn't matter, as shown by the tuning IBF results, what matter is the difference between sets
 	public static void tuningHashCount(int num_keys, int num_cells){
 		int trials = 1000;
 		int[] deltas = {0,5,10,15,20,25,30,35,40,45,50};
 		int[] hashCount = {2,3,4,5,6};
 		InvertibleBloomFilter ibf1, ibf2, ibf_sub;
		Set<String> files_base, files_different;
		ArrayList<String> decoded_files;
 		double[][] results = new double[deltas.length][hashCount.length];
 		
 		for (int i=0; i < deltas.length; ++i)
 			for (int j=0; j < hashCount.length; ++j)
 				results[i][j] = 0;
 		
 		for (int i=0; i < deltas.length; i++){	
 			System.out.println("Computing Decoding Probability for Delta of " + deltas[i] + " for varying hash counts using " + num_keys + " keys.");
 			for (int t=0; t < trials; ++t){
 				
 				files_base = createFileNames(num_keys-deltas[i]);
 				files_different = new HashSet<String>(files_base);
 				files_different = createFileNames(deltas[i], files_different);
 				
 				for (int j=0; j < hashCount.length; ++j){
 					ibf1 = new InvertibleBloomFilter(hashCount[j], num_cells, files_base);
 					ibf2 = new InvertibleBloomFilter(hashCount[j], num_cells, files_different);
 					
 					ibf_sub = InvertibleBloomFilter.subtract(ibf1, ibf2);
 					decoded_files = ibf_sub.getPureKeys();
 					
 					if (deltas[i] == 0)
 						results[i][j] = 1; //TODO set to 1 because we can't divide by 0 when deltas[0];
 					else
 						results[i][j] += (double) decoded_files.size()/(double) deltas[i]; 
 				}
 			}
 		}
 		
 		System.out.println("-----Results----");
 		for (int i=0; i < deltas.length; ++i){
 			System.out.println(deltas[i]+" Set Difference");
 			for (int j=0; j < hashCount.length; ++j){
 				results[i][j] /= (double) trials;
 				System.out.print(results[i][j] + ", ");
 			}
 			System.out.println("");
 		}
 	}
 	
 	//num keys shouldn't matter, as shown by the tuning IBF results, what matter is the difference between sets
 	public static void correctionOverhead(int num_keys, int num_strata, int num_hash_functions){
 		int trials = 20;
 		int[] deltas = {10, 100, 1000, 10000}; //, 100000};
 		int[] cell_sizes = {20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160};
 		StrataEstimator se1, se2;
 		Set<String> files_base, files_different;
 		double difference = 0;
 		double[][] results = new double[deltas.length][cell_sizes.length];
 		
 		
 		for (int i=0; i < deltas.length; ++i)
 			for (int j=0; j < cell_sizes.length; ++j)
 				results[i][j] = 0;
 		
 		for (int i=0; i < deltas.length; ++i){
 			
 			System.out.println("Computing Correction Overhead for Delta of " + deltas[i] + " for Strata Estimators with varying IBF sizes.");
 			for (int t=0; t < trials; ++t){
 				files_base = createFileNames(num_keys);
 				files_different = new HashSet<String>(files_base);
 				files_different = createFileNames(deltas[i], files_different);
 				
 				for (int j=0; j < cell_sizes.length; ++j){
 					se1 = new StrataEstimator(num_strata, files_base, cell_sizes[j], num_hash_functions);
 					se2 = new StrataEstimator(num_strata, files_different, cell_sizes[j], num_hash_functions);
 					
 					difference = 0;
 					try {
						difference = StrataEstimator.estimateDifference(se1, se2);
 					} catch (Exception e) {
 						System.out.println(deltas[i]+" - "+cell_sizes[j]+" : Strata estimator was unable to deocde");
 					}
 					
 					results[i][j] += (float) deltas[i] / (float) difference;				
 				}
 			}
 		}
 		
 		System.out.println("-----Results----");
 		for (int i=0; i < deltas.length; ++i){
 			for (int j=0; j < cell_sizes.length; ++j){
 				results[i][j] /= (double) trials;
 				System.out.print(results[i][j] + ", ");
 			}
 			System.out.println("");
 		}
 		
 		
 		
 	}
 
 
 	
     public static void main(String[] args) {
 
     	
    	//tuningIBF(4,50);   	
     	//tuningHashCount(100, 50);
 		//correctionOverhead(100, 32, 4); //TODO enable throw exception otherwise results are completely off
     	
     }
 }
