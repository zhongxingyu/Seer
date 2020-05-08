 package ca.wasabistudio.chat.rs;
 
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 
 import org.springframework.transaction.annotation.Transactional;
 
 import ca.wasabistudio.chat.dto.ClientDTO;
 import ca.wasabistudio.chat.dto.MessageDTO;
 import ca.wasabistudio.chat.dto.RoomDTO;
 import ca.wasabistudio.chat.entity.Client;
 import ca.wasabistudio.chat.entity.Message;
 import ca.wasabistudio.chat.entity.Room;
 import ca.wasabistudio.chat.entity.RoomSetting;
 import ca.wasabistudio.chat.support.NotFoundException;
 import ca.wasabistudio.chat.support.RequestErrorException;
 import ca.wasabistudio.chat.support.Session;
 import ca.wasabistudio.chat.support.SessionExpiredException;
 
 @Path("/room")
 public class RoomResource {
 
     private EntityManager em;
     private Session session;
 
     @PersistenceContext
     public void setEntityManager(EntityManager em) {
         this.em = em;
     }
 
     public void setSession(Session session) {
         this.session = session;
     }
 
     @POST
     @Path("/join/{room}")
     @Produces("application/json")
     @Transactional
     public void joinRoom(@PathParam("room") String roomKey) {
         Client client = getClient();
         Room room = getRoom(roomKey);
         if (room == null) {
             room = new Room(roomKey);
             em.persist(room);
         }
         room.addClient(client);
     }
 
     @GET
     @Path("/list")
     @Produces("application/json")
     @Transactional
     @SuppressWarnings("unchecked")
     public List<RoomDTO> getRooms() {
         List<Room> rooms = em.createQuery("select r from Room r")
             .getResultList();
         return RoomDTO.toDTOs(rooms);
     }
 
     @GET
     @Path("/info/{room}/clients")
     @Produces("application/json")
     @Transactional
     public Collection<ClientDTO> getClients(@PathParam("room") String roomKey) {
         Room room = getRoom(roomKey);
         if (room == null) {
             String message = "Room cannot be found.";
             throw new NotFoundException(message);
         }
 
         List<Client> clients = room.getClients();
         return ClientDTO.toDTOs(clients);
     }
 
     @GET
     @Path("/info/{room}/messages")
     @Produces("application/json")
     @Transactional
     @SuppressWarnings("unchecked")
     public MessageDTO[] getMessages(@PathParam("room") String roomKey) {
         Client client = getClient();
         Room room = getRoom(roomKey);
         if (room == null) {
             throw new RequestErrorException("Room cannot be found.");
         }
 
         // acquire messages
         List<Message> messages;
         RoomSetting setting = client.getRoomSetting(room);
         Message lastMessage = setting.getLastMessage();
         if (lastMessage == null) {
             messages = em.createQuery("select m from Message m " +
                    "where m.createTime >= :time")
                 .setParameter("time", setting.getEnterTime())
                 .getResultList();
         } else {
             messages = em.createQuery("select m from Message m " +
                    "where m.id > :id")
                 .setParameter("id", setting.getLastMessage().getId())
                 .getResultList();
         }
 
         // update last message sync'ed
         if (messages.size() > 0) {
             Message last = messages.get(messages.size() - 1);
             setting.setLastMessage(last);
         }
 
         // mark that the client has sync'ed
         client.sync();
 
         List<MessageDTO> result = MessageDTO.toDTOs(messages);
         return result.toArray(new MessageDTO[messages.size()]);
     }
 
     @POST
     @Path("/info/{room}/messages")
     @Produces("application/json")
     @Transactional
     public void addMessage(@PathParam("room") String roomKey,
             MessageDTO message) {
         if ("".equals(message.getBody())) {
             throw new RequestErrorException("Message body cannot be empty.");
         }
 
         Client client = getClient();
         Room room = getRoom(roomKey);
         if (room == null) {
             throw new RequestErrorException("Room cannot be found.");
         }
 
         Message result = new Message(client, room, message.getBody());
         client.addMessage(result);
         room.addMessage(result);
     }
 
     private Client getClient() {
         try {
             Client client = em.merge(session.getClient());
             if (client == null) {
                 throw new SessionExpiredException();
             }
             session.setClient(client);
             return client;
         } catch (IllegalArgumentException exception) {
             throw new SessionExpiredException(exception);
         }
     }
 
     private Room getRoom(String roomKey) {
         if ((roomKey == null) || "".equals(roomKey)) {
             String message = "Room cannot be empty.";
             throw new RequestErrorException(message);
         }
         return (Room)em.find(Room.class, roomKey);
     }
 
 }
