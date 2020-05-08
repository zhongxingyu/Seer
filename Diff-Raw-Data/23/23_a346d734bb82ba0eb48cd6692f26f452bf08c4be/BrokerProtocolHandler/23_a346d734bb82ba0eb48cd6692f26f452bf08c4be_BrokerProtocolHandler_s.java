 package pt.com.broker.client;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.concurrent.Sleep;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BaseBrokerClient.BrokerClientState;
 import pt.com.broker.client.messaging.PendingAcceptRequestsManager;
 import pt.com.broker.client.net.ProtocolHandler;
 import pt.com.broker.types.BindingSerializer;
 import pt.com.broker.types.NetAccepted;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetFault;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetProtocolType;
 import pt.com.broker.types.NetAction.ActionType;
 
 /**
  * BrokerProtocolHandler extends ProtocolHandler defining protocol aspects such as message handling (including errors), message encoding/decoding and on failure behavior.
  * 
  */
 public class BrokerProtocolHandler extends ProtocolHandler<NetMessage>
 {
 
 	private static final Logger log = LoggerFactory.getLogger(BrokerProtocolHandler.class);
 
 	private final BaseBrokerClient brokerClient;
 
 	long connectionVersion = 0;
 
 	private final BaseNetworkConnector connector;
 
 	private BindingSerializer serializer;
 
 	private short proto_type = 1;
 
 	private HostInfo hostInfo = null;
 
 	public BrokerProtocolHandler(BaseBrokerClient brokerClient, NetProtocolType ptype, BaseNetworkConnector connector) throws UnknownHostException, IOException, Throwable
 	{
 		this.brokerClient = brokerClient;
 		this.connector = connector;
 
 		setHostInfo(brokerClient.getHostInfo());
 
 		try
 		{
 			connector.connect();
 		}
 		catch (Throwable error)
 		{
 			final Throwable rootCause = ErrorAnalyser.findRootCause(error);
 			if (rootCause instanceof IOException)
 			{
 				onIOFailure(connector.getConnectionVersion());
 			}
 			else
 			{
 				throw error;
 			}
 		}
 
 		try
 		{
 			if (ptype == null)
 				ptype = NetProtocolType.PROTOCOL_BUFFER;
 
 			switch (ptype)
 			{
 			case SOAP:
 				proto_type = 0;
 				serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.xml.SoapBindingSerializer").newInstance();
 				break;
 			case PROTOCOL_BUFFER:
 				proto_type = 1;
 				serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.protobuf.ProtoBufBindingSerializer").newInstance();
 				break;
 			case THRIFT:
 				proto_type = 2;
 				serializer = (BindingSerializer) Class.forName("pt.com.broker.codec.thrift.ThriftBindingSerializer").newInstance();
 				break;
 			default:
 				throw new Exception("Invalid Protocol Type: " + ptype);
 			}
 
 		}
 		catch (Throwable t)
 		{
 			log.error("Put the binding implentation of your choice in the classpath and try again", t);
 			Shutdown.now();
 		}
 		brokerClient.setState(BrokerClientState.OK);
 	}
 
 	@Override
 	public BaseNetworkConnector getConnector()
 	{
 		return connector;
 	}
 
 	@Override
 	public void onConnectionClose()
 	{
 		log.debug("Connection Closed");
 	}
 
 	@Override
 	public void onConnectionOpen()
 	{
 		if (closed.get())
 			return;
 		log.debug("Connection Opened");
 		try
 		{
 			brokerClient.sendSubscriptions();
 		}
 		catch (Throwable t)
 		{
 			log.error(t.getMessage(), t);
 		}
 	}
 
 	protected synchronized void onIOFailure(long connectionVersion)
 	{
 		if (connectionVersion == this.connectionVersion)
 		{
 			log.warn("onIoFailure -  connectionVersion: '{}'", connectionVersion);
 			this.brokerClient.setState(BrokerClientState.FAIL);
 			int count = 0;
 
 			do
 			{
 				try
 				{
 					// Close existing connections
 					this.connector.close();
 
 					// Try to reconnected them
 					this.brokerClient.setState(BrokerClientState.CONNECT);
 					long newConnectionVersion = ++this.connectionVersion;
 					this.connector.connect(getHostInfo(), newConnectionVersion);
 
 					this.brokerClient.setState(BrokerClientState.OK);
 
 					// READ THREADS
 					start();
 
 					// AUTH
 					if (this.brokerClient instanceof SslBrokerClient)
 					{
 						SslBrokerClient sslClient = (SslBrokerClient) this.brokerClient;
 						if (sslClient.isAuthenticationRequired())
 						{
 							this.brokerClient.setState(BrokerClientState.AUTH);
 							if (sslClient.hasCredentialsProvider())
 							{
 								sslClient.renewCredentials();
 							}
 							sslClient.authenticateClient();
 							this.brokerClient.setState(BrokerClientState.OK);
 						}
 
 					}
 
 					// Send subs
 					onConnectionOpen();
 
 					this.notifyAll();
 
 					return;
 				}
 				catch (Throwable t)
 				{
 					log.error("Failed to reconnect to agent " + getHostInfo().getHostname() + ":" + getHostInfo().getPort());
 					if (getNumberOfTries() == 0)
 					{
 						throw new RuntimeException(t);
 					}
 					Sleep.time((++count) * 500);
 				}
 				setHostInfo(brokerClient.getHostInfo());
 			}
 			while (count != getNumberOfTries());
 
 			this.brokerClient.setState(BrokerClientState.CLOSE);
 			this.notifyAll();
 
 			onError(new Exception("Unable to reconnect after " + getNumberOfTries() + " tries!"));
 		}
 
 	}
 
 	@Override
 	public void onError(Throwable error)
 	{
 		brokerClient.getErrorListener().onError(error);
 	}
 
 	public final static String PollTimeoutErrorMessageCode = NetFault.PollTimeoutErrorMessage.getAction().getFaultMessage().getCode();
 	public final static String NoMessageInQueueErrorMessageCode = NetFault.NoMessageInQueueErrorMessage.getAction().getFaultMessage().getCode();
 
 	public static final NetMessage TimeoutUnblockNotification = new NetMessage(new NetAction(NetAction.ActionType.FAULT));
 	public static final NetMessage NoMessageUnblockNotification = new NetMessage(new NetAction(NetAction.ActionType.FAULT));
 
 	@Override
 	protected void handleReceivedMessage(NetMessage message)
 	{
 		NetAction action = message.getAction();
 		// NetAction action = null;
 		// try
 		// {
 		message.getAction();
 
 		switch (action.getActionType())
 		{
 		case NOTIFICATION:
 			NetNotification notification = action.getNotificationMessage();
 			if (!notification.getDestinationType().equals(NetAction.DestinationType.TOPIC))
 			{
 				boolean received = brokerClient.offerPollResponse(notification.getSubscription(), message);
 				if (received)
 				{
 					return;
 				}
 
 			}
 
 			brokerClient.notifyListener(notification);
 
 			break;
 		case PONG:
 			try
 			{
 				brokerClient.feedStatusConsumer(action.getPongMessage());
 			}
 			catch (Throwable e)
 			{
 				brokerClient.getErrorListener().onError(e);
 			}
 			break;
 		case FAULT:
 			NetFault fault = action.getFaultMessage();
 
 			if (fault.getCode().equals(PollTimeoutErrorMessageCode) || fault.getCode().equals(NoMessageInQueueErrorMessageCode))
 			{
 				String destination = fault.getDetail();
 
 				if (fault.getCode().equals(PollTimeoutErrorMessageCode) && brokerClient.offerPollResponse(destination, TimeoutUnblockNotification))
 				{
 					return;
 				}
 				else if (brokerClient.offerPollResponse(destination, NoMessageUnblockNotification))
 				{
 					return;
 				}
 				log.error("A PollTimeout or NoMessageInQueue fault message was received but there wasn't a sync consumer.");
 			}
 
 			if (fault.getActionId() != null)
 			{
 				// Give pending requests a change to process error messages
 				if (PendingAcceptRequestsManager.messageFailed(fault))
 					return;
 			}
 			brokerClient.getErrorListener().onFault(fault);
 			break;
 		case ACCEPTED:
 			NetAccepted accepted = action.getAcceptedMessage();
 			PendingAcceptRequestsManager.acceptedMessageReceived(accepted.getActionId());
 			break;
 		default:
 			throw new RuntimeException("Unexepected ActionType in received message. ActionType: " + action.getActionType());
 
 		}
 		// }
 		// catch (NullPointerException npe)
 		// {
 		// System.err.println("Null Pointer ex");
 		// }
 	}
 
 	@Override
 	public NetMessage decode(DataInputStream in) throws IOException
 	{
		try
		{
 			short protocolType = in.readShort();
 			short protocolVersion = in.readShort();
 			int len = in.readInt();
 
 			if (serializer == null)
 			{
 				throw new RuntimeException("Received message uses an unknown encoding");
 			}
 
 			byte[] data = new byte[len];
 			in.readFully(data);
 
 			NetMessage message = (NetMessage) serializer.unmarshal(data);
 			return message;
		}
		catch (SocketException se)
		{
			if (!closed.get())
			{
				log.error("Failed to read socket...", se);
			}
		}
		return null;
 	}
 
 	@Override
 	public void encode(NetMessage message, DataOutputStream out) throws IOException
 	{
 		short protocolType = proto_type;
 		short protocolVersion = (short) 0;
 
 		byte[] encodedMsg = serializer.marshal(message);
 
 		out.writeShort(protocolType);
 		out.writeShort(protocolVersion);
 		out.writeInt(encodedMsg.length);
 
 		out.write(encodedMsg);
 	}
 
 	@Override
 	public void sendMessage(final NetMessage message) throws Throwable
 	{
 		if (brokerClient.getState() == BrokerClientState.CLOSE)
 		{
 			onError(new Exception("Message cannot be sent because client was closed"));
 			return;
 		}
 
 		if ((brokerClient.getState() == BrokerClientState.OK) || ((brokerClient.getState() == BrokerClientState.AUTH) && message.getAction().getActionType() == ActionType.AUTH))
 		{
 			super.sendMessage(message);
 			return;
 		}
 		synchronized (this)
 		{
 			while ((brokerClient.getState() != BrokerClientState.OK) && (brokerClient.getState() != BrokerClientState.CLOSE))
 			{
 				this.wait();
 			}
 			if ((brokerClient.getState() == BrokerClientState.OK))
 			{
 				super.sendMessage(message);
 				return;
 			}
 			// BrokerCliet state is CLOSE. onFailure already invoked. Notify
 			// message lost
 			onError(new Exception("Message Lost due to failure of agent"));
 			log.error("Message Lost due to failure of agent!");
 		}
 	}
 
 	public BaseBrokerClient getBrokerClient()
 	{
 		return brokerClient;
 	}
 
 	public void setHostInfo(HostInfo hostInfo)
 	{
 		this.hostInfo = hostInfo;
 	}
 
 	public HostInfo getHostInfo()
 	{
 		return hostInfo;
 	}
 
 	public void setNumberOfTries(int numberOfTries)
 	{
 		this.brokerClient.setNumberOfTries(numberOfTries);
 	}
 
 	@Override
 	public int getNumberOfTries()
 	{
 		return this.brokerClient.getNumberOfTries();
 	}
 }
