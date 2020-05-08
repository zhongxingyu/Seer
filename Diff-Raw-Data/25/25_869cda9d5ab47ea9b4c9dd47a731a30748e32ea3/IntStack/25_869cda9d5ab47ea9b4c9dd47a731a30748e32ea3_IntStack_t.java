 public class IntStack{
     int[] stack;
     int top;
 
     public IntStack(int size)   //param optional: size of stack}
     {
 	stack = new int[size];
 	top=0;
     }
     boolean isEmpty()
     {
 	return top==0;
     }
     void push(int num)
     {
 	stack[top++]=num;
     }
     int pop()
     {
 	return stack[--top];
     }
     /**
       ima build pop all  right bout ur  sgon pop allll o dem thangs
       @author THEOLLIELLAMA
     */
    void  popAll()
    {
	popMultiple(top);	
    }
    /**
        ima build pop multiple right bout hur is gon pop mo than wun
        @author THEOLLIELLAMA
        @param number of items to pop
      */
     void  popMultiple(int pops)
     {
	for (int i = pops;i>0; i --)
 	    {
 		System.out.println( pop());
 	    }
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
 
  
     public static void main (String[] args){
	IntStack is = new IntStack(15);
         is.push(4);
 	is.push(5);
 	is.push(6);
 	is.push(7);
 	is.push(8);
 	is.push(9);
 	is.push(10);
	is.push(11);
	is.push(12);
	is.push(13);
	is.push(14);
	is.push(15);
	is.push(16);
 	System.out.println(is.peek());
 	
 	//tests for peekDepth:
 	System.out.println(is.peekDepth(100));
 
 	//test for stackSize
 	System.out.println("The Size is: " + is.stackSize());
 	
 	int val = is.pop();
 	
 	//Test for popMultiple
 	is.popMultiple(3);
	//Test for popAll
	is.popAll();
 	System.out.println(val);
     }
 }
