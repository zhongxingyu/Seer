 package org.freecode.irc.votebot.api;
 
 import org.freecode.irc.Privmsg;
 import org.freecode.irc.Transmittable;
 import org.freecode.irc.votebot.FreeVoteBot;
 
 import java.sql.Connection;
 
 public abstract class CommandModule extends FVBModule {
     public CommandModule(FreeVoteBot fvb) {
         super(fvb);
     }
 
     @Override
     public boolean canRun(Transmittable trns) {
         if (!trns.isPrivmsg())
             return false;
         String msg = trns.asPrivmsg().getMessage();
        String command = "!" + getName().toLowerCase();
         return msg.toLowerCase().startsWith(command)
                 && msg.substring(command.length()).matches(getParameterRegex());
     }
 
     @Override
     public void process(Transmittable trns) {
         processMessage((Privmsg) trns);
     }
 
     public abstract void processMessage(Privmsg privmsg);
 
     protected String getParameterRegex() {
         return ".*";
     }
 }
