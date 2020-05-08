 package ca.wasabistudio.chat.rs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 
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
 
 /**
  * REST services for working with chat rooms.
  *
  * @author wasabi
  */
 @Path("/room")
 public class RoomResource {
 
 	private static final int LONG_POLL_TIMEOUT = 60000; // 60 seconds
 	private static final int POLL_QUERY_INTERVAL = 2000; // 2 seconds
 
 	private EntityManager em;
 	private Session session;
 
 	@PersistenceContext
 	public void setEntityManager(EntityManager em) {
 		this.em = em;
 	}
 
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	/**
 	 * Join a chatroom.
 	 *
 	 * @param roomKey key of the chatroom
 	 */
 	@POST
 	@Path("/join/{room}")
 	@Produces("application/json")
 	@Transactional
 	public void joinRoom(@PathParam("room") String roomKey,
 			@Context HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		Client client = getClient(session.getId());
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			room = new Room(roomKey);
 			em.persist(room);
 		}
 		room.addClient(client);
 	}
 
 	/**
 	 * Get all current chatrooms.
 	 *
 	 * @return a list of all current chatrooms.
 	 */
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
 
 	/**
 	 * Get all current clients of the chatroom specified.
 	 *
 	 * @param roomKey key of the chatroom to query
 	 * @return a collection of clients in the current chatroom
 	 */
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
 
 	/**
 	 * Get new messages from the chatroom since last sync.
 	 *
 	 * @param roomKey key of the chatroom to query
 	 * @return an array of all messages from the chatroom since last sync
 	 */
 	@GET
 	@Path("/info/{room}/messages")
 	@Produces("application/json")
 	public MessageDTO[] getMessages(@PathParam("room") String roomKey,
 			@Context HttpServletRequest request) {
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			throw new RequestErrorException("Room cannot be found.");
 		}
 
 		// mark that the client has sync'ed
 		String sessionId = request.getSession().getId();
 		syncClient(sessionId);
 
 		// start long poll
 		try {
 			int time = 0;
 			List<Message> messages = new ArrayList<Message>();
 			while (time < LONG_POLL_TIMEOUT) {
				messages = loadMessages(room, getClient(sessionId));
 				if (messages.size() > 0) {
 					break;
 				}
 
 				Thread.sleep(POLL_QUERY_INTERVAL);
 				time += POLL_QUERY_INTERVAL;
 			}
 
 			List<MessageDTO> result = MessageDTO.toDTOs(messages);
 			return result.toArray(new MessageDTO[messages.size()]);
 		} catch (InterruptedException exception) {
 			// return an empty array
 			return new MessageDTO[0];
 		}
 	}
 
 	/**
 	 * Do so here to commit the transaction right away.
 	 */
 	@Transactional
 	private void syncClient(String sessionId) {
 		getClient(sessionId).sync();
 	}
 
 	@SuppressWarnings("unchecked")
	private List<Message> loadMessages(Room room, Client client) {
 		// acquire messages
 		List<Message> messages;
		RoomSetting setting = client.getRoomSetting(room);
 		Message lastMessage = setting.getLastMessage();
 		if (lastMessage == null) {
 			messages = em.createQuery("select m from Message m " +
 					"where m.createTime >= :time " +
 						"and m.roomKey = :roomKey")
 				.setParameter("time", setting.getEnterTime())
 				.setParameter("roomKey", room.getKey())
 				.getResultList();
 		} else {
 			messages = em.createQuery("select m from Message m " +
 					"where m.id > :id " +
 						"and m.roomKey = :roomKey")
 				.setParameter("id", setting.getLastMessage().getId())
 				.setParameter("roomKey", room.getKey())
 				.getResultList();
 		}
 
 		// update last message sync'ed
 		if (messages.size() > 0) {
 			Message last = messages.get(messages.size() - 1);
 			setting.setLastMessage(last);
 		}
 
 		return messages;
 	}
 
 	/**
 	 * Post a new message in the chatroom.
 	 *
 	 * @param roomKey key of the chatroom to post to
 	 * @param message message to post
 	 */
 	@POST
 	@Path("/info/{room}/messages")
 	@Produces("application/json")
 	@Transactional
 	public void addMessage(@PathParam("room") String roomKey,
 			MessageDTO message, @Context HttpServletRequest request) {
 		if ("".equals(message.getBody())) {
 			throw new RequestErrorException("Message body cannot be empty.");
 		}
 
 		Client client = getClient(request.getSession().getId());
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			throw new RequestErrorException("Room cannot be found.");
 		}
 
 		Message result = new Message(client, room, message.getBody());
 		client.addMessage(result);
 		room.addMessage(result);
 	}
 
 	private Client getClient(String chatSessionId) {
 		try {
 			String username = session.getClient().getUsername();
 			Client client = em.find(Client.class, username);
 			if ((client == null)
 					|| !chatSessionId.equals(client.getChatSessionId())) {
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
