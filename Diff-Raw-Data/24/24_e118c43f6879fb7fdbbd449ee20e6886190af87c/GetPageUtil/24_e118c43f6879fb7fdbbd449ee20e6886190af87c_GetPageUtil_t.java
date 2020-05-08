 package sla.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class GetPageUtil {
 
 	public static String get(String urlStr) throws Exception {
 		URL url = new URL(urlStr);
 		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 		connection
 				.setRequestProperty(
 						"User-Agent",
 						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_2) AppleWebKit/534.52.7 (KHTML, like Gecko) Version/5.1.2 Safari/534.52.7");
 		connection
 				.setRequestProperty("Accept",
 						"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
 		connection.setRequestProperty("Cache-Control", "max-age=0");
 		connection.setRequestMethod("GET");
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(
 				connection.getInputStream(), "UTF-8"));
 		String content = "";
 		String line;
 		while ((line = br.readLine()) != null) {
 			content += line + "\n";
 		}
 		return content;
 	}
 	
 	public static List<String> getImageUrls(String urlStr) throws Exception {
 		String content = get(urlStr);
 		
		List<String> imageUrls = new ArrayList<String>();
 		
 		Pattern p = Pattern.compile("<img src=\"(.*?)\"");
 		Matcher m = p.matcher(content);
 
 		while (m.find()) {
 			String imageUrl = m.group(1);
 			System.out.println(imageUrl);
 			imageUrls.add(imageUrl);
 		}
 		return imageUrls;
 	}
 	
 	public static void main(String[] args) throws Exception {
 		System.out.println(getImageUrls("http://naver.com").size());
 	}
 
 }
