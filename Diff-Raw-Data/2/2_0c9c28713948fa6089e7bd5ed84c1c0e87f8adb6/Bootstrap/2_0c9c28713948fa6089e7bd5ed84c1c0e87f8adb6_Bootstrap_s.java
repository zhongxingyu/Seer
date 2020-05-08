 package kkckkc.jsourcepad;
 
 import com.sun.net.httpserver.HttpServer;
 import kkckkc.jsourcepad.http.PreviewServer;
 import kkckkc.jsourcepad.model.Application;
 import kkckkc.jsourcepad.model.Window;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.util.Config;
 import kkckkc.jsourcepad.util.io.ErrorDialog;
 import kkckkc.utils.PerformanceLogger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.script.ScriptException;
 import java.awt.*;
 import java.io.*;
 import java.net.ServerSocket;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.concurrent.CountDownLatch;
 
 
 @SuppressWarnings("restriction")
 public class Bootstrap implements Runnable {
 	private static Logger logger = LoggerFactory.getLogger(Bootstrap.class);
     private String[] args;
 
     public static void main(String... args) throws IOException {
         logger.info("Initializing");
 
         if (checkAlreadyRunning()) {
             contactApplication(args);
         } else {
             startApplication(args);
         }
 	}
 
     private static void contactApplication(String... args) {
         for (String arg : args) {
             try {
                 URL url = new URL("http://localhost:" + Config.getHttpPort() + "/cmd/open?url=" + arg);
                 URLConnection conn = url.openConnection();
                 conn.connect();
 
                 // Get the response
                 try {
                     BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                     StringBuffer sb = new StringBuffer();
                     String line;
                     while ((line = rd.readLine()) != null) {
                         sb.append(line);
                     }
                     rd.close();
                     sb.toString();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     private static void startApplication(String... args) {
         PerformanceLogger.get().enter(Bootstrap.class.getName() + "#init");
 
         ThreadGroup tg = new ThreadGroup("Editor");
 
         Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
             public void uncaughtException(Thread t, Throwable e) {
                 ErrorDialog errorDialog = Application.get().getErrorDialog();
                 errorDialog.show(e, null);
             }
         });
 
         Runtime.getRuntime().addShutdownHook(new Thread() {
             public void run() {
                 PerformanceLogger.get().dump();
             }
         });
 
         Bootstrap b = new Bootstrap(args);
 
         Thread mainThread = new Thread(tg, b);
         mainThread.start();
     }
 
     private static boolean checkAlreadyRunning() {
         boolean portTaken = false;
         ServerSocket socket = null;
         try {
             socket = new ServerSocket(Config.getHttpPort());
         } catch (IOException e) {
             portTaken = true;
         } finally {
             // Clean up
             if (socket != null) try {
                 socket.close();
             } catch (IOException e) {
                 // Ignore
             }
         }
 
         return portTaken;
     }
 
 
     public Bootstrap(String... args) {
         this.args = args;
 	}
 	
 	@Override
 	public void run() {
         final CountDownLatch cdl = new CountDownLatch(2);
 
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
                 Application.get();
                 Application.get().getBeanFactory().getBean("applicationController");
 
 				// Create new window
 				try {
                     if (args == null || args.length == 0) {
	                    Window w = Application.get().getWindowManager().newWindow(new File("/Users/magnus/.jsourcepad/Shared/Bundles/Perl.tmBundle"));
                         w.getDocList().create();
                     } else {
                         for (String s : args) {
                             File f = new File(s);
                             if (f.isDirectory()) {
                                 Application.get().open(f);
                             }
                         }
 
                         for (String s : args) {
                             File f = new File(s);
                             if (! f.isDirectory()) {
                                 Application.get().open(f);
                             }
                         }
                     }
                     
 	                if (System.getProperty("startupScript") != null) {
 		                try {
 		                    Application.get().getWindowManager().getWindows().iterator().next().getScriptEngine().
                                     eval(new FileReader(System.getProperty("startupScript")));
 	                    } catch (ScriptException e1) {
 		                    e1.printStackTrace();
 	                    }
 	                }
                 } catch (IOException e) {
 	                throw new RuntimeException(e);
                 }
 
                 final BundleManager bundleManager = Application.get().getBundleManager();
                 
         		KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
         		focusManager.addKeyEventDispatcher(new GlobalKeyEventDispatcher(bundleManager));
 
                 HttpServer server = Application.get().getHttpServer();
                 PreviewServer ps = Application.get().getBeanFactory().getBean(PreviewServer.class);
 
                 PerformanceLogger.get().exit();
                 cdl.countDown();
             }
         });
 
 	    cdl.countDown();
 
         if ("true".equals(System.getProperty("immediateExitForBenchmark"))) {
             try {
                 cdl.await();
             } catch (InterruptedException e) {
                 throw new RuntimeException(e);
             }
             System.exit(0);
         }
     }
 }
