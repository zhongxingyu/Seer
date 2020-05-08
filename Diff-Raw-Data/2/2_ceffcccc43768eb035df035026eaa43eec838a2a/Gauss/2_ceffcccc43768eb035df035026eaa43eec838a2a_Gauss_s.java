 package poo.sistema;
 
 import poo.util.Mat;
 
 public class Gauss extends Sistema {
 	protected double[][] a;
 	public Gauss(double[][] a, double[] y) {
 		super(a, y);
 		this.a = new double[a.length][a.length + 1];
 		for (int i = 0; i < a.length; i++) {
 			System.arraycopy(a[i], 0, this.a[i], 0, a.length);
 			this.a[i][a.length] = y[i];
 		}
 	} // Costruttore normale
 	public double[] risolvi() {
 		triangolarizza();
 		return calcoloSoluzione();
 	} // risolvi
 	protected void triangolarizza() {
 		int n = getN();
 		for (int j = 0; j < n; j++) {
 			if (Mat.circaUguali(a[j][j], 0)) { // pivoting
 				int p = j + 1; int pmax = p;
 				// Cerco pivot massimo per minimizzare gli errori di approssimazione
 				for (; p < n; p++)
 					if (Math.abs(a[p][j]) > Math.abs(a[pmax][j])) pmax = p;
				if (pmax == n) throw new SistemaSingolare();
 				double[] tmp = a[j];
 				a[j] = a[pmax];
 				a[pmax] = tmp;
 			}
 			for (int i = j + 1; i < n; i++) {
 				if (!Mat.circaUguali(a[i][j], 0D)) {
 					double c = a[i][j] / a[j][j];
 					for (int k = j; k <= n; k++)
 						a[i][k] -= c * a[j][k];
 				}
 			} // for i
 		} // for j
 	} // triangolarizza
 	protected double[] calcoloSoluzione() {
 		int n = getN();
 		double[] x = new double[n];
 		for (int i = n - 1; i >= 0; i--) {
 			double sm = a[i][n];
 			for (int j = i + 1; j < n; j++)
 				sm -= a[i][j] * x[j];
 			x[i] = sm / a[i][i];
 		}
 		return x;
 	} // calcoloSoluzione
 	public String toString() {
 		int n = getN(); final int ELEM_LENGTH = 10;
 		StringBuilder sb = new StringBuilder(n * n * ELEM_LENGTH);
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++)
 				sb.append(String.format("%8.2f ", a[i][j]));
 			sb.append(String.format("| %1.2f\n", a[i][n]));
 		}
 		return sb.toString();
 	} // toString
 } // Gauss
