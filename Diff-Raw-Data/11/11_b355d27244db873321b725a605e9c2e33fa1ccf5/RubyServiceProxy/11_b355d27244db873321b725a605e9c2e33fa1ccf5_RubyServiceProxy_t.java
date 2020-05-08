 package fi.aspluma.hookjar.ruby;
 
 import org.jruby.embed.ScriptingContainer;
 import org.jruby.runtime.builtin.IRubyObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fi.aspluma.hookjar.ServiceProxy;
 
 /**
  * Ruby based {@link ServiceProxy} implementation base class.
  * 
  * @author aspluma
  */
 public class RubyServiceProxy extends ServiceProxy {
   private static final Logger logger = LoggerFactory.getLogger(RubyServiceProxy.class);
   private ScriptingContainer ruby;
 	private IRubyObject service;
 	
 	RubyServiceProxy(ScriptingContainer ruby, IRubyObject service) {
 	  this.ruby = ruby;
 		this.service = service;
 	}
 	
 	@Override
   public void processRequest() {
     logger.debug("processRequest: "+service);
     ruby.callMethod(service, "receive_push", Object.class);
 	}
 	
 	void runScriptlet(String scriptlet) {
		String vn = String.format("_thread_%d_svc", Thread.currentThread().getId());
		scriptlet = scriptlet.replaceAll("#\\{svc\\}", vn);
		ruby.put(vn, service);
 		ruby.runScriptlet(scriptlet);
 	}
 
 }
