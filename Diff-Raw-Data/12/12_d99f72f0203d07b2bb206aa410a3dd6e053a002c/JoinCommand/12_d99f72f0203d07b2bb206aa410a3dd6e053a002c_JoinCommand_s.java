 package commands;
 
 import java.util.Collections;
 import java.util.List;
 
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.Types.ChannelType;
 import de.skuzzle.polly.sdk.Types.ListType;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.model.User;
 
 public class JoinCommand extends Command {
 
     
     
     public JoinCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "join");
         this.createSignature("Lsst polly den angegebenen Channel betreten.", 
                 new Parameter("Channel", Types.CHANNEL));
         this.createSignature("Lsst polly alle Channels in der Liste betreten.", 
                 new Parameter("Channelliste", new ListType(Types.CHANNEL)));
         this.createSignature("Lsst polly einen Channel mit Passwort betreten", 
             new Parameter("Channel", Types.CHANNEL),
             new Parameter("Passwort", Types.STRING));
         this.setRegisteredOnly();
         this.setUserLevel(UserManager.ADMIN);
         this.setHelpText("Befehl zum joinen von Channels.");
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) {
 
         List<ChannelType> channels = null;
         if (this.match(signature, 0)) {
             ChannelType ct = new ChannelType(signature.getStringValue(0));
             channels = Collections.singletonList(ct);
         } else if (this.match(signature, 1)) {
             channels = signature.getListValue(ChannelType.class, 0);
         } else if (this.match(signature, 2)) {
             String c = signature.getStringValue(0);
             String pw = signature.getStringValue(1);
             
             this.getMyPolly().irc().joinChannel(c, pw);
             return false;
         }
         
         for (ChannelType ct : channels) {
             this.getMyPolly().irc().joinChannel(ct.getValue(), "");
         }
         
         return false;
     }
 
 }
