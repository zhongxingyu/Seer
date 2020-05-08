 package org.eweb4j.spiderman.spider;
 
 import java.util.Map;
 
 import org.eweb4j.cache.Props;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.FileUtil;
 
 public class Settings {
 
 	private final static Map<String, String> settings = Props.getMap("spiderman");
 	
 	public static String website_xml_folder(){
 		return settings.get("website.xml.folder").replace("#{ClassPath}", FileUtil.getTopClassPath(Settings.class));
 	}
 	
	public static String website_visited_folder(){
		return settings.get("website.visited.folder").replace("#{ClassPath}", FileUtil.getTopClassPath(Settings.class));
	}
	
 	public static int http_fetch_retry(){
 		return Integer.parseInt(settings.get("http.fetch.retry"));
 	}
 	
 	public static long http_fetch_timeout(){
 		return CommonUtil.toSeconds(settings.get("http.fetch.timeout")).longValue();
 	}
 }
