 package hu.rgai.android.messageproviders;
 
 import android.content.Context;
 import android.content.Intent;
 import hu.uszeged.inf.rgai.messagelog.MessageProvider;
 import hu.uszeged.inf.rgai.messagelog.beans.account.FacebookAccount;
 import hu.uszeged.inf.rgai.messagelog.beans.FacebookMessageRecipient;
 import hu.uszeged.inf.rgai.messagelog.beans.fullmessage.MessageAtom;
 import hu.uszeged.inf.rgai.messagelog.beans.MessageListElement;
 import hu.uszeged.inf.rgai.messagelog.beans.MessageRecipient;
 import hu.uszeged.inf.rgai.messagelog.beans.Person;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 import java.security.cert.CertPathValidatorException;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import javax.mail.AuthenticationFailedException;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import javax.net.ssl.SSLHandshakeException;
 
 import org.jivesoftware.smack.Chat;
 
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.SmackConfiguration;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.util.Log;
 
 
 import com.facebook.HttpMethod;
 import com.facebook.Request;
 import com.facebook.Session;
 import com.facebook.Response;
 import com.facebook.model.GraphObject;
 import hu.rgai.android.config.Settings;
 import hu.uszeged.inf.rgai.messagelog.ThreadMessageProvider;
 import hu.uszeged.inf.rgai.messagelog.beans.fullmessage.FullMessage;
 import hu.uszeged.inf.rgai.messagelog.beans.fullmessage.FullThreadMessage;
 import java.util.Collection;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Presence;
 
 /**
  *
  * @author Tamas Kojedzinszky
  */
 public class FacebookMessageProvider implements ThreadMessageProvider {
 
   private static XMPPConnection xmpp = null;
   // use this variable to access facebook
   private FacebookAccount account;
   private MessageListener mslistener;
 //  private Activity activity;
 
 // public void onCreate(Bundle savedInstanceState) {
 //         super.onCreate(savedInstanceState);
 //         // setContentView(R.layout.activity_main);
 // }
 //
   public FacebookMessageProvider(FacebookAccount account) {
     this.account = account;
   }
 
   public List<MessageListElement> getMessageList(int offset, int limit)
           throws CertPathValidatorException, SSLHandshakeException, ConnectException,
           NoSuchProviderException, UnknownHostException, IOException, MessagingException,
           AuthenticationFailedException {
     return getMessageList(offset, limit, 20);
   }
   
   @Override
   public List<MessageListElement> getMessageList(int offset, int limit, int snippetMaxLength)
           throws CertPathValidatorException, SSLHandshakeException, ConnectException,
           NoSuchProviderException, UnknownHostException, IOException, MessagingException,
           AuthenticationFailedException {
     Bundle params = new Bundle();
 
     final List<MessageListElement> messages = new LinkedList<MessageListElement>();
 
     String fqlQuery = "{"
             + "'msgs':"
             + "'SELECT thread_id, originator, recipients, unread, unseen, subject, snippet, updated_time "
             + " FROM thread"
             + " WHERE folder_id = 0"
             + " ORDER BY updated_time DESC LIMIT " + offset + "," + limit + "'"
             + ","
             + "'friend':"
             + "'SELECT name, username, uid"
             + " FROM user"
             + " WHERE uid IN (SELECT recipients FROM #msgs)'"
             + "}";
     params.putString("q", fqlQuery);
 
     Session session = Session.getActiveSession();
     Request request = new Request(
             session,
             "/fql",
             params,
             HttpMethod.GET,
             new Request.Callback() {
       public void onCompleted(Response response) {
         if (response != null) {
 //                    Log.d("rgai", "Got results: " + response.toString());
           if (response.getGraphObject() != null) {
             try {
               GraphObject go = response.getGraphObject();
               JSONObject jso = go.getInnerJSONObject();
               JSONArray arr = jso.getJSONArray("data");
 
               // loop through result sets
               for (int i = 0; i < (arr.length()); i++) {
                 JSONObject resultSet = arr.getJSONObject(i);
                 String resSetName = resultSet.getString("name");
                 String resSet = resultSet.getString("fql_result_set");
                 if (resSetName.equals("msgs")) {
                   JSONArray msgArr = new JSONArray(resSet);
 
                   // loop through messages
                   for (int j = 0; j < msgArr.length(); j++) {
                     JSONObject msg = msgArr.getJSONObject(j);
 
                     // fetching recipients
                     JSONArray recipientsArr = new JSONArray(msg.getString("recipients"));
                     List<String> recipIds = new LinkedList<String>();
                     for (int l = 0; l < recipientsArr.length(); l++) {
 //                                JSONObject recipient = new
                       String id = recipientsArr.getString(l);
                      Log.d("rgai", account.toString());
                       if (!account.getId().equals(id) && recipIds.isEmpty()) {
                         recipIds.add(id);
                       }
                     }
 
                     // recipipIds MUST contain an id which is not mine
                     assert !recipIds.isEmpty();
 
                     // building list item title
                     boolean seen = msg.getInt("unseen") == 0;
                     int unreadCount = msg.getInt("unread");
                     String snippet = msg.getString("snippet")/*.replaceAll("\n", " ").replaceAll(" {2,}", " ")*/;
 //                    if (snippet.length() > 30) {
 //                      snippet = snippet.substring(0, 30) + "...";
 //                    }
 //                    if (!seen && unreadCount > 0) {
 //                      snippet = "(" + unreadCount + ") " + snippet;
 //                    }
                     messages.add(new MessageListElement(
                             msg.getString("thread_id"),
                             seen,
                             snippet,
                             unreadCount,
                             new Person(recipIds.get(0), null, MessageProvider.Type.FACEBOOK),
                             new Date(msg.getLong("updated_time") * 1000),
                             MessageProvider.Type.FACEBOOK));
                   }
                 } else if (resSetName.equals("friend")) {
                   JSONArray userArr = new JSONArray(resSet);
                   // loop through friends
                   for (int j = 0; j < userArr.length(); j++) {
                     JSONObject msg = userArr.getJSONObject(j);
 //                              Log.d("rgai", msg.getString("uid"));
 //                              Log.d("rgai", msg.getString("name"));
                     // matching friend names to messages by id
                     for (int k = 0; k < messages.size(); k++) {
                       if (messages.get(k).getFrom().getId().equals(msg.getString("uid"))) {
                         messages.get(k).getFrom().setName(msg.getString("name"));
                       }
                     }
                   }
                 }
               }
             } catch (Throwable t) {
               t.printStackTrace();
             }
           }
         } else {
           Log.d("rgai", "RESPONSE IS NULL");
         }
       }
     });
     Request.executeAndWait(request);
 
     return messages;
   }
 
   public static void initConnection(FacebookAccount fba, final Context context) {
 
     if (xmpp == null || !xmpp.isConnected()) {
 
       StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
       StrictMode.setThreadPolicy(policy);
 
       ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222, "chat.facebook.com");
       config.setSASLAuthenticationEnabled(true);
       config.setSecurityMode(SecurityMode.enabled);
       config.setRosterLoadedAtLogin(true);
       config.setSendPresence(false);
 
 
       xmpp = new XMPPConnection(config);
 
       try {
         xmpp.connect();
         SmackConfiguration.setPacketReplyTimeout(10000);
         xmpp.login(fba.getUniqueName(), fba.getPassword());
 //        Roster roster = xmpp.getRoster();
 
 
 //        Collection<RosterEntry> entries = roster.getEntries();
 //        Log.d("rgai", "Connected ON XMPP!");
 
         xmpp.getChatManager().addChatListener(new ChatManagerListener() {
           @Override
           public void chatCreated(Chat chat, boolean arg1) {
 
             chat.addMessageListener(new MessageListener() {
               public void processMessage(Chat chat, Message message) {
                 if (message != null && message.getBody() != null) {
                   Log.d("rgai", "MESSAGE FROM -> " + message.getFrom());
                   Intent res = new Intent(Settings.Intents.NEW_MESSAGE_ARRIVED_BROADCAST);
                   res.putExtra("type", MessageProvider.Type.FACEBOOK.toString());
                   context.sendBroadcast(res);
 //                  RosterEntry roster = xmpp.getRoster().getEntry(message.getFrom());
 //                  System.out.println(message.getType());
 
 //                  System.out.println(roster.getName() + " : " + message.getBody());
 //                  receiveAttachments();
 //                  receivedMessages.add(roster.getName() + " : " + message.getBody());
 //                  receivedMessages.notifyDataSetChanged();
 
                 }
               }
             });
           }
         });
 
       } catch (XMPPException e) {
         Log.i("rgai", "nem lep be");
         xmpp.disconnect();
         e.printStackTrace();
       } catch (Exception k) {
         k.printStackTrace();
         System.out.println(k);
         System.out.println("HIBAA");
       }
     }
   }
   
   public static void closeConnection() {
     if (xmpp != null) {
       Log.d("rgai", "Disconnecting XMPP");
       xmpp.disconnect();
       xmpp = null;
     }
   }
 
   @Override
   public FullThreadMessage getMessage(String id, int offset, int limit) throws NoSuchProviderException, MessagingException, IOException {
 
     Bundle params = new Bundle();
     final FullThreadMessage ftm = new FullThreadMessage();
 
     String fqlQuery = "{"
             + "'msgs':"
             + "'SELECT author_id, body, created_time, message_id"
             + " FROM message"
             + " WHERE thread_id = " + id + ""
             + " ORDER BY created_time DESC"
             + " LIMIT "+ offset +","+ limit +"'"
             + ","
             + "'friend':"
             + "'SELECT name, username, uid"
             + " FROM user"
             + " WHERE uid IN (SELECT author_id FROM #msgs)'"
             + "}";
     params.putString("q", fqlQuery);
 
     Session session = Session.getActiveSession();
     Request request = new Request(
             session,
             "/fql",
             params,
             HttpMethod.GET,
             new Request.Callback() {
       public void onCompleted(Response response) {
         if (response != null) {
 //                    Log.d("rgai", "Got results: " + response.toString());
           if (response.getGraphObject() != null) {
             try {
               GraphObject go = response.getGraphObject();
               JSONObject jso = go.getInnerJSONObject();
               JSONArray arr = jso.getJSONArray("data");
 
               // loop through result sets
               // TODO: we should first collect the persons, that would more efficient
               for (int i = 0; i < (arr.length()); i++) {
                 JSONObject resultSet = arr.getJSONObject(i);
                 String resSetName = resultSet.getString("name");
                 String resSet = resultSet.getString("fql_result_set");
                 if (resSetName.equals("msgs")) {
                   JSONArray msgArr = new JSONArray(resSet);
 
                   // loop through messages
                   for (int j = 0; j < msgArr.length(); j++) {
                     JSONObject msg = msgArr.getJSONObject(j);
 
                     // building list item title
 //                    int unreadCount = msg.getInt("unread");
                     String body = msg.getString("body");
 
                     ftm.addMessage(new MessageAtom(
                             msg.getString("message_id"),
                             "",
                             body,
                             new Date(msg.getLong("created_time") * 1000),
                             new Person(msg.getString("author_id"), null, MessageProvider.Type.FACEBOOK),
                             msg.getString("author_id").equals(account.getId()),
                             MessageProvider.Type.FACEBOOK,
                             null));
                   }
                 } else if (resSetName.equals("friend")) {
                   JSONArray userArr = new JSONArray(resSet);
                   // loop through friends
                   for (int j = 0; j < userArr.length(); j++) {
                     JSONObject user = userArr.getJSONObject(j);
 //                              Log.d("rgai", msg.getString("uid"));
 //                              Log.d("rgai", msg.getString("name"));
                     // matching friend names to messages by id
                     for (MessageAtom ma : ftm.getMessages()) {
                       if (ma.getFrom().getId().equals(user.getString("uid"))) {
                         ma.getFrom().setName(user.getString("name"));
                       }
                     }
                   }
                 }
               }
             } catch (Throwable t) {
               t.printStackTrace();
             }
           }
         } else {
           Log.d("rgai", "RESPONSE IS NULL");
         }
       }
     });
     Request.executeAndWait(request);
 
 
     // EXAMPLE CODE FOR PETI
 //    Person sender = new Person(3, "Zelk Zoltán");
 //    Person me = new Person(4, "Tamas Kojedzsinszky");
 //    FullThreadMessage ffm = new FullThreadMessage();
 //    Log.d("rgai", "THREAD ID -> " + id);
 //
 //    ffm.addMessage(new MessageAtom("4", null, id, new Date(), sender, MessageProvider.Type.FACEBOOK, null));
 //    ffm.addMessage(new MessageAtom("1", null, "This is the content of a message item...", new Date(), sender, MessageProvider.Type.FACEBOOK, null));
 //    ffm.addMessage(new MessageAtom("2", null, "This is the content of a message item2...", new Date(), me, MessageProvider.Type.FACEBOOK, null));
 //    ffm.addMessage(new MessageAtom("3", null, "This is the content of a message item3...", new Date(), sender, MessageProvider.Type.FACEBOOK, null));
 //
     return ftm;
   }
 
   @Override
   public void sendMessage(Set<? extends MessageRecipient> to, String content, String subject) throws
           NoSuchProviderException, MessagingException, IOException {
 
     ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com", 5222, "chat.facebook.com");
 
     config.setSASLAuthenticationEnabled(true);
     config.setSecurityMode(SecurityMode.enabled);
     config.setRosterLoadedAtLogin(true);
     config.setSendPresence(false);
 
 //    final XMPPConnection xmpp = new XMPPConnection(config);
      if (xmpp == null || !xmpp.isConnected()) {
         try {
           xmpp.connect();
           SmackConfiguration.setPacketReplyTimeout(10000);
           xmpp.login(account.getUniqueName(), account.getPassword());
         } catch (XMPPException e) {
           xmpp.disconnect();
           e.printStackTrace();
         } catch (Exception e) {
           e.printStackTrace();
         }
      }
 
     for (MessageRecipient mr : to) {
       FacebookMessageRecipient fmr = (FacebookMessageRecipient) mr;
 
       Chat chat = xmpp.getChatManager().createChat(fmr.getId() + "@chat.facebook.com", mslistener);
 
       try {
         chat.sendMessage(content);
       } catch (XMPPException e) {
         e.printStackTrace();
       }
 
       // sending message here to facebook user using the information in FacebookMessageRecipient class
     }
   }
 
   public void onClickPickQuery() {
 
     String fqlQuery = "{"
             + "'notifications':'SELECT unread, unseen,recent_authors, thread_id FROM thread where folder_id=0 AND unseen > 0 ORDER BY thread_id DESC ',"
             + "'messages':'SELECT author_id, created_time, body FROM message WHERE thread_id IN "
             + "(SELECT thread_id FROM #notifications)',"
             + "'users':'SELECT uid, name, pic_square FROM user WHERE uid IN "
             + "(SELECT author_id FROM #messages)',"
             + "}";
 
     Bundle params = new Bundle();
     params.putString("q", fqlQuery);
 
 
     Session session = Session.getActiveSession();
     Request request = new Request(session, "/fql", params, HttpMethod.GET,
             new Request.Callback() {
       public void onCompleted(Response response) {
 
         try {
 
           GraphObject go = response.getGraphObject();
           JSONObject jso = go.getInnerJSONObject();
           JSONArray arr = jso.getJSONArray("data").getJSONObject(1)
                   .getJSONArray("fql_result_set");
           JSONArray arr2 = jso.getJSONArray("data").getJSONObject(2)
                   .getJSONArray("fql_result_set");
 
           if (arr.length() != 0) {
             String name = "";
             for (int i = 0; i < (arr.length()); i++) {
 
               JSONObject json_obj = arr.getJSONObject(i);
               JSONObject json_obj2;
 
               for (int j = 0; j < (arr2.length()); j++) {
 
                 json_obj2 = arr2.getJSONObject(j);
 
                 if (json_obj.getString("author_id").contains(json_obj2.getString("uid"))) {
                   name += json_obj2.getString("name");
                   name += " : ";
 
                 }
 
               }
 
               name += json_obj.getString("body");
               name += "\n";
               Log.i("FONTOS", name.toString());
 
             }
 
           } else {
 
             Log.i("FONTOS", "Nincs adat!");
           }
 
         } catch (JSONException e) {
           // TODO Auto-generated catch block
           e.printStackTrace();
         }
 
       }
     });
     Request.executeAndWait(request);
     // Request.executeBatchAndWait(request);
 //                 Request.executeBatchAsync(request);
 
   }
 
   public FullMessage getMessage(String id) throws NoSuchProviderException, MessagingException, IOException {
     return getMessage(id, 0, 20);
   }
 }
