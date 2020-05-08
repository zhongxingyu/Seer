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
 
 import javax.annotation.Nullable;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.regex.Pattern;
 
 public class MipsAssembler {
     private static final Logger log = LoggerFactory.getLogger(MipsAssembler.class);
 
     private static final Pattern PATTERN_LABEL = Pattern.compile("^[a-zA-Z_][a-zA-Z_0-9]*$");
 
     private final static Map<Pattern, String> codeNormMap = createCodeNormMap();
 
     private static Map<Pattern, String> createCodeNormMap() {
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
 
     public Instruction[] loadInstructions(List<String> codeLines, Result[] resRef, final Map<String, Integer> labelIndex) {
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
 
     private boolean loadInstructionsFirstPass(List<String> codeLines, List<Instruction> instructions, Map<String, Integer> labelIndex, Result[] resRef) {
         final StringBuilder code = new StringBuilder();
         final StringBuilder syntax = new StringBuilder();
         final List<String> labels = new ArrayList<String>();
         int instructionIndex = 0;
         for (int lineIndex = 0, codeLinesLength = codeLines.size(); lineIndex < codeLinesLength; lineIndex++) {
             final String prefix = "Code(line " + (lineIndex + 1) + "): ";
             final String codeLine = codeLines.get(lineIndex).trim();
 
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
                 removeOpName(syntax);    //	it must be the same as code stated above
 
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
 
     private static void trim(StringBuilder syntax) {
         while (syntax.length() > 0 && syntax.charAt(0) == ' ') {
             syntax.deleteCharAt(0);
         }
     }
 
     private static boolean parseAddr(final String id, final String numId, StringBuilder syntax, StringBuilder code, Instruction inst, Result[] resRef, String prefixOn) {
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
 
     private static boolean parseNum(final String id, StringBuilder syntax, StringBuilder code, Instruction inst, Result[] resRef, String prefixOn) {
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
 
     private static boolean parseReg(
             final String regId, StringBuilder syntax, StringBuilder code,
             InstructionDesc desc, Instruction inst,
             Result[] resRef, String prefixOn
     ) {
         if (syntax.indexOf(regId) == 0) {
             final String regToken = scanChunk(code, ",()");
             syntax.delete(0, regId.length());
             final Reg reg = parseReg(regToken, log, resRef, prefixOn);
             if (reg == null) {
                 return true;
             }
             if (!G.contains(Reg.publicRegs, reg)) {
                 Result.failure(log, resRef, prefixOn + "direct ref to $" + reg.toString());
                 return true;
             }
             if (desc.writeRegs().contains(regId) && G.contains(Reg.roRegs, reg)) {
                 Result.failure(log, resRef, prefixOn + "write to $" + reg.toString());
                 return true;
             }
 
             inst.setReg(regId, reg);
         }
 
         return false;
     }
 
     private static String scanChunk(StringBuilder code, final String term) {
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
 
     private static boolean checkSeparator(final char sep, StringBuilder syntax, StringBuilder code, Result[] resRef, String prefix) {
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
 
     private static void loadNormed(String codeLine, StringBuilder code) {
         code.setLength(0);
         code.insert(0, codeLine.toLowerCase());
 
         for (Pattern pat : codeNormMap.keySet()) {
             final String replace = pat.matcher(code).replaceAll(codeNormMap.get(pat));
             code.setLength(0);
             code.insert(0, replace);
         }
     }
 
     private static String removeOpName(StringBuilder code) {
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
 
     public TIntIntHashMap[] loadData(final Map<Integer,TaskBean.TestSpecMem> dataSpecs, Result[] resRef) {
         final TIntIntHashMap dataIn = new TIntIntHashMap();
         final TIntIntHashMap dataOut = new TIntIntHashMap();
 
         for (TaskBean.TestSpecMem specMem : dataSpecs.values()) {
             if (specMem.before != null) {
                 dataIn.put(specMem.address, specMem.before);
             }
             if (specMem.after != null) {
                dataIn.put(specMem.address, specMem.after);
             }
         }
 
         Result.success(log, resRef, "Data loaded fine");
         return new TIntIntHashMap[]{dataIn, dataOut};
     }
 
     public TIntIntHashMap[] loadRegs(final Map<Reg,TaskBean.TestSpecReg> regsLines, Result[] resRef) {
         final TIntIntHashMap regsIn = new TIntIntHashMap();
         final TIntIntHashMap regsOut = new TIntIntHashMap();
 
         for (TaskBean.TestSpecReg spec : regsLines.values()) {
             if (spec.before != null) {
                 regsIn.put(spec.reg.ordinal(), spec.before);
             }
             if (spec.after != null) {
                 regsOut.put(spec.reg.ordinal(), spec.after);
             }
         }
 
         Result.success(resRef, "Registers validated and loaded fine");
         return new TIntIntHashMap[]{regsIn, regsOut};
     }
 
     public static Reg parseReg(final String regToken, @Nullable final Logger log, @Nullable final Result[] resRef, final String prefix) {
         if (!regToken.startsWith("$")) {
             Result.failure(log, resRef, prefix + "register token must be either $name or $number");
             return null;
         }
 
         final String regName = regToken.substring(1).toLowerCase();
         final Reg regByName = Reg.getByName().get(regName);
         if (regByName != null) {
             return regByName;
         }
 
         if (!Data.isNum(regName, 5)) {
             Result.failure(log, resRef, prefix + "refers to unknown register '" + regName + "'");
             return null;
         }
 
         final long regNumLong = Data.parse(regName);
         if (regNumLong < 0) {
             Result.failure(log, resRef, prefix + "refers to register with negative number '" + regNumLong + "'");
             return null;
         }
         if (regNumLong > Reg.values().length) {
             Result.failure(log, resRef, prefix + "refers to register with illegal number '" + regNumLong + "'");
             return null;
         }
 
         return Reg.values()[(int) regNumLong];
     }
 }
 
