 package org.transtruct.cmthunes.ircbot;
 
 import java.net.*;
 import java.sql.*;
 
 import org.transtruct.cmthunes.irc.*;
 import org.transtruct.cmthunes.irc.messages.*;
 import org.transtruct.cmthunes.irc.messages.filter.*;
 import org.transtruct.cmthunes.ircbot.applets.*;
 import org.transtruct.cmthunes.irclog.*;
 
 public class Bot {
     public static void main(String[] args) throws Exception {
         InetSocketAddress addr = new InetSocketAddress("irc.brewtab.com", 6667);
 
         /* Create IRC client */
         IRCClient client = new IRCClient(addr);
         
         /* Create logger */
         Class.forName("org.h2.Driver");
         Connection connection = DriverManager.getConnection("jdbc:h2:brewtab", "sa", "");
         IRCLogger logger = new IRCLogger(connection);
 
         /* Register applets with the bot */
         BotChannelListener botChannelListener = new BotChannelListener();
         BashApplet bashApplet = new BashApplet();
         GroupHugApplet groupHugApplet = new GroupHugApplet();
         TextsFromLastNightApplet textsFromLastNightApplet = new TextsFromLastNightApplet();
         CalcApplet calcApplet = new CalcApplet();
         WeatherApplet weatherApplet = new WeatherApplet();
         StatsApplet statsApplet = new StatsApplet(logger);
        
         botChannelListener.registerApplet("bash", bashApplet);
 
         botChannelListener.registerApplet("gh", groupHugApplet);
         botChannelListener.registerApplet("grouphug", groupHugApplet);
 
         botChannelListener.registerApplet("tfln", textsFromLastNightApplet);
         botChannelListener.registerApplet("texts", textsFromLastNightApplet);
 
         botChannelListener.registerApplet("m", calcApplet);
         botChannelListener.registerApplet("math", calcApplet);
         botChannelListener.registerApplet("calc", calcApplet);
 
         botChannelListener.registerApplet("w", weatherApplet);
         botChannelListener.registerApplet("weather", weatherApplet);
 
         botChannelListener.registerApplet("last", statsApplet);
 
         /* Will block until connection process is complete */
         client.connect("testbot", "bot", "kitimat", "Mr. Bot");
 
         /*
          * We can add a message handler for the client to print all messages
          * from the server if we needed to for debugging
          */
         client.addHandler(IRCMessageFilters.PASS, new IRCMessageHandler() {
             @Override
             public void handleMessage(IRCMessage message) {
                 System.out.println("root>>> " + message.toString().trim());
             }
         });
 
         /*
          * Join a channel. Channels can also be directly instantiated and
          * separately joined
          */
         IRCChannel c = client.join("#bot");
 
         /* We add a handler for channel messages */
         c.addListener(botChannelListener);
         c.addListener(logger);
 
         /* Wait for client object's connection to exit and close */
         client.getConnection().awaitClosed();
         System.out.println("Exiting");
     }
 }
