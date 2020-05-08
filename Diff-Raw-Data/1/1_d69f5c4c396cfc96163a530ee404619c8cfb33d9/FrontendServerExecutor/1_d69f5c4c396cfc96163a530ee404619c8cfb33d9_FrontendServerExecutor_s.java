 package edu.uw.zookeeper.orchestra.frontend;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ScheduledExecutorService;
 
 import com.google.common.base.Function;
 import com.google.common.base.Optional;
 import com.google.common.collect.MapMaker;
 import com.google.common.eventbus.Subscribe;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.inject.AbstractModule;
 import com.google.inject.Inject;
 import com.google.inject.Provides;
 import com.google.inject.Singleton;
 import edu.uw.zookeeper.Session;
 import edu.uw.zookeeper.common.Automaton;
 import edu.uw.zookeeper.common.Configuration;
 import edu.uw.zookeeper.common.Factories;
 import edu.uw.zookeeper.common.Factory;
 import edu.uw.zookeeper.common.Pair;
 import edu.uw.zookeeper.common.Processor;
 import edu.uw.zookeeper.common.Processors;
 import edu.uw.zookeeper.common.Publisher;
 import edu.uw.zookeeper.common.TaskExecutor;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.event.SessionStateEvent;
 import edu.uw.zookeeper.net.Connection;
 import edu.uw.zookeeper.orchestra.common.CachedFunction;
 import edu.uw.zookeeper.orchestra.common.DependentService;
 import edu.uw.zookeeper.orchestra.common.DependsOn;
 import edu.uw.zookeeper.orchestra.common.Identifier;
 import edu.uw.zookeeper.orchestra.common.ServiceLocator;
 import edu.uw.zookeeper.orchestra.data.Volume;
 import edu.uw.zookeeper.orchestra.data.VolumeCacheService;
 import edu.uw.zookeeper.orchestra.peer.protocol.ClientPeerConnections;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessagePacket;
 import edu.uw.zookeeper.orchestra.peer.protocol.MessageSessionResponse;
 import edu.uw.zookeeper.orchestra.peer.protocol.ShardedResponseMessage;
 import edu.uw.zookeeper.orchestra.peer.protocol.PeerConnection.ClientPeerConnection;
 import edu.uw.zookeeper.protocol.ConnectMessage;
 import edu.uw.zookeeper.protocol.FourLetterRequest;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.ProtocolRequestMessage;
 import edu.uw.zookeeper.protocol.ProtocolResponseMessage;
 import edu.uw.zookeeper.protocol.SessionOperation;
 import edu.uw.zookeeper.protocol.FourLetterResponse;
 import edu.uw.zookeeper.protocol.server.AssignZxidProcessor;
 import edu.uw.zookeeper.protocol.server.ConnectTableProcessor;
 import edu.uw.zookeeper.protocol.server.FourLetterRequestProcessor;
 import edu.uw.zookeeper.protocol.server.ServerTaskExecutor;
 import edu.uw.zookeeper.protocol.server.ZxidEpochIncrementer;
 import edu.uw.zookeeper.protocol.server.ZxidGenerator;
 import edu.uw.zookeeper.protocol.server.ZxidReference;
 import edu.uw.zookeeper.protocol.proto.IDisconnectRequest;
 import edu.uw.zookeeper.protocol.proto.OpCode;
 import edu.uw.zookeeper.protocol.proto.OpCodeXid;
 import edu.uw.zookeeper.protocol.proto.Records;
 import edu.uw.zookeeper.server.DefaultSessionParametersPolicy;
 import edu.uw.zookeeper.server.ExpiringSessionService;
 import edu.uw.zookeeper.server.ExpiringSessionTable;
 import edu.uw.zookeeper.server.SessionParametersPolicy;
 import edu.uw.zookeeper.server.SessionTable;
 
 @DependsOn({EnsembleConnectionsService.class, VolumeCacheService.class, AssignmentCacheService.class, ExpiringSessionService.class })
 public class FrontendServerExecutor extends DependentService {
 
     public static Module module() {
         return new Module();
     }
     
     public static class Module extends AbstractModule {
 
         public Module() {}
         
         @Override
         protected void configure() {
             bind(ServerTaskExecutor.class).to(FrontendServerTaskExecutor.class).in(Singleton.class);
            bind(ZxidEpochIncrementer.class).in(Singleton.class);
             bind(ZxidGenerator.class).to(ZxidEpochIncrementer.class).in(Singleton.class);
             bind(ZxidReference.class).to(ZxidGenerator.class).in(Singleton.class);
         }
 
         @Provides @Singleton
         public ExpiringSessionTable getSessionTable(
                 Configuration configuration,
                 Factory<? extends Publisher> publishers) {
             SessionParametersPolicy policy = DefaultSessionParametersPolicy.create(configuration);
             return ExpiringSessionTable.newInstance(publishers.get(), policy);
         }
 
         @Provides @Singleton
         public ExpiringSessionService getExpiringSessionService(
                 Configuration configuration,
                 ScheduledExecutorService executor,
                 ExpiringSessionTable sessions) {
             return ExpiringSessionService.newInstance(sessions, executor, configuration);
         }
         
         @Provides @Singleton
         public ZxidEpochIncrementer getZxids() {
             return ZxidEpochIncrementer.fromZero();
         }
 
         @Provides @Singleton
         public FrontendServerExecutor getServerExecutor(
                 ServiceLocator locator,
                 VolumeCacheService volumes,
                 AssignmentCacheService assignments,
                 PeerToEnsembleLookup peerToEnsemble,
                 ClientPeerConnections peers,
                 EnsembleConnectionsService ensembles,
                 Executor executor,
                 ExpiringSessionTable sessions,
                 ZxidGenerator zxids) {
             return FrontendServerExecutor.newInstance(
                             volumes, assignments, peerToEnsemble, peers, ensembles, executor, sessions, zxids, locator);
         }
 
         @Provides @Singleton
         public FrontendServerTaskExecutor getServerTaskExecutor(
                 FrontendServerExecutor server) {
             return server.asTaskExecutor();
         }
     }
     
     public static FrontendServerExecutor newInstance(
             VolumeCacheService volumes,
             AssignmentCacheService assignments,
             PeerToEnsembleLookup peerToEnsemble,
             ClientPeerConnections peers,
             EnsembleConnectionsService ensembles,
             Executor executor,
             ExpiringSessionTable sessions,
             ZxidGenerator zxids,
             ServiceLocator locator) {
         ConcurrentMap<Long, FrontendSessionExecutor> handlers = new MapMaker().makeMap();
         FrontendServerTaskExecutor server = FrontendServerTaskExecutor.newInstance(handlers, volumes, assignments, peerToEnsemble, ensembles, executor, sessions, zxids);
         return new FrontendServerExecutor(handlers, server, peers, locator);
     }
     
     protected final FrontendServerTaskExecutor executor;
     protected final ConcurrentMap<Long, FrontendSessionExecutor> handlers;
     protected final ClientPeerConnectionListener connections;
     
     protected FrontendServerExecutor(
             ConcurrentMap<Long, FrontendSessionExecutor> handlers,
             FrontendServerTaskExecutor executor,
             ClientPeerConnections connections,
             ServiceLocator locator) {
         super(locator);
         this.handlers = handlers;
         this.executor = executor;
         this.connections = new ClientPeerConnectionListener(handlers, connections);
     }
     
     public FrontendServerTaskExecutor asTaskExecutor() {
         return executor;
     }
 
     @Override
     protected void startUp() throws Exception {
         super.startUp();
         connections.start();
     }
 
     
     @Override
     protected void shutDown() throws Exception {
         super.shutDown();
         connections.stop();
     }
     
     protected static class ResponseProcessor implements Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> {
 
         public static ResponseProcessor create(
                 ConcurrentMap<Long, FrontendSessionExecutor> handlers,
                 SessionTable sessions,
                 ZxidGenerator zxids) {
             return new ResponseProcessor(handlers, sessions, zxids);
         }
         
         protected final SessionTable sessions;
         protected final AssignZxidProcessor zxids;
         protected final Map<Long, ?> handlers;
 
         @Inject
         public ResponseProcessor(
                 Map<Long, ?> handlers,
                 SessionTable sessions,
                 ZxidGenerator zxids) {
             this(handlers, sessions, AssignZxidProcessor.newInstance(zxids));
         }
         
         public ResponseProcessor(
                 Map<Long, ?> handlers,
                 SessionTable sessions,
                 AssignZxidProcessor zxids) {
             this.handlers = handlers;
             this.sessions = sessions;
             this.zxids = zxids;
         }
         
         @Override
         public Message.ServerResponse<?> apply(Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>> input) {
             Optional<Operation.ProtocolRequest<?>> request = input.second().first();
             Records.Response response = input.second().second();
             int xid;
             if (response instanceof Operation.RequestId) {
                 xid = ((Operation.RequestId) response).getXid();
             } else {
                 xid = request.get().getXid();
             }
             OpCode opcode;
             if (OpCodeXid.has(xid)) {
                 opcode = OpCodeXid.of(xid).getOpcode();
             } else {
                 opcode = request.get().getRecord().getOpcode();
             }
             long zxid = zxids.apply(opcode);
             if ((opcode == OpCode.CLOSE_SESSION) && !(response instanceof Operation.Error)) {
                 Long sessionId = input.first();
                 sessions.remove(sessionId);
                 handlers.remove(sessionId);
             }
             return ProtocolResponseMessage.of(xid, zxid, response);
         }
         
     }
 
     protected static class FrontendServerTaskExecutor extends ServerTaskExecutor {
         
         public static FrontendServerTaskExecutor newInstance(
                 ConcurrentMap<Long, FrontendSessionExecutor> handlers,
                 VolumeCacheService volumes,
                 AssignmentCacheService assignments,
                 PeerToEnsembleLookup peerToEnsemble,
                 EnsembleConnectionsService connections,
                 Executor executor,
                 ExpiringSessionTable sessions,
                 ZxidGenerator zxids) {
             TaskExecutor<FourLetterRequest, FourLetterResponse> anonymousExecutor = 
                     ServerTaskExecutor.ProcessorExecutor.of(
                             FourLetterRequestProcessor.newInstance());
             TaskExecutor<Pair<ConnectMessage.Request, Publisher>, ConnectMessage.Response> connectExecutor = 
                     ServerTaskExecutor.ProcessorExecutor.of(
                             new ConnectProcessor(
                                 handlers,
                                 volumes.asLookup(),
                                 assignments.get().asLookup(),
                                 peerToEnsemble.get().asLookup().first(),
                                 connections.getConnectionForEnsemble(),
                                 ConnectTableProcessor.create(sessions, zxids),
                                 ResponseProcessor.create(handlers, sessions, zxids),
                                 executor));
             SessionTaskExecutor sessionExecutor = 
                     new SessionTaskExecutor(sessions, handlers);
             return new FrontendServerTaskExecutor(
                     anonymousExecutor,
                     connectExecutor,
                     sessionExecutor);
         }
         
         public FrontendServerTaskExecutor(
                 TaskExecutor<? super FourLetterRequest, ? extends FourLetterResponse> anonymousExecutor,
                 TaskExecutor<Pair<ConnectMessage.Request, Publisher>, ConnectMessage.Response> connectExecutor,
                 SessionTaskExecutor sessionExecutor) {
             super(anonymousExecutor, connectExecutor, sessionExecutor);
         }
         
         @Override
         public SessionTaskExecutor getSessionExecutor() {
             return (SessionTaskExecutor) sessionExecutor;
         }
     }
     
     protected static class SessionTaskExecutor implements TaskExecutor<SessionOperation.Request<?>, Message.ServerResponse<?>> {
 
         protected final ExpiringSessionTable sessions;
         protected final ConcurrentMap<Long, FrontendSessionExecutor> handlers;
 
         public SessionTaskExecutor(
                 ExpiringSessionTable sessions,
                 ConcurrentMap<Long, FrontendSessionExecutor> handlers) {
             this.sessions = sessions;
             this.handlers = handlers;
             
             sessions.register(this);
         }
         
         @Override
         public ListenableFuture<Message.ServerResponse<?>> submit(
                 SessionOperation.Request<?> request) {
             long sessionId = request.getSessionId();
             sessions.touch(sessionId);
             FrontendSessionExecutor executor = handlers.get(sessionId);
             return executor.submit(ProtocolRequestMessage.of(request.getXid(), request.getRecord()));
         }
 
         @Subscribe
         public void handleSessionStateEvent(SessionStateEvent event) {
             switch (event.event()) {
             case SESSION_EXPIRED:
             {
                 long sessionId = event.session().id();
                 FrontendSessionExecutor executor = handlers.get(sessionId);
                 if (executor != null) {
                     executor.submit(ProtocolRequestMessage.of(0, Records.newInstance(IDisconnectRequest.class)));
                 }
                 break;
             }
             default:
                 break;
             }
         }
     }
 
     protected static class ConnectProcessor implements Processor<Pair<ConnectMessage.Request, Publisher>, ConnectMessage.Response> {
 
         protected final ConnectTableProcessor connector;
         protected final Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor;
         protected final ConcurrentMap<Long, FrontendSessionExecutor> handlers;
         protected final CachedFunction<ZNodeLabel.Path, Volume> volumeLookup;
         protected final CachedFunction<Identifier, Identifier> assignmentLookup;
         protected final Function<? super Identifier, Identifier> ensembleForPeer;
         protected final CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup;
         protected final Executor executor;
         
         public ConnectProcessor(
                 ConcurrentMap<Long, FrontendSessionExecutor> handlers,
                 CachedFunction<ZNodeLabel.Path, Volume> volumeLookup,
                 CachedFunction<Identifier, Identifier> assignmentLookup,
                 Function<? super Identifier, Identifier> ensembleForPeer,
                 CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup,
                 ConnectTableProcessor connector,
                 Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor,
                 Executor executor) {
             this.connector = connector;
             this.processor = processor;
             this.volumeLookup = volumeLookup;
             this.assignmentLookup = assignmentLookup;
             this.ensembleForPeer = ensembleForPeer;
             this.connectionLookup = connectionLookup;
             this.executor = executor;
             this.handlers = handlers;
         }
         
         @Override
         public ConnectMessage.Response apply(Pair<ConnectMessage.Request, Publisher> input) {
             ConnectMessage.Response output = connector.apply(input.first());
             if (output instanceof ConnectMessage.Response.Valid) {
                 Session session = output.toSession();
                 handlers.putIfAbsent(
                         session.id(), 
                         FrontendSessionExecutor.newInstance(
                                 session, 
                                 input.second(),
                                 processor,
                                 volumeLookup,
                                 assignmentLookup,
                                 ensembleForPeer,
                                 connectionLookup,
                                 executor));
                 // TODO: what about reconnects?
             }
             input.second().post(output);
             return output;
         }
     }
     
     protected static class ClientPeerConnectionListener {
         
         protected final ClientPeerConnections connections;
         protected final ConcurrentMap<Long, FrontendSessionExecutor> executors;
         protected final ConcurrentMap<ClientPeerConnection<?>, ClientPeerConnectionDispatcher> dispatchers;
         
         public ClientPeerConnectionListener(
                 ConcurrentMap<Long, FrontendSessionExecutor> executors,
                 ClientPeerConnections connections) {
             this.executors = executors;
             this.connections = connections;
             this.dispatchers = new MapMaker().makeMap();
         }
         
         public void start() {
             connections.register(this);
             for (ClientPeerConnection<?> c: connections) {
                 handleConnection(c);
             }
         }
         
         public void stop() {
             try {
                 connections.unregister(this);
             } catch (IllegalArgumentException e) {}
         }
         
         @Subscribe
         public void handleConnection(ClientPeerConnection<?> connection) {
             ClientPeerConnectionDispatcher d = new ClientPeerConnectionDispatcher(connection);
             if (dispatchers.putIfAbsent(connection, d) == null) {
                 connection.register(d);
             }
         }
 
         protected class ClientPeerConnectionDispatcher extends Factories.Holder<ClientPeerConnection<?>> {
 
             public ClientPeerConnectionDispatcher(
                     ClientPeerConnection<?> connection) {
                 super(connection);
             }
 
             @Subscribe
             public void handleTransition(Automaton.Transition<?> event) {
                 if (Connection.State.CONNECTION_CLOSED == event.to()) {
                     try {
                         get().unregister(this);
                     } catch (IllegalArgumentException e) {}
                     dispatchers.remove(get(), this);
                     for (FrontendSessionExecutor e: executors.values()) {
                         e.handleTransition(Pair.<Identifier, Automaton.Transition<?>>create(get().remoteAddress().getIdentifier(), event));
                     }
                 }
             }
             
             @Subscribe
             public void handleMessage(MessagePacket message) {
                 switch (message.first().type()) {
                 case MESSAGE_TYPE_SESSION_RESPONSE:
                 {
                     MessageSessionResponse body = message.getBody(MessageSessionResponse.class);
                     FrontendSessionExecutor e = executors.get(body.getIdentifier());
                     e.handleResponse(Pair.<Identifier, ShardedResponseMessage<?>>create(get().remoteAddress().getIdentifier(), body.getValue()));
                     break;
                 }
                 default:
                     break;
                 }
             }
         }
     }
 }
