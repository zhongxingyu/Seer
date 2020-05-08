 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package issa.w6.college;
 
 import issa.w6.college.chainOfResponsibility.Manager;
 import issa.w6.college.chainOfResponsibility.Handler;
 import issa.w6.college.chainOfResponsibility.Emporer;
 import issa.w6.college.chainOfResponsibility.President;
 import issa.w6.college.chainOfResponsibility.Director;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author jappie
  */
 public class HandlerTest {
 	
 	public HandlerTest() {
 	}
 	
 	@BeforeClass
 	public static void setUpClass() {
 	}
 	
 	@AfterClass
 	public static void tearDownClass() {
 	}
 	
 	@Before
 	public void setUp() {
 	}
 	
 	@After
 	public void tearDown() {
 	}
 
 	/**
 	 * Test of getSucessor method, of class Handler.
 	 */
 	@Test
 	public void testGetSucessor() {
 		System.out.println("getSucessor");
 		Handler man = new Manager();
 		Handler dir = new Director();
 		Handler pres = new President();
 		Handler emp = new Emporer();
 		
 		man.setSucessor(dir);
 		dir.setSucessor(pres);
 		
		assertEquals("issa.w6.college.Manager handles", man.HandleRequest(10));
		assertEquals("issa.w6.college.Director handles", man.HandleRequest(9999));
		assertEquals("issa.w6.college.President handles", man.HandleRequest(99999));
		assertEquals("issa.w6.college.Emporer handles", man.HandleRequest(9999999));
		assertEquals("issa.w6.college.Emporer handles", man.HandleRequest(Integer.MAX_VALUE));
 	}
 }
