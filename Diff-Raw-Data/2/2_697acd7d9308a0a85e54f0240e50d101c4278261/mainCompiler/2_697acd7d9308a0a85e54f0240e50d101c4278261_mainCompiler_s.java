 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.regex.*;
 import java.lang.String;
 
 /**
  * 
  * @author Troxel
  */
 public class mainCompiler {
 
 	/**
 	 * @param args
 	 *            the command line arguments
 	 */
 	public static void main(String[] args) {
 		// TODO code application logic here
 		String labelRegex = "^[a-zA-Z_]+:";
 		Pattern label = Pattern.compile(labelRegex);
 		Pattern register = Pattern.compile("^\\$[0-9]?[0-9]");
 		HashMap labelLoc = new HashMap();
 		int instCount = 0;
 		int byteLine = 0;
 		int lastByte = 0;
 		int maxMem = 0;
 		ArrayList<int[]> memplacement = new ArrayList<int[]>();
 		String filename = "";
 		try {
 			filename = args[0];
 		} catch (Exception e) {
 			Scanner consolein = new Scanner(System.in);
 			System.out
 					.println("Give the path or filename of source code to compile:");
 			filename = consolein.next();
 			consolein.close();
 		}
 		boolean data = false;
 		File infile = new File(filename);
 		String[] tempParams = infile.getName().split("\\.(?=[^\\.]+$)");
 		String temp = tempParams[0];
 		File exeFile = new File(temp + ".exe.mif");
 		File memFile = new File(temp + ".mem.mif");
 		// Create file if it doesn't already exist
 		try {
 			exeFile.createNewFile();
 			Scanner input = new Scanner(infile);
 			FileWriter fw;
 			FileWriter memfw;
 			try {
 				String instruction = "";
 				int code = 0;
 				ArrayList<String> instructionLines = new ArrayList<String>();
 				ArrayList<String> origInstruction = new ArrayList<String>();
 				ArrayList<String[]> Symbols = new ArrayList<String[]>();
 				String parameters = "";
 				String[] params;
 				int index;
 				int lineNum = 0;
 
 				// initialize memory file writers
 				memfw = new FileWriter(memFile);
 				PrintWriter mempw = new PrintWriter(memfw);
 				mempw.print("WIDTH = 32; \r\n-- data width \r\n"
 						+ "DEPTH = 256; \r\n-- number of memory slots \r\n"
 						+ "-- thus, the total size is 256 x 32 bits\r\n"
 						+ "ADDRESS_RADIX = HEX; \r\nDATA_RADIX = HEX; \r\n"
						+ "CONTENT BEGIN \r\nEND;\r\n");
 
 				// initialize executable file writers
 				fw = new FileWriter(exeFile);
 				PrintWriter pw = new PrintWriter(fw);
 				pw.print("WIDTH = 32; \r\n-- data width \r\n"
 						+ "DEPTH = 256; \r\n-- number of memory slots \r\n"
 						+ "-- thus, the total size is 256 x 32 bits\r\n"
 						+ "ADDRESS_RADIX = HEX; \r\nDATA_RADIX = HEX; \r\n"
 						+ "CONTENT BEGIN \r\n");
 
 				code = 0;
 
 				// start reading the file
 				while (input.hasNextLine()) {
 					params = input.nextLine().split("#");
 					if (params.length > 0) {
 						instruction = params[0].trim();
 					} else {
 						instruction = "";
 					}
 					if (instruction.contains(".data")) {
 						data = true;
 						lineNum++;
 						instCount++;
 						byteLine = 0;
 					}
 					if (!data) {
 						if (instruction.contains(":")) {
 							params = instruction.split(":");
 							index = ContainsSymbol(Symbols, params[0].trim());
 							if (index > -1) {
 								if (Symbols.get(index)[1].contains("-1")) {
 									Symbols.set(
 											index,
 											new String[] { params[0].trim(),
 													String.valueOf(lineNum * 4) });
 								} else {
 									System.out.println("Symbol error (Line: "
 											+ lineNum
 											+ "): Symbol already exists.");
 								}
 							} else {
 								Symbols.add(new String[] { params[0],
 										String.valueOf(lineNum * 4) });
 							}
 							if (params.length > 1) {
 								instruction = params[1].trim();
 							} else {
 								instruction = "";
 							}
 						}
 						if (instruction.length() > 0) {
 							instructionLines.add(instruction);
 							origInstruction.add(instruction);
 							parameters = instruction.split(" ", 2)[1].trim();
 							params = parameters.split(",");
 							for (int i = 0; i < params.length; i++) {
 								if (!(params[i].trim().startsWith("$"))
 										&& !isNumber(params[i].trim())) {
 									index = ContainsSymbol(Symbols,
 											params[i].trim());
 									if (index > -1 && index < Symbols.size()) {
 										if (!Symbols.get(index)[1]
 												.contains("-1")) {
 											instructionLines
 													.set(lineNum,
 															instructionLines
 																	.get(lineNum)
 																	.replace(
 																			Symbols.get(index)[0],
 																			Symbols.get(index)[1]));
 										}
 									} else {
 										Symbols.add(new String[] {
 												params[i].trim(), "-1" });
 									}
 								}
 							}
 							lineNum++;
 							instCount++;
 						}
 					} else {
 						if (instruction.contains(":")) {
 							params = instruction.split(":");
 							index = ContainsSymbol(Symbols, params[0].trim());
 							if (index > -1) {
 								if (Symbols.get(index)[1].contains("-1")) {
 									Symbols.set(index,
 											new String[] { params[0].trim(),
 													String.valueOf(byteLine) });
 								} else {
 									System.out.println("Symbol error (Line: "
 											+ lineNum
 											+ "): Symbol already exists.");
 								}
 							} else {
 								Symbols.add(new String[] { params[0],
 										String.valueOf(byteLine) });
 							}
 							if (params.length > 1) {
 								instruction = params[1].trim();
 							} else {
 								instruction = "";
 							}
 						}
 						if (instruction.length() > 0) {
 							params = instruction.split(" ", 2);
 							switch (params[0]) {
 							case ".ascii":
 								temp = params[0].replaceAll("\"", "");
 								for (int i = 0; i < temp.length(); i++) {
 									code |= ((byte) temp.charAt(i)) << ((3 - byteLine % 4) * 8);
 									byteLine++;
 									if (byteLine % 4 == 0) {
 										memplacement.add(new int[] {
 												byteLine - 4, code });
 										code = 0;
 										lastByte = byteLine - 4;
 									}
 								}
 								break;
 							case ".byte":
 								code |= Integer.valueOf(params[1].trim())
 										.intValue() << ((3 - byteLine % 4) * 8);
 								byteLine++;
 								if (byteLine % 4 == 0) {
 									memplacement.add(new int[] { byteLine - 4,
 											code });
 									code = 0;
 									lastByte = byteLine - 4;
 								}
 								break;
 							case ".asciiz":
 								temp = params[1].replaceAll("\"", "")
 										+ ((char) (0));
 								for (int i = 0; i < temp.length(); i++) {
 									code |= ((byte) temp.charAt(i)) << ((3 - byteLine % 4) * 8);
 									byteLine++;
 									if (byteLine % 4 == 0) {
 										memplacement.add(new int[] {
 												byteLine - 4, code });
 										code = 0;
 										lastByte = byteLine - 4;
 									}
 								}
 								break;
 							case "org":
 								memplacement.add(new int[] {
 										byteLine - byteLine % 4, code });
 								code = 0;
 								byteLine = Integer.valueOf(params[1].trim())
 										.intValue();
 								break;
 							default:
 							}
 						}
 					}
 				}
 				if (byteLine % 4 != 0 && byteLine - 4 != lastByte) {
 					memplacement.add(new int[] { byteLine - (byteLine % 4), code });
 					code = 0;
 				}
 				for (int i = 0; i <= 256; i = i + 4) {
 					mempw.printf("%02x : %08x; -- %s\r\n", i / 4,
 							GetWord(memplacement, i), i);
 				}
 				for (lineNum = 0; lineNum < instructionLines.size(); lineNum++) {
 					instruction = instructionLines.get(lineNum);
 					System.out.println(origInstruction.get(lineNum));
 					parameters = instruction.split(" ", 2)[1].trim();
 					instruction = instruction.split(" ", 2)[0].trim();
 					params = parameters.split(",");
 					for (int i = 0; i < params.length; i++) {
 						if (!(params[i].trim().startsWith("$"))
 								&& !isNumber(params[i].trim())) {
 							if (params[i].contains("(")) {
 								index = ContainsSymbol(Symbols, params[i]
 										.trim().split("\\(")[0]);
 								if (index > -1 && index < Symbols.size()) {
 									if (!Symbols.get(index)[1].contains("-1")) {
 										params[i] = params[i].replace(
 												Symbols.get(index)[0],
 												Symbols.get(index)[1]);
 									}
 								} else {
 									System.out.println("ERROR Symbol "
 											+ params[i].trim()
 											+ " does not exist.");
 								}
 								if (!(params[i].trim().split("\\(")[1]
 										.startsWith("$"))
 										&& !isNumber(params[i].trim().split(
 												"\\(")[1].replace(")", ""))) {
 									index = ContainsSymbol(Symbols, params[i]
 											.trim().split("\\(")[1].replace(
 											")", ""));
 								}
 							} else {
 								index = ContainsSymbol(Symbols,
 										params[i].trim());
 							}
 							if (index > -1 && index < Symbols.size()) {
 								if (!Symbols.get(index)[1].contains("-1")) {
 									params[i] = params[i].replace(
 											Symbols.get(index)[0],
 											Symbols.get(index)[1]);
 								}
 							} else {
 								System.out
 										.println("ERROR Symbol "
 												+ params[i].trim()
 												+ " does not exist.");
 							}
 						}
 					}
 					try {
 						code = 0;
 						switch (instruction) {
 						// R-Type
 						case "add":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x20;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "and":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x24;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "jalr":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= 0x09;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "jr":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 21;
 							code |= 0x08;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "nor":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x27;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "or":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x25;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "sll":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 6;
 							code |= 0x25;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "sllv":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 21;
 							code |= 0x04;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "slt":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x2A;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "srl":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(params[2].trim())
 									.intValue() << 5;
 							code |= 0x02;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "sub":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x22;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "xor":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 11;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[2].replace("$", "").trim())
 									.intValue() << 16;
 							code |= 0x25;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						// I-Type
 						case "addi":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(params[2].trim())
 									.intValue();
 							code |= 8 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "andi":
 							code = Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(params[2].trim())
 									.intValue();
 							code |= 12 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "beq":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(params[2].trim())
 									.intValue() - lineNum * 4;
 							code |= 4 << 26;
 							// origInstruction = origInstruction.split(" ")[0] +
 							// origInstruction.split(" ")[1] +
 							// origInstruction.split(" ")[2] +
 							// (labelLoc.get(origInstruction.split(" ")[3])).toString();
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "bne":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(params[2].trim())
 									.intValue() - lineNum * 4;
 							code |= 0x05 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "lb":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(params[1].trim())
 									.intValue();
 							code |= 0x20 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "lh":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(params[1].trim())
 									.intValue();
 							code |= 0x21 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "lui":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(params[1].trim())
 									.intValue();
 							code |= 0x0F << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "li":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= (byte) (Integer.valueOf(params[1].trim())
 									.intValue() & 0xFF);
 							code |= 0x0F << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							lineNum++;
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 21;
 							code |= (byte) ((Integer.valueOf(params[1].trim())
 									.intValue() >> 8) & 0xFF);
 							code |= 0x0D << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "lw":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[1].trim().split("\\(")[0])
 									.intValue();
 							code |= Integer.valueOf(
 									params[1].trim().split("\\(")[1].replace(
 											")", "").replace("$", ""))
 									.intValue() << 21;
 							code |= 0x23 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "ori":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[1].replace("$", "").trim())
 									.intValue() << 21;
 							code |= Integer.valueOf(params[2].trim())
 									.intValue();
 							code |= 0x0D << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "sw":
 							code |= Integer.valueOf(
 									params[0].replace("$", "").trim())
 									.intValue() << 16;
 							code |= Integer.valueOf(
 									params[1].trim().split("\\(")[0])
 									.intValue();
 							code |= Integer.valueOf(
 									params[1].trim().split("\\(")[1].replace(
 											")", "").replace("$", ""))
 									.intValue() << 21;
 							code |= 0x2B << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						// J-Type
 						case "j":
 							code = Integer.valueOf(params[0].trim()).intValue() << 2;
 							code |= 2 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						case "jal":
 							code = Integer.valueOf(params[0].trim()).intValue() << 2;
 							code |= 3 << 26;
 							pw.printf("%02x : %08x; -- %s\r\n", lineNum, code,
 									origInstruction.get(lineNum));
 							break;
 						default:
 							if (!(instruction.startsWith("#"))) {
 								System.out.println("Error reading instruction");
 							}
 						}
 					} catch (Exception e) {
 						e.printStackTrace();
 						System.out.println("Syntax Error Line " + lineNum);
 					}
 				}
 
 				// Finish the files
 				pw.print("END;\r\n");
 				mempw.print("END;\r\n");
 
 				// close the writers
 				pw.close();
 				mempw.close();
 				try {
 					memfw.close();
 					fw.close();
 				} catch (IOException e) {
 					System.out.println("Could not Close FileWriter");
 				}
 			} catch (IOException e) {
 				System.out.println("Could not open FileWriter");
 				fw = null;
 			}
 
 		} catch (IOException e) {
 			System.out.println("File couldn't be created");
 		}
 	}
 
 	private static int ContainsSymbol(ArrayList<String[]> symbols, String target) {
 		for (int i = 0; i < symbols.size(); i++) {
 			if (symbols.get(i)[0].contains(target)) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	private static int GetWord(ArrayList<int[]> codes, int target) {
 		for (int i = 0; i < codes.size(); i++) {
 			if (codes.get(i)[0] == target) {
 				return codes.get(i)[1];
 			}
 		}
 		return 0;
 	}
 
 	public static boolean isNumber(String sCheck) {
 		try {
 			double num = Double.parseDouble(sCheck);
 		} catch (NumberFormatException nfe) {
 			return false;
 		}
 		return true;
 	}
 
 }
