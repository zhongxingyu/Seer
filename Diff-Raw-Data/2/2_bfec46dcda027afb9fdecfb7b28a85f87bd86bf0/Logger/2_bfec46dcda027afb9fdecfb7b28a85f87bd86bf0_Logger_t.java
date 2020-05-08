 package org.jggug.kobo.gircbot.reactors;
 
 import org.jggug.kobo.gircbot.core.Reactor;
 
 public class Logger extends Reactor {
 
     private LogAppender appender;
 
     public Logger(LogAppender appender) {
         this.appender = appender;
     }
 
     @Override
     public void onMessage(String channel, String sender, String login, String hostname, String message) {
         appender.append("PRIVMSG", channel, sender, message);
     }
 
     @Override
     public void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
         if (target.startsWith("#")) {
            appender.append("NOTICE", target, sourceNick, notice);
         }
     }
 
     @Override
     public void onJoin(String channel, String sender, String login, String hostname) {
         appender.append("JOIN", channel, sender, String.format("+ %s joined to %s (%s@%s)", sender, channel, login, hostname));
     }
 
     @Override
     public void onPart(String channel, String sender, String login, String hostname) {
         appender.append("PART", channel, sender, String.format("- %s was parted from %s (%s@%s)", sender, channel, login, hostname));
     }
 
     @Override
     public void onNickChange(String oldNick, String login, String hostname, String newNick) {
         appender.append("NICK", null, oldNick, String.format("* %s -> %s (%s@%s)", oldNick, newNick, login, hostname));
     }
 
     @Override
     public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
         appender.append("KICK", channel, kickerNick, String.format("- %s was kicked by %s (%s@%s) because:", recipientNick, channel, kickerNick, kickerLogin, kickerHostname, reason));
     }
 
     @Override
     public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
         appender.append("QUICK", null, sourceNick, String.format("- %s was quited (%s@%s) because:", sourceNick, sourceLogin, sourceHostname, reason));
     }
 
     @Override
     public void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
         if (changed) {
             appender.append("TOPIC", channel, setBy, String.format("* %s by %s", topic, setBy));
         }
     }
 
     @Override
     public void onMode(String channel, String sourceNick, String sourceLogin, String sourceHostname, String mode) {
         appender.append("MODE", channel, sourceNick, String.format("* %s changed %s's mode %s (%s@%s)", sourceNick, channel, mode, sourceLogin, sourceHostname));
     }
 
     @Override
     public void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname, String channel) {
         appender.append("INVITE", channel, sourceNick, String.format("* %s is invited to %s by %s (%s@%s)", targetNick, channel, sourceNick, sourceLogin, sourceHostname));
     }
 
 }
