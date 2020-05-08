 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.packet.Message;
 
 /** This class holds the clean JID of the User and a chat Object in case you want to talk to this user
   */
 public class UserEntity{
  private String jid;
   private Chat chat = null;
   private MultiUserChat muc = null;
 
   /** Default is used when User is from Muc
     * 
     * @param jid The users Jid
     * @param muc Instance of the Nuc the Bot is in
     */
   public UserEntity( String jid, MultiUserChat muc ){
     this.jid = jid;
     this.muc = muc;
   }
 
   /** This Constructor is used when User is in private Chat
    * 
     * @param jid The Users Jid
     * @param chat If from a private message we already have a Chat Object
     */
   public UserEntity( String jid, Chat chat ){
     this.jid = jid;
     this.chat = chat;
   }
 
   /** Returns the Users Jid
    * 
    * @return String The user's Jid
     */
   public String getJid(){ return jid; }
 
   /** Returns a Chat Object to reply to this user
    *
     * @return Chat
     */
   public Chat getChat(){
     if( chat == null ) return muc.createPrivateChat( jid, new MessageListener(){ public void processMessage(Chat chat, Message message){} } );
     else return chat;
   }
 
 }
