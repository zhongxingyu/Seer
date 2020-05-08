 package math;
 
import java.util.Hashtable;
import java.util.Map;
 
 public class Matrix {
	private Map<Integer, Float> matrixValues[] = null;
 	private int m, n;
 
 	public Matrix(int m, int n) {
		matrixValues = (Hashtable<Integer, Float>[]) new Hashtable[m];
 		for (int i=0; i<m; i++) {
			matrixValues[i] = new Hashtable<Integer, Float>();
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
		matrixValues[i].put(j, value);
 	}
 
 }
