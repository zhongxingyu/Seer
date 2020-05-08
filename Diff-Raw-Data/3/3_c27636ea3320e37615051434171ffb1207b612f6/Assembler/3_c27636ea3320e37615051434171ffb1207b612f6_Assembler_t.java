 package assemblernator;
 
 import java.util.Scanner;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The Assembler class parses a file into a Module object.
  * @author Noah
  * @Date Apr 5, 2012 7:48:36PM
  *
  */
 public class Assembler {
 	/** Map of opId's to static Instructions*/
 	public static Map<String, Instruction> instructions = new HashMap<String, Instruction>();
 	/** Map of opCodes to static Instructions*/ 
 	public static  Map<Integer, Instruction> byteCodes = new HashMap<Integer, Instruction>();
 	/** version of Assembler*/
 	public static final int VERSION = 1;
 	/** set of operand keywords. */
 	public static Set<String> keyWords = new HashSet<String>();
 	
 	/**
 	 * fills keyWords with key words.
 	 */
 	static {
 		keyWords.add("DM");
 		keyWords.add("DR");
 		keyWords.add("DX");
 		keyWords.add("EX");
 		keyWords.add("FC");
 		keyWords.add("FL");
 		keyWords.add("FM");
 		keyWords.add("FR");
 		keyWords.add("FS");
 		keyWords.add("FX");
 		keyWords.add("LR");
 		keyWords.add("NW");
 		keyWords.add("ST");
 	}
 	
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
 	 * Instruction i = Instruction.parse(line);
 	 * startOp = first line of file = name * "KICKO" * "FC:" * address,
 	 * 	where name is a string of characters representing the name of the module, 
 	 * 		and address is a string of characters representing a memory address.
 	 * module = sub-string from programName to keyword "END".
 	 * returns Module m, where for all lines in file,
 	 * 	m.assembly = sequence of i;
 	 * 	m.symbols = Map of (i.label, i);
 	 * 	m.programName = name;
 	 * 	m.startAddr = number of modules from start of file.
 	 *  m.moduleLength = length in lines of module;
 	 *  m.execStart = address;}
 	 *  </pre>
 	 * @specRef N/A
 	 */
 	public static final Module parseFile(File file) {
 		try {
 			Scanner fileScan = new Scanner(file);
 			int startAddr = 0;
 			while (fileScan.hasNextLine()) {
 				Module module = new Module();
 				String line = fileScan.nextLine();
 				
 				Instruction instr = Instruction.parse(line);
 				
 				//increment lc of instr by word count;
 				//increment startAddr by word count;
 				
 				/*
 				 * if start of module, record startAddr of module.
 				 * execStart of module.
 				 */
 				if (instr.getOpId().equalsIgnoreCase("KICKO")) {
 					module.startAddr = startAddr;
 				} 
 				
 				if (instr.label != null) {
 					//add Instruction to symbol table.
 				} 
 				
 				module.assembly.add(instr);
 				
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println("Error in parsing file: " + e.getMessage());
			e.printStackTrace();
 		}
 		
 		return new Module();
 	}
 	
 }
