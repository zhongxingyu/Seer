 /**
  * This is essentially the Client. It is the main application and
  * handles the creation of the Command object and setting its receiver,
  * and then passing that command object to the invoker. The commands
  * can then be executed via the invoker.
  */
 public class Main {
 
   public static void main(String[] args) {
 
     // Create a new receiver.
     Receiver receiver = new Receiver();
 
     // Create a new command and bind it to our receiver.
     Command command = new ConcreteCommand(receiver);
 
     // Create an invoker to execute commands.
     Invoker invoker = new Invoker();
 
     // Bind the command to our invoker and execute it.
     invoker.setCommand(command);
     invoker.executeCommand();
  }
 
 }
