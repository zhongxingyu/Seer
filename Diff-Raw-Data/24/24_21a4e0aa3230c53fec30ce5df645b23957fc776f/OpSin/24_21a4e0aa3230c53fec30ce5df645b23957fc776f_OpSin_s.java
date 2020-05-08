 package jgpfun.genetics.lgp.operations;
 
 /**
  *
  * @author dahmen
  */
 public class OpSin implements Operation, UnaryOperation {
 
     @Override
     public int execute(int src1, int src2) {
        return (int)(Math.sin(src1 / 65535.0) * 65535);
     }
     
 }
