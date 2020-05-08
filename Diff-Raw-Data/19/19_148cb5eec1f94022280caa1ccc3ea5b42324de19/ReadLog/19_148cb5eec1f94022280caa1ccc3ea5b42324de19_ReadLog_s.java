 package net.daiznaew.dbot3.Listeners.Commands;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import net.daiznaew.dbot3.Listeners.core.BotCommand;
 import net.daiznaew.dbot3.util.NumberMethods;
 import net.daiznaew.dbot3.util.enums.AccessLevel;
 import net.daiznaew.dbot3.util.enums.ColorFormat;
 import net.daiznaew.dbot3.util.messages.Messages;
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  *
  * @author Daiz
  * @author Snaiperskaya
  */
 
 public class ReadLog extends BotCommand {
     
     BufferedReader in;
     String read;
     String readnext;
     
     public ReadLog()
     {
         getAliases().add("!read");
         setMinAccessLevel(AccessLevel.NORMAL);        
         setArgumentsString("<Number>");
         setDescription("A command to read the logs");
     }
     
     @Override
     @SuppressWarnings("empty-statement")
     public void onMessage(MessageEvent<PircBotX> event) throws Exception
     {
         if (performGenericChecks(event.getChannel(), event.getUser(), event.getMessage().split(" ")))
         {
             String readLastLog = "last";
             
             if(getArgs().length >= 2)
             {
 
                 /**
                 * Checks if "last" function is used. 
                 * This will allow the bot to run to the end of the log and report the last entry it finds. 
                 * Count is kept, so this should display the valid number of that entry.
                 */
                 
                 if(getArgs()[1].toLowerCase().equalsIgnoreCase(readLastLog))
                 {
                     in = new BufferedReader(new FileReader("captainslogs.txt"));
                     
                     int linecount = 1; //Will be used to track entry numbers.
                     
                     readnext = in.readLine();
                     read = "No entries found! Please make the Captain sit down and write something before we all die!!";
                     
                     while(readnext != null)
                     {
                         read = readnext;
                         readnext = in.readLine(); //Always 1 line ahead, so it can be null
                         linecount++;
                     }
                     
                     
                     Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), "Captain's Log entry #" +linecount+ ": " +read+ "Log ended.");
                     in.close();
                 }
                 
                 /**
                 * If not last, it will see what number the second argument is (if a number at all) and only accept those above 0.
                 * If a number is entered that is greater than the total number of entries, this should still stop at the last entry.
                 * Even if this does hit the last entry first, this should still report the accurate number of that entry.
                 */
                 
                 else if (Integer.valueOf(getArgs()[1]) > 0)
                 {
                     int linenum = Integer.valueOf(getArgs()[1]) - 1;
                     
                     in = new BufferedReader(new FileReader("captainslogs.txt"));
                     
                     int linecount = 1;
                     
                     readnext = in.readLine();
                    read = "No entries found! Please make the Captain sit down and write something before we all die!!";
                
                    while(linenum > 0 && readnext != null)
                     {
                         read = readnext;
                        readnext = in.readLine();
                        linenum--;
                        linecount++;
                     }
                 
                     Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), "Captain's Log entry #" +linecount+ ": " +read+ "!Log ended!");
                     in.close();
                 }
             } else showUsage(); 
         }
     }
 }
