 // Copyright (2006-2007) Schibsted Søk AS
 package no.schibstedsok.searchportal.http;
 
 import org.apache.log4j.Logger;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.ConnectException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.SocketTimeoutException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLStreamHandler;
 import java.net.JarURLConnection;
 import java.text.DecimalFormat;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Utility class to fetch URLs and return them as either BufferedReaders or XML documents.
  * Keeps statistics on connection times and failures.
  * TODO Provide Quality of Service through ramp up/down and throttling functionality.
  * XXX redesign into multiple classes with less static methods.
  * <p/>
  * Supports protocols http, https, ftp, and file.
  * If no protocol is specified in the host it defaults to http.
  *
  * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
  * @author <a href="mailto:mick@sesam.no">Mck</a>
  * @version <tt>$Id$</tt>
  */
 public final class HTTPClient {
 
     // Constants -----------------------------------------------------
 
     private static final int CONNECT_TIMEOUT = 1000; // milliseconds
     private static final int READ_TIMEOUT = 1000; // millisceonds
 
     private static final Logger LOG = Logger.getLogger(HTTPClient.class);
     private static final String DEBUG_USING_URL = "Using url {0} and Host-header {1} ";
 
     private final String id;
     private URLConnection urlConn;
     private final URL u;
 
     private HTTPClient(final URL u) {
         this(u, null);
     }
                                                                                            
     private HTTPClient(final URL u, final String physicalHost) {
         try {
             this.u = new URL(u, "", new PhysicalHostStreamHandler(physicalHost));
             this.id = u.getHost() + ':' + u.getPort();
         } catch (final MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
     private HTTPClient(final String hostHeader, final URL u) {
         try {
             this.u = new URL(u, "", new HostHeaderStreamHandler(hostHeader));
             this.id = u.getHost() + ':' + u.getPort();
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
 
     /**
      * Returns client for specified host and port.
      *
      * @param host The host to use. If no protocol is given then http is assumed.
      * @param port The port to use.
      *
      * @return a client.
      */
     public static HTTPClient instance(final String host, final int port) {
         return instance(ensureProtocol(host), port, null);
     }
 
     /**
      * Returns client for specified host, port and host header. Useful if you need to use a virtual host different
      * from the physical host.
      *
      * @param host the physical host to use.
      * @param port the port to use.
      * @param hostHeader virtual host to use.
      *
      * @return a client.
      */
     public static HTTPClient instance(final String host, final int port, final String hostHeader) {
         try {
             return new HTTPClient(hostHeader, new URL(ensureProtocol(host) + ':' + port));
         } catch (MalformedURLException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Returns client instance for the specified URL. The URL can either be complete or just contain the host.
      * The path can be supplied later when using the querying methods like
      * {@link HTTPClient#getBufferedStream(String path)}.
      *
      * @param url The URL.
      * @return a client.
      */
     public static HTTPClient instance(final URL url) {
         return new HTTPClient(url);
     }
 
     /**
      * Returns client instance for the specified URL and physical host. Use this if the virtual host is different from
      * the physcical host. The original host in the URL will be replaced by the supplied physical host and and the
      * original host will instead be used as a host header.
      *
      * @param url The url.
      * @param physicalHost The physical host.
      *
      * @return a client.
      */
     public static HTTPClient instance(final URL url, final String physicalHost) {
         return new HTTPClient(url, physicalHost);
     }
 
     /**
      * Returns client for the url. The client will use the supplied host haeder for all requests.
      *
      * @param hostHeader host haeder to use.
      * @param url url.
      * @return a client.
      */
     public static HTTPClient instance(final String hostHeader, final URL url) {
         return new HTTPClient(hostHeader, url);
     }
 
     /**
      * Convenience method to create a URL with an attached URLStreamHandler. This stream handler will replace the host
      * of the supplied URL with the supplied physical host. The original host will be used as a host header.
      *
      * @param url the original url.
      * @param physicalHost the physical host to use.
      * @return a url with the host replaces.
      *
      * @throws MalformedURLException on error.
      */
     public static URL getURL(final URL url, final String physicalHost) throws MalformedURLException {
         return new URL(url, "", new PhysicalHostStreamHandler(physicalHost));
     }
 
     /**
      * @param path
      * @return
      * @throws java.io.IOException
      * @throws org.xml.sax.SAXException
      */
     public Document getXmlDocument(final String path) throws IOException, SAXException {
 
         loadUrlConnection(path);
 
         final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         final DocumentBuilder builder;
 
         try {
             builder = factory.newDocumentBuilder();
 
             final long start = System.nanoTime();
 
             final Document result = builder.parse(urlConn.getInputStream());
 
             Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);
 
             return result;
 
         } catch (ParserConfigurationException e) {
             throw new IOException(e.getMessage());
 
         } catch (IOException e) {
             throw interceptIOException(e);
 
         } finally {
 
             if (null != urlConn && null != urlConn.getInputStream()) {
                 urlConn.getInputStream().close();
             }
             if (null != urlConn) {
                 // definitely done with connection now
                 urlConn = null;
             }
         }
     }
 
     /**
      * @param path
      * @return
      * @throws java.io.IOException
      */
     public BufferedInputStream getBufferedStream(final String path) throws IOException {
 
         loadUrlConnection(path);
 
         try {
             final long start = System.nanoTime();
 
             final BufferedInputStream result = new BufferedInputStream(urlConn.getInputStream());
 
             Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);
 
             return result;
 
         } catch (IOException e) {
             throw interceptIOException(e);
 
         }
     }
 
     /**
      * @param path
      * @return
      * @throws java.io.IOException
      */
     public BufferedReader getBufferedReader(final String path) throws IOException {
 
         loadUrlConnection(path);
 
         try {
             final long start = System.nanoTime();
 
             final BufferedReader result = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
 
             Statistic.getStatistic(this.id).addInvocation(System.nanoTime() - start);
 
             return result;
 
         } catch (IOException e) {
             throw interceptIOException(e);
 
         }
     }
 
     /**
      * @param path
      * @return
      * @throws java.io.IOException
      */
     public long getLastModified(final String path) throws IOException {
 
         try {
             return loadUrlConnection(path).getLastModified();
 
         } catch (IOException e) {
             throw interceptIOException(e);
 
         } finally {
             urlConn = null;
         }
     }
 
     /**
      * @param path
      * @return
      * @throws java.io.IOException
      */
     public boolean exists(final String path) throws IOException {
 
         boolean success = false;
         loadUrlConnection(path);
 
         if (urlConn instanceof HttpURLConnection  || urlConn instanceof JarURLConnection) {
             try {
 
                 if (urlConn instanceof HttpURLConnection) {
                     ((HttpURLConnection)urlConn).setInstanceFollowRedirects(false);
                     ((HttpURLConnection)urlConn).setRequestMethod("HEAD");
                     success = HttpURLConnection.HTTP_OK == ((HttpURLConnection)urlConn).getResponseCode();
                 } else {
                     success = urlConn.getContentLength() > 0;
                 }
             } catch (IOException e) {
                 throw interceptIOException(e);
 
             } finally {
                 urlConn = null;
             }
         } else {
             final File file = new File(path);
             success = file.exists();
         }
 
         return success;
     }
 
     /**
      * @param ioe
      * @return
      */
     public IOException interceptIOException(final IOException ioe) {
 
         final IOException e = interceptIOException(id, urlConn, ioe);
 
         // definitely done with connection now
         urlConn = null;
 
         return e;
     }
 
     /**
      * @param conn
      * @param ioe
      * @return
      */
     public static IOException interceptIOException(
             final URLConnection conn,
             final IOException ioe) {
 
         final String id = conn.getURL().getHost() + ':'
                 + (-1 != conn.getURL().getPort() ? conn.getURL().getPort() : 80);
 
         return interceptIOException(id, conn, ioe);
     }
 
     private static IOException interceptIOException(
             final String id,
             final URLConnection urlConn,
             final IOException ioe) {
 
         if (ioe instanceof SocketTimeoutException) {
             Statistic.getStatistic(id).addReadTimeout();
 
         } else if (ioe instanceof ConnectException) {
             Statistic.getStatistic(id).addConnectTimeout();
 
         } else {
             Statistic.getStatistic(id).addFailure();
         }
 
         // Clean out the error stream. See
         if (urlConn instanceof HttpURLConnection) {
             cleanErrorStream((HttpURLConnection) urlConn);
         }
 
 
         return ioe;
     }
 
 
     /**
      * @param conn
      * @param time
      */
     public static void addConnectionStatistic(final URLConnection conn, final long time) {
 
         final String id = conn.getURL().getHost() + ':'
                 + (-1 != conn.getURL().getPort() ? conn.getURL().getPort() : 80);
 
         Statistic.getStatistic(id).addInvocation(time);
     }
 
     private URLConnection loadUrlConnection(final String path) throws IOException {
         if (null == urlConn) {
             urlConn = new URL(u, path).openConnection();
         }
         return urlConn;
     }
 
     private static String ensureProtocol(final String host) {
         return host.contains("://") ? host : "http://" + host;
     }
 
     private static void cleanErrorStream(final HttpURLConnection con) {
 
         if (null != con.getErrorStream()) {
 
             final BufferedReader errReader = new BufferedReader(new InputStreamReader(con.getErrorStream()));
             final StringBuilder err = new StringBuilder();
             try {
                 for (String line = errReader.readLine(); null != line; line = errReader.readLine()) {
                     err.append(line);
                 }
                 con.getErrorStream().close();
 
             } catch (IOException ioe) {
                 LOG.warn(ioe.getMessage(), ioe);
             }
             LOG.info(err.toString());
         }
     }
 
     private static class PhysicalHostStreamHandler extends URLStreamHandler {
 
         private final String physicalHost;
 
         public PhysicalHostStreamHandler(final String physicalHost) {
             this.physicalHost = physicalHost;
         }
 
         protected URLConnection openConnection(final URL u) throws IOException {
            final URL url = new URL(u.getProtocol(), physicalHost, u.getPort(), u.getFile());
 
             final URLConnection connection = url.openConnection();
 
             connection.addRequestProperty("host", u.getHost());
             connection.setConnectTimeout(CONNECT_TIMEOUT);
             connection.setReadTimeout(READ_TIMEOUT);
 
             if (LOG.isTraceEnabled()) {
                 LOG.trace(MessageFormat.format(DEBUG_USING_URL, url, u.getHost()));
             }
 
             return connection;
         }
     }
 
     private static class HostHeaderStreamHandler extends URLStreamHandler {
 
         private final String hostHeader;
 
         public HostHeaderStreamHandler(final String hostHeader) {
            this.hostHeader = hostHeader;
         }
 
         protected URLConnection openConnection(final URL u) throws IOException {
 
 
             final URL url = new URL(u.getProtocol(), u.getHost(), u.getPort(), u.getFile());
             final URLConnection connection = url.openConnection();
 
             if (! u.getHost().equals(hostHeader)) {
                 connection.addRequestProperty("host", hostHeader);
             }
 
             connection.setConnectTimeout(CONNECT_TIMEOUT);
             connection.setReadTimeout(READ_TIMEOUT);
 
             if (LOG.isTraceEnabled()) {
                 LOG.trace(MessageFormat.format(DEBUG_USING_URL, url, hostHeader));
             }
 
             return connection;
         }
     }
 
     private static final class Statistic implements Comparable<Statistic> {
 
 
         private static final Map<String, Statistic> STATISTICS = new ConcurrentHashMap<String, Statistic>();
 
         private static final Logger STATISTICS_LOG = Logger.getLogger(Statistic.class);
 
         private final String id;
         private long totalTime = 0;
         private long longest = 0;
         private long invocations = 0;
         private volatile long connectTimeouts = 0;
         private volatile long readTimeouts = 0;
         private volatile long failures = 0;
         private static volatile long lastPrint = System.currentTimeMillis() / 60000;
 
         static Statistic getStatistic(final String id) {
 
             if (null == STATISTICS.get(id)) {
                 STATISTICS.put(id, new Statistic(id));
             }
             return STATISTICS.get(id);
         }
 
         private Statistic(final String id) {
             this.id = id;
         }
 
         synchronized void addInvocation(final long time) {
 
             final long timeMs = (time / 1000000);
             totalTime += timeMs;
             if (timeMs > longest) {
                 longest = timeMs;
             }
             ++invocations;
 
             if (STATISTICS_LOG.isDebugEnabled() && System.currentTimeMillis() / 60000 != lastPrint) {
 
                 printStatistics();
                 lastPrint = System.currentTimeMillis() / 60000;
             }
         }
 
         void addFailure() {
             ++failures;
         }
 
         void addConnectTimeout() {
             ++connectTimeouts;
         }
 
         void addReadTimeout() {
             ++readTimeouts;
         }
 
         private long getAverageInvocationTime() {
             return 0 < invocations ? (totalTime * (long) 1000 / invocations) : 0;
         }
 
         @Override
         public String toString() {
 
             return ": " + new DecimalFormat("000,000,000").format(invocations)
                     + " : " + new DecimalFormat("00,000").format(longest)
                     + "ms : " + new DecimalFormat("0,000,000").format(getAverageInvocationTime())
                     + "µs :   " + new DecimalFormat("00,000").format(failures)
                     + " :         " + new DecimalFormat("00,000").format(connectTimeouts)
                     + " : " + new DecimalFormat("00,000").format(readTimeouts)
                     + " <-- " + id;
         }
 
         public int compareTo(Statistic o) {
             return (int) (o.getAverageInvocationTime() - getAverageInvocationTime());
         }
 
 
         private static void printStatistics() {
 
             final List<Statistic> list = new ArrayList<Statistic>(STATISTICS.values());
             Collections.sort(list);
 
             final StringBuilder msg = new StringBuilder();
             msg.append("\n------ Printing HTTPClient statistics ------\n"
                     + ": invocations : longest  : average     "
                     + ": failures : connect errors : read timeouts <- client\n");
 
             for (Statistic stat : list) {
                 msg.append(stat.toString() + '\n');
             }
             msg.append("------ ------------------------------ ------");
             STATISTICS_LOG.debug(msg.toString());
         }
     }
 }
