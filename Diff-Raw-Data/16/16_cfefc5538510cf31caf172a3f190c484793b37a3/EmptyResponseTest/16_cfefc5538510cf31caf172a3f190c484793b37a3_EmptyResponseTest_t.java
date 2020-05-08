 /**
  * 
  */
 package net.skyebook.betaville.json;
 
 import java.util.List;
 
 import edu.poly.bxmc.betaville.model.Design;
 
 /**
  * @author Skye Book
  *
  */
 public class EmptyResponseTest {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JSONClientManager jcm = new JSONClientManager();
 		long startTime = System.currentTimeMillis();
		List<Design> designs = jcm.findAllDesignsByCity(2);
 		long endTime = System.currentTimeMillis();
		System.out.println(designs.size());
 		System.out.println("Took " + (endTime - startTime) + " ms");
 	}
 
 }
