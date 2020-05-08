 package net.marioosh.spring.springonly.utils;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import net.htmlparser.jericho.Element;
 import net.htmlparser.jericho.HTMLElementName;
 import net.htmlparser.jericho.Source;
 
 public class WebUtils {
 	
 	private Logger log = Logger.getLogger(getClass());
 	
 	public static Map<String, String> pageInfo(String u) {
 		Map<String, String> map = new HashMap<String, String>();
 		try {
 			u = u.startsWith("http://") || u.startsWith("https://") ? u : "http://" + u;
 			// jericho
 			Source source = new Source(new URL(u));
 			List<Element> titles = source.getAllElements(HTMLElementName.TITLE);
 			if (!titles.isEmpty()) {
 				Element title = titles.get(0);
 				// return new String(title.getContent().getTextExtractor().toString().getBytes(), "ISO-8859-2");
 				map.put("title", title.getTextExtractor().toString());
 			}
 
 			List<Element> meta = source.getAllElements(HTMLElementName.META);
 			if (!meta.isEmpty()) {
 				for (Element e : meta) {
 					if (e.getAttributeValue("name") != null) {
 						if (e.getAttributeValue("name").equalsIgnoreCase("description")) {
 							map.put("description", e.getAttributeValue("content"));
 						}
 					}
 				}
 			}
 
 		} catch (IOException e) {
 			return map;
 		}
 		return map;
 	}
 }
