 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 public class RISCInstruction {
 
   private String cmd;
   private String args1;
   private String args2;
   private String args3;
   private String Instruction;
   private boolean isImmediate;
   private int immediate;
   private final String methodName = "exeCmd";
   private short modifier;
   private String label;
 
 
   // Constructors.........
   public RISCInstruction() {
   }
 
   public RISCInstruction(String command) {
     cmd = command;
     populateValues();
   }
 
   //public functions ......
   public void exeCmd() {
     if (isLabel()) {
       Debug.write("you are trying to compile a label instruction");
     } else {
       try {
         Class<?> cls = Class.forName(getClassName());
         Method meth = cls.getMethod(methodName, this.getClass());
         meth.invoke(cls.newInstance(), this);
       } catch (NoSuchMethodException e) {
         Debug.forceQuit("NEVER REACHING:: method not found" + getClassName());
       } catch (IllegalAccessException e) {
         Debug.forceQuit("NEVER REACHING:: class illegally reached " + getClassName());
       } catch (InstantiationException e) {
         Debug.forceQuit("NEVER REACHING:: instantiateion failed " + getClassName());
       } catch (InvocationTargetException e) {
        Debug.write("NEVER REACHING:: invocation failed " + getClassName());
       } catch (ClassNotFoundException e) {
         Debug.forceQuit("NEVER REACHING:: class is not found " + getClassName());
       }
     }
   }
 
   public boolean isLabel() {
     if (label.length() > 1) return true;
     else return false;
   }
 
   //private functions for internal use ........
   private void populateValues() {
     label = "";
     if (!isLabel()) {
       getInstruction();
      assignModifier();
       pullArgs1();
       args2 = pullArgAfter2();
      if (!isImmediate) {
         args3 = pullArgAfter2();
      } else {
        args3 = "";
      }
     }
   }
 
   private void checkForLabel() {
     cmd = cmd.trim();
   }
 
   private String pullArgAfter2() {
     String result = "";
     if (cmd.length() > 0) {
       if (cmd.charAt(0) == 'r') {
         isImmediate = false;
         result = truncateAt(',');
       } else if ((cmd.length() > 2) && cmd.substring(0, 2).equals("0x")) {
         isImmediate = true;
         cmd = cmd.substring(2, cmd.length()).replaceAll("\\s+", "");
         immediate = Integer.parseInt(cmd, 16);
       } else {
         isImmediate = true;
         immediate = Integer.parseInt(cmd);
       }
     }
     return result;
   }
 
   private void pullArgs1() {
     args1 = truncateAt(',');
   }
 
   private void getInstruction() {
     cmd = cmd.trim();
     Instruction = truncateAt(' ');
    Instruction = Instruction.replaceAll("\\.", "");
     assignModifier();
   }
 
   private void assignModifier() {
    switch (Instruction.charAt(Instruction.length() - 1)) {
       case 'u':
         modifier = 1;
        Instruction = Instruction.substring(0, Instruction.length() - 1);
         break;
       case 'h':
         modifier = 2;
        Instruction = Instruction.substring(0, Instruction.length() - 1);
         break;
       case ':':
        label = Instruction.substring(0, Instruction.length() - 1);
        label = label.trim();
       default:
         modifier = 0;
     }
   }
 
   private String truncateAt(char cha) {
     if (cmd.length() > 1) {
       int index = cmd.indexOf(cha);
       String result;
       if (index > 0) {
         result = cmd.substring(0, index);
         result = result.trim();
         cmd = cmd.substring(index + 1, cmd.length());
         cmd = cmd.trim();
       } else {
         result = cmd.trim();
       }
       return result;
     } else return "";
   }
 
   private String getClassName() {
     return "CMDS_" + Instruction.toUpperCase();
   }
 
   //accessor functions
   public short getModifier() {
     return modifier;
   }
 
   public String getArgs3() {
     return args3;
   }
 
   public String getArg1() {
     return args1;
   }
 
   public boolean isImmediate() {
     return isImmediate;
   }
 
   public int getImmediate() {
     return immediate;
   }
 
   public String getLabel() {
     if (isLabel()) return label;
     else return "";
 
   }
 
   public String getArg2() {
     return args2;
   }
 
   public void pushImmediate(String regI, int imm) {
     Main.register.set(regI, imm);
   }
 
   public void assign(String reg1, String reg2) {
     int valueAt_reg2 = Main.register.get(reg2);
     pushImmediate(reg1, valueAt_reg2);
   }
 }
