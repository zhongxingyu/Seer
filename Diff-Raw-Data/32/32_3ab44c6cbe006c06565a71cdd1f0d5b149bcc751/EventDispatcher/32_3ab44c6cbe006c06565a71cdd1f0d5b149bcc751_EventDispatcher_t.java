 package fi.aspluma.hookjar;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 
 import fi.aspluma.hookjar.config.StaticJavaConfiguration;
 import fi.aspluma.hookjar.java.JavaServiceProxyFactory;
 import fi.aspluma.hookjar.ruby.RubyServiceProxyFactory;
 
 public class EventDispatcher {
   private static final Logger logger = LoggerFactory.getLogger(EventDispatcher.class);
 
   private Map<String, HandlerChain> chains = new HashMap<String, HandlerChain>();
   private Map<HandlerType, ServiceProxyFactory> factories;
   
   public EventDispatcher() throws IOException {
     initialize();
   }
 
   public void dispatch(String requestURI, byte[] eventData) {
     Map<?, ?> data = new Gson().fromJson(new String(eventData), Map.class);
     logger.debug("parsing input data with Gson lib: "+data);
     
     HandlerChain chain = chains.get(requestURI);  // NB: exact match
     for(Handler h : chain.getHandlers()) {
       logger.debug("invoking handler: "+h);
       ServiceProxyFactory sf = factories.get(h.getType());
      ServiceProxy srv = sf.createServiceProxy(h.getClassName(), h.getParameters(), eventData, data);
       srv.configure();
       srv.processRequest();
     }
   }
   
   private void initialize() throws IOException {
 	  factories = getServiceProxyFactories();
 	  chains = new StaticJavaConfiguration().getConfiguredHandlerChains();
   }
   
   private Map<HandlerType, ServiceProxyFactory> getServiceProxyFactories() throws IOException {
     String rubyHome = System.getProperty("ghj.ruby.home");
     String githubServicesHome = System.getProperty("ghj.github-services.home");
     
     if(rubyHome == null || githubServicesHome == null) {
       throw new RuntimeException("ghj.ruby.home and ghj.github-services.home system properties must be set");
     }
     
     Map<HandlerType, ServiceProxyFactory> f = new HashMap<HandlerType, ServiceProxyFactory>();
     f.put(HandlerType.RUBY, new RubyServiceProxyFactory(rubyHome, githubServicesHome));
     f.put(HandlerType.JAVA, new JavaServiceProxyFactory());
     return f;
   }
   
 
 }
