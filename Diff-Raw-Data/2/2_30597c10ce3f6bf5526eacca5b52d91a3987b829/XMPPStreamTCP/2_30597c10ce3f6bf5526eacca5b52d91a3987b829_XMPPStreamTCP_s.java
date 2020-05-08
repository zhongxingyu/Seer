 /**
  * $RCSfile$
  * $Revision: 11616 $
  * $Date: 2010-02-09 07:40:11 -0500 (Tue, 09 Feb 2010) $
  *
  * Copyright 2003-2007 Jive Software.
  *
  * All rights reserved. Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.jivesoftware.smack;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.Proxy;
 import java.net.Socket;
 import java.security.cert.CertificateException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.net.ssl.SSLSocket;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jivesoftware.smack.packet.XMPPError;
 import org.jivesoftware.smack.proxy.SocketConnectorFactory.SocketConnector;
 import org.jivesoftware.smack.util.DNSUtil;
 import org.jivesoftware.smack.util.DNSUtil.HostAddress;
 import org.jivesoftware.smack.util.ObservableReader;
 import org.jivesoftware.smack.util.ObservableWriter;
 import org.jivesoftware.smack.util.ThreadUtil;
 import org.jivesoftware.smack.util.WriterListener;
 import org.jivesoftware.smack.util.XmlUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 /**
  * XMPP TCP transport, implementing TLS and compression. 
  */
 public class XMPPStreamTCP extends XMPPStream
 {
     private Socket socket = null; 
     private Reader reader = null;
     private Writer writer = null;
     
     private ObservableReader.ReadEvent readEvent;
     private ObservableWriter.WriteEvent writeEvent;
 
     private ObservableWriter.WriteEvent keepaliveMonitorWriteEvent;
 
     /** True if the connection is encrypted, whether or not the certificate is verified. */
     private boolean usingTLS = false;
 
     /** True if the connection is secure (encrypted with a verified certificate). */
     private boolean usingSecureConnection = false;
 
     /* True if XMPP compression is enabled.  If TLS compression is enabled, this is false. */
     private boolean usingXMPPCompression = false;
 
     /* True if TLS compression is enabled. */
     private boolean usingTLSCompression = false;
 
     private boolean threadExited = false;
 
     private final ReentrantLock lock = new ReentrantLock();
     private final Condition cond = lock.newCondition();
 
     private ConnectionConfiguration config;
     private String originalServiceName;
 
     private XmlPullParser parser;
     XMPPSSLSocketFactory sslSocketFactory;
 
     /** If true, the most recent <features/> advertised <starttls/>. */
     private boolean featureStartTLSReceived = false;
     
     /** The compression methods advertised in the most recent <features/>. */
     private List<String> featureCompressionMethods;
 
     /** Callbacks for stream events. */
     private PacketCallback callbacks;
 
     private void assertNotLocked() {
         if(lock.isHeldByCurrentThread())
             throw new RuntimeException("Lock should not be held");
     }
 
     private void assertLocked() {
         if(!lock.isHeldByCurrentThread())
             throw new RuntimeException("Lock should be held");
     }
 
     public void writePacket(String packet) throws XMPPException {
         assertNotLocked();
 
         // writer can be cleared by calls to disconnect.  We can't hold a lock
         // on XMPPStreamTCP while we use it, since it can block indefinitely.
         // Take a reference to writer.
         lock.lock();
         Writer writerCopy = this.writer;
         lock.unlock();
 
         if(writerCopy == null)
             throw new XMPPException("Wrote a packet while the connection was closed");
 
         try {
             synchronized(writerCopy) {
                 writerCopy.write(packet);
                 writerCopy.flush();
             }
         } catch(IOException e) {
             throw new XMPPException(e);
         }
     }
     public boolean isSecureConnection() { return usingSecureConnection; }
     public boolean isUsingCompression() { return usingXMPPCompression || usingTLSCompression; }
 
     String connectionID;
     public String getConnectionID() { return connectionID; }
 
     private PacketReaderThread packetReaderThread;
 
     public XMPPStreamTCP(ConnectionConfiguration config)
     {
         this.config = config;
         
         try {
             parser = XmlPullParserFactory.newInstance().newPullParser();
             parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
         }
         catch (XmlPullParserException xppe) {
             xppe.printStackTrace();
             throw new RuntimeException(xppe);
         }
 
         /* We update config.serviceName when we see the service name from the server,
          * but we need to retain the original value for TLS certificate checking. */
         originalServiceName = config.getServiceName();
         keepaliveMonitorWriteEvent = new ObservableWriter.WriteEvent();
     }
 
     public void setReadWriteEvents(ObservableReader.ReadEvent readEvent, ObservableWriter.WriteEvent writeEvent) {
         this.writeEvent = writeEvent;
         this.readEvent = readEvent;
     }
 
     /**
      * This class performs the initial SRV lookup.  This is only set during
      * initializeConnection while performing the lookup.  Other threads may access
      * this while locked in order to cancel the lookup, but must not clear it.
      */
     DNSUtil.CancellableLookup initialLookup;
     
     /**
      * This class performs the socket connection, and handles cancellation.  Like
      * {@link #initialLookup}, this is cancelled asynchronously by {@link #disconnect()}.
      */
     private SocketConnector socketConnector;
 
     class ConnectDataTCP extends ConnectData {
         Vector<DNSUtil.HostAddress> addresses;
 
         int connectionAttempts() {
             return addresses.size();
         }
     };
 
     public ConnectData getDefaultConnectData() {
         assertNotLocked();
 
         lock.lock();
         try {
             ConnectDataTCP data = new ConnectDataTCP();
             data.addresses = new Vector<DNSUtil.HostAddress>();
 
             String host = config.getHost();
             int port = config.getPort();
             if(host != null)
                 data.addresses.add(new HostAddress(host, port));
             else
                 data.addresses.add(new HostAddress(config.getServiceName(), port));
             return data;
         } finally {
             lock.unlock();
         }
     }
     
     public ConnectData getConnectData() throws XMPPException {
         assertNotLocked();
 
         if(socket != null)
             throw new RuntimeException("The connection has already been initialized");
 
         lock.lock();
         try {
             String host = config.getHost();
             int port = config.getPort();
 
             ConnectDataTCP data = new ConnectDataTCP();
             if(host != null)
                 return getDefaultConnectData();
 
             // If no host was specified, look up the XMPP service name.
             DNSUtil.XMPPDomainLookup lookup = new DNSUtil.XMPPDomainLookup(config.getServiceName(), true);
             initialLookup = lookup;
 
             // Unlock while we run the blocking lookup.  Other threads can call
             // initialLookup.cancel while we have it unlocked, which will cause run()
             // to cancel.  Other threads may not modify the value of initialLookup, so
             // it doesn't disappear while we're unlocked.
             lock.unlock();
             try {
                 data.addresses = lookup.run();
             } finally {
                 lock.lock();
             }
 
             initialLookup = null;
 
             if(data.addresses == null) {
                 // Set remote_server_not_found, so connectUsingConfiguration knows to stop trying
                 // this transport.  If we don't do this, it'll treat it as a per-server error and
                 // try again with a higher index.
                 throw new XMPPException("Connection cancelled", XMPPError.Condition.remote_server_not_found);
             }
 
             return data;
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Look up the specified hostname.  An asynchronous call to disconnect() will
      * abort the lookup.
      * <p>
      * @param host
      * @return
      * @throws XMPPException
      */
     private InetAddress lookupHostIP(String host) throws XMPPException {
         assertNotLocked();
 
         DNSUtil.AddressLookup lookup;
         lock.lock();
         try {
             // This is called before the socket is open, so we can't call throwIfDisconnected().
             // If threadExited is true, then disconnect() was called before we got this far.
             // Exit without starting.
             if(threadExited)
                 throw new XMPPException("Connection cancelled");
 
             lookup = new DNSUtil.AddressLookup(host);
             initialLookup = lookup;
         } finally {
             lock.unlock();
         }
 
         // Look up the host.
         Vector<InetAddress> ips = lookup.run();
 
         lock.lock();
         try {
             initialLookup = null;
 
             if(ips == null)
                 throw new XMPPException("Connection cancelled");
 
             if(ips.size() == 0)
                 throw new XMPPException("Couldn't resolve host: " + host);
 
             // Although the address might have multiple A records, we only try the first.  DNS-
             // based load balancing for XMPP should be done using SRV records, not A records.
             return ips.get(0);
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * Begin the initial connection to the server.  Returns when the connection
      * is established.
      */
     public void initializeConnection(ConnectData data, int attempt) throws XMPPException {
         if(!(data instanceof ConnectDataTCP))
             throw new IllegalArgumentException("data argument was not created with XMPPStreamTCP.getConnectData");
         ConnectDataTCP dataTCP = (ConnectDataTCP) data;
 
         assertNotLocked();
 
         if(socket != null)
             throw new RuntimeException("The connection has already been initialized");
         if(attempt >= dataTCP.addresses.size())
             throw new IllegalArgumentException();
 
         String host = dataTCP.addresses.get(attempt).getHost();
         int port = dataTCP.addresses.get(attempt).getPort();
 
         SetupPacketCallback setupCallbacks;
 
         lock.lock();
         try {
             // If threadExited is true, then disconnect() was called before we got this far.
             // Exit without starting.
             if(threadExited)
                 throw new XMPPException("Connection cancelled");
 
             try {
                 socket = new Socket(Proxy.NO_PROXY);
                 socketConnector = config.getProxyInfo().getSocketConnectorFactory().createConnector(socket);
                 
                 // Unlock while we connect to the server.  disconnect() may close the stream,
                 // cancelling the connection.
                 lock.unlock();
                 try {
                     socketConnector.connectSocket(host, port);
                 } finally {
                     lock.lock();
                     socketConnector = null;
                 }
 
                 initReaderAndWriter();
             } catch(IOException e) {
                 throw new XMPPException("Could not connect to " + host + ":" + port, e);
             }
 
             setupCallbacks = new SetupPacketCallback(host, port);
             this.callbacks = setupCallbacks;
 
             packetReaderThread = new PacketReaderThread();
             packetReaderThread.setName("XMPP packet reader (" + host + ":" + port + ")");
             packetReaderThread.start();
         } finally {
             lock.unlock();
         }
 
         // Send the initial stream header, starting the protocol.
         streamReset();
 
         try {
             // Wait for TLS and compression negotiation to complete.  If this returns successfully,
             // this.callbacks is updated to point to userCallbacks.
             setupCallbacks.waitForCompletion();
 
             // On successful completion, setupCallbacks cleared callbacks.  We're not locked,
             // but setPacketCallbacks must not be called until connect() returns.
             if(callbacks != null) 
                 throw new IllegalStateException("callbacks should be cleared");
 
             /* Start keepalives after TLS has been set up. */
             startKeepAliveProcess();
         } catch(XMPPException e) {
             disconnect();
             throw e;
         }
     }
 
     public void setPacketCallbacks(PacketCallback userCallbacks) {
         if(userCallbacks == null)
             throw new IllegalArgumentException("userCallbacks can not be nnull");
         
         Element packet;
 
         assertNotLocked();
         lock.lock();
         try {
             if(queuedPacket == null)
                 throw new IllegalStateException("Should have had a packet buffered, but didn't");
             packet = queuedPacket;
             queuedPacket = null;
         } finally {
             lock.unlock();
         }
 
         // Pass the initial <features> packet that we received in SetupPacketCallback to the
         // user's handler.  Don't call this while locked.
         userCallbacks.onPacket(packet);
         
         lock.lock();
         try {
             if(this.callbacks != null)
                 throw new IllegalStateException("PacketCallbacks already set");
 
             // Set the user's callback.
             this.callbacks = userCallbacks;
             
             // If PacketReaderThread is waiting for callbacks to be set, wake it up.
             cond.signalAll();
         } finally {
             lock.unlock();
         }
     }
     Element queuedPacket;
 
     /**
      * This callback handler receives packets during initial setup: TLS and
      * compression negitiation.  Once we're finished with that, it switches
      * callbacks over to the ones the user provided.
      */
     class SetupPacketCallback extends PacketCallback {
         boolean complete = false;
         XMPPException error = null;
 
         String host;
         int port;
 
         SetupPacketCallback(String host, int port) {
             this.host = host;
             this.port = port;
         }
 
         public void onPacket(Element packet) {
             assertNotLocked();
 
             try {
                 String initResponse;
                 lock.lock();
                 try {
                     throwIfDisconnected();
 
                     // If this returns null, then initialization is finished.
                     initResponse = processInitializationPacket(packet);
                 } finally {
                     lock.unlock();
                 }
 
                 if(initResponse != null) {
                     // We need to be unlocked for these responses.  Note that we may be
                     // disconnected asynchronously once we unlock.
                     if(initResponse == ENABLE_COMPRESSION)
                         enableCompression();
                     else if(initResponse == ENABLE_TLS)
                         proceedTLSReceived();
                     else if(initResponse == DO_NOTHING)
                         ;
                     else
                         writePacket(initResponse);
 
                     return;
                 }
 
                 lock.lock();
                 try {
                     throwIfDisconnected();
 
                     // After a successful connect, fill in config with the host we actually connected
                     // to.  This allows the client to detect what it's actually talking to.  Don't do
                     // this until we're actually connected, so later autodiscovery attempts aren't modified.
                     config.setHost(host);
                     config.setPort(port);
 
                     // We just received the first <features/> packet that isn't related to
                     // stream negotiation.  Queue it; we'll pass it to the user's callback
                     // when it's set.
                     queuedPacket = packet;
 
                     // Clear ourself as the callback.
                     if(callbacks != this)
                         throw new IllegalStateException("unexpected value for callbacks");
                     callbacks = null;
                     
                     // Wake initializeConnection back up.
                     complete = true;
                     cond.signalAll();
                 } finally {
                     lock.unlock();
                 }
             } catch(IOException e) {
                 onError(new XMPPException("I/O error establishing connection to server", e));
             } catch(XMPPException e) {
                 onError(e);
             }
         }
 
         public void onError(XMPPException error) {
             assertNotLocked();
             lock.lock();
             try {
                 this.error = error;
                 cond.signalAll();
             } finally {
                 lock.unlock();
             }
         }
 
         /** Wait until stream negotiation is complete. */
         public void waitForCompletion() throws XMPPException {
             assertNotLocked();
 
             lock.lock();
             try {
                 while(!complete && error == null) {
                     ThreadUtil.uninterruptibleWait(cond);
                 }
 
                 if(error != null)
                     throw new XMPPException(error);
             } finally {
                 lock.unlock();
             }
         }
     };
 
     /**
      * Return true if zlib (deflate) compression is supported.
      */
     private static boolean getZlibSupported() {
         try {
             Class.forName("com.jcraft.jzlib.ZOutputStream");
         }
         catch (ClassNotFoundException e) {
             // throw new IllegalStateException("Cannot use compression. Add smackx.jar to the classpath");
             return false;
         }
         return true;
     }
 
 
     /**
      * Attempt to negotiate a feature, based on the most recent <features/> packet.
      * 
      * @return true if a feature is being negotiated.
      * @throws XMPPException
      * @throws IOException
      */
     private String negotiateFeature() throws XMPPException, IOException {
         assertLocked();
 
         // If TLS is required but the server doesn't offer it, disconnect
         // from the server and throw an error. First check if we've already negotiated TLS
         // and are secure, however (features get parsed a second time after TLS is established).
         if (!isSecureConnection() && !featureStartTLSReceived &&
                 config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required)
         {
             throw new XMPPException("Server does not support security (TLS), " +
                     "but security required by connection configuration.",
                     XMPPError.Condition.forbidden);
         }
 
         if(!isSecureConnection() && featureStartTLSReceived &&
                 config.getSecurityMode() != ConnectionConfiguration.SecurityMode.disabled) {
             
             // If we havn't yet set up sslSocketFactory, and encryption is available,
             // set it up. 
             if(sslSocketFactory == null)
                 sslSocketFactory = new XMPPSSLSocketFactory(config, originalServiceName);
 
             if(sslSocketFactory.isAvailable()) {
                 // The server is offering TLS, so enable it.  This should result in <proceed/>.
                 return "<starttls xmlns=\"urn:ietf:params:xml:ns:xmpp-tls\"/>";
             }
             
             // Encryption was offered, but we weren't able to initialize it.  If the user required
             // TLS, fail.  We could handle this failure earlier, but it's a rare case.
             if(!sslSocketFactory.isAvailable() && config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
                 throw new XMPPException("System does not support encryption, " +
                         "but security is required by connection configuration.", XMPPError.Condition.forbidden);
             }
         }
 
         // Compression must be negotiated after encryption. 
         if(!usingXMPPCompression && config.isCompressionEnabled()) {
             // If we we share a supported compression method with the server, enable compression.
             if(getZlibSupported() && featureCompressionMethods.contains("zlib")) {
                 // Only attempt to negotiate a protocol once per <features/>.  If it fails
                 // we'll be notified by <failure/>, which will retry feature negotiation; if
                 // we don't do this, it'll just try to negotiate the same compressor over and
                 // over.
                 featureCompressionMethods.remove("zlib");
 
                 // Request this protocol; we should now receive <compressed/>.
                 return "<compress xmlns='http://jabber.org/protocol/compress'>" + 
                     "<method>" + "zlib" + "</method></compress>";
             }
         }
         
         // We're not interested in the transport features of this connection, so
         // the transport negotiation is complete.  The <features/> we just received
         // must be returned to the application.
         return null;
     }
 
     static final String ENABLE_TLS = "enable-tls";
     static final String ENABLE_COMPRESSION = "enable-compression";
     static final String DO_NOTHING = "do-nothing";
 
     /**
      * Process a packet during initialization.  Returns one of the following values:
      * <p>
      * {@code ENABLE_TLS}: TLS must be enabled.
      * <br>
      * {@code ENABLE_COMPRESSION}: Compression must be enabled.
      * <br>
      * {@code DO_NOTHING}: Do nothing, and continue waiting for initialization packets.
      * <br>
      * Any other string is an initialization packet to be sent to the server.
      * <br>
      * {@code null}: Initialization is complete.  The packet should be sent to the user.
      * 
      * @param node The packet to process.
      * @return whether initialization continues.
      * @throws XMPPException
      * @throws IOException
      */
     private String processInitializationPacket(Element node)
         throws XMPPException, IOException
     {
         assertLocked();
 
         if (node.getNodeName().equals("features")) {
             featureStartTLSReceived = false;
             featureCompressionMethods = new ArrayList<String>();
 
             for (Element child: XmlUtil.getChildElements(node)) {
                 if (!usingTLS && child.getNodeName().equals("starttls")) {
                     featureStartTLSReceived = true;
 
                     for (Element startTlsChild: XmlUtil.getChildElements(child)) {
                         if (startTlsChild.getNodeName().equals("required")) {
                             if (config.getSecurityMode() == ConnectionConfiguration.SecurityMode.disabled) {
                                 throw new XMPPException(
                                     "TLS required by server but not allowed by connection configuration", XMPPError.Condition.forbidden);
                             }
                         }
                     }
                 }
                 else if (child.getNodeName().equals("compression")) {
                     for (Element compressionChild: XmlUtil.getChildElements(child)) {
                         if (!compressionChild.getNodeName().equals("method"))
                             continue;
                         featureCompressionMethods.add(XmlUtil.getTextContent(compressionChild));
                     }
                 }
             }
 
             // We've received a new feature list; see if we can negotiate any of them. 
             return negotiateFeature();
         }
 
         else if(node.getNodeName().equals("proceed") && node.getNamespaceURI().equals("urn:ietf:params:xml:ns:xmpp-tls")) {
             // The server has acknowledged our <starttls/> request.  Enable TLS.
             return ENABLE_TLS;
         }
         else if(node.getNodeName().equals("failure")) { 
             if(node.getNamespaceURI().equals("urn:ietf:params:xml:ns:xmpp-tls")) {
                 // The server offered STARTTLS, but responded with a failure when we
                 // tried to use it.  The stream will be closed by the server.  This is an
                 // abnormal condition.
                 throw new XMPPException("Server failed while initializing TLS");
             } else if(node.getNamespaceURI().equals("http://jabber.org/protocol/compress")) {
                 // The server offered compression, but failed when we tried to use it.
                 // This isn't a fatal error, so attempt to negotiate the next feature.
                 // XXX: test this (jabberd2 is always returning success, even on a bogus method)
                 return negotiateFeature();
             }
         }
         else if (node.getNodeName().equals("compressed") && node.getNamespaceURI().equals("http://jabber.org/protocol/compress")) {
             // Server confirmed that it's possible to use stream compression. Start
             // stream compression
             return ENABLE_COMPRESSION;
         }
 
         /* We received an initialization packet we don't know about.  Is it valid for
          * the server to send anything at all before <features/>? */
         return DO_NOTHING;
     }
 
     /**
      * Parse the <stream version> string.  If not present, treat the
      * version as "0.9" as required by spec.  If the string fails to
      * parse, raises an exception.
      * 
      * @return the version number multiplied by 100; version 1.5 is 150 
      */
     private static int parseVersionString(String version) throws XMPPException {
         if(version == null)
             version = "0.9";
 
         // Verify the string all at once, so we don't have to do it below.
         if(!version.matches("\\d+(\\.\\d*)?"))
             throw new XMPPException("Invalid version string from server: " + version);
 
         String[] parts = version.split("\\.");
         int major = Integer.parseInt(parts[0]);
         int minor = Integer.parseInt(parts[1]);
         if(minor < 10)
             minor *= 10;
 
         return major * 100 + (minor % 100);
     }
 
     private void shutdown_stream() {
         assertLocked();
 
         if(initialLookup != null) {
             // initializeConnection() is performing a DNS lookup.  Cancel it, but
             // don't clear the reference.
             initialLookup.cancel();
         }
 
         if(socketConnector != null) {
             // Cancel any ongoing connection; don't clear the reference.
             socketConnector.cancel();
         }
 
         // If socket isn't set, then initializeConnection hasn't yet set up the
         // socket or thread.  Skip to the threadExited state, so it'll abort without
         // starting them.
         if(socket == null) {
             threadExited = true;
             cond.signalAll(); // always signal when changing threadExited
             return;
         }
 
         try {
             // Closing the socket will cause any blocking readers and writers on the
             // socket to stop waiting and throw an exception.
             if(socket != null)
                 socket.close();
         } catch(IOException e) {
             throw new RuntimeException("Unexpected I/O error disconnecting socket", e);
         }
 
         // Wait for the thread to exit, unless we're inside the thread itself.
        if(packetReaderThread != Thread.currentThread()) {
             while(!threadExited)
                 ThreadUtil.uninterruptibleWait(cond);
         }
         packetReaderThread = null;
 
         // Shut down the keepalive thread, if any.  Do this after closing the socket,
         // so it'll receive an IOException immediately if it's currently blocking to write,
         // guaranteeing it'll exit quickly.
         if(this.keepAlive != null) {
             this.keepAlive.close();
             this.keepAlive = null;
         }
 
         this.socket = null;
 
         if (reader != null) {
             try { reader.close(); } catch (IOException ignore) { /* ignore */ }
             reader = null;
         }
 
         if (writer != null) {
             try { writer.close(); } catch (IOException ignore) { /* ignore */ }
             writer = null;
         }
     }
 
     /**
      * Forcibly disconnect the stream.  If readPacket() is waiting for input, it will
      * return end of stream immediately.
      */
     public void disconnect() {
         assertNotLocked();
 
         lock.lock();
         try {
             shutdown_stream();
         } finally {
             lock.unlock();
         }
     }
 
     static boolean waitUntilTime(Condition cond, long waitUntil) {
         long ms = waitUntil - System.currentTimeMillis();
         if(ms <= 0)
             return false;
 
         try {
             cond.await(ms, TimeUnit.MILLISECONDS);
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
         return true;
     }
 
     public void gracefulDisconnect(String packet)
     {
         assertNotLocked();
 
         /* Ask the stream to close. */
         try {
             if (packet == null)
                 packet = "";
 
             // Append the final packet (if any) and </stream> and send them together,
             // so they're sent together.
             packet += "</stream:stream>";
             writePacket(packet);
         }
         catch (XMPPException e) {
             // If this fails for some reason, just close the connection.
             e.printStackTrace();
             disconnect();
             return;
         }
 
         lock.lock();
         try {
             // Wait for the connection to close gracefully.
             long waitUntil = System.currentTimeMillis() + SmackConfiguration.getPacketReplyTimeout();
             while(!threadExited) {
                 if(!waitUntilTime(cond, waitUntil))
                     break;
             }
         } finally {
             lock.unlock();
         }
 
         // If the connection didn't close gracefully, force it.
         disconnect();
     }
 
     /**
      * Read settings from the top-level stream header.
      * 
      * If a non-null Element is returned, the stream header is from a legacy
      * server, and the provided element should be processed as if it was a
      * received packet.
      */
     private Element loadStreamSettings(Element element) throws XMPPException {
         assertLocked();
 
         // Save the connection id.
         connectionID = element.getAttribute("id");
 
         // Save the service name.
         String from = element.getAttribute("from");
         if(from != null)
             config.setServiceName(from);
 
         // If the server isn't version 1.0, then we'll assume it doesn't support
         // <features/>.  Consider the connection established now.
 
         // The version attribute is only present for version 1.0 and higher.
         int protocolVersion = parseVersionString(element.getAttribute("version"));
 
         // If the protocol version is lower than 1.0, we may not receive <features/>.
         // Disable compression and encryption, and establish the session now.
         if(protocolVersion < 100) {
             // Old versions of the protocol may not send <features/>.  Mask this to the
             // user by returning a dummy <features/> node.
             try {
                 DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
                 DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
                 Document doc = docBuilder.newDocument();
                 return doc.createElementNS("http://etherx.jabber.org/streams", "features");
             } catch(ParserConfigurationException e) {
                 throw new RuntimeException("Unexpected error", e);
             }
         }
         return null;
     }
 
     class PacketReaderThread extends Thread {
         /** Wait for callbacks to be available.  Returns {@link PacketCallback}, or
          *  null if {@link XMPPStreamTCP#disconnect} is called. */
         private PacketCallback getCallbacks() {
             assertLocked();
 
             while(callbacks == null && socket != null && !socket.isClosed())
                 ThreadUtil.uninterruptibleWait(cond);
 
             return callbacks;
         }
 
         public void run() {
             while(true) {
                 try {
                     Element result = readPacketLoop(parser);
                     assertNotLocked();
                     lock.lock();
 
                     PacketCallback currentCallbacks = null;
 
                     try {
                         if(parser.getDepth() == 1) {
                             // Process the stream header.  If it returns a packet, treat it
                             // as the received packet; otherwise move on and read the next packet.
                             result = loadStreamSettings(result);
                             if(result == null)
                                 continue;
                         }
                         
                         currentCallbacks = getCallbacks();
                     } finally {
                         lock.unlock();
                     }
 
                     if(currentCallbacks != null)
                         currentCallbacks.onPacket(result);
                 } catch(XMPPException e) {
                     assertNotLocked();
 
                     PacketCallback currentCallbacks = null;
 
                     lock.lock();
                     try {
                         currentCallbacks = getCallbacks();
                     } finally {
                         lock.unlock();
                     }
                     
                     // This is our normal exit path; the stream will be closed and readPacketLoop
                     // will throw a socket error.
                     disconnect();
 
                     if(currentCallbacks != null)
                         currentCallbacks.onError(e);
 
                     // Notify any disconnect() calls in other threads that we're exiting.
                     lock.lock();
                     try {
                         threadExited = true;
                         cond.signalAll();
                     } finally {
                         lock.unlock();
                     }
 
                     return;
                 }
             }
         }
     };
 
     /**
      * Read the next element from the given parser.
      * <p>
      * This function must run without locking XMPPStreamTCP, as it blocks.  In order to
      * prevent accidental use of data which requires locking, this function is static
      * and only uses the provided parser.
      * <p>
      * If the parser returns at depth 1, the returned Element represents the top-level
      * stream header.  Otherwise, the parser returns at depth 2 and the returned Element
      * represents a received XMPP stanza.
      */
     private static Element readPacketLoop(XmlPullParser parser) throws XMPPException {
         try {
             // Depth 0 means we're just starting; 1 means we've read the stream header;
             // 2 means we've read at least one packet.  If we're at depth 2, then the
             // previous element must be END_TAG, as a result of calling ReadNodeFromXmlPull
             // below.
             if (parser.getDepth() > 2)
                 throw new RuntimeException("Unexpected parser depth: " + parser.getDepth());
             if (parser.getDepth() == 2 && parser.getEventType() != XmlPullParser.END_TAG)
                 throw new RuntimeException("Unexpected event type: " + parser.getEventType());
 
             // Read the next packet.
             parser.next();
 
             /* If there are any text nodes between stanzas, ignore them. */
             while (parser.getEventType() == XmlPullParser.TEXT)
                 parser.next();
 
             // END_DOCUMENT means the stream has ended.
             if (parser.getEventType() == XmlPullParser.END_DOCUMENT)
                 throw new XMPPException("Session terminated");
 
             // If we receive END_TAG, then </stream:stream> has been closed and the
             // connection is about to be closed.  If we receive END_DOCUMENT, then the
             // stream has been closed abruptly.
             if (parser.getEventType() == XmlPullParser.END_TAG)
                 throw new XMPPException("Session terminated");
 
             // We've checked all other possibilities; the event type must be START_TAG.
             if (parser.getEventType() != XmlPullParser.START_TAG)
                 throw new RuntimeException("Unexpected state from XmlPullParser: " + parser.getEventType());
 
             // We must now be at depth 1 (<stream> starting) or 2 (a new stanza).
             if (parser.getDepth() != 1 && parser.getDepth() != 2)
                 throw new RuntimeException("Unexpected post-packet parser depth: " + parser.getDepth());
 
             /* If we havn't yet received the opening <stream> tag, wait until we get it. */
             if(parser.getDepth() == 1) {
                 // Check that the opening stream is what we expect.
                 if (!parser.getName().equals("stream") ||
                         !parser.getNamespace().equals("http://etherx.jabber.org/streams") ||
                         !parser.getNamespace(null).equals("jabber:client")) {
                     throw new XMPPException("Expected stream:stream");
                 }
 
                 return XmlUtil.ReadElementFromXmlPullNonRecursive(parser);
             } else {
                 // We have an XMPP stanza.  Read the whole thing into a DOM node and return it.
                 return XmlUtil.ReadNodeFromXmlPull(parser);
             }
         }
         catch (XmlPullParserException e) {
             throw new XMPPException("XML error", e);
         }
         catch (IOException e) {
             throw new XMPPException("I/O error", e);
         }
     }
 
     private void initReaderAndWriter() throws XMPPException, IOException
     {
         assertLocked();
 
         InputStream inputStream = socket.getInputStream(); 
         OutputStream outputStream = socket.getOutputStream(); 
 
         if(usingXMPPCompression) {
             try {
                 Class<?> zoClass = Class.forName("com.jcraft.jzlib.ZOutputStream");
                 Constructor<?> constructor =
                     zoClass.getConstructor(OutputStream.class, Integer.TYPE);
                 OutputStream compressed_out = (OutputStream) constructor.newInstance(outputStream, 9);
                 Method method = zoClass.getMethod("setFlushMode", Integer.TYPE);
                 method.invoke(compressed_out, 2); // Z_SYNC_FLUSH
     
                 Class<?> ziClass = Class.forName("com.jcraft.jzlib.ZInputStream");
                 constructor = ziClass.getConstructor(InputStream.class);
                 InputStream compressed_in = (InputStream) constructor.newInstance(inputStream);
                 method = ziClass.getMethod("setFlushMode", Integer.TYPE);
                 method.invoke(compressed_in, 2); // Z_SYNC_FLUSH
     
                 inputStream = compressed_in;
                 outputStream = compressed_out;
             }
             catch (Exception e) {
                 // If this fails, we can't continue; the other side is expecting
                 // compression.  This shouldn't fail, since we checked importing the
                 // class earlier.
                 e.printStackTrace();
                 throw new RuntimeException("Unexpected error initializing compression", e);
             }
         }        
         
         reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
         writer = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
         
         /* Send incoming and outgoing data to the read and write observers, if any. */
         if(readEvent != null) {
             ObservableReader obsReader = new ObservableReader(reader);
             obsReader.setReadEvent(readEvent);
             reader = obsReader;
         }
 
         if(writeEvent != null) {
             ObservableWriter obsWriter = new ObservableWriter(writer);
             obsWriter.setWriteEvent(writeEvent);
             writer = obsWriter;
         }
 
         // For keepalive purposes, add an observer wrapper to monitor writes.
         {
             ObservableWriter obsWriter = new ObservableWriter(writer);
             obsWriter.setWriteEvent(keepaliveMonitorWriteEvent);
             writer = obsWriter;
         }
     }
 
     /** If the socket has been disconnected due to a call to disconnect(), throw
      *  an exception. */
     private void throwIfDisconnected() throws XMPPException {
         assertLocked();
         if(socket == null || socket.isClosed())
             throw new XMPPException("Connection has been closed");
     }
 
     private void enableCompression() throws XMPPException, IOException {
         assertNotLocked();
 
         lock.lock();
         try {
             throwIfDisconnected();
 
             usingXMPPCompression = true;
             initReaderAndWriter();
         } finally {
             lock.unlock();
         }
 
         streamReset();
     }
 
     private void proceedTLSReceived() throws XMPPException
     {
         // We need control over locking to support cancellation.
         assertNotLocked();
 
         // Take a reference to the socket.
         Socket socketCopy;
         lock.lock();
         try {
             throwIfDisconnected();
             socketCopy = socket;
         } finally {
             lock.unlock();
         }
 
         // Create the SSLSocket.  This shouldn't block, but as it can throw IOException,
         // let's assume that it might and not hold onto the lock.
         SSLSocket sslSocket;
         try {
             sslSocket = sslSocketFactory.attachSSLConnection(socketCopy, originalServiceName, socketCopy.getPort());
             sslSocket.setSoTimeout(0);
         } catch(IOException e) {
             throw new XMPPException("Error initializing TLS", e);
         }
 
         // Update socket to point to sslSocket, so calls to disconnect() will close it.
         lock.lock();
         try {
             throwIfDisconnected();
             socket = sslSocket;
             initReaderAndWriter();
         } catch(IOException e) {
             throw new XMPPException(e);
         } finally {
             lock.unlock();
         }
 
         // Perform the TLS handshake unlocked.  If another thread calls disconnect(), it'll
         // close the socket and this will throw an IOException.
         try {
             sslSocket.startHandshake();
         } catch(IOException e) {
             throw new XMPPException("Error initializing TLS", e);
         }
 
         // Send the new stream header.
         streamReset();
 
         lock.lock();
         try {
             throwIfDisconnected();
 
             // If usingSecureConnection is false, we're encrypted but we couldn't verify
             // the server's certificate.
             usingTLS = true;
             CertificateException insecureReason = sslSocketFactory.isInsecureConnection(sslSocket);
             usingSecureConnection = (insecureReason == null);
 
             // Record if TLS compression is active, so we won't try to negotiate XMPP
             // compression too.
             if(sslSocketFactory.getCompressionMethod(sslSocket) != null)
                 usingTLSCompression = true;
 
             // If !usingSecureConnection, then we have an encrypted TLS connection but with
             // an unvalidated certificate.  If a secure connection was required, fail.
             if(!usingSecureConnection && config.getSecurityMode() == ConnectionConfiguration.SecurityMode.required) {
                 throw new XMPPException("Server does not support security (TLS), " + 
                         "but the configuration requires a secure connection.",
                         XMPPError.Condition.forbidden, insecureReason);
             }
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * When authentication is successful, we must open a new <stream:stream>.
      * This is step 12 in http://xmpp.org/extensions/xep-0178.html#c2s. 
      */
     public void streamReset() throws XMPPException
     {
         assertNotLocked();
 
         lock.lock();
         try {
             throwIfDisconnected();
 
             // The <stream:stream> element will not be closed after a stream reset,
             // so reset the parser state.
             //
             // Note that special care is needed for stream resets when the stream format is
             // changing.  See comments in setupTransport.  External calls to this function
             // don't involve stream format changes.
             try {
                 parser.setInput(reader);
             }
             catch (XmlPullParserException xppe) {
                 xppe.printStackTrace();
                 throw new RuntimeException(xppe);
             }
         } finally {
             lock.unlock();
         }
 
         /* Send the stream:stream to start the new stream. */
         StringBuilder stream = new StringBuilder();
         stream.append("<?xml version='1.0'?>");
         stream.append("<stream:stream");
         stream.append(" to=\"").append(config.getServiceName()).append("\"");
         stream.append(" xmlns=\"jabber:client\"");
         stream.append(" xmlns:stream=\"http://etherx.jabber.org/streams\"");
         stream.append(" version=\"1.0\">");
 
         writePacket(stream.toString());
     }
 
     /**
      * Starts the keepalive process. A white space (aka heartbeat) is going to be
      * sent to the server every 30 seconds (by default) since the last stanza was sent
      * to the server.
      */
     private KeepAliveTask keepAlive;
     private void startKeepAliveProcess() throws XMPPException {
         assertNotLocked();
         
         lock.lock();
         try {
             throwIfDisconnected();
 
             // Schedule a keep-alive task to run if the feature is enabled. will write
             // out a space character each time it runs to keep the TCP/IP connection open.
             int keepAliveInterval = SmackConfiguration.getKeepAliveInterval();
             if (keepAliveInterval == 0)
                 return;
             
             KeepAliveTask task = new KeepAliveTask(keepAliveInterval);
             keepAlive = task;
         } finally {
             lock.unlock();
         }
     }
 
     /**
      * A TimerTask that keeps connections to the server alive by sending a space
      * character on an interval.
      */
     private class KeepAliveTask implements Runnable {
         private int delay;
         private Thread thread;
         private boolean done = false;
 
         public KeepAliveTask(int delay) {
             this.delay = delay;
             
             Thread keepAliveThread = new Thread(this);
             keepAliveThread.setDaemon(true);
             keepAliveThread.setName("Smack Keepalive");
             keepAliveThread.start();
             this.thread = keepAliveThread;
         }
 
         /**
          * Close the keepalive thread. 
          */
         public void close() {
             synchronized (this) {
                 done = true;
                 thread.interrupt();
             }
 
             ThreadUtil.uninterruptibleJoin(thread);
         }
 
         long lastActive = System.currentTimeMillis();
         public void run() {
             // Add a write listener to track the time of the most recent write.  This is used
             // to only send heartbeats when the connection is idle.
             long lastActive[] = {System.currentTimeMillis()};
             WriterListener listener = new WriterListener() {
                 public void write(String str) {
                     KeepAliveTask.this.lastActive = System.currentTimeMillis();
                 }
             };
             keepaliveMonitorWriteEvent.addWriterListener(listener);
             
             while (true) {
                 // Send heartbeat if no packet has been sent to the server for a given time
                 if (System.currentTimeMillis() - KeepAliveTask.this.lastActive >= delay) {
                     try {
                         writePacket(" ");
                     }
                     catch (XMPPException e) {
                         // Do nothing, and assume that whatever caused an error
                         // here will cause one in the main code path, too.  This
                         // will also happen if the write blocked and XMPPStreamTCP.disconnect
                         // closed the socket.
                     }
                 }
 
                 try {
                     synchronized (this) {
                         if (done)
                             break;
                         
                         // Sleep until we should write the next keep-alive.
                         wait(delay);
                     }
                 }
                 catch (InterruptedException ie) {
                     // close() interrupted us to shut down the thread.
                     break;
                 }
             }
 
             keepaliveMonitorWriteEvent.removeWriterListener(listener);
         }
     }
 };
