 import java.lang.Math;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Iterator;
 import edu.rit.util.Random;
 
public class PartitionSeq
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
 	
 	class PartitionResult {
 		int min_score;
 		LinkedList<int[]> min_arrangements;
 	};
 
 	static PartitionResult[] partition_results;
 
 	public static void main(String[] args)
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
 
 		numbers = new int[N];
 		current_arrangement = new int[N];
 
 		num_arrangements = 1L;
 		for (int i = 0; i < N; ++i) {
 			num_arrangements *= M;
 		}
 		arrangements_remaining = num_arrangements;
 
 		min_score = Integer.MAX_VALUE;
 		min_arrangements = new LinkedList<int[]>();
 
 		// Generate numbers to operate on and initialize first arrangement
 		for (int i = 0; i < N; ++i) {
 			numbers[i] = prng.nextInt(N) - N/2;
 			current_arrangement[i] = 0;
 		}
 		System.out.printf("numbers:\n");
 		System.out.println(" " + dump_array(numbers));
 		System.out.printf("possible arrangements: %d\n", num_arrangements);
 
 		while (arrangements_remaining > 0) {
 			if (debug) {
 				System.out.printf("arrangements_remaining #%d\n", arrangements_remaining);
 				System.out.println(dump_array(current_arrangement));
 			}
 
 			// Score arrangement
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
 
 				if (score <= min_score) {
 					if (score < min_score) {
 						min_arrangements.clear();
 						min_score = score;
 					}
 					min_arrangements.add((int[])current_arrangement.clone());
 				}
 				if (debug) {
 					System.out.printf("sums #%d\n", arrangements_remaining);
 					System.out.println(dump_array(sums));
 					System.out.printf("min: %d, max: %d\n", min_index, max_index);
 					System.out.printf("score: %d\n", score);
 					System.out.printf("-\n");
 				}
 			}
 
 			// Generate next arrangement
 			for (int i = 0; i < N; ++i) {
 				current_arrangement[i]++;
 				if (current_arrangement[i] < M) {
 					break;
 				} else {
 					current_arrangement[i] = 0;
 				}
 			}
 
 			--arrangements_remaining;
 		}
 
 		if (debug) {
 			System.out.printf("\n");
 		}
 
 		System.out.printf("min_score: %d\n", min_score);
 		System.out.printf("arrangements with this score: %d\n", min_arrangements.size());
 		//TODO: Sort arrangements and print only the top one
 		Iterator<int[]> iter = min_arrangements.iterator();
 		while (iter.hasNext()) {
 			int[] arrangement = iter.next();
 			System.out.println(" " + dump_array(arrangement) + " -> " + arrangement_toString(arrangement));
 		}
 		
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
