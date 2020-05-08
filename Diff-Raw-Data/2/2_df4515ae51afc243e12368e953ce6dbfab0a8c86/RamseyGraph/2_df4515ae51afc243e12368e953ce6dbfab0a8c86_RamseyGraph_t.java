 package gtc.assignment.piyush;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 public class RamseyGraph {
 	int n;
 	int MAXE;
 	int MAXC;
 	final int MAX = 10;
 	final int MAXF = 10000;
 	final String filename = "ramseyGraph.txt";
 	int[][][] tf = new int[MAXF][MAX][MAX];
 	int count_g;
 	permatrix isomrphc;
 
 	public RamseyGraph(int x) {
 		n = x;
 		count_g = 0;
 		isomrphc = new permatrix(n);
 		call();
 	}
 
 	void call() {
 		int[][] a = new int[MAX][MAX];
 
 		for (int i = 0; i < n; i++)
 			for (int j = 0; j < i; j++) {
 				a[i][j] = 1;
 				a[j][i] = 1;
 			}
 		MAXC = (int) (n - 1) / 2;
 		System.out.println(MAXC);
 		MAXE = (n * n - n) / 2;
 		if (MAXC == 1) {
 			if (isRamseyGraph(a))
 				addGraph(a);
 		} else {
 			for (int C = 2; C <= MAXC; C++) {
 				int[][] b = new int[n][n];
 				for (int ii = 0; ii < n; ii++) {
 					System.arraycopy(a[ii], 0, b[ii], 0, n);
 				}
 				b[0][1] = C;
 				b[1][0] = C;
 				addEdge(b, 1);
 			}
 		}
 		printRamseyGraphs();
 	}
 
 	/**
 	 * @param args
 	 */
 
 	void addEdge(int[][] a, int e) {
 		int i, j;
 		if (isRamseyGraph(a))
 			addGraph(a);
 
 		if (e < MAXE) {
 			for (i = 0; i < n; i++)
 				for (j = 0; j < i; j++) {
 					if (i != j && a[i][j] == 1) {
 						for (int C = 2; C <= MAXC; C++) {
 							int[][] b = new int[n][n];
 							for (int ii = 0; ii < n; ii++) {
 								System.arraycopy(a[ii], 0, b[ii], 0, n);
 							}
 							b[i][j] = C;
 							b[j][i] = C;
 							addEdge(b, e + 1);
 						}
 					}
 				}
 		}
 	}
 
 	void addGraph(int[][] a) {
 		int c;
 		if (count_g == 0) {
 			for (int ii = 0; ii < n; ii++) {
 				System.arraycopy(a[ii], 0, tf[count_g][ii], 0, n);
 			}
 			count_g++;
 			return;
 		}
 
 		for (c = 0; c < count_g; c++) {
 			int sum = 0;
 			for (int i = 0; i < n; i++) {
 				for (int j = 0; j < i; j++) {
 					if (tf[c][i][j] == a[i][j])
 						sum++;
 				}
 			}
 			if (sum == (n * n - n) / 2) {
 				return;
 			}
 		}
 
 		for (c = 0; c < count_g; c++) {
 			int tf_c = 0, a_c = 0;
 			for (int i = 0; i < n; i++) {
 				for (int j = 0; j < i; j++) {
 					if (tf[c][i][j] == 1)
 						tf_c++;
 					if (a[i][j] == 1)
 						a_c++;
 				}
 			}
 			if (tf_c != a_c) {
 				continue;
 			}
 			if (isomrphc.isomorphic(a, tf[c]) == 0)
 				return;
 		}
 		printGraph(a, "GraphFound");
 		for (int ii = 0; ii < n; ii++) {
 			System.arraycopy(a[ii], 0, tf[count_g][ii], 0, n);
 		}
 		count_g++;
 		return;
 	}
 
 	Boolean isRamseyGraph(int[][] a) {
 		int sum = 0;
 		for (int C = 1; C <= MAXC; C++) {
 			int[][] b = new int[n][n];
 			for (int i = 0; i < n; i++) {
 				for (int j = 0; j < n; j++) {
 					if (a[i][j] == C) {
						b[i][j] = 1;
 					}
 				}
 			}
 			if (!istrainglefree(b))
 				sum++;
 		}
 		if (sum == 1)
 			return true;
 		return false;
 	}
 
 	Boolean istrainglefree(int[][] a) {
 		int i, j, k, sum;
 		int[][] x = new int[n][n];
 		int[][] y = new int[n][n];
 		int trace = 0;
 		for (i = 0; i < n; i++) {
 			for (j = 0; j < n; j++) {
 				sum = 0;
 				for (k = 0; k < n; k++)
 					sum = sum + a[i][k] * a[k][j];
 				x[i][j] = sum;
 			}
 		}
 		for (i = 0; i < n; i++) {
 			for (j = 0; j < n; j++) {
 				sum = 0;
 				for (k = 0; k < n; k++)
 					sum = sum + x[i][k] * a[k][j];
 				y[i][j] = sum;
 			}
 		}
 
 		for (i = 0; i < n; i++) {
 			trace += y[i][i];
 		}
 
 		if (trace == 0)
 			return true;
 		else
 			return false;
 	}
 
 	void printGraph(int[][] a, String s) {
 		System.out.println(s);
 		System.out.print(" | ");
 		for (int i = 0; i < n; i++)
 			System.out.print(i + 1 + " ");
 		System.out.println();
 		for (int i = 0; i < n; i++) {
 			System.out.print(i + 1 + "| ");
 			for (int j = 0; j < n; j++) {
 				System.out.print(a[i][j] + " ");
 			}
 			System.out.println("");
 		}
 	}
 
 	void printRamseyGraphs() {
 		int i, j, k;
 		for (k = 0; k < count_g; k++) {
 			System.out.print(" | ");
 			for (i = 0; i < n; i++)
 				System.out.print(i + 1 + " ");
 			System.out.println();
 			for (i = 0; i < n; i++) {
 				System.out.print(i + 1 + "| ");
 				for (j = 0; j < n; j++) {
 					System.out.print(tf[k][i][j] + " ");
 				}
 				System.out.println("");
 			}
 		}
 		System.out.print("No of Ramsey graphs: " + count_g + "\n");
 	}
 
 	public void writeTextFile(String fileName, String s) {
 		FileWriter output = null;
 		try {
 			output = new FileWriter(fileName);
 			BufferedWriter writer = new BufferedWriter(output);
 			writer.write(s);
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			if (output != null) {
 				try {
 					output.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 	public static void main(String[] args) {
 		RamseyGraph g = new RamseyGraph(5);
 	}
 }
