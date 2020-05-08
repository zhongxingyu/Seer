 package org.cggh.casutils.test;
 
 import java.io.File;
 import java.net.ConnectException;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.cggh.casutils.CasProtectedResourceDownloader;
 import org.cggh.casutils.NotFoundException;
 
 import junit.framework.TestCase;
 
 /**
  * @author timp
  * @since 10 December 2010 10:21:17
  *
  */
 abstract public class CasProtectedResourceDownloaderSpec extends TestCase {
 
   abstract String getCasProxyProtocol(); 
   abstract String getCasProxyHost();
   
   // Default https port is 443
   abstract String getCasProxyPort();
 
   abstract String getServiceHostUrl();
   
   // NOTE /repository goes to orbeon, but /repo behaves
   static final String CONTEXTPATH = "/repository";
   static final String SERVICEPATH = "/service/";
   
   String getTicketGrantingHostAndPort() { 
     return getCasProxyHost() + (getCasProxyPort() == "" ? "" : ":" + getCasProxyPort());
   }
   
   // Note we are not currently specifying port
   String getServiceUrl() { 
 	  return getServiceHostUrl() + CONTEXTPATH + SERVICEPATH;
   }
   String getContentUrl() { 
 	  return getServiceUrl() + "content/";
   }
   String getTestCollectionUrl() { 
     return getContentUrl() + "studies";
   }
   
   // This needs to be manually created and have the zip file from test/resources uploaded
   // Go to http://cloud1.cggh.org/repository/contributor/
   // login as cora@example.org, password bar
   
   abstract String getStudyId();
  
   String getTestStudyUrl() { 
     return getTestCollectionUrl() + "/" + getStudyId() ;
   }
   
   abstract String getTestZipFileUrl(); 
   abstract String getUser();
   abstract String getPassword();
 
 
   
   static final HttpClient client = new HttpClient();
 
   public CasProtectedResourceDownloaderSpec(String name) {
     super(name);
   }
 
   /**
    * Test method for {@link org.cggh.casutils.CasProtectedResourceDownloader#download(java.lang.String)}.
    */
   public void testDownload() throws Exception {
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(getCasProxyProtocol(),getTicketGrantingHostAndPort(),getUser(), getPassword(), "/tmp/");
     System.err.println(it);
     String result = it.download(getTestStudyUrl());
     assertEquals("file:///tmp/" + getStudyId(), result);
   }
 
 
   
 
   public void testGetStudy() throws Exception {
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(getCasProxyProtocol(), getTicketGrantingHostAndPort(),getUser(), getPassword(), "/tmp/");
     String result = it.download(getTestStudyUrl());
     assertEquals("file:///tmp/" + getStudyId() , result);
     File r = new File("/tmp/" + getStudyId());
     assertTrue("deleting " + r, r.delete());
     CasProtectedResourceDownloader badPasswordSupplied = new CasProtectedResourceDownloader(getCasProxyProtocol(),getTicketGrantingHostAndPort(),"adam@example.org", "bair", "/tmp/"); 
     try { 
       badPasswordSupplied.download(getTestStudyUrl());
       fail("Should have bombed");
     } catch (RuntimeException e) { 
       e = null;
     }
   }
   
   
   /**
    * Test method for {@link org.cggh.casutils.CasProtectedResourceDownloader#downloadUrlToFile(java.lang.String, java.io.File)}.
    */
   public void testDownloadUrlToFile() throws Exception {
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(getCasProxyProtocol(),getTicketGrantingHostAndPort(),getUser(), getPassword(), "/tmp/");
     File f = new File("t.tmp");
     assertEquals(200, it.downloadUrlToFile(getTestStudyUrl(), f));
     assertTrue("deleting " + f, f.delete());
   }
   
  public void FAILINGtestDownloadBadUrlToFile() throws Exception {
     CasProtectedResourceDownloader it = 
         new CasProtectedResourceDownloader(
             getCasProxyProtocol(),
             getTicketGrantingHostAndPort(),
             getUser(), getPassword(), "/tmp/");
     try { 
       String url = getServiceHostUrl() +  "/not_there";
       File f = new File("t.tmp");
       it.downloadUrlToFile(url, f);
       assertTrue("deleting " + f, f.delete());
       fail("Should have bombed with 404 for " + url);
     } catch (NotFoundException e) {
       e = null;
     }
   }
 
   public void testGetUrlWithParameters() throws Exception { 
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(getCasProxyProtocol(),getTicketGrantingHostAndPort(),getUser(), getPassword(), "/tmp/");
     String result = it.download(getTestStudyUrl() + "?foo=bar");
     assertEquals("file:///tmp/" + getStudyId() + "_foo=bar", result);
     File f = new File("/tmp/"+ getStudyId() + "_foo=bar");
     assertTrue("deleting " + f, f.delete());    
   }
   
   public void testDownloadBadStatusToFile() throws Exception {
     CasProtectedResourceDownloader it = 
         new CasProtectedResourceDownloader(
             getCasProxyProtocol(),getTicketGrantingHostAndPort(),getUser(), getPassword(), "/tmp/");
     File f = new File("t.tmp");
     try { 
       it.downloadUrlToFile("http://localhost:8080/httpstatus/http?status=999", f);
       fail("Should have bombed");
     } catch (ConnectException e) {
       System.err.println("Have you installed https://github.com/timp21337/http-status-generator");
       e = null;
     } catch (NotFoundException e) {
       e = null;
       System.err.println("Have you installed https://github.com/timp21337/http-status-generator");
     } catch (RuntimeException e) { 
       assertTrue(e.getMessage(), e.getMessage().startsWith("Invalid response code (999) from server"));
     }
     // It should not have been created
     assertFalse("deleting " + f, f.delete());    
   }
 
   
   
   public void testBadCasProtocolTrapped() throws Exception { 
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(
         "http://",getTicketGrantingHostAndPort() + ":443", 
         getUser(), getPassword(), "/tmp/");
     File f = new File("t.tmp");
     try { 
       it.downloadUrlToFile(getTestStudyUrl(), f);
       fail("Should have bombed");
     } catch (RuntimeException e) { 
       e = null;
     }
     // It should not have been created
     assertFalse("deleting " + f, f.delete());    
   }
   
   public void testBadStatusHandled() throws Exception { 
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(
         "ftp://",getTicketGrantingHostAndPort(), 
         getUser(), getPassword(), "/tmp/");
     File f = new File("t.tmp");
     try { 
       it.downloadUrlToFile(getTestStudyUrl(), f);
       fail("Should have bombed");
     } catch (RuntimeException e) { 
       e = null;
     }
     // It should not have been created
     assertFalse("deleting " + f, f.delete());    
   }
   
   public void testDownloadZip() throws Exception {
     CasProtectedResourceDownloader it = new CasProtectedResourceDownloader(getCasProxyProtocol(),getTicketGrantingHostAndPort(),getUser(), getPassword(), "/tmp/");
     String downloadedUrl;
     try { 
       downloadedUrl = it.download(getTestZipFileUrl()); 
     } catch (RuntimeException e) { 
       throw new RuntimeException("Have you installed the cacerts? See README.txt", e);
     }
     assertEquals(getTestZipFileUrl().substring(getTestZipFileUrl().lastIndexOf('/')).replace('?','_'),
         downloadedUrl.substring(downloadedUrl.lastIndexOf('/')));
   }
     
   public void testBadDownloaderDownloadZip() throws Exception {
    
     CasProtectedResourceDownloader bad = new CasProtectedResourceDownloader(getCasProxyProtocol(),getTicketGrantingHostAndPort(),getUser(), "bad", "/tmp/");
     try { 
       bad.download(getTestZipFileUrl());
       fail("Should have bombed");
     } catch (RuntimeException e) { 
       e = null;
     }
     
   }
   
 }
