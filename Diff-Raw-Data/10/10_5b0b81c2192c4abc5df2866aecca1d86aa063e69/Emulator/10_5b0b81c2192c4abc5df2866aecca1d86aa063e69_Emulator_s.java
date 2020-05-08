 package ch.zhaw.mppce;
 
 import ch.zhaw.mppce.compiler.instructions.Instruction;
 import ch.zhaw.mppce.cpu.CPU;
 import ch.zhaw.mppce.cpu.Memory;
 import ch.zhaw.mppce.gui.PcEmuGUI;
 
 /**
  * Created with IntelliJ IDEA.
  * User: bbu
  * Date: 14.10.12
  * Time: 21:58
  */
 public class Emulator {
     // Startwerte des Memories
     private static final int PROGRAM_START = 100;
     private static final int DATA_START = 500;
 
     // Instance Variables
     private CPU cpu;
     private PcEmuGUI gui;
     private Memory programMemory;
     private int commandCounter;
 
     // Constructor
     public Emulator(CPU cpu, PcEmuGUI gui) {
         this.cpu = cpu;
         this.gui = gui;
         this.programMemory = cpu.getProgramMemory();
 
         cpu.setCommandCounter(PROGRAM_START);
     }
 
     // Run the program stored in programMemory
     public void run() {
         String end = "";
         while (!end.equals("END")) {
             Instruction instr = programMemory.getCommand(Integer.toString(cpu.getCommandPointer()));
             if (instr != null) {
                 instr.doIt(cpu);
                 end = instr.getClass().getSimpleName();
             }
             cpu.incCommandCounter();
 
 
             // Print Accumulator & Registers
             cpu.printAccumulator();
             cpu.printRegisters();
 
             // Print Memory
             //cpu.printProgramMemory();
             //cpu.printDataMemory();
 
         }
     }
 }
