 package zinara.ast.instructions;
 
 import java.util.ArrayList;
 
 import zinara.symtable.SymTable;
 
 public class CodeBlock extends Instruction{
     private ArrayList block;   // ArrayList of... ?
     private SymTable symTable;
 
     public CodeBlock(ArrayList b){
 	this.block = b;
     }
 
     public ArrayList getBlock(){
 	return this.block;
     }
 
     public void addInst(Instruction i){
 	this.block.add(i);
     }
 
     public void setSymTable(SymTable st) { this.symTable = st; }
 }
