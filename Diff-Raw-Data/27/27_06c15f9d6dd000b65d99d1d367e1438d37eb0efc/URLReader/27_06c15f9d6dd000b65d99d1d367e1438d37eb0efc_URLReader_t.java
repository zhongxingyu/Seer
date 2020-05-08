 /**
  * 
  */
 package rezend.musix;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 
 /**
  * This class provides methods to read the contents from a given URL (or String
  * of URL).
  * 
  * @author Rafael Rezende
  * 
  */
 public final class URLReader {
 
 	/**
 	 * Receives the URL of the desired page, creates a connection, downloads it
 	 * and replaces special characters from HTML accordingly.
 	 * 
 	 * @param url
 	 *            URL of the website to be fetched.
 	 * @return The website source code in String format.
 	 */
 	public static String getURLContent(URL url) {
 
 		// Holds the URL content
 		StringBuilder urlContent = new StringBuilder();
 
 		try {
 			// open connection to the provided URL
 			URLConnection urlConn = url.openConnection();
 			// copy content to a buffer
 			BufferedReader urlContentBuffer = new BufferedReader(
 					new InputStreamReader(urlConn.getInputStream()));
 
 			// using StringBuilder instead of String concatenation within a loop
 			// for performance reasons, since Strings are immutable.
 			String urlLine = "";
 			while ((urlLine = urlContentBuffer.readLine()) != null) {
 				urlContent.append(urlLine);
 			}
 			
 			// close the buffered reader
 			urlContentBuffer.close();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// using Apache Commons Lang to obtain HTML special characters properly
 		return StringEscapeUtils.unescapeHtml4(urlContent.toString());
 	}
 
 	/**
 	 * Receives the String URL of the desired page, creates a connection,
 	 * downloads it and replaces special characters from HTML accordingly.
 	 * 
 	 * @param urlStr
 	 *            String of the URL
 	 * @return The website source code in String format.
 	 */
 	public static String getURLContent(String urlStr) {
 
 		String urlContent = null;
 		try {
 			// complete the url with "http://" in case the given one does not
 			// provide the protocol (https, ftp etc)
			if (!urlStr.toLowerCase().contains("://")) {
 				urlStr = "http://" + urlStr;
 			}
 
 			URL url = new URL(urlStr);
 			urlContent = getURLContent(url);
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return urlContent;
 	}
 }
