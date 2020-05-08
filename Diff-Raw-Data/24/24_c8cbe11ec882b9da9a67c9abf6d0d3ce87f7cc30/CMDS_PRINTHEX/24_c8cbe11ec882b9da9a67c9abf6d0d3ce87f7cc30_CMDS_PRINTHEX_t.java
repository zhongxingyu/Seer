 /**
  * User: moulikrishna.
  * Institute: IIT Delhi.
  * purpose: As part of course requirements CSL211 Computer Architecture.
  * Submission date: 15th September, 2013
  */
 public class CMDS_PRINTHEX extends RISCInstruction {
 
   public static void exeCmd(RISCInstruction risc) {
     //get sp and immediate(the count of sps to print)
     int stackStart = Main.register.get(risc.getArg1());
     int count = risc.getProperImm(3);
     String strObtaines = "0x ";
 
     //iterating to create a string to be printed
     while (count-- > 0) {
       int valueAtAddress = Main.stack.getValueAt(stackStart++);
      String add = Integer.toHexString(valueAtAddress);
      if (add.length() == 1) add += "0";
      strObtaines += add + " ";
     }
 
 
     Debug.forceWrite(strObtaines);
   }
 }
