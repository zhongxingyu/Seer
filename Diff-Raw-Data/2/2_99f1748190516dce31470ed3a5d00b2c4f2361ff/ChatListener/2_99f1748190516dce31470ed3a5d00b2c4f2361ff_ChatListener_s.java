  package com.minecarts.barrenschat.listener;
  
 import com.herocraftonline.squallseed31.heroicdeath.HeroicDeathEvent;
 import com.minecarts.barrenschat.BarrensChat;
 import com.minecarts.barrenschat.ChatChannel;
 import com.minecarts.barrenschat.ChatFormatString;
 import com.minecarts.barrenschat.event.*;
 import com.minecarts.barrenschat.helpers.ChannelInfo;
 import com.minecarts.barrenschat.listener.PlayerListener.RecipientData;
 import com.minecarts.barrenschat.cache.CacheIgnore;
 
 import java.text.MessageFormat;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.CustomEventListener;
 import org.bukkit.event.Event;
 
  public class ChatListener extends CustomEventListener {
    private BarrensChat plugin;
  
    public ChatListener(BarrensChat instance) {
      this.plugin = instance;
    }
  
    private static enum events {
      ChatWhisperEvent, 
      ChatChannelMessageEvent, 
      ChatChannelJoinEvent, 
      ChatChannelLeaveEvent,
      ChatChannelAnnounceEvent, 
      ChatLocalMessageEvent,
      IgnoreListAddEvent, 
      IgnoreListRemoveEvent,
      ChatDefaultChangeEvent,
  
      HeroicDeathEvent;
    }
 
    public void onCustomEvent(Event event){
      try {
        events.valueOf(event.getEventName());
      } catch (IllegalArgumentException e) {
        return;
      }
  
      switch (events.valueOf(event.getEventName())){
          case ChatWhisperEvent: {
              ChatWhisperEvent e = (ChatWhisperEvent)event;
              if (e.isCancelled()) break;
 
              Player sender = e.getSender();
              Player receiver = e.getReceiver();
              String msg = e.getMessage();
 
              sender.sendMessage(MessageFormat.format(ChatFormatString.WHISPER_SEND, ChatColor.DARK_AQUA,receiver.getDisplayName(),msg));
              receiver.sendMessage(MessageFormat.format(ChatFormatString.WHISPER_RECEIVE, ChatColor.AQUA,sender.getDisplayName(),msg));
 
              this.plugin.whisperTracker.setWhisperSent(sender, receiver);
              this.plugin.whisperTracker.setWhisperReceived(sender, receiver);
 
              this.plugin.log.info("[Whisper] " + sender.getName() + " -> " + receiver.getName() + ": " + msg);
              break;
          }
          case ChatChannelMessageEvent: {
              ChatChannelMessageEvent e = (ChatChannelMessageEvent)event;
              if (e.isCancelled()) break;
 
              e.getChannel().chat(e.getPlayer(), e.getMessage());
              this.plugin.log.info("[" + e.getChannel().getName() + "] " + e.getPlayer().getName() + ": " + e.getMessage());
              break;
          }
          case ChatChannelJoinEvent: {
              ChatChannelJoinEvent e = (ChatChannelJoinEvent)event;
              if (e.isCancelled()) break;
 
              this.plugin.channelHelper.joinChannel(e.getPlayer(), e.getChannel(), e.getRejoining(),e.getAlertSelf(),e.getAlertOthers(),e.getDefault());
              this.plugin.log.info(String.format("[%s]: %s joined the channel", new Object[] { e.getChannel().getName(), e.getPlayer().getName() }));
              break;
          }
          case ChatChannelLeaveEvent: {
              ChatChannelLeaveEvent e = (ChatChannelLeaveEvent)event;
              if (e.isCancelled()) break;
              ChatChannel chan = e.getChannel();
              if(chan.getId().equalsIgnoreCase("global") || chan.getId().equalsIgnoreCase("pvp")){
                  e.getChannel().leave(e.getPlayer(), false); //Don't alert global and pvp leaves
              } else { 
                  e.getChannel().leave(e.getPlayer(), true); 
              }
 
              if (e.getReason() == "COMMAND") {
                  this.plugin.dbHelper.removePlayerChannel(e.getPlayer(), e.getChannel()); //They won't rejoin when they reconnect
              }
              this.plugin.log.info(String.format("[%s]: %s left the channel (%s)", new Object[] { e.getChannel().getName(), e.getPlayer().getName(), e.getReason() }));
              break;
          }
          case ChatChannelAnnounceEvent: {
              ChatChannelAnnounceEvent e = (ChatChannelAnnounceEvent)event;
              if (e.isCancelled()) break;
 
              e.getChannel().announce(e.getMessage());
              break;
          }
          case ChatLocalMessageEvent: {
              ChatLocalMessageEvent e = (ChatLocalMessageEvent)event;
              if(e.isCancelled()) break;
 
              for(RecipientData rd : e.getRecipients()){
                  if (CacheIgnore.isIgnoring(rd.player, e.getPlayer())) { continue; }
                  if(rd.distance <= 75){
                      rd.player.sendMessage(MessageFormat.format(ChatFormatString.USER_SAY,ChatColor.WHITE,e.getPlayer().getDisplayName(),e.getMessage()));
                  } else if(rd.distance <= 200){
                      rd.player.sendMessage(MessageFormat.format(ChatFormatString.USER_SAY,ChatColor.GRAY,e.getPlayer().getDisplayName(),e.getMessage()));
                  } else {
                      //They are out of range
                  }
              }
              break;
          }
          case ChatDefaultChangeEvent: {
              ChatDefaultChangeEvent e = (ChatDefaultChangeEvent)event;
              Player player = e.getPlayer();
              ChatChannel chan = e.getChannel();
              if(chan == null){ //If it's null, their default channel is /say
                  plugin.dbHelper.clearDefaultChannel(player);
              } else {
                  this.plugin.dbHelper.setDefaultChannel(player, chan);
              }
              break;
          }
          case HeroicDeathEvent: {
            HeroicDeathEvent e = (HeroicDeathEvent)event;
            ChatChannel chan = this.plugin.channelHelper.getChannelFromName("PVP");
            String msg = e.getDeathCertificate().getMessage();
 
            Player attacker = Bukkit.getServer().getPlayer(e.getDeathCertificate().getAttacker());
            Player defender  = Bukkit.getServer().getPlayer(e.getDeathCertificate().getDefender());
 
            java.util.ArrayList<Player>involvedList = new java.util.ArrayList<Player>();
            if(attacker != null) involvedList.add(attacker);
            if(defender != null) involvedList.add(defender);
           chan.announce(involvedList.toArray(new Player[involvedList.size()]),msg.replaceAll("\u00A7[0-Fa-f]", ""));
            break;
          }
      }//switch
    }//onCustomEvent
  }//class
