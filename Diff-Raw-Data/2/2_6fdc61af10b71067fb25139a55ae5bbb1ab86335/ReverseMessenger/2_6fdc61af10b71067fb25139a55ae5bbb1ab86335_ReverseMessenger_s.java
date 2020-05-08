 /*
  This file is part of NerdzApi-java.
 
     NerdzApi-java is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     NerdzApi-java is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with NerdzApi-java.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) 2013 Marco Cilloni <marco.cilloni@yahoo.com>
 */
 
 package eu.nerdz.api.impl.reverse.messages;
 
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import eu.nerdz.api.BadStatusException;
 import eu.nerdz.api.ContentException;
 import eu.nerdz.api.HttpException;
 import eu.nerdz.api.LoginException;
 import eu.nerdz.api.UserInfo;
 import eu.nerdz.api.WrongUserInfoTypeException;
 import eu.nerdz.api.impl.reverse.AbstractReverseApplication;
 import eu.nerdz.api.messages.Conversation;
 import eu.nerdz.api.messages.ConversationHandler;
 import eu.nerdz.api.messages.Message;
 import eu.nerdz.api.messages.Messenger;
 
 /**
  * Reverse implementation of eu.nerdz.api.messages.Messenger.
  */
 public class ReverseMessenger extends AbstractReverseApplication implements Messenger {
 
     /**
      *
      */
     private static final long serialVersionUID = -1627978816568946110L;
     //The only ConversationHandler of the instance.
     private ConversationHandler mConversationHandler;
 
     /**
      * Creates a ReverseMessenger, initializing the underlining AbstractReverseApplication.
      *
      * @param user     the username
      * @param password the password
      * @throws IOException
      * @throws HttpException
      * @throws LoginException
      */
     public ReverseMessenger(String user, String password) throws IOException, HttpException {
         super(user, password);
         this.mConversationHandler = this.createHandler();
     }
 
     /**
      * Creates a ReverseMessenger from existing login data, initializing the underlining AbstractReverseApplication.
      *
      * @param loginData existing login data
      * @throws WrongUserInfoTypeException if loginData is not an instance of AbstractReverseApplication.ReverseUserInfo
      */
     public ReverseMessenger(UserInfo loginData) throws WrongUserInfoTypeException {
         super(loginData);
         this.mConversationHandler = this.createHandler();
     }
 
     //Initializing the handler
     private ConversationHandler createHandler() {
 
         //The mConversationHandler instance is created. This is an inner class because it needs access to post/get methods, that are protected.
         return new ConversationHandler() {
 
 
             /**
              *
              */
             private static final long serialVersionUID = -798135247830642378L;
 
             @Override
             public List<Conversation> getConversations() throws IOException, HttpException, ContentException {
                 List<Conversation> conversations = null;
 
                 List<String> rows = this.parseTableRows(ReverseMessenger.this.get("/pages/pm/inbox.html.php"));
                 if (rows != null) {
 
                     conversations = new ArrayList<Conversation>(20);
 
                     for (String row : rows)
                         conversations.add(this.parseConversationRow(row));
 
                 }
 
                 return conversations;
             }
 
             @Override
             public List<Message> getMessages(Conversation conversation) throws IOException, HttpException, ContentException {
                 return this.getMessages(conversation, 0, 10);
             }
 
             @Override
             public List<Message> getMessages(Conversation conversation, int start, int howMany) throws IOException, HttpException, ContentException {
                 List<Message> messages;
                 HashMap<String, String> form = new HashMap<String, String>(4);
                 form.put("from", String.valueOf(conversation.getOtherID()));
                 form.put("to", String.valueOf(ReverseMessenger.this.getUserID()));
                 form.put("start", String.valueOf(start));
                 form.put("num", String.valueOf(howMany));
 
                 String body = ReverseMessenger.this.post("/pages/pm/read.html.php?action=conversation", form);
 
                 int endOfList = body.indexOf("<form id=\"convfrm\"");
 
                 switch (endOfList) {
                     case 0:
                         return null;
                     case -1:
                         throw new ContentException("malformed response: " + body);
                     default: {
                         int headOfList = body.indexOf("<div style=\"margin-top: 3px\" id=\"pm");
                         if (headOfList < 0) {
                             throw new ContentException("malformed response: " + body);
                         }
 
                         messages = new LinkedList<Message>();
                         for (String messageString : this.splitMessages(body.substring(headOfList, endOfList)))
                             messages.add(new ReverseMessage(this.parseSender(messageString), this.parseMessage(messageString), this.parseSenderID(messageString), this.parseDate(messageString)));
                     }
                 }
 
                 return messages;
             }
 
             @Override
             public Message getLastMessage(Conversation conversation) throws IOException, HttpException, ContentException {
                 List<Message> messages =  this.getMessages(conversation, 0, 1);
 
                 if (messages.size() != 1)
                     throw new ContentException("Something is very broken in NERDZ. Report to nessuno ASAP that read.html.php?action=conversation is broken");
 
                return messages.get(1);
             }
 
             @Override
             public void deleteConversation(Conversation conversation) throws IOException, HttpException, BadStatusException, ContentException {
 
                 HashMap<String, String> form = new HashMap<String, String>(2);
 
                 form.put("from", String.valueOf(conversation.getOtherID()));
                 form.put("to", String.valueOf(ReverseMessenger.this.getUserID()));
 
                 try {
                     JSONObject response = new JSONObject(ReverseMessenger.this.post("/pages/pm/delete.json.php", form, AbstractReverseApplication.NERDZ_DOMAIN_NAME + "/pm.php"));
 
                     if (!(response.getString("status").equals("ok") && response.getString("message").equals("OK"))) {
                         throw new BadStatusException("wrong status couple (" + response.getString("status") + ", " + response.getString("message") + "), conversation not deleted");
                     }
                 } catch (JSONException e) {
                     throw new ContentException("Error while parsing JSON in new messages: " + e.getLocalizedMessage());
                 }
 
             }
 
             /**
              * This method parses the timestamp from a raw message, and returns it as a Date.
              * @param messageString the raw message string parsed by splitMessage
              * @return A Date representing the moment this message has been sent.
              * @throws ContentException
              */
             private Date parseDate(String messageString) throws ContentException {
 
                 int timestampPosition = messageString.indexOf("data-timestamp=\"");
                 if (timestampPosition < 0) {
                     throw new ContentException("malformed response: " + messageString);
                 }
 
                 timestampPosition += 16;
 
                 return new Date(Long.parseLong(messageString.substring(timestampPosition, messageString.indexOf('"', timestampPosition))) * 1000L);
 
             }
 
             /**
              * This method parses the sender's ID from a raw message, and returns it as an int.
              * @param messageString the raw message string parsed by splitMessage
              * @return An int containing the sender's ID.
              * @throws ContentException
              */
             private int parseSenderID(String messageString) throws ContentException {
 
                 int fromIDPosition = messageString.indexOf("data-fromid=\"");
                 if (fromIDPosition < 0) {
                     throw new ContentException("malformed response: " + messageString);
                 }
 
                 fromIDPosition += 13;
 
                 return Integer.parseInt(messageString.substring(fromIDPosition, messageString.indexOf('"', fromIDPosition)));
 
             }
 
             /**
              * This method parses the sender's username from a raw message.
              * @param messageString the raw message string parsed by splitMessage
              * @return A String containing the sender's username
              * @throws ContentException
              */
             private String parseSender(String messageString) throws ContentException {
 
                 int closeLinkPosition = messageString.lastIndexOf("</a>", messageString.lastIndexOf("<time"));
                 if (closeLinkPosition < 0) {
                     throw new ContentException("malformed response: " + messageString);
                 }
 
                 int nickStart = messageString.lastIndexOf('>', closeLinkPosition) + 1;
                 if (nickStart < 0) {
                     throw new ContentException("malformed response: " + messageString);
                 }
 
                 return StringEscapeUtils.unescapeHtml4(messageString.substring(nickStart, closeLinkPosition));
 
             }
 
             /**
              * This method parses the message from a raw message string.
              * @param messageString the raw message string parsed by splitMessage
              * @return A message
              * @throws ContentException
              */
             private String parseMessage(String messageString) throws ContentException {
 
                 int msgStart = messageString.lastIndexOf("1pt solid #FFF\">") + 16;
                 if (msgStart <= 0) {
                     throw new ContentException("malformed message string: " + messageString);
                 }
 
                 return this.removeTags(StringEscapeUtils.unescapeHtml4(messageString.substring(msgStart)));
 
             }
 
             /**
              * Parses and removes all HTML tags in a message.
              * @param msg A message parsed by parseMessage
              * @return The parsed message
              */
             private String removeTags(String msg) {
 
                 return this.linkParse(this.ytParse(this.removeDivs(msg.replaceAll("<br />", "\n").replaceAll("<hr style=\"clear:both\" />", "\n"))));
 
             }
 
             /**
              * Finds links and replaces them with its URL.
              * @param msg A message parsed by parseMessage
              * @return The parsed message
              */
             private String linkParse(String msg) {
 
                 int linkPos, hrefPos, hrefEnd, linkEnd;
                 while ((linkPos = msg.indexOf("<a ")) >= 0) {
                     hrefPos = msg.indexOf("href=\"", linkPos) + 6;
                     hrefEnd = msg.indexOf('"', hrefPos);
                     linkEnd = msg.indexOf("</a>", hrefEnd) + 4;
                     msg = msg.substring(0, linkPos) + msg.substring(hrefPos, hrefEnd) + msg.substring(linkEnd);
                 }
                 return msg;
 
             }
 
             /**
              * Removes divs.
              * @param msg A message parsed by parseMessage
              * @return The parsed message
              */
             private String removeDivs(String msg) {
 
                 int divPos, divEnd;
                 while ((divPos = msg.indexOf("<div")) >= 0) {
                     divEnd = msg.indexOf('>', divPos) + 1;
                     msg = msg.substring(0, divPos) + msg.substring(divEnd);
                 }
                 return msg;
 
             }
 
             /**
              * Finds YouTube videos and replaces them with the video's url.
              * @param msg A message parsed by parseMessage
              * @return The parsed message
              */
             private String ytParse(String msg) {
 
                 int iframePos, srcPos, srcEnd, iframeEnd;
                 while ((iframePos = msg.indexOf("<iframe")) >= 0) {
                     srcPos = msg.indexOf("src=\"", iframePos) + 5;
                     srcEnd = msg.indexOf('"', srcPos);
                     iframeEnd = msg.indexOf("</iframe>", srcEnd) + 9;
                     msg = msg.substring(0, iframePos) + this.fixYTLink(msg.substring(srcPos, srcEnd)) + msg.substring(iframeEnd);
                 }
                 return msg;
 
             }
 
             /**
              * Fixes the embedded YouTube link to a regular youtu.be one.
              * @param link the raw link
              * @return The parsed message
              */
             private String fixYTLink(String link) {
                 return "http://youtu.be/" + link.substring(link.lastIndexOf('/') + 1, link.lastIndexOf('?'));
             }
 
             /**
              * Splits messages from a response into raw strings.
              * @param list a raw list of messages
              * @return a list containing raw message strings.
              * @throws ContentException
              */
             private List<String> splitMessages(String list) throws ContentException {
                 int lastCloseDivs, lastMessagePosition = 0;
                 List<String> messages = new LinkedList<String>();
                 while ((lastCloseDivs = list.indexOf("</div></div>", lastMessagePosition)) > 0) {
                     messages.add(list.substring(lastMessagePosition, lastCloseDivs));
                     lastMessagePosition = lastCloseDivs + 12;
                 }
                 return messages;
             }
 
             /**
              * Parses Conversations table, returning raw conversation strings as a list.
              * @param table a string containing a raw conversation HTML table.
              * @return a list of raw conversation strings.
              */
             private List<String> parseTableRows(String table) {
                 List<String> conversations = new ArrayList<String>(20);
 
                 //If no conversation is present, return null
                 int beginning = table.indexOf("<tr ");
                 if (beginning < 0) {
                     return null;
                 }
 
                 //Everything except conversations table should be removed
                 table = table.substring(beginning, table.indexOf("</table>"));
                 int tdIndex, endTrIndex = 0;
 
                 while ((tdIndex = table.indexOf("<td", endTrIndex)) != -1) {
 
                     endTrIndex = table.indexOf("</tr>", endTrIndex + 5); //add 5 to last value found
                     conversations.add(table.substring(tdIndex, endTrIndex));
 
                 }
                 return conversations;
             }
 
             /**
              * Parses a raw conversation string,  returning a Conversation.
              * @param row a raw conversation string
              * @return A Conversation containing data parsed from row.
              * @throws ContentException
              */
             private Conversation parseConversationRow(String row) throws ContentException {
 
                 int otherNamePosition = row.indexOf("<a href=");
                 if (otherNamePosition < 0) {
                     throw new ContentException("Malformed content \"" + row + "\"");
                 }
 
                 String otherName = StringEscapeUtils.unescapeHtml4(row.substring(row.indexOf('>', otherNamePosition) + 1, row.indexOf("</a>")));
 
                 int dataFromPosition = row.indexOf("data-from=\"");
                 if (dataFromPosition < 0) {
                     throw new ContentException("Malformed content \"" + row + "\"");
                 }
                 dataFromPosition += 11;
 
                 int dataFrom = Integer.parseInt(row.substring(dataFromPosition, row.indexOf('"', dataFromPosition)));
 
                 int dataTimePosition = row.indexOf("data-timestamp=\"");
                 if (dataTimePosition < 0) {
                     throw new ContentException("Malformed content \"" + row + "\"");
                 }
                 dataTimePosition += 16;
 
                 Date lastDate = new Date(Long.parseLong(row.substring(dataTimePosition, row.indexOf('"', dataTimePosition))) * 1000L);
 
                 return new ReverseConversation(otherName, dataFrom, lastDate);
             }
 
 
         };
     }
 
 
     @Override
     public ConversationHandler getConversationHandler() {
         return this.mConversationHandler;
     }
 
     @Override
     public void sendMessage(String to, String message) throws IOException, HttpException, ContentException, BadStatusException {
 
         HashMap<String, String> form = new HashMap<String, String>(2);
 
         form.put("to", to);
         form.put("message", message);
 
         try {
             JSONObject response = new JSONObject(this.post("/pages/pm/send.json.php", form, AbstractReverseApplication.NERDZ_DOMAIN_NAME + "/pm.php"));
 
             if (!(response.getString("status").equals("ok") && response.getString("message").equals("OK"))) {
                 throw new BadStatusException("wrong status couple (" + response.getString("status") + ", " + response.getString("message") + "), message not sent");
             }
         } catch (JSONException e) {
             throw new ContentException("Error while parsing JSON in new messages: " + e.getLocalizedMessage());
         }
 
     }
 
     @Override
     public int newMessages() throws IOException, HttpException, ContentException {
         try {
             return new JSONObject(this.get("/pages/pm/notify.json.php")).getInt("message");
         } catch (JSONException e) {
             throw new ContentException("Error while parsing JSON in new messages: " + e.getLocalizedMessage());
         }
     }
 
     @Override
     public Features[] getSupportedFeatures() {
         return new Features[]{ Features.MESSENGER };
     }
 }
