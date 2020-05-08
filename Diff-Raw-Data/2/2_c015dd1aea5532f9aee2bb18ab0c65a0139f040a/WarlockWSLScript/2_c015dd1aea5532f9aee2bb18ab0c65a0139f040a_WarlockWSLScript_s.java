 package cc.warlock.script.wsl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import cc.warlock.script.AbstractScript;
 import cc.warlock.script.CallbackEvent;
 import cc.warlock.script.IMatch;
 import cc.warlock.script.IScriptCallback;
 import cc.warlock.script.IScriptCommands;
 import cc.warlock.script.IScriptListener;
 import cc.warlock.script.internal.Match;
 import cc.warlock.script.wsl.internal.WarlockWSLLexer;
 import cc.warlock.script.wsl.internal.WarlockWSLParser;
 
 public class WarlockWSLScript extends AbstractScript implements IScriptCallback, Runnable {
 	
 	protected String script, scriptName;
 	protected boolean running, stopped;
 	protected Hashtable<String, Integer> labelOffsets = new Hashtable<String, Integer>();
 	protected Hashtable<String, String> variables = new Hashtable<String, String>();
 	protected ArrayList<String> scriptArguments = new ArrayList<String>();
 	protected ArrayList<ArrayList<String>> lineTokens;
 	protected HashMap<String, WarlockWSLCommand> wslCommands = new HashMap<String, WarlockWSLCommand>();
 	protected int pauseLine, nextLine;
 	protected Thread scriptThread;
 	
 	private String mode;
 	private static final String MODE_START = "start";
 	private static final String MODE_CONTINUE = "continue";
 	private static final String MODE_WAITING = "waiting";
 	private static final String MODE_EXIT = "exit";
 	
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
 
 	public boolean isRunning() {
 		return running;
 	}
 	
 	public void start (ArrayList<String> arguments)
 	{
 		this.scriptArguments.addAll(arguments);
 		mode = MODE_START;
 		
 		scriptThread = new Thread(this);
 		scriptThread.setName("Wizard Script: " + scriptName);
 		scriptThread.start();
 		
 		for (IScriptListener listener : listeners) listener.scriptStarted(this);
 	}
 	
 	protected void doStart ()
 	{
 		WarlockWSLParser parser = new WarlockWSLParser(new WarlockWSLLexer(new ByteArrayInputStream(script.getBytes())));
 		
 		try {
 			lineTokens = parser.script();
 			
 			int i = 0;
 			
 			commands.echo("[script started: " + scriptName + "]");
 			running = true;
 			stopped = false;
 			
 			for (ArrayList<String> tokens : lineTokens)
 			{
 				parseLabel(tokens, i);
 				i++;
 			}
 			
 			processLineTokens(0);
 		} catch (RecognitionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TokenStreamException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	protected void processLineTokens (int startLine)
 	{
 		for (int i = startLine; i < lineTokens.size();)
 		{
 			if (stopped || mode == MODE_WAITING) break;
 			
 			nextLine = i+1;
 			
 			ArrayList<String> tokens = lineTokens.get(i);
 			parseLine(tokens, i);
 			
 			if ((i+1) < lineTokens.size() && nextLine != i+1)
 			{
 				i = nextLine;
 				continue;
 			}
 			
 			i++;
 		}
 	}
 	
 	public void stop() {
 		commands.removeCallback(this);
 		
 		running = false;
 		stopped = true;
 		mode = MODE_EXIT;
 		
 		super.stop();
 	}
 
 	public void suspend() {
 		mode = MODE_WAITING;
 		commands.removeCallback(this);
 		running = false;
 		pauseLine = nextLine;
 		
 		commands.echo("[script paused: " + scriptName + "]");
 		super.suspend();
 	}
 	
 	public void resume() {
 		mode = MODE_CONTINUE;
 		
 		nextLine = pauseLine;
 		running = true;
 		
 		commands.echo("[script resumed: " + scriptName + "]");
 
 		super.resume();
 	}
 	
 	private Pattern labelPattern = Pattern.compile("^([^:]+): *$");
 	protected void parseLabel (ArrayList<String> tokens, int lineIndex)
 	{
 		String firstToken = tokens.get(0);
 		Matcher matcher = labelPattern.matcher(firstToken);
 		
		if (matcher.matches())
 		{
 			labelOffsets.put(matcher.group(1), lineIndex);
 			tokens.remove(0);
 		}
 	}
 	
 	protected String replaceVariables (String token)
 	{
 		String newToken = new String(token);
 		
 		for (String varName : variables.keySet())
 		{
 			if (newToken.contains("%" + varName))
 			{
 				newToken = newToken.replaceAll("\\%" + varName, variables.get(varName));
 			}
 		}
 		
 		for (String var : commands.getClient().getServerSettings().getVariableNames())
 		{
 			if (newToken.contains("%" + var))
 			{
 				newToken = newToken.replaceAll("\\%" + var, commands.getClient().getServerSettings().getVariable(var));
 			}
 		}
 		
 		for (int i = 0; i <= 9; i++)
 		{
 			if (newToken.contains("%" + i))
 			{
 				try {
 					String argument = scriptArguments.get(i-1);
 					newToken = newToken.replaceAll("\\%" + i, argument);
 				}
 				catch (IndexOutOfBoundsException ex) {
 					newToken = newToken.replaceAll("\\%" + i, "");
 				}
 			}
 		}
 		
 		newToken = newToken.replace("\\%[A-Za-z0-9_]+", "");
 		return newToken;
 	}
 
 	
 	protected void parseLine (ArrayList<String> tokens, int lineIndex)
 	{
 		mode = MODE_CONTINUE;
 		
 		if (tokens.size() == 0) return ;// empty line -- most likely a label
 		
 		String curCommandName = replaceVariables(tokens.get(0));
 		List<String> arguments = null;
 		if (tokens.size() > 0) arguments = tokens.subList(1, tokens.size());
 		
 		this.nextLine = lineIndex + 1;
 		
 		WarlockWSLCommand command = wslCommands.get(curCommandName);
 		if(command != null) {
 			command.execute(arguments);
 		} else {
 			// this acts as a comment
 		}
 	}
 	
 	protected void addCommand (WarlockWSLCommand command) {
 		wslCommands.put(command.getName(), command);
 	}
 	
 	abstract protected class WarlockWSLCommand {
 		protected String toString (List<String> strings)
 		{
 			StringBuffer buffer = new StringBuffer();
 			Iterator<String> iter = strings.iterator();
 			while (iter.hasNext())
 			{
 				buffer.append(iter.next());
 				if(iter.hasNext()) {
 					buffer.append(' ');
 				}
 			}
 			return replaceVariables(buffer.toString());
 		}
 		
 		abstract public String getName();
 		
 		abstract public void execute(List<String> arguments);
 	}
 	
 	protected class WarlockWSLSave extends WarlockWSLCommand {
 		public String getName() {
 			return "save";
 		}
 		
 		public void execute(List<String> arguments) {
 			variables.put("s", toString(arguments));
 		}
 	}
 
 	protected class WarlockWSLShift extends WarlockWSLCommand {
 		public String getName() {
 			return "shift";
 		}
 		
 		public void execute (List<String> arguments) {
 			scriptArguments.remove(0);
 		}
 	}
 
 	protected class WarlockWSLCounter extends WarlockWSLCommand {
 		public String getName() {
 			return "counter";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() == 2)
 			{
 				String counterFunction = arguments.get(0);
 				int value = variables.containsKey("c") ? Integer.parseInt(variables.get("c")) : 0;
 
 				if ("set".equalsIgnoreCase(counterFunction))
 				{
 					variables.put("c", replaceVariables(arguments.get(1)));
 				}
 				else if ("add".equalsIgnoreCase(counterFunction))
 				{	
 					int newValue = value + Integer.parseInt(arguments.get(1));
 					variables.put("c", "" + newValue);
 				}
 				else if ("subtract".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value - Integer.parseInt(arguments.get(1));
 					variables.put("c", "" + newValue);
 				}
 				else if ("multiply".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value * Integer.parseInt(arguments.get(1));
 					variables.put("c", "" + newValue);
 				}
 				else if ("divide".equalsIgnoreCase(counterFunction))
 				{
 					int newValue = value / Integer.parseInt(arguments.get(1));
 					variables.put("c", "" + newValue);
 				}
 			} else { /*throw error */ }
 		}
 	}
 
 	protected class WarlockWSLDeleteVariable extends WarlockWSLCommand {
 		public String getName() {
 			return "deletevariable";
 		}
 		
 		public void execute (List<String> arguments) {
 			variables.remove(arguments.get(1));
 		}
 	}
 
 	protected class WarlockWSLSetVariable extends WarlockWSLCommand {
 		public String getName() {
 			return "setvariable";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() == 2)
 			{
 				variables.put(arguments.get(0), arguments.get(1));
 			} else { /*throw error*/ }
 		}
 	}
 	
 	protected void gotoLabel (String label)
 	{
 		int offset = -1;
 		if (labelOffsets.containsKey(label))
 		{
 			offset = labelOffsets.get(label);
 		}
 		else if (labelOffsets.containsKey("labelError"))
 		{
 			offset = labelOffsets.get("labelError");
 		}
 		
 		if (offset > -1)
 		{
 			this.nextLine = offset;
 		}
 		else {
 			commands.echo ("***********");
 			commands.echo ("*** WARNING: Label \"" + label + "\" doesn't exist, skipping goto statement ***");
 			commands.echo ("***********");
 		}
 	}
 	
 	protected class WarlockWSLGoto extends WarlockWSLCommand {
 		public String getName() {
 			return "goto";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() == 1)
 			{
 				String label = replaceVariables(arguments.get(0));
 				gotoLabel(label);
 			} else { /*throw error*/ }
 		}
 	}
 
 	protected class WarlockWSLMatchWait extends WarlockWSLCommand {
 		public String getName() {
 			return "matchwait";
 		}
 		
 		public void execute (List<String> arguments) {
 			mode = MODE_WAITING;
 			
 			commands.matchWait(matchset.toArray(new IMatch[matchset.size()]), WarlockWSLScript.this);
 			running = false;
 		}
 	}
 
 	private ArrayList<IMatch> matchset = new ArrayList<IMatch>();
 	
 	protected class WarlockWSLMatchRe extends WarlockWSLCommand {
 		public String getName() {
 			return "matchre";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() >= 2)
 			{
 				mode = MODE_WAITING;
 				
 				String regex = toString(arguments.subList(1, arguments.size()));
 				Match match = new Match();
 				
 				int end = regex.length() - 1;
 				if (regex.endsWith("/i"))
 				{
 					match.ignoreCase = true;
 					end = regex.length() - 2;
 				}
 				regex = regex.substring(1, end);
 				
 				match.data.put("label", replaceVariables(arguments.get(0)));
 				match.matchText = regex; 
 				match.regex = true;
 				
 				matchset.add(match);
 			} else { /* TODO throw error */ }
 		}
 
 	}
 
 	protected class WarlockWSLMatch extends WarlockWSLCommand {
 		public String getName() {
 			return "match";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() >= 2)
 			{
 				Match match = new Match();
 				match.data.put("label", replaceVariables(arguments.get(0)));
 				match.matchText = toString(arguments.subList(1, arguments.size()));
 				match.regex = false;
 				match.ignoreCase = true;
 				
 				matchset.add(match);
 			} else { /* TODO throw error */ }
 		}
 	}
 
 	protected class WarlockWSLWaitForRe extends WarlockWSLCommand {
 		public String getName() {
 			return "waitforre";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() >= 1)
 			{
 				mode = MODE_WAITING;
 				
 				String regex = toString(arguments);
 				int end = regex.length() - 1;
 				boolean ignoreCase = false;
 				
 				if (regex.endsWith("/i"))
 				{
 					ignoreCase = true;
 					end = regex.length() - 2;
 				}
 				regex = regex.substring(1, end);
 				
 				commands.waitFor(regex, true, ignoreCase, WarlockWSLScript.this);
 				running = false;
 			} else { /* TODO throw error */ }
 		}
 	}
 	
 	protected class WarlockWSLWaitFor extends WarlockWSLCommand {
 		public String getName() {
 			return "waitfor";
 		}
 		
 		public void execute (List<String> arguments) {
 			if (arguments.size() >= 1)
 			{
 				mode = MODE_WAITING;
 				
 				String text = toString(arguments);
 				commands.waitFor(text, false, true, WarlockWSLScript.this);
 				running = false;
 				
 			} else { /* TODO throw error */ }
 		}
 	}
 
 	protected class WarlockWSLWait extends WarlockWSLCommand {
 		public String getName() {
 			return "wait";
 		}
 		
 		public void execute (List<String> arguments) {
 			mode = MODE_WAITING;
 			
 			commands.waitForPrompt(WarlockWSLScript.this);
 			running = false;
 		}
 	}
 	
 	protected class WarlockWSLPut extends WarlockWSLCommand {
 		public String getName() {
 			return "put";
 		}
 		
 		public void execute(List<String> arguments) {
 			commands.put(WarlockWSLScript.this, toString(arguments));
 		}
 	}
 	
 	protected class WarlockWSLEcho extends WarlockWSLCommand {
 		public String getName() {
 			return "echo";
 		}
 		
 		public void execute (List<String> arguments)
 		{
 			commands.echo(WarlockWSLScript.this, toString(arguments));
 		}
 	}
 	
 	protected class WarlockWSLPause extends WarlockWSLCommand {
 		public String getName() {
 			return "pause";
 		}
 		
 		public void execute (List<String> arguments)
 		{
 			mode = MODE_WAITING;
 			if (arguments.size() == 1)
 			{
 				int time = Integer.parseInt(arguments.get(0));
 				commands.pause(time, WarlockWSLScript.this);
 			}
 			else {
 				// "empty" pause.. just means wait for RT
 				commands.pause(0, WarlockWSLScript.this);
 			}
 		}
 	}
 	
 	protected class WarlockWSLMove extends WarlockWSLCommand {
 		public String getName() {
 			return "move";
 		}
 		
 		public void execute (List<String> arguments)
 		{
 			commands.move(toString(arguments), WarlockWSLScript.this);
 			mode = MODE_WAITING;
 		}
 	}
 	
 	protected class WarlockWSLNextRoom extends WarlockWSLCommand {
 		public String getName() {
 			return "nextroom";
 		}
 		
 		public void execute (List<String> arguments)
 		{
 			commands.nextRoom(WarlockWSLScript.this);
 			mode = MODE_WAITING;
 		}
 	}
 	
 	protected class WarlockWSLExit extends WarlockWSLCommand {
 		public String getName() {
 			return "exit";
 		}
 		
 		public void execute (List<String> arguments) {
 			running = false;
 			stopped = true;
 			
 			mode = MODE_EXIT;
 		}
 	}
 	
 	protected class WarlockWSLIf_ extends WarlockWSLCommand {
 		protected String variableName;
 		
 		public WarlockWSLIf_ (String variableName) {
 			this.variableName = variableName;
 		}
 		
 		public String getName () {
 			return "if_" + variableName;
 		}
 		
 		public void execute (List<String> arguments) {
 			if (variables.containsKey(variableName)) {
 				String curCommandName = arguments.get(0);
 				arguments = arguments.subList(1, arguments.size());
 				
 				WarlockWSLCommand command = wslCommands.get(curCommandName);
 				if(command != null) {
 					command.execute(arguments);
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
 	
 	public void run ()
 	{
 		try {
 			while (!stopped)
 			{
 				if (MODE_START.equals(mode)) { doStart(); }
 				else if (MODE_CONTINUE.equals(mode)) { processLineTokens(nextLine); }
 				else if (MODE_EXIT.equals(mode)) { break; }
 				
 				/* MODE_WAITING is implicit */
 				Thread.sleep((long) 200);
 			}
 			
 			commands.echo("[script finished: " + scriptName + "]");		
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	protected void continueAtNextLine ()
 	{
 		mode = MODE_CONTINUE;
 	}
 	
 	public void handleCallback(CallbackEvent event) {
 		switch (event.type)
 		{
 			case FinishedWaitingForPrompt:
 				commands.waitForRoundtime(this); break;
 			case FinishedWaiting:
 			case FinishedPausing:
 				commands.waitForPrompt(this); break;
 			case FinishedWaitingForRoundtime:
 			case InNextRoom:
 				continueAtNextLine(); break;
 			case Matched:
 			{
 				IMatch match = (IMatch) event.data.get(CallbackEvent.DATA_MATCH);
 				if (match != null)
 				{
 					matchset.clear();
 					gotoLabel(replaceVariables(match.getData().get("label")));
 					continueAtNextLine();
 				}
 			} break;
 		}
 	}
 }
