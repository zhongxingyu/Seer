 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Scanner;
 
 // Minimum Scalar Product
 public class A
 {
 	static String FILE_NAME = "A-sample";
//	static String FILE_NAME = "A-small-attempt0";
//	static String FILE_NAME = "A-large";
 
 	// helper function
	static int scalarProd(List<Integer> v1, List<Integer> v2)
 	{
		int sum = 0;
 		for (int i = 0; i < v1.size(); i++) {
 			sum += v1.get(i) * v2.get(i);
 		}
 		return sum;
 	}
 	
 	// main processing function - takes structured case input and returns structured output
	static int minScalarProd(List<Integer> v1, List<Integer> v2)
 	{
 		Collections.sort(v1);
 		Collections.sort(v2);
 		Collections.reverse(v2);
 		return scalarProd(v1, v2);
 	}
 	
 	public static void main(String[] args)
 	{
 		Scanner sc = null;
 		PrintWriter pw = null;
 		try {
 			sc = new Scanner(new File(FILE_NAME+".in"));
 			pw = new PrintWriter(FILE_NAME+".out");
 			int nCases = sc.nextInt();
 			for (int c = 1; c <= nCases; c++) {
 				/* CASE BEGINS */
 				
 				// get input
 				int len = sc.nextInt();
 				
				List<Integer> v1 = new ArrayList<Integer>(len);
 				for (int i = 0; i < len; i++) {
					v1.add(sc.nextInt());
 				}
 				
				List<Integer> v2 = new ArrayList<Integer>(len);
 				for (int i = 0; i < len; i++) {
					v2.add(sc.nextInt());
 				}
 				
 				// process
				int result = minScalarProd(v1, v2);
 				
 				// output
 				pw.printf("Case #%d: %d\n", c, result);
 				
 				/* CASE ENDS */
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} finally {
 			sc.close();
 			pw.close();
 		}
 	}
 }
