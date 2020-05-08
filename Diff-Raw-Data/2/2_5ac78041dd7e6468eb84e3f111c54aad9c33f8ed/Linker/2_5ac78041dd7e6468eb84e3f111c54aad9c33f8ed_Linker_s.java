 package assemblernator;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.OperandChecker.isValidLiteral;
 import static assemblernator.OperandChecker.isValidMem;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction.ConstantRange;
 
 /**
  * 
  * @author Noah
  * @date May 12, 2012; 1:37:15 PM
  */
 public class Linker {
 	/**
 	 * Returns the loader header record.
 	 * @author Noah
 	 * @date May 13, 2012; 1:22:22 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param prgName name of first module's program name.
 	 * @param loadAddr the load address of the linked programs.
 	 * @param execStart the execution start address of the linked programs.
 	 * @param totalLen the total length of the linked programs.
 	 * @param date the date of assembly.
 	 * @param version the version number of assembler.
 	 * @return a loader file header record.
 	 * @specRef LM1
 	 */
 	public static byte[] LoaderHeader(String prgName, int loadAddr, int execStart, int totalLen, String date, int version) {
 		ByteArrayOutputStream header = new ByteArrayOutputStream();
 		try {
 			header.write((byte)'H'); //LM1.1
 			header.write((byte)':'); //LM1.2
 			header.write(prgName.getBytes()); //LM1.3
 			header.write((byte)':'); //LM1.4
 			header.write(IOFormat.formatIntegerWithRadix(loadAddr, 16, 4)); //LM1.5 
 			header.write((byte)':'); //LM1.6
 			header.write(IOFormat.formatIntegerWithRadix(execStart, 16, 4)); //LM1.7 
 			header.write((byte)':'); //LM1.8
 			header.write(IOFormat.formatIntegerWithRadix(totalLen, 16, 4)); //LM1.9 
 			header.write((byte)':'); //LM1.10
 			header.write(date.getBytes()); //LM1.11
 			header.write((byte)':'); //LM1.12
 			header.write("URBAN-LLM".getBytes()); //LM1.13
 			header.write((byte)':'); //LM1.14
 			header.write(IOFormat.formatIntegerWithRadix(version, 16, 4)); //LM1.15
 			header.write((byte)':'); //LM1.16
 			header.write(prgName.getBytes()); //LM1.17
 			header.write((byte)':'); //LM1.18
 		} catch(IOException e) {
 			e.printStackTrace();
 			return ":Something wicked has happened:".getBytes();
 		}
 		return header.toByteArray();
 	}
 	
 	/**
 	 * Returns a text record for a single instruction.
 	 * @author Noah
 	 * @date May 13, 2012; 1:43:16 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param addr address of instruction
 	 * @param code the assembled code of instruction adjusted by linker.
 	 * @param prgName program name.
 	 * @return a text record.
 	 * @specRef N/A
 	 */
 	public static byte[] LoaderText(int addr, int code, String prgName) {
 		ByteArrayOutputStream text = new ByteArrayOutputStream();
 		try {
 			text.write((byte)'T'); //LM2.1
 			text.write((byte)':'); //LM2.2
 			text.write(IOFormat.formatIntegerWithRadix(addr, 16, 4)); //LM2.3 
 			text.write((byte)':'); //LM2.4
 			text.write(IOFormat.formatIntegerWithRadix(code, 16, 8)); //LM2.5 
 			text.write((byte)':'); //LM2.6
 			text.write(prgName.getBytes()); //LM2.7
 			text.write((byte)':'); //LM2.8
 		} catch(IOException e) {
 			e.printStackTrace();
 			return ":Something wicked has happened:".getBytes();
 		}
 		
 		return text.toByteArray();
 	}
 	
 	/**
 	 * Returns an end record.
 	 * @author Noah
 	 * @date May 13, 2012; 1:44:04 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param totalRecords total number of records in the program.
 	 * @param totalTextRecords total number of text records in the program.
 	 * @param prgName the name of the program.
 	 * @return an end record.
 	 * @specRef LM5
 	 */
 	public static byte[] LoaderEnd(int totalRecords, int totalTextRecords, String prgName) {
 		ByteArrayOutputStream end = new ByteArrayOutputStream();
 		try {
 			end.write((byte)'E'); //LM5.1
 			end.write((byte)':'); //LM5.2
 			end.write(IOFormat.formatIntegerWithRadix(totalRecords, 16, 4)); //LM5.3
 			end.write((byte)':'); //LM5.4
 			end.write(IOFormat.formatIntegerWithRadix(totalTextRecords, 16, 4)); //LM5.5 
 			end.write((byte)':'); //LM5.6
 			end.write(prgName.getBytes()); //LM5.7
 			end.write((byte)':'); //LM5.8
 		} catch(IOException e) {
 			e.printStackTrace();
 			return ":Something wicked has happened:".getBytes();
 		}
 		return end.toByteArray();
 	}
 	
 	/**
 	 * Wraps link(LinkerModule, OutputStream, ErrorHandler).
 	 * Takes filename of file to output to, and an array of LinkerModules and outputs a loader file.
 	 * the load file.
 	 * Requires that all modules were created from valid object files.
 	 * @author Noah
 	 * @date May 18, 2012; 11:11:32 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param modules an array of LinkerModules.
 	 * @param filename name of file to output to.
 	 * @param hErr error handler
 	 * @specRef N/A
 	 */
 	public static void link(LinkerModule[] modules, String filename, ErrorHandler hErr) {
 		try {
 			OutputStream out = new FileOutputStream(filename);
 			link(modules, out, hErr);
 		} catch (FileNotFoundException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		}
 	}
 	/**
 	 * Takes an array of LinkerModules and outputs a loader file.
 	 * @author Noah
 	 * @date May 12, 2012; 3:44:18 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param modules an array of LinkerModules.
 	 * @param out output stream to output load file.
 	 * @param hErr error handler.
 	 * @specRef N/A
 	 */
 	public static void link(LinkerModule[] modules, OutputStream out, ErrorHandler hErr) {
 		Map<String, Integer> linkerTable = new HashMap<String, Integer>();
 		boolean isValid = true;
 		//sort the modules by order of address of modules.
 		Arrays.sort(modules);
 		
 		if(modules.length > 0) {
 			int totalLen = modules[0].prgTotalLen;
 			int totalRecords = 2;
 			int totalTextRecords = 0;
 			int execStartAddr = modules[0].execStart;
 			int offset = 0;
 			modules[0].offset = offset;
 			linkerTable.putAll(modules[0].linkRecord);// put all link records from first module.
 			//calc offset and adjust prog, linker record addr, and text record addr by offset.
 			//add LinkerModule with adjusted addresses to offsetModules.
 			for(int i = 0; i < modules.length - 1; ++i) {
 				if(modules[i+1].loadAddr <= modules[i].loadAddr) {
 					//calc offset
 					offset = ((modules[i].loadAddr + modules[i].prgTotalLen) - modules[i+1].loadAddr);
 					modules[i+1].loadAddr += offset;
 					//error if module loadAddr + offset > max addr
 					if(modules[i+1].loadAddr > 4095) {
 						hErr.reportError(makeError("OOM"), -1, -1);
 						break;
 					}
 					totalLen += modules[i+1].prgTotalLen;
 					if(modules[i+1].execStart > execStartAddr) {
 						execStartAddr = modules[i+1].execStart;
 					}
 					//put all linker records of current module into linker table with offset.
 					for(Map.Entry<String, Integer> lr : modules[i+1].linkRecord.entrySet()) {
 						if(!linkerTable.containsKey(lr.getKey())) {
 							linkerTable.put(lr.getKey(), lr.getValue() + offset);
 						} else {
 							hErr.reportError(makeError("dupLbl"), -1, -1);
 						}
 					}
 				}
 				
 				modules[i+1].offset = offset;
 			}
 			try {
 				//write header record.
 				out.write(LoaderHeader(modules[0].progName, modules[0].loadAddr, execStartAddr, totalLen, modules[0].date, modules[0].version));
 				System.err.println("Mod len: " + modules.length);
 				//iterate through all linker modules.
 				for(LinkerModule offMod : modules) {
 					//iterate through entries in text and mod record map of a linker module.
					for(LinkerModule.textModRecord textMod
 							: offMod.textMod) {
 						textMod.text.assignedLC += offMod.offset; //offset text lc.
 						//if both high and low flags are 'A', no adjustments is necessary.
 						if(!(textMod.text.flagHigh == 'A' && textMod.text.flagLow == 'A')) {
 							char litBit = textMod.text.instrData.charAt(7);
 							char formatBit = textMod.text.instrData.charAt(6);
 							int opcode = IOFormat.parseHex32Int(textMod.text.instrData); //the assembled code.
 							int mem;
 							int adjustVal = 0;
 							final int memMaskLow = 0x00000FFF;
 							final int memMaskHigh = 0x00FFF000;
 							final int litMaskLow = 0x0000FFFF;
 							final int memMaskHighLow = 0x00FFFFFF;
 							final int iniHighMemBit = 0x1000;
 							int mask;
 							
 							//adjust mem of instruction data by offset if relocatable.
 							if(textMod.text.flagHigh == 'R' || textMod.text.flagLow == 'R') {
 								int highMem = 0;
 								int lowMem = 0;
 								
 								if(textMod.text.flagHigh == 'R' && textMod.text.flagLow == 'R') {
 									highMem = (opcode & memMaskHigh) + (offMod.offset * iniHighMemBit); //get high mem bits and adjust by offset.
 									lowMem = (opcode & memMaskLow) + offMod.offset; //get low mem bits and adjust by offset.
 									opcode &= (~memMaskHighLow); //zero out mem bits of opcode.
 								} else if(textMod.text.flagHigh == 'R') {
 									highMem = (opcode & memMaskHigh) + (offMod.offset * iniHighMemBit); //get high mem bits and adjust by offset.
 									opcode &= (~memMaskHigh); //zero out mem bits of opcode.
 								} else if(litBit == '0'){
 									lowMem = (opcode & memMaskLow) + offMod.offset; //get low mem bits and adjust by offset.
 									opcode &= (~memMaskLow); //zero out mem bits of opcode.
 								} else {
 									lowMem = (opcode & litMaskLow) + offMod.offset; //get low lit bits and adjust by offset.
 									opcode &= (~litMaskLow); //zero out lit bits of opcode.
 								}
 								
 								
 								if(formatBit == '1' && litBit == '1') { 
 									System.err.println(highMem);
 									System.err.println(lowMem);
 									isValid = (isValidLiteral(highMem/iniHighMemBit, ConstantRange.RANGE_13_TC) && isValidMem(lowMem));
 									if(!isValid) hErr.reportError(makeError("lnkOORAddr"), -1, -1);
 								} else if(litBit == '1') {
 									isValid = isValidLiteral(lowMem, ConstantRange.RANGE_16_TC);
 									if(!isValid) hErr.reportError(makeError("lnkOORLit16"), -1, -1);
 								} else {
 									isValid = (isValidMem(highMem/iniHighMemBit) && isValidMem(lowMem));
 									if(!isValid) hErr.reportError(makeError("lnkOORAddr"), -1, -1);
 								}
 								
 								
 								opcode |= highMem;
 								opcode |= lowMem;
 								
 								textMod.text.instrData = IOFormat.formatHexInteger(opcode, 8);
 							}
 							
 							//iterate through mod records mapped to text.
 							for(LinkerModule.ModRecord mRec : textMod.mods) {
 								//iterate through contents of mod record.
 								for(LinkerModule.MiddleMod midMod : mRec.midMod) {
 									if(midMod.flagRE == 'E') {
 										if(linkerTable.containsKey(midMod.linkerLabel)) {
 											adjustVal = linkerTable.get(midMod.linkerLabel);
 										} else {
 											isValid = false;
 											hErr.reportError(makeError("noLbl"), -1, -1);
 											continue;
 										}
 									} else { //'R'
 										adjustVal = offMod.offset;
 									}
 	
 									if((mRec.HLS == 'S' && litBit == '0') || (mRec.HLS == 'L')) {
 										mask = 0x00000FFF;
 									} else if(mRec.HLS == 'S') {
 										mask = 0x0000FFFF;
 									} else { //'H's
 										mask = 0x00FFF000;
 										adjustVal = offMod.offset * iniHighMemBit; //can't just add because high mem bits don't start at lowest bit.
 									}
 									
 									mem = opcode & mask; //unaltered opcode & mask to get mem bits.
 									opcode &= (~mask); //zero out membits.
 									
 									//adjust mem
 									if(midMod.plusMin == '+') {
 										mem += adjustVal; 
 									} else {
 										mem -= adjustVal;
 									}
 									
 									if(litBit == '1' && formatBit == '1') {
 										isValid = isValidLiteral(mem/iniHighMemBit, ConstantRange.RANGE_13_TC);
 										if(!isValid) hErr.reportError(makeError("lnkOORLit13"), -1, -1);
 									} else if(litBit == '1') {
 										isValid = isValidLiteral(mem, ConstantRange.RANGE_16_TC);
 										if(!isValid) hErr.reportError(makeError("lnkOORLit16"), -1, -1);
 									} else if(formatBit == '1') {
 										isValid = isValidMem(mem/iniHighMemBit);
 										if(!isValid) hErr.reportError(makeError("lnkOORAddr"), -1, -1);
 									} else {
 										isValid = isValidMem(mem);
 										if(!isValid) hErr.reportError(makeError("lnkOORAddr"), -1, -1);
 									}
 									
 									if(isValid) {
 										opcode |= mem; //fill in mem bits.
 										textMod.text.instrData = IOFormat.formatHexInteger(opcode, 8);
 									}
 								}
 							}
 						}
 						//write text records.
 						if(isValid) {
 							out.write(LoaderText(textMod.text.assignedLC, IOFormat.parseHex32Int(textMod.text.instrData), modules[0].progName));
 							++totalTextRecords;
 						}
 					}
 					
 				}
 				//write end record
 				totalRecords += totalTextRecords;
 				out.write(LoaderEnd(totalRecords, totalTextRecords, modules[0].progName));
 				
 			} catch (IOException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			}
 			
 		}
 	}
 	
 	/**
 	 * Given fileNames, constructs an array of LinkerModules from the object files with
 	 * the names in fileNames.
 	 * @author Noah
 	 * @date May 12, 2012; 4:05:35 PM
 	 * @modified UNMODIFIED
 	 * @tested UNTESTED
 	 * @errors NO ERRORS REPORTED
 	 * @codingStandards Awaiting signature
 	 * @testingStandards Awaiting signature
 	 * @param fileNames file names of object files to read from.
 	 * @param hErr error handler.
 	 * @return an array of LinkerModules representing the object files.
 	 * @specRef N/A
 	 */
 	public static LinkerModule[] getModules(String[] fileNames, ErrorHandler hErr) {
 		List<LinkerModule> modules = new ArrayList<LinkerModule>(); 
 		
 		for(int fileIndex = 0; fileIndex < fileNames.length; ++fileIndex) {
 			try {
 				InputStream in = new BufferedInputStream(new FileInputStream(fileNames[fileIndex]));
 				for(int i = 0; i < fileNames.length; ++i) {
 					boolean hasNext = true;
 					LinkerModule temp;
 					while(hasNext) { //while there are modules left in the object file.s
 						temp = new LinkerModule(in, hErr);
 						if(temp.success) { //add to list of linker modules only if object file is valid.
 							modules.add(temp);
 						}
 						hasNext = !temp.done; 
 					}
 				}
 			} catch(FileNotFoundException e) {
 				System.err.println(e.getMessage());
 				e.printStackTrace();
 			}
 		}
 		
 		return modules.toArray(new LinkerModule[modules.size()]);
 	}
 }
