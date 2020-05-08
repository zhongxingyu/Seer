 /* The contents of this file are subject to the license and copyright terms
  * detailed in the license directory at the root of the source tree (also
  * available online at http://www.fedora.info/license/).
  */
 
 package fedora.test.integration;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import java.util.Random;
 
 import junit.framework.JUnit4TestAdapter;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.UsernamePasswordCredentials;
 import org.apache.commons.httpclient.auth.AuthScope;
 import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.PartSource;
 
 import org.junit.Test;
 
 import fedora.server.access.FedoraAPIA;
 import fedora.server.management.FedoraAPIM;
 import fedora.server.types.gen.MIMETypedStream;
 
 import fedora.test.FedoraTestCase;
 
 /**
  * Performs ingest and export tests using large datastreams.
  * This test creates and moves very large files and as such
  * takes significant time to run.
  *
  * Non-SSL transports *MUST* be available for all APIs in order
  * for this test to run properly.
  *
  * @author Bill Branan
  */
 public class TestLargeDatastreams
         extends FedoraTestCase {
 
     private FedoraAPIM apim;
 
     private FedoraAPIA apia;
 
     private static final String pid = "demo:LargeDatastreams";
 
     private static byte[] DEMO_FOXML;
 
     private static long gigabyte = 1073741824;
     private long fileSize = gigabyte * 5;
 
     static {
         // Test FOXML object
         StringBuilder sb = new StringBuilder();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
         sb.append("<foxml:digitalObject VERSION=\"1.1\" PID=\"" + pid + "\" ");
         sb.append("  xmlns:foxml=\"info:fedora/fedora-system:def/foxml#\" ");
         sb.append("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ");
         sb.append("  xsi:schemaLocation=\"info:fedora/fedora-system:def/foxml# ");
         sb.append("  http://www.fedora.info/definitions/1/0/foxml1-1.xsd\">");
         sb.append("  <foxml:objectProperties>");
         sb.append("    <foxml:property NAME=\"info:fedora/fedora-system:def/model#state\" VALUE=\"A\"/>");
         sb.append("  </foxml:objectProperties>");
         sb.append("</foxml:digitalObject>");
 
         try {
             DEMO_FOXML = sb.toString().getBytes("UTF-8");
         } catch (UnsupportedEncodingException uee) {
         }
     }
 
     @Override
     public void setUp() throws Exception {
         apim = getFedoraClient().getAPIM();
         apia = getFedoraClient().getAPIA();
     }
 
     @Test
     public void testLargeDatastreamIO() throws Exception {
         System.out.println("Running testLargeDatastreams...");
 
         // Create the object, add the datastream
         System.out.println("  Uploading a file of size " + fileSize + " bytes...");
         String uploadId = upload();
         System.out.println("  Creating data object...");
         apim.ingest(DEMO_FOXML, FOXML1_1.uri, "new foxml object");
         System.out.println("  Adding uploaded file as a datastream...");
         apim.addDatastream(pid,
                            "DS1",
                            null,
                            "Large Datastream",
                            false,
                            "text/plain",
                            null,
                            uploadId,
                            "M",
                            "A",
                            null,
                            null,
                            "Adding Large Datastream");
 
         // Export the datastream via API-A-Lite
         System.out.println("  Exporting datastream via API-A-Lite...");
         long exportFileAPIALiteSize = exportAPIALite("DS1");
 
         // Check the file size to make sure the entire file was retrieved
         assertEquals(fileSize, exportFileAPIALiteSize);
 
         // Export the datastream via API-A
         System.out.println("  Exporting datastream via API-A...");
         long exportFileAPIASize = exportAPIA("DS1");
 
         // If file was small enough to transfer via API-A
         if(exportFileAPIASize >= 0) {
             assertEquals(fileSize, exportFileAPIASize);
         }
 
         // Clean up
         apim.purgeObject(pid, "Removing Test Object", false);
         System.out.println("  Test Complete.");
     }
 
     private String upload() throws Exception {
         String url = apia.describeRepository().getRepositoryBaseURL() +
                      "/management/upload";
         EntityEnclosingMethod httpMethod = new PostMethod(url);
         httpMethod.setDoAuthentication(true);
         httpMethod.getParams().setParameter("Connection", "Keep-Alive");
         httpMethod.setContentChunked(true);
         Part[] parts = {new FilePart("file", new SizedPartSource())};
         httpMethod.setRequestEntity(
                 new MultipartRequestEntity(parts, httpMethod.getParams()));
 
         HttpClient client = new HttpClient();
         client.getParams().setAuthenticationPreemptive(true);
         client.getState().setCredentials(
                 new AuthScope(getHost(), Integer.valueOf(getPort()), "realm"),
                 new UsernamePasswordCredentials(getUsername(), getPassword()));
 
         client.executeMethod(httpMethod);
         String response = new String(httpMethod.getResponseBody());
         response = response.replaceAll("\r", "").replaceAll("\n", "");
 
         httpMethod.releaseConnection();
 
         return response;
     }
 
     private long exportAPIALite(String dsId) throws Exception {
         String url = apia.describeRepository().getRepositoryBaseURL() +
                      "/get/" + pid + "/" + dsId;
         HttpMethod httpMethod = new GetMethod(url);
         httpMethod.setDoAuthentication(true);
         httpMethod.getParams().setParameter("Connection", "Keep-Alive");
 
         HttpClient client = new HttpClient();
         client.getParams().setAuthenticationPreemptive(true);
         client.getState().setCredentials(
                 new AuthScope(getHost(), Integer.valueOf(getPort()), "realm"),
                 new UsernamePasswordCredentials(getUsername(), getPassword()));
 
         client.executeMethod(httpMethod);
         BufferedInputStream dataStream =
                 new BufferedInputStream(httpMethod.getResponseBodyAsStream());
 
         long bytesRead = 0;
         while (dataStream.read() >= 0) {
             ++bytesRead;
         }
 
         return bytesRead;
     }
 
     private long exportAPIA(String dsId) throws Exception {
         MIMETypedStream fileStream = null;
         try {
             fileStream = apia.getDatastreamDissemination(pid, dsId, null);
         } catch(Exception e) {
             if(e.getMessage().indexOf("The datastream you are attempting " +
                                       "to retrieve is too large") > 0) {
                 System.out.println("  Expected error generated in API-A export:");
                 System.out.println("    Error text: " + e.getMessage());
                 return -1;
             } else {
                 fail("  Unexpected exception encountered in exportAPIA: " +
                      e.getMessage());
             }
         }
 
         return fileStream.getStream().length;
     }
 
     public static junit.framework.Test suite() {
         return new JUnit4TestAdapter(TestLargeDatastreams.class);
     }
 
     private class SizedPartSource implements PartSource {
 
         public InputStream createInputStream() throws IOException {
             return new SizedInputStream();
         }
 
         public String getFileName() {
             return "file";
         }
 
         public long getLength() {
             return fileSize;
         }
     }
 
     private class SizedInputStream extends InputStream {
 
         private long bytesRead = 0;
         Random generator = new Random(System.currentTimeMillis());
 
         @Override
         public int available() throws IOException {
             return (int)(fileSize - bytesRead);
         }
 
         @Override
         public int read() throws IOException {
             if(bytesRead < fileSize) {
                 ++bytesRead;
                return generator.nextInt();
             } else {
                 return -1;
             }
         }
     }
 }
