 /**
  * XWeb project
  * https://github.com/abdollahpour/xweb
  * Hamed Abdollahpour - 2013
  */
 
 package ir.xweb.module;
 
 import org.apache.commons.fileupload.FileItem;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.lang.Math;
 import java.util.HashMap;
 import java.io.File;
 
 public class LogModule extends Module {
 
     private final static Logger logger = LoggerFactory.getLogger(LogModule.class);
 
     private final static String SESSION_LAST_POSITION = "module_log_position_";
 
     private final static int MAX_READ = 30 * 1024;
 
     private File log;
 
     public LogModule(
             final Manager manager,
             final ModuleInfo info,
             final ModuleParam properties) throws ModuleException {
 
         super(manager, info, properties);
 
         String path = properties.getString("path", null);
         log = new File(path);
     }
 
     @Override
     public void process(
             final ServletContext context,
             final HttpServletRequest request,
             final HttpServletResponse response,
             final ModuleParam params,
             final HashMap<String, FileItem> files) throws IOException {
 
         /** We have different log history for different IDs, so we can have different HTMLs **/
        final String id = params.getString("id", "0");
 
         if(log != null && log.exists()) {
             response.setContentType("text/plain");
 
             long last = log.lastModified();
             long len = log.length();
 
             Long position = (Long) request.getSession().getAttribute(SESSION_LAST_POSITION + id);
 
             // init
             if(position == null) {
                 position = Math.max(0, len - MAX_READ);
             }
 
             final PrintWriter writer = response.getWriter();
             final FileReader reader = new FileReader(log);
             if(position > 0) {
                 reader.skip(position);
             }
 
             char[] buffer = new char[1024];
             int size;
 
             while((size = reader.read(buffer)) > 0) {
                 writer.write(buffer, 0, size);
                 position += size;
             }
 
             request.getSession().setAttribute(SESSION_LAST_POSITION + id, position);
 
             reader.close();
         } else {
             logger.error("Log file not found: " + log);
         }
     }
 }
