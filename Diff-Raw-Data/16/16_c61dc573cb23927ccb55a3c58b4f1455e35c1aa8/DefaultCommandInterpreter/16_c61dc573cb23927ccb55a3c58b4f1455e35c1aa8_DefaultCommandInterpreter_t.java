 package de.deyovi.chat.core.services.impl;
 
 import java.awt.Color;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.text.SimpleDateFormat;
 
 import de.deyovi.chat.core.services.CommandInterpreter;
 import org.apache.log4j.Logger;
 
 import de.deyovi.chat.core.constants.ChatConstants.ChatCommand;
 import de.deyovi.chat.core.constants.ChatConstants.MessagePreset;
 import de.deyovi.chat.core.objects.ChatUser;
 import de.deyovi.chat.core.objects.Message;
 import de.deyovi.chat.core.objects.Room;
 import de.deyovi.chat.core.objects.Segment;
 import de.deyovi.chat.core.objects.impl.SystemMessage;
 import de.deyovi.chat.core.objects.impl.TextSegment;
 import de.deyovi.chat.core.services.ChatUserService;
 import de.deyovi.chat.core.services.InputProcessorService;
 import de.deyovi.chat.core.services.RoomService;
 import de.deyovi.chat.core.utils.ChatUtils;
 
 public class DefaultCommandInterpreter implements CommandInterpreter {
 
 	private static final Logger logger = Logger.getLogger(DefaultCommandInterpreter.class);
 
 	private final RoomService roomService = DefaultRoomService.getInstance();
 	private final ChatUserService chatUserService = DefaultChatUserService.getInstance();
     private final InputProcessorService inputProcessorService;
 
     public DefaultCommandInterpreter(InputProcessorService inputProcessorService) {
         this.inputProcessorService = inputProcessorService;
     }
 
 	@Override
 	public boolean process(ChatUser user, ChatCommand cmd, String payload, InputStream uploadStream, String uploadName) {
 		switch (cmd) {
 		case ALIAS:
 			String alias = payload != null ? payload.trim() : "";
 			alias(user, alias);
 			return true;
 		case WHO:
 			String whoRoomString = payload != null ? payload.trim() : "";
 			who(user, whoRoomString);
 			return true;
 		case FOREGROUND:
 			String fontColor = payload != null ? payload.trim() : "000000";
 			foreground(user, fontColor);
 			return true;
 		case BACKGROUND:
 			background(user, payload, uploadStream, uploadName);
 			return true;
 		case AWAY:
 			away(user, true);
 			return true;
 		case LOGOUT:
 			chatUserService.logout(user);
 			return true;
 		case CLOSEROOM:
 			closeRoom(user, payload);
 			return true;
 		case OPENROOM:
 			openRoom(user, payload);
 			return true;
 		case INVITE:
 			if (payload == null || (payload = payload.trim()).isEmpty()) {
 				logger.warn("INVITE with no user supplied!");
 			} else {
 				invite(user, payload);
 			}
 			return true;
 		case NEWROOM:
 			if (payload == null || (payload = payload.trim()).isEmpty()) {
 				logger.warn("NEWROOM with no room supplied!");
 			} else {
 				String color;
 				String room;
 				int whitespace = payload.indexOf(' ');
 				if (whitespace != -1) {
 					room = payload.substring(0, whitespace).trim();
 					color = payload.substring(whitespace).trim();
 				} else {
 					color = null;
 					room = payload;
 				}
 				newRoom(user, room, color);
 			}
 			return true;
 		case SEARCH:
 			if (payload == null || (payload = payload.trim()).isEmpty()) {
 				logger.warn("SEARCH with no user supplied!");
 			} else {
 				search(user, payload);
 			}
 			return true;
 		case IGNORE:
 			// TODO
 			return true;
 		case JOIN:
 			join(user, payload != null ? payload.trim() : null);
 			return true;
 		case WHISPER:
 			int whitespace;
 			if (payload == null) {
 				whitespace = -1;
 			} else {
 				payload = payload.trim();
 				whitespace = payload.indexOf(' ');
 			}
 			if (whitespace == -1) {
 				logger.warn("WHISPER with no message supplied!");
 			} else {
 				String targetUsername = payload.substring(0, whitespace).trim();
 				String message = payload.substring(whitespace).trim();
 				whisper(user, targetUsername, message, uploadStream, uploadName);
 			}
 			return true;
 		case PROFILE:
 			String profileUser = payload != null ? payload.trim() : "";
 			profile(user, profileUser);
 			return true;
 		case CLEAR:
 			clear(user, payload);
 			return true;
 		case MOTD:
 			String motd = payload != null ? payload.trim() : "";
 			motd(user, uploadStream, uploadName, motd);
 			return true;
 		default:
 			return false;
 		}
 	}
 
 	@Override
 	public void join(ChatUser user, String roomName) {
 		if (user != null) {
 			Room room;
 			if (roomName == null) {
 				room = roomService.getByName(user.getLastInvite());
 			} else {
 				room = roomService.getByName(roomName);
 			}
 			if (room != null) {
 				logger.debug(user + " tries to join room " + room.getName());
 				if (room.isVisible() || room.isInvited(user) || room.getOwner().equals(user)) {
 					room.join(user);
 					user.alive();
 					chatUserService.broadcast(new SystemMessage(null, 0l, MessagePreset.REFRESH));
 				} else {
 					user.push(new SystemMessage(null, 0l, MessagePreset.CHANNEL_NOTALLOWED, room.getName()));
 				}
 			} else {
 				user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_CHANNEL, roomName));
 			}
 		}
 	}
 	
 	@Override
 	public void away(ChatUser user, boolean away) {
 		if (chatUserService.isActive(user) && !user.isGuest()) {
 			user.setAway(away);
 			user.alive();
 			Room room = user.getCurrentRoom();
 			if (room != null) {
 				Message msg;
 				if (user.isAway()) {
 					msg  = new SystemMessage(user, 0, MessagePreset.USER_AWAY, user.getUserName());
 				} else {
 					msg  = new SystemMessage(user, 0, MessagePreset.USER_BACK, user.getUserName());
 				}
 				room.shout(msg);
 				chatUserService.broadcast(new SystemMessage(null, 0l, MessagePreset.REFRESH));
 			}
 		}
 	}
 
 	/**
 	 * A user whispers to a target 
 	 * @param user
 	 * @param target
 	 * @param message
 	 * @param uploadStream
 	 * @param uploadName
 	 */
 	private void whisper(ChatUser user, String target, String message, InputStream uploadStream, String uploadName) {
 		if (chatUserService.isActive(user)) {
 			ChatUser targetUser = chatUserService.getByName(target);
 			user.alive();
 			if (targetUser == null) {
 				logger.info("WHISPER with unknown user '" + target + "' supplied!");
 				user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_USER, target));
 			} else if (!chatUserService.isActive(targetUser)) {
 				logger.info("WHISPER with unactive user '" + target + "' supplied!");
 				user.push(new SystemMessage(null, 0l, MessagePreset.USER_NOT_LOGGED_IN, target));
 			} else {
 				// Perpare the SystemMessage
 				SystemMessage msg4Target = new SystemMessage(user, 0l, MessagePreset.WHISPER, user.getUserName());
 				SystemMessage msg4Source = new SystemMessage(null, 0l, MessagePreset.WHISPERTO, targetUser.getUserName());
 				// parse the actual content
 				Segment[] msgSegments = inputProcessorService.process(user, message, uploadStream, uploadName);
 				// and append the Segments to the Message
 				msg4Target.append(msgSegments);
 				msg4Source.append(msgSegments);
 				targetUser.push(msg4Target);
 				user.push(msg4Source);
 				if (logger.isDebugEnabled()) {
 					logger.debug("Message[" + message + "] from user[" + user + "] to user[" + targetUser + "]");
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Searches for a user
 	 * @param user
 	 * @param wanted
 	 */
 	private void search(ChatUser user, String wanted) {
 		if (chatUserService.isActive(user)) {
 			ChatUser wantedUser = chatUserService.getByName(wanted);
 			if (wantedUser == null) {
 				logger.info("SEARCH with unknown user '" + wanted + "' supplied!");
 				user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_USER, wanted));
 			} else if (!chatUserService.isActive(wantedUser)) {
                 SimpleDateFormat sdf_date = new SimpleDateFormat("dd.MM.yyyy");
 				String date = sdf_date.format(wantedUser.getLastLogin());
                 SimpleDateFormat sdf_time =  new SimpleDateFormat("HH:mm");
 				String time = sdf_time.format(wantedUser.getLastLogin());
 				user.push(new SystemMessage(null, 0l, MessagePreset.SEARCH_LAST, wantedUser.getUserName(), date, time));
 			} else {
 				Room room = wantedUser.getCurrentRoom();
 				if (room == null) {
 					user.push(new SystemMessage(null, 0l, MessagePreset.SEARCH_NOWHERE, wantedUser.getUserName()));
 				} else if (!room.isVisible()) {
 					user.push(new SystemMessage(null, 0l, MessagePreset.SEARCH_PRIVATE, wantedUser.getUserName()));
 				} else {
 					user.push(new SystemMessage(null, 0l, MessagePreset.SEARCH_CHANNEL, wantedUser.getUserName(), room.getName()));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a new room with the given color
 	 * @param user
 	 * @param roomName
 	 * @param color
 	 */
 	private void newRoom(ChatUser user, String roomName, String color) {
 		if (chatUserService.isActive(user)) {
 			logger.debug(user + " wants to create " + roomName + " with color " + color);
 			if (user.isGuest()) {
 				user.push(new SystemMessage(null, 0l, MessagePreset.CREATE_NOGUEST));
 			} else {
 				Room room = roomService.spawn(roomName);
 				if (room == null) {
 					logger.debug(user + " wanted to create " + roomName + " which name is already in use");
 					room = roomService.getByName(roomName);
 					user.push(new SystemMessage(null, 0l, MessagePreset.CREATE_NAMEGIVEN, room.getName()));
 				} else {
 					logger.info(user + " created " + roomName + " with color " + color);
 					if (color != null) {
 						room.setColor(color);
 					}
 					room.setOwner(user);
 					join(user, roomName);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Invites another user to join the inveters current room
 	 * @param user
 	 * @param invitee
 	 */
 	private void invite(ChatUser user, String invitee) {
 		if (chatUserService.isActive(user)) {
 			logger.debug(user + " invites " + invitee);
 			if (user.isGuest()) {
 				user.push(new SystemMessage(null, 0l, MessagePreset.INVITE_NOGUEST));
 			} else {
 				ChatUser inviteeUser = chatUserService.getByName(invitee);
 				if (inviteeUser == null) {
 					user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_USER, invitee));
 				} else if (!chatUserService.isActive(inviteeUser)) {
 					user.push(new SystemMessage(null, 0l, MessagePreset.USER_NOT_LOGGED_IN, inviteeUser.getUserName()));
 				} else {
 					Room room = user.getCurrentRoom();
 					if (!room.invite(inviteeUser)) {
 						user.push(new SystemMessage(null, 0l, MessagePreset.INVITETO_USER_ALREADY, inviteeUser.getUserName()));
 					} else {
 						inviteeUser.push(new SystemMessage(user, 0l, MessagePreset.INVITE_USER, user.getUserName(), room.getName()));
 						user.push(new SystemMessage(null, 0l, MessagePreset.INVITETO_USER, inviteeUser.getUserName()));
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Closes a room
 	 * @param user
 	 * @param roomName
 	 */
 	private void closeRoom(ChatUser user, String roomName) {
 		if (chatUserService.isActive(user)) {
 			logger.debug(user + " closes room " + roomName);
 			Room room = roomName == null ? user.getCurrentRoom() : roomService.getByName(roomName);
 			if (room == null) {
 				user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_CHANNEL, roomName));
 			} else if (room.getOwner().equals(user)) {
 				if (!room.setOpen(false)) {
 					user.push(new SystemMessage(null, 0l, MessagePreset.CLOSE_CHANNEL_ALREADY));
 				} else {
 					chatUserService.broadcast(new SystemMessage(null, 0l, MessagePreset.REFRESH));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Opens a room
 	 * @param user
 	 * @param roomName
 	 */
 	private void openRoom(ChatUser user, String roomName) {
 		if (chatUserService.isActive(user)) {
 			logger.debug(user + " opens room " + roomName);
 			Room room = roomName == null ? user.getCurrentRoom() : roomService.getByName(roomName);
 			if (room == null) {
 				logger.debug("no room found");
 				user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_CHANNEL, roomName));
 			} else {
 				logger.debug("checking owner");
 				if (user.equals(room.getOwner())) {
 					if (!room.setOpen(true)) {
 						user.push(new SystemMessage(null, 0l, MessagePreset.OPEN_CHANNEL_ALREADY));
 					} else {
 						chatUserService.broadcast(new SystemMessage(null, 0l, MessagePreset.REFRESH));
 					}
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Sets the Message of the Day for a room
 	 * @param user
 	 * @param uploadStream
 	 * @param uploadName
 	 * @param motd
 	 */
 	private void motd(ChatUser user, InputStream uploadStream, String uploadName, String motd) {
 		Room userRoom;
 		userRoom = user.getCurrentRoom();
 		if (user.getSettings().isTrusted() || user.equals(userRoom.getOwner())) {
 			if (logger.isDebugEnabled()) {
 				logger.debug(user + " sets motd for Room '" + userRoom + "' to '" + motd + "'");
 			}
 			if (!motd.isEmpty()) {
                 Segment[] segments = inputProcessorService.process(user, motd, uploadStream, uploadName);
                 userRoom.setMotd(user, segments);
 			} else {
 				userRoom.setMotd(user, null);
 			}
 			userRoom.shout(new SystemMessage(null, 0l, MessagePreset.MOTD_SET, user.getUserName()));
 		} else {
 			logger.warn("MotD from unprivileged user '" + user + "'");
 			user.push(new SystemMessage(null, 0l, MessagePreset.MOTD_NOTALLOWED));
 		}
 	}
 
 	/**
 	 * Clears the protocols of a room
 	 * @param user
 	 * @param payload
 	 */
 	private void clear(ChatUser user, String payload) {
 		Room userRoom;
 		userRoom = user.getCurrentRoom();
 		if (user.getSettings().isTrusted() || user.equals(userRoom.getOwner())) {
 			String target = payload != null ? payload.trim() : "";
 			if (target.equals("all") || target.equals("log")) {
 				userRoom.clearLog(user.getUserName());
 				logger.debug(user + " clears Log for Room '" + userRoom + "'");
 				userRoom.shout(new SystemMessage(null, 0l, MessagePreset.CLEAR_LOG, user.getUserName()));
 			}
 			if (target.equals("all") || target.equals("media")) {
 				userRoom.clearMedia(user.getUserName());
 				userRoom.shout(new SystemMessage(null, 0l, MessagePreset.CLEAR_MEDIA, user.getUserName()));
 				logger.debug(user + " clears Media for Room '" + userRoom + "'");
 			}
 		} else {
 			logger.warn("MotD from unprivileged user '" + user + "'");
 			user.push(new SystemMessage(null, 0l, MessagePreset.MOTD_NOTALLOWED));
 		}
 	}
 
 	/**
 	 * Opens a user's profile
 	 * @param user
 	 * @param profileUser
 	 */
 	private void profile(ChatUser user, String profileUser) {
 		ChatUser targetUser = chatUserService.getByName(profileUser);
 		if (targetUser == null) {
 			logger.warn("PROFILE with unknown user '" + profileUser + "' supplied!");
 			user.push(new SystemMessage(null, 0l, MessagePreset.UNKNOWN_USER, profileUser));
 		} else {
 			user.push(new SystemMessage(null, 0l, MessagePreset.PROFILE_OPEN, targetUser.getUserName()));
 		}
 	}
 
 	/**
 	 * Changes a rooms backgrounds
 	 * @param user
 	 * @param payload
 	 * @param uploadStream
 	 * @param uploadName
 	 */
 	private void background(ChatUser user, String payload, InputStream uploadStream, String uploadName) {
 		Room userRoom;
 		userRoom = user.getCurrentRoom();
 		// if the user is this rooms owner
 		if (user.getSettings().isTrusted() || user.equals(userRoom.getOwner())) {
 			// parse color and operations out of the payload
 			String newColor = null;
 			boolean resize = false;
 			boolean clear = false;
 			if (payload != null) {
 				for (String load : payload.split(" ")) {
 					String trimmedLoad = load.trim();
 					if (trimmedLoad.equalsIgnoreCase("resize")) {
 						resize = true;
 					} else if (trimmedLoad.equalsIgnoreCase("clear")) {
 						clear = true;
 					} else {
 						if (!trimmedLoad.isEmpty()) {
 							newColor = trimmedLoad;
 						}
 					}
 				}
 			}
 			if (newColor != null) {
 				userRoom.setColor(newColor);
 			}
 			if (uploadStream != null) {
 				try {
 					ByteArrayOutputStream bos = new ByteArrayOutputStream();
 					Color backdrop = Color.WHITE;
 
 					if (newColor != null) {
 						if (newColor.length() == 3) {
 							char[] bytes = newColor.toCharArray();
 							newColor = new String(new char[] { bytes[0], bytes[0], bytes[1], bytes[1], bytes[2], bytes[2]});
 						}
 						int hex = Integer.parseInt(newColor, 16);
 						int r = (hex & 0xFF0000) >> 16;
 						int g = (hex & 0xFF00) >> 8;
 						int b = (hex & 0xFF);
 						logger.debug("parsed big hex to " + r + " " + g + " " + b);
 						backdrop = new Color(r, g, b);
 					}
 					ChatUtils.makeImageMoreTransparent(uploadStream, bos, backdrop);
 					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					String bgImage = "u/" + ChatUtils.createAndStoreResized("bg_", bis, uploadName, 1240, (resize ? 1024 : -1), backdrop);
 					userRoom.setBgImage(bgImage);
 				} catch (Exception e) {
 					logger.error("Error, processing background for user " + user + " file: " + uploadName, e);
 					userRoom.setBgImage(null);
 				}
 			} else if (clear) {
 				userRoom.setBgImage(null);
 			}
 			
 			userRoom.shout(new SystemMessage(null, 0, MessagePreset.CHANNEL_BG_CHANGED, user));
 		}
 	}
 
 	private void foreground(ChatUser user, String fontColor) {
 		Room userRoom;
 		userRoom = user.getCurrentRoom();
 		if (user.getSettings().isTrusted() || user.equals(userRoom.getOwner())) {
 			userRoom.setFontColor(fontColor);
 			userRoom.shout(new SystemMessage(null, 0, MessagePreset.CHANNEL_FG_CHANGED, user));
 		}
 	}
 
 	private void who(ChatUser user, String whoRoomString) {
 		Room whoRoom;
 		if (!whoRoomString.isEmpty()) {
 			whoRoom	= DefaultRoomService.getInstance().getByName(whoRoomString);
 			if (whoRoom == null) {
 				user.push(new SystemMessage(null, 0, MessagePreset.UNKNOWN_CHANNEL, whoRoomString));
 			}
 		} else {
 			whoRoom = user.getCurrentRoom();
 		}
 		if (whoRoom != null) {
 			if (whoRoom.isMember(user) || (whoRoom.isVisible() && !whoRoom.isAnonymous())) {
 				StringBuffer output = new StringBuffer();
 				output.append('*');
 				output.append(whoRoom.getName());
 				output.append(": ");
 				boolean first = true;
 				for (ChatUser roomUser : whoRoom.getUsers()) {
 					if (!first) {
 						output.append(", ");
 					} else {
 						first = false;
 					}
 					output.append(roomUser);
 				}
 				user.push(new SystemMessage(null, 0, new TextSegment(null, output.toString())));
 			} else {
 				user.push(new SystemMessage(null, 0, MessagePreset.CHANNEL_PRIVATE, whoRoom.getName()));
 			}
 		}
 	}
 
 	/**
 	 * Alters a users name, declaring it as 'alias'
 	 * @param user
 	 * @param alias
 	 */
 	private void alias(ChatUser user, String alias) {
 		Room userRoom;
 		userRoom = user.getCurrentRoom();
 		if (alias.isEmpty()) {
 			user.setAlias(null);
 			userRoom.shout(new SystemMessage(null, 0l, MessagePreset.USER_ALIAS_CLEARED, user.getUserName()));
 		} else {
 			user.setAlias(alias);
 			userRoom.shout(new SystemMessage(null, 0l, MessagePreset.USER_ALIAS_SET, user.getUserName(), alias));
 		}
 	}
 	
 }
