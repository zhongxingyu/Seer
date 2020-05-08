 package cc.warlock.script.wsl;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cc.warlock.script.wsl.WarlockWSLScript.WarlockWSLCommand;
 
 public class WarlockWSLScriptLine {
 	private int lineNumber;
 	private WarlockWSLScript script;
 	private WarlockWSLScriptLine next;
 	private ArrayList<WarlockWSLScriptArg> args = new ArrayList<WarlockWSLScriptArg>();
 	private boolean isLabel;
 	
 	private Pattern commandPattern;
 	
 	public WarlockWSLScriptLine(WarlockWSLScript script, int lineNumber) {
 		this(script, lineNumber, false);
 	}
 	
 	public WarlockWSLScriptLine(WarlockWSLScript script, int lineNumber, boolean isLabel) {
 		this.script = script;
 		this.lineNumber = lineNumber;
 		this.isLabel = isLabel;
 		commandPattern = Pattern.compile("^([\\w_]+)(\\s+(.*))?");
 	}
 	
 	
 	public void execute(HashMap<String, String> variables, HashMap<String, WarlockWSLCommand> commands) {
 		StringBuffer buffer = new StringBuffer();
 		for(WarlockWSLScriptArg arg : args) {
 			System.out.print("appending arg \"" + arg.getString(variables) + "\"\n");
 			
 			buffer.append(arg.getString(variables));
 		}
 		
 		System.out.print("running line: " + buffer.toString() + "\n");
 		
 		Matcher m = commandPattern.matcher(buffer.toString());
 		
 		if (!m.find()) {
 			// TODO handle the error
 			return;
 		}
 		
 		String commandName = m.group(1).toLowerCase();
 		System.out.print("command \"" + commandName + "\"\n");
 		String arguments = m.group(3);
 		System.out.print("arguments \"" + arguments + "\"\n");
 		
 		WarlockWSLCommand command = commands.get(commandName);
 		if(command != null) command.execute(arguments);
 	}
 	
 	public int getLineNumber() {
 		return lineNumber;
 	}
 	
 	public void setNext(WarlockWSLScriptLine next) {
 		this.next = next;
 	}
 	
 	public WarlockWSLScriptLine getNext() {
 		return next;
 	}
 	
 	public void addArg(WarlockWSLScriptArg arg) {
 		args.add(arg);
 	}
 	
 	public void addArgs(ArrayList<WarlockWSLScriptArg> args) {
 		for(WarlockWSLScriptArg arg : args) {
 			addArg(arg);
 		}
 	}
 
 	public boolean isLabel() {
 		return isLabel;
 	}
 }
