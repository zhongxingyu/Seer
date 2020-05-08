 public class LLTest
 {
     public static void main(String[] args)
     {
 	LinkedListI mylist = new LinkedListI();

	System.out.println(mylist.isEmpty(mylist.first));

 	mylist.add(1);
 	mylist.add(2);
 	mylist.add(3);
 
 	mylist.set(2, 4, mylist.first);
 	
 	mylist.print(mylist.first);
 
 	System.out.println(mylist.factorial(5));
 
 	System.out.println(mylist.size(mylist.first));

	System.out.println(mylist.isEmpty(mylist.first));

 	
 	mylist.print(mylist.clone(mylist.first));
 	
     }//main
 
 }//class
