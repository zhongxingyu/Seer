 package directi.androidteam.training.chatclient.Chat;
 
 import directi.androidteam.training.StanzaStore.MessageStanza;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vinayak
  * Date: 26/9/12
  * Time: 5:25 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ComboMessage {
     private MessageStanza ms;
     private ChatListItem cli;
     public ComboMessage(MessageStanza messageStanza,ChatListItem chatListItem){
         this.ms = messageStanza;
         this.cli = chatListItem;
     }
     public void setCli(ChatListItem cli){
         this.cli=cli;
     }
     public void setFailure(){
        if(cli==null)
            return;
         cli.setStatus(false);
         ms.setStatus(false);
     }
 }
