 package cc.warlock.script.wsl;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import antlr.RecognitionException;
 import antlr.TokenStreamException;
 import cc.warlock.script.CallbackEvent;
 import cc.warlock.script.IMatch;
 import cc.warlock.script.IScript;
 import cc.warlock.script.IScriptCallback;
 import cc.warlock.script.IScriptCommands;
 import cc.warlock.script.internal.Match;
 import cc.warlock.script.wsl.internal.WarlockWSLLexer;
 import cc.warlock.script.wsl.internal.WarlockWSLParser;
 
 public class WarlockWSLScript implements IScript, IScriptCallback, Runnable {
 	
 	protected String script, scriptName;
 	protected boolean running, stopped;
 	protected Hashtable<String, Integer> labelOffsets = new Hashtable<String, Integer>();
 	protected Hashtable<String, String> variables = new Hashtable<String, String>();
 	protected ArrayList<String> scriptArguments = new ArrayList<String>();
 	protected ArrayList<ArrayList<String>> lineTokens;
 	protected IScriptCommands commands;
 	protected int pauseLine, nextLine;
 	protected Thread scriptThread;
 	
	protected static final int EXIT_LINE = -99;
 	protected static final String FUNCTION_PUT = "put";
 	protected static final String FUNCTION_ECHO = "echo";
 	protected static final String FUNCTION_PAUSE = "pause";
 	protected static final String FUNCTION_MOVE = "move";
 	protected static final String FUNCTION_NEXT_ROOM = "nextroom";
 	protected static final String FUNCTION_WAIT = "wait";
 	protected static final String FUNCTION_WAIT_FOR = "waitfor";
 	protected static final String FUNCTION_WAIT_FOR_RE = "waitforre";
 	protected static final String FUNCTION_MATCH = "match";
 	protected static final String FUNCTION_MATCH_RE = "matchre";
 	protected static final String FUNCTION_MATCH_WAIT = "matchwait";
 	protected static final String FUNCTION_GOTO = "goto";
 	protected static final String FUNCTION_SET_VARIABLE = "setvariable";
 	protected static final String FUNCTION_DELETE_VARIABLE = "deletevariable";
 	protected static final String FUNCTION_COUNTER = "counter";
 	protected static final String FUNCTION_SHIFT = "shift";
 	protected static final String FUNCTION_SAVE = "save";
 	protected static final String FUNCTION_ADD_TO_HIGHLIGHT_STRINGS = "addtohighlightstrings";
 	protected static final String FUNCTION_DELETE_FROM_HIGHLIGHT_STRINGS = "deletefromhighlightstrings";
 	protected static final String FUNCTION_DELETE_FROM_HIGHLIGHT_NAMES = "deletefromhighlightnames";
 	protected static final String FUNCTION_EXIT = "exit";
 	
 	private String mode;
 	private static final String MODE_START = "start";
 	private static final String MODE_CONTINUE = "continue";
 	private static final String MODE_WAITING = "waiting";
 	
 	public WarlockWSLScript (IScriptCommands commands, String scriptName, Reader scriptReader)
 		throws IOException
 	{
 		this.commands = commands;
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
			if (!running) break;
 			nextLine = i+1;
 			
 			ArrayList<String> tokens = lineTokens.get(i);
 			parseLine(tokens, i);
 			
			if (nextLine == EXIT_LINE) break;
			
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
		nextLine = EXIT_LINE;
		commands.echo("[script exited: " + scriptName + "]");
 	}
 
 	public void suspend() {
 		commands.removeCallback(this);
 		running = false;
 		pauseLine = nextLine;
 		
 		commands.echo("[script paused: " + scriptName + "]");
 	}
 	
 	public void resume() {
 		nextLine = pauseLine;
 		running = true;
 		
 		commands.echo("[script resumed: " + scriptName + "]");
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
 		
 		for (int i = 0; i <= 9; i++)
 		{
 			if (newToken.contains("%" + i))
 			{
 				try {
 					String argument = scriptArguments.get(i-1);
 					newToken = newToken.replaceAll("\\%" + i, argument);
 				}
 				catch (IndexOutOfBoundsException ex) { }
 			}
 		}
 		
 		return newToken;
 	}
 	
 	protected String toString (List<String> strings)
 	{
 		String str = "";
 		boolean first = true;
 		for (String string : strings)
 		{
 			if (!first)
 			{
 				str += " ";
 			}
 			str += string;
 			first = false;
 		}
 		return replaceVariables(str);
 	}
 	
 	protected void parseLine (ArrayList<String> tokens, int lineIndex)
 	{
 		mode = MODE_CONTINUE;
 		
 		if (tokens.size() == 0) return ;// empty line -- most likely a label
 		
 		String function = replaceVariables(tokens.get(0));
 		List<String> arguments = null;
 		if (tokens.size() > 0) arguments = tokens.subList(1, tokens.size());
 		
 		this.nextLine = lineIndex + 1;
 		
 		if (FUNCTION_PUT.equalsIgnoreCase(function)) handlePut(arguments);
 		else if (FUNCTION_ECHO.equalsIgnoreCase(function)) handleEcho(arguments);
 		else if (FUNCTION_PAUSE.equalsIgnoreCase(function)) handlePause(arguments);
 		else if (FUNCTION_MOVE.equalsIgnoreCase(function)) handleMove(arguments);
 		else if (FUNCTION_NEXT_ROOM.equalsIgnoreCase(function)) handleNextRoom(arguments);
 		else if (FUNCTION_WAIT.equalsIgnoreCase(function)) handleWait(arguments);
 		else if (FUNCTION_WAIT_FOR.equalsIgnoreCase(function)) handleWaitFor(arguments);
 		else if (FUNCTION_WAIT_FOR_RE.equalsIgnoreCase(function)) handleWaitForRe(arguments);
 		else if (FUNCTION_MATCH.equalsIgnoreCase(function)) handleMatch(arguments);
 		else if (FUNCTION_MATCH_RE.equalsIgnoreCase(function)) handleMatchRe(arguments);
 		else if (FUNCTION_MATCH_WAIT.equalsIgnoreCase(function)) handleMatchWait(arguments);
 		else if (FUNCTION_GOTO.equalsIgnoreCase(function)) handleGoto(arguments);
 		else if (FUNCTION_SET_VARIABLE.equalsIgnoreCase(function)) handleSetVariable(arguments);
 		else if (FUNCTION_DELETE_VARIABLE.equalsIgnoreCase(function)) handleDeleteVariable(arguments);
 		else if (FUNCTION_COUNTER.equalsIgnoreCase(function)) handleCounter(arguments);
 		else if (FUNCTION_SHIFT.equalsIgnoreCase(function)) handleShift(arguments);
 		else if (FUNCTION_SAVE.equalsIgnoreCase(function)) handleSave(arguments);
 		else if (FUNCTION_ADD_TO_HIGHLIGHT_STRINGS.equalsIgnoreCase(function)) handleAddToHighlightStrings(arguments);
 		else if (FUNCTION_DELETE_FROM_HIGHLIGHT_STRINGS.equalsIgnoreCase(function)) handleDeleteFromHighlightStrings(arguments);
 		else if (FUNCTION_DELETE_FROM_HIGHLIGHT_NAMES.equalsIgnoreCase(function)) handleDeleteFromHighlightNames(arguments);
 		else if (FUNCTION_EXIT.equalsIgnoreCase(function)) handleExit(arguments);
 		
 	}
 	
 	private void handleSave(List<String> arguments) {
 		variables.put("s", toString(arguments));
 	}
 
 	private void handleShift(List<String> arguments) {
 		scriptArguments.remove(0);
 	}
 
 	private void handleCounter(List<String> arguments) {
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
 
 	private void handleDeleteVariable(List<String> arguments) {
 		variables.remove(arguments.get(1));
 	}
 
 	private void handleSetVariable(List<String> arguments) {
 		if (arguments.size() == 2)
 		{
 			variables.put(arguments.get(0), arguments.get(1));
 		} else { /*throw error*/ }
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
 	}
 	
 	protected void handleGoto(List<String> arguments) {
 		if (arguments.size() == 1)
 		{
 			String label = replaceVariables(arguments.get(0));
 			gotoLabel(label);
 		} else { /*throw error*/ }
 	}
 
 	private void handleMatchWait(List<String> arguments) {
 		mode = MODE_WAITING;
 		
 		commands.matchWait(matchset.toArray(new IMatch[matchset.size()]), this);
 		running = false;
 	}
 
 	private ArrayList<IMatch> matchset = new ArrayList<IMatch>();
 	private void handleMatchRe(List<String> arguments) {
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
 
 	private void handleMatch(List<String> arguments) {
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
 
 	private void handleWaitForRe(List<String> arguments) {
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
 			
 			commands.waitFor(regex, true, ignoreCase, this);
 			running = false;
 		} else { /* TODO throw error */ }
 	}
 
 	private void handleWaitFor(List<String> arguments) {
 		if (arguments.size() >= 1)
 		{
 			String text = toString(arguments);
 			
 			commands.waitFor(text, false, true, this);
 			running = false;
 			mode = MODE_WAITING;
 		} else { /* TODO throw error */ }
 	}
 
 	private void handleWait(List<String> arguments) {
 		mode = MODE_WAITING;
 		
 		commands.waitForLine(this);
 		running = false;
 	}
 
 	protected void handlePut (List<String> arguments)
 	{
 		commands.put(toString(arguments));
 	}
 	
 	protected void handleEcho (List<String> arguments)
 	{
 		commands.echo(toString(arguments));
 	}
 	
 	protected void handlePause (List<String> arguments)
 	{
 		mode = MODE_WAITING;
 		
 		if (arguments.size() == 1)
 		{
 			int time = Integer.parseInt(arguments.get(0));
 			commands.pause(time);
 		}
 		else {
 			// "empty" pause.. just means wait for RT
 			commands.pause(0);
 		}
 	}
 	
 	protected void handleMove (List<String> arguments)
 	{
 		commands.move(toString(arguments), this);
 		mode = MODE_WAITING;
 	}
 	
 	protected void handleNextRoom (List<String> arguments)
 	{
 		commands.nextRoom(this);
 		mode = MODE_WAITING;
 	}
 	
 	private void handleExit(List<String> arguments) {
		this.nextLine = EXIT_LINE;
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
 				
 				/* MODE_WAITING is implicit */
 				Thread.sleep((long) 200);
 			}
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
 			case FinishedWaiting:
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
