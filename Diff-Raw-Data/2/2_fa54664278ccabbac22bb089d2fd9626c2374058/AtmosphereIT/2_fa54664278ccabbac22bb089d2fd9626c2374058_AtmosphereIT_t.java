 package fi.gekkio.splake.atmosphere.it;
 
 import javax.annotation.Nullable;
 
 import lombok.val;
 
 import org.atmosphere.cpr.AtmosphereServlet;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.server.nio.SelectChannelConnector;
 import org.eclipse.jetty.servlet.ServletContextHandler;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.zkoss.zk.au.http.DHtmlUpdateServlet;
 import org.zkoss.zk.ui.http.DHtmlLayoutServlet;
 import org.zkoss.zk.ui.http.HttpSessionListener;
 
 import com.google.common.base.Predicate;
 
 public class AtmosphereIT {
 
     private static Server jetty;
     private static int port;
 
     @BeforeClass
     public static void init() throws Exception {
         jetty = new Server();
 
         val context = new ServletContextHandler(ServletContextHandler.SESSIONS);
         context.setContextPath("/");
 
         context.addEventListener(new HttpSessionListener());
 
         context.setResourceBase("src/test/resources/integration-test");
         val zkLoader = context.addServlet(DHtmlLayoutServlet.class, "*.zul");
         zkLoader.setInitParameter("update-uri", "/zkau");
 
         context.addServlet(DHtmlUpdateServlet.class, "/zkau/*");
 
         val atmosphere = context.addServlet(AtmosphereServlet.class, "/zkau/comet");
         atmosphere.setAsyncSupported(true);
 
         val connector = new SelectChannelConnector();
         jetty.addConnector(connector);
         jetty.setHandler(context);
 
         jetty.start();
         port = connector.getLocalPort();
     }
 
     @Test
     public void testServerPush() throws Exception {
         WebDriver driver = new FirefoxDriver();
         try {
            driver.get("http://127.0.0.1:" + port + "/index.zul");
 
             final JavascriptExecutor js = (JavascriptExecutor) driver;
             js.executeAsyncScript("zk.afterMount(arguments[0])");
 
             new WebDriverWait(driver, 10).until(new Predicate<WebDriver>() {
                 @Override
                 public boolean apply(@Nullable WebDriver input) {
                     return "5".equals(js.executeScript("return zk.Widget.$('$label').getValue()"));
                 }
             });
         } finally {
             driver.quit();
         }
     }
 
     @AfterClass
     public static void destroy() throws Exception {
         jetty.stop();
     }
 
 }
