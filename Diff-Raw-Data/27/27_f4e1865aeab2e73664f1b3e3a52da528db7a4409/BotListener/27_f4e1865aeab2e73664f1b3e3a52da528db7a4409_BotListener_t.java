 package net.h31ix.anticheatbot;
 
 import org.pircbotx.Channel;
 import org.pircbotx.Colors;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.hooks.Listener;
 import org.pircbotx.hooks.ListenerAdapter;
 import org.pircbotx.hooks.events.MessageEvent;
 
 public class BotListener extends ListenerAdapter implements Listener
 {
     private PircBotX bot;
     private Channel channel;
 
     public BotListener(PircBotX bot, Channel channel)
     {
         this.bot = bot;
         this.channel = channel;
     }
 
     @Override
     public void onMessage(MessageEvent event) throws Exception
     {
         String[] message = event.getMessage().split(" ");
         String command = message[0];
         User sender = event.getUser();
         if(command.startsWith("."))
         {
             // Public commands
 
             if (command.equalsIgnoreCase(".time"))
             {
                 System.out.println("Responded to "+event.getUser().getNick()+" with current time.");
                 String time = new java.util.Date().toString();
                 event.respond("The time is "+Colors.BOLD+time);
             }
 
             else if(command.equalsIgnoreCase(".version"))
             {
                 System.out.println("Responded to "+event.getUser().getNick()+" with current version.");
                 event.respond("Latest AntiCheat version is "+Colors.BOLD+AntiCheatBot.version);
             }
 
             // Admin commands
 
             else if(command.equalsIgnoreCase(".kick"))
             {
                 if(sender.getChannelsOpIn().contains(this.channel))
                 {
                     if(message.length == 2)
                     {
                         User user = bot.getUser(message[1]);
                         if(user != null)
                         {
                             System.out.println("Kicked "+user.getNick()+" from the channel.");
                             bot.kick(channel, bot.getUser(message[1]));
                         }
                         else
                         {
                             event.respond("User not found.");
                         }
                     }
                     else
                     {
                         event.respond("Usage: .kick [user]");
                     }
                 }
                 else
                 {
                     System.out.println("WARNING: "+sender.getNick()+" tried to kick without permission.");
                     event.respond("Insufficient permissions.");
                 }
             }
 
             else if(command.equalsIgnoreCase(".ban"))
             {
             	if(sender.getChannelsOpIn().contains(this.channel))
                 {
                     if(message.length == 2)
                     {
                         User user = bot.getUser(message[1]);
                         if(user != null)
                         {
                             System.out.println("Banned "+user.getNick()+" from the channel.");
                             bot.ban(channel, bot.getUser(message[1]).getHostmask());
                             bot.kick(channel, bot.getUser(message[1]), "The banhammer has spoken!");
                         }
                         else
                         {
                             event.respond("User not found.");
                         }
                     }
                     else
                     {
                         event.respond("Usage: .ban [user]");
                     }
                 }
                 else
                 {
                     System.out.println("WARNING: "+sender.getNick()+" tried to ban without permission.");
                     event.respond("Insufficient permissions.");
                 }
             }
 
             else if(command.startsWith(".q"))
             {
             	if(sender.getChannelsOpIn().contains(this.channel))
                 {
                     if (command.equalsIgnoreCase(".qreload"))
                     {
                         AntiCheatBot.updateQueries();
                         event.respond("Reloaded Queries Successfully!");
                         return;
                     }
                     if(command.equalsIgnoreCase(".qadd"))
                     {
                         String [] s1 = event.getMessage().split("-");
                         String [] s2 = s1[0].split(" ");
                         String input = "";
                         for(int i = 1; i < s2.length; i++)
                         {
                             input += s2[i] + (i == s2.length - 1 ? "" : " ");
                         }
                         String output = "";
                         s2 = s1[1].split(" ");
                         for(int i = 1; i < s2.length; i++)
                         {
                             output += s2[i] + (i == s2.length - 1 ? "" : " ");
                         }
                         String response2 = AntiCheatBot.getResponse(2, input);
                         if(response2 == null)
                         {
                             AntiCheatBot.addQuery(2, input, output);
                             event.respond("Added Query successfully!");
                         }
                         else
                         {
                             event.respond("A query already exists for the string '"+input+"'");
                         }
                     }
                     else if (command.equalsIgnoreCase(".qrem"))
                     {
                         if(message.length >= 2)
                         {
                             String [] s2 = event.getMessage().split(" ");
                             String input = "";
                             for(int i = 1; i < s2.length; i++)
                             {
                                 input += s2[i] + (i == s2.length - 1 ? "" : " ");
                             }
                             AntiCheatBot.removeQuery(input);
                             event.respond("Removed Query successfully!");
                         }
                         else
                         {
                             event.respond("Usage: .qrem [query]");
                         }
                     }
                     else
                     {
                         event.respond("Usage: .q<add/rem/reload> [messagematch] - [message/<<action>>]");
                     }
                 }
                 else
                 {
                    System.out.println("WARNING: "+sender.getNick()+" tried to use query system without permission.");
                    event.respond("Insufficient permissions.");
                 }
             }
 
             else if(command.equalsIgnoreCase(".die"))
             {
             	if(sender.getChannelsOpIn().contains(this.channel))
                 {
                     if(message.length == 1)
                     {
                         bot.disconnect();
                         bot.shutdown();
                     }
                 }
                 else
                 {
                     System.out.println("WARNING: "+sender.getNick()+" tried to kill bot without permission.");
                     event.respond("Insufficient permissions.");
                 }
             }
 
             else if(command.equalsIgnoreCase(".bug"))
             {
                 if(message.length == 2)
                 {
                     String id = message[1];
                     event.respond(AntiCheatBot.getBugDetails(id));
                 }
                 else if(message.length == 3 && message[2].equalsIgnoreCase("close"))
                 {
                     if(sender.getChannelsOpIn().contains(this.channel))
                     {
                        int id = Integer.parseInt(message[1]);
                        if(id <= AntiCheatBot.bug_id)
                        {
                            AntiCheatBot.closeBug(id, sender.getNick());
                            event.respond("Closed bug "+id);
                        }
                        else
                        {
                            event.respond("That bug report doesn't exist yet.");
                        }
                     }
                     else
                     {
                         System.out.println("WARNING: "+sender.getNick()+" tried to close a bug without permission.");
                         event.respond("Insufficient permissions.");
                     }
                 }
                 else
                 {
                     event.respond("Usage: .bug [id] [close]");
                 }
             }
         }
         else
         {
             // Queries
             //if(!sender.getChannelsOpIn().contains(this.channel))
             {
                 for(String string : AntiCheatBot.messages.keySet())
                 {
                     if(event.getMessage().toLowerCase().contains(string.toLowerCase()) || event.getMessage().toLowerCase().equals(string.toLowerCase()))
                     {
                         String response = AntiCheatBot.getResponse(2, string);
                         if(response.startsWith("<<"))
                         {
                             String [] cmd = response.split(">>");
                             String reason = "Don't violate the rules";
                             if(cmd.length == 2)
                             {
                                 reason = cmd[1].replaceAll("<a>", "'");
                             }
                             if(response.startsWith("<<KICK>>"))
                             {
                                 System.out.println("Kicked "+sender.getNick()+" from the channel for "+reason+".");
                                 bot.kick(channel, sender, reason);
                             }
                             else if(response.startsWith("<<BAN>>"))
                             {
                                 System.out.println("Banned "+sender.getNick()+" from the channel for "+reason+".");
                                 bot.ban(channel, sender.getHostmask());
                                 bot.kick(channel, sender, reason);
                             }
                             if(response.startsWith("<<WARN>>"))
                             {
                                 int warnings = AntiCheatBot.addWarning(sender.getNick());
                                 System.out.println("Warned "+sender.getNick()+" for "+reason+". ("+warnings+"/3)");
                                 event.respond(Colors.RED+"WARNING - "+reason+". ("+warnings+"/3)");
                                 if(warnings >= 3)
                                 {
                                     System.out.println("Banned "+sender.getNick()+" from the channel for "+reason+".");
                                     bot.ban(channel, sender.getHostmask());
                                     bot.kick(channel, sender, reason);
                                 }
                             }
                         }
                         else
                         {
                             bot.sendMessage(channel, response);
                         }
                     }
                 }
             }
         }
     }
 }
