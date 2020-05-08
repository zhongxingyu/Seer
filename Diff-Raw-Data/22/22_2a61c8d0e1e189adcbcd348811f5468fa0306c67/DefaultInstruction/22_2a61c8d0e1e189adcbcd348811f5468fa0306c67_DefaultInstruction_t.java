 package br.com.leandromoreira.jdcpu16br;
 
 public abstract class DefaultInstruction implements Instruction {
    public static final int DEFAULT_SIZE_INSTRUCTION = 1;
 
    protected int defaultSumToNextInstruction = DEFAULT_SIZE_INSTRUCTION;
 
     @Override
     public abstract void execute();
 
     @Override
     public int cycles() {
        return DEFAULT_SIZE_INSTRUCTION;
     }
 
     @Override
     public int sumToPC() {
         return defaultSumToNextInstruction;
     }
 }
