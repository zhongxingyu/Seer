 package edu.uw.zookeeper.orchestra.backend;
 
import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.TimeUnit;
 
 import com.google.common.base.Function;
 import com.google.common.base.Throwables;
 import com.google.common.collect.MapMaker;
 import com.google.common.eventbus.Subscribe;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.common.util.concurrent.Service;
 import com.google.inject.AbstractModule;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import com.google.inject.TypeLiteral;
 
 import edu.uw.zookeeper.RuntimeModule;
 import edu.uw.zookeeper.client.Materializer;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.net.Connection;
 import edu.uw.zookeeper.orchestra.DependentService;
 import edu.uw.zookeeper.orchestra.DependsOn;
 import edu.uw.zookeeper.orchestra.Identifier;
 import edu.uw.zookeeper.orchestra.Volume;
 import edu.uw.zookeeper.orchestra.ServiceLocator;
 import edu.uw.zookeeper.orchestra.VolumeLookup;
 import edu.uw.zookeeper.orchestra.VolumeAssignmentService;
 import edu.uw.zookeeper.orchestra.control.ControlMaterializerService;
 import edu.uw.zookeeper.orchestra.peer.PeerConfiguration;
 import edu.uw.zookeeper.orchestra.peer.PeerConnectionsService;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageHandshake;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessagePacket;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageSessionClose;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageSessionOpen;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageSessionRequest;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageSessionResponse;
 import edu.uw.zookeeper.protocol.ConnectMessage;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.client.AssignXidCodec;
 import edu.uw.zookeeper.protocol.client.ConnectTask;
 import edu.uw.zookeeper.protocol.client.PingingClient;
 import edu.uw.zookeeper.protocol.proto.IDisconnectRequest;
 import edu.uw.zookeeper.protocol.proto.Records;
 import edu.uw.zookeeper.util.Automaton;
 import edu.uw.zookeeper.util.Pair;
 import edu.uw.zookeeper.util.Reference;
 import edu.uw.zookeeper.util.TimeValue;
 
 @DependsOn({ControlMaterializerService.class, BackendConnectionsService.class})
 public class BackendRequestService<C extends Connection<? super Operation.Request>> extends DependentService.SimpleDependentService {
 
     public static Module module() {
         return new Module();
     }
     
     public static class Module extends AbstractModule {
 
         public Module() {}
         
         @Override
         protected void configure() {
             install(BackendConnectionsService.module());
             TypeLiteral<BackendRequestService<?>> generic = new TypeLiteral<BackendRequestService<?>>() {};
             bind(BackendRequestService.class).to(generic);
             bind(generic).to(new TypeLiteral<BackendRequestService<PingingClient<Operation.Request,AssignXidCodec,Connection<Operation.Request>>>>() {});
         }
 
         @Provides @Singleton
         public BackendRequestService<PingingClient<Operation.Request,AssignXidCodec,Connection<Operation.Request>>> getBackendRequestService(
                 ServiceLocator locator,
                 BackendConnectionsService<PingingClient<Operation.Request,AssignXidCodec,Connection<Operation.Request>>> connections,
                 VolumeAssignmentService volumes,
                 RuntimeModule runtime) throws Exception {
             BackendRequestService<PingingClient<Operation.Request,AssignXidCodec,Connection<Operation.Request>>> instance = 
                     BackendRequestService.newInstance(locator, volumes.get(), connections);
             runtime.serviceMonitor().addOnStart(instance);
             return instance;
         }
     }
     
     public static <C extends Connection<? super Operation.Request>> BackendRequestService<C> newInstance(
             ServiceLocator locator,
             final VolumeLookup volumes,
             BackendConnectionsService<C> connections) {
 
         Function<ZNodeLabel.Path, Identifier> lookup = new Function<ZNodeLabel.Path, Identifier>() {
             @Override
             public Identifier apply(ZNodeLabel.Path input) {
                 return volumes.get(input).getId();
             }
         };
         VolumeShardedOperationTranslators translator = new VolumeShardedOperationTranslators(
                 new Function<Identifier, Volume>() {
                     @Override
                     public Volume apply(Identifier input) {
                         return volumes.get(input);
                     }
                 });
         BackendRequestService<C> instance = new BackendRequestService<C>(locator, connections, lookup, translator);
         instance.new Advertiser(MoreExecutors.sameThreadExecutor());
         return instance;
     }
 
     protected final BackendConnectionsService<C> connections;
    protected final ConcurrentMap<PeerConnectionsService<?>.ServerPeerConnection, ServerPeerConnectionListener> peers;
     protected final ConcurrentMap<Long, ServerPeerConnectionListener.BackendClient> clients;
     protected final ShardedOperationTranslators translator;
     protected final Function<ZNodeLabel.Path, Identifier> lookup;
     
     protected BackendRequestService(
             ServiceLocator locator,
             BackendConnectionsService<C> connections,
             Function<ZNodeLabel.Path, Identifier> lookup,
             ShardedOperationTranslators translator) {
         super(locator);
         this.connections = connections;
        this.peers = new MapMaker().makeMap();
         this.clients = new MapMaker().makeMap();
         this.lookup = lookup;
         this.translator = translator;
     }
     
     public ServerPeerConnectionListener.BackendClient get(Long sessionId) {
         return clients.get(sessionId);
     }
 
     @Subscribe
     public void handleServerPeerConnection(PeerConnectionsService<?>.ServerPeerConnection peer) {
         new ServerPeerConnectionListener(peer);
     }
 
     @Override
     protected void startUp() throws Exception {
         super.startUp();
         
        PeerConnectionsService<?> peers = locator().getInstance(PeerConnectionsService.class);
        peers.servers().register(this);
        for (Entry<Identifier, PeerConnectionsService<?>.ServerPeerConnection> e: peers.servers().entrySet()) {
            handleServerPeerConnection(e.getValue());
        }
     }
     
     @Override
     protected void shutDown() throws Exception {
         try {
             locator().getInstance(PeerConnectionsService.class).servers().unregister(this);
         } catch (IllegalArgumentException e) {}
         
         super.shutDown();
     }
     
     protected ShardedClientConnectionExecutor<C> connect(MessageSessionOpen message) {
         ConnectMessage.Request request = ConnectMessage.Request.NewRequest.newInstance(TimeValue.create(
                 Long.valueOf(message.getTimeOutMillis()), TimeUnit.MILLISECONDS), connections.zxids().get());
         C connection = connections.get();
         ShardedClientConnectionExecutor<C> client = 
                 ShardedClientConnectionExecutor.newInstance(
                         translator, lookup, ConnectTask.create(connection, request), connection);
         return client;
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
             Identifier myEntity = locator().getInstance(PeerConfiguration.class).getView().id();
             BackendView view = locator().getInstance(BackendConfiguration.class).getView();
             try {
                 BackendConfiguration.advertise(myEntity, view, materializer);
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
 
     protected class ServerPeerConnectionListener implements Reference<PeerConnectionsService<?>.ServerPeerConnection> {
     
         protected final PeerConnectionsService<?>.ServerPeerConnection peer;
         
         public ServerPeerConnectionListener(PeerConnectionsService<?>.ServerPeerConnection peer) {
             this.peer = peer;
            if (peers.putIfAbsent(peer, this) == null) {
                peer.register(this);
            }
            // TODO: remove from map when peer closes?
         }
         
         @Override
         public PeerConnectionsService<?>.ServerPeerConnection get() {
             return peer;
         }
         
         @Subscribe
         public void handlePeerMessage(MessagePacket message) {
             switch (message.first().type()) {
             case MESSAGE_TYPE_HANDSHAKE:
                 handleMessageHandshake((MessageHandshake) message.second());
                 break;
             case MESSAGE_TYPE_SESSION_OPEN:
                 handleMessageSessionOpen((MessageSessionOpen) message.second());
                 break;
             case MESSAGE_TYPE_SESSION_CLOSE:
                 handleMessageSessionClose((MessageSessionClose) message.second());
                 break;
             case MESSAGE_TYPE_SESSION_REQUEST:
                 handleMessageSessionRequest((MessageSessionRequest) message.second());
                 break;
             default:
                 throw new AssertionError(message.toString());
             }
         }
         
         public void handleMessageHandshake(MessageHandshake second) {
             assert(second.getId() == get().localAddress().getIdentifier());
         }
 
         public void handleMessageSessionOpen(MessageSessionOpen message) {
             // we don't want to leave this function until we have a client
             // because we may get a request message immediately and we need somewhere to send it
             long sessionId = message.getSessionId();
             BackendClient client = clients.get(sessionId);
             if (client == null) {
                 ShardedClientConnectionExecutor<C> connection = connect(message);
                 client = new BackendClient(sessionId, connection);
                 BackendClient prev = clients.putIfAbsent(sessionId, client);
                 if (prev != null) {
                     try {
                         client.disconnect();
                     } catch (Throwable t) {}
                 }
             }
         }
     
         public void handleMessageSessionClose(MessageSessionClose message) {
             final long sessionId = message.getSessionId();
             final BackendClient client = clients.get(sessionId);
             if (client != null) {
                 try {
                     final ListenableFuture<Pair<Message.ClientRequest<Records.Request>, Message.ServerResponse<Records.Response>>> future = client.disconnect();
                     future.addListener(new Runnable() {
                         @Override
                         public void run() {
                             try {
                                 future.get();
                                 clients.remove(client.getSessionId(), client);
                             } catch (Exception e) {}
                         }
                     }, MoreExecutors.sameThreadExecutor());
                 } catch (Throwable t) {}
             }
         }
         
         public void handleMessageSessionRequest(MessageSessionRequest message) {
             BackendClient client = clients.get(message.getSessionId());
             if (client != null) {
                 client.submit(message.getRequest());
             } else {
                 // TODO
                 throw new UnsupportedOperationException();
             }
         }
     
         protected class BackendClient {
             
             protected final long sessionId;
             protected final ShardedClientConnectionExecutor<?> client;
             
             public BackendClient(
                     long sessionId,
                     ShardedClientConnectionExecutor<?> client) {
                 this.sessionId = sessionId;
                 this.client = client;
                 
                 client.register(this);
             }
             
             public long getSessionId() {
                 return sessionId;
             }
             
             public ShardedClientConnectionExecutor<?> getClient() {
                 return client;
             }
             
             public ListenableFuture<Pair<Message.ClientRequest<Records.Request>, Message.ServerResponse<Records.Response>>> disconnect() {
                 return submit(Records.newInstance(IDisconnectRequest.class));
             }
 
             public ListenableFuture<Pair<Message.ClientRequest<Records.Request>, Message.ServerResponse<Records.Response>>> submit(Operation.Request request) {
                 return getClient().submit(request);
             }
     
             @Subscribe
             public void handleTransition(Automaton.Transition<?> event) {
                 if (Connection.State.CONNECTION_CLOSED == event.to()) {
                     try {
                         getClient().unregister(this);
                     } catch (IllegalArgumentException e) {}
                 }
             }
             
             @Subscribe
             public void handleResponse(ShardedResponseMessage<?> message) {
                 get().write(MessagePacket.of(MessageSessionResponse.of(getSessionId(), message)));
             }
         }
     } 
 }
