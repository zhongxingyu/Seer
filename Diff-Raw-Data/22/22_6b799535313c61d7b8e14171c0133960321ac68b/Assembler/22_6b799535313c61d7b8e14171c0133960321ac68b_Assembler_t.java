 package assemblernator;
 
 import instructions.Comment;
 import instructions.USI_ADRC;
 import instructions.USI_AEXS;
 import instructions.USI_AND;
 import instructions.USI_CHAR;
 import instructions.USI_CLR;
 import instructions.USI_CLRA;
 import instructions.USI_CLRX;
 import instructions.USI_CRKB;
 import instructions.USI_CWSR;
 import instructions.USI_DMP;
 import instructions.USI_END;
 import instructions.USI_ENT;
 import instructions.USI_EQU;
 import instructions.USI_EQUE;
 import instructions.USI_EXT;
 import instructions.USI_HLT;
 import instructions.USI_IAA;
 import instructions.USI_IADD;
 import instructions.USI_IDIV;
 import instructions.USI_IMAD;
 import instructions.USI_IMUL;
 import instructions.USI_IRKB;
 import instructions.USI_ISHL;
 import instructions.USI_ISHR;
 import instructions.USI_ISLA;
 import instructions.USI_ISRA;
 import instructions.USI_ISRG;
 import instructions.USI_ISUB;
 import instructions.USI_IWSR;
 import instructions.USI_KICKO;
 import instructions.USI_MOVD;
 import instructions.USI_MOVDN;
 import instructions.USI_NEWLC;
 import instructions.USI_NOP;
 import instructions.USI_NUM;
 import instructions.USI_OR;
 import instructions.USI_POP;
 import instructions.USI_PSH;
 import instructions.USI_PST;
 import instructions.USI_PWR;
 import instructions.USI_RET;
 import instructions.USI_ROL;
 import instructions.USI_ROR;
 import instructions.USI_SKIPS;
 import instructions.USI_SKT;
 import instructions.USI_TR;
 import instructions.USI_TRDR;
 import instructions.USI_TREQ;
 import instructions.USI_TRGT;
 import instructions.USI_TRLK;
 import instructions.USI_TRLT;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.ErrorReporting.URBANSyntaxException;
 import assemblernator.Instruction.Usage;
 
 /**
  * The Assembler class parses a file into a Module object.
  * 
  * @author Noah
  * @date Apr 5, 2012 7:48:36PM
  * 
  */
 public class Assembler {
 	/** Map of opId's to static Instructions */
 	public static Map<String, Instruction> instructions = new HashMap<String, Instruction>();
 	/** Map of opCodes to static Instructions */
 	public static Map<Integer, Instruction> byteCodes = new HashMap<Integer, Instruction>();
 	/** version of Assembler */
 	public static final int VERSION = 1;
 	/** set of operand keywords. */
 	public static Set<String> keyWords = new HashSet<String>();
 
 	/**
 	 * fills keyWords with key words.
 	 * calls getInstance on all extensions of Instruction.
 	 */
 	static {
 		// add all key words.
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
 
 		// get static instances of all Instruction types.
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
 	public static void initialize() {}
 
 	/**
 	 * Parses a file into a Module.
 	 * 
 	 * @author Noah
 	 * @date Apr 5, 2012; 7:33:45 PM
 	 * @modified Apr 7, 2012; 9:28:15 AM: added line to add instructions w/ labels
 	 *           to symbol table. -Noah<br>
 	 *           Apr 9, 2012; 12:22:16 AM: Assigned lc above newLC - Noah<br>
 	 *           Apr 11, 2012; 2:54:53 PM: Added error handler instance. - Josh <br>
 	 *           Apr 15, 2012; 1:23:08 PM: Moved try-catch block inside loop and added continues,
 	 *           so the loop continues even when an exception is caught. - Noah
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param source
 	 *            The source code for module.
 	 * @param hErr
 	 *            An error handler to which any problems will be reported.
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
 	 * </pre>
 	 * @specRef N/A
 	 */
 	public static final Module parseFile(Scanner source, ErrorHandler hErr) {
 		int lineNum = 0;
 		Module module = new Module();
 		int startAddr = 0;
 		int lc = 0;
 
 		while (source.hasNextLine()) {
 			try{
 				lineNum++;
 	
 				String line = source.nextLine();
 	
 				Instruction instr = Instruction.parse(line);
 				if (instr == null)
 					continue;
 	
 				instr.origSrcLine = line; // Gives instruction source line.
 				instr.lineNum = lineNum;
 	
				//checks for operand errors in instruction.
				instr.check(hErr);
				
 				// Get new lc for next instruction.
 				instr.lc = lc;
 				lc = instr.getNewLC(lc, module);
 	

 				/* if start of module, record startAddr of module.
 				 * execStart of module. */
 				if (instr.getOpId().equalsIgnoreCase("KICKO")) {
 					module.startAddr = startAddr;
 				}
 	
 				//if instr can be used in symbol table.
 				if (instr.usage != Usage.NONE) {
 					module.getSymbolTable().addEntry(instr);
 				}
 	
 				module.assembly.add(instr);
 	
 				module.startAddr += lc;
 			} catch (URBANSyntaxException e) {
 				hErr.reportError(e.getMessage(), lineNum, e.index);
 				if (e.getMessage() == null || e.getMessage().length() <= 5)
 					e.printStackTrace();
 				
 				continue;
 				
 			} catch (IOException e) {
 				if (!e.getMessage().startsWith("RAL"))
 					e.printStackTrace();
 				else
 					System.err.println("Line " + lineNum + " is not terminated by a semicolon.");
 				
 				continue;
 			}
 		}
 
 		return module;
 	}
 
 	/**
 	 * parses a file.
 	 * 
 	 * @author Noah
 	 * @date Apr 9, 2012; 1:12:19 AM
 	 * @modified Apr 11, 2012; 2:54:53 PM: (Josh) Added error handler instance.
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param file
 	 *            contains source code.
 	 * @param hErr
 	 *            An error handler to which any problems will be reported.
 	 * @return @see #:"parseFile(Scanner)"
 	 * @specRef N/A
 	 */
 	public static final Module parseFile(File file, ErrorHandler hErr) {
 		Module module = new Module();
 
 		try {
 			Scanner source = new Scanner(file);
 
 			module = parseFile(source, hErr);
 
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 			hErr.reportError("Failed to open file for parse: file not found.", -1, -1);
 		}
 
 		return module;
 	}
 
 	/**
 	 * parses a string.
 	 * 
 	 * @author Noah
 	 * @date Apr 9, 2012; 1:15:59 AM
 	 * @modified Apr 11, 2012; 2:54:53 PM: (Josh) Added error handler instance.
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param strSrc
 	 *            String contains source code.
 	 * @param hErr
 	 *            An error handler to which any problems will be reported.
 	 * @return @see #"parsefile(Scanner)"
 	 * @specRef N/A
 	 */
 	public static final Module parseString(String strSrc, ErrorHandler hErr) {
 		Scanner source = new Scanner(strSrc);
 		Module module = parseFile(source, hErr);
 		return module;
 	}
 }
