 package fi.aspluma.hookjar.config;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import fi.aspluma.hookjar.DefaultHandler;
 import fi.aspluma.hookjar.HandlerChain;
 import fi.aspluma.hookjar.HandlerType;
 import fi.aspluma.hookjar.ProxyInitializer;
 import fi.aspluma.hookjar.ServiceProxyFactory;
 import fi.aspluma.hookjar.java.JavaServiceProxyFactory;
 import fi.aspluma.hookjar.ruby.RubyProxyInitializer;
 import fi.aspluma.hookjar.ruby.RubyServiceProxyFactory;
 
 /**
  * Static Java based implementation of the Configuration interface.
  * 
  * @author aspluma
  */
 public class StaticJavaConfiguration implements Configuration {
 
   @Override
   public Map<String, HandlerChain> getConfiguredHandlerChains() {
     Map<String, HandlerChain> chains = new HashMap<String, HandlerChain>();
     
     HandlerChain hc1 = new HandlerChain("/foo");
 
     @SuppressWarnings("unused")
 		// you can execute Ruby expressions for initializing the service.
 		// pass initializer to Handler constructor.
     ProxyInitializer i = new RubyProxyInitializer(
    		//String.format("#{svc}.email_config['address'] = '%s'", "127.0.0.1")
     		""
     );
     DefaultHandler h = new DefaultHandler(HandlerType.RUBY, "Service::CommitMsgChecker");
     h.addParameter("message_format", "'\\[#WEB-\\d{1,5} status:\\d+ resolution:\\d+\\] .*$'");
     h.addParameter("recipients", "a@b.fi, c@d.fi");
     h.addParameter("subject", "foobar");
     hc1.addHandler(h);
     
     h = new DefaultHandler(HandlerType.RUBY, "Service::Jira");
     h.addParameter("server_url", "http://127.0.0.1:5050/foobar");
     h.addParameter("api_version", "123");
     h.addParameter("username", "myuser");
     h.addParameter("password", "mypwd");
     hc1.addHandler(h);
     
     h = new DefaultHandler(HandlerType.JAVA, "fi.aspluma.hookjar.java.services.DemoLogService");
     h.addParameter("param1", "val1");
     hc1.addHandler(h);
 
     chains.put(hc1.getUrl(), hc1);
     
 //    h = new Handler(HandlerType.RUBY, "Service::Email");
 //    h.addParameter("address", "a@b.fi c@d.fi");
 //    hc1.addHandler(h);
     
     // TODO: add other handlers
     
     // TODO: add other chains
     
     return chains;
   }
 
 	@Override
   public Map<HandlerType, ServiceProxyFactory> getServiceProxyFactories() {
 		Map<HandlerType, ServiceProxyFactory> f = new HashMap<HandlerType, ServiceProxyFactory>();
 		f.put(HandlerType.RUBY, new RubyServiceProxyFactory());
 		f.put(HandlerType.JAVA, new JavaServiceProxyFactory());
 		return f;
 	}
   
 }
