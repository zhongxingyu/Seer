 package fedora.server;
 
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.errors.ServerShutdownException;
 import fedora.server.errors.ModuleInitializationException;
 import fedora.server.errors.ModuleShutdownException;
 import fedora.server.storage.DOManager;
 
 import java.io.IOException;
 import java.io.File;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class BasicServer 
         extends Server {
         
     public BasicServer(NodeList configNodes, File fedoraHomeDir) 
             throws ServerInitializationException,
                    ModuleInitializationException {
         super(configNodes, fedoraHomeDir);
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
     
     public String getHelp() {
         return "This can be configured such and such a way...etc..";
     }
     
 }
