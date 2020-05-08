 package play.modules.webtest;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.MethodRule;
 import org.junit.runners.model.FrameworkMethod;
 import org.junit.runners.model.Statement;
 import org.openqa.selenium.WebDriver;
 
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 import play.Invoker;
 import play.Play;
 import play.server.Server;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.ServerSocket;
 
 public abstract class WebTest {
 
     @Rule
     public StartPlay startPlayBeforeTests = StartPlay.rule();
 
     public static WebDriver driver;
     public static String httpPort;
     public static String httpsPort;
 
     @BeforeClass
     public static void startDriver() {
         httpPort = "9000";
         httpsPort = "9001";
 
         if (portInUse(9000)) {
             httpPort = "9500";
             httpsPort = "9501";
         } else {
             System.setProperty("play.id", "test");
             Play.init(new File("."), "test");
             Play.javaPath.add(Play.getVirtualFile("test"));
         }
 
         Server server = new Server(new String[] { "--http.port=" + httpPort, "--https.port=" + httpsPort });
 
         if (driver == null) {
             String chromeDriverLocation = System.getProperty("webdriver.chrome.driver");
             if (chromeDriverLocation == null) {
                 System.setProperty("webdriver.chrome.driver", "/Applications/chromedriver");
             }
             // driver = new ChromeDriver();
             // driver = new FirefoxDriver();
            driver = new HtmlUnitDriver(true);
         }
     }
 
     private static boolean portInUse(int port) {
         boolean portInUse = false;
 
         ServerSocket socket = null;
         try {
             socket = new ServerSocket(port);
         } catch (IOException e) {
             portInUse = true;
         } finally {
             // Clean up
             if (socket != null) {
                 try {
                     socket.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
 
         return portInUse;
     }
 
     @AfterClass
     public static void stopDriver() {
         if (driver != null) {
             driver.quit();
         }
     }
 
     protected void loginAs(String user, String password) {
         new LoginScreen(driver).login(user, password);
     }
 
     public enum StartPlay implements MethodRule {
 
         INVOKE_THE_TEST_IN_PLAY_CONTEXT {
 
             public Statement apply(final Statement base, FrameworkMethod method, Object target) {
 
                 return new Statement() {
 
                     @Override
                     public void evaluate() throws Throwable {
 
                         try {
                             Invoker.invokeInThread(new Invoker.DirectInvocation() {
 
                                 @Override
                                 public void execute() throws Exception {
                                     try {
                                         base.evaluate();
                                     } catch (Throwable e) {
                                         throw new RuntimeException(e);
                                     }
                                 }
 
                                 @Override
                                 public Invoker.InvocationContext getInvocationContext() {
                                     return new Invoker.InvocationContext("JUnitTest");
                                 }
                             });
                         } catch (Throwable e) {
                             throw ExceptionUtils.getRootCause(e);
                         }
                     }
                 };
             }
         },
         JUST_RUN_THE_TEST {
 
             public Statement apply(final Statement base, FrameworkMethod method, Object target) {
                 return new Statement() {
 
                     @Override
                     public void evaluate() throws Throwable {
                         base.evaluate();
                     }
                 };
             }
         };
 
         public static StartPlay rule() {
             return Play.id.equals("test") ? INVOKE_THE_TEST_IN_PLAY_CONTEXT : JUST_RUN_THE_TEST;
         }
     }
 }
