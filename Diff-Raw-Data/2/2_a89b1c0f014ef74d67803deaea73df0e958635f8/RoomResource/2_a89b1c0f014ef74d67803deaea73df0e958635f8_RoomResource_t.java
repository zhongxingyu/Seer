 package ca.wasabistudio.chat.rs;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
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
 import javax.ws.rs.core.Response;
 
 import org.jboss.resteasy.annotations.Suspend;
 import org.jboss.resteasy.spi.AsynchronousResponse;
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
 import ca.wasabistudio.chat.support.UpdateQueue;
 import ca.wasabistudio.chat.support.UpdateWatcher;
 import ca.wasabistudio.chat.support.WatcherRemovalTask;
 
 /**
  * REST services for working with chat rooms.
  *
  * @author wasabi
  */
 @Path("/room")
 public class RoomResource {
 
 	private static final long LONG_POLLING_TIMEOUT = 6000;
 	private static final long ASYNC_RESPONSE_TIMEOUT = 6500;
 
 	private final ScheduledExecutorService scheduler =
 			Executors.newScheduledThreadPool(1);
 
 	private EntityManager em;
 	private Session session;
 	private UpdateQueue messageUpdateQueue;
 
 	@PersistenceContext
 	public void setEntityManager(EntityManager em) {
 		this.em = em;
 	}
 
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	public void setMessageUpdateQueue(UpdateQueue queue) {
 		messageUpdateQueue = queue;
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
 	public void getMessagesAsync(@PathParam("room") String roomKey,
 			@Context HttpServletRequest request,
 			@Suspend(ASYNC_RESPONSE_TIMEOUT) AsynchronousResponse response) {
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			throw new RequestErrorException("Room cannot be found.");
 		}
 
 		// mark that the client has sync'ed
 		String sessionId = request.getSession().getId();
 		syncClient(sessionId);
 		returnMessages(room, sessionId, response);
 	}
 
 	private void returnMessages(final Room room, final String sessionId,
 			final AsynchronousResponse response) {
 		// it's very important here to add a scheduled task that removes the
 		// watcher after timeout, so watcher does not get constantly added to
 		// the queue and cause memory leak
 		WatcherRemovalTask task = new WatcherRemovalTask(messageUpdateQueue);
 		final ScheduledFuture<?> future = scheduler.schedule(task,
 				LONG_POLLING_TIMEOUT, TimeUnit.MILLISECONDS);
 		UpdateWatcher watcher = new UpdateWatcher() {
 
 			@Override
 			public void pushUpdate(Object data) {
 				List<Message> messages = loadMessages(room, sessionId);
 				List<MessageDTO> result = MessageDTO.toDTOs(messages);
 				response.setResponse(Response.ok(result).build());
 				future.cancel(true);
 			}
 
 		};
 
 		// when the watcher times out, send an empty response
 		task.setUpdateWatcher(watcher);
 		task.setRemovalEventHandler(new Callable<Boolean>() {
 
 			@Override
 			public Boolean call() throws Exception {
 				List<MessageDTO> result = new ArrayList<MessageDTO>();
 				response.setResponse(Response.ok(result).build());
 				return true;
 			}
 
 		});
 
 		messageUpdateQueue.addWathcer(watcher);
 	}
 
 	/**
 	 * Do so here to commit the transaction right away.
 	 */
 	@Transactional
 	private void syncClient(String sessionId) {
 		getClient(sessionId).sync();
 	}
 
 	@Transactional
 	@SuppressWarnings("unchecked")
 	private List<Message> loadMessages(Room room, String sessionId) {
 		// acquire messages
 		List<Message> messages;
 		RoomSetting setting = getClient(sessionId).getRoomSetting(room);
 		Message lastMessage = setting.getLastMessage();
 		if (lastMessage == null) {
 			messages = em.createQuery("select m from Message m " +
 					"where m.createTime >= :time " +
 						"and m.roomKey = :roomKey " +
 					"order by m.createTime")
 				.setParameter("time", setting.getEnterTime())
 				.setParameter("roomKey", room.getKey())
 				.getResultList();
 		} else {
 			messages = em.createQuery("select m from Message m " +
 					"where m.id > :id " +
						"and m.roomKey = :roomKey " +
 						"order by m.createTime")
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
 	public void addMessage(@PathParam("room") String roomKey,
 			MessageDTO message, @Context HttpServletRequest request) {
 		if ("".equals(message.getBody())) {
 			throw new RequestErrorException("Message body cannot be empty.");
 		}
 
 		String sessionId = request.getSession().getId();
 		storeNewMessage(roomKey, sessionId, message.getBody());
 
 		// now notify the queue
 		messageUpdateQueue.pushUpdate(null);
 	}
 
 	@Transactional
 	private void storeNewMessage(String roomKey, String sessionId,
 			String messageBody) {
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			throw new RequestErrorException("Room cannot be found.");
 		}
 		Client client = getClient(sessionId);
 		Message message = new Message(client, room, messageBody);
 		client.addMessage(message);
 		room.addMessage(message);
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
