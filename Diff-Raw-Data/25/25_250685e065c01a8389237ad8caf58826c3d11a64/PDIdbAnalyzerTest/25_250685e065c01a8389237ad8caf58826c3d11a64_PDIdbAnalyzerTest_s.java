 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package pdidbanalyzer;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author archvile18
  */
 public class PDIdbAnalyzerTest {
 
     public PDIdbAnalyzerTest() {
     }
 
     @BeforeClass
     public static void setUpClass() throws Exception {
     }
 
     @AfterClass
     public static void tearDownClass() throws Exception {
     }
 
     @Before
     public void setUp() {
     }
 
     @After
     public void tearDown() {
     }
 
     /**
      * Test of main method, of class PDIdbAnalyzer.
      */
     @Test
    public void testMain() {
         System.out.println("main");
         String[] args = new String[1];
        args[0] = "-f AsnCytosine.pdb";
         PDIdbAnalyzer.main(args);
     }
 
 }
