 import java.util.StringTokenizer;
 
 /**
  * User: moulikrishna.
  * Institute: IIT Delhi.
  * purpose: As part of course requirements CSL211 Computer Architecture.
  * Submission date: 15th September, 2013
  */
 public class CMDS_READ extends RISCInstruction {
 
   public static void exeCmd(RISCInstruction risc) {
     //Create a string tokenizer with the input from Main.getNextLine();
     String input = Main.readNextLine();
    input = input.replace("0x", "");
     StringTokenizer st = new StringTokenizer(input, " ");
 
 
     //iterate through st and populate all the values.
     int index = 0;
     int address = Main.register.get(risc.getArg2());
     while (st.hasMoreTokens()) {
       //get the address to store value sp -
       address--;
 
       //get the value t be stored
      String nextToken = st.nextToken();
      int valueToStore = Integer.parseInt(nextToken, 16);
 
       //store value int the given address.
       Main.stack.setValueAt(address, valueToStore);
       index++;
     }
 
     //setting the register sent as args2 as index
     risc.pushImmediate(risc.getArg1(), index);
   }
 }
