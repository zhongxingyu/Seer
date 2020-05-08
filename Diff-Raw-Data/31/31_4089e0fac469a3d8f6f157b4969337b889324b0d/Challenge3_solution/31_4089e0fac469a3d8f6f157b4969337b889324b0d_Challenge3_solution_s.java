 package lab3;
 
 /**
  * This challenge is similar to the first one, except that the architecture has
  * change to SOA. Now it's more important than ever to throw exceptions back to
  * a class that is responsible for displaying error messages. <p> No input or
  * output should be performed here.
  *
  * @author Jim Lombardo, jlombardo@wctc.edu
  * @version 1.00
  */
 public class Challenge3_solution {
 
    public static void main(String[] args) throws InvalidNameException {
 
         new InputOutputGui_solution().startConversation();
     }
 }
