 package com.lair.mtduck.irc;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.math.BigDecimal;
 import java.util.Properties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.lair.mtduck.mtgox.MtGoxManager;
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.PircBot;
 
 /**
  *
  */
 public class TickerIRCMessenger extends PircBot {
 
     private static final Logger logger = LoggerFactory.getLogger(MtGoxManager.class);
 
     private String host;
     private String password;
     private String channel;
 
     private int priceUp = 0;
     private int priceDown = 0;
     private BigDecimal currentPrice = null;
 
     public TickerIRCMessenger(Properties properties) {
         this.setName("McDuck");
         this.host = properties.getProperty("irc.server.host");
         this.password = properties.getProperty("irc.server.password");
         this.channel = properties.getProperty("irc.channel");
         try {
             this.setEncoding("utf8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
     }
 
     public void connect() throws IrcException, IOException {
         connect(host, 6667, password);
         joinChannel(channel);
         sendMessage(channel, "Coin coin!"); // I'm that annoying :)
     }
 
     public void updateTicker(BigDecimal currentPrice) {
         this.currentPrice = currentPrice;
         if (priceUp == 0 && priceDown == 0) {
             initRefPrice(currentPrice);
             sendMessage("Current price is " + currentPrice.toPlainString() + ", setting priceUp to " + priceUp + " priceDown to " + priceDown);
         } else if (currentPrice.compareTo(new BigDecimal(priceUp)) > 0) {
             sendMessage("Price just broke " + priceUp + " upwards at " + currentPrice.toPlainString() + " !");
             priceUp += 5;
             priceDown += 5;
         } else if (currentPrice.compareTo(new BigDecimal(priceDown)) < 0) {
             sendMessage("Price just broke " + priceDown + " downwards at " + currentPrice.toPlainString() + " !");
             priceUp -= 5;
             priceDown -= 5;
         }
     }
 
 
     private void initRefPrice(BigDecimal currentPrice) {
         int result = currentPrice.intValue();
         priceUp = result + 10 - (result % 5);
         priceDown = result - (result % 5);
     }
 
     public void sendMessage(String message) {
         sendMessage(channel, message);
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         switch(message) {
             case "!price":
                 sendMessage(channel, "Price is currently " + currentPrice);
                 break;
             case "!refprices":
                 sendMessage(channel, "priceUp is " + priceUp + " priceDown is " + priceDown);
                 break;
             case "!gtfo":
                 sendMessage(channel, "Okay :(");
                 System.exit(0);
                 break;
             case "!help":
                 sendMessage(channel, "Available commands : !price !refprices !gtfo");
                 break;
             case "McDuck":
                sendMessage(channel, "Coin coin ! (Type !help for a list of available commands");
                 break;
         }
     }
 }
