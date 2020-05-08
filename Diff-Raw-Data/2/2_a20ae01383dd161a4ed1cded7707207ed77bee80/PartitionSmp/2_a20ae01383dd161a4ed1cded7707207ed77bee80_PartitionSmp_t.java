 import java.lang.Math;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Iterator;
 import edu.rit.pj.ParallelRegion;
 import edu.rit.pj.ParallelTeam;
 import edu.rit.pj.LongForLoop;
 import edu.rit.pj.Comm;
 import edu.rit.util.Random;
 
 public class PartitionSmp
 {
 	static int N;
 	static int M;
 	static int s;
 
 	static Boolean debug = false;
 	static Random prng;
 
 	static int[] numbers; // Set of numbers to be partitioned
 	static int[] current_arrangement; // Each index corresponds to an index of the numbers array, the value represents the set in which the number is placed
 	static long num_arrangements;
 	static long arrangements_remaining;
 	
 	static private class PartitionResult {
 		int min_score;
 		LinkedList<int[]> min_arrangements;
 	};
 
 	static PartitionResult[] partition_results;
 
 	static ParallelTeam pt;
     
     static private int[] startConfig(long iteration, int numVar, int numParts){
         int[] ret = new int[numVar];
         for(int i = 0; i < numVar; i ++){
            ret[i] = (int)((iteration/(Math.pow(numParts, i)))%numParts);
         }
         return ret;
     }
 
 	public static void main(String[] args) throws Exception
 	{
 		long t0 = System.currentTimeMillis();
 		
 		if (args.length != 3) {
 			usage();
 			return;
 		}
 
 		N = Integer.parseInt(args[0]);
 		M = Integer.parseInt(args[1]);
 		s = Integer.parseInt(args[2]);
 
 		if (M > N) {
 			System.err.printf("M must be less than or equal to N\n");
 			return;
 		}
 
 		prng = Random.getInstance(s);
 
 		pt = new ParallelTeam();
 
 		numbers = new int[N];
 
 		num_arrangements = 1L;
 		for (int i = 0; i < N; ++i) {
 			num_arrangements *= M;
 		}
 
 		// Generate numbers to operate on and initialize first arrangement
 		for (int i = 0; i < N; ++i) {
 			numbers[i] = prng.nextInt(N) - N/2;
 		}
 		System.out.printf("numbers:\n");
 		System.out.println(" " + dump_array(numbers));
 		System.out.printf("possible arrangements: %d\n", num_arrangements);
 
 		pt.execute(new ParallelRegion() {
 			public void run() throws Exception {
 				partition_results = new PartitionResult[getThreadCount()];
 
 				execute(0, num_arrangements - 1, new LongForLoop() {
 					public void run(long first, long last) throws Exception {
 						PartitionResult partition_result;
 						int thread_index = getThreadIndex();
 						partition_results[thread_index] = partition_result = new PartitionResult();
 						partition_result.min_score = Integer.MAX_VALUE;
 						partition_result.min_arrangements = new LinkedList<int[]>();
 						int[] current_arrangement; // Each index corresponds to an index of the numbers array, the value represents the set in which the number is placed
 						current_arrangement = startConfig(first, N, M);
 
 						for (long a = first; a <= last; ++a) {
 							if (debug) {
 								System.out.printf("arrangement #%d\n", a);
 								System.out.println(dump_array(current_arrangement));
 							}
 
 							// score arrangement
 							{
 								int[] sums = new int[M];
 								int score;
 								int min_index = 0;
 								int max_index = 0;
 								int min_sum = Integer.MAX_VALUE;
 								int max_sum = Integer.MIN_VALUE;
 								for (int i = 0; i < N; ++i) {
 									int index = current_arrangement[i];
 									int num = numbers[i];
 									sums[index] += num;
 								}
 								for (int i = 0; i < M; ++i) {
 									int sum = sums[i];
 									if (sum < min_sum) {
 										min_index = i;
 										min_sum = sum;
 									}
 									if (sum > max_sum) {
 										max_index = i;
 										max_sum = sum;
 									}
 								}
 								score = sums[max_index] - sums[min_index];
 
 								if (score <= partition_result.min_score) {
 									if (score < partition_result.min_score) {
 										partition_result.min_arrangements.clear();
 										partition_result.min_score = score;
 									}
 									partition_result.min_arrangements.add((int[])current_arrangement.clone());
 								}
 								if (debug) {
 									System.out.printf("sums #%d\n", arrangements_remaining);
 									System.out.println(dump_array(sums));
 									System.out.printf("min: %d, max: %d\n", min_index, max_index);
 									System.out.printf("score: %d\n", score);
 									System.out.printf("-\n");
 								}
 							}
 
 							
 							// generate next arrangement
 							for (int i = 0; i < N; ++i) {
 								current_arrangement[i]++;
 								if (current_arrangement[i] < M) {
 									break;
 								} else {
 									current_arrangement[i] = 0;
 								}
 							}
 						}
 					}
 				});
 			}
 		});
 
 		// find lowest min_score amoung threads
 		// print first arrangement from first thread matching min_score
 		int index = -1;
 		int result = Integer.MAX_VALUE;
 		int num_of_min_arrangements = 0;
 		
 		for( int i = 0; i < partition_results.length; i++){
 			if (partition_results[i] != null) {
 				if(partition_results[i].min_score < result){
 					result = partition_results[i].min_score;
 				}
 			}
 		}
 		for( int i = 0; i < partition_results.length; i++){
 			if (partition_results[i] != null) {
 				if (partition_results[i].min_score == result) {
 					num_of_min_arrangements += partition_results[i].min_arrangements.size();
 					if (index < 0)
 						index = i;
 				}
 			}
 		}
 		System.out.printf("min_score: %d\n", result);
 		System.out.printf("arrangements with this score: %d\n", num_of_min_arrangements);
 		System.out.println(arrangement_toString(partition_results[index].min_arrangements.getFirst()));
 
 		long t1 = System.currentTimeMillis();
 		System.out.printf("%d msecs\n", t1 - t0);
 	}
 
 	public static void usage() {
 		System.err.println("Usage: PartitionSeq <N numbers> <M partitions> <seed>");
 	}
 
 	public static String dump_array(int[] a) {
 		String str = "[";
 		for (int i = 0; i < a.length; ++i) {
 			str += String.format("%d", a[i]);
 			if (i < a.length - 1)
 				str += ",";
 		}
 		str += "]";
 		return str;
 	}
 
 	public static String arrangement_toString(int[] arrangement) {
 		ArrayList<LinkedList<Integer>> sets = new ArrayList<LinkedList<Integer>>();
 		String str = "";
 
 		for (int i = 0; i < M; ++i) {
 			sets.add(new LinkedList<Integer>());
 		}
 
 		for (int i = 0; i < N; ++i) {
 			int index = arrangement[i];
 			sets.get(index).add(numbers[i]);
 		}
 
 		Iterator<Integer> iter;
 		for (int i = 0; i < M; ++i) {
 			iter = sets.get(i).listIterator();
 			str += "{";
 			while (iter.hasNext()) {
 				int number = iter.next();
 				str += String.format("%d", number);
 				if (iter.hasNext())
 					str += ",";
 			}
 			str += "}";
 			if (i < M - 1)
 				str += ",";
 		}
 		return str;
 	}
 }
