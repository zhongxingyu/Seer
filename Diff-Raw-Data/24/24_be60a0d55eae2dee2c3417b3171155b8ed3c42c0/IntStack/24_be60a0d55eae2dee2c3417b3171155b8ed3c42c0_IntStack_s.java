 import java.util.Scanner;
 //CHANGE
 
 class IntStack
 {
     //todo:
     //write resize
 
     //declarations:
     int[] stack;
     int top=0;
 
     IntStack(int size)
     {
 	stack = new int[size];
     }
 
     int pop()
     {
 	if(isEmpty()) return -1;
 	return stack[--top];
     }
 
 
     void push(int i)
     {
 	stack[top++]=i;
 	if(top==stack.length) resize();
     }
 
 
     int peek()
     {
 	if(isEmpty()) return -1;
 	return stack[top-1];
     }
 
     boolean isEmpty()
     {
 	return top==0;
     }
 
     private void resize()
     {
 	int[] newStack = new int[stack.length*2];
 	
 	for(int i= 0; i< top; i++) {
 	    newStack[i]=stack[i];
 	}
 	 
 	//System.out.println("resizing...");
 	stack=newStack;
     }
 
 
     /*
      * Size
      * gives the current size of the stack.
      * @author Charlie Forster
      */
     int size()
     {
 	return top-1;
     }
 
 
     /*
      * Mode
      * Gives the number that occurs most often.
      * If given paramater returns the number of times that number occurs in the stack.
      * This is achieved by overloading the method.
      * @author Zachary Buttenwieser
      * @author Adam Phillips
      * @param num
      */
 
     int mode()
     {
 	 int maxValue, maxCount;
 	 maxValue = -1;
 	 maxCount = -1;
     for (int i = 0; i < stack.length; ++i)
 	{
         int count = 0;
         for (int j = 0; j < stack.length; ++j) 
 	    {
 		if (stack[j] == stack[i]) ++count;
 	    }
         if (count > maxCount) 
 	    {
             maxCount = count;
             maxValue = stack[i];
 	    }
 	}
     return maxValue;
 	
     }
 
     int mode(int num)
     {
 	int counter = 0;
 	for (int i = 0; i<top; i++)
 	    {
 		if(stack[i]==num)
 		    {
 			counter++;
 		    }
 	    }
 	return counter;
 	
     }
 
     /*
      * Peek
      * Shows a number at a specific position.
      * Given depth paramter, finds and returns the number at that position
      * @author Grace Monk
      * @author Elana Simon
      * @param depth
      */
     int peek(int depth)
     {
 	if(depth > top) return -1;
 	//System.out.println("Here");
 	//System.out.println(top);
 	//for (int i = 0; i < stack.length; i++) {
 	// System.out.println(stack[i]);
 	//}
 	return stack[depth];
 
     }//close method
 
     
     void popUntil(int f)
     {
 	int position = 0;
 	while (peek () != f) 
      {
 	 pop();
      }
     }
 
     double mean()
     {
         double tot=0;
 	for(int i=0; i < top; i++)
 	    {
 		tot += stack[i];
 	    }
 	tot = tot/top;
 	return tot;
     }
 
     /* 
      * le ginger's work 
      *
      */
 
 
      /*
      * PopAll
      * Pops the entire stack
      * @author Allegra Simon and Alex Fisher
      */
    int[]	popAll()
 	{
 	    int[] fullStack = new int[this.size()];
 
 	    for(int i=0; i<this.size(); i++)
 		{
 			
 			fullStack[i] = stack[i];
 			this.pop();
 		}
 
 		return fullStack;
 	}
 
     
 
     /*
      * Casey, Sonia, Jenna, Rachel
      * Searches through the stack for certain number.
      * Return: true or false. 
      */
     boolean peekNum(int num)
     {
 	boolean peekNum = false;
	for(int i = 0; i<stack.length; i++)
 	    if(stack[i]==num) 
 		peekNum = true;
 	return peekNum;
     }
 
 
 
 
 
 }
