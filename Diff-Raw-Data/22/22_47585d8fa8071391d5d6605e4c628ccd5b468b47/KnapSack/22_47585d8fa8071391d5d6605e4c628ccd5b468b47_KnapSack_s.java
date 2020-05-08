 package codejam.lib.dp;
 
 public class KnapSack {
 	public static void main(String[] args) {
 
 		int max = solve(50, new int[] { 10, 20, 30 },
 				new int[] { 60, 100,
 				120 });
 		System.out.println(max);
 
 	}
 
 	public static int solve(int W, int[] weight, int[] value) {
 		int n = weight.length + 1;
		int t[][] = new int[n][W];
 
 		for (int i = 0; i < W; i++) {
 			t[n - 1][i] = 0;
 		}
 		for (int i = 0; i < n; i++) {
 			t[i][0] = 0;
 		}
 
 		for (int i = n - 2; i >= 0; i--) {
			for (int j = W - 1; j >= 0; j--) {
 				t[i][j] = t[i + 1][j];
 				if (j - weight[i] >= 0) {
 					t[i][j] = Math.max(t[i][j], t[i + 1][j - weight[i]]
 							+ value[i]);
 				}
 			}
 		}
 
		return t[0][W - 1];
 	}
 
 }
