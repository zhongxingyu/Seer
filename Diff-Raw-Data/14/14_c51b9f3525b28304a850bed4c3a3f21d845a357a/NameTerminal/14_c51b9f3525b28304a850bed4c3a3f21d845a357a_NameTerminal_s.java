 package ast.terminal;
 
 import edu.pjwstk.jps.ast.terminal.INameTerminal;
 import edu.pjwstk.jps.ast.terminal.IStringTerminal;
 
public class NameTerminal extends TerminalExpression<String> implements
		INameTerminal {
 
 	public NameTerminal(String name) {
 		super(name);
 	}
 
 	public String getName() {
		return value;
 	}
 
 }
