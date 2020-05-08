 // welcome to ___HELL___
 // this is the most ugly code in pointsOp,
 // because it deals with the most ugly protocol (IRC-pointsxt)
 //
 package ru.narod.vn91.pointsop.server;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import javax.swing.ImageIcon;
 
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 import ru.narod.vn91.pointsop.data.Dot;
 import ru.narod.vn91.pointsop.data.TimeLeft;
 import ru.narod.vn91.pointsop.data.GameOuterInfo.GameState;
 import ru.narod.vn91.pointsop.data.TimeSettings;
 import ru.narod.vn91.pointsop.gameEngine.RandomMovesProvider;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngine;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngineInterface;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngineInterface.MoveResult;
 import ru.narod.vn91.pointsop.gameEngine.SingleGameEngineInterface.SurroundingAbstract;
 import ru.narod.vn91.pointsop.gui.GuiForServerInterface;
 import ru.narod.vn91.pointsop.server.irc.IrcNicknameManager;
 import ru.narod.vn91.pointsop.server.irc.IrcRegexp;
 import ru.narod.vn91.pointsop.server.irc.SimpleMove;
 import ru.narod.vn91.pointsop.utils.Function;
 import ru.narod.vn91.pointsop.utils.Function2;
 import ru.narod.vn91.pointsop.utils.Settings;
 
 public class ServerPointsxt
 		extends PircBot
 		implements ServerInterface {
 
 	GuiForServerInterface gui;
 	String myNickOnServ, myNick_Originally;
 	private String defaultServ;
 	private String defaultChannel;
 	private String defaultPass;
 	private String defaultServer_Visible;
 	protected String ircPassword;
 	protected boolean ircAcceptsRussianNicks;
 	String pointsxtVersion = "187";
 	static String gamePrefix = "#pxt";
 	static String commandCommonPrefix = "OpCmd ";
 	static String commandIWantJoinGame = "I want to join this game.";
 	static String commandAcceptOpponent = "I accept opponent ";
 	Date lastSpectrTime = new Date(0);
 	HashMap<String, String> spectrGameData = new HashMap<String, String>();
 	IrcNicknameManager nicknameManager = new IrcNicknameManager();
 	MyGame myGame = new MyGame();
 
 	public void connect() {
 		new Thread() {
 			public void run() {
 				Function<Integer, Boolean> connector = new Function<Integer, Boolean>() {
 					@Override
 					public Boolean call(Integer port) {
 						try {
 							gui.rawConnectionState(
 											ServerPointsxt.this,
 											"Cоединение с сервером " + defaultServ
 													+ ":" + port
 												+ ". Пожалуйста, подождите... (примерно 30 секунд)"
 											);
 							connect(defaultServ, port, ircPassword);
 							myNickOnServ = getNick();
 							ServerPointsxt.super.sendMessage(
 											"podbot",
 											"!opConnect0423"
 											);
 						} catch (NickAlreadyInUseException ignored) {
 							gui.rawConnectionState(ServerPointsxt.this, "Данный ник уже используется. Пожалуйста, выберите себе другой ник.");
 						} catch (IrcException ignored) {
 						} catch (IOException ignored) {
 							return false;
 						}
 						return true;
 					}
 				};
 
 				// let's make the code unreadable, yeah!
 				// in reality, I just try out FP methods
 				// like "foreach" and others :)
 				int[] portList = { Settings.getIrcPort(), 6667, 6029, 7029, 46175 };
 				for (int port : portList) {
 					if (connector.call(port) == true) {
 						Settings.setIrcPort(port);
 						break;
 					} else {
 						gui.rawConnectionState(ServerPointsxt.this,
 										"Не удалось соединиться с " + defaultServ + ":" + port);
 					}
 				}
 				// the same written in a classical way:
 				//
 				// if (connector.call(6667) == false) {
 				// if (connector.call(46175)==false) {
 				// connector.call(5190);
 				// }
 				// }
 			};
 		}.start();
 	}
 
 	public void disconnectServer() {
 		super.disconnect();
 		super.dispose();
 	}
 
 	public ServerPointsxt(
 			String server,
 			GuiForServerInterface gui,
 			String myName,
 			String ircPassword,
 			String roomPassword,
 			boolean ircAcceptsRussianNicks
 	) {
 		super();
 		this.gui = gui;
 		this.defaultServ = server;
 		this.ircPassword = ircPassword;
 		defaultChannel = "#pointsxt";
 		defaultPass = roomPassword;
 		this.ircAcceptsRussianNicks = ircAcceptsRussianNicks;
 		defaultServer_Visible = defaultServ;
 
 		String login = "";
 		try {
 			login = InetAddress.getLocalHost().getHostName();
 			login = login.replaceAll("[^a-zA-Z0-9-]", "");
 			if (login.length() > 9) {
 				login = login.substring(0, 9);
 			}
 		} catch (Exception ignored) {
 		}
 		if (login.equals("")) {
 			login = "l";
 		}
 		super.setLogin(login);
 
 		myName = getAllowedNick(myName, ircAcceptsRussianNicks);
 		if (myName.equals("")) {
 			myName = String.format("Guest%04d", (int) (Math.random() * 9999));
 		}
 		myNick_Originally = myName;
 		myName = "^" + myName;
 		myName = myName + "_X" + pointsxtVersion + "000000000[free]";
 		super.setName(myName);
 		this.myNickOnServ = myName;
 		super.setAutoNickChange(false);
 
 //		super.setVerbose(true);
 
 		super.setMessageDelay(100L);
 		super.setVersion("Op");
 		try {
 			super.setEncoding("CP1251");
 		} catch (UnsupportedEncodingException ignored) {
 		}
 	}
 
 	public synchronized void createGameVacancy() {
 		myGame.leaveGame(false);
 		int roomNumber;
 		String roomAsString;
 		boolean unoccupiedFound = false;
 		do {
 			roomNumber = (int) (Math.random() * 99999);
 			roomAsString = gamePrefix + String.format("%05d", roomNumber);
 			unoccupiedFound = true;
 			User[] userList = getUsers(defaultChannel);
 			for (User anUserList : userList) {
 				String user = anUserList.getNick();
 				if (roomAsString.equals(getPlayerRoom(user))) {
 					// equal found
 					unoccupiedFound = false;
 				}
 			}
 		} while (unoccupiedFound == false);
 		myGame.roomName = roomAsString;
 
 		myGame.engine = new SingleGameEngine(39, 32);
 		myGame.opponentName = "";
 		super.joinChannel(roomAsString);
 
 		this.setPointsxtNickname(
 				""+roomNumber, true, true);
 
 //		super.changeNick(
 //				String.format(
 //						"%s_X" + pointsxtVersion + "0000%05d[g101]",
 //						getMyName(),
 //						roomNumber
 //						)
 //		);
 		{
 			// chat notify...
 			 String notifyMessage =
 			 "оставил(а) заявку на БЛИЦ";
 			 super.sendMessage(defaultChannel, "ACTION " + notifyMessage);
 			 gui.chatReceived(
 			 this,
 			 defaultChannel,
 			 getMyName(),
 			 "*** " + notifyMessage,
 			 null
 			 );
 		}
 	}
 
 	public void acceptGameVacancyOpponent(
 			String roomName,
 			String newOpponent) {
 		if (! roomName.equals(myGame.roomName)) {
 			return;
 		}
 		// if (opponentName.equals("") == false) {
 		// return;
 		// } this protection is already done earlier
 		String ircOpponentName = nicknameManager.id2irc(newOpponent);
 		if (ircOpponentName == null) {
 			// do nothing
 		} else if (IrcRegexp.isPointsXTNickname(ircOpponentName)) {
 			tryInvitePointsxt(ircOpponentName, false);
 		} else if (isPointsopSameVersionNickname(ircOpponentName)) {
 			myGame.opponentName = newOpponent;
 			myGame.amIRed = true;
 			super.sendMessage(
 					roomName,
 					commandCommonPrefix + commandAcceptOpponent + myGame.getOpponentIrcName()
 			);
 			this.setPointsxtNickname(roomName, true, true);
 			gui.subscribedGame(
 				this, roomName);
 			for (User user : super.getUsers(roomName)) {
 				String ircNick = user.getNick();
 				gui.userJoinedRoom(this, roomName, nicknameManager.irc2id(ircNick), true);
 			}
 		}
 	}
 
 	@Override
 	public void rejectGameVacancyOpponent(String roomName, String notWantedOpponent) {
 		super.sendMessage(
 				nicknameManager.id2irc(notWantedOpponent),
 				"Приглашение на игру отклонено.");
 	}
 
 	public void stopGameVacancy() {
 		myGame.leaveGame(false);
 	}
 
 	public void askGameVacancyPlay(String gameRoomName) {
 		super.joinChannel(gameRoomName);
 		super.sendMessage(
 				gameRoomName,
 				commandCommonPrefix + commandIWantJoinGame
 		);
 	}
 
 	@Override
 	public void acceptPersonalGameInvite(String playerId) {
 	}
 
 	@Override
 	public void cancelPersonalGameInvite(String playerId) {
 	}
 
 	@Override
 	public void rejectPersonalGameInvite(String playerId) {
 	}
 
 	public void tryInvitePointsxt(String target, boolean isVerbose) {
 //		if (target.startsWith("\\^")) {
 //			super.sendMessage(
 //					target,
 //					"чтобы принять заявку клиентом pointsOp надо дважды кликнуть по заявке."
 //			);
 //			return;
 //		} else if (myGame.isInSearchNow() == false) {
 //			if (isVerbose) {
 //				super.sendMessage(
 //						target,
 //						"На игру можно вызывать только игрока " +
 //								"который в состоянии поиска оппонента. " +
 //								"(Это системное сообщение.)"
 //						);
 //			}
 //			return;
 //		} else
 
 		super.sendMessage(target, "/PointsXTStart NotRait Blits Chisto");
 //		super.sendMessage(target, "/PointsXTStart NotRait Normal Chisto");
 	}
 
 	public static String getAllowedNick(
 			final String myNameOnServer,
 			final boolean acceptNonEnglish) {
 		// final = I'm testing Functional Programming approach:)
 		final String filtered;
 		if (myNameOnServer.matches(".*[a-zA-Z].*")) {
 			filtered = myNameOnServer.replaceAll("[^a-zA-Z0-9]", "");
 		} else if (myNameOnServer.matches(".*[ёа-яЁА-Я].*") && acceptNonEnglish) {
 			filtered = myNameOnServer.replaceAll("[^ёа-яЁА-Я0-9]", "");
 		} else if (myNameOnServer.matches(".*[a-żA-Ż].*") && acceptNonEnglish) {
 			filtered = myNameOnServer.replaceAll("[^a-żA-Ż0-9]", "");
 		} else {
 			filtered = myNameOnServer.replaceAll("[^0-9]", "");
 		}
 		final String result;
 		if (filtered.length()>9) {
 			result = filtered.substring(0,9);
 		} else {
 			result = filtered;
 		}
 		// myNameOnServer = myNameOnServer.replaceAll("[^a-zA-Zёа-яЁА-Яa-żA-Ż0-9]",
 		// "");
 		return result;
 	}
 
 	public void subscribeRoom(String roomName) {
 		if (roomName.equals(defaultChannel)) {
 			gui.subscribedLangRoom(
 					roomName,
 					this,
 					defaultServer_Visible,
 					defaultChannel.equals(roomName)
 			);
 		} else {
 			gui.subscribedGame(this, roomName);
 		}
 
 		if (roomName.equals("#pointsxt")) {
 			super.joinChannel(roomName, defaultPass);
 		} else {
 			super.joinChannel(roomName);
 		}
 	}
 
 	public void unsubscribeRoom(String roomName) {
 		if ((new Date()).getTime() - lastSpectrTime.getTime() >= 0) {
 			// limit the users from joining-leaving too fast
 			// because it causes a bug in pointsxt
 			lastSpectrTime = new Date();
 			if (roomName.equals(myGame.roomName)) {
 				myGame.leaveGame(true);
 			} else {
 				super.partChannel(roomName);
 				gui.unsubscribedRoom(this, roomName);
 			}
 		}
 	}
 
 	@Override
 	protected void onConnect() {
 		if (super.isConnected()) {
 			gui.rawConnectionState(
 					this,
 					"Удалось соединиться с " + defaultServ
 					+ ", пытаюсь подключиться к основной комнате...");
 		}
 	}
 
 	@Override
 	protected void onJoin(
 			String channel,
 			String sender,
 			String login,
 			String hostname) {
 		userConnected_PointsxtStyle(channel, sender, /*not silent*/ false);
 // other code is executed only when user joins a channel, not changes his nick.
 		if ((channel.equals(defaultChannel)) && (sender.equals(myNickOnServ))) {
 			gui.rawConnectionState(this, "Успешно подключился к основной комнате.");
 		}
 		if (channel.equals(myGame.roomName) && myGame.amIRed) {
 			this.sendSpectr(sender);
 		}
 
 		Function2<User[], String, Boolean> amIOp = new Function2<User[], String, Boolean>() {
 			@Override
 			public Boolean call(User[] users, String me) {
 				for (User user : users) {
 					if (user.getNick().equals(me) && user.isOp()) {
 						return true;
 					}
 				}
 				return false;
 			}
 		};
 		if (sender.equalsIgnoreCase("podbot") &&
 				amIOp.call(getUsers(defaultChannel), getNick())) {
 			super.op(defaultChannel, "podbot");
 		}
 	}
 
 	@Override
 	protected void onDisconnect() {
 		gui.serverClosed(this);
 	}
 
 	@Override
 	protected void onUserList(
 			String channel,
 			User[] users) {
 		for (User user : users) {
 			userConnected_PointsxtStyle(channel, user.getNick(),/*silent*/ true);
 		}
 	}
 
 	@Override
 	protected void onTopic(
 			String channel,
 			String topic,
 			String setBy,
 			long date,
 			boolean changed) {
 		gui.serverNoticeReceived(this, channel, "Topic: " + topic);
 	}
 
 	@Override
 	protected void onKick(
 			String channel,
 			String kickerNick,
 			String kickerLogin,
 			String kickerHostname,
 			String recipientNick,
 			String reason) {
 		userDisconnected_PointsxtStyle(channel, recipientNick, IrcRegexp.cutIrcText(reason));
 	}
 
 	@Override
 	protected void onPart(
 			String channel,
 			String sender,
 			String login,
 			String hostname) {
 		userDisconnected_PointsxtStyle(channel, sender, null);
 	}
 
 	@Override
 	protected void onQuit(
 			String sourceNick,
 			String sourceLogin,
 			String sourceHostname,
 			String reason) {
 		for (String channelName : super.getChannels()) {
 			userDisconnected_PointsxtStyle(channelName, sourceNick, IrcRegexp.cutIrcText(reason));
 		}
 		gui.userDisconnected(this, nicknameManager.irc2id(sourceNick), IrcRegexp.cutIrcText(reason));
 		nicknameManager.removeIrcNick(sourceNick);
 	}
 
 	@Override
 	protected void onNickChange(
 			String oldNick,
 			String login,
 			String hostname,
 			String newNick) {
 		if (getPlayerRoom(oldNick).equals("") == false &&
 				getPlayerRoom(oldNick).equals(getPlayerRoom(newNick)) == false) {
 			clearCreatedGames_PointsxtStyle(oldNick);
 		}
 		nicknameManager.changeIrcNick(oldNick, newNick);
 //		if (nicknameManager.getOrCreateShortNick(newNick).
 //				equals(nicknameManager.getOrCreateShortNick(oldNick)) == false) {
 //			userDisconnected_PointsxtStyle(defaultChannel, oldNick);
 //		}
 		userConnected_PointsxtStyle(defaultChannel, newNick, /*silent*/ true);
 
 //		int rankOld = getPlayerRank(oldNick);
 //		int rankNew = getPlayerRank(newNick);
 //		if ((rankNew != rankOld) && (rankOld != 0) && (rankNew != 0)) {
 //			gui.serverNoticeReceived(
 //					this, defaultChannel,
 //					"" + nicknameManager.irc2id(newNick)
 //							+ " " + rankOld + " -> " + rankNew
 //			);
 //		}
 	}
 
 	@Override
 	protected void onMessage(
 			String channel,
 			String sender,
 			String login,
 			String hostname,
 			String message) {
 		if (channel.equals(defaultChannel) && (
 				message.equalsIgnoreCase("!s")
 						|| message.equalsIgnoreCase("!ы"))) {
 
 			if (IrcRegexp.isPointsopNickname(sender)) {
 //				super.sendMessage(
 //						target,
 //						"чтобы принять заявку клиентом pointsOp надо дважды кликнуть по заявке."
 //				);
 //				return;
 			} else if (myGame.isSearching() == false) {
 			} else {
 				gui.askedPlay(
 						this, myGame.roomName,
 						nicknameManager.irc2id(sender));
 			}
 		}
 		// catch game-invite
 		String nick = nicknameManager.irc2id(sender);
 		int playerNumb = getPlayerIngameNumber(sender);
 		if (message.startsWith(commandCommonPrefix)) {
 			if ((message.startsWith(commandCommonPrefix + commandIWantJoinGame))
 					&& (channel.equals(myGame.roomName))
 					&& (myGame.isPlaying() == false)) {
 				String opponentNick = nicknameManager.irc2id(
 						sender
 				);
 				gui.askedPlay(this, channel, opponentNick);
 			} else if (message.startsWith(commandCommonPrefix
 					+ commandAcceptOpponent + myNickOnServ
 					) && isPointsopSameVersionNickname(sender)) {
 				this.setPointsxtNickname(
 						channel,
 						true, false);
 				myGame.amIRed = false;
 				myGame.engine = new SingleGameEngine(39, 32);
 				myGame.moveList = new ArrayList<SimpleMove>();
 				myGame.opponentName = nick;
 				myGame.roomName = channel;
 				gui.subscribedGame(this, channel);
 				for (User user : super.getUsers(channel)) {
 					String ircNick = user.getNick();
 					gui.userJoinedRoom(this, channel, nicknameManager.irc2id(ircNick), true);
 				}
 			}
 			return; /* "if I won't take it - no one will."
 			if we had a pointsOp message and failed to handle it - do nothing.
 			Because this may be a message from higher versions of the protocol.
 			And this message shouldn't be visible as text in this case */
 		} else if (channel.equals(defaultChannel)) {
 			if (message.startsWith("ACTION")) {
 				gui.chatReceived(this, channel, "***" + nick, 
 					IrcRegexp.cutIrcText(message.substring(7)), null);
 			} else {
 				gui.chatReceived(this, channel, nick, IrcRegexp.cutIrcText(message), null);
 			}
 		} else {
 			// game channel
 			if ((playerNumb != - 1) && (message.matches("..[0-9]{3,3}"))) {
 				// new move
 				int x = getCoordinate(message.charAt(0));
 				int y = getCoordinate(message.charAt(1));
 				x = x + 1; // pointsOp version of coordinates
 				y = 31 - y + 1; // pointsOp version of coordinates
 				sendMoveToGui(
 						channel, false /*not silent*/,
 						x,
 						y,
 						(playerNumb == 1)
 				);
 			} else if (message.equalsIgnoreCase("/ImLost")) {
 				User[] users = getUsers(channel);
 				boolean biggerFishFound = false;
 				for (User user1 : users) {
 					String user = user1.getNick();
 					if ((user.compareTo(getNick()) > 0) && (user.startsWith("^"))) {
 						biggerFishFound = true;
 					}
 				}
 				gui.gameLost(
 						this, getPlayerRoom(sender), playerNumb == 1,
 						biggerFishFound == false
 				);
 			} else if (message.equalsIgnoreCase("/StopTheGame")) {
 				gui.gameStop(this, getPlayerRoom(sender), playerNumb == 1);
 			}
 		}
 	}
 
 	@Override
 	protected void onPrivateMessage(
 			String sender,
 			String login,
 			String hostname,
 			String message) {
 
 		if (message.equals("/SendSOUND")) {
 //			new Sounds().playAlarmSignal();
 //			gui.serverNoticeReceived(
 //					this, defaultChannel, nicknameManager.getOrCreateShortNick(
 //					sender
 //			) + " sends you a sound"
 //			);
 
 			if (IrcRegexp.isPointsopNickname(sender)) {
 				gui.soundReceived(this, nicknameManager.irc2id(sender));
 			} else if (myGame.isSearching() == false) {
 				gui.soundReceived(this, nicknameManager.irc2id(sender));
 				super.sendMessage(
 						sender,
 						"На игру можно вызывать только игрока " +
 								"который в состоянии поиска оппонента. " +
 								"(Это системное сообщение.)"
 						);
 			} else {
 				gui.askedPlay(
 								this, myGame.roomName, nicknameManager
 										.irc2id(sender));
 //			tryInvitePointsxt(sender, true);
 			}
 		} else if (message.equals("/GetTehGame")) {
 			//			if (myGame.isActive()) {
 			//				super.sendMessage(
 			//						sender,
 			//						"Игра уже начата, извините. (Это системное сообщение.)"
 			//				);
 			//			} else {
 			this.sendSpectr(sender);
 			//			}
 		} else if (message.startsWith("/PointsXTAccept ")) {
 			String room = getPlayerRoom(sender);
 			if (IrcRegexp.isPointsXTNickname(sender)
 					&& (room.equals("")) == false) {
 				if (myGame.isPlaying()) {
 					super.sendMessage(
 							sender,
 							"Игра уже начата, извините. (Это системное сообщение.)"
 					);
 				} else {
 					super.partChannel(myGame.roomName);
 					this.setPointsxtNickname(room, true, true);
 					super.joinChannel(room);
 					myGame.amIRed = true;
 					myGame.engine = new SingleGameEngine(39, 32);
 					myGame.moveList = new ArrayList<SimpleMove>();
 					myGame.opponentName = nicknameManager.irc2id(sender);
 					myGame.roomName = room;
 					gui.subscribedGame(this, room);
 					for (User user : super.getUsers(room)) {
 						String ircNick = user.getNick();
 						gui.userJoinedRoom(this, room, nicknameManager.irc2id(ircNick), true);
 					}
 				}
 			}
 		} else if (message.startsWith("/PointsXTDiscard")) {
 		} else if (message.startsWith("/PointsXTStart")) {
 		} else if (
 				message.equalsIgnoreCase("!s")
 						|| message.startsWith("!opstart")
 						|| message.equalsIgnoreCase("!ы")) {
 //			tryInvitePointsxt(sender, true);
 		} else if (message.startsWith("/PASSOK")
 				&& (sender.equalsIgnoreCase("podbot"))) {
 			subscribeRoom("#pointsxt");
 		} else if (message.startsWith("mpPasswordedLoginAccepted ")
 				&& (sender.equalsIgnoreCase("podbot"))) {
 //			subscribeRoom("#pointsxt");
 		} else if (message.startsWith("/PASSOK")) {
 			// someone else sent "passok"
 		} else if (message.startsWith("/SpectrGame")) {
 			String targetRoom = getPlayerRoom(sender);
 			int headerEndsPosition;
 			headerEndsPosition = message.lastIndexOf("00000000") + 8;
 			int tailStartsPosition;
 			tailStartsPosition = message.lastIndexOf(" ");
 			if (tailStartsPosition < headerEndsPosition) {
 				tailStartsPosition = message.length();
 			} else {
 			}
 
 			String suffPartAsString = message.substring(
 					headerEndsPosition,
 					tailStartsPosition
 			);
 			String previousData = spectrGameData.get(targetRoom);
 			if (previousData == null) {
 				previousData = "";
 			}
 			spectrGameData.put(targetRoom, previousData + suffPartAsString);
 
 			if (message.endsWith("-")) {
 				byte[] sufficiendPart = spectrGameData.get(targetRoom).getBytes();
 
 				for (
 						int moveN = 0; ((moveN * 2 + 1) < sufficiendPart.length); moveN++) {
 					int y = sufficiendPart[moveN * 2 + 1];
 					int x = sufficiendPart[moveN * 2];
 					boolean isRed = (x >= 88);
 					x = (x >= 88) ? x - 88 : x - 48;
 					y = (y >= 88) ? y - 88 : y - 48;
 
 					x = x + 1; // pointsOp version of coordinates
 					y = 31 - y + 1; // pointsOp version of coordinates
 					sendMoveToGui(
 							targetRoom, true /*silent*/, x, y,
 							isRed
 					);
 				}
 				sendMoveToGui(
 						targetRoom, false, - 2, - 2, true
 				); // update the paper of the user
 			}
 
 		} else if (message.equals("/Ping")) {
 			// pointsxt-style ping-ing
 			super.sendMessage(sender, "/Pong");
 		} else if (message.equals("/GameMinimaze")) {
 		} else if (message.equals("/GameMaximaze")) {
 		} else if (sender.equalsIgnoreCase("podbot") && message.startsWith(">Игрок")) {
 			gui.privateMessageReceived(this, message.split(" ")[1], message);
 		} else {
 			String nick = nicknameManager.irc2id(sender);
 			gui.privateMessageReceived(this, nick, IrcRegexp.cutIrcText(message));
 		}
 	}
 
 	@Override
 	protected void onUnknown(String line) {
 		if (line.matches(":[^ ]* PONG [^ ]* :[0-9]*")) {
 			// it's a PING-PONG. Don't do anything
 		} else {
 			gui.raw(this, IrcRegexp.cutIrcText(line));
 		}
 	}
 
 	private String getPlayerRoom(String nick) {
 		if (IrcRegexp.isGamerNickname(nick) == false) {
 			return "";
 		} else {
 			String roomSuffix =
 					nick.substring(nick.length() - 11, nick.length() - 6);
 			if (roomSuffix.equals("00000")) {
 				return "";
 			} else {
 				return "#pxt" + roomSuffix;
 			}
 		}
 	}
 
 	private Boolean isRated(String userIrcName) {
 		String pxtGameInfo = userIrcName.substring(
 				userIrcName.length() - 5, userIrcName.length() - 1
 				);
 		if (pxtGameInfo.startsWith("1", 2)) {
 			return true;
 		} else if (pxtGameInfo.startsWith("0", 2)) {
 			return false;
 		} else {
 			return null;
 		}
 	}
 
 	private Boolean isBlitz(String userIrcName) {
 		String pxtGameInfo = userIrcName.substring(
 				userIrcName.length() - 5, userIrcName.length() - 1
 				);
 		if (pxtGameInfo.startsWith("1", 3)) {
 			return true;
 		} else if (pxtGameInfo.startsWith("0", 3)) {
 			return false;
 		} else {
 			return null;
 		}
 	}
 
 //	GameInfoAbstract getGameInfoFromIrcNick(String ircNick) {
 //		return getGameInfoFromRoomName(getPlayerRoom(ircNick));
 //	}
 
 //	GameInfoAbstract getGameInfoFromRoomName(String roomName) {
 //		GameInfoAbstract gameInfoAbstract = new GameInfoAbstract();
 //		User[] users = super.getUsers(defaultChannel);
 //		for (User user : users) {
 //			String userIrcName = user.getNick();
 //			if (getPlayerRoom(userIrcName).equals(roomName)) {
 //				String pxtGameInfo = userIrcName.substring(
 //						userIrcName.length() - 5, userIrcName.length() - 1
 //				);
 //				gameInfoAbstract.isRated = pxtGameInfo.startsWith("1", 2);
 //				gameInfoAbstract.timeLimits = pxtGameInfo.startsWith(
 //						"1", 3
 //				) ? "5sec/turn" : "180sec/5turns";
 //				if (pxtGameInfo.startsWith("1", 1)) {
 //					gameInfoAbstract.userFirst = nicknameManager.getOrCreateShortNick(
 //							userIrcName
 //					);
 //					gameInfoAbstract.rank1 = getPlayerRank(userIrcName);
 //				} else {
 //					gameInfoAbstract.userSecond = nicknameManager.getOrCreateShortNick(
 //							userIrcName
 //					);
 //					gameInfoAbstract.rank2 = getPlayerRank(userIrcName);
 //				}
 //				gameInfoAbstract.startingPosition = "";
 //				//				with this code you can connect even if 1 player disconnects from the public chat.
 //				//				if (gameInfoAbstract.userFirst==null) {
 //				//					gameInfoAbstract.userFirst="";
 //				//				}
 //				//				if (gameInfoAbstract.userSecond==null) {
 //				//					gameInfoAbstract.userSecond="";
 //				//				}
 //			}
 //		}
 //		if ((gameInfoAbstract.userFirst != null) &&
 //				(gameInfoAbstract.userSecond != null)) {
 //			return gameInfoAbstract;
 //		} else {
 //			return null;
 //		}
 //	}
 
 	private void setPointsxtNickname(
 			String roomName,
 			boolean amIPlaying,
 			boolean amIRed
 //			boolean isRanked,
 //			boolean isBlits
 			) {
 		String result = "";
 		result = getMyName();
 		result += "_X";
 //		result += String.format("%03d", programVersion);
 		result += pointsxtVersion;
 		result += "0000";
 		roomName = roomName.replaceAll(".pxt", "");
 		while (roomName.length() < 5) {
 			roomName = "0" + roomName;
 		}
 		result += roomName;
 		if (amIPlaying == false) {
 			result += "[free]";
 		} else {
 			result += "[g";
 			result += amIRed ? "1" : "2";
 //			result += isRanked ? "1" : "0";
 //			result += isBlits ? "1" : "0";
 			result += "01]";
 		}
 		super.changeNick(result);
 	}
 
 	private int getPlayerIngameNumber(String fullNick) {
 		if (IrcRegexp.isGamerNickname(fullNick)) {
 			String letter = fullNick.substring(
 					fullNick.length() - 4,
 					fullNick.length() - 3
 			);
 			if (letter.equals("1")) {
 				return 1;
 			} else if (letter.equals("2")) {
 				return 2;
 			} else {
 				return - 1;
 			}
 		} else {
 			return - 1;
 		}
 
 	}
 
 	private int getCoordinate(char charr) {
 		return charr - "0".charAt(0);
 	}
 
 	private boolean isPointsopSameVersionNickname(String ircNick) {
 		return ircNick.startsWith("^")
 				&& ircNick.matches(
 						".*" + "_X" + pointsxtVersion + "[0-9]{9,9}\\[....\\]" + ".*");
 	}
 
 	public void userConnected_PointsxtStyle(
 			String room,
 			String ircNick,
 			boolean silent) {
 		String shortNick = nicknameManager.irc2id(ircNick);
 		gui.updateUserInfo(
 				this, shortNick,
 				nicknameManager.getGuiNick(ircNick), null, IrcRegexp.getPlayerRank(ircNick),
 				0, 0, 0, IrcRegexp.extractUserStatus(ircNick));
 		if (room.equals(defaultChannel)) {
 			// join Lang room
 			gui.userJoinedRoom(this, room, shortNick, silent);
 		} else {
 			// join Game room
 			if (ircNick.equalsIgnoreCase("podbot") == false) {
 				gui.userJoinedRoom(this, room, shortNick, silent);
 			}
 		}
 		if (room.equals(defaultChannel)) {
 			String newRoom = getPlayerRoom(ircNick);
 			if (newRoom.length() > 0) {
 				gui.updateGameInfo(
 					this, newRoom,
 					defaultChannel, null, null,
 					39, 32,
 					true, null, 0, 0, false, true, false, null,
 					null, 0, 0, null, null);
 				// searching his opponent in the list
 				User[] users = getUsers(defaultChannel);
 				String opponent = "";
 				for (User user : users) {
 					String possibleOpponent = user.getNick();
 					if ((getPlayerRoom(possibleOpponent).equals(newRoom))
 							&& (!nicknameManager.irc2id(possibleOpponent).equals(shortNick))) {
 						opponent = nicknameManager.irc2id(
 								possibleOpponent
 						);
 					}
 				}
 				if (opponent.length() > 0) {
 					String playerFirst = null, playerSecond = null;
 					if (getPlayerIngameNumber(ircNick) == 1) {
 						playerFirst = shortNick;
 						playerSecond = opponent;
 					} else if (getPlayerIngameNumber(ircNick) == 2) {
 						playerFirst = opponent;
 						playerSecond = shortNick;
 					} else {
 						gui.raw(this, "IRC: player number not in bounds 1..2");
 					}
 
 					gui.updateGameInfo(
 							this, newRoom,
 							defaultChannel, playerFirst, playerSecond,
 							39, 32,
 							true, isRated(ircNick),
 							0, 0, false, true, false,
 							GameState.Playing,
 							isBlitz(ircNick) ? 5 : 180, 0, 0, isBlitz(ircNick) ? 1 : 5,
 							null);
 
 					gui.gameRowCreated(this, defaultChannel, newRoom);
 				} else if (IrcRegexp.isPointsopNickname(ircNick)) {
 					// no opponent found of a pointsOp player
 					gui.updateGameInfo(
 							this, newRoom,
 							defaultChannel, shortNick, null,
 							39, 32,
 							true, false, 0, 0, false, true, false,
 							GameState.SearchingOpponent,
 							myGame.getDefaultTime(), 0, 0, myGame.periodLength,
 							null);
 					gui.gameRowCreated(this, defaultChannel, newRoom);
 				}
 			}
 		}
 	}
 
 	public void userDisconnected_PointsxtStyle(
 			String room,
 			String user,
 			String reason) {
 		String userShort = nicknameManager.irc2id(user);
 		if (room.equals(defaultChannel)) {
 			gui.userLeftRoom(
 					this, room,
 					userShort,
 					reason
 			);
 			clearCreatedGames_PointsxtStyle(user);
 		} else {
 			if (user.equalsIgnoreCase("podbot") == false) {
 				if (userShort.equals(getMyName())) {
 					gui.unsubscribedRoom(this, room);
 				} else {
 					gui.userLeftRoom(
 							this, room,
 							userShort,
 							reason
 							);
 				}
 				if (userShort.
 						equals(myGame.getOpponentShortName())
 						&& room.equals(myGame.roomName)) {
 					myGame.clear();
 					gui.chatReceived(this, room, "", "Оппонент покинул игру", null);
 				}
 			}
 		}
 	}
 
 	public void clearCreatedGames_PointsxtStyle(String user) {
 		if (getPlayerRoom(user).length() > 0) {
 			gui.gameRowDestroyed(this, getPlayerRoom(user));
 		}
 	}
 
 	public synchronized void sendMoveToGui(
 			final String room,
 			boolean silent,
 			int x,
 			int y,
 			boolean isRed) {
 		if (room.equals(myGame.roomName)) {
 			MoveResult moveResult = myGame.engine.makeMove(x, y, isRed);
 			if (moveResult != MoveResult.ERROR) {
 				myGame.moveList.add(new SimpleMove(x, y, isRed));
 
 				gui.makedMove(this, room, silent, x, y, isRed, !isRed
 //					, !isRed, myGame.getTimeLeftRed(), myGame.getTimeLeftForBlue()
 					);
 				gui.timeUpdate(this, room, new TimeLeft(
 					myGame.getTimeLeftRed(), myGame.getTimeLeftForBlue(),
 					!isRed, isRed));
 				boolean iHaveMoved = ! myGame.isMyMoveNow();
 				if (iHaveMoved) {
 					myGame.lastTimeoutThread = null;
 				} else if (myGame.moveList.size() >= 2){
 
 					Thread timeOutThread = new Thread() {
 //						@SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
 						@Override
 						public void run() {
 							long timeEnd = new Date().getTime() + myGame.getTimeLeftForMe() * 1000;
 							long estimatedTime = timeEnd - new Date().getTime();
 							while (estimatedTime > 0) {
 								Object o = new Object();
 								synchronized (o) {
 									try {
 										o.wait(estimatedTime + 10);
 									} catch (InterruptedException ignored) {
 									}
 								}
 								estimatedTime = timeEnd - new Date().getTime();
 							}
 							if (this.equals(myGame.lastTimeoutThread)) {
 								Dot dot = myGame.randomMovesProvider.findEmptyRandomPlace(
 										myGame.engine
 								);
 								if (dot != null) {
 									gui.serverNoticeReceived(
 											ServerPointsxt.this,
 											room,
 											"Время вышло и точка сама поставилась в случаиное место на поле"
 									);
 									makeMove(room, dot.x, dot.y);
 								}
 							}
 						}
 					};
 					myGame.lastTimeoutThread = timeOutThread;
 					timeOutThread.start();
 				}
 			}
 		} else {
 			gui.makedMove(this, room, silent, x, y, isRed, !isRed
 //				, !isRed, myGame.getTimeLeftRed(), myGame.getTimeLeftForBlue()
 				);
			gui.timeUpdate(this, room, new TimeLeft(-1, -1, false, false));
 		}
 	}
 
 	public void surrender(String roomName) {
 		myGame.surrender(roomName);
 	}
 
 	@Override
 	public void stop(String roomName) {
 	}
 
 	@Override
 	public void askNewGame(String roomName) {
 	}
 
 	@Override
 	public void cancelAskingNewGame(String roomId) {
 	}
 
 	@Override
 	public void acceptNewGame(String roomName) {
 	}
 
 	@Override
 	public void rejectNewGame(String roomName) {
 	}
 
 	@Override
 	public void askEndGameAndScore(String roomName) {
 	}
 
 	@Override
 	public void cancelAskingEndGameAndScore(String roomId) {
 	}
 
 	@Override
 	public void acceptEndGameAndScore(String roomName) {
 	}
 
 	@Override
 	public void rejectEndGameAndScore(String roomName) {
 	}
 
 	@Override
 	public void askUndo(String roomName) {
 	}
 
 	@Override
 	public void cancelAskingUndo(String roomId) {
 	}
 
 	@Override
 	public void acceptUndo(String roomName) {
 	}
 
 	@Override
 	public void rejectUndo(String roomName) {
 	}
 
 	@Override
 	public void askDraw(String roomName) {
 	}
 
 	@Override
 	public void cancelAskingDraw(String roomId) {
 	}
 
 	@Override
 	public void acceptDraw(String roomName) {
 	}
 
 	@Override
 	public void rejectDraw(String roomName) {
 	}
 
 	@Override
 	public void pauseOpponentTime(String roomName) {
 	}
 
 	@Override
 	public void unpauseOpponentTime(String roomName) {
 	}
 
 	@Override
 	public void addOpponentTime(String roomName, int seconds) {
 	}
 
 	@Override
 	public boolean isStopEnabled() {
 		return false;
 	}
 
 	@Override
 	synchronized public void makeMove(
 			String roomName,
 			int x,
 			int y) {
 		myGame.makeMove(roomName, x, y, myGame.getDefaultTime());
 	}
 
 	public void sendChat(
 			String room,
 			String message) {
 		if (room.equals(defaultChannel)) {
 			super.sendMessage(room, message);
 			gui.chatReceived(
 					this, room, getMyName(),
 					message.replaceAll("ACTION", "***"),
 					null
 			);
 		} else if (room.startsWith("#")) {
 			gui.rawError(this, "IRC не поддерживает сообщений в игровых комнатах");
 		} else {
 			gui.rawError(this, "Была произведена попытка отправить публичное чатовое сообщение в приват.");
 	}
 	}
 
 	public void sendPrivateMsg(
 			String target,
 			String message) {
 		String fullTargetName = nicknameManager.id2irc(target);
 		if (fullTargetName != null) {
 			super.sendMessage(fullTargetName, message);
 		} else {
 			gui.rawError(this,
 					"Не удалось отправить приватное сообщение пользователю " + target);
 		}
 	}
 
 	/**
 	 * @return GUI name
 	 */
 	public String getMyName() {
 		return IrcRegexp.cutIrcTail(myNickOnServ);
 	}
 
 	public String getMainRoom() {
 		return defaultChannel;
 	}
 
 	public String getServerName() {
 		return defaultServer_Visible;
 	}
 
 	@Override
 	public int getMaximumMessageLength() {
 		return 340;
 	}
 
 	public String int2SpectrGameCharacter(int i) {
 		return Character.toString((char) (i + '0'));
 	}
 
 	protected String getSpectr_MainPart() {
 		StringBuilder result = new StringBuilder();
 		for (SimpleMove simpleMove : myGame.moveList) {
 			int x = simpleMove.x - 1;
 			int y = 32 - simpleMove.y;
 			if (simpleMove.isRed) {
 				result.append(int2SpectrGameCharacter(x + 40));
 			} else {
 				result.append(int2SpectrGameCharacter(x));
 			}
 			result.append(int2SpectrGameCharacter(y));
 		}
 		result.append(" ");
 		List<SurroundingAbstract> surroundingsList =
 				myGame.engine.getSurroundings();
 		for (SurroundingAbstract surrounding : surroundingsList) {
 			int x = surrounding.firstCapturedEnemy.x - 1;
 			int y = 32 - surrounding.firstCapturedEnemy.y;
 			if ((myGame.engine.getDotType(x, y)
 					== SingleGameEngineInterface.DotType.RED_EATED_BLUE)
 					|| (myGame.engine.getDotType(x, y)
 					== SingleGameEngineInterface.DotType.RED_TIRED)) {
 				// damn this pointsxt!!!!!!!!!!!
 				result.append(int2SpectrGameCharacter(x + 40));
 			} else {
 				result.append(int2SpectrGameCharacter(x));
 			}
 			result.append(int2SpectrGameCharacter(y));
 		}
 		result.append("-");
 		return result.toString();
 	}
 
 	protected String getSpectr_CommonPrefix() {
 		StringBuilder result = new StringBuilder();
 		result.append("/SpectrGame ");
 		String numberOfTurnsAsString =
 				myGame.moveList.isEmpty() ? "-001"
 						: String.format("%03d", myGame.moveList.size() - 1);
 		result.append(numberOfTurnsAsString);
 		result.append(
 				String.format(
 						"%03d",
 						myGame.engine.getSurroundings().size()
 				)
 		);
 		result.append(myGame.isRedMoveNow() ? "2" : "1");
 		result.append("00000000");
 		return result.toString();
 	}
 
 	public void sendSpectr(String targetIrcNick) {
 		String mainSpectrData = getSpectr_MainPart();
 		final int blockStep = getMaximumMessageLength();
 		int blockStart = 0;
 		while (blockStart < mainSpectrData.length()) {
 			int blockEnd = Math.min(mainSpectrData.length(), blockStart + blockStep);
 			String blockAsString = mainSpectrData.substring(blockStart, blockEnd);
 			super.sendMessage(
 					targetIrcNick,
 					getSpectr_CommonPrefix() + blockAsString
 			);
 			blockStart += blockStep;
 		}
 	}
 
 	@Override
 	public String coordinatesToString(Integer xOrNull, Integer yOrNull) {
 		if (xOrNull != null && yOrNull != null) {
 			return String.format("%02d:%02d", xOrNull - 1, yOrNull - 1);
 		} else if (xOrNull != null) {
 			return String.format("%02d", xOrNull - 1);
 		} else if (yOrNull != null) {
 			return String.format("%02d", yOrNull - 1);
 		} else {
 			return "";
 		}
 	}
 
 	@Override
 	public boolean isIncomingYInverted() {
 		return false;
 	}
 
 	@Override
 	public boolean isGuiYInverted() {
 		return true;
 	}
 
 	@Override
 	public boolean isPrivateChatEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isPingEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isSoundNotifyEnabled() {
 		return true;
 	}
 
 	public void getUserInfoText(String user) {
 		sendPrivateMsg("Podbot", "!mppersinf " + user.replaceAll("\\(.*\\)", ""));
 	}
 
 	public void getUserpic(String user) {
 		// try {
 		// URL url = new URL("http://pointsgame.net/mp/irc-icons/" + user + ".gif");
 		// } catch (MalformedURLException ex) {
 		// }
 
 		URL url = ServerPointsxt.class.getResource("irc/" + user.toLowerCase() + ".gif");
 		if (url != null) {
 			ImageIcon imageIcon = new ImageIcon(url);
 			gui.updateUserInfo(this, user, null, imageIcon, null, null, null, null, null);
 		}
 
 	}
 
 	@Override
 	public boolean isPrivateGameInviteAllowed() {
 		return false;
 	}
 
 	@Override
 	public boolean isGlobalGameVacancyAllowed() {
 		return true;
 	}
 
 	@Override
 	public void addPersonalGameInvite(String playerId, TimeSettings settings, int fieldX, int fieldY) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isField20x20Allowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isField25x25Allowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isField30x30Allowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isField39x32Allowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isStartingEmptyFieldAllowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isStartingCrossAllowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public boolean isStarting4CrossAllowed() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public TimeSettings getTimeSettingsMaximum() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public TimeSettings getTimeSettingsMinimum() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public TimeSettings getTimeSettingsDefault() {
 		throw new UnsupportedOperationException();
 	}
 
 	class MyGame {
 
 		String roomName = "";
 		String opponentName = "";
 		boolean amIRed;
 		SingleGameEngineInterface engine;
 		ArrayList<SimpleMove> moveList = new ArrayList<SimpleMove>();
 		Thread lastTimeoutThread = null;
 		RandomMovesProvider randomMovesProvider = new RandomMovesProvider(39, 32);
 		final int timeAllowedPerTurn = 5;
 		final int periodLength = 1; // not working yet.
 
 		public String getOpponentIrcName() {
 			return nicknameManager.id2irc(opponentName);
 		}
 
 		public String getOpponentShortName() {
 			return opponentName;
 		}
 
 		public int getDefaultTime() {
 			return this.timeAllowedPerTurn;
 		}
 
 		public int getTimeLeftRed() {
 			return getDefaultTime();
 		}
 
 		public int getTimeLeftForBlue() {
 			return getDefaultTime();
 		}
 
 		public int getTimeLeftForColor(boolean color) {
 			if (color == true) {
 				return getTimeLeftRed();
 			} else {
 				return getTimeLeftForBlue();
 			}
 		}
 
 		public int getTimeLeftForMe() {
 			return getTimeLeftForColor(amIRed);
 		}
 
 		private boolean isPlaying() {
 			return "".equals(opponentName) == false;
 		}
 
 		private boolean isSearching() {
 			return roomName != null
 				&& roomName.equals("") == false
 				&& isPlaying() == false;
 		}
 
 		private void clear() {
 			randomMovesProvider = new RandomMovesProvider(39, 32);
 			roomName = "";
 			moveList.clear();
 			opponentName = "";
 			engine = null;
 			ServerPointsxt.this.setPointsxtNickname("", false, false);
 		}
 
 		void leaveGame(boolean isGuiVisible) {
 			if (isPlaying() || isSearching()) {
 				for (String channel : ServerPointsxt.this.getChannels()) {
 					if (channel.equals(roomName)) {
 						String roomNameCopy = roomName;
 						ServerPointsxt.this.partChannel(roomNameCopy);
 						clear();
 						if (isGuiVisible) {
 							ServerPointsxt.this.gui.unsubscribedRoom(ServerPointsxt.this, roomNameCopy);
 						}
 						break;
 					}
 				}
 			}
 		}
 
 		boolean isMyMoveNow() {
 			boolean firstMove = amIRed && moveList.isEmpty();
 			boolean myTurnNow = (moveList.isEmpty() == false)
 				&& (moveList.get(
 						moveList.size() - 1
 						).isRed
 				^ amIRed);
 			return firstMove || myTurnNow;
 		}
 
 		boolean isRedMoveNow() {
 			boolean firstMove = amIRed && moveList.isEmpty();
 			boolean previousWasBlue = (moveList.isEmpty() == false)
 				&& (moveList.get(
 						moveList.size() - 1
 						).isRed == false);
 			return firstMove || previousWasBlue;
 		}
 
 		public void makeMove(
 				String roomName,
 				int x,
 				int y,
 				int timeLeft) {
 			if (roomName.equals(this.roomName)) {
 				boolean isFirstMoveAllowed = ((moveList.size() >= 2))
 					|| ((x - 1 >= 12) && (x - 1 <= 19)
 						&& (32 - y >= 12) && (32 - y <= 26)); // 12<=x<=19, 12<=y<=26
 				if (isMyMoveNow()
 					&& isFirstMoveAllowed
 					&& engine.getDotType(x, y).isEmpty()) {
 					ServerPointsxt.this.sendMoveToGui(
 						roomName, false, x, y,
 						amIRed
 							);
 					String timeLeftAsString = String.format("%03d", timeLeft);
 					ServerPointsxt.this.sendMessage(
 						roomName,
 						"" + (char) ('0' + x - 1)
 							+ (char) ('0' + 32 - y)
 							+ timeLeftAsString
 							);
 				}
 			}
 		}
 
 		public void surrender(String roomName) {
 			if (roomName.equals(this.roomName)
 				&& isMyMoveNow()) {
 				ServerPointsxt.this.sendMessage(roomName, "/ImLost");
 			}
 		}
 	}
 
 }
 
 
