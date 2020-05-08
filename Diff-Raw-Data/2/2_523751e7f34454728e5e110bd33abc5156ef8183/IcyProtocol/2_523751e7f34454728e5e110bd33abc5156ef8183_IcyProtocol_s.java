 package com.github.tumas.jspellcast.proto;
 
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 public class IcyProtocol {
 	
 	public static final String HEADERLINETOKEN = "\r\n";
 	public static final String HEADERENDTOKEN = HEADERLINETOKEN + HEADERLINETOKEN;
 	
 	public static final String CLIENT2SRVMESSAGE = "GET /%s %s\r\n" + "Host: %s:%s\r\n" + 
 		"User-Agent: JSpellCast\r\n" + "Icy-MetaData: %d\r\n" + "Connection: close\r\n\r\n";
 
 	public static final String ICYSRV2SRCOK = "ICY 200 OK" + HEADERENDTOKEN;
 	public static final String ICYSRVTOCLMSG = "ICY 200 OK" + HEADERLINETOKEN +
 		"icy-name:%s" + HEADERLINETOKEN +
 		"Content-Type:audio/mpeg" + HEADERLINETOKEN + 
 		"icy-br:%s" + HEADERLINETOKEN + 
 		"icy-metaint:%d" + HEADERENDTOKEN;
 	
 	/* Naive method definitions ... */
 	
 	public static HashMap<String, String> parseHeader(String header){
 		HashMap<String, String> protoObject = new HashMap<String, String>();
 		StringTokenizer st = new StringTokenizer(header, HEADERLINETOKEN);
 		String line;
 
 		while (st.hasMoreTokens()){
 			line = st.nextToken();
 			
			String[] strs = line.split(":");
 			if (strs.length >= 2) {
 				protoObject.put(strs[0].toLowerCase(), strs[1]);
 			}
 		}
 	
 		return protoObject;
 	}
 	
 	// TODO: rewrite this into a cryptic regExp one liner.
 	public static String getHeaderMountPoint(String header){
 		String[] lines = header.split(HEADERLINETOKEN);
 		
 		if (lines.length <= 0) 
 			return null;
 		
 		String[] lineTokens = lines[0].split(" ");
 		
 		if (lineTokens.length < 2)
 			return null;
 		
 		return lineTokens[1].substring(1);
 	}
 }
