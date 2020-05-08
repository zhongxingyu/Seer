 package fedora.server;
 
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.errors.ServerShutdownException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ModuleShutdownException;
 import fedora.server.storage.DOManager;
 
 import java.io.IOException;
 import java.io.File;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.XMLFormatter;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class BasicServer 
         extends Server {
         
     public BasicServer(Element rootElement, File fedoraHomeDir) 
             throws ServerInitializationException,
                    ModuleInitializationException {
         super(rootElement, fedoraHomeDir);
     }
    
    public void initServer() 
            throws ServerInitializationException {
        Logger logger=Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINEST);
        File logDir=new File(getHomeDir(), Server.LOG_DIR);
        String logPattern=logDir.getAbsolutePath() + "/server-log-%g.jxl";
        int maxSize=1024*1024;
        int maxFiles=5;
        FileHandler fh=null;
        try {
            fh=new FileHandler(logPattern, maxSize, maxFiles, false);
        } catch (IOException ioe) {
            throw new ServerInitializationException("IO Problem initializing loghandler: " + ioe.getMessage());
        }
        fh.setFormatter(new XMLFormatter());
        logger.addHandler(fh);
        setLogger(logger);
        logInfo("Here's my first log message");
    }
 
     /**
      * Gets the names of the roles that are required to be fulfilled by
      * modules specified in this server's configuration file.
      *
      * @returns String[] The roles.
      */
     public String[] getRequiredModuleRoles() {
         return new String[] {Server.DOMANAGER_CLASS};
     }
     
     public DOManager getManager(String name) {
         return null;
     }
     
 }
