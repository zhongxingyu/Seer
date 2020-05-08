 package lab;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 //import java.util.logging.LoggingMXBean;
 
 public class ReportFileHandler extends FileRequestHandler {
 	public void get() {
         if (request.getHeaders().get("referer") == null) {
             response.setStatus(403);
             return;
         }
 
 
         URL referrer = null;
         try {
             referrer = new URL(request.getHeaders().get("referer"));
         } catch (MalformedURLException e) {
             response.setStatus(403);
             e.printStackTrace();
             return;
         }
 
        if (!referrer.getPath().equals("/") && !referrer.getPath().startsWith("/reports")) {
             Logger.info("Someone tried to access a report from a wrong referrer!");
             response.setStatus(403);
             return;
         }
 
        super.get();
 	}
 
 
 }
