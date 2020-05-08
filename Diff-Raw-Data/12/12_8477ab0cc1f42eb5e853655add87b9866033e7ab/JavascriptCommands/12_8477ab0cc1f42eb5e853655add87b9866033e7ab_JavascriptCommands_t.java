 /**
  * Warlock, the open-source cross-platform game client
  *  
  * Copyright 2008, Warlock LLC, and individual contributors as indicated
  * by the @authors tag. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package cc.warlock.core.script.javascript;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Script;
 
 import cc.warlock.core.script.IMatch;
 import cc.warlock.core.script.IScriptCommands;
 import cc.warlock.core.script.IScriptFileInfo;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.script.internal.TextMatch;
 
 public class JavascriptCommands {
 
 	protected IScriptCommands commands;
 	protected JavascriptScript script;
 	protected HashMap<Integer, TimerTask> timeTasks = new HashMap<Integer, TimerTask>();
 	private int nextTimerID = 1;
 	private Timer timer = new Timer();
 	
 	public JavascriptCommands(IScriptCommands commands, JavascriptScript script) {
 		this.commands = commands;
 		this.script = script;
 	}
 	
 	public void echo(String text) {
 		script.checkStop();
 		
 		commands.echo(text);
 	}
 	
 	public void echo() {
 		script.checkStop();
 		
 		commands.echo("");
 	}
 
 	public boolean include (String otherScript)
 	{
 		script.checkStop();
 		
 		if (script.getScriptInfo() instanceof IScriptFileInfo)
 		{
 			IScriptFileInfo info = (IScriptFileInfo) script.getScriptInfo();
 			
 			File scriptFile = new File(otherScript);
 			if (!scriptFile.exists())
 			{
 				scriptFile = new File(info.getScriptFile().getParentFile(), otherScript);
 			}
 			if (scriptFile.exists()) {
 				try {
 					FileReader reader = new FileReader(scriptFile);
 					
 					Script includedScript = 
 						script.getContext().compileReader(reader, scriptFile.getName(), 1, null);
 					
 					includedScript.exec(script.getContext(), script.getScope());
 					
 					return true;
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			} else {
 				commands.echo("ERROR:  Cannot include specified file: " + otherScript);
 			}
 		}
 		return false;
 	}
 
 	public void move(String direction) {
 		script.checkStop();
 		
 		try {
 			commands.move(direction);
 		} catch(InterruptedException e) {
 			script.checkStop();
 		}
 	}
 
 	public void pause(double seconds) {
 		script.checkStop();
 		
 		try {
 			commands.pause(seconds);
 		} catch(InterruptedException e) {
 			script.checkStop();
 		}
 	}
 
 	public void put(String text) {
 		script.checkStop();
 		
 		try {
 			commands.put(text);
 		} catch(InterruptedException e) {
 			script.checkStop();
 		}
 	}
 	
 	public void waitFor(String string)
 	{
 		script.checkStop();
 		
 		waitFor(new TextMatch(string, true));
 	}
 	
 	// Default to case sensitivity
 	public void waitForRe(String string) {
 		script.checkStop();
 		
 		waitForRe(string, false);
 	}
 	
 	public void waitForRe(String string, Boolean ignoreCase)
 	{
 		script.checkStop();
 		
 		waitFor(new RegexMatch(string, ignoreCase));
 	}
 
 	public void waitFor(IMatch match) {
 		script.checkStop();
 		
 		try {
 			commands.waitFor(match);
 		} catch(InterruptedException e) {
 			script.checkStop();
 		}
 	}
 
 	public void waitForPrompt() {
 		script.checkStop();
 		
 		try {
 			commands.waitForPrompt();
 		} catch(InterruptedException e) {
 			script.checkStop();
 		}
 	}
 
 	public void exit() throws Error {
 		commands.stop();
 		
 		throw new Error();
 	}
 	
 	public IScriptCommands getScriptCommands ()
 	{
 		script.checkStop();
 		
 		return commands;
 	}
 	
 	public JavascriptScript getScript ()
 	{
 		return script;
 	}
 	
 	private class CommandCallback extends TimerTask {
 		private String command;
 		
 		public CommandCallback(String command) {
 			this.command = command;
 		}
 		
 		public void run() {
 			Context.enter();
 			try {
				script.checkStop();
				
 				Script jsCommand = script.getContext().compileString(command, "callback", 0, null);
 
 				jsCommand.exec(script.getContext(), script.getScope());
			} catch(EcmaError e) {
				commands.echo(e.getMessage());
				this.cancel();
			} catch(StopException e) {
				this.cancel();
 			} catch(Exception e) {
				commands.echo(e.getMessage());
 				e.printStackTrace();
				this.cancel();
 			} finally {
 				Context.exit();
 			}
 		}
 	}
 	
 	public int setInterval(String command, double interval) {
 		script.checkStop();
 		
 		int id = nextTimerID++;
 		
 		CommandCallback c = new CommandCallback(command);
 		timeTasks.put(id, c);
 		timer.scheduleAtFixedRate(c, (long)(interval * 1000.0), (long)(interval * 1000.0));
 		
 		return id;
 	}
 	
 	public int setTimeout(String command, double timeout) {
 		script.checkStop();
 		
 		int id = nextTimerID++;
 		
 		CommandCallback c = new CommandCallback(command);
 		timeTasks.put(id, c);
 		timer.schedule(c, (long)(timeout * 1000.0));
 		
 		return id;
 	}
 	
 	public void playSound (String filename) {
 		File file = new File(filename);
 		if (file.exists()) {
 			try {
 				commands.playSound(new FileInputStream(file));
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 }
