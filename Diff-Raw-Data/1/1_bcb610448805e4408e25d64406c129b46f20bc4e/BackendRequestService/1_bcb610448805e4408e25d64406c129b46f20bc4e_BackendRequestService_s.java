 package edu.uw.zookeeper.safari.backend;
 
 import java.util.List;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ScheduledExecutorService;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.common.base.Function;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.MapMaker;
 import com.google.common.eventbus.Subscribe;
 import com.google.common.util.concurrent.ForwardingListenableFuture;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.common.util.concurrent.Service;
 import com.google.inject.Injector;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.TypeLiteral;
 
 import edu.uw.zookeeper.client.ConnectionClientExecutorService;
 import edu.uw.zookeeper.client.Materializer;
 import edu.uw.zookeeper.common.Automaton;
 import edu.uw.zookeeper.common.Factories;
 import edu.uw.zookeeper.common.TimeValue;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.net.Connection;
 import edu.uw.zookeeper.protocol.ConnectMessage;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.ProtocolCodec;
 import edu.uw.zookeeper.protocol.ProtocolCodecConnection;
 import edu.uw.zookeeper.protocol.client.OperationClientExecutor;
 import edu.uw.zookeeper.protocol.proto.OpCode;
 import edu.uw.zookeeper.safari.Identifier;
 import edu.uw.zookeeper.safari.common.CachedLookup;
 import edu.uw.zookeeper.safari.common.DependentModule;
 import edu.uw.zookeeper.safari.common.DependentService;
 import edu.uw.zookeeper.safari.common.DependsOn;
 import edu.uw.zookeeper.safari.control.Control;
 import edu.uw.zookeeper.safari.control.ControlMaterializerService;
 import edu.uw.zookeeper.safari.data.VolumeCacheService;
 import edu.uw.zookeeper.safari.peer.PeerConfiguration;
 import edu.uw.zookeeper.safari.peer.protocol.JacksonModule;
 import edu.uw.zookeeper.safari.peer.protocol.MessagePacket;
 import edu.uw.zookeeper.safari.peer.protocol.MessageSessionOpenRequest;
 import edu.uw.zookeeper.safari.peer.protocol.MessageSessionOpenResponse;
 import edu.uw.zookeeper.safari.peer.protocol.MessageSessionRequest;
 import edu.uw.zookeeper.safari.peer.protocol.MessageSessionResponse;
 import edu.uw.zookeeper.safari.peer.protocol.ServerPeerConnection;
 import edu.uw.zookeeper.safari.peer.protocol.ServerPeerConnections;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedResponseMessage;
 
 @DependsOn({BackendConnectionsService.class})
 public class BackendRequestService<C extends ProtocolCodecConnection<? super Message.ClientSession, ? extends ProtocolCodec<?,?>, ?>> extends DependentService {
 
     public static Module module() {
         return new Module();
     }
     
     public static class Module extends DependentModule {
 
         public Module() {}
         
         @Override
         protected void configure() {
             super.configure();
             TypeLiteral<BackendRequestService<?>> generic = new TypeLiteral<BackendRequestService<?>>() {};
             bind(BackendRequestService.class).to(generic);
         }
 
         @Provides @Singleton
         public BackendRequestService<?> getBackendRequestService(
                 Injector injector,
                 BackendConnectionsService<?> connections,
                 ServerPeerConnections peers,
                 VolumeCacheService volumes,
                 ScheduledExecutorService executor) throws Exception {
             return BackendRequestService.newInstance(
                     injector, volumes, connections, peers, executor);
         }
 
         @Override
         protected List<com.google.inject.Module> getDependentModules() {
             return ImmutableList.<com.google.inject.Module>of(BackendConnectionsService.module());
         }
     }
     
     public static <C extends ProtocolCodecConnection<? super Message.ClientSession, ? extends ProtocolCodec<?,?>, ?>> BackendRequestService<C> newInstance(
             Injector injector,
             VolumeCacheService volumes,
             BackendConnectionsService<C> connections,
             ServerPeerConnections peers,
             ScheduledExecutorService executor) {
         BackendRequestService<C> instance = new BackendRequestService<C>(
                 injector,
                 connections, 
                 peers, 
                 newVolumePathLookup(), 
                 CachedLookup.create(VolumeShardedOperationTranslators.of(
                         volumes.byId())),
                 executor);
         instance.new Advertiser(injector, MoreExecutors.sameThreadExecutor());
         return instance;
     }
     
     public static Function<ZNodeLabel.Path, Identifier> newVolumePathLookup() {
         return new Function<ZNodeLabel.Path, Identifier>() {
             @Override
             public Identifier apply(ZNodeLabel.Path input) {
                 return BackendSchema.Volumes.Root.getShard(input);
             }
         };
     }
     
     protected static Executor sameThreadExecutor = MoreExecutors.sameThreadExecutor();
 
     protected final Logger logger;
     protected final BackendConnectionsService<C> connections;
     protected final ConcurrentMap<ServerPeerConnection<?>, ServerPeerConnectionListener> peers;
     protected final ConcurrentMap<Long, ShardedClientExecutor<C>> clients;
     protected final Function<ZNodeLabel.Path, Identifier> lookup;
     protected final CachedLookup<Identifier, OperationPrefixTranslator> translator;
     protected final ServerPeerConnectionListener listener;
     protected final ScheduledExecutorService executor;
     
     protected BackendRequestService(
             Injector injector,
             BackendConnectionsService<C> connections,
             ServerPeerConnections peers,
             Function<ZNodeLabel.Path, Identifier> lookup,
             CachedLookup<Identifier, OperationPrefixTranslator> translator,
             ScheduledExecutorService executor) {
         super(injector);
         this.logger = LogManager.getLogger(getClass());
         this.connections = connections;
         this.lookup = lookup;
         this.translator = translator;
         this.executor = executor;
         this.peers = new MapMaker().makeMap();
         this.clients = new MapMaker().makeMap();
         this.listener = new ServerPeerConnectionListener(peers);
     }
     
     public ShardedClientExecutor<C> get(Long sessionId) {
         return clients.get(sessionId);
     }
 
     @Override
     protected void startUp() throws Exception {
         super.startUp();
         
         OperationClientExecutor<C> client = OperationClientExecutor.newInstance(
                 ConnectMessage.Request.NewRequest.newInstance(), 
                 connections.get().get(),
                 executor);
         Control.createPrefix(Materializer.newInstance(
                 BackendSchema.getInstance().get(), 
                 JacksonModule.getSerializer(),
                 client));
         ConnectionClientExecutorService.disconnect(client);
         
         listener.start();
     }
     
     @Override
     protected void shutDown() throws Exception {
         listener.stop();
         
         super.shutDown();
     }
     
     public class Advertiser extends Service.Listener {
 
         protected final Injector injector;
         
         public Advertiser(Injector injector, Executor executor) {
             this.injector = injector;
             addListener(this, executor);
         }
         
         @Override
         public void running() {
             Materializer<?> materializer = injector.getInstance(ControlMaterializerService.class).materializer();
             Identifier myEntity = injector.getInstance(PeerConfiguration.class).getView().id();
             BackendView view = injector.getInstance(BackendConfiguration.class).getView();
             try {
                 BackendConfiguration.advertise(myEntity, view, materializer);
             } catch (Exception e) {
                 throw Throwables.propagate(e);
             }
         }
     }
 
     protected class ServerPeerConnectionListener {
         
         protected final ServerPeerConnections connections;
         protected final ConcurrentMap<ServerPeerConnection<?>, ServerPeerConnectionDispatcher> dispatchers;
         
         public ServerPeerConnectionListener(
                 ServerPeerConnections connections) {
             this.connections = connections;
             this.dispatchers = new MapMaker().makeMap();
         }
         
         public void start() {
             connections.register(this);
             for (ServerPeerConnection<?> c: connections) {
                 handleConnection(c);
             }
         }
         
         public void stop() {
             try {
                 connections.unregister(this);
             } catch (IllegalArgumentException e) {}
         }
         
         @Subscribe
         public void handleConnection(ServerPeerConnection<?> connection) {
             ServerPeerConnectionDispatcher d = new ServerPeerConnectionDispatcher(connection);
             if (dispatchers.putIfAbsent(connection, d) == null) {
                 connection.register(d);
             }
         }
     }
     
     protected class ServerPeerConnectionDispatcher extends Factories.Holder<ServerPeerConnection<?>> {
     
         public ServerPeerConnectionDispatcher(ServerPeerConnection<?> connection) {
             super(connection);
         }
 
         @Subscribe
         public void handleTransition(Automaton.Transition<?> event) {
             if (Connection.State.CONNECTION_CLOSED == event.to()) {
                 try {
                     get().unregister(this);
                 } catch (IllegalArgumentException e) {}
                 listener.dispatchers.remove(get(), this);
             }
         }
         
         @Subscribe
         public void handlePeerMessage(MessagePacket message) {
             switch (message.first().type()) {
             case MESSAGE_TYPE_HANDSHAKE:
             case MESSAGE_TYPE_HEARTBEAT:
                 break;
             case MESSAGE_TYPE_SESSION_OPEN_REQUEST:
                 handleMessageSessionOpen(message.getBody(MessageSessionOpenRequest.class));
                 break;
             case MESSAGE_TYPE_SESSION_REQUEST:
                 handleMessageSessionRequest(message.getBody(MessageSessionRequest.class));
                 break;
             default:
                 throw new AssertionError(message.toString());
             }
         }
         
         protected void handleMessageSessionOpen(MessageSessionOpenRequest message) {
             Long sessionId = message.getIdentifier();
             ShardedClientExecutor<C> client = clients.get(sessionId);
             if (client == null) {
                 new ClientCallback(message);
             } else {
                 Futures.addCallback(
                         client.session(), 
                         new SessionOpenResponseTask(message),
                         sameThreadExecutor);
             }
         }
         
         protected void handleMessageSessionRequest(MessageSessionRequest message) {
             logger.debug("{}", message);
             ShardedClientExecutor<C> client = clients.get(message.getIdentifier());
             if (client != null) {
                 client.submit(message.getValue());
             } else {
                 // FIXME
                 throw new UnsupportedOperationException();
             }
         }
 
         protected class ClientCallback extends ForwardingListenableFuture<ShardedClientExecutor<C>>
                 implements FutureCallback<ShardedResponseMessage<?>>, Runnable {
 
             protected final MessageSessionOpenRequest session;
             protected final ListenableFuture<ShardedClientExecutor<C>> client;
             
             public ClientCallback(
                     MessageSessionOpenRequest session) {
                 this.session = session;
                 this.client = Futures.transform(
                         connections.get(), 
                         new SessionOpenTask(),
                         sameThreadExecutor);
             }
             
             public MessageSessionOpenRequest session() {
                 return session;
             }
 
             @Override
             public void run() {
                 try {
                     ShardedClientExecutor<C> client = get();
                     if (clients.putIfAbsent(session.getIdentifier(), client) == null) {
                         Futures.addCallback(
                                 client.session(), 
                                 new SessionOpenResponseTask(session),
                                 sameThreadExecutor);
                     } else {
                         throw new AssertionError(String.valueOf(session));
                     }
                     // client.register(this);
                 } catch (Exception e) {
                     onFailure(e);
                 }
             }
 
             @Override
             public void onSuccess(ShardedResponseMessage<?> result) {
                 instance.write(MessagePacket.of(MessageSessionResponse.of(
                         session.getIdentifier(), result)));
                 if (result.record().opcode() == OpCode.CLOSE_SESSION) {
                     try {
                         clients.remove(session.getIdentifier(), client.get());
                     } catch (Exception e) {
                         throw new AssertionError(e);
                     }
                 }
             }
 
             @Override
             public void onFailure(Throwable t) {
                 // FIXME
                 throw new AssertionError(t);
             }
 
             /*@Subscribe
             public void handleTransition(Automaton.Transition<?> event) {
                 if (Connection.State.CONNECTION_CLOSED == event.to()) {
                     try {
                         client.unregister(this);
                     } catch (Exception e) {
                     }
                 }
             }*/
             
             @Override
             protected ListenableFuture<ShardedClientExecutor<C>> delegate() {
                 return client;
             }
 
             protected class SessionOpenTask implements Function<C, ShardedClientExecutor<C>> {
             
                 public SessionOpenTask() {}
                 
                 @Override
                 public ShardedClientExecutor<C> apply(C connection) {
                     ConnectMessage.Request request;
                     if (session.getValue() instanceof ConnectMessage.Request.NewRequest) {
                         request = ConnectMessage.Request.NewRequest.newInstance(
                                 TimeValue.milliseconds(session.getValue().getTimeOut()), 
                                     connections.zxids().get());
                     } else {
                         request = ConnectMessage.Request.RenewRequest.newInstance(
                                 session.getValue().toSession(), connections.zxids().get());
                     }
                     
                     return ShardedClientExecutor.newInstance(
                             ClientCallback.this,
                             lookup, 
                             translator.asLookup(), 
                             request, 
                             connection,
                             executor);
                 }
             }
         }
 
         protected class SessionOpenResponseTask implements FutureCallback<ConnectMessage.Response> {
 
             protected final MessageSessionOpenRequest request;
             
             public SessionOpenResponseTask(
                     MessageSessionOpenRequest request) {
                 this.request = request;
             }
 
             @Override
             public void onSuccess(ConnectMessage.Response result) {
                 instance.write(
                         MessagePacket.of(
                                 MessageSessionOpenResponse.of(
                                         request.getIdentifier(), result)));
             }
         
             @Override
             public void onFailure(Throwable t) {
                 // FIXME
                 throw new AssertionError(t);
             }
         }
     }
 }
