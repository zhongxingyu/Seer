 package br.com.leandromoreira.jdcpu16br;
 
 import static br.com.leandromoreira.jdcpu16br.OpCodes.*;
 
 public class InstructionTable {
 
     private static final int NUMBER_OF_INSTRUCTIONS = 0x10;
     private static final int ZERO = 0;
     private static final HexaFormatter formatter = new HexaFormatter();
 
     public Instruction[] instructionSet(final CPU cpu) {
         final Instruction[] instruction = new Instruction[NUMBER_OF_INSTRUCTIONS];
 
         instruction[NOT_BASIC] = new DefaultInstruction() {
 
             private int cycles = 2;
             private String assembler = "";
             private final Syscall[] syscalls = new Syscall[0b111111];
             private boolean syscallsAlreadyFilled = false;
 
             @Override
             public void execute() {
                 final int syscallOpCode = cpu.getCurrentWord().a();
                 final int a = cpu.getCurrentWord().b();
                 final ParameterDecoder aDecoded = cpu.decoderFor(a);
                 final Syscall newInstruction = syscalls()[syscallOpCode];
 
                 assembler = (newInstruction != null) ? newInstruction.execute(aDecoded.read()) : "RESERVED " + formatter.toHexadecimal(aDecoded.read());
             }
 
             public Syscall[] syscalls() {
                 if (!syscallsAlreadyFilled) {
                     syscalls[SYSCALL_JSR] = new Syscall() {
 
                         @Override
                         public String execute(final int a) {
                             cpu.setStackPointer(cpu.getProgramCounter() + 1);
                             cpu.setProgramCounter(a);
                             defaultSumToNextInstruction = ZERO;
                             return "JSR " + formatter.toHexadecimal(a);
                         }
                     };
                     syscallsAlreadyFilled = true;
                 }
                 return syscalls;
             }
 
             @Override
             public int cycles() {
                 return cycles;
             }
 
             @Override
             public String toString() {
                 return assembler;
             }
         };
         instruction[SET] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.parameterA().write(cpu.parameterB().read());
             }
         };
         instruction[ADD] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 final int newValue = cpu.parameterA().read() + cpu.parameterB().read();
                 final int newOverflow = (newValue > 0xFFFF) ? 0x0001 : 0x0000;
                 cpu.parameterA().write(newValue & 0xFFFF);
                 cpu.setOverflow(newOverflow);
             }
 
             @Override
             public int cycles() {
                 return 2;
             }
         };
         instruction[SUB] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 final int newValue = cpu.parameterA().read() - cpu.parameterB().read();
                 final int newOverflow = (newValue < 0x0000) ? 0xFFFF : 0x0000;
                 cpu.parameterA().write(newValue & 0xFFFF);
                 cpu.setOverflow(newOverflow);
             }
 
             @Override
             public int cycles() {
                 return 2;
             }
         };
         instruction[MUL] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.parameterA().write(cpu.parameterA().read() * cpu.parameterB().read());
                 cpu.setOverflow((cpu.parameterA().read() >> 16) & 0xFFF);
             }
 
             @Override
             public int cycles() {
                 return 2;
             }
         };
         instruction[DIV] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 if (cpu.parameterB().read() != ZERO) {
                     cpu.parameterA().write(cpu.parameterA().read() / cpu.parameterB().read());
                     cpu.setOverflow(((cpu.parameterA().read() << 16) / cpu.parameterB().read()) & 0xFFFF);
                 } else {
                     cpu.parameterA().write(ZERO);
                     cpu.setOverflow(ZERO);
                 }
             }
 
             @Override
             public int cycles() {
                 return 3;
             }
         };
         instruction[MOD] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 final int mod = (cpu.parameterB().read() == 0) ? ZERO : cpu.parameterA().read() % cpu.parameterB().read();
                 cpu.parameterA().write(mod);
             }
 
             @Override
             public int cycles() {
                 return 3;
             }
         };
         instruction[SHL] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.setOverflow(((cpu.parameterA().read() << cpu.parameterB().read()) >> 16) & 0xFFFF);
                 cpu.parameterA().write(cpu.parameterA().read() << cpu.parameterB().read());
             }
 
             @Override
             public int cycles() {
                 return 2;
             }
         };
         instruction[SHR] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.setOverflow(((cpu.parameterA().read() << 16) >> cpu.parameterB().read()) & 0xFFFF);
                 cpu.parameterA().write(cpu.parameterA().read() >> cpu.parameterB().read());
             }
 
             @Override
             public int cycles() {
                 return 2;
             }
         };
         instruction[AND] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.parameterA().write(cpu.parameterA().read() & cpu.parameterB().read());
             }
         };
         instruction[BOR] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.parameterA().write(cpu.parameterA().read() | cpu.parameterB().read());
             }
         };
         instruction[XOR] = new DefaultInstruction() {
 
             @Override
             public void execute() {
                 cpu.parameterA().write(cpu.parameterA().read() ^ cpu.parameterB().read());
 
             }
         };
         instruction[IFE] = new DefaultInstruction() {
 
             private int costOfFaling = 0;
 
             @Override
             public void execute() {
                 if (cpu.parameterA().read() != cpu.parameterB().read()) {
                     defaultSumToNextInstruction += nextInstructionSize(cpu, cpu.getProgramCounter() + 1);
                     costOfFaling = 1;
                 } else {
                     costOfFaling = 0;
                 }
             }
 
             @Override
             public int cycles() {
                 return 2 + costOfFaling;
             }
         };
         instruction[IFN] = new DefaultInstruction() {
 
             private int costOfFaling = 0;
 
             @Override
             public void execute() {
                 if (cpu.parameterA().read() == cpu.parameterB().read()) {
                     defaultSumToNextInstruction += nextInstructionSize(cpu, cpu.getProgramCounter() + 1);
                     costOfFaling = 1;
                 } else {
                     costOfFaling = 0;
                 }
             }
 
             @Override
             public int cycles() {
                 return 2 + costOfFaling;
             }
         };
         instruction[IFG] = new DefaultInstruction() {
 
             private int costOfFaling = 0;
 
             @Override
             public void execute() {
                 if (cpu.parameterA().read() < cpu.parameterB().read()) {
                     defaultSumToNextInstruction += nextInstructionSize(cpu, cpu.getProgramCounter() + 1);
                     costOfFaling = 1;
                 } else {
                     costOfFaling = 0;
                 }
             }
 
             @Override
             public int cycles() {
                 return 2 + costOfFaling;
             }
         };
         instruction[IFB] = new DefaultInstruction() {
 
             private int costOfFaling = 0;
 
             @Override
             public void execute() {
                 if ((cpu.parameterA().read() & cpu.parameterB().read()) == ZERO) {
                     defaultSumToNextInstruction += nextInstructionSize(cpu, cpu.getProgramCounter() + 1);
                     costOfFaling = 1;
                 } else {
                     costOfFaling = 0;
                 }
             }
 
             @Override
             public int cycles() {
                 return 2 + costOfFaling;
             }
         };
 
         return instruction;
     }
 
     public int nextInstructionSize(final CPU cpu,final int newProgramCounter) {
         final Word currentWord = new Word(cpu.memory().readFrom(newProgramCounter));
         final ParameterDecoder a = cpu.decoderFor(currentWord.a());
         final ParameterDecoder b = cpu.decoderFor(currentWord.b());
         final Instruction instruction = cpu.getInstructions()[currentWord.code()];
         return (instruction.sumToPC() != 0) ? instruction.sumToPC() + a.size() + b.size() : 0;
     }
 
     private interface Syscall {
 
         String execute(final int a);
     }
 }
