 package directi.androidteam.training.chatclient.PacketHandlers;
 
 import android.util.Log;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.TagStore.Tag;
 import directi.androidteam.training.chatclient.Chat.ChatBox;
 import directi.androidteam.training.chatclient.Chat.ChatNotifier;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 6/9/12
  * Time: 6:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MessageHandler implements Handler{
 
     private static final MessageHandler messageHandler = new MessageHandler();
 
 
 
     private HashMap<String,NotifierArrayList> chatLists;
 
     private MessageHandler(){
         chatLists = new HashMap<String, NotifierArrayList>();
 
         //chatpanes.put("vinayak.bhavnani",new ChatBox());
     }
 
      public String FragToJid(int i){
          return (String)chatLists.keySet().toArray()[i];
      }
 
      public int JidToFrag(String from){
          addChatcontact(from);
          String[] t=null;
          Log.d("arraysize",new Integer(chatLists.keySet().size()).toString());
          Object[]  set = chatLists.keySet().toArray();
          for(int i=0;i<chatLists.size();i++){
              Log.d("keyval",(String)set[i]);
             if(((String)set[i]).equals(from))
                 return i;
          }
 
          return -1;
 
      }
 
      public  ArrayList<MessageStanza> getFragList(String from){
          addChatcontact(from);
          return chatLists.get(from);
      }
 
     public static MessageHandler getInstance(){
         return messageHandler;
     }
 
     public HashMap<String,NotifierArrayList> getChatLists() {
         return chatLists;
     }
 
     private void addChatcontact(String from){
         if(!chatLists.containsKey(from)){
             chatLists.put(from, new NotifierArrayList());
             ChatBox.recreateFragments();
         }
     }
     public void addChat(String from , MessageStanza ms){
         addChatcontact(from);
         chatLists.get(from).add(ms);
     }
 
     @Override
     public void processPacket(Tag tag){
 
         MessageStanza ms = new MessageStanza(tag);
         String message = ms.getBody();
         String from = ms.getTag().getAttribute("from").split("/")[0];
         if(ms.getBody()==null)
             return;
         addChat(from,ms);
 
         Log.d("chatsize",new Integer(chatLists.get(from).size()).toString()+from);
         //ChatBox.openChat(from);

        ChatBox.notifyChat(ms);
 
    }
 }
