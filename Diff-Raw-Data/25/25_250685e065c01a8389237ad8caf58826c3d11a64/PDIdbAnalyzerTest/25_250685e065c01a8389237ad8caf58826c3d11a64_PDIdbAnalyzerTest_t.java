 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package pdidbanalyzer;
 
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
    
    String readFile(String fileName) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(fileName));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return line;
    }
 
     /**
      * Test of main method, of class PDIdbAnalyzer.
      */
     @Test
    public void testMain() throws FileNotFoundException, IOException {
         System.out.println("main");
         String[] args = new String[1];
        args[0] = "-f testdata/1bl0_1_1.ent.gz";
         PDIdbAnalyzer.main(args);
        String expResult = readFile("testdata/1bl0_1_1.dat");
        String result = readFile("1bl0_1_1.dat");
        assertEquals(expResult, result);
     }
 
 }
