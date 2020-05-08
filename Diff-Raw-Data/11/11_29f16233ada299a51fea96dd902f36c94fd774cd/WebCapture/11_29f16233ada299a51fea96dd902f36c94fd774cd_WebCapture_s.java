 package org.filirom1.webcapture;
 
 import com.thoughtworks.selenium.DefaultSelenium;
 import com.thoughtworks.selenium.Selenium;
 import org.apache.http.annotation.ThreadSafe;
 import org.openqa.selenium.server.RemoteControlConfiguration;
 import org.openqa.selenium.server.SeleniumServer;
 
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Logger;
 
 /**
  * <p>
  * Define a browser, or DEFAULT_BROWSER will be used<br />
  * Define a port, or DEFAULT_PORT will be used. <br />
  * Define a timeout, or DEFAULT_TIMEOUT will be used.  <br />
  * </p>
  * <p>Call start() to start a Selenium Server. </p>
  * <p>Call captureScreenShotAndHTML to wait for a screenshot and an html captured.</p>
  * <p>Call captureScreenShotAndHTMLAsync to execute a callback after a screenshot and an html is captured.</p>
  */
 @ThreadSafe
 public class WebCapture {
     private static Logger log = Logger.getLogger(WebCapture.class.getName());
 
     public static final int DEFAULT_PORT = 8765;
     public static final int DEFAULT_TIMEOUT = 30000;
     public static final String DEFAULT_BROWSER = "*firefox";
     public static final int DEFAULT_CONCURRENT_BROWSER = 4;
 
     private int port = DEFAULT_PORT;
     private int timeout = DEFAULT_TIMEOUT;
     private String browser = DEFAULT_BROWSER;
     private int concurrentBrowser = DEFAULT_CONCURRENT_BROWSER;
 
 
     private SeleniumServer server;
     private ExecutorService executorService;
     private volatile boolean started = false;
 
    void start() {
         if (!started) {
             executorService = Executors.newScheduledThreadPool(getConcurrentBrowser());
             log.info("Start Selenium Server on port " + port);
             try {
                 RemoteControlConfiguration rcl = new RemoteControlConfiguration();
                 rcl.setPort(getPort());
                 server = new SeleniumServer(rcl);
                 server.start();
                 log.info("Selenium Server Started");
                 started = true;
             } catch (Exception e) {
                 throw new RuntimeException("Unable to start Selenium Server.", e);
             }
         } else {
             log.warning("Web Capture already started.");
         }
     }
 
    void stop() {
         if (started) {
             log.info("Stop Web Capture");
             executorService.shutdownNow();
             server.stop();
             log.info("Web Capture Stopped");
             started = false;
         } else {
             log.warning("Web Capture not started.");
         }
     }
 
     /**
      * Capture a screen-shot and an html, and wait until the captures are ready.
      * <p/>
      * Start the server if not started.
      *
      * @param url to capture
      * @return the html and screenshot
      */
    ScreenShotAndHTMLWrapper captureScreenShotAndHTML(String url) {
         if (!started) {
             start();
         }
         ScreenShotAndHTMLWrapper wrapper = new ScreenShotAndHTMLWrapper();
         try {
             log.info("captureScreenShotAndHTML " + url);
             HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
             connection.setRequestMethod("HEAD");
             int responseCode = connection.getResponseCode();
             if (responseCode != 404) {
                 final Selenium client = new DefaultSelenium("localhost", getPort(), getBrowser(), url);
                 try {
                     TimerTask timerTask = new TimerTask() {
                         public void run() {
                             log.warning("Firefox Driver blocked, url=$url");
                             client.stop();
                         }
                     };
                     new Timer().schedule(timerTask, (long) timeout);
                     client.start();
                     client.setTimeout(String.valueOf(timeout));
                     client.windowMaximize();
                     client.open(url);
                     timerTask.cancel();
                     wrapper.setHtml(client.getHtmlSource());
                     wrapper.setScrenShotBase64(client.captureEntirePageScreenshotToString(""));
                 } catch (Exception ie) {
                     client.stop();
                 }
                 client.stop();
             } else {
                 log.info("URL " + url + " is not reachable.");
             }
         } catch (Exception e) {
             throw new RuntimeException("Unable to captureScreenShotAndHTML " + url, e);
         }
         return wrapper;
     }
 
     /**
      * Capture an html and a screenshot and execute a callback after capture (do not wait the captures to be ready,
      * this method returns instantly)
      * <p/>
      * Start the server if not started.
      *
      * @param url      to capture
      * @param callback to execute after capture.
      */
    void captureScreenShotAndHTMLAsync(final String url, final AfterCaptureCallback callback) {
         if (!started) {
             start();
         }
         executorService.submit(new Runnable() {
             public void run() {
                 final ScreenShotAndHTMLWrapper wrapper = captureScreenShotAndHTML(url);
 
                 //Do not wait for callback being executed.
                 new Thread(new Runnable() {
                     public void run() {
                         callback.run(wrapper);
                     }
                 }).start();
             }
         });
     }
 
     public int getPort() {
         return port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public int getTimeout() {
         return timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 
     public String getBrowser() {
         return browser;
     }
 
     public void setBrowser(String browser) {
         this.browser = browser;
     }
 
     public int getConcurrentBrowser() {
         return concurrentBrowser;
     }
 
     public void setConcurrentBrowser(int concurrentBrowser) {
         this.concurrentBrowser = concurrentBrowser;
     }
 }
