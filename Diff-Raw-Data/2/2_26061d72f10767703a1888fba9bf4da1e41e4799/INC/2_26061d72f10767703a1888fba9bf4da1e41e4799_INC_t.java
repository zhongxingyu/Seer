 package ch.zhaw.mppce.compiler.instructions;
 
 import ch.zhaw.mppce.cpu.CPU;
 import ch.zhaw.mppce.cpu.Memory;
 import ch.zhaw.mppce.cpu.Register;
 import ch.zhaw.mppce.tools.Tools;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bbu
  * Date: 13.10.12
  * Time: 16:02
  */
 public class INC extends Instruction {
 
     @Override
     public void doIt(CPU cpu) {
         Tools tools = new Tools();
         Boolean overflow = false;
         Register accu = cpu.getAccu();
 
         // Get Value in Accumulator);
         String accuValue = accu.getRegister();
 
         // Convert to Dec
        int a = tools.convertToDec(accuValue);
 
         // Check for overflow
         if (a >= 65536) {
             overflow = true;
         }
 
         // +1
         a++;
 
         // Convert to Twos Complement
         accuValue = tools.convertToBin(a);
 
         // Save
         accu.setRegister(accuValue);
 
         // Set carry bit if necessary
         if (overflow)
             accu.setCarryBit();
     }
 
     @Override
     public String convertToOpcode(Memory dataMemory) {
         return "00000001--------";
     }
 }
