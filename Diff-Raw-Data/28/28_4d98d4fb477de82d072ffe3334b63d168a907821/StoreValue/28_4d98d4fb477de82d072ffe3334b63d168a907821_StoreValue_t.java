 package compiler.ir.instructions;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 
 import compiler.back.regAloc.VirtualRegister;
 import compiler.front.symbolTable.Symbol;
 import compiler.front.symbolTable.Symbol.SymbolKind;
 
 
 public class StoreValue extends Instruction {
 
 	public Symbol symbol;
     public Instruction value;
 	public Index address;
 	
 	public StoreValue(Index address, Instruction value) {
 		// store value to address
         this.value = value;
 		this.address = address;
 	}
 
 	public StoreValue(Symbol symbol, Instruction value) {
		// store value to symbol address
 		// this.address = symbol.getAddress(); //TODO
 		this.value = value;
		this.symbol = symbol;
 	}
 	
 	public List<VirtualRegister> getInputOperands() {
 		// lazily fill the input operands list
 		if(inputOps == null) {
 			this.inputOps = new ArrayList<VirtualRegister>();
 			this.inputOps.add(Instruction.resolve(value).outputOp);
 			if (address != null) {
 				this.inputOps.add(Instruction.resolve(address).outputOp);
 			}
 		}
 		return inputOps;
 	}
 
 	
	public String toString() {
 		return getInstrNumber() + " : STORE " +
				"(" + Instruction.resolve(value).getInstrNumber() + ")" +
 				"(" + (symbol != null ? "@"+symbol.ident  : address.getInstrNumber() ) + ")" + 
 				" \n [" + Instruction.resolve(value).outputOp +  (symbol != null ?"": ", "+Instruction.resolve(address).outputOp ) +"]" ;
 	}
 
 }
