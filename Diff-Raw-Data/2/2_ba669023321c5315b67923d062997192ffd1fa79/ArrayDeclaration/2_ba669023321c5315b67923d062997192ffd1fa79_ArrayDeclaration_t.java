 package ast;
 
 import crux.Symbol;
 
 /**
  * Command for array declaration.
  */
public class ArrayDeclaration extends Command implements Declaration {
 
 	private Symbol symbol;
 
 	public ArrayDeclaration(int lineNum, int charPos, Symbol symbol) {
 		super(lineNum, charPos);
 		this.symbol = symbol;
 	}
 
 	public Symbol symbol() {
 		return symbol;
 	}
 
 	public String toString() {
 		return super.toString() + "[" + symbol.toString() + "]";
 	}
 
 	public void accept(CommandVisitor visitor) {
 		visitor.visit(this);
 	}
 }
