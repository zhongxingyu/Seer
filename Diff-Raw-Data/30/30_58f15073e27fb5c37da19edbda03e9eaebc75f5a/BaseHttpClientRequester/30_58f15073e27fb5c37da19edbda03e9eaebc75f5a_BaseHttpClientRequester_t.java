 package org.lastbamboo.common.http.client;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.io.StringReader;
 import java.util.Collection;
 import java.util.zip.GZIPInputStream;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.StatusLine;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.lastbamboo.common.util.DefaultHttpClient;
 import org.lastbamboo.common.util.DefaultHttpClientImpl;
 import org.lastbamboo.common.util.Pair;
 import org.lastbamboo.common.util.UriUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Issues a post request using HTTP client. 
  */
 public class BaseHttpClientRequester
     {
 
     private final Logger LOG = LoggerFactory.getLogger(getClass());
     private final DefaultHttpClient m_clientManager = 
         new DefaultHttpClientImpl();
     private final String m_url;
     
     public BaseHttpClientRequester(final String baseAddress, 
         final Collection<Pair<String, String>> parameters)
         {
         this.m_url = UriUtils.newUrl(baseAddress, parameters);
         LOG.debug("Using URL string: "+this.m_url);
         }
 
     public String post() throws IOException, ServiceUnavailableException
         {
         final PostMethod method = new PostMethod(this.m_url);
         return request(method);
         }
     
     public String get() throws IOException, ServiceUnavailableException
         {
         final GetMethod method = new GetMethod(this.m_url);
         return request(method);
         }
 
     private String request(final HttpMethod method) throws IOException, 
         ServiceUnavailableException
         {
         method.setRequestHeader("Accept-Encoding", "gzip");
         InputStream is = null;
         try
             {
             this.m_clientManager.executeMethod(method);
             final int statusCode = method.getStatusCode();
             final StatusLine statusLine = method.getStatusLine();
             final Header encoding = 
                 method.getResponseHeader("Content-Encoding");
             if (encoding != null && encoding.getValue().equals("gzip"))
                 {
                 LOG.debug("Unzipping body...");
                 is = new GZIPInputStream(method.getResponseBodyAsStream());
                 }
             else
                 {
                 is = method.getResponseBodyAsStream();
                 }
             final String body = IOUtils.toString(is);
             if (StringUtils.isBlank(body))
                 {
                 // Could easily be a post request, which would not have a body.
                 LOG.debug("No response body.  Post request?");
                 }
             
             if (statusCode == HttpStatus.SC_SERVICE_UNAVAILABLE)
                 {
                 final String msg = "Got 503 Service Unavailable " + 
                     this.m_url + "\n" +
                     statusLine + "\n" + body;
                 LOG.warn(msg);
                 throw new ServiceUnavailableException(msg);
                 }
             if (statusCode != HttpStatus.SC_OK)
                 {
                 final String msg = "NO 200 OK: " + this.m_url + "\n" +
                     statusLine + "\n" + body;
                 LOG.warn(msg);
                writeToFile(body);
                 throw new IOException(msg);
                 }
             else
                 {
                 LOG.debug("Got 200 response...");
                 }
             
             return body;
             }
         finally
             {
             IOUtils.closeQuietly(is);
             method.releaseConnection();
             }
         
         }
 
    private void writeToFile(final String body)
        {
        final File out = new File(getClass().getSimpleName()+"-Error.html");
        if (out.exists())
            {
            out.delete();
            }
        final StringReader is = new StringReader(body);
        try
            {
            IOUtils.copy(is, new FileOutputStream(out));
            }
        catch (final FileNotFoundException e)
            {
            LOG.debug("FNF", e);
            }
        catch (final IOException e)
            {
            LOG.debug("IO", e);
            }
        }

     }
