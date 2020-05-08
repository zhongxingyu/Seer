 import java.io.File;
 
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
import javax.xml.transform.dom.DOMResult;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.xpath.*;
 
 public class FixStrings {
 
 	private static boolean isOptionInCommandLine(String[] args, String option) {
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].toLowerCase().equals(option))
 				return true;
 		}
 		return false;
 	}
 
 	private static String getOptionInCommandLine(String[] args, int index) {
 		int found = 0;
 		for (int i = 0; i < args.length; i++) {
 			if (args[i].charAt(0) != '-') {
 				if (index == found)
 					return args[i];
 				found++;
 			}
 		}
 		return null;
 	}
 
 	public static void main(String[] args) {
 		int foundStrings = 0;
 		int removedStrings = 0;
 
 		try {
 			if (isOptionInCommandLine(args, "-h") || isOptionInCommandLine(args, "--help")) {
				System.out.println("Usage: FixStrings [OPTION]... <SOURCE_FILE> <TRANSLATED_FILE> [FIXED_FILE]\n");
 				System.out.println("Mandatory arguments to long options are mandatory for short options too.");
 				System.out.println("  -f             fill untraslated string's content adding 'TODO:' prefix.");
 				System.out.println("  -s             show untraslated strings list.");
				System.out.println("  -q		     quiet mode.\n");
 				System.out.println("  -h, --help     this help.\n");
 				System.out.println("SOURCE_FILE      is /res/values/strings.xml source file.");
 				System.out.println("TRANSLATED_FILE  is /res/values-xx/strings.xml translated file.");
 				System.out.println("FIXED_FILE       is filename where to store fixed translated file. If omited will be used TRANSLATED_FILE.");
 				return;
 			}
 
 			boolean showUntranslated = isOptionInCommandLine(args, "-s");
 			boolean fillUntranslated = isOptionInCommandLine(args, "-f");
 			boolean quietMode = isOptionInCommandLine(args, "-q");
 
 			String sourceFilename = getOptionInCommandLine(args, 0);
 			String translatedFilename = getOptionInCommandLine(args, 1);
 			String fixedTranslatedFileName = getOptionInCommandLine(args, 2);
 
 			if (sourceFilename == null) {
 				System.out.println("Error: missing <SOURCE_FILE>\n");
 				System.out.println("Usage: FixStrings [OPTION]... <SOURCE_FILE> <TRANSLATED_FILE> [FIXED_FILE]");
 				return;
 			}
 
 			if (translatedFilename == null) {
 				System.out.println("Error: missing <TRANSLATED_FILE>\n");
 				System.out.println("Usage: FixStrings [OPTION]... <SOURCE_FILE> <TRANSLATED_FILE> [FIXED_FILE]");
 				return;
 			}
 
 			if (fixedTranslatedFileName == null)
 				fixedTranslatedFileName = translatedFilename;
 
 			DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			XPathFactory xpathFactory = XPathFactory.newInstance();
 			XPath xpath = xpathFactory.newXPath();
 
 			Document documentS = domBuilder.parse(new File(sourceFilename));
 			Document documentT = domBuilder.parse(new File(translatedFilename));

			DOMSource domSourceS = new DOMSource(documentS);
			DOMResult domSourceF = new DOMResult();
			transformer.transform(domSourceS, domSourceF);
			Document documentF = (Document) domSourceF.getNode();
 
 			XPathExpression expr = xpath.compile("//string");
 			NodeList nodesS = (NodeList) expr.evaluate(documentS, XPathConstants.NODESET);
 
 			if (!quietMode)
 				System.out.println(String.format("Parsing file %s...\n", sourceFilename));
 
 			for (int i = 0; i < nodesS.getLength(); i++) {
 				foundStrings++;
 
 				String nodeNameS = nodesS.item(i).getAttributes().item(0).getNodeValue();
 				String nodeTextS = nodesS.item(i).getTextContent();
 
 				expr = xpath.compile(String.format("//string[@name='%s']", nodeNameS));
 				NodeList nodesT = (NodeList) expr.evaluate(documentT, XPathConstants.NODESET);
 
 				switch (nodesT.getLength()) {
 
 					case 0:
 						if (!quietMode && showUntranslated)
 							System.out.println("Found untranslated string: " + nodeNameS);
 
 						if (fillUntranslated) {
 							expr = xpath.compile(String.format("//string[@name='%s']", nodeNameS));
 							NodeList nodesF = (NodeList) expr.evaluate(documentF, XPathConstants.NODESET);
 							nodesF.item(0).setTextContent("TODO:" + nodeTextS);
 						} else {
 							expr = xpath.compile(String.format("//string[@name='%s']", nodeNameS));
 							NodeList nodesF = (NodeList) expr.evaluate(documentF, XPathConstants.NODESET);
 							Element element = (Element) nodesF.item(0);
 							Node node = element.getParentNode();
 							Node prevNode = element.getPreviousSibling();
 							if (prevNode != null && prevNode.getNodeType() == Node.TEXT_NODE && prevNode.getNodeValue().trim().length() == 0)
 								node.removeChild(prevNode);
 							node.removeChild(element);
 							documentF.normalize();
 						}
 
 						removedStrings++;
 						break;
 
 					case 1:
 						expr = xpath.compile(String.format("//string[@name='%s']", nodeNameS));
 						NodeList nodesF = (NodeList) expr.evaluate(documentF, XPathConstants.NODESET);
 						nodesF.item(0).setTextContent(nodesT.item(0).getTextContent());
 						break;
 
 					default:
 						System.out.println("Error: found duplicated string: " + nodeNameS + " in translated file.");
 						return;
 				}
 			}
 
 			DOMSource domSource = new DOMSource(documentF);
 			StreamResult streamResult = new StreamResult(new File(fixedTranslatedFileName));
 			transformer.transform(domSource, streamResult);
 
 			if (!quietMode) {
 				if (removedStrings != 0 && showUntranslated)
 					System.out.println("");
 				System.out.println(String.format("Found %s strings.", foundStrings));
 				if (fillUntranslated)
 					System.out.println(String.format("Filled %s untraslated strings with 'TODO:' prefix.", removedStrings));
 				else
 					System.out.println(String.format("Removed %s untraslated strings.", removedStrings));
 			}
 
 		} catch (Exception e) {
 			System.out.println(String.format("Internal error: %s\n", e.getMessage()));
 			return;
 		}
 	}
 
 }
