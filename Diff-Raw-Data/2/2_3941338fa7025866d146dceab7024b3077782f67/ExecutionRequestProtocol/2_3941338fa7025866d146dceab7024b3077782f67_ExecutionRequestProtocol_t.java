 package org.xmodel.net.execution;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBufferInputStream;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.xmodel.IModelObject;
 import org.xmodel.Xlate;
 import org.xmodel.compress.ICompressor;
 import org.xmodel.compress.TabularCompressor;
 import org.xmodel.log.Log;
 import org.xmodel.log.SLog;
 import org.xmodel.net.IXioCallback;
 import org.xmodel.net.XioChannelHandler.Type;
 import org.xmodel.net.XioExecutionException;
 import org.xmodel.net.execution.ExecutionResponseProtocol.ResponseTask;
 import org.xmodel.xaction.IXAction;
 import org.xmodel.xaction.XActionDocument;
 import org.xmodel.xpath.expression.IContext;
 import org.xmodel.xpath.expression.StatefulContext;
 
 public class ExecutionRequestProtocol
 {
   public ExecutionRequestProtocol( ExecutionProtocol bundle)
   {
     this.bundle = bundle;
     this.requests = new ConcurrentHashMap<Integer, RequestRunnable>();
   }
   
   /**
    * Reset this instance by releasing internal resources.  This method should be called after 
    * the channel is closed to prevent conflict between protocol traffic and the freeing of resources.
    */
   public void reset()
   {
     requests.clear();
   }
   
   /**
    * Send an execution request via the specified channel.
    * @param channel The channel.
    * @param context The local context.
    * @param vars Shared variables from the local context.
    * @param element The script element to execute.
    * @param timeout The timeout in milliseconds.
    * @return Returns the result.
    */
   public Object[] send( Channel channel, IContext context, String[] vars, IModelObject element, int timeout) throws XioExecutionException, IOException, InterruptedException
   {
     int correlation = bundle.responseProtocol.nextCorrelation();
     log.debugf( "ExecutionRequestProtocol.send (sync): corr=%d, vars=%s, @name=%s, timeout=%d", correlation, Arrays.toString( vars), Xlate.get( element, "name", ""), timeout);
     
     //
     // The execution protocol can function in a variety of client/server threading models.
     // A progressive, shared TabularCompressor may be used when the worker pool has only one thread.
     //
     ICompressor compressor = (bundle.requestCompressor != null)? bundle.requestCompressor: new TabularCompressor( false);
     
     IModelObject request = ExecutionSerializer.buildRequest( context, vars, element);
     List<byte[]> buffers = compressor.compress( request);
     ChannelBuffer buffer2 = ChannelBuffers.wrappedBuffer( buffers.toArray( new byte[ 0][]));
     
     ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( 0, Type.executeRequest, 4 + buffer2.readableBytes(), correlation);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
     
     return (timeout > 0)? bundle.responseProtocol.waitForResponse( correlation, context, timeout): null;
   }
   
   /**
    * Send an asynchronous execution request via the specified channel.
    * @param channel The channel.
    * @param correlation The preallocated correlation number.
    * @param context The local context.
    * @param vars Shared variables from the local context.
    * @param element The script element to execute.
    * @param callback The callback.
    * @param timeout The timeout in milliseconds.
    */
   public void send( Channel channel, int correlation, IContext context, String[] vars, IModelObject element, IXioCallback callback, int timeout) throws IOException, InterruptedException
   {
     ResponseTask task = new ResponseTask( context, callback);
     if ( timeout != Integer.MAX_VALUE)
     {
       ScheduledFuture<?> timer = bundle.scheduler.schedule( new ResponseTimeout( task), timeout, TimeUnit.MILLISECONDS);
       task.setTimer( timer);
     }
     
     bundle.responseProtocol.setCorrelation( correlation, task);
     log.debugf( "ExecutionRequestProtocol.send (async): corr=%d, vars=%s, @name=%s, timeout=%d", correlation, Arrays.toString( vars), Xlate.get( element, "name", ""), timeout);
     
     // The execution protocol can function in a variety of client/server threading models.
     // A progressive, shared TabularCompressor may be used when the worker pool has only one thread.
     //
     ICompressor compressor = (bundle.requestCompressor != null)? bundle.requestCompressor: new TabularCompressor( false);
     
     IModelObject request = ExecutionSerializer.buildRequest( context, vars, element);
     List<byte[]> buffers = compressor.compress( request);
     ChannelBuffer buffer2 = ChannelBuffers.wrappedBuffer( buffers.toArray( new byte[ 0][]));
     
     ChannelBuffer buffer1 = bundle.headerProtocol.writeHeader( 0, Type.executeRequest, 4 + buffer2.readableBytes(), correlation);
     
     // ignoring write buffer overflow for this type of messaging
     channel.write( ChannelBuffers.wrappedBuffer( buffer1, buffer2));
   }
   
   /**
    * Handle a request.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handle( Channel channel, ChannelBuffer buffer) throws IOException
   {
     int correlation = buffer.readInt();
 
     if ( bundle.context == null)
     {
       bundle.responseProtocol.send( channel, correlation, null, (Object[])null);
       return;
     }
 
     //
     // The execution protocol can function in a variety of client/server threading models.
     // A progressive, shared TabularCompressor may be used when the client worker pool has only one thread.
     //
     ICompressor compressor = (bundle.responseCompressor != null)? bundle.responseCompressor: new TabularCompressor( false);
     
     IModelObject request = compressor.decompress( new ChannelBufferInputStream( buffer));
     RequestRunnable runnable = new RequestRunnable( channel, correlation, request);
     requests.put( correlation, runnable);
     bundle.executor.execute( runnable);
   }
   
   /**
    * Cancel an asynchronous execute request.  This method first removes the async context associated with the specified
    * correlation number, and then sends a cancel request to the server.  The handling of the cancel request is implementation
    * dependent, and no guarantee is made that the server will interrupt the operation.
    * @param channel The channel. 
    * @param correlation The correlation number of the request.
    */
   public void cancel( Channel channel, int correlation)
   {
     log.debugf( "ExecutionRequestProtocol.cancel: corr=%d", correlation);
     
     // ignore response with this correlation
     bundle.responseProtocol.cancel( correlation);
 
     // send cancel
     ChannelBuffer buffer = bundle.headerProtocol.writeHeader( 0, Type.cancelRequest, 4, correlation);
     channel.write( buffer);
   }
   
   /**
    * Handle an execution cancel request. 
    * @param channel The channel.
    * @param buffer The buffer.
    */
   public void handleCancel( Channel channel, ChannelBuffer buffer) throws IOException
   {
     int correlation = buffer.readInt();
     
     log.debugf( "ExecutionRequestProtocol.handleCancel: corr=%d", correlation);
     
    RequestRunnable runnable = requests.remove( correlation);
     if ( runnable != null) runnable.interrupt();
   }
   
   /**
    * Verify that execution privileges allow the specified script element to be executed.
    * @param element The script element.
    */
   private void checkPrivileges( IModelObject element) throws IOException
   {
     if ( privilege != null && !privilege.isPermitted( bundle.context, element))
     {
       throw new IOException( "Script contains restricted operations.");
     }
   }
   
   /**
    * Compile the specified script element.
    * @param element The script element.
    * @return Returns the script.
    */
   private IXAction compile( IModelObject element) throws IOException
   {
     XActionDocument document = new XActionDocument( element);
     document.setRoot( element);
     IXAction script = document.getAction( element);
     if ( script != null) return script;
     throw new IOException( String.format( "Unable to resolve IXAction class: %s.", element.getType()));
   }
 
   /**
    * Execute the specified script.
    * @param channel The channel.
    * @param correlation The correlation.
    * @param request The request.
    */
   private void execute( Channel channel, int correlation, IModelObject request)
   {
     //
     // Model locking is left to the script for finer granularity.  A private context is created
     // to hold the variables that will be passed to the script.
     //
     IContext context = new StatefulContext( bundle.context);
     
     // store remote address in context
     InetSocketAddress address = (InetSocketAddress)channel.getRemoteAddress();
     context.set(  "remoteHost", address.getHostName());
     
     try
     {
       IModelObject element = ExecutionSerializer.readRequest( request, context);
       checkPrivileges( element);
       
       IXAction script = compile( element);
       Object[] results = script.run( context);
       bundle.responseProtocol.send( channel, correlation, context, results);
     }
     catch( Exception e)
     {
       try
       {
         bundle.responseProtocol.send( channel, correlation, context, e);
       }
       catch( Exception e2)
       {
         SLog.warnf( this, "Unable to send exception execution response because %s.", e2.getMessage());
       }
       
       SLog.exceptionf( this, e, "Exception thrown during remote execution: ");
     }
   }
   
   private class RequestRunnable implements Runnable
   {
     public RequestRunnable( Channel channel, int correlation, IModelObject request)
     {
       this.channel = channel;
       this.correlation = correlation;
       this.request = request;
     }
     
     /**
      * Interrupt the thread currently processing this request.
      */
     public void interrupt()
     {
       if ( thread != null) thread.interrupt();
     }
     
     /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run()
     {
       try
       {
         thread = Thread.currentThread();
         execute( channel, correlation, request);
       }
       finally
       {
         requests.remove( this);
       }
     }
 
     private Channel channel;
     private int correlation;
     private IModelObject request;
     private Thread thread;
   }
   
   private class ResponseTimeout implements Runnable
   {
     public ResponseTimeout( ResponseTask task)
     {
       this.task = task;
     }
     
     /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run()
     {
       log.debug( "Response timeout.");
       task.setError( "timeout");
       
       bundle.executor.execute( task);
     }
 
     private ResponseTask task;
   }
   
   private final static Log log = Log.getLog( ExecutionRequestProtocol.class);
 
   private ExecutionProtocol bundle;
   private ExecutionPrivilege privilege;
   private Map<Integer, RequestRunnable> requests;
 }
