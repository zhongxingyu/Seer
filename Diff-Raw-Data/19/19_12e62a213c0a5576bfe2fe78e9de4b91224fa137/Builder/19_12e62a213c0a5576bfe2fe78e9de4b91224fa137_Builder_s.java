 package pt.com.broker.codec.xml;
 
 import java.nio.charset.Charset;
 import java.util.Date;
 
 import org.caudexorigo.text.DateUtil;
 
 import pt.com.broker.types.NetAccepted;
 import pt.com.broker.types.NetAcknowledge;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetFault;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetPing;
 import pt.com.broker.types.NetPoll;
 import pt.com.broker.types.NetPong;
 import pt.com.broker.types.NetPublish;
 import pt.com.broker.types.NetSubscribe;
 import pt.com.broker.types.NetUnsubscribe;
 
 public class Builder
 {
 	private static final Charset CHARSET = Charset.forName("UTF-8");
 
 	protected static final NetMessage soapToNetMessage(SoapEnvelope msg)
 	{
 		NetMessage message = null;
 
 		String actionId = null;
 
 		if (msg.body.notification != null)
 		{
 			Notification notification = msg.body.notification;
 
 			String dest = notification.brokerMessage.destinationName;
 			NetAction.DestinationType destType = NetAction.DestinationType.valueOf(msg.header.wsaFrom.address);
 			NetBrokerMessage brkMsg = new NetBrokerMessage(notification.brokerMessage.textPayload.getBytes(CHARSET));
 			brkMsg.setMessageId(notification.brokerMessage.messageId);
 			String val = notification.brokerMessage.expiration;
 			try
 			{
 				if ((val != null) && (!val.equals("")))
 					brkMsg.setExpiration(Long.parseLong(val));
 				val = notification.brokerMessage.timestamp;
 				if ((val != null) && (!val.equals("")))
 					brkMsg.setExpiration(Long.parseLong(val));
 				brkMsg.setTimestamp(Long.parseLong(val));
 			}
 			catch (NumberFormatException nfe)
 			{
 			}
 
 			String subs = msg.header.wsaTo;
 			NetNotification netNotification = new NetNotification(dest, destType, brkMsg, subs);
 			NetAction netAction = new NetAction(NetAction.ActionType.NOTIFICATION);
 
 			message = new NetMessage(netAction);
 			message.getAction().setNotificationMessage(netNotification);
 
 		}
 		else if (msg.body.publish != null)
 		{
 			BrokerMessage xmsg = msg.body.publish.brokerMessage;
 			actionId = msg.body.publish.actionId;
 
 			NetAction netAction = new NetAction(NetAction.ActionType.PUBLISH);
 			NetAction.DestinationType dtype = NetAction.DestinationType.TOPIC;
 
 			NetBrokerMessage netBkMsg = new NetBrokerMessage(xmsg.textPayload.getBytes(CHARSET));
 			netBkMsg.setMessageId(xmsg.messageId);
 
 			NetPublish netPublish = new NetPublish(xmsg.destinationName, dtype, netBkMsg);
 			netPublish.setActionId(actionId);
 
 			message = new NetMessage(netAction);
 			message.getAction().setPublishMessage(netPublish);
 
 		}
 		else if (msg.body.enqueue != null)
 		{
 			BrokerMessage xmsg = msg.body.enqueue.brokerMessage;
 			actionId = msg.body.enqueue.actionId;
 
 			NetAction netAction = new NetAction(NetAction.ActionType.PUBLISH);
 			NetAction.DestinationType dtype = NetAction.DestinationType.QUEUE;
 
 			NetBrokerMessage netBkMsg = new NetBrokerMessage(xmsg.textPayload.getBytes(CHARSET));
 			netBkMsg.setMessageId(xmsg.messageId);
 
 			NetPublish netPublish = new NetPublish(xmsg.destinationName, dtype, netBkMsg);
 			netPublish.setActionId(actionId);
 
 			message = new NetMessage(netAction);
 			message.getAction().setPublishMessage(netPublish);
 		}
 		else if (msg.body.notify != null)
 		{
 			Notify sb = msg.body.notify;
 			actionId = sb.actionId;
 
 			NetAction netAction = new NetAction(NetAction.ActionType.SUBSCRIBE);
			NetAction.DestinationType dtype = NetAction.DestinationType.valueOf(sb.destinationType);
 			NetSubscribe netSubscribe = new NetSubscribe(sb.destinationName, dtype);
 			netSubscribe.setActionId(actionId);
 
 			message = new NetMessage(netAction);
 			message.getAction().setSubscribeMessage(netSubscribe);
 
 		}
 		else if (msg.body.poll != null)
 		{
 			actionId = msg.body.poll.actionId;
 			Poll poll = msg.body.poll;
 
 			NetAction netAction = new NetAction(NetAction.ActionType.POLL);
			NetPoll netPoll = new NetPoll(poll.destinationName, Long.MAX_VALUE/2);
 			netPoll.setActionId(actionId);
 
 			message = new NetMessage(netAction);
 			message.getAction().setPollMessage(netPoll);
 
 		}
 		else if (msg.body.acknowledge != null)
 		{
 			actionId = msg.body.acknowledge.actionId;
 			Acknowledge ack = msg.body.acknowledge;
 
 			NetAction netAction = new NetAction(NetAction.ActionType.ACKNOWLEDGE);
 			NetAcknowledge netAck = new NetAcknowledge(ack.destinationName, ack.messageId);
 			netAck.setActionId(actionId);
 
 			message = new NetMessage(netAction);
 			message.getAction().setAcknowledgeMessage(netAck);
 		}
 		else if (msg.body.accepted != null)
 		{
 			actionId = msg.body.accepted.actionId;
 			NetAction netAction = new NetAction(NetAction.ActionType.ACCEPTED);
 			NetAccepted netAccepted = new NetAccepted(actionId);
 			message = new NetMessage(netAction);
 			message.getAction().setAcceptedMessage(netAccepted);
 		}
 		else if (msg.body.unsubscribe != null)
 		{
 			Unsubscribe unsubs = msg.body.unsubscribe;
 			actionId = msg.body.unsubscribe.actionId;
 
 			NetAction netAction = new NetAction(NetAction.ActionType.UNSUBSCRIBE);
 			NetAction.DestinationType dtype = NetAction.DestinationType.valueOf(unsubs.destinationType);
 			NetUnsubscribe netUnsubscribe = new NetUnsubscribe(unsubs.destinationName, dtype);
 			netUnsubscribe.setActionId(actionId);
 
 			message = new NetMessage(netAction);
 			message.getAction().setUnsbuscribeMessage(netUnsubscribe);
 		}
 		else if (msg.body.checkStatus != null)
 		{
 			NetAction netAction = new NetAction(NetAction.ActionType.PING);
 			NetPing netPing = new NetPing(NetPong.getUniversalActionId());
 			message = new NetMessage(netAction);
 			message.getAction().setPingMessage(netPing);
 		}
 		else if (msg.body.status != null)
 		{
 			NetAction netAction = new NetAction(NetAction.ActionType.PONG);
 			NetPong netPong = new NetPong(msg.body.status.message);
 			message = new NetMessage(netAction);
 			message.getAction().setPongMessage(netPong);
 		}
 		else if (msg.body.fault != null)
 		{
 			NetAction netAction = new NetAction(NetAction.ActionType.FAULT);
 			String faultCode = null;
 			String faultMessage = null;
 			String faultDetail = null;
 
 			if (msg.body.fault.faultCode != null)
 				faultCode = msg.body.fault.faultCode.value;
 			if (msg.body.fault.faultReason != null)
 				faultMessage = msg.body.fault.faultReason.text;
 
 			faultDetail = msg.body.fault.detail;
			
 			NetFault netFault = new NetFault(faultCode, faultMessage);
 			netFault.setDetail(faultDetail);
 			message = new NetMessage(netAction);
 			message.getAction().setFaultMessage(netFault);
 		}
 		else
 		{
 			throw new RuntimeException("Not a valid request");
 		}
 		return message;
 	}
 
 	protected static final SoapEnvelope netMessageToSoap(NetMessage netMessage)
 	{
 		SoapEnvelope soap = new SoapEnvelope();
 
 		switch (netMessage.getAction().getActionType())
 		{
 		case ACCEPTED:
 			Accepted a = new Accepted();
 			a.actionId = netMessage.getAction().getAcceptedMessage().getActionId();
 			soap.body.accepted = a;
 			break;
 		case ACKNOWLEDGE:
 			Acknowledge ack = new Acknowledge();
 			NetAcknowledge nack = netMessage.getAction().getAcknowledgeMessage();
 			ack.actionId = nack.getActionId();
 			ack.destinationName = nack.getDestination();
 			ack.messageId = nack.getMessageId();
 			soap.body.acknowledge = ack;
 			break;
 		case FAULT:
 			SoapFault f = new SoapFault();
 			NetFault nf = netMessage.getAction().getFaultMessage();
 			f.faultCode.value = nf.getCode();
 			f.faultReason.text = nf.getMessage();
 			f.detail = nf.getDetail();
 			soap.body.fault = f;
 			break;
 		case NOTIFICATION:
 			Notification notf = new Notification();
 			NetNotification nnotf = netMessage.getAction().getNotificationMessage();
 			notf.brokerMessage = buildXmlBrokerMessage(nnotf.getMessage(), nnotf.getDestination());
 			notf.actionId = nnotf.getMessage().getMessageId();
 
 			SoapEnvelope soap_env = new SoapEnvelope();
 			SoapHeader soap_header = new SoapHeader();
 			EndPointReference epr = new EndPointReference();
 			epr.address = nnotf.getDestinationType().toString();
 			soap_header.wsaFrom = epr;
 			if (nnotf.getSubscription() != null)
 			{
 				soap_header.wsaTo = nnotf.getSubscription();
 			}
 
 			soap_header.wsaMessageID = "http://services.sapo.pt/broker/message/" + notf.brokerMessage.messageId;
 			soap_header.wsaAction = "http://services.sapo.pt/broker/notification/";
 			soap_env.header = soap_header;
 			soap.header = soap_header;
 			soap.body.notification = notf;
 			break;
 		case POLL:
 			Poll poll = new Poll();
 			NetPoll npoll = netMessage.getAction().getPollMessage();
 			poll.actionId = npoll.getActionId();
 			poll.destinationName = npoll.getDestination();
 			soap.body.poll = poll;
 			break;
 		case PUBLISH:
 			handlePublish(soap, netMessage);
 			break;
 		case SUBSCRIBE:
 			Notify notify = new Notify();
 			NetSubscribe nsubs = netMessage.getAction().getSubscribeMessage();
 			notify.actionId = nsubs.getActionId();
 			notify.destinationName = nsubs.getDestination();
 			notify.destinationType = nsubs.getDestinationType().toString();
 			soap.body.notify = notify;
 			break;
 		case UNSUBSCRIBE:
 			Unsubscribe unsubscribe = new Unsubscribe();
 			NetUnsubscribe nunsubscribe = netMessage.getAction().getUnsbuscribeMessage();
 			unsubscribe.actionId = nunsubscribe.getActionId();
 			unsubscribe.destinationName = nunsubscribe.getDestination();
 			unsubscribe.destinationType = nunsubscribe.getDestinationType().toString();
 			soap.body.unsubscribe = unsubscribe;
 			break;
 		case PING:
 			soap.body.checkStatus = new CheckStatus();
 			break;
 		case PONG:
 			NetPong pong = netMessage.getAction().getPongMessage();
 			Status status = new Status();
 			status.message = pong.getActionId();
 			soap.body.status = status;
 			break;
 		}
 
 		return soap;
 	}
 
 	private static void handlePublish(SoapEnvelope soap, NetMessage netMessage)
 	{
 		NetPublish publish = netMessage.getAction().getPublishMessage();
 		switch (publish.getDestinationType())
 		{
 		case TOPIC:
 			Publish pub = new Publish();
 			pub.actionId = publish.getActionId();
 			pub.brokerMessage = buildXmlBrokerMessage(publish.getMessage(), publish.getDestination());
 			soap.body.publish = pub;
 			break;
 		case QUEUE:
 			Enqueue enqueue = new Enqueue();
 			enqueue.actionId = publish.getActionId();
 			enqueue.brokerMessage = buildXmlBrokerMessage(publish.getMessage(), publish.getDestination());
 			soap.body.enqueue = enqueue;
 			break;
 		default:
 			throw new RuntimeException("Invalid destination type");
 		}
 
 	}
 
 	private static final BrokerMessage buildXmlBrokerMessage(NetBrokerMessage net_bkmsg, String destinationName)
 	{
 		BrokerMessage bkmsg = new BrokerMessage();
 		bkmsg.destinationName = destinationName;
 		bkmsg.messageId = net_bkmsg.getMessageId();
 		bkmsg.textPayload = new String(net_bkmsg.getPayload(), CHARSET);
 
 		if (net_bkmsg.getExpiration() > 0)
 		{
 			bkmsg.expiration = DateUtil.formatISODate(new Date(net_bkmsg.getExpiration()));
 		}
 
 		if (net_bkmsg.getTimestamp() > 0)
 		{
 			bkmsg.timestamp = DateUtil.formatISODate(new Date(net_bkmsg.getTimestamp()));
 		}
 
 		return bkmsg;
 	}
 
 }
