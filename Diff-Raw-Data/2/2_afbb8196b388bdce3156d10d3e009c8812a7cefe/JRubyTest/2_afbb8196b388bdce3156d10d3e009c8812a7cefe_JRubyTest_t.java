 package de.nofail.jruby;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.apache.bsf.BSFException;
 import org.apache.bsf.BSFManager;
 import org.junit.Test;
 
 
 public class JRubyTest {
 
 	@Test
	public void execute_jruby_with_bsf() throws MalformedURLException, BSFException {
 		// http://www.javaworld.com/javaworld/jw-07-2006/jw-0717-ruby.html
 		// JRuby must be registered in BSF.
 		// jruby.jar and bsf.jar must be on classpath.
 		BSFManager.registerScriptingEngine("ruby", "org.jruby.javasupport.bsf.JRubyEngine", new String[] { "rb" });
 		BSFManager manager = new BSFManager();
 		// Make the variable myUrl available from Ruby.
 		manager.declareBean("myUrl", new URL("http://www/jruby.org"), URL.class);
 		// Note that the Method getDefaultPort is available from Ruby
 		// as getDefaultPort and also as defaultPort.
 		// The following line illustrates the combination of Ruby syntax
 		// and a Java method call.
 		manager.eval("ruby", "(java)", 1, 1, "puts 'hello world'");
 	}
 }
