 package Analyzer.code.java.metrics;
 
 import Analyzer.code.FileLoader;
 import Analyzer.code.java.parse.CodeParse;
 import java.io.File;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class ClassMetricsTest {
 
     private ClassMetrics codeTest;
 
     public ClassMetricsTest() {
         FileLoader fileLoader = new FileLoader("." + File.separator + "test" + File.separator + "TestFiles" + File.separator + "CodeFile0.java");
         CodeParse codeParse = new CodeParse(fileLoader.toArray());
         codeParse.splitCode();
         Metrics[] metricsCalculators = codeParse.getMetricsList();
         codeTest = (ClassMetrics) metricsCalculators[1];
     }
 
 // TODO Hacer Test que faltan
     @Test
     public void testAddMethod() {
     }
 
     @Test
     public void testGetNumberOfMethod() {
     }
 
     @Test
     public void testGetNumberOfAttribute() {
         assertEquals(10.0, codeTest.getNumberOfAttribute(), 0.0);
     }
 
     @Test
     public void testGetLackOfCohesion() {
         assertEquals(0.8, codeTest.getLackOfCohesion(), 0.001);
     }
 }
