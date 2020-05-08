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
        ima build pop multiple right bout hur is gon pop mo than wun
        @author THEOLLIELLAMA
        @param number of items to pop
      */
     void  popMultiple(int pops)
     {
	for (int i = pops;i>0; i --) s
 	    {
 		System.out.println( pop());
 	    }
     }
 
     int peek()  //sometimes
     {
 	return stack[top-1];
     }
 
     public static void main (String[] args){
 	IntStack is = new IntStack(10);
         is.push(4);
 	is.push(5);
 	is.push(6);
 	is.push(7);
 	int val = is.pop();
 	
 	//Test for popMultiple
 	is.popMultiple(3);
 
 	System.out.println(val);
     }
 }
