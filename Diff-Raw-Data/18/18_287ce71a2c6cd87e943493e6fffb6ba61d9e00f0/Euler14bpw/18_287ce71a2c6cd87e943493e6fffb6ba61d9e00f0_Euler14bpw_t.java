 // The following iterative sequence is defined for the set of positive integers:
 
 // n  n/2 (n is even)
 // n  3n + 1 (n is odd)
 
 // Using the rule above and starting with 13, we generate the following sequence:
 
 // 13  40  20  10  5  16  8  4  2  1
 // It can be seen that this sequence (starting at 13 and finishing at 1) contains 
 // 10 terms. Although it has not been proved yet (Collatz Problem), it is thought 
 // that all starting numbers finish at 1.
 // Which starting number, under one million, produces the longest chain?
 
 // NOTE: Once the chain starts the terms are allowed to go above one million.
 
 
 import static java.lang.System.out;
 import java.util.*;
 
 public class Euler14bpw {
 
 	public static void main(String[] argv) {
 
 		// NOTE:
 		// Interestingly, the hashmap, while in theory should speed up the process, actually ends 
		// up slowing down the programm. Even so, I kept the code which utilizes the hashmap for
 		// reference, but left it commented out. It only needs to be uncommented in order for it 
		// to work again.
 
 		long begin = System.currentTimeMillis();
 
 		Euler14bpw solver = new Euler14bpw();
 		solver.run();
 
 		long end = System.currentTimeMillis();
 		out.println(end - begin + "ms");
 
 	}
 
	// ----------------------------------------------------------------------------------------------- 
 	// private Map<Integer, Integer> known;
 
 	// public Euler14() {
 	// 	this.known = new HashMap<Integer, Integer>();
 	// }
	// ----------------------------------------------------------------------------------------------- 
 
 
 	public void run() {
 
 		int answer = 0;
 
 		int count = 0;
 		int prev_count = 0;
 		double num = 0d;
 
 		for(int i=1; i<=1000000; i++) {
 
 			num = (double)(i);
 			count = 0;
 			while(num != 1d) {
 				num = next_num(num);
 				count++;
 
				// ------------------------------------------------------------------------
 				// if(known.containsKey(num) == true) {
 				// 	count += known.get(num);
 				// 	break;
 				// }
				// ------------------------------------------------------------------------
 
 			}
 
 			// -------------------------------------------------------------------------------- 
 			// known.put(i, count);
 			// -------------------------------------------------------------------------------- 
 
 
 			if(count > prev_count) {
 				prev_count = count;
 				answer = i;
 			}
 
 		}
 
 		out.println(answer);
 
 	}
 
 	public Double next_num(Double i) {
 
 		if(i % 2 == 0) {
 			return i/2;
 		}
 		return (3 * i + 1);
 
 	}
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
