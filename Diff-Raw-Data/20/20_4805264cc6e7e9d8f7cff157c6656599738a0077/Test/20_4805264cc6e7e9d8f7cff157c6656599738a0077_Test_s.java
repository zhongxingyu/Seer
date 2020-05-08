 /**
  * Test.java
  */
 package artiano.core.test;
 
 import artiano.core.Matrix;
 import artiano.core.Range;
 
 /**
  * <p>Description:</p>
  * @author Nano.Michael
  * @version 1.0.0
  * @date 2013-8-23
  * @author (latest modification by Nano.Michael)
  * @since 1.0.0
  */
 public class Test {
 	static double[] A =
 	   {0,1,2,3,4,
 		5,6,7,8,9,
 		10,11,12,13,14,
 		15,16,17,18,19
 	   };
 	
 	static double[] B =
 		{0,1,2,3,4,
 		5,6,7,8,9,
 		10,11,12,13,14,
 		15,16,17,18,19
 	   };
 	
 	static void printMatrix(Matrix m){
 		System.out.println("-----------------------------");
 		for (int i = 0; i < m.rows(); i++){
 			for (int j = 0; j < m.columns(); j++)
 				System.out.print(m.at(i, j) + " ");
 			System.out.println();
 		}
 		System.out.println("-----------------------------");
 	}
 	
 	static void testMatrix(){
 		Matrix m = new Matrix(4, 5, A);
 		printMatrix(m);
 		Matrix q = m.at(new Range(1, 3), new Range(1, 4));
 		printMatrix(q);
 		Matrix z = q.at(Range.all(), new Range(0, 2));
 		printMatrix(z);
 		Matrix x = m.at(Range.all(), new Range(0, 3));
 		printMatrix(x);
 		printMatrix(m.row(2));
 		printMatrix(m.col(2));
 		m.subtract(0, 0, 3);
 		printMatrix(m);
 		Matrix n = m.at(new Range(1, 3), new Range(2, 4));
 		printMatrix(n);
 		Matrix e = new Matrix(2, 2);
 		for (int i = 0; i < n.rows(); i++)
 			for (int j = 0; j < n.columns(); j++)
 				n.set(i, j, 10 + i);
 		printMatrix(n);
 		for (int i = 0; i < e.rows(); i++)
 			for (int j = 0; j < e.columns(); j++)
 				e.set(i, j, i + 1 + j);
 		printMatrix(e);
 	}
 	
 	public static void main(String[] argStrings){
 		testMatrix();
 	}
 }
