 package org.techhub.techq.ruby;
 
 import java.io.StringWriter;
 
import org.jruby.embed.ParseFailedException;
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
		ruby.setErrorWriter(writer);
 		initialized = true;
 	}
 	
 	
 	@Override
 	public String runScript(String script) {
 		synchronized (lock) {
 			if(!initialized){
 				init();
 			}
 		}
		String sysout = "";
		try{
			ruby.runScriptlet(script);
			writer.flush();
			sysout = writer.toString();
		}catch(ParseFailedException e){
			sysout = e.getMessage();
		}
 		return sysout;
 	}
 
 }
