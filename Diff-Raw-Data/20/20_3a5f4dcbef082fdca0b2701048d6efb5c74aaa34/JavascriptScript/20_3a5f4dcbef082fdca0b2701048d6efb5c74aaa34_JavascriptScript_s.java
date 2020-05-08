 /*
  * Created on Mar 26, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package cc.warlock.core.script.javascript;
 
 import java.io.Reader;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 
 import cc.warlock.core.client.IWarlockClient;
 import cc.warlock.core.script.AbstractScript;
 import cc.warlock.core.script.IScriptCommands;
 import cc.warlock.core.script.IScriptEngine;
 import cc.warlock.core.script.IScriptInfo;
 import cc.warlock.core.script.IScriptListener;
 import cc.warlock.core.script.internal.ScriptCommands;
 
 /**
  * @author Marshall
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class JavascriptScript extends AbstractScript {
 
 	private JavascriptEngine engine;
 	private boolean stopped;
 	private Context context;
 	private JavascriptCommands jsCommands;
 	private IScriptCommands commands;
 	IWarlockClient client;
 	
 	public JavascriptScript (JavascriptEngine engine, IScriptInfo info, IWarlockClient client)
 	{
 		super(info);
 		
 		this.engine = engine;
 		this.client = client;
 		this.commands = new ScriptCommands(client, getName());
 		this.jsCommands = new JavascriptCommands(commands, this);
 	}
 
 	public boolean isRunning() {
 		return !stopped;
 	}
 	
 	public void start () {
 		stopped = false;
 		
 		for (IScriptListener listener : listeners) listener.scriptStarted(this);
 	}
 
 	public void stop() {
 		stopped = true;
 		commands.stop();
 		
 		super.stop();
 	}
 	
 	public void suspend() {

 	}
 	
 	public void resume() {
		// TODO Auto-generated method stub
		
 	}
 
 	public JavascriptCommands getCommands() {
 		return jsCommands;
 	}
 	
 	public Scriptable getScope() {
 		return engine.scope;
 	}
 
 	public Context getContext() {
 		return context;
 	}
 
 	public void setContext(Context context) {
 		this.context = context;
 	}
 	
 	public IScriptEngine getScriptEngine() {
 		return engine;
 	}
 	
 	public Reader getReader() {
 		return reader;
 	}
 	
 	public void movedToRoom() {
 		commands.movedToRoom();
 	}
 	
 	public IWarlockClient getClient() {
 		return client;
 	}
 	
 	public void execute(String command) { }
 }
