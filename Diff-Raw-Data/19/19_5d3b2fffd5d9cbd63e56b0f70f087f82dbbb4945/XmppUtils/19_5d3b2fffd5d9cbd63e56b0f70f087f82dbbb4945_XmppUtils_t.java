 package org.littleshoot.commom.xmpp;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Map;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import javax.net.SocketFactory;
 import javax.security.auth.login.CredentialException;
 import javax.xml.xpath.XPathExpressionException;
 
 import org.apache.commons.lang.StringUtils;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionConfiguration.SecurityMode;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.PacketCollector;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketIDFilter;
 import org.jivesoftware.smack.packet.IQ;
 import org.jivesoftware.smack.packet.IQ.Type;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.RosterPacket;
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.provider.ProviderManager;
 import org.jivesoftware.smackx.packet.VCard;
 import org.jivesoftware.smackx.provider.VCardProvider;
 import org.lastbamboo.common.p2p.P2PConstants;
 import org.littleshoot.dnssec4j.VerifiedAddressFactory;
 import org.littleshoot.util.xml.XPathUtils;
 import org.littleshoot.util.xml.XmlUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xmlpull.v1.XmlPullParser;
 
 public class XmppUtils {
     
     private static final Logger LOG = LoggerFactory.getLogger(XmppUtils.class);
     private static ConnectionConfiguration globalConfig;
     
     private XmppUtils() {}
     
     static {
         
         ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp",
                 new VCardProvider());
         //ProviderManager.getInstance().addIQProvider(
         //    "query", "google:shared-status", new GenericIQProvider());
         
         ProviderManager.getInstance().addIQProvider(
                 "query", "google:shared-status", new GenericIQProvider() {
                     
                     @Override
                     public IQ parseIQ(final XmlPullParser parser) throws Exception {
                         //System.out.println("GOT PULL PARSER: "+parser);
                         return super.parseIQ(parser);
                     }
                 });
         ProviderManager.getInstance().addIQProvider(
             "query", "google:nosave", new GenericIQProvider());
         ProviderManager.getInstance().addIQProvider(
             "query", "http://jabber.org/protocol/disco#info", 
             new GenericIQProvider());
         ProviderManager.getInstance().addIQProvider(
             "query", "google:jingleinfo", new GenericIQProvider());
         /*
         ProviderManager.getInstance().addIQProvider(
             "query", "jabber:iq:roster", new GenericIQProvider() {
                 
                 @Override
                 public IQ parseIQ(final XmlPullParser parser) throws Exception {
                     System.out.println("GOT PULL PARSER: "+parser);
                     return super.parseIQ(parser);
                 }
             });
         ProviderManager.getInstance().addIQProvider(
             "query", "google:roster", new GenericIQProvider() {
                 
                 @Override
                 public IQ parseIQ(final XmlPullParser parser) throws Exception {
                     System.out.println("GOT GOOGLE ROSTER PULL PARSER: "+parser);
                     return super.parseIQ(parser);
                 }
             });
             */
     }
 
     /**
      * Extracts STUN servers from a response from Google Talk containing
      * those servers.
      * 
      * @param xml The XML with server data.
      * @return The servers.
      */
     public static Collection<InetSocketAddress> extractStunServers(
         final String xml) {
         LOG.info("Processing XML: {}", xml);
         final Collection<InetSocketAddress> servers = 
             new ArrayList<InetSocketAddress>(12);
         final Document doc;
         try {
             doc = XmlUtils.toDoc(xml);
         } catch (final IOException e) {
             LOG.warn("Could not lookup Google STUN servers");
             return Collections.emptyList();
         } catch (final SAXException e) {
             LOG.warn("Could not lookup Google STUN servers");
             return Collections.emptyList();
         }
         final XPathUtils xpath = XPathUtils.newXPath(doc);
         final String str = "/iq/query/stun/server";
         try {
             final NodeList nodes = xpath.getNodes(str);
             for (int i = 0; i < nodes.getLength(); i++) {
                 final Node node = nodes.item(i);
                 
                 final NamedNodeMap nnm = node.getAttributes();
                 final Node hostNode = nnm.getNamedItem("host");
                 final Node portNode = nnm.getNamedItem("udp");
                 if (hostNode == null || portNode == null) {
                     continue;
                 }
                 final String host = hostNode.getNodeValue();
                 final String port = portNode.getNodeValue();
                 if (StringUtils.isBlank(host) || StringUtils.isBlank(port)) {
                     continue;
                 }
                 servers.add(new InetSocketAddress(host,Integer.parseInt(port)));
             }
             LOG.info("Returning servers...");
             return servers;
         } catch (final XPathExpressionException e) {
             LOG.error("XPath error", e);
             throw new Error("Tested XPath no longer working: "+str, e);
         }
     }
     
     public static String extractSdp(final Document doc) {
         return extractXmppProperty(doc, P2PConstants.SDP);
     }
 
     //public static String extractKey(final Document doc) {
     //    return extractXmppProperty(doc, P2PConstants.SECRET_KEY);
     //}
     
 
     public static long extractTransactionId(final Document doc) {
         final String id = extractXmppProperty(doc, P2PConstants.TRANSACTION_ID);
         return Long.parseLong(id);
     }
     
 
     public static String extractFrom(final Document doc) {
         final String xml = XmlUtils.toString(doc);
         LOG.info("Got an XMPP message: {}", xml);
         final XPathUtils xpath = XPathUtils.newXPath(doc);
         final String str = "/message/From";
         try {
             return xpath.getString(str);
         } catch (final XPathExpressionException e) {
             throw new Error("Tested XPath no longer working: "+str, e);
         }
     }
 
     private static String extractXmppProperty(final Document doc, 
         final String name) {
         //final String xml = XmlUtils.toString(doc);
         //LOG.info("Got an XMPP message: {}", xml);
         final XPathUtils xpath = XPathUtils.newXPath(doc);
         final String str = 
             "/message/properties/property[name='"+name+"']/value";
         try {
             return xpath.getString(str);
         } catch (final XPathExpressionException e) {
             throw new Error("Tested XPath no longer working: "+str, e);
         }
     }
 
     public static void printMessage(final Packet msg) {
         LOG.info(toString(msg));
     }
 
     public static String toString(final Packet msg) {
         final XMPPError error = msg.getError();
         final StringBuilder sb = new StringBuilder();
         sb.append("\nMESSAGE: ");
         sb.append("\nBODY: ");
         if (msg instanceof Message) {
             sb.append(((Message)msg).getBody());
         }
         sb.append("\nFROM: ");
         sb.append(msg.getFrom());
         sb.append("\nTO: ");
         sb.append(msg.getTo());
         sb.append("\nSUBJECT: ");
         if (msg instanceof Message) {
             sb.append(((Message)msg).getSubject());
         }
         sb.append("\nPACKET ID: ");
         sb.append(msg.getPacketID());
         
         sb.append("\nERROR: ");
         if (error != null) {
             sb.append(error);
             sb.append("\nCODE: ");
             sb.append(error.getCode());
             sb.append("\nMESSAGE: ");
             sb.append(error.getMessage());
             sb.append("\nCONDITION: ");
             sb.append(error.getCondition());
             sb.append("\nEXTENSIONS: ");
             sb.append(error.getExtensions());
             sb.append("\nTYPE: ");
             sb.append(error.getType());
         }
         sb.append("\nEXTENSIONS: ");
         sb.append(msg.getExtensions());
         sb.append("\nTYPE: ");
         if (msg instanceof Message) {
             sb.append(((Message)msg).getType());
         }
         sb.append("\nPROPERTY NAMES: ");
         sb.append(msg.getPropertyNames());
         return sb.toString();
     }
     
 
     private static final Map<String, XMPPConnection> xmppConnections = 
         new ConcurrentHashMap<String, XMPPConnection>();
 
     static XMPPConnection persistentXmppConnection(final String username, 
             final String password, final String id) throws IOException, 
             CredentialException {
         return persistentXmppConnection(username, password, id, 4);
     }
     
     public static XMPPConnection persistentXmppConnection(final String username, 
         final String password, final String id, final int attempts) 
         throws IOException, CredentialException {
         return persistentXmppConnection(username, password, id, attempts, 
             "talk.google.com", 5222, "gmail.com", null);
     }
     
     public static XMPPConnection persistentXmppConnection(final String username, 
         final String password, final String id, final int attempts,
         final String host, final int port, final String serviceName,
         final XmppP2PClient clientListener) 
             throws IOException, CredentialException {
         final String key = username+password;
         if (xmppConnections.containsKey(key)) {
             final XMPPConnection conn = xmppConnections.get(key);
             if (conn.isAuthenticated() && conn.isConnected()) {
                 LOG.info("Returning existing xmpp connection");
                 return conn;
             } else {
                 LOG.info("Removing stale connection");
                 xmppConnections.remove(key);
             }
         }
         XMPPException exc = null;
         for (int i = 0; i < attempts; i++) {
             try {
                 LOG.info("Attempting XMPP connection...");
                 final XMPPConnection conn = 
                     singleXmppConnection(username, password, id, host, port, 
                         serviceName, clientListener);
                 
                 LOG.info("Created offerer");
                 xmppConnections.put(key, conn);
                 return conn;
             } catch (final XMPPException e) {
                 final String msg = "Error creating XMPP connection";
                 LOG.error(msg, e);
                 exc = e;    
             } 
             
             // Gradual backoff.
             try {
                 Thread.sleep(i * 200);
             } catch (final InterruptedException e) {
                 LOG.info("Interrupted?", e);
             }
         }
         if (exc != null) {
             throw new IOException("Could not log in!!", exc);
         }
         else {
             throw new IOException("Could not log in?");
         }
     }
     
     private static InetAddress getHost(final String host) throws IOException {
         return VerifiedAddressFactory.newVerifiedInetAddress(host, 
             XmppConfig.isUseDnsSec());
     }
     
     public static void setGlobalConfig(final ConnectionConfiguration config) {
         XmppUtils.globalConfig = config;
     }
     
     public static ConnectionConfiguration getGlobalConfig() {
         return XmppUtils.globalConfig;
     }
     
     private static ExecutorService connectors = Executors.newCachedThreadPool(
         new ThreadFactory() {
             private int count = 0;
             @Override
             public Thread newThread(Runnable r) {
                 final Thread t = new Thread(r, "XMPP-Connecting-Thread-"+count);
                 t.setDaemon(true);
                 count++;
                 return t;
             }
         });
     
     
     public static XMPPConnection simpleGoogleTalkConnection(
         final String username, final String password, final String id) 
         throws CredentialException, XMPPException, IOException {
         
         return singleXmppConnection(username, password, id, "talk.google.com", 
             5222, "gmail.com", null);
     }
     
     private static XMPPConnection singleXmppConnection(final String username, 
         final String password, final String id, final String xmppServerHost, 
         final int xmppServerPort, final String xmppServiceName, 
         final XmppP2PClient clientListener) throws XMPPException, IOException, 
         CredentialException {
         
         final InetAddress server = getHost(xmppServerHost);
         final ConnectionConfiguration config;
         if (getGlobalConfig() != null) {
             config = getGlobalConfig();
         } else {
             config = newConfig(server, xmppServerPort, xmppServiceName);
         }
         
         final Future<XMPPConnection> fut = 
             connectors.submit(new Callable<XMPPConnection>() {
             @Override
             public XMPPConnection call() throws Exception {
                 return newConnection(username, password, config, id, clientListener);
             }
         });
         try {
             final XMPPConnection conn = fut.get(40, TimeUnit.SECONDS);
             // Make sure we signify gchat support.
             XmppUtils.getSharedStatus(conn);
             return conn;
         } catch (final InterruptedException e) {
             throw new IOException("Interrupted during login!!", e);
         } catch (final ExecutionException e) {
             final Throwable t = e.getCause();
             if (t instanceof XMPPException) {
                 throw (XMPPException)t;
             } else if (t instanceof IOException) {
                 throw (IOException)t;
             } else if (t instanceof CredentialException) {
                 throw (CredentialException)t;
             } else {
                 throw new IllegalStateException ("Unrecognized cause", t);
             }
         } catch (final TimeoutException e) {
             throw new IOException("Took too long to login!!", e);
         }
     }
 
    /**
     * Creates a generic XMPP configuration. Note that in practice callers
     * will typically include their own global configuration for things
     * like trust managers, and this method will NOT BE CALLED.
     */
     private static ConnectionConfiguration newConfig(final InetAddress server,
         final int xmppServerPort, final String xmppServiceName) {
         final ConnectionConfiguration config = 
             new ConnectionConfiguration(server.getHostAddress(), 
                 xmppServerPort, xmppServiceName);
         config.setExpiredCertificatesCheckEnabled(true);
         config.setNotMatchingDomainCheckEnabled(true);
         config.setSendPresence(false);
         
         config.setCompressionEnabled(true);
         
         //config.setRosterLoadedAtLogin(true);
         config.setReconnectionAllowed(false);
         
         config.setVerifyChainEnabled(true);

        config.setVerifyRootCAEnabled(true);
         config.setSelfSignedCertificateEnabled(false);
         
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
                 LOG.info("Creating socket");
                 final Socket sock = new Socket();
                 sock.connect(new InetSocketAddress(host, port), 40000);
                 LOG.info("Socket connected");
                 return sock;
             }
             
             @Override
             public Socket createSocket(final String host, final int port) 
                 throws IOException, UnknownHostException {
                 LOG.info("Creating socket");
                 return createSocket(InetAddress.getByName(host), port);
             }
         });
         return config;
     }
 
     private static XMPPConnection newConnection(final String username, 
         final String password, final ConnectionConfiguration config,
         final String id, final XmppP2PClient clientListener) 
         throws XMPPException, CredentialException {
         config.setSecurityMode(SecurityMode.required);
         //config.setSecurityMode(SecurityMode.disabled);
         final XMPPConnection conn = new XMPPConnection(config);
         conn.connect();
         conn.addConnectionListener(new ConnectionListener() {
             
             @Override
             public void reconnectionSuccessful() {
                 LOG.info("Reconnection successful...");
             }
             
             @Override
             public void reconnectionFailed(final Exception e) {
                 LOG.info("Reconnection failed", e);
             }
             
             @Override
             public void reconnectingIn(final int time) {
                 LOG.info("Reconnecting to XMPP server in "+time);
             }
             
             @Override
             public void connectionClosedOnError(final Exception e) {
                 LOG.warn("XMPP connection closed on error", e);
                 handleClose();
             }
             
             @Override
             public void connectionClosed() {
                 LOG.info("XMPP connection closed. Creating new connection.");
                 handleClose();
             }
             
             private void handleClose() {
                 if (clientListener != null) {
                     clientListener.handleClose();
                 }
             }
         });
         
         LOG.info("Connection is Secure: {}", conn.isSecureConnection());
         LOG.info("Connection is TLS: {}", conn.isUsingTLS());
 
         try {
             conn.login(username, password, id);
         } catch (final XMPPException e) {
             //conn.disconnect();
             final String msg = e.getMessage();
             if (msg != null && msg.contains("No response from the server")) {
                 // This isn't necessarily a credentials issue -- try to catch
                 // non-credentials issues whenever we can.
                 throw e;
             }
             LOG.info("Credentials error!", e);
             throw new CredentialException("Authentication error");
         }
         
         while (!conn.isAuthenticated()) {
             LOG.info("Waiting for authentication");
             try {
                 Thread.sleep(80);
             } catch (final InterruptedException e1) {
                 LOG.error("Exception during sleep?", e1);
             }
         }
         
         return conn;
     }
     
     public static String jidToUser(final String jid) {
         return StringUtils.substringBefore(jid, "/");
     }
     
     public static String jidToUser(final XMPPConnection conn) {
         return jidToUser(conn.getUser());
     }
     
     public static VCard getVCard(final XMPPConnection conn, 
         final String emailId) throws XMPPException {
         final VCard card = new VCard();
         card.load(conn, emailId);
         return card;
     }
     
     //// The following includes a whole bunch of custom Google Talk XMPP 
     //// messages.
     
     public static Packet goOffTheRecord(final String jidToOtr, 
         final XMPPConnection conn) {
         LOG.info("Activating OTR for {}...", jidToOtr);
         final String query =
             "<query xmlns='google:nosave'>"+
                 "<item xmlns='google:nosave' jid='"+jidToOtr+"' value='enabled'/>"+
              "</query>";
         return setGTalkProperty(conn, query);
     }
     
     public static Packet goOnTheRecord(final String jidToOtr, 
         final XMPPConnection conn) {
         LOG.info("Activating OTR for {}...", jidToOtr);
         final String query =
             "<query xmlns='google:nosave'>"+
                 "<item xmlns='google:nosave' jid='"+jidToOtr+"' value='disabled'/>"+
              "</query>";
         return setGTalkProperty(conn, query);
     }
 
     public static Packet getOtr(final XMPPConnection conn) {
         LOG.info("Getting OTR status...");
         return getGTalkProperty(conn, "<query xmlns='google:nosave'/>");
     }
     
     public static Packet getSharedStatus(final XMPPConnection conn) {
         LOG.info("Getting shared status...");
         return getGTalkProperty(conn, 
             "<query xmlns='google:shared-status' version='2'/>");
     }
     
     public static RosterPacket extendedRoster(final XMPPConnection conn) {
         LOG.info("Requesting extended roster");
         final String query =
             "<query xmlns:gr='google:roster' gr:ext='2' xmlns='jabber:iq:roster'/>";
         return (RosterPacket) getGTalkProperty(conn, query);
     }
     
     public static Collection<InetSocketAddress> googleStunServers(
         final XMPPConnection conn) {
         LOG.info("Getting Google STUN servers...");
         final String xml = 
             getGTalkProperty(conn, "<query xmlns='google:jingleinfo'/>").toXML();
         return extractStunServers(xml);
     }
     
     public static Packet discoveryRequest(final XMPPConnection conn) {
         LOG.info("Sending discovery request...");
         return getGTalkProperty(conn, 
             "<query xmlns='http://jabber.org/protocol/disco#info'/>");
     }
     
     private static Packet setGTalkProperty(final XMPPConnection conn, 
         final String query) {
         return sendXmppMessage(conn, query, Type.SET);
     }
     
     private static Packet getGTalkProperty(final XMPPConnection conn, 
         final String query) {
         return sendXmppMessage(conn, query, Type.GET);
     }
     
     private static Packet sendXmppMessage(final XMPPConnection conn, 
         final String query, final Type iqType) {
         
         LOG.info("Sending XMPP stanza message...");
         final IQ iq = new IQ() {
             @Override
             public String getChildElementXML() {
                 return query;
             }
         };
         final String jid = conn.getUser();
         iq.setTo(jidToUser(jid));
         iq.setFrom(jid);
         iq.setType(iqType);
         final PacketCollector collector = conn.createPacketCollector(
             new PacketIDFilter(iq.getPacketID()));
         
         LOG.info("Sending XMPP stanza packet:\n"+iq.toXML());
         conn.sendPacket(iq);
         final Packet response = collector.nextResult(40000);
         return response;
     }
 
     /**
      * Note we don't even need to set this property to maintain compatibility
      * with Google Talk presence -- just sending them the shared status 
      * message signifies we generally understand the protocol and allows us
      * to not clobber other clients' presences.
      * @param conn
      * @param to
      */
     public static void setGoogleTalkInvisible(final XMPPConnection conn, 
         final String to) {
         final IQ iq = new IQ() {
             @Override
             public String getChildElementXML() {
                 return "<query xmlns='google:shared-status' version='2'><invisible value='true'/></query>";
             }
         };
         iq.setType(Type.SET);
         iq.setTo(to);
         LOG.info("Setting invisible with XML packet:\n"+iq.toXML());
         conn.sendPacket(iq);
     }
 }
