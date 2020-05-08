 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.daiznaew.dbot3.Listeners.Commands;
 
 import net.daiznaew.dbot3.Listeners.core.BotCommand;
 import net.daiznaew.dbot3.util.NumberMethods;
 import net.daiznaew.dbot3.util.enums.AccessLevel;
 import net.daiznaew.dbot3.util.enums.ColorFormat;
 import net.daiznaew.dbot3.util.messages.ErrorMessages;
 import net.daiznaew.dbot3.util.messages.Messages;
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 
 /**
  *
  * @author Daiz
  */
 public class DegreesCalc extends BotCommand 
 {
     
     public DegreesCalc()
     {
         
         getAliases().add("!degrees");
         getAliases().add("!temp");
         
         setMinAccessLevel(AccessLevel.NORMAL);
 
         setDescription("Calculates degrees");
 
 
         setArgumentsString("<Degrees in number><C/F>");
         
     }
     
     @Override
     @SuppressWarnings("empty-statement")
     public void onMessage(MessageEvent<PircBotX> event) throws Exception
     {
         if (performGenericChecks(event.getChannel(), event.getUser(), event.getMessage().split(" ")))
         {
             if (getArgs().length == 3)
             {
                 String operation = getArgs()[2];
 
                 if (NumberMethods.isDouble(getArgs()[1]))
                 {
                     double num1 = Double.valueOf(getArgs()[1]);
                     int A = 32;
                     double B = 1.8;
                     double celsius;
                     double farenheit;
                     
                     switch (operation)
                     {
                         case "F":
                             celsius = (num1 - A) / B;
                             Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), (celsius)+"C");
                             break;
                             
                         case "C":
                             farenheit = num1 * B + A;
                             Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), (farenheit)+"F");
                             break;
                             
                         default:
                             ErrorMessages.invalidOperation(getChannel(), getUser());
                             break;
                     }
                 } else ErrorMessages.invalidNumber(getChannel(), event.getUser());
             } 
             else showUsage();
                     }
     }
 }
