 package org.melati;
 
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.webapp.WebAppContext;
 
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
 public abstract class JettyWebTestCase extends WebTestCase {
 
   private static Server server;
   private static String contextName = "test";
 
   /**
    * Constructor.
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
     server = new Server(0);
    WebAppContext wac = new WebAppContext(
        "src/test/webapp", "/" + contextName);
    org.mortbay.resource.FileResource.setCheckAliases(false); 
    server.addHandler(wac);
    server.start();
   
     // getLocalPort returns the port that was actually assigned
     int actualPort = server.getConnectors()[0].getLocalPort();
     getTestContext().setBaseUrl(
         "http://localhost:" + actualPort + "/" );
     wac.dumpUrl();
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
     server = new Server(8080);
    WebAppContext wac = new WebAppContext(
        "src/test/webapp", "/");
    org.mortbay.resource.FileResource.setCheckAliases(false); 
    server.addHandler(wac);
    server.start();
   }
   /**
    * Just to say hello.
    */
   public void testIndex() {
     beginAt("/index.html");
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
     super.gotoPage(contextUrl(url));
   }
   protected String contextUrl(String url) { 
     return "/" + contextName  + url;
   }
 }
