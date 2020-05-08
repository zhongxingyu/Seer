 package edu.uw.zookeeper.orchestra.peer;
 
 import java.net.InetSocketAddress;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executor;
 
 import javax.annotation.Nullable;
 
 import org.apache.zookeeper.KeeperException;
 
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.google.common.base.Function;
 import com.google.common.base.Throwables;
 import com.google.common.collect.MapMaker;
 import com.google.common.eventbus.Subscribe;
 import com.google.common.util.concurrent.AbstractIdleService;
 import com.google.common.util.concurrent.AsyncFunction;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.common.util.concurrent.Service;
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.TypeLiteral;
 
 import edu.uw.zookeeper.RuntimeModule;
 import edu.uw.zookeeper.client.Materializer;
 import edu.uw.zookeeper.net.ClientConnectionFactory;
 import edu.uw.zookeeper.net.Connection;
 import edu.uw.zookeeper.net.ConnectionFactory;
 import edu.uw.zookeeper.net.ServerConnectionFactory;
 import edu.uw.zookeeper.net.intravm.IntraVmConnection;
 import edu.uw.zookeeper.net.intravm.IntraVmConnectionEndpoint;
 import edu.uw.zookeeper.orchestra.CachedFunction;
 import edu.uw.zookeeper.orchestra.DependentService;
 import edu.uw.zookeeper.orchestra.DependsOn;
 import edu.uw.zookeeper.orchestra.Identifier;
 import edu.uw.zookeeper.orchestra.ServiceLocator;
 import edu.uw.zookeeper.orchestra.backend.BackendRequestService;
 import edu.uw.zookeeper.orchestra.control.ControlMaterializerService;
 import edu.uw.zookeeper.orchestra.control.Orchestra;
 import edu.uw.zookeeper.orchestra.netty.NettyModule;
 import edu.uw.zookeeper.orchestra.peer.protocol.FramedMessagePacketCodec;
 import edu.uw.zookeeper.orchestra.peer.protocol.JacksonModule;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageHandshake;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessagePacket;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessagePacketCodec;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageType;
 import edu.uw.zookeeper.util.Automaton;
 import edu.uw.zookeeper.util.Pair;
 import edu.uw.zookeeper.util.ParameterizedFactory;
 import edu.uw.zookeeper.util.Promise;
 import edu.uw.zookeeper.util.PromiseTask;
 import edu.uw.zookeeper.util.Publisher;
 
 @DependsOn({BackendRequestService.class})
 public class PeerConnectionsService<C extends Connection<? super MessagePacket>> extends DependentService.SimpleDependentService {
 
     public static Module module() {
         return new Module();
     }
     
     public static class Module extends AbstractModule {
 
         public static ParameterizedFactory<Publisher, Pair<Class<MessagePacket>, FramedMessagePacketCodec>> codecFactory(
                 final ObjectMapper mapper) {
             return new ParameterizedFactory<Publisher, Pair<Class<MessagePacket>, FramedMessagePacketCodec>>() {
                 
                 protected final Pair<Class<MessagePacket>, FramedMessagePacketCodec> codec = Pair.create(MessagePacket.class, FramedMessagePacketCodec.newInstance(MessagePacketCodec.newInstance(mapper)));
                 
                 @Override
                 public Pair<Class<MessagePacket>, FramedMessagePacketCodec> get(
                         Publisher value) {
                     return codec;
                 }
             };
         }
         
         public static ParameterizedFactory<Pair<Pair<Class<MessagePacket>, FramedMessagePacketCodec>, Connection<MessagePacket>>, Connection<MessagePacket>> connectionFactory() {
             return new ParameterizedFactory<Pair<Pair<Class<MessagePacket>, FramedMessagePacketCodec>, Connection<MessagePacket>>, Connection<MessagePacket>>() {
                 @Override
                 public Connection<MessagePacket> get(
                         Pair<Pair<Class<MessagePacket>, FramedMessagePacketCodec>, Connection<MessagePacket>> value) {
                     return value.second();
                 }
             };
         }
         
         public Module() {}
         
         @Override
         protected void configure() {
             TypeLiteral<PeerConnectionsService<?>> generic = new TypeLiteral<PeerConnectionsService<?>>() {};
             bind(PeerConnectionsService.class).to(generic);
             bind(generic).to(new TypeLiteral<PeerConnectionsService<Connection<MessagePacket>>>() {});
         }
 
         @Provides @Singleton
         public PeerConnectionsService<Connection<MessagePacket>> getPeerConnectionsService(
                 PeerConfiguration configuration,
                 ControlMaterializerService<?> control,
                 ServiceLocator locator,
                 NettyModule netModule,
                 RuntimeModule runtime) throws InterruptedException, ExecutionException, KeeperException {
             ServerConnectionFactory<MessagePacket, Connection<MessagePacket>> serverConnections = 
                     netModule.servers().get(
                             codecFactory(JacksonModule.getMapper()), 
                             connectionFactory())
                     .get(configuration.getView().address().get());
             runtime.serviceMonitor().addOnStart(serverConnections);
             ClientConnectionFactory<MessagePacket, Connection<MessagePacket>> clientConnections =  
                     netModule.clients().get(
                             codecFactory(JacksonModule.getMapper()), 
                             connectionFactory()).get();
             runtime.serviceMonitor().addOnStart(clientConnections);
            InetSocketAddress loopbackAddress = InetSocketAddress.createUnresolved("localhost", 0);
             Pair<IntraVmConnectionEndpoint<InetSocketAddress>, IntraVmConnectionEndpoint<InetSocketAddress>> loopbackEndpoints = Pair.create(
                    IntraVmConnectionEndpoint.create(loopbackAddress, runtime.publisherFactory().get(), MoreExecutors.sameThreadExecutor()),
                    IntraVmConnectionEndpoint.create(loopbackAddress, runtime.publisherFactory().get(), MoreExecutors.sameThreadExecutor()));
             Pair<IntraVmConnection<InetSocketAddress>, IntraVmConnection<InetSocketAddress>> loopback = 
                     IntraVmConnection.createPair(loopbackEndpoints);
             PeerConnectionsService<Connection<MessagePacket>> instance = 
                     PeerConnectionsService.newInstance(
                             configuration.getView().id(), 
                             serverConnections, 
                             clientConnections, 
                             loopback, 
                             control.materializer(),
                             locator);
             runtime.serviceMonitor().addOnStart(instance);
             return instance;
         }
     }
     
     public static <C extends Connection<? super MessagePacket>> PeerConnectionsService<C> newInstance(
             Identifier identifier,
             ServerConnectionFactory<? super MessagePacket, C> serverConnectionFactory,
             ClientConnectionFactory<? super MessagePacket, C> clientConnectionFactory,
             Pair<? extends Connection<? super MessagePacket>, ? extends Connection<? super MessagePacket>> loopback,
             Materializer<?,?> control,
             ServiceLocator locator) {
         PeerConnectionsService<C> instance = new PeerConnectionsService<C>(
                 identifier, 
                 serverConnectionFactory, 
                 clientConnectionFactory, 
                 loopback, 
                 control,
                 locator);
         instance.new Advertiser(MoreExecutors.sameThreadExecutor());
         return instance;
     }
 
     protected final Identifier identifier;
     protected final Materializer<?,?> control;
     protected final ServerPeerConnections servers;
     protected final ClientPeerConnections clients;
     
     protected PeerConnectionsService(
             Identifier identifier,
             ServerConnectionFactory<? super MessagePacket, C> serverConnectionFactory,
             ClientConnectionFactory<? super MessagePacket, C> clientConnectionFactory,
             Pair<? extends Connection<? super MessagePacket>, ? extends Connection<? super MessagePacket>> loopback,
             Materializer<?,?> control,
             ServiceLocator locator) {
         super(locator);
         this.identifier = identifier;
         this.control = control;
         this.servers = new ServerPeerConnections(serverConnectionFactory);
         this.clients = new ClientPeerConnections(clientConnectionFactory);
         
         servers.put(new ServerPeerConnection(identifier, loopback.first()));
         clients.put(new ClientPeerConnection(identifier, loopback.second()));
     }
     
     public Identifier identifier() {
         return identifier;
     }
     
     public ClientPeerConnections clients() {
         return clients;
     }
 
     public ServerPeerConnections servers() {
         return servers;
     }
     
     @Override
     protected void startUp() throws Exception {
         super.startUp();
         
         servers().start().get();
         clients().start().get();
     }
 
     @Override
     protected void shutDown() throws Exception {
         servers().stop().get();
         clients().stop().get();
 
         super.shutDown();
     }
 
     public class Advertiser implements Service.Listener {
     
         public Advertiser(Executor executor) {
             addListener(this, executor);
         }
         
         @Override
         public void starting() {
         }
     
         @Override
         public void running() {
             Materializer<?,?> materializer = locator().getInstance(ControlMaterializerService.class).materializer();
             try {
                 PeerConfiguration.advertise(identifier(), materializer);
             } catch (Exception e) {
                 throw Throwables.propagate(e);
             }
         }
     
         @Override
         public void stopping(State from) {
         }
     
         @Override
         public void terminated(State from) {
         }
     
         @Override
         public void failed(State from, Throwable failure) {
         }
     }
 
     public class PeerConnections<V extends PeerConnection<Connection<? super MessagePacket>>> extends AbstractIdleService implements Publisher {
         
         protected final ConnectionFactory<? super MessagePacket, C> connections;
         protected final ConcurrentMap<Identifier, V> peers;
         
         public PeerConnections(
                 ConnectionFactory<? super MessagePacket, C> connections) {
             this.connections = connections;
             this.peers = new MapMaker().makeMap();
         }
         
         public ConnectionFactory<? super MessagePacket, C> connections() {
             return connections;
         }
 
         public V get(Identifier peer) {
             return peers.get(peer);
         }
         
         public Set<Map.Entry<Identifier, V>> entrySet() {
             return peers.entrySet();
         }
 
         @Override
         public void post(Object event) {
             connections().post(event);
         }
 
         @Override
         public void register(Object handler) {
             connections().register(handler);
         }
 
         @Override
         public void unregister(Object handler) {
             connections().unregister(handler);
         }
         
         protected V put(Identifier id, V v) {
             V prev = peers.put(id, v);
             new RemoveOnClose(id, v);
             if (prev != null) {
                 prev.close();
             }
             post(v);
             return prev;
         }
 
         protected V putIfAbsent(Identifier id, V v) {
             V prev = peers.putIfAbsent(id, v);
             if (prev != null) {
                 v.close();
             } else {
                 new RemoveOnClose(id, v);
                 post(v);
             }
             return prev;
         }
         
         @Override
         protected void startUp() throws Exception {
             connections().start().get();
         }
 
         @Override
         protected void shutDown() throws Exception {
             connections().stop().get();
         }
 
         protected class RemoveOnClose {
             
             protected final Identifier identifier;
             protected final V instance;
             
             public RemoveOnClose(Identifier identifier, V instance) {
                 this.identifier = identifier;
                 this.instance = instance;
                 instance.register(this);
             }
         
             @Subscribe
             public void handleTransition(Automaton.Transition<?> event) {
                 if (Connection.State.CONNECTION_CLOSED == event.to()) {
                     try {
                         instance.unregister(this);
                     } catch (IllegalArgumentException e) {}
                     peers.remove(identifier, instance);
                 }
             }
         }
     }
     
     public class ClientPeerConnection extends PeerConnection<Connection<? super MessagePacket>> {
 
         public ClientPeerConnection(
                 Identifier remoteIdentifier,
                 Connection<? super MessagePacket> delegate) {
             super(identifier(), remoteIdentifier, delegate);
         }
     }
 
     public class ClientPeerConnections extends PeerConnections<ClientPeerConnection> {
 
         protected final MessagePacket handshake = MessagePacket.of(MessageHandshake.of(identifier));
         protected final ConnectionTask connectionTask = new ConnectionTask();
         protected final CachedFunction<Identifier, Orchestra.Peers.Entity.PeerAddress> lookup;
         
         public ClientPeerConnections(
                 ClientConnectionFactory<? super MessagePacket, C> connections) {
             super(connections);
             this.lookup = Orchestra.Peers.Entity.PeerAddress.lookup(control);
         }
 
         @Override
         public ClientConnectionFactory<? super MessagePacket, C> connections() {
             return (ClientConnectionFactory<? super MessagePacket, C>) connections;
         }
 
         public ListenableFuture<ClientPeerConnection> connect(Identifier identifier) {
             ClientPeerConnection connection = get(identifier);
             if (connection != null) {
                 return Futures.immediateFuture(connection);
             } else {
                 ListenableFuture<Orchestra.Peers.Entity.PeerAddress> lookupFuture;
                 try {
                     lookupFuture = lookup.apply(identifier);
                 } catch (Exception e) {
                     return Futures.immediateFailedFuture(e);
                 }
                 ListenableFuture<C> connectionFuture = Futures.transform(lookupFuture, connectionTask);
                 ConnectTask connectTask = new ConnectTask(identifier);
                 Futures.addCallback(connectionFuture, connectTask);
                 return connectTask;
             }
         }
 
         protected ListenableFuture<MessagePacket> handshake(ClientPeerConnection peer) {
             return peer.write(handshake);
         }
 
         protected ClientPeerConnection put(ClientPeerConnection v) {
             ClientPeerConnection prev = put(v.remoteAddress().getIdentifier(), v);
             handshake(v);
             return prev;
         }
 
         protected ClientPeerConnection putIfAbsent(ClientPeerConnection v) {
             ClientPeerConnection prev = putIfAbsent(v.remoteAddress().getIdentifier(), v);
             if (prev == null) {
                 handshake(v);
             }
             return prev;
         }
         
         public CachedFunction<Identifier, PeerConnectionsService<?>.ClientPeerConnection> connectFunction() {
             return CachedFunction.create(
                     new Function<Identifier, PeerConnectionsService<?>.ClientPeerConnection>() {
                         @Override
                         public @Nullable PeerConnectionsService<?>.ClientPeerConnection apply(Identifier ensemble) {
                             return get(ensemble);
                         }                    
                     }, 
                     new AsyncFunction<Identifier, PeerConnectionsService<?>.ClientPeerConnection>() {
                         @Override
                         public ListenableFuture<PeerConnectionsService<?>.ClientPeerConnection> apply(Identifier ensemble) {
                             return connect(ensemble);
                         }
                     });
         }
         
         protected class ConnectionTask implements AsyncFunction<Orchestra.Peers.Entity.PeerAddress, C> {
             @Override
             public ListenableFuture<C> apply(
                     Orchestra.Peers.Entity.PeerAddress input) throws Exception {
                 return connections().connect(input.get().get());
             }
         }
 
         protected class ConnectTask extends PromiseTask<Identifier, ClientPeerConnection> implements FutureCallback<C> {
         
             public ConnectTask(Identifier task) {
                 this(task, PromiseTask.<ClientPeerConnection>newPromise());
             }
 
             public ConnectTask(Identifier task,
                     Promise<ClientPeerConnection> delegate) {
                 super(task, delegate);
             }
         
             @Override
             public void onSuccess(C result) {
                 try {
                     if (! isDone()) {
                         ClientPeerConnection peer = new ClientPeerConnection(task(), result);
                         ClientPeerConnection prev = putIfAbsent(peer);
                         if (prev != null) {
                             set(prev);
                         } else {
                             set(peer);
                         }
                     } else {
                         result.close();
                     }
                 } catch (Exception e) {
                     result.close();
                     onFailure(e);
                 }
             }
         
             @Override
             public void onFailure(Throwable t) {
                 setException(t);
             }
         }
     }
 
     public class ServerPeerConnection extends PeerConnection<Connection<? super MessagePacket>> {
 
         public ServerPeerConnection(
                 Identifier remoteIdentifier,
                 Connection<? super MessagePacket> delegate) {
             super(identifier(), remoteIdentifier, delegate);
         }
     }
     
     public class ServerPeerConnections extends PeerConnections<ServerPeerConnection> {
 
         public ServerPeerConnections(
                 ServerConnectionFactory<? super MessagePacket, C> connections) {
             super(connections);
         }
 
         @Override
         public ServerConnectionFactory<? super MessagePacket, C> connections() {
             return (ServerConnectionFactory<? super MessagePacket, C>) connections;
         }
 
         @Subscribe
         public void handleServerConnection(Connection<MessagePacket> connection) {
             if (! (connection instanceof PeerConnectionsService.ServerPeerConnection)) {
                 new ServerAcceptTask(connection);
             }
         }
         
         @Override
         protected void startUp() throws Exception {
             connections().register(this);
             
             super.startUp();
         }
 
         @Override
         protected void shutDown() throws Exception {
             try {
                 connections().unregister(this);
             } catch (IllegalArgumentException e) {}
             
             super.shutDown();
         }
 
         protected ServerPeerConnection put(ServerPeerConnection v) {
             return put(v.remoteAddress().getIdentifier(), v);
         }
 
         protected ServerPeerConnection putIfAbsent(ServerPeerConnection v) {
             return putIfAbsent(v.remoteAddress().getIdentifier(), v);
         }
 
         protected class ServerAcceptTask {
 
             protected final Connection<MessagePacket> connection;
             
             protected ServerAcceptTask(Connection<MessagePacket> connection) {
                 this.connection = connection;
                 
                 connection.register(this);
             }
             
             @Subscribe
             public void handleMessage(MessagePacket event) {
                 if (MessageType.MESSAGE_TYPE_HANDSHAKE == event.first().type()) {
                     MessageHandshake body = (MessageHandshake) event.second();
                     ServerPeerConnection peer = new ServerPeerConnection(body.getId(), connection);
                     connection.unregister(this);
                     put(peer);
                 } else {
                     throw new AssertionError(event.toString());
                 }
             }
 
             @Subscribe
             public void handleTransition(Automaton.Transition<?> event) {
                 if (Connection.State.CONNECTION_CLOSED == event.to()) {
                     connection.unregister(this);
                 }
             }
         }
     }
 }
 
