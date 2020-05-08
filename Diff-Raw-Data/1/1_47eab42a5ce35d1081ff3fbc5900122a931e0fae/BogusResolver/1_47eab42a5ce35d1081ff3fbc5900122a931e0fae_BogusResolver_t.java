 import java.io.ByteArrayInputStream;
 
 import javax.xml.stream.XMLResolver;
 import javax.xml.stream.XMLStreamException;
 
 public class BogusResolver implements XMLResolver {
     public Object resolveEntity(String publicID, String systemID, String baseURI, String namespace) throws XMLStreamException
     {
         return new ByteArrayInputStream("".getBytes());
     }
 }
