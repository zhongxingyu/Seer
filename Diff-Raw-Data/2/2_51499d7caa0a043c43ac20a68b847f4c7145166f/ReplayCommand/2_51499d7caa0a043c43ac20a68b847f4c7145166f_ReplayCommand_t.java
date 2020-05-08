 package commands;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import core.PollyLoggingManager;
 import core.filters.DateLogFilter;
 import core.filters.LogFilter;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Signature;
 import de.skuzzle.polly.sdk.exceptions.CommandException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.model.User;
 import entities.LogEntry;
 
 
 public class ReplayCommand extends AbstractLogCommand {
 
     public ReplayCommand(MyPolly polly, PollyLoggingManager logManager) 
                 throws DuplicatedSignatureException {
         super(polly, "replay", logManager);
         this.createSignature(
             "Zeigt alle IRC Nachrichten an, die seit deiner letzten Aktion gepostet wurden");
         this.setHelpText(
             "Zeigt alle IRC Nachrichten an, die seit deiner letzten Aktion gepostet wurden");
         this.setRegisteredOnly();
     }
 
     
     
     @Override
     protected boolean executeOnBoth(User executer, String channel, Signature signature) 
             throws CommandException, InsufficientRightsException {
         
         try {
             if (this.match(signature, 0)) {
                 List<LogEntry> logs = new ArrayList<LogEntry>();
                 
                 for (String chan : this.getMyPolly().irc().getChannels()) {
                     if (this.getMyPolly().irc().isOnChannel(chan, 
                             executer.getCurrentNickName())) {
                         
                        logs.addAll(this.logManager.preFilterChannel(chan));
                     }
                 }
                 LogFilter dateFilter = new DateLogFilter(
                         new Date(executer.getLastIdleTime()));
                 
                 logs = this.logManager.postFilter(logs, dateFilter);
                 this.logManager.outputLogResults(this.getMyPolly(), executer, logs, 
                     executer.getCurrentNickName());
             }
         } catch (DatabaseException e) {
             throw new CommandException(e);
         }
         
         return false;
     }
 }
