 package com.wixpress.fjarr.client;
 
 import com.wixpress.fjarr.http.ContentTypeUtils;
 import com.wixpress.fjarr.util.SettableFuture;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.codec.http.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.URI;
 import java.nio.charset.Charset;
 import java.util.Map;
 import java.util.concurrent.*;
 
 import static java.lang.String.format;
 
 /**
  * @author alexeyr
  */
 
 public class NettyInvoker implements RpcInvoker
 {
 
     private static final Logger logger = LoggerFactory.getLogger(NettyInvoker.class);
 
     private ClientBootstrap bootstrap;
     private final NettyClientConfig clientConfig;
 
     private ChannelGroup activeChannels = new DefaultChannelGroup();
 
     public NettyInvoker(NettyClientConfig clientConfig)
     {
         this.clientConfig = clientConfig;
 
         // Configure the client.
         bootstrap = new ClientBootstrap(
                 new NioClientSocketChannelFactory(
                        threadPool(clientConfig.getCoreThreads(), clientConfig.getMaxThreads(), clientConfig.getRejectionPolicy()),
                        threadPool(clientConfig.getCoreThreads(), clientConfig.getMaxThreads(), clientConfig.getRejectionPolicy()))
         );
 
         // Set up the event pipeline factory.
         bootstrap.setPipelineFactory(new HttpClientPipelineFactory(new ResponseFinalizer()));
         bootstrap.setOption("tcpNoDelay", true);
     }
 
     public void shutdown()
     {
 
         activeChannels.close();
         bootstrap.releaseExternalResources();
     }
 
 
     @Override
     public RpcInvocationResponse invoke(RpcInvocation invocation) throws IOException
     {
 
         URI uri = invocation.getServiceUri();
 
         // Https not supported
         // String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
         String host = uri.getHost() == null ? "localhost" : uri.getHost();
         int port = uri.getPort();
         if (port == -1)
         {
             port = 80;
         }
 
         logger.debug(format("Start performing request to %s", invocation.getServiceUri().toASCIIString()));
 
 
         // Start the connection attempt.
         ChannelFuture future = bootstrap.connect(new InetSocketAddress(host, port));
 
         Channel channel = future.getChannel();
         channel.getConfig().setConnectTimeoutMillis(clientConfig.getConnectionTimeoutMillis());
         SettableFuture<RpcInvocationResponse> invocationResponseFuture = new SettableFuture<RpcInvocationResponse>();
 
 
         channel.getPipeline().getContext(HttpClientPipelineFactory.HANDLER_NAME).setAttachment(
                 new RequestResponse(invocation, invocationResponseFuture));
 
         activeChannels.add(channel);
         future.addListener(new InvocationSender(invocation, invocationResponseFuture));
 
         try
         {
             return invocationResponseFuture.get();
         }
         catch (InterruptedException e)
         {
             throw new IOException("Interrupted while processing request", e);
         }
         catch (ExecutionException e)
         {
             throw new IOException("Execution error", e);
         }
     }
 
     private class InvocationSender implements ChannelFutureListener
     {
 
         RpcInvocation invocation;
         private final SettableFuture<RpcInvocationResponse> responseFuture;
 
         public InvocationSender(RpcInvocation invocation, SettableFuture<RpcInvocationResponse> responseFuture)
         {
             this.invocation = invocation;
             this.responseFuture = responseFuture;
         }
 
         public void operationComplete(ChannelFuture future) throws Exception
         {
 
             // Wait until the connection attempt succeeds or fails.
             Channel channel = future.getChannel();
 
             try
             {
                 String host = invocation.getServiceUri().getHost();
 
                 if (!future.isSuccess())
                 {
                     logger.error(format("Failed to connect to %s", host));
                     failure(channel, future.getCause());
                     return;
                 }
 
                 logger.debug(format("Connected to %s", host));
 
                 // Prepare the HTTP request.
                 // Prepare the HTTP request.
                 HttpRequest request = new DefaultHttpRequest(
                         HttpVersion.HTTP_1_0, HttpMethod.POST, invocation.getServiceUri().getRawPath());
                 request.setHeader(HttpHeaders.Names.HOST, host);
                 request.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
                 request.setHeader(HttpHeaders.Names.ACCEPT_ENCODING, HttpHeaders.Values.GZIP);
                 request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
                         ContentTypeUtils.join(invocation.getContentType(), invocation.getCharacterEncoding()));
                 request.setHeader(HttpHeaders.Names.ACCEPT_CHARSET, invocation.getCharacterEncoding());
 
                 // Additional headers
                 for (Map.Entry<String, String> entry : invocation.getAllHeaders().entrySet())
                 {
                     request.addHeader(entry.getKey(), entry.getValue());
                 }
 
                 final ChannelBuffer content = ChannelBuffers.wrappedBuffer(invocation.getBody().getBytes(Charset.forName(invocation.getCharacterEncoding())));
                 request.setContent(content);
                 request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, content.readableBytes());
 
                 // Send the HTTP request.
                 channel.write(request);
 
                 logger.debug(format("HTTP Request sent, waiting for response from %s", host));
             }
             catch (Exception e)
             {
                 logger.error(format("Failed to complete task for %s", invocation.getServiceUri().toASCIIString()), e);
                 failure(channel, e);
             }
         }
 
         public void failure(Channel channel, Throwable e) throws Exception
         {
 
             if (responseFuture != null)
             {
                 responseFuture.setError(e);
 
             }
             requestFinished(channel);
         }
     }
 
     private void requestFinished(Channel channel)
     {
 
         channel.close();
     }
 
     private class ResponseFinalizer implements HttpResponseCompletedListener
     {
 
         public void responseCompleted(Channel channel)
         {
             requestFinished(channel);
         }
     }
 
    private ExecutorService threadPool(int coreThreads, int maxThreads, RejectedExecutionHandler rejectedExecutionHandler)
     {
         return new ThreadPoolExecutor(coreThreads, maxThreads,
                 60L, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),rejectedExecutionHandler);
     }
 }
