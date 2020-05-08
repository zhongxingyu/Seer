 package name.vysoky.epub;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * If this is not set to XML reader, than XHTML DOCTYPE cause parser freeze!
  * @author Jiri Vysoky
  */
 public class EpubEntityResolver implements EntityResolver {
 
     final Logger logger = LoggerFactory.getLogger(EpubEntityResolver.class);
 
     @Override
     public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
         logger.info("Resolve entity - publicId: " + publicId + ", systemId: " + systemId);
        String resource = "/epubdtd/" + systemId.substring(systemId.lastIndexOf('/'));
         InputStream stream = getClass().getResourceAsStream(resource);
        if (stream != null) {
            return new InputSource(stream);
        } else {
            logger.warn("Stream is null!");
        }
         // default - null?
         return new InputSource(new java.io.StringReader(""));
     }
 
 }
