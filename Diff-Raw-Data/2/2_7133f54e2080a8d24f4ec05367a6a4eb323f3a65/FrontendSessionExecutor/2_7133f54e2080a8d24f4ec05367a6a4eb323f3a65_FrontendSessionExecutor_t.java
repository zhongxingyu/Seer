 package edu.uw.zookeeper.safari.frontend;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executor;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.annotation.Nullable;
 
 import org.apache.zookeeper.KeeperException;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.base.Throwables;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.google.common.eventbus.Subscribe;
 import com.google.common.util.concurrent.AsyncFunction;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.MoreExecutors;
 
 import edu.uw.zookeeper.protocol.Session;
 import edu.uw.zookeeper.common.Actor;
 import edu.uw.zookeeper.common.Automaton;
 import edu.uw.zookeeper.common.ExecutedActor;
 import edu.uw.zookeeper.common.LoggingPromise;
 import edu.uw.zookeeper.common.Pair;
 import edu.uw.zookeeper.common.Processors;
 import edu.uw.zookeeper.common.Promise;
 import edu.uw.zookeeper.common.PromiseTask;
 import edu.uw.zookeeper.common.Publisher;
 import edu.uw.zookeeper.common.SettableFuturePromise;
 import edu.uw.zookeeper.common.TaskExecutor;
 import edu.uw.zookeeper.data.CreateFlag;
 import edu.uw.zookeeper.data.CreateMode;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.net.Connection;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.ProtocolRequestMessage;
 import edu.uw.zookeeper.protocol.proto.IDisconnectResponse;
 import edu.uw.zookeeper.protocol.proto.IErrorResponse;
 import edu.uw.zookeeper.protocol.proto.IMultiRequest;
 import edu.uw.zookeeper.protocol.proto.IMultiResponse;
 import edu.uw.zookeeper.protocol.proto.OpCode;
 import edu.uw.zookeeper.protocol.proto.OpCodeXid;
 import edu.uw.zookeeper.protocol.proto.Records;
 import edu.uw.zookeeper.protocol.server.PingProcessor;
 import edu.uw.zookeeper.safari.Identifier;
 import edu.uw.zookeeper.safari.common.CachedFunction;
 import edu.uw.zookeeper.safari.common.CachedLookup;
 import edu.uw.zookeeper.safari.common.LinkedIterator;
 import edu.uw.zookeeper.safari.common.LinkedQueue;
 import edu.uw.zookeeper.safari.common.SharedLookup;
 import edu.uw.zookeeper.safari.data.Volume;
 import edu.uw.zookeeper.safari.peer.protocol.MessagePacket;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedRequestMessage;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedResponseMessage;
 import edu.uw.zookeeper.safari.peer.protocol.PeerConnection.ClientPeerConnection;
 
 public class FrontendSessionExecutor extends ExecutedActor<FrontendSessionExecutor.FrontendRequestFuture> implements TaskExecutor<Message.ClientRequest<?>, Message.ServerResponse<?>>, Publisher {
     
     public static FrontendSessionExecutor newInstance(
             Session session,
             Publisher publisher,
             Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor,
             CachedFunction<ZNodeLabel.Path, Volume> volumeLookup,
             CachedFunction<Identifier, Identifier> assignmentLookup,
             Function<? super Identifier, Identifier> ensembleForPeer,
             CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup,
             Executor executor) {
         return new FrontendSessionExecutor(session, publisher, processor, volumeLookup, assignmentLookup, ensembleForPeer, connectionLookup, executor);
     }
     
     public static interface FrontendRequestFuture extends OperationFuture<Message.ServerResponse<?>> {}
     
     protected final Logger logger;
     protected final Executor executor;
     protected final LinkedQueue<FrontendRequestFuture> mailbox;
     protected final Publisher publisher;
     protected final Session session;
     protected final CachedFunction<ZNodeLabel.Path, Volume> volumes;
     protected final CachedFunction<Identifier, Identifier> assignments;
     protected final BackendLookup backends;
     protected final Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor;
     // not thread safe
     protected LinkedIterator<FrontendRequestFuture> finger;
 
     public FrontendSessionExecutor(
             Session session,
             Publisher publisher,
             Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor,
             CachedFunction<ZNodeLabel.Path, Volume> volumes,
             CachedFunction<Identifier, Identifier> assignments,
             Function<? super Identifier, Identifier> ensembleForPeer,
             CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup,
             Executor executor) {
         super();
         this.logger = LogManager.getLogger(getClass());
         this.executor = executor;
         this.mailbox = LinkedQueue.create();
         this.publisher = publisher;
         this.session = session;
         this.processor = processor;
         this.volumes = CachedFunction.create(
                 volumes.first(),
                 RunnableLookup.create(
                         volumes.second(), this, MoreExecutors.sameThreadExecutor()));
         this.assignments = CachedFunction.create(
                 assignments.first(),
                 RunnableLookup.create(
                         assignments.second(), this, MoreExecutors.sameThreadExecutor()));
         this.backends = new BackendLookup(ensembleForPeer, connectionLookup);
         this.finger = mailbox.iterator();
     }
     
     @Override
     protected Executor executor() {
         return executor;
     }
 
     @Override
     protected LinkedQueue<FrontendRequestFuture> mailbox() {
         return mailbox;
     }
 
     @Override
     protected Logger logger() {
         return logger;
     }
 
     @Override
     public void register(Object handler) {
         publisher.register(handler);
     }
 
     @Override
     public void unregister(Object handler) {
         publisher.unregister(handler);
     }
 
     @Override
     public void post(Object event) {
         if (event instanceof ShardedResponseMessage<?>) {
             Records.Response response = ((ShardedResponseMessage<?>) event).record();
             event = processor().apply(
                     Pair.create(session().id(),
                             Pair.create(Optional.<Operation.ProtocolRequest<?>>absent(), response)));
         }
         publisher.post(event);
     }
     
     public Session session() {
         return session;
     }
     
     protected CachedFunction<ZNodeLabel.Path, Volume> volumes() {
         return volumes;
     }
     
     protected CachedFunction<Identifier, Identifier> assignments() {
         return assignments;
     }
     
     protected BackendLookup backends() {
         return backends;
     }
     
     protected Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor() {
         return processor;
     }
 
     @Override
     public ListenableFuture<Message.ServerResponse<?>> submit(
             Message.ClientRequest<?> request) {
         Promise<Message.ServerResponse<?>> promise = 
                 LoggingPromise.create(logger, 
                         SettableFuturePromise.<Message.ServerResponse<?>>create());
         FrontendRequestFuture task; 
         if (request.xid() == OpCodeXid.PING.xid()) {
             task = new LocalRequestTask(OperationFuture.State.SUBMITTING, request, promise);
         } else {
             task = new BackendRequestTask(OperationFuture.State.WAITING, request, promise);
         }
         if (! send(task)) {
             task.cancel(true);
         }
         return task;
     }
     
     @Override
     public boolean send(FrontendRequestFuture message) {
         // short circuit pings
         if (message.xid() == OpCodeXid.PING.xid()) {
             try {
                 while (message.call() != OperationFuture.State.PUBLISHED) {}
             } catch (Exception e) {
                 throw Throwables.propagate(e);
             }
         } else {
             logger.trace("Submitting {}", message);
             if (! super.send(message)) {
                 return false;
             }
             message.addListener(this, MoreExecutors.sameThreadExecutor());
         }
         return true;
     }
 
     @Subscribe
     public void handleTransition(Pair<Identifier, Automaton.Transition<?>> event) {
         if (state() == State.TERMINATED) {
             return;
         }
         Identifier ensemble = backends.getEnsembleForPeer().apply(event.first());
         BackendSessionExecutor backend = backends.asCache().get(ensemble);
         if (backend == null) {
             return;
         }
         backend.handleTransition(event.second());
         
         if (backend.state().compareTo(State.TERMINATED) >= 0) {
             run();
         }
     }
 
     @Subscribe
     public void handleResponse(Pair<Identifier, ShardedResponseMessage<?>> message) {
         if (state() == State.TERMINATED) {
             return;
         }
         Identifier ensemble = backends.getEnsembleForPeer().apply(message.first());
         BackendSessionExecutor backend = backends.asCache().get(ensemble);
         if (backend == null) {
             return;
         }
         backend.handleResponse(message.second());
     }
 
     @Override
     protected void doRun() throws Exception {
         finger = mailbox.iterator();
         FrontendRequestFuture next;
         while ((next = finger.peekNext()) != null) {
             if (!apply(next) || (finger.peekNext() == next)) {
                 finger = null;
                 break;
             }
         }
     }
     
     @Override
     protected synchronized boolean apply(FrontendRequestFuture input) throws Exception {
         if (state() != State.TERMINATED) {
             for (;;) {
                 OperationFuture.State state = input.state();
                 if (OperationFuture.State.PUBLISHED == state) {
                     finger.next();
                     finger.remove();
                     break;
                 } else if (((OperationFuture.State.WAITING == state) || (OperationFuture.State.COMPLETE == state))
                        && (finger.hasPrevious() && (finger.peekPrevious().state().compareTo(state) <= 0))) {
                     // we need to preserve ordering of requests per volume
                     // as a proxy for this requirement, 
                     // don't submit until the task before us has submitted
                     // (note this is stronger than necessary)
                     // this also means that no tasks after this one can run!
                     // we also don't want to publish before our predecessor!
                     break;
                 } else if (input.call() == state) {
                     finger.next();
                     break;
                 }
             }
         }
         return (state() != State.TERMINATED);
     }
     
     @Override
     protected void runExit() {
         if (state.compareAndSet(State.RUNNING, State.WAITING)) {
             if ((finger != null) && finger.hasNext()) {
                 schedule();
             }
         }
     }
 
     protected synchronized void doStop() {
         FrontendRequestFuture next;
         while ((next = mailbox.poll()) != null) {
             next.cancel(true);
         }
         // FIXME stop backends
     }
 
     protected static class RunnableLookup<I,O> implements AsyncFunction<I,O> {
     
         public static <I,O> RunnableLookup<I,O> create(
                 AsyncFunction<I,O> delegate,
                 Runnable runnable,
                 Executor executor) {
             return new RunnableLookup<I,O>(delegate, runnable, executor);
         }
         
         protected final AsyncFunction<I,O> delegate;
         protected final Runnable runnable;
         protected final Executor executor;
         protected final Set<ListenableFuture<O>> futures;
         
         public RunnableLookup(
                 AsyncFunction<I,O> delegate,
                 Runnable runnable,
                 Executor executor) {
             this.delegate = delegate;
             this.runnable = runnable;
             this.executor = executor;
             this.futures = Sets.newHashSet();
         }
         
         public synchronized ListenableFuture<O> apply(I input) throws Exception {
             ListenableFuture<O> future = delegate.apply(input);
             if (!future.isDone() && !futures.contains(future)) {
                 new Listener(future);
             }
             return future;
         }
         
         protected synchronized boolean remove(ListenableFuture<O> future) {
             return futures.remove(future);
         }
         
         protected class Listener implements Runnable {
 
             protected final ListenableFuture<O> future;
             
             public Listener(ListenableFuture<O> future) {
                 this.future = future;
                 futures.add(future);
                 future.addListener(runnable, executor);
                 future.addListener(this, MoreExecutors.sameThreadExecutor());
             }
             
             @Override
             public void run() {
                 remove(future);
             }
         }
     }
 
     // TODO handle disconnects and reconnects
     protected class BackendLookup extends CachedLookup<Identifier, BackendSessionExecutor> {
     
         protected final CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup;
         protected final Function<? super Identifier, Identifier> ensembleForPeer;
         
         public BackendLookup(
                 Function<? super Identifier, Identifier> ensembleForPeer,
                 CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup) {
             this(ensembleForPeer, connectionLookup, 
                     new MapMaker().<Identifier, BackendSessionExecutor>makeMap());
         }
         
         public BackendLookup(
                 final Function<? super Identifier, Identifier> ensembleForPeer,
                 final CachedFunction<Identifier, ClientPeerConnection<Connection<? super MessagePacket>>> connectionLookup,
                 final ConcurrentMap<Identifier, BackendSessionExecutor> cache) {
             super(cache, CachedFunction.<Identifier, BackendSessionExecutor>create(
                         new Function<Identifier, BackendSessionExecutor>() {
                              @Override
                              public BackendSessionExecutor apply(Identifier ensemble) {
                                  BackendSessionExecutor value = cache.get(ensemble);
                                  if ((value != null) && (value.state().compareTo(Actor.State.TERMINATED) < 0)) {
                                      return value;
                                  } else {
                                      return null;
                                  }
                              }
                         },
                         RunnableLookup.create(SharedLookup.create(
                             new AsyncFunction<Identifier, BackendSessionExecutor>() {
                                 @Override
                                 public ListenableFuture<BackendSessionExecutor> apply(Identifier ensemble) throws Exception {
                                     final BackendSessionExecutor prev = cache.get(ensemble);
                                     Optional<Session> session = (prev == null) ? 
                                             Optional.<Session>absent() : 
                                                 Optional.of(prev.getSession());
                                     final EstablishBackendSessionTask task = new EstablishBackendSessionTask(
                                             session(),
                                             session,
                                             ensemble, 
                                             connectionLookup.apply(ensemble),
                                             SettableFuturePromise.<Session>create());
                                     final FrontendSessionExecutor self = FrontendSessionExecutor.this;
                                     return Futures.transform(
                                             task, 
                                             new Function<Session, BackendSessionExecutor>() {
                                                 @Override
                                                 @Nullable
                                                 public BackendSessionExecutor apply(Session input) {
                                                     BackendSessionExecutor backend = BackendSessionExecutor.create(
                                                             self.session().id(), 
                                                             task.task(),
                                                             input,
                                                             Futures.getUnchecked(task.connection()),
                                                             self,
                                                             MoreExecutors.sameThreadExecutor());
                                                     BackendSessionExecutor prev = cache.put(backend.getEnsemble(), backend);
                                                     if (prev != null) {
                                                         assert (prev.state() == Actor.State.TERMINATED);
                                                     }
                                                     return backend;
                                                 }
                                             });
                                 }
                             }), 
                             FrontendSessionExecutor.this, 
                             MoreExecutors.sameThreadExecutor())));
             this.ensembleForPeer = ensembleForPeer;
             this.connectionLookup = connectionLookup;
         }
         
         public Function<? super Identifier, Identifier> getEnsembleForPeer() {
             return ensembleForPeer;
         }
     }
 
     protected abstract class RequestTask extends PromiseTask<Message.ClientRequest<?>, Message.ServerResponse<?>> implements FrontendRequestFuture {
 
         protected final AtomicReference<State> state;
 
         public RequestTask(
                 State state,
                 Message.ClientRequest<?> task,
                 Promise<Message.ServerResponse<?>> delegate) {
             super(task, delegate);
             this.state = new AtomicReference<State>(state);
         }
         
         @Override
         public int xid() {
             return task().xid();
         }
 
         @Override
         public synchronized boolean cancel(boolean mayInterruptIfRunning) {
             boolean cancel = super.cancel(mayInterruptIfRunning);
             if (cancel) {
                 OperationFuture.State state = state();
                 if (OperationFuture.State.COMPLETE.compareTo(state) > 0) {
                     this.state.compareAndSet(state, OperationFuture.State.COMPLETE);
                 }
             }
             return cancel;
         }
         
         @Override
         public boolean set(Message.ServerResponse<?> result) {
             if ((result.record().opcode() != task().record().opcode())
                     && (! (result.record() instanceof Operation.Error))) {
                 throw new IllegalArgumentException(result.toString());
             }
             
             OperationFuture.State state = state();
             if (OperationFuture.State.COMPLETE.compareTo(state) > 0) {
                 this.state.compareAndSet(state, OperationFuture.State.COMPLETE);
             }
 
             return super.set(result);
         }
         
         @Override
         public boolean setException(Throwable t) {
             OperationFuture.State state = state();
             if (OperationFuture.State.COMPLETE.compareTo(state) > 0) {
                 this.state.compareAndSet(state, OperationFuture.State.COMPLETE);
             }
             
             if (t instanceof KeeperException) {
                 return complete(new IErrorResponse(((KeeperException) t).code()));
             } else {
                 return super.setException(t);
             }
         }
         
         @Override
         public OperationFuture.State state() {
             return state.get();
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this)
                     .add("state", state())
                     .add("task", task())
                     .add("future", delegate())
                     .toString();
         }
 
         protected ListenableFuture<Message.ServerResponse<?>> complete() throws InterruptedException, ExecutionException {
             if (state() == OperationFuture.State.SUBMITTING) {
                 Records.Response result = null;
                 switch (task().record().opcode()) {
                 case CLOSE_SESSION:
                 {
                     result = Records.newInstance(IDisconnectResponse.class);
                     break;
                 }
                 case PING:
                 {
                     result = PingProcessor.getInstance().apply(task().record());
                     break;
                 }
                 default:
                     throw new AssertionError(this);
                 }
                 if (result != null) {
                     complete(result);
                 }
             }
             return this;
         }
         
         protected synchronized boolean complete(Records.Response result) {
             if (isDone()) {
                 return false;
             }
             Message.ServerResponse<?> message = processor().apply(
                     Pair.create(session().id(),
                             Pair.create(Optional.<Operation.ProtocolRequest<?>>of(task()), result)));
             return set(message);
         }
 
         protected boolean publish() {
             if (this.state.compareAndSet(OperationFuture.State.COMPLETE, OperationFuture.State.PUBLISHED)) {
                 // TODO: exception?
                 post(Futures.getUnchecked(this));
                 return true;
             } else {
                 return false;
             }
         }
     }
     
     protected class LocalRequestTask extends RequestTask {
 
         public LocalRequestTask(
                 State state,
                 Message.ClientRequest<?> task,
                 Promise<Message.ServerResponse<?>> delegate) {
             super(state, task, delegate);
         }
         
         @Override
         public synchronized State call() throws Exception {
             State state = state();
             switch (state) {
             case WAITING:
                 this.state.compareAndSet(state, OperationFuture.State.SUBMITTING);
                 break;
             case SUBMITTING:
                 complete();
                 break;
             case COMPLETE:
                 publish();
                 break;
             default:
                 break;
             }
             
             return state();
         }
     }
 
     protected static final ImmutableMap<Volume, Set<BackendSessionExecutor.BackendRequestFuture>> EMPTY_RESPONSES = 
             ImmutableMap.of(Volume.none(), (Set<BackendSessionExecutor.BackendRequestFuture>) ImmutableSet.<BackendSessionExecutor.BackendRequestFuture>of());
     
     protected class BackendRequestTask extends RequestTask {
 
         protected final ImmutableSet<ZNodeLabel.Path> paths;
         protected final Map<ZNodeLabel.Path, Volume> volumes;
         protected final Map<Volume, ShardedRequestMessage<?>> shards;
         protected final Map<Volume, Set<BackendSessionExecutor.BackendRequestFuture>> submitted;
         protected final Set<ListenableFuture<?>> pending;
 
         public BackendRequestTask(
                 State state,
                 Message.ClientRequest<?> task,
                 Promise<Message.ServerResponse<?>> delegate) {
             super(state, task, delegate);
             this.paths = ImmutableSet.copyOf(PathsOfRequest.getPathsOfRequest(task.record()));
             this.volumes = Maps.<ZNodeLabel.Path, Volume>newHashMap();
             this.shards = Maps.<Volume, ShardedRequestMessage<?>>newHashMap();
             this.submitted = Maps.<Volume, Set<BackendSessionExecutor.BackendRequestFuture>>newHashMap();
             this.pending = Sets.<ListenableFuture<?>>newHashSet();
         }
         
         @Override
         public int xid() {
             return task().xid();
         }
 
         @Override
         public boolean cancel(boolean mayInterruptIfRunning) {
             boolean cancel = super.cancel(mayInterruptIfRunning);
             if (cancel) {
                 for (Set<BackendSessionExecutor.BackendRequestFuture> backends: submitted.values()) {
                     for (BackendSessionExecutor.BackendRequestFuture e: backends) {
                         e.cancel(mayInterruptIfRunning);
                     }
                 }
                 pending.clear();
             }
             return cancel;
         }
 
         @Override
         public boolean set(Message.ServerResponse<?> result) {
             boolean set = super.set(result);
             if (set) {
                 for (Set<BackendSessionExecutor.BackendRequestFuture> backends: submitted.values()) {
                     for (BackendSessionExecutor.BackendRequestFuture e: backends) {
                         e.cancel(true);
                     }
                 }
                 pending.clear();
             }
             return set;
         }
         
         @Override
         public boolean setException(Throwable t) {
             boolean setException = super.setException(t);
             if (setException) {
                 for (Set<BackendSessionExecutor.BackendRequestFuture> backends: submitted.values()) {
                     for (BackendSessionExecutor.BackendRequestFuture e: backends) {
                         e.cancel(true);
                     }
                 }
                 pending.clear();
             }
             return setException;
         }
         
         @Override
         public synchronized State call() throws Exception {
             logger.entry(this);
             
             Iterator<ListenableFuture<?>> p = pending.iterator();
             while (p.hasNext()) {
                 ListenableFuture<?> next = p.next();
                 if (next.isDone()) {
                     p.remove();
                 }
             }
             if (pending.isEmpty()) {
                 switch (state()) {
                 case WAITING:
                 {
                     submit();
                     break;
                 }
                 case SUBMITTING:
                 {
                     complete();
                     break;
                 }
                 case COMPLETE:
                 {
                     publish();
                     break;
                 }
                 default:
                     break;
                 }
             }
                         
             return logger.exit(state());
         }
 
         protected Optional<Map<ZNodeLabel.Path, Volume>> getVolumes(Set<ZNodeLabel.Path> paths) throws Exception {
             Sets.SetView<ZNodeLabel.Path> difference = Sets.difference(paths, volumes.keySet());
             for (ZNodeLabel.Path path: difference) {
                 ListenableFuture<Volume> v = volumes().apply(path);
                 if (v.isDone()) {
                     volumes.put(path, v.get());
                 } else {
                     pending.add(v);
                 }
             }
             if (difference.isEmpty()) {
                 return Optional.of(volumes);
             } else {
                 if (logger.isTraceEnabled()) {
                     logger.trace("Waiting for path lookups {}", difference);
                 }
                 return Optional.<Map<ZNodeLabel.Path, Volume>>absent();
             }
         }
         
         protected ImmutableSet<Volume> getUniqueVolumes(Collection<Volume> volumes) {
             return volumes.isEmpty()
                         ? ImmutableSet.of(Volume.none())
                         : ImmutableSet.copyOf(volumes);
         }
         
         protected Map<Volume, ShardedRequestMessage<?>> getShards(Map<ZNodeLabel.Path, Volume> volumes) throws KeeperException {
             ImmutableSet<Volume> uniqueVolumes = getUniqueVolumes(volumes.values());
             Sets.SetView<Volume> difference = Sets.difference(uniqueVolumes, shards.keySet());
             if (! difference.isEmpty()) {
                 if ((OpCode.MULTI == task().record().opcode())
                         && ! ImmutableSet.of(Volume.none()).equals(uniqueVolumes)) {
                     Map<Volume, List<Records.MultiOpRequest>> byShardOps = Maps.newHashMapWithExpectedSize(difference.size());
                     for (Records.MultiOpRequest op: (IMultiRequest) task().record()) {
                         ZNodeLabel.Path[] paths = PathsOfRequest.getPathsOfRequest(op);
                         assert (paths.length > 0);
                         for (ZNodeLabel.Path path: paths) {
                             Volume v = volumes.get(path);
                             if (! difference.contains(v)) {
                                 continue;
                             }
                             List<Records.MultiOpRequest> ops;
                             if (byShardOps.containsKey(v)) {
                                 ops = byShardOps.get(v);
                             } else {
                                 ops = Lists.newLinkedList();
                                 byShardOps.put(v, ops);
                             }
                             ops.add(validate(v, op));
                         }
                     }
                     for (Map.Entry<Volume, List<Records.MultiOpRequest>> e: byShardOps.entrySet()) {
                         shards.put(e.getKey(), 
                                 ShardedRequestMessage.of(
                                         e.getKey().getId(),
                                         ProtocolRequestMessage.of(
                                                 task().xid(),
                                                 new IMultiRequest(e.getValue()))));
                     }
                 } else {
                     for (Volume v: difference) {
                         validate(v, task().record());
                         shards.put(v, ShardedRequestMessage.of(v.getId(), task()));
                     }
                 }
             }
             assert (difference.isEmpty());
             return shards;
         }
         
         protected <T extends Records.Request> T validate(Volume volume, T request) throws KeeperException {
             switch (request.opcode()) {
             case CREATE:
             case CREATE2:
             {
                 // special case: root of a volume can't be sequential!
                 Records.CreateModeGetter create = (Records.CreateModeGetter) request;
                 if (CreateMode.valueOf(create.getFlags()).contains(CreateFlag.SEQUENTIAL)
                         && volume.getDescriptor().getRoot().toString().equals(create.getPath())) {
                     // fail
                     throw new KeeperException.BadArgumentsException(create.getPath());
                 }
             }
             default:
                 break;
             }
             return request;
         }
 
         protected Optional<Map<Volume, Set<BackendSessionExecutor>>> getConnections(Set<Volume> volumes) throws Exception {
             Map<Volume, Set<BackendSessionExecutor>> backends = Maps.newHashMapWithExpectedSize(volumes.size());
             for (Volume v: volumes) {
                 if (v.equals(Volume.none())) {
                     boolean done = true;
                     Set<BackendSessionExecutor> all = Sets.newHashSetWithExpectedSize(backends().asCache().size());
                     for (Identifier ensemble: backends().asCache().keySet()) {
                         ListenableFuture<BackendSessionExecutor> backend = backends().asLookup().apply(ensemble);
                         if (backend.isDone()) {
                             all.add(backend.get());
                         } else {
                             if (logger.isTraceEnabled()) {
                                 logger.trace("Waiting for backend {}", ensemble);
                             }
                             pending.add(backend);
                             done = false;
                         }
                     }
                     if (done) {
                         backends.put(v, all);
                     }
                 } else {
                     ListenableFuture<Identifier> assignment = assignments().apply(v.getId());
                     if (assignment.isDone()) {
                         Identifier ensemble = assignment.get();
                         ListenableFuture<BackendSessionExecutor> backend = backends().asLookup().apply(ensemble);
                         if (backend.isDone()) {
                             backends.put(v, ImmutableSet.of(backend.get()));
                         } else {
                             if (logger.isTraceEnabled()) {
                                 logger.trace("Waiting for backend {}", ensemble);
                             }
                             pending.add(backend);
                         }
                     } else {
                         if (logger.isTraceEnabled()) {
                             logger.trace("Waiting for assignment {}", v.getId());
                         }
                         pending.add(assignment);
                     }
                 }
             }
             Sets.SetView<Volume> difference = Sets.difference(volumes, backends.keySet());
             if (difference.isEmpty()) {
                 return Optional.of(backends);
             } else {
                 if (logger.isTraceEnabled()) {
                     logger.trace("Waiting for volume connections {}", difference);
                 }
                 return Optional.absent();
             }
         }
         
         protected Optional<Map<Volume, Set<BackendSessionExecutor.BackendRequestFuture>>> submit() throws Exception {
             Optional<Map<ZNodeLabel.Path, Volume>> volumes = getVolumes(paths);
             if (! volumes.isPresent()) {
                 return Optional.absent();
             }
             Set<Volume> uniqueVolumes = getUniqueVolumes(volumes.get().values());
             Optional<Map<Volume, Set<BackendSessionExecutor>>> backends = getConnections(uniqueVolumes);
             if (! backends.isPresent()) {
                 return Optional.absent();
             }
             Map<Volume, ShardedRequestMessage<?>> shards;
             try {
                 shards = getShards(volumes.get());
             } catch (KeeperException e) {
                 // fail
                 complete(new IErrorResponse(e.code()));
                 return Optional.absent();
             }
             return Optional.of(submit(shards, backends.get()));
         }
         
         protected Map<Volume, Set<BackendSessionExecutor.BackendRequestFuture>> submit(
                 Map<Volume, ShardedRequestMessage<?>> shards,
                 Map<Volume, Set<BackendSessionExecutor>> backends) {
             this.state.compareAndSet(OperationFuture.State.WAITING, OperationFuture.State.SUBMITTING);
             for (Map.Entry<Volume, ShardedRequestMessage<?>> shard: shards.entrySet()) {
                 Volume k = shard.getKey();
                 Set<BackendSessionExecutor> volumeBackends = backends.get(k);
                 Set<BackendSessionExecutor.BackendRequestFuture> requests = submitted.get(k);
                 if (requests == null) {
                     requests = Sets.newHashSetWithExpectedSize(volumeBackends.size());
                     submitted.put(k, requests);
                 }
                 for (BackendSessionExecutor backend: volumeBackends) {
                     boolean submitted = false;
                     for (BackendSessionExecutor.BackendRequestFuture e: requests) {
                         if (e.executor() == backend) {
                             submitted = true;
                             break;
                         }
                     }
                     if (submitted) {
                         continue;
                     }
                     BackendSessionExecutor.BackendRequestFuture task = backend.submit(
                                 Pair.<OperationFuture<?>, ShardedRequestMessage<?>>create(this, shard.getValue()));
                     requests.add(task);
                     pending.add(task);
                     task.addListener(FrontendSessionExecutor.this, executor);
                 }
             }
             return submitted;
         }
         
         @Override
         protected ListenableFuture<Message.ServerResponse<?>> complete() throws InterruptedException, ExecutionException {
             if (state.get() != OperationFuture.State.SUBMITTING) {
                 return this;
             }
             
             if (! Sets.difference(shards.keySet(), submitted.keySet()).isEmpty()) {
                 return this;
             }
 
             if (submitted.equals(EMPTY_RESPONSES)) {
                 return super.complete();
             }
 
             Records.Response result = null;
             if (OpCode.MULTI == task().record().opcode()) {
                 Map<Volume, ListIterator<Records.MultiOpResponse>> responses = Maps.newHashMapWithExpectedSize(shards.size());
                 for (Map.Entry<Volume, Set<BackendSessionExecutor.BackendRequestFuture>> e: submitted.entrySet()) {
                     BackendSessionExecutor.BackendRequestFuture request = Iterables.getOnlyElement(e.getValue());
                     if (! request.isDone()) {
                         return this;
                     } else {
                         responses.put(
                                 e.getKey(), 
                                 ((IMultiResponse) request.get().record()).listIterator());
                     }
                 }
                 if (Sets.difference(shards.keySet(), responses.keySet()).isEmpty()) {
                     Map<Volume, ListIterator<Records.MultiOpRequest>> requests = Maps.newHashMapWithExpectedSize(shards.size());
                     for (Map.Entry<Volume, ShardedRequestMessage<?>> e: shards.entrySet()) {
                         requests.put(
                                 e.getKey(), 
                                 ((IMultiRequest) e.getValue().record()).listIterator());
                     }
                     IMultiRequest multi = (IMultiRequest) task().record();
                     List<Records.MultiOpResponse> ops = Lists.newArrayListWithCapacity(multi.size());
                     for (Records.MultiOpRequest op: multi) {
                         Pair<Volume, Records.MultiOpResponse> response = null;
                         for (Map.Entry<Volume, ListIterator<Records.MultiOpRequest>> request: requests.entrySet()) {
                             if (! request.getValue().hasNext()) {
                                 continue;
                             }
                             if (! op.equals(request.getValue().next())) {
                                 request.getValue().previous();
                                 continue;
                             }
 
                             Volume v = request.getKey();
                             if ((response == null) 
                                     || (response.first().getDescriptor().getRoot().prefixOf(
                                             v.getDescriptor().getRoot()))) {
                                 response = Pair.create(v, responses.get(v).next());
                             }
                         }
                         assert (response != null);
                         ops.add(response.second());
                     }
                     result = new IMultiResponse(ops);
                 }
             } else {
                 Pair<Volume, BackendSessionExecutor.BackendRequestFuture> selected = null;
                 for (Map.Entry<Volume, Set<BackendSessionExecutor.BackendRequestFuture>> requests: submitted.entrySet()) {
                     Volume v = requests.getKey();
                     if (requests.getValue().isEmpty()) {
                         return this;
                     }
                     for (BackendSessionExecutor.BackendRequestFuture request: requests.getValue()) {
                         if (! request.isDone()) {
                             return this;
                         } else {
                             ShardedResponseMessage<?> response;
                             try {
                                 response = request.get();
                             } catch (ExecutionException e) {
                                 setException(e.getCause());
                                 return this;
                             }
                             if (selected == null) {
                                selected = Pair.create(v, request);
                             } else {
                                if ((selected.second().get().record() instanceof Operation.Error) || (response.record() instanceof Operation.Error)) {
                                    if (task().record().opcode() != OpCode.CLOSE_SESSION) {
                                        throw new UnsupportedOperationException();
                                    }
                                } else {
                                    // we should only get here for create, create2, and delete
                                    // and only if the path is for a volume root
                                    // in that case, pick the response that came from the volume root
                                    if (selected.first().getDescriptor().getRoot().prefixOf(
                                            v.getDescriptor().getRoot())) {
                                        selected = Pair.create(v, request);
                                    }
                                }
                            }
                         }
                     }
                 }
                 assert (selected != null);
                 result = selected.second().get().record();
             }
             assert (result != null);
             super.complete(result);
             return this;
         }
         
         @Override
         protected boolean publish() {
             boolean published = super.publish();
             if (published) {
                 for (Set<BackendSessionExecutor.BackendRequestFuture> e: submitted.values()) {
                     for (BackendSessionExecutor.BackendRequestFuture request: e) {
                         request.executor().run();
                     }
                 }
             }
             return published;
         }
 
         @Override
         public synchronized String toString() {
             return Objects.toStringHelper(this)
                     .add("state", state())
                     .add("task", task())
                     .add("future", delegate())
                     .add("volumes", volumes)
                     .add("shards", shards)
                     .add("submitted", submitted)
                     .add("pending", pending)
                     .toString();
         }
     }
 }
