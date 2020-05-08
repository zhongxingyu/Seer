 package org.oruji.odt2html;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.jdom.Text;
 import org.jopendocument.dom.ODPackage;
 
 public class App {
 	private static StringBuilder outHTML = new StringBuilder("");
 	private static ODPackage openDocumentPackage;
 	private static Element automaticStyle;
 	private static Element rootElement;
 	private static Element bodyElement;
 	private static Element textElement;
 
 	public static void main(String[] args) throws IOException {
 		openDocumentPackage = new ODPackage(new File("test.odt"));
 
 		rootElement = openDocumentPackage.getTextDocument()
 				.getContentDocument().getRootElement();
 
 		automaticStyle = (Element) rootElement.getContent().get(2);
 		bodyElement = (Element) rootElement.getContent().get(3);
 		textElement = (Element) bodyElement.getContent().get(0);
 
 		// iterating <office:text>
 		for (Object obj : textElement.getContent()) {
 			Element myElement = ((Element) obj);
 
 			if (myElement.getName().equals("sequence-decls"))
 				continue;
 
 			String endTag = "";
 
 			if (myElement.getName().equals("p")) {
				// outHTML.append("<p>");
				// endTag = "</p>";

				outHTML.append("");
				endTag = "<br />";
 			}
 
 			else if (myElement.getName().equals("h")) {
 				String myAttribute = myElement.getAttributeValue("style-name",
 						myElement.getNamespace());
 
 				for (Object con : automaticStyle.getContent()) {
 					if (((Element) con).getAttributeValue("name",
 							((Element) con).getNamespace()).equals(myAttribute)) {
 						switch (((Element) con).getAttributeValue(
 								"parent-style-name",
 								((Element) con).getNamespace())) {
 						case "Heading_20_1":
 							outHTML.append("<h1>");
 							endTag = "</h1>";
 							break;
 
 						case "Heading_20_2":
 							outHTML.append("<h2>");
 							endTag = "</h2>";
 							break;
 
 						case "Heading_20_3":
 							outHTML.append("<h3>");
 							endTag = "</h3>";
 							break;
 
 						default:
 							break;
 						}
 					}
 				}
 			}
 
 			else if (myElement.getName().equals("list")) {
 				outHTML.append("<li>");
 				endTag = "</li>";
 			}
 
 			for (Object myObj : myElement.getContent())
 				recursiveElement(myObj);
 
 			outHTML.append(endTag);
 		}
 
 		htmlBuilder();
 		System.out.println(outHTML);
 		saveToFile("test.html", outHTML.toString());
 	}
 
 	public static void recursiveElement(Object obj) {
 		if (!obj.toString().startsWith("[Text: ")) {
 			Element element = ((Element) obj);
 
 			if (element.getName().equals("tab"))
 				outHTML.append("&nbsp;&nbsp;&nbsp;&nbsp;");
 
 			else if (element.getName().equals("s"))
 				outHTML.append("&nbsp;");
 
 			else
 				for (Object obj2 : element.getContent()) {
 					recursiveElement(obj2);
 				}
 
 			return;
 		}
 
 		Element currentElement = null;
 		Text currentText = null;
 		String currentAttr = null;
 		String createdStyle = null;
 		String tagName = null;
 		String tagBody = null;
 
 		currentText = (Text) obj;
 		currentElement = (Element) currentText.getParent();
 		currentAttr = currentElement.getAttributeValue("style-name",
 				currentElement.getNamespace());
 
 		createdStyle = createStyle(getStyleList(currentAttr, automaticStyle));
 
		if (currentElement.getName().equals("h")
				|| currentElement.getName().equals("p"))
 			tagName = "span";
 
 		else
 			tagName = currentElement.getName();
 
 		createdStyle = createdStyle.equals("") ? "" : " " + createdStyle;
 
 		tagBody = currentText.getValue().replace("<", "&lt;")
 				.replace(">", "&gt;");
 
 		outHTML.append("<" + tagName + createdStyle + ">" + tagBody + "</"
 				+ tagName + ">");
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Attribute> getStyleList(String attName,
 			Element automaticStyle) {
 
 		for (Object el : automaticStyle.getContent()) {
 			for (Object att : ((Element) el).getAttributes()) {
 				if (((Attribute) att).getName().equals("name")
 						&& ((Attribute) att).getValue().equals(attName)) {
 					((Attribute) att).getName();
 
 					return ((Element) ((Element) el).getContent().get(0))
 							.getAttributes();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public static String createStyle(List<Attribute> attList) {
 		StringBuilder createdStyle = new StringBuilder("");
 
 		if (attList != null && attList.size() > 0) {
 			createdStyle.append("style='");
 
 			for (Attribute att : attList) {
 				if (att.getName().equals("font-name"))
 					createdStyle.append("font-family:" + att.getValue() + ";");
 
 				if (att.getNamespacePrefix().equals("fo")) {
 					createdStyle.append(att.getName() + ":" + att.getValue()
 							+ ";");
 				}
 			}
 
 			createdStyle.append("'");
 		}
 
 		if (createdStyle.toString().equals("style=''"))
 			createdStyle = new StringBuilder();
 
 		return createdStyle.toString();
 	}
 
 	public static void htmlBuilder() {
 		StringBuilder myOutHTML = new StringBuilder();
 		myOutHTML
 				.append("<html><head><meta http-equiv='Content-Type' content='text/html; charset=UTF-8' /></head><body>\n\n");
 		myOutHTML.append(outHTML);
 		myOutHTML.append("\n\n</body></html>");
 		outHTML = new StringBuilder(myOutHTML);
 	}
 
 	public static void saveToFile(String fileName, String text) {
 		Writer out = null;
 		File file = new File(fileName);
 
 		try {
 			out = new BufferedWriter(new FileWriter(file));
 			out.write(text);
 			out.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 }
