 package org.hydra.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.security.cert.X509Certificate;
 import java.util.Properties;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 
 import org.glite.security.util.DNHandler;
 import org.hydra.HydraAPI;
 import org.hydra.KeyPiece;
 import org.infinispan.Cache;
 import org.infinispan.manager.DefaultCacheManager;
 import org.joni.test.meta.ACLHandler;
 import org.joni.test.meta.ACLItem;
 
 import com.caucho.hessian.server.HessianServlet;
 
 public class HydraService extends HessianServlet implements HydraAPI {
 
     /**
      * 
      */
     private static final long serialVersionUID = 260572232283667758L;
     public static final String CACHE_CONFIG_FILE_OPT = "storeConfigFile";
     public static final String SUPER_USER_OPT = "superuser";
     private String _superUser;
     private DefaultCacheManager _storeManager;
     private static Cache<String, KeyPiece> _store = null;
     
     private static ThreadLocal<X509Certificate[]> certStore = new ThreadLocal<X509Certificate[]>();
 
     /**
      * Overridden to store the user certificate to the thread local storage
      * before the request is served.
      * 
      * @see com.caucho.hessian.server.HessianServlet#service(javax.servlet.ServletRequest,
      *      javax.servlet.ServletResponse)
      */
     @Override
     public void service(ServletRequest request, ServletResponse response) throws IOException, ServletException {
         // Interpret the client's certificate.
         X509Certificate[] cert = (X509Certificate[]) request.getAttribute("javax.servlet.request.X509Certificate");
         certStore.set(cert);
         
         super.service(request, response);
     }
 
     /**
      * Gets the user name, in current mode from the certificate chain in thread
      * local storage where they were stored during the initial call to the
      * service method.
      * 
      * @return The user name.
      */
     private String getUser() {
         // Interpret the client's certificate.
         X509Certificate[] cert = certStore.get();
         String user = DNHandler.getSubject(cert[0]).getRFCDNv2();
 
         return user;
     }
 
     /**
      * Creates the Hydra service object.
      * 
      * @param configFile The config file where to read the service config.
      * @throws IOException Thrown in case there is problems reading the config
      *             file.
      */
     public HydraService(String configFile) throws IOException {
         File testFile = new File(configFile);
         if (!testFile.exists()) {
             throw new FileNotFoundException("Configuration file \"" + configFile + "\" not found.");
         }
         if (testFile.isDirectory()) {
             throw new FileNotFoundException("The file \"" + configFile
                     + "\" given as a configuration file is a directory!");
         }
         Properties props = new Properties();
         props.load(new FileReader(configFile));
         String storeConfig = props.getProperty(CACHE_CONFIG_FILE_OPT);
         String superUser = props.getProperty(SUPER_USER_OPT);
         // if (superUser == null) {
         // throw new
         // IOException("No superuser setting found in the configuration file.");
         // }
         _superUser = superUser;
         testFile = new File(storeConfig);
         if (!testFile.exists()) {
             throw new FileNotFoundException("Storage configuration file \"" + storeConfig + "\" not found.");
         }
         if (testFile.isDirectory()) {
             throw new FileNotFoundException("The file \"" + storeConfig
                     + "\" given as a storage configuration file is a directory!");
         }
         System.out.println(configFile);
         _storeManager = new DefaultCacheManager(storeConfig);
         _store = _storeManager.getCache("hydra");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.hydra.HydraAPI#putKeyPiece(java.lang.String, org.hydra.KeyPiece)
      */
     public void putKeyPiece(String id, KeyPiece key) throws IOException {
         if (id == null) {
             throw new NullPointerException("Can't put keypiece with id null.");
         }
         if (key == null) {
             throw new NullPointerException("Can't put null keypiece.");
         }
         // TODO should batch this to avoid race condition...
         KeyPiece piece = _store.get(id);
         if (piece != null) {
             throw new IOException("Key already exists, remove it first.");
         }
         _store.put(id, key);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.hydra.HydraAPI#getKeyPiece(java.lang.String)
      */
     public KeyPiece getKeyPiece(String id) throws IOException {
         if (id == null) {
             throw new NullPointerException("Cannot search for a key piece with null id.");
         }
         KeyPiece piece = _store.get(id);
         if (piece == null) {
             throw new IOException("Key piece does not exist.");
         }
         if (!ACLHandler.hasReadAccess(getUser(), piece.getACL())) {
             throw new IOException("Access denied.");
         }
         return piece;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.hydra.HydraAPI#removeKeyPiece(java.lang.String)
      */
     public void removeKeyPiece(String id) throws IOException {
         if (id == null) {
             throw new NullPointerException("Cannot search for a key piece with null id.");
         }
         // TODO should batch this to avoid race condition...
         KeyPiece piece = _store.get(id);
         if (piece == null) {
             throw new IOException("Key piece does not exist.");
         }
         if (!ACLHandler.hasWriteAccess(getUser(), piece.getACL())) {
             
             System.out.println("User " + getUser() + " was denied access to " + id + ", the ACL has entries:");
             for(ACLItem item : piece.getACL()){
                 System.out.println(item.getUser());
             }
             throw new IOException("Access denied.");
         }
         _store.remove(id);
         return;
     }
 
     /* (non-Javadoc)
      * @see org.hydra.HydraAPI#getVersion()
      */
     public String getVersion() throws IOException {
        return "0.0.1";
     }
 }
