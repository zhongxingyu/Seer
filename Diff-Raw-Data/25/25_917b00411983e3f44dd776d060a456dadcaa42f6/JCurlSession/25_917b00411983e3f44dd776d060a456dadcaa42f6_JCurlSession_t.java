 package jcurl.main.session;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.nio.charset.Charset;
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import sun.misc.IOUtils;
 
 import jcurl.main.converter.CurlConverter;
 import jcurl.main.converter.CurlObject;
 
 public class JCurlSession {
 
 	private final Pattern cookiePattern = Pattern.compile("([^=]*)=(.*)");
 	/**
 	 * Logger
 	 */
 	private Logger log = Logger.getLogger(JCurlSession.class.getName());
 
 	/**
 	 * Current cookies
 	 */
 	private Map<String, String> currentCookies;
 
 	/**
 	 * The current response object
 	 */
 	private CurlResponse curlResponse;
 
 	/**
 	 * The timeout of each curl call
 	 */
 	private int timeout = 0;
 
 	/**
 	 * To replace a variable in curl string, use this as front regex
 	 */
 	private String frontParamDetect = "\\$\\{";
 
 	/**
 	 * To replace a variable in curl string use this as the end of regex
 	 */
 	private String backParamDetect = "\\}";
 
 	/**
 	 * Create a new default JCurlSession instance timeout is infinite/0
 	 * ${variable}
 	 */
 	public JCurlSession() {
 		currentCookies = new HashMap<String, String>();
 	}
 
 	public CurlResponse callCurl(File curlFile, KeyValuePair... args)
 			throws IOException {
 		FileInputStream fis = new FileInputStream(curlFile);
 		String curlString = new String(IOUtils.readFully(fis, -1, true),
 				Charset.forName("UTF-8"));
 		log.info(MessageFormat.format("Read curl string {0}", curlString));
 		return callCurl(curlString, args);
 	}
 
 	public String getFrontParamDetect() {
 		return frontParamDetect;
 	}
 
 	public void setFrontParamDetect(String frontParamDetect) {
 		this.frontParamDetect = frontParamDetect;
 	}
 
 	public String getBackParamDetect() {
 		return backParamDetect;
 	}
 
 	public void setBackParamDetect(String backParamDetect) {
 		this.backParamDetect = backParamDetect;
 	}
 
 	public CurlResponse callCurl(String curlString, KeyValuePair... args) {
 		for (KeyValuePair pair : args) {
 			curlString = curlString.replaceAll(frontParamDetect + pair.getKey()
 					+ backParamDetect, pair.getValue());
 		}
 
 		return callCurl(curlString);
 	}
 
 	/**
 	 * Input a curl string that would run in command line and return the
 	 * response object
 	 * 
 	 * @param curlString
 	 * @throws IOException
 	 */
 	private CurlResponse callCurl(String curlString) {
 		log.fine(MessageFormat.format("Calling curl string {0}", curlString));
 
 		log.fine("Converting curl string to curl object");
 		// convert string to curl object
 		CurlObject curlObject = CurlConverter.convertCurl(curlString);
 
 		log.fine("Done converting, creating connection now");
 
 		// create the connection
 		URL url = curlObject.getUrl();
		HttpURLConnection connection = null;
 		try {
			connection = (HttpURLConnection) url.openConnection();
 
 			// set http method
 			connection.setRequestMethod(curlObject.getHttpMethod());
 
 			connection.setDoOutput(true);
 
 			log.fine("Connection created, adding headers now");
 
 			// iterate over headers and add to request properties
 			Map<String, String> headers = curlObject.getHeaders();
 			for (String key : headers.keySet()) {
 				connection.setRequestProperty(key, headers.get(key));
 			}
 
 			// from previous response object get cookies
 			String cookieString = convertCookiesToString(currentCookies);
 
 			log.fine(MessageFormat
 					.format("Done adding headers, now adding cookies {0}",
 							cookieString));
 
 			// if there are previous cookies set the header
 			if (cookieString != null) {
 				connection.setRequestProperty("Cookie", cookieString);
 			}
 
 			connection.setConnectTimeout(timeout);
 
 			log.fine("Trying to connect to url");
 
 			// connect to the url
 			connection.connect();
 
 			log.fine("Done connection to url, getting output stream");
 
 			log.fine("Writing bytes to output stream");
 
 			if (curlObject.getData() != null && !curlObject.getData().isEmpty()) {
				OutputStream os = connection.getOutputStream();
 				os.write(curlObject.getData().getBytes());
				os.flush();
 			}
 
 			log.fine("Done writing bytes, creating response object");
 
 			curlResponse = new CurlResponse(connection, curlObject);
 
 			log.fine(MessageFormat.format("Done creating response \n{0}\n",
 					curlResponse));
 
 			log.fine("Adding response cookies to current cookies");
 			addCookies(curlResponse.getCookies());
 
 			return curlResponse;
 		} catch (IOException e) {
 			log.log(Level.SEVERE, "", e);
 		} finally {
			if (connection != null) {
				connection.disconnect();
 			}
 		}
 		return null;
 	}
 
 	public void addCookie(String key, String value) {
 		currentCookies.put(key, value);
 	}
 
 	/**
 	 * Method that takes a cookie list and adds or overwrite if cookie exists
 	 * 
 	 * @param cookieString
 	 */
 	public void addCookies(List<String> cookies) {
 		if (cookies == null) {
 			return;
 		}
 		for (String cookie : cookies) {
 			String key, value;
 			// iterate over every cookie
 			// split cookie on =
 			Matcher m = cookiePattern.matcher(cookie);
 			if (m.find()) {
 				key = m.group(1).trim();
 				value = m.group(2).trim();
 				addCookie(key, value);
 			}
 
 		}
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder stringBuilder = new StringBuilder();
 		stringBuilder.append("\n<JCURLSESSION>\n");
 		stringBuilder.append(curlResponse);
 
 		stringBuilder.append(" CURRENT COOKIES\n----------------\n");
 		for (String key : currentCookies.keySet()) {
 			stringBuilder.append(" ").append(key).append(" : ")
 					.append(currentCookies.get(key)).append("\n");
 		}
 		stringBuilder.append(" ------------\n</JCURLSESSION>\n");
 
 		return stringBuilder.toString();
 
 	}
 
 	private String convertCookiesToString(Map<String, String> cookies) {
 		if (cookies.isEmpty()) {
 			return "";
 		}
 		StringBuilder cookieString = new StringBuilder();
 
 		for (String key : cookies.keySet()) {
 			cookieString.append(key).append("=").append(cookies.get(key))
 					.append(";");
 		}
 		cookieString.deleteCharAt(cookieString.length() - 1);
 
 		return cookieString.toString();
 	}
 
 	/**
 	 * Clean up the curl session, should be called after done using a
 	 * JCurlSession object
 	 * 
 	 * @throws Throwable
 	 */
 	public void close() throws Throwable {
 		this.finalize();
 	}
 
 	public int getTimeout() {
 		return timeout;
 	}
 
 	public void setTimeout(int timeout) {
 		this.timeout = timeout;
 	}
 
 	public static class KeyValuePair {
 		private String key, value;
 
 		public String getKey() {
 			return key;
 		}
 
 		public void setKey(String key) {
 			this.key = key;
 		}
 
 		public String getValue() {
 			return value;
 		}
 
 		public void setValue(String value) {
 			this.value = value;
 		}
 
 		public KeyValuePair(String key, String value) {
 			super();
 			this.key = key;
 			this.value = value;
 		}
 
 	}
 
 	public CurlResponse getCurlResponse() {
 		return curlResponse;
 	}
 
 }
