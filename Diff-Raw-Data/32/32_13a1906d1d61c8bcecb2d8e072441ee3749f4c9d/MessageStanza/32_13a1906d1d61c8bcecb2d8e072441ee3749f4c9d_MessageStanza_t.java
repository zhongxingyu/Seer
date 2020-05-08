 package directi.androidteam.training.StanzaStore;
 
 import directi.androidteam.training.TagStore.MessageTag;
 import directi.androidteam.training.TagStore.Tag;
 import directi.androidteam.training.chatclient.Authentication.Account;
 import directi.androidteam.training.chatclient.Authentication.AccountManager;
 import directi.androidteam.training.chatclient.Chat.ChatStore;
 import directi.androidteam.training.chatclient.Util.PacketWriter;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 3/9/12
  * Time: 6:22 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MessageStanza extends TagWrapper{
     private long time;
 
     public void setMsgMergedCount(int msgMergedCount) {
         this.msgMergedCount = msgMergedCount;
     }
 
     private int msgMergedCount = 0;
 
     public void setTime(long time) {
         this.time = time;
     }
 
     public MessageStanza(String to, String body){
         tag = new MessageTag(to,body,null);
         setCurrentTime();
     }
 
     public MessageStanza(String to, String body, String subject){
         tag = new MessageTag(to,body,subject);
         setCurrentTime();
     }
 
     public MessageStanza(Tag tag) {
         this.tag = tag;
         setCurrentTime();
     }
 
     public MessageStanza(String to) {
         tag = new MessageTag(to);
     }
 
     public void setID(String id) {
         tag.addAttribute("id",id);
     }
 
     public void appendBody(String appendText) {
         MessageTag messageTag = new MessageTag(tag);
         String prevBody = messageTag.getBody();
         messageTag.setBody(prevBody + "\n" + appendText);
         tag = messageTag;
         msgMergedCount++;
     }
 
     public void formActiveMsg() {
         MessageTag messageTag = new MessageTag(tag);
         messageTag.addActiveTag();
         tag = messageTag;
     }
 
     public void formInActiveMsg() {
         MessageTag messageTag = new MessageTag(tag);
         messageTag.addInactive();
         tag = messageTag;
     }
     public void formComposingMsg() {
         MessageTag messageTag = new MessageTag(tag);
         messageTag.addComposingTag();
         tag = messageTag;
     }
     public void formGoneMsg() {
         MessageTag messageTag = new MessageTag(tag);
         messageTag.addGoneTag();
         tag = messageTag;
     }
     public void formPausedMsg() {
         MessageTag messageTag = new MessageTag(tag);
         messageTag.addPaused();
         tag = messageTag;
     }
 
     public boolean isStatus() {
         return status;
     }
 
     public void setStatus(boolean status) {
         this.status = status;
     }
 
     private boolean status = true;
 
     private void setCurrentTime(){
         time = System.currentTimeMillis();
     }
 
     public long getTime() {
         return time;
     }
 
     public String getBody(){
         ArrayList<Tag> children = tag.getChildTags();
         for (Tag child : children) {
             if(child.getTagname().equals("body"))
                 return child.getContent();
         }
 
         return null;
     }
     public Tag getTag(){
         return tag;
     }
     public String getFrom(){
         String from  = tag.getAttribute("from");
         if(from==null)
             return null;
         else
            return from.split("/")[0];
     }
 
     public String getTo(){
         return tag.getAttribute("to").split("@")[0];
     }
     public String getID(){
         return tag.getAttribute("id");
     }
 
     public String getChatState() {
         ArrayList<Tag> childList = tag.getChildTags();
         if(childList==null)
             return "null";
         for (Tag tag1 : childList) {
             if(tag1.getTagname().equals("cha:active"))
                 return "active";
             else if(tag1.getTagname().equals("cha:composing"))
                 return "composing";
             else if(tag1.getTagname().equals("cha:gone"))
                 return "gone";
             else if(tag1.getTagname().equals("cha:inactive"))
                 return "inactive";
         }
 
         return "no chatstate";
     }
 
     public int getMsgMergedCount() {
         return msgMergedCount;
     }
 
     public void setFrom(String from) {
         tag.addAttribute("from",from);
     }
 
     public void send(String jid) {
         Account account = AccountManager.getInstance().getAccount(ChatStore.getInstance().getAcctUID(jid));
         if(account==null)
             return;
         tag.addAttribute("from", account.getFullJID());
         tag.setRecipientAccount(account.getAccountUid());
         setID(UUID.randomUUID().toString());
         PacketWriter.addToWriteQueue(getTag());
     }
 }
