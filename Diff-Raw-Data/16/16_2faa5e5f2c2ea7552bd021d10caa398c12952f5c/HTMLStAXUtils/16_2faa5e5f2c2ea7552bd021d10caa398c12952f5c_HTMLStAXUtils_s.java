 package iudex.html.tree;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.transform.Source;
 
 import com.gravitext.xml.tree.Element;
 import com.gravitext.xml.tree.StAXUtils;
 
 public class HTMLStAXUtils extends StAXUtils
 {
     public static Element readDocument( XMLStreamReader sr )
         throws XMLStreamException
     {
         return new HTMLStAXConsumer().readDocument( sr );
     }
 
     public static Element staxParse( Source source )
         throws XMLStreamException
     {
         return readDocument( staxReader( source ) );
     }
 }
