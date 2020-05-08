 package commands;
 
 
 import polly.core.MyPlugin;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.Conversation;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 3851c1b
  */
 public class QuitCommand extends Command {
 
     private final static String[] answers = {"ja", "yo", "jup", "yes", "jo", "ack"};
     
     public QuitCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "flyaway");
         this.createSignature("Beendet polly.", MyPlugin.QUIT_PERMISSION);
         this.createSignature("Beendet polly mit der angegebenen Quit-Message",
                 MyPlugin.QUIT_PERMISSION,
                 new Parameter("Quit-Message", Types.STRING));
         this.setRegisteredOnly();
         this.setUserLevel(UserManager.ADMIN);
         this.setHelpText("Befehl zum Beenden von Polly.");
     }
 
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) throws CommandException {
 
         String message = "*krchz* *krchz* *krchz*";
         if (this.match(signature, 1)) {
             message = signature.getStringValue(0);
         }
         
 
         Conversation c = null;
         try {
             c = this.createConversation(executer, channel);
            c.writeLine("Yo' seroius?");
             String a = c.readLine().getMessage();
             
             for (String ans : answers) {
                 if (a.equals(ans)) {
                     this.getMyPolly().irc().quit(message);
                     this.getMyPolly().shutdownManager().shutdown();
                     return false;
                 }
             }
         } catch (InterruptedException e) {
             throw new CommandException("Answer timeout");
         } catch (Exception e) {
             throw new CommandException(e);
         } finally {
             if (c != null) {
                 c.close();
             }
         }
         
 
         return false;
     }
 }
