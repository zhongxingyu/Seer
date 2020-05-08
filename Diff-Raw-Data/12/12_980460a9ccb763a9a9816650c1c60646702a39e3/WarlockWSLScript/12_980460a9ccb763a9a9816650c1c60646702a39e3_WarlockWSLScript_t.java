 package cc.warlock.script.wsl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 
 import cc.warlock.script.AbstractScript;
 import cc.warlock.script.IScriptCommands;
 import cc.warlock.script.IScriptListener;
 import cc.warlock.script.Match;
 
 public class WarlockWSLScript extends AbstractScript implements Runnable {
 	
 	protected String script, scriptName;
 	protected boolean running, stopped;
 	protected HashMap<String, WarlockWSLScriptLine> labels = new HashMap<String, WarlockWSLScriptLine>();
 	protected WarlockWSLScriptLine nextLine;
 	protected WarlockWSLScriptLine curLine;
 	protected WarlockWSLScriptLine endLine;
 	protected HashMap<String, String> variables = new HashMap<String, String>();
 	protected HashMap<String, WarlockWSLCommand> wslCommands = new HashMap<String, WarlockWSLCommand>();
 	protected int pauseLine;
 	protected Thread scriptThread;
 	private ArrayList<Match> matchset = new ArrayList<Match>();
 	
 	private final Lock lock = new ReentrantLock();
 	private final Condition gotResume = lock.newCondition();
 	
 	private static final String argSeparator = "\\s+";
 	
 	public WarlockWSLScript (IScriptCommands commands, String scriptName, Reader scriptReader)
 		throws IOException
 	{
 		super(commands);
 		
 		// add command handlers
 		addCommand(new WarlockWSLPut());
 		addCommand(new WarlockWSLEcho());
 		addCommand(new WarlockWSLPause());
 		addCommand(new WarlockWSLShift());
 		addCommand(new WarlockWSLSave());
 		addCommand(new WarlockWSLCounter());
 		addCommand(new WarlockWSLDeleteVariable());
 		addCommand(new WarlockWSLSetVariable());
 		addCommand(new WarlockWSLGoto());
 		addCommand(new WarlockWSLMatchWait());
 		addCommand(new WarlockWSLMatchRe());
 		addCommand(new WarlockWSLMatch());
 		addCommand(new WarlockWSLWaitForRe());
 		addCommand(new WarlockWSLWaitFor());
 		addCommand(new WarlockWSLWait());
 		addCommand(new WarlockWSLMove());
 		addCommand(new WarlockWSLNextRoom());
 		addCommand(new WarlockWSLExit());
 		// change these to be added/removed as variables are set/deleted
 		for(int i = 1; i <= 9; i++) {
 			addCommand(new WarlockWSLIf_(Integer.toString(i)));
 		}
 		
 		this.scriptName = scriptName;
 		
 		StringBuffer script = new StringBuffer();
 		
 		char[] bytes = new char[1024];
 		int size = 0;
 		
 		while (size != -1)
 		{	
 			size = scriptReader.read(bytes);
 			if (size != -1)
 				script.append(bytes, 0, size);
 		}
 		scriptReader.close();
 		
 		this.script = script.toString();
 	}
 	
 	public String getName() {
 		return scriptName;
 	}
 
 	public Map<String, String> getVariables() {
 		return variables;
 	}
 	
 	public Map<String, WarlockWSLCommand> getCommands() {
 		return wslCommands;
 	}
 	
 	public boolean isRunning() {
 		return running;
 	}
 	
 	public void start (ArrayList<String> arguments)
 	{
 		for (int i = 0; i < arguments.size(); i++) {
 			variables.put(Integer.toString(i + 1), arguments.get(i));
 		}
 		
 		scriptThread = new Thread(this);
 		scriptThread.setName("Wizard Script: " + scriptName);
 		scriptThread.start();
 		
 		for (IScriptListener listener : listeners) listener.scriptStarted(this);
 	}
 	
 	protected void doStart ()
 	{
 		CharStream input = new ANTLRStringStream(script + "\n");
 		WarlockWSLLexer lex = new WarlockWSLLexer(input);
 		CommonTokenStream tokens = new CommonTokenStream(lex);
 		WarlockWSLParser parser = new WarlockWSLParser(tokens);
 		
 		parser.setScript(this);
 		
 		try {
 			parser.script();
 		} catch (RecognitionException e) {
 			// TODO handle the exception
 		}
 
 		commands.echo("[script started: " + scriptName + "]");
 		running = true;
 		stopped = false;
 	}
 	
 	public void run() {
 		doStart();
 		
 		while(curLine != null && !stopped) {
 			checkState();
 			nextLine = curLine.getNext();
 			
 			curLine.execute();
 			curLine = nextLine;
 		}
 		
 		stop();
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
 	
 	public void addLabel(String label, WarlockWSLScriptLine line) {
 		labels.put(label, line);
 	}
 	
 	public void addLine(WarlockWSLScriptLine line) {
 		if(curLine == null) {
 			curLine = line;
 		}
 		if(endLine != null) {
 			endLine.setNext(line);
 		}
 		endLine = line;
 	}
 	
 	public void stop() {
 		running = false;
 		stopped = true;
 		
 		commands.echo("[script stopped: " + scriptName + "]");
 		super.stop();
 	}
 
 	public void suspend() {
 		running = false;
 		//pauseLine = nextLine;
 		
 		commands.echo("[script paused: " + scriptName + "]");
 		super.suspend();
 	}
 	
 	public void resume() {
 		
 		//nextLine = pauseLine;
 		running = true;
 		
 		commands.echo("[script resumed: " + scriptName + "]");
 
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
 	
 	protected void addCommand (WarlockWSLCommand command) {
 		wslCommands.put(command.getName(), command);
 	}
 	
 	abstract protected class WarlockWSLCommand {
 		abstract public String getName();
 		
 		abstract public void execute(String arguments);
 	}
 	
 	protected class WarlockWSLSave extends WarlockWSLCommand {
 		public String getName() {
 			return "save";
 		}
 		
 		public void execute(String arguments) {
 			variables.put("s", arguments);
 		}
 	}
 
 	protected class WarlockWSLShift extends WarlockWSLCommand {
 		public String getName() {
 			return "shift";
 		}
 		
 		public void execute (String arguments) {
 			for (int i = 0; ; i++) {
 				String arg = variables.get(Integer.toString(i + 1));
 				if (arg == null) {
 					variables.remove(Integer.toString(i));
 					break;
 				}
 				variables.put(Integer.toString(i), arg);
 			}
 		}
 	}
 
 	protected class WarlockWSLCounter extends WarlockWSLCommand {
 		public String getName() {
 			return "counter";
 		}
 		
 		public void execute (String arguments) {
 			String[] args = arguments.split(argSeparator);
 			if (args.length >= 2)
 			{
 				String counterFunction = args[0];
 				int value = variables.containsKey("c") ? Integer.parseInt(variables.get("c")) : 0;
 
 				if ("set".equalsIgnoreCase(counterFunction))
 				{
 					variables.put("c", args[1]);
 				}
 				else if ("add".equalsIgnoreCase(counterFunction))
 				{	
 					int newValue = value + Integer.parseInt(args[1]);
 					variables.put("c", "" + newValue);
 				}
 				else if ("subtract".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value - Integer.parseInt(args[1]);
 					variables.put("c", "" + newValue);
 				}
 				else if ("multiply".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value * Integer.parseInt(args[1]);
 					variables.put("c", "" + newValue);
 				}
 				else if ("divide".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value / Integer.parseInt(args[1]);
 					variables.put("c", "" + newValue);
 				}
 			} else { /*throw error */ }
 		}
 	}
 
 	protected class WarlockWSLDeleteVariable extends WarlockWSLCommand {
 		public String getName() {
 			return "deletevariable";
 		}
 		
 		public void execute (String arguments) {
 			String var = arguments.split(argSeparator)[0];
 			variables.remove(var);
 		}
 	}
 
 	protected class WarlockWSLSetVariable extends WarlockWSLCommand {
 		private Pattern format;
 		
 		public WarlockWSLSetVariable() {
 			format = Pattern.compile("^([\\w_]+)\\s+(.*)");
 		}
 		
 		public String getName() {
 			return "setvariable";
 		}
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			if (m.matches())
 			{
 				// System.out.print("variable: \"" + m.group(1) + "\" value: \"" + m.group(2) + "\"\n");
 				variables.put(m.group(1), m.group(2));
 			} else {
 				// System.out.print("Didn't match \"" + arguments + "\"\n");
 			}
 		}
 	}
 	
 	protected void gotoLabel (String label)
 	{
 		// System.out.println("going to label: \"" + label + "\"");
 		
 		WarlockWSLScriptLine command = labels.get(label);
 		
 		if (command != null)
 		{
 			// System.out.println("found label");
 			curLine = nextLine = command;
 		}
 		else {
 			// System.out.println("label not found");
 			command = labels.get("labelerror");
 			if (command != null)
 			{
 				curLine = nextLine = command;
 			}
 			else {
 				commands.echo ("***********");
 				commands.echo ("*** WARNING: Label \"" + label + "\" doesn't exist, skipping goto statement ***");
 				commands.echo ("***********");
 			}
 		}
 	}
 	
 	protected class WarlockWSLGoto extends WarlockWSLCommand {
 		public String getName() {
 			return "goto";
 		}
 		
 		public void execute (String arguments) {
 			String[] args = arguments.split(argSeparator);
 			if (args.length >= 1)
 			{
 				String label = args[0];
 				gotoLabel(label);
 			} else { /*throw error*/ }
 		}
 	}
 
 	protected class WarlockWSLMatchWait extends WarlockWSLCommand {
 		public String getName() {
 			return "matchwait";
 		}
 		
 		public void execute (String arguments) {
 			// mode = Mode.waiting;
 			
 			Match match = commands.matchWait(matchset.toArray(new Match[matchset.size()]));
 			
 			if (match != null)
 			{
 				// System.out.println("matched label: \"" + match.getAttribute("label") + "\"");
 				matchset.clear();
 				gotoLabel((String)match.getAttribute("label"));
 				commands.waitForPrompt();
				commands.waitForRoundtime();
 			} else {
 				commands.echo("*** Internal error, no match was found!! ***\n");
 			}
 		}
 	}
 
 
 	
 	protected class WarlockWSLMatchRe extends WarlockWSLCommand {
 		private Pattern format;
 		
 		public WarlockWSLMatchRe() {
 			format = Pattern.compile("^([\\w_]+)\\s+/(.*)/(\\w*)");
 		}
 		
 		public String getName() {
 			return "matchre";
 		}
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.matches())
 			{
 				String regex = m.group(2);
 				Match match = new Match();
 				
 				if (m.group(3).contains("i"))
 				{
 					match.setRegex(regex, true);
 				} else {
 					match.setRegex(regex, false);
 				}
 				
 				match.setAttribute("label", m.group(1));
 				
 				matchset.add(match);
 			} else { /* TODO throw error */ }
 		}
 
 	}
 
 	protected class WarlockWSLMatch extends WarlockWSLCommand {
 		private Pattern format;
 		
 		public WarlockWSLMatch() {
 			format = Pattern.compile("^([\\w_]+)\\s+(.*)");
 		}
 		
 		public String getName() {
 			return "match";
 		}
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.matches())
 			{
 				Match match = new Match();
 				match.setAttribute("label", m.group(1));
 				match.setMatchText(m.group(2));
 				
 				// System.out.println("adding match \"" + m.group(1) + "\": \"" + m.group(2) + "\"");
 				
 				matchset.add(match);
 			} else { /* TODO throw error */ }
 		}
 	}
 
 	protected class WarlockWSLWaitForRe extends WarlockWSLCommand {
 		private Pattern format;
 		
 		public WarlockWSLWaitForRe() {
 			format = Pattern.compile("^/(.*)/(\\w*)");
 		}
 		
 		public String getName() {
 			return "waitforre";
 		}
 		
 		public void execute (String arguments) {
 			Matcher m = format.matcher(arguments);
 			
 			if (m.matches())
 			{
 				String flags = m.group(2);
 				boolean ignoreCase = false;
 				
 				if (flags != null && flags.contains("i"))
 				{
 					ignoreCase = true;
 				}
 				
 				Match match = new Match();
 				match.setRegex(m.group(1), ignoreCase);
 				
 				commands.waitFor(match);
 			} else { /* TODO throw error */ }
 		}
 	}
 	
 	protected class WarlockWSLWaitFor extends WarlockWSLCommand {
 		
 		public String getName() {
 			return "waitfor";
 		}
 		
 		public void execute (String arguments) {
 			if (arguments.length() >= 1)
 			{
 				Match match = new Match();
 				match.setMatchText(arguments);
 				commands.waitFor(match);
 				
 			} else { /* TODO throw error */ }
 		}
 	}
 
 	protected class WarlockWSLWait extends WarlockWSLCommand {
 		public String getName() {
 			return "wait";
 		}
 		
 		public void execute (String arguments) {
 			commands.waitForPrompt();
 		}
 	}
 	
 	protected class WarlockWSLPut extends WarlockWSLCommand {
 		public String getName() {
 			return "put";
 		}
 		
 		public void execute(String arguments) {
 			commands.put(WarlockWSLScript.this, arguments);
 		}
 	}
 	
 	protected class WarlockWSLEcho extends WarlockWSLCommand {
 		public String getName() {
 			return "echo";
 		}
 		
 		public void execute (String arguments)
 		{
 			commands.echo(WarlockWSLScript.this, arguments);
 		}
 	}
 	
 	protected class WarlockWSLPause extends WarlockWSLCommand {
 		public String getName() {
 			return "pause";
 		}
 		
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
 	
 	protected class WarlockWSLMove extends WarlockWSLCommand {
 		public String getName() {
 			return "move";
 		}
 		
 		public void execute (String arguments)
 		{
 			commands.move(arguments);
 		}
 	}
 	
 	protected class WarlockWSLNextRoom extends WarlockWSLCommand {
 		public String getName() {
 			return "nextroom";
 		}
 		
 		public void execute (String arguments)
 		{
 			commands.nextRoom();
 		}
 	}
 	
 	protected class WarlockWSLExit extends WarlockWSLCommand {
 		public String getName() {
 			return "exit";
 		}
 		
 		public void execute (String arguments) {
 			running = false;
 			stopped = true;
 		}
 	}
 	
 	protected class WarlockWSLIf_ extends WarlockWSLCommand {
 		protected String variableName;
 		private Pattern format;
 		
 		public WarlockWSLIf_ (String variableName) {
 			this.variableName = variableName;
 			format = Pattern.compile("^([a-zA-Z0-9_]+) +(.*)");
 		}
 		
 		public String getName () {
 			return "if_" + variableName;
 		}
 		
 		public void execute (String arguments) {
 			if (variables.containsKey(variableName)) {
 				Matcher m = format.matcher(arguments);
 				m.find();
 				
 				String curCommandName = m.group(1).toLowerCase();
 				
 				WarlockWSLCommand command = wslCommands.get(curCommandName);
 				if(command != null) {
 					command.execute(m.group(2));
 				} else {
 					// this acts as a comment
 				}
 			}
 		}
 	}
 	
 	private void handleDeleteFromHighlightNames(List<String> arguments) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void handleDeleteFromHighlightStrings(List<String> arguments) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void handleAddToHighlightStrings(List<String> arguments) {
 		// TODO Auto-generated method stub
 	}
 	
 	public void stopScript() {
 		stopped = true;
 	}
 }
