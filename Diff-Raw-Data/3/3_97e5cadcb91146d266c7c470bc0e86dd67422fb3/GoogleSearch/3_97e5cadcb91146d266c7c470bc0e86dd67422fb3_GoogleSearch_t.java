 package net.daiznaew.dbot3.Listeners.Commands;
 
 /**
  *
  * @author Daiz
  */
 
 import com.google.gson.Gson;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.net.URLEncoder;
 import net.daiznaew.dbot3.Listeners.core.BotCommand;
 import net.daiznaew.dbot3.util.enums.AccessLevel;
 import net.daiznaew.dbot3.util.enums.ColorFormat;
 import net.daiznaew.dbot3.util.messages.Messages;
 import org.pircbotx.PircBotX;
 import org.pircbotx.hooks.events.MessageEvent;
 
 public class GoogleSearch extends BotCommand {
     
     public GoogleSearch() {
         
         
         getAliases().add("!g");
         getAliases().add("!search");
         getAliases().add("!google");
         
         setMinAccessLevel(AccessLevel.VOICE);
         
         setDescription("A command used to search through google.");
         
         setArgumentsString("<Search query>");
     }
     
     public void onMessage(MessageEvent<PircBotX> event) throws Exception
     {
         if (performGenericChecks(event.getChannel(), event.getUser(), event.getMessage().split(" ")))
         {
             if (getArgs().length >= 2 ) 
             {
                 String google = "http://ajax.googleapis.com/ajax/services/search/web?v=1.0&q=";
                 String charset = "UTF-8";
                 String searchquery = "";
                 for (int i = 1; i < getArgs().length; i++)
                 {
                     searchquery += getArgs()[i] + " ";
                 }
                 searchquery = searchquery.substring(0, searchquery.length());
                 
                 URL url = new URL(google + URLEncoder.encode(searchquery, charset));
                 Reader reader = new InputStreamReader(url.openStream(), charset);
                 GoogleIntegration results = new Gson().fromJson(reader, GoogleIntegration.class);
                 
                 String titleresult = results.getResponseData().getResults().get(0).getTitle();
                 String urlresult = results.getResponseData().getResults().get(0).getUrl();
                 
                Messages.respond(getChannel(), ColorFormat.NORMAL, getUser(), "Search results for: "+searchquery + "are: " + "Title: "+titleresult+" Url: "+urlresult);
                 
             } else { showUsage(); }
         }
     }
 }
