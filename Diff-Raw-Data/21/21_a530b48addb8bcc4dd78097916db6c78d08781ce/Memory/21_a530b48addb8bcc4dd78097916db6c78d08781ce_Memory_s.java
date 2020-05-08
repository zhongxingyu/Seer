 import java.util.EmptyStackException;
 
 public class Memory {
 
     public int[] mem;
    //public int SP;  
     public int memorySize;
 
     public Memory(int memorySize) {
         /* create the mem with specified memorySize */
         this.mem = new int[memorySize];
         this.memorySize = memorySize;
         /* point SP to one position above the mem */
         setSP(this.mem.length);
        //this.SP = this.mem.length;
     }
 
     public void push(int val) {
         if (getSP() <= 0) {  /* full mem */
             System.err.println("Can't push another value--full mem.");
             System.exit(1);
         } else {
             setSP(getSP()-1);
             this.mem[getSP()] = val;
         }
     }
 
     public int pop() {
         if (getSP() >= this.memorySize) {  /* empty mem */
             throw new EmptyStackException();  
         } else {
             setSP(getSP()+1);
             return this.mem[getSP()-1];
         }
     }
 
     public int getContents(int addr) {  
         /* get the contents at a specific address */
         if (0 > addr || addr > this.memorySize-1) {
             System.err.println("ERROR: Attempt to get a value from an address out of range");
             System.err.println("  Illegal Address: " + addr);
             System.exit(1);
         }
 
         return this.mem[addr];
     }
 
     public void putContents(int addr, int value) {
         /* *addr = val */
         if (0 > addr || addr > this.memorySize-1) {
             System.err.println("ERROR: Attempt to put a value into an address out of range");
             System.err.println("  Illegal Address: " + addr);
             System.exit(1);
         } 
 
         this.mem[addr] = value;
     }
 
     public int getSP() {
         return getContents(0);
     }
 
     public void setSP(int num) {
         putContents(0, num);
     }
 
     public void reveal() { 
         /* prints out the stack much like how our brains imagine what a stack
          * looks like */
         for (int pointer = memorySize-1; pointer >= 15990; pointer--) { 
             /* start from the top of the stack and print adresses & value pairs
              * all the way down to SP -- also print out an arrow to where SP
              * points */
             System.out.format("%2d| %2s%s\n", pointer, this.mem[pointer], 
                     (pointer == getSP()) ? " <-- SP" : "");
         }
     }
 
 }
