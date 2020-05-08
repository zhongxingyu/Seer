 package fi.aspluma.hookjar.java;
 
 import java.util.Map;
 
 import fi.aspluma.hookjar.ServiceProxy;
 import fi.aspluma.hookjar.ServiceProxyFactory;
 
 public class JavaServiceProxyFactory implements ServiceProxyFactory {
 
  @Override
  public ServiceProxy createServiceProxy(String serviceName, Map<String, String> config, Map<?, ?> eventData) {
     try {
       Class<?> c = Class.forName(serviceName);
       JavaServiceProxy p = (JavaServiceProxy) c.newInstance();
       p.setConfiguration(config);
      p.setEventData(eventData);
       return p;
     } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
       throw new RuntimeException("failed to initialize java service proxy", e);
     }
   }
 
 }
