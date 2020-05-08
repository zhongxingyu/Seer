 package brown.puzzles.breathalyzer;
 
 
 /**
  * @author Matt Brown
  * @date Feb 23, 2010
  *
  */
 public class Levenshtein {
 
 	/**
 	 * Stolen from <a
 	 * href="http://en.wikipedia.org/wiki/Levenshtein_distance">Wikipedia
 	 * article on Levenshtein distance</a>
 	 * 
 	 * @param s
 	 * @param t
 	 * @return
 	 */
	public static int score(String a, String b) {

		String s = a.toLowerCase();
		String t = b.toLowerCase();
 
 		final int m = s.length();
 		final int n = t.length();
 
 		final int[][] d = new int[m + 1][n + 1];
 
 		for (int i = 0; i <= m; i++) {
 			d[i][0] = i;
 		}
 
 		for (int j = 0; j <= n; j++) {
 			d[0][j] = j;
 		}
 
 		for (int j = 1; j <= n; j++) {
 			for (int i = 1; i <= m; i++) {
				char x = s.charAt(i - 1);
				char y = t.charAt(j - 1);
				if (x == y) {
 					d[i][j] = d[i - 1][j - 1];
 				}
 				else{
 					d[i][j] = min(d[i - 1][j] + 1, d[i][j - 1] + 1, d[i - 1][j - 1] + 1);
 				}
 			}
 		}
 
		return d[m][n];
 	}
 
 	private static int min(int a, int b, int c) {
 		return Math.min(Math.min(a, b), c);
 	}
 }
