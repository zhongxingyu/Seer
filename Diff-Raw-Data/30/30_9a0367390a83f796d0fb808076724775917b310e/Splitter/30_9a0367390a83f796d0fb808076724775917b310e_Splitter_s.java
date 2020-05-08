 import java.io.IOException;
 import javax.xml.transform.TransformerConfigurationException;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLFilter;
 import org.xml.sax.XMLReader;
 import org.xml.sax.ext.DefaultHandler2;
 import org.xml.sax.helpers.XMLReaderFactory;
 
 
 public class Splitter {
 	
 	public static void main(String [] args) throws SAXException, IOException, TransformerConfigurationException {
 	
 		DefaultHandler2 dh = new DblpHandler();
 		XMLFilter filter = new PagesFilter();
 		XMLReader xr =	XMLReaderFactory.createXMLReader();		
 		
 		filter.setContentHandler(dh);
 		filter.setParent(xr);
		filter.parse(new InputSource("test.xml"));
 //		xr.setFeature("http://apache.org/xml/features/continue-after-fatal-error", true);
 //		xr.parse(new InputSource("test.xml"));
 //		xr.parse(new InputSource("D:/Uni/DKE Praktikum/dblp.xml"));
 	}
 
 
 }
