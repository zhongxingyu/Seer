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
 		// store value to address
 		// this.address = symbol.getAddress(); //TODO
 		this.symbol = symbol;
 		this.value = value;
 	}
 	
 	public List<VirtualRegister> getInputOperands() {
 		// lazily fill the input operands list
		if(inputOps == null && address != null) {
 			this.inputOps = new ArrayList<VirtualRegister>();
 			this.inputOps.add(Instruction.resolve(value).outputOp);
			this.inputOps.add(Instruction.resolve(address).outputOp);
 		}
 		return inputOps;
 	}
 
 	
 	public String toString(){
 		return getInstrNumber() + " : STORE " +
 				"(" + value.getInstrNumber() + ")" +
 				"(" + (symbol != null ? "@"+symbol.ident  : address.getInstrNumber() ) + ")" + 
 				" \n [" + Instruction.resolve(value).outputOp +  (symbol != null ?"": ", "+Instruction.resolve(address).outputOp ) +"]" ;
 	}
 
 }
