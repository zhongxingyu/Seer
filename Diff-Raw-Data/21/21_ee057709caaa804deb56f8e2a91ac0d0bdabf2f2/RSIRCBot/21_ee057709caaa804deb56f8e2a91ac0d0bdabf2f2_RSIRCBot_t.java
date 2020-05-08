 package org.nolat.rsircbot;
 
 import java.io.IOException;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.nolat.rsircbot.commands.Command;
 import org.nolat.rsircbot.settings.Settings;
 import org.nolat.rsircbot.settings.json.Channel;
 import org.nolat.rsircbot.tools.Greetings;
 
 public class RSIRCBot extends PircBot {
 
     public static final String VERSION = "1.1.1a";
 
     private Settings settings;
 
     public RSIRCBot(Settings settings) {
         this.settings = settings;
         setName(settings.getName());
         setAutoNickChange(true);
         setMessageDelay(50);

        setup();
    }

    private void setup() {
         try {
             connect(settings.getServer(), settings.getPort());
         } catch (NickAlreadyInUseException e) {
             System.out.println("Nick was in use.");
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (IrcException e) {
             e.printStackTrace();
         }
         for (Channel c : settings.getChannels()) {
             joinChannel(c.getName());
         }
     }
 
     @Override
    protected void onDisconnect() {
        super.onDisconnect();
        System.out.println("Why did this happen?");
        int tries = 0;
        while (tries < 5 || isConnected()) {
            System.out.println("Attempting to reconnect... try " + (tries + 1));
            setup();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }

    @Override
     protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname,
             String recipientNick, String reason) {
         if (recipientNick.equalsIgnoreCase(getNick())) {
             //we were kicked so remove this channel from the settings
             getSettings().removeChannel(channel);
 
         }
     }
 
     @Override
     protected void onInvite(String targetNick, String sourceNick, String sourceLogin, String sourceHostname,
             String channel) {
         Channel c = new Channel(channel, true, true, "Use !qotd to set the qotd or !toggle qotd to turn it off.");
         settings.addChannel(c);
         this.joinChannel(channel); //join channel we were invited to
     }
 
     @Override
     protected void onJoin(String channel, String sender, String login, String hostname) {
         super.onJoin(channel, sender, login, hostname);
         if (!sender.equalsIgnoreCase(getNick())) {
             onUserJoin(channel, sender);
         } else {
             onBotJoin(channel);
         }
     }
 
     private void onUserJoin(String channel, String user) {
         Channel c = settings.getChannel(channel);
         if (c.shouldDisplayGreeting()) {
             sendMessage(channel, Greetings.getRandomGreeting() + " " + user
                     + ", type !help to view help for this bot. Send commands directly to the bot with /msg "
                     + getNick() + " !help");
         }
 
         if (c.shouldDisplayQotd()) {
             sendMessage(channel, "QOTD: " + c.getQotdMessage());
         }
     }
 
     private void onBotJoin(String channel) {
         Channel c = settings.getChannel(channel);
         sendMessage(channel, Greetings.getRandomGreeting()
                 + " everybody! type !help or /msg "
                 + getNick() + " !help to view help. (Version: " + VERSION
                 + "; Patch Notes: https://github.com/Talon876/RSIRCBot#Change_Log)");
 
         if (c.shouldDisplayQotd()) {
             sendMessage(channel, "QOTD: " + c.getQotdMessage());
         }
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         super.onMessage(channel, sender, login, hostname, message);
         process(channel, sender, message);
     }
 
     @Override
     protected void onPrivateMessage(String sender, String login, String hostname, String message) {
         super.onPrivateMessage(sender, login, hostname, message);
         process(sender, sender, message);
     }
 
     public void sendMessage(String channel, String executor, String message, Command command) {
         if (command.isPrivateReply()) {
             sendNotice(executor, message);
         } else {
             sendMessage(channel, message);
         }
     }
 
     private void process(String target, String sender, String message) {
         Command command = Command.getCommand(message);
         if (command != null) {
             command.executeCommand(this, target, sender, message);
         } else {
             System.out.println(message + " wasn't a command");
         }
     }
 
     public Settings getSettings() {
         return settings;
     }
 }
