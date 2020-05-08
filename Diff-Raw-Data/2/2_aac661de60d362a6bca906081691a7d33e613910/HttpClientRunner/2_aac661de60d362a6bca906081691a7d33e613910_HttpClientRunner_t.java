 package org.lastbamboo.common.http.client;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.URIException;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.io.output.NullOutputStream;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.LongRange;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.lastbamboo.common.util.InputStreamHandler;
 import org.lastbamboo.common.util.RuntimeIoException;
 
 /**
  * Performs an HTTP download using HttpClient.
  */
 public final class HttpClientRunner implements Runnable
     {
 
     /**
      * Logger for this class.
      */
     private static final Log LOG = LogFactory.getLog(HttpClientRunner.class);
 
     private final InputStreamHandler m_inputStreamHandler;
 
     /**
      * Listener for download events.
      */
     private final HttpListener m_listener;
 
     private final CommonsHttpClient m_httpClient;
 
     private final HttpMethod m_httpMethod;
 
     /**
      * Creates a new HTTP client <code>Runnable</code> with the specified
      * collaborating classes.
      *
      * @param handler The class that should receive the <code>InputStream</code>
      * for the HTTP message body.
      * @param client The <code>HttpClient</code> instance that will send the
      * HTTP request.
      * @param method The HTTP method handler for the request.  This could be,
      * "GET" or "HEAD", for example.
      * @param listener The listener for HTTP events during the download.
      */
     public HttpClientRunner(final InputStreamHandler handler,
         final CommonsHttpClient client, final HttpMethod method,
         final HttpListener listener)
         {
         this.m_inputStreamHandler = handler;
         this.m_httpClient = client;
         this.m_httpMethod = method;
         this.m_listener = listener;
         }
 
     /**
      * Performs the download on a separate thread.
      */
     public void run()
         {
         try
             {
             handleDownload();
             }
         catch (final RuntimeIoException e)
             {
             LOG.debug("Could not connect to host or connection lost?", e);
             }
         catch (final Throwable t)
             {
             LOG.error("Unexpected exception", t);
             }
         }
 
     /**
      * Handles the download request catching any expected exceptions.
      */
     private void handleDownload()
         {
         this.m_listener.onStatusEvent("Connecting...");
         try
             {
             download();
             }
         catch (final HttpException e)
             {
             LOG.warn("HTTP exception downloading, method: " + 
                 this.m_httpMethod.getPath() + " " + e.getReason(), e);
             final int reasonCode = e.getReasonCode();
             final String status;
             if (reasonCode >= 100 && reasonCode < 600)
                 {
                 status = "Could Not Access User";
                 }
             else
                 {
                 status = "Unknown response";
                 }
 
             this.m_listener.onStatusEvent(status);
             this.m_listener.onHttpException(e);
             }
         catch (final IOException e)
             {
             LOG.warn("Could not connect to user -- method: " + 
                 this.m_httpMethod.getPath(), e);
             this.m_listener.onStatusEvent("User Offline");
             this.m_listener.onCouldNotConnect();
             }
         }
 
     /**
      * Downloads the specified <code>URI</code> resource from the peer
      * with the given user ID.
      *
      * @throws IOException If an I/O (transport) error occurs. Some transport
      *  exceptions can be recovered from.
      * @throws HttpException If a protocol exception occurs. Usually protocol
      *  exceptions cannot be recovered from.
      */
     private void download () throws HttpException, IOException
         {
         LOG.trace ("Sending download request to user...");
 
         m_listener.onDownloadStarted ();
         try
             {
             executeHttpRequest ();
             LOG.trace ("Finished executing HTTP request...");
             }
         catch (final RuntimeException e)
             {
             LOG.debug("Caught exception on request", e);
             throw e;
             }
         finally
             {
             // Release the connection if no other methods need it.
             m_httpMethod.releaseConnection();
             LOG.trace("Released connection...");
             }
         }
 
     /**
      * Executes the HTTP request for the download.
      *
      * @throws IOException If an I/O error occurs.
      */
     private void executeHttpRequest () throws IOException
         {
         LOG.trace ("Sending HTTP GET request for: " + 
             this.m_httpMethod.getQueryString());
         
         final long start = System.currentTimeMillis();
         m_httpClient.executeMethod (m_httpMethod);
         final long connected = System.currentTimeMillis();
 
         LOG.trace ("Received status code: " + m_httpMethod.getStatusCode ());
 
         // Notify the listener that we've connected to the user.
         this.m_listener.onConnect(connected-start);
         
         final int statusCode = m_httpMethod.getStatusCode ();
         if (statusCode == HttpStatus.SC_OK)
             {
             onTwoHundredResponse();
             }
         else if (statusCode == HttpStatus.SC_PARTIAL_CONTENT)
             {
             onPartialContent();
             }
         else
             {
             onNonTwoHundredLevelResponse();
             }
         }
 
     /**
      * Handles the case where we receive a non-200 level response.
      * 
      * @throws IOException If we can't read the response stream.
      */
     private void onNonTwoHundredLevelResponse () throws IOException
         {
         final InputStream inputStream = m_httpMethod.getResponseBodyAsStream ();
         try
             {
             // Just send it into the ether.  We just need to make sure we still 
             // read the body here.
             IOUtils.copy(inputStream, new NullOutputStream());
             LOG.warn("Did not receive 200 OK response for request to URI: " +
                 this.m_httpMethod.getURI() + "\nInstead received: "+
                 this.m_httpMethod.getStatusLine()+
                 "\nRequest headers:\n"+
                 headerString(this.m_httpMethod.getRequestHeaders())+
                 "\nResponse headers:\n"+
                 headerString(this.m_httpMethod.getResponseHeaders()));
             }
         catch (final URIException e)
             {
             LOG.error("Could not resolve URI", e);
             }
         finally
             {
             IOUtils.closeQuietly(inputStream);
             }
 
         m_listener.onNoTwoHundredOk (m_httpMethod.getStatusCode ());
         }
 
     private static String headerString(final Header[] headers)
         {
         final StringBuilder sb = new StringBuilder();
         for (int i = 0; i < headers.length; i++)
             {
             sb.append(headers[i].getName());
             sb.append(": ");
             sb.append(headers[i].getValue());
             sb.append("\n");
             }
         return sb.toString();
         }
 
     private void onPartialContent() throws IOException
         {
         final Header rangeHeader = 
             this.m_httpMethod.getResponseHeader ("Content-Range");
 
         LOG.debug("Received range header: "+rangeHeader);
         if (rangeHeader == null)
             {
             throw new IOException("Received Partial Content response with " +
                 "no Content-Range header");
             }
         String rangeString = rangeHeader.getValue().trim();
         if (!rangeString.startsWith("bytes"))
             {
             this.m_listener.onBadHeader(rangeHeader.toString());
             throw new IOException("Could not read header: "+rangeHeader);
             }
         
         rangeString = 
             StringUtils.substringBetween(rangeString, "bytes", "/").trim();
         
         final String minString = 
             StringUtils.substringBefore(rangeString, "-").trim();
         final String maxString = 
             StringUtils.substringAfter(rangeString, "-").trim();
         final long min = Long.parseLong(minString);
         final long max = Long.parseLong(maxString);
         final LongRange range = new LongRange(min, max);
         m_listener.onContentRange(range);
         onTwoHundredResponse();
         }
     
     /**
     * The method called when a 200-level response is received when making 
      * the HTTP request.
      *
      * @throws IOException If an I/O error occurs.
      */
     private void onTwoHundredResponse () throws IOException
         {
         LOG.trace("Received "+this.m_httpMethod.getStatusCode() + 
             " for request: " +  this.m_httpMethod.getPath());
         
         final Header length = m_httpMethod.getResponseHeader ("Content-Length");
 
         if (length != null)
             {
             m_listener.onContentLength(
                 Long.parseLong (length.getValue ()));
             }
         
         // Uncompress the data if it's gzipped, otherwise just process it.
         final Header contentEncoding =
             m_httpMethod.getResponseHeader("Content-Encoding");
 
         // Note:  We don't close this input stream in a finally block because 
         // it's ultimately the input stream of the socket, and we likely want
         // to re-use it.  We just let HttpClient take care of connection
         // management.
         final InputStream inputStream = m_httpMethod.getResponseBodyAsStream ();
 
         if (contentEncoding == null)
             {
             m_inputStreamHandler.handleInputStream (inputStream);
             }
         else if (StringUtils.contains (contentEncoding.getValue (), "gzip"))
             {
             LOG.trace("Handling gzipped message body...");
             m_inputStreamHandler.handleInputStream(
                 new GZIPInputStream (inputStream));
             }
         else
             {
             LOG.warn("Unrecognized content encoding: "+contentEncoding);
             }
 
         m_listener.onMessageBodyRead ();
         }
     }
