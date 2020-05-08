 package emulator.cpu; 
 
 import java.util.Scanner;
 import java.util.Iterator;
 import java.util.Arrays;
 import java.io.File;
  
 /**
  * Memory
  * 
  * Represents the memory of a microcontroller device.
  * 
  * Note: For effeciency's sake, it is OK to directly access the 
  *       public instance variables of this class.
  * 
  * @author Jacob Weiss, Alex Teiche
  * @version 0.0.3
  */
 public class Memory
 {
     public char[] registers; // Register File
     public char[] io; // IO Space
     public char[] sram; // Data Space
     public int[]  flash; // Program Space
     public Memory(int regNum, int ioNum, int sramNum, int flashNum)
     {
         this.registers = new char[regNum];
         this.io = new char[ioNum];
         this.sram = new char[sramNum];
         this.flash = new int[flashNum];
     }
     
     /**
      * writeIO()
      * 
      * Writes a value to the io space.
      * 
      * Overriding this method in a subclass can allow the
      * processor to respond to the accessing of io
      * locations.
      * 
      * @param   int     the address
      * @param   char    the data
      */
     public void writeIO(int addr, char data)
     {        
         this.io[addr] = data;
     }
     /**
      * readIO()
      * 
      * Reads a value from the io space.
      * 
      * Overriding this method in a subclass can allow the
      * processor to respond to the accessing of io
      * locations.
      * 
      * @param   int     the address
      * @return  char    the data
      */
     public char readIO(int addr)
     {
         return this.io[addr];
     }
     
     /**
      * write()
      * 
      * Treats the register, io space, and sram as one 
      * continuous block of memory.
      * 
      * @param   int     the address
      * @param   char    the data
      */
     public void write(int addr, char data)
     {
        System.out.println(addr);
         if (addr < this.registers.length)
         {
             this.registers[addr] = data;
         }
         else if (addr < this.io.length)
         {
             this.writeIO(addr, data);
         }
         else if (addr < this.sram.length)
         {
             this.sram[addr] = data;
         }
     }
     /**
      * read()
      * 
      * Treats the register, io space, and sram as one 
      * continuous block of memory.
      * 
      * @param   int     the address
      * @return  char    the data
      */
     public char read(int addr)
     {
         if (addr < this.registers.length)
         {
             return this.registers[addr];
         }
         else if (addr < this.io.length)
         {
             return this.readIO(addr);
         }
         else if (addr < this.sram.length)
         {
             return this.sram[addr];
         }
         throw new java.lang.IndexOutOfBoundsException();
     }
     
     /**
      * loadBin(String path) - Load an assembled binary program from a file
      * 
      * 
      */
     public boolean loadBin(String path)
     {   
         try
         {
             Scanner scanner = new Scanner(new File(path));
             scanner.useDelimiter("\n");
 
             return this.loadInstructions(scanner);
         }
         catch (Throwable e)
         {
             System.out.println("404 File Not Found: " + path);
             System.out.println(e.getMessage());
         }
         return true;
     }
     
     public boolean loadString(String code)
     {
         return this.loadInstructions(Arrays.asList(code.split("\n")).iterator());
     }
     
     /**
      * Loads an iterable of instructions that are strings(with or without spaces), one string per instruction
      * @param instructions The list of instructions to load
      * @returns true -> It succeeded
      */
     public boolean loadInstructions(Iterator<String> instructions)
     {
         int pos = 0;
         
         while (instructions.hasNext())
         {
             this.flash[pos] = (int)Long.parseLong(instructions.next().replace(" ", ""), 2);
                 
             pos++;
                 
             if (pos >= this.flash.length)
             {
                 return false;
             }
         }
         
         System.out.println(pos + " instructions loaded.");
         return true;
     }
 }
