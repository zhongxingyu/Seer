 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package gvME;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 import javax.microedition.io.HttpsConnection;
 import javax.microedition.rms.InvalidRecordIDException;
 import javax.microedition.rms.RecordStoreException;
 import javax.microedition.rms.RecordStoreNotOpenException;
 
 /**
  *
  * @author matt
  */
 public class parseMsgs {
 
     private static final String storedMsgName = "storedMsgs";
     private static final String markReadURL = "https://www.google.com/voice/m/mark?p=1&label=unread&id=";
     private static final String getMsgsURL = "https://www.google.com/voice/inbox/recent/unread";//"https://www.google.com/voice/m/i/unread/";
 
     private static final String jsonBegin = "<json><![CDATA[{\"messages\":{\"";
     private static final String idToken = "{\"id\":\"";
     private static final String separateToken = "\",\"";
     private static final String phoneNumToken = "phoneNumber\":\"+1";
     private static final String jsonEnd = "}}";
     private static final String beginMsgToken = "<div id=\"";
     private static final String msgFromToken = "<span class=\"gc-message-sms-from\">";
     private static final String endSpan = "</span>";
     private static final String msgTextToken = "<span class=\"gc-message-sms-text\">";
     private static final String msgTimeToken = "<span class=\"gc-message-sms-time\">";
     private static final String endMsgToken = "<table class=\"gc-message-bg-bottom\">";
     private static final String isReadToken = "\"isRead\":";
     private static final String isSpamToken = ",\"isSpam\"";
     private static final String dateToken = "displayStartDateTime\":\"";
     private static final String noMsgsString = "No unread items in your inbox.";
     private static final String msgBottomToken = "<td class=\"gc-sline-bottom\">";
 
     private final static String[] contentType = {"Content-Type", "application/x-www-form-urlencoded"};
     private final static String[] connection = {"Connection", "keep-alive"};
 
     private static Vector reqProps = new Vector(5);
     private static Vector convosVect = new Vector(10);
     private static Hashtable storedConvos = new Hashtable();
 
     static int count = 0;
 
     public static void setReqProps()
     {
         reqProps.insertElementAt(contentType, 0);
         reqProps.insertElementAt(connection, 1);
     }
 
     public static void setConvosVect(Vector convos)
     {
         parseMsgs.convosVect = convos;
     }
 
     public static Vector getReqProps()
     {
         return parseMsgs.reqProps;
     }
 
     public static Vector getConvosVect()
     {
         return parseMsgs.convosVect;
     }
     
     public static void removeConvo(textConvo convo)
     {
         parseMsgs.convosVect.removeElement(convo);
         try {
             setStoredConvos();
         } catch (InvalidRecordIDException ex) {
             ex.printStackTrace();
         } catch (IOException ex) {
             ex.printStackTrace();
         } catch (RecordStoreNotOpenException ex) {
             ex.printStackTrace();
         } catch (RecordStoreException ex) {
             ex.printStackTrace();
         }
     }
     public static Vector initStoredConvos() throws InvalidRecordIDException, IOException, RecordStoreNotOpenException, RecordStoreException
     {
         setReqProps();
         convosVect = updateRS.vectFromRS(storedMsgName);
         setStoredConvos(convosVect);
         return convosVect;
     }
     public static void setStoredConvos(Vector convos) throws InvalidRecordIDException, IOException, RecordStoreNotOpenException, RecordStoreException
     {     
         Enumeration convosEnum = convos.elements();
         textConvo crnt;
         parseMsgs.storedConvos = new Hashtable();
         while(convosEnum.hasMoreElements())
         {
              crnt = (textConvo) convosEnum.nextElement();
              parseMsgs.storedConvos.put(crnt.getMsgID(), crnt);
         }    
     }
 
     public static void setStoredConvos() throws IOException, RecordStoreException
     {
         Enumeration convosEnum = convosVect.elements();
         textConvo crnt;
             parseMsgs.storedConvos = new Hashtable();
         while(convosEnum.hasMoreElements())
         {
              crnt = (textConvo) convosEnum.nextElement();
              parseMsgs.storedConvos.put(crnt.getMsgID(), crnt);
         }
         updateRS.fromVect(convosVect, storedMsgName);
     }
 
     public static Hashtable getStoredConvosHash()
     {
         return parseMsgs.storedConvos;
     }
 
     public static Vector getStoredConvos()
     {
         return convosVect;
     }
 
     public static int readMsgs() throws IOException, Exception
     {
         String html = getHTML();
 
         //checks to see if new messages have arrived and returns if none have
         if(html.indexOf(noMsgsString) > 0)
             return 0;
         //gets message's ID & replyNums from json
         Hashtable convos = getJSONdata(html);
 
         //goes through xml for each msgID found in json (now existing as a hash enumeration)
         Enumeration convosEnum = convos.keys();
         Vector msgVect = null, oldMsgs;
         String Key = "";
         int newMsgCnt = 0;
         textConvo messages;
         while(convosEnum.hasMoreElements())
         {
            Key = (String) convosEnum.nextElement();
 
            messages = getMsgs(Key, html);
            if(messages == null)
                break;
            msgVect = (Vector) messages.getMessages();
 
            if(storedConvos.containsKey(Key))
            {
                 oldMsgs = (Vector) ((textConvo) storedConvos.get(Key)).getMessages();
                 msgVect = tools.combineVectors(oldMsgs, msgVect);
            }
 
            textConvo crnt = (textConvo) ((Vector)convos.get(Key)).firstElement();
            crnt.setConvo(messages);
            storedConvos.put(Key, crnt);
            convosVect.addElement(crnt);
            newMsgCnt++;
        }
 
         if(convosVect.size() > 0)
         {
             updateRS.fromVect(convosVect, storedMsgName);
         }
 
         gvME.setNumNewMsgs(newMsgCnt);
         return newMsgCnt;
     }
 
     public static textConvo getMsgs(String msgID, String html) throws IOException, Exception
     {
         Vector msgVect = new Vector(10);
         int i = 0;
         textMsg crnt = null;
         String beginToken = beginMsgToken + msgID;
         html = (String) regexreplace(i, html, beginToken, endMsgToken).getKey();
 
         int stop = html.indexOf(msgBottomToken, i);
         String sender = "";
         String message = "";
         String time = "";
         String lastMessage = "";
         KeyValuePair kvp = null;
 
 
         int numMsgs = 0;
         if(storedConvos.containsKey(msgID))
         {
             lastMessage = ((textConvo)storedConvos.get(msgID)).getLastMsg().getMessage();
         }
 
         //run until no more senders are found for the current conversation
         while(i > -1 && (checkSender((String) (kvp = regexreplace(i, stop, html, msgFromToken, endSpan)).getKey())) && i < stop && i < html.length())
         {            
             i =  castInt(kvp.getValue());
             String hold_Sender = (String) kvp.getKey();
 
             if(hold_Sender.indexOf("Me:") < 0)
             {
                 sender = parseSender(hold_Sender);//hold_Sender prevents garbage from being submitted as the sender of the message
                 kvp = regexreplace(i, html, msgTextToken, endSpan);
                 message = (String) kvp.getKey();
                 i = castInt(kvp.getValue());
                 
                 if(lastMessage.equals(message))
                 {
                     markMsgRead(msgID);
                     return null;
                 }
                 kvp = regexreplace(i, html, msgTimeToken, endSpan);
                 time = ((String) kvp.getKey()).trim();
                 
                 crnt = new textMsg(msgID, message, time);
                 numMsgs++;
                 msgVect.addElement(crnt);
             }            
             i = castInt(kvp.getValue());
         }
         markMsgRead(msgID);
         return new textConvo(numMsgs, msgID, sender, msgVect, crnt);
     }
 
     public static boolean checkSender(String sender)
     {
         if(sender.equals("") || sender.length() > 25)
             return false;
         else
             return true;
     }
 
     //mark message as read
     public static void markMsgRead(String msgID) throws IOException, IOException, Exception
     {           
         String[] strings = {
                             markReadURL,
                             msgID,
                             "&read=1"
                             };
         HttpsConnection markRead = createConnection.open(tools.combineStrings(strings), "GET", reqProps, "");
         createConnection.close(markRead);
     }
     
     public static Hashtable getJSONdata(String html)
     {
         int i = 0;
         KeyValuePair kvp;
         kvp = regexreplace(i, html, jsonBegin, jsonEnd);
         String json = (String) kvp.getKey();
 
         String msgID = "";
         String replyNum = "";
         Vector convo = new Vector(1);
         Hashtable convoHash = new Hashtable();
         String isRead = "";
         String date = "";
         while(json.indexOf(idToken, i) != -1)
         {
             kvp = regexreplace(i, json, idToken, separateToken);
             msgID = (String) kvp.getKey();
             i = castInt(kvp.getValue());
             
             kvp = regexreplace(i, json, phoneNumToken, separateToken);
             replyNum = (String) kvp.getKey();
             i = castInt(kvp.getValue());
             
             kvp = regexreplace(i, json, dateToken, separateToken);
             date = parseDate((String) kvp.getKey());
             i = castInt(kvp.getValue());          
             
             kvp = regexreplace(i, json, isReadToken, isSpamToken);
             isRead = (String) kvp.getKey();
             i = castInt(kvp.getValue());
 
             if(isRead.equals("true"))
                     break;
 
             textConvo newConvo = new textConvo(0, msgID, replyNum, date);
             convo.addElement(newConvo);
             convoHash.put(msgID, convo);
         }
         return convoHash;
     }
     /*
      *  regexreplace
      *  Finds the next occurrence of the regular expression: beginToken(.*)endToken
      *  It returns the index of the last character in the endToken
     */
     public static KeyValuePair regexreplace(int i, String html, String beginToken, String endToken)
     {
         return regexreplace(i, 0, html, beginToken, endToken);
     }
     public static KeyValuePair regexreplace(int i, int stop, String html, String beginToken, String endToken)
     {
         i = html.indexOf(beginToken, i)+beginToken.length();
         int end = html.indexOf(endToken, i);
         
         if(stop == 0 || (i >= 0 && end >= 0 && end < stop))
             return new KeyValuePair(html.substring(i, end), new Integer(end));
         else
             return new KeyValuePair("", null);
     }
     
     synchronized public static String getHTML()
     {
         count++;
         String html = "";
         try{
 
             HttpsConnection c = createConnection.open(getMsgsURL, "GET", reqProps, "");
             if(c.getResponseCode() != 200)
             {
                 createConnection.close(c);

                System.out.println(html);
                throw new Exception("request code bad"+gvME.countCons);
 
             }
             DataInputStream dis = c.openDataInputStream();
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             byte[] buffer = new byte[4096];
             int read = dis.read(buffer);
 
             while(read != -1)
             {
                 baos.write(buffer,0,read);
                 read = dis.read(buffer);
             }
             dis.close();
             html = new String(baos.toByteArray());
 
             createConnection.close(c);
             
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         finally{
             return html;
         }
     }
     public static String parseSender(String sender)
     {
         while(sender.indexOf('\n') >= 0)
         {
             sender = sender.replace('\n', ' ');
         }
         sender = sender.replace(':', ' ');
         return sender.trim();
     }
 
     public static String parseDate(String date)
     {
         int slashIndex = date.indexOf("/", date.indexOf("/")+1);
         return date.substring(0, slashIndex+3);
     }
     
     public static int castInt(Object i)
     {
         return ((Integer) i).intValue();
     }
 }
 
 
