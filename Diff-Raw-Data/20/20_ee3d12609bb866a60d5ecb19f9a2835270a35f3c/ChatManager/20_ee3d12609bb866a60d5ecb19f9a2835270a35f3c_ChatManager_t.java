 package com.craftminecraft.craftchat;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Map;
 import java.io.File;
import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.regex.Pattern;
 
 import org.bukkit.entity.Player;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.configuration.ConfigurationSection;
 
 import com.craftminecraft.craftchat.CraftChat;
 import com.craftminecraft.craftchat.conversations.Channel;
 import com.craftminecraft.craftchat.utils.Utils;
 
 public class ChatManager {
     private CraftChat plugin;
     // Make those things thread-safe.
     private List<Channel> channelList;
     private ConcurrentHashMap<Player, List<Channel>> playerConvs;
     private ConcurrentHashMap<Player, Channel> playerConvFocus;
     private ConcurrentHashMap<String, String> formats;
 
     public ChatManager(CraftChat plugin) {
         this.plugin = plugin;
         this.channelList = new ArrayList<Channel>();
         this.playerConvs = new ConcurrentHashMap<Player, List<Channel>>();
         this.playerConvFocus = new ConcurrentHashMap<Player, Channel>();
         this.formats = new ConcurrentHashMap<String, String>();
         // SETUP FORMATS //
         for (String key : this.plugin.getConfig().getConfigurationSection("formats").getKeys(false)) {
             formats.put(key, this.plugin.getConfig().getString("formats." + key));
         }
 
         // SETUP CHANNEL CONFIG //
         File chanListFile = new File(this.plugin.getDataFolder(), "channels.yml");
         YamlConfiguration chanListConfig = YamlConfiguration.loadConfiguration(chanListFile);
         InputStream defChanListStream = this.plugin.getResource("channels.yml");
         if (defChanListStream != null) {
             YamlConfiguration defChanList = YamlConfiguration.loadConfiguration(defChanListStream);
             chanListConfig.setDefaults(defChanList);
            try {
                chanListConfig.save(new File(this.plugin.getDataFolder(), "channels.yml"));
            } catch (IOException e) {
                this.plugin.getLogger().warning("Couldn't save channels.yml. Oh well.");
            }
         } else {
            this.plugin.getLogger().warning("You modified our jar, builtin channels.yml is not found ! Bad boy !");
         }
         // SET UP CHANNEL LIST //
         Map<String,Object> chanList = chanListConfig.getDefaultSection().getValues(false);
         for (Map.Entry<String, Object> chan : chanList.entrySet()) {
             ConfigurationSection chanConfig = (ConfigurationSection) chan.getValue();
             this.channelList.add(new Channel(chanConfig));
         }
     }
 
     public void autoJoinChannels(Player p) {
         for(Channel channel : channelList) {
             if (p.hasPermission("cmc.join." + channel.getName())) {
                 joinChannel(p, channel);
                 setFocus(p, channel);
                 // last channel to join is focus. Will change... eventually.
             }
         }
     }
 
     public void autoLeaveChannels(Player p) {
         for (Channel channel : this.playerConvs.get(p)) {
             channel.leave(p);
         }
         this.playerConvs.remove(p);
         this.playerConvFocus.remove(p);
     }
 
     public boolean joinChannel(Player p, Channel joinChan) {
         List<Channel> convList = playerConvs.get(p.getName());
         if (convList == null) {
             convList = new ArrayList<Channel>();
         }
         convList.add(joinChan);
         this.playerConvs.put(p, convList);
 
         // also put in participents of channel.
         joinChan.join(p);
         return true;
     }
 
     public void leaveChannel(Player p, String leaveChan) {
         for (Channel channel : channelList) {
             if (channel.getName() == leaveChan) {
                 this.leaveChannel(p, channel);
                 break;
             }
         }
     }
 
     public void leaveChannel(Player p, Channel leaveChan) {
         List<Channel> convList = playerConvs.get(p.getName());
         if (convList != null) {
             convList.remove(leaveChan);
             this.playerConvs.put(p, convList);
         }
         leaveChan.leave(p);
     }
 
     public void setFocus(Player p, Channel channel) {
         playerConvFocus.put(p, channel);
     }
 
     public Channel getFocus(Player p) {
         return playerConvFocus.get(p);
     }
 
     
     public Channel getChannel(String name) {
         for (Channel channel : channelList) {
             if ((channel.getName().equalsIgnoreCase(name)) || (channel.getNick().equalsIgnoreCase(name))) {
                 return channel;
             }
         }
         return null;
     }
 
     public String formatString(Player player) {
         return formatString(player, getFocus(player));
     }
 
     public String formatString(Player player, Channel channel) {
         String format = channel.getFormat();
         for (String formatname : formats.keySet()) {
             format = format.replaceAll("(?i)" + Pattern.quote("{" + formatname + "}"), formats.get(formatname));
         }
         return Utils.formatString(format, player, channel);
     }
 
     public void chat(Player sender, String message, Channel channel) {
         String formatString = formatString(sender, channel);
         message = String.format(formatString, sender.getDisplayName(), message);
         for (Player player : channel.getParticipants()) {
             player.sendMessage(message);
         }
     }
 
     public List<Channel> getChannelList() {
         return channelList;
     }
 }
