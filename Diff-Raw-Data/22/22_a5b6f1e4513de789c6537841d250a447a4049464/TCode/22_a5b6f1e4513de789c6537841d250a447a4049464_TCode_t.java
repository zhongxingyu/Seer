 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Nathanael
  * Date: 4/7/13
  * Time: 5:30 PM
  */
 public class TCode {
     private LinkedHashMap<String, Symbol> symbolTable = new LinkedHashMap<String, Symbol>();
     private List<ICode> iCodeList = new ArrayList<ICode>();
     private List<String> tCode = new ArrayList<String>();
     private LinkedHashMap<String, String> reg = new LinkedHashMap<String, String>();
 
     private void initReg() {
         reg.put("R0", "");
         reg.put("R1", "");
         reg.put("R2", "");
         reg.put("R3", "");
         reg.put("R4", "");
         reg.put("R5", "");
         reg.put("R6", "");
         reg.put("R7", "");
         reg.put("R8", "");
         reg.put("R9", "");
     }
 
     public TCode(LinkedHashMap<String, Symbol> symbolTable, List<ICode> iCodeList) {
         this.symbolTable = symbolTable;
         this.iCodeList = iCodeList;
         initReg();
     }
 
     private String getRegister(String id) {
         for (String s : reg.keySet()) {
             if (reg.get(s).equals("")) {
                 reg.put(s, id);
                 return s;
             }
         }
         return null;
     }
 
     private void freeResource(String r) {
         reg.put(r, "");
     }
 
     public void buildCode() {
         for (String key : symbolTable.keySet()) {
             Symbol s = symbolTable.get(key);
 
             if (s.getSymId().startsWith("L") && Character.isDigit(s.getSymId().toCharArray()[1])) {
                 if (s.getData() instanceof VaribleData) {
                     if (((VaribleData) s.getData()).getType().equals("int")) {
                         tCode.add(s.getSymId() + " .INT " + s.getValue().substring(1, s.getValue().length()));
                     } else {
                         if (s.getValue().equals("\'\\n\'"))
                             tCode.add(s.getSymId() + " .BYT " + "\'13\'");
                         else
                             tCode.add(s.getSymId() + " .BYT " + s.getValue());
                     }
                 }
             }
         }
 
         for (ICode iCode : iCodeList) {
             if (iCode.getOperation().equals("CREATE")) {
                 if (symbolTable.get(iCode.getLabel()).getData() instanceof VaribleData) {
                     if (((VaribleData) symbolTable.get(iCode.getLabel()).getData()).getType().equals("int")) {
                         tCode.add(iCode.getLabel() + " " + iCode.getArg1() + " " + "0" + " " + iCode.getComment());
                     } else {
                        tCode.add(iCode.getLabel() + " " + iCode.getArg1() + " " + "\'0\'" + " " + iCode.getComment());
                     }
                } else if (symbolTable.get(iCode.getLabel()).getData() instanceof FunctionData) {
                    tCode.add(iCode.getLabel() + " " + iCode.getArg1() + " " + "\'0\'" + iCode.getComment());
                 }
             }
         }
 
         tCode.add("CLR .INT 0");
 
         for (ICode iCode : iCodeList) {
             if (iCode.getOperation().equals("JMP")) {
                 if (iCode.getLabel().equals("")) {
                     tCode.add(iCode.getOperation() + " " + iCode.getArg1() + " " + iCode.getComment());
                 } else {
                     tCode.add(iCode.getLabel() + " " + iCode.getOperation() + " " + iCode.getArg1() + " " + iCode.getComment());
                 }
                 continue;
             }
 
             if (iCode.getOperation().equals("MAINST")) {
                 tCode.add(iCode.getLabel() + " " + "ADI" + " " + "R0" + " " + "0" + " " + "; start of main");
                 continue;
             }
 
             if (iCode.getOperation().equals("TRP")) {
                 if (iCode.getArg1().equals("0")) {
                     tCode.add("FINISH " + iCode.getOperation() + " " + iCode.getArg1() + " " + iCode.getComment());
                 }
                 continue;
             }
 
             if (iCode.getOperation().equals("RTN")) {
                 tCode.add("JMP FINISH");
                 continue;
             }
 
             if (iCode.getOperation().equals("MOVI")) {
                 String argReg1 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg1 + " CLR");
 
                 String value = symbolTable.get(iCode.getArg2()).getValue();
                 tCode.add("ADI " + argReg1 + " " + value.substring(1, value.length()));
 
                 tCode.add("STR " + argReg1 + " " + iCode.getArg1() + " " + iCode.getComment());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("MOV")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 String argReg2 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg2 + " CLR");
 
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("MOV " + argReg1 + " " + argReg2 + " " + iCode.getComment());
                 tCode.add("STR " + argReg1 + " " + iCode.getArg1());
                 freeResource(argReg1);
                 freeResource(argReg2);
             }
 
             if (iCode.getOperation().equals("ADI")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 String argReg2 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg2 + " CLR");
 
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("ADD " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("ADD")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 String argReg2 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg2 + " CLR");
 
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("ADD " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("SUB")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 String argReg2 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg2 + " CLR");
 
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("SUB " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("DIV")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 String argReg2 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg2 + " CLR");
 
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("DIV " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("MUL")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 String argReg2 = getRegister(iCode.getArg2());
                 tCode.add("LDR " + argReg2 + " CLR");
 
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("SUB " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("WRTI")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 tCode.add("TRP 1 " + iCode.getComment());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("WRTC")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 tCode.add("LDR " + argReg1 + " CLR");
                 tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
 
                 tCode.add("TRP 3 " + iCode.getComment());
                 freeResource(argReg1);
                 continue;
             }
         }
 
         for (String j : tCode) {
             System.out.println(j);
         }
 
         try {
             FileWriter fWriter = new FileWriter("NNM-program.asm");
             BufferedWriter writer = new BufferedWriter(fWriter);
 
             for (String s : tCode) {
                 writer.write(s);
                 writer.newLine();
             }
             writer.close();
         } catch (Exception e) {
             System.out.println("error creating file");
         }
 
         Assembler assembler = new Assembler();
         assembler.action("NNM-program.asm");
     }
 
     private boolean isLiteral(String arg2) {
         return arg2.startsWith("L");
     }
 }
