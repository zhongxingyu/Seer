 package no.rmz.blobee.rpc;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.protobuf.Message;
 import com.google.protobuf.RpcChannel;
 import com.google.protobuf.RpcController;
 import java.net.InetSocketAddress;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.ChannelFuture;
 
 public final class ConnectingRpcClientImpl implements RpcClient {
     private final RpcClientImpl rpcClient;
     private final ClientBootstrap clientBootstrap;
     final InetSocketAddress socketAddress;
 
     public ConnectingRpcClientImpl(
             final ClientBootstrap clientBootstrap,
             final InetSocketAddress socketAddress) {
         this.rpcClient = new RpcClientImpl(4711); // XXX BOGUS
         this.socketAddress = checkNotNull(socketAddress);
         // XXX BOGUS
         this.clientBootstrap = checkNotNull(clientBootstrap);
     }
 
     // XXX This stuff needs to be rewritten so it can work
     //     without having to start its own ChannelFuture.  It should
     //     be possible to graft the thing onto a server instance.
     @Override
     public RpcClient start() {
         checkNotNull(socketAddress);
 
         // Start the connection attempt.
         final ChannelFuture future = clientBootstrap.connect(socketAddress);
         rpcClient.start(future.getChannel(), new ChannelShutdownCleaner() {
             public void shutdownHook() {
                 // Shut down thread pools to exit.
                 clientBootstrap.releaseExternalResources();
             }
         });
         return this;
     }
 
     public void cancelInvocation(final long rpcIndex) {
         rpcClient.cancelInvocation(rpcIndex);
     }
 
     public void failInvocation(final long rpcIndex, final String errorMessage) {
         rpcClient.failInvocation(rpcIndex, errorMessage);
     }
 
     public RpcChannel newClientRpcChannel() {
         return rpcClient.newClientRpcChannel();
     }
 
     public RpcController newController() {
         return rpcClient.newController();
     }
 
     public void returnCall(final RemoteExecutionContext dc, final Message message) {
         rpcClient.returnCall(dc, message);
     }
 
 
     public MethodSignatureResolver getResolver() {
        return rpcClient.getResolver();
     }
 
     public RpcClient addProtobuferRpcInterface(final Object instance) {
        return rpcClient.addProtobuferRpcInterface(instance);
     }
 }
