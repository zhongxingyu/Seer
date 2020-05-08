 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import javax.xml.xpath.XPathExpressionException;
 import org.xml.sax.SAXException;
 
 /**
  * 
  * @author DarkCthulhu
  * Converts xml --> Nested_set_model (generating left and right elements by pre-order traversal)
  */
 public class XmlWalkTest {
 
 	/**
 	 * Tests the XmlWalk Class
 	 * @throws XPathExpressionException 
 	 * @throws IOException 
 	 * @throws SAXException 
 	 */
 	public static void main(String[] args) throws XPathExpressionException, SAXException, IOException {
 		ArrayList<ExtendedNode> result;
 		
 		System.out.println("********* TESTING XML FILE **********");
 		//Using an xml file
 		File xmlFile = new File("src/sampleHtml.xml");
 		XmlWalk xmw = new XmlWalk(xmlFile, new String[]{}); //pass empty string, for no exclusions
 		result = xmw.getResult(); //fetch results
 		for(ExtendedNode en : result){
 			System.out.println(en.getNode().getNodeName() + " --> " + en.getLeft() + " : " + en.getRight());
 		}
 		
 		//using an xml string
 		System.out.println("********* TESTING XML STRING **********");
 		String xmlString = new StringBuilder()
         .append("<html><head><title>")
         .append("ox")
         .append("</title></head>")
         .append("<body><div>")
         .append("<div>1.1</div><div>1.2></div>")
         .append("</div><div>2</div>")
         .append("</body></html>")
         .toString();
 		
		xmw = new XmlWalk(xmlString, new String[]{"html", "title"}); //traversal will skip "html" and "head"
 		result = xmw.getResult(); //fetch results
 		for(ExtendedNode en : result){
 			System.out.println(en.getNode().getNodeName() + " --> " + en.getLeft() + " : " + en.getRight());
 		}
 	}
 
 }
