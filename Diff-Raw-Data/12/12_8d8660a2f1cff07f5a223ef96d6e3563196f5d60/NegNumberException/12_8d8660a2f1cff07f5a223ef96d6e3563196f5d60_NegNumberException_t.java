 public class NegNumberException extends Exception
 {
   public NegNumberException()
   {
     super("You have entered a negative number.");
   }
   
   public NegNumberException(String message)
   {
    super(message);
   }
 }
   
