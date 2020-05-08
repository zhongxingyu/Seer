 /*
  * Copyright (c) Anton Kraievoy, IASA, Kiev, Ukraine, 2006.
  * This work is based on code of Dr. Dalton R. Hunkins, CS dept. of St. Bonaventure University, 2006.
  */
 package elw.dp.mips.asm;
 
 import base.pattern.Result;
 import elw.dp.mips.*;
 import gnu.trove.TIntIntHashMap;
 import org.akraievoy.gear.G;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class MipsAssembler {
 	private static final Logger log = LoggerFactory.getLogger(MipsAssembler.class);
 
 	public static final Pattern PATTERN_LABEL = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
 
 	protected final static Map<Pattern, String> codeNormMap = createCodeNormMap();
 
 	protected static Map<Pattern, String> createCodeNormMap() {
 		final Map<Pattern, String> map = new LinkedHashMap<Pattern, String>();
 
 		map.put(Pattern.compile("\\s+"), " ");
 		map.put(Pattern.compile("^ "), "");
 		map.put(Pattern.compile("\" $\""), "");
 		map.put(Pattern.compile(" ?, ?"), ",");
 		map.put(Pattern.compile(" ?: ?"), ":");
 		map.put(Pattern.compile(" ?\\( ?"), "(");
 		map.put(Pattern.compile(" ?\\) ?"), ")");
 
 		return map;
 	}
 
 	public Instruction[] loadInstructions(String[] codeLines, Result[] resRef, final Map<String, Integer> labelIndex) {
 		labelIndex.clear();
 		final List<Instruction> instructions = new ArrayList<Instruction>();
 
 		try {
 			if (loadInstructionsFirstPass(codeLines, instructions, labelIndex, resRef)) {
 				return null;
 			}
 
 			for (Instruction instruction : instructions) {
 				final String prefix = "Code(line " + instruction.getLineIndex() + "): ";
 
 				if (instruction.resolve(0, labelIndex)) {
 					Result.failure(log, resRef, prefix + "missing label '" + instruction.getAddr() + "'");
 					return null;
 				}
 				if (!instruction.isAssembled()) {
 					Result.failure(log, resRef, prefix + "instruction assembly incomplete: " + instruction);
 					return null;
 				}
 			}
 		} catch (Exception e) {
 			Result.failure(log, resRef, "failed: " + e.getMessage());
 			log.warn("failed:", e);
 			return null;
 		}
 
 		Result.success(log, resRef, "Instructions Assembled");
 		return instructions.toArray(new Instruction[instructions.size()]);
 	}
 
 	protected boolean loadInstructionsFirstPass(String[] codeLines, List<Instruction> instructions, Map<String, Integer> labelIndex, Result[] resRef) {
 		final StringBuilder code = new StringBuilder();
 		final StringBuilder syntax = new StringBuilder();
 		final List<String> labels = new ArrayList<String>();
 		int instructionIndex = 0;
 		for (int lineIndex = 0, codeLinesLength = codeLines.length; lineIndex < codeLinesLength; lineIndex++) {
 			final String prefix = "Code(line " + (lineIndex + 1) + "): ";
 			final String codeLine = codeLines[lineIndex].trim();
 
 			if (codeLine.startsWith("#") || codeLine.isEmpty()) {
 				continue;
 			}
 
 			loadNormed(codeLine, code);
 			final String[] labelList;
 			final int labelsEnd = code.indexOf(":");
 			if (labelsEnd >= 0) {
 				labelList = code.substring(0, labelsEnd).split(",");
 				code.delete(0, labelsEnd + 1);
 			} else {
 				labelList = G.STRINGS_EMPTY;
 			}
 
 			if (labelList.length > 0) {
 				for (String label : labelList) {
 					if (!PATTERN_LABEL.matcher(label).matches()) {
 						Result.failure(log, resRef, prefix + "wrong label syntax '" + label + "'");
 						return true;
 					}
 					if (labelIndex.get(label) != null) {
 						Result.failure(log, resRef, prefix + "ambiguous label '" + label + "'");
 						return true;
 					}
 					labelIndex.put(label, instructionIndex);
 				}
 				labels.addAll(Arrays.asList(labelList));
 			}
 
 			if (code.length() > 0) {
 				final String opName = removeOpName(code);
 
 				final Method aluMethod;
 				try {
 					aluMethod = Alu.class.getDeclaredMethod(opName, InstructionContext.class);
 				} catch (NoSuchMethodException e) {
 					Result.failure(log, resRef, prefix + "unspecified operation '" + opName + "'");
 					return true;
 				}
 
 				final InstructionDesc desc = aluMethod.getAnnotation(InstructionDesc.class);
 				loadNormed(desc.syntax(), syntax);
 				removeOpName(syntax);	//	it must be the same as code stated above
 
 				Instruction inst = new Instruction(
 						desc, codeLine, instructionIndex, lineIndex + 1,
 						labels.toArray(new String[labels.size()])
 				);
 
 				int argIndex = 1;
 				int syntaxLen;
 				while ((syntaxLen = syntax.length()) > 0) {
 					trim(syntax);
 					trim(code);
 
 					final String prefixBefore = prefix + "before arg. " + argIndex + " ";
 					if (checkSeparator(',', syntax, code, resRef, prefixBefore) ||
 							checkSeparator('(', syntax, code, resRef, prefixBefore) ||
 							checkSeparator(')', syntax, code, resRef, prefixBefore)) {
 						return true;
 					}
 
 					final String prefixOn = prefix + "arg. " + argIndex + " ";
 					if (parseReg(Instruction.T_REG_D, syntax, code, desc, inst, resRef, prefixOn) ||
 							parseReg(Instruction.T_REG_T, syntax, code, desc, inst, resRef, prefixOn) ||
 							parseReg(Instruction.T_REG_S, syntax, code, desc, inst, resRef, prefixOn)) {
 						return true;
 					}
 
 					if (parseNum(Instruction.T_IMM16, syntax, code, inst, resRef, prefixOn) ||
 							parseNum(Instruction.T_H5, syntax, code, inst, resRef, prefixOn)) {
 						return true;
 					}
 
 					if (parseAddr(Instruction.T_ADDR16, Instruction.T_IMM16, syntax, code, inst, resRef, prefixOn) ||
 							parseAddr(Instruction.T_ADDR26, Instruction.T_IMM26, syntax, code, inst, resRef, prefixOn)) {
 						return true;
 					}
 
 					if (syntaxLen == syntax.length()) {
 						//	unable to cut any of tokens
 						throw new IllegalStateException("syntax broken: " + desc.syntax() + " -> " + syntax.toString());
 					}
 
 					argIndex++;
 				}
 				if (code.length() > 0) {
 					Result.failure(log, resRef, prefix + "redundant code '" + code.toString() + "'");
 					return true;
 				}
 
 				instructions.add(inst);
 
 				labels.clear();
 				instructionIndex++;
 			}
 		}
 		return false;
 	}
 
 	protected static void trim(StringBuilder syntax) {
 		while (syntax.length() > 0 && syntax.charAt(0) == ' ') {
 			syntax.deleteCharAt(0);
 		}
 	}
 
 	protected static boolean parseAddr(final String id, final String numId, StringBuilder syntax, StringBuilder code, Instruction inst, Result[] resRef, String prefixOn) {
 		if (syntax.indexOf(id) == 0) {
 			final String token = scanChunk(code, ",()");
 			syntax.delete(0, id.length());
 
 			if (PATTERN_LABEL.matcher(token).matches()) {
 				inst.setAddr(id, token);
 				return false;
 			}
 
 			final int bits = Instruction.getWidth(id) + 2;
 			if (!Data.isNum(token, bits)) {
 				Result.failure(log, resRef, prefixOn + "must be a " + bits + "-bit number");
 				return true;
 			}
 
 			final int num = (int) Data.parse(token);
 			inst.setBits(numId, num >> 4);
 		}
 
 		return false;
 	}
 
 	protected static boolean parseNum(final String id, StringBuilder syntax, StringBuilder code, Instruction inst, Result[] resRef, String prefixOn) {
 		if (syntax.indexOf(id) == 0) {
 			final String numToken = scanChunk(code, ",()");
 			syntax.delete(0, id.length());
 
 			final int bits = Instruction.getWidth(id);
 			if (!Data.isNum(numToken, bits)) {
 				Result.failure(log, resRef, prefixOn + "must be a " + bits + "-bit number");
 				return true;
 			}
 
 			final int num = (int) Data.parse(numToken);
 			inst.setBits(id, num);
 		}
 
 		return false;
 	}
 
 	protected static boolean parseReg(final String regId, StringBuilder syntax, StringBuilder code, InstructionDesc desc, Instruction inst, Result[] resRef, String prefixOn) {
 		if (syntax.indexOf(regId) == 0) {
 			final String regToken = scanChunk(code, ",()");
 			syntax.delete(0, regId.length());
 			final Reg reg = parseReg(regToken, log, resRef, prefixOn);
 			if (!G.contains(Reg.publicRegs, reg)) {
 				Result.failure(log, resRef, prefixOn + "direct ref to $" + reg.toString());
 				return true;
 			}
 			if (desc.writeRegs().indexOf(regId) >= 0 && G.contains(Reg.roRegs, reg)) {
 				Result.failure(log, resRef, prefixOn + "write to $" + reg.toString());
 				return true;
 			}
 
 			inst.setReg(regId, reg);
 		}
 
 		return false;
 	}
 
 	protected static String scanChunk(StringBuilder code, final String term) {
 		for (int l = 0; l < code.length(); l++) {
 			if (term.indexOf(code.charAt(l)) >= 0) {
 				final String chunk = code.substring(0, l);
 				code.delete(0, l);
 				return chunk;
 			}
 		}
 
 		final String chunk = code.toString();
 		code.setLength(0);
 		return chunk;
 	}
 
 	protected static boolean checkSeparator(final char sep, StringBuilder syntax, StringBuilder code, Result[] resRef, String prefix) {
 		if (syntax.charAt(0) == sep) {
 			if (code.length() > 0 && code.charAt(0) == sep) {
 				syntax.delete(0, 1);
 				code.delete(0, 1);
 			} else {
 				Result.failure(log, resRef, prefix + "expecting '" + sep + "'");
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	protected static void loadNormed(String codeLine, StringBuilder code) {
 		code.setLength(0);
 		code.insert(0, codeLine.toLowerCase());
 
 		for (Pattern pat : codeNormMap.keySet()) {
 			final String replace = pat.matcher(code).replaceAll(codeNormMap.get(pat));
 			code.setLength(0);
 			code.insert(0, replace);
 		}
 	}
 
 	protected static String removeOpName(StringBuilder code) {
 		String opName;
 
 		final int opNameEnd = code.indexOf(" ");
 		if (opNameEnd < 0) {
 			opName = code.toString();
 			code.setLength(0);
 		} else {
 			opName = code.substring(0, opNameEnd);
 			code.delete(0, opNameEnd + 1);
 		}
 
 		return opName;
 	}
 
 	public TIntIntHashMap[] loadData(final String[] dataLines, Result[] resRef) {
 		final TIntIntHashMap dataIn = new TIntIntHashMap();
 		final TIntIntHashMap dataOut = new TIntIntHashMap();
 
 		for (int lineNum = 0, dataLinesLength = dataLines.length; lineNum < dataLinesLength; lineNum++) {
 			final String dataLine = dataLines[lineNum];
 			final String line = dataLine.replaceAll("\\s+", "");
 
 			if (line.length() == 0 || line.startsWith("#")) {
 				continue;
 			}
 
 			final String prefix = "Memory(line " + (lineNum + 1) + "): ";
 			final String[] tokens = line.split(":");
 			if (tokens.length != 3) {
 				Result.failure(log, resRef, prefix + "must be in format addr:valueIn:valueOut");
 				return null;
 			}
 
 			int address = 0;
 			for (int t = 0; t < 3; t++) {
 				final String token = tokens[t];
 
 				if (t > 0 && token.length() == 0) {
 					continue;
 				}
 
 				if (!Data.isNum(token, 32)) {
 					Result.failure(log, resRef, prefix + "token#'" + t + "' must be a 32-bit number");
 					return null;
 				}
 
 				final long value = Data.parse(token);
 
 				if (t == 0) {
 					if (value < 0) {
 						Result.failure(log, resRef, prefix + "address must >= 0");
 						return null;
 					}
 					if (value > Integer.MAX_VALUE) {
 						final String maxHex = Integer.toString(Integer.MAX_VALUE, 16);
 						Result.failure(log, resRef, prefix + "address '" + value + "' must be <= 0x" + maxHex);
 						return null;
 					}
 					if (value % 4 > 0) {
 						Result.failure(log, resRef, prefix + "address '" + value + "' must be word-aligned");
 						return null;
 					}
 
 					address = (int) value;
 				}
 
 				(t == 1 ? dataIn : dataOut).put(address, (int) value);
 			}
 		}
 
 		Result.success(log, resRef, "Data validated and loaded fine");
 		return new TIntIntHashMap[]{dataIn, dataOut};
 	}
 
 	public TIntIntHashMap[] loadRegs(final String[] regsLines, Result[] resRef) {
 		final TIntIntHashMap regsIn = new TIntIntHashMap();
 		final TIntIntHashMap regsOut = new TIntIntHashMap();
 
 		for (int lineNum = 0, regsLinesLength = regsLines.length; lineNum < regsLinesLength; lineNum++) {
 			final String dataLine = regsLines[lineNum];
 			final String line = dataLine.replaceAll("\\s+", "");
 
 			if (line.length() == 0 || line.startsWith("#")) {
 				continue;
 			}
 
 			final String prefix = "Registers(line " + (lineNum + 1) + "): ";
 			final String[] tokens = line.split(":");
 			if (tokens.length != 3) {
 				Result.failure(log, resRef, prefix + "must be in format register:valueIn:valueOut");
 				return null;
 			}
 
 			final String regToken = tokens[0];
 			final Reg reg = parseReg(regToken, log, resRef, prefix);
 			if (reg == null) {
 				return null;
 			}
 
 			if (!G.contains(Reg.publicRegs, reg) || G.contains(Reg.roRegs, reg)) {
 				Result.failure(log, resRef, prefix + "register $" + reg.toString() + " is reserved/read-only");
 				return null;
 			}
 			if (G.contains(Reg.autoRegs, reg)) {
 				Result.failure(log, resRef, prefix + "register $" + reg.toString() + " is set/verified automatically");
 				return null;
 			}
 			if (G.contains(Reg.tempRegs, reg)) {
 				Result.failure(log, resRef, prefix + "register $" + reg.toString() + " is temporary");
 				return null;
 			}
 
 			if (tokens[1].length() > 0) {
 				if (!Data.isNum(tokens[1], 32)) {
 					Result.failure(log, resRef, prefix + "input value must be a 32-bit number");
 					return null;
 				}
 				regsIn.put(reg.ordinal(), (int) Data.parse(tokens[1]));
 			}
 			if (tokens[2].length() > 0) {
 				if (!Data.isNum(tokens[2], 32)) {
 					Result.failure(log, resRef, prefix + "output value must be a 32-bit number");
 					return null;
 				}
 				regsOut.put(reg.ordinal(), (int) Data.parse(tokens[2]));
 			}
 		}
 
 		Result.success(resRef, "Registers validated and loaded fine");
 		return new TIntIntHashMap[]{regsIn, regsOut};
 	}
 
 	protected static Reg parseReg(final String regToken, final Logger log, final Result[] resRef, final String prefix) {
 		final Reg reg;
 		if (regToken.startsWith("$")) {
 			final String regName = regToken.substring(1).toLowerCase();
 			final Reg regByName = Reg.getByName().get(regName);
 			if (regByName == null) {
 				if (!Data.isNum(regName, 5)) {
 					Result.failure(log, resRef, prefix + "refers to unknown register '" + regName + "'");
 					return null;
 				} else {
 					return parseReg(regName, log, resRef, prefix);
 				}
 			}
 			reg = regByName;
 			return reg;
 		}
 
 		if (Data.isNum(regToken, 5)) {
 			final long regNumLong = Data.parse(regToken);
 
 			if (regNumLong < 0) {
 				Result.failure(log, resRef, prefix + "refers to register with negative number '" + regNumLong + "'");
 				return null;
 			}
 			if (regNumLong > Reg.values().length) {
 				Result.failure(log, resRef, prefix + "refers to register with illegal number '" + regNumLong + "'");
 				return null;
 			}
 
 			reg = Reg.values()[(int) regNumLong];
 			return reg;
 		}
 
 		Result.failure(log, resRef, prefix + "register token must be either $name or valid number");
 		return null;
 	}
 }
 
