 public class IntStack{
     int[] stack;
     int top=0;
 
     public IntStack(int n){ //param optional: size of stack
 	stack = new int[n];
     }
 
     boolean isEmpty(){
        return top == 0;
     }
     void push(int n){
 	stack[top++]=n; //you can write that here
 	//	top++;
     }
     int pop(){
 	return stack[--top];//you can also write this
 	//	top--;
     }
     int peek(){
 	return stack[top-1];
  
     }   
    
    //Lila Lowest Number In Stack
    int low(){
	if(top!=0){
	    return stack[0];
	}
	return 0;
    }
    
 
     public static void main(String[]args){
 	IntStack is = new IntStack(10);
	is.push(2);
 	is.push(5);
 	is.push(6);

	//test for lowest:
	int low = is.low();
	System.out.println(low);
     }
 }
  
 
 
