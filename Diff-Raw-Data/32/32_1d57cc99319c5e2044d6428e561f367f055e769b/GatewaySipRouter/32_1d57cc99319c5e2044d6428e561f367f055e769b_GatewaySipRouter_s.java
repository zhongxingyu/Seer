 /*
  * Copyright (C) 2011 - Jingle Nodes - Yuilop - Neppo
  *
  *   This file is part of Switji (http://jinglenodes.org)
  *
  *   Switji is free software; you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation; either version 2 of the License, or
  *   (at your option) any later version.
  *
  *   Switji is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with MjSip; if not, write to the Free Software
  *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  *   Author(s):
  *   Benhur Langoni (bhlangonijr@gmail.com)
  *   Thiago Camargo (barata7@gmail.com)
  */
 
 package org.jinglenodes.sip.router;
 
 import org.apache.log4j.Logger;
 import org.jinglenodes.sip.SipPacketProcessor;
 import org.jinglenodes.sip.SipRouter;
 import org.jinglenodes.sip.account.SipAccount;
 import org.jinglenodes.sip.account.SipAccountProvider;
 import org.xmpp.jnodes.nio.DatagramListener;
 import org.xmpp.jnodes.nio.ListenerDatagramChannel;
 import org.xmpp.packet.JID;
 import org.zoolu.sip.address.SipURL;
 import org.zoolu.sip.message.Message;
 import org.zoolu.sip.message.SipChannel;
 import org.zoolu.sip.provider.SipProviderInfoInterface;
 import org.zoolu.sip.provider.SipProviderInformation;
 import org.zoolu.tools.NamingThreadFactory;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.SocketAddress;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 public class GatewaySipRouter implements SipRouter, DatagramListener {
 
     static final Logger log = Logger.getLogger(GatewaySipRouter.class);
     private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(5, new NamingThreadFactory("DynamicMultiPortSipRouterThread"));
     private final List<SipRoutingListener> routingListeners = new ArrayList<SipRoutingListener>();
     private final List<SipPacketProcessor> packetProcessors = new ArrayList<SipPacketProcessor>();
     private final List<SipPacketFilter> packetFilters = new ArrayList<SipPacketFilter>();
     private final ConcurrentHashMap<String, SipChannel> channels = new ConcurrentHashMap<String, SipChannel>();
     private final ConcurrentHashMap<Integer, SipChannel> iChannels = new ConcurrentHashMap<Integer, SipChannel>();
 
     private final String localSipIp;
 
     // SIP Routing
     private final SipAccountProvider sipAccountProvider;
     private final int keepAliveDelay = 15; // 10 seconds delay in the keep alive packets
     private final SipProviderInfoInterface sipProvider;
 
    public GatewaySipRouter(final String localSipIp, final SipAccountProvider sipAccountProvider, final String fakeLocalIp, final int fakeLocalPort) {
         this.localSipIp = localSipIp;
         this.sipAccountProvider = sipAccountProvider;
         this.sipProvider = new SipProviderInformation(fakeLocalIp, fakeLocalPort);
         createSipRouter();
     }
 
     private void createSipRouter() {
         DynamicKeepAliveTask keepAliveTask = new DynamicKeepAliveTask(this);
         scheduledThreadPoolExecutor.scheduleWithFixedDelay(keepAliveTask, keepAliveDelay, keepAliveDelay, TimeUnit.SECONDS);
     }
 
     public Collection<SipChannel> getSipChannels() {
         return channels.values();
     }
 
     public void destroyChannel(final SipChannel channel) {
         iChannels.remove(channel.getDatagramChannel().hashCode());
         channel.closeDatagramChannel();
     }
 
     public SipChannel getSipChannel(String id) {
         return channels.get(id);
     }
 
     public void routeSIP(final Message message, final JID sender) {
 
 
         for (final SipRoutingListener routingListener : routingListeners) {
             routingListener.routingSIP(message, sender);
         }
 
         // Route Packet
         final byte[] bytes;
         try {
 
             final String msg = message.toString();
 
             bytes = msg.getBytes("UTF-8");
             final ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
             final SocketAddress destination = getDestinationAddress(message, sender);
 
             if (destination == null) {
                 log.warn("Discarding Packet Without Destination: " + msg);
                 return;
             }
 
             try {
 
                 SipChannel c = message.getArrivedAt();
 
                 if (c == null) {
                     c = channels.get(getSipChannelID(sender));
                 }
 
                 if (sender != null && c == null) {
                     log.debug("Creating SIP Channel for: " + sender);
                     c = createSipChannel(sender, destination);
                 }
 
                 if (c == null) {
                     log.warn("Could NOT Bind Message to any SipChannel.");
                     return;
                 }
 
                 log.debug("Sending SIP: " + msg);
 
                 c.send(byteBuffer, destination);
 
                 updateKeepAliveTargets(destination);
 
                 for (final SipRoutingListener routingListener : getRoutingListeners()) {
                     routingListener.routedSIP(message, sender);
                 }
 
             } catch (Exception e) {
                 log.warn("Unresolved Address.", e);
                 for (final SipRoutingListener routingListener : getRoutingListeners()) {
                     routingListener.routingError(message, sender, SipRoutingError.unresolvedAddress);
                 }
             }
 
         } catch (UnsupportedEncodingException e) {
             log.error("Packet Encoding Error.", e);
         }
 
     }
 
     public SipChannel createSipChannel(JID sender, SocketAddress destination) throws IOException {
        final SipChannel c = new SipChannel(getSipChannelID(sender), destination, localSipIp, this);
         channels.put(getSipChannelID(sender), c);
         iChannels.put(c.getDatagramChannel().hashCode(), c);
         return c;
     }
 
     public String getSipChannelID(final JID sender) {
         if (sender == null) {
             return null;
         }
         return "fixed"; //sender.toBareJID();
     }
 
     public SipChannel getSipChannel(final JID sender) {
         return channels.get(getSipChannelID(sender));
     }
 
     SocketAddress getDestinationAddress(final Message message, final JID sender) {
 
         if (message.getSendTo() != null) {
             return message.getSendTo();
         }
 
         final SipAccount sipAccount = sipAccountProvider.getSipAccount(sender);
         if (sipAccount != null) {
             final SocketAddress result = CachedAddressResolver.getInstance().getSocketAddress(sipAccount.getOutboundproxy());
             message.setSendTo(result);
             return result;
         }
 
         if (message.getToHeader() != null) {
             final SipURL sipUrl = message.getToHeader().getNameAddress().getAddress();
             final SocketAddress result = CachedAddressResolver.getInstance().getSIPSocketAddress(sipUrl.getHost(), sipUrl.getPort());
             message.setSendTo(result);
             return result;
         }
 
         return null;
 
     }
 
     public SipProviderInfoInterface getSipProvider() {
         return sipProvider;
     }
 
     public void handlePacketReceived(ByteBuffer byteBuffer, SocketAddress address, final SipChannel channel) {
         log.debug("Handling SIP Packet Received...");
         int i = 0;
         for (final SipPacketProcessor packetProcessor : packetProcessors) {
             log.debug("Processing SIP Packet Received (" + (i++) + "):" + packetProcessor.getClass().getCanonicalName());
             packetProcessor.processSipPacket(byteBuffer, address, channel);
         }
     }
 
     public void addPacketFilter(final SipPacketFilter filter) {
         packetFilters.add(filter);
     }
 
     public void addRoutingListener(final SipRoutingListener routingListener) {
         routingListeners.add(routingListener);
     }
 
     public void removeRoutingListener(final SipRoutingListener routingListener) {
         routingListeners.remove(routingListener);
     }
 
     public void addPacketProcessor(final SipPacketProcessor sipPacketProcessor) {
         log.debug("Added SipPacketProcessor: " + sipPacketProcessor.getClass().getCanonicalName());
         packetProcessors.add(sipPacketProcessor);
     }
 
     public void removePacketProcessor(final SipPacketProcessor sipPacketProcessor) {
         packetProcessors.remove(sipPacketProcessor);
     }
 
     public List<SipRoutingListener> getRoutingListeners() {
         return routingListeners;
     }
 
     public Collection<SipChannel> getKeepAliveTargets() {
         //TODO Filter Single Destinations!!!
         return channels.values();
     }
 
     void updateKeepAliveTargets(final SocketAddress address) {
     }
 
     public void shutdown() {
         scheduledThreadPoolExecutor.shutdownNow();
     }
 
     public void packetReceived(ByteBuffer byteBuffer, SocketAddress address, SipChannel channel) {
 
         for (final SipPacketFilter filter : packetFilters) {
             if (!filter.acceptPacket(byteBuffer, address, channel)) {
                 // Discard Packet
                 return;
             }
         }
 
         handlePacketReceived(byteBuffer, address, channel);
     }
 
     public void cleanUpCachedAddressResolver() {
         CachedAddressResolver.getInstance().cleanUp();
     }
 
     public void destroyChannel(final String id) {
         final SipChannel sipChannel = channels.remove(id);
         if (sipChannel != null) {
             iChannels.remove(sipChannel.getDatagramChannel().hashCode());
             try {
                 sipChannel.getDatagramChannel().close();
             } catch (IOException e) {
                 log.error("Could not Close Channel: " + id, e);
             }
         }
     }
 
     public void datagramReceived(ListenerDatagramChannel listenerDatagramChannel, ByteBuffer byteBuffer, SocketAddress address) {
         log.debug("Datagram Received on: " + address.toString());
         packetReceived(byteBuffer, address, iChannels.get(listenerDatagramChannel.hashCode()));
     }
 
 }
