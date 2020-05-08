 //Copyright @fredghostkyle
 //2013 allrights myne! 
 import java.io.*;
 class GetName {
      public static void main(String args[]) throws IOException
   {
 //start
 	System.out.println("© fredghostkyle 2013. ALL RIGHTS RESERVED\nDo not copy or remake. Made in Java.");
 	System.out.println("http://Fredghostkyle.com/CR \n"); //for CopyRight
   	System.out.println("Enter your name: "); //Asks for name
   	InputStreamReader inp = new InputStreamReader(System.in);
  	BufferedReader br = new BufferedReader(inp); 
   	String name = br.readLine(); //set it to +name
   	System.out.println("Thank you for telling me!!!");
 	System.out.println("Your name from now on is: "+name); //tells name back to user
 	System.out.println("\n");
 	System.out.println("Enter your age:"); //asks for age
   	InputStreamReader inpg = new InputStreamReader(System.in);
  	BufferedReader brg = new BufferedReader(inp);
   	String age = br.readLine(); //set it to +age
 	System.out.println("Thank you for telling me!!!"); //will tell Name/Age
 	System.out.println("your age is: "+age);
 	System.out.println("and your name is: "+name);
 	System.out.println("\nWhat is your current address?"); //Asks for address
 	InputStreamReader inpi = new InputStreamReader(System.in);
  	BufferedReader bri = new BufferedReader(inp);
  	String address = br.readLine(); //sets to +address
 	System.out.println("Thank you for telling me! "); //tells Age, Name, Address
 	System.out.println("Your name is "+name);
 	System.out.println("Your age is "+age);
 	System.out.println("you address is "+address);
 	System.out.println("\n");
 	System.out.println("finish? y/n"); //finish?
 	InputStreamReader inph = new InputStreamReader(System.in);
  	BufferedReader brh = new BufferedReader(inp);
   	String end = br.readLine(); //sets to +end
    System.out.println("Goodbye!"); //Goodbye!
 //end
   }
 }
