 package directi.androidteam.training.chatclient.Chat;
 
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.chatclient.Chat.dbAccess.dbAccess;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 10/2/12
  * Time: 1:13 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MessageManager {
     private static MessageManager messageManager = new MessageManager();
     HashMap<String,ArrayList<MessageStanza>> messageStore;
     ChatFragment listener_frag;
 
     private MessageManager() {
         messageStore = convertListToMap(new dbAccess().getAllMsg());
         for (String s : messageStore.keySet()) {
             messageStore.put(s,new MsgGroupFormating().formatMsgList(messageStore.get(s)));
         }
         //messageStore = new HashMap<String, ArrayList<MessageStanza>>();
     }
 
     public static MessageManager getInstance() {
         return messageManager;
     }
 
     public void insertMessage(String from, MessageStanza ms) {
         if(!messageStore.containsKey(from)) {
             ArrayList<MessageStanza> arrayList = new ArrayList<MessageStanza>();
             arrayList.add(ms);
             messageStore.put(from,arrayList);
 /*
             if(ChatBox.getContext()!=null)
                 ChatBox.recreateFragments();
 */
             propagateChangesToFragments(ms, false);
         }
         else {
             ArrayList<MessageStanza> arrayList = messageStore.get(from);
             if(arrayList.size()>0) {
                 MessageStanza lastMessageStanza = arrayList.get(arrayList.size()-1);
                 if(lastMessageStanza.getFrom()!=null && lastMessageStanza.getFrom().equals(ms.getFrom())) {
                     MsgGroupFormating msgGroupFormating = new MsgGroupFormating(lastMessageStanza,ms);
                     Boolean bool = msgGroupFormating.formatMsg();
                     if(bool) {
                         lastMessageStanza.appendBody(ms.getBody());
                         propagateChangesToFragments(lastMessageStanza, true);
                     }
                     else {
                         arrayList.add(ms);
                         propagateChangesToFragments(ms, false);
                     }
                 }
                 else {
                     arrayList.add(ms);
                     propagateChangesToFragments(ms, false);
                 }
             }
             else {
                 arrayList.add(ms);
                 propagateChangesToFragments(ms, false);
             }
         }
         addToDB(ms);
     }
 
     private void removeFromDB(final String id) {
         Thread t = new Thread() {public void run() { dbAccess db =  new dbAccess(); db.removeMsg(id);}};
         t.start();
     }
 
     private void addToDB(final MessageStanza ms) {
         Thread t = new Thread() {public void run() { dbAccess db =  new dbAccess(); db.addMessage(ms);}};
         t.start();
     }
 
     private void propagateChangesToFragments(MessageStanza ms, boolean b) {
         if(ChatBox.getContext()!=null)
             ChatBox.recreateFragments();
         if (listener_frag!=null)
             listener_frag.addChatItem(ms,b);
     }
 
     public void registerFragment(ChatFragment frag){
         this.listener_frag = frag;
     }
 
     public HashMap<String, ArrayList<MessageStanza>> getMessageStore() {
         return messageStore;
     }
 
     public ArrayList<MessageStanza> getMsgList(String from) {
         if(from==null || !messageStore.containsKey(from))
             return null;
         return messageStore.get(from);
     }
 
     public void insertEntry(String from) {
         if(messageStore==null)
             messageStore = new HashMap<String, ArrayList<MessageStanza>>();
         if(!messageStore.containsKey(from)) {
             messageStore.put(from,new ArrayList<MessageStanza>());
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
 
     public HashMap<String,ArrayList<MessageStanza>> convertListToMap(ArrayList<MessageStanza> messageStanzas) {
         HashMap<String,ArrayList<MessageStanza>> map = new HashMap<String, ArrayList<MessageStanza>>();
         if(messageStanzas==null)
             return map;
         for (MessageStanza messageStanza : messageStanzas) {
             if(map.containsKey(messageStanza.getFrom())) {
                 map.get(messageStanza.getFrom()).add(messageStanza);
             }
             else {
                 ArrayList<MessageStanza> arrayList = new ArrayList<MessageStanza>();
                 arrayList.add(messageStanza);
                 map.put(messageStanza.getFrom(),arrayList);
             }
         }
         return map;
     }
 }
