 package euler.level4;
 
 import euler.Problem;
 
public class Problem160 extends Problem<Long> {
 	@Override
	public Long solve() {
 		long value = 1;
		for (long i = 1; i <= 2560000; i++) {
			long m = i;
 			while (m % 5 == 0) {
 				m /= 5;
 				value /= 2;
 			}
			value = (value * m) % 100000;
 		}
		return value;
 	}
 }
