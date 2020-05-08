 package pt.com.gcs.messaging;
 
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.text.StringUtils;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.jboss.netty.channel.ChannelHandler.Sharable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.CriticalErrors;
 import pt.com.broker.types.MessageListener;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetAction.DestinationType;
 import pt.com.broker.types.channels.ChannelAttributes;
 import pt.com.broker.types.channels.ListenerChannelFactory;
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.conf.GlobalConfig;
 import pt.com.gcs.messaging.GlobalConfigMonitor.GlobalConfigModifiedListener;
 import pt.com.gcs.net.Peer;
 
 /**
  * GcsAcceptorProtocolHandler is an NETTY SimpleChannelHandler. It handles remote subscription messages and acknowledges from other agents.
  */
 
 @Sharable
 class GcsAcceptorProtocolHandler extends SimpleChannelHandler
 {
 	private static Logger log = LoggerFactory.getLogger(GcsAcceptorProtocolHandler.class);
 	private static final Charset UTF8 = Charset.forName("UTF-8");
 
 	private static List<InetSocketAddress> peersAddressList;
 
 	static
 	{
 		createPeersList();
 		GlobalConfigMonitor.addGlobalConfigModifiedListener(new GlobalConfigModifiedListener()
 		{
 			@Override
 			public void globalConfigModified()
 			{
 				globalConfigReloaded();
 			}
 		});
 	}
 
 	private static void createPeersList()
 	{
 		List<Peer> peerList = GlobalConfig.getPeerList();
 		peersAddressList = new ArrayList<InetSocketAddress>(peerList.size());
 		for (Peer peer : peerList)
 		{
 			InetSocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
 			peersAddressList.add(addr);
 		}
 	}
 
 	public static void globalConfigReloaded()
 	{
 		createPeersList();
 	}
 
 	@Override
 	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(e.getCause());
 		CriticalErrors.exitIfCritical(rootCause);
 		log.error("Exception Caught:'{}', '{}'", ctx.getChannel().getRemoteAddress().toString(), rootCause.getMessage());
 		log.error("STACKTRACE", rootCause);
 	}
 
 	@Override
 	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception
 	{
 		final NetMessage m = (NetMessage) e.getMessage();
 		String mtype = m.getHeaders().get("TYPE");
 
 		NetNotification nnot = m.getAction().getNotificationMessage();
 		NetBrokerMessage brkMsg = nnot.getMessage();
 
 		String msgContent = new String(brkMsg.getPayload(), "UTF-8");
 
 		if (log.isDebugEnabled())
 		{
 			log.debug(String.format("Message Received from: '%s', Destination: '%s', Type: '%s', MsgId: '%s'", ctx.getChannel().getRemoteAddress(), nnot.getDestination(), mtype, brkMsg.getMessageId()));
 		}
 
 		if (mtype.equals("ACK"))
 		{
 			Gcs.ackMessage(nnot.getDestination(), brkMsg.getMessageId());
 			return;
 		}
 		else if (mtype.equals("HELLO"))
 		{
 			Peer peer = Peer.createPeerFromHelloMessage(msgContent);
 			if (peer == null)
 			{
 				log.error("Invalid 'HELLO' message: ", msgContent);
 				return;
 			}
 
 			validatePeer(ctx, peer, msgContent);
 			boolean isValid = ((Boolean) ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.ISVALID")).booleanValue();
 			if (!isValid)
 			{
 				String paddr = String.valueOf(ChannelAttributes.get(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.PEER_ADDRESS"));
 				log.error("A peer from \"{}\" tried to connect but it does not appear in the world map.", paddr);
 				ctx.getChannel().close();
 			}
 			else
 			{
 				log.debug("Peer is valid!");
 
 				ChannelHandlerContext previousChannel = RemoteChannels.add(peer.getAddress(), ctx);
 				if (previousChannel != null)
 				{
 					log.info(String.format("Peer '%s' connected through channel '%s' was connected through channel '%s'", peer.getAddress(), ctx.getChannel().toString(), previousChannel.getChannel().toString()));
 
					handleChannelClosed(ctx);
 				}
 
 				return;
 			}
 			return;
 		}
 		else if (mtype.equals("SYSTEM_TOPIC") || mtype.equals("SYSTEM_QUEUE"))
 		{
 
 			final String action = extract(msgContent, "<action>", "</action>");
 			final String src_name = extract(msgContent, "<source-name>", "</source-name>");
 
 			final String subscriptionKey = extract(msgContent, "<destination>", "</destination>");
 
 			if (StringUtils.isBlank(subscriptionKey))
 			{
 				String errorMessage = String.format("Sytem Queue or Topic message has a blank destination field. Message content: %s", msgContent);
 				log.error(errorMessage);
 				throw new RuntimeException(errorMessage);
 			}
 
 			if (StringUtils.isBlank(action))
 			{
 				String errorMessage = String.format("Sytem Queue or Topic message has a blank action field. Message content: %s", msgContent);
 				log.error(errorMessage);
 				throw new RuntimeException(errorMessage);
 			}
 
 			if (log.isInfoEnabled())
 			{
 				String lmsg = String.format("Action: '%s' Consumer; Subscription: '%s'; Source: '%s'", action, subscriptionKey, src_name);
 				log.info(lmsg);
 			}
 
 			acknowledgeSystemMessage(brkMsg.getMessageId(), ctx);
 
 			if (mtype.equals("SYSTEM_TOPIC"))
 			{
 				MessageListener remoteListener = new RemoteListener(ListenerChannelFactory.getListenerChannel(ctx.getChannel()), subscriptionKey, DestinationType.TOPIC, DestinationType.TOPIC);
 
 				TopicProcessor tp = TopicProcessorList.get(subscriptionKey);
 
 				if (tp == null)
 				{
 					log.error("Failed to obtain a TopicProcessor instance for topic '{}'.", subscriptionKey);
 					return;
 				}
 
 				if (action.equals("CREATE"))
 				{
 					tp.add(remoteListener, false);
 				}
 				else if (action.equals("DELETE"))
 				{
 					tp.remove(remoteListener);
 				}
 
 			}
 			else if (mtype.equals("SYSTEM_QUEUE"))
 			{
 				MessageListener remoteListener = new RemoteListener(ListenerChannelFactory.getListenerChannel(ctx.getChannel()), subscriptionKey, DestinationType.QUEUE, DestinationType.QUEUE);
 
 				QueueProcessor qp = QueueProcessorList.get(subscriptionKey);
 				if (qp == null)
 				{
 					log.error("Failed to obtain a QueueProcessor instance for queue '{}'.", subscriptionKey);
 					return;
 				}
 
 				if (action.equals("CREATE"))
 				{
 					qp.add(remoteListener);
 				}
 				else if (action.equals("DELETE"))
 				{
 					qp.remove(remoteListener);
 				}
 			}
 		}
 		else
 		{
 			log.warn("Unkwown message type. Don't know how to handle message");
 		}
 	}
 
 	private void acknowledgeSystemMessage(String messageId, ChannelHandlerContext ctx)
 	{
 		Channel channel = ctx.getChannel();
 
 		final String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><message-id>%s</message-id></sysmessage>";
 		String payload = String.format(ptemplate, "SYSTEM_ACKNOWLEDGE", GcsInfo.getAgentName(), channel.getLocalAddress().toString(), messageId);
 
 		NetBrokerMessage brkMsg = new NetBrokerMessage(payload.getBytes(UTF8));
 
 		NetNotification notification = new NetNotification("/system/peer", DestinationType.TOPIC, brkMsg, "/system/peer");
 
 		NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
 		naction.setNotificationMessage(notification);
 
 		NetMessage nmsg = new NetMessage(naction);
 		nmsg.getHeaders().put("TYPE", "SYSTEM_ACK");
 
 		if (log.isDebugEnabled())
 		{
 			log.debug(String.format("Acknowledging System Message. Payload: %s", payload));
 
 		}
 
 		if (channel.isWritable())
 		{
 			channel.write(nmsg);
 		}
 		else
 		{
 			log.warn(String.format("Can ack system message because the channel is not writable. Message id '%s' could not be sent to '%s'. Closing connection.", messageId, channel.getRemoteAddress().toString()));
 			channel.close();
 		}
 	}
 
 	@Override
 	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
 	{
 		super.channelClosed(ctx, e);
 		handleChannelClosed(ctx);
 	}
 
 	private void handleChannelClosed(ChannelHandlerContext ctx)
 	{
 		log.info("Session Closed: '{}'", ctx.getChannel().getRemoteAddress());
 
 		TopicProcessorList.removeSession(ctx.getChannel());
 		QueueProcessorList.removeSession(ctx.getChannel());
 
 		if (!RemoteChannels.remove(ctx))
 		{
 			log.warn("Failed to remove '{}' from RemoteChannels. It should be there.", ctx.getChannel());
 		}
 
 		ChannelAttributes.remove(ChannelAttributes.getChannelId(ctx));
 		ListenerChannelFactory.channelClosed(ctx.getChannel());
 	}
 
 	private boolean validPeerAddress(ChannelHandlerContext ctx)
 	{
 		InetSocketAddress remotePeer = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
 		InetAddress address = remotePeer.getAddress();
 
 		for (InetSocketAddress addr : peersAddressList)
 		{
 			if (address.equals(addr.getAddress()))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
 	{
 		super.channelConnected(ctx, e);
 		log.info("Session Opened: '{}'", ctx.getChannel().getRemoteAddress());
 
 		if (!validPeerAddress(ctx))
 		{
 			ctx.getChannel().close();
 			log.warn("GCS: connection refused");
 			return;
 		}
 		if (log.isDebugEnabled())
 		{
 			log.debug("Session Created: '{}'", ctx.getChannel().getRemoteAddress());
 		}
 	}
 
 	private void validatePeer(ChannelHandlerContext ctx, Peer peer, String helloMessage)
 	{
 		log.debug("\"Hello\" message received: '{}'", helloMessage);
 		try
 		{
 			ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.PEER_ADDRESS", peer.getAddress());
 			if (Gcs.getPeerList().contains(peer))
 			{
 				log.debug("Peer '{}' exists in the world map'", peer.toString());
 				ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.ISVALID", true);
 				return;
 			}
 		}
 		catch (Throwable t)
 		{
 			ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.PEER_ADDRESS", "Unknown address");
 
 			log.error(t.getMessage(), t);
 		}
 		ChannelAttributes.set(ChannelAttributes.getChannelId(ctx), "GcsAcceptorProtocolHandler.ISVALID", false);
 	}
 
 	private String extract(String ins, String prefix, String sufix)
 	{
 		if (StringUtils.isBlank(ins))
 		{
 			return "";
 		}
 
 		int s = ins.indexOf(prefix) + prefix.length();
 		int e = ins.indexOf(sufix);
 		return ins.substring(s, e);
 	}
 }
