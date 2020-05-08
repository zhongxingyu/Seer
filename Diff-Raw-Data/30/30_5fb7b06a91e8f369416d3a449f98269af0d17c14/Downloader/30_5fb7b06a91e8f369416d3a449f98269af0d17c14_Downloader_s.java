 package fedora.client;
 
 import java.awt.Dimension;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.methods.GetMethod;
 
 import fedora.server.utilities.StreamUtility;
 
 /**
  * A client for performing HTTP GET requests on a Fedora server (with
  * authentication) or any other server (without authentication).
  *
  * Each kind of request can either request an InputStream or request that
  * the Downloader write the content directly to a provided OutputStream.
  *
  * @author cwilper@cs.cornell.edu
  */
 public class Downloader {
 
     private MultiThreadedHttpConnectionManager m_cManager=
             new MultiThreadedHttpConnectionManager();
 
     private String m_fedoraUrlStart;
     private String m_host;
     private UsernamePasswordCredentials m_creds;
 
     /**
      * Construct a downloader for a certain repository as a certain user.
      */
     public Downloader(String host, int port, String user, String pass)
             throws IOException {
         m_fedoraUrlStart="http://" + host + ":" + port + "/fedora/get/";
         m_host=host;
         m_creds=new UsernamePasswordCredentials(user, pass);
     }
 
     public void getDatastreamContent(String pid, String dsID, Date asOfDateTime,
             OutputStream out)
             throws IOException {
         InputStream in=getDatastreamContent(pid, dsID, asOfDateTime);
         StreamUtility.pipeStream(in, out, 4096);
     }
 
     public InputStream getDatastreamContent(String pid, String dsID,
             Date asOfDateTime)
             throws IOException {
         HashMap parms=new HashMap();
         parms.put("itemID", dsID);
         return getDissemination(pid, "fedora-system:3", "getItem", parms, 
                 asOfDateTime);
     }
 
     public void getDissemination(String pid, String bDef, String method, 
             Map parms, Date asOfDateTime, OutputStream out) 
             throws IOException {
         InputStream in=getDissemination(pid, bDef, method, parms, asOfDateTime);
         StreamUtility.pipeStream(in, out, 4096);
     }
 
     public InputStream getDissemination(String pid, String bDef, String method, 
             Map parms, Date asOfDateTime) 
             throws IOException {
         StringBuffer buf=new StringBuffer();
         buf.append(m_fedoraUrlStart);
         buf.append(pid);
         buf.append('/');
         buf.append(bDef);
         buf.append('/');
         buf.append(method);
         if (asOfDateTime!=null) {
             SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
             buf.append('/');
             buf.append(formatter.format(asOfDateTime));
         }
         if (parms!=null) {
             Iterator iter=parms.keySet().iterator();
             int i=0;
             while (iter.hasNext()) {
                 String name=(String) iter.next();
                 String value=(String) parms.get(name);
                 if (i==0) {
                     buf.append("?");
                 } else {
                     buf.append("&");
                 }
                 try {
                     buf.append(URLEncoder.encode(name, "UTF-8"));
                     buf.append('=');
                     buf.append(URLEncoder.encode(value, "UTF-8"));
                 } catch (UnsupportedEncodingException e) {
                     // won't happen
                 }
                 i++;
             }
         }
         return get(buf.toString());
     }
 
     /**
      * Get data via HTTP and write it to an OutputStream, following redirects, 
      * and supplying credentials if the host is the Fedora server.
      */
     public void get(String url, OutputStream out) 
             throws IOException {
         InputStream in=get(url);
         StreamUtility.pipeStream(in, out, 4096);
     }
 
     /**
      * Get data via HTTP as an InputStream, following redirects, and supplying 
      * credentials if the host is the Fedora server.
      */
     public InputStream get(String url) 
             throws IOException {
         GetMethod get=null;
         boolean ok=false;
         try {
             HttpClient client=new HttpClient(m_cManager);
             client.setConnectionTimeout(20000); // wait 20 seconds max
             client.getState().setCredentials(null, m_host, m_creds);
             client.getState().setAuthenticationPreemptive(true); // don't bother with challenges
            get=new GetMethod(url);
            get.setDoAuthentication(true);
            get.setFollowRedirects(true);
             Dimension d=null;
            if (Administrator.INSTANCE!=null) {
                d=Administrator.PROGRESS.getSize();
                // if they're using Administrator, tell them we're downloading...
                Administrator.PROGRESS.setString("Dowloading " + url + " . . .");
                Administrator.PROGRESS.setValue(100);
                Administrator.PROGRESS.paintImmediately(0, 0, (int) d.getWidth()-1, (int) d.getHeight()-1);
             }
            int resultCode=client.executeMethod(get);
             if (resultCode!=200) {
                 System.err.println(get.getResponseBodyAsString());
                 throw new IOException("Server returned error: " 
                         + resultCode + " " + HttpStatus.getStatusText(resultCode));
             }
             ok=true;
             if (Administrator.INSTANCE!=null) {
                 // cache it to a file
                 File tempFile=File.createTempFile("fedora-client-download-", null);
                 tempFile.deleteOnExit();
                 HashMap PARMS=new HashMap();
                 PARMS.put("in", get.getResponseBodyAsStream());
                 PARMS.put("out", new FileOutputStream(tempFile));
                 // do the actual download in a safe thread
                 SwingWorker worker=new SwingWorker(PARMS) {
                     public Object construct() {
                         try {
                             StreamUtility.pipeStream(
                                     (InputStream) parms.get("in"), 
                                     (OutputStream) parms.get("out"), 
                                     8192);
                         } catch (Exception e) {
                             thrownException=e;
                         }
                         return "";
                     }
                 };
                 worker.start();
                 // The following code will run in the (safe) 
                 // Swing event dispatcher thread.
                 int ms=200;
                 while (!worker.done) {
                     try {
                         Administrator.PROGRESS.setValue(ms);
                         Administrator.PROGRESS.paintImmediately(0, 0, (int) d.getWidth()-1, (int) d.getHeight()-1);
                         Thread.sleep(100);
                         ms=ms+100;
                         if (ms>=2000) ms=200;
                     } catch (InterruptedException ie) { }
                 }
                 if (worker.thrownException!=null)
                     throw worker.thrownException;
                 Administrator.PROGRESS.setValue(2000);
                 Administrator.PROGRESS.paintImmediately(0, 0, (int) d.getWidth()-1, (int) d.getHeight()-1);
                 try {
                     Thread.sleep(100);
                 } catch (InterruptedException ie) { }
                 return new FileInputStream(tempFile);
             }
             return get.getResponseBodyAsStream();
         } catch (Exception e) {
             throw new IOException(e.getMessage());
         } finally {
             if (get!=null && !ok) get.releaseConnection();
             if (Administrator.INSTANCE!=null) {
                 Administrator.PROGRESS.setValue(0);
                 Administrator.PROGRESS.setString("");
             }
         }
 
     }
 
     /**
      * Test this class.
      */
     public static void main(String[] args) {
         try {
             if (args.length==7 || args.length==8) {
                 Date asOfDateTime=null;
                 if (args.length==8) {
                     SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                     asOfDateTime=format.parse(args[7], new ParsePosition(0));
                 }
                 FileOutputStream out=new FileOutputStream(new File(args[6]));
                 Downloader downloader=new Downloader(args[0], 
                         Integer.parseInt(args[1]), args[2], args[3]);
                 downloader.getDatastreamContent(args[4], args[5], asOfDateTime, out);
             } else {
                 System.err.println("Usage: Downloader host port user pass pid dsid outfile [MMDDYYTHH:MM:SS]");
             }
         } catch (Exception e) {
             e.printStackTrace();
             System.err.println("ERROR: " + e.getMessage());
         }
     }
 
 }
