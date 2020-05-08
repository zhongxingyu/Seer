 package cc.warlock.script.wsl;
 
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import cc.warlock.script.wsl.WarlockWSLScript.WarlockWSLCommand;
 
 public class WarlockWSLScriptLine {
 	private WarlockWSLScript script;
 	private int lineNumber;
 	private WarlockWSLScriptLine next;
 	private ArrayList<WarlockWSLScriptArg> args = new ArrayList<WarlockWSLScriptArg>();
 	
 	private Pattern commandPattern;
 	
 	public WarlockWSLScriptLine(WarlockWSLScript script, int lineNumber) {
 		this.script = script;
 		this.lineNumber = lineNumber;
 		commandPattern = Pattern.compile("^([\\w_]+)(\\s+(.*))?");
 	}
 	
 	
 	public void execute() {
 		if(args == null) return; // in a label
 		
 		StringBuffer buffer = new StringBuffer();
 		for(WarlockWSLScriptArg arg : args) {
 			// System.out.print("appending arg \"" + arg.getString(variables) + "\"\n");
 			
			buffer.append(arg.getString(script.getVariables()));
 		}
 		
 		String line = buffer.toString();
 		System.out.print("running line: " + line + "\n");
 		
 		Matcher m = commandPattern.matcher(line);
 		
 		if (!m.find()) {
 			System.out.println("Couldn't find the command");
 			return;
 		}
 		
 		String commandName = m.group(1).toLowerCase();
 		System.out.print("command \"" + commandName + "\"\n");
 		String arguments = m.group(3);
 		if(arguments == null) arguments = "";
 		System.out.print("arguments \"" + arguments + "\"\n");
 		
 		WarlockWSLCommand command = script.getCommands().get(commandName);
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
 }
