 package org.eclipse.dltk.ruby.debug.tests.launching;
 
 import junit.framework.Test;
 
 import org.eclipse.dltk.ruby.debug.RubyDebugConstants;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 
 public class RubyFastDebuggerTests extends RubyLaunchingTests {
 
 	public RubyFastDebuggerTests(String name) {
 		super(name);
 	}
 	
 	public static Test suite() {
 		return new Suite(RubyFastDebuggerTests.class);
 	}
 	
 	public void testDebug() throws Exception {
 		RubyDebugPlugin.getDefault().getPluginPreferences().setValue(
 				RubyDebugConstants.DEBUGGING_ENGINE_ID_KEY,
 				"org.eclipse.dltk.ruby.fastdebugger");
 
		super.testDebug();
 	}
 
 	protected String[] getRequiredInterpreterNames() {
 		String[] required = { "ruby"  };
 		return required;
 	}
 
 	public void testRun() throws Exception {
 		// Nothing to do
 	}
 }
