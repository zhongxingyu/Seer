 import hopfield.HopfieldNet;
 import hopfield.SynchHopfieldNet;
 
 import java.util.Arrays;
 
 import utils.ImageUtils;
 
 public class Main {
 
 	public static final int numberOfPatterns = 2;
 	
 	public static void main(String[] args) {
 
 		System.out.println("Network will memorize " + numberOfPatterns + " patterns.");
 		int[][] patterns = new int[numberOfPatterns][];
 		int N = getDummyPatterns(patterns);
 
 		HopfieldNet net = new SynchHopfieldNet(N);
 		net.storePatterns(patterns);
 		
 		// For debugging purposes
 //		System.out.println("Pesos: ");
 //		MatrixUtils.print(net.getWeights());
 		
 		// Set here what you want the net to recongnize
 //		int[] recognize = ImageUtils.loadBlackAndWhiteImage("./TPE3/resources/line1.png");
 		int[] recognize = new int[] {1, -1, 1};
 		net.initialize(recognize);
 		int[] ans = net.iterateUntilConvergence(); // Evolve the states until converge
 		
 		System.out.println("Patron devuelto");
 		System.out.println(Arrays.toString(ans));
 		int patternIndex = getMatchingPatternIndex(patterns, ans);
 		if (patternIndex != -1) {
 			System.out.println("El patron introducido se parece al " + patternIndex);
 		} else {
 			System.out.println("El array devuelto no matchea con ningun patron");
 		}
 	}
 	
 	/**
 	 *
 	 * Check if any of the original memorized patterns if equal to the
 	 * the given pattern (usually the network output).
 	 * 
 	 * @patterns: Original patterns matrix.
 	 * @vec: Pattern we want to check for equality.
 	 *
 	 */ 
 	private static int getMatchingPatternIndex(int[][] patterns, int[] vec) {
 		int index = 0;
 		for (int[] pattern: patterns) {
 			if (Arrays.equals(pattern, vec)) {
 				return index;
 			}
 			index++;
 		}
 		return -1;
 	}
 	
 	private static int getImagePatterns(int[][] patterns) {
 		patterns[0] = ImageUtils.loadBlackAndWhiteImage("./TPE3/resources/line1.png");
 		patterns[1] = ImageUtils.loadBlackAndWhiteImage("./TPE3/resources/line2.png");
 //		patterns[2] = ImageUtils.loadBlackAndWhiteImage("./TPE3/resources/line3.png");
 //		patterns[3] = ImageUtils.loadBlackAndWhiteImage("./TPE3/resources/line4.png");
		return (int) Math.sqrt(patterns[0].length);	// should always be 64
 	}
 	
 	private static int getDummyPatterns(int[][] patterns) {
 		patterns[0] = new int[] {1, -1, 1};
 		patterns[1] = new int[] {-1, -1, 1};
 		return patterns[0].length;
 	}
 }
