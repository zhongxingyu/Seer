 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package pjwiki;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author aq615c
  */
 public class WikiSyntaxParserHeadersTest {
 
     public WikiSyntaxParserHeadersTest() {
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
      * Test of prepare method, of class WikiSyntaxParserHeaders.
      */
     @Test
     public void testExecute() {
         System.out.println("prepare");
         String text = 
                 "======Test======\r\n" +
                 "test test\r\n" +
                 "==== testing  ====   \r\n" +
                 "test";
         WikiSyntaxParserHeaders instance = new WikiSyntaxParserHeaders();
         String expResult = 
                 "<div id=\"TOC\">\r\n" +
                 "  * [[#test|Test]]\r\n" +
                 "      * [[#test_testing|testing]]\r\n" +
                 "</div>\r\n" +
                "<a name=\"test\"><h1>Test</h1></a>\r\n" +
                 "test test\r\n" +
                "<a name=\"test_testing\"><h3>testing</h3></a>\r\n" +
                 "test";
         String result = instance.execute(text);
         System.out.println(expResult);
         System.out.println(result);
         assertEquals(expResult, result);
         // TODO review the generated test code and remove the default call to fail.
     }
 }
