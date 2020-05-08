 package de.deyovi.chat.core.objects.impl;
 
 import de.deyovi.chat.core.constants.ChatConstants.MessagePreset;
 import de.deyovi.chat.core.objects.ChatUser;
 import de.deyovi.chat.core.objects.Message;
 import de.deyovi.chat.core.objects.Room;
 import de.deyovi.chat.core.objects.Segment;
 import de.deyovi.chat.core.objects.Segment.ContentType;
 import de.deyovi.chat.core.services.impl.DefaultRoomService;
 import org.apache.log4j.Logger;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.*;
 import java.util.concurrent.atomic.AtomicLong;
 
 public class DefaultRoom implements Room {
 
 	private final static Logger logger = Logger.getLogger(DefaultRoom.class);
 	
 	
 	private final AtomicLong ids;
 	private final TreeSet<ChatUser> users = new TreeSet<ChatUser>();
 	private final Deque<Segment> media = new LinkedList<Segment>();
 	private final Set<String> invitations = new HashSet<String>();
 	private final Set<String> bans = new HashSet<String>();
 	private final List<Message> protocol = new LinkedList<Message>();
 	private final DefaultSegment protocolSegment;
 	private String bgImage = null;
 	private boolean open = false;
 	private boolean anonymous = false;
 	protected final String name;
 	private String bgColor = "FFFFFF";
 	private String fgColor = "000000";
 	private ChatUser owner;
 	private Message motd = null;
 	
 	public DefaultRoom(String name, boolean individual) {
 		this.name = name;
 		if (individual) {
 			String escapedName;
 			try {
 				escapedName = URLEncoder.encode(name, "UTF-8");
 			} catch (UnsupportedEncodingException e) {
 				escapedName = name;
 				logger.error(e);
 			}
 			protocolSegment = new DefaultSegment(name, "data?protocol=" + escapedName, ContentType.PROTOCOL, null, null, null);
 			protocolSegment.setAlternateName("$PROTOCOL{room=" + name + "}");
 		} else {
 			protocolSegment = null;
 		}
 		ids = new AtomicLong();
 	}
 	
 	public void setAnonymous(boolean anonymous) {
 		this.anonymous = anonymous;
 	}
 	
 	public boolean setOpen(boolean open) {
 		if (this.open != open) {
 			if (owner != null) {
 				if (open) {
 					broadcast(new SystemMessage(owner, 0l, MessagePreset.OPEN_CHANNEL, owner.getUserName()));
 				} else {
 					broadcast(new SystemMessage(owner, 0l, MessagePreset.CLOSE_CHANNEL, owner.getUserName()));
 				}
 			}
 			this.open = open;
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public Collection<ChatUser> getUsers() {
 		return users;
 	}
 	
 	@Override
 	public boolean isVisible() {
 		return open;
 	}
 
 	@Override
 	public boolean isMember(ChatUser user) {
 		if (user == null) {
 			return false; 
 		} else {
 			return users.contains(user);
 		}
 	}
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String getColor() {
 		return bgColor;
 	}
 	
 	public void setBgImage(String filename) {
 		this.bgImage = filename;
 	}
 
 	@Override
 	public void setColor(String color) {
 		this.bgColor = color;
 	}
 	
 	@Override
 	public void setFontColor(String fontColor) {
 		this.fgColor = fontColor;
 	}
 	
 	public void clearLog(String username) {
 		if (this.protocol != null) {
 			this.protocol.clear();
 		}
 	}
 	
 	public void clearMedia(String username) {
 		if (this.media != null) {
 			this.media.clear();
 		}
 	}
 
 	@Override
 	public void leave(ChatUser user) {
 		users.remove(user);
 		if (owner != null) {
 			// if the owner left
 			if (owner.equals(user)) {
 				if (!users.isEmpty()) {
 					// .. pass the ownership on
 					owner = users.first();
 				} else {
 					// or leave it empty and remove the room!
 					owner = null;
 					DefaultRoomService.getInstance().remove(this);
 				}
 			}
 		}
 		user.setCurrentRoom(null);
 		broadcast(new SystemMessage(user, nextId(), MessagePreset.LEFT_CHANNEL, user.getUserName()));
 	}
 
 	@Override
 	public void join(ChatUser user) {
 		Room oldRoom = user.getCurrentRoom();
 		broadcast(new SystemMessage(null, nextId(), MessagePreset.JOIN_CHANNEL, user.getUserName()));
 		// if this isn't the users previous room
 		if (oldRoom != this) {
 			// leave it
 			if (oldRoom != null) {
 				oldRoom.leave(user);
 			}
 			// and set this as his room
 			user.setCurrentRoom(this);
 			users.add(user);
 			user.push(new SystemMessage(null, nextId(), MessagePreset.SWITCH_CHANNEL, getName(), getColor()));
 			if (motd != null) {
 				user.push(motd);
 			}
 		}
 	}
 
 	@Override
 	public boolean isAnonymous() {
 		return anonymous;
 	}
 
 	public RoomInfo getInfoForUser(ChatUser user) {
 		String name;
 		String bgColor;
 		String fgColor;
 		String bgImage;
 		ChatUser[] usersArray;
 		Segment[] mediaArray;
 		boolean member = isMember(user);
 		if (isVisible() || member) {
 			if (member) {
 				bgImage = this.bgImage;
 			} else {
 				bgImage = null;
 			}
 			name = this.name;
 			bgColor = this.bgColor;
 			fgColor = this.fgColor;
 			if (!isAnonymous() || member) {
 				usersArray = new ChatUser[users.size()];
 				int i = 0;
 				for (ChatUser tmp : getUsers()) {
 					usersArray[i++] = tmp;
 				}
 			} else {
 				usersArray = new ChatUser[0];
 			}
 			if (member && protocolSegment != null) {
 				mediaArray = new DefaultSegment[media.size() + 1];
 				int i = 0;
 				mediaArray[i++] = protocolSegment;
 				for (Segment e : media) {
 					mediaArray[i++] = e;
 				}
 			} else {
 				mediaArray = new Segment[0];
 			}
 		} else {
 			name = null;
 			bgColor = null;
 			fgColor = null;
 			bgImage = null;
 			usersArray = new ChatUser[0];
 			mediaArray = new Segment[0];
 		}
 		return new MyRoomInfo(name, bgColor, fgColor,bgImage, usersArray, mediaArray);
 	}
 	
 	@Override
 	public void talk(ChatUser user, Segment[] segments) {
 		// look for nice stuff (like media)
 		for (Segment segment : segments) {
 			if (segment instanceof DefaultSegment) {
 				// and remember it!
 				media.addFirst((DefaultSegment) segment);
 			}
 		}
 		// build a message consisting of the segments...
 		SystemMessage sm = new SystemMessage(user, nextId(), segments);
 		if (logger.isDebugEnabled()) {
 			logger.debug(name + "> " + (user != null ? user.getUserName() : "null") + ": " + sm.toString());
 		}
 		// ..and tell everyone about it :)
 		broadcast(sm);
 	}
 	
 	public void shout(Message msg) {
 		broadcast(new SystemMessage(nextId(), msg));
 	}
 	
 	@Override
 	public void setOwner(ChatUser owner) {
 		this.owner = owner;
 	}
 	
 	@Override
 	public ChatUser getOwner() {
 		return owner;
 	}
 	
 	@Override
 	public boolean isInvited(ChatUser user) {
 		return invitations.contains(user.getUserName());
 	}
 	
 	@Override
 	public boolean invite(ChatUser user) {
 		user.setLastInvite(name);
 		return invitations.add(user.getUserName());
 	}
 	
 	@Override
 	public boolean revokeInvitation(ChatUser user) {
 		return invitations.remove(user.getUserName());
 	}
 	
 	@Override
 	public boolean ban(ChatUser user) {
 		return bans.add(user.getUserName());
 	}
 	
 	@Override 
 	public boolean revokeBan(ChatUser user) {
 		return bans.remove(user.getUserName());
 	}
 
 	@Override 
 	public void setMotd(ChatUser user, Segment[] segments) {
 		if (segments == null) {
 			this.motd = null;
 		} else {
 			segments = Arrays.copyOf(segments, segments.length + 1);
 			segments[segments.length - 1] =  new TextSegment(user != null ? user.getUserName() : "$system", "\n");
 			SystemMessage motdMessage = new SystemMessage(null, 0, MessagePreset.MOTD);
 			motdMessage.append(segments);
 			this.motd = motdMessage;
 		}
 	}
 	
 	@Override
 	public List<Message> getProtocol() {
 		if (protocolSegment != null) {
 			return protocol;
 		} else {
 			return null;
 		}
 	}
 	
 	private void broadcast(Message msg) {
 		for (ChatUser listener : getUsers()) {
 			listener.push(msg);
 		}
 		if (protocolSegment != null) {
 			protocol.add(msg);
 		}
 	}
 
 	private long nextId() {
 		return ids.getAndIncrement();
 	}
 	
 	private class MyRoomInfo implements RoomInfo {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 7428038028924932419L;
 		private final String name;
 		private final String bgColor;
 		private final String fgColor;
 		private final String bgImage;
 		private final ChatUser[] users;
 		private final Segment[] media;
 		
 		private MyRoomInfo(String name, String bgColor, String fgColor, String bgImage, ChatUser[] users, Segment[] media) {
 			this.name = name;
 			this.bgColor = bgColor;
 			this.fgColor = fgColor;
 			this.users = users;
 			this.media = media;
			this.bgImage = bgImage != null ? "data/" + bgImage : null;
 		}
 		
 
 		@Override
 		public String getName() {
 			return name;
 		}
 
 		@Override
 		public String getBgColor() {
 			return bgColor;
 		}
 
 		@Override
 		public String getFgColor() {
 			return fgColor;
 		}
 		
 		@Override
 		public String getBgImage() {
 			return bgImage;
 		}
 		
 		@Override
 		public ChatUser[] getUsers() {
 			return users;
 		}
 		
 		@Override
 		public Segment[] getMedia() {
 			return media;
 		}
 		
 	}
 
 }
