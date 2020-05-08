 package directi.androidteam.training.chatclient.Chat;
 
 import android.util.Log;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.Authentication.Account;
 import directi.androidteam.training.chatclient.Authentication.AccountManager;
 import directi.androidteam.training.chatclient.Chat.dbAccess.dbAccess;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 10/2/12
  * Time: 1:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MessageManager {
     private static MessageManager messageManager = new MessageManager();
     HashMap<String,Vector<MessageStanza>> messageStore;
 
     private MessageManager() {
         messageStore = convertListToMap(new dbAccess().getAllMsg());
         for (String s : messageStore.keySet()) {
             messageStore.put(s,new MsgGroupFormating().formatMsgList(messageStore.get(s)));
             Log.d("DBDB","key : "+s);
         }
     }
 
     public static MessageManager getInstance() {
         return messageManager;
     }
 
     public void insertMessage(String from, MessageStanza ms) {
         addToDB(ms);
         if(!messageStore.containsKey(from)) {
             Vector<MessageStanza> arrayList = new Vector<MessageStanza>();
             arrayList.add(ms);
             messageStore.put(from,arrayList);
             propagateChangesToFragments(from, ms, false);
         }
         else {
             Vector<MessageStanza> arrayList = messageStore.get(from);
             if(arrayList.size()>0) {
                 MessageStanza lastMessageStanza = arrayList.get(arrayList.size()-1);
                 if(lastMessageStanza.getFrom()!=null && lastMessageStanza.getFrom().equals(ms.getFrom())) {
                     MsgGroupFormating msgGroupFormating = new MsgGroupFormating(lastMessageStanza,ms);
                     Boolean bool = msgGroupFormating.formatMsg();
                     if(bool) {
                         lastMessageStanza.appendBody(ms.getBody());
                         propagateChangesToFragments(from, lastMessageStanza, true);
                     }
                     else {
                         arrayList.add(ms);
                         propagateChangesToFragments(from, ms, false);
                     }
                 }
                 else {
                     arrayList.add(ms);
                     propagateChangesToFragments(from, ms, false);
                 }
             }
             else {
                 arrayList.add(ms);
                 propagateChangesToFragments(from, ms, false);
             }
         }
     }
 
     private void removeFromDB(final String id) {
         Thread t = new Thread() {public void run() { dbAccess db =  new dbAccess(); db.removeMsg(id);}};
         t.start();
     }
 
     private void addToDB(final MessageStanza ms) {
         Log.d("DBDB","db ");
         Thread t = new Thread() {public void run() { dbAccess db =  new dbAccess(); db.addMessage(ms);}};
         t.start();
     }
 
     private void propagateChangesToFragments(final String from, final MessageStanza ms, final boolean b) {
         if(ChatBox.getContext()==null)
             return;
 /*
         ((Activity)ChatBox.getContext()).runOnUiThread(new Runnable() {
             public void run() {        MyFragmentManager.getInstance().updateFragment(from , ms, b);
             } });
 */
         MyFragmentManager.getInstance().updateFragment(from , ms, b);
     }
 
     public HashMap<String, Vector<MessageStanza>> getMessageStore() {
         return messageStore;
     }
 
     public Vector<MessageStanza> getMsgList(String from) {
         if(from==null || !messageStore.containsKey(from))
             return null;
         return messageStore.get(from);
     }
 
     public void insertEntry(String from) {
         if(messageStore==null)
             messageStore = new HashMap<String, Vector<MessageStanza>>();
         if(!messageStore.containsKey(from)) {
             messageStore.put(from, new Vector<MessageStanza>());
         }
     }
 
     public void removeEntry(String buddyid) {
         if(messageStore!=null) {
             messageStore.remove(buddyid);
         }
     }
 
     public int getNumberofChatsInStore() {
         if(messageStore==null)
             return 0;
         else return messageStore.size();
     }
 
     public String getRequiredJiD(int queryJID) {
         if(queryJID<0 || messageStore==null || queryJID>= getNumberofChatsInStore())
             return null;
         else {
             return (String) messageStore.keySet().toArray()[queryJID];
         }
     }
 
     public HashMap<String,Vector<MessageStanza>> convertListToMap(Vector<MessageStanza> messageStanzas) {
         HashMap<String,Vector<MessageStanza>> map = new HashMap<String, Vector<MessageStanza>>();
         if(messageStanzas==null || messageStanzas.isEmpty())
             return map;
         for (MessageStanza messageStanza : messageStanzas) {
             String from = messageStanza.getFrom().split("@")[0];
             String to = messageStanza.getTo().split("@")[0];
             String myJid ;
             ArrayList<Account> accounts = AccountManager.getInstance().getAccountList();
             Account[] accounts1 = accounts.toArray(new Account[accounts.size()]);
             for (Account account : accounts1) {
                 myJid = account.getAccountUid();
                 if(from.equals(myJid)) {
                     if(map.containsKey(to)) {
                         map.get(to).add(messageStanza);
                         break;
                     }
                     else {
                         Vector<MessageStanza> arrayList = new Vector<MessageStanza>();
                         arrayList.add(messageStanza);
                         map.put(to,arrayList);
                         break;
                     }
                 }
                 else {
                     if(map.containsKey(from)) {
                         map.get(from).add(messageStanza);
                         break;
                     }
                     else {
                         Vector<MessageStanza> arrayList = new Vector<MessageStanza>();
                         arrayList.add(messageStanza);
                         map.put(from,arrayList);
                         break;
                     }
                 }
             }
 
         }
         return map;
     }
 }
