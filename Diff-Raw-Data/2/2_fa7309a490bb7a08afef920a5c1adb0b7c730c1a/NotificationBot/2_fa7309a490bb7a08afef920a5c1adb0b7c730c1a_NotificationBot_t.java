 package jk_5.nailed.client.updateNotifier;
 
import jk_5.nailed.common.NailedLog;
 import net.minecraft.client.Minecraft;
 import org.jibble.pircbot.PircBot;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class NotificationBot extends PircBot {
 
     private final String host;
     private final int port;
     private final String channel;
 
     public NotificationBot(String host, int port, String channel) {
         this.host = host;
         this.port = port;
         this.channel = channel;
 
         this.setName(Minecraft.getMinecraft().getSession().getUsername());
         this.setVersion("1.0");
         this.setAutoNickChange(true);
     }
 
     public void connect(){
         try{
             this.connect(this.host, this.port);
             this.joinChannel(this.channel);
         }catch (Exception e){
             NailedLog.severe(e, "Error while connecting with update server");
         }
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         UpdateNotificationManager.handleIncoming(message);
     }
 
     @Override
     protected void onPrivateMessage(String sender, String login, String hostname, String message) {
         UpdateNotificationManager.handleIncoming(message);
     }
 }
