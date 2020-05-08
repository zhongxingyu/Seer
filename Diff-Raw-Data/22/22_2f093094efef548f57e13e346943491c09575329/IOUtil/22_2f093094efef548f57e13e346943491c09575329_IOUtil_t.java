 package com.sk.web;

 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
import java.util.regex.Pattern;
 import java.util.Random;
 
 public class IOUtil {
 
 	public static String urlEncode(String s) {
 		try {
			return URLEncoder.encode(s, "UTF-8").replaceAll(Pattern.quote("+"), "%20")
					.replaceAll(Pattern.quote("*"), "%2A");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static String urlDecode(String s) {
 		try {
 			return URLDecoder.decode(s, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static Map<String, String> splitParams(String query) {
 		Map<String, String> ret = new HashMap<>();
 		if (query != null) {
 			for (String part : query.split("&")) {
 				int eqIndex = part.indexOf('=');
 				ret.put(IOUtil.urlDecode(part.substring(0, eqIndex)),
 						IOUtil.urlDecode(part.substring(eqIndex + 1)));
 			}
 		}
 		return ret;
 	}
 
 	public static String joinParams(Map<String, String> params) {
 		StringBuilder query = new StringBuilder();
 		for (Entry<String, String> param : params.entrySet()) {
 			if (query.length() > 0)
 				query.append("&");
 			query.append(IOUtil.urlEncode(param.getKey()));
 
 			query.append("=");
 			query.append(IOUtil.urlEncode(param.getValue()));
 		}
 		return query.toString();
 	}
 
 	public static String generateNonce() {
 		return generateNonce(32);
 	}
 
 	public static String generateNonce(int length) {
 		return generateString("1qaz2wsx3edc4rfv5tgb6yhn7ujm8ik9ol0pQAZWSXEDCRFVTGBYHNUJMIKOLP", length);
 	}
 
 	public static String generateString(String alphabet, int length) {
 		if (alphabet.length() == 0)
 			return null;
 		StringBuilder ret = new StringBuilder(length);
 		for (int i = 0; i < length; ++i)
 			ret.append(alphabet.charAt(random.nextInt(alphabet.length())));
 		return ret.toString();
 	}
 
 	public static String readFrom(InputStream stream) throws IOException {
 		StringBuilder ret = new StringBuilder();
 		String line;
 		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
 		while ((line = reader.readLine()) != null) {
 			ret.append(line);
 			ret.append("\n");
 		}
 		reader.close();
 		return ret.toString();
 	}
 
 	public static final Random random = new Random();
 }
