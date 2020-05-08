 package me.draconia.chat.server;
 
 import me.draconia.chat.net.packets.PacketChannelAction;
 import me.draconia.chat.net.packets.PacketChannelUserSnapshotResponse;
 import me.draconia.chat.types.Channel;
 import me.draconia.chat.types.User;
 
import java.io.IOException;
import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 
 public class ServerChannel extends Channel implements Serializable {
     public static final long serialVersionUID = -1L;
 
     protected String password;
 
    private transient HashSet<ServerUser> users = new HashSet<ServerUser>();
     private transient HashSet<ServerUser> usersView = new HashSet<ServerUser>();
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        users = new HashSet<ServerUser>();
        usersView = new HashSet<ServerUser>();
    }
 
     protected ServerChannel(String name) {
         super(name);
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public boolean checkPassword(String password) {
         return (this.password == null && (password == null || password.isEmpty())) || this.password.equals(password);
     }
 
     public Set<ServerUser> getUsers() {
         return usersView;
     }
 
     public void joinUser(ServerUser serverUser) {
         synchronized (users) {
             synchronized (serverUser.channels) {
                 users.add(serverUser);
                 serverUser.channels.add(this);
             }
             informChannelAction(serverUser, PacketChannelAction.ACTION_JOIN);
             usersView = (HashSet<ServerUser>)users.clone();
         }
 
         PacketChannelUserSnapshotResponse packetChannelUserSnapshotResponse = new PacketChannelUserSnapshotResponse();
         packetChannelUserSnapshotResponse.channel = this;
         packetChannelUserSnapshotResponse.users = usersView.toArray(new User[usersView.size()]);
         serverUser.sendPacket(packetChannelUserSnapshotResponse);
     }
 
     public void leaveUser(ServerUser serverUser) {
         synchronized (users) {
             informChannelAction(serverUser, PacketChannelAction.ACTION_LEAVE);
             synchronized (serverUser.channels) {
                 users.remove(serverUser);
                 serverUser.channels.remove(this);
             }
             usersView = (HashSet<ServerUser>)users.clone();
         }
     }
 
     private void informChannelAction(ServerUser serverUser, byte action) {
         PacketChannelAction packetChannelAction = new PacketChannelAction();
         packetChannelAction.user = serverUser;
         packetChannelAction.channel = this;
         packetChannelAction.action = action;
         for(ServerUser otherUser : users) {
             otherUser.sendPacket(packetChannelAction);
         }
     }
 }
