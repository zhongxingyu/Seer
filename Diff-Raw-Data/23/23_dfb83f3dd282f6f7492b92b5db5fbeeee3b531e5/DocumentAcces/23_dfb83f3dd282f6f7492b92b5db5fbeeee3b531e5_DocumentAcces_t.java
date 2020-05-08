 package cudl;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * This class represent the remote object accessor.
  * 
  * @author sdiawara
  * 
  */
 public class DocumentAcces {
 	private String cookies;
 	private DocumentBuilder documentBuilder;
 	private final String userAgent;
 	private URL lastBaseUrl = null;
 
 	public DocumentAcces(final String userAgent) throws ParserConfigurationException {
 		this.userAgent = userAgent;
 		DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
 		fac.setNamespaceAware(false);
 		fac.setValidating(false);
 		fac.setFeature("http://xml.org/sax/features/namespaces", false);
 		fac.setFeature("http://xml.org/sax/features/validation", false);
 		fac.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
 		fac.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
 		
 		documentBuilder = fac.newDocumentBuilder();
 	}
 
 	/**
 	 * This method acces remote object using post method
 	 * 
 	 * @param uri
 	 * @param namelist
 	 * @return
 	 * @throws IOException
 	 * 
 	 */
 	public Document post(String uri, Map<String, String> namelist) throws IOException, SAXException {
 		URL url = ((getLastBaseUrl() == null) ? new URL(uri) : new URL(getLastBaseUrl(), uri));
 		URLConnection connection = url.openConnection();
 		connection.setDoOutput(true);
 		connection.setRequestProperty("User-agent", userAgent);
 		if (connection instanceof HttpURLConnection) {
 			HttpURLConnection httpConnection = (HttpURLConnection) connection;
 			httpConnection.setRequestMethod("POST");
 			flushParams(namelist, httpConnection);
 			setCookies(httpConnection);
 		}
 		return documentBuilder.parse(new BufferedInputStream(connection.getInputStream()));
 	}
 
 	/**
 	 * This method acces remote object using get method
 	 * 
 	 * @param uri
 	 * @param namelist
 	 * @return
 	 * @throws IOException
 	 * @throws SAXException
 	 */
 	public Document get(String uri, Map<String, String> namelist) throws IOException, SAXException {
 		//URL url = new URL(uri + ((namelist == null) ? "" : getParams(namelist)));
 		URL url = ((getLastBaseUrl() == null) ? new URL(uri) : new URL(getLastBaseUrl(), uri));
 		URLConnection connection = (URLConnection) url.openConnection();
 		connection.setRequestProperty("User-agent", userAgent);
 		
 		if (connection instanceof HttpURLConnection) {
 			HttpURLConnection httpConnection = (HttpURLConnection) connection;
 			setCookies(httpConnection);
 		}
 		setLastBaseUrl(url);
 		System.out.println(url);
 		
 		return documentBuilder.parse(connection.getInputStream());
 	}
 
 	private void setCookies(HttpURLConnection connection) {
 		if (cookies != null)
 			connection.setRequestProperty("Cookie", cookies);
 
		String tmpCookies = connection.getHeaderField("Set-Cookie");
		if (tmpCookies != null) {
			cookies = tmpCookies.replaceAll(";.*", "");
		}
 	}
 
 	@SuppressWarnings("unused")
 	private URL encodeUrl(String uri) throws MalformedURLException, UnsupportedEncodingException {
 		return new URL(URLEncoder.encode(uri, "UTF-8"));
 	}
 
 	private void flushParams(Map<String, String> namelist, HttpURLConnection connection) throws IOException {
 		OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
 		for (String param : ((namelist == null) ? "" : getParams(namelist)).split("[& ]")) {
 			writer.write(param);
 			writer.flush();
 		}
 	}
 
 	private String getParams(Map<String, String> namelist) {
 		String params = "?";
 		for (String param : namelist.keySet()) {
 			params += params + "=" + namelist.get(param) + "&";
 		}
 		return params;
 	}
 
 	public URL getLastBaseUrl() {
 		return lastBaseUrl;
 	}
 
 	public void setLastBaseUrl(URL lastBaseUrl) {
 		this.lastBaseUrl = lastBaseUrl;
 	}
 }
