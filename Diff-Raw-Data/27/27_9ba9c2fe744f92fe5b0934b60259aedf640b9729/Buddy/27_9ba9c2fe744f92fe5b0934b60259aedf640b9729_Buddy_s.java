 package net.sayon.dovor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Scanner;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 
 import net.sayon.dovor.events.*;
 
 public class Buddy extends Thread {
 	private static final Logger log;
 	public static final int OFFLINE = -1;
 	public static final int CONNECTING_ME = -2; // full connection waiting on me
 	public static final int CONNECTING_THEM = -3; // full connection waiting on them
 	public static final int CONNECTING = -4; // almost full connection
 	public static final int ONLINE = 1;
 	public static final int AWAY = 2;
 	public static final int XA = 3;
 
 	private Dovor dov;
 	private String address;
 	private String nick;
 	private String profile_name;
 	private String profile_text;
 	private String client;
 	private String version;
 	private String cookie;
 	private String cookieToPong;
 	private Socket incoming;
 	private Socket outgoing;
 	private OutputStreamWriter outgoingWriter;
 	private AtomicBoolean connecting = new AtomicBoolean(false);
 	private int status = OFFLINE;
 	private int unansweredPings = 0;
 	private int failedConnections = 0;
 	private long startConnectingAt = -1;
 	private long lastPingSent = 0;
 	private long lastKeepAliveReceived = -1;
 	private long nextReconnection = -1;
 	private long connectedAt = -1;
 	private long lastKeepAliveSent = -1;
 	private boolean ofcExcecuted = false;
 	private boolean pongSent = false;
 	private boolean receivedPong = false;
 	private boolean fullBuddy;
 	private ScheduledFuture<?> sc;
 	private ArrayList<String> queue = new ArrayList<String>(); // TODO save to disk somewhere
 
 	static {
 		log = Logger.getLogger(Buddy.class.getName());
 		log.setParent(Logger.getLogger(Dovor.class.getName()));
 		log.setLevel(Logger.getLogger(Dovor.class.getName()).getLevel());
 	}
 
 	public Buddy(Dovor dov, String address, boolean fullBuddy) {
 		this.dov = dov;
 		this.address = address;
 		this.cookie = makeCookie();
 		this.fullBuddy = fullBuddy;
 
 		this.sc = dov.getExecutor().scheduleWithFixedDelay(new Bupov(), (long) (dov.getRandom().nextDouble() * 30 * 1000), 30 * 1000, TimeUnit.MILLISECONDS);
 	}
 
 	public void connect() {
 		log.info(getAddress() + ": Connect called.");
 		synchronized (connecting) {
 			if (connecting.get()) {
 				log.warning(getAddress() + " connect() called but already connecting!");
 				return;
 			}
 			if (outgoing != null) {
 				log.warning(getAddress() + " connect() called but outgoing socket not null!");
 				return;
 			}
 			connecting.set(true);
 		}
 		new Thread() {
 			public void run() {
 				try {
 					nextReconnection = -1;
 					startConnectingAt = System.currentTimeMillis();
 					log.info(getAddress() + ": Connect to proxy started.");
 					outgoing = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", dov.getConfig().getSocksPort())));
 					log.info(getAddress() + ": Connect to buddy started.");
 					outgoing.connect(InetSocketAddress.createUnresolved(getAddress() + ".onion", 11009));
 					if (incoming != null)
 						setStatus(CONNECTING);
 					else
 						setStatus(CONNECTING_THEM);
 					log.info(getAddress() + ": Handshake started with " + getAddress());
 					outgoingWriter = new OutputStreamWriter(outgoing.getOutputStream(), "UTF-8");
 					sendPing();
 					if (incoming != null && cookieToPong != null && !pongSent) {
 						sendPong(cookieToPong);
 						cookieToPong = null;
 						checkOfc();
 					}
 
 					connectedAt = System.currentTimeMillis();
 					failedConnections = 0;
 				} catch (Exception e) {
 					e.printStackTrace();
 					failedConnections++;
 					setNextReconnection();
 					disconnect();
 				} finally {
 					startConnectingAt = -1;
 					connecting.set(false);
 				}
 			}
 		}.start();
 	}
 
 	public void setNextReconnection() {
 		nextReconnection = (long) (System.currentTimeMillis() + (failedConnections <= 3 ? 10 : failedConnections <= 6 ? 120 : failedConnections <= 10 ? 600 : 1800) * 1000 + ((dov.getRandom().nextDouble() - 0.5d) * 20 * 2 * 1000));
 	}
 
 	public void disconnect() {
 		Thread.dumpStack();
 		log.info(getAddress() + ": Disconnect called.");
 		if (incoming != null) {
 			try {
 				incoming.close();
 			} catch (IOException e) {
 				// nothing
 			}
 		}
 		if (outgoingWriter != null)
 			try {
 				outgoingWriter.close();
 			} catch (IOException e1) {
 				// nothing
 			}
 		if (outgoing != null) {
 			try {
 				outgoing.close();
 			} catch (IOException e) {
 				// nothing
 			}
 		}
 		setStatus(OFFLINE);
 		startConnectingAt = -1;
 		incoming = null;
 		outgoingWriter = null;
 		outgoing = null;
 		ofcExcecuted = false;
 		pongSent = false;
 		lastPingSent = -1;
 		lastKeepAliveSent = -1;
 		lastKeepAliveReceived = -1;
 		unansweredPings = 0;
 		cookieToPong = null;
 		connecting.set(false);
 		// TODO maybe generate a new cookie here?
 	}
 
 	public void setStatus(int status) {
 		if (this.status != status) {
 			StatusEvent se = new StatusEvent(this, status, this.status);
 			try {
 				dov.getDispatcher().onEvent(se);
 			} catch (IOException e) {
 				e.printStackTrace();
 				disconnect();
 			}
 			this.status = status;
 		}
 	}
 
 	public void sendPing() throws IOException {
 		send(String.format("ping %s %s", dov.getConfig().getTorchatId(), cookie));
 		unansweredPings++;
 		lastPingSent = System.currentTimeMillis();
 	}
 
 	public void sendPong(String pong) throws IOException {
 		send(String.format("pong %s", pong));
 		pongSent = true;
 	}
 
 	public void sendVersion() throws IOException {
 		send(String.format("version %s", dov.getConfig().getVersion()));
 	}
 
 	public void sendProfileText() throws IOException {
 		send(String.format("profile_text %s", dov.getConfig().getProfileText()));
 	}
 
 	public void sendClient() throws IOException {
 		send(String.format("client %s", dov.getConfig().getClient()));
 	}
 
 	public void sendProfileName() throws IOException {
 		send(String.format("profile_name %s", dov.getConfig().getProfileName()));
 	}
 
	public void sendAddNe() throws IOException {
 		send("add_me");
 	}
 
 	public void sendStatus() throws IOException {
 		send(String.format("status %s", dov.getStatusStringInternal()));
 		lastKeepAliveSent = System.currentTimeMillis();
 	}
 
 	public void send(String s) throws IOException {
 		synchronized (outgoing) {
 			try {
 				log.info(getAddress() + " < " + s);
 				outgoingWriter.write(s);
 				outgoingWriter.write(0x0A);
 				outgoingWriter.flush();
 			} catch (IOException e) {
 				disconnect();
 				throw e;
 			}
 		}
 	}
 
 	public void attatchIncoming(Socket s, Scanner sc, String cookietp, String pong) {
 		log.info(getAddress() + ": incoming attatch");
 		try {
 			if (pong != null && !checkPong(pong))
 				return;
 			checkCtp(cookietp);
 			this.incoming = s;
 			while (sc.hasNextLine()) {
 				String l = sc.nextLine();
 				log.info(getAddress() + " > " + l);
 				String[] spl = l.split(" ");
 				if (spl[0].equals("pong")) {
 					if (spl.length < 2) {
 						log.warning(getAddress() + " sent us a pong without a cookie!");
 						break;
 					}
 					if (!checkPong(spl[1]))
 						break;
 				} else if (spl[0].equals("ping")) {
 					String address = spl[1];
 					if (!address.equalsIgnoreCase(getAddress())) {
 						log.warning(getAddress() + " sent us a ping with " + address + " as their id");
 						break;
 					}
 					String cookie = spl[2];
 
 					checkCtp(cookie);
 				} else if (!receivedPong) {
 					log.warning(getAddress() + " sent " + l + " before pong, disconnecting.");
 					break;
 				} else if (spl[0].equals("status")) {
 					lastKeepAliveReceived = System.currentTimeMillis();
 					if (spl[1].equals("available"))
 						setStatus(ONLINE);
 					else if (spl[1].equals("away"))
 						setStatus(AWAY);
 					else if (spl[1].equals("xa"))
 						setStatus(XA);
 					else {
 						log.warning("Received unknown status '" + spl[1] + "' from " + getAddress());
 					}
 				} else if (spl[0].equals("version")) {
 					String newVal;
 					if (l.split(" ", 2).length == 2)
 						newVal = l.split(" ", 2)[1];
 					else
 						newVal = "";
 					version = newVal;
 					VersionEvent ve = new VersionEvent(this, newVal);
 					dov.getDispatcher().onEvent(ve);
 				} else if (spl[0].equals("client")) {
 					String newVal;
 					if (l.split(" ", 2).length == 2)
 						newVal = l.split(" ", 2)[1];
 					else
 						newVal = "";
 					client = newVal;
 					ClientEvent ce = new ClientEvent(this, newVal);
 					dov.getDispatcher().onEvent(ce);
 				} else if (spl[0].equals("profile_text")) {
 					String newVal;
 					if (l.split(" ", 2).length == 2)
 						newVal = l.split(" ", 2)[1];
 					else
 						newVal = "";
 					profile_text = newVal;
 					ProfileTextEvent pte = new ProfileTextEvent(this, newVal);
 					dov.getDispatcher().onEvent(pte);
 				} else if (spl[0].equals("profile_name")) {
 					String newVal;
 					if (l.split(" ", 2).length == 2)
 						newVal = l.split(" ", 2)[1];
 					else
 						newVal = "";
 					profile_name = newVal;
 					ProfileNameEvent pne = new ProfileNameEvent(this, newVal);
 					dov.getDispatcher().onEvent(pne);
 				} else if (spl[0].equals("add_me")) {
 					AddMeEvent ame = new AddMeEvent(this);
 					dov.getDispatcher().onEvent(ame);
 					if (!ame.isConsumed()) {
 						setFullBuddy(true);
 					}
 				} else if (spl[0].equals("remove_me")) {
 					RemoveMeEvent rme = new RemoveMeEvent(this);
 					dov.getDispatcher().onEvent(rme);
 					if (!rme.isConsumed()) {
 						setFullBuddy(false);
 					}
 				} else if (spl[0].equals("message")) {
 					MessageEvent me = new MessageEvent(this, l.split(" ", 2)[1]);
 					dov.getDispatcher().onEvent(me);
 				} else if (spl[0].equals("not_implemented")) {
 					String[] xspl = l.split(" ", 2);
 					NotImplementedEvent me = new NotImplementedEvent(this, xspl.length > 1 ? xspl[1] : null);
 					dov.getDispatcher().onEvent(me);
 				} else { // TODO maybe should have most events be similar to this.
 					String[] xspl = l.split(" ", 2);
 					TextEvent te = new TextEvent(this, spl[0], xspl.length > 1 ? xspl[1] : null);
 					dov.getDispatcher().onEvent(te);
 				}
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			disconnect();
 		}
 	}
 
 	private void checkCtp(String cookietp) throws IOException {
 		synchronized (connecting) {
 			if (!connecting.get() && outgoingWriter != null) {
 				sendPong(cookietp);
 				checkOfc();
 			} else {
 				this.cookieToPong = cookietp;
 				if (!connecting.get()) {
 					setStatus(CONNECTING_ME);
 					connect();
 				}
 			}
 		}
 	}
 
 	private boolean checkPong(String pong) throws IOException {
 		if (!pong.equals(cookie)) {
 			log.warning(getAddress() + " sent us a bad pong, " + pong + " instead of " + cookie);
 			return false;
 		}
 		receivedPong = true;
 		checkOfc();
 		return true;
 	}
 
 	private synchronized void checkOfc() throws IOException {
 		if (receivedPong && pongSent && !ofcExcecuted) {
 			ofcExcecuted = true;
 			onFullyConnected();
 		}
 	}
 
 	private String makeCookie() { // I was really tempted to name this method bakeCookie :3
 		StringBuilder sb = new StringBuilder();
 		final String cs = "abcdefghijklmnopqrstuvwxyz1234567890";
 		for (int i = 0; i < 77; i++) {
 			sb.append(cs.charAt(dov.getRandom().nextInt(cs.length())));
 		}
 		return sb.toString();
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public String getProfile_name() {
 		return profile_name;
 	}
 
 	public String getProfile_text() {
 		return profile_text;
 	}
 
 	public String getClient() {
 		return client;
 	}
 
 	public String getVersion() {
 		return version;
 	}
 
 	public String getNick() {
 		return nick;
 	}
 
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	/**
 	 * Intended for use to set the buddies last known profile_name when loading buddies
 	 * 
 	 * @param profile_name
 	 */
 	public void setProfileName(String profile_name) {
 		this.profile_name = profile_name;
 	}
 
 	public boolean isFullBuddy() {
 		return fullBuddy;
 	}
 
 	public void setFullBuddy(boolean fullBuddy) {
 		this.fullBuddy = fullBuddy;
 	}
 
 	public int getStatus() {
 		return status;
 	}
 
 	public String getStatusStringDisplay() {
 		return Dovor.getStatusStringDisplay(status);
 	}
 
 	public String getStatusStringInternal() {
 		return Dovor.getStatusStringInternal(status);
 	}
 
 	public void onFullyConnected() throws IOException {
 		sendAllInfo();
 		checkQueue();
 		new BuddyReverse().start();
 		FullyConnectedEvent fce = new FullyConnectedEvent(this);
 		dov.getDispatcher().onEvent(fce);
 	}
 
 	public boolean isPongOk() {
 		return pongSent && receivedPong;
 	}
 
 	public void requestAdd() throws IOException {
 		if (isPongOk()) {
 			send("add_me");
 		} else {
 			addToQueue("add_me");
 		}
 	}
 
 	public void addToQueue(String string) {
 		synchronized (queue) {
 			queue.add(string);
 		}
 	}
 
 	public void sendAllInfo() throws IOException {
 		sendClient();
 		sendVersion();
 		sendProfileName();
 		sendProfileText();
 		sendStatus();
 	}
 
 	public long getNextReconnection() {
 		return nextReconnection;
 	}
 
 	private class Bupov implements Runnable {
 		public void run() {
 			if (outgoingWriter != null) {
 				if (unansweredPings > 3) {
 					disconnect();
 					return;
 				} else if (!receivedPong && System.currentTimeMillis() - lastPingSent > 30 * 1000) {
 					try {
 						sendPing();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				} else if (System.currentTimeMillis() - lastKeepAliveReceived > dov.getConfig().getKeepAliveInterval() * 2
 						&& System.currentTimeMillis() - connectedAt > dov.getConfig().getKeepAliveInterval() / 4) {
 					disconnect();
 					return;
 				}
 
 				if (pongSent && receivedPong && System.currentTimeMillis() - lastKeepAliveSent > dov.getConfig().getKeepAliveInterval()) {
 					try {
 						sendStatus();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 				try {
 					checkQueue();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			} else {
 				if (!connecting.get() && nextReconnection >= 0 && System.currentTimeMillis() - nextReconnection > 0) {
 					connect();
 				}
 			}
 		}
 	}
 
 	public void checkQueue() throws IOException {
 		if (pongSent && receivedPong && outgoingWriter != null && incoming != null) {
 			while (queue.size() > 0) {
 				String s = queue.remove(0);
 				send(s);
 			}
 		}
 	}
 
 	private class BuddyReverse extends Thread {
 		public void run() {
 			try {
 				// our own reference so we don't accidentally take a new connection's sockets
 				Socket incoming = Buddy.this.incoming;
 				Socket outgoing = Buddy.this.outgoing;
 
 				InputStream is = outgoing.getInputStream();
 				OutputStream os = incoming.getOutputStream();
 				int c;
 
 				StringBuilder sb = new StringBuilder();
 				while ((c = is.read()) >= 0) {
 					if (c == 0x0A || c == ' ') {
 						String l = sb.toString();
 						sb = new StringBuilder();
 						ReverseEvent re = new ReverseEvent(Buddy.this, l, is, os);
 						dov.getDispatcher().onEvent(re);
 					} else {
 						sb.append((char) c);
 					}
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				disconnect();
 			}
 		}
 	}
 
 }
