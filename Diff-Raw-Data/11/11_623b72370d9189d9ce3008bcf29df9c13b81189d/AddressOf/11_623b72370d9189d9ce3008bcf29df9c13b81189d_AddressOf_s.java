 package ast;
 
 /**
 * TODO Command for getting the address of 
  */
 public class AddressOf extends Command implements Expression {
 
 	private crux.Symbol symbol;
 
 	public AddressOf(int lineNum, int charPos, crux.Symbol sym) {
 		super(lineNum, charPos);
 		this.symbol = sym;
 	}
 
 	public crux.Symbol symbol() {
 		return symbol;
 	}
 
 	public String toString() {
 		return super.toString() + "[" + symbol.name() + "]";
 	}
 
 	public void accept(CommandVisitor visitor) {
 		visitor.visit(this);
 	}
 }
