 package util;
 
 import java.io.ByteArrayInputStream;
 
 
 import javax.xml.parsers.*;
 import org.bukkit.ChatColor;
 import org.w3c.dom.*;
 
 public class ColorParser {
 	private static String parseColors0(String s) throws Throwable
 	{
 		StringBuilder sb = new StringBuilder();
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		DocumentBuilder db = dbf.newDocumentBuilder();
		ByteArrayInputStream bais = new ByteArrayInputStream(("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>"+s).getBytes("UTF-8"));
 		Document doc = db.parse(bais);
 		Element root = doc.getDocumentElement();
 		NodeList fontElems = root.getElementsByTagName("font");
 		int ind = s.indexOf('<');
 		if (ind == -1) return s;
 		sb.append(s.substring(0, ind));
 		for (int i = 0; i < fontElems.getLength(); i++)
 		{
 			Element elem = (Element) fontElems.item(i);
 			NodeList nodesInside = elem.getChildNodes();
 			if (nodesInside.getLength() < 1) continue;
 			String color = elem.getAttribute("color");
 			try {
 				ChatColor col = ChatColor.valueOf(ChatColor.class, color.toUpperCase());
 				sb.append(ChatColor.COLOR_CHAR);
 				sb.append(col.getChar());
 			} catch (Throwable t){}
 			sb.append(nodesInside.item(0).getTextContent());
 			sb.append(ChatColor.COLOR_CHAR);
 			sb.append(ChatColor.RESET.getChar());
 		}
 		int lind = s.lastIndexOf('>');
 		if (lind == -1) return s;
 		sb.append(s.substring(lind));
 		return sb.toString();
 	}
 	
 	public static String parseColors(String s)
 	{
 		try {
 			return parseColors0(s);
 		} catch (Throwable e) {
 			e.printStackTrace();
 			return s;
 		}
 	}
 }
