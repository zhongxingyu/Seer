 package ca.wasabistudio.chat.rs;
 
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 import javax.ws.rs.FormParam;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.Response;
 
 import org.jboss.resteasy.annotations.Suspend;
 import org.jboss.resteasy.spi.AsynchronousResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import ca.wasabistudio.chat.entity.Client;
 import ca.wasabistudio.chat.entity.Message;
 import ca.wasabistudio.chat.entity.Message.Type;
 import ca.wasabistudio.chat.entity.Room;
 import ca.wasabistudio.chat.entity.RoomSetting;
 import ca.wasabistudio.chat.repo.ClientRepository;
 import ca.wasabistudio.chat.repo.MessageRepository;
 import ca.wasabistudio.chat.repo.RoomRepository;
 import ca.wasabistudio.chat.support.NotFoundException;
 import ca.wasabistudio.chat.support.RequestErrorException;
 import ca.wasabistudio.chat.support.Session;
 import ca.wasabistudio.chat.support.SessionExpiredException;
 import ca.wasabistudio.chat.support.UpdateQueue;
 import ca.wasabistudio.chat.support.UpdateWatcher;
 import ca.wasabistudio.chat.support.WatcherRemovalTask;
 import ca.wasabistudio.chat.text.MessageParser;
 
 /**
  * REST services for working with chat rooms.
  *
  * @author wasabi
  */
 @Path("/room")
 public class RoomResource {
 
 	private static final long LONG_POLLING_TIMEOUT = 6000;
 	private static final long ASYNC_RESPONSE_TIMEOUT = 6500;
 
 	@Autowired
 	private RoomRepository roomRepo;
 
 	@Autowired
 	private ClientRepository clientRepo;
 
 	@Autowired
 	private MessageRepository messageRepo;
 
 	@Autowired
 	private MessageParser messageParser;
 
 	private ScheduledExecutorService scheduler;
 
 	private Session session;
 
 	private Map<String, UpdateQueue> messageUpdateQueues;
 
 	public void setSession(Session session) {
 		this.session = session;
 	}
 
 	public void setMessageUpdateQueues(Map<String, UpdateQueue> queues) {
 		messageUpdateQueues = queues;
 	}
 
 	public void setWatcherRemovalService(ScheduledExecutorService scheduler) {
 		this.scheduler = scheduler;
 	}
 
 	/**
 	 * Join a chatroom.
 	 *
 	 * @param roomKey key of the chatroom
 	 */
 	@POST
 	@Path("/join/{room}")
 	@Produces("application/json")
 	public void joinRoom(@PathParam("room") String roomKey,
 			@Context HttpServletRequest request) {
 		HttpSession session = request.getSession();
 		Client client = getClient(session.getId());
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			room = createRoom(roomKey);
 		}
 		joinRoom(room, client);
 
 		// now notify other people
 		Message message = new Message(client, room, Type.Entrance);
 		saveMessage(room, client, message);
 		findOrCreateMessageUpdateQueue(roomKey).pushUpdate(null);
 	}
 
 	@Transactional
 	private Room createRoom(String key) {
 		Room room = new Room(key);
 		roomRepo.save(room);
 		return room;
 	}
 
 	@Transactional
 	private void joinRoom(Room room, Client client) {
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
 	@Transactional(readOnly=true)
 	public List<Room> getRooms() {
 		return roomRepo.findAll();
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
 	public Collection<Client> getClients(@PathParam("room") String roomKey) {
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			String message = "Room cannot be found.";
 			throw new NotFoundException(message);
 		}
 
 		return room.getClients();
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
 		Client client = getClient(sessionId);
 		syncClient(client);
 
 		// if there are results, load them directly
 		Message roomLastMessage = room.getLastMessage();
 		Message clientLastMessage = client.getRoomSetting(room).getLastMessage();
 		if ((roomLastMessage != null) && (clientLastMessage != null)
 				&& (clientLastMessage.getId() < roomLastMessage.getId())) {
 			// no checking for the result size because checking of the last
 			// message's id indicates the outcome already
 			List<Message> messages = loadMessages(room, sessionId);
 			processMessages(messages);
 			response.setResponse(Response.ok(messages).build());
 			return;
 		}
 
 		// return messages in async mode
 		returnMessages(room, sessionId, response);
 	}
 
 	private void returnMessages(final Room room, final String sessionId,
 			final AsynchronousResponse response) {
 		// it's very important here to add a scheduled task that removes the
 		// watcher after timeout, so watcher does not get constantly added to
 		// the queue and cause memory leak
 		UpdateQueue queue = findOrCreateMessageUpdateQueue(room.getKey());
 		WatcherRemovalTask task = new WatcherRemovalTask(queue);
 		final ScheduledFuture<?> future = scheduler.schedule(task,
 				LONG_POLLING_TIMEOUT, TimeUnit.MILLISECONDS);
 		UpdateWatcher watcher = new UpdateWatcher() {
 
 			private boolean finished;
 
 			@Override
 			public void pushUpdate(Object data) {
 				// return result only
 				finished = false;
 				List<Message> messages = loadMessages(room, sessionId);
 				if (messages.size() > 0) {
 					processMessages(messages);
 					response.setResponse(Response.ok(messages).build());
 					future.cancel(true);
 					finished = true;
 					return;
 				}
 			}
 
 			@Override
 			public void cancel(Object data) {
 				if (sessionId.equals(data)) {
 					future.cancel(true);
 					finished = true;
 				}
 			}
 
 			@Override
 			public boolean isFinished() {
 				return finished;
 			}
 
 		};
 
 		// when the watcher times out, send an empty response
 		task.setUpdateWatcher(watcher);
 		task.setRemovalEventHandler(new Callable<Boolean>() {
 
 			@Override
 			public Boolean call() throws Exception {
 				// return an empty list of messages
 				List<Message> result = new ArrayList<Message>(0);
 				response.setResponse(Response.ok(result).build());
 				return true;
 			}
 
 		});
 
 		queue.addWathcer(watcher);
 	}
 
 	@Transactional(propagation=Propagation.NEVER)
 	private void processMessages(List<Message> messages) {
 		for (Message message : messages) {
 			message.setBody(messageParser.process(message.getBody()));
 		}
 	}
 
 	private UpdateQueue findOrCreateMessageUpdateQueue(String roomKey) {
 		UpdateQueue queue = messageUpdateQueues.get(roomKey);
 		if (queue == null) {
 			queue = new UpdateQueue();
 			messageUpdateQueues.put(roomKey, queue);
 		}
 		return queue;
 	}
 
 	/**
 	 * Do so here to commit the transaction right away.
 	 */
 	@Transactional
 	private void syncClient(Client client) {
 		client.sync();
 		clientRepo.save(client);
 	}
 
 	@Transactional
 	private List<Message> loadMessages(Room room, String sessionId) {
 		// acquire messages
 		List<Message> messages;
 		Client client = getClient(sessionId);
 		RoomSetting setting = client.getRoomSetting(room);
 		Message lastMessage = setting.getLastMessage();
 		if (lastMessage == null) {
 			messages = messageRepo.findMessagesByTime(setting.getEnterTime(),
 					room.getKey(), client.getUsername());
 		} else {
 			int lastMessageId = setting.getLastMessage().getId();
 			messages = messageRepo.findMessagesByLastMessage(lastMessageId,
 					room.getKey(),
 					client.getUsername());
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
 	 * @return processed message
 	 * @throws UnsupportedEncodingException
 	 */
 	@POST
 	@Path("/info/{room}/messages")
 	@Produces("text/plain")
 	public String addMessage(@PathParam("room") String roomKey,
 				@FormParam("body") String messageBody,
				@Context HttpServletRequest request) {
 		if ((messageBody == null) || "".equals(messageBody)) {
 			throw new RequestErrorException("Message body cannot be empty.");
 		}
 
 		String sessionId = request.getSession().getId();
 		storeNewMessage(roomKey, sessionId, messageBody);
 
 		// now notify the queue
 		findOrCreateMessageUpdateQueue(roomKey).pushUpdate(null);
 		return messageParser.process(messageBody);
 	}
 
 	@POST
 	@Path("exit/{room}")
 	public void exit(@PathParam("room") String roomKey,
 			@Context HttpServletRequest request) {
 		String sessionId = request.getSession().getId();
 		Client client = getClient(sessionId);
 		Room room = roomRepo.findOne(roomKey);
 		exitRoom(room, client);
 
 		// remove the watcher
 		UpdateQueue queue = findOrCreateMessageUpdateQueue(roomKey);
 		queue.cancel(sessionId);
 
 		// now notify other people
 		Message message = new Message(client, room, Type.Exit);
 		saveMessage(room, client, message);
 		queue.pushUpdate(null);
 	}
 
 	@Transactional
 	private void exitRoom(Room room, Client client) {
 		client.exitRoom(room);
 		clientRepo.save(client);
 		roomRepo.save(room);
 	}
 
 	private void storeNewMessage(String roomKey, String sessionId,
 			String messageBody) {
 		Room room = getRoom(roomKey);
 		if (room == null) {
 			throw new RequestErrorException("Room cannot be found.");
 		}
 		Client client = getClient(sessionId);
 		Message message = new Message(client, room, messageBody);
 		saveMessage(room, client, message);
 	}
 
 	@Transactional
 	private void saveMessage(Room room, Client client, Message message) {
 		client.addMessage(message);
 		room.addMessage(message);
 		clientRepo.save(client);
 		roomRepo.save(room);
 		messageRepo.save(message);
 	}
 
 	private Client getClient(String chatSessionId) {
 		try {
 			String username = session.getClient().getUsername();
 			Client client = clientRepo.findOne(username);
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
 		return roomRepo.findOne(roomKey);
 	}
 
 }
