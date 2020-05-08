 package pt.com.broker.http;
 
 import java.io.OutputStream;
 import java.util.concurrent.TimeUnit;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.http.netty.HttpAction;
 import org.caudexorigo.text.StringUtils;
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
 
 import pt.com.broker.core.BrokerExecutor;
 import pt.com.broker.types.CriticalErrors;
 import pt.com.gcs.messaging.Gcs;
 
 /**
  * AdminAction is an HttpAction. It supports some administrative options such agent's shutdown or queue deletion.
  * 
  */
 
 public class AdminAction extends HttpAction
 {
 	private static final String content_type = "text/plain";
 
 	private static final Logger log = LoggerFactory.getLogger(AdminAction.class);
 	private ChannelBuffer BAD_REQUEST_RESPONSE;
 
 	public AdminAction()
 	{
 		super();
 		try
 		{
 			byte[] bad_arr = "<p>Only the POST verb is supported</p>".getBytes("UTF-8");
 			BAD_REQUEST_RESPONSE = ChannelBuffers.buffer(bad_arr.length);
 			BAD_REQUEST_RESPONSE.setBytes(0, bad_arr);
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
 				String action = new String(request.getContent().array());
 				
 				System.out.println("AdminAction.writeResponse.action: " + action);
 
 				if (StringUtils.isBlank(action))
 				{
 					throw new IllegalArgumentException("No arguments supplied");
 				}
 
 				if (action.equals("SHUTDOWN"))
 				{
 					Runnable kill = new Runnable()
 					{
 						public void run()
 						{
 							Shutdown.now();
 						}
 					};
 					BrokerExecutor.schedule(kill, 1000, TimeUnit.MILLISECONDS);
 
 				}
 				else if (action.startsWith("QUEUE:"))
 				{
 					String from = channel.getRemoteAddress().toString();
 					String queueName = StringUtils.substringAfter(action, "QUEUE:");
 					Gcs.deleteQueue(queueName);
					String message = String.format("Queue '%s' was deleted. Request from: '%s'%n", queueName, from);
 					log.info(message);
 					ChannelBuffer bbo = ChannelBuffers.wrappedBuffer(message.getBytes("UTF-8"));
 					response.setContent(bbo);
 				}
 
 				response.setStatus (HttpResponseStatus.OK);
 			}
 			else
 			{
 				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 				response.setContent(BAD_REQUEST_RESPONSE.duplicate());
 			}
 		}
 		catch (Throwable e)
 		{
 			response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
 			fault(e, response);
 			if (log.isErrorEnabled())
 			{
				log.error("HTTP Service error, cause:" + e.getMessage() + ". Client address:" + channel.getRemoteAddress());
 			}
 		}
 	}
 
 	public void fault(Throwable cause, HttpResponse response)
 	{
 		try
 		{
 			ChannelBuffer bbf = ChannelBuffers.buffer(1024);
 			OutputStream out = new ChannelBufferOutputStream(bbf);
 			Throwable rootCause = ErrorAnalyser.findRootCause(cause);
 			CriticalErrors.exitIfCritical(rootCause);
 			out.write(("Error: " + rootCause.getMessage() + "\n").getBytes("UTF-8"));
 			out.flush();
 			
 			response.setHeader(HttpHeaders.Names.CONTENT_TYPE, content_type);
 			response.setContent(bbf);
 		}
 		catch (Throwable e)
 		{
 			// ignore
 		}
 
 	}
 
 }
