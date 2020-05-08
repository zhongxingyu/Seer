 package com.javatar;
 
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 /**
  * 
  * Used to get Gravatar Image
  *
  */
 
 public class JavatarImage {
 	
 	private StringBuilder url;
 	private List<String> args;
 	
 	/**
 	 * Default 404 image
 	 */
 	public static final String ERROR_404 = "404";
 	
 	/**
 	 * Default Mystery Man image
 	 */
 	public static final String MYSTERYMAN = "mm";
 	
 	/**
 	 * Default geometric pattern image
 	 */
 	public static final String IDENTICON = "identicon";
 	
 	/**
 	 * Default Monster image
 	 */
 	public static final String MONSTER = "monsterid";
 	
 	/**
 	 * Default Random face image
 	 */
 	public static final String WAVATAR = "wavatar";
 	
 	/**
 	 * Default Retro image
 	 */
 	public static final String RETRO = "retro";
 	
 	/**
 	 * G Rating
 	 */
 	public static final String G = "g";
 	
 	/**
 	 * PG Rating
 	 */
 	public static final String PG = "pg";
 	
 	/**
 	 * R Rating
 	 */
 	public static final String R = "r";
 	
 	/**
 	 * X Rating
 	 */
 	public static final String X = "x";
 	
 	
 	/**
 	 * 
 	 * @param hash The MD5 hash of the email
 	 */
 	public JavatarImage(String hash) {
 		url = new StringBuilder("http://www.gravatar.com/avatar/");
 		url.append(hash);
 		
 		args = new ArrayList<String>();
 	}
 	
 	/**
 	 * 
 	 * @param hash The MD5 hash of the email
 	 * @param ssl Use SSL
 	 */
 	public JavatarImage(String hash, boolean ssl) {
 		url = new StringBuilder(ssl ? "https://secure.gravatar.com/" : "http://www.gravatar.com/avatar/");
 		url.append(hash);
 		
 		args = new ArrayList<String>();
 	}
 	
 	/**
 	 * 
 	 * @param hash The MD5 hash of the email
 	 * @param ssl Use SSL
 	 * @param filending Use file ending (jpg)
 	 */
 	public JavatarImage(String hash, boolean ssl, boolean filending) {
 		url = new StringBuilder(ssl ? "https://secure.gravatar.com/" : "http://www.gravatar.com/avatar/");
 		url.append(hash + (filending ? ".jpg" : ""));
 		
 		args = new ArrayList<String>();
 	}
 	
 	/**
 	 * Sets the size of the avatar
 	 * 
 	 * @param pixels Width and Height of the avatar
 	 */
 	public void setSize(int pixels) {
 		while (true) {
 			if (args.size() >= 1)
 				break;
 			
 			args.add("");
 		}
 		
 		args.set(0, "s="+pixels); // first index
 	}
 	
 	/**
 	 * Sets the default avatar
 	 * 
 	 * @param image The URL or type of image to use as default
 	 * @throws UnsupportedEncodingException
 	 */
 	public void setDefaultImage(String image) throws UnsupportedEncodingException {
		while (args.size() != 2) {
 			args.add("");
 		}
 		
 		args.set(1, "d="+URLEncoder.encode(image, "UTF-8")); // second index
 	}
 	
 	/**
 	 * Force default avatar
 	 * 
 	 * @param b Force default avatar
 	 */
 	public void setForce(boolean b) {
		while (args.size() != 3) {
 			args.add("");
 		}
 		
 		if (b)
 			args.set(2, "f=y"); // third index
 		else
 			args.set(2, "");
 	}
 	
 	/**
 	 * Sets the max rating of the avatar
 	 * 
 	 * @param rating The maximum rating of the avatar
 	 */
 	public void setRating(String rating) {
		while (args.size() != 4) {
 			args.add("");
 		}
 		
 		args.set(3, "r="+rating); // fourth index
 	}
 	
 	/**
 	 * Generates the URL of the Gravatar image
 	 * 
 	 * @return URL of the Gravatar
 	 * @throws MalformedURLException
 	 */
 	public URL toURL() throws MalformedURLException {
 		StringBuilder urlstring = new StringBuilder("?");
 		Iterator<String> i = args.iterator();
 		urlstring.append(i.next());
 		
 		while (i.hasNext()) {
 			String str = i.next();
 			if (str.trim().equals(""))
 				continue;
 			
 			urlstring.append("&"+str);
 		}
 		
 		url.append(urlstring);
 		
 		return new URL(url.toString());
 	}
 
 }
