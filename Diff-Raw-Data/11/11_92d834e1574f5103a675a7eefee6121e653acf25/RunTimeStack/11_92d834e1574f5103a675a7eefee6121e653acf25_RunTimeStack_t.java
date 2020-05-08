 package interpreter;
 
 import java.util.Stack;
 import java.util.Vector;
 
 
 public class RunTimeStack {
     private Vector<Integer> runStack;
     private Stack<Integer> framePointers;
 
     public RunTimeStack() {
         runStack = new Vector<Integer>();
         framePointers = new Stack<Integer>();
         framePointers.add(0);
     }
 
     public void dump(){
         System.out.print("[");
         for (int i = 0; i < runStack.size(); i++) {
             if (framePointers.contains(i))
                 System.out.print("][");
             
             System.out.print(i);
         }        
         System.out.println("]");
     }
 
     public int peek() {
         return runStack.lastElement();
     }
 
     public int pop() {
         return runStack.remove(runStack.size()-1);
     }
 
     public void newFrameAt(int offset) {
         framePointers.add(offset);
     }
 
     public void popFrame() {
        // Empty the contents of the frame, and add it's top element back to the 'runStack'
         int returnValue = runStack.lastElement();
        int frameIndex = framePointers.pop();
        int numOfRemovals = runStack.size() - frameIndex;
        for (int i = 0; i < numOfRemovals; i++)
            runStack.remove(frameIndex);

         runStack.add(returnValue);
     }
 
     public int push(int i) {
         runStack.add(i);
         return i;
     }
 
     public Integer push(Integer i) {
         runStack.add(i);
         return i;
     }
 
     public int store(int offset) {
         // Overwrites the value at index 'offset' with the top element of the "stack", which is then removed.
         runStack.set(offset, runStack.lastElement());
         return runStack.remove(runStack.size()-1);
     }
 
     public int load(int offset) {
         runStack.add(runStack.get(offset));
         return runStack.lastElement();
     }
 }
