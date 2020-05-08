 package com.nullblock.vemacs.trans;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 
 public class TransUtil {
 
 	public static String readURL(String url) {
 		String response = "";
 		try {
 			URL toread = new URL(url);
 			URLConnection yc = toread.openConnection();
 			// Yahoo uses this UserAgent, so might as well use it to prevent 403s
 			yc.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.1; en-GB;     rv:1.9.2.13) Gecko/20101203 Firefox/3.6.13 (.NET CLR 3.5.30729)");
 			BufferedReader in = new BufferedReader(new InputStreamReader(yc
 					.getInputStream(), "UTF-8"));
 			String inputLine;
 			while ((inputLine = in.readLine()) != null) {
 				response = response + inputLine;
 
 			}
 			in.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return response;
 	}
 
 	public static String getTranslation(String text, String lang){
 		HashMap hm = new HashMap(); 
 		Pattern p = Pattern
 				.compile("(?i)\\b((?:https?://|www\\d{0,3}[.]|[a-z0-9.\\-]+[.][a-z]{2,4}/)(?:[^\\s()<>]+|\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\))+(?:\\(([^\\s()<>]+|(\\([^\\s()<>]+\\)))*\\)|[^\\s`!()\\[\\]{};:'\".,<>?������]))");
 		Matcher m = p.matcher(text);
 		StringBuffer sb = new StringBuffer();
 		String urlTmp = "";
 		// URL handling
 		while (m.find()) {
 			urlTmp = m.group(1);
 			String uuid = UUID.randomUUID().toString().replace("-", "");
 			hm.put(uuid, urlTmp);
 			text.replace(urlTmp, uuid);
 			m.appendReplacement(sb, "");
 			sb.append(urlTmp);
 		}
 		m.appendTail(sb);
 		text = sb.toString();
 		// end replace with UUID
 		text = URLEncoder.encode(text);
 		String response = readURL("http://translate.google.com/translate_a/t?q=" + text + "&client=t&text=&sl=auto&tl=" + lang);
 		int end = response.indexOf(URLDecoder.decode(text));
 		if( (end > 4) ) {
 			// prevents substring errors
 			response = response.substring(4, end);
 			response = response.substring(0, response.length() - 3);
 
 		} else {
 			response = URLDecoder.decode(text);
 		}
 		// begin UUID to URL		
 		Set set = hm.entrySet(); 
 		Iterator i = set.iterator(); 
 		while(i.hasNext()) { 
 			Map.Entry me = (Map.Entry)i.next(); 
 			response.replace(me.getKey().toString(), me.getValue().toString()); 
 		} 
 		// end UUID to URL
 		return response;
 	}
 
 	public static String postProcess(String response, String lang){
 		// post processing text
		response = response.replace(" :", ":");
 		if( response.startsWith("") && StringUtils.countMatches(response, "?") == 0){
 			response = response + "?";
 		}
 		if( response.startsWith("") && StringUtils.countMatches(response, "!") == 0 ){
 			response = response + "!";
 		}
 		if( lang.equals("en") && response.startsWith("'re")){
 			response = "You" + response;
 		}
 		return response;
 	}
 }
