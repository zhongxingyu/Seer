 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.parser_xmpp;
 
 import com.dmdirc.parser.common.AwayState;
 import com.dmdirc.parser.common.BaseSocketAwareParser;
 import com.dmdirc.parser.common.ChannelJoinRequest;
 import com.dmdirc.parser.common.ChildImplementations;
 import com.dmdirc.parser.common.DefaultStringConverter;
 import com.dmdirc.parser.common.ParserError;
 import com.dmdirc.parser.common.QueuePriority;
 import com.dmdirc.parser.interfaces.ChannelInfo;
 import com.dmdirc.parser.interfaces.LocalClientInfo;
 import com.dmdirc.parser.interfaces.StringConverter;
 import com.dmdirc.parser.interfaces.callbacks.AwayStateListener;
 import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
 import com.dmdirc.parser.interfaces.callbacks.ChannelSelfJoinListener;
 import com.dmdirc.parser.interfaces.callbacks.ConnectErrorListener;
 import com.dmdirc.parser.interfaces.callbacks.DataInListener;
 import com.dmdirc.parser.interfaces.callbacks.DataOutListener;
 import com.dmdirc.parser.interfaces.callbacks.NumericListener;
 import com.dmdirc.parser.interfaces.callbacks.OtherAwayStateListener;
 import com.dmdirc.parser.interfaces.callbacks.PrivateMessageListener;
 import com.dmdirc.parser.interfaces.callbacks.ServerReadyListener;
 import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.ChatManagerListener;
 import org.jivesoftware.smack.ConnectionConfiguration;
 import org.jivesoftware.smack.ConnectionListener;
 import org.jivesoftware.smack.MessageListener;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.RosterEntry;
 import org.jivesoftware.smack.RosterListener;
 import org.jivesoftware.smack.XMPPConnection;
 import org.jivesoftware.smack.XMPPException;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smack.packet.Presence;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 
 /**
  * A parser which can understand the XMPP protocol.
  */
 @ChildImplementations({
     XmppClientInfo.class, XmppLocalClientInfo.class, XmppFakeChannel.class,
     XmppChannelClientInfo.class
 })
 public class XmppParser extends BaseSocketAwareParser {
 
     /** Pattern to use to extract priority. */
     private static final Pattern PRIORITY_PATTERN
             = Pattern.compile("(?i)(?:^|&)priority=([0-9]+)(?:$|&)");
 
     /** The connection to use. */
     private XMPPConnection connection;
 
     /** A cache of known chats. */
     private final Map<String, Chat> chats = new HashMap<String, Chat>();
 
     /** A cache of known clients. */
     private final Map<String, XmppClientInfo> contacts = new HashMap<String, XmppClientInfo>();
 
     /** Whether or not to use a fake local channel for a buddy list replacement. */
     private final boolean useFakeChannel;
 
     /** The priority of this endpoint. */
     private final int priority;
 
     /** The fake channel to use is useFakeChannel is enabled. */
     private XmppFakeChannel fakeChannel;
 
     /**
      * Creates a new XMPP parser for the specified address.
      *
      * @param address The address to connect to
      */
     public XmppParser(final URI address) {
         super(address);
 
          if (address.getQuery() == null) {
             useFakeChannel = false;
             priority = 0;
         } else {
             final Matcher matcher = PRIORITY_PATTERN.matcher(address.getQuery());
 
             useFakeChannel = address.getQuery().matches("(?i).*(^|&)showchannel($|&).*");
             priority = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void disconnect(final String message) {
         // TODO: Pass quit message on as presence?
         connection.disconnect();
     }
 
     /** {@inheritDoc} */
     @Override
     public void joinChannels(final ChannelJoinRequest... channels) {
         for (ChannelJoinRequest request : channels) {
             final MultiUserChat muc = new MultiUserChat(connection, request.getName().substring(1));
 
             try {
                 if (request.getPassword() == null) {
                     muc.join(getLocalClient().getNickname());
                 } else {
                     muc.join(getLocalClient().getNickname(), request.getPassword());
                 }
 
                 // TODO: Send callbacks etc
             } catch (XMPPException ex) {
                 // TODO: handle
             }
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public ChannelInfo getChannel(final String channel) {
         // TODO: Implement
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<? extends ChannelInfo> getChannels() {
         return Collections.<ChannelInfo>emptyList();
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxLength(final String type, final String target) {
         return -1; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxLength() {
         return -1; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public LocalClientInfo getLocalClient() {
         final String[] parts = parseHostmask(connection.getUser());
 
         // TODO: Cache this
         return new XmppLocalClientInfo(this, parts[0], parts[2], parts[1]);
     }
 
     /** {@inheritDoc} */
     @Override
     public XmppClientInfo getClient(final String details) {
         final String[] parts = parseHostmask(details);
 
         if (!contacts.containsKey(parts[0])) {
             contacts.put(parts[0], new XmppClientInfo(this, parts[0], parts[2], parts[1]));
         }
 
         return contacts.get(parts[0]);
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendRawMessage(final String message) {
         // Urgh, hacky horrible rubbish. These commands should call methods.
         if (message.toUpperCase().startsWith("WHOIS ")) {
             handleWhois(message.split(" ")[1]);
         }
     }
 
     /**
      * Handles a whois request for the specified target.
      *
      * @param target The user being WHOIS'd
      */
     private void handleWhois(final String target) {
         // Urgh, hacky horrible rubbish. This should be abstracted.
         if (contacts.containsKey(target)) {
             final XmppClientInfo client = contacts.get(target);
             final String[] userParts = client.getNickname().split("@", 2);
 
             callNumericCallback(311, target, userParts[0], userParts[1], "*", client.getRealname());
 
             for (Map.Entry<String, XmppEndpoint> endpoint : client.getEndpoints().entrySet()) {
                 callNumericCallback(399, target, endpoint.getKey(),
                         "(" + endpoint.getValue().getPresence() + ")", "has endpoint");
             }
         } else {
             callNumericCallback(401, target, "No such contact found");
         }
 
         callNumericCallback(318, target, "End of /WHOIS.");
     }
 
     private void callNumericCallback(final int numeric, final String ... args) {
         final String[] newArgs = new String[args.length + 3];
         newArgs[0] = ":xmpp.server";
         newArgs[1] = (numeric < 100 ? "0" : "") + (numeric < 10 ? "0" : "") + numeric;
         newArgs[2] = getLocalClient().getNickname();
         System.arraycopy(args, 0, newArgs, 3, args.length);
 
        getCallback(NumericListener.class).onNumeric(null, null, numeric, newArgs);
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendRawMessage(final String message, final QueuePriority priority) {
         sendRawMessage(message);
     }
 
     /** {@inheritDoc} */
     @Override
     public StringConverter getStringConverter() {
         return new DefaultStringConverter();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isValidChannelName(final String name) {
         return false; // TODO: Implement
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean compareURI(final URI uri) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<? extends ChannelJoinRequest> extractChannels(final URI uri) {
         return Collections.<ChannelJoinRequest>emptyList();
     }
 
     /** {@inheritDoc} */
     @Override
     public String getNetworkName() {
         return "XMPP"; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerSoftware() {
         return "Unknown"; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerSoftwareType() {
         return "XMPP"; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public List<String> getServerInformationLines() {
         return Collections.<String>emptyList(); // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxTopicLength() {
         return 0; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getBooleanChannelModes() {
         return ""; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getListChannelModes() {
         return ""; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxListModes(final char mode) {
         return 0; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isUserSettable(final char mode) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getParameterChannelModes() {
         return ""; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getDoubleParameterChannelModes() {
         return ""; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getUserModes() {
         return ""; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getChannelUserModes() {
         return ""; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public String getChannelPrefixes() {
         return "#";
     }
 
     /** {@inheritDoc} */
     @Override
     public long getServerLatency() {
         return 1000L; // TODO
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendCTCP(final String target, final String type, final String message) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendCTCPReply(final String target, final String type, final String message) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendMessage(final String target, final String message) {
         if (!chats.containsKey(target)) {
             chats.put(target, connection.getChatManager().createChat(target, new MessageListenerImpl()));
         }
 
         try {
             chats.get(target).sendMessage(message);
         } catch (XMPPException ex) {
             // TODO: Handle this
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendNotice(final String target, final String message) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendAction(final String target, final String message) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendInvite(final String channel, final String user) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public String getLastLine() {
         return "TODO: Implement me";
     }
 
     /** {@inheritDoc} */
     @Override
     public String[] parseHostmask(final String hostmask) {
         return new XmppProtocolDescription().parseHostmask(hostmask);
     }
 
     /** {@inheritDoc} */
     @Override
     public long getPingTime() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
         final String[] userInfoParts = getURI().getUserInfo().split(":", 2);
         final String[] userParts = userInfoParts[0].split("@", 2);
 
         final ConnectionConfiguration config = new ConnectionConfiguration(getURI().getHost(), getURI().getPort(), userParts[0]);
         config.setSecurityMode(getURI().getScheme().equalsIgnoreCase("xmpps")
                 ? ConnectionConfiguration.SecurityMode.required
                 : ConnectionConfiguration.SecurityMode.disabled);
         config.setSASLAuthenticationEnabled(true);
         config.setReconnectionAllowed(false);
         config.setRosterLoadedAtLogin(true);
         config.setSocketFactory(getSocketFactory());
         connection = new FixedXmppConnection(config);
 
         try {
             connection.connect();
 
             connection.addConnectionListener(new ConnectionListenerImpl());
             connection.addPacketListener(new PacketListenerImpl(DataInListener.class), new AcceptAllPacketFilter());
             connection.addPacketWriterListener(new PacketListenerImpl(DataOutListener.class), new AcceptAllPacketFilter());
             connection.getChatManager().addChatListener(new ChatManagerListenerImpl());
 
             connection.login(userInfoParts[0], userInfoParts[1], "DMDirc.");
 
             connection.sendPacket(new Presence(Presence.Type.available, null, priority, Presence.Mode.available));
             connection.getRoster().addRosterListener(new RosterListenerImpl());
 
             setServerName(connection.getServiceName());
 
             getCallback(ServerReadyListener.class).onServerReady(null, null);
 
             for (RosterEntry contact : connection.getRoster().getEntries()) {
                 getClient(contact.getUser()).setRosterEntry(contact);
             }
 
             if (useFakeChannel) {
                 fakeChannel = new XmppFakeChannel(this, "&contacts");
                 getCallback(ChannelSelfJoinListener.class).onChannelSelfJoin(null, null, fakeChannel);
                 fakeChannel.updateContacts(contacts.values());
             }
         } catch (XMPPException ex) {
             connection = null;
 
             final ParserError error = new ParserError(ParserError.ERROR_ERROR, "Unable to connect", "");
 
             if (ex.getCause() instanceof IOException) {
                 // Pass along the underlying socket error instead of an XMPP
                 // specific one
                 error.setException((IOException) ex.getCause());
             } else {
                 error.setException(ex);
             }
 
             getCallback(ConnectErrorListener.class).onConnectError(null, null, error);
         }
     }
 
     /**
      * Handles a client's away state changing.
      *
      * @param client The client whose state is changing
      * @param isBack True if the client is coming back, false if they're going away
      */
     public void handleAwayStateChange(final XmppClientInfo client, final boolean isBack) {
         if (useFakeChannel) {
             getCallback(OtherAwayStateListener.class)
                     .onAwayStateOther(null, null, client,
                     isBack ? AwayState.AWAY : AwayState.HERE,
                     isBack ? AwayState.HERE : AwayState.AWAY);
         }
     }
 
     /**
      * Marks the local user as away with the specified reason.
      *
      * @param reason The away reason
      */
     public void setAway(final String reason) {
         connection.sendPacket(new Presence(Presence.Type.available, reason,
                 priority, Presence.Mode.away));
 
         getCallback(AwayStateListener.class).onAwayState(null, null,
                 AwayState.HERE, AwayState.AWAY, reason);
     }
 
     /**
      * Marks the local user as back.
      */
     public void setBack() {
         connection.sendPacket(new Presence(Presence.Type.available, null,
                 priority, Presence.Mode.available));
 
         getCallback(AwayStateListener.class).onAwayState(null, null,
                 AwayState.AWAY, AwayState.HERE, null);
     }
 
     private class ConnectionListenerImpl implements ConnectionListener {
 
         /** {@inheritDoc} */
         @Override
         public void connectionClosed() {
             getCallback(SocketCloseListener.class).onSocketClosed(null, null);
         }
 
         /** {@inheritDoc} */
         @Override
         public void connectionClosedOnError(final Exception excptn) {
             // TODO: Handle exception
             getCallback(SocketCloseListener.class).onSocketClosed(null, null);
         }
 
         /** {@inheritDoc} */
         @Override
         public void reconnectingIn(final int i) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         /** {@inheritDoc} */
         @Override
         public void reconnectionSuccessful() {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         /** {@inheritDoc} */
         @Override
         public void reconnectionFailed(final Exception excptn) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
     }
 
     private class RosterListenerImpl implements RosterListener {
 
         /** {@inheritDoc} */
         @Override
         public void entriesAdded(final Collection<String> clctn) {
             // Do nothing, yet
         }
 
         /** {@inheritDoc} */
         @Override
         public void entriesUpdated(final Collection<String> clctn) {
             // Do nothing, yet
         }
 
         /** {@inheritDoc} */
         @Override
         public void entriesDeleted(final Collection<String> clctn) {
             // Do nothing, yet
         }
 
         /** {@inheritDoc} */
         @Override
         public void presenceChanged(final Presence prsnc) {
             getClient(prsnc.getFrom()).setPresence(prsnc);
         }
 
     }
 
     private class ChatManagerListenerImpl implements ChatManagerListener {
 
         /** {@inheritDoc} */
         @Override
         public void chatCreated(final Chat chat, final boolean bln) {
             if (!bln) {
                 // Only add chats that weren't created locally
                 chats.put(parseHostmask(chat.getParticipant())[0], chat);
                 chat.addMessageListener(new MessageListenerImpl());
             }
         }
 
     }
 
     private class MessageListenerImpl implements MessageListener {
 
         /** {@inheritDoc} */
         @Override
         public void processMessage(final Chat chat, final Message msg) {
             if (msg.getBody() != null) {
                 // TOOD: Handle error messages
                 getCallback(PrivateMessageListener.class).onPrivateMessage(null,
                         null, msg.getBody(), msg.getFrom());
             }
         }
 
     }
 
     private class PacketListenerImpl implements PacketListener {
 
         private final Class<? extends CallbackInterface> callback;
 
         public PacketListenerImpl(final Class<? extends CallbackInterface> callback) {
             this.callback = callback;
         }
 
         /** {@inheritDoc} */
         @Override
         public void processPacket(final Packet packet) {
             if (callback.equals(DataOutListener.class)) {
                 getCallback(DataOutListener.class).onDataOut(null, null, packet.toXML(), true);
             } else {
                 getCallback(DataInListener.class).onDataIn(null, null, packet.toXML());
             }
         }
 
     }
 
     private static class AcceptAllPacketFilter implements PacketFilter {
 
         /** {@inheritDoc} */
         @Override
         public boolean accept(final Packet packet) {
             return true;
         }
 
     }
 
 }
