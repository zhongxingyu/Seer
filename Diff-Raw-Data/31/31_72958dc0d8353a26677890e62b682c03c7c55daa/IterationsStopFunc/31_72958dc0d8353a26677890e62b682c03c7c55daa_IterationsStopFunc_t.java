 
 package tajmi.instances.som;
 
 import tajmi.abstracts.som.StopFunc;
 import tajmi.som.SOMParams;
 
 /**
  * 
  * @author badi
  */
 public class IterationsStopFunc extends StopFunc {
 
     private int iterations;
 
     public IterationsStopFunc setIterations(int iterations) {
         this.iterations = iterations;
         return this;
     }
 
     public Boolean call() {
         SOMParams state = getState();
 
        if (state.iterations <= this.iterations)
             return false;
         else return true;
     }
 
 }
