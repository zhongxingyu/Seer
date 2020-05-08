 package pt.com.broker.codec.protobuf;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.codec.protobuf.PBMessage.Atom;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Accepted;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.AcknowledgeMessage;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Action;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Authentication;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.BrokerMessage;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Fault;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Header;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Notification;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Parameter;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Ping;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Poll;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Pong;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Publish;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Subscribe;
 import pt.com.broker.codec.protobuf.PBMessage.Atom.Unsubscribe;
 import pt.com.broker.types.BindingSerializer;
 import pt.com.broker.types.NetAccepted;
 import pt.com.broker.types.NetAcknowledge;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetAuthentication;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetFault;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetPing;
 import pt.com.broker.types.NetPoll;
 import pt.com.broker.types.NetPong;
 import pt.com.broker.types.NetProtocolType;
 import pt.com.broker.types.NetPublish;
 import pt.com.broker.types.NetSubscribe;
 import pt.com.broker.types.NetUnsubscribe;
 import pt.com.broker.types.NetAction.DestinationType;
 import pt.com.broker.types.stats.EncodingStats;
 
 import com.google.protobuf.ByteString;
 
 /**
  * Google Protocol Buffer utility class for encoding and decoding.
  * 
  */
 
 public class ProtoBufBindingSerializer implements BindingSerializer
 {
 	private static final Logger log = LoggerFactory.getLogger(ProtoBufBindingSerializer.class);
 
 	static private NetAction.ActionType translate(PBMessage.Atom.Action.ActionType actionType)
 	{
 		switch (actionType)
 		{
 		case ACCEPTED:
 			return NetAction.ActionType.ACCEPTED;
 		case ACKNOWLEDGE_MESSAGE:
 			return NetAction.ActionType.ACKNOWLEDGE;
 		case FAULT:
 			return NetAction.ActionType.FAULT;
 		case NOTIFICATION:
 			return NetAction.ActionType.NOTIFICATION;
 		case POLL:
 			return NetAction.ActionType.POLL;
 		case PUBLISH:
 			return NetAction.ActionType.PUBLISH;
 		case SUBSCRIBE:
 			return NetAction.ActionType.SUBSCRIBE;
 		case UNSUBSCRIBE:
 			return NetAction.ActionType.UNSUBSCRIBE;
 		case PING:
 			return NetAction.ActionType.PING;
 		case PONG:
 			return NetAction.ActionType.PONG;
 		case AUTH:
 			return NetAction.ActionType.AUTH;
 
 		}
 		throw new IllegalArgumentException("Unexpected action type (PBMessage.Atom.Action.ActionType): " + actionType);
 	}
 
 	static private NetAction.DestinationType translate(PBMessage.Atom.DestinationType destinationType)
 	{
 		switch (destinationType)
 		{
 		case QUEUE:
 			return NetAction.DestinationType.QUEUE;
 		case TOPIC:
 			return NetAction.DestinationType.TOPIC;
 		case VIRTUAL_QUEUE:
 			return NetAction.DestinationType.VIRTUAL_QUEUE;
 		}
 		throw new IllegalArgumentException("Unexpected destination type (PBMessage.Atom.DestinationType): " + destinationType);
 	}
 
 	protected NetMessage constructMessage(PBMessage.Atom atom)
 	{
 		Map<String, String> parameters = null;
 		if (atom.hasHeader())
 		{
 			parameters = extractParameters(atom.getHeader());
 		}
 
 		NetMessage message = new NetMessage(extractAction(atom.getAction()), parameters);
 		return message;
 	}
 
 	private NetAccepted extractAcceptedMessage(Action action)
 	{
 		NetAccepted netAccepted = new NetAccepted(action.getAccepted().getActionId());
 
 		return netAccepted;
 	}
 
 	private NetAcknowledge extractAcknowledgeMessage(Action action)
 	{
 		AcknowledgeMessage protoBufAckMsg = action.getAckMessage();
 		String destination = protoBufAckMsg.getDestination();
 		String messageId = protoBufAckMsg.getMessageId();
 		NetAcknowledge ackMessage = new NetAcknowledge(destination, messageId);
 		if (action.getAckMessage().hasActionId())
 			ackMessage.setActionId(action.getAckMessage().getActionId());
 
 		return ackMessage;
 	}
 
 	private NetAction extractAction(Action action)
 	{
 		NetAction.ActionType actionType = translate(action.getActionType());
 		NetAction netAction = new NetAction(actionType);
 
 		switch (actionType)
 		{
 		case ACCEPTED:
 			netAction.setAcceptedMessage(extractAcceptedMessage(action));
 			break;
 		case ACKNOWLEDGE:
 			netAction.setAcknowledgeMessage(extractAcknowledgeMessage(action));
 			break;
 		case FAULT:
 			netAction.setFaultMessage(extractFaultMessage(action));
 			break;
 		case NOTIFICATION:
 			netAction.setNotificationMessage(extractNotificationMessage(action));
 			break;
 		case POLL:
 			netAction.setPollMessage(extractPollMessage(action));
 			break;
 		case PUBLISH:
 			netAction.setPublishMessage(extractPublishMessage(action));
 			break;
 		case SUBSCRIBE:
 			netAction.setSubscribeMessage(extractSubscribeMessage(action));
 			break;
 		case UNSUBSCRIBE:
 			netAction.setUnsbuscribeMessage(extractUnsubscribeMessage(action));
 			break;
 		case PING:
 			netAction.setPingMessage(extractPingMessage(action));
 			break;
 		case PONG:
 			netAction.setPongMessage(extractPongMessage(action));
 			break;
 		case AUTH:
 			netAction.setAuthenticationMessage(extractAuthenticationMessage(action));
 			break;
 		}
 		return netAction;
 	}
 
 	private NetAuthentication extractAuthenticationMessage(Action action)
 	{
 		Atom.Authentication clientAuth = action.getAuth();
 
 		NetAuthentication netClientAuth = new NetAuthentication(clientAuth.getToken().toByteArray(), clientAuth.getAuthenticationType());
 
 		if (clientAuth.hasActionId())
 			netClientAuth.setActionId(clientAuth.getActionId());
 		if (clientAuth.hasUserId())
 			netClientAuth.setUserId(clientAuth.getUserId());
 		if (clientAuth.getRoleCount() != 0)
 			netClientAuth.setRoles(clientAuth.getRoleList());
 
 		return netClientAuth;
 	}
 
 	private NetFault extractFaultMessage(Action action)
 	{
 		Fault fault = action.getFault();
 		String code = fault.getFaultCode();
 		String message = fault.getFaultMessage();
 
 		NetFault netFault = new NetFault(code, message);
 
 		if (fault.hasActionId())
 			netFault.setActionId(fault.getActionId());
 
 		if (fault.hasFaultDetail())
 			netFault.setDetail(fault.getFaultDetail());
 
 		return netFault;
 	}
 
 	private NetNotification extractNotificationMessage(Action action)
 	{
 		Notification notification = action.getNotification();
 
 		String dest = notification.getDestination();
 		NetAction.DestinationType destType = translate(notification.getDestinationType());
 		NetBrokerMessage brkMsg = obtainBrokerMessage(notification.getMessage());
 		String subs = notification.getSubscription();
 
 		NetNotification netNotification = new NetNotification(dest, destType, brkMsg, subs);
 
 		return netNotification;
 	}
 
 	private Map<String, String> extractParameters(Header header)
 	{
 
 		int paramsCount = header.getParameterCount();
 
 		Map<String, String> parameters = new HashMap<String, String>();
 
 		for (int i = 0; i != paramsCount; ++i)
 		{
 			Parameter param = header.getParameter(i);
 			parameters.put(param.getName(), param.getValue());
 		}
 
 		return parameters;
 	}
 
 	private NetPing extractPingMessage(Action action)
 	{
 		Atom.Ping ping = action.getPing();
 
 		NetPing netPing = new NetPing(ping.getActionId());
 
 		return netPing;
 	}
 
 	private NetPoll extractPollMessage(Action action)
 	{
 		Poll poll = action.getPoll();
 		String destination = poll.getDestination();
 
 		NetPoll pollMsg = new NetPoll(destination, poll.getTimeout());
 
 		if (poll.hasActionId())
 			pollMsg.setActionId(poll.getActionId());
 
 		return pollMsg;
 	}
 
 	private NetPong extractPongMessage(Action action)
 	{
 		Atom.Pong pong = action.getPong();
 
 		NetPong netPong = new NetPong(pong.getActionId());
 
 		return netPong;
 	}
 
 	private NetPublish extractPublishMessage(Action action)
 	{
 		Publish pub = action.getPublish();
 
 		String dest = pub.getDestination();
 		NetAction.DestinationType destType = translate(pub.getDestinationType());
 		NetBrokerMessage brkMsg = obtainBrokerMessage(pub.getMessage());
 
 		NetPublish netPub = new NetPublish(dest, destType, brkMsg);
 
 		if (pub.hasActionId())
 			netPub.setActionId(pub.getActionId());
 
 		return netPub;
 	}
 
 	private NetSubscribe extractSubscribeMessage(Action action)
 	{
 		Subscribe subs = action.getSubscribe();
 
 		String dest = subs.getDestination();
 		NetAction.DestinationType destType = translate(subs.getDestinationType());
 
 		NetSubscribe netSubs = new NetSubscribe(dest, destType);
 
 		if (subs.hasActionId())
 			netSubs.setActionId(subs.getActionId());
 
 		return netSubs;
 	}
 
 	private NetUnsubscribe extractUnsubscribeMessage(Action action)
 	{
 		Unsubscribe unsubs = action.getUnsubscribe();
 
 		String dest = unsubs.getDestination();
 		NetAction.DestinationType destType = translate(unsubs.getDestinationType());
 
 		NetUnsubscribe cgsUnsubs = new NetUnsubscribe(dest, destType);
 
 		if (unsubs.hasActionId())
 			cgsUnsubs.setActionId(unsubs.getActionId());
 
 		return cgsUnsubs;
 	}
 
 	private Accepted getAccepted(NetMessage netMessage)
 	{
 		NetAccepted gcsAccepted = netMessage.getAction().getAcceptedMessage();
 
 		PBMessage.Atom.Accepted.Builder builder = PBMessage.Atom.Accepted.newBuilder();
 		builder.setActionId(gcsAccepted.getActionId());
 
 		return builder.build();
 	}
 
 	private AcknowledgeMessage getAcknowledge(NetMessage netMessage)
 	{
 		NetAcknowledge net = netMessage.getAction().getAcknowledgeMessage();
 
 		PBMessage.Atom.AcknowledgeMessage.Builder builder = PBMessage.Atom.AcknowledgeMessage.newBuilder();
 
 		builder.setDestination(net.getDestination()).setMessageId(net.getMessageId());
 		if (net.getActionId() != null)
 			builder.setActionId(net.getActionId());
 
 		return builder.build();
 	}
 
 	private Action getAction(NetMessage netMessage)
 	{
 		PBMessage.Atom.Action.Builder builder = PBMessage.Atom.Action.newBuilder();
 
 		switch (netMessage.getAction().getActionType())
 		{
 		case ACCEPTED:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.ACCEPTED);
 			builder.setAccepted(getAccepted(netMessage));
 			break;
 		case ACKNOWLEDGE:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.ACKNOWLEDGE_MESSAGE);
 			builder.setAckMessage(getAcknowledge(netMessage));
 			break;
 		case FAULT:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.FAULT);
 			builder.setFault(getFault(netMessage));
 			break;
 		case NOTIFICATION:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.NOTIFICATION);
 			builder.setNotification(getNotification(netMessage));
 			break;
 		case POLL:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.POLL);
 			builder.setPoll(getPoll(netMessage));
 			break;
 		case PUBLISH:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.PUBLISH);
 			builder.setPublish(getPublish(netMessage));
 			break;
 		case SUBSCRIBE:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.SUBSCRIBE);
 			builder.setSubscribe(getSubscribe(netMessage));
 			break;
 		case UNSUBSCRIBE:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.UNSUBSCRIBE);
 			builder.setUnsubscribe(getUnsubscribe(netMessage));
 			break;
 		case PING:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.PING);
 			builder.setPing(getPing(netMessage));
 			break;
 		case PONG:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.PONG);
 			builder.setPong(getPong(netMessage));
 			break;
 		case AUTH:
 			builder.setActionType(PBMessage.Atom.Action.ActionType.AUTH);
 			builder.setAuth(getAuth(netMessage));
 		}
 		return builder.build();
 	}
 
 	private Authentication getAuth(NetMessage netMessage)
 	{
		NetAuthentication authClientAuthrentication = netMessage.getAction().getAuthenticationMessage();
 
 		PBMessage.Atom.Authentication.Builder builder = PBMessage.Atom.Authentication.newBuilder();
 
		builder.setToken(ByteString.copyFrom(authClientAuthrentication.getToken()));
 
		if (authClientAuthrentication.getActionId() != null)
			builder.setActionId(authClientAuthrentication.getActionId());
 
		if (authClientAuthrentication.getAuthenticationType() != null)
			builder.setAuthenticationType(authClientAuthrentication.getAuthenticationType());
 
		if (authClientAuthrentication.getUserId() != null)
			builder.setUserId(authClientAuthrentication.getUserId());
 
		if (authClientAuthrentication.getRoles() != null)
 		{
 			int i = 0;
			for (String role : authClientAuthrentication.getRoles())
 				builder.setRole(i++, role);
 		}
 		return builder.build();
 	}
 
 	private Fault getFault(NetMessage netMessage)
 	{
 		NetFault net = netMessage.getAction().getFaultMessage();
 
 		PBMessage.Atom.Fault.Builder builder = PBMessage.Atom.Fault.newBuilder();
 
 		builder.setFaultCode(net.getCode()).setFaultMessage(net.getMessage());
 
 		if (net.getActionId() != null)
 			builder.setActionId(net.getActionId());
 		if (net.getDetail() != null)
 			builder.setFaultDetail(net.getDetail());
 
 		return builder.build();
 	}
 
 	private PBMessage.Atom.Header getHeaders(NetMessage netMessage)
 	{
 		PBMessage.Atom.Header.Builder builder = PBMessage.Atom.Header.newBuilder();
 		boolean hasParams = false;
 
 		Map<String, String> params = netMessage.getHeaders();
 		if (params != null)
 		{
 			Iterator<String> it = params.keySet().iterator();
 			while (it.hasNext())
 			{
 				hasParams = true;
 				String k = it.next();
 				String v = params.get(k);
 
 				if ((k != null) && (v != null))
 					builder.addParameter(PBMessage.Atom.Parameter.newBuilder().setName(k).setValue(v));
 			}
 		}
 		if (hasParams)
 			return builder.build();
 		return null;
 	}
 
 	private BrokerMessage getMessageBroker(NetBrokerMessage message)
 	{
 		PBMessage.Atom.BrokerMessage.Builder builder = PBMessage.Atom.BrokerMessage.newBuilder();
 
 		builder.setPayload(ByteString.copyFrom(message.getPayload()));
 
 		if (message.getMessageId() != null)
 			builder.setMessageId(message.getMessageId());
 
 		if (message.getExpiration() != -1)
 			builder.setExpiration(message.getExpiration());
 
 		if (message.getTimestamp() != -1)
 			builder.setTimestamp(message.getTimestamp());
 
 		return builder.build();
 	}
 
 	private PBMessage.Atom.Notification getNotification(NetMessage netMessage)
 	{
 		NetNotification net = netMessage.getAction().getNotificationMessage();
 
 		String subs = StringUtils.trimToEmpty(net.getSubscription());
 
 		PBMessage.Atom.Notification.Builder builder = PBMessage.Atom.Notification.newBuilder();
 		builder.setDestination(net.getDestination()).setMessage(getMessageBroker(net.getMessage())).setDestinationType(translate(net.getDestinationType())).setSubscription(subs);
 
 		return builder.build();
 	}
 
 	private Ping getPing(NetMessage netMessage)
 	{
 		NetPing gcsPing = netMessage.getAction().getPingMessage();
 
 		PBMessage.Atom.Ping.Builder builder = PBMessage.Atom.Ping.newBuilder();
 		builder.setActionId(gcsPing.getActionId());
 
 		return builder.build();
 	}
 
 	private Poll getPoll(NetMessage netMessage)
 	{
 		NetPoll net = netMessage.getAction().getPollMessage();
 
 		PBMessage.Atom.Poll.Builder builder = PBMessage.Atom.Poll.newBuilder();
 		builder.setDestination(net.getDestination()).setTimeout(net.getTimeout());
 
 		if (net.getActionId() != null)
 			builder.setActionId(net.getActionId());
 
 		return builder.build();
 	}
 
 	private Pong getPong(NetMessage netMessage)
 	{
 		NetPong gcsPong = netMessage.getAction().getPongMessage();
 
 		PBMessage.Atom.Pong.Builder builder = PBMessage.Atom.Pong.newBuilder();
 		builder.setActionId(gcsPong.getActionId());
 
 		return builder.build();
 	}
 
 	private Publish getPublish(NetMessage netMessage)
 	{
 		NetPublish net = netMessage.getAction().getPublishMessage();
 
 		PBMessage.Atom.Publish.Builder builder = PBMessage.Atom.Publish.newBuilder();
 		builder.setDestination(net.getDestination()).setMessage(getMessageBroker(net.getMessage())).setDestinationType(translate(net.getDestinationType()));
 
 		if (net.getActionId() != null)
 			builder.setActionId(net.getActionId());
 
 		return builder.build();
 	}
 
 	private Subscribe getSubscribe(NetMessage netMessage)
 	{
 		NetSubscribe net = netMessage.getAction().getSubscribeMessage();
 
 		PBMessage.Atom.Subscribe.Builder builder = PBMessage.Atom.Subscribe.newBuilder();
 		builder.setDestination(net.getDestination()).setDestinationType(translate(net.getDestinationType()));
 
 		if (net.getActionId() != null)
 			builder.setActionId(net.getActionId());
 
 		return builder.build();
 	}
 
 	private Unsubscribe getUnsubscribe(NetMessage netMessage)
 	{
 		NetUnsubscribe net = netMessage.getAction().getUnsbuscribeMessage();
 
 		PBMessage.Atom.Unsubscribe.Builder builder = PBMessage.Atom.Unsubscribe.newBuilder();
 		builder.setDestination(net.getDestination()).setDestinationType(translate(net.getDestinationType()));
 
 		if (net.getActionId() != null)
 			builder.setActionId(net.getActionId());
 
 		return builder.build();
 	}
 
 	protected Atom buildAtom(NetMessage message)
 	{
 		Header header = getHeaders(message);
 
 		PBMessage.Atom.Builder atomBuilder = PBMessage.Atom.newBuilder().setAction(getAction(message));
 		if (header != null)
 			atomBuilder.setHeader(header);
 
 		Atom build = atomBuilder.build();
 		return build;
 	}
 
 	@Override
 	public byte[] marshal(NetMessage message)
 	{
 		byte[] result = null;
 
 		try
 		{
 			Atom build = buildAtom(message);
 			result = build.toByteArray();
 		}
 		catch (Throwable e)
 		{
 			log.error("Error parsing Protocol Buffer message.", e.getMessage());
 		}
 
 		EncodingStats.newProtoEncodedMessage();
 
 		return result;
 	}
 
 	@Override
 	public void marshal(NetMessage message, OutputStream out)
 	{
 		Atom build = null;
 		try
 		{
 			build = buildAtom(message);
 			build.writeTo(out);
 			EncodingStats.newProtoEncodedMessage();
 		}
 		catch (Throwable e)
 		{
 			if (build != null)
 				System.out.println(build.toString());
 			log.error("Error parsing Protocol Buffer message.", e.getMessage());
 		}
 	}
 
 	private NetBrokerMessage obtainBrokerMessage(BrokerMessage message)
 	{
 
 		NetBrokerMessage brkMsg = new NetBrokerMessage(message.getPayload().toByteArray());
 
 		if (message.hasTimestamp())
 			brkMsg.setTimestamp(message.getTimestamp());
 		if (message.hasExpiration())
 			brkMsg.setExpiration(message.getExpiration());
 		if (message.hasMessageId())
 			brkMsg.setMessageId(message.getMessageId());
 
 		return brkMsg;
 	}
 
 	private PBMessage.Atom.DestinationType translate(DestinationType destinationType)
 	{
 		switch (destinationType)
 		{
 		case QUEUE:
 			return PBMessage.Atom.DestinationType.QUEUE;
 		case TOPIC:
 			return PBMessage.Atom.DestinationType.TOPIC;
 		case VIRTUAL_QUEUE:
 			return PBMessage.Atom.DestinationType.VIRTUAL_QUEUE;
 		}
 		throw new IllegalArgumentException("Unexpected destination type (pt.com.broker.types.NetAction.DestinationType): " + destinationType);
 	}
 
 	@Override
 	public NetMessage unmarshal(byte[] packet)
 	{
 		NetMessage message = null;
 		try
 		{
 			PBMessage.Atom atom = PBMessage.Atom.parseFrom(packet);
 			message = constructMessage(atom);
 
 			EncodingStats.newProtoDecodedMessage();
 		}
 		catch (Throwable e)
 		{
 			throw new RuntimeException(e);
 		}
 		return message;
 	}
 
 	@Override
 	public NetMessage unmarshal(InputStream in)
 	{
 		NetMessage message = null;
 		try
 		{
 			PBMessage.Atom atom = PBMessage.Atom.parseFrom(in);
 			message = constructMessage(atom);
 
 			EncodingStats.newProtoDecodedMessage();
 		}
 		catch (Throwable e)
 		{
 			throw new RuntimeException(e);
 		}
 		return message;
 	}
 
 	@Override
 	public NetProtocolType getProtocolType()
 	{
 		return NetProtocolType.PROTOCOL_BUFFER;
 	}
 }
