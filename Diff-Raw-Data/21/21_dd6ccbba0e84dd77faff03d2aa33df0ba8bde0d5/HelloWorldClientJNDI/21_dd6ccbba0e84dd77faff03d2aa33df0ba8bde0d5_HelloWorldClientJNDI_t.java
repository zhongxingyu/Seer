 package oracle.ejb30;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 
 public class HelloWorldClientJNDI {
     public static void main(String[] args) {
         try {
             System.out.println("Looking up HelloWorld");
             InitialContext ic = new InitialContext();
             HelloWorld helloWorld = (HelloWorld) ic.lookup("java:comp/env/ejb/HelloWorld");
            System.out.println("Invoking sayHello method with parameter \"" + " Sausage & Chips " + "\"");
             // Call any of the Remote methods below to access the EJB
            helloWorld.sayHello("Sausage and Chips!");
             System.out.println("Please view the console to see the output emitted by the EJB Method");
         } catch (Throwable ex) {
             ex.printStackTrace();
         }
     }
 }
