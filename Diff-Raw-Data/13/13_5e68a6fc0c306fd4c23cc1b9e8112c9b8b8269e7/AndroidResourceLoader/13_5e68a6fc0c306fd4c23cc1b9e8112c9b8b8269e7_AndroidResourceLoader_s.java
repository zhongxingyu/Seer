 package net.mms_projects.copyit;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpressionException;
 import javax.xml.xpath.XPathFactory;
 
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.wb.swt.SWTResourceManager;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class AndroidResourceLoader {
 
 	private static Map<String, String> strings;
 
 	public static Image getImage(String path) {
 		System.out.println(AndroidResourceLoader.class.getPackage());
 		String basePath = "/";
 		return SWTResourceManager.getImage(AndroidResourceLoader.class,
 				basePath + path);
 	}
 
 	public static String getString(String name) {
 		if (strings == null) {
 			loadStrings();
 		}
 		if (isReference(name)) {
 			if (getReferenceType(name).equals("string")) {
 				return getString(resolveReference(name));
 			}
 			return "Invalid reference type " + getReferenceType(name)
 					+ " for reference " + name;
 		}
 		String text = strings.get(name);
 		if (isReference(text)) {
 			if (getReferenceType(text).equals("string")) {
 				return getString(resolveReference(text));
 			}
 			return "Invalid reference type " + getReferenceType(text)
 					+ " for reference " + name;
 		}
 		return text;
 	}
 	
 	public static String getString(String name, Object... formatArgs) {
 		String raw = getString(name);
 		return String.format(raw, formatArgs);
 	}
 
 	public static void loadStrings() {
 		strings = new HashMap<String, String>();
 
 		InputStream stream = AndroidResourceLoader.class
 				.getResourceAsStream(getResourceName("values/strings.xml"));
 		InputSource inputSource = new InputSource(stream);
 
 		try {
 			parseStrings(inputSource);
 		} catch (XPathExpressionException exception) {
 			// TODO Auto-generated catch block
 			exception.printStackTrace();
 		}
 	}
 
 	protected static void parseStrings(InputSource source)
 			throws XPathExpressionException {
 		XPath xpath = XPathFactory.newInstance().newXPath();
 
 		Node root = (Node) xpath.evaluate("/", source, XPathConstants.NODE);
 
 		NodeList xmlStrings = null;
 
 		xmlStrings = (NodeList) xpath.evaluate("//string", root,
 				XPathConstants.NODESET);
 		for (int i = 0; i < xmlStrings.getLength(); ++i) {
 			String key = (String) xpath.evaluate("//string[" + (i + 1)
 					+ "]/@name", root, XPathConstants.STRING);
 			String value = xmlStrings.item(i).getTextContent();
 
 			strings.put(key, value);
 		}
 	}
 
 	public static String getResourceName(String name) {
 		String basePath = "/";
 		return basePath + name;
 	}
 
 	public static boolean isReference(String reference) {
 		return reference.startsWith("@");
 	}
 
 	public static String getReferenceType(String reference) {
 		String[] data = reference.split("/");
 		return data[0].substring(1);
 	}
 
 	public static String resolveReference(String reference) {
 		String[] data = reference.split("/");
 		return data[1];
 	}
 
 }
