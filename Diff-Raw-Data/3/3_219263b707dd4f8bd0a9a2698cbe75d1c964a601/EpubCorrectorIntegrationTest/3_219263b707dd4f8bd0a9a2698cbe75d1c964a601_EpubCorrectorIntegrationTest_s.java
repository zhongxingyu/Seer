 package name.vysoky.epub;
 
 import org.junit.Test;
 
 import java.io.File;
 import java.util.Locale;
 
 /**
  * Corrector tests.
  * @author Jiri Vysoky
  */
 public class EpubCorrectorIntegrationTest {
 
     @Test
     public void testCorrect() {
         try {
             String sampleDirectoryPath = System.getProperty("sampleDirectoryPath");
             File directory = new File(sampleDirectoryPath);
             Locale locale = new Locale("cs", "CZ");
             EpubTool epubTool = new EpubTool(directory);
             EpubCorrector corrector = new EpubCorrector(epubTool, locale);
            corrector.correct();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
