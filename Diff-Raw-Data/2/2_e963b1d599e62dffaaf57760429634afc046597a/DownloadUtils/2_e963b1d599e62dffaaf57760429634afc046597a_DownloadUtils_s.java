 package eu.stratuslab.storage.disk.utils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.security.SecureRandom;
 import java.security.cert.X509Certificate;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import org.apache.http.Header;
 import org.apache.http.HeaderElement;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpRequestInterceptor;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpResponseInterceptor;
 import org.apache.http.client.entity.GzipDecompressingEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.conn.ClientConnectionManager;
 import org.apache.http.conn.scheme.PlainSocketFactory;
 import org.apache.http.conn.scheme.Scheme;
 import org.apache.http.conn.scheme.SchemeRegistry;
 import org.apache.http.conn.ssl.SSLSocketFactory;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.BasicClientConnectionManager;
 import org.apache.http.protocol.HttpContext;
 import org.restlet.data.Status;
 import org.restlet.resource.ResourceException;
 
 import eu.stratuslab.marketplace.metadata.MetadataUtils;
 
 public class DownloadUtils {
 
     public static Map<String, BigInteger> copyUrlContentsToFileWithOS(
             String url, File file) throws IOException {
 
         List<String> cmd = new LinkedList<String>();
         cmd.add("/opt/stratuslab/storage/pdisk/bin/ddcopy");
         cmd.add(url);
         cmd.add(file.getAbsolutePath());
 
         // FIXME: This information should be passed as a parameter.
         if (url.endsWith(".gz")) {
             cmd.add("YES");
         } else {
            cmd.add("NO")
         }
 
         ProcessBuilder pb = new ProcessBuilder(cmd);
 
         String msg = "error copying url (" + url + ") contents to disk";
         try {
             ProcessUtils.execute(pb, msg);
         } catch (ResourceException e) {
             throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, msg);
         }
 
         InputStream is = null;
         try {
             is = new FileInputStream(file);
             return MetadataUtils.streamInfo(is);
         } finally {
             FileUtils.closeIgnoringError(is);
         }
     }
 
     public static Map<String, BigInteger> copyUrlContentsToFile(String url,
             File file) throws IOException {
 
         Map<String, BigInteger> streamInfo = new HashMap<String, BigInteger>();
 
         DefaultHttpClient client = getHttpClient();
 
         try {
 
             HttpGet get = new HttpGet(url);
 
             HttpResponse response = client.execute(get);
 
             HttpEntity entity = response.getEntity();
 
             if (entity != null) {
 
                 InputStream is = null;
                 OutputStream os = null;
 
                 try {
                     is = entity.getContent();
 
                     // FIXME: This information should be passed as a parameter.
                     if (url.endsWith(".gz")) {
                         is = new GZIPInputStream(is);
                     }
 
                     os = new FileOutputStream(file);
                     streamInfo = MetadataUtils.copyWithStreamInfo(is, os);
 
                 } finally {
                     FileUtils.closeIgnoringError(is);
                     FileUtils.closeIgnoringError(os);
                 }
 
                 // Explicitly set the size of the output file.
                 // RandomAccessFile f = null;
                 // try {
                 // f = new RandomAccessFile(file.getAbsolutePath(), "rw");
                 // long length = streamInfo.get("BYTES").longValue();
                 // f.setLength(length);
                 // } finally {
                 // FileUtils.closeIgnoringError(f);
                 // }
 
             }
 
         } finally {
             client.getConnectionManager().shutdown();
         }
 
         return streamInfo;
     }
 
     private static class GzipRequestInterceptor implements
             HttpRequestInterceptor {
 
         public void process(final HttpRequest request, final HttpContext context)
                 throws HttpException, IOException {
 
             if (!request.containsHeader("Accept-Encoding")) {
                 request.addHeader("Accept-Encoding", "gzip");
             }
         }
     }
 
     private static class GzipResponseInterceptor implements
             HttpResponseInterceptor {
 
         public void process(final HttpResponse response,
                 final HttpContext context) throws HttpException, IOException {
 
             HttpEntity entity = response.getEntity();
             if (entity != null) {
                 Header ceheader = entity.getContentEncoding();
                 if (ceheader != null) {
                     HeaderElement[] codecs = ceheader.getElements();
                     for (int i = 0; i < codecs.length; i++) {
                         if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                             response.setEntity(new GzipDecompressingEntity(
                                     response.getEntity()));
                             return;
                         }
                     }
                 }
             }
         }
 
     }
 
     private static DefaultHttpClient getHttpClient() {
 
         try {
 
             SSLContext sslContext = SSLContext.getInstance("SSL");
 
             sslContext.init(null, new TrustManager[] { new X509TrustManager() {
 
                 public X509Certificate[] getAcceptedIssuers() {
                     return new X509Certificate[] {};
                 }
 
                 public void checkClientTrusted(X509Certificate[] certs,
                         String authType) {
                 }
 
                 public void checkServerTrusted(X509Certificate[] certs,
                         String authType) {
                 }
 
             } }, new SecureRandom());
 
             SSLSocketFactory sf = new SSLSocketFactory(sslContext,
                     SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
 
             Scheme httpsScheme = new Scheme("https", 443, sf);
             Scheme httpScheme = new Scheme("http", 80, new PlainSocketFactory());
             SchemeRegistry schemeRegistry = new SchemeRegistry();
             schemeRegistry.register(httpsScheme);
             schemeRegistry.register(httpScheme);
 
             ClientConnectionManager cm = new BasicClientConnectionManager(
                     schemeRegistry);
 
             DefaultHttpClient client = new DefaultHttpClient(cm);
 
             // client.addRequestInterceptor(new GzipRequestInterceptor());
             // client.addResponseInterceptor(new GzipResponseInterceptor());
 
             return client;
 
         } catch (Exception e) {
             // FIXME: This should probably do something more intelligent!
             throw new RuntimeException(e.getMessage());
         }
     }
 
 }
