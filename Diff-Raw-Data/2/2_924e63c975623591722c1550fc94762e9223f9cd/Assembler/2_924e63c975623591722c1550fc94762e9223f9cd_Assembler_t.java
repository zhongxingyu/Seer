 package assemblernator;
 
 import instructions.*;
 import java.util.Scanner;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * The Assembler class parses a file into a Module object.
  * @author Noah
  * @date Apr 5, 2012 7:48:36PM
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
 	 * calls getInstance on all extensions of Instruction.
 	 */
 	static {
 		//add all key words.
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
 		
 		//get static instances of all Instruction types.
 		USI_KICKO.getInstance();
 		USI_NEWLC.getInstance();
 		USI_EQU.getInstance();
 		USI_EQUE.getInstance();
 		USI_ENT.getInstance();
 		USI_EXT.getInstance();
 		USI_END.getInstance();
 		USI_AEXS.getInstance();
 		USI_SKIPS.getInstance();
 		USI_CHAR.getInstance();
 		USI_NUM.getInstance();
 		USI_ADRC.getInstance();
 		USI_MOVD.getInstance();
 		USI_MOVDN.getInstance();
 		USI_IADD.getInstance();
 		USI_IMAD.getInstance();
 		USI_IAA.getInstance();
 		USI_ISRG.getInstance();
 		USI_ISUB.getInstance();
 		USI_IMUL.getInstance();
 		USI_IDIV.getInstance();
 		USI_PWR.getInstance();
 		USI_CLR.getInstance();
 		USI_CLRA.getInstance();
 		USI_CLRX.getInstance();
 		USI_ISHR.getInstance();
 		USI_ISHL.getInstance();
 		USI_ISRA.getInstance();
 		USI_ISLA.getInstance();
 		USI_ROL.getInstance();
 		USI_ROR.getInstance();
 		USI_AND.getInstance();
 		USI_OR.getInstance();
 		USI_TREQ.getInstance();
 		USI_TRLT.getInstance();
 		USI_TRGT.getInstance();
 		USI_TR.getInstance();
 		USI_TRDR.getInstance();
 		USI_TRLK.getInstance();
 		USI_RET.getInstance();
 		USI_SKT.getInstance();
 		USI_IWSR.getInstance();
 		USI_IRKB.getInstance();
 		USI_CWSR.getInstance();
 		USI_CRKB.getInstance();
 		USI_PSH.getInstance();
 		USI_POP.getInstance();
 		USI_PST.getInstance();
 		USI_NOP.getInstance();
 		USI_DMP.getInstance();
 		USI_HLT.getInstance();
 		Comment.getInstance();
 		
 		
 	}
 	
 	/**
 	 * 
 	 * @author Noah
 	 * @date Apr 8, 2012; 7:33:22 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @specRef N/A
 	 */
 	public static void initialize() {
 	}
 	
 	/**
 	 * Parses a file into a Module.
 	 * @author Noah
 	 * @date Apr 5, 2012; 7:33:45 PM
 	 * @modified Apr 7, 2012; 9:28:15AM added line to add instructions w/ labels to symbol table. <br>
	 * Apr 9, 2012; 12:22:16AM assigned lc above newLC
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param file source code for module.
 	 * @return <pre>
 	 * {@code let line = a line of characters in a file.
 	 * Instruction i = Instruction.parse(line);
 	 * startOp = first line of file = name + "KICKO" + "FC:" + address,
 	 * 	where name is a string of characters representing the name of the module, 
 	 * 		and address is a string of characters representing a memory address.
 	 * module = sub-string from programName to keyword "END".
 	 * returns Module m, where for all lines in file,
 	 * 	m.assembly = sequence of i;
 	 * 	m.symbols = Map of (i.label, i);
 	 * 	m.startAddr = number of modules from start of file.
 	 *  m.moduleLength = length in lines of module;}
 	 *  </pre>
 	 * @specRef N/A
 	 */
 	public static final Module parseFile(File file) {
 		int lineNum = 1;
 		Module module = new Module();
 		try {
 			Scanner fileScan = new Scanner(file);
 			int startAddr = 0;
 			int lc = 0;
 
 			while (fileScan.hasNextLine()) {
 				//Module module = new Module();
 				String line = fileScan.nextLine();
 
 				Instruction instr = Instruction.parse(line);
 				if (instr == null)
 					continue;
 				
 				instr.origSrcLine = line; // Gives instruction source line.
 				instr.lineNum = lineNum;
 				
 				// Get new lc for next instruction.
 				instr.lc = lc;
 				lc = instr.getNewLC(lc, module);
 				
 
 				/*
 				 * if start of module, record startAddr of module.
 				 * execStart of module.
 				 */
 				if (instr.getOpId().equalsIgnoreCase("KICKO")) {
 					module.startAddr = startAddr;
 				} 
 							
 				if (instr.label != null) {
 					module.getSymbolTable().addEntry(instr);
 				} 
 				
 				module.assembly.add(instr);
 				
 				module.startAddr += lc;
 				lineNum++;
 			}
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		} catch (URBANSyntaxException e) {
 			System.err.println("Line " + lineNum + ", position " + e.index + ": Error in parsing file: " + e.getMessage());
 			if (e.getMessage() == null || e.getMessage().length() <= 5)
 				e.printStackTrace();
 		} catch (IOException e) {
 			if (!e.getMessage().startsWith("RAL"))
 				e.printStackTrace();
 			else
 				System.err.println("Line " + lineNum + " is not terminated by a semicolon.");
 		}
 		
 		
 		return module;
 	}
 	
 }
