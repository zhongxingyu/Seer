 package org.littleshoot.commom.xmpp;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
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
 import org.apache.commons.lang.math.RandomUtils;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.filter.PacketTypeFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
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
 import org.littleshoot.util.PublicIp;
 import org.littleshoot.util.SessionSocketListener;
 import org.littleshoot.util.ThreadUtils;
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
     
     private final Map<Long, TransactionData> transactionIdsToProcessors =
         new ConcurrentHashMap<Long, TransactionData>();
     
     private static final Map<String, Socket> incomingControlSockets = 
         new ConcurrentHashMap<String, Socket>();
 
     private static final int TIMEOUT = 2 * 60 * 1000;
 
     private final OfferAnswerFactory offerAnswerFactory;
 
     private XMPPConnection xmppConnection;
     
     private final Collection<MessageListener> messageListeners =
         new ArrayList<MessageListener>();
 
     private final int relayWaitTime;
 
     private final String xmppServiceName;
 
     private final SessionSocketListener callSocketListener;
 
     private final InetSocketAddress plainTextRelayAddress;
     
     private final ExecutorService messageProcessingExecutor = 
         Executors.newCachedThreadPool();
     
     private final ExecutorService inviteProcessingExecutor = 
         Executors.newCachedThreadPool();
     
     private final Map<URI, Socket> outgoingControlSockets = 
         new ConcurrentHashMap<URI, Socket>();
 
     private final boolean useRelay;
     
     private final Set<String> sentMessageIds = new HashSet<String>();
     
     private final Map<URI, InetSocketAddress> urisToMappedServers =
         new ConcurrentHashMap<URI, InetSocketAddress>();
 
     private final PublicIp publicIp;
 
     private final String xmppServerHost;
 
     private final int xmppServerPort;
 
     private final SocketFactory socketFactory;
 
     
     public static ControlXmppP2PClient newGoogleTalkDirectClient(
         final OfferAnswerFactory factory,
         final InetSocketAddress plainTextRelayAddress, 
         final SessionSocketListener callSocketListener, final int relayWait,
         final PublicIp publicIp, final SocketFactory socketFactory) {
         return new ControlXmppP2PClient(factory, plainTextRelayAddress, 
             callSocketListener, relayWait, "talk.google.com", 5222, "gmail.com", 
             false, publicIp, socketFactory);
     }
 
     /*
     public static ControlXmppP2PClient newGoogleTalkClient(
         final OfferAnswerFactory factory,
         final InetSocketAddress plainTextRelayAddress, 
         final SessionSocketListener callSocketListener, final int relayWait,
         final PublicIp publicIp) {
         return new ControlXmppP2PClient(factory, plainTextRelayAddress, 
             callSocketListener, relayWait, "talk.google.com", 5222, "gmail.com", 
             true, publicIp);
     }
     */
 
     private ControlXmppP2PClient(final OfferAnswerFactory offerAnswerFactory,
         final InetSocketAddress plainTextRelayAddress,
         final SessionSocketListener callSocketListener,
         final int relayWaitTime, final String host, final int port, 
         final String serviceName, final boolean useRelay,
         final PublicIp publicIp, final SocketFactory socketFactory) {
         this.offerAnswerFactory = offerAnswerFactory;
         this.plainTextRelayAddress = plainTextRelayAddress;
         this.callSocketListener = callSocketListener;
         this.relayWaitTime = relayWaitTime;
         this.xmppServerHost = host;
         this.xmppServerPort = port;
         this.xmppServiceName = serviceName;
         this.useRelay = useRelay;
         this.publicIp = publicIp;
         this.socketFactory = socketFactory;
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
         
         // If the remote host has their ports mapped, we just use those.
         if (streamDesc.isTcp() && urisToMappedServers.containsKey(uri)) {
             log.info("USING MAPPED PORT SERVER!");
             return newMappedServerSocket(uri, raw);
         }
         
         final Socket control = controlSocket(uri, streamDesc);
         
         if (streamDesc.isTcp() && urisToMappedServers.containsKey(uri)) {
             log.info("USING MAPPED PORT SERVER AFTER CONTROL!");
             // No reason to keep the control socket around if we have the
             // mapped port. Note we do go through with creating the control in
             // any case to avoid getting into weird states with socket 
             // negotiation on both the local and the remote sides.
             IOUtils.closeQuietly(control);
             return newMappedServerSocket(uri, raw);
         }
 
         // Note we use a short timeout for waiting for answers. This is 
         // because we've seen XMPP messages get lost in the ether, and we 
         // just want to send a few of them quickly when this does happen.
         final DefaultTcpUdpSocket tcpUdpSocket = 
             new DefaultTcpUdpSocket(
                 new OffererOverControlSocket(control, streamDesc), 
                 this.offerAnswerFactory,
                 this.relayWaitTime, 20 * 1000, streamDesc);
         
         log.info("Trying to create new socket...raw="+raw);
         final Socket sock = tcpUdpSocket.newSocket(uri);
         if (raw) {
             log.info("Returning raw socket");
             return sock;
         }
         final byte[] writeKey = tcpUdpSocket.getWriteKey();
         final byte[] readKey = tcpUdpSocket.getReadKey();
         log.info("Creating new CipherSocket with write key {} and read key {}", 
             writeKey, readKey);
         return new CipherSocket(sock, writeKey, readKey);
     }
     
     private Socket newMappedServerSocket(final URI uri, final boolean raw) 
         throws IOException {
         final InetSocketAddress serverIp = urisToMappedServers.get(uri);
         final Socket sock;
         if (raw) {
             log.info("Creating raw socket and skipping socket factory");
             sock = new Socket();
         } else {
             log.info("Using socket factory: {}", this.socketFactory);
             sock = this.socketFactory.createSocket();
         }
         try {
             sock.connect(serverIp, 30 * 1000);
             return sock;
         } catch (final IOException e) {
             log.info("Could not connect -- peer offline?", e);
             urisToMappedServers.remove(uri);
             throw e;
         }
     }
 
     private Socket controlSocket(final URI uri, 
         final IceMediaStreamDesc streamDesc) throws IOException, 
         NoAnswerException {
         // We want to synchronized on the control sockets and block new 
         // incoming sockets because it's pointless for them to do much before
         // the control socket is established, since that's how they'll connect
         // themselves.
         synchronized (this.outgoingControlSockets) {
             if (!this.outgoingControlSockets.containsKey(uri)) {
                 log.info("Creating new control socket");
                 final Socket control = establishControlSocket(uri, streamDesc);
                 this.outgoingControlSockets.put(uri, control);
                 return control;
             } else {
                 log.info("Using existing control socket");
                 final Socket control = this.outgoingControlSockets.get(uri);
                 if (!control.isClosed()) {
                     return control;
                 }
                 
                 final Socket newControl = 
                     establishControlSocket(uri, streamDesc);
                 this.outgoingControlSockets.put(uri, newControl);
                 return newControl;
             }
         }
     }
 
     private Socket establishControlSocket(final URI uri, 
         final IceMediaStreamDesc streamDesc) throws IOException, 
         NoAnswerException {
         final DefaultTcpUdpSocket tcpUdpSocket = 
             new DefaultTcpUdpSocket(this, this.offerAnswerFactory,
                 this.relayWaitTime, 30 * 1000, streamDesc);
         
         final Socket sock = tcpUdpSocket.newSocket(uri);
         sock.setSoTimeout(TIMEOUT);
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
         final Message offerMessage = 
             newInviteToEstablishControlSocket(jid, offer, transactionListener, 
                 keyStorage);
         xmppConnection.sendPacket(offerMessage);
     }
     
     private Message newInviteToEstablishControlSocket(final String jid, 
         final byte[] offer, 
         final OfferAnswerTransactionListener transactionListener,
         final KeyStorage keyStorage) {
         final long id = RandomUtils.nextLong();
         transactionIdsToProcessors.put(id, 
             new TransactionData(transactionListener, keyStorage));
         //transactionIdsToProcessors.put(id, td);
         final Message msg = new Message();
         msg.setTo(jid);
         log.info("Sending offer: {}", new String(offer));
         final String base64Sdp = 
             Base64.encodeBase64URLSafeString(offer);
         msg.setProperty(P2PConstants.TRANSACTION_ID, id);
         msg.setProperty(P2PConstants.MESSAGE_TYPE, P2PConstants.INVITE);
         msg.setProperty(P2PConstants.SDP, base64Sdp);
         msg.setProperty(P2PConstants.CONTROL, "true");
         if (keyStorage != null) {
             final byte[] writeKey = keyStorage.getWriteKey();
             if (writeKey == null) {
                 log.error("Null write key!!!");
                 throw new IllegalArgumentException("Null write key!!");
             } else {
                 msg.setProperty(P2PConstants.SECRET_KEY, 
                     Base64.encodeBase64String(writeKey));
             }
         } else {
             log.error("Null key storage?"+ThreadUtils.dumpStack());
         }
         return msg;
     }
     
     private void processMessages() {
         final PacketFilter filter = new PacketTypeFilter(Message.class);
         final PacketListener myListener = new PacketListener() {
             @Override
             public void processPacket(final Packet packet) {
                 final Message msg = (Message) packet;
                 final String id = msg.getPacketID();
                 log.info("Checking message ID: {}", id);
                 if (sentMessageIds.contains(id)) {
                     log.warn("Message is from us!!");
                     
                     // This is a little silly in that we're sending a 
                     // message back to ourselves, but this signals to the 
                     // client thread right away that the invite has failed.
                     final Message error = newError(msg);
                     xmppConnection.sendPacket(error);
                 } else {
                     messageProcessingExecutor.execute(
                         new PacketProcessor(msg));
                 }
             }
         };
         // Register the listener.
         this.xmppConnection.addPacketListener(myListener, filter);
     }
     
     protected Message newError(final Message msg) {
         return newError(msg.getFrom(), 
             (Long)msg.getProperty(P2PConstants.TRANSACTION_ID));
     }
     
     protected Message newError(final String from, final Long tid) {
         final Message error = new Message();
         error.setProperty(P2PConstants.MESSAGE_TYPE, 
             P2PConstants.INVITE_ERROR);
         if (tid != null) {
             error.setProperty(P2PConstants.TRANSACTION_ID, tid);
         }
         error.setTo(from);
         return error;
     }
 
     /**
      * This processes an INVITE to establish a control socket.
      * 
      * @param msg The INVITE message received from the XMPP server to establish
      * the control socket.
      */
     private void processControlInvite(final Message msg) {
         //final String readString = 
         //    (String) msg.getProperty(P2PConstants.SECRET_KEY);
         //final byte[] readKey = Base64.decodeBase64(readString);
         final String sdp = (String) msg.getProperty(P2PConstants.SDP);
         final ByteBuffer offer = ByteBuffer.wrap(Base64.decodeBase64(sdp));
         final String offerString = MinaUtils.toAsciiString(offer);
         log.info("Processing offer: {}", offerString);
         
         final OfferAnswer offerAnswer;
         try {
             offerAnswer = this.offerAnswerFactory.createAnswerer(
                 new ControlSocketOfferAnswerListener(msg.getFrom()), false);
         }
         catch (final OfferAnswerConnectException e) {
             // This indicates we could not establish the necessary connections 
             // for generating our candidates.
             log.warn("We could not create candidates for offer: " + sdp, e);
             
             final Message error = newError(msg);
             xmppConnection.sendPacket(error);
             return;
         }
         final byte[] answer = offerAnswer.generateAnswer();
         final long tid = (Long) msg.getProperty(P2PConstants.TRANSACTION_ID);
         
         // TODO: This is a throwaway key here since the control socket is not
         // encrypted as of this writing.
         final Message inviteOk = 
             newInviteOk(tid, answer, CommonUtils.generateKey());
         inviteOk.setTo(msg.getFrom());
         log.info("Sending CONTROL INVITE OK to {}", inviteOk.getTo());
         xmppConnection.sendPacket(inviteOk);
 
         offerAnswer.processOffer(offer);
         log.debug("Done processing CONTROL XMPP INVITE!!!");
     }
     
     private Message newInviteOk(final Long tid, final byte[] answer, 
         final byte[] answerKey) {
         final Message inviteOk = new Message();
         if (tid != null) {
             inviteOk.setProperty(P2PConstants.TRANSACTION_ID, tid.longValue());
         }
         inviteOk.setProperty(P2PConstants.MESSAGE_TYPE, P2PConstants.INVITE_OK);
         inviteOk.setProperty(P2PConstants.SDP, 
             Base64.encodeBase64String(answer));
         if (answerKey != null) {
             inviteOk.setProperty(P2PConstants.SECRET_KEY, 
                 Base64.encodeBase64String(answerKey));
         }
         
         if (this.offerAnswerFactory.isAnswererPortMapped()) {
             inviteOk.setProperty(P2PConstants.MAPPED_PORT, 
                 this.offerAnswerFactory.getMappedPort());
             inviteOk.setProperty(P2PConstants.PUBLIC_IP, 
                 this.publicIp.getPublicIpAddress().getHostAddress());
         }
         return inviteOk;
     }
     
 
     private final class TransactionData {
 
         private final OfferAnswerTransactionListener transactionListener;
         private final KeyStorage keyStorage;
 
         private TransactionData(
             final OfferAnswerTransactionListener transactionListener,
             final KeyStorage keyStorage) {
             this.transactionListener = transactionListener;
             this.keyStorage = keyStorage;
         }
         
     }
     
     /**
      * Runnable for processing incoming packets. These will can be Presence 
      * packets, info packets from the controller, INVITEs, INVITE OKs, etc.
      */
     private final class PacketProcessor implements Runnable {
 
         private final Message msg;
 
         private PacketProcessor(final Message msg) {
             this.msg = msg;
         }
         
         @Override
         public void run() {
             log.info("Got message from {}", msg.getFrom());
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
                     processControlInvite(msg);
                     break;
                 case P2PConstants.INVITE_OK:
                     // We just pass these along to the other listener -- 
                     // sometimes this listener can get notified first for
                     // whatever reason.
                     log.info("Got INVITE_OK");
                     final TransactionData okTd = toTransactionData();
                     if (okTd == null) {
                         log.error("No matching transaction ID?");
                     } else {
                         log.info("Got transaction data!!");
                         final OfferAnswerMessage oam = toOfferAnswerMessage(okTd);
                         addMappedServer();
                         okTd.transactionListener.onTransactionSucceeded(oam);
                     }
                     break;
                 case P2PConstants.INVITE_ERROR:
                     // This can happen when a message is in fact from us, and
                     // we send an error message to ourselves, for example. 
                     // We'll see messages from us when trying to send them to
                     // non-existent peers, for example.
                     log.info("Got INVITE_ERROR - transaction failed");
                     final TransactionData eTd = toTransactionData();
                     if (eTd == null) {
                         log.error("No matching transaction ID?");
                     } else {
                         final OfferAnswerMessage oam = toOfferAnswerMessage(eTd);
                         eTd.transactionListener.onTransactionFailed(oam);
                     }
                     break;
                 default:
                     log.info("Non-standard message on aswerer..." +
                         "sending to additional listeners, if any: "+ mt);
                     notifyListeners();
                     break;
             }
         }
 
         private TransactionData toTransactionData() {
             final Long id = 
                 (Long) msg.getProperty(P2PConstants.TRANSACTION_ID);
             return transactionIdsToProcessors.remove(id);
         }
 
         private OfferAnswerMessage toOfferAnswerMessage(
             final TransactionData td) {
             final byte[] body = CommonUtils.decodeBase64(
                 (String) msg.getProperty(P2PConstants.SDP));
             final byte[] key = CommonUtils.decodeBase64(
                 (String) msg.getProperty(P2PConstants.SECRET_KEY));
             td.keyStorage.setReadKey(key);
             return new OfferAnswerMessage() {
                 @Override
                 public String getTransactionKey() {
                     return String.valueOf(hashCode());
                 }
                 @Override
                 public ByteBuffer getBody() {
                     return ByteBuffer.wrap(body);
                 }
             };
         }
 
         private boolean addMappedServer() {
             final String ip = (String) msg.getProperty(P2PConstants.PUBLIC_IP);
             log.info("Got public IP address: {}", ip);
             if (StringUtils.isNotBlank(ip)) {
                 final Integer port = 
                     (Integer) msg.getProperty(P2PConstants.MAPPED_PORT);
                 if (port != null) {
                     final InetSocketAddress mapped =
                         new InetSocketAddress(ip, port);
                     log.info("ADDING MAPPED SERVER PORT!!");
                     try {
                         urisToMappedServers.put(new URI(msg.getFrom()), mapped);
                     } catch (final URISyntaxException e) {
                         log.error("Bad URI?", msg.getFrom());
                     }
                     return true;
                 } 
             }
             return false;
         }
         
         private void notifyListeners() {
             log.info("Notifying global listeners");
             synchronized (messageListeners) {
                 if (messageListeners.isEmpty()) {
                     log.info("No message listeners to forward to");
                 }
                 for (final MessageListener ml : messageListeners) {
                     ml.processMessage(null, msg);
                 }
             }
         }
         
         @Override
         public String toString() {
             return "INVITE Runner for Chat with: "+msg.getFrom();
         }
     }
     
     /**
      * This class sends offers over an established control socket.
      */
     private class OffererOverControlSocket implements Offerer {
 
         private Socket control;
         private final IceMediaStreamDesc streamDesc;
 
         private OffererOverControlSocket(final Socket control, 
             final IceMediaStreamDesc streamDesc) {
             this.control = control;
             this.streamDesc = streamDesc;
         }
 
         @Override
         public void offer(final URI uri, final byte[] offer,
             final OfferAnswerTransactionListener transactionListener,
             final KeyStorage keyStore) {
             log.info("Sending message from local address: {}", 
                 this.control.getLocalSocketAddress());
             synchronized (this.control) {
                 log.info("Got lock on control socket...");
                 final Message msg = 
                     newInviteOverControlSocket(uri.toASCIIString(), offer, keyStore);
                 final String xml = toXml(msg);
                 log.info("Writing XML offer on control socket: {}", xml);
                 
                 // We just block on a single offer and answer.
                 
                 // We also need to catch IOExceptions here for when the control
                 // socket is broken for some reason.
                 try {
                     writeToControlSocket(xml);
                 } catch (final IOException e) {
                     log.info("Control socket timed out? We'll try to " +
                         "establish a new one", e);
                     try {
                         this.control = establishControlSocket(uri, streamDesc);
                         writeToControlSocket(xml);
                     } catch (final IOException ioe) {
                         log.warn("Still could not establish or write to " +
                             "new control socket", ioe);
                         return;
                     } catch (final NoAnswerException nae) {
                         log.warn("Still could not establish or write to " +
                             "new control socket", nae);
                         return;
                     }
                 }
                 
                 
                 try {
                     final InputStream is = this.control.getInputStream();
                     log.info("Reading incoming answer on control socket");
                     final Document doc = XmlUtils.toDoc(is, "</message>");
                     final String received = XmlUtils.toString(doc);
                     log.info("Got INVITE OK on CONTROL socket: {}", received);
                     
                     // We need to extract the SDP to establish the new socket.
                     final String sdp = XmppUtils.extractSdp(doc);
                     final byte[] sdpBytes = Base64.decodeBase64(sdp); 
                     
                     final OfferAnswerMessage message = new OfferAnswerMessage(){
                         @Override
                         public String getTransactionKey() {
                             return String.valueOf(hashCode());
                         }
                         @Override
                         public ByteBuffer getBody() {
                             return ByteBuffer.wrap(sdpBytes);
                         }
                     };
                     final String from = XmppUtils.extractFrom(doc);
                     final String encodedKey = XmppUtils.extractKey(doc);
                     final byte[] key = CommonUtils.decodeBase64(encodedKey);
                     keyStore.setReadKey(key);
                     //final Long tid = XmppUtils.extractTransactionId(doc);
                     log.info("Got INVITE OK establishing new socket over " +
                         "control socket...from: "+from+" read key: "+key);
                     
                     log.info("Calling transaction succeeded on listener: {}", 
                         transactionListener);
                     transactionListener.onTransactionSucceeded(message);
                 } catch (final SAXException e) {
                     log.warn("Could not parse INVITE OK", e);
                     // Close the socket?
                     IOUtils.closeQuietly(this.control);
                 } catch (final IOException e) {
                     log.warn("Exception handling control socket", e);
                 }
             }
         }
         
         private Message newInviteOverControlSocket(final String jid, 
             final byte[] offer, final KeyStorage keyStorage) {
             final Message msg = new Message();
             msg.setTo(jid);
             log.info("Sending offer: {}", new String(offer));
             final String base64Sdp = 
                 Base64.encodeBase64URLSafeString(offer);
             msg.setProperty(P2PConstants.MESSAGE_TYPE, P2PConstants.INVITE);
             msg.setProperty(P2PConstants.SDP, base64Sdp);
             msg.setProperty(P2PConstants.CONTROL, "true");
             if (keyStorage != null) {
                 final byte[] writeKey = keyStorage.getWriteKey();
                 if (writeKey == null) {
                     log.error("Null write key!!!");
                     throw new IllegalArgumentException("Null write key!!");
                 } else {
                     log.info("Setting client write key to: {}", writeKey);
                     msg.setProperty(P2PConstants.SECRET_KEY, 
                             Base64.encodeBase64String(writeKey));
                 }
             } else {
                 log.error("Null key storage?"+ThreadUtils.dumpStack());
             }
             return msg;
         }
         
         private void writeToControlSocket(final String xml) throws IOException {
             final OutputStream os = this.control.getOutputStream();
             os.write(xml.getBytes("UTF-8"));
             os.flush();
             log.info("Wrote message on control socket stream: {}", os);
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
             log.info("Got a UDP socket: {}", sock);
             onSocket(sock);
         }
     
         private void onSocket(final Socket sock) {
             log.info("Got control socket on 'server' side: {}", sock);
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
                 log.info("Trying to read next offer on control socket...");
                 final Document doc = XmlUtils.toDoc(is, "</message>");
                 log.info("Got XML INVITE: {}", XmlUtils.toString(doc));
                 
                 final String sdp = XmppUtils.extractSdp(doc);
                 final String from = XmppUtils.extractFrom(doc);
                 final String key = XmppUtils.extractKey(doc);
                 
                 final ByteBuffer offer = 
                     ByteBuffer.wrap(Base64.decodeBase64(sdp));
                 processInviteOverControlSocket(offer, sock, key, from);
             }
         }
     }
     
 
     /**
      * This processes an incoming offer received on the control socket after
      * the control socket has already been established.
      * 
      * @param tid The ID of the transaction.
      * @param offer The offer itself.
      * @param controlSocket The control socket.
      * @param readKey The key for decrypting incoming data.
      * @param from The user this is from.
      * @throws IOException If any IO error occurs, including normal socket
      * closings.
      */
     private void processInviteOverControlSocket(
         final ByteBuffer offer, final Socket controlSocket, 
         final String readKey, final String from) throws IOException {
         log.info("Processing offer...");
         if (StringUtils.isBlank(readKey)) {
             log.error("Null key?");
             throw new NullPointerException("Null key for new secure socket!!");
         }
         final String offerString = MinaUtils.toAsciiString(offer);
         
         final byte[] answerKey = CommonUtils.generateKey();
         final OfferAnswer offerAnswer;
        final byte[] key = CommonUtils.decodeBase64(readKey);
         log.info("Read key from client INVITE -- our read key: {}", key);
         
         try {
             offerAnswer = this.offerAnswerFactory.createAnswerer(
                 new AnswererOfferAnswerListener("", 
                     this.plainTextRelayAddress, callSocketListener, 
                     offerString, answerKey, key), this.useRelay);
         }
         catch (final OfferAnswerConnectException e) {
             // This indicates we could not establish the necessary connections 
             // for generating our candidates.
             log.warn("We could not create candidates for offer", e);
             error(from, null, controlSocket);
             return;
         }
         log.info("Creating answer");
         final byte[] answer = offerAnswer.generateAnswer();
         log.info("Creating INVITE OK");
         final Message inviteOk = newInviteOk(null, answer, answerKey);
         log.info("Writing INVITE OK");
         writeMessage(inviteOk, controlSocket);
         log.info("Wrote INVITE OK");
         
         inviteProcessingExecutor.submit(new Runnable() {
             @Override
             public void run() {
                 log.info("Passing offer processing to listener...");
                 offerAnswer.processOffer(offer);
             }
         });
         log.info("Done processing offer...");
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
                 processMessages();
                 //addChatManagerListener(this.xmppConnection);
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
 
     private XMPPConnection singleXmppConnection(final String username, 
         final String password, final String id) throws XMPPException {
         final ConnectionConfiguration config = 
             //new ConnectionConfiguration("talk.google.com", 5222, "gmail.com");
             new ConnectionConfiguration(this.xmppServerHost, 
                 this.xmppServerPort, this.xmppServiceName);
         config.setExpiredCertificatesCheckEnabled(true);
         config.setNotMatchingDomainCheckEnabled(true);
         config.setSendPresence(false);
         
         config.setCompressionEnabled(true);
         config.setRosterLoadedAtLogin(true);
         config.setReconnectionAllowed(false);
         
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
                 Thread.sleep(200);
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
     
     private void error(final String from, final Long tid, final Socket sock) {
         final Message error = newError(from, tid);
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
