 class IntStackTest
 {
     public static void main(String[] args)
     {
 	IntStack is = new IntStack(10);
 
 	System.out.println(is.isEmpty());
 
 	is.push(3);
 	is.push(4);
 	is.push(5);
 
 	is.push(5);
 	is.push(3);
 	is.push(5);
 	//is.mode();
 		//is.mode(3);
 		//is.peek(3);
 
 	//System.out.println(is.peek());
 	System.out.println(is.peek(5));
 	/**System.out.println(is.pop());
 	System.out.println(is.pop());
 	System.out.println(is.pop());
 	System.out.println(is.pop());
 	System.out.println(is.pop());
 	System.out.println(is.pop());
 	System.out.println(is.pop());
 	System.out.println(is.isEmpty());
 	System.out.println(is.pop());**/
 
 
 	System.out.println(is.mean());
 
 
 	//Tests for popAall
 	System.out.println(is.popAll());
 	System.out.println(is.isEmpty());
<<<<<<< HEAD
 
	//test peek:
	System.out.println(is.peekNum(4));
 
 
 
=======
 	System.out.println(is.pop());
     is.push (3);
     is.push (4);
     is.push (5);
     is.push (6);
     is.push (7);
     is.push (8);
     is.popUntil (4);
>>>>>>> added popUntil (MD, RC, AK)
     }
 }
 	
