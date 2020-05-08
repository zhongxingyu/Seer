 package br.com.leandromoreira.jdcpu16br;
 
 public abstract class DefaultInstruction implements Instruction {
 
    protected int defaultSumToNextInstruction = 1;
 
     @Override
     public abstract void execute();
 
     @Override
     public int cycles() {
        return 1;
     }
 
     @Override
     public int sumToPC() {
         return defaultSumToNextInstruction;
     }
 }
