 package commands;
 
 import core.TrainBill;
 import core.TrainManager;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 public class MyTrainsCommand extends Command {
     
     private TrainManager trainManager;
 
 
     public MyTrainsCommand(MyPolly polly, TrainManager trainManager) 
             throws DuplicatedSignatureException {
         super(polly, "mytrains");
         this.createSignature("Listet die offene Capitrain Rechnung fr einen " +
         		"Benutzer auf.");
         this.setRegisteredOnly();
         this.setHelpText("Listet die offene Capitrain Rechnung fr einen " +
                 "Benutzer auf.");
         this.trainManager = trainManager;
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
         Signature signature) {
         
         if (this.match(signature, 0)) {
            TrainBill b = this.trainManager.getBill(executer.getCurrentNickName());
            this.reply(channel, b.toString());
         }
         return false;
     }
 }
