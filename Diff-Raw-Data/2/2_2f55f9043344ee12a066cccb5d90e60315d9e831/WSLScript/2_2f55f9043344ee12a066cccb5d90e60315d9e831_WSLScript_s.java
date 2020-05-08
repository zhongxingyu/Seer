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
 package cc.warlock.core.stormfront.script.wsl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Stack;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import cc.warlock.core.client.settings.IVariable;
 import cc.warlock.core.client.settings.internal.ClientSettings;
 import cc.warlock.core.script.AbstractScript;
 import cc.warlock.core.script.IMatch;
 import cc.warlock.core.script.IScriptCommands;
 import cc.warlock.core.script.IScriptEngine;
 import cc.warlock.core.script.IScriptInfo;
 import cc.warlock.core.script.configuration.ScriptConfiguration;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;
 import cc.warlock.core.stormfront.script.internal.StormFrontScriptCommands;
 
 public class WSLScript extends AbstractScript {
 	
 	private boolean debugging = false;
 	private int debugLevel = 1;
 	protected double delay = 0.0;
 	private HashMap<String, Integer> labels = new HashMap<String, Integer>();
 	private int nextLine = 0;
 	private WSLAbstractCommand curCommand;
 	private String curLine;
 	private HashMap<String, IWSLValue> specialVariables = new HashMap<String, IWSLValue>();
 	private HashMap<String, IWSLValue> localVariables = new HashMap<String, IWSLValue>();
 	private Stack<WSLFrame> callstack = new Stack<WSLFrame>();
 	
 	private Thread scriptThread;
 	private Pattern commandPattern = Pattern.compile("^([\\w]+)(\\s+(.*))?");
 
 	private boolean lastCondition = false;
 	private ArrayList<WSLAbstractCommand> commands = new ArrayList<WSLAbstractCommand>();
 	private ArrayList<WSLMatch> matches = new ArrayList<WSLMatch>();
 	private BlockingQueue<String> matchQueue;
 	
 	protected WSLEngine engine;
 	protected IStormFrontScriptCommands scriptCommands;
 	protected IStormFrontClient sfClient;
 	
 	public WSLScript (WSLEngine engine, IScriptInfo info, IStormFrontClient client)
 	{
 		super(info, client);
 		this.engine = engine;
 		this.sfClient = client;
 		
 		this.debugging = !ScriptConfiguration.instance().getSupressExceptions().get();
 		
 		scriptCommands = new StormFrontScriptCommands(client, this);
 		
 		setSpecialVariable("rt", new WSLRoundTime());
 		setSpecialVariable("monstercount", new WSLMonsterCount());
 		setSpecialVariable("lhand", new WSLLeftHand());
 		setSpecialVariable("rhand", new WSLRightHand());
 		setSpecialVariable("spell", new WSLSpell());
 		setSpecialVariable("roomdesc", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_DESCRIPTION));
 		setSpecialVariable("roomexits", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_EXITS));
 		setSpecialVariable("roomplayers", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_PLAYERS));
 		setSpecialVariable("roomobjects", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_OBJECTS));
 		setSpecialVariable("roomtitle", new WSLRoomTitle());
 		setSpecialVariable("lastcommand", new WSLLastCommand());
 	}
 
 	public IWSLValue getVariable(String name) {
 		// these values are maintained by the script
 		IWSLValue val = specialVariables.get(name);
 		if (val != null)
 			return val;
 		
 		// return value from settings. All user global variables are stored here
 		IVariable var = sfClient.getClientSettings().getVariable(name);
 		if (var != null)
 			return new WSLString(var.getValue());
 		
 		return null;
 	}
 	
 	public boolean variableExists(String name) {
 		return specialVariables.containsKey(name) || sfClient.getClientSettings().getVariable(name) != null;
 	}
 	
 	public boolean localVariableExists(String name) {
 		return localVariables.containsKey(name);
 	}
 	
 	public IWSLValue getLocalVariable(String name) {
 		return localVariables.get(name);
 	}
 	
 	private class WSLFrame {
 		private int line;
 		private HashMap<String, IWSLValue> localVariables;
 		
 		public WSLFrame(int line, HashMap<String, IWSLValue> variables) {
 			this.line = line;
 			this.localVariables = variables;
 		}
 
 		public void restore() {
 			WSLScript.this.localVariables = localVariables;
 			curCommand = commands.get(line);
 			nextLine = line;
 			while(curCommand == null) {
 				nextLine++;
 				if(nextLine >= commands.size())
 					break;
 				curCommand = commands.get(nextLine);
 			}
 		}
 	}
 	
 	private class WSLRoundTime extends WSLAbstractNumber {
 		public double toDouble() {
 			return sfClient.getRoundtime().get();
 		}
 	}
 	
 	private class WSLMonsterCount extends WSLAbstractNumber {
 		public double toDouble() {
 			return sfClient.getMonsterCount().get();
 		}
 	}
 	
 	private class WSLLeftHand extends WSLAbstractString {
 		public String toString() {
 			return sfClient.getLeftHand().get();
 		}
 	}
 	
 	private class WSLRightHand extends WSLAbstractString {
 		public String toString() {
 			return sfClient.getRightHand().get();
 		}
 	}
 	
 	private class WSLSpell extends WSLAbstractString {
 		public String toString() {
 			return sfClient.getCurrentSpell().get();
 		}
 	}
 	
 	private class WSLRoomTitle extends WSLAbstractString {
 		public String toString() {
 			return sfClient.getStream("room").getFullTitle();
 		}
 	}
 	
 	private class WSLComponent extends WSLAbstractString {
 		protected String componentName;
 		public WSLComponent(String componentName) {
 			this.componentName = componentName;
 		}
 		
 		public String toString () {
 			return sfClient.getComponent(componentName).get();
 		}
 	}
 	
 	private class WSLLastCommand extends WSLAbstractString {
 		public String toString() {
 			return scriptCommands.getLastCommand();
 		}
 	}
 	
 	private class ScriptRunner  implements Runnable {
 		public void run() {
 			scriptCommands.addThread(Thread.currentThread());
 			try {
 				Reader scriptReader = info.openReader();
 				
 				CharStream input = new ANTLRNoCaseReaderStream(scriptReader);
 				WSLLexer lex = new WSLLexer(input);
 				CommonTokenStream tokens = new CommonTokenStream(lex);
 				WSLParser parser = new WSLParser(tokens);
 
 				parser.setScript(WSLScript.this);
 
 				parser.script();
 			} catch(IOException e) {
 				e.printStackTrace();
 				return;
 			} catch (RecognitionException e) {
 				e.printStackTrace();
 				return;
 			}
 			
 			curCommand = commands.get(0);
 			while(curCommand == null) {
 				nextLine++;
 				if(nextLine >= commands.size())
 					break;
 				curCommand = commands.get(nextLine);
 			}
 			
 			while(isRunning()) {
 				if(curCommand == null)
 					break;
 				// find the next non-null command
 				do {
 					nextLine++;
 					if(nextLine >= commands.size())
 						break;
 				} while (commands.get(nextLine) == null);
 				
 				// crazy dance to make sure we're not suspended and not in a roundtime
 				try {
 					if(!curCommand.isInstant())
 						scriptCommands.waitForRoundtime(delay);
 					while(scriptCommands.isSuspended()) {
 						scriptCommands.waitForResume();
 						if(!curCommand.isInstant())
 							scriptCommands.waitForRoundtime(delay);
 					}
 				} catch(InterruptedException e) {
 					
 				} finally {
 					if(!isRunning())
 						break;
 				}
 				
 				try {
 					curCommand.execute();
 				} catch(InterruptedException e) {
 					if(!isRunning())
 						break;
 				}
 				
 				if(nextLine >= commands.size())
 					break;
 				
 				curCommand = commands.get(nextLine);
 			}
 			
 			if(isRunning())
 				stop();
 		}
 	}
 	
 	public void start (Collection<String> arguments)
 	{
 		super.start();
 		
 		StringBuffer totalArgs = new StringBuffer();
 		int i = 1;
 		for (String argument : arguments) {
 			setSpecialVariable(Integer.toString(i), argument);
 			if (i > 1)
 				totalArgs.append(" ");
 			totalArgs.append(argument);
 			i++;
 		}
 		// populate the rest of the argument variable
 		for(; i <= 9; i++) {
 			setSpecialVariable(Integer.toString(i), "");
 		}
 		// set 0 to the entire list
 		setSpecialVariable("0", totalArgs.toString());
 		
 		scriptThread = new Thread(new ScriptRunner());
 		scriptThread.setName("Wizard Script: " + getName());
 		scriptThread.start();
 	}
 	
 	public void addLabel(String label, Integer line) {
 		labels.put(label.toLowerCase(), line);
 	}
 	
 	public int labelLineNumber(String label) {
 		Integer line = labels.get(label.toLowerCase());
 		if(line != null)
 			return line;
 		else
 			return -1;
 	}
 	
 	protected void addCommand(WSLAbstractCommand command) {
 		commands.add(command);
 	}
 	
 	protected void execute(String line) throws InterruptedException {
 		curLine = line;
 		Matcher m = commandPattern.matcher(line.trim());
 		
 		if (!m.find()) {
 			return;
 		}
 		
 		String commandName = m.group(1).toLowerCase();
 		String arguments = m.group(3);
 		if(arguments == null) arguments = "";
 		
 		IWSLCommandDefinition command = WSLScriptCommands.getCommand(commandName);
 		if(command != null) {
 			scriptDebug(2, "Debug: " + line);
 			command.execute(this, arguments);
 		} else {
 			//TODO output the line number here
 			scriptCommands.echo("Invalid command \"" + line + "\"");
 		}
 	}
 	
 	protected void scriptError(String message) {
 		echo("Script error on line " + curCommand.getLineNumber() + " (" + curLine + "): " + message);
 		stop();
 	}
 	
 	protected void scriptWarning(String message) {
 		echo("Script warning on line " + curCommand.getLineNumber() + " (" + curLine + "): " + message);
 	}
 	
 	protected void scriptDebug (int level, String message)
 	{
 		if (level <= debugLevel && debugging) {
 			echo(message);
 		}
 	}
 	
 	
 	protected void setGlobalVariable(String name, IWSLValue value) {
 		setGlobalVariable(name, value.toString());
 	}
 	
 	protected void setGlobalVariable(String name, String value) {
 		if(specialVariables.containsValue(name))
 			scriptError("Cannot overwrite special variable \"" + name + "\"");
 		((ClientSettings)sfClient.getClientSettings()).getVariableConfigurationProvider().addVariable(name, value);
 	}
 	
 	protected void setSpecialVariable(String name, String value) {
 		setSpecialVariable(name, new WSLString(value));
 	}
 	
 	protected void setSpecialVariable(String name, IWSLValue value) {
 		deleteVariable(name);
 		specialVariables.put(name, value);
 	}
 	
 	protected void deleteVariable(String name) {
 		((ClientSettings)sfClient.getClientSettings()).getVariableConfigurationProvider().removeVariable(name);
 	}
 	
 	protected void deleteLocalVariable(String name) {
 		localVariables.remove(name);
 	}
 	
 	public void setLocalVariable(String name, String value) {
 		setLocalVariable(name, new WSLString(value));
 	}
 	
 	public void setLocalVariable(String name, IWSLValue value) {
 		localVariables.put(name, value);
 	}
 	
 	protected void setDebug(boolean onoff) {
 		this.debugging = onoff;
 	}
 	
 	protected void setDebugLevel(int level) {
 		this.debugLevel = level;
 	}
 	
 	protected void setDelay(double delay) {
 		this.delay = delay;
 	}
 	
 	protected void matchWait(double timeout) throws InterruptedException {
 		/*
 		 * Remove matchQueue before going into the wait rather than
 		 * after coming out so we don't end up trashing another's queue
 		 * as we come out.
 		 */
 		BlockingQueue<String> myQueue = matchQueue;
 		matchQueue = null;
 		
 		try {
 			boolean haveTimeout = timeout > 0.0;
 			long timeoutEnd = 0L;
 			if(haveTimeout)
 				timeoutEnd = System.currentTimeMillis() + (long)(timeout * 1000.0);
 			
 			// run until we get a match or are told to stop
 			while(true) {
 				String text = null;
 				// wait for some text
 				if(haveTimeout) {
 					long now = System.currentTimeMillis();
 					if(timeoutEnd >= now)
 						text = myQueue.poll(timeoutEnd - now, TimeUnit.MILLISECONDS);
 					if(text == null)
 						return;
 				} else {
 					text = myQueue.take();
 				}
 				// try all of our matches
 				for(WSLMatch match : matches) {
 					if(match.getMatch().matches(text)) {
 						match.run();
 						return;
 					}
 				}
 			}
 		} finally {
 			matches.clear();
 			scriptCommands.removeLineQueue(myQueue);
 		}
 	}
 	
 	protected void gotoCommand(int line) {
 		curCommand = commands.get(line);
 		nextLine = line;
 		while(curCommand == null) {
 			nextLine++;
 			if(nextLine >= commands.size())
 				break;
 			curCommand = commands.get(nextLine);
 		}
 		
 		// if we're in an action, interrupt execution on the main thread
 		if(Thread.currentThread() != scriptThread) {
 			scriptCommands.interrupt();
 		}
 	}
 	
 	protected void gotoLabel (String label)
 	{
 		// remove ":" from labels
 		int pos = label.indexOf(':');
 		if(pos >= 0)
 			label = label.substring(0, pos);
 		
 		Integer command = labels.get(label.toLowerCase());
 		
 		if (command != null) {
 			gotoCommand(command);
 		} else {
 			command = labels.get("labelerror");
 			if (command != null)
 			{
 				scriptDebug(1, "Label \"" + label + "\" does not exist, going to \"labelerror\"");
 				gotoCommand(command);
 			}
 			else
 			{
 				scriptError("Label \"" + label + "\" does not exist");
 			}
 		}
 	}
 	
 	private static final Pattern gosubArgRegex =
 		Pattern.compile("(?:(['\"])(.*?)(?<!\\\\)(?>\\\\\\\\)*\\1|([^\\s]+))");
 	protected void gosub (String label, String arguments)
 	{
 		WSLFrame frame = new WSLFrame(nextLine, localVariables);
 		callstack.push(frame);
 
 		// TODO perhaps abstract this
 		localVariables = (HashMap<String, IWSLValue>)localVariables.clone();
 		setLocalVariable("0", arguments);
 
 		// parse the args, splitting on " and ', and leaving in \-escaped quotes
 		Matcher m = gosubArgRegex.matcher(arguments);
 		ArrayList<String> matchList = new ArrayList<String>();
 		while (m.find()) {
 			String phrase;
 			if (m.group(2) != null) {
 				// Add quoted string without the quotes
 				phrase = m.group(2);
 			} else {
 				// Add unquoted word
 				phrase = m.group();
 			}
 			
			phrase = phrase.replaceAll("\\(['\"])", "\\1");
 			matchList.add(phrase);
 		}
 		
 		int i = 1;
 		for(String arg : matchList) {
 			setLocalVariable(String.valueOf(i + 1), arg);
 			i++;
 		}
 
 		Integer command = labels.get(label.toLowerCase());
 
 		if (command != null)
 		{
 			gotoCommand(command);
 		} else {
 			scriptError("Invalid gosub statement, label \"" + label + "\" does not exist");
 		}
 	}
 	
 	protected void gosubReturn () {
 		if (callstack.empty()) {
 			scriptError("Invalid use of return, not in a subroutine");
 		} else {
 			WSLFrame frame = callstack.pop();
 			frame.restore();
 		}
 	}
 	
 	protected void addMatch(String label, IMatch match) {
 		if(matchQueue == null)
 			matchQueue = scriptCommands.createLineQueue();
 		
 		matches.add(new WSLTextMatch(label, match));
 	}
 	
 	protected void addMatchRe(String label, RegexMatch match) {
 		if(matchQueue == null)
 			matchQueue = scriptCommands.createLineQueue();
 	
 		matches.add(new WSLRegexMatch(label, match));
 	}
 	
 	private abstract class WSLMatch {
 		private IMatch match;
 		
 		public WSLMatch(IMatch match) {
 			this.match = match;
 		}
 		
 		public IMatch getMatch() {
 			return match;
 		}
 		
 		public abstract void run();
 	}
 
 	private class WSLTextMatch extends WSLMatch {
 		
 		private String label;
 		
 		public WSLTextMatch(String label, IMatch match) {
 			super(match);
 			this.label = label;
 		}
 		
 		public void run() {
 			gotoLabel(label);
 		}
 	}
 	
 	private class WSLRegexMatch extends WSLMatch {
 		
 		private String label;
 		private RegexMatch match;
 		
 		public WSLRegexMatch(String label, RegexMatch match) {
 			super(match);
 			this.label = label;
 			this.match = match;
 		}
 		
 		public void run() {
 			setVariablesFromMatch(match);
 			gotoLabel(label);
 		}
 	}
 	
 	protected void setVariablesFromMatch(RegexMatch match) {
 		int i = 0;
 		for(String var : match.groups()) {
 			setLocalVariable(String.valueOf(i), var);
 			i++;
 		}
 	}
 
 	public void setLastCondition(boolean condition) {
 		this.lastCondition = condition;
 	}
 	
 	public boolean getLastCondition() {
 		return lastCondition;
 	}
 	
 	public IScriptEngine getScriptEngine() {
 		return engine;
 	}
 	
 	public IScriptCommands getCommands() {
 		return scriptCommands;
 	}
 	
 	protected void echo(String message) {
 		super.echo(message);
 	}
 }
