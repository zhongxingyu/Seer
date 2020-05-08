 package org.nohope.protobuf.rpc.client;
 
 import org.nohope.protobuf.core.net.PipelineFactory;
 import org.nohope.rpc.protocol.RPC;
 import com.google.protobuf.BlockingRpcChannel;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 
 import javax.annotation.Nonnull;
 import java.util.concurrent.Executors;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 2013-10-01 15:04
  */
 public final class RpcClient implements IRpcClient {
     private final ClientBootstrap bootstrap;
     private final RpcClientOptions options;
 
     public RpcClient(@Nonnull final RpcClientOptions options) {
         this.options = options;
         this.bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(
                 Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
         this.bootstrap.setPipelineFactory(new PipelineFactory(new RpcClientHandler(),
                 RPC.RpcResponse.getDefaultInstance()));
         this.bootstrap.setOption("remoteAddress", options.getAddress());
     }
 
     @Override
     public BlockingRpcChannel connect() {
         return new RpcChannelImpl(bootstrap, options.getTimeout(), options.getTimeoutUnit());
     }
 
     @Override
     public void shutdown() {
         bootstrap.shutdown();
         bootstrap.releaseExternalResources();
     }
 }
