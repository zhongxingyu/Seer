 package com.nullblock.vemacs.trans;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 
 import org.apache.commons.lang.StringUtils;
 
 public class TransUtil {
 	
 	public static String readURL(String url) {
 	    String response = "";
 		 try {
 	            URL toread = new URL(url);
 	            URLConnection yc = toread.openConnection();
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
 		text = URLEncoder.encode(text);
 		String response = readURL("http://translate.google.com/translate_a/t?q=" + text + "&client=t&text=&sl=auto&tl=" + lang);
 		int end = response.indexOf(URLDecoder.decode(text));
		if( !(end == 4) ) {
 			response = response.substring(4, end);
 			response = response.substring(0, response.length() - 3);
 
 		} else {
 			response = URLDecoder.decode(text);
 		}
 		// latin punctuation tweaks
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
