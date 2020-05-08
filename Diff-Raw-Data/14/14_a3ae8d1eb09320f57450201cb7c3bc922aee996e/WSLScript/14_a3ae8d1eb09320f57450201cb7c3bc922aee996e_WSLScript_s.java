 package cc.warlock.core.stormfront.script.wsl;
 
 import java.io.IOException;
 import java.io.Reader;
import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Stack;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.ANTLRReaderStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import cc.warlock.core.script.AbstractScript;
 import cc.warlock.core.script.IScriptEngine;
 import cc.warlock.core.script.IScriptInfo;
 import cc.warlock.core.script.IScriptListener;
 import cc.warlock.core.script.Match;
 import cc.warlock.core.script.internal.RegexMatch;
 import cc.warlock.core.script.internal.TextMatch;
 import cc.warlock.core.stormfront.client.IStormFrontClient;
 import cc.warlock.core.stormfront.script.IStormFrontScriptCommands;
 import cc.warlock.core.stormfront.script.internal.StormFrontScriptCommands;
 
 public class WSLScript extends AbstractScript {
 	
 	protected boolean running, stopped;
 	protected HashMap<String, WSLScriptLine> labels = new HashMap<String, WSLScriptLine>();
 	protected WSLScriptLine nextLine;
 	protected WSLScriptLine curLine;
 	protected WSLScriptLine endLine;
 	protected HashMap<String, String> variables = new HashMap<String, String>();
 	protected Stack<WSLScriptLine> callstack = new Stack<WSLScriptLine>();
 	protected HashMap<String, WSLCommand> wslCommands = new HashMap<String, WSLCommand>();
 	protected int pauseLine;
 	protected Thread scriptThread;
 	private Pattern commandPattern = Pattern.compile("^([\\w_]+)(\\s+(.*))?");
 	private WSLCommand if_ = new WSLIf_();
 	private ScriptTimer timer = new ScriptTimer();
 	
 	protected WSLEngine engine;
 	protected IStormFrontScriptCommands commands;
 	protected IStormFrontClient client;
 	
 	private final Lock lock = new ReentrantLock();
 	private final Condition gotResume = lock.newCondition();
 	
 	private static final String argSeparator = "\\s+";
 	
 	public WSLScript (WSLEngine engine, IScriptInfo info, IStormFrontClient client)
 	{
 		super(info);
 		this.engine = engine;
 		this.client = client;
 		
 		commands = new StormFrontScriptCommands(client, this);
 		
 		// add command handlers
 		addCommand("put", new WSLPut());
 		addCommand("echo", new WSLEcho());
 		addCommand("pause", new WSLPause());
 		addCommand("shift", new WSLShift());
 		addCommand("save", new WSLSave());
 		addCommand("action", new WSLAction());
 		addCommand("counter", new WSLCounter());
 		addCommand("deletevariable", new WSLDeleteVariable());
 		addCommand("setvariable", new WSLSetVariable());
 		addCommand("goto", new WSLGoto());
 		addCommand("gosub", new WSLGosub());
 		addCommand("random", new WSLRandom());
 		addCommand("return", new WSLReturn());
 		addCommand("matchwait", new WSLMatchWait());
 		addCommand("matchre", new WSLMatchRe());
 		addCommand("match", new WSLMatch());
 		addCommand("waitforre", new WSLWaitForRe());
 		addCommand("waitfor", new WSLWaitFor());
 		addCommand("wait", new WSLWait());
 		addCommand("move", new WSLMove());
 		addCommand("nextroom", new WSLNextRoom());
 		addCommand("exit", new WSLExit());
 		addCommand("timer", new WSLTimer());
 		
 	}
 
 	public IWSLValue getVariable(String name) {
 		if(name.equals("t")) return new WSLNumber(timer.get());
 		if(name.equals("mana")) return new WSLNumber(client.getMana().get());
 		if(name.equals("health")) return new WSLNumber(client.getHealth().get());
 		if(name.equals("fatigue")) return new WSLNumber(client.getFatigue().get());
 		if(name.equals("spirit")) return new WSLNumber(client.getSpirit().get());
 		
 		return new WSLString(variables.get(name));
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 	
 	private class ScriptRunner  implements Runnable {
 		public void run() {
 			doStart();
 			
 			while(curLine != null && !stopped) {
 				checkState();
 				nextLine = curLine.getNext();
 				
 				String line = curLine.get();
 				if(line != null) {
 					execute(line);
 				}
 				
 				curLine = nextLine;
 				commands.clearInterrupt();
 			}
 			
 			if(!stopped)
 				stop();
 		}
 	}
 	
 	public void start (List<String> arguments)
 	{
 		for (int i = 0; i < arguments.size(); i++) {
 			setVariable(Integer.toString(i + 1), arguments.get(i));
 		}
 		
 		for (String varName : commands.getStormFrontClient().getServerSettings().getVariableNames())
 		{
 			setVariable(varName, commands.getStormFrontClient().getServerSettings().getVariable(varName));
 		}
 		
 		scriptThread = new Thread(new ScriptRunner());
 		scriptThread.setName("Wizard Script: " + getName());
 		scriptThread.start();
 		
 		for (IScriptListener listener : listeners) listener.scriptStarted(this);
 	}
 	
 	protected void doStart ()
 	{
 		try {
 			Reader scriptReader = info.openReader();
 			
 			CharStream input = new ANTLRReaderStream(scriptReader);
 			WSLLexer lex = new WSLLexer(input);
 			CommonTokenStream tokens = new CommonTokenStream(lex);
 			WSLParser parser = new WSLParser(tokens);
 
 			parser.setScript(this);
 
 			parser.script();
 		} catch(IOException e) {
 			e.printStackTrace();
 			return;
 		} catch (RecognitionException e) {
 			e.printStackTrace();
 			// TODO handle the exception
 		}
 
 		commands.echo("[script started: " + getName() + "]");
 		running = true;
 		stopped = false;
 	}
 	
 	private void checkState() {
 		while(!running && !stopped) {
 			lock.lock();
 			try {
 				gotResume.await();
 			} catch(Exception e) {
 				e.printStackTrace();
 			} finally {
 				lock.unlock();
 			}
 		}
 	}
 	
 	public void addLabel(String label, WSLScriptLine line) {
 		labels.put(label.toLowerCase(), line);
 	}
 	
 	public void addLine(WSLScriptLine line) {
 		if(curLine == null) {
 			curLine = line;
 		}
 		if(endLine != null) {
 			endLine.setNext(line);
 		}
 		endLine = line;
 	}
 	
 	public void execute(String line) {
 		System.out.print("script line: " + line + "\n");
 		
 		Matcher m = commandPattern.matcher(line);
 		
 		if (!m.find()) {
 			System.out.println("Couldn't find the command");
 			return;
 		}
 		
 		String commandName = m.group(1).toLowerCase();
 		// System.out.print("command \"" + commandName + "\"\n");
 		String arguments = m.group(3);
 		if(arguments == null) arguments = "";
 		// System.out.print("arguments \"" + arguments + "\"\n");
 		
 		WSLCommand command = wslCommands.get(commandName);
 		if(command != null) command.execute(arguments);
 	}
 	
 	public void stop() {
 		running = false;
 		stopped = true;
 		commands.stop();
 		
 		commands.echo("[script stopped: " + getName() + "]");
 		super.stop();
 	}
 
 	public void suspend() {
 		running = false;
 		//pauseLine = nextLine;
 		
 		commands.echo("[script paused: " + getName() + "]");
 		super.suspend();
 	}
 	
 	public void resume() {
 		
 		//nextLine = pauseLine;
 		running = true;
 		
 		commands.echo("[script resumed: " + getName() + "]");
 
 		super.resume();
 		
 		lock.lock();
 		try {
 			gotResume.signalAll();
 		} catch(Exception e) {
 			e.printStackTrace();
 		} finally {
 			lock.unlock();
 		}
 	}
 	
 	protected void addCommand (String name, WSLCommand command) {
 		wslCommands.put(name, command);
 	}
 	
 	protected class ScriptTimer {
 		private long timerStart = -1L;
 		private long timePast = 0L;
 		
 		public long get() {
 			if(timerStart < 0) return timePast / 1000;
 			return (System.currentTimeMillis() - timerStart) / 1000;
 		}
 		
 		public void start() {
 			if(timerStart < 0)
 				timerStart = System.currentTimeMillis() - timePast;
 		}
 		
 		public void stop() {
 			if(timerStart >= 0) {
 				timePast = timerStart - System.currentTimeMillis();
 				timerStart = -1L;
 			}
 		}
 		
 		public void clear() {
 			timerStart = -1L;
 			timePast = 0L;
 		}
 	}
 	
 	abstract protected class WSLCommand {
 		
 		abstract public void execute(String arguments);
 		
 	}
 	
 	protected class WSLSave extends WSLCommand {
 		
 		public void execute(String arguments) {
 			setVariable("s", arguments);
 		}
 	}
 
 	protected class WSLShift extends WSLCommand {
 		
 		public void execute (String arguments) {
 			for (int i = 0; ; i++) {
 				String arg = variables.get(Integer.toString(i + 1));
 				if (arg == null) {
 					deleteVariable(Integer.toString(i));
 					break;
 				}
 				setVariable(Integer.toString(i), arg);
 			}
 		}
 	}
 
 	protected class WSLCounter extends WSLCommand {
 		
 		public void execute (String arguments) {
 			String[] args = arguments.split(argSeparator);
 			if (args.length >= 2)
 			{
 				String counterFunction = args[0];
 				int value = variables.containsKey("c") ? Integer.parseInt(variables.get("c")) : 0;
 
 				if ("set".equalsIgnoreCase(counterFunction))
 				{
 					setVariable("c", args[1]);
 				}
 				else if ("add".equalsIgnoreCase(counterFunction))
 				{	
 					int newValue = value + Integer.parseInt(args[1]);
 					setVariable("c", Integer.toString(newValue));
 				}
 				else if ("subtract".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value - Integer.parseInt(args[1]);
 					setVariable("c", Integer.toString(newValue));
 				}
 				else if ("multiply".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value * Integer.parseInt(args[1]);
 					setVariable("c", Integer.toString(newValue));
 				}
 				else if ("divide".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value / Integer.parseInt(args[1]);
 					setVariable("c", Integer.toString(newValue));
 				}
 				else if ("modulus".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value % Integer.parseInt(args[1]);
 					setVariable("c", Integer.toString(newValue));
 				}
 			} else { /*throw error */ }
 		}
 	}
 
 	protected class WSLDeleteVariable extends WSLCommand {
 		
 		public void execute (String arguments) {
 			String var = arguments.split(argSeparator)[0];
 			deleteVariable(var);
 		}
 	}
 
 	private void setVariable(String name, String value) {
 		variables.put(name, value);
 		String command = "if_" + name;
 		if(!wslCommands.containsKey(command)) {
 			wslCommands.put(command, if_);
 		}
 	}
 	
 	private void deleteVariable(String name) {
 		wslCommands.remove(name);
 		variables.remove(name);
 	}
 	
 	protected class WSLSetVariable extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s+(.*)$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			if (m.find())
 			{
 				// System.out.print("variable: \"" + m.group(1) + "\" value: \"" + m.group(2) + "\"\n");
 				setVariable(m.group(1), m.group(2));
 			} else {
 				// System.out.print("Didn't match \"" + arguments + "\"\n");
 			}
 		}
 	}
 	
 	protected class WSLAction extends WSLCommand {
 		
 		private Pattern clearFormat = Pattern.compile("^clear");
 		private Pattern removeFormat = Pattern.compile("^remove\\s+(.*)$");
 		private Pattern addFormat = Pattern.compile("^(.*)\\s+when\\s+(.*)$");
 		
 		public void execute(String arguments) {
 			Matcher clearMatcher = clearFormat.matcher(arguments);
 			Matcher removeMatcher = removeFormat.matcher(arguments);
 			Matcher addMatcher = addFormat.matcher(arguments);
 			
 			if(clearMatcher.find()) {
 				commands.clearActions();
 			} else if(removeMatcher.find()) {
 				commands.removeAction(removeMatcher.group(1));
 			} else if(addMatcher.find()) {
 				commands.addAction(addMatcher.group(1), addMatcher.group(2));
 			} else {
 				// TODO print some error message about a poorly formed action
 			}
 		}
 	}
 	
 	protected void gotoLine(WSLScriptLine command) {
 		curLine = nextLine = command;
 		
 		// if we're in an action, interrupt execution on the main thread
 		if(Thread.currentThread() != scriptThread) {
 			commands.interrupt();
 		}
 	}
 	
 	protected void gotoLabel (String label)
 	{
 		// System.out.println("going to label: \"" + label + "\"");
 		
 		WSLScriptLine command = labels.get(label.toLowerCase());
 		
 		if (command != null)
 		{
 			// System.out.println("found label");
 			gotoLine(command);
 		}
 		else {
 			// System.out.println("label not found");
 			command = labels.get("labelerror");
 			if (command != null)
 			{
 				gotoLine(command);
 			}
 			else { // TODO: Fix gotoLabel to throw an exception instead of outputting to user
 				commands.echo ("***********");
 				commands.echo ("*** WARNING: Label \"" + label + "\" doesn't exist, skipping goto statement ***");
 				commands.echo ("***********");
 			}
 		}
 	}
 	
 	protected class WSLGoto extends WSLCommand {
 		
 		public void execute (String arguments) {
 			String[] args = arguments.split(argSeparator);
 			if (args.length >= 1)
 			{
 				String label = args[0];
 				gotoLabel(label);
 			} else { /*throw error*/ }
 		}
 	}
 	
 	protected void gosub (String label, String arguments)
 	{
 		String[] args = arguments.split(argSeparator);
 		
 		// TODO save previous state of variables
 		setVariable("$0", arguments);
 		for(int i = 0; i < args.length; i++) {
 			setVariable("$" + (i + 1), args[i]);
 		}
 		
 		callstack.push(nextLine);
 		gotoLabel(label);
 	}
 	
 	protected class WSLGosub extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s*(.*)?$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				System.out.println("calling label " + m.group(1));
 				gosub(m.group(1), m.group(2));
 			} else {
 				System.out.println("label not found");
 				/*throw error*/ 
 			}
 		}
 	}
 	
 	protected void gosubReturn () {
 		if (callstack.empty()) {
 			commands.echo ("***********");
 			commands.echo ("*** WARNING: No outstanding calls were executed, skipping return statement ***");
 			commands.echo ("***********");
 		} else {
 			curLine = nextLine = callstack.pop();
 		}
 	}
 	
 	protected class WSLReturn extends WSLCommand {
 		
 		public void execute (String arguments) {
 			gosubReturn();
 		}
 	}
 
 	protected class WSLMatchWait extends WSLCommand {
 		
 		public void execute (String arguments) {
 			// mode = Mode.waiting;
 			
 			Match match = commands.matchWait();
 			
 			if (match != null)
 			{
 				// System.out.println("matched label: \"" + match.getAttribute("label") + "\"");
 				gotoLabel((String)match.getAttribute("label"));
 				commands.waitForPrompt();
 				commands.waitForRoundtime();
 			} else {
 				if(!stopped)
 					commands.echo("*** Internal error, no match was found!! ***\n");
 			}
 		}
 	}
 
 	protected void addMatch(String label, Match match) {
 		match.setAttribute("label", label);
 		commands.addMatch(match);
 	}
 	
 	protected class WSLMatchRe extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s+/(.*)/(\\w*)");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				String regex = m.group(2);
 				boolean caseInsensitive = m.group(3).contains("i");
 				Match match = new RegexMatch(regex, caseInsensitive);
 				
 				addMatch(m.group(1), match);
 			} else { /* TODO throw error */ }
 		}
 
 	}
 
 	protected class WSLMatch extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s+(.*)$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				Match match = new TextMatch(m.group(2));
 				addMatch(m.group(1), match);
 			} else { /* TODO throw error */ }
 		}
 	}
 
 	protected class WSLWaitForRe extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^/(.*)/(\\w*)");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				String flags = m.group(2);
 				boolean ignoreCase = false;
 				
 				if (flags != null && flags.contains("i"))
 				{
 					ignoreCase = true;
 				}
 				
 				Match match = new RegexMatch(m.group(1), ignoreCase);
 				
 				commands.waitFor(match);
 			} else { /* TODO throw error */ }
 		}
 	}
 	
 	protected class WSLWaitFor extends WSLCommand {
 		
 		public void execute (String arguments) {
 			if (arguments.length() >= 1)
 			{
 				Match match = new TextMatch(arguments);
 				commands.waitFor(match);
 				
 			} else { /* TODO throw error */ }
 		}
 	}
 
 	protected class WSLWait extends WSLCommand {
 		
 		public void execute (String arguments) {
 			commands.waitForPrompt();
 		}
 	}
 	
 	protected class WSLPut extends WSLCommand {
 		
 		public void execute(String arguments) {
 			commands.put(WSLScript.this, arguments);
 		}
 	}
 	
 	protected class WSLEcho extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			commands.echo(WSLScript.this, arguments);
 		}
 	}
 	
 	protected class WSLPause extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			String[] args = arguments.split(argSeparator);
 			int time = 1;
 			
 			if (args.length >= 1)
 			{
 				try {
 					time = Integer.parseInt(args[0]);
 				} catch(NumberFormatException e) {
 					// time = 1;
 				}
 			} else {
 				// "empty" pause.. means wait 1 second
 			}
 			commands.pause(time);
 		}
 	}
 	
 	protected class WSLMove extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			commands.move(arguments);
 		}
 	}
 	
 	protected class WSLNextRoom extends WSLCommand {
 		
 		public void execute (String arguments)
 		{
 			commands.nextRoom();
 		}
 	}
 	
 	protected class WSLExit extends WSLCommand {
 		
 		public void execute (String arguments) {
 			running = false;
 			stopped = true;
 			
 			// TODO figure out if we should make this call here or elsewhere
 			stop();
 		}
 	}
 	
 	protected class WSLIf_ extends WSLCommand {
 		
 		public void execute (String arguments) {
 			WSLScript.this.execute(arguments);
 		}
 	}
 	
 	private class WSLRandom extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^(\\d+)\\s+(\\d+)");
 		
 		public void execute(String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if(m.find()) {
 				int min = Integer.parseInt(m.group(1));
 				int max = Integer.parseInt(m.group(2));
 				int r = min + new Random().nextInt(max - min + 1);
 				
 				setVariable("r", Integer.toString(r));
 			} else {
 				// print an error?
 			}
 		}
 	}
 	
 	private class WSLTimer extends WSLCommand {
 		
 		private Pattern format = Pattern.compile("^(\\w+)");
 		
 		public void execute(String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if(m.find()) {
 				String command = m.group(1);
 				if(command.equals("start"))			timer.start();
 				else if(command.equals("stop"))		timer.stop();
 				else if(command.equals("clear"))	timer.clear();
 				else {
 					// print an error?
 				}
 			} else {
 				// print an error?
 			}
 		}
 	}
 	
 	public void stopScript() {
 		stopped = true;
 	}
 	
 	public IScriptEngine getScriptEngine() {
 		return engine;
 	}
 	
 	public void movedToRoom() {
 		commands.movedToRoom();
 	}
 	
 }
