 package edu.uw.zookeeper.orchestra.net;
 
 
 import java.net.SocketAddress;
 import java.util.concurrent.ThreadFactory;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.channel.EventLoopGroup;
import edu.uw.zookeeper.common.EventBusPublisher;
 import edu.uw.zookeeper.common.Factory;
 import edu.uw.zookeeper.common.ParameterizedFactory;
 import edu.uw.zookeeper.common.RuntimeModule;
 import edu.uw.zookeeper.net.NetClientModule;
 import edu.uw.zookeeper.net.NetServerModule;
 import edu.uw.zookeeper.netty.DaemonThreadFactory;
 import edu.uw.zookeeper.netty.EventLoopGroupService;
 import edu.uw.zookeeper.netty.client.NettyClientModule;
 import edu.uw.zookeeper.netty.client.NioClientBootstrapFactory;
 import edu.uw.zookeeper.netty.nio.NioEventLoopGroupFactory;
 import edu.uw.zookeeper.netty.server.NioServerBootstrapFactory;
 import edu.uw.zookeeper.netty.server.NettyServerModule;
 
 public class NettyModule extends AbstractModule {
     
     public static NettyModule create() {
         return new NettyModule();
     }
     
     @Override
     protected void configure() {
         bind(NettyModule.class).in(com.google.inject.Singleton.class);
         bind(NetClientModule.class).to(NettyClientModule.class).in(com.google.inject.Singleton.class);
         bind(NetServerModule.class).to(NettyServerModule.class).in(com.google.inject.Singleton.class);
     }
     
     @Provides @Singleton
     public Factory<? extends EventLoopGroup> getEventLoopGroup(
             RuntimeModule runtime) {
         ThreadFactory threads = DaemonThreadFactory.getInstance().get(
                 runtime.getThreadFactory().get());
         return EventLoopGroupService.factory(
                 NioEventLoopGroupFactory.DEFAULT,
                 runtime.getServiceMonitor()).get(threads);
     }
     
     @Provides @Singleton
     public NettyServerModule getNettyServerModule(
             Factory<? extends EventLoopGroup> eventLoopGroup) {
         ParameterizedFactory<SocketAddress, ServerBootstrap> bootstraps = 
                 NioServerBootstrapFactory.ParameterizedDecorator.newInstance(
                         NioServerBootstrapFactory.newInstance(eventLoopGroup));
        return NettyServerModule.newInstance(
                EventBusPublisher.factory(), bootstraps);
     }
     
     @Provides @Singleton
     public NettyClientModule getNettyClientModule(
             Factory<? extends EventLoopGroup> eventLoopGroup) {
         Factory<Bootstrap> bootstraps = 
                 NioClientBootstrapFactory.newInstance(eventLoopGroup);  
        return NettyClientModule.newInstance(
                EventBusPublisher.factory(), bootstraps);
     }
 }
