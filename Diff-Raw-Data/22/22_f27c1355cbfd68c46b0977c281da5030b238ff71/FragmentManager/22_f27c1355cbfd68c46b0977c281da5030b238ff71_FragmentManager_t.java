 package directi.androidteam.training.chatclient.Chat;
 
 import directi.androidteam.training.StanzaStore.MessageStanza;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: ssumit
  * Date: 10/3/12
  * Time: 12:54 PM
  * To change this template use File | Settings | File Templates.
  */
 public class FragmentManager {
 
     private Vector<String> FragToJID;
     private static FragmentManager fragmentManager  = new FragmentManager();
     private FragmentManager() {
         FragToJID = new Vector<String>();
     }

     public static FragmentManager getInstance() {
         return fragmentManager;
 
     }
 
     public synchronized void addFragEntry(String jid) {
         for (String s : FragToJID) {
             if(s.equals(jid))
                 return;
         }
         FragToJID.add(jid);
         if(ChatBox.getContext()!=null)
             ChatBox.recreateFragments();
     }
 
     public void removeFragEntry(int index) {
         FragToJID.remove(index);
     }
 
     public synchronized void removeFragEntry(String jid) {
         for (String s : FragToJID) {
             if(s.equals(jid))
                 FragToJID.remove(s);
         }
        if(ChatBox.getContext()!=null)
            ChatBox.recreateFragments();
     }
 
     public String FragToJid(int queryJID){
         if(queryJID<0 || queryJID>= getSizeofActiveChats())
             return null;
         return FragToJID.get(queryJID);
         //return MessageManager.getInstance().getRequiredJiD(i);
     }
 
     public int JidToFrag(String from){
         if(from==null)
             return -1;
         int i =0;
         for (String s : FragToJID) {
             if(s.equals(from))
                 return i;
             i++;
         }
 
         return -1;
     }
 
     public  ArrayList<MessageStanza> getFragList(String from){
         addFragEntry(from);
         MessageManager.getInstance().insertEntry(from);
         return MessageManager.getInstance().getMsgList(from);
     }
 
     public int getSizeofActiveChats() {
         return FragToJID.size();
     }
 }
