 package fi.aspluma.hookjar;
 
 import java.util.Map;
 
 public interface ServiceProxyFactory {
 //  HandlerType getHandlerType();
	ServiceProxy createServiceProxy(String serviceName, Map<String, String> config, Map<?, ?> data);
 }
