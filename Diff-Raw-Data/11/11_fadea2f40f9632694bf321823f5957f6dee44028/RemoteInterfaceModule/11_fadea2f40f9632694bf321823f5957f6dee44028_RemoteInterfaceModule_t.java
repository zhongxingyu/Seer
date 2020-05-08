 package polly.core.remote;
 
 import java.io.File;
 import java.io.IOException;
 
 import polly.configuration.PollyConfiguration;
 import polly.core.ShutdownManagerImpl;
 import polly.core.remote.tcp.AdministrationServer;
 import polly.core.users.UserManagerImpl;
 import polly.moduleloader.AbstractModule;
 import polly.moduleloader.ModuleLoader;
 import polly.moduleloader.SetupException;
 import polly.moduleloader.annotations.Module;
 import polly.moduleloader.annotations.Require;
 
 
 @Module(requires = {
     @Require(component = PollyConfiguration.class),
     @Require(component = UserManagerImpl.class),
     @Require(component = ShutdownManagerImpl.class)}
 )
 public class RemoteInterfaceModule extends AbstractModule {
 
     private PollyConfiguration config;
     private AdministrationManager adminManager;
     private ShutdownManagerImpl shutdownManager;
     private ProtocolHandler protocolHandler;
     private AdministrationServer server;
     private UserManagerImpl userManager;
     
     public RemoteInterfaceModule(ModuleLoader loader) {
         super("PORAT", loader, false);
     }
     
     
     
     @Override
     public void beforeSetup() {
         this.config = this.requireNow(PollyConfiguration.class);
         this.userManager = this.requireNow(UserManagerImpl.class);
         this.shutdownManager = this.requireNow(ShutdownManagerImpl.class);
     }
     
     
     
     @Override
     public void setup() throws SetupException {
         this.initKeyStore(this.config.getKeyStoreFile(), 
             this.config.getKeyStorePassword());
         
         this.adminManager = new AdministrationManager(this.userManager);
         this.protocolHandler = new ProtocolHandler(this.adminManager);
         try {
             this.server = new AdministrationServer(null, 24500, 5);
             this.shutdownManager.addDisposable(this.server);
            this.shutdownManager.addDisposable(this.adminManager);
             this.server.addObjectReceivedListener(this.protocolHandler);
             this.server.addConnectionListener(this.protocolHandler);
             this.server.listen();
         } catch (IOException e) {
             throw new SetupException(e);
         }
     }
     
     
 
     private void initKeyStore(String keyStore, String password) throws SetupException {
         File keyFile = new File(keyStore);
         
         if (!keyFile.exists()) {
             throw new SetupException("file not found: " + keyFile);
         }
         
         System.setProperty("javax.net.ssl.keyStore", keyStore);
         System.setProperty("javax.net.ssl.keyStorePassword", password);
     }
 }
