 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cpu.instruction;
 
 import inputreader.InstructionInputFileReader;
 import statistics.AddressBlock;
 
 /**
  *
  * @author naclaeys
  */
 public class InstructionThread {
     
     private long id;
     private InstructionInputFileReader reader;
     
     private Instruction instruction;
     private AddressBlock prevBlock;
     private long waitingTime;
     
     public InstructionThread(long id, InstructionInputFileReader reader) {
         this.id = id;
         this.reader = reader;
         instruction = null;
         waitingTime = 0;
         // dummy temp block
         prevBlock = new AddressBlock(0, 0);
     }
 
     public long getId() {
         return id;
     }
 
     public Instruction setNextInstruction() {
         instruction = reader.getInstructionFromThread(id);
         return instruction;
     }
 
     public Instruction getInstruction() {
         return instruction;
     }
     
     public AddressBlock getPrevBlock() {
         return prevBlock;
     }
 
     public void setPrevBlock(AddressBlock prevBlock) {
         this.prevBlock = prevBlock;
     }
     
     public long getAddressIndex(long addressBlockSize) {
        Address a = instruction.getInstructionAdress();
        return a.divideBy(addressBlockSize);
     }
 
     public void setWaitingTime(long waitingTime) {
         this.waitingTime = waitingTime;
     }
     
     public void decreaseWaitingTime() {
         if(waitingTime > 0) {
             waitingTime--;
         }
     }
 
     public long getWaitingTime() {
         return waitingTime;
     }
     
 }
