 package jgpfun.genetics.lgp;
 
 import jgpfun.genetics.lgp.operations.OpAbs;
 import jgpfun.genetics.lgp.operations.OpAdd;
 import jgpfun.genetics.lgp.operations.OpDiv;
 import jgpfun.genetics.lgp.operations.OpMax;
 import jgpfun.genetics.lgp.operations.OpMin;
 import jgpfun.genetics.lgp.operations.OpMod;
 import jgpfun.genetics.lgp.operations.OpMov;
 import jgpfun.genetics.lgp.operations.OpMul;
 import jgpfun.genetics.lgp.operations.OpNeg;
 import jgpfun.genetics.lgp.operations.OpSqrt;
 import jgpfun.genetics.lgp.operations.OpSub;
 import jgpfun.genetics.lgp.operations.Operation;
 
 /**
  *
  * @author hansinator
  */
 public abstract class BaseMachine {
 
     static {
         //compatible instruction set
         //ops = new Operation[]{new OpAdd(), new OpSub(), new OpMul(), new OpDiv(), new OpMod()};
 
         //extended instruction set
         ops = new Operation[]{
                     new OpAdd(),
                     new OpSub(),
                     new OpMul(),
                     new OpDiv(),
                     new OpMod(),
                     new OpSqrt(),
                     new OpNeg(),
                     new OpMin(),
                     new OpMax(),
                     new OpAbs(),
                     //new OpSin(),
                     new OpMov(), //
                 //new OpInc(),
                 //new OpDec(),
                 // OpBranchLt(),
                 //new OpBranchGt()
                 //new JumpOp(),
                 //new JumpTarg()
                 };
     }
 
     static Operation[] ops;
 
     public int[] regs;
 
 
     protected static OpCode[] normalizeProgram(OpCode[] program, int numRegs) {
         for (int i = 0; i < program.length; i++) {
             OpCode curop = program[i];
 
             curop.src1 = Math.abs(curop.src1) % numRegs;
            curop.src2 = curop.immediate ? (curop.src2 / 65535) : (Math.abs(curop.src2) % numRegs);
             curop.trg = Math.abs(curop.trg) % numRegs;
             curop.op = Math.abs(curop.op) % ops.length;
             curop.operation = ops[curop.op];
         }
 
         return program;
     }
 
 
     public abstract void run();
 
 
     public abstract int getProgramSize();
 
 }
