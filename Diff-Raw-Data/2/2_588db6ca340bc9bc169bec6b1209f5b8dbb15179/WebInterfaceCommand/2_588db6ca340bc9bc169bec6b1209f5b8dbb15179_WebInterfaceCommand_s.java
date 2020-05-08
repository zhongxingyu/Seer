 package commands;
 
 import java.io.IOException;
 
 import polly.core.MSG;
 import de.skuzzle.polly.sdk.Command;
 import de.skuzzle.polly.sdk.Configuration;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Parameter;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.User;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.roles.RoleManager;
 
 public class WebInterfaceCommand extends Command {
 
     public WebInterfaceCommand(MyPolly polly) throws DuplicatedSignatureException {
         super(polly, "web"); //$NON-NLS-1$
         this.createSignature(MSG.webSig0Desc);
         this.createSignature(MSG.webSig1Desc, 
             RoleManager.ADMIN_PERMISSION, 
             new Parameter(MSG.webSig1OnOff, Types.BOOLEAN));
         this.setHelpText(MSG.webHelp);
     }
     
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel,
             Signature signature) throws CommandException, InsufficientRightsException {
         boolean ssl = false;
         try {
             ssl = this.getMyPolly().configuration().open("http.cfg").readBoolean( //$NON-NLS-1$
                     Configuration.HTTP_USE_SSL);
         } catch (Exception e1) {
             e1.printStackTrace();
         }
         
         final int port = this.getMyPolly().webInterface().getPort();
         boolean appendPort = ssl && port != 443 || !ssl && port != 80;
         String url = "http" + (ssl ? "s" : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
         url += "://" + this.getMyPolly().webInterface().getPublicHost(); //$NON-NLS-1$
        url += appendPort ? ":" + port : ""; //$NON-NLS-1$
         
         
         if (this.match(signature, 0)) {
             if (this.getMyPolly().webInterface().getServer().isRunning()) {
                 this.reply(channel, MSG.bind(MSG.webShowUrl, url));
             } else {
                 this.reply(channel, MSG.webOffline);
             }
         } else if (this.match(signature, 1)) {
             boolean newState = signature.getBooleanValue(0);
             if (this.getMyPolly().webInterface().getServer().isRunning() && !newState) {
                 this.getMyPolly().webInterface().getServer().shutdown(0);
                 this.reply(channel, MSG.webTurnedOff);
             } else if (!this.getMyPolly().webInterface().getServer().isRunning() && newState) {
                 try {
                     this.getMyPolly().webInterface().getServer().start();
                     this.reply(channel, MSG.bind(MSG.webTurnedOn, url));
                 } catch (IOException e) {
                     throw new CommandException(e);
                 }
             }
         }
         
         return false;
     }
 
 }
