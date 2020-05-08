 package org.techhub.techq.ruby;
 
 import java.io.StringWriter;
 
 import org.jruby.embed.ScriptingContainer;
 import org.techhub.techq.EvaluationContainer;
 
 /**
  * 
  * @author tango
  * 
  */
 public class RubyEvaluationContainer implements EvaluationContainer {
 
 	private Object lock = new Object();
 	
 	private boolean initialized = false;
 	
 	private ScriptingContainer ruby;
 	
 	private StringWriter writer;
 	
 	private synchronized void init(){
 		ruby = new ScriptingContainer();
 		writer = new StringWriter();
 		ruby.setWriter(writer);
 		initialized = true;
 	}
 	
 	
 	@Override
 	public String runScript(String script) {
 		synchronized (lock) {
 			if(!initialized){
 				init();
 			}
 		}
		ruby.runScriptlet(script);
		writer.flush();
		String sysout = writer.toString();
		
 		return sysout;
 	}
 
 }
