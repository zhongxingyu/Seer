 /*
  * FOR TODAY:
  *  WP FILE AS A JAVA STRING
  *  LABEL
  *  RJMP
  */
 
 /*
  * Missile on 3
  */
 
 package emulator.assembler;
 
 import java.util.Scanner;
 import java.util.Iterator;
 import java.util.List;
 import java.util.LinkedList;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.BufferedWriter;
 import java.util.ArrayList;
 import java.util.Arrays; 
 import java.util.Hashtable;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 import emulator.wp.*;
 
 /**
  * Write a description of class ASMParser here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Assembler
 {
     // The delimiter used to separate instructions in the final code
     public static String SEP = "\n";
 
     private WPParser parser;
     
     // Assembled binary code(output)
     private List<String> binary;
 
     // Unassembled assembly code(input)
     private List<String> lines;
 
     private boolean assembled;
     
     private Hashtable<String, String> defs;
     
     private Pattern isRegister;
     
     /**
      * Create an assembly parser given a path to an assembly file
      * @param path The path to the file
      */
     public Assembler(String path)
     {
         this.defs = new Hashtable();
         this.lines = new LinkedList();
         
         this.isRegister = Pattern.compile("R[0-9]*");
         
         try
         {
             Scanner scanner = new Scanner(new File(path));
 
             while (scanner.hasNextLine())
             {
                 lines.add(scanner.nextLine());
             }
         }
         catch (Throwable e)
         {
             System.out.println("404 File Not Found: " + path);
         }
     }
 
     /**
      * Do the assembling
      * The assembled program will be printed, and stored in the internal storage of this object
      * Any preprocessor directives will be kept track of past assembling this file
      */
     public boolean assemble()
     {   
         this.parser = new WPParser();
 
         // Format the file
         this.format(lines);
 
         // Handle preprocessor directives
         List<List<String>> parsed = new ArrayList();
         parsed = this.preprocess(lines);
 
         // Do the assembling
         List<Binary> insts = new ArrayList();
         
         try
         {       
             insts = this.generateInstructions(parsed);
         }
         catch (Throwable e)
         {
             return false;
         }
         
         // Format the final output
         this.binary = new ArrayList();
         this.binary = finalize(insts);
         
         for (String s : this.binary)
         {
             System.out.print(s);
         }
       
         System.out.println();
         System.out.println("ASSEMBLY COMPLETE");
         
         this.assembled = true;
         return true;
     }
     
     public void write(String path) throws IOException
     {
         assert this.assembled;
         
         FileWriter fstream = new FileWriter(path);
         BufferedWriter out = new BufferedWriter(fstream);
         
         for (String line : this.binary)
         {
             out.write(line);
         }
         
         out.close();
     }
     
     public String generateString()
     {
         assert this.assembled;
         
         StringBuilder output = new StringBuilder();
         
         for (String s : this.binary)
         {
             output.append(s + "\n");
         }
         
         return output.toString();
     }    
         
     /**
      * Remove all newlines
      */
     private void format(List<String> lines)
     {
         Iterator<String> i = lines.iterator();
         String line;
         
         while (i.hasNext())
         {
             line = i.next().trim();
             if ((line.length() == 0) || (line.substring(0, 1).equals(";")))
             {
                 i.remove();
             }
         }
         
     }
     
     /**
      * Parse all .def directives and generate the hashtable
      * This does not do the replacing in the actual code
      * THIS CODE DOES THE FOLLOWING THINGS:
      *  SIDE EFFECT: Modifies this.defs to reflect the defined things
      *  RETURNS: The list of lines, with every .def line removed
      */
     private List<String> hash_defs(List<String> lines)
     {
         // This is the output of this method, it won't contain any recognized preprocessor directives
         ArrayList<String> pureAsm = new ArrayList();
         
         for (String l : lines)
         {
             if (l.substring(0, 4).equalsIgnoreCase(".def"))
             {
                 String[] parts = l.split("[ ,]");
                 String key = parts[1];
                 String value = parts[2];
                 this.defs.put(key, value);
                 System.out.println(".DEF " + key + " AS " + value + " RECOGNIZED.");
             }
             else
             {
                 pureAsm.add(l);
             }
         }
         
         return pureAsm;
     }
     
     /**
      * Parse all labels and place them in the same HashMap as .def directives
      * This must be the last preprocessor method called
      * Precondition: There can be nothing in the input other than instructions and labels
      *      Everything is either an instruction or a label
      */
     private List<String> hash_labels(List<String> lines)
     {
         // This is the output of this method with labels stripped out(lines that contain them are removed, they are still present in instructions)
         ArrayList<String> pureAsm = new ArrayList();
         
         // This points to the predicted location for an instruction when it is loaded into memory
         // It's functionally a line count, excluding lines that are just a label(and .def and newlines but those are stripped out earlier)
         int instructionPointer = 0;
         
         for (String l : lines)
         {
             // We know it's a label if the last character is :
             if (l.substring(l.length() - 1).equals(":"))
             {
                 String label = l.substring(0, l.length() - 1);
                 String addr = "0d" + Integer.toString(instructionPointer);
                 this.defs.put(label, addr);
                 System.out.println("Label: " + label + " calculated to be memory address " + addr);
             }
             else
             {
                 //It wasn't a label, so it was an instruction(see precondition), so increment the instruction pointer
                 instructionPointer++;
                 pureAsm.add(l);
             }
         }
         
         return pureAsm;
     }
             
             
         
     
     private List<List<String>> preprocess(List<String> lines)
     {
         lines = this.hash_labels(this.hash_defs(lines));
             
         // Split a list of strings that are a complete instruction into a list of lists, where each element of each sublist is a string that is one part of an instruction(the op code, argument, etc)
         // ["SBI 0xFF 0x04"] -> [["SBI", "0xFF", "0x04"]]
         List<List<String>> processed = new ArrayList();
         
         // Parse register addresses
         for (String l : lines)
         {
             processed.add(this.parseLine(l));
         }
         
         return processed;
     }
     
     private List<Binary> generateInstructions(List<List<String>> lines) throws InvalidInputException
     {
         ArrayList<Binary> instructions = new ArrayList();
         
         for (List<String> parsed : lines)
         {
             WPChunk op = this.parser.getChunk(parsed.get(0));
             
             // Try and replace any shortcuts with their actual value (.def)
             parsed = this.applyDefs(parsed);
             
             // Change stuff like R17 into its address
             parsed = this.parseRegisterAddresses(parsed);
             
             List<Binary> operands = this.makeBinariesFromOperands(parsed.subList(1, parsed.size()));
             
             Binary instr = op.generateInstruction(operands);
             instructions.add(instr);
         }
         
         return instructions;    
     }
     
     /**
      * Add the preceding colons to every line of Intel Hex and convert the binary objects to Strings
      * @param 
      */
     private List<String> finalize(List<Binary> insts)
     {
         List<String> l = new ArrayList();
         
         // Split into 4 bit chunks for prettyness
         // Convert to strings for writing to a file
         
         for (Binary b : insts)
         {
             // Split into chunks of 4 with spaces
             // These are removed by the loader, but are put here for easy reading by humans
             String line = b.toBinaryString();
             for(int i = 4; i < line.length(); i += 5)
             {
                 line = new StringBuffer(line).insert(i, " ").toString();
             }
             l.add(line + Assembler.SEP);
         }
         
         return l;
     }
     
     private List<String> applyDefs(List<String> instructionParts)
     {
         for (int i = 0; i < instructionParts.size(); i++)
         {
             String part = instructionParts.get(i);
             
             if (this.defs.containsKey(part))
             {
                 System.out.println(this.defs.get(part) + " -> " + instructionParts.get(i));
                 instructionParts.set(i, this.defs.get(part));
             }
             else
             {
                 // If it wasn't defined in this file, see if it's was defined in the m644def.inc file
                 try 
                 {
                     String replacement = "0d" + ToastyIO.class.getDeclaredField(part).get(int.class).toString();
                     System.out.println("REPLACE BASED ON def.inc: " + part + " with " + replacement);
                     
                     instructionParts.set(i, replacement);
                 }
                 catch (Throwable e)
                 {
                    //System.out.println(e);
                 }
             }
 
 
                     
         }
         
         return instructionParts;
     }
     
     /**
      * @param instructionParts Just one line, each element is a part of one assembly instruction(the name, or arguments)
      */
     private List<String> parseRegisterAddresses(List<String> instructionParts)
     {
         List<String> output = new ArrayList();
         
         
         
         for (String part : instructionParts)
         {
             if (this.isRegister.matcher(part).matches())
             {
                 output.add(part.replace("R", "0d"));
             }
             else
             {
                 output.add(part);
             }
         }
         
         return output;  
     }
  
     public static List<String> parseLine(String line)
     {
         List<String> parsed = new ArrayList();
         
         for (String s : line.replace(",", " ").split(" "))
         {
             if (s.length() != 0)
             {
                 parsed.add(s);
             }
         }
        
         return parsed;
 
     }
     
     public List<Binary> makeBinariesFromOperands(List<String> ops)
     {
         List<Binary> bins = new ArrayList();
         
         // JACOBERROR, WORKS HERE/BINARY RETURNS CORRECT VALUE
         for (String s : ops)
         {
             Binary x = new Binary(s);
             bins.add(x);
         }
         System.out.println();
         
         return bins;
     }
     
     public static void test() throws IOException
     {
         Assembler test = new Assembler("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/assembler/thursday.asm");
         //Assembler test = new Assembler("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/cpu/Print.asm");
         
         test.assemble();
         test.write("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/assembler/thursday.tst");
         //return test.generateString();
     }
 }
