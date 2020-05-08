 package ch.zhaw.mppce.compiler.instructions;
 
 import ch.zhaw.mppce.cpu.CPU;
 import ch.zhaw.mppce.cpu.Memory;
 import ch.zhaw.mppce.cpu.Register;
 import ch.zhaw.mppce.tools.Tools;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bbu
  * Date: 13.10.12
  * Time: 16:04
  * <p/>
  * LWDD
  * <p/>
  * In das Register mit der Nummer xx (00 bis 11 für Akku, R-1, R-2 bzw. R-3) wird
  * der Inhalt der Speicherzellen Adr und Adr + 1 (1 Wort = 2 Byte) geladen.
  * Mit 10 Bit können 1KiB Speicher adressiert werden.
  */
 public class LWDD extends Instruction {
     // Instance Vars
 
     // Constructor
     public LWDD(String parameters) {
         super(parameters);
     }
 
     // Methods
     @Override
     public void doIt(CPU cpu) {
         /**
          *  Parse Parameters, we need a:
          *  - Register R1, R2 or R3
          *  - Value of Address + Address+1
          */
         Tools tools = new Tools();
         String params = getParameters();
         Memory dataMemory = cpu.getDataMemory();
 
         // Get Register from Params
         Register registerData = tools.getRegisterFromParams(cpu, params);
 
         // Get address from Params
         int address = tools.getAddressFromParams(params);
 
         // Get values from address and address + 1
         String value1 = dataMemory.getValue(Integer.toString(address));
        String value2 = dataMemory.getValue(Integer.toString(address + 1));
 
         if (value2 == null)
             value2 = "00000000";
 
         // Concatenate the two strings together to get a 16bit string
         String totalValue = value2 + value1;
 
         // Write it into the Register
         registerData.setRegister(totalValue);
 
         // Increase command counter
         cpu.incCommandPointer();
 
     }
 
     @Override
     public String convertToOpcode(Memory dataMemory) {
         String address;
         String value;
         Tools tools = new Tools();
 
         Memory data = dataMemory;
         String[] params = getParameters().split(" ");
         String register = convertRegister(Integer.parseInt(params[1].replace("R", "").replace(",", "")));
 
         if (params[2].contains("#")) {
             // Get Value from Address
             address = params[2].replace("#", "");
             value = data.getValue(address);
             if (value == null)   // TODO: that happens because we run this command before there is data in the data memory
                 value = "0000000000";
         } else {
             value = tools.convertToBin(Integer.valueOf(params[2]), 10);
         }
 
         return "010-" + register + value;
     }
 }
