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
 
     public static void main (String[] args){
 	IntStack is = new IntStack(10);
         is.push(4);
 	is.push(5);
 	is.push(6);
 	is.push(7);
 	int val = is.pop();
 	System.out.println(val);
     }
 }
