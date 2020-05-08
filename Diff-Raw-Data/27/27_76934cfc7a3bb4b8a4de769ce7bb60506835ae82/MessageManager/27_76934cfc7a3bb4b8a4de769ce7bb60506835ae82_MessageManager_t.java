 package directi.androidteam.training.chatclient.Chat;
 
 import directi.androidteam.training.StanzaStore.MessageStanza;
 
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
         messageStore = new HashMap<String, ArrayList<MessageStanza>>();
     }
 
     public static MessageManager getInstance() {
         return messageManager;
     }
 
     public void insertMessage(String from, MessageStanza ms) {
         if(!messageStore.containsKey(from)) {
             ArrayList<MessageStanza> arrayList = new ArrayList<MessageStanza>();
             arrayList.add(ms);
             messageStore.put(from,arrayList);
             if(ChatBox.getContext()!=null)
                 ChatBox.recreateFragments();
            propagateChangesToFragments(ms,false);
         }
         else {
             ArrayList<MessageStanza> arrayList = messageStore.get(from);
             if(arrayList.size()>0) {
                 MessageStanza lastMessageStanza = arrayList.get(arrayList.size()-1);
                 if(lastMessageStanza.getCreater()!=null && lastMessageStanza.getCreater().equals(ms.getCreater())) {
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
     }
 
    private void propagateChangesToFragments(MessageStanza ms, boolean b) {
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
             if(ChatBox.getContext()!=null)
                 ChatBox.recreateFragments();
         }
     }
 
     public void removeEntry(String buddyid) {
         if(messageStore!=null) {
             messageStore.remove(buddyid);
         }
     }
 
     public int getSizeofActiveChats() {
         if(messageStore==null)
             return 0;
         else return messageStore.size();
     }
 
     public String getRequiredJiD(int queryJID) {
         if(queryJID<0 || messageStore==null || queryJID>=getSizeofActiveChats())
             return null;
         else return (String) messageStore.keySet().toArray()[queryJID];
     }
 }
