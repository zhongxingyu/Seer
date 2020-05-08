 package project;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 10/2/12
  * Time: 9:57 PM
  */
 public class Assembler {
     private final String GROUPNAME = "asmGroup";
     private Set<String> preLoadedOpcodes = new HashSet<String>();
     public int INT = 4;
     public int INSTRUCTION = 12;
     public int BYT = 1;
     public HashMap<String, String> reg = new HashMap<String, String>();
     public List<Memory> mem = new ArrayList<Memory>();
     HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();
     ThreadGroup threadGroup = new ThreadGroup(GROUPNAME);
 
     public List<String> nameRegRegList = new ArrayList<String>();
     public int threadCount = 1;
 
     private final int ADD = 20;
     private final int SUB = 21;
     private final int MUL = 22;
     private final int DIV = 23;
     private final int LDR = 24;
     private final int TRP = 25;
     private final int LDA = 26;
     private final int JMP = 27;
     private final int CMP = 28;
     private final int BNZ = 29;
     private final int ADDI = 30;
     private final int MOV = 31;
     private final int ADI = 32;
     private final int JMR = 33;
     private final int STR = 34;
     private final int STRI = 35;
     private final int BLT = 36;
     private final int BRZ = 37;
     private final int RUN = 38;
     private final int END = 39;
     private final int BLK = 40;
     private final int BGT = 41;
 
     private void initalizeRegReglist() {
         nameRegRegList.add("ADD");
         nameRegRegList.add("SUB");
         nameRegRegList.add("MUL");
         nameRegRegList.add("DIV");
         nameRegRegList.add("CMP");
         nameRegRegList.add("ADDI");
         nameRegRegList.add("MOV");
         nameRegRegList.add("STRI");
     }
     private void preloadReg() {
         for (int i = 0; i < 101; i++) {
             reg.put(Integer.toString(i), "0");
         }
     }
 
     private void loadOpcodes() {
         preLoadedOpcodes.add("TRP");
         preLoadedOpcodes.add("ADD");
         preLoadedOpcodes.add("SUB");
         preLoadedOpcodes.add("DIV");
         preLoadedOpcodes.add("MUL");
         preLoadedOpcodes.add("LDR");
         preLoadedOpcodes.add("LDA");
         preLoadedOpcodes.add("JMP");
         preLoadedOpcodes.add("CMP");
         preLoadedOpcodes.add("BNZ");
         preLoadedOpcodes.add("ADDI");
         preLoadedOpcodes.add("MOV");
         preLoadedOpcodes.add("ADI");
         preLoadedOpcodes.add("JMR");
         preLoadedOpcodes.add("STR");
         preLoadedOpcodes.add("STRI");
         preLoadedOpcodes.add("BLT");
         preLoadedOpcodes.add("BRZ");
         preLoadedOpcodes.add("RUN");
         preLoadedOpcodes.add("END");
         preLoadedOpcodes.add("BLK");
         preLoadedOpcodes.add("BGT");
     }
 
     public void action(String file) {
         loadOpcodes();
         initalizeRegReglist();
 
         try {
             String lastLabelLoaded = null;
             String lastDirectiveLoaded = null;
 
             // pass one; build symbol table
             symbolTable.put("INII", INT);
             mem.add(new Memory("INII", "0"));
             symbolTable.put("INPT", INT);
             symbolTable.put("INCT", INT);
             mem.add(new Memory("INPT", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("", ""));
             mem.add(new Memory("INCT", "0"));
             BufferedReader reader = new BufferedReader(new FileReader(file));
             String line;
             String[] lineInfo;
             int byteCount = 0;
 
             while ((line = reader.readLine()) != null) {
                 if (line.trim().length() == 0) {
                     continue;
                 }
 
                 lineInfo = line.trim().split("[\t ]+");
 
 
                 if (!preLoadedOpcodes.contains(lineInfo[0])) {
 
                     if (symbolTable.containsKey(lineInfo[0].trim())) {
                         System.out.println("Assembly file cannot have duplicate Label names");
                         return;
                     }
 
                     // handle int arrays
                     if (lastLabelLoaded != null) {
                         if (isDirective(lineInfo[0].trim())) {
                             if (!lastDirectiveLoaded.equals(lineInfo[0].trim())) {
                                 System.out.println("Array elements must be of the same type");
                                 return;
                             }
                             mem.add(new Memory("", lineInfo[1]));
                             if (lineInfo[0].equals(".INT")) {
                                 byteCount += INT;
                             } else {
                                 byteCount += BYT;
                             }
                             continue;
                         }
                     }
 
                     symbolTable.put(lineInfo[0].trim(), byteCount);
 
                     if (lineInfo[1].equals(".INT")) {
                         byteCount += INT;
                         mem.add(new Memory(lineInfo[0].trim(), lineInfo[2]));
 
                         lastDirectiveLoaded = lineInfo[1].trim();
                         lastLabelLoaded = lineInfo[0].trim();
                     } else if (lineInfo[1].equals(".BYT")) {
                         byteCount += BYT;
                         int asciiTest;
 
                         if (lineInfo[2].substring(1, lineInfo[2].length() - 1).matches("^[0-9]+$")) {
                             asciiTest = Integer.parseInt(lineInfo[2].substring(1, lineInfo[2].length() - 1));
                             if (asciiTest == 32) {
                                 mem.add(new Memory(lineInfo[0].trim(), " "));
                             } else if (asciiTest == 13) {
                                 mem.add(new Memory(lineInfo[0].trim(), "\r\n"));
                             } else {
                                 mem.add(new Memory(lineInfo[0].trim(), lineInfo[2].substring(1, lineInfo[2].length() - 1)));
                             }
                         } else {
                             mem.add(new Memory(lineInfo[0].trim(), lineInfo[2].substring(1, lineInfo[2].length() - 1)));
                         }
 
                         lastDirectiveLoaded = lineInfo[1].trim();
                         lastLabelLoaded = lineInfo[0].trim();
                     } else if (lineInfo[0].trim().length() > 4) {
                         if (lineInfo[1].equals("TRP") || lineInfo[1].equals("JMP") || lineInfo[1].equals("JMR")) {
                             mem.add(new Memory(lineInfo[0].trim(), lineInfo[1] + "\t" + lineInfo[2]));
                         } else {
                             mem.add(new Memory(lineInfo[0].trim(), lineInfo[1] + "\t" + lineInfo[2] + "\t" + lineInfo[3]));
                         }
                         byteCount += INSTRUCTION;
                     }
                 }
             }
 
 
             // pass two; check syntax, references
             BufferedReader reader3 = new BufferedReader(new FileReader(file));
             List<Instruction> instructionList = new ArrayList<Instruction>();
 
             int counter = 1;
             while ((line = reader3.readLine()) != null) {
                 if (line.trim().length() == 0) {
                     counter++;
                     continue;
                 }
 
                 lineInfo = line.trim().split("[\t ]+");
 
                 if (lineInfo[0].equals("END")) {
                     instructionList.add(new Instruction(END, ""));
                     continue;
                 } else if (lineInfo[1].equals("END")) {
                     instructionList.add(new Instruction(END, "", "", lineInfo[0]));
                     continue;
                 }
 
                 // check TRP for correct layout
                 if (checkForTRPLayout(lineInfo, counter, instructionList)) return;
 
                 if (checkForRegImmLabels(lineInfo, counter, instructionList)) return;
 
                 // check for operator Labels
                 if (checkForRegRegLabels(lineInfo, counter, instructionList)) return;
 
                 // check for LDR labels
                 if (checkForRegMemLabels(symbolTable, lineInfo, counter, instructionList)) return;
 
                 // check for valid jmp
                 if (checkForJMPLayout(symbolTable, lineInfo, counter, instructionList)) return;
 
                 // check for valid jmr
                 if (checkForJMRLayout(lineInfo, counter, instructionList)) return;
 
                 if(checkMultiThreadLayout(lineInfo, counter, symbolTable, instructionList)) return;
 
                 counter++;
             }
             reader.close();
 
             // virtual machine
             runVM(instructionList, 0, instructionList.size());
 
         } catch (Exception e) {
             System.out.println("STOP! An error has been found.");
         }
     }
 
     private boolean checkMultiThreadLayout(String[] lineInfo, int counter, HashMap<String, Integer> symbolTable, List<Instruction> instructionList) {
         if (lineInfo[0].equals("RUN") || lineInfo[0].equals("END")) {
             if (lineInfo[0].equals("END")) {
                 instructionList.add(new Instruction(END, ""));
                 return false;
             }
 
             if (!isValidRegister(lineInfo[1])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!symbolTable.containsKey(lineInfo[2])) {
                 System.out.println("Label at line: " + counter + " operand 2 does not exist.");
                 return true;
             }
             if (lineInfo[2].length() <= 4) {
                 System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                 return true;
             }
             instructionList.add(new Instruction(RUN, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
 
         } else if (lineInfo[1].equals("RUN") || lineInfo[1].equals("END")) {
             if (lineInfo[1].equals("END")) {
                 instructionList.add(new Instruction(END, "", "", lineInfo[0].trim()));
                 return false;
             }
 
             if (!isValidRegister(lineInfo[2])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!symbolTable.containsKey(lineInfo[3])) {
                 System.out.println("Label at line: " + counter + " operand 2 does not exist.");
                 return true;
             }
             if (lineInfo[3].length() <= 4) {
                 System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                 return true;
             }
             instructionList.add(new Instruction(RUN, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
         }
 
         return false;
     }
 
     private boolean checkForJMRLayout(String[] lineInfo, int counter, List<Instruction> instructions) {
         if (lineInfo[0].equals("JMR")) {
             if (!isValidRegister(lineInfo[1])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (isValidAddress(reg.get(lineInfo[1].substring(1, lineInfo[1].length())), instructions.size())) {
                 System.out.println("Data in Register" + lineInfo[1] + " at line: " + counter + " must contain an integer address.");
                 return true;
             }
 
             instructions.add(new Instruction(JMR, lineInfo[1].substring(1, lineInfo[1].length())));
         } else if (lineInfo[1].equals("JMR")) {
             if (!isValidRegister(lineInfo[2])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (isValidAddress(reg.get(lineInfo[2].substring(1, lineInfo[2].length())), instructions.size())) {
                 System.out.println("Data in Register" + lineInfo[1] + " at line: " + counter + " must contain an integer address.");
                 return true;
             }
 
             instructions.add(new Instruction(JMR, lineInfo[2].substring(1, lineInfo[2].length()), "", lineInfo[0].trim()));
         }
         return false;
     }
 
     private boolean isValidAddress(String s, int size) {
         int address = 0;
         try {
             address = Integer.parseInt(s);
         } catch (NumberFormatException e) {
             return false;
         }
         return address <= size - 1;
     }
 
     private boolean checkForJMPLayout(HashMap<String, Integer> symbolTable, String[] lineInfo, int counter, List<Instruction> instructions) {
         if (lineInfo[0].equals("JMP")) {
             if (!symbolTable.containsKey(lineInfo[1])) {
                 System.out.println("Label at line: " + counter + " operand 2 does not exist.");
                 return true;
             }
             if (lineInfo[1].length() <= 4) {
                 System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                 return true;
             }
 
             instructions.add(new Instruction(JMP, lineInfo[1]));
         } else if (lineInfo[1].equals("JMP")) {
             if (!symbolTable.containsKey(lineInfo[2])) {
                 System.out.println("Label at line: " + counter + " operand 2 does not exist.");
                 return true;
             }
 
             if (lineInfo[2].length() <= 4) {
                 System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                 return true;
             }
 
             instructions.add(new Instruction(JMP, lineInfo[2], "", lineInfo[0].trim()));
         }
         return false;
     }
 
     private boolean isDirective(String possDirective) {
         return (possDirective.equals(".INT") || possDirective.equals(".BYT"));
     }
 
     public void runVM(List<Instruction> instructionList, int startInstructionAt, int endInstr) {
         preloadReg();
         boolean stopVM = false;
         String lastRegUsedInt = "";
         String lastRegUsedChar = "";
 
         for (int i = startInstructionAt; i < endInstr; i++) {
             Integer newValue;
             switch (instructionList.get(i).getOpCode()) {
                 case ADI:
                     if (!validADIOpd(instructionList.get(i))) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " the ADI instruction requires integer values in the registers being added");
                         stopVM = true;
                         break;
                     }
                     Integer value = Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) + Integer.parseInt(instructionList.get(i).getOpd2());
                     reg.put(instructionList.get(i).getOpd1(), value.toString());
                     break;
                 case ADD:
                     if (!validOpd(instructionList.get(i))) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " the ADD instruction requires integer values in the registers being added");
                         stopVM = true;
                         break;
                     }
 
                     newValue = Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) + Integer.parseInt(reg.get(instructionList.get(i).getOpd2()));
                     reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                     lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
 
                     break;
                 case ADDI:
 
                     String newValue1 = mem.get(Integer.parseInt(reg.get(instructionList.get(i).getOpd2()))).getData();
                     reg.put(instructionList.get(i).getOpd1(), newValue1);
                     lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
 
                     break;
                 case SUB:
                     if (!validOpd(instructionList.get(i))) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " the SUB instruction requires integer values in the registers being subtracted");
                         stopVM = true;
                         break;
                     }
 
                     newValue = Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) - Integer.parseInt(reg.get(instructionList.get(i).getOpd2()));
                     reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                     lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
 
                     break;
                 case MUL:
                     if (!validOpd(instructionList.get(i))) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " the MUL instruction requires integer values in the registers being multiplied");
                         stopVM = true;
                         break;
                     }
 
                     newValue = Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) * Integer.parseInt(reg.get(instructionList.get(i).getOpd2()));
                     reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                     lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
 
                     break;
                 case DIV:
                     if (!validOpd(instructionList.get(i))) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " the DIV instruction requires integer values in the registers being divided");
                         stopVM = true;
                         break;
                     }
 
                     if (Integer.parseInt(reg.get(instructionList.get(i).getOpd2())) == 0) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " Can not divide by zero");
                         stopVM = true;
                         break;
                     }
 
                     newValue = Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) / Integer.parseInt(reg.get(instructionList.get(i).getOpd2()));
                     reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                     lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
 
                     break;
                 case STR:
                     if (instructionList.get(i).getOpd2().trim().length() > 4) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " operand two must be a label for a directive");
                         stopVM = true;
                         break;
                     }
 
                     for (Memory m : mem) {
 
                         if (m.getLabel() != null && m.getLabel().equals(instructionList.get(i).getOpd2())) {
                             m.setData(reg.get(instructionList.get(i).getOpd1()));
                             break;
                         }
                     }
                     break;
                 case STRI:
                     // oprand 1 must be an index to a value in mem
                     mem.get(Integer.parseInt(reg.get(instructionList.get(i).getOpd1()))).setData(reg.get(instructionList.get(i).getOpd2()));
                     lastRegUsedChar = reg.get(instructionList.get(i).getOpd2());
 
                     break;
                 case LDR:
                     String opd2 = "";
 
                     if (instructionList.get(i).getOpd2().length() > 4) {
                         String instruction = null;
                         for (Memory m : mem) {
                             if (m.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 instruction = m.getData();
                                 break;
                             }
                         }
                         opd2 = handleInstruction(instruction);
                     } else {
                         for (Memory m : mem) {
                             if (m.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 opd2 = m.getData();
                                 break;
                             }
                         }
                     }
 
                     if (opd2 == null) {
                         System.out.println("Error: unknown error has occurred with LDR operation");
                         stopVM = true;
                         break;
                     }
 
                     reg.put(instructionList.get(i).getOpd1(), opd2);
 
                     if (reg.get(instructionList.get(i).getOpd1()).matches("^(-|\\+)?[0-9]+$")) {
                         lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
                     } else {
                         lastRegUsedChar = reg.get(instructionList.get(i).getOpd1());
                     }
                     break;
                 case TRP:
                     if (instructionList.get(i).getOpd1().equals("0")) {
                         stopVM = true;
                         break;
                     }
                     if (instructionList.get(i).getOpd1().equals("1")) {
                         System.out.print(lastRegUsedInt);
                         break;
                     }
                     if (instructionList.get(i).getOpd1().equals("3")) {
                         System.out.print(lastRegUsedChar);
                         break;
                     }
                     if (instructionList.get(i).getOpd1().equals("2")) {
                         Integer number = 0;
                         try {
                             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
                             String input = bufferedReader.readLine();
                             number = Integer.parseInt(input);
                         } catch (NumberFormatException ex) {
                             System.out.println("Not a number !");
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
 
                         for (Memory m : mem) {
                             if (m.getLabel().equals("INII")) {
                                 m.setData(number.toString());
                                 break;
                             }
                         }
                     }
                     if (instructionList.get(i).getOpd1().equals("4")) {
                         if (mem.get(0).getData().equals("") || mem.get(0).getData().equals("\r\n")) {
                             Integer size = 30;
                             for (Memory m : mem) {
                                 if (m.getLabel().equals("SIZE")) {
                                     size = Integer.parseInt(m.getData());
                                     break;
                                 }
                             }
 
                             Integer inCount = 0;
 
                             for (Memory m : mem) {
                                 if (m.getLabel().equals("INCT")) {
                                     inCount = Integer.parseInt(m.getData());
                                     break;
                                 }
                             }
 
                             int count = 0;
                             boolean firstPass = true;
                             try {
                                 char c;
                                 while ((c = (char) System.in.read()) != '\n') {
                                     if (count < size - 2) {
                                         mem.get(count).setData(String.valueOf(c));
                                         if (firstPass) {
                                             lastRegUsedInt = String.valueOf(c);
                                             firstPass = false;
                                         }
                                     }
                                     inCount++;
                                     count++;
                                 }
                                 mem.get(count).setData("\r\n");
                                 inCount++;
                                 count++;
 
                                 for (Memory m : mem) {
                                     if (m.getLabel().equals("INCT")) {
                                         m.setData(inCount.toString());
                                         break;
                                     }
                                 }
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }
                     }
                     break;
                 case CMP:
 
                     if (reg.get(instructionList.get(i).getOpd1()).matches("^(-|\\+)?[0-9]+$") && reg.get(instructionList.get(i).getOpd2()).matches("^(-|\\+)?[0-9]+$")) {
                        newValue = new Integer(reg.get(instructionList.get(i).getOpd2())) - new Integer(reg.get(instructionList.get(i).getOpd1()));
                     } else {
                         newValue = reg.get(instructionList.get(i).getOpd1()).compareTo(reg.get(instructionList.get(i).getOpd2()));
                     }
 
                     reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                     break;
                 case JMP:
                     for (Instruction jmptest : instructionList) {
                         if (jmptest.getLabel() != null && jmptest.getLabel().equals(instructionList.get(i).getOpd1())) {
                             i = instructionList.indexOf(jmptest) - 1;
                             break;
                         }
                     }
 
                     break;
                 case JMR:
                     i = Integer.parseInt(reg.get(instructionList.get(i).getOpd1()));
                     break;
                 case LDA:
                     newValue = -1;
                     if (instructionList.get(i).getOpd2().length() < 5) {
                         for (Memory m : mem) {
                             if (m.getLabel() != null && m.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 newValue = mem.indexOf(m);
                                 reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                                 break;
                             }
                         }
                     } else if (instructionList.get(i).getOpd2().length() > 4) {
                         for (Instruction instruction : instructionList) {
                             if (instruction.getLabel() != null && instruction.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 newValue = instructionList.indexOf(instruction);
                                 reg.put(instructionList.get(i).getOpd1(), newValue.toString());
                                 break;
                             }
                         }
                     }
 
                     if (newValue < 0) {
                         System.out.println("Error on instruction: " + instructionList.get(i).getOpCode() + " " + instructionList.get(i).getOpd1() + " " + instructionList.get(i).getOpd2() + " the LDA instruction requires a valid label.");
                         stopVM = true;
                     }
 
                     break;
                 case BNZ:
                     newValue = 0;
                     if (!reg.get(instructionList.get(i).getOpd1()).equals(newValue.toString())) {
                         for (Instruction bnxTest : instructionList) {
                             if (bnxTest.getLabel() != null && bnxTest.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 i = instructionList.indexOf(bnxTest) - 1;
                                 break;
                             }
                         }
                     }
 
                     break;
                 case BLT:
                     newValue = 0;
                     if (Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) < newValue) {
                         for (Instruction bnxTest : instructionList) {
                             if (bnxTest.getLabel() != null && bnxTest.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 i = instructionList.indexOf(bnxTest) - 1;
                                 break;
                             }
                         }
                     }
 
                     break;
                 case BGT:
                     newValue = 0;
                     if (Integer.parseInt(reg.get(instructionList.get(i).getOpd1())) > newValue) {
                         for (Instruction bnxTest : instructionList) {
                             if (bnxTest.getLabel() != null && bnxTest.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 i = instructionList.indexOf(bnxTest) - 1;
                                 break;
                             }
                         }
                     }
 
                     break;
                 case BRZ:
                     newValue = 0;
                     if (reg.get(instructionList.get(i).getOpd1()).equals(newValue.toString())) {
                         for (Instruction bnxTest : instructionList) {
                             if (bnxTest.getLabel() != null && bnxTest.getLabel().equals(instructionList.get(i).getOpd2())) {
                                 i = instructionList.indexOf(bnxTest) - 1;
                                 break;
                             }
                         }
                     }
 
                     break;
                 case MOV:
                     reg.put(instructionList.get(i).getOpd1(), reg.get(instructionList.get(i).getOpd2()));
 
                     if (reg.get(instructionList.get(i).getOpd1()).matches("^(-|\\+)?[0-9]+$")) {
                         lastRegUsedInt = reg.get(instructionList.get(i).getOpd1());
                     } else {
                         lastRegUsedChar = reg.get(instructionList.get(i).getOpd1());
                     }
                     break;
                 case RUN:
                     int index = 0;
                     for (Instruction findLabel : instructionList) {
                         if (findLabel.getLabel() != null && findLabel.getLabel().equals(instructionList.get(i).getOpd2())) {
                             index = instructionList.indexOf(findLabel);
                             break;
                         }
                     }
 
                     RunnableThread thread = new RunnableThread(threadGroup, "thread_" + threadCount++, this, instructionList, index, index + 8);
                     reg.put(instructionList.get(i).getOpd1(), Long.valueOf(thread.getId()).toString());
                     thread.start();
 
                     break;
                 case END:
                     RunnableThread test[] = new RunnableThread[threadGroup.activeCount()];
                     threadGroup.enumerate(test);
                     Thread.currentThread().interrupt();
 
                     break;
                 case BLK:
 
 //                    Thread test[] = new Thread[threadGroup.activeCount()];
 //                    threadGroup.enumerate(test);
 //                    test[1].suspend();
                     break;
                 default:
                     // do nothing
                     break;
             }
 
             if (stopVM) {
                 break;
             }
         }
     }
 
     private String handleInstruction(String opd2) {
         String[] instructions = opd2.split("[\t ]+");
         int switcher = 0;
 
         if (instructions[0].equals("ADD")) {
             switcher = ADD;
         } else if (instructions[0].equals("ADDI")) {
             switcher = ADDI;
         } else if (instructions[0].equals("ADI")) {
             switcher = ADI;
         } else if (instructions[0].equals("SUB")) {
             switcher = SUB;
         } else if (instructions[0].equals("MUL")) {
             switcher = MUL;
         } else if (instructions[0].equals("DIV")) {
             switcher = DIV;
         } else if (instructions[0].equals("LDR")) {
             switcher = LDR;
         } else if (instructions[0].equals("TRP")) {
             System.out.println("Error: Can not use TRP as an operand of LDR");
             return null;
         } else if (instructions[0].equals("MOV")) {
             switcher = MOV;
         } else {
             return null;
         }
 
         Instruction instr = new Instruction();
 
         Integer retValue = 0;
 
         switch (switcher) {
             case ADI:
                 if (!reg.get(instr.getOpd1()).matches("^(-|\\+)?[0-9]+$")) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADD instruction requires integer values in the registers being added");
                     return null;
                 }
 
                 return reg.put(instr.getOpd1(), instr.getOpd2());
             case ADD:
                 instr = new Instruction(switcher, instructions[1].substring(1, instructions[1].length()), instructions[2].substring(1, instructions[2].length()));
                 if (!validOpd(instr)) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADD instruction requires integer values in the registers being added");
                     return null;
                 }
 
                 retValue = Integer.parseInt(reg.get(instr.getOpd1())) + Integer.parseInt(reg.get(instr.getOpd2()));
                 return retValue.toString();
             case ADDI:
                 if (!reg.get(instr.getOpd1()).matches("^(-|\\+)?[0-9]+$")) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADDI instruction requires integer values in the registers being added");
                     break;
                 }
                 if (!mem.get(Integer.parseInt(reg.get(instr.getOpd2()))).getData().matches("^(-|\\+)?[0-9]+$")) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADDI instruction requires integer values in the registers being added");
                     break;
                 }
 
                 Integer newValue = Integer.parseInt(reg.get(instr.getOpd1())) + Integer.parseInt(mem.get(Integer.parseInt(reg.get(instr.getOpd2()))).getData());
                 return newValue.toString();
             case SUB:
                 instr = new Instruction(switcher, instructions[1].substring(1, instructions[1].length()), instructions[2].substring(1, instructions[2].length()));
                 if (!validOpd(instr)) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the SUB instruction requires integer values in the registers being subtracted");
                     return null;
                 }
 
                 retValue = Integer.parseInt(reg.get(instr.getOpd1())) - Integer.parseInt(reg.get(instr.getOpd2()));
                 return retValue.toString();
             case MUL:
                 instr = new Instruction(switcher, instructions[1].substring(1, instructions[1].length()), instructions[2].substring(1, instructions[2].length()));
                 if (!validOpd(instr)) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the MUL instruction requires integer values in the registers being multiplied");
                     return null;
                 }
 
                 retValue = Integer.parseInt(reg.get(instr.getOpd1())) * Integer.parseInt(reg.get(instr.getOpd2()));
                 return retValue.toString();
             case DIV:
                 instr = new Instruction(switcher, instructions[1].substring(1, instructions[1].length()), instructions[2].substring(1, instructions[2].length()));
                 if (!validOpd(instr)) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the DIV instruction requires integer values in the registers being divided");
                     return null;
                 }
 
                 if (Integer.parseInt(reg.get(instr.getOpd2())) == 0) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " -> Can not divide by zero");
                     return null;
                 }
 
                 retValue = Integer.parseInt(reg.get(instr.getOpd1())) / Integer.parseInt(reg.get(instr.getOpd2()));
                 return retValue.toString();
             case LDR:
                 instr = new Instruction(switcher, instructions[1].substring(1, instructions[1].length()), instructions[2]);
                 String opdTwo = "";
 
                 if (instr.getOpd2().length() > 4) {
                     opdTwo = handleInstruction(instr.getOpd2());
                 } else {
                     for (Memory m : mem) {
                         if (m.getLabel().equals(instr.getOpd2())) {
                             return m.getData();
                         }
                     }
                 }
 
                 break;
             case LDA:
                 newValue = -1;
                 for (Memory m : mem) {
                     if (m.getLabel().equals(instr.getOpd2())) {
                         newValue = mem.indexOf(m);
                         return newValue.toString();
                     }
                 }
 
                 if (newValue < 0) {
                     System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the LDA instruction requires a valid label.");
                     return null;
                 }
 
                 break;
             case MOV:
                 return reg.get(instr.getOpd2());
             default:
                 // do nothing
                 break;
         }
         return null;
     }
 
     private boolean validOpd(Instruction instr) {
         if (!reg.get(instr.getOpd1()).matches("^(-|\\+)?[0-9]+$")) {
             System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADD instruction requires integer values in the registers being added");
             return false;
         }
         if (!reg.get(instr.getOpd2()).matches("^(-|\\+)?[0-9]+$")) {
             System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADD instruction requires integer values in the registers being added");
             return false;
         }
         return true;
     }
 
     private boolean validADIOpd(Instruction instr) {
         if (!reg.get(instr.getOpd1()).matches("^(-|\\+)?[0-9]+$")) {
             System.out.println("Error on instruction: " + instr.getOpCode() + " " + instr.getOpd1() + " " + instr.getOpd2() + " the ADI instruction requires integer values in the registers being added");
             return false;
         }
         return true;
     }
 
     private boolean checkForTRPLayout(String[] lineInfo, int counter, List<Instruction> instructions) {
         if (lineInfo[0].equals("TRP")) {
             if (!lineInfo[1].matches("^(\\d|4)$")) {
                 System.out.println("invalid trap type on line: " + counter + ".  Must choose from 0 - 4");
                 return true;
             }
             instructions.add(new Instruction(TRP, lineInfo[1]));
         } else if (lineInfo[1].equals("TRP")) {
             if (!lineInfo[2].matches("^(\\d|4)$")) {
                 System.out.println("invalid trap type on line: " + counter + ".  Must choose from 0 - 4");
                 return true;
             }
             instructions.add(new Instruction(TRP, lineInfo[2], "", lineInfo[0].trim()));
         }
         return false;
     }
 
     private boolean checkForRegImmLabels(String[] lineInfo, int counter, List<Instruction> instructions) {
         if (lineInfo[0].equals("ADI")) {
             if (!isValidRegister(lineInfo[1])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!lineInfo[2].matches("^(-|\\+)?[0-9]+$")) {
                 System.out.println("Line " + counter + ": operand 2 must be a valid integer");
                 return true;
             }
             instructions.add(new Instruction(ADI, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
         } else if (lineInfo[1].equals("ADI")) {
             if (!isValidRegister(lineInfo[2])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!lineInfo[3].matches("^(-|\\+)?[0-9]+$")) {
                 System.out.println("Line " + counter + ": operand 2 must be a valid integer");
                 return true;
             }
             instructions.add(new Instruction(ADI, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].trim(), lineInfo[0].trim()));
         }
         return false;
     }
 
     private boolean checkForRegRegLabels(String[] lineInfo, int counter, List<Instruction> instructions) {
         if (lineInfo[0].equals("ADD") || lineInfo[0].equals("SUB") || lineInfo[0].equals("MUL") || lineInfo[0].equals("DIV") || lineInfo[0].equals("CMP") || lineInfo[0].equals("ADDI") || lineInfo[0].equals("MOV") || lineInfo[0].equals("STRI")) {
             if (!isValidRegister(lineInfo[1])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!isValidRegister(lineInfo[2])) {
                 System.out.println("Line " + counter + ": operand 2 must be a valid register");
                 return true;
             }
 
             if (lineInfo[0].equals("ADD")) {
                 instructions.add(new Instruction(ADD, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else if (lineInfo[0].equals("SUB")) {
                 instructions.add(new Instruction(SUB, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else if (lineInfo[0].equals("MUL")) {
                 instructions.add(new Instruction(MUL, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else if (lineInfo[0].equals("DIV")) {
                 instructions.add(new Instruction(DIV, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else if (lineInfo[0].equals("ADDI")) {
                 instructions.add(new Instruction(ADDI, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else if (lineInfo[0].equals("MOV")) {
                 instructions.add(new Instruction(MOV, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else if (lineInfo[0].equals("STRI")) {
                 instructions.add(new Instruction(STRI, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             } else {
                 instructions.add(new Instruction(CMP, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2].substring(1, lineInfo[2].length())));
             }
 
         } else if (lineInfo[1].equals("ADD") || lineInfo[1].equals("SUB") || lineInfo[1].equals("MUL") || lineInfo[1].equals("DIV") || lineInfo[1].equals("CMP") || lineInfo[1].equals("ADDI") || lineInfo[1].equals("MOV") || lineInfo[1].equals("STRI")) {
             if (!isValidRegister(lineInfo[2])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!isValidRegister(lineInfo[3])) {
                 System.out.println("Line " + counter + ": operand 2 must be a valid register");
                 return true;
             }
 
             if (lineInfo[1].equals("ADD")) {
                 instructions.add(new Instruction(ADD, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else if (lineInfo[1].equals("SUB")) {
                 instructions.add(new Instruction(SUB, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else if (lineInfo[1].equals("MUL")) {
                 instructions.add(new Instruction(MUL, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else if (lineInfo[1].equals("DIV")) {
                 instructions.add(new Instruction(DIV, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else if (lineInfo[1].equals("ADDI")) {
                 instructions.add(new Instruction(ADDI, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else if (lineInfo[1].equals("MOV")) {
                 instructions.add(new Instruction(MOV, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else if (lineInfo[1].equals("STRI")) {
                 instructions.add(new Instruction(STRI, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             } else {
                 instructions.add(new Instruction(CMP, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3].substring(1, lineInfo[3].length()), lineInfo[0].trim()));
             }
         }
         return false;
     }
 
     private boolean checkForRegMemLabels(HashMap<String, Integer> symbolTable, String[] lineInfo, int counter, List<Instruction> instructions) {
         if (lineInfo[0].equals("LDR") || lineInfo[0].equals("LDA") || lineInfo[0].equals("BNZ") || lineInfo[0].equals("STR") || lineInfo[0].equals("BLT") || lineInfo[0].equals("BRZ") || lineInfo[0].equals("BGT")) {
 
             if (!isValidRegister(lineInfo[1])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!symbolTable.containsKey(lineInfo[2])) {
                 System.out.println("Label at line: " + counter + " operand 2 does not exist");
                 return true;
             }
 
             if (lineInfo[0].equals("LDR")) {
                 instructions.add(new Instruction(LDR, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             } else if (lineInfo[0].equals("LDA")) {
                 instructions.add(new Instruction(LDA, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             } else if (lineInfo[0].equals("BNZ")) {
                 if (lineInfo[2].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BNZ, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             } else if (lineInfo[0].equals("BLT")) {
                 if (lineInfo[2].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BLT, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             }  else if (lineInfo[0].equals("BGT")) {
                 if (lineInfo[2].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BGT, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             } else if (lineInfo[0].equals("BRZ")) {
                 if (lineInfo[2].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BRZ, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             } else if (lineInfo[0].equals("STR")) {
                 instructions.add(new Instruction(STR, lineInfo[1].substring(1, lineInfo[1].length()), lineInfo[2]));
             }
 
         } else if (lineInfo[1].equals("LDR") || lineInfo[1].equals("LDA") || lineInfo[1].equals("BNZ") || lineInfo[1].equals("STR") || lineInfo[1].equals("BLT") || lineInfo[1].equals("BRZ") || lineInfo[1].equals("BGT")) {
 
             if (!isValidRegister(lineInfo[2])) {
                 System.out.println("Line " + counter + ": operand 1 must be a valid register");
                 return true;
             }
             if (!symbolTable.containsKey(lineInfo[3])) {
                 System.out.println("Label at line: " + counter + " operand 2 does not exist");
                 return true;
             }
 
             if (lineInfo[1].equals("LDR")) {
                 instructions.add(new Instruction(LDR, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             } else if (lineInfo[1].equals("LDA")) {
                 instructions.add(new Instruction(LDA, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             } else if (lineInfo[1].equals("BNZ")) {
                 if (lineInfo[3].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BNZ, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             } else if (lineInfo[1].equals("BLT")) {
                 if (lineInfo[3].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BLT, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             } else if (lineInfo[1].equals("BGT")) {
                 if (lineInfo[3].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BGT, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             } else if (lineInfo[1].equals("BRZ")) {
                 if (lineInfo[3].length() <= 4) {
                     System.out.println("Label at line: " + counter + " must be associated with an instruction not a directive.");
                     return true;
                 }
 
                 instructions.add(new Instruction(BRZ, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             } else if (lineInfo[1].equals("STR")) {
                 instructions.add(new Instruction(STR, lineInfo[2].substring(1, lineInfo[2].length()), lineInfo[3], lineInfo[0].trim()));
             }
         }
         return false;
     }
 
     private boolean isValidRegister(String reg) {
         return reg.matches("^R(?:100|\\d{1,2})$");
     }
 }
