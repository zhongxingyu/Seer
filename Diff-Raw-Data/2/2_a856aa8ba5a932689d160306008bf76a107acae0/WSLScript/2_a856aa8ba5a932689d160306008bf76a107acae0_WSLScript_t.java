 package cc.warlock.core.stormfront.script.wsl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.Stack;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import cc.warlock.core.client.internal.WarlockStyle;
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
 	protected boolean debugging = false;
 	protected HashMap<String, WSLAbstractCommand> labels = new HashMap<String, WSLAbstractCommand>();
 	protected WSLAbstractCommand nextCommand;
 	protected WSLAbstractCommand curCommand;
 	private String curLine;
 	protected HashMap<String, IWSLValue> globalVariables = new HashMap<String, IWSLValue>();
 	protected HashMap<String, IWSLValue> localVariables = new HashMap<String, IWSLValue>();
 	protected Stack<WSLFrame> callstack = new Stack<WSLFrame>();
 	protected HashMap<String, WSLCommandDefinition> wslCommands = new HashMap<String, WSLCommandDefinition>();
 	protected int pauseLine;
 	protected Thread scriptThread;
 	private Pattern commandPattern = Pattern.compile("^([\\w_]+)(\\s+(.*))?");
 	private ScriptTimer timer = new ScriptTimer();
 	private boolean lastCondition = false;
 	private ArrayList<WSLAbstractCommand> commands = new ArrayList<WSLAbstractCommand>();
 	
 	protected WSLEngine engine;
 	protected IStormFrontScriptCommands scriptCommands;
 	protected IStormFrontClient client;
 	
 	private final Lock lock = new ReentrantLock();
 	private final Condition gotResume = lock.newCondition();
 	
 	private static final String argSeparator = "\\s+";
 	
 	public WSLScript (WSLEngine engine, IScriptInfo info, IStormFrontClient client)
 	{
 		super(info);
 		this.engine = engine;
 		this.client = client;
 		
 		scriptCommands = new StormFrontScriptCommands(client, this);
 		
 		// add command handlers
 		addCommandDefinition("counter", new WSLCounter());
 		addCommandDefinition("deletevariable", new WSLDeleteVariable());
 		addCommandDefinition("echo", new WSLEcho());
 		addCommandDefinition("else", new WSLElse());
 		addCommandDefinition("exit", new WSLExit());
 		addCommandDefinition("gosub", new WSLGosub());
 		addCommandDefinition("goto", new WSLGoto());
 		for(int i = 0; i <= 9; i++) {
 			addCommandDefinition("if_" + i, new WSLIf_(String.valueOf(i)));
 		}
 		addCommandDefinition("match", new WSLMatch());
 		addCommandDefinition("matchre", new WSLMatchRe());
 		addCommandDefinition("matchwait", new WSLMatchWait());
 		addCommandDefinition("math", new WSLMath());
 		addCommandDefinition("move", new WSLMove());
 		addCommandDefinition("nextroom", new WSLNextRoom());
 		addCommandDefinition("pause", new WSLPause());
 		addCommandDefinition("put", new WSLPut());
 		addCommandDefinition("random", new WSLRandom());
 		addCommandDefinition("return", new WSLReturn());
 		addCommandDefinition("save", new WSLSave());
 		addCommandDefinition("setlocalvariable", new WSLSetLocalVariable());
 		addCommandDefinition("setvariable", new WSLSetVariable());
 		addCommandDefinition("shift", new WSLShift());
 		addCommandDefinition("timer", new WSLTimer());
 		addCommandDefinition("wait", new WSLWait());
 		addCommandDefinition("waitfor", new WSLWaitFor());
 		addCommandDefinition("waitforre", new WSLWaitForRe());
 		
 		setVariable("t", new WSLTime());
 		setVariable("mana", new WSLMana());
 		setVariable("health", new WSLHealth());
 		setVariable("fatigue", new WSLFatigue());
 		setVariable("spirit", new WSLSpirit());
 		setVariable("rt", new WSLRoundTime());
 		setVariable("lhand", new WSLLeftHand());
 		setVariable("rhand", new WSLRightHand());
 		setVariable("spell", new WSLSpell());
 		setVariable("roomdesc", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_DESCRIPTION));
 		setVariable("roomexits", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_EXITS));
 		setVariable("roomplayers", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_PLAYERS));
 		setVariable("roomobjects", new WSLComponent(IStormFrontClient.COMPONENT_ROOM_OBJECTS));
 		setVariable("roomtitle", new WSLRoomTitle());
 	}
 
 	public IWSLValue getVariable(String name) {
 		return globalVariables.get(name.toLowerCase());
 	}
 	
 	public boolean variableExists(String name) {
 		return globalVariables.containsKey(name.toLowerCase());
 	}
 	
 	public boolean localVariableExists(String name) {
 		return localVariables.containsKey(name.toLowerCase());
 	}
 	
 	public IWSLValue getLocalVariable(String name) {
 		return localVariables.get(name.toLowerCase());
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 	
 	private class WSLFrame {
 		private WSLAbstractCommand line;
 		private HashMap<String, IWSLValue> localVariables;
 		
 		public WSLFrame(WSLAbstractCommand line, HashMap<String, IWSLValue> variables) {
 			this.line = line;
 			this.localVariables = variables;
 		}
 
 		public void restore() {
 			WSLScript.this.localVariables = localVariables;
 			WSLScript.this.curCommand = line;
 			WSLScript.this.nextCommand = line;
 		}
 	}
 	
 	private class WSLTime extends WSLAbstractNumber {
 		public double toDouble() {
 			return timer.get();
 		}
 	}
 	
 	private class WSLMana extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getMana().get().getValue();
 		}
 	}
 	
 	private class WSLHealth extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getHealth().get().getValue();
 		}
 	}
 	
 	private class WSLFatigue extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getFatigue().get().getValue();
 		}
 	}
 	
 	private class WSLSpirit extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getSpirit().get().getValue();
 		}
 	}
 	
 	private class WSLRoundTime extends WSLAbstractNumber {
 		public double toDouble() {
 			return client.getRoundtime().get();
 		}
 	}
 	
 	private class WSLLeftHand extends WSLAbstractString {
 		public String toString() {
 			return client.getLeftHand().get();
 		}
 	}
 	
 	private class WSLRightHand extends WSLAbstractString {
 		public String toString() {
 			return client.getRightHand().get();
 		}
 	}
 	
 	private class WSLSpell extends WSLAbstractString {
 		public String toString() {
 			return client.getCurrentSpell().get();
 		}
 	}
 	
 	private class WSLRoomTitle extends WSLAbstractString {
 		public String toString() {
 			return client.getStream(IStormFrontClient.ROOM_STREAM_NAME).getTitle().get();
 		}
 	}
 	
 	private class WSLComponent extends WSLAbstractString {
 		protected String componentName;
 		public WSLComponent(String componentName) {
 			this.componentName = componentName;
 		}
 		
 		public String toString () {
 			return client.getComponent(componentName).get();
 		}
 	}
 	
 	private class ScriptRunner  implements Runnable {
 		public void run() {
 			doStart();
 			
 			while(curCommand != null && !stopped) {
 				waitForResume();
 				int index = commands.indexOf(curCommand) + 1;
 				if(index < commands.size())
 					nextCommand = commands.get(index);
 				else
 					nextCommand = null;
 				
 				curCommand.execute();
 				
 				curCommand = nextCommand;
 				scriptCommands.clearInterrupt();
 			}
 			
 			if(!stopped)
 				stop();
 		}
 	}
 	
 	public void start (Collection<String> arguments)
 	{
 		StringBuffer totalargs = new StringBuffer();
 		int i = 1;
 		for (String argument : arguments) {
 			setVariable(Integer.toString(i), argument);
 			if (i > 1)
 				totalargs.append(" ");
 			totalargs.append(argument);
 			i++;
 		}
 		for(; i <= 9; i++) {
 			setVariable(Integer.toString(i), "");
 		}
 		setVariable("0", totalargs.toString());
 		
 		for (String varName : scriptCommands.getStormFrontClient().getServerSettings().getVariableNames())
 		{
 			setVariable(varName, scriptCommands.getStormFrontClient().getServerSettings().getVariable(varName));
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
 			
 			CharStream input = new ANTLRNoCaseReaderStream(scriptReader);
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
 
 		client.getDefaultStream().echo("[script started: " + getName() + "]\n");
 		running = true;
 		stopped = false;
 		scriptCommands.waitForRoundtime();
 		curCommand = commands.get(0);
 	}
 	
 	private void waitForResume() {
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
 	
 	public void addLabel(String label, WSLAbstractCommand line) {
 		labels.put(label.toLowerCase(), line);
 	}
 	
 	public void addCommand(WSLAbstractCommand command) {
 		commands.add(command);
 	}
 	
 	public void execute(String line) {
 		curLine = line;
 		Matcher m = commandPattern.matcher(line);
 		
 		if (!m.find()) {
 			return;
 		}
 		
 		String commandName = m.group(1).toLowerCase();
 		String arguments = m.group(3);
 		if(arguments == null) arguments = "";
 		
 		WSLCommandDefinition command = wslCommands.get(commandName);
 		if(command != null) {
 			if (debugging) scriptCommands.echo("Debug: " + line);
 			command.execute(arguments);
 		} else {
 			//TODO output the line number here
 			scriptCommands.echo("Invalid command \"" + line + "\"");
 		}
 	}
 	
 	public void stop() {
 		running = false;
 		stopped = true;
 		scriptCommands.stop();
 		
 		client.getDefaultStream().echo("[script stopped: " + getName() + "]\n");
 		super.stop();
 	}
 
 	public void suspend() {
 		running = false;
 		
 		client.getDefaultStream().echo("[script paused: " + getName() + "]\n");
 		super.suspend();
 	}
 	
 	public void resume() {
 		running = true;
 		
 		client.getDefaultStream().echo("[script resumed: " + getName() + "]\n");
 
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
 	
 	protected void addCommandDefinition (String name, WSLCommandDefinition command) {
 		wslCommands.put(name, command);
 	}
 	
 	protected void scriptError(String message) {
 		client.getDefaultStream().echo("Script error on line " + curCommand.getLineNumber() + " (" + curLine + "): " + message + "\n");
 		stop();
 	}
 	
 	protected void scriptWarning(String message) {
 		client.getDefaultStream().echo("Script warning on line " + curCommand.getLineNumber() + " (" + curLine + "): " + message + "\n");
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
 	
 	abstract protected class WSLCommandDefinition {
 		
 		abstract public void execute(String arguments);
 		
 	}
 	
 	protected class WSLSave extends WSLCommandDefinition {
 		
 		public void execute(String arguments) {
 			setVariable("s", arguments);
 		}
 	}
 
 	protected class WSLShift extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			StringBuffer zeroarg = new StringBuffer();
 			for (int i = 1; ; i++) {
 				if (!variableExists(Integer.toString(i+1)))
 				{
 					if (zeroarg.length() > 0) { 
 						zeroarg.deleteCharAt(zeroarg.length() - 1);
 						setVariable("0",zeroarg.toString());
 					} else {
 						deleteVariable("0");
 					}
 					deleteVariable(Integer.toString(i));
 					break;
 				}
 				else
 				{
 					String arg = getVariable(Integer.toString(i+1)).toString();
 					if (arg == null) {
 						if (zeroarg.length() > 0) {
 							zeroarg.deleteCharAt(zeroarg.length() - 1);
 							setVariable("0",zeroarg.toString());
 						} else {
 							deleteVariable("0");
 						}
 						
 						deleteVariable(Integer.toString(i));
 						break;
 					}
 					zeroarg.append(arg + " ");
 					setVariable(Integer.toString(i), arg);
 				}
 			}
 		}
 	}
 
 
 
 	protected class WSLDeleteVariable extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			String var = arguments.split(argSeparator)[0];
 			deleteVariable(var);
 		}
 	}
 
 	private void setVariable(String name, String value) {
 		setVariable(name, new WSLString(value));
 	}
 	
 	private void setVariable(String name, IWSLValue value) {
 		globalVariables.put(name.toLowerCase(), value);
 	}
 	
 	private void deleteVariable(String name) {
 		globalVariables.remove(name.toLowerCase());
 	}
 	
 	protected class WSLSetVariable extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)(\\s+(.+)?)?$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			if (m.find())
 			{
 				String name = m.group(1);
 				String value = m.group(3);
 				if(value == null)
 					value = " ";
 				scriptCommands.echo("setVariable: " + name + "=" + value);
 				setVariable(name, value);
 			} else {
 				scriptWarning("Invalid arguments to setvariable");
 			}
 		}
 	}
 	
 	protected class WSLSetLocalVariable extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)(\\s+(.+)?)?$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			if (m.find())
 			{
 				String name = m.group(1);
 				String value = m.group(3);
 				if(value == null)
 					value = " ";
 				scriptCommands.echo("setLocalVariable: " + name + "=" + value);
 				setLocalVariable(name, value);
 			} else {
 				scriptError("Invalid arguments to setLocalVariable");
 			}
 		}
 	}
 	
 	protected void gotoCommand(WSLAbstractCommand command) {
 		curCommand = nextCommand = command;
 		
 		// if we're in an action, interrupt execution on the main thread
 		if(Thread.currentThread() != scriptThread) {
 			scriptCommands.interrupt();
 		}
 	}
 	
 	protected void gotoLabel (String label)
 	{
 		WSLAbstractCommand command = labels.get(label.toLowerCase());
 		
 		if (command != null)
 		{
 			gotoCommand(command);
 		}
 		else {
 			command = labels.get("labelerror");
 			if (command != null)
 			{
 				gotoCommand(command);
 			}
 			else
 			{
 				scriptError("Invalid goto statement, label \"" + label + "\" does not exist");
 			}
 		}
 	}
 	
 	protected class WSLGoto extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			if(arguments.trim().length() > 0) {
 				String[] args = arguments.split(argSeparator);
 				String label = args[0];
 				gotoLabel(label);
 			} else {
 				scriptError("Invalid arguments to goto");
 			}
 		}
 	}
 	
 	public void setLocalVariable(String name, String value) {
 		setLocalVariable(name, new WSLString(value));
 	}
 	
 	public void setLocalVariable(String name, IWSLValue value) {
 		localVariables.put(name, value);
 	}
 	
 	protected void gosub (String label, String arguments)
 	{
 		String[] args = arguments.split(argSeparator);
 		
 		WSLFrame frame = new WSLFrame(nextCommand, localVariables);
 		callstack.push(frame);
 		
 		// TODO perhaps abstract this
 		localVariables = (HashMap<String, IWSLValue>)localVariables.clone();
 		setLocalVariable("0", arguments);
 		for(int i = 0; i < args.length; i++) {
 			setLocalVariable(String.valueOf(i + 1), args[i]);
 		}
 		
 		WSLAbstractCommand command = labels.get(label.toLowerCase());
 		
 		if (command != null)
 		{
 			gotoCommand(command);
 		} else {
 			scriptError("Invalid gosub statement, label \"" + label + "\" does not exist");
 		}
 	}
 	
 	protected class WSLGosub extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s*(.*)?$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find()) {
 				gosub(m.group(1), m.group(2));
 			} else {
 				scriptError("Invalid arguments to gosub");
 			}
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
 	
 	protected class WSLReturn extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			gosubReturn();
 		}
 	}
 
 	private void getVariablesFromMatch(Match match) {
 		String value;
 		for(int i = 0; (value = (String)match.getAttribute(String.valueOf(i))) != null; i++) {
 			setLocalVariable(String.valueOf(i), value);
 		}
 	}
 	
 	protected class WSLMatchWait extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			double time;
 			
 			if(arguments.trim().length() > 0) {
 				String[] args = arguments.split(argSeparator);
 			
 				try {
 					time = Double.parseDouble(args[0]);
 				} catch(NumberFormatException e) {
 					scriptError("Non-numeral \"" + args[0] + "\" passed to matchwait");
 					return;
 				}
 			} else {
 				time = 0;
 			}
 			
 			Match match = scriptCommands.matchWait(time);
 			
 			if (match != null)
 			{
 				getVariablesFromMatch(match);
 				gotoLabel((String)match.getAttribute("label"));
 				scriptCommands.waitForPrompt();
 				scriptCommands.waitForRoundtime();
 			}
 		}
 	}
 
 	protected void addMatch(String label, Match match) {
 		match.setAttribute("label", label);
 		scriptCommands.addMatch(match);
 	}
 	
 	protected class WSLMatchRe extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^([\\w_]+)\\s+/(.*)/(\\w*)");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				String regex = m.group(2);
 				boolean caseInsensitive = m.group(3).contains("i");
 				Match match = new RegexMatch(regex, caseInsensitive);
 				
 				addMatch(m.group(1), match);
 			} else {
 				scriptError("Invalid arguments to matchre");
 			}
 		}
 
 	}
 
 	protected class WSLMatch extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^([^\\s]+)\\s+(.*)$");
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.find())
 			{
 				Match match = new TextMatch(m.group(2));
 				addMatch(m.group(1), match);
 			} else {
 				scriptError("Invalid arguments to match");
 			}
 		}
 	}
 	
 	protected class WSLCounter extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			if (arguments.length() == 0) {
 				scriptError("You must provide an argument to counter");
 				return;
 			}
 			
 			doMath("c", arguments);
 			
 		}
 	}
 	
 	protected class WSLMath extends WSLCommandDefinition {
 
 		public void execute (String arguments) {
 			String[] args = arguments.split(argSeparator, 2);
 			if (args.length < 2) {
 				scriptError("Not enough arguments to math");
 				return;
 			}
 
 			doMath(args[0], args[1]);
 
 		}
 	}
 	
 	private void doMath(String targetVar, String arguments) {
 		String[] args = arguments.split(argSeparator);
 		if (args.length < 1) {
 			scriptError("No operator for math");
 			return;
 		}
 
 		String operator = args[0].trim().toLowerCase();
 		
 		int operand;
 		if (args.length > 1) {
 			try {
 				operand = Integer.parseInt(args[1].trim());
 			} catch (NumberFormatException e) {
 				scriptError("Operand must be a number");
 				return;
 			}
 		} else
 				operand = 1;
 
 		if ("set".equalsIgnoreCase(operator))
 		{
 			setVariable(targetVar, Integer.toString(operand));
 			return;
 		}
 		
 		int value;
 		if(variableExists(targetVar)) {
 			try {
 				value = (int)getVariable(targetVar).toDouble();
 			} catch(NumberFormatException e) {
 				scriptError("The variable \"" + targetVar + "\" must be a number to do math with it");
 				return;
 			}
 		} else
 				value = 0;
 
 
 		if ("add".equalsIgnoreCase(operator))
 		{	
 			int newValue = value + operand;
 			setVariable(targetVar, Integer.toString(newValue));
 		}
 		else if ("subtract".equalsIgnoreCase(operator))
 		{
 			int newValue = value - operand;
 			setVariable(targetVar, Integer.toString(newValue));
 		}
 		else if ("multiply".equalsIgnoreCase(operator))
 		{
 			int newValue = value * operand;
 			setVariable(targetVar, Integer.toString(newValue));
 		}
 		else if ("divide".equalsIgnoreCase(operator))
 		{
 			if (operand == 0) {
 				scriptError("Cannot divide by zero");
 				return;
 			}
 			int newValue = value / operand;
 			setVariable(targetVar, Integer.toString(newValue));
 		}
 		else if ("modulus".equalsIgnoreCase(operator))
 		{
 			int newValue = value % operand;
 			setVariable(targetVar, Integer.toString(newValue));
 		}
 		else
 		{
 			scriptError("Unrecognized math command \"" + operator + "\"");
 		}
 	}
 
 	protected class WSLWaitForRe extends WSLCommandDefinition {
 
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
 				
 				scriptCommands.waitFor(match);
 				getVariablesFromMatch(match);
 			} else {
 				scriptError("Invalid arguments to waitforre");
 			}
 		}
 	}
 	
 	protected class WSLWaitFor extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			if (arguments.length() >= 1)
 			{
 				Match match = new TextMatch(arguments);
 				scriptCommands.waitFor(match);
 				
 			} else {
 				scriptError("Invalid arguments to waitfor");
 			}
 		}
 	}
 
 	protected class WSLWait extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			scriptCommands.waitForPrompt();
 		}
 	}
 	
 	protected class WSLPut extends WSLCommandDefinition {
 		
 		public void execute(String arguments) {
 			scriptCommands.put(arguments);
 		}
 	}
 	
 	protected class WSLEcho extends WSLCommandDefinition {
 		
 		public void execute (String arguments)
 		{
 			scriptCommands.echo(arguments);
 		}
 	}
 	
 	protected class WSLPause extends WSLCommandDefinition {
 		
 		public void execute (String arguments)
 		{
 			double time;
 			
 			if(arguments.trim().length() > 0) {
 				String[] args = arguments.split(argSeparator);
 			
 				try {
 					time = Double.parseDouble(args[0]);
 				} catch(NumberFormatException e) {
 					scriptError("Non-numeral \"" + args[0] + "\" passed to pause");
 					return;
 				}
 			} else {
 				time = 1;
 			}
 			scriptCommands.pause(time);
 		}
 	}
 	
 	protected class WSLMove extends WSLCommandDefinition {
 		
 		public void execute (String arguments)
 		{
 			scriptCommands.move(arguments);
 		}
 	}
 	
 	protected class WSLNextRoom extends WSLCommandDefinition {
 		
 		public void execute (String arguments)
 		{
 			scriptCommands.nextRoom();
 		}
 	}
 	
 	protected class WSLExit extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			// TODO figure out if we should make this call here or elsewhere
 			stop();
 		}
 	}
 	
 	protected class WSLIf_ extends WSLCommandDefinition {
 		private String variableName;
 		public WSLIf_ (String variableName)
 		{
 			this.variableName = variableName;
 		}
 		
 		public void execute (String arguments) {
			if (variableExists(variableName) && getVariable(variableName).toString().length() > 0)
 			{
 				WSLScript.this.execute(arguments);
 			}
 		}
 	}
 	
 	private class WSLRandom extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^(\\d+)\\s+(\\d+)");
 		
 		public void execute(String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if(m.find()) {
 				int min = Integer.parseInt(m.group(1));
 				int max = Integer.parseInt(m.group(2));
 				int r = min + new Random().nextInt(max - min + 1);
 				
 				setVariable("r", Integer.toString(r));
 			} else {
 				scriptError("Invalid arguments to random");
 			}
 		}
 	}
 	
 	private class WSLTimer extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^(\\w+)");
 		
 		public void execute(String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if(m.find()) {
 				String command = m.group(1);
 				if(command.equals("start"))			timer.start();
 				else if(command.equals("stop"))		timer.stop();
 				else if(command.equals("clear"))	timer.clear();
 				else {
 					scriptError("Invalid command \"" + command + "\" given to timer");
 				}
 			} else {
 				scriptError("Invalid arguments to timer");
 			}
 		}
 	}
 	
 	protected class WSLAddHighlightString extends WSLCommandDefinition {
 		
 		private Pattern format = Pattern.compile("^\"([^\"])\"(\\s*(.*))?");
 		private Pattern optionFormat = Pattern.compile("(\\w+)=(.*)");
 		
 		public void execute (String arguments)
 		{
 			Matcher m = format.matcher(arguments);
 			if(m.find()) {
 				String text = m.group(1);
 				String optionString = m.group(3);
 				String[] options = optionString.split(argSeparator);
 				
 				WarlockStyle style = new WarlockStyle();
 				for(String option : options) {
 					Matcher optionMatcher = optionFormat.matcher(option);
 					if(optionMatcher.find()) {
 						String key = optionMatcher.group(1);
 						String value = optionMatcher.group(2);
 						
 						if(key.equalsIgnoreCase("forecolor")) {
 							
 						}
 					}
 				}
 			}
 		}
 	}
 	
 	public void setLastCondition(boolean condition) {
 		this.lastCondition = condition;
 	}
 	
 	private class WSLElse extends WSLCommandDefinition {
 		
 		public void execute (String arguments) {
 			if (!lastCondition)
 			{
 				WSLScript.this.execute(arguments);
 			}
 		}
 	}
 	
 	public IScriptEngine getScriptEngine() {
 		return engine;
 	}
 	
 	public void movedToRoom() {
 		scriptCommands.movedToRoom();
 	}
 	
 }
