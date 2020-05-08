 /*
  * This was requested by snaiperskaya.
  * I bring him this because he is so kawaii desu~ <3
  * 
  */
 
 package net.daiznaew.dbot3.Listeners.Commands;
 
 import java.io.*;
 import net.daiznaew.dbot3.Listeners.core.BotCommand;
 import net.daiznaew.dbot3.main.DaizBot;
 import net.daiznaew.dbot3.util.enums.AccessLevel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  *
  * @author Daiz
  */
 
 public class CaptainsLog extends BotCommand 
 {
     BufferedWriter out;
     BufferedReader in;
     String read;
 
     public CaptainsLog()
     {
         
         
         getAliases().add("!log");
         setMinAccessLevel(AccessLevel.OP);        
         setArgumentsString("<Message>");
         setDescription("A command to log things, usefull for snoosnoo.");
     }
     
     @Override
     @SuppressWarnings("empty-statement")
     public void onMessage(MessageEvent<PircBotX> event) throws Exception
     {
         if (performGenericChecks(event.getChannel(), event.getUser(), event.getMessage().split(" ")))
         {
             
             if (getArgs().length >= 2)
             {
                 //this is just a copy of the tweeting class, but modified to work with write to file.
                 String quote = "";
                User user = DaizBot.getBot().getUser(event.getUser().getNick());
                 
                 for (int i = 1; i < getArgs().length; i++)
                 {
                    quote += getArgs()[i] + " : Logged by : " + user;
                 }
                 quote = quote.substring(0, quote.length());
                 
                 //finds the text to write to
                 out = new BufferedWriter(new FileWriter("captainslogs.txt",true));
                 
                 //writes the message
                out.write(quote);
                 //goes to the next line on the text
                 out.newLine();
                 //prepares for a new write
                 out.close();
                 
             } else {showUsage();}
         }
     }
 }
