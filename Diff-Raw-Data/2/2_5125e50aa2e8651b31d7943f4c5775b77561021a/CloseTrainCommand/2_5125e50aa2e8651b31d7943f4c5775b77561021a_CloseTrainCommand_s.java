 package polly.rx.commands;
 
 import polly.rx.MyPlugin;
 import polly.rx.core.TrainManagerV2;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 public class CloseTrainCommand extends Command {
 
     private TrainManagerV2 trainManager;
     
     public CloseTrainCommand(MyPolly polly, TrainManagerV2 trainManager) 
             throws DuplicatedSignatureException {
         super(polly, "closetrain");
         this.createSignature("Schliesst alle offenen Rechnungen fr den angegebenen " +
             "Benutzer.", 
             MyPlugin.CLOSE_TRAIN_PERMISSION,
             new Parameter("User", Types.USER));
         this.createSignature("Schliesst einzelnen Trainposten.", 
             MyPlugin.CLOSE_TRAIN_PERMISSION,
             new Parameter("Train Id", Types.NUMBER));
         this.setRegisteredOnly();
        this.setHelpText("Schliesst offene Capitrain rechnungen.");
         this.trainManager = trainManager;
     }
 
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
         Signature signature) throws CommandException {
         
         if (this.match(signature, 0)) {
             String userName = signature.getStringValue(0);
             try {
                 this.trainManager.closeOpenTrains(executer, userName);
                 this.reply(channel, "Alle offenen Rechnungen fr '" + userName + 
                     "' geschlossen.");
             } catch (DatabaseException e) {
                 this.reply(channel, "Interner Datenbankfehler!");
             }
         } else if (this.match(signature, 1)) {
             int id = (int) signature.getNumberValue(0);
             try {
                 this.trainManager.closeOpenTrain(executer, id);
                 this.reply(channel, "Posten mit der Id '" + id + " ' geschlossen.");
             } catch (DatabaseException e) {
                 this.reply(channel, "Interner Datenbankfehler!");
             }
         }
         return false;
     }
 }
