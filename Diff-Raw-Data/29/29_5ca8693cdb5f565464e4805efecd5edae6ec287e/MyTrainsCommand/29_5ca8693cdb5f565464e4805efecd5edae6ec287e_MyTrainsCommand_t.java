 package commands;
 
 import core.TrainBill;
 import core.TrainManager;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
import de.skuzzle.polly.sdk.Types.BooleanType;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
import entities.TrainEntity;
 
 
 public class MyTrainsCommand extends Command {
     
     private TrainManager trainManager;
 
 
     public MyTrainsCommand(MyPolly polly, TrainManager trainManager) 
             throws DuplicatedSignatureException {
         super(polly, "mytrains");
         this.createSignature("Listet die offene Capitrain Rechnung fr einen " +
         		"Benutzer auf.");
        this.createSignature("Listet eine detaillierte Capitrainrechnung fr einen " +
        		"Benutzer auf", new BooleanType());
         this.setRegisteredOnly();
         this.setHelpText("Listet die offene Capitrain Rechnung fr einen " +
                 "Benutzer auf.");
         this.trainManager = trainManager;
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
         Signature signature) {
         
         if (this.match(signature, 0)) {
            this.printTrains(false, executer.getCurrentNickName(), channel);
        } else if (this.match(signature, 1)) {
            this.printTrains(signature.getBooleanValue(0), executer.getCurrentNickName(), 
                channel);
         }

         return false;
     }
    
    
    
    private void printTrains(boolean detailed, String forUser, String channel) {
        TrainBill b = this.trainManager.getBill(forUser);
        if (detailed) {
            for (TrainEntity train : b.getTrains()) {
                this.reply(channel, train.format(this.getMyPolly().formatting()));
            }
            this.reply(channel, "=========================");
        }
        this.reply(channel, b.toString());
    }
 }
