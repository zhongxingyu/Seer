 package assemblernator;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * The Assembler class parses a file into a Module object.
  * @author Noah
  * @Date Apr 5, 2012 7:48:36PM
  *
  */
 public class Assembler {
 	private Map<String, Instruction> instructions = new HashMap<String, Instruction>();
	private Map<String, Instruction> byteCodes = new HashMap<String, Instruction>();
 	private int version = 1;
 	
 	/**
 	 * Parses a file into a Module.
 	 * @author Noah
 	 * @date Apr 5, 2012; 7:33:45 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param file source code for module.
 	 * @return <pre>
 	 * {@code let line = a line of characters in a file.
 	 * let instr = sub-string of characters before a ';' character in a line.
 	 * Instruction i = Instruction.parse(instr);
 	 * startOp = first line of file = name * "KICKO" * "FC:" * address,
	 * where name is a string of characters and address is a string of characters
	 * representing a memory address.
 	 * returns Module m, where for all lines in file,
 	 * 	m.assembly = sequence of i;
 	 * 	m.symbols = Map of (i.label, i);
 	 * 	m.programName = name;
	 * 	m.startAddr = number of 
 	 *  m.moduleLength = length of substring from programName to keyword "End" in file.
	 *  m.execStart = address;
 	 * @specRef N/A
 	 */
 	public Module parseFile(File file) {
 		
 		return new Module();
 	}
 	
 }
