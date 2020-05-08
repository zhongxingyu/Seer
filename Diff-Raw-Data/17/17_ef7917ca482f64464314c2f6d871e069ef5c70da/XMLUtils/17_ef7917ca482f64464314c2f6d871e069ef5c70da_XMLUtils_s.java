 package cs3013;
 
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 
 public final class XMLUtils {
 	public static void skipElement(XMLStreamReader parser) throws XMLStreamException {
 		int numEndTags = 1;
 		while(numEndTags > 0) {
 			switch(parser.next()) {
 			case XMLStreamConstants.START_ELEMENT:
 				++numEndTags;//go down
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				--numEndTags;//go up
 				break;
 			}
 		}
 	}
 
 	public static void dumpXML(XMLStreamReader parser) throws XMLStreamException {
		int depth = 1;
		while(depth > 0) {
 			switch(parser.getEventType()) {
 			case XMLStreamConstants.START_ELEMENT:
 				for(int i = 1; i < depth; ++i) {
 					System.out.print("    ");
 				}
 				System.out.print("<");
 				System.out.print(parser.getLocalName());
 				for(int i = 0; i < parser.getAttributeCount(); ++i) {
 					System.out.print(" ");
 					System.out.print(parser.getAttributeLocalName(i));
 					System.out.print("=\"");
 					System.out.print(parser.getAttributeValue(i));
 					System.out.print("\"");
 				}
 				System.out.println(">");
 
 				++depth;
 				break;
 			case XMLStreamConstants.END_ELEMENT:
 				--depth;
 				for(int i = 1; i < depth; ++i) {
 					System.out.print("    ");
 				}
 				System.out.print("</");
 				System.out.print(parser.getLocalName());
 				System.out.println(">");
 				break;
 			case XMLStreamConstants.CHARACTERS:
 				for(int i = 1; i < depth; ++i) {
 					System.out.print("    ");
 				}
 				
 				System.out.println(parser.getText());
 				break;
 			}
			parser.next();
		}
 	}
 }
