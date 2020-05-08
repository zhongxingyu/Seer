 package oracle.ejb30;
 
 import javax.ejb.EJB;
 
 public class HelloWorldClient {
      @EJB
         private static HelloWorld helloWorld;
 
     	public  static void main(String[] args) {
      
          	System.out.println("Looking up HelloWorld");
             System.out.println("Using Dependency Injection .. invoking sayHello ");
             helloWorld.sayHello("Goran Soldar!");
             System.out.println("Please view the console to see the output emitted by the EJB Method");
         }
 }
