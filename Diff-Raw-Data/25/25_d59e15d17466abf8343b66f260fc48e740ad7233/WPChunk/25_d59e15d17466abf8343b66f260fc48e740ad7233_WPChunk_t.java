 package emulator.wp; 
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.List;
 import java.util.HashMap;
 
 /**
  * Represents one assembly operation
  * There is one WPChunk for every operation in the AVR Assembly Definition
  * 
  * @author Jacob Weiss,Alex Teiche 
  * @version 0.0.4
  */
 public class WPChunk
 {   
     private String opName; // The syntax eg. "add"
     private String[] operands; // The operands eg. {"Rd=mnopq", "Rr=abcde"}
     private String[] ranges; // The ranges eg. {"0-Rd-31", "0-Rr-31"}
     private String opCode; // The template eg. "0010 00am nopq bcde"
     
     public WPChunk(String line)
     {
         line = line.replace(" ", "");
         
         String[] parts = line.split("\\|");
         this.opName = parts[0];
         if (!parts[1].equals(""))
         {
             this.operands = parts[1].split(":");
         }
         else
         {
             this.operands = new String[0];
         }
         
         if (!parts[1].equals(""))
         {
             this.ranges = parts[2].split(":");
         }
         else 
         {
             this.ranges = new String[0];
         }
         
         this.opCode = parts[3];
     }
     
     /**
      * generateInstruction()
      * 
      * Takes a list of Binary objects that correspond with the operands of this.
      * 
      * @param List<Binary>  the list of operands
      */
     public Binary generateInstruction(List<Binary> asm) throws InvalidInputException
     {
         // Check to make sure that the number of arguments to this method equals
         // the number of operands that this operation takes.
         
         if (asm.size() != operands.length)
         {
             System.out.println(asm.size() + " " + operands.length);
             throw new InvalidInputException();
         }
         for (int i = 0; i < asm.size(); i++)
         {
             asm.get(i).setMinLength(this.operands[i].split("=")[1].length());
         }
         
         String instruction = this.opCode;
         for (int op = 0; op < operands.length; op++)
         {
             String operandTemplate = this.operands[op].split("=")[1]; // Template of input
             int operandTemplateLength = operandTemplate.length();
             String asmInput = asm.get(op).toBinaryString();  // Instuction operand
             int asmInputLength = asmInput.length();
             for (int i = 0; i < operandTemplate.length(); i++)
             {
                 instruction = instruction.replaceAll(operandTemplate.substring(operandTemplateLength - i - 1, operandTemplateLength - i), 
                                                             asmInput.substring(asmInputLength - i - 1, asmInputLength - i));
             }
         }
         return new Binary("0b" + instruction);
     }
     public Binary generateInstruction(Binary... args)
     {
         return this.generateInstruction(args);
     }
     
     // Public Getter Methods
     public String getOpName()
     {
         return this.opName;
     }    
     public String[] getOperands()
     {
         return this.operands;
     }    
     public String[] getRanges()
     {
         return this.ranges;
     }    
     public String getOpCode()
     {
         return this.opCode;
     }
 }
