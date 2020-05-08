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
         System.out.println("ASSEMBLY COMPLETE: ");
         
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
             if (line.length() == 0)
             {
                 i.remove();
             }
         }
         
     }
     
     /**
      * Parse all .def directives and generate the hashtable
      * This does not do the replacing in the actual code
      */
     private List<String> hash_defs(List<String> lines)
     {
         // This is the output of this method, it won't contain any recognized preprocessor directives
         ArrayList<String> pureAsm = new ArrayList();
         
         for (String l : lines)
         {
             if (l.substring(0, 4).equalsIgnoreCase(".def"))
             {
                 String[] parts = l.split(" ");
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
     
     private List<List<String>> preprocess(List<String> lines)
     {
         lines = this.hash_defs(lines);
             
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
        //Assembler test = new Assembler("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/assembler/go.asm");
        Assembler test = new Assembler("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/cpu/Print.asm");
         
         test.assemble();
         test.write("/Users/alexteiche/Desktop/FROIDZ/java-src/emulator/assembler/go.tst");
     }
 }
