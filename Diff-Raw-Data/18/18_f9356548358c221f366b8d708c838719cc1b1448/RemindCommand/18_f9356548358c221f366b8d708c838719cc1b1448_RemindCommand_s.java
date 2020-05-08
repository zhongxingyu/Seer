 package commands;
 
 import java.util.Date;
 import java.util.List;
 
 import core.RemindManager;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types.ChannelType;
 import de.skuzzle.polly.sdk.Types.DateType;
 import de.skuzzle.polly.sdk.Types.ListType;
 import de.skuzzle.polly.sdk.Types.StringType;
 import de.skuzzle.polly.sdk.Types.UserType;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.RemindEntity;
 
 
 
 public class RemindCommand extends AbstractRemindCommand {
 
     public RemindCommand(MyPolly myPolly, RemindManager remindManager) 
             throws DuplicatedSignatureException {
         super(myPolly, remindManager, "remind");
         this.createSignature("Erinnert den Benutzer zu einer angegebenen Zeit im " +
                 "angegebenen Channel an etwas.", 
                 new UserType(), new ChannelType(), new DateType(), new StringType());
         this.createSignature("Erinnert eine Liste von Benutzern zu einer angegebenen " +
                 "Zeit an etwas.", 
                 new ListType(new UserType()), new ChannelType(), new DateType(), 
                 new StringType());
         this.createSignature("Erinnert dich zu einer bestimmten Zeit an etwas.", 
                 new DateType(), new StringType());
         this.setRegisteredOnly();
         this.setHelpText("Hinterlsst Erinnerungen fr Benutzer.");
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) {
                
         if (this.match(signature, 0)) {
             String location = signature.getStringValue(1);
             Date dueDate = signature.getDateValue(2);
             String msg = signature.getStringValue(3);
             String fromUser = executer.getCurrentNickName();
             String forUser = signature.getStringValue(0);
 
             RemindEntity remind = new RemindEntity(msg, fromUser, forUser, location, 
                     dueDate);
             this.addRemind(remind, true);
             this.reply(channel, FORMATTER.format(remind, this.getMyPolly().formatting()));
             
         } else if (this.match(signature, 1)) {
             String location = signature.getStringValue(1);
             Date dueDate = signature.getDateValue(2);
             String msg = signature.getStringValue(3);
             String fromUser = executer.getCurrentNickName();
             
             List<UserType> users = signature.getListValue(UserType.class, 0);
             for (UserType ut : users) {
                 RemindEntity remind = new RemindEntity(msg, fromUser, ut.getValue(), 
                         location, dueDate);
                 this.addRemind(remind, true);
             }
             ListType tmp = (ListType) signature.getValue(0);
             this.reply(channel, "Erinnerungen fr die Benutzer " + 
                     tmp.valueString(this.getMyPolly().formatting()) + " hinzugefgt.");
             
         } else if (this.match(signature, 2)) {
             Date dueDate = signature.getDateValue(0);
             String msg = signature.getStringValue(1);
             
             RemindEntity remind = new RemindEntity(msg, executer.getCurrentNickName(), 
                     executer.getCurrentNickName(), channel, dueDate);
             this.addRemind(remind, true);
             this.reply(channel, FORMATTER.format(remind, this.getMyPolly().formatting()));
         }
         return false;
     }
 }
