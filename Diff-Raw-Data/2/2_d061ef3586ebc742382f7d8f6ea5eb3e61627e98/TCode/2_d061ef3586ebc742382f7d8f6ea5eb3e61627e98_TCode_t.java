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
     private int condIncr = 1000;
     private boolean useCondLabel = false;
 
     private void initReg() {
         reg.put("R0", "0");
         reg.put("R1", "1");
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
         tCode.add("LDR " + r + " CLR");
     }
 
     public void buildCode() {
 
         tCode.add("CLR .INT 0");
 
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
             if (iCode.getOperation().equals("CREATE") && !iCode.getLabel().startsWith("L")) {
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
 
         tCode.add("");
         tCode.add("LDR R0 CLR");
         tCode.add("LDR R1 CLR");
         tCode.add("ADI R1 1");
 
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
 
                 String value = symbolTable.get(iCode.getArg2()).getValue();
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("ADI " + argReg1 + " " + value.substring(1, value.length()));
                 } else {
                     tCode.add(iCode.getLabel() + " ADI " + argReg1 + " " + value.substring(1, value.length()));
                 }
 
                 tCode.add("STR " + argReg1 + " " + iCode.getArg1() + " " + iCode.getComment());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("MOV")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("MOV " + argReg1 + " " + argReg2 + " " + iCode.getComment());
                 tCode.add("STR " + argReg1 + " " + iCode.getArg1());
                 freeResource(argReg1);
                 freeResource(argReg2);
             }
 
             if (iCode.getOperation().equals("ADI")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("ADD " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("ADD")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("ADD " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("SUB")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("SUB " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("DIV")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                 tCode.add("DIV " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("MUL")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
 
                tCode.add("MUL " + argReg1 + " " + argReg2);
                 tCode.add("STR " + argReg1 + " " + iCode.getResult() + " " + iCode.getComment());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 continue;
             }
 
             if (iCode.getOperation().equals("WRTI")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("TRP 1 " + iCode.getComment());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("WRTC")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
 
                 tCode.add("TRP 3 " + iCode.getComment());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("RDI")) {
                 if (iCode.getLabel().equals("")) {
                     tCode.add("TRP 2");
                 } else {
                     tCode.add(iCode.getLabel() + " TRP 2");
                 }
                 String argReg1 = getRegister(iCode.getArg1());
 
                 tCode.add("LDR " + argReg1 + " INII");
                 tCode.add("STR " + argReg1 + " " + iCode.getArg1());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("RDC")) {
                 if (iCode.getLabel().equals("")) {
                     tCode.add("TRP 4");
                 } else {
                     tCode.add(iCode.getLabel() + "TRP 4");
                 }
                 String argReg1 = getRegister(iCode.getArg1());
 
                 tCode.add("LDR " + argReg1 + " INPT");
                 tCode.add("STR " + argReg1 + " " + iCode.getArg1());
                 freeResource(argReg1);
                 continue;
             }
 
             if (iCode.getOperation().equals("EQ")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("LDR " + argReg3 + " " + iCode.getResult());
 
                 tCode.add("MOV " + argReg3 + " " + argReg1);
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BRZ " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " == " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult() + " ; Set FALSE");
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult() + " ; Set TRUE");
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("LT")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("LDR " + argReg3 + " " + iCode.getResult());
 
                 tCode.add("MOV " + argReg3 + " " + argReg1);
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BLT " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " < " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult() + " ; Set FALSE");
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult() + " ; Set TRUE");
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("GT")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("LDR " + argReg3 + " " + iCode.getResult());
 
                 tCode.add("MOV " + argReg3 + " " + argReg1);
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BGT " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " > " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult() + " ; Set FALSE");
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult() + " ; Set TRUE");
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("NE")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("LDR " + argReg3 + " " + iCode.getResult());
 
                 tCode.add("MOV " + argReg3 + " " + argReg1);
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BNZ " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " != " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult() + " ; Set FALSE");
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult() + " ; Set TRUE");
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("LE")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("LDR " + argReg3 + " " + iCode.getResult());
 
                 tCode.add("MOV " + argReg3 + " " + argReg1 + " ; Test " + iCode.getArg1() + " < " + iCode.getArg2());
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BLT " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " < " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("MOV " + argReg3 + " " + argReg1 + " ; Test " + iCode.getArg1() + " == " + iCode.getArg2());
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BRZ " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " == " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult() + " ; Set FALSE");
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult() + " ; Set TRUE");
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("GE")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 if (iCode.getLabel().equals("")) {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 } else {
                     tCode.add(iCode.getLabel() + " LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("LDR " + argReg3 + " " + iCode.getResult());
 
                 tCode.add("MOV " + argReg3 + " " + argReg1 + " ; Test " + iCode.getArg1() + " > " + iCode.getArg2());
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BGT " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " > " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("MOV " + argReg3 + " " + argReg1 + " ; Test " + iCode.getArg1() + " == " + iCode.getArg2());
                 tCode.add("CMP " + argReg3 + " " + argReg2);
                 tCode.add("BRZ " + argReg3 + " " + L3 + " ; " + iCode.getResult() + " == " + iCode.getArg2() + " GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult() + " ; Set FALSE");
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult() + " ; Set TRUE");
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("AND")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
 
                 if (useCondLabel) {
                     tCode.set(tCode.size() - 1, "L" + condIncr++ + " LDR " + argReg1 + " " + iCode.getArg1());
                     useCondLabel = false;
                 } else {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 }
 
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 tCode.add("CMP " + argReg1 + " R1 ; Check " + iCode.getArg1() +" for True");
                 tCode.add("BNZ  " + argReg1 + " " + L3 + " ; if FALSE GOTO " + L3);
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("CMP " + argReg2 + " R1 ; Check " + iCode.getArg2() +" for True");
                 tCode.add("BNZ  " + argReg2 + " " + L3 + " ; if FALSE GOTO " + L3);
                 tCode.add("STR R1 " + iCode.getResult());
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R0 " + iCode.getResult());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("OR")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 String argReg2 = getRegister(iCode.getArg2());
                 String argReg3 = getRegister(iCode.getResult());
 
                 if (useCondLabel) {
                     tCode.set(tCode.size() - 1, "L" + condIncr++ + " LDR " + argReg1 + " " + iCode.getArg1());
                     useCondLabel = false;
                 } else {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 }
 
                 String L3 = "L" + condIncr++;
                 String L4 = "L" + condIncr;
 
                 tCode.add("CMP " + argReg1 + " R1 ; Check " + iCode.getArg1() +" for True");
                 tCode.add("BRZ  " + argReg1 + " " + L3 + " ; if TRUE GOTO " + L3);
                 tCode.add("LDR " + argReg2 + " " + iCode.getArg2());
                 tCode.add("CMP " + argReg2 + " R1 ; Check " + iCode.getArg2() +" for True");
                 tCode.add("BRZ  " + argReg2 + " " + L3 + " ; if TRUE GOTO " + L3);
                 tCode.add("STR R0 " + iCode.getResult());
                 tCode.add("JMP " + L4);
                 tCode.add(L3 + " STR R1 " + iCode.getResult());
 
                 freeResource(argReg1);
                 freeResource(argReg2);
                 freeResource(argReg3);
 
                 useCondLabel = true;
                 tCode.add(L4);
             }
 
             if (iCode.getOperation().equals("BF")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 if (useCondLabel) {
                     tCode.set(tCode.size() - 1, "L" + condIncr++ + " LDR " + argReg1 + " " + iCode.getArg1());
                     useCondLabel = false;
                 } else {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("BRZ " + argReg1 + " " + iCode.getArg2() + " " + iCode.getComment());
                 freeResource(argReg1);
             }
 
             if (iCode.getOperation().equals("BT")) {
                 String argReg1 = getRegister(iCode.getArg1());
                 if (useCondLabel) {
                     tCode.set(tCode.size() - 1, "L" + condIncr++ + " LDR " + argReg1 + " " + iCode.getArg1());
                     useCondLabel = false;
                 } else {
                     tCode.add("LDR " + argReg1 + " " + iCode.getArg1());
                 }
                 tCode.add("BNZ " + argReg1 + " " + iCode.getArg2() + " " + iCode.getComment());
                 freeResource(argReg1);
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
 }
