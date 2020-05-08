 package commands;
 
 import java.util.Date;
 
 import core.RemindManager;
 
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.RemindEntity;
 
 public class SleepCommand extends Command {
 
     private RemindManager remindManager;
     
     public SleepCommand(MyPolly polly, RemindManager remindManager) 
             throws DuplicatedSignatureException {
    
         super(polly, "sleep");
         this.remindManager = remindManager;
         this.createSignature("Verlngert die Erinnerung die dir zuletzt zugestellt wurde", 
             new Parameter("Neue Zeit", Types.DATE));
         this.setRegisteredOnly();
         this.setHelpText("Verlngert Erinnerungen");
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) throws CommandException {
         if (this.match(signature, 0)) {
             Date dueDate = signature.getDateValue(0);
             RemindEntity sleeping = this.remindManager.getSleep(
                     executer.getCurrentNickName());
             
             if (sleeping == null) {
                 this.reply(channel, "Kein Remind aktiv.");
                 return false;
             }
             
             RemindEntity copy = sleeping.copyForNewDueDate(dueDate);
             try {
                 this.remindManager.addRemind(copy);
                 this.remindManager.scheduleRemind(copy, dueDate);
                 this.remindManager.removeSleep(sleeping.getForUser());
                this.reply(channel, "Erinnerung wurde verlngert.");
             } catch (DatabaseException e) {
                 throw new CommandException(e);
             }
         }
         return false;
     }
 
 }
