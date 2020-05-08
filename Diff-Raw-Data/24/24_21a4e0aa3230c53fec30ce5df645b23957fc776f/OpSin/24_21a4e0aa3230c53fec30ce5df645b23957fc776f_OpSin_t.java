 package jgpfun.genetics.lgp.operations;
 
import jgpfun.world2d.Organism2d;

 /**
  *
  * @author dahmen
  */
 public class OpSin implements Operation, UnaryOperation {
 
     @Override
     public int execute(int src1, int src2) {
        return (int)(Math.sin(src1 / Organism2d.intScaleFactor) * Organism2d.intScaleFactor);
     }
     
 }
