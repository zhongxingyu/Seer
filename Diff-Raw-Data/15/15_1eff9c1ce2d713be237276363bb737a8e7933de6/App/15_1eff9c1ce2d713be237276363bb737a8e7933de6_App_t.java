 package org.oruji.odt2html;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.Text;
 import org.jopendocument.dom.ODPackage;
 
 public class App {
 	private static StringBuilder outHTML = new StringBuilder("");
 	private static ODPackage openDocumentPackage;
 	private static Element automaticStyle;
 	private static Element rootElement;
 	private static Element bodyElement;
 	private static Element textElement;
 
 	public static void recursiveElement(Object obj) {
 		// Loop Condition
 		if (obj.toString().startsWith("[Text: ")) {
 			outHTML.append(((Text) obj).getValue().replace("<", "&lt;")
 					.replace(">", "&gt;"));
 			return;
 		}
 
 		Element element = ((Element) obj);
 		String startTag = "";
 		String endTag = "";
 
 		if (element.getName().equals("span")) {
 			String currentAtt = getAttVal(element, "style-name");
 			String createdStyle = createStyle(getStyleList(currentAtt,
 					automaticStyle));
 			createdStyle = createdStyle.equals("") ? "" : " " + createdStyle;
 
 			startTag = "<span" + createdStyle + ">";
 			endTag = "</span>";
 
 		} else if (element.getName().equals("h")) {
 			String tagName = "";
 			String currentAtt = getAttVal(element, "style-name");
 
 			for (Object con : automaticStyle.getContent()) {
 				Element loopEl = (Element) con;
 
 				if (currentAtt.equals(getAttVal(loopEl, "name"))) {
 					switch (getAttVal(loopEl, "parent-style-name")) {
 					case "Heading_20_1":
 						tagName = "h1";
 						break;
 
 					case "Heading_20_2":
 						tagName = "h2";
 						break;
 
 					case "Heading_20_3":
 						tagName = "h3";
 						break;
 
					case "Heading_20_4":
						tagName = "h4";
						break;	
						
					case "Heading_20_5":
						tagName = "h5";
						break;
						
					case "Heading_20_6":
						tagName = "h6";
						break;
						
 					default:
 						break;
 					}
 				}
 			}
 
 			String createdStyle = createStyle(getStyleList(currentAtt,
 					automaticStyle));
 
 			createdStyle = createdStyle.equals("") ? "" : " " + createdStyle;
 			startTag = "<" + tagName + createdStyle + ">";
 			endTag = "</" + tagName + ">";
 
 		} else if (element.getName().equals("p")) {
 
 			String currentAtt = getAttVal(element, "style-name");
 			String createdStyle = createStyle(getStyleList(currentAtt,
 					automaticStyle));
 			createdStyle = createdStyle.equals("") ? "" : " " + createdStyle;
 			startTag = "<div" + createdStyle + ">";
 			endTag = "</div>";
 
 		} else if (element.getName().equals("a")) {
 			String myUrl = element.getAttributeValue("href", Namespace
 					.getNamespace("xlink", "http://www.w3.org/1999/xlink"));
 			startTag = "<a href='" + myUrl + "'>";
 			endTag = "</a>";
 
 		} else if (element.getName().equals("tab")) {
 			for (int i = 0; i < 8; i++)
 				outHTML.append("&nbsp;");
 
 		} else if (element.getName().equals("s")) {
 
 			String spaceNo = getAttVal(element, "c");
 
 			if (spaceNo == null) {
 				outHTML.append("&nbsp;");
 
 			} else {
 				for (int i = 0; i < (spaceNo == null ? 0 : Integer
 						.parseInt(spaceNo)); i++)
 					outHTML.append("&nbsp;");
 			}
 
 		} else if (element.getName().equals("list-item")) {
 			startTag = "<li>";
 			endTag = "</li>";
 
 		} else if (element.getName().equals("list")) {
 			startTag = "<ul>";
 
 			Element myEl6 = (Element) element.getContent().get(0);
 			Element pElement = (Element) myEl6.getContent().get(0);
 
 			String currentAtt = getAttVal(pElement, "style-name");
 
 			String createdStyle = createULStyle(getStyleList(currentAtt,
 					automaticStyle));
 			createdStyle = createdStyle.equals("") ? "" : " " + createdStyle;
 			startTag = "<ul" + createdStyle + ">";
 
 			endTag = "</ul>";
 		}
 
 		// start tag
 		outHTML.append(startTag);
 
 		// body tag
 		for (Object obj2 : element.getContent()) {
 			recursiveElement(obj2);
 		}
 
 		// end tag
 		outHTML.append(endTag);
 	}
 
 	@SuppressWarnings("unchecked")
 	public static List<Attribute> getStyleList(String attValue,
 			Element automaticStyle) {
 
 		for (Object obj : automaticStyle.getContent()) {
 			Element element = (Element) obj;
 
 			if (attValue != null && isSameEl(element, "name", attValue)) {
 				if (element.getContent().size() > 0) {
 					List<Attribute> attList = new ArrayList<>();
 
 					for (Object obj2 : element.getContent()) {
 						Element element2 = (Element) obj2;
 
 						for (Attribute att : (List<Attribute>) element2
 								.getAttributes()) {
 							attList.add(att);
 						}
 					}
 
 					return attList;
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
 				if (att.getName().equals("font-name")) {
 					createdStyle.append("font-family:" + att.getValue() + ";");
 
 				} else if (att.getName().equals("writing-mode")) {
 					switch (att.getValue()) {
 					case "rl-tb":
 						createdStyle.append("direction:rtl;");
 						break;
 
 					case "lr-tb":
 						createdStyle.append("direction:ltr;");
 						break;
 
 					default:
 						break;
 					}
 
 				} else if (att.getName().equals("text-align")) {
 					switch (att.getValue()) {
 					case "start":
 						createdStyle.append("text-align:left;");
 						break;
 
 					case "end":
 						createdStyle.append("text-align:right;");
 						break;
 
 					default:
 						break;
 					}
 				}
 
 				if (att.getNamespacePrefix().equals("fo")) {
 					if (!att.getName().equals("text-align")) {
 						createdStyle.append(att.getName() + ":"
 								+ att.getValue() + ";");
 					}
 				}
 			}
 
 			createdStyle.append("'");
 		}
 
 		if (createdStyle.toString().equals("style=''"))
 			createdStyle = new StringBuilder();
 
 		return createdStyle.toString();
 	}
 
 	public static String createULStyle(List<Attribute> attList) {
 		StringBuilder createdStyle = new StringBuilder("");
 
 		if (attList != null && attList.size() > 0) {
 			createdStyle.append("style='");
 
 			for (Attribute att : attList) {
 				if (att.getName().equals("writing-mode")) {
 					switch (att.getValue()) {
 					case "rl-tb":
 						createdStyle.append("direction:rtl;");
 						break;
 
 					case "lr-tb":
 						createdStyle.append("direction:ltr;");
 						break;
 
 					default:
 						break;
 					}
 
 				} else if (att.getName().equals("text-align")) {
 					switch (att.getValue()) {
 					case "start":
 						createdStyle.append("text-align:left;");
 						break;
 
 					case "end":
 						createdStyle.append("text-align:right;");
 						break;
 
 					default:
 						break;
 					}
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
 
 	public static Element getChildByName(Element element, String name) {
 		for (Object obj : element.getContent()) {
 			Element myEl = ((Element) obj);
 			if (name.equals(myEl.getName())) {
 				return myEl;
 			}
 		}
 		return null;
 	}
 
 	//
 	// public static Element getChildByAttNameValue(Element element, String
 	// name,
 	// String value) {
 	// for (Object obj : element.getContent()) {
 	// Element el = ((Element) obj);
 	//
 	// if (getAttVal(el, name).equals(value)) {
 	// return el;
 	// }
 	// }
 	//
 	// return null;
 	// }
 
 	public static Attribute getAtt(Element element, String attName) {
 		return element.getAttribute(attName, element.getNamespace());
 	}
 
 	public static String getAttVal(Element element, String attName) {
 		return getAtt(element, attName) == null ? null : getAtt(element,
 				attName).getValue();
 	}
 
 	public static boolean isSameEl(Element element, String attName,
 			String attValue) {
 		Attribute attribute = getAtt(element, attName);
 
		if (attribute != null && attValue.equals(attribute.getValue()))
 			return true;
 
 		return false;
 	}
 
 	public static void main(String[] args) throws IOException {
 		openDocumentPackage = new ODPackage(new File("test.odt"));
 
 		rootElement = openDocumentPackage.getTextDocument()
 				.getContentDocument().getRootElement();
 
 		automaticStyle = (Element) getChildByName(rootElement,
 				"automatic-styles");
 		bodyElement = (Element) rootElement.getContent().get(3);
 		textElement = (Element) bodyElement.getContent().get(0);
 
 		// iterating <office:text>
 		for (Object myObj : textElement.getContent())
 			recursiveElement(myObj);
 
 		// Output HTML
 		htmlBuilder();
 		System.out.println(outHTML);
 		saveToFile("test.html", outHTML.toString());
 	}
 }
