 package net.sf.jooreports.templates;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
import java.util.Locale;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.xml.sax.SAXException;
 
 public abstract class AbstractTemplateTest extends TestCase {
 
 	private DocumentTemplateFactory documentTemplateFactory;
 
 	protected void setUp() throws Exception {
 		documentTemplateFactory = new DocumentTemplateFactory();
		documentTemplateFactory.getFreemarkerConfiguration().setLocale(Locale.US);
 	}
 	
 	protected File getTestFile(String fileName) {
 		return new File("src/test/resources", fileName);
 	}
 
     protected static File createTempFile(String extension) throws IOException {
         File tempFile = File.createTempFile("document", "."+ extension);
         tempFile.deleteOnExit();
         return tempFile;
     }
 
     protected static void assertFileCreated(File file) {
         assertTrue("file created", file.isFile() && file.length() > 0);
     }
 
     protected String extractTextContent(File openDocumentFile) throws IOException {
 		try {
 			return TextExtractor.extractTextAsString(openDocumentFile).trim();
 		} catch (SAXException saxException) {
 			throw new RuntimeException(saxException);
 		}
 	}
 
     protected String processTemplate(File templateFile, Map model) throws IOException, DocumentTemplateException {
     	File openDocumentFile = createTempFile("odt");
         DocumentTemplate template = documentTemplateFactory.getTemplate(templateFile);
         template.createDocument(model, new FileOutputStream(openDocumentFile));
         assertFileCreated(openDocumentFile);
         return extractTextContent(openDocumentFile);
     }
 
 }
