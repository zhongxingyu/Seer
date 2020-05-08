 package fi.jawsy.jawwa.zk.rabbitmq;
 
 import lombok.val;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.zkoss.lang.Library;
 import org.zkoss.zk.ui.WebApp;
 import org.zkoss.zk.ui.util.WebAppCleanup;
 import org.zkoss.zk.ui.util.WebAppInit;
 
 import com.rabbitmq.client.ConnectionFactory;
 
 import fi.jawsy.jawwa.lang.Option;
 
 public class RabbitBridgesInit implements WebAppInit, WebAppCleanup {
 
     private final Logger log = LoggerFactory.getLogger(this.getClass());
 
     public static final int DEFAULT_RECONNECT_DELAY = 5000;
     public static final int DEFAULT_RECONNECT_TIMEOUT = 30000;
 
     @Override
     public void cleanup(WebApp wapp) throws Exception {
         val manager = (RabbitBridgeManager) wapp.removeAttribute(RabbitBridges.ATTR_NAME);
         if (manager != null) {
             manager.shutdown();
             log.info("RabbitMQ bridge shut down");
         }
     }
 
     private Option<Integer> libraryInt(String name) {
         for (val valueString : Option.option(Library.getProperty(this.getClass() + "." + name))) {
             return Option.some(Integer.parseInt(valueString));
         }
         return Option.none();
     }
 
     private Option<String> libraryString(String name) {
         return Option.option(Library.getProperty(this.getClass() + "." + name));
     }
 
     @Override
     public void init(WebApp wapp) throws Exception {
         if (wapp.hasAttribute(RabbitBridges.ATTR_NAME))
             return;
 
         val connectionFactory = new ConnectionFactory();
 
        for (val value : libraryInt("connectionTimeout")) {
            connectionFactory.setConnectionTimeout(value);
        }
         for (val value : libraryString("host")) {
             connectionFactory.setHost(value);
         }
         for (val value : libraryInt("port")) {
             connectionFactory.setPort(value);
         }
        for (val value : libraryString("username")) {
            connectionFactory.setUsername(value);
        }
         for (val value : libraryString("password")) {
             connectionFactory.setPassword(value);
         }
         for (val value : libraryString("virtualHost")) {
             connectionFactory.setVirtualHost(value);
         }
         for (val value : libraryString("uri")) {
             connectionFactory.setUri(value);
         }
 
         val manager = new RabbitBridgeManager(connectionFactory, getSerializer(), wapp, libraryInt("reconnectDelay").getOrElse(DEFAULT_RECONNECT_DELAY),
                 libraryInt("reconnectTimeout").getOrElse(DEFAULT_RECONNECT_TIMEOUT));
         wapp.setAttribute(RabbitBridges.ATTR_NAME, manager);
         log.info("RabbitMQ bridge initialized");
     }
 
     protected RabbitBridgeSerializer getSerializer() {
         return new RabbitBridgeSerializer.Default();
     }
 
 }
