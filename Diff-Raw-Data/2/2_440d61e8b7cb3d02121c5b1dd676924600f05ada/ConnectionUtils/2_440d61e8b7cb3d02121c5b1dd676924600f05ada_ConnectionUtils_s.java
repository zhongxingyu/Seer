 package nl.nikhef.jgridstart.util;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.logging.Logger;
 
 /** Helper methods for reading (external) URLs */
 public class ConnectionUtils {
     
     static private Logger logger = Logger.getLogger("nl.nikhef.jgridstart.util");
     
     /** Return a Reader for a URL */
     public static Reader pageReader(URL url) throws IOException {
 	return pageReader(url, (String)null, false);
     }
     /** Return the contents of a URL */
     public static String pageContents(URL url) throws IOException {
 	return pageContents(url, (String)null, false);
     }
    
     /** Return a reader for a URL with pre or post data
      * 
      * @param url URL to submit to
      * @param data Array of {@code "key","value","key2","value2",...}
      * @param post {@code true} to post data, {@code false} for get
      * @return reader for reading from the URL
      * @throws IOException 
      */
     public static Reader pageReader(URL url, String[] data, boolean post) throws IOException {
 	return pageReader(url, createQueryString(data, post), post);
     }
     /** Return the contents of a URL with pre or post data
      * 
      * @param url URL to submit to
      * @param data Array of {@code "key","value","key2","value2",...}
      * @param post {@code true} to post data, {@code false} for get
      * @return reader for reading from the URL
      * @throws IOException 
      */
     public static String pageContents(URL url, String[] data, boolean post) throws IOException {
 	return pageContents(url, createQueryString(data, post), post);
     }
     
     /** Return the contents of a URL with pre or post data
      * 
      * @param url URL to submit to
      * @param data String of "{@code key=value&amp;otherkey=othervalue}" post data
      * @param post {@code true} to post data, {@code false} for get
      * @return data returned by server
      * @throws IOException
      */
     public static String pageContents(URL url, String data, boolean post) throws IOException {
 	Reader reader = pageReader(url, data, post);
 	StringBuffer result = new StringBuffer();
 	BufferedReader breader = new BufferedReader(reader);
 	String line = null;
 	while ((line = breader.readLine()) != null) {
 	    result.append(line);
 	    result.append(System.getProperty("line.separator"));
 	}
 	return result.toString();
     }
     /** Return a Reader for a URL with pre or post data
      * 
      * @param url URL to submit to
      * @param data String of "{code key=value&amp;otherkey=othervalue}" post data
      * @param post {@code true} to post data, {@code false} for get
      * @return data returned by server
      * @throws IOException
      */
     public static Reader pageReader(URL url, String data, boolean post) throws IOException {
 	URLConnection conn = null;
 	if (data != null) {
 	    if (post) {
 		// post: write data to stream
 		conn = URLopenConnection(url);
 		// send post data if present
 		if (post) {
 		    conn.setDoOutput(true);
 		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 		    wr.write(data);
 		    wr.close();
 		}
 	    } else {
 		// pre: put data in URL
 		url = new URL(url, "?" + data);
 		conn = URLopenConnection(url);
 	    }
 	} else
 	    conn = URLopenConnection(url);
 	logger.fine("Creating Reader for url: "+url);
 	// return reader for response
 	return new InputStreamReader(conn.getInputStream());
     }
     
     /** Open connection with workarounds.
      * <p>
      * Currently specifically sets user-agent because with Java Web Start
      * the system property <tt>http.agent</tt> does not properly set the
      * user-agent for connections.
      * <p>
      * See also <a href="http://www.noizeramp.com/article.php?article=se-networking_specifics_under_Java_Web_Start">Networking specifics under Java Web Start</a>.
      */
     protected static URLConnection URLopenConnection(URL url) throws IOException {
 	URLConnection conn = url.openConnection();
 	if (conn instanceof HttpURLConnection) {
 	    String agent = System.getProperty("http.agent") +
	    	" Java "+System.getProperty("java.version");
 	    if (agent != null)
 		((HttpURLConnection)conn).setRequestProperty("User-Agent", agent);
 	}
 	return conn;
     }
     
     /** Return a query string from arguments for pre or post
      * 
      * @param data Array of {@code "key=value"} Strings
      * @return single String with urlencoded data
      * @throws UnsupportedEncodingException 
      */
     protected static String createQueryString(String[] data, boolean post) throws UnsupportedEncodingException {
 	String sdata = "";
 	String sep = "&";
 	for (int i=0; i<(data.length-1); i+=2) {
 	    String key = data[i]!=null ? data[i] : "";
 	    String val = data[i+1]!=null ? data[i+1] : "";
 	    sdata += sep + URLEncoder.encode(key, "UTF-8")
 	                 + "=" + URLEncoder.encode(val, "UTF-8");
 	}
 	sdata = sdata.substring(sep.length());
 	return sdata;
     }
 }
