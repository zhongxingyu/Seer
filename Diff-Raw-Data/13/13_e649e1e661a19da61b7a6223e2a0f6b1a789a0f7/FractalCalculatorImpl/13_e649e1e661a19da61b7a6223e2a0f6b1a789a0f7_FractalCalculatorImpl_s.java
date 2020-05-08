 package tr.fractal;
 
 import tr.fractal.math.Complex;
 import tr.fractal.math.ComplexVector;
 import tr.fractal.math.FractalFormula;
 
 
 public class FractalCalculatorImpl implements FractalCalculator {
 
 	private Complex v1 = Complex.ZERO;
 	private Complex v2 = Complex.ZERO;
 	
 	private int xn;
 	private int yn;
 	
 	private int maxIterations;
 	
 	private boolean recalculate = true;
 	private int[][] fractalMatrix;
 	private FractalFormula formula;
 
 	
 	public FractalCalculatorImpl(FractalFormula formula) {
 		this.formula = formula;
 	}
 
 	public ComplexVector getArea() {
 		return new ComplexVector(v1, v2);
 	}
 	
 	public synchronized void setArea(Complex v1, Complex v2) {
 		if (!this.v1.equals(v1)) {
 			this.v1 = v1;
 			this.recalculate = true;
 		}
 		if (!this.v2.equals(v2)) {
 			this.v2 = v2;
 			this.recalculate = true;
 		}
 	}
 	
 	public synchronized void setArea(ComplexVector vector) {
 		setArea(vector.getV1(), vector.getV2());
 	}
 
 	public synchronized void setGranularity(int xn, int yn) {
 		boolean recalc = false;
 		if (this.xn != xn) {
 			this.xn = xn;
 			recalc = true;
 		}
 		if (this.yn != yn) {
 			this.yn = yn;
 			recalc = true;
 		}
 		if (recalc) {
 			fractalMatrix = new int[xn][yn];
 			recalculate = true;
 		}
 	}
 
 	public int getMaxIterations() {
 		return maxIterations;
 	}
 	public synchronized void setMaxIterations(int maxIterations) {
 		if (this.maxIterations != maxIterations) {
 			this.maxIterations = maxIterations;
 			recalculate = true;
 		}
 	}
 
 	public synchronized int[][] calculate() {
 		if (recalculate) {
 			recalculate = false;
 			
 			double a1 = v1.getA();
 			double b1 = v1.getB();
 			double a2 = v2.getA();
 			double b2 = v2.getB();
 			
 			double dx = (a2 - a1) / xn;
 			double dy = (b2 - b1) / yn;
 			
 			double ai = a1;
 			for (int i = 0; i < xn; i++, ai += dx) {
 				double bi = b1;
 				for (int j = 0; j < yn; j++, bi += dy) {
 					
 					fractalMatrix[i][j] = formula.calculate(ai, bi, maxIterations);
 				}
 				
 			}
 		}
 		return fractalMatrix;
 	}
 
 	public int getMatrixItem(int mx, int my) {
		return fractalMatrix[mx][my];
 	}
 }
