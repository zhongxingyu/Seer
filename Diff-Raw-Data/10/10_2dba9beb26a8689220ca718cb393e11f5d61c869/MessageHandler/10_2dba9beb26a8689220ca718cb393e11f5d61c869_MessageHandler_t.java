 package directi.androidteam.training.chatclient.PacketHandlers;
 
 import android.util.Log;
 import directi.androidteam.training.ChatApplication;
 import directi.androidteam.training.StanzaStore.MessageStanza;
 import directi.androidteam.training.TagStore.Query;
 import directi.androidteam.training.TagStore.Tag;
 import directi.androidteam.training.chatclient.Chat.ChatBox;
 import directi.androidteam.training.chatclient.Chat.ChatNotifier;
import directi.androidteam.training.chatclient.Chat.dbAccess.dbAccess;
 
 import java.util.ArrayList;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 6/9/12
  * Time: 6:11 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MessageHandler implements Handler{
 
     public MessageHandler(){
     }
 
     @Override
     public void processPacket(Tag tag){
       //  HashMap<String,Vector<MessageStanza>> chatLists = MessageManager.getInstance().getMessageStore();
         if(tag==null || tag.getTagname()==null)
             return;
         if(tag.getTagname().equals("message")) {
            final MessageStanza ms = new MessageStanza(tag);
             String from = ms.getTag().getAttribute("from").split("/")[0];
             String chatState = ms.getChatState();
            // if(chatLists.containsKey(from) && chatState.equals("composing")) {
                 if(chatState.equals("composing")) {
                     Log.d("CHAT STATE","Compose received from :" + from);
                 ChatBox.composeToast(from +" is composing");
                 return;
             }
             else if(ms.getBody()!=null) {
                 if(ChatBox.getContext()==null){
                     ChatNotifier cn = new ChatNotifier(ChatApplication.getAppContext());
/*
                    Thread t = new Thread() {public void run() { dbAccess db =  new dbAccess(); db.addMessage(ms);}};
                    t.start();
*/
                    dbAccess db =  new dbAccess(); db.addMessage(ms);
                     cn.notifyChat(ms);
                 }
                 else {
                     ChatBox.notifyChat(ms,from);
                 }
 
             }
         }
         else if(tag.getTagname().equals("iq")) {
             ArrayList<Tag> childlist = tag.getChildTags();
             if(childlist==null)
                 return;
             if(childlist.get(0).getTagname().equals("query")) {
                 Query query = new Query(childlist.get(0));
                 String xmlnsQuery = query.getAttribute("xmlns");
                 if(xmlnsQuery==null)
                     return;
                 if(xmlnsQuery.equals("http://jabber.org/protocol/disc#info"))
                 {
                     ArrayList<Tag> queryChildList = query.getChildTags();
                     if(queryChildList==null) {
                         Log.d("MessageHandler","feature not included");
                     }
                     else {
                         Tag tag1 = queryChildList.get(0);
                         Log.d("MessageHandler","var - "+tag1.getAttribute("var"));
                     }
                 }
             }
         }
    }
 }
