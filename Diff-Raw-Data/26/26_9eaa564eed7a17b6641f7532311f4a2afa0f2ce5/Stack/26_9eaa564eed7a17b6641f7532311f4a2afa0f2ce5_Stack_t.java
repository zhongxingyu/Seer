 import java.util.EmptyStackException;
 
 public class Stack {
 
     public int SP;  /* assume stack is full for now and that sp is not
                            contained on the stack */
     public int PC;  /* program counter */
     public int[] stack;
     public int height;
 
     public Stack(int height) {
         this.SP = height;
         this.stack = new int[height];  /* create the stack with specified
                                           height */
         this.PC = 16;
         this.height = height;
     }
 
     public void push(int val) {
         if (SP <= 0) {  /* full stack */
             System.err.println("Can't push another value--full stack.");
             System.exit(1);
         } else {
             this.stack[--SP] = val;
         }
     }
 
     public int pop() {
         if (SP >= height) {  /* empty stack */
             throw new EmptyStackException();  /* need to throw an exception
                                                  because Java complains that
                                                  we're not returning anything
                                                  if we just quit */
         } else {
             return this.stack[SP++];
         }
     }
 
     public int getContents(int addr) {  /* get the contents at a specific
                                            address */
         return stack[addr];
     }
 
     public void putContents(int addr, int val) {
         /* *addr = val */
         stack[addr] = val;
     }
 
    public void printMe() { /* prints out the stack much like how our brains
                                   imagine what a stack looks like */
         String s = new String();
         String temp;
 
        for (int pointer = height-1; pointer >= 0; pointer--) { 
             /* start from the top of the stack and don't stop until you get
              * past SP */
            /* print out an arrow w/ SP, so we can see where SP is */
            System.out.format("%2d| %2s\n", pointer, pointer == SP ? stack[pointer] + " <-- SP" : stack[pointer]);
            //temp = "" + stack[pointer] + (pointer == SP ? " <-- SP" : "") + '\n';
            //s += temp;
         }
 
        //return s;
     }
 }
