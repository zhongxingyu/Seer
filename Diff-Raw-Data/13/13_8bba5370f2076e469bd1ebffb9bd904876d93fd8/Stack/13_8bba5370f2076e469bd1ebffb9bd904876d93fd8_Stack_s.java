 /* This class represents a stack, although stack purists will disagree.  Let me
  * explain:  A true stack is a data structure in which it is only possible to
  * push and pop values.  This stack supports pushing and popping, but also
 * allows the user to access the stack pointer (SP) value, as well providing
 * the user with the ability to insert values wherever they would like
 * (putContents() method), along with allowing the user to grab values from
 * anywhere on the stack (getContents() method).  These methods made
  * implementing the Stack Machine Emulator far easier. This stack class also
  * provides a reveal() method, in which each element in the stack is printed
 * along-side its location (the stack grows from the top down--the top value is
 * the `highest' location in the stack */
 
 import java.util.EmptyStackException;
 
 public class Stack {
 
     public int SP;  
     // assume stack is full for now and that SP is not contained on the stack
     public int[] stack;
     public int height;
 
     public Stack(int height) {
         this.SP = height;
         this.stack = new int[height];  /* create the stack with specified
                                           height */
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
             /* need to throw an exception because Java complains that we're not
              * returning anything if we just quit */
             throw new EmptyStackException();  
         } else {
             return this.stack[SP++];
         }
     }
 
     public int getContents(int addr) {  
         /* get the contents at a specific address */
         return stack[addr];
     }
 
     public void putContents(int addr, int val) {
         /* *addr = val */
         stack[addr] = val;
     }
 
     public void reveal() { 
         /* prints out the stack much like how our brains imagine what a stack
          * looks like */
         String s = new String();
         String temp;
 
         for (int pointer = height-1; pointer >= 0; pointer--) { 
             /* start from the top of the stack and don't stop until you get
              * past SP */
             System.out.format("%2d| %2s\n", pointer, (pointer == SP) ? stack[pointer] + " <-- SP" : stack[pointer]);
             /* the turnary part prints out an arrow that points to where SP
              * points */
         }
     }
 }
