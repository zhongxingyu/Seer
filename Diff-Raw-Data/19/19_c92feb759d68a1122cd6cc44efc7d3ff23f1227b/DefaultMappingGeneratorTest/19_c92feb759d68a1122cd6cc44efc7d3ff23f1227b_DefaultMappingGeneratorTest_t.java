 package soapproxy.application.mapping;
 
 import org.apache.xmlbeans.XmlObject;
import org.custommonkey.xmlunit.Diff;
import org.dom4j.dom.DOMElement;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import static org.junit.Assert.*;
 
 public class DefaultMappingGeneratorTest {
  //@Test
   public void testGenerateMappingWithPrimitiveType() throws Exception {
     String operationName = "sayHello";
     String mappingResultFile = "helloTestMapping.xml";
     testGenerateMappings(operationName, mappingResultFile);
   }
 
  //@Test
   public void testGenerateMappingWithPartReferringElement() throws Exception {
     String operationName = "sayHelloToAll";
     String mappingResultFile = "helloToAllTestMapping.xml";
     testGenerateMappings(operationName, mappingResultFile);
   }
 
   private void testGenerateMappings(String operationName, String mappingResultFile) throws Exception {
     String path = this.getClass().getResource(".").getPath().replace("%20", " ");
     String wsdlUri = path + "/helloTest.wsdl";
    String expectedResultMapping = getFileContent(path + "/" + mappingResultFile);
 
     DefaultMappingGenerator mappingGenerator = new DefaultMappingGenerator();
    String actualResultMapping = mappingGenerator.getMapping(wsdlUri, operationName, "defaultSchema.js");
    Diff xmlDiff = new Diff(actualResultMapping, expectedResultMapping);
    assertTrue(xmlDiff.toString(), xmlDiff.similar());
   }
 
   private String getFileContent(String pathname) throws IOException {
     File file = new File(pathname);
     BufferedReader buff = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
     String nextLine;
     StringBuilder sb = new StringBuilder();
     while (true) {
       nextLine = buff.readLine();
       if (nextLine != null) {
         sb.append(nextLine);
       }
       else {
         break;
       }
     }
     return sb.toString();
   }
 }
