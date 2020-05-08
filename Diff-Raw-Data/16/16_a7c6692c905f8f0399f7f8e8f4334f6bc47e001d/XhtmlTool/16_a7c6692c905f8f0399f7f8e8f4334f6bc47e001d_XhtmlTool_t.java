 package name.vysoky.xhtml;
 
 import name.vysoky.epub.EpubEntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.helpers.XMLReaderFactory;
 
import javax.xml.parsers.ParserConfigurationException;
 import java.io.*;
 
 /**
  * Static library for XHTML files.
  * @author Jiri Vysoky
  */
 @SuppressWarnings("unused")
 public class XhtmlTool {
 
     /**
      * Extract plain text from XHTML input stream.
      * @param inputStream XHTML input stream
      * @param stringBuilder string builder where text has been append
      * @throws IOException common IO exception
      * @throws SAXException thrown when XHTML is not well formatted
      */
    public static void extractPlainText(InputStream inputStream, StringBuilder stringBuilder) throws IOException, SAXException, ParserConfigurationException {
         XhtmlTextHandler handler = new XhtmlTextHandler(stringBuilder);
         XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setFeature("http://xml.org/sax/features/validation", false);
         reader.setEntityResolver(new EpubEntityResolver());
         reader.setContentHandler(handler);
         reader.setErrorHandler(handler);
         reader.parse(new InputSource(inputStream));
     }
 
     /**
      * Extract plain text from XHTML input stream.
      * @param file XHTML file
      * @param stringBuilder string builder where text has been append
      * @throws IOException common IO exception
      * @throws SAXException thrown when XHTML is not well formatted
      */
    public static void extractPlainText(File file, StringBuilder stringBuilder) throws IOException, SAXException, ParserConfigurationException {
         InputStream inputStream = new BufferedInputStream(new FileInputStream(file));
         extractPlainText(inputStream, stringBuilder);
     }
 }
