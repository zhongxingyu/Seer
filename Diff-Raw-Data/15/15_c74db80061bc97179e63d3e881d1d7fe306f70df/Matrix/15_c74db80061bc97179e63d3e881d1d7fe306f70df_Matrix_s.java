 package math;
 
import java.util.List;
 
 public class Matrix {
	private List<Float> matrixValues[] = null;
 	private int m, n;
 
 	public Matrix(int m, int n) {
		matrixValues = (java.util.Vector<Float>[]) new java.util.Vector[m];
 		for (int i=0; i<m; i++) {
			matrixValues[i] = new java.util.Vector<Float>(n);
 		}
 		this.m = m;
 		this.n = n;
 	}
 
 	public int getM() {
 		return m;
 	}
 
 	public int getN() {
 		return n;
 	}
 
 	public float getValue(int i, int j) {
 		return matrixValues[i].get(j);
 	}
 
 	public void setValue(int i, int j, float value) {
		matrixValues[i].add(j, value);
 	}
 
 }
