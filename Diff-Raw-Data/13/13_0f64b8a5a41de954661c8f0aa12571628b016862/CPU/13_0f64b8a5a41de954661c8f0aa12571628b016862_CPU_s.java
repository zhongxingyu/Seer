 package sos;
 
 import java.util.*;
 
 /**
  * This class is the centerpiece of a simulation of the essential hardware of a
  * microcomputer.  This includes a processor chip, RAM and I/O devices.  It is
  * designed to demonstrate a simulated operating system (SOS).
  *
  * @see RAM
  * @see SOS
  * @see Program
  * @see Sim
  */
 
 public class CPU
 {
     
     //======================================================================
     //Constants
     //----------------------------------------------------------------------
 
     //These constants define the instructions available on the chip
     public static final int SET    = 0;    /* set value of reg */
     public static final int ADD    = 1;    // put reg1 + reg2 into reg3
     public static final int SUB    = 2;    // put reg1 - reg2 into reg3
     public static final int MUL    = 3;    // put reg1 * reg2 into reg3
     public static final int DIV    = 4;    // put reg1 / reg2 into reg3
     public static final int COPY   = 5;    // copy reg1 to reg2
     public static final int BRANCH = 6;    // goto address in reg
     public static final int BNE    = 7;    // branch if not equal
     public static final int BLT    = 8;    // branch if less than
     public static final int POP    = 9;    // load value from stack
     public static final int PUSH   = 10;   // save value to stack
     public static final int LOAD   = 11;   // load value from heap
     public static final int SAVE   = 12;   // save value to heap
     public static final int TRAP   = 15;   // system call
     
     //These constants define the indexes to each register
     public static final int R0   = 0;     // general purpose registers
     public static final int R1   = 1;
     public static final int R2   = 2;
     public static final int R3   = 3;
     public static final int R4   = 4;
     public static final int PC   = 5;     // program counter
     public static final int SP   = 6;     // stack pointer
     public static final int BASE = 7;     // bottom of currently accessible RAM
     public static final int LIM  = 8;     // top of accessible RAM
     public static final int NUMREG = 9;   // number of registers
 
     //Misc constants
     public static final int NUMGENREG = PC; // the number of general registers
     public static final int INSTRSIZE = 4;  // number of ints in a single instr +
                                             // args.  (Set to a fixed value for simplicity.)
 
     //======================================================================
     //Member variables
     //----------------------------------------------------------------------
     /**
      * specifies whether the CPU should output details of its work
      **/
     private boolean m_verbose = true;
 
     /**
      * This array contains all the registers on the "chip".
      **/
     private int m_registers[];
 
     /**
      * A pointer to the RAM used by this CPU
      *
      * @see RAM
      **/
     private RAM m_RAM = null;
     
     /**
      * The initial address of the stack
      **/
    private int m_stackSize = 0;
 
     //======================================================================
     //Methods
     //----------------------------------------------------------------------
 
     /**
      * CPU ctor
      *
      * Intializes all member variables.
      */
     public CPU(RAM ram)
     {
         m_registers = new int[NUMREG];
         for(int i = 0; i < NUMREG; i++)
         {
             m_registers[i] = 0;
         }
         m_RAM = ram;
 
     }//CPU ctor
 
     /**
      * getPC
      *
      * @return the value of the program counter
      */
     public int getPC()
     {
         return m_registers[PC];
     }
 
     /**
      * getSP
      *
      * @return the value of the stack pointer
      */
     public int getSP()
     {
         return m_registers[SP];
     }
 
     /**
      * getBASE
      *
      * @return the value of the base register
      */
     public int getBASE()
     {
         return m_registers[BASE];
     }
 
     /**
      * getLIMIT
      *
      * @return the value of the limit register
      */
     public int getLIM()
     {
         return m_registers[LIM];
     }
 
     /**
      * getRegisters
      *
      * @return the registers
      */
     public int[] getRegisters()
     {
         return m_registers;
     }
 
     /**
      * setPC
      *
      * @param v the new value of the program counter
      */
     public void setPC(int v)
     {
         m_registers[PC] = v;
     }
 
     /**
      * setSP
      *
      * @param v the new value of the stack pointer
      */
     public void setSP(int v)
     {
         m_registers[SP] = v;
     }
 
     /**
      * setBASE
      *
      * @param v the new value of the base register
      */
     public void setBASE(int v)
     {
         m_registers[BASE] = v;
     }
 
     /**
      * setLIM
      *
      * @param v the new value of the limit register
      */
     public void setLIM(int v)
     {
         m_registers[LIM] = v;
     }
 
     /**
      * regDump
      *
      * Prints the values of the registers.  Useful for debugging.
      */
     private void regDump()
     {
         for(int i = 0; i < NUMGENREG; i++)
         {
             System.out.print("r" + i + "=" + m_registers[i] + " ");
         }//for
         System.out.print("PC=" + m_registers[PC] + " ");
         System.out.print("SP=" + m_registers[SP] + " ");
         System.out.print("BASE=" + m_registers[BASE] + " ");
         System.out.print("LIM=" + m_registers[LIM] + " ");
         System.out.println("");
     }//regDump
 
     /**
      * printIntr
      *
      * Prints a given instruction in a user readable format.  Useful for
      * debugging.
      *
      * @param instr the current instruction
      */
     public void printInstr(int[] instr)
     {
             switch(instr[0])
             {
                 case SET:
                     System.out.println("SET R" + instr[1] + " = " + instr[2]);
                     break;
                 case ADD:
                     System.out.println("ADD R" + instr[1] + " = R" + instr[2] + " + R" + instr[3]);
                     break;
                 case SUB:
                     System.out.println("SUB R" + instr[1] + " = R" + instr[2] + " - R" + instr[3]);
                     break;
                 case MUL:
                     System.out.println("MUL R" + instr[1] + " = R" + instr[2] + " * R" + instr[3]);
                     break;
                 case DIV:
                     System.out.println("DIV R" + instr[1] + " = R" + instr[2] + " / R" + instr[3]);
                     break;
                 case COPY:
                     System.out.println("COPY R" + instr[1] + " = R" + instr[2]);
                     break;
                 case BRANCH:
                     System.out.println("BRANCH @" + instr[1]);
                     break;
                 case BNE:
                     System.out.println("BNE (R" + instr[1] + " != R" + instr[2] + ") @" + instr[3]);
                     break;
                 case BLT:
                     System.out.println("BLT (R" + instr[1] + " < R" + instr[2] + ") @" + instr[3]);
                     break;
                 case POP:
                     System.out.println("POP R" + instr[1]);
                     break;
                 case PUSH:
                     System.out.println("PUSH R" + instr[1]);
                     break;
                 case LOAD:
                     System.out.println("LOAD R" + instr[1] + " <-- @R" + instr[2]);
                     break;
                 case SAVE:
                     System.out.println("SAVE R" + instr[1] + " --> @R" + instr[2]);
                     break;
                 case TRAP:
                     System.out.print("TRAP ");
                     break;
                 default:        // should never be reached
                     System.out.println("?? ");
                     break;          
             }//switch
 
     }//printInstr
 
 
     //<insert method header here>
     public void run()
     {
    	m_stackSize = getSP();
     	while(m_registers[PC] < m_registers[LIM]) {
     		int[] inst = m_RAM.fetch(getPC());
     		
     		if(m_verbose){
     			regDump();
     			printInstr(inst);
     		}
     		
     		switch(inst[0]){
     			case SET: setHelper(inst[1], inst[2]);
     				break;
     			case ADD: addHelper(inst[1], inst[2], inst[3]);
     				break;
     			case SUB: subHelper(inst[1], inst[2], inst[3]);
     				break;
     			case MUL: mulHelper(inst[1], inst[2], inst[3]);
     				break;
     			case DIV: divHelper(inst[1], inst[2], inst[3]);
     				break;
     			case COPY: copyHelper(inst[1], inst[2]);
     				break;
     			case BRANCH: branchHelper(inst[1]);
     				break;
     			case BNE: bneHelper(inst[1], inst[2], inst[3]);
     				break;
     			case BLT: bltHelper(inst[1], inst[2], inst[3]);
     				break;
     			case POP: popHelper(inst[1]);
     				break;
     			case PUSH: pushHelper(inst[1]);
     				break;
     			case LOAD: loadHelper(inst[1], inst[2]);
     				break;
     			case SAVE: saveHelper(inst[1], inst[2]);
     				break;
     			case TRAP: return;//trapHelper();
     				//break;
     			//default case?
     			
     		} //switch
     		setPC(getPC()+INSTRSIZE);
     	} //while
     }//run
     
     private void setHelper(int arg1, int arg2) {
     	m_registers[arg1] = arg2;
     } //setHelper
     
     private void addHelper(int arg1, int arg2, int arg3) {
     	m_registers[arg1] = m_registers[arg2] + m_registers[arg3];
     } //addHelper
     
     private void subHelper(int arg1, int arg2, int arg3) {
     	m_registers[arg1] = m_registers[arg2] - m_registers[arg3];
     }//subHelper
     
     private void mulHelper(int arg1, int arg2, int arg3) {
     	m_registers[arg1] = m_registers[arg2] * m_registers[arg3];
     } //mulHelper
     
     private void divHelper(int arg1, int arg2, int arg3) {
     	m_registers[arg1] = m_registers[arg2] / m_registers[arg3];
     } //divHelper
     
     private void copyHelper(int arg1, int arg2) {
     	m_registers[arg1] = m_registers[arg2];
     }//copyHelper
     
     private void branchHelper(int arg1) {
     	checkRAM(arg1);
     	setPC(arg1-3);
     }//branchHelper
     
     private void bneHelper(int arg1, int arg2, int arg3) {
     	if (m_registers[arg1] != m_registers[arg2]) {
     		checkRAM(arg3);
     		// Our PC starts at 1, and we need to offset this command before
     		// we increment the PC in the main switch statement
     		setPC(arg3-3);
     	}
     }//bneHelper
     
     private void bltHelper(int arg1, int arg2, int arg3) {
     	if (m_registers[arg1] < m_registers[arg2]){
     		checkRAM(arg3);
     		setPC(arg3-3);
     	}
     }//bltHelper
     
 	private void popHelper(int arg1) {
		if (getSP() < m_stackSize) {
 			System.out.println("Accessing stack at invalid memory address " + getSP());
 			System.exit(-1);
 		}
 		// Do we need to check stacksize anywhere else?
 		m_registers[arg1] = m_RAM.read(getSP());
 		setSP(getSP() - 1);
 	}//popHelper
 	
 	private void pushHelper(int arg1) {
 		setSP(getSP()+1);
 		checkRAM(getSP());
		m_RAM.write(getSP(), arg1);
 	}//pushHelper
 	
 	private void loadHelper(int arg1, int arg2) {
 		checkRAM(m_registers[arg2]);
 		m_registers[arg1] = m_RAM.read(m_registers[arg2]);
 	}//loadHelper
 	
 	private void saveHelper(int arg1, int arg2) {
 		checkRAM(m_registers[arg1]);
 		m_RAM.write(m_registers[arg2], m_registers[arg1]);
 	} //saveHelper  
 
 	private void checkRAM (int addr){
 		if ((addr > m_registers[LIM]) || (addr < m_registers[BASE])) {
 			System.out.println("Error: Invalid memory accessed at " + addr + ", the PC was " + PC + ".");
 			System.exit(-1);
 		}
 	}//checkRAM
 };//class CPU
