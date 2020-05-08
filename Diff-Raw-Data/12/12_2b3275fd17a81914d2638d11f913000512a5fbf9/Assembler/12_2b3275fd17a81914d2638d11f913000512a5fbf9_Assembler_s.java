 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
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
 	 * @modified Apr 7, 2012; 9:28:15 AM: added line to add instructions w/
 	 *           labels
 	 *           to symbol table. -Noah<br>
 	 *           Apr 9, 2012; 12:22:16 AM: Assigned lc above newLC - Noah<br>
 	 *           Apr 11, 2012; 2:54:53 PM: Added error handler instance. - Josh <br>
 	 *           Apr 15, 2012; 1:23:08 PM: Moved try-catch block inside loop and
 	 *           added continues,
 	 *           so the loop continues even when an exception is caught. - Noah
 	 *           Apr 16, 2012; 10:22:15 PM: Added assignment of program name to
 	 *           module.
 	 *           Apr 17, 2012; 1:43:24 AM: Prevent invalid instruction from
 	 *           being added to symbol table.
 	 *           and moved lc assignment above check. - Noah
 	 *           Apr 17, 2012; 2:00:32 PM: Added second pass for check(). - Josh
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
 		int lineNum = 0, skipls = 0;
 		Module module = new Module();
 		int execStartAddr = 0;
 		int lc = 0;
 		boolean firstKICKO = false, valid = true, hasEnd = false;
 
 		hasNextLineLoop: while (source.hasNextLine()) {
 			try {
 				lineNum += skipls + 1;
 				skipls = 0;
 
				String line = source.nextLine();
 
 				Instruction instr = null;
 				for (;;) {
 					try {
 						instr = Instruction.parse(line);
 						if(instr != null && hasEnd) {
 							instr.errors.add(instr.errors.size(), makeError("OOM")); //add error into list of errors.
 							hErr.reportWarning("LinePastEnd", lineNum, -1);
 						}
 						break;
 					} catch (IOException e) {
 						if (!source.hasNextLine()) {
 							hErr.reportError(makeError("expSemiEOF"), lineNum, 0);
 							break hasNextLineLoop;
 						}
 						line += "\n" + source.nextLine();
 						skipls++;
 					}
 				}
 				if (instr == null) {
 					continue;
 				} 
 					
 
 				instr.origSrcLine = line; // Gives instruction source line.
 				instr.lineNum = lineNum;
 
 				if(instr.getOpId().equalsIgnoreCase("END")) {
 					hasEnd = true;
 				}
 
 				/* if start of module, record execStartAddr of module.*/
 				if (instr.getOpId().equalsIgnoreCase("KICKO") && !firstKICKO) {
 					//instr.immediateCheck(instr.getHErr(hErr), module);
 					module.programName = instr.label;
 					firstKICKO = true;
 				} 
 				
 				if (!firstKICKO) {
 					hErr.reportError(makeError("KICKOlineNum"), lineNum, -1);
 					break;
 				}
 				
 				if(!instr.getOpId().equalsIgnoreCase("EXT")) {
 					instr.lc = lc;
 				} else {
 					instr.lc = 0;
 				}
 
 				// checks for operand errors in instruction.
 				valid = instr.immediateCheck(instr.getHErr(hErr), module);
 				// Get new lc for next instruction.
 				lc = instr.getNewLC(lc, module);
 				
 				//assign execStartAddr.
 				if(instr.getOpId().equalsIgnoreCase("KICKO")) {
 					module.execStartAddr = lc;
 					module.loadAddr = lc;
 				}
 
 
 				if (lc > 4095) {
 					instr.errors.add(instr.errors.size(), makeError("OOM")); //add error into list of errors.
 					hErr.reportError(makeError("OOM"), lineNum, -1);
 				}
 
 				// if instr can be used in symbol table.
 				if (instr.usage != Usage.NONE && valid) {
 					module.getSymbolTable().addEntry(instr, hErr);
 				}
 
 				
 				if(valid) {
 					module.addInstruction(instr, hErr);
 				} else {
 					Instruction temp = USI_NOP.getInstance().getNewInstance();
 					temp.lc = instr.lc;
 					temp.errors = instr.errors;
 					temp.lineNum = instr.lineNum;
 					temp.origSrcLine = instr.origSrcLine;
 					module.addInstruction(temp, hErr);
 					
 				}
 				
 
 			} catch (URBANSyntaxException e) {
 				hErr.reportError(e.getMessage(), lineNum, e.index);
 				if (e.getMessage() == null || e.getMessage().length() <= 5)
 					e.printStackTrace();
 
 				continue;
 
 			}
 		}
 
 		// Pass two
 		for (Instruction i : module.assembly) {
 			valid = i.check(i.getHErr(hErr), module);
 			if(valid) {
 				module.execStartAddr = execStartAddr;
 				i.assembled = i.assemble(); //for now.  should replace all assemble w/ directly changing self's field.
 			} else {
 				i.assembled = USI_NOP.getInstance().assemble();
 				i.operands.clear();
 			}
 		}
 		
 		if(!hasEnd && module.assembly.size() > 0) {
 			hErr.reportWarning(makeError("NoEnd"), lineNum, -1);
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
 			hErr.reportError("Failed to open file for parse: file not found.",
 					-1, -1);
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
