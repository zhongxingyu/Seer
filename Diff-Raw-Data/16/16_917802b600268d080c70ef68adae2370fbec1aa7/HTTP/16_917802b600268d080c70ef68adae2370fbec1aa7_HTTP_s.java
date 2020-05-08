 package org.series.crawler;
 
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLStreamHandler;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.silvertunnel.netlib.adapter.url.NetlibURLStreamHandlerFactory;
 import org.silvertunnel.netlib.api.NetFactory;
 import org.silvertunnel.netlib.api.NetLayer;
 import org.silvertunnel.netlib.api.NetLayerIDs;
 import org.silvertunnel.netlib.layer.logger.LoggingNetLayer;
 import org.silvertunnel.netlib.layer.tcpip.TcpipNetLayer;
 import org.silvertunnel.netlib.layer.tls.TLSNetLayer;
 import org.silvertunnel.netlib.layer.tor.TorNetLayer;
 import org.silvertunnel.netlib.util.TempfileStringStorage;
 
 /**
  * Get a file from the Internet.
  * 
  * @author jabba
  */
 public class HTTP {
 	/**
 	 * The URL of the file we want to fetch.
 	 */
	private URL url;
	private URLConnection urlConnection;
	private URLStreamHandler handler;
 	protected static NetLayer loggingTcpipNetLayer;
 	protected static NetLayer loggingTlsNetLayer;
 	protected static TorNetLayer torNetLayer;
 	protected static final Log log = LogFactory.getLog(HTTP.class);
 	private static String MESSAGE = "    ====== Getting information of external website: ${URL} =====";
 	private Boolean anonymous = Boolean.FALSE;
 
 	public HTTP() {
 		this(Boolean.FALSE);
 		// this.handler = getNewIdentity();
 	}
 
 	public HTTP(Boolean anonymous) {
 		this.anonymous = anonymous;
 	}
 
 	synchronized public URLStreamHandler getNewIdentity() throws IOException {
 		if (this.anonymous) {
 			// do it only once
 			// if (loggingTcpipNetLayer == null) {
 			// create TCP/IP layer
 			NetLayer tcpipNetLayer = new TcpipNetLayer();
 			loggingTcpipNetLayer = new LoggingNetLayer(tcpipNetLayer, "upper tcpip  ");
 			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, loggingTcpipNetLayer);
 
 			// create TLS/SSL over TCP/IP layer
 			TLSNetLayer tlsNetLayer = new TLSNetLayer(loggingTcpipNetLayer);
 			loggingTlsNetLayer = new LoggingNetLayer(tlsNetLayer, "upper tls/ssl");
 			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);
 
 			// create TCP/IP layer for directory access (use different layer here to get different logging output)
 			NetLayer tcpipDirNetLayer = new TcpipNetLayer();
 			NetLayer loggingTcpipDirNetLayer = new LoggingNetLayer(tcpipDirNetLayer, "upper tcpip tor-dir  ");
 
 			// create Tor over TLS/SSL over TCP/IP layer
 			torNetLayer = new TorNetLayer(loggingTlsNetLayer, /* loggingT */tcpipDirNetLayer,
 					TempfileStringStorage.getInstance());
 			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
 			torNetLayer.waitUntilReady();
 			// }
 
 			// refresh net layer registration
 			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TCPIP, loggingTcpipNetLayer);
 			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TLS_OVER_TCPIP, loggingTlsNetLayer);
 			NetFactory.getInstance().registerNetLayer(NetLayerIDs.TOR_OVER_TLS_OVER_TCPIP, torNetLayer);
 
 			// // classic: TcpipNetLayer with NetLayerIDs.TCPIP (--> HTTP over plain TCP/IP)
 			// // anonymous: TorNetLayer with NetLayerIDs.TOR (--> HTTP over TCP/IP over Tor network)
 			// // NetLayer lowerNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TCPIP);
 			// NetLayer torNetLayer = NetFactory.getInstance().getNetLayerById(NetLayerIDs.TOR);
 			// torNetLayer.clear();
 			//
 			// // wait until TOR is ready (optional) - this is only relevant for anonymous communication:
 			// torNetLayer.waitUntilReady();
 
 			// prepare URL handling on top of the lowerNetLayer
 			NetlibURLStreamHandlerFactory factory = new NetlibURLStreamHandlerFactory(false);
 			// the following method could be called multiple times
 			// to change layer used by the factory over the time:
 			factory.setNetLayerForHttpHttpsFtp(torNetLayer);
 
 			// create the suitable URL object
 			log.debug("**********************");
 			log.warn("* NEW IDENTITY READY *");
 			log.debug("**********************");
 			return factory.createURLStreamHandler("http");
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Get the content of the given URL as a string.
 	 * 
 	 * @param url
 	 *            The URL we want to get.
 	 * @return The content of the URL as a string.
 	 */
 	public String getURL(String url) {
 		return getContent(getURLConnection(url));
 	}
 
 	public String getHeaderField(String url, String field) {
 		return getURLConnection(url).getHeaderField(field);
 	}
 
 	protected URLConnection getURLConnection(String urlStr) {
 		log.debug(getDebugMessage(urlStr));
 		return anonymous ? this.getTorHttpURLConnection(urlStr) : this.getHttpURLConnection(urlStr);
 	}
 
 	protected String getDebugMessage(String urlStr) {
 		return MESSAGE.replace("${URL}", urlStr);
 	}
 
 	/**
 	 * Gets the content of the given URL as an array of strings.
 	 * 
 	 * @param urlStr
 	 *            The URL of the file we want to read.
 	 * @return An array of strings containing the lines of the given file (URL).
 	 */
 	public String[] getURLasLines(String urlStr) {
 		log.debug(getDebugMessage(urlStr));
 		String all = this.getURL(urlStr);
 		all = all.replaceAll("\r", "\n");
 		return all.split("\n");
 	}
 
 	/**
 	 * It can check if a given URL is alive or not.
 	 * 
 	 * @param urlStr
 	 *            URL address that we want to check if it's available or not.
 	 * @return True, if it's available. False, otherwise.
 	 */
 	public boolean checkHttpUrl(String urlStr) {
 		try {
 			log.debug(getDebugMessage(urlStr));
			this.url = parseURLString(urlStr);
 			HttpURLConnection huc = (HttpURLConnection) url.openConnection();
 			int response = huc.getResponseCode();
 			if (response == 200)
 				return true;
 			else
 				return false;
 		} catch (java.io.IOException ioe) {
 		}
 		return false;
 	}
 
 	/*
 	 * ======================================================================= and here come the private functions
 	 * =======================================================================
 	 */
 
 	/**
 	 * Get the content of a URL as a string.
 	 * 
 	 * @param urlc
 	 *            URLConnection object the URL we want to read.
 	 * @return String content of the given URL.
 	 */
 	private String getContent(URLConnection urlc) {
 		try {
 			InputStream is = urlc.getInputStream();
 			DataInputStream ds = new DataInputStream(is);
 			int length;
 			length = urlc.getContentLength();
 			if (length != -1) {
 				byte b[] = new byte[length];
 				ds.readFully(b);
 				String s = new String(b);
 				return s;
 			} else {
 				StringBuilder s = new StringBuilder();
 				int i = is.read();
 				while (i != -1) {
 					s.append((char) i);
 					i = is.read();
 				}
 				return new String(s);
 			}
 		} catch (Exception e) {
 			System.err.println("Error: I/O error while trying to read the network file!");
 			System.exit(-1);
 		}
 		return null; // it cannot arrive here
 	}
 
 	/**
 	 * Checks if the URL is well-formed.
 	 * 
 	 * @param address
 	 *            The URL we want to check.
 	 * @return The URL itself if it is correct. If it's not correct, the program terminates.
 	 */
 	private URL parseURLString(String address) {
 		URL url = null;
 
 		try {
 			url = new URL(address);
 		} catch (MalformedURLException e) {
 			System.err.println("Error: the URL \"" + address + "\" is malformed!");
 			System.exit(-1);
 		}
 		return url;
 	}
 
 	private URLConnection getTorHttpURLConnection(String urlStr) {
 		HttpURLConnection urlConnection = null;
 		try {
 			URL context = null;
			URL url = new URL(context, urlStr, this.handler);
			this.url = url;
 
 			// /////////////////////////////////////////////
 			// the rest of this method is as for every java.net.URL object,
 			// read JDK docs to find out alternative ways:
 			// /////////////////////////////////////////////
 
 			// send request without POSTing data
 			urlConnection = HttpURLConnection.class.cast(url.openConnection());
 			HttpURLConnection.setFollowRedirects(false);
 			urlConnection.setInstanceFollowRedirects(false);
 			urlConnection
 					.setRequestProperty("User-agent",
 							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
 			urlConnection.setDoInput(true);
 			urlConnection.setDoOutput(false);
 			urlConnection.connect();
 
 			// // receive and print the response
 			// BufferedReader response = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
 			// String line;
 			// while ((line = response.readLine()) != null) {
 			// System.out.println(line);
 			// }
 			// response.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
		this.urlConnection = urlConnection;
		return this.urlConnection;
 	}
 
 	private URLConnection getHttpURLConnection(String urlStr) {
 		HttpURLConnection urlConnection = null;
 		try {
 			urlConnection = HttpURLConnection.class.cast(parseURLString(urlStr).openConnection());
 			urlConnection.setInstanceFollowRedirects(false);
 			HttpURLConnection.setFollowRedirects(false);
 			urlConnection
 					.setRequestProperty("User-agent",
 							"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.64 Safari/537.11");
 			urlConnection.connect();
 		} catch (Exception e) {
 			System.err.println("Error: network error while trying to open network connection!");
 			System.exit(-1);
 		}
 		return urlConnection;
 	}
 }
