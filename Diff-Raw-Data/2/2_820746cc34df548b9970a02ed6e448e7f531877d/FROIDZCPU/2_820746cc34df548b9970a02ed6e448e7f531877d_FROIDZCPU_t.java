 package cpu; 
    
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
 public class FROIDZCPU
 {
     // This' processor
     public Processor proc;
     
     // How many cycles extra have been used
     // This is subtracted from the number total we can use
     private int cycleDebt = 0;
     
     public FROIDZCPU()
     {
         Memory mem = new AVRMemory();
         this.proc = new ToastyProcessor(mem, 1, new USART(mem));
     }
     
     public FROIDZCPU(String path)
     {
         Memory mem = new AVRMemory();
         mem.loadBin(path);
         
         this.proc = new ToastyProcessor(mem, 1, new USART(mem));
     }
     
     public void connectToSerial(IUSART part)
     {
         
     }
    public void connectToPin(PinConnector part, int clock, int dataIn, int dataOut)
     {
         
     }
     
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
