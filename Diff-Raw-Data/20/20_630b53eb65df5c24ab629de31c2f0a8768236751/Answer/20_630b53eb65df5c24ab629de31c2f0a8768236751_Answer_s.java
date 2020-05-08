 /*
  * © Copyright 2008–2009 by Edgar Kalkowski (eMail@edgar-kalkowski.de)
  * 
  * This file is part of the chatbot ABCPeter.
  * 
  * The chatbot ABCPeter is free software; you can redistribute it and/or modify it under the terms
  * of the GNU General Public License as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with this program. If
  * not, see <http://www.gnu.org/licenses/>.
  */
 
 package erki.xpeter.parsers;
 
 import java.util.LinkedList;
 
 import erki.api.util.Observer;
 import erki.xpeter.Bot;
 import erki.xpeter.con.Connection;
 import erki.xpeter.msg.TextMessage;
 import erki.xpeter.util.BotApi;
 
 /**
  * This {@link Parser} can answer questions. The questions can be of two forms:
  * <ol>
 * <li>( ) a ( ) b: In this case maximal one option is selected by the bot
 * <li>[ ] a [ ] b: In this case one or more options are selected by the bot
  * </ol>
 * This means that the two questions “( ) a” and “[ ] a” are handled equally.
  * 
  * @author Edgar Kalkowski
  */
 public class Answer implements Parser, Observer<TextMessage> {
     
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
         Connection con = msg.getConnection();
         String nick = con.getNick();
         
         // don’t reply to self
         if (msg.getNick().equals(nick)) {
             return;
         }
         
         // omit the bot’s nick
         if (BotApi.addresses(text, nick)) {
             text = BotApi.trimNick(text, nick);
         }
         
         // check box
         if (text.contains("[ ]")) {
             String response = "";
             
             while (text.contains("[ ]")) {
                 response += text.substring(0, text.indexOf("[ ]"));
                 text = text.substring(text.indexOf("[ ]") + 3);
                 int rnd = (int) (Math.random() * 2);
                 
                 if (rnd == 0) {
                     response += "[x]";
                 } else {
                     response += "[ ]";
                 }
             }
             
            con.send(response + text);
         }
         
         // option box
         if (text.contains("( )")) {
             String response = "";
             LinkedList<String> options = new LinkedList<String>();
             
             while (text.contains("( )")) {
                 options.add(text.substring(0, text.indexOf("( )")));
                 text = text.substring(text.indexOf("( )") + 3);
             }
             
             options.add(text);
             int choice = (int) (Math.random() * (options.size() - 1));
             
             for (int i = 0; i < options.size() - 1; i++) {
                 response += options.get(i);
                 
                 if (i == choice) {
                     response += "(x)";
                 } else {
                     response += "( )";
                 }
             }
             
            con.send(response + options.get(options.size() - 1));
         }
     }
 }
