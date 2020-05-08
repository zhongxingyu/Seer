 package jk_5.nailed.irc;
 
 import com.google.common.collect.ImmutableMap;
 import cpw.mods.fml.common.eventhandler.SubscribeEvent;
 import jk_5.nailed.NailedLog;
 import jk_5.nailed.NailedServer;
 import jk_5.nailed.api.config.ConfigTag;
 import jk_5.nailed.api.events.PlayerChatEvent;
 import jk_5.nailed.api.events.PlayerJoinEvent;
 import jk_5.nailed.api.events.PlayerLeaveEvent;
 import lombok.Getter;
 import lombok.Setter;
 import net.minecraft.event.HoverEvent;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.server.management.ServerConfigurationManager;
 import net.minecraft.util.ChatComponentText;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraft.util.IChatComponent;
 import net.minecraftforge.common.MinecraftForge;
 import org.jibble.pircbot.Colors;
 import org.jibble.pircbot.PircBot;
 
 import java.util.Map;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 public class IrcBot extends PircBot {
 
     @Getter @Setter private boolean enabled = false;
     private final String host;
     private final int port;
     private String serverPassword;
     private final String channel;
     private String channelPassword;
 
     private static final Map<String, EnumChatFormatting> colors = ImmutableMap.<String, EnumChatFormatting>builder()
             .put(Colors.BLACK, EnumChatFormatting.WHITE)
             .put(Colors.BLUE, EnumChatFormatting.BLUE)
             //.put(Colors.BOLD, EnumChatFormatting.BOLD)
             .put(Colors.BROWN, EnumChatFormatting.GOLD)
             .put(Colors.CYAN, EnumChatFormatting.AQUA)
             .put(Colors.DARK_BLUE, EnumChatFormatting.DARK_BLUE)
             .put(Colors.DARK_GRAY, EnumChatFormatting.DARK_GRAY)
             .put(Colors.DARK_GREEN, EnumChatFormatting.DARK_GREEN)
             .put(Colors.GREEN, EnumChatFormatting.GREEN)
             .put(Colors.LIGHT_GRAY, EnumChatFormatting.GRAY)
             .put(Colors.MAGENTA, EnumChatFormatting.LIGHT_PURPLE)
             .put(Colors.NORMAL, EnumChatFormatting.RESET)
             .put(Colors.OLIVE, EnumChatFormatting.DARK_GREEN)
             .put(Colors.PURPLE, EnumChatFormatting.DARK_PURPLE)
             .put(Colors.RED, EnumChatFormatting.RED)
             .put(Colors.TEAL, EnumChatFormatting.DARK_AQUA)
             //.put(Colors.UNDERLINE, EnumChatFormatting.UNDERLINE)
             .put(Colors.WHITE, EnumChatFormatting.BLACK)
             .put(Colors.YELLOW, EnumChatFormatting.YELLOW).build();
 
     public IrcBot(){
         ConfigTag tag = NailedServer.getConfig().getTag("irc").useBraces();
         this.enabled = tag.getTag("enabled").getBooleanValue(false);
         this.host = tag.getTag("host").getValue("");
         this.port = tag.getTag("port").getIntValue(6667);
         this.serverPassword = tag.getTag("serverPassword").getValue("");
         this.channel = tag.getTag("channel").getValue("");
         this.channelPassword = tag.getTag("channelPassword").getValue("");
         if(this.serverPassword.length() == 0) this.serverPassword = null;
         if(this.channelPassword.length() == 0) this.channelPassword = null;
         this.setName("Nailed");
         this.setLogin("Nailed");
         this.setVersion("Nailed");
         this.setVerbose(false);
 
         MinecraftForge.EVENT_BUS.register(this);
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onChat(PlayerChatEvent event){
         this.sendMessage(this.channel, "<" + event.player.getUsername() + "> " + event.message);
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onPlayerJoin(PlayerJoinEvent event){
         this.sendMessage(this.channel, "* " + event.player.getUsername() + " has joined the game");
     }
 
     @SubscribeEvent
     @SuppressWarnings("unused")
     public void onPlayerJoin(PlayerLeaveEvent event){
         this.sendMessage(this.channel, "* " + event.player.getUsername() + " has left the game");
     }
 
     public void connect(){
         if(this.enabled){
             new ConnectThread().start();
         }
     }
 
     @Override
     protected void onConnect() {
         NailedLog.info("Connected to irc!");
     }
 
     @Override
     protected void onDisconnect() {
         NailedLog.info("Disconnected from irc!");
     }
 
     @Override
     protected void onMessage(String channel, String sender, String login, String hostname, String message) {
         ServerConfigurationManager configManager = MinecraftServer.getServer().getConfigurationManager();
         if(message.equals("!list") || message.equals("!players")){
             this.sendMessage(channel, configManager.getCurrentPlayerCount() + " online players: " + configManager.getPlayerListAsString());
         }else{
             ChatComponentText component = new ChatComponentText("");
             IChatComponent comp = new ChatComponentText("[" + channel + "]");
             comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
             component.appendSibling(comp);
             component.appendText(" <");
             comp = new ChatComponentText(sender);
             comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(login + "@" + hostname)));
             component.appendSibling(comp);
             message = "> " + message;
             this.append(component, message);
             configManager.sendChatMsg(component);
         }
     }
 
     @Override
     protected void onPrivateMessage(String sender, String login, String hostname, String message) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         comp = new ChatComponentText(sender);
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(login + "@" + hostname)));
         component.appendSibling(comp);
         comp = new ChatComponentText("] ");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         this.append(component, message);
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onAction(String sender, String login, String hostname, String target, String action) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + target + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * ");
         comp = new ChatComponentText(sender);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(login + "@" + hostname)));
         component.appendSibling(comp);
         this.append(component, " " + action);
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onJoin(String channel, String sender, String login, String hostname) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + channel + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * ");
         comp = new ChatComponentText(sender);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(login + "@" + hostname)));
         component.appendSibling(comp);
         component.appendText(" joined the channel");
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onPart(String channel, String sender, String login, String hostname) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + channel + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * ");
         comp = new ChatComponentText(sender);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(login + "@" + hostname)));
         component.appendSibling(comp);
         component.appendText(" left the channel");
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + channel + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * ");
         comp = new ChatComponentText(oldNick);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(login + "@" + hostname)));
         component.appendSibling(comp);
         component.appendText(" is now known as " + newNick);
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + channel + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * " + recipientNick + " was kicked by ");
         comp = new ChatComponentText(kickerNick);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(kickerLogin + "@" + kickerHostname)));
         component.appendSibling(comp);
         component.appendText(" (" + reason + ")");
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + channel + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * ");
         comp = new ChatComponentText(sourceNick);
         comp.getChatStyle().setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText(sourceLogin + "@" + sourceHostname)));
         component.appendSibling(comp);
         component.appendText(" left irc (" + reason + ")");
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     @Override
     protected void onTopic(String channel, String topic, String setBy, long date, boolean changed) {
         if(!changed) return;
         ChatComponentText component = new ChatComponentText("");
         IChatComponent comp = new ChatComponentText("[" + channel + "]");
         comp.getChatStyle().setColor(EnumChatFormatting.GRAY);
         component.appendSibling(comp);
         component.appendText(" * " + setBy + " set the topic: " + topic);
         MinecraftServer.getServer().getConfigurationManager().sendChatMsg(component);
     }
 
     private class ConnectThread extends Thread {
         @Override
         public void run() {
             try{
                 NailedLog.info("Connecting to irc...");
                 connect(host, port, serverPassword);
                 joinChannel(channel, channelPassword);
             }catch(Exception e){
                 NailedLog.warn("An error was thrown while connecting to irc");
             }
         }
     }
 
     private void append(IChatComponent component, String message){
         IChatComponent comp;
         String msg = "";
         EnumChatFormatting color = null;
         boolean bold = false, underline = false, italic = false;
         for(int i = 0; i < message.length(); i++){
             char current = message.charAt(i);
             if(current == '\u0003'){ //Every IRC colorcode starts with this magic char
                 if(msg.length() > 0){ //Write everything we read
                     comp = new ChatComponentText(msg);
                     if(color != EnumChatFormatting.WHITE && color != EnumChatFormatting.RESET) comp.getChatStyle().setColor(color);
                     if(bold) comp.getChatStyle().setBold(true);
                     if(italic) comp.getChatStyle().setItalic(true);
                     if(underline) comp.getChatStyle().setUnderlined(true);
                     component.appendSibling(comp);
                 }
                 //if(msg.length() < i + 1) break;
                 String code = current + "" + message.charAt(i + 1) + "" + message.charAt(i + 2);
                 msg = "";
                 color = colors.get(code);
                 i += 2;
             }else if(current == '\u000F'){ //Reset
                 if(msg.length() > 0){ //Write everything we read
                     comp = new ChatComponentText(msg);
                     if(color != EnumChatFormatting.WHITE && color != EnumChatFormatting.RESET) comp.getChatStyle().setColor(color);
                     if(bold) comp.getChatStyle().setBold(true);
                     if(italic) comp.getChatStyle().setItalic(true);
                     if(underline) comp.getChatStyle().setUnderlined(true);
                     component.appendSibling(comp);
                 }
                 msg = "";
                 color = null;
                 underline = italic = bold = false;
             }else if(current == '\u0002'){ //Bold
                 if(msg.length() > 0){ //Write everything we read
                     comp = new ChatComponentText(msg);
                     if(color != EnumChatFormatting.WHITE && color != EnumChatFormatting.RESET) comp.getChatStyle().setColor(color);
                     if(bold) comp.getChatStyle().setBold(true);
                     if(italic) comp.getChatStyle().setItalic(true);
                     if(underline) comp.getChatStyle().setUnderlined(true);
                     component.appendSibling(comp);
                 }
                 msg = "";
                 color = null;
                 bold = true;
             }else if(current == '\u001F'){ //Underline
                 if(msg.length() > 0){ //Write everything we read
                     comp = new ChatComponentText(msg);
                     if(color != EnumChatFormatting.WHITE && color != EnumChatFormatting.RESET) comp.getChatStyle().setColor(color);
                     if(bold) comp.getChatStyle().setBold(true);
                     if(italic) comp.getChatStyle().setItalic(true);
                     if(underline) comp.getChatStyle().setUnderlined(true);
                     component.appendSibling(comp);
                 }
                 msg = "";
                 color = null;
                 underline = true;
             }else if(current == '\u0016'){ //Reverse / italic (We use italic)
                 if(msg.length() > 0){ //Write everything we read
                     comp = new ChatComponentText(msg);
                     if(color != EnumChatFormatting.WHITE && color != EnumChatFormatting.RESET) comp.getChatStyle().setColor(color);
                     if(bold) comp.getChatStyle().setBold(true);
                     if(italic) comp.getChatStyle().setItalic(true);
                     if(underline) comp.getChatStyle().setUnderlined(true);
                     component.appendSibling(comp);
                 }
                 msg = "";
                 color = null;
                 italic = true;
             }else{
                 msg += current;
             }
         }
         if(msg.length() > 0){
             comp = new ChatComponentText(msg);
             if(color != EnumChatFormatting.WHITE && color != EnumChatFormatting.RESET) comp.getChatStyle().setColor(color);
             if(bold) comp.getChatStyle().setBold(true);
             if(italic) comp.getChatStyle().setItalic(true);
             if(underline) comp.getChatStyle().setUnderlined(true);
             component.appendSibling(comp);
         }
     }
 }
