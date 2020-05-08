 package com.ninchat.offhand;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.SocketAddress;
 import java.nio.channels.AsynchronousChannelGroup;
 import java.nio.channels.AsynchronousSocketChannel;
 import java.nio.channels.CompletionHandler;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public abstract class ConnectingConsumer extends Consumer
 {
 	private static final Log log = LogFactory.getLog(ConnectingConsumer.class);
 
 	protected class Peer extends Consumer.Peer
 	{
 		private final SocketAddress address;
 
 		protected Peer(SocketAddress address)
 		{
 			this.address = address;
 		}
 
 		protected void reconnect() throws IOException
 		{
 			assert this.channel == null;
 
 			AsynchronousSocketChannel channel = AsynchronousSocketChannel.open(channelGroup);
 			channel.connect(address, null, new Connection());
 
 			this.channel = channel;
 		}
 
 		private class Connection implements CompletionHandler<Void, Void>
 		{
 			public void completed(Void result, Void attachment)
 			{
 				log.debug("peer connected");
 				connected();
 			}
 
 			public void failed(Throwable e, Void attachment)
 			{
 				if (e instanceof ConnectException && "Connection refused".equals(e.getMessage())) {
 					log.error(Peer.this + " connection refused");
 				} else {
 					log.error(Peer.this, e);
 				}
 
 				disconnect();
 			}
 		}
 
 		void disconnect()
 		{
 			super.disconnect();
 			disconnecting(this);
 		}
 
 		public String toString()
 		{
 			return super.toString() + " " + this.address.toString();
 		}
 	}
 
 	protected List<Peer> peers = new ArrayList<Peer>();
 	protected Set<Peer> disconnected = new HashSet<Peer>();
 
 	public ConnectingConsumer(AsynchronousChannelGroup channelGroup)
 	{
 		super(channelGroup);
 	}
 
 	public void connect(SocketAddress address) throws IOException
 	{
 		log.debug("connecting peer");
 		Peer peer = new Peer(address);
 		peers.add(peer);
 		peer.reconnect();
 	}
 
 	public void reconnect()
 	{
 		Set<Peer> reconnecting = disconnected;
 		disconnected = new HashSet<Peer>();
 
 		for (Peer peer : reconnecting) {
 			try {
 				log.debug("reconnecting peer");
 				peer.reconnect();
 			} catch (IOException e) {
 				log.error(peer, e);
				disconnected.add(peer);
 			}
 		}
 	}
 
 	private void disconnecting(Peer peer)
 	{
 		disconnected.add(peer);
 	}
 
 	public void close()
 	{
 		for (Peer peer : peers) {
 			if (!disconnected.contains(peer)) {
 				log.debug("closing peer");
 				peer.close();
 			}
 		}
 	}
 }
