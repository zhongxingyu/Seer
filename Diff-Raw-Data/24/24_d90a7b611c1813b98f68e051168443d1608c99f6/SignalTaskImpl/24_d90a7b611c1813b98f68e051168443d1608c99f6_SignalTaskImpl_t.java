 package xingu.node.commons.signal.impl;
 
import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import xingu.node.commons.signal.ExceptionSignal;
 import xingu.node.commons.signal.Signal;
 import xingu.node.commons.signal.behavior.BehaviorPerformer;
 import xingu.node.commons.signal.processor.SignalTask;
 import br.com.ibnetwork.xingu.container.Inject;
 
 public class SignalTaskImpl
 	implements SignalTask
 {
 	@Inject
 	protected BehaviorPerformer	performer;
 
 	protected Signal			signal;
 
 	protected Channel			channel;
 
 	protected Logger			logger	= LoggerFactory.getLogger(this.getClass());
 
 	protected ChannelFutureListener onWrite = new ChannelFutureListener()
 	{
 		@Override
 		public void operationComplete(ChannelFuture future)
 			throws Exception
 		{
 			boolean success = future.isSuccess();
 			if(!success)
 			{
 				//TODO: store the result/reply and send it to the server when apropriate
 				logger.error("Error writing Signal reply", future.getCause());
 			}
 		}
 	};
 	
 	public SignalTaskImpl(Signal signal, Channel pipe)
 	{
 		this.signal  = signal;
 		this.channel = pipe;
 	}
 
 	@Override
 	public Signal call()
 		throws Exception
 	{
 		Signal reply = null;
 		try
 		{
 			reply = performer.performBehavior(signal);
 		}
 		catch(Throwable t)
 		{
 			logger.error("Error processing signal " + signal, t);
 			signal.setForward(false);
			String trace = ExceptionUtils.getStackTrace(t);
			reply        = new ExceptionSignal(signal, trace);
 		}
 		if(reply != null)
 		{
 			long signalId = signal.getSignalId();
 			reply.setSignalId(signalId);
 			channel.write(reply).addListener(onWrite);
 		}
 		return reply;
 	}
 }
