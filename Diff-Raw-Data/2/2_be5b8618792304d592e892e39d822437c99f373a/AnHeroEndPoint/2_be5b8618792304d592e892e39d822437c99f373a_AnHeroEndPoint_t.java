 package org.dequis.anherobridge;
 
 import java.util.List;
 
 import org.bukkit.plugin.Plugin;
 
 import com.dthielke.herochat.Herochat;
 import com.dthielke.herochat.Channel;
 
 import com.ensifera.animosity.craftirc.CraftIRC;
 import com.ensifera.animosity.craftirc.EndPoint;
 import com.ensifera.animosity.craftirc.RelayedMessage;
 
 public class AnHeroEndPoint implements EndPoint {
 
     private CraftIRC craftirc;
     public String herotag;
     public String irctag;
 
     private Channel herochatChannel;
 
     public AnHeroEndPoint(CraftIRC craftirc, String herotag, String irctag) {
         this.craftirc = craftirc;
         this.herotag = herotag;
         this.irctag = irctag;
 
        this.herochatChannel = Herochat.getChannelManager().getChannel(herotag);
     }
 
     public void register() {
         this.craftirc.registerEndPoint(this.irctag, this);
     }
 
     public void unregister() {
         this.craftirc.unregisterEndPoint(this.irctag);
     }
 
     @Override
     public Type getType() {
         return EndPoint.Type.MINECRAFT;
     }
 
     @Override
     public void messageIn(RelayedMessage msg) {
         // msg.getEvent() == "action", herochatChannel.emote?
         this.herochatChannel.announce(msg.getMessage(this));
     }
 
     @Override
     public boolean userMessageIn(String username, RelayedMessage msg) {
         return false;
     }
 
     @Override
     public boolean adminMessageIn(RelayedMessage msg) {
         return false;
     }
 
     @Override
     public List<String> listUsers() {
         return null;
     }
 
     @Override
     public List<String> listDisplayUsers() {
         return null;
     }
 }
