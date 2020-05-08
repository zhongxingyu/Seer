import java.util.*;

 public class IntStack{
     int[] stack;
     int top;
 
     public IntStack(int size)   //param optional: size of stack}
     {
 	stack = new int[size];
 	top=0;
     }
     boolean isEmpty() {
 	return top==0;
     }
     void push(int num){
 	stack[top++]=num;
     }
     int pop(){
 	return stack[--top];
     }
     int peek()  //sometimes
     {
 	return stack[top-1];
     }
     
     /**
       Function to peek at a specific depth
       @author Henry Screen
       @param depth the distance from the top
     */
     int peekDepth(int depth)      
     {
 	if (depth<=top) return stack[top-depth];
 	else
 	{
 	    return -1;
 	}
     }
 
     /**
        Function to return the size of the stack
        @author Henry Screen
     */
     int stackSize()
     {
 	return top;
     }
 
    /**
       Function to resize the array when it gets to big for the amount of things pushed into it (by default  a factor of 10)
       @auther Henry Screen
       @param Takes the IntStack in order to change the size of the stack that is within it 
     */
    IntStack resizeStack(IntStack is)
    {
	int [] newStackSize = new int[stack.length*10];
	 for (int i = 0; i < stack.length; i++)
	            newStackSize[i] = stack[i];
	stack = newStackSize;
	return is;
	
    }
  
     public static void main (String[] args){
 	IntStack is = new IntStack(10);
         is.push(4);
 	is.push(5);
 	is.push(6);
 	is.push(7);
 	is.push(8);
 	is.push(9);
 	is.push(10);
 	System.out.println(is.peek());
 	
 	//tests for peekDepth:
 	System.out.println(is.peekDepth(100));
 
 	//test for stackSize
 	System.out.println("The Size is: " + is.stackSize());
 	
	//test for resizeStack
	System.out.println("The stack max length is: " +  is.stack.length);
	is.resizeStack(is);
	System.out.println("The stack max length is: " +  is.stack.length);

 	int val = is.pop();
 	System.out.println(val);
     }
 }
