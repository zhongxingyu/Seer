 package pt.com.broker.http;
 
 import java.io.OutputStream;
 
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.http.netty.HttpAction;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferOutputStream;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.handler.codec.http.HttpHeaders;
 import org.jboss.netty.handler.codec.http.HttpMethod;
 import org.jboss.netty.handler.codec.http.HttpRequest;
 import org.jboss.netty.handler.codec.http.HttpResponse;
 import org.jboss.netty.handler.codec.http.HttpResponseStatus;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.auth.AccessControl;
 import pt.com.broker.auth.Session;
 import pt.com.broker.auth.AccessControl.ValidationResult;
 import pt.com.broker.codec.xml.SoapBindingSerializer;
 import pt.com.broker.codec.xml.SoapEnvelope;
 import pt.com.broker.codec.xml.SoapSerializer;
 import pt.com.broker.core.ErrorHandler;
 import pt.com.broker.messaging.BrokerProducer;
 import pt.com.broker.messaging.MQ;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.stats.ChannelStats;
 
 /**
  * BrokerHttpAction is an HttpAction. It supports message publishing.
  * 
  */
 
 public class BrokerHttpAction extends HttpAction
 {
 	private static final String content_type = "text/xml";
 
 	private static final Logger log = LoggerFactory.getLogger(BrokerHttpAction.class);
 
 	private static final long serialVersionUID = 9072384515868129239L;
 
 	private static final BrokerProducer _http_broker = BrokerProducer.getInstance();
 
 	private static final SoapBindingSerializer bindingSerializer = new SoapBindingSerializer();
 
 	private ChannelBuffer BAD_REQUEST_RESPONSE;
 	private ChannelBuffer ACCESS_DENIED_REQUEST_RESPONSE;
 	private ChannelBuffer INVALID_MESSAGE_TYPE_RESPONSE;
 
 	public BrokerHttpAction()
 	{
 		super();
 		try
 		{
 			byte[] bad_arr = "<p>Only the POST verb is supported</p>".getBytes("UTF-8");
 			BAD_REQUEST_RESPONSE = ChannelBuffers.wrappedBuffer(bad_arr);
 
 			bad_arr = "<p>Access denied</p>".getBytes("UTF-8");
 			ACCESS_DENIED_REQUEST_RESPONSE = ChannelBuffers.wrappedBuffer(bad_arr);
 
 			bad_arr = "<p>Invalid message type. Only publish is supported (topic and queue).</p>".getBytes("UTF-8");
 			INVALID_MESSAGE_TYPE_RESPONSE = ChannelBuffers.wrappedBuffer(bad_arr);
 		}
 		catch (Throwable error)
 		{
 			log.error("Fatal JVM error!", error);
 			Shutdown.now();
 		}
 	}
 
 	@Override
 	public void writeResponse(ChannelHandlerContext ctx, HttpRequest request, HttpResponse response)
 	{
 		Channel channel = ctx.getChannel();
 		try
 		{
 			if (request.getMethod().equals(HttpMethod.POST))
 			{
 				ChannelBuffer bb = request.getContent();
 
 				byte[] buf = bb.array();
 
 				NetMessage message = (NetMessage) bindingSerializer.unmarshal(buf);
				
 				ChannelStats.newHttpMessageReceived();
 				
 				ValidationResult validationResult = AccessControl.validate(message, new Session(channel));
 
 				if (!validationResult.accessGranted)
 				{
 					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 					response.setContent(ACCESS_DENIED_REQUEST_RESPONSE.duplicate());
 					return;
 				}
 
 				if ((message.getAction().getActionType() != NetAction.ActionType.PUBLISH) || (message.getAction().getPublishMessage() == null))
 				{
 					response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 					response.setContent(INVALID_MESSAGE_TYPE_RESPONSE.duplicate());
 					return;
 				}
 
 				if (message.getAction().getPublishMessage().getDestinationType() == NetAction.DestinationType.TOPIC)
 				{
 					_http_broker.publishMessage(message.getAction().getPublishMessage(), MQ.requestSource(message));
 				}
 				else if (message.getAction().getPublishMessage().getDestinationType() == NetAction.DestinationType.QUEUE)
 				{
 					_http_broker.enqueueMessage(message.getAction().getPublishMessage(), MQ.requestSource(message));
 				}
 
 				response.setStatus(HttpResponseStatus.ACCEPTED);
 			}
 			else
 			{
 				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR); // Internal
 				// server
 				// error?...
 				response.setContent(BAD_REQUEST_RESPONSE.duplicate());
 			}
 		}
 		catch (Throwable e)
 		{
 			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 			fault(null, e, response);
 			if (log.isErrorEnabled())
 			{
 				log.error("HTTP Service error, cause: '{}'. Client address: '{}'", e.getMessage(), channel.getRemoteAddress());
 			}
 		}
 	}
 
 	public void fault(String faultCode, Throwable cause, HttpResponse response)
 	{
 		try
 		{
 			ChannelBuffer bbf = ChannelBuffers.dynamicBuffer();
 			OutputStream out = new ChannelBufferOutputStream(bbf);
 
 			SoapEnvelope ex_msg = ErrorHandler.buildSoapFault(faultCode, cause).Message;
 			SoapSerializer.ToXml(ex_msg, out);
 			out.flush();
 			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, content_type);
 			response.setContent(bbf);
 		}
 		catch (Throwable t)
 		{
 			log.error(t.getMessage(), t);
 		}
 	}
 
 }
