 package com.drtshock.willie.command;
 
 import com.drtshock.willie.Willie;
 import com.drtshock.willie.auth.Auth;
 import com.drtshock.willie.github.GistHelper;
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.User;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class CommandManager extends ListenerAdapter<Willie> implements Listener<Willie> {
 
     private static final Logger logger = Logger.getLogger(CommandManager.class.getName());
 
     private Willie bot;
     private HashMap<String, Command> commands;
     private String cmdPrefix;
 
     public CommandManager(Willie bot) {
         this.bot = bot;
         this.cmdPrefix = bot.getConfig().getCommandPrefix();
         this.commands = new HashMap<>();
     }
 
     public void registerCommand(Command command) {
         this.commands.put(command.getName(), command);
     }
 
     public Collection<Command> getCommands() {
         return this.commands.values();
     }
 
     public void setCommandPrefix(String prefix) {
         this.cmdPrefix = prefix;
     }
 
     @Override
     public void onMessage(MessageEvent<Willie> event) {
         handlerMessage(event.getMessage(), event.getChannel(), event.getUser());
     }
 
     /**
      * Additional Step for handling command
      * This allow Willie to use commands
      * NOTE: Commands used by Willie should support it !
      * A command is used by Willie when sender == null
      *
      * @param message The message
      * @param channel The channel
      * @param sender  The sender or Null if it's Willie
      */
     public void handlerMessage(String message, Channel channel, User sender) {
         if (message.toLowerCase().endsWith("o/") && (!message.contains("\\o/"))) {
             channel.sendMessage("\\o");
             return;
         }
         if (message.toLowerCase().endsWith("\\o") && (!message.contains("\\o/"))) {
             channel.sendMessage("o/");
             return;
         }
 
         if (!message.startsWith(cmdPrefix)) {
             return;
         }
 
         String[] parts = message.substring(1).split(" ");
 
         String commandName = parts[0].toLowerCase();
         String[] args = new String[parts.length - 1];
         System.arraycopy(parts, 1, args, 0, args.length);
 
         Command command = this.commands.get(commandName);
         if (command.isAdminOnly() && (sender == null || !Auth.checkAuth(sender).isAdmin)) {
            channel.sendMessage(Colors.RED + String.format("%s, you aren't an admin. Maybe you forgot to identify yourself?", (sender == null ? "Willie" : sender.getNick())));
             return;
         }
         try {
             command.getHandler().handle(this.bot, channel, sender, args);
         } catch (Exception e) {
             final Writer writer = new StringWriter();
             final PrintWriter printWriter = new PrintWriter(writer);
             e.printStackTrace(printWriter);
             final String stackTrace = writer.toString();
 
             logger.log(Level.SEVERE, e.getMessage(), e);
 
             final String msg1 = "Exception caught when " + (sender == null ? "Willie" : sender.getNick()) + " used the command \"" + message + "\".";
             channel.sendMessage(Colors.RED + msg1);
             logger.severe(msg1);
 
             final String msg2 = "I pasted the exception there: " + GistHelper.gist(stackTrace);
             channel.sendMessage(Colors.RED + msg2);
             logger.severe(msg2);
 
             String willieCommand = "!fix " + (sender == null ? "Willie" : sender.getNick());
             channel.sendMessage(willieCommand);
             handlerMessage(willieCommand, channel, null);
         }
     }
 
 }
