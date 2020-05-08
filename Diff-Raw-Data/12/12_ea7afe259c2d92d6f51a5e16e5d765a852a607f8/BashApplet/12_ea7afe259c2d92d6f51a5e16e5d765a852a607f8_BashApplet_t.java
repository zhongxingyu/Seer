 package org.transtruct.cmthunes.ircbot.applets;
 
 import java.io.*;
 import java.net.*;
 
 import org.jsoup.Jsoup;
 import org.jsoup.helper.Validate;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import org.transtruct.cmthunes.util.*;
 import org.transtruct.cmthunes.irc.*;
 
 
 public class BashApplet implements BotApplet {
 
     public String[] getQuote() {
         Document doc = null;
         try {
             doc = Jsoup.connect("http://bash.org/?random").get();
         } catch (IOException e) {
         }
         Element p = doc.select("p.qt").first();
         String html = p.html();
         html = html.replaceAll("&lt;", "<");
         html = html.replaceAll("&gt;", ">");
         String[] text = html.split("<br />");
         for (int i=0; i < text.length; i++) {
            text[i] = text[i].trim();
         }
         return text;
     }
 
     public void run(IRCChannel channel, IRCUser from, String command, String[] args, String unparsed) {
         String[] quote = this.getQuote();
         channel.writeMultiple(quote);
     }
 
 }
 
