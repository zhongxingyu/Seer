 package convolution;
 
 import jama.Matrix;
 
 public class Convolution {
 	public static final int ITERATIONS = 200;
 	
 	public static void main(String[] args) {
 		Matrix y0LinearCombo = new Matrix(new double[][]{{1.0, 1.0, 0.0, 1.0}});
 		Matrix y1LinearCombo = new Matrix(new double[][]{{1.0, 0.0, 1.0, 1.0}});
 		
 		//Simple
 		Matrix x = new Matrix(new double[][]{{1, 0, 1, 1, 0}});
 		Matrix a0 = createA(x, y0LinearCombo);
 		Matrix a1 = createA(x, y1LinearCombo);
 		Matrix y0 = convolude(x, a0);
 		Matrix y1 = convolude(x, a1);
		y0.print(10, 0);
		jacobiIteration(a0, y0).print(10, 2);
 		//y1.print(10, 0);
 		
 		//Part I
 		x = new Matrix(new double[][]{{1,0,1,1,1,0,1,1,1,0,0,0,1,1,1,1,0,1,0,1,0}});
 		a0 = createA(x, y0LinearCombo);
 		a1 = createA(x, y1LinearCombo);
 		y0 = convolude(x, a0);
 		y1 = convolude(x, a1);
 		//y0.print(10, 0);
 		//y1.print(10, 0);
 	}
 	
 	public static Matrix convolude(Matrix x, Matrix a) {
 		//Pad the X vector with zeros so it matches up with A
 		double[][] newXArray = new double[1][a.getColumnDimension()];
 		for (int i = 0; i < newXArray[0].length; i++) {
 			if (i < x.getColumnDimension()) {
 				newXArray[0][i] = x.get(0, i);
 			} else {
 				newXArray[0][i] = 0;
 			}
 		}
 		Matrix newX = new Matrix(newXArray);
 		
 		//Multiply and perform binary addition
 		Matrix y = a.times(newX.transpose());
 		for (int i = 0; i < y.getRowDimension(); i++) {
 			int value = (int)y.get(i, 0);
 			if (value == 2) {
 				y.set(i, 0, 0);
 			} else if (value == 3) {
 				y.set(i, 0, 1);
 			}
 		}
 		
 		return y;
 	}
 	
 	public static Matrix createA(Matrix x, Matrix linearCombo) {
 		int sizeOfA = x.getColumnDimension() + linearCombo.getColumnDimension() - 1;
 		
 		Matrix a = new Matrix(sizeOfA, sizeOfA, 0);
 		
 		int padding = 1 - linearCombo.getColumnDimension();
 		for (int r = 0; r < a.getRowDimension(); r++) {
 			for (int i = 0; i < linearCombo.getColumnDimension(); i++) {
 				if (i + padding >= 0) {
 					a.set(r, i + padding, linearCombo.get(0, i));
 				}
 			}
 			padding++;
 		}
 		
 		return a;
 	}
 	
 	public static Matrix jacobiIteration(Matrix a, Matrix b) {
 		Matrix s = Matrix.identity(a.getRowDimension(), a.getColumnDimension());
		s.setMatrix(0, 1, 0, a.getColumnDimension() - 1, a);
 		
 		Matrix t = s.minus(a);
 		
 		Matrix x = new Matrix(a.getRowDimension(), 1, 0);
 		Matrix xPlus = (Matrix)x.clone();
 		
 		for (int i = 0; i < ITERATIONS; i++) {
 			xPlus = t.times(x).plus(b);
 			
 			for (int j = 0; j < xPlus.getRowDimension(); j++) {
 				xPlus.set(j, 0, xPlus.get(j, 0) / s.get(j, j));
 			}
 			
 			x = (Matrix)xPlus.clone();
 		}
 		
 		return x;
 	}
 }
