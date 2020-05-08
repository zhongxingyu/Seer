 package org.melati;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mortbay.resource.FileResource;
 
 import net.sourceforge.jwebunit.junit.WebTestCase;
 
 /**
  *
  * Much thanks to 
  * http://today.java.net/pub/a/today/2007/04/12/embedded-integration-testing-of-web-applications.html
  * 
  * @author timp
  * @since 2008/01/01
  * 
  */
 public class JettyWebTestCase extends WebTestCase {
 
   private static Server server;
   private static boolean started = false;
   protected static String contextName = "melatitest";
   protected static String webAppDirName = "src/test/webapp";
   protected static String referenceOutputDir = "src/test/resources";
 
   protected static int actualPort;  
   /**
    * 
    * Default constructor.
    */
   public JettyWebTestCase() {
     super();
   }
 
   /**
    * Constructor, with name.
    * @param name
    */
   public JettyWebTestCase(String name) {
     super(name);
   }
 
   protected void setUp() throws Exception {
     // Port 0 means "assign arbitrarily port number"
     actualPort = startServer(0);
     getTestContext().setBaseUrl("http://localhost:" + actualPort + "/" );
   }
 
   protected void tearDown() throws Exception {
     super.tearDown();
   }
   
   /**
    * If you don't know by now.
    * @param args
    * @throws Exception
    */
   public static void main(String[] args) throws Exception {
     actualPort = startServer(8080);
   }
 
   protected static int startServer(int port) throws Exception {
     
     if (!started) { 
       server = new Server(port);
       WebAppContext wac = new WebAppContext(
               getWebAppDirName(), "/" + getContextName());
       FileResource.setCheckAliases(false); 
       server.addHandler(wac);
       server.start();
       wac.dumpUrl();
       started = true;
     }
     // getLocalPort returns the port that was actually assigned
     return server.getConnectors()[0].getLocalPort();
     
   }
   
   protected int getActualPort() { 
     return actualPort;
   }
   
   /**
    * Just to say hello.
    */
   public void testIndex() {
     beginAt("/");
     assertTextPresent("Hello World!");
   }
   
    /**
    * {@inheritDoc}
    * @see net.sourceforge.jwebunit.junit.WebTestCase#beginAt(java.lang.String)
    */
   public void beginAt(String url) { 
     super.beginAt(contextUrl(url));
   }
   /**
    * {@inheritDoc}
    * @see net.sourceforge.jwebunit.junit.WebTestCase#gotoPage(java.lang.String)
    */
   public void gotoPage(String url) { 
     System.err.println(contextUrl(url));
     super.gotoPage(contextUrl(url));
   }
   protected String contextUrl(String url) { 
     return "/" + getContextName()  + url;
   }
 
   /**
    * @return the contextName
    */
   public static String getContextName() {
     return contextName;
   }
   
   /**
    * @return relative path of webapp dir
    */
   public static String getWebAppDirName() {
     return webAppDirName;
   }
 
   /**
    * @param contextName the contextName to set
    */
   protected static void setContextName(String contextName) {
     JettyWebTestCase.contextName = contextName;
   }
 
   /**
    * @param webAppDirName the webAppDirName to set
    */
   protected static void setWebAppDirName(String webAppDirName) {
     JettyWebTestCase.webAppDirName = webAppDirName;
   }
 
   private boolean generateCached() { 
     return false;
   }
   protected void assertPageEqual(String url, String fileName) throws Exception { 
     beginAt(url);
     String generated = getTester().getPageSource();
     File referenceFile = new File(referenceOutputDir, fileName);
     if (referenceFile.exists() && ! generateCached()) {
       FileInputStream file = new FileInputStream (referenceFile);
       byte[] b = new byte[file.available()];
       file.read(b);
       file.close ();
       String cached = new String(b);
       assertEquals(cached, generated);
     } else { 
       FileOutputStream file = new FileOutputStream(referenceFile);
       file.write(generated.getBytes());
      file.close();
       fail("Reference output file generated: " + referenceFile.getCanonicalPath() + " modify generateCached and rerun");
     }
   }
   
 }
