 package matrixLib;
 
 public class Demo {
 
 	public static void test_eigen() throws NotSquareException, DimensionMismatchException {
 		//ComplexNumber[][] z = {{new ComplexNumber(1,0), new ComplexNumber(1,1)},{new ComplexNumber(2,-1),new ComplexNumber(3,0)}};
 		ComplexNumber[][] z = {{new ComplexNumber(0,1), new ComplexNumber(2, 0)},{new ComplexNumber(1,0),new ComplexNumber(1,1)}};
 		SquareMatrix zm = new SquareMatrix(z);
 		ComplexNumber[] evals = zm.eigenvalues();
 		System.out.println(evals[0] + " " + evals[1]);
 	}
 	
 	public static void test_proj() throws DimensionMismatchException {
 		ComplexNumber[] a = {new ComplexNumber(2,1), new ComplexNumber(3, -1)};
 		ComplexNumber[] b = {new ComplexNumber(1,1), new ComplexNumber(4, -1)};
 		Vector v1 = new Vector(a);
 		Vector v2 = new Vector(b);
 
 		System.out.println("dot: " + v1.dot(v2));
 		System.out.println("dot: " + v2.dot(v2));
 		
 		// projection of v1 onto v2
 		System.out.println(v1.proj(v2));
 	}
 	
 	public static void test_dot() throws DimensionMismatchException {
 		
 		ComplexNumber[] a = {new ComplexNumber(1,1),new ComplexNumber(2,1)};
 		ComplexNumber[] b = {new ComplexNumber(3,-1), new ComplexNumber(4,1)};
 		
 		System.out.println((new Vector(a)).dot(new Vector(b)));
 	}
 	
 	public static void test_qr() throws NotSquareException, DimensionMismatchException {
 		float[][] c = {{1,2},{3,4}};
 		SquareMatrix m = new SquareMatrix(c);
 		
 		//Matrix[] qr1 = m.QRDecompose();
 		//System.out.println(qr1[0]);
 		//System.out.println(qr1[1]);
 		
 		//ComplexNumber[][] z = {{new ComplexNumber(1,0), new ComplexNumber(1,1)},{new ComplexNumber(2,-1),new ComplexNumber(3,0)}};
 		ComplexNumber[][] z = {{new ComplexNumber(0,1), new ComplexNumber(2, 0)},{new ComplexNumber(1,0),new ComplexNumber(1,1)}};
 		SquareMatrix zm = new SquareMatrix(z);
 		
 		Matrix[] qr = zm.QRDecompose();
 		System.out.println(qr[0]);
 		System.out.println(qr[1]);
 	}
 	
 	public static void test_cholesky() throws NotSquareException, DimensionMismatchException {
 		
 		float[][] f = {{2,-1,0},{-1,2,-1},{0,-1,2}};
 		SquareMatrix m = new SquareMatrix(f);
 		
 		SquareMatrix L = m.choleskyDecompose();
 		System.out.println(L);
 		System.out.println(L.multiply(L.conjugateTranspose()));
 	}
 	
 	public static void test_det() throws NotSquareException {
 		float[][] f = {{1,2,3},{4,5,6},{7,8,7}};
 		float[][] g = {{1,2},{3,4}};
 		float[][] h = {{3,0,6,-3},{0,2,3,0},{-4,-7,2,0},{2,0,1,10}};
 		SquareMatrix m = new SquareMatrix(f);
 		SquareMatrix t = new SquareMatrix(g);
 		SquareMatrix u = new SquareMatrix(h);
 		
 		System.out.println("det(f) = " + m.determinant());
 		System.out.println("det(g) = " + t.determinant());
 		System.out.println("det(I_9) = " + (new SquareMatrix(9)).determinant());
 		System.out.println("det(h) = " + u.determinant());
 	}
 	
 	public static void test_lu() throws NotSquareException, DimensionMismatchException, SingularMatrixException {
 		float[][] a = {{1,2},{3,4}};
 		SquareMatrix m = new SquareMatrix(a);
 		
 		Matrix[] lu = m.luDecompose();
 		
 		System.out.println(lu[0]);
 		System.out.println(lu[1]);
 	}
 	
 	public static void test_norm() {
 		ComplexNumber[] a = {new ComplexNumber(1,0),new ComplexNumber(0,1)};
 		Vector v = new Vector(a);
 		System.out.println(v.normalize());
 	}
 	
 	public static void test_rref() throws DimensionMismatchException {
 		float[][] f = {{1,2,3},{4,5,6},{7,8,7}};
 		Matrix m = new Matrix(f);
 		System.out.println(m.rref());
 		//System.out.println("rank: " + m.rank());
 		//System.out.println("nullity: " + m.nullity());
 	}
 	
 	public static void test_inverse() throws NotSquareException, DimensionMismatchException {
 		float[][] f = {{1,0},{-3,1}};
 		SquareMatrix m = new SquareMatrix(f);
 		System.out.println(m.inverse());
 	}
 	
 	public static void main(String[] args) throws Exception {
 		
		ComplexNumber c = new ComplexNumber(5,0);
		ComplexNumber z = new ComplexNumber(2,3);
		
		System.out.println(c.reciprocal());
		System.out.println(z.reciprocal());
		
		
		//test_det();
 		//test_rref();
 		//test_lu();
 		//test_inverse();
 		//test_cholesky();
 		//test_qr();
 		//test_proj();
 		//test_dot();
 		//test_eigen();
 	}
 
 }
