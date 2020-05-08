 package entropia.clubmonitor;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.TimeUnit;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.Connection;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.Roster;
 import org.jivesoftware.smack.Roster.SubscriptionMode;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.keepalive.KeepAliveManager;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smack.packet.Presence.Type;
import org.jivesoftware.smack.ping.PingFailedListener;
 import org.jivesoftware.smackx.ServiceDiscoveryManager;
 import org.jivesoftware.smackx.filetransfer.FileTransferListener;
 import org.jivesoftware.smackx.filetransfer.FileTransferManager;
 import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
 import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import entropia.clubmonitor.MpdNotifier.Status;
 import entropia.clubmonitor.XMPPNotifier.StatusChange;
 
 enum XMPPThread implements Runnable {
     INSTANCE;
     
     static {
 	// just to be sure
 	Roster.setDefaultSubscriptionMode(SubscriptionMode.accept_all);
     }
     
     private static final Logger logger =
 	    LoggerFactory.getLogger(XMPPThread.class);
     
     public static Thread startXMPPThread() {
 	Thread thread = new Thread(INSTANCE);
 	thread.setName(XMPPThread.class.getCanonicalName());
 	thread.setDaemon(true);
 	thread.start();
 	return thread;
     }
     
     private static ConnectionConfiguration getConfig() {
 	final String service = Config.getXMPPService();
 	final String host = Config.getXMPPHost();
 	final int port = Config.getXMPPPort();
 	final ConnectionConfiguration config =
 		new ConnectionConfiguration(host, port, service);
 	config.setSASLAuthenticationEnabled(true);
 	config.setCompressionEnabled(false);
 	config.setReconnectionAllowed(true);
 	config.setSecurityMode(SecurityMode.required);
 	config.setSelfSignedCertificateEnabled(true);
 	config.setExpiredCertificatesCheckEnabled(false);
 	config.setNotMatchingDomainCheckEnabled(false);
 	config.setVerifyChainEnabled(false);
 	config.setVerifyRootCAEnabled(false);
 	config.setRosterLoadedAtLogin(true);
 	config.setSendPresence(false);
 	return config;
     }
 
    private static final PingFailedListener EMPTY_PING_FAILED_LISTENER =
            new PingFailedListener() {
                @Override
                public void pingFailed() {
                }
            };
    
     private static XMPPConnection initConnection() throws XMPPException {
 	final XMPPConnection connection = new XMPPConnection(getConfig());
 	connection.connect();
 	connection.login(
 		Config.getXMPPUsername(),
 		Config.getXMPPPassword(),
 		Config.getXMPPResource());
 	FileTransferNegotiator.setServiceEnabled(connection, true);
	KeepAliveManager.getInstanceFor(connection)
	    .addPingFailedListener(EMPTY_PING_FAILED_LISTENER);
 	logger.debug("xmpp connection established");
 	final Iterator<String> features =
 		ServiceDiscoveryManager.getInstanceFor(connection).getFeatures();
 	while (features.hasNext()) {
 	    logger.debug("feature enabled: " + features.next());
 	}
 	return connection;
     }
 
     private static final long RESTART_WAIT_SECONDS =
 	    TimeUnit.MINUTES.toMillis(1);
 
     private void run_() throws InterruptedException {
 	try {
 	    final XMPPConnection connection = initConnection();
 	    try {
 		process(connection);
 	    } finally {
 		connection.disconnect();
 	    }
 	} catch (XMPPException e) {
 	    logger.warn("xmpp error", e);
 	    Thread.sleep(RESTART_WAIT_SECONDS);
 	}
     }
     
     @Override
     public void run() {
 	logger.info("started");
 	final String threadName = Thread.currentThread().getName();
 	while (!Thread.interrupted()) {
 	    try {
 		run_();
 	    } catch (final InterruptedException e) {
 		Thread.currentThread().interrupt();
 	    } catch (final Exception e) {
 		logger.warn("exception in " + threadName, e);
 	    }
 	}
     }
 
     public LinkedBlockingDeque<XMPPNotifier.StatusChange> status =
 	    new LinkedBlockingDeque<XMPPNotifier.StatusChange>();
     
     private void process(Connection connection)
 	    throws InterruptedException, XMPPException {
 	final FileTransferManager fileManager =
 		new FileTransferManager(connection);
 	addFileTransferManager(fileManager);
 	addChatResponder(connection);
 	final MultiUserChat muc = joinMuc(connection);
 	logSubscribedUser(connection);
 	logAdmins();
 	while (true) {
 	    final XMPPNotifier.StatusChange s = status.takeFirst();
 	    setPresence(connection, s);
 	    if (muc != null && s.deliverToMuc()) {
 		notifyMuc(muc, s);
 	    }
 	    if (s.deliverToAll()) {
 		deliverToAll(connection, s);
 	    }
 	}
     }
 
     private static MultiUserChat joinMuc(Connection connection) {
 	if (Config.isXMPPMUCEnabled()) {
 	    try {
 		final String xmppmuc = Config.getXMPPMUC();
 		if (xmppmuc == null) {
 		    throw new IllegalArgumentException("no muc name specified");
 		}
 		final MultiUserChat multiUserChat = new MultiUserChat(connection,
 			xmppmuc);
 		multiUserChat.join(Config.getXMPPUsername());
 		logger.debug("joined " + xmppmuc);
 		final MultiUserChat muc = multiUserChat;
 		addMucResponder(muc);
 		return muc;
 	    } catch (final XMPPException e) {
 		logger.error("error joining muc: " + Config.getXMPPMUC(), e);
 	    }
 	}
 	return null;
     }
 
     private static void logAdmins() {
 	logger.info("*** start admins ***");
 	for (final String s : Config.getXMPPAdmins()) {
 	    logger.info("admin: " + s);
 	}
 	logger.info("*** end admins ***");
     }
     
     private static void logSubscribedUser(Connection connection) {
 	Collection<RosterEntry> entries = connection.getRoster().getEntries();
 	logger.info("*** start subscribed user ***");
 	for (RosterEntry e : entries) {
 	    logger.info(e.getUser() + " : " + e.getType().toString());
 	}
 	logger.info("*** end subscribed user ***");
     }
 
     private static void addFileTransferManager(FileTransferManager fileManager) {
 	fileManager.addFileTransferListener(new FileTransferListener() {
 	    @Override
 	    public void fileTransferRequest(FileTransferRequest request) {
 		try {
 		    final File temp =
 			    File.createTempFile(XMPPThread.class.getName(), ".raw");
 		    try {
 			logger.info("file event from " + request.getRequestor());
 			FileHandlerThread.add(request);
 		    } finally {
 			if (!temp.delete()) {
 			    throw new IOException("temp file delete");
 			}
 		    }
 		} catch (Exception e) {
 		    logger.warn("error receiving file", e);
 		}
 	    }
 	});
     }
 
     private static void addMucResponder(final MultiUserChat muc) {
 	muc.addMessageListener(new PacketListener() {
 	    @Override
 	    public void processPacket(Packet packet) {
 		try {
 		    if (packet instanceof Message) {
 			final Message msg = (Message) packet;
 			final String body = msg.getBody();
 			if (msg.getError() != null) {
 			    return;
 			}
 			if (body != null
 			        && body.startsWith(Config.getXMPPUsername())) {
 			    muc.sendMessage("\n" + StatusServer.oldjson());
 			}
 		    }
 		} catch (Exception e) {
 		    logger.warn("error sending muc message", e);
 		}
 	    }
 	});
     }
 
     private static void addChatResponder(final Connection connection) {
         connection.getChatManager().addChatListener(new ChatManagerListener() {
             @Override
             public void chatCreated(Chat chat, boolean createdLocally) {
                 if (createdLocally) {
                     return;
                 }
                 chat.addMessageListener(new MessageListener() {
                     @Override
                     public void processMessage(Chat chat2, Message message) {
                         try {
                             if (message.getError() != null) {
                         	logger.warn(message.getError().toString());
                         	return;
                             }
                             final String from = message.getFrom();
 			    if (!checkSubscribed(connection, from)) {
 				return;
 			    }
 			    final boolean isAdmin = checkAdmin(from);
                             final String msg = message.getBody();
 			    if ("status".equals(msg)) {
                                 chat2.sendMessage("\n" + StatusServer.oldjson());
                             }
 			    if ("mpd-next".equals(msg)) {
                         	MpdNotifier.submitEvent(Status.NEXT);
                             } else if ("mpd-previous".equals(msg)) {
                         	MpdNotifier.submitEvent(Status.PREVIOUS);
                             } else if ("mpd-pause".equals(msg)) {
                         	MpdNotifier.submitEvent(Status.PAUSE);
                             } else if ("mpd-unpause".equals(msg)) {
                         	MpdNotifier.submitEvent(Status.UNPAUSE);
                             } else if (isAdmin && "ignore-windows".equals(msg)) {
                         	TernaryStatusRegister.OVERRIDE_WINDOWS.on();
                         	chat2.sendMessage("ok");
                             } else if (isAdmin && "unignore-windows".equals(msg)) {
                         	TernaryStatusRegister.OVERRIDE_WINDOWS.off();
                         	chat2.sendMessage("ok");
                             }
                         } catch (Exception e) {
                             logger.warn("error sending direct jabber msg", e);
                         }
                     }
                 });
             }
         });
     }
 
     private static boolean checkAdmin(String from) {
 	final List<String> xmppAdmins = Config.getXMPPAdmins();
 	final int idxSlash = from.indexOf('/');
 	if (idxSlash == -1) {
 	    return xmppAdmins.contains(from);
 	}
 	final String u = from.substring(0, idxSlash);
 	return xmppAdmins.contains(u);
     }
     
     private static boolean checkSubscribed(final Connection connection, 
 	    final String user) {
 	final int idxSlash = user.indexOf('/');
 	final boolean status;
 	if (idxSlash == -1) {
 	    status = connection.getRoster().contains(user);
 	} else {
 	    final String newUser = user.substring(0, idxSlash);
 	    status = connection.getRoster().contains(newUser);
 	}
 	logger.info(String.format("subscribed status of user %s is %b", user, status));
 	return status;
     }
     
     private static void deliverToAll(Connection connection, StatusChange s) {
 	final Collection<RosterEntry> entries = connection.getRoster().getEntries();
 	for (RosterEntry e : entries) {
 	    String user = e.getUser();
 	    final Message message = new Message(user);
 	    message.setBody(s.getMessage());
 	    connection.sendPacket(message);
 	}
     }
 
     private static void notifyMuc(MultiUserChat muc, StatusChange statusChange)
 	    throws XMPPException {
 	muc.sendMessage(statusChange.getMessage());
     }
     
     private static void setPresence(Connection connection, StatusChange s) {
 	final Presence presence;
 	presence = new Presence(Type.available);
 	presence.setMode(s.getMode());
 	presence.setStatus(s.getMessage());
 	connection.sendPacket(presence);
 	logger.debug("xmpp state changed: " + s.getMessage());
     }
 }
