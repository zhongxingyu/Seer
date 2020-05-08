 package com.kpro.test;
 
 import com.kpro.main.Gio;
import com.kpro.main.PrivacyAdviser;
 
 import junit.framework.TestCase;
 
 public class LearnAlgSimplerTest extends TestCase {
 
 
 	static Gio theIO;
 //	private static PolicyObject po;	
 
 	
 	protected void setUp() throws Exception {
 
 		System.out.println("testttttt");
 		String[] args = {"e","e"};
 		try {
 			theIO = new Gio(args);
 		} catch (Exception e) {
 			System.err.println("unable to initiatlize. exiting.");
 			e.printStackTrace();
 			System.exit(1);
 		} 
 		theIO.loadDB();
 		
 	}
 	public static void main(String[] args) {
 		System.out.println("sadf");
 	}
 
 	public void testApplyML() {
 		//assertEquals(expected, actual);
 		fail("Not yet implemented");
 	}
 
 	public void testLearnAlgSimpler() {
 		fail("Not yet implemented");
 	}
 }
 
