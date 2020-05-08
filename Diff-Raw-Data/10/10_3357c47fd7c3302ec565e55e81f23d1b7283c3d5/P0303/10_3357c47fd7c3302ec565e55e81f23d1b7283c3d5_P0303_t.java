 /* 3.3 Imagine a (literal) stack of plates. If the stack gets too high, it 
  * might topple. Therefore, in real life, we would likely start a new stack
  * when the previous stack exceeds some threshold. Implement a data structure
  * SetOfStacks that mimics this. SetOfStacks should be composed of several 
  * stacks and should create a new stack once the previous one exceeds capacity
  * SetOfStacks.push() and SetOfStacks.pop() should behave idetically to a
  * single stack (that is, pop() should return the same values as it would if
  * there were just one single stack).
  *
  * FOLLOW UP
  * Implement a function popAt(int index) which performs a pop operation on a
  * specific sub-stack.
  **/
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 import java.util.NoSuchElementException;
 
 public class P0303 {
    public class SetOfStacks {
       // We use ArrayList here to make use of its resizing feature.
       private ArrayList<Stack<Integer>> buffer;
       private int index;
       private int stackCapacity;
       private Stack<Integer> currentStack;
 
       public SetOfStacks() {
          this(10);
       }
 
       public SetOfStacks(int stackCapacity) {
          this.stackCapacity = stackCapacity;
          buffer = new ArrayList<Stack<Integer>>();
          currentStack = new Stack<Integer>();
          buffer.add(currentStack);
          index = 0;
       }
 
       public void push(int value) {
          if (currentStack.size() == stackCapacity) {
             currentStack = new Stack<Integer>();
             buffer.add(currentStack);
             index++;
          }
          currentStack.push(value);
       }
 
       public int pop() {
         int res = currentStack.pop();
         if (currentStack.size() == 0 && index > 0) {
            buffer.remove(index);
            currentStack = buffer.get(--index);
         }

         return res;
       }
 
       public int getIndex() {
          return index;
       }
 
       public int popAt(int index) {
          if (index < 0 || index > this.index)
             throw new NoSuchElementException("Invalid stack index!");
 
          Stack<Integer> tmp = buffer.get(index);
          if (tmp.size() == 0)
             throw new NoSuchElementException("Stack #" + index + " is empty.");
          else
             return tmp.pop();
       }
 
       private boolean isCurrentEmpty() {
          return currentStack.size() == 0;
       }
 
    }
 
    public static void main(String[] args) {
       P0303 p0303 = new P0303();
       SetOfStacks stacks = p0303.new SetOfStacks(1);
       stacks.push(1);
       System.out.println(stacks.getIndex());
       stacks.push(2);
       System.out.println(stacks.getIndex());
       System.out.println(stacks.pop());
      System.out.println(stacks.pop());
       System.out.println(stacks.popAt(0));
       System.out.println(stacks.popAt(1));
    }
 }
