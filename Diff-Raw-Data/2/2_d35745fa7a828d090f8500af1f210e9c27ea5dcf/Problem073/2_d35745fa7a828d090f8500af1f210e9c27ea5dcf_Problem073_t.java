 package euler.level2;
 
 import euler.Problem;
 
 public class Problem073 extends Problem<Integer> {
	private final static int MAX = 12000;
 
 	@Override
 	public Integer solve() {
 		return count(2, 3);
 	}
 
 	private final int count(final int b, final int d) {
 		if (b + d > MAX) {
 			return 0;
 		} else {
 			return 1 + count(b, b + d) + count(b + d, d);
 		}
 	}
 }
