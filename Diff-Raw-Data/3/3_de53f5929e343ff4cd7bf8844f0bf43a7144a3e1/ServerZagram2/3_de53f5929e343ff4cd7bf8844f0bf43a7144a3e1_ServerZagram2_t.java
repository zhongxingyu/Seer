 package ru.narod.vn91.pointsop.server;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.ImageIcon;
 
 import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
 
 import ru.narod.vn91.pointsop.data.GameOuterInfo.GameState;
 import ru.narod.vn91.pointsop.data.TimeLeft;
 import ru.narod.vn91.pointsop.data.TimeSettings;
 import ru.narod.vn91.pointsop.model.GuiForServerInterface;
 import ru.narod.vn91.pointsop.server.zagram.MessageQueue;
 import ru.narod.vn91.pointsop.utils.Function;
 import ru.narod.vn91.pointsop.utils.Settings;
 import ru.narod.vn91.pointsop.utils.Wait;
 
 @SuppressWarnings("serial")
 public class ServerZagram2 implements ServerInterface {
 
 	final String myNameOnServer;
 	final boolean isPassworded;
 	final boolean isInvisible;
 	final GuiForServerInterface gui;
 	final String secretId;
 	volatile boolean isDisposed = false;
 	final MessageQueue queue = new MessageQueue(10);
 	final List<String> transiendQueue = new ArrayList<String>();
 
 	volatile boolean isBusy = false;
 	Set<String> personalInvitesIncoming = new LinkedHashSet<String>();
 	Set<String> personalInvitesOutgoing = new LinkedHashSet<String>();
 	String currentInvitationPlayer = "";
 	Set<String> subscribedRooms = new LinkedHashSet<String>();
 	Map<String,Set<String>> playerRooms = new HashMap<String, Set<String>>();
 	ThreadMain threadMain;
 
 	final Map<String,String> avatarUrls = new HashMap<String, String>();
 	final Map<String,ImageIcon> avatarImages = new HashMap<String, ImageIcon>();
 
 	public ServerZagram2(GuiForServerInterface gui, String myNameOnServer, String password, boolean isInvisible) {
 
 		if (myNameOnServer.matches(".*[a-zA-Z].*")) {
 			myNameOnServer = myNameOnServer.replaceAll("[^a-zA-Z0-9 ]", "");
 		} else if (myNameOnServer.matches(".*[ёа-яЁА-Я].*")) {
 			myNameOnServer = myNameOnServer.replaceAll("[^ёа-яЁА-Я0-9 ]", "");
 		} else if (myNameOnServer.matches(".*[a-żA-Ż].*")) {
 			myNameOnServer = myNameOnServer.replaceAll("[^a-żA-Ż0-9 ]", "");
 		} else {
 			myNameOnServer = myNameOnServer.replaceAll("[^0-9 ]", "");
 		}
 		isPassworded = !myNameOnServer.equals("") && !password.equals("");
 		this.isInvisible = isInvisible;
 		if (isPassworded) {
 			this.myNameOnServer = myNameOnServer;
 			this.gui = gui;
 			this.secretId =
 					getBase64ZagramVersion(
 						getSha1(myNameOnServer + "9WB2qGYzzWry1vbVjoSK" + password)).
 							substring(0, 10);
 		} else {
 			if (myNameOnServer.equals("")) {
 				myNameOnServer = String.format("Guest%04d", (int) (Math.random() * 9999));
 			}
 			myNameOnServer = "*" + myNameOnServer;
 			this.myNameOnServer = myNameOnServer;
 			this.gui = gui;
 			Integer secretIdAsInt = (int) (Math.random() * 999999);
 			secretId = secretIdAsInt.toString();
 		}
 	}
 
 	class ZagramGameTyme{
 		final int fieldX, FieldY;
 		final boolean isStopEnabled, isEmptyScored;
 		final int timeStarting, timeAdditional;
 		final String startingPosition;
 		final boolean isRated;
 		final int instantWin;
 		public ZagramGameTyme(int fieldX, int fieldY, boolean isStopEnabled, boolean isEmptyScored, int timeStarting, int timeAdditional,
 				String startingPosition, boolean isRated, int instantWin) {
 			this.fieldX = fieldX;
 			this.FieldY = fieldY;
 			this.isStopEnabled = isStopEnabled;
 			this.isEmptyScored = isEmptyScored;
 			this.timeStarting = timeStarting;
 			this.timeAdditional = timeAdditional;
 			this.startingPosition = startingPosition;
 			this.isRated = isRated;
 			this.instantWin = instantWin;
 		}
 	}
 
 	private static String getGameTypeString(
 			int fieldX, int fieldY, int startingTime, int periodAdditionalTime, boolean isRanked) {
 		return String.format("%s%sstan%s0.%s.%s",
 			fieldX, fieldY, isRanked ? "R" : "F", startingTime, periodAdditionalTime);
 //		return "" + fieldX + fieldY + "stanF0." + startingTime + "." + periodAdditionalTime;
 	}
 
 	ZagramGameTyme getZagramGameTyme(String str) {
 		// 2525noT4R0.180.15
 		String[] dotSplitted = str.split("\\.");
 		try {
 			int startingTime = Integer.parseInt(dotSplitted[1]);
 			int addTime = Integer.parseInt(dotSplitted[2]);
 			String hellishString = dotSplitted[0];
 			int sizeX = Integer.parseInt(hellishString.substring(0, 2));
 			int sizeY = Integer.parseInt(hellishString.substring(2, 4));
 			String rulesAsString = hellishString.substring(4, 8);
 			boolean isStopEnabled = rulesAsString.matches("noT4|noT1|terr");
 			boolean isEmptyScored = rulesAsString.matches("terr");
 			// boolean manualEnclosings = rulesAsString.matches("terr");
 			// boolean stopEnabled = rulesAsString.matches("noT4|noT1");
 			String startingPosition = rulesAsString.replaceAll("no|terr|stan", "");
 			boolean isRated = !(hellishString.substring(8, 9).equals("F"));
 			Integer instantWin = Integer.parseInt(hellishString.substring(9));
 			return new ZagramGameTyme(
 				sizeX, sizeY, isStopEnabled, isEmptyScored,
 				startingTime, addTime, 
 				startingPosition, 
 				isRated, instantWin);
 		} catch (NumberFormatException e) {
 			return null;
 		} catch (ArrayIndexOutOfBoundsException e) {
 			return null;
 		}
 	}
 
 	private static String getBase64ZagramVersion(String input) {
 		if (input.length() % 3 == 2) {
 			input = input + "0";
 		} else if (input.length() % 3 == 1) {
 			input = input + "00";
 		} else {
 		}
 		final char[] base64ZagramStyle = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789_.".toCharArray();
 
 		StringBuilder stringBuilder = new StringBuilder();
 		for (int i = 0; i + 2 < input.length(); i = i + 3) {
 			// 111122223333
 			// aaaaaabbbbbb
 			int hex0 = Integer.parseInt(input.substring(i,i+1), 16);
 			int hex1 = Integer.parseInt(input.substring(i+1,i+2), 16);
 			int hex2 = Integer.parseInt(input.substring(i+2,i+3), 16);
 			int base64_1 = hex0 * 4 + hex1 / 4;
 			int base64_2 = (hex1 & 3) * 16 + hex2;
 			stringBuilder.append(base64ZagramStyle[base64_1]);
 			stringBuilder.append(base64ZagramStyle[base64_2]);
 		}
 		return stringBuilder.toString();
 	}
 
 	private static String getSha1(String input) {
 		try {
 			MessageDigest sha1 = MessageDigest.getInstance("SHA1");
 			byte[] digest = sha1.digest(input.getBytes());
 			return HexBin.encode(digest);
 		} catch (NoSuchAlgorithmException ex) {
 			return input;
 		}
 	}
 
 	@Override
 	public void connect() {
 		Thread threadStartup = new Thread() {
 			@Override
 			public void run() {
 				if (isInvisible) {
 					// do not authorize
 					threadMain = new ThreadMain();
 					threadMain.start();
 				} else {
 					gui.rawConnectionState(ServerZagram2.this, "Подключение...");
 
 					String authorizationURL;
 					if (isPassworded) {
 						authorizationURL = "http://zagram.org/auth.py?co=loguj&opisGracza=" +
 							getServerEncoded(myNameOnServer) +
 							"&idGracza=" +
 							secretId +
 							"&lang=ru";
 					} else {
 						authorizationURL = "http://zagram.org/a.kropki?co=guestLogin&idGracza=" +
 							secretId + "&opis=" +
 							getServerEncoded(myNameOnServer) + "&lang=ru";
 					}
 
 					String authorizationResult = getLinkContent(authorizationURL);
 					boolean isAuthorized;
 					if (authorizationResult.equals("")) {
 						gui.rawConnectionState(ServerZagram2.this, "Соединился. Подключаюсь к основной комнате...");
 						isAuthorized = true;
 					} else if (authorizationResult.startsWith("ok.zalogowanyNaSerwer.")) {
 						// avatarUrls.put(myNameOnServer, authorizationResult.split("\\.")[2]);
 						gui.rawConnectionState(ServerZagram2.this,
 							"Авторизовался (" + myNameOnServer + "). Подключаюсь к основной комнате...");
 						isAuthorized = true;
 					} else {
 						gui.rawConnectionState(ServerZagram2.this, "Ошибка авторизации! Возможно, вы ввели неправильный пароль.");
 						isAuthorized = false;
 					}
 
 					if (isAuthorized) {
 						getLinkContent("http://zagram.org/a.kropki?idGracza=" + secretId + "&co=changeLang&na=ru");
 
 						final Thread disconnectThread = new Thread() {
 							@Override
 							public void run() {
 								disconnectServer();
 							}
 						};
 						Thread killUltimatively = new Thread() {
 							@Override
 							public void run() {
 								// give the "disconnectThread" a little time, and after that kill it
 								Wait.waitExactly(1000L);
 								disconnectThread.interrupt();
 							};
 						};
 						killUltimatively.setDaemon(true);
 						Runtime.getRuntime().addShutdownHook(killUltimatively);
 						Runtime.getRuntime().addShutdownHook(disconnectThread);
 						threadMain = new ThreadMain();
 						threadMain.start();
 					}
 				}
 			};
 		};
 		threadStartup.setDaemon(true);
 		threadStartup.setPriority(Thread.MIN_PRIORITY);
 		threadStartup.setName("zagramThread");
 		threadStartup.start();
 	}
 
 	@Override
 	public void disconnectServer() {
 		this.isDisposed = true;
 
 		final Thread urlInformer = new Thread() {
 			@Override
 			public void run() {
 				getLinkContent("http://zagram.org/a.kropki?playerId=" +
 					secretId + "&co=usunGracza");
 			}
 		};
 		urlInformer.start();
 
 		Thread killer = new Thread() {
 			@Override
 			public void run() {
 				Wait.waitExactly(1000L);
 				urlInformer.interrupt();
 			}
 		};
 		killer.start();
 	}
 
 	@Override
 	public String getMainRoom() {
 		return "0";
 	}
 
 	@Override
 	public String getMyName() {
 		return myNameOnServer;
 	}
 
 	@Override
 	public String getServerName() {
 		return "zagram.org";
 	}
 
 	@Override
 	public int getMaximumMessageLength() {
 		return 100;
 	}
 
 	@Override
 	public void makeMove(String roomName, int x, int y) {
 		String message = "s" + queue.sizePlusOne() +
 							"." + roomName + "." + coordinatesToString(x, y);
 		sendCommandToServer(message);
 	}
 
 	@Override
 	public void askGameVacancyPlay(String gameRoomName) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void acceptGameVacancyOpponent(String roomName, String newOpponent) {
 		gui.raw(this, "MultiPoints пока-что не умеет оставлять заявки на игру на этом сервере..");
 	}
 
 	@Override
 	public void rejectGameVacancyOpponent(String roomName, String notWantedOpponent) {
 		gui.raw(this, "MultiPoints пока-что не умеет оставлять заявки на игру на этом сервере..");
 	}
 
 	@Override
 	public void acceptPersonalGameInvite(String playerId) {
 //		if (personalInvites.contains(playerId)) {
 			String message = "v" + queue.sizePlusOne() +
 					"." + getMainRoom() + "." + "a";
 			sendCommandToServer(message);
 //		}
 	}
 
 	@Override
 	public void cancelPersonalGameInvite(String playerId) {
 		String message = "v" + queue.sizePlusOne() +
 			"." + getMainRoom() + "." + "c";
 		sendCommandToServer(message);
 		gui.youCancelledPersonalInvite(this, playerId, playerId + "@outgoing");
 	}
 
 	@Override
 	public void rejectPersonalGameInvite(String playerId) {
 		if (personalInvitesIncoming.contains(playerId)) {
 			String message = "v" + queue.sizePlusOne() +
 				"." + getMainRoom() + "." + "r";
 			sendCommandToServer(message);
 		}
 	}
 
 	@Override
 	public void addPersonalGameInvite(String playerId, TimeSettings time, int fieldX, int fieldY, boolean isRanked) {
 		String msgToSend =
 				"v" + queue.sizePlusOne() + "." + "0"/* room# */+ "." +
 					"s." + getServerEncoded(playerId) + "." +
 					getGameTypeString(fieldX, fieldY, time.starting, time.periodAdditional, isRanked);
 		sendCommandToServer(msgToSend);
 		gui.updateGameInfo(this, playerId + "@outgoing", getMainRoom(), getMyName(), null,
 			fieldX, fieldY, false, false, 0, 0, false,
 			false, false, null, 0, time.periodAdditional, time.starting, 1, playerId);
 //		gui.yourPersonalInviteSent(this, playerId, playerId + "@outgoing");
 	}
 
 	@Override
 	public void createGameVacancy() {
 		gui.rawError(this, "невозможно оставлять заявки на игру на этом сервере");
 	}
 
 	@Override
 	public void sendChat(String room, String message) {
 		String msgToSend = "c" + queue.sizePlusOne() + "." + room + "."
 				+ getServerEncoded(message);
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void sendPrivateMsg(String target, String message) {
 		gui.rawError(this, "невозможно писать приватные сообщения на этом сервере");
 	}
 
 	@Override
 	public void stopGameVacancy() {
 		gui.rawError(this, "невозможно оставлять заявки на игру на этом сервере");
 	}
 
 	@Override
 	public void subscribeRoom(String room) {
 		String msgToSend = "b" + queue.sizePlusOne() + "." + room + ".";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void unsubscribeRoom(String room) {
 		String msgToSend = "q" + queue.sizePlusOne() + "." + room + ".";
 		sendCommandToServer(msgToSend);
 //		gui.unsubscribedRoom(this, room);
 	}
 
 	@Override
 	public void setStatus(boolean isBusy) {
 		this.isBusy = isBusy;
 		sendCommandToServerTransient("i0nJ" + (isBusy ? "b" : "a"));
 		gui.statusSet(this, isBusy);
 	}
 	
 	@Override
 	public void surrender(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "resign";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void stop(String roomId) {
 		getLinkContent("http://zagram.org/a.kropki?idGracza=" + secretId +
 			"&co=stopMove&stol=" + roomId);
 	}
 
 	@Override
 	public void askNewGame(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "new";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void cancelAskingNewGame(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "cancel1new";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void acceptNewGame(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "new"; // same as ask
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void rejectNewGame(String roomId) {
 		// cannot reject, just ignore
 	}
 
 	@Override
 	public void askEndGameAndScore(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "score";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void cancelAskingEndGameAndScore(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "cancel1score";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void acceptEndGameAndScore(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "accept2score";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void rejectEndGameAndScore(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "reject2score";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void askUndo(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "undo";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void cancelAskingUndo(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "cancel1undo";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void acceptUndo(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "accept2undo";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void rejectUndo(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "reject2undo";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void askDraw(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "draw";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void cancelAskingDraw(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "cancel1draw";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void acceptDraw(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "accept2draw";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void rejectDraw(String roomId) {
 		String msgToSend = "u" + queue.sizePlusOne() + "." + roomId + "." + "reject2draw";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void pauseOpponentTime(String roomId) {
 		String msgToSend = "t" + queue.sizePlusOne() + "." + roomId + "." + "p";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void unpauseOpponentTime(String roomId) {
 		String msgToSend = "t" + queue.sizePlusOne() + "." + roomId + "." + "u";
 		sendCommandToServer(msgToSend);
 	}
 
 	@Override
 	public void addOpponentTime(String roomId, int seconds) {
 		getLinkContent("http://zagram.org/a.kropki" +
 			"?idGracza=" + secretId +
 			"&co=dodajeCzas&ile=" + seconds +
 			"&stol=" + roomId);
 	}
 
 	private static synchronized String getLinkContent(String link) {
 		StringBuilder result = new StringBuilder();
 		try {
 			URL url;
 			URLConnection urlConn;
 			InputStreamReader inStream;
 			url = new URL(link);
 			urlConn = url.openConnection();
 			inStream = new InputStreamReader(
 						urlConn.getInputStream(), "UTF-8");
 			BufferedReader buff = new BufferedReader(inStream);
 			while (true) {
 				String nextLine;
 				nextLine = buff.readLine();
 				if (nextLine != null) {
 					result.append(nextLine);
 				} else {
 					break;
 				}
 			}
 		} catch (MalformedURLException e) {
 		} catch (IOException e) {
 		}
 //		if (Settings.isDebug()) {
 			System.out.println("visiting: " + link);
 			System.out.println("received: " + result.toString());
 //		}
 		return result.toString();
 	}
 
 	private void sendCommandToServer(String message) {
 		queue.add(message);
 	}
 	
 	private void sendCommandToServerTransient(String message) {
 		transiendQueue.add(message);
 	}
 
 	private static String get1SgfCoord(int i) {
 		if (i <= 26) {
 			return Character.toString((char) ('a' + i - 1));
 		} else {
 			return Character.toString((char) ('A' + i - 26 - 1));
 		}
 	}
 
 	private static String coordinatesToString(int x, int y) {
 		return get1SgfCoord(x) + get1SgfCoord(y);
 	}
 
 	private static Integer charToCoordinate(char c) {
 		if (c >= 'a' && c <= 'z') {
 			return Integer.class.cast(c - 'a' + 1);
 		} else if (c >= 'A' && c <= 'Z') {
 			return Integer.class.cast(c - 'A' + 27);
 		} else {
 			return null;
 		}
 	}
 
 	private static Point stringToCoordinates(String twoLetterString) {
 		if (twoLetterString.length() >= 2) {
 			try {
 				char xAsChar = twoLetterString.charAt(0);
 				char yAsChar = twoLetterString.charAt(1);
 				return new Point(
 					charToCoordinate(xAsChar),
 					charToCoordinate(yAsChar));
 			} catch (NullPointerException e) {
 				return null;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	private static String getServerEncoded(String s) {
 		// the order of replacing matters
 		s = s.replaceAll("@", "@A");
 		s = s.replaceAll("/", "@S");
 		try {
 			return URLEncoder.encode(s, "UTF-8");
 		} catch (UnsupportedEncodingException ignore) {
 			return "";
 		}
 	}
 
 	private static String getServerDecoded(String s) {
 		// the order of replacing matters
 		return s
 				.replaceAll("@S", "/")
 				.replaceAll("@A", "@")
 				.replaceAll("&#60;", "<")
 				.replaceAll("&#62;", ">")
 				.replaceAll("&#39;", "'")
 				.replaceAll("&#34;", "\"")
 				.replaceAll("&#45;", "-");
 	}
 
 	private class ThreadMain extends Thread {
 
 		int lastSentCommandNumber = 0;
 		int lastServerMessageNumber = 0;
 
 		@Override
 		public void run() {
 			sendCommandToServerTransient("i0nJ" + (isBusy ? "b" : "a"));
 
 			while (isDisposed == false) {
 
 				String commands = "";
 
 				for (String message : transiendQueue) {
 					commands = commands + message + "/";
 				}
 				transiendQueue.clear();
 
 				for (int i = lastSentCommandNumber + 1; i < queue.size() + 1; i++) {
 					// (non-standard interval. Here is: A < x <= B)
 					commands = commands + queue.get(i) + "/";
 				}
 				lastSentCommandNumber = queue.size();
 
 				if (commands.equals("")) {
 					commands = "x";
 				} else {
 					commands = commands.substring(0, commands.length() - 1);
 				}
 				String text = getLinkContent(
 						"http://zagram.org/a.kropki?playerId=" +
 							secretId + "&co=getBMsg&msgNo=" +
 							lastServerMessageNumber + "&msgFromClient=" + commands);
 				handleText(text);
 
 				Wait.waitExactly(1000L);
 			}
 		}
 
 		private synchronized void handleText(String text) {
 			// if (text.startsWith("ok/") && text.endsWith("/end")) {
 			ServerInterface server = ServerZagram2.this;
 			if (text.matches("ok.*(end|oend)") ||
 					(text.matches("sd.*(end|oend)") && isInvisible)) {
 				String[] splitted =
 						text
 //						.substring(
 //										text.indexOf("ok/") + "ok/".length(),
 //										text.length() - "/end".length())
 								.split("/");
 				Set<String> personalInvitesIncomingNew = new LinkedHashSet<String>();
 				Set<String> personalInvitesOutgoingNew = new LinkedHashSet<String>();
 				Set<String> modifiedSubscribedRooms = new HashSet<String>();
 
 				String currentRoom = "";
 				for (String message : splitted) {
 					if (message.startsWith("b")) { // room subscriptions
 						// b*Вася.0.234.1234.1451.21
 						String player = message.replaceAll("\\..*", "").substring(1);
 						String[] dotSplitted = message.replaceFirst(".*?\\.", "").split("\\.");
 						
 						final Set<String> newRooms = new LinkedHashSet<String>();
 						for (String room : dotSplitted) {
 							newRooms.add(room);
 						}
 						
 						final Set<String> oldRooms = playerRooms.get(player);
 						if (oldRooms==null) {
 //							for (String room : newRooms) {
 //								gui.userJoinedRoom(server, room, player, true);
 //							}
 						}
 						else {
 							for (String room : oldRooms) {
 								if (newRooms.contains(room)==false) {
 //									gui.userLeftRoom(server, room, player, "");
 								}
 							}
 							
 							for (String room : newRooms) {
 								if (oldRooms.contains(room)==false) {
 //									gui.userJoinedRoom(server, room, player, false);
 								}
 							}
 						}
 						
 					}
 					else if (message.startsWith("ca") || message.startsWith("cr")) { // chat
 						String[] dotSplitted = message.substring("ca".length())
 								.split("\\.", 4);
 						try {
 							long time = Long.parseLong(dotSplitted[0]) * 1000L;
 							String nick = dotSplitted[1];
 							// String nickType = dotSplitted[2];
 							String chatMessage =
 									getServerDecoded(dotSplitted[3]);
 							gui.chatReceived(server,
 									currentRoom, nick, chatMessage, time);
 						} catch (NumberFormatException e) {
 							gui.raw(server, "unkown chat: " + message.substring(2));
 						} catch (ArrayIndexOutOfBoundsException e) {
 							gui.raw(server, "unknown chat: " + message.substring(2));
 						}
 					}
 					else if (message.startsWith("d")) { // game description
 
 						// first section and last two sections
 						String suffecientPart = message.
 								replaceFirst("[^.]*.", "").
 								replaceFirst("\\.[^.]*$", "").
 								replaceFirst("\\.[^.]*$", "");
 						ZagramGameTyme gameType = getZagramGameTyme(suffecientPart);
 
 						String[] dotSplitted = message.split("\\.");
 						String roomId = dotSplitted[0].replaceFirst("d", "");
 						String player1 = dotSplitted[dotSplitted.length - 2];
 						String player2 = dotSplitted[dotSplitted.length - 1];
 						gui.updateGameInfo(
 							server, roomId,
 							currentRoom, player1, player2,
 							gameType.fieldX, gameType.FieldY,
 							false, gameType.isRated, 0, gameType.instantWin,
 							false, gameType.isStopEnabled, false, null, 0,
 							gameType.timeAdditional, gameType.timeStarting, 1,
 							null);
 					} else if (message.startsWith("f")) { // flags
 						try {
 							String timeLimitsAsString = message.split("_")[1];
 							if (timeLimitsAsString.equals("")) {
 							} else {
 								Integer time1 = Integer.parseInt(
 										timeLimitsAsString.split("\\.")[0]);
 								Integer time2 = Integer.parseInt(
 										timeLimitsAsString.split("\\.")[1]);
 								gui.timeUpdate(server, currentRoom,
 									new TimeLeft(time1, time2, null, null));
 							}
 						} catch (Exception e) {
 						}
 					} else if (message.startsWith("ga") || message.startsWith("gr")) { // +game
 						String[] dotSplitted = message.substring("ga".length())
 								.split("\\.");
 						for (String gameId : dotSplitted) {
 							if (gameId.length() != 0) {
 								gui.gameRowCreated(server, currentRoom, gameId);
 							}
 						}
 					} else if (message.startsWith("gd")) { // - game
 						String[] dotSplitted = message.substring("gd".length())
 								.split("\\.");
 						for (String gameId : dotSplitted) {
 							if (gameId.length() != 0) {
 								gui.gameRowDestroyed(server, gameId);
 							}
 						}
 					} else if (message.startsWith("h")) { // additional flags
 					} else if (message.startsWith("i")) { // player info
 						String[] dotSplitted = message.substring("i".length()).split("\\.");
 						String player = null, status = null, language = null, myStatus = null;
 						Integer rating = null, winCount = null, lossCount = null, drawCount = null;
 						if (dotSplitted.length == 4 || dotSplitted.length == 8) {
 							player = dotSplitted[0];
 							avatarUrls.put(player, dotSplitted[1]);
 							status = (dotSplitted[2].matches(".*(N|b).*") ? "" : "free");
 							// + (dotSplitted[2].contains("J") ? " java" : "web");
 							language = dotSplitted[3];
 							myStatus = language + " (" + status + ")";
 						}
 						if (dotSplitted.length == 8) {
 							try {
 								rating = Integer.parseInt(dotSplitted[4]);
 								winCount = Integer.parseInt(dotSplitted[5]);
 								lossCount = Integer.parseInt(dotSplitted[7]);
 								drawCount = Integer.parseInt(dotSplitted[6]);
 							} catch (NumberFormatException e) {
 							}
 						}
 						gui.updateUserInfo(server, player, player, null, rating,
 								winCount, lossCount, drawCount, myStatus, null);
 					} else if (message.startsWith("m")) { // message numbers info
 						// try {
 						String tail = message.substring(1);
 						String[] dotSplitted = tail.split("\\.");
 						String a1 = dotSplitted.length >= 1 ? dotSplitted[0] : "";
 						String a2 = dotSplitted.length >= 2 ? dotSplitted[1] : "";
 						String a3 = dotSplitted.length >= 3 ? dotSplitted[2] : "";
 						int i1 = Integer.parseInt(a1);
 						int i2 = Integer.parseInt(a2);
 						int i3 = Integer.parseInt(a3);
 						if (i1 > lastServerMessageNumber + 1) {
 							// break; no, don't break on this step -- we
 							// want to stay alive in case of a server restart.
 						}
 						lastServerMessageNumber = i2;
 						lastSentCommandNumber = i3;
 						// } catch (IndexOutOfBoundsException ignore) {
 						// } catch (NumberFormatException e) {
 						// }
 						// don't catch anything.
 						// If an exception would be thrown then we are completely
 						// unfamiliar with this server protocol.
 						// } else if (message.startsWith("b")) { // player in tables
 						// String playerId = message.replaceAll("\\..*", "");
 						// String[] roomList = message.replaceFirst(".*\\.",
 						// "").split("\\.");
 						// for (String roomId : roomList) {
 						// gui.userJoinedRoom(server, roomId, playerId, false);
 						// }
 					} else if (message.startsWith("pa") || message.startsWith("pr")) { // player joined
 						String[] dotSplitted = message.substring("pa".length()).split("\\.");
 						for (String player : dotSplitted) {
 							if (player.length() != 0) {
 								gui.userJoinedRoom(
 										server, currentRoom, player, message.startsWith("pr"));
 							}
 						}
 					} else if (message.startsWith("pd")) { // - player
 						String[] dotSplitted = message.substring("pd".length())
 								.split("\\.");
 						for (String player : dotSplitted) {
 							if (player.length() != 0) {
 								gui.userLeftRoom(server, currentRoom, player, "");
 							}
 						}
 					} else if (message.startsWith("q")) { // current room
 						String room = message.substring(1);
 						currentRoom = room;
 
 						modifiedSubscribedRooms.add(room);
 						if (subscribedRooms.contains(room)) { // old set
 						} else {
 							if (currentRoom.equals("0")) {
 								gui.subscribedLangRoom(
 									currentRoom,
 									server,
 									"общий чат: zagram",
 									true);
 							} else {
 								gui.subscribedGame(server, currentRoom);
 							}
 						}
 					} else if (message.matches("sa.*|sr.*")) { // game actions
 						message = message.replaceAll("\\(|\\)", "");
 						class FatalGameRoomError extends Exception {
 							public FatalGameRoomError(String s) {
 								super(s);
 							}
 						}
 						try {
 							String usefulPart = message.replaceFirst("sa;|sr;", "");
 							String[] semiSplitted = usefulPart.split(";");
 							for (String sgfNode : semiSplitted) {
 								// ([A-Z]{1,2}\[([^\]|\\\\)*?\])*
 								// ([A-Z]{1,2}\[.*?\])*
 								if (sgfNode.matches("([A-Z]{1,2}\\[.*\\])*") == false) {
 									gui.raw(server, "unknown message structure: " + message);
 								} else {
 									String lastMovePropertyName = "";
 									String[] sgfPropertyList = sgfNode.split("\\]");
 									for (String sgfProperty : sgfPropertyList) {
 										String propertyName = sgfProperty.replaceAll("\\[.*", "");
 										String propertyValue = sgfProperty.replaceFirst(".*\\[", "");
 										if (propertyName.matches("U(B|W)")) {
 											throw new FatalGameRoomError("UNDO is unsupported");
 										} else if (propertyName.matches("B|W|AB|AW|")) {
 											boolean isWhite = propertyName.matches("W|AW") 
 													|| (propertyName.equals("") && lastMovePropertyName.matches("A|AW"));
											if (!propertyName.equals(""))
												lastMovePropertyName = propertyName;
 											gui.makedMove(
 													server, currentRoom,
 													message.startsWith("sr"),
 													stringToCoordinates(propertyValue).x,
 													stringToCoordinates(propertyValue).y,
 													isWhite, !isWhite
 													);
 										} else {
 										}
 									}
 								}
 							}
 							gui.makedMove(server, currentRoom, false, -1, -1, true, false);
 						} catch (FatalGameRoomError e) {
 //							server.unsubscribeRoom(currentRoom);
 							gui.raw(server, "ERROR in game room '" +
 								e.getMessage() +
 								"'. The game position is not guaranteed to be correct in this game for now on.");
 						}
 					} else if (message.matches("u.undo")) {
 						boolean isRed = message.charAt(1) == '1';
 						String playerAsString = isRed ? "red" : "blue";
 						gui.chatReceived(
 							server,
 							currentRoom,
 							"",
 							"player "
 									+ playerAsString
 									+
 									" Запрос на 'undo'. Клиент MultiPoints пока-что не умеет обрабатывать этот вызов.:(",
 							null
 								);
 					} else if (message.startsWith("vg")) { // game invite
 						String usefulPart = message.substring(2);
 						String sender = usefulPart.replaceAll("\\..*", ""); // first part
 						if (personalInvitesIncoming.contains(sender) == false) {
 							String gameDescription = usefulPart.replaceFirst("[^.]*\\.", ""); // other
 							ZagramGameTyme gameType = getZagramGameTyme(gameDescription);
 							if (isBusy == true) {
 								personalInvitesIncoming.add(sender);
 								server.rejectPersonalGameInvite(sender);
 								personalInvitesIncoming.remove(sender); // kind of a hack
 							}
 							else if (gameType.isEmptyScored == true) {
 
 								personalInvitesIncoming.add(sender);
 								server.rejectPersonalGameInvite(sender);
 								personalInvitesIncoming.remove(sender); // kind of a hack
 
 								gui.raw(server, String.format(
 									"Игрок '%s' вызвал(а) тебя на игру: " +
 										"К сожалению, принять заявку невозможно, " +
 										"т.к. польские правила с ручными обводами территории " +
 										"пока-что не поддерживаютяс программой. " +
 										"Отослан отказ от игры. ",
 									sender));
 							} else {
 								gui.updateGameInfo(
 									server, sender + "@incoming", currentRoom,
 									sender, null,
 									gameType.fieldX, gameType.FieldY, false,
 									gameType.isRated, 0,
 									gameType.instantWin, false, gameType.isStopEnabled, false,
 									GameState.SearchingOpponent,
 									0, gameType.timeAdditional, gameType.timeStarting, 1,
 									sender + "to YOU");
 								gui.personalInviteReceived(server, sender, sender + "@incoming");
 								personalInvitesIncomingNew.add(sender);
 							}
 						} else {
 							personalInvitesIncomingNew.add(sender);
 						}
 					} else if (message.startsWith("vr")) {
 						String user = message.substring(2);
 						gui.yourPersonalInviteRejected(server, user, user + "@outgoing");
 						
 						// "OK to the fact that someone rejected your invitation" :-/
 						sendCommandToServer("v" + queue.sizePlusOne() + "." + "0" + "." + "o");
 					} else if (message.startsWith("vs")) {
 						String user = message.substring(2).split("\\.")[0];
 						if (personalInvitesOutgoing.contains(user) == false) {
 							gui.yourPersonalInviteSent(server, user, user + "@outgoing");
 						}
 						personalInvitesOutgoingNew.add(user);
 					}
 				}
 
 				{
 					// warning: UGLY CODE
 					// now I have to compare two sets of player invitations
 					// if I don't have a player in the new set -
 					// then the invitation is closed and I should delete it.
 					for (Iterator<String> iterator = personalInvitesIncoming.iterator(); iterator.hasNext();) {
 						String player = iterator.next();
 						if (personalInvitesIncomingNew.contains(player) == false) {
 							// invitation cancelled
 							gui.personalInviteCancelled(server, player, player + "@incoming");
 							sendCommandToServerTransient("i0nJa");
 						}
 					}
 					personalInvitesIncoming = personalInvitesIncomingNew;
 					personalInvitesIncomingNew = new LinkedHashSet<String>();
 				}
 				{
 					for (Iterator<String> iterator = personalInvitesOutgoing.iterator(); iterator.hasNext();) {
 						String player = iterator.next();
 						if (personalInvitesOutgoingNew.contains(player) == false) {
 							// invitation cancelled
 							gui.yourPersonalInviteRejected(server, player, player + "@outgoing");
 						}
 					}
 					personalInvitesOutgoing = personalInvitesOutgoingNew;
 					personalInvitesOutgoingNew = new LinkedHashSet<String>();
 				}
 				{
 					for (Iterator<String> iterator = subscribedRooms.iterator(); iterator.hasNext();) {
 						String room = iterator.next();
 						if (modifiedSubscribedRooms.contains(room) == false) {
 							// unsubscribed room
 							gui.unsubscribedRoom(server, room);
 						}
 					}
 					subscribedRooms = modifiedSubscribedRooms;
 					modifiedSubscribedRooms = new LinkedHashSet<String>();
 				}
 
 			} else if (text.equals("")) {
 				// we got an empty result. Well, lets treat that as normal.
 			} else {
 				gui.serverClosed(server);
 				isDisposed = true;
 			}
 		}
 	}
 
 	@Override
 	public String coordinatesToString(Integer xOrNull, Integer yOrNull) {
 		Function<Integer, String> getGuiX = new Function<Integer, String>() {
 			@Override
 			public String call(Integer i) {
 				if (i <= 8) {
 					// "a" .. "h"
 					return Character.toString((char) ('a' + i - 1));
 				} else if (i > 8 && i <= 25) {
 					// "j" .. "z"
 					// letter "i" is skipped in zagram coordinates
 					return Character.toString((char) ('a' + i));
 				} else if (i > 25 && i <= 25 + 8) {
 					// "A" .. "H"
 					return Character.toString((char) ('A' + i - 26));
 				} else if (i > 25 + 8) {
 					// "J" .. "Z"
 					return Character.toString((char) ('A' + i - 26 + 1));
 				} else {
 					return "";
 				}
 			}
 		};
 
 		if (xOrNull != null && yOrNull != null) {
 			return String.format("%s%s", getGuiX.call(xOrNull), yOrNull);
 		} else if (xOrNull != null) {
 			return String.format("%s", getGuiX.call(xOrNull));
 		} else if (yOrNull != null) {
 			return String.format("%s", yOrNull);
 		} else {
 			return "";
 		}
 	}
 
 	public void getUserInfoText(String user) {
 	}
 
 	public void getUserpic(String user) {
 		try {
 			if (avatarImages.get(user) != null) {
 			} else if (
 					avatarUrls.get(user) != null
 //					&& !avatarUrls.get(user).equals("")
 //					&& !avatarUrls.get(user).equals("0")
 					) {
 				URL url;
 				if (avatarUrls.get(user).equals("") ||
 						avatarUrls.get(user).equals("0")) {
 					url = new URL("http://zagram.org/awatar2.png");
 				} else {
 					url = new URL("http://zagram.org/awatary/" + avatarUrls.get(user) + ".gif");
 				}
 				// URL url = new URL("http://www.citilink.com/~grizzly/anigifs/bear.gif"); // animation
 				ImageIcon imageIcon = new ImageIcon(url);
 				// imageIcon.getImage().
 				gui.updateUserInfo(this, user, null, imageIcon, null, null, null, null, null, null);
 			}
 		} catch (Exception ex) {
 		}
 	}
 
 	@Override
 	public boolean isIncomingYInverted() {
 		return true;
 	}
 
 	@Override
 	public boolean isGuiYInverted() {
 		return false;
 	}
 
 	@Override
 	public boolean isPrivateChatEnabled() {
 		return false;
 	}
 
 	@Override
 	public boolean isPingEnabled() {
 		return false;
 	}
 	
 	@Override
 	public boolean isStopEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isSoundNotifyEnabled() {
 		return false;
 	}
 
 	@Override
 	public boolean isField20x20Allowed() {
 		return true;
 	}
 
 	@Override
 	public boolean isField25x25Allowed() {
 		return true;
 	}
 
 	@Override
 	public boolean isField30x30Allowed() {
 		return true;
 	}
 
 	@Override
 	public boolean isField39x32Allowed() {
 		return true;
 	}
 
 	@Override
 	public boolean isStartingEmptyFieldAllowed() {
 		return true;
 	}
 
 	@Override
 	public boolean isStartingCrossAllowed() {
 		return false;
 	}
 
 	@Override
 	public boolean isStarting4CrossAllowed() {
 		return false;
 	}
 
 	@Override
 	public TimeSettings getTimeSettingsMaximum() {
 		return new TimeSettings(120 * 60 /* 120 minutes */, 60, 0, 1, 0);
 	}
 
 	@Override
 	public TimeSettings getTimeSettingsMinimum() {
 		return new TimeSettings(60, 2, 0, 1, 0);
 	}
 	
 	@Override
 	public TimeSettings getTimeSettingsDefault() {
 		return new TimeSettings(3 * 60, 15, 0, 1, 0);
 	}
 
 	@Override
 	public boolean isPrivateGameInviteAllowed() {
 		return true;
 	}
 
 	@Override
 	public boolean isGlobalGameVacancyAllowed() {
 		return false;
 	}
 }
