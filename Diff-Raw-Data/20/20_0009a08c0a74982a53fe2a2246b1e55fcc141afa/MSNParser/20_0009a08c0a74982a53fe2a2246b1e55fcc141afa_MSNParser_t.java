 /*
  * Copyright (c) 2006-2011 DMDirc Developers
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
 
 package com.dmdirc.addons.parser_msn;
 
 import com.dmdirc.parser.common.BaseParser;
 import com.dmdirc.parser.common.ChannelJoinRequest;
 import com.dmdirc.parser.common.ChildImplementations;
 import com.dmdirc.parser.common.CompositionState;
 import com.dmdirc.parser.common.DefaultStringConverter;
 import com.dmdirc.parser.common.QueuePriority;
 import com.dmdirc.parser.interfaces.ChannelInfo;
 import com.dmdirc.parser.interfaces.ClientInfo;
 import com.dmdirc.parser.interfaces.LocalClientInfo;
 import com.dmdirc.parser.interfaces.StringConverter;
 
 import java.net.URI;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.jml.MsnContact;
 import net.sf.jml.MsnMessenger;
 import net.sf.jml.impl.MsnMessengerFactory;
 
 /**
  * A parser which can understand the MSN protocol.
  */
 @ChildImplementations({
     MSNClientInfo.class, MSNLocalClientInfo.class, MSNFakeChannel.class,
     MSNChannelClientInfo.class
 })
 public class MSNParser extends BaseParser {
 
     /** MSN Connection. */
     private MsnMessenger msn;
     /** A cache of known clients. */
     private final Map<String, MSNClientInfo> clients
             = new HashMap<String, MSNClientInfo>();
     /** Whether or not to use a channel for contact lists. */
     private final boolean useFakeChannel;
     /** The fake channel to use is useFakeChannel is enabled. */
     private MSNFakeChannel fakeChannel;
     /** MSN Listener. */
     private MSNListener listener;
 
     /**
      * Creates a new parser for the specified address.
      *
      * @param address The address to connect to
      */
     public MSNParser(final URI address) {
         super(address);
 
         if (address.getQuery() == null) {
             useFakeChannel = false;
         } else {
             useFakeChannel = address.getQuery().matches(
                     "(?i).*(^|&)showchannel($|&).*");
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void disconnect(final String message) {
        super.disconnect(message);
         try {
             msn.logout();
         } catch (Exception ex) {
             //Fallthrough to finally block
         } finally {
             removeMSNParser();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void joinChannels(final ChannelJoinRequest... channels) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public ChannelInfo getChannel(final String channel) {
         if ("&contacts".equalsIgnoreCase(channel)) {
             return fakeChannel;
         }
         return null;
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<? extends ChannelInfo> getChannels() {
         return Collections.<ChannelInfo>emptyList();
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxLength(final String type, final String target) {
         return -1;
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxLength() {
         return -1;
     }
 
     /** {@inheritDoc} */
     @Override
     public LocalClientInfo getLocalClient() {
         final String[] parts = parseHostmask(msn.getOwner().getEmail()
                 .getEmailAddress());
         return new MSNLocalClientInfo(msn.getOwner(), this, parts[0], parts[2],
                 parts[1]);
     }
 
     /** {@inheritDoc} */
     @Override
     public MSNClientInfo getClient(final String details) {
         final String[] parts = parseHostmask(details);
         return clients.get(parts[0]);
     }
 
     /**
      * Retrieves a {@link ClientInfo} object which corresponds to the specified
      * contact. If the client wasn't previously known, it will be created.
      *
      * @param contact The contact to look up
      *
      * @return A corresponding client info object
      */
     public MSNClientInfo getClient(final MsnContact contact) {
         return getClient(contact.getEmail().getEmailAddress());
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendRawMessage(final String message) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendRawMessage(final String message,
             final QueuePriority priority) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public StringConverter getStringConverter() {
         return new DefaultStringConverter();
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isValidChannelName(final String name) {
         return false;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean compareURI(final URI uri) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public Collection<? extends ChannelJoinRequest> extractChannels(
             final URI uri) {
         return Collections.<ChannelJoinRequest>emptyList();
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerName() {
         return "MSN";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getNetworkName() {
         return "MSN";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerSoftware() {
         return "MSN";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getServerSoftwareType() {
         return "MSN";
     }
 
     /** {@inheritDoc} */
     @Override
     public List<String> getServerInformationLines() {
         return Collections.<String>emptyList();
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxTopicLength() {
         return 0;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getBooleanChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getListChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public int getMaxListModes(final char mode) {
         return 0;
     }
 
     /** {@inheritDoc} */
     @Override
     public boolean isUserSettable(final char mode) {
         return false;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getParameterChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getDoubleParameterChannelModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getUserModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getChannelUserModes() {
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String getChannelPrefixes() {
         return "#";
     }
 
     /** {@inheritDoc} */
     @Override
     public long getServerLatency() {
         return 0;
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendCTCP(final String target, final String type,
             final String message) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendCTCPReply(final String target, final String type,
             final String message) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /** {@inheritDoc} */
     @Override
     public void sendMessage(final String target, final String message) {
         msn.sendText(getClient(target).getContact().getEmail(), message);
 
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
         return "";
     }
 
     /** {@inheritDoc} */
     @Override
     public String[] parseHostmask(final String hostmask) {
         return new MSNProtocolDescription().parseHostmask(hostmask);
     }
 
     /** {@inheritDoc} */
     @Override
     public long getPingTime() {
         return 0;
     }
 
     /** Destroys the existing MSN object. */
     protected void removeMSNParser() {
         if (msn != null && listener != null) {
             msn.removeListener(listener);
         }
         msn = null;
         listener = null;
     }
 
     /** Creates a new MSN object. */
     protected void setupMSNParser() {
         if (msn != null) {
             removeMSNParser();
         }
         final String[] userInfoParts = getURI().getUserInfo().split(":", 2);
         msn = MsnMessengerFactory.createMsnMessenger(
                 userInfoParts[0], userInfoParts[1]);
         listener = new MSNListener(this);
         msn.addListener(listener);
     }
 
     /** {@inheritDoc} */
     @Override
     public void run() {
         setupMSNParser();
         msn.login();
         if (useFakeChannel) {
             fakeChannel = new MSNFakeChannel(this, "&contacts");
         }
     }
 
     /**
      * Updates the clients in the specified channel.
      *
      * @param channel Channel to update
      * @param clients Clients to use in the channel
      */
     public void updateClients(final ChannelInfo channel,
             final Collection<ClientInfo> clients) {
         if (channel == fakeChannel) {
             fakeChannel.replaceContacts(clients);
         }
     }
 
     /**
      * Adds the clients in the specified channel.
      *
      * @param channel Channel to update
      * @param clients Clients to add in the channel
      */
     public void addClients(final ChannelInfo channel,
             final Collection<ClientInfo> clients) {
         if (channel == fakeChannel) {
             fakeChannel.addContacts(clients);
         }
     }
 
     /**
      * Removes the clients in the specified channel.
      *
      * @param channel Channel to update
      * @param clients Clients to add in the channel
      */
     public void removeClients(final ChannelInfo channel,
             final Collection<ClientInfo> clients) {
         if (channel == fakeChannel) {
             fakeChannel.removeContacts(clients);
         }
     }
 
     /**
      * Returns the fake channel for this parser, this will be null if the
      * user has opted not to use the fake channel.
      *
      * @return Fake channel or null
      */
     public ChannelInfo getFakeChannel() {
         return fakeChannel;
     }
 
     /** {@inheritDoc} */
     @Override
     public String getBindIP() {
         return msn.getConnection().getInternalIP();
     }
 
     /** {@inheritDoc} */
     @Override
     public void setBindIP(final String ip) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public int getLocalPort() {
         return msn.getConnection().getInternalPort();
     }
 
     /**
      * Adds a contact to the list of known clients.
      *
      * @param contact Contact to add
      */
     public void addClient(final MsnContact contact) {
         final String email = contact.getEmail().getEmailAddress();
         final String[] parts = parseHostmask(email);
         clients.put(email, new MSNClientInfo(contact, this, parts[0], parts[1],
                 parts[2]));
     }
 
     /**
      * Removes a contact from the list of known clients.
      *
      * @param contact Contact to remove
      */
     public void removeClient(final MsnContact contact) {
         clients.remove(contact.getEmail().getEmailAddress());
     }
 
     /** {@inheritDoc} */
     @Override
     public void setCompositionState(final String host, final CompositionState state) {
         // Do nothing
     }
 
     /** {@inheritDoc} */
     @Override
     public void requestGroupList(final String searchTerms) {
         // Do nothing
     }
 }
