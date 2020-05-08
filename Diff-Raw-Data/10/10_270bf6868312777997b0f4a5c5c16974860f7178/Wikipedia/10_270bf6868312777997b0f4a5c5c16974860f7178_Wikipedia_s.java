 /*
  * © Copyright 2008–2010 by Edgar Kalkowski <eMail@edgar-kalkowski.de>
  * 
  * This file is part of the chatbot xpeter.
  * 
  * The chatbot xpeter is free software; you can redistribute it and/or modify it under the terms of
  * the GNU General Public License as published by the Free Software Foundation; either version 3 of
  * the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter.parsers;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
 import erki.api.util.Log;
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.msg.Message;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.util.BotApi;
 
 /**
  * This parser allows to retrieve information from wikipedia.
  * 
  * @author Edgar Kalkowski
  */
 public class Wikipedia implements Parser, Observer<TextMessage> {
     
     @Override
     public void init(Bot bot) {
         bot.register(TextMessage.class, this);
     }
     
     @Override
     public void destroy(Bot bot) {
         bot.deregister(TextMessage.class, this);
     }
     
     @Override
     public void inform(TextMessage msg) {
         String text = msg.getText();
         
         if (BotApi.addresses(text, msg.getBotNick())) {
             text = BotApi.trimNick(text, msg.getBotNick());
         } else {
             return;
         }
         
         String match = "[wW]iki(pedia)?[,:;]? (.*?)\\.?";
         
         if (text.matches(match)) {
             String query = text.replaceAll(match, "$2");
             Log.debug("Recognized query “" + query + "”.");
             
             Log.info("Making query to wikipedia.");
             
             try {
                String result = BotApi.getWebsite("de.wikipedia.org", "/wiki/" + query, "UTF-8");
                 msg.respond(new Message(parse(result)));
             } catch (UnknownHostException e) {
                 Log.error(e);
                 msg.respond(new Message("Ich konnte leider keine Verbindung zur Wikipedia "
                         + "herstellen. :("));
             } catch (IOException e) {
                 Log.error(e);
                 msg.respond(new Message("Ich konnte leider keine Verbindung zur Wikipedia "
                         + "herstellen. :("));
             }
         }
     }
     
     private static String parse(String text) {
         
         if (!text.toUpperCase().contains("HTTP/1.0 200 OK")
                 || !text.toLowerCase().contains("<!-- start content -->")) {
             return "Ich konnte leider keine passende Seite bei Wikipedia finden.";
         }
         
         text = text.substring(text.indexOf("<!-- start content -->"));
         text = text.substring(text.indexOf("<p>"));
         text = text.substring(0, text.indexOf("</p>"));
         text = text.replaceAll("<p>", "");
         text = text.replaceAll("</p>", "");
         text = text.replaceAll("<i>", "");
         text = text.replaceAll("</i>", "");
         text = text.replaceAll("<b>", "");
         text = text.replaceAll("</b>", "");
         text = text.replaceAll("<a href=.*?>", "");
         text = text.replaceAll("</a>", "");
         text = text.replaceAll("<span .*?>", "");
         text = text.replaceAll("</span>", "");
         text = text.replaceAll("&#160;", " ");
         return text;
     }
 }
