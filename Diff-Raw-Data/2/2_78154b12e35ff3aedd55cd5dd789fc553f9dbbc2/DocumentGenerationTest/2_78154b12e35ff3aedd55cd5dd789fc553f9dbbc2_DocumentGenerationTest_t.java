 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 import models.generate.Document;
 import models.generate.DocumentGenerator;
 import org.junit.Test;
 import play.Play;
 import play.test.UnitTest;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author Entwickler
  */
 public class DocumentGenerationTest extends UnitTest {
 
     private final String applicationPath = Play.applicationPath.getAbsolutePath();
 
     @Test
     public void testSimpleGeneration() {
         File templateFile = new File(applicationPath + "/data/test/SimpleDocument.txt");
        Map<String, String> replaceMap = new HashMap<String, String>();
         replaceMap.put("%name%", "John");
 
         DocumentGenerator generator = new DocumentGenerator(templateFile, replaceMap);
         Document document = generator.create();
 
         assertTrue("Document creation failed.", document != null);
         assertEquals("Substition failed", document.getContent(), "My name is John!");
 
         File file = document.getFile();
         file.delete();
     }
 }
