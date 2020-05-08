 package org.littleshoot.commom.xmpp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.URI;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.net.SocketFactory;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManager;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.packet.Message;
 import org.lastbamboo.common.offer.answer.AnswererOfferAnswerListener;
 import org.lastbamboo.common.offer.answer.IceMediaStreamDesc;
 import org.lastbamboo.common.offer.answer.NoAnswerException;
 import org.lastbamboo.common.offer.answer.OfferAnswer;
 import org.lastbamboo.common.offer.answer.OfferAnswerConnectException;
 import org.lastbamboo.common.offer.answer.OfferAnswerFactory;
 import org.lastbamboo.common.offer.answer.OfferAnswerListener;
 import org.lastbamboo.common.offer.answer.OfferAnswerMessage;
 import org.lastbamboo.common.offer.answer.OfferAnswerTransactionListener;
 import org.lastbamboo.common.offer.answer.Offerer;
 import org.lastbamboo.common.p2p.DefaultTcpUdpSocket;
 import org.lastbamboo.common.p2p.P2PConstants;
 import org.littleshoot.mina.common.ByteBuffer;
 import org.littleshoot.util.CipherSocket;
 import org.littleshoot.util.CommonUtils;
 import org.littleshoot.util.KeyStorage;
 import org.littleshoot.util.SessionSocketListener;
 import org.littleshoot.util.mina.MinaUtils;
 import org.littleshoot.util.xml.XmlUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * Default implementation of an XMPP P2P client connection.
  */
 public class ControlXmppP2PClient implements XmppP2PClient {
 
     private final Logger log = LoggerFactory.getLogger(getClass());
     
     private static final Map<String, Socket> incomingControlSockets = 
         new ConcurrentHashMap<String, Socket>();
     
     private final OfferAnswerFactory offerAnswerFactory;
 
     private XMPPConnection xmppConnection;
     
     private final Collection<MessageListener> messageListeners =
         new ArrayList<MessageListener>();
 
     private final int relayWaitTime;
 
     private final String host;
 
     private final int port;
 
     private final String serviceName;
 
     private final SessionSocketListener callSocketListener;
 
     private final InetSocketAddress plainTextRelayAddress;
     
     private final ExecutorService messageProcessingExecutor = 
         Executors.newCachedThreadPool();
     
     private final Map<URI, Socket> outgoingControlSockets = 
         new ConcurrentHashMap<URI, Socket>();
 
     private final boolean useRelay;
     
     private final Set<String> sentMessageIds = new HashSet<String>();
     
     public static ControlXmppP2PClient newGoogleTalkDirectClient(
         final OfferAnswerFactory factory,
         final InetSocketAddress plainTextRelayAddress, 
         final SessionSocketListener callSocketListener, final int relayWait) {
         return new ControlXmppP2PClient(factory, plainTextRelayAddress, 
             callSocketListener, relayWait, "talk.google.com", 5222, "gmail.com", 
             false);
     }
 
     public static ControlXmppP2PClient newGoogleTalkClient(
         final OfferAnswerFactory factory,
         final InetSocketAddress plainTextRelayAddress, 
         final SessionSocketListener callSocketListener, final int relayWait) {
         return new ControlXmppP2PClient(factory, plainTextRelayAddress, 
             callSocketListener, relayWait, "talk.google.com", 5222, "gmail.com", 
             true);
     }
 
     /*
     public static DefaultXmppP2PClient newFacebookChatClient(
         final OfferAnswerFactory factory,
         final SessionSocketListener socketListener, 
         final SessionSocketListener callSocketListener, final int relayWait) {
         return new DefaultXmppP2PClient(factory, socketListener, 
             callSocketListener, relayWait, "chat.facebook.com", 5222, 
             "chat.facebook.com");
     }
     */
 
     private ControlXmppP2PClient(final OfferAnswerFactory offerAnswerFactory,
         final InetSocketAddress plainTextRelayAddress,
         final SessionSocketListener callSocketListener,
         final int relayWaitTime, final String host, final int port, 
         final String serviceName,
         final boolean useRelay) {
         this.offerAnswerFactory = offerAnswerFactory;
         this.plainTextRelayAddress = plainTextRelayAddress;
         this.callSocketListener = callSocketListener;
         this.relayWaitTime = relayWaitTime;
         this.host = host;
         this.port = port;
         this.serviceName = serviceName;
         this.useRelay = useRelay;
     }
     
     @Override
     public Socket newSocket(final URI uri) 
         throws IOException, NoAnswerException {
         log.trace ("Creating XMPP socket for URI: {}", uri);
         if (useRelay) {
             return newSocket(uri, IceMediaStreamDesc.newReliable(), false);
         }
         return newSocket(uri, IceMediaStreamDesc.newReliableNoRelay(), false);
     }
     
     @Override
     public Socket newUnreliableSocket(final URI uri) 
         throws IOException, NoAnswerException {
         log.trace ("Creating XMPP socket for URI: {}", uri);
         if (useRelay) {
             return newSocket(uri, IceMediaStreamDesc.newUnreliableUdpStream(), 
                 false);
         }
         return newSocket(uri, 
             IceMediaStreamDesc.newUnreliableUdpStreamNoRelay(), false);
     }
     
     @Override
     public Socket newRawSocket(final URI uri) 
         throws IOException, NoAnswerException {
         log.trace ("Creating XMPP socket for URI: {}", uri);
         if (useRelay) {
             return newSocket(uri, IceMediaStreamDesc.newReliable(), true);
         }
         return newSocket(uri, IceMediaStreamDesc.newReliableNoRelay(), true);
     }
     
     @Override
     public Socket newRawUnreliableSocket(final URI uri) 
         throws IOException, NoAnswerException {
         log.trace ("Creating XMPP socket for URI: {}", uri);
         if (useRelay) {
             return newSocket(uri, IceMediaStreamDesc.newUnreliableUdpStream(), 
                 true);
         }
         return newSocket(uri, 
             IceMediaStreamDesc.newUnreliableUdpStreamNoRelay(), true);
     }
     
     private Socket newSocket(final URI uri, 
         final IceMediaStreamDesc streamDesc, final boolean raw) 
         throws IOException, NoAnswerException {
         log.trace ("Creating XMPP socket for URI: {}", uri);
         
         final Socket control;
         // We want to synchronized on the control sockets and block new 
         // incoming sockets because it's pointless for them to do much before
         // the control socket is established, since that's how they'll connect
         // themselves.
         synchronized (this.outgoingControlSockets) {
             if (!this.outgoingControlSockets.containsKey(uri)) {
                 log.info("Creating new control socket");
                 control = establishControlSocket(uri, streamDesc);
                 this.outgoingControlSockets.put(uri, control);
             } else {
                 log.info("Using existing control socket");
                 control = this.outgoingControlSockets.get(uri);
             }
         }
         // Note we use a short timeout for waiting for answers. This is 
         // because we've seen XMPP messages get lost in the ether, and we 
         // just want to send a few of them quickly when this does happen.
         final DefaultTcpUdpSocket tcpUdpSocket = 
             new DefaultTcpUdpSocket(new ControlSocketOfferer(control), 
                 this.offerAnswerFactory,
                 this.relayWaitTime, 20 * 1000, streamDesc);
         
         log.info("Trying to create new socket...raw="+raw);
         final Socket sock = tcpUdpSocket.newSocket(uri);
         if (raw) {
             return sock;
         }
         log.info("Creating new CipherSocket");
         return new CipherSocket(sock, tcpUdpSocket.getWriteKey(), 
             tcpUdpSocket.getReadKey());
     }
     
     private Socket establishControlSocket(final URI uri, 
         final IceMediaStreamDesc streamDesc) throws IOException {
         final DefaultTcpUdpSocket tcpUdpSocket = 
             new DefaultTcpUdpSocket(this, this.offerAnswerFactory,
                 this.relayWaitTime, 30 * 1000, streamDesc);
         
         final Socket sock = tcpUdpSocket.newSocket(uri);
         log.info("Created control socket!!");
         //return new CipherSocket(sock, tcpUdpSocket.getWriteKey(), 
         //    tcpUdpSocket.getReadKey());
         return sock;
     }
 
     @Override
     public String login(final String username, final String password) 
         throws IOException {
         return persistentXmppConnection(username, password, "SHOOT-");
     }
     
     @Override
     public String login(final String username, final String password,
         final String id) throws IOException {
         return persistentXmppConnection(username, password, id);
     }
     
     @Override
     public void offer(final URI uri, final byte[] offer,
         final OfferAnswerTransactionListener transactionListener, 
         final KeyStorage keyStorage) throws IOException {
 
         // We need to convert the URI to a XMPP/Jabber JID.
         final String jid = uri.toASCIIString();
         
         final ChatManager chatManager = xmppConnection.getChatManager();
         final Message offerMessage = newOffer(offer, keyStorage);
 
         log.info("Creating chat from: {}", xmppConnection.getUser());
         final Chat chat = chatManager.createChat(jid, 
             new MessageListener() {
                 @Override
                 public void processMessage(final Chat ch, final Message msg) {
                     log.info("Got answer on offerer: {}", msg);
                     messageProcessingExecutor.execute(new XmppInviteOkRunner(msg, 
                         transactionListener, keyStorage));
                 }
             });
         
         log.info("Message listeners just after OFFER: {}", chat.getListeners());
         log.info("Sending INVITE to: {}", jid);
         try {
             chat.sendMessage(offerMessage);
             final String id = offerMessage.getPacketID();
             log.info("Adding packet ID: {}", id);
             sentMessageIds.add(id);
         } catch (final XMPPException e) {
             log.error("Could not send offer!!", e);
             throw new IOException("Could not send offer", e);
         }
     }
     
     private Message newOffer(final byte[] offer, final KeyStorage keyStorage) {
         final Message offerMessage = new Message();
         log.info("Sending offer: {}", new String(offer));
         final String base64Sdp = 
             Base64.encodeBase64URLSafeString(offer);
         offerMessage.setProperty(P2PConstants.MESSAGE_TYPE, P2PConstants.INVITE);
         offerMessage.setProperty(P2PConstants.SDP, base64Sdp);
         offerMessage.setProperty(P2PConstants.CONTROL, "true");
         if (keyStorage != null) {
             offerMessage.setProperty(P2PConstants.SECRET_KEY, 
                     Base64.encodeBase64String(keyStorage.getWriteKey()));
         }
         return offerMessage;
     }
 
     private String persistentXmppConnection(final String username, 
         final String password, final String id) throws IOException {
         XMPPException exc = null;
         for (int i = 0; i < 20000; i++) {
             try {
                 log.info("Attempting XMPP connection...");
                 this.xmppConnection = 
                     singleXmppConnection(username, password, id);
                 log.info("Created offerer");
                 addChatManagerListener(this.xmppConnection);
                 return this.xmppConnection.getUser();
             } catch (final XMPPException e) {
                 final String msg = "Error creating XMPP connection";
                 log.error(msg, e);
                 exc = e;    
             }
             
             // Gradual backoff.
             try {
                 Thread.sleep(i * 100);
             } catch (final InterruptedException e) {
                 log.info("Interrupted?", e);
             }
         }
         if (exc != null) {
             throw new IOException("Could not log in!!", exc);
         }
         else {
             throw new IOException("Could not log in?");
         }
     }
     
     private void addChatManagerListener(final XMPPConnection conn) {
         final ChatManager cm = conn.getChatManager();
         cm.addChatListener(new ChatManagerListener() {
             @Override
             public void chatCreated(final Chat chat, 
                 final boolean createdLocally) {
                 log.info("Created a chat with: {}", chat.getParticipant());
                 log.info("I am: {}", conn.getUser());
                 //log.info("Message listeners on chat: {}", chat.getListeners());
                 log.info("Created locally: " + createdLocally);
                 chat.addMessageListener(new MessageListener() {
                     
                     @Override
                     public void processMessage(final Chat ch, final Message msg) {
                         final String id = msg.getPacketID();
                         log.info("Checking message ID: {}", id);
                         if (sentMessageIds.contains(id)) {
                             log.warn("Message is from us!!");
                             
                             // This is a little silly in that we're sending a 
                             // message back to ourselves, but theoretically
                             // this should signal to the client thread right
                             // away that the invite has failed.
                             final Message error = new Message();
                             error.setProperty(P2PConstants.MESSAGE_TYPE, 
                                 P2PConstants.INVITE_ERROR);
                             error.setTo(chat.getParticipant());
                             try {
                                 chat.sendMessage(error);
                             } catch (final XMPPException e1) {
                                 log.error("Could not send error message", e1);
                             }
                             return;
                         }
                         messageProcessingExecutor.execute(
                             new XmppInviteRunner(ch, msg));
                     }
                 });
             }
         });
     }
     
     private void processControlInvite(final Chat chat, final Message msg) {
         final String readString = 
             (String) msg.getProperty(P2PConstants.SECRET_KEY);
         //final byte[] readKey = Base64.decodeBase64(readString);
         final String sdp = (String) msg.getProperty(P2PConstants.SDP);
         final ByteBuffer offer = ByteBuffer.wrap(Base64.decodeBase64(sdp));
         final String offerString = MinaUtils.toAsciiString(offer);
         log.info("Processing offer: {}", offerString);
         
         final OfferAnswer offerAnswer;
         try {
             offerAnswer = this.offerAnswerFactory.createAnswerer(
                 new ControlSocketOfferAnswerListener(chat.getParticipant()),
                 false);
                 //new AnswererOfferAnswerListener(chat.getParticipant(), 
                 //    this.plainTextRelayAddress, callSocketListener, 
                 //    offerString, answerKey, readKey));
         }
         catch (final OfferAnswerConnectException e) {
             // This indicates we could not establish the necessary connections 
             // for generating our candidates.
             log.warn("We could not create candidates for offer: " + sdp, e);
             final Message error = new Message();
             error.setProperty(P2PConstants.MESSAGE_TYPE, 
                 P2PConstants.INVITE_ERROR);
             error.setTo(chat.getParticipant());
             try {
                 chat.sendMessage(error);
             } catch (final XMPPException e1) {
                 log.error("Could not send error message", e1);
             }
             //this.m_sipClient.writeInviteRejected(invite, 488, 
             //"Not Acceptable Here");
             return;
         }
         final byte[] answer = offerAnswer.generateAnswer();
         final Message inviteOk = newInviteOk(answer);
         inviteOk.setTo(chat.getParticipant());
         log.info("Sending CONTROL INVITE OK to {}", inviteOk.getTo());
         try {
             chat.sendMessage(inviteOk);
             log.info("Sent CONTROL INVITE OK");
         } catch (final XMPPException e) {
             log.error("Could not send error message", e);
         }
         offerAnswer.processOffer(offer);
         log.debug("Done processing CONTROL XMPP INVITE!!!");
     }
     
     private Message newInviteOk(final byte[] answer) {
         final Message inviteOk = new Message();
         inviteOk.setProperty(P2PConstants.MESSAGE_TYPE, P2PConstants.INVITE_OK);
         inviteOk.setProperty(P2PConstants.SDP, 
             Base64.encodeBase64String(answer));
         inviteOk.setProperty(P2PConstants.SECRET_KEY, 
             Base64.encodeBase64String(CommonUtils.generateKey()));
         return inviteOk;
     }
 
     private XMPPConnection singleXmppConnection(final String username, 
         final String password, final String id) throws XMPPException {
         final ConnectionConfiguration config = 
             //new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
             new ConnectionConfiguration(this.host, this.port, this.serviceName);
         config.setExpiredCertificatesCheckEnabled(true);
         config.setNotMatchingDomainCheckEnabled(true);
         config.setSendPresence(false);
         
         config.setCompressionEnabled(true);
         config.setRosterLoadedAtLogin(true);
         config.setReconnectionAllowed(false);
         
         // TODO: This should probably be an SSLSocketFactory no??
         config.setSocketFactory(new SocketFactory() {
             
             @Override
             public Socket createSocket(final InetAddress host, final int port, 
                 final InetAddress localHost, final int localPort) 
                 throws IOException {
                 // We ignore the local port binding.
                 return createSocket(host, port);
             }
             
             @Override
             public Socket createSocket(final String host, final int port, 
                 final InetAddress localHost, final int localPort)
                 throws IOException, UnknownHostException {
                 // We ignore the local port binding.
                 return createSocket(host, port);
             }
             
             @Override
             public Socket createSocket(final InetAddress host, final int port) 
                 throws IOException {
                 log.info("Creating socket");
                 final Socket sock = new Socket();
                 sock.connect(new InetSocketAddress(host, port), 40000);
                 log.info("Socket connected");
                 return sock;
             }
             
             @Override
             public Socket createSocket(final String host, final int port) 
                 throws IOException, UnknownHostException {
                 log.info("Creating socket");
                 return createSocket(InetAddress.getByName(host), port);
             }
         });
         
         return newConnection(username, password, config, id);
     }
 
     private XMPPConnection newConnection(final String username, 
         final String password, final ConnectionConfiguration config,
         final String id) throws XMPPException {
         final XMPPConnection conn = new XMPPConnection(config);
         conn.connect();
         
         log.info("Connection is Secure: {}", conn.isSecureConnection());
         log.info("Connection is TLS: {}", conn.isUsingTLS());
         conn.login(username, password, id);
         
         while (!conn.isAuthenticated()) {
             log.info("Waiting for authentication");
             try {
                 Thread.sleep(400);
             } catch (final InterruptedException e1) {
                 log.error("Exception during sleep?", e1);
             }
         }
         
         conn.addConnectionListener(new ConnectionListener() {
             
             @Override
             public void reconnectionSuccessful() {
                 log.info("Reconnection successful...");
             }
             
             @Override
             public void reconnectionFailed(final Exception e) {
                 log.info("Reconnection failed", e);
             }
             
             @Override
             public void reconnectingIn(final int time) {
                 log.info("Reconnecting to XMPP server in "+time);
             }
             
             @Override
             public void connectionClosedOnError(final Exception e) {
                 log.info("XMPP connection closed on error", e);
                 try {
                     persistentXmppConnection(username, password, id);
                 } catch (final IOException e1) {
                     log.error("Could not re-establish connection?", e1);
                 }
             }
             
             @Override
             public void connectionClosed() {
                 log.info("XMPP connection closed. Creating new connection.");
                 try {
                     persistentXmppConnection(username, password, id);
                 } catch (final IOException e1) {
                     log.error("Could not re-establish connection?", e1);
                 }
             }
         });
         
         return conn;
     }
 
     @Override
     public XMPPConnection getXmppConnection() {
         return xmppConnection;
     }
 
     @Override
     public void addMessageListener(final MessageListener ml) {
         messageListeners.add(ml);
     }
     
 
     /**
      * Runnable for off-loading INVITE OK processing to a thread pool.
      */
     private final class XmppInviteOkRunner implements Runnable {
 
         private final Message msg;
         private final OfferAnswerTransactionListener transactionListener;
         private final KeyStorage keyStorage;
 
         private XmppInviteOkRunner(final Message msg, 
             final OfferAnswerTransactionListener transactionListener, 
             final KeyStorage keyStorage) {
             this.msg = msg;
             this.transactionListener = transactionListener;
             this.keyStorage = keyStorage;
             
         }
         
         @Override
         public void run() {
             final byte[] body = CommonUtils.decodeBase64(
                 (String) msg.getProperty(P2PConstants.SDP));
             final byte[] key = CommonUtils.decodeBase64(
                 (String) msg.getProperty(P2PConstants.SECRET_KEY));
             keyStorage.setReadKey(key);
             final OfferAnswerMessage oam = 
                 new OfferAnswerMessage() {
                     @Override
                     public String getTransactionKey() {
                         return String.valueOf(hashCode());
                     }
                     @Override
                     public ByteBuffer getBody() {
                         return ByteBuffer.wrap(body);
                     }
                 };
                 
             final Object obj = 
                 msg.getProperty(P2PConstants.MESSAGE_TYPE);
             if (obj == null) {
                 log.error("No message type!!");
                 return;
             }
             final int type = (Integer) obj;
             switch (type) {
                 case P2PConstants.INVITE_OK:
                     transactionListener.onTransactionSucceeded(oam);
                     break;
                 case P2PConstants.INVITE_ERROR:
                     // This can happen when a message is in fact from us, and
                     // we send an error message to ourselves, for example. 
                     // We'll see messages from us when trying to send them to
                     // non-existent peers, for example.
                     log.info("Got INVITE_ERROR - transaction failed");
                     transactionListener.onTransactionFailed(oam);
                     break;
                 default:
                     log.error("Did not recognize type: " + type);
                     log.error(XmppUtils.toString(msg));
             }
         }
     }
     
     /**
      * Runnable for off-loading INVITE processing to a thread pool.
      */
     private final class XmppInviteRunner implements Runnable {
 
         private final Chat chat;
         private final Message msg;
 
         private XmppInviteRunner(final Chat chat, final Message msg) {
             this.chat = chat;
             this.msg = msg;
         }
         
         @Override
         public void run() {
             log.info("Got message from {}", chat.getParticipant());
             final Object obj = 
                 msg.getProperty(P2PConstants.MESSAGE_TYPE);
             if (obj == null) {
                 log.info("No message type!! Notifying listeners");
                 notifyListeners();
                 return;
             }
 
             final int mt = (Integer) obj;
             switch (mt) {
                 case P2PConstants.INVITE:
                     log.info("Processing CONTROL INVITE");
                     //processInvite(chat, msg);
                     processControlInvite(chat, msg);
                     break;
                 default:
                     log.info("Non-standard message on aswerer..." +
                         "sending to additional listeners, if any: "+ mt);
                     notifyListeners();
                     break;
             }
         }
 
         private void notifyListeners() {
             log.info("Notifying global listeners");
             synchronized (messageListeners) {
                 if (messageListeners.isEmpty()) {
                     log.info("No message listeners to forward to");
                 }
                 for (final MessageListener ml : messageListeners) {
                     ml.processMessage(chat, msg);
                 }
             }
         }
     }
     
     private class ControlSocketOfferer implements Offerer {
 
         private final Socket control;
 
         private ControlSocketOfferer(final Socket control) {
             this.control = control;
         }
 
         @Override
         public void offer(final URI uri, final byte[] offer,
             final OfferAnswerTransactionListener transactionListener,
             final KeyStorage keyStore) throws IOException {
             log.info("Sending message from local address: {}", 
                 this.control.getLocalSocketAddress());
             synchronized (this.control) {
                 log.info("Got lock on control socket");
                 final Message msg = newOffer(offer, null);
                 final String xml = toXml(msg);
                 log.info("Writing XML offer on control socket: {}", xml);
                 
                 // We just block on a single offer and answer.
                
                // We also need to catch IOExceptions here for when the control
                // socket is broken for some reason.
                 final OutputStream os = this.control.getOutputStream();
                 os.write(xml.getBytes("UTF-8"));
                 os.flush();
                 
                 log.info("Wrote message on control socket stream: {}", os);
                 final InputStream is = this.control.getInputStream();
                 try {
                     log.info("Reading incoming answer on control socket");
                     final Document doc = XmlUtils.toDoc(is, "</message>");
                     final String received = XmlUtils.toString(doc);
                     log.info("Got XML answer: {}", received);
                     
                     // We need to extract the SDP to establish the new socket.
                     final String sdp = XmppUtils.extractSdp(doc);
                     final byte[] sdpBytes = Base64.decodeBase64(sdp); 
                     
                     final OfferAnswerMessage message = 
                         new OfferAnswerMessage() {
                             @Override
                             public String getTransactionKey() {
                                 return String.valueOf(hashCode());
                             }
                             @Override
                             public ByteBuffer getBody() {
                                 return ByteBuffer.wrap(sdpBytes);
                             }
                         };
                         
                     log.info("Calling transaction succeeded on listener: {}", 
                         transactionListener);
                     transactionListener.onTransactionSucceeded(message);
                 } catch (final SAXException e) {
                     log.warn("Could not parse INVITE OK", e);
                     // Close the socket?
                     IOUtils.closeQuietly(this.control);
                 }
             }
         }
     }
 
     private final class ControlSocketOfferAnswerListener 
         implements OfferAnswerListener {
     
         private final String fullJid;
     
         public ControlSocketOfferAnswerListener(final String fullJid) {
             log.info("Creating listener on answerwer with full JID: {}", 
                 fullJid);
             this.fullJid = fullJid;
         }
     
         @Override
         public void onOfferAnswerFailed(final OfferAnswer offerAnswer) {
             // The following will often happen for one of TCP or UDP.
             log.info("TCP or UDP offer answer failed: {}", offerAnswer);
         }
     
         @Override
         public void onTcpSocket(final Socket sock) {
            log.info("Got a TCP socket!");
             onSocket(sock);
         }
     
         @Override
         public void onUdpSocket(final Socket sock) {
            log.info("Got a UDP socket!");
             onSocket(sock);
         }
     
         private void onSocket(final Socket sock) {
             log.info("Got control socket on 'server' side");
             // We use one control socket for sending offers and another one
             // for receiving offers. This is an incoming socket for 
             // receiving offers.
             incomingControlSockets.put(this.fullJid, sock);
             try {
                 readInvites(sock);
             } catch (final IOException e) {
                log.info("Exception reading invites - this will happen " +
                    "whenever the other side closes the connection, which " +
                    "will happen all the time.", e);
             } catch (final SAXException e) {
                 log.info("Exception reading invites", e);
             }
         }
     
         private void readInvites(final Socket sock) throws IOException, 
             SAXException {
             final InputStream is = sock.getInputStream();
             log.info("Reading streams from remote address: {}", 
                  sock.getRemoteSocketAddress());
             log.info("Reading answerer invites on input stream: {}", is);
             while (true) {
                 // This will parse the full XML/XMPP message and extract the 
                 // SDP from it.
                 final Document doc = XmlUtils.toDoc(is, "</message>");
                 log.info("Got XML INVITE: {}", XmlUtils.toString(doc));
                 final String sdp = XmppUtils.extractSdp(doc);
                 final String key = XmppUtils.extractKey(doc);
                 
                 final ByteBuffer offer = 
                     ByteBuffer.wrap(Base64.decodeBase64(sdp));
                 processOffer(offer, sock, key);
             }
         }
     }
     
 
     private void processOffer(final ByteBuffer offer, final Socket sock, 
         final String readKey) throws IOException {
         log.info("Processing offer...");
         final String offerString = MinaUtils.toAsciiString(offer);
         
         final byte[] answerKey = CommonUtils.generateKey();
         final OfferAnswer offerAnswer;
         final byte[] key;
         if (StringUtils.isBlank(readKey)) {
             key = null;
         } else {
             key = readKey.getBytes("UTF-8");
         }
         try {
             offerAnswer = this.offerAnswerFactory.createAnswerer(
                 new AnswererOfferAnswerListener("", 
                     this.plainTextRelayAddress, callSocketListener, 
                     offerString, null, null), this.useRelay);
         }
         catch (final OfferAnswerConnectException e) {
             // This indicates we could not establish the necessary connections 
             // for generating our candidates.
             log.warn("We could not create candidates for offer", e);
             error(sock);
             return;
         }
         log.info("Creating answer");
         final byte[] answer = offerAnswer.generateAnswer();
         log.info("Creating INVITE OK");
         final Message inviteOk = newInviteOk(answer);
         log.info("Writing INVITE OK");
         writeMessage(inviteOk, sock);
         log.info("Wrote INVITE OK");
        
        log.info("Processing offer...");
        offerAnswer.processOffer(offer);
     }
     
    private void error(final Socket sock) {
         final Message error = new Message();
         error.setProperty(P2PConstants.MESSAGE_TYPE, 
             P2PConstants.INVITE_ERROR);
         try {
             writeMessage(error, sock);
         } catch (final IOException e) {
             log.warn("Could not write message", e);
         }
     }
 
     private void writeMessage(final Message msg, final Socket sock) 
         throws IOException {
         log.info("Sending message through socket: {}", sock);
         final String msgString = toXml(msg);
         log.info("Writing XMPP message: {}", msgString);
         final OutputStream os = sock.getOutputStream();
         log.info("Writing message to output stream: {}", os);
         os.write(msgString.getBytes("UTF-8"));
         os.flush();
     }
 
     private String toXml(final Message msg) {
         return msg.toXML() + "\n";
     }
 }
