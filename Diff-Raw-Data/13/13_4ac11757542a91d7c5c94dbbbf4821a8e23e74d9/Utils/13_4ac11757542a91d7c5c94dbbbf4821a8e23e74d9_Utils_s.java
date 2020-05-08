 package com.teamboid.twitterapi.utilities;
 
import org.apache.commons.codec.binary.Base64;
 import org.w3c.dom.Document;
 import org.xml.sax.InputSource;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.*;
 
 /**
  * @author Aidan Follestad
  */
 public class Utils {
 
 	@SuppressWarnings("resource")
 	public static String getBase64FromFile(File file) throws Exception {
 		InputStream is = new FileInputStream(file);
 		long length = file.length();
 		if (length > Integer.MAX_VALUE) {
 			throw new IOException("The file " + file.getName() + " is too large!");
 		}
 		byte[] bytes = new byte[(int) length];
 		int offset = 0;
 		int numRead = 0;
 		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
 			offset += numRead;
 		}
 		if (offset < bytes.length) {
 			throw new IOException("Could not read the entire file " + file.getName());
 		}
 		is.close();
		return Base64.encodeBase64String(bytes);
 	}
 
 	public static String unescape(String str) {
 		if(str == null) return null;
 		return str.replace("&quot;", "\"").replace("&lt;", "<").replace("&amp;", "&")
 				.replace("&gt;", ">").replace("\\/","//");
 	}
 
 	public static String stripAnchor(String str) {
 		if(!str.contains("<a")) return str;
 		try {
 			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(str)));
 			return doc.getDocumentElement().getTextContent();
 		} catch(Exception e) {
 			return str.substring(str.indexOf(">"));
 		}
 	}
 
 	public static Object deserializeObject(String input) {
 		try {
			byte [] data = Base64.decodeBase64(input);
 			ObjectInputStream ois = new ObjectInputStream( 
 					new ByteArrayInputStream(data));
 			Object o = ois.readObject();
 			ois.close();
 			return o;
 		} catch(Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static String serializeObject(Serializable tweet){
 		try{
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ObjectOutputStream oos = new ObjectOutputStream(baos);
 			oos.writeObject(tweet);
 			oos.close();
			return Base64.encodeBase64String(baos.toByteArray());
 		} catch(Exception e){
 			e.printStackTrace();
 			return "";
 		}
 	}
 }
