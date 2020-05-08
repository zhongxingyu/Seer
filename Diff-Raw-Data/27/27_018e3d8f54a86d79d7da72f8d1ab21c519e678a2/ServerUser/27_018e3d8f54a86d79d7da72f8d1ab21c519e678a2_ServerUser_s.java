 package me.draconia.chat.server;
 
 import iaik.sha3.IAIKSHA3Provider;
 import me.draconia.chat.net.packets.Packet;
 import me.draconia.chat.net.packets.PacketMessageToClient;
 import me.draconia.chat.types.GenericContext;
 import me.draconia.chat.types.Message;
 import me.draconia.chat.types.TextMessage;
 import me.draconia.chat.types.User;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 
 import java.io.*;
 import java.security.MessageDigest;
 import java.util.Arrays;
 import java.util.HashSet;
 
 public class ServerUser extends User implements Serializable {
     public static final long serialVersionUID = -1L;
 
     private byte[] password;
     private transient Channel channel;
 
    protected transient final HashSet<ServerChannel> channels = new HashSet<ServerChannel>();
 
     protected ServerUser(String login) {
         super(login);
     }
 
     protected void setChannel(final Channel channel) {
         this.channel = channel;
         channel.getCloseFuture().addListener(new ChannelFutureListener() {
             @Override
             public void operationComplete(ChannelFuture channelFuture) throws Exception {
                 disconnected(channel);
             }
         });
     }
 
     protected void disconnected(Channel channel) {
         if(channel != this.channel) return;
        final ServerChannel[] sChannels;
         synchronized (channels) {
            sChannels = channels.toArray(new ServerChannel[channels.size()]);
             for(ServerChannel serverChannel : sChannels) {
                 serverChannel.leaveUser(this);
             }
             channels.clear();
         }
         this.channel = null;
     }
 
     protected Channel getChannel() {
         return channel;
     }
 
     private byte[] hashPassword(String password) {
         try {
             MessageDigest messageDigest = MessageDigest.getInstance("KECCAK256", IAIKSHA3Provider.getInstance());
             messageDigest.update(login.getBytes("UTF-8"));
             messageDigest.update(password.getBytes("UTF-8"));
             return messageDigest.digest();
         } catch(Exception e) {
             e.printStackTrace();
             throw new RuntimeException("UPS");
         }
     }
 
     public void setPassword(String password) {
         this.password = hashPassword(password);
     }
 
     public boolean checkPassword(String password) {
         if(this.password == null || password == null)
             return false;
         return Arrays.equals(this.password, hashPassword(password));
     }
 
     public boolean hasPassword() {
         return (this.password != null);
     }
 
     public boolean sendPacket(Packet packet) {
         if(channel == null) return false;
         channel.write(packet.getData());
         return true;
     }
 
     public boolean sendSystemMessage(String text) {
         return sendSystemMessage(text, TextMessage.TYPE_SYSTEM);
     }
 
     public boolean sendSystemError(String text) {
         return sendSystemMessage(text, TextMessage.TYPE_SYSTEM_ERROR);
     }
 
     public boolean sendSystemMessage(String text, byte type) {
         TextMessage message = new TextMessage();
         message.content = text;
         message.type = type;
         message.context = GenericContext.instance;
         message.from = User.getSYSTEM();
         PacketMessageToClient packetMessageToClient = new PacketMessageToClient();
         packetMessageToClient.message = message;
         return sendPacket(packetMessageToClient);
     }
 }
