 package assemblernator;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import assemblernator.Instruction.Usage;
 
 /**
  * A class representing an assembled urban module.
  * 
  * <pre> 
  * {@code assembly = a sequence of Instructions.
  * symbolTable is a Module.SymbolTable
  * startAddr is the integer address at which the module starts.
  * moduleLength is the integer length of the module.}
  * </pre>
  * 
  * Module = (assembly, symbolTable, startAddr, moduleLength).
  * 
  * @author Josh Ventura
  * @date Apr 5, 2012; 7:15:44 PM
  */
 public class Module {
 	/**
 	 * <pre>
 	 * symbolTable is a sequence of entry's where an entry = (label, address, usage, string);
 	 * 	for an instruction with the label, 
 	 * 	the address is the location of the instruction,
 	 * 	the usage is how the instruction is used,
 	 * 	and the string is the string of characters that the label is equated to if 
 	 * 	the opcode of the instruction = EQU or EQUe.
 	 * </pre>
 	 */
 	public static class SymbolTable implements Iterable<Map.Entry<String, Instruction>>{
 		
 		/** 
 		 * let (label, Instruction) = p.
 		 * symbols is a sorted Map of p's.
 		 * Each p is a single entry in SymbolTable, where 
 		 * 	label = Instruction.label,
 		 * 	address = Instruction.lc, 
 		 * 	usage = Instruction.usage,
 		 *  and string = the value of the operand in Instruction.
 		 */
 		private SortedMap<String, Instruction> symbols =  new TreeMap<String, Instruction>();
 	
 		/**
 		 * adds an entry into the symbol table.
 		 * @author Noah
 		 * @date Apr 6, 2012; 1:56:43 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @param instr instruction to add.
 		 * @specRef N/A
 		 */
 		public void addEntry(Instruction instr) {
 			symbols.put(instr.label, instr);
 		}
 		
 		/**
 		 * provides an Iterator over the elements of the symbol table.
 		 * @author Noah
 		 * @date Apr 7, 2012; 4:19:44 PM
 		 * @modified UNMODIFIED
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return an Iterator over elements of symbol table.
 		 * @specRef N/A
 		 */
 		@Override
 		public Iterator<Map.Entry<String, Instruction>> iterator() {
 			return symbols.entrySet().iterator();
 		}
 		
 		/**
 		 * String representation of the symbol table.
 		 * @author Eric
 		 * @date Apr 6, 2012; 8:58:56 AM
 		 * @modified Apr 7, 1:26:50 PM; added opcode to lines. - Noah <br>
 		 * Apr 6, 11:02:08 AM; removed remove() call to prevent destruction of symbol table, <br>
 		 * 	also, cleaned code up. -Noah<br>
 		 * @tested UNTESTED
 		 * @errors NO ERRORS REPORTED
 		 * @codingStandards Awaiting signature
 		 * @testingStandards Awaiting signature
 		 * @return <pre>
 		 * {@code let line = a string of character representing a single entry
 		 * 	in the symbol table, concatenated w/ the opcode of the instruction with
		 * 	the label in hex format, uppercased. specifically:
 		 * 		opcode + " " + label + " " + address + " " + " " + usage + " " + string + "\n".
 		 * 	each line is unique.
 		 * returns a sequence of lines for all the entries in the symbol table.}
 		 * </pre>
 		 * @specRef N/A
 		 */
 		@Override
 		public String toString() {
 			//way of storing each line of the symbol table
 			ArrayList<String> completeTable = new ArrayList<String>();
 
 			//iterator over elements of set of label, Instruction pairs.
 			Iterator<Entry<String, Instruction>> tableIt = symbols.entrySet().iterator();
 			
 			//loop runs through the whole symbol table
 			while (tableIt.hasNext()) { 
 				//gets the set values <K,V> stored into a map entry which can be used to get the values/key of K and V
 				Map.Entry<String, Instruction> entry = tableIt.next();
 				
 				//retrieve string representation of the opcode from the Instruction associated w/ the label.
 				//an opcode is 6 digits.
 				String opcode = IOFormat.formatHexInteger(entry.getValue().getOpcode(), 6);
				System.err.println(opcode);
 		        String label = entry.getKey();
 		        Instruction instr = entry.getValue();
 		        int addr = instr.lc;
 		        Usage usage = instr.usage;
 		        
 		        String oneLine = opcode + " " + label + " " + addr + " " + usage;
 		     
 		        //since equate are the only one with a string in the symbol table i use this to get the value of that string
 		        if (usage == Usage.EQUATE) {
 		        	//gets iterator over set of operands.
 		        	Iterator<Entry<String, String>> operandsIt = instr.operands.entrySet().iterator();
 		        	
 		        	//only one operand if usage = EQUATE.
 		        	oneLine = oneLine + operandsIt.next().getValue(); //value of operand not operand Key. 
 		        } 
 		        
 		        oneLine = oneLine + "\n";
 		        
 		        completeTable.add(oneLine);
 		        
 		    } 
 			
 			String megaTable = "";
 			//makes one big string out of each line of the symbol table
 			while (!completeTable.isEmpty()) {
 				megaTable = megaTable + completeTable.remove(0);
 			}
 			
 			return megaTable;
 		}
 		
 		
 	}
 
 	/** An array of parsed instructions in the order they appeared in the file. **/
 	List<Instruction> assembly = new ArrayList<Instruction>();
 	
 	/** 
 	 * a symbol table.
 	 */
 	private SymbolTable symbolTable = new SymbolTable();
 	
 	/**
 	 * The address at which execution will begin; set from the KICKO instruction
 	 * or by using the AEXS instruction. Must be between 0 and 1023, according
 	 * to specification.
 	 * @specRef OB1.5
 	 */
 	int startAddr;
 
 	/**
 	 * The length of this module, in instructions, between 0 and 1023, according
 	 * to specification.
 	 * @specRef OB1.3
 	 */
 	int moduleLength;
 	
 	/**
 	 * Returns a reference to the symbol table.
 	 * @author Noah
 	 * @date Apr 7, 2012; 10:24:23 AM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @return this.symbolTable
 	 * @specRef N/A
 	 */
 	public SymbolTable getSymbolTable() {
 		return symbolTable;
 	}
 	
 }
