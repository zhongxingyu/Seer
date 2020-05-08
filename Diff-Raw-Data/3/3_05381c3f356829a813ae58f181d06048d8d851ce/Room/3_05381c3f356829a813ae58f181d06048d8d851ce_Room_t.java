 package ca.wasabistudio.chat.entity;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Access;
 import javax.persistence.AccessType;
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 @Entity
 @Table(name="rooms")
 public class Room implements Serializable {
 
     private static final long serialVersionUID = 3056095542238612660L;
 
     @Id
     @Column(name="room_key")
     @Access(AccessType.FIELD)
     private String key;
 
     @Column(name="title")
     private String title;
 
     @Column(name="motd")
     private String motd;
 
     @Column(name="create_time")
     @Temporal(TemporalType.TIMESTAMP)
     @Access(AccessType.FIELD)
     private Date createTime;
 
     @OneToMany(cascade = CascadeType.ALL, mappedBy="room")
     @Access(AccessType.FIELD)
     private List<Message> messages;
 
     @ManyToMany
     @JoinTable(name="rooms_clients",
         joinColumns=@JoinColumn(name="room_key"),
        inverseJoinColumns=@JoinColumn(name="username",
            referencedColumnName="username"))
     @Access(AccessType.FIELD)
     @OrderBy("username")
     private List<Client> clients;
 
     @JoinColumn(name="last_message")
     private Message lastMessage;
 
     Room() {
         this.key = "";
         this.title = "";
         this.motd = "";
         this.createTime = new Date();
         this.messages = new ArrayList<Message>();
         this.clients = new ArrayList<Client>();
     }
 
     public Room(String key) {
         this();
         this.key = key;
     }
 
     public String getKey() {
         return key;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getMotd() {
         return motd;
     }
 
     public void setMotd(String motd) {
         this.motd = motd;
     }
 
     public Date getCreateTime() {
         return (Date)createTime.clone();
     }
 
     public List<Message> getMessages() {
         return messages;
     }
 
     public void addMessage(Message message) {
         messages.add(message);
     }
 
     public void removeMessage(Message message) {
         messages.remove(message);
     }
 
     public List<Client> getClients() {
         return Collections.unmodifiableList(clients);
     }
 
     public void addClient(Client client) {
         RoomSetting setting = new RoomSetting(client, this);
         setting.setLastMessage(getLastMessage());
         client.addRoomSetting(setting);
         clients.add(client);
     }
 
     public void removeClient(Client client) {
         if (!clients.contains(client)) {
             return;
         }
         client.removeRoomSetting(this);
         clients.remove(client);
     }
 
     public Message getLastMessage() {
         return lastMessage;
     }
 
     public void setLastMessage(Message message) {
         lastMessage = message;
     }
 
     @Override
     public int hashCode() {
         return this.getKey().hashCode();
     }
 
     @Override
     public boolean equals(Object other) {
         if (other instanceof Room) {
             Room otherRoom = (Room)other;
             if ("".equals(getKey()) || "".equals(otherRoom.getKey())) {
                 return super.equals(other);
             }
             return getKey().equals(otherRoom.getKey());
         }
         return false;
     }
 
 }
