 
 package org.codemetrics.metricparser;
 
 import java.io.File;
 import java.lang.reflect.Method;
 import org.codemetrics.classloader.ClassLoader;
 import org.codemetrics.classloader.FileCompiler;
 import org.codemetrics.codeline.CodeLineMetric;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author Johanna
  */
 public class MethodMetricParserTest {
     
     private Method[] methods;
     
     @Test
     public void testSomeMethod() {
         MethodMetricParser methodParser = new MethodMetricParser();
         initializeMethods();
         CodeLineMetric metric = methodParser.getCodeLines(getFile(), methods[0]);
         assertTrue(metric.getCommentLines()==1);
         assertTrue(metric.getEmptyLines()==0);
        assertTrue(metric.getEffectiveLines()==6);
         assertTrue(metric.getTotalCodeLines()==7);
     }
     
     private void initializeMethods(){
         FileCompiler compiler = new FileCompiler();
        String outPutFolder = "compiledTestFiles/"; 
 
         if(
           compiler.compileJavaFile(getFile().getAbsolutePath(), outPutFolder) ){
           Class loadedClass = (new ClassLoader()).loadJavaClass(outPutFolder, "integerToStringManually.IntegerToStringManually");
           this.methods = loadedClass.getMethods();
         }
     }
     
     private File getFile() {
         File file = new File
       ("test/org/codemetrics/testFiles/"
                 + "integerToStringManually/IntegerToStringManually.java"); 
         return file;
     }
 }
