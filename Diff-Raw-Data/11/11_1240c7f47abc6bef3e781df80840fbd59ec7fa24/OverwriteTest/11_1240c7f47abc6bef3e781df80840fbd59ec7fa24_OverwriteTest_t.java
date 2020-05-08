 package org.codehaus.xfire.generator;
 
 import java.io.File;
 
 import org.codehaus.xfire.gen.Wsdl11Generator;
 
 import com.sun.codemodel.JCodeModel;
 import com.sun.codemodel.JDefinedClass;
 
 public class OverwriteTest
     extends GenerationTestSupport
 {   
     public void testOneWay() throws Exception
     {
         System.out.println("WRITE #1");
         Wsdl11Generator generator = new Wsdl11Generator();
         generator.setWsdl(getTestFilePath("src/wsdl/oneway.wsdl"));
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setBinding("jaxb");
         generator.setOverwrite(true);
         
         generator.generate();
         
         JCodeModel model = generator.getCodeModel();
         JDefinedClass echo = model._getClass("org.codehaus.xfire.test.echo.SendMessagePortType");
         assertNotNull(echo);
         
         File file = getTestFile("target/test-services/org/codehaus/xfire/test/echo/SendMessageImpl.java");
         assertTrue(file.exists());
         long lastModified = file.lastModified();
        Thread.sleep(100);
         System.out.println("WRITE #2");
         generator = new Wsdl11Generator();
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setWsdl(getTestFilePath("src/wsdl/oneway.wsdl"));
         generator.setOverwrite(false);
         generator.generate();
         
         assertEquals(lastModified, file.lastModified());
        Thread.sleep(100);
         System.out.println("WRITE #3");
         generator = new Wsdl11Generator();
         generator.setOutputDirectory(getTestFilePath("target/test-services"));
         generator.setWsdl(getTestFilePath("src/wsdl/oneway.wsdl"));
         generator.setOverwrite(true);
         generator.generate();
         
      /*  System.out.println("l1 " + lastModified);
         System.out.println("l2 " + file.lastModified());
        assertTrue(lastModified < file.lastModified());*/
     }
     
 }
