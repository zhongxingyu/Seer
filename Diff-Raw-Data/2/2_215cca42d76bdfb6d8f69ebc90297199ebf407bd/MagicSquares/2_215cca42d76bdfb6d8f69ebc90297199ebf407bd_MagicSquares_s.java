 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 public class MagicSquares {
 	
 	int NUM_THREADS = 64;
 	int order;
 	int max;
 	int magic_constant;
 	long start_time;
 	boolean print_squares = true;
 	ArrayList<MagicSquares.SquareMatrix> magic_squares = new ArrayList<MagicSquares.SquareMatrix>();
 	
 	public static void main(String[] args) {
 		if (args.length > 0) {
 			
 			MagicSquares obj = new MagicSquares();
 			int order = Integer.parseInt(args[0]);
 			
			if (args.length > 1 && args[1] == "threads") {
 				obj.testNumThreads();
 			} else {
 			
 				System.out.println("Finding all magic matricies of order " + order);
 				
 				obj.init(order);
 				
 				long end_time = System.currentTimeMillis();
 		        long runtime = end_time - obj.start_time;
 		        double runtime_seconds = (double) runtime / (double) 1000;
 		        
 		        System.out.println("Found "+obj.magic_squares.size()+" magic squares in "+String.format("%f", runtime_seconds)+" seconds");
 	        
 			}
         } else {
             System.out.println("Usage: java MagicSquares <order>");
         }
 	}
 	
 	public void init(int order) {
 		
 		this.start_time = System.currentTimeMillis();
 		this.order = order;
 		this.max = this.order*this.order;
 		this.magic_constant = (this.order*this.order*this.order + this.order) / 2;
 		
         long i = 0;
         long end_i = MagicSquares.factorial(this.max);
         
         long chunk_size = MagicSquares.factorial(this.max) / this.NUM_THREADS;
         
         ArrayList<Thread> threads = new ArrayList<Thread>();
         
         for (int j = 0; j < this.NUM_THREADS; j++) {
         	long a = i;
         	long b = Math.min(i + chunk_size, end_i);
         	Thread t = new Thread(this.new MatrixThread(a,b));
         	threads.add(t);
         	t.start();
         	if (b >= end_i)
         		break;
         	i += chunk_size + 1;
         }
         
         for (int j = 0; j < threads.size(); j++) {
         	try {
 				threads.get(j).join();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
         }
         
 	}
 	
 	public class MatrixThread extends Thread {
 		long a;
 		long b;
 		public MatrixThread(long a, long b) {
 			this.a = a;
 			this.b = b;
 			//System.out.println("I've got perms " + a +" thru " + b + "!");
 		}
 		public void run() {
 			for (long i = this.a; i < this.b; i++) {
 				int[] current_permutation = get_permutation(i);
 				MagicSquares.SquareMatrix m = new SquareMatrix(current_permutation);
 	        	if (m.is_magic()) {
 	        		magic_squares.add(m);
 	        		if (print_squares)
 	        			thread_message(m, a, b, i);
 	        	}
 			}
 		}
 	}
 	
 	public void thread_message(MagicSquares.SquareMatrix m, long a, long b, long i) {
 		long time = System.currentTimeMillis();
 		String name = Thread.currentThread().getName();
 		System.out.println((time-start_time) + "ms: Magic Matrix "+magic_squares.size()+" found at "+i+" ("+(i-a)+" of "+(b-a+1)+" on "+name+"):");
 		System.out.println(m.toString());
 	}
 	
 	public class SquareMatrix {
 		int[] data;
 		
 		public SquareMatrix(int[] data) {
 			this.data = data;
 		}
 		
 		public boolean is_magic() {
 			
 			for (int m = 0; m < order; m++) {
 				int row_sum = 0;
 				for (int n = 0; n < order; n++)
 					row_sum += this.data[order * m +n];
 				if (row_sum != magic_constant)
 					return false;
 			}
 			
 			for (int n = 0; n < order; n++) {
 				int col_sum = 0;
 				for (int m = 0; m < order; m++)
 					col_sum += this.data[order * m + n];
 				if (col_sum != magic_constant)
 					return false;
 			}
 			
 			int left_diagonal_sum = 0;
 			for (int i = 0; i < order; i++)
 				left_diagonal_sum += this.data[order*i+i];
 			if (left_diagonal_sum != magic_constant)
 				return false;
 			
 			int right_diagonal_sum = 0;
 			for (int i = 0; i < order; i++)
 				right_diagonal_sum += this.data[order*i+order-i-1];
 			if (right_diagonal_sum != magic_constant)
 				return false;
 			
 			return true;
 		}
 		
 		public String toString() {
 			// old_data[m][n] = new_data[order*m + n]
 			String max_term = max + "";
 			int max_term_size = max_term.length();
 			
 			String result = "";
 			
 			String border = "+";
 			String border_between = "|";
 			for (int i = 0; i < order; i++) {
 				border += "--" + MagicSquares.str_repeat("-", max_term_size);
 				border_between += "--" + MagicSquares.str_repeat("-", max_term_size);
 				if (i != order - 1 ) {
 					border += "-";
 					border_between += "+";
 				}
 			}
 			border += "+\n";
 			border_between += "|\n";
 			
 			result += border;
 			
 			for (int m = 0; m < order; m++) {
 				result += "|";
 				for (int n = 0; n < order; n++) {
 					int padding_right = max_term_size - (data[order*m+n] + "").length();
 					result += " " + data[order*m+n] + MagicSquares.str_repeat(" ", padding_right)+ " |";
 				}
 				result += "\n";
 				if (m != order - 1)
 					result += border_between;
 			}
 			result += border;
 			return result;
 		}
 	}
 	
 	private static String str_repeat(String str, int repeat) {
 		String result = "";
 		for (int i = 0; i < repeat; i++)
 			result += str;
 		return result;
 	}
 	
 	public static long factorial(int n) {
 		long ret = 1;
         for (int i = 1; i <= n; ++i) ret *= i;
         return ret;
     }
 	
 	public int[] get_permutation(long i) {
 		ArrayList<Integer> p = new ArrayList<Integer>();
 		for (int k = 1; k < max+1; k++)
 			p.add(k);
 		
 		int[] r = new int[max];
 		for (int j = 0; j<max; j++) {
 			long g = MagicSquares.factorial(p.size() - 1);
 			int k = (int)(i/g);
 			r[j] = p.get(k);
 			p.remove(k);
 			i = i % g;
 		}
 		return r;
 	}
 	
 	public void testNumThreads() {
 		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
 		MagicSquares obj = new MagicSquares();
 		for (int i = 1; i <= 128; i++) {
 			obj.print_squares = false;
 			obj.NUM_THREADS = i;
 			obj.init(3);
 			long end_time = System.currentTimeMillis();
 			int runtime = (int) (end_time - obj.start_time);
 			System.out.println("Threads: " + i + ", Time: " + runtime + "ms");
 			map.put(i, runtime);
 		}
 		
 		Integer min = Collections.min(map.values());
 		ArrayList<Map.Entry<Integer,Integer>> min_entries = new ArrayList<Map.Entry<Integer,Integer>>();
 		
 		for (Map.Entry<Integer, Integer> entry : map.entrySet())
 		    if (min == entry.getValue())
 		        min_entries.add(entry);
 
 		System.out.println("Fastest time was "+min+"ms and occured when there were ");
 		for (int i = 0; i < min_entries.size(); i++) {
 			System.out.println(min_entries.get(i).getKey() + " threads");
 		}
 	}
 
 }
