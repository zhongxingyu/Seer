 package jgpfun.genetics.lgp;
 
 /**
  *
  * @author hansinator
  */
 public class EvoVM extends BaseMachine {
 
     private int pc;
 
     private final OpCode[] program;
 
 
     public EvoVM(int numRegs, OpCode[] program) {
         regs = new int[numRegs];
 
         //normalize program and strip strctural intron code portions
        this.program = normalizeProgram(program, numRegs);//EvoCodeUtils.stripStructuralIntronCode(normalizeProgram(program, numRegs), numRegs);
     }
 
 
     @Override
     public void run() {
         pc = 0;
         while (pc < program.length) {
             execute(pc++);
         }
     }
 
 
     public void execute(int pc) {
         OpCode curop = program[pc];
 
         /*if (op instanceof BranchOperation) {
         if(op.execute(regs[curop.src1], (curop.immediate ? curop.src2 : regs[curop.src2])) != 1) {
         pc++;
         }*/
         /*} else if(op instanceof JumpOp) {
         //fast forward until the next jumptarg is found or program end is reached
         do {
         pc++;
         } while ((pc < program.length) && !(ops[program[pc].op] instanceof JumpTarg));
         } else if(op instanceof JumpTarg) {
         //do nothing*/
         //} else {
         //execute the operation
         regs[curop.trg] = curop.operation.execute(regs[curop.src1], (curop.immediate ? curop.src2 : regs[curop.src2]));
         //}
     }
 
 
     @Override
     public int getProgramSize() {
         return program.length;
     }
 
 }
