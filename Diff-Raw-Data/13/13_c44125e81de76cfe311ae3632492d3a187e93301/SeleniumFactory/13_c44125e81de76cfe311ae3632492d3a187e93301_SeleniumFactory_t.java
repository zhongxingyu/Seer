 package helpers;
 
 import org.openqa.selenium.UnsupportedCommandException;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: sarndeep
  * Date: 21/07/13
  * Time: 16:43
  * To change this template use File | Settings | File Templates.
  */
 public class SeleniumFactory extends Thread {
 
     private static WebDriver aDriver=null;
     private static boolean avoidRecursiveCall=false;
 
     public enum BrowserName{FIREFOX, GOOGLECHROME, SAUCELABS, OPERA, IE, GRID}
     public static BrowserName currentDriver;
     private static final BrowserName useThisDriver = BrowserName.valueOf(System.getProperty("browser"));
 
 
     public static WebDriver get() {
         if (aDriver==null) {
 
             switch(useThisDriver){
                 case FIREFOX:
                     FirefoxProfile profile = new FirefoxProfile();
                     profile.setEnableNativeEvents(true);
                     aDriver  = new FirefoxDriver();
                     currentDriver = BrowserName.FIREFOX;
                    break;
 
                 case GRID:
                     try {
                         DesiredCapabilities capability = DesiredCapabilities.firefox();
                         aDriver= new RemoteWebDriver(new URL("http://192.168.100.13:4444/wd/hub"), capability);
                         currentDriver = BrowserName.GRID;
                     }catch(MalformedURLException e){
                         e.printStackTrace();
                     }
                    break;


                case IE:
                    try {
                        DesiredCapabilities capability = DesiredCapabilities.internetExplorer();
                        aDriver= new RemoteWebDriver(new URL("http://192.168.100.13:4444/wd/hub"), capability);
                    }catch(MalformedURLException e){
                        e.printStackTrace();
                    }
                    break;
 
 
             }
 
             Runtime.getRuntime().addShutdownHook(
                     new Thread(){
                         public void run(){
                             SeleniumFactory.quit();
                         }
                     }
             );
 
 
         }else{
             try{
                // if(aDriver.getWindowHandle() !=null) {
                     //assume is still alive
                // }
             }catch(Exception e){
                 if(avoidRecursiveCall){
                     throw new RuntimeException();
                 }
 
                 quit();
                 aDriver=null;
                 avoidRecursiveCall =true;
                 return get();
             }
         }
         avoidRecursiveCall = false;
         return aDriver;
     }
 
     public static WebDriver get(String aURL, boolean maximise){
         get();
         aDriver.get(aURL);
         if(maximise){
             try{
                 aDriver.manage().window().maximize();
             }catch (UnsupportedCommandException e) {
                 System.out.println("Remote Driver does not support maximise");
             }catch (UnsupportedOperationException e) {
                 System.out.println("Opera does not support maximize yet");
             }
         }
         return aDriver;
     }
 
     public static void quit() {
         if(aDriver!=null) {
             try{
                 aDriver.quit();
                 aDriver=null;
             }catch (Exception e){
                 e.printStackTrace();
             }
         }
     }
 
 
 }
