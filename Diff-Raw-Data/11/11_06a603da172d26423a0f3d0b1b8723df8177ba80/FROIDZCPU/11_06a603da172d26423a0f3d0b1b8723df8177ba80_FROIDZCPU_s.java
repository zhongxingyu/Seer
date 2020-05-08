 package emulator.cpu;
    
 /**
  * FROIDCPU
  * 
  * A FROIDCPU understands what it means to be an actor in a FROIDZ Simulation.
  * FROIDCPU's act method takes a time, which it converts into cycles and then
  * gives to the processor. The cycleDebt is kept track of here.
  * 
  * @author Jacob Weiss, Alex Teiche
  * @version 0.0.2
  */
 
 public abstract class FROIDZCPU
 
 {
     // This' processor
     public Processor proc;
     
     // How many cycles extra have been used
     // This is subtracted from the number total we can use
     private int cycleDebt = 0;
     
     public FROIDZCPU()
     {
     }
     public FROIDZCPU(String path)
     {
         this.proc.mem.loadBin(path);        
     }
     
     abstract public void connectToSerial(IAsynchronousUSART part, int i);
     abstract public void connectToPWM(PinConnector part, int i);
  
     /**
      * Tell the CPU to do things
      * @param time Milliseconds for the CPU to do things
      */
     public void act(int time)
     {
         // Execute a certain number of cycles on the processor
         this.cycleDebt = this.proc.run((time * proc.getClockSpeed()) - this.cycleDebt);
     }
     
     // public getter methods
     
     public Processor getProcessor()
     {
         return this.proc;
     } 
 }
