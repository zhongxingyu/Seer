 package codejam.lib.dp;
 
 public class KnapSack {
 	public static void main(String[] args) {
 
 		int max = solve(50, new int[] { 10, 20, 30 },
 				new int[] { 60, 100,
 				120 });
 		System.out.println(max);
 
		max = solve(100, new int[] { 10, 20, 30, 40, 20 }, new int[] { 60, 100,
				120, 10, 110 });
		System.out.println(max);

 	}
 
 	public static int solve(int W, int[] weight, int[] value) {
 		int n = weight.length + 1;
		int t[][] = new int[n][W + 1];
 
		// prefix based
 		for (int i = 0; i < W; i++) {
 			t[n - 1][i] = 0;
 		}
 		for (int i = 0; i < n; i++) {
 			t[i][0] = 0;
 		}
 
 		for (int i = n - 2; i >= 0; i--) {
			for (int j = W; j >= 0; j--) {
 				t[i][j] = t[i + 1][j];
 				if (j - weight[i] >= 0) {
 					t[i][j] = Math.max(t[i][j], t[i + 1][j - weight[i]]
 							+ value[i]);
 				}
				System.out.println(i + "," + j + "=" + t[i][j]);
 			}
 		}
 

		return t[0][W];
	}

	public static int solveRecursive(int S, int[] weight, int[] value) {
		return 1;
 	}
 
 }
