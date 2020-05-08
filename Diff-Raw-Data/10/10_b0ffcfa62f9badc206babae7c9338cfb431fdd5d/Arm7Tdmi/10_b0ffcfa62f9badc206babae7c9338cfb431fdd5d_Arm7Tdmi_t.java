 package com.lemoulinstudio.gfa.core.cpu;
 
 import com.lemoulinstudio.gfa.core.memory.GfaMMU;
 import com.lemoulinstudio.gfa.core.memory.MemoryInterface;
 import com.lemoulinstudio.gfa.core.time.Time;
 
 public abstract class Arm7Tdmi {
 
   protected GfaMMU memory;
   protected Time time;
 
 /*
   r13 a.k.a. SP "Stack Pointer"   : used by convention as a base for the stack.
   r14 a.k.a. LR "Link Register"   : used to save the PC address before an interrupt or a subroutine.
   r15 a.k.a. PC "Program Counter" : contains the current address of the execution in the program.
 */
 
   protected ArmReg[][] allRegisters;
 
   public ArmReg[] usrRegisters;
   public ArmReg[] fiqRegisters;
   public ArmReg[] irqRegisters;
   public ArmReg[] svcRegisters;
   public ArmReg[] abtRegisters;
   public ArmReg[] undRegisters;
   public ArmReg[] sysRegisters;
 
   public ArmReg PC;
 
   public ArmReg CPSR; // Contains the condition flag, the mode bits, the state bit, and others not used (reserved).
   //            SPSR; // "Saved Program Status Register" : used to save the cpsr value when an interrupt occurs.
   public ArmReg SPSR_fiq;
   public ArmReg SPSR_svc;
   public ArmReg SPSR_abt;
   public ArmReg SPSR_irq;
   public ArmReg SPSR_und;
 
   public static final int nFlagBit  = 0x80000000; // negative or less than.
   public static final int zFlagBit  = 0x40000000; // zero.
   public static final int cFlagBit  = 0x20000000; // carry or borrow or extends.
   public static final int vFlagBit  = 0x10000000; // overflow.
   public static final int iFlagBit  = 0x00000080; // irq disable.
   public static final int fFlagBit  = 0x00000040; // fiq disable.
   public static final int tFlagBit  = 0x00000020; // (thumb) state bit.
 
   public static final int modeBitsMask = 0x0000001f; // Cover all bits concerning mode.
   public static final int usrModeBits  = 0x00000010; // User mode.
   public static final int fiqModeBits  = 0x00000011; // FIQ mode.
   public static final int irqModeBits  = 0x00000012; // IRQ mode.
   public static final int svcModeBits  = 0x00000013; // Supervisor mode.
   public static final int abtModeBits  = 0x00000017; // Abort mode.
   public static final int undModeBits  = 0x0000001b; // Undefined mode.
   public static final int sysModeBits  = 0x0000001f; // System mode.
 
   public static final int resetVectorAddress                = 0x00000000;
   public static final int undefinedInstructionVectorAddress = 0x00000004;
   public static final int softwareInterrupVectorAddress     = 0x00000008;
   public static final int prefetchAbortVectorAddress        = 0x0000000c;
   public static final int dataAbortVectorAddress            = 0x00000010;
   public static final int irqVectorAddress                  = 0x00000018;
   public static final int fiqVectorAddress                  = 0x0000001c;
 
   public static final int REG_IE_Address  = 0x04000200;
   public static final int REG_IF_Address  = 0x04000202;
   public static final int REG_IME_Address = 0x04000208;
   
   public Arm7Tdmi() {
     initRegisters();
   }
 
   protected ArmReg newArmReg(int v) {
     return new ArmReg(v);
   }
   
   protected final void initRegisters() {
     usrRegisters = new ArmReg[18];
     fiqRegisters = new ArmReg[18];
     irqRegisters = new ArmReg[18];
     svcRegisters = new ArmReg[18];
     abtRegisters = new ArmReg[18];
     undRegisters = new ArmReg[18];
     sysRegisters = new ArmReg[18];
 
     for (int i = 0; i <= 7; i++) {
       ArmReg reg = newArmReg(0);
       usrRegisters[i] = reg;
       fiqRegisters[i] = reg;
       irqRegisters[i] = reg;
       svcRegisters[i] = reg;
       abtRegisters[i] = reg;
       undRegisters[i] = reg;
       sysRegisters[i] = reg;
     }
 
     for (int i = 8; i <= 12; i++) {
       ArmReg reg = newArmReg(0);
       usrRegisters[i] = reg;
       irqRegisters[i] = reg;
       svcRegisters[i] = reg;
       abtRegisters[i] = reg;
       undRegisters[i] = reg;
       sysRegisters[i] = reg;
 
       ArmReg reg_fiq = newArmReg(0);
       fiqRegisters[i] = reg_fiq;
     }
 
     ArmReg r13 = newArmReg(0);
     ArmReg r13_fiq = newArmReg(0);
     ArmReg r13_irq = newArmReg(0);
     ArmReg r13_svc = newArmReg(0);
     ArmReg r13_abt = newArmReg(0);
     ArmReg r13_und = newArmReg(0);
 
     usrRegisters[13] = r13;
     fiqRegisters[13] = r13_fiq;
     irqRegisters[13] = r13_irq;
     svcRegisters[13] = r13_svc;
     abtRegisters[13] = r13_abt;
     undRegisters[13] = r13_und;
     sysRegisters[13] = r13;
 
     ArmReg r14 = newArmReg(0);
     ArmReg r14_fiq = newArmReg(0);
     ArmReg r14_irq = newArmReg(0);
     ArmReg r14_svc = newArmReg(0);
     ArmReg r14_abt = newArmReg(0);
     ArmReg r14_und = newArmReg(0);
 
     usrRegisters[14] = r14;
     fiqRegisters[14] = r14_fiq;
     irqRegisters[14] = r14_irq;
     svcRegisters[14] = r14_svc;
     abtRegisters[14] = r14_abt;
     undRegisters[14] = r14_und;
     sysRegisters[14] = r14;
 
     ArmReg r15 = newArmReg(0);
 
     usrRegisters[15] = r15;
     fiqRegisters[15] = r15;
     irqRegisters[15] = r15;
     svcRegisters[15] = r15;
     abtRegisters[15] = r15;
     undRegisters[15] = r15;
     sysRegisters[15] = r15;
     PC = r15;
 
     CPSR = newArmReg(0);
     usrRegisters[16] = CPSR;
     fiqRegisters[16] = CPSR;
     irqRegisters[16] = CPSR;
     svcRegisters[16] = CPSR;
     abtRegisters[16] = CPSR;
     undRegisters[16] = CPSR;
     sysRegisters[16] = CPSR;
 
     SPSR_fiq = newArmReg(0);
     SPSR_svc = newArmReg(0);
     SPSR_abt = newArmReg(0);
     SPSR_irq = newArmReg(0);
     SPSR_und = newArmReg(0);
 
     usrRegisters[17] = null;
     fiqRegisters[17] = SPSR_fiq;
     irqRegisters[17] = SPSR_irq;
     svcRegisters[17] = SPSR_svc;
     abtRegisters[17] = SPSR_abt;
     undRegisters[17] = SPSR_und;
     sysRegisters[17] = null;
     
     allRegisters = new ArmReg[32][];
     allRegisters[usrModeBits] = usrRegisters;
     allRegisters[fiqModeBits] = fiqRegisters;
     allRegisters[irqModeBits] = irqRegisters;
     allRegisters[svcModeBits] = svcRegisters;
     allRegisters[abtModeBits] = abtRegisters;
     allRegisters[undModeBits] = undRegisters;
     allRegisters[sysModeBits] = sysRegisters;
   }
 
   public void connectToMemory(MemoryInterface memory) {
     this.memory = (GfaMMU) memory;
   }
 
   public void connectToTime(Time time) {
     this.time = time;
   }
 
   public Time getTime() {
     return time;
   }
 
   public String disassembleInstruction(int offset, ExecutionState executionState) {
     return "";
   }
 
   public ArmReg[][] getRegisters() {
     return allRegisters;
   }
 
   public ExecutionState getExecutionState() {
     return (CPSR.isBitSet(tFlagBit) ? ExecutionState.Thumb : ExecutionState.Arm);
   }
 
   public ArmReg getRegister(int registerNumber) {
     return allRegisters[CPSR.get() & modeBitsMask][registerNumber];
   }
 
   public ArmReg getRegister(int registerNumber, int modeBits) {
     return allRegisters[modeBits][registerNumber];
   }
 
   protected void setArmState() {
     CPSR.setOff(tFlagBit);
   }
 
   protected void setThumbState() {
     CPSR.setOn(tFlagBit);
   }
 
   public String getModeName() {
     switch (CPSR.get() & modeBitsMask) {
       case usrModeBits: return "usr";
       case fiqModeBits: return "fiq";
       case irqModeBits: return "irq";
       case svcModeBits: return "svc";
       case abtModeBits: return "abt";
       case undModeBits: return "und";
       case sysModeBits: return "sys";
       default : return "???";
     }
   }
 
   protected void setMode(int modeBits) {
     CPSR.set((CPSR.get() & ~modeBitsMask) | modeBits);
   }
 
   public void reset() {
     // Set all registers to the zero value.
     for (int i = 0; i < allRegisters.length; i++)
       if (allRegisters[i] != null)
 	for (int j = 0; j < allRegisters[i].length; j++)
 	  if (allRegisters[i][j] != null)
 	    allRegisters[i][j].set(0);
 
     svcRegisters[14].set(PC);
     svcRegisters[17].set(PC);
     setMode(svcModeBits);
     CPSR.setOn(iFlagBit | fFlagBit);
     setArmState();
     PC.set(resetVectorAddress);
     
     /* The following lines are used to boot without running the bios. */
    ///*
     PC.set(0x08000000);      // pas ecrit dans la doc
     CPSR.setOff(iFlagBit | fFlagBit);
    getRegister(13, usrModeBits).set(0x03007f00); // et ca non plus
    getRegister(13, irqModeBits).set(0x03007fa0); // et ca non plus
     getRegister(13, svcModeBits).set(0x03007fe0); // et ca non plus
    //*/
   }
 
   public abstract void step();
 
 }
