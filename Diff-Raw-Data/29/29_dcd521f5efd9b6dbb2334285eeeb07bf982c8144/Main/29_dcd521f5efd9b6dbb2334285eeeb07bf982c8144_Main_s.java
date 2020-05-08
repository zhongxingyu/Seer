 package com.kit.lightserver;
 
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import com.test.KitTransactionServerHttpMain;

 import org.dajo.framework.configuration.ConfigAccessor;
 import org.dajo.framework.configuration.ConfigurationReader;
 
 public final class Main {
 
     static private final Logger LOGGER = LoggerFactory.getLogger(Main.class);
 
     public static void main(final String[] args) {
 
         KitTransactionServerGui server = null;
        KitTransactionServerHttpMain httpsServer = null;
         try {
 
             ConfigAccessor configAcessor = ConfigurationReader.loadExternalProperties(KitPropertiesFiles.SERVER_PROPERTIES, KitPropertiesFiles.DATABASE_PROPERTIES);
 
             server = new KitTransactionServerGui(configAcessor);
 
            httpsServer = new KitTransactionServerHttpMain(configAcessor, "res/kittransactionalserver-dev-keystore.jks", "Kit45$buZZzL#");

            server.addShutdownHandler(new HttpServerShutdownHandler(httpsServer));
 
             server.start();
            httpsServer.start();
 
             LOGGER.info("Init finished.");
 
         } catch (Throwable t) {
             JOptionPane.showMessageDialog(null, "Um erro inexperado occoreu, por favor consulte os arquivos de log. (e="+t.getMessage()+")");
             if( server != null ) {
                 server.stopServer(true);
             }
            if( httpsServer != null ) {
                httpsServer.stop();
            }
         }
 
     }
 
    static final class HttpServerShutdownHandler implements ShutdownHandler {
        private final KitTransactionServerHttpMain server;
        public HttpServerShutdownHandler(final KitTransactionServerHttpMain server) {
            this.server = server;
        }
        @Override
        public void onShutdown() {
            server.stop();
        }
    }
 
 }// class
