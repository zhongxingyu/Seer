 package edu.uw.zookeeper.safari.frontend;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executor;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.ScheduledExecutorService;
 
 import net.engio.mbassy.common.IConcurrentSet;
 import net.engio.mbassy.common.StrongConcurrentSet;
 
 import org.apache.zookeeper.KeeperException;
 import org.apache.zookeeper.Watcher;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Optional;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.MapMaker;
 import com.google.common.collect.Maps;
 import com.google.common.util.concurrent.AsyncFunction;
 import com.google.common.util.concurrent.ForwardingListenableFuture;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.MoreExecutors;
 import com.google.inject.Provider;
 
 import edu.uw.zookeeper.protocol.Session;
 import edu.uw.zookeeper.common.Automaton;
 import edu.uw.zookeeper.common.Automatons;
 import edu.uw.zookeeper.common.Actors.ExecutedPeekingQueuedActor;
 import edu.uw.zookeeper.common.ForwardingPromise;
 import edu.uw.zookeeper.common.LoggingPromise;
 import edu.uw.zookeeper.common.Pair;
 import edu.uw.zookeeper.common.ParameterizedFactory;
 import edu.uw.zookeeper.common.Processors;
 import edu.uw.zookeeper.common.Promise;
 import edu.uw.zookeeper.common.PromiseTask;
 import edu.uw.zookeeper.common.SettableFuturePromise;
 import edu.uw.zookeeper.data.CreateFlag;
 import edu.uw.zookeeper.data.CreateMode;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.NotificationListener;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.ProtocolResponseMessage;
 import edu.uw.zookeeper.protocol.ProtocolState;
 import edu.uw.zookeeper.protocol.SessionListener;
 import edu.uw.zookeeper.protocol.proto.IErrorResponse;
 import edu.uw.zookeeper.protocol.proto.IGetChildren2Response;
 import edu.uw.zookeeper.protocol.proto.IGetChildrenResponse;
 import edu.uw.zookeeper.protocol.proto.IMultiRequest;
 import edu.uw.zookeeper.protocol.proto.IPingResponse;
 import edu.uw.zookeeper.protocol.proto.IWatcherEvent;
 import edu.uw.zookeeper.protocol.proto.Records;
 import edu.uw.zookeeper.safari.Identifier;
 import edu.uw.zookeeper.safari.common.CachedFunction;
 import edu.uw.zookeeper.safari.data.Volume;
 import edu.uw.zookeeper.safari.frontend.ClientPeerConnectionDispatchers.ClientPeerConnectionDispatcher;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedRequestMessage;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedResponseMessage;
 import edu.uw.zookeeper.server.AbstractSessionExecutor;
 
 public class FrontendSessionExecutor extends AbstractSessionExecutor<ShardedResponseMessage<IWatcherEvent>> implements Processors.UncheckedProcessor<Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>, Message.ServerResponse<?>> {
     
     public static ParameterizedFactory<Session, FrontendSessionExecutor> factory(
              final Provider<? extends Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>>> processor,
              final CachedFunction<ZNodeLabel.Path, Volume> volumeLookup,
              final CachedFunction<Identifier, Identifier> assignmentLookup,
              final AsyncFunction<Identifier, ClientPeerConnectionDispatcher> dispatchers,
              final ScheduledExecutorService scheduler,
              final Executor executor) {
         return new ParameterizedFactory<Session, FrontendSessionExecutor>() {
             @Override
             public FrontendSessionExecutor get(Session value) {
                 return newInstance(
                         value, 
                         processor.get(),
                         volumeLookup,
                         assignmentLookup,
                         dispatchers,
                         scheduler,
                         executor);
             }
         };
     }
     
     public static FrontendSessionExecutor newInstance(
             Session session,
             Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor,
             CachedFunction<ZNodeLabel.Path, Volume> volumeLookup,
             CachedFunction<Identifier, Identifier> assignmentLookup,
             AsyncFunction<Identifier, ClientPeerConnectionDispatcher> dispatchers,
             ScheduledExecutorService scheduler,
             Executor executor) {
         Automaton<ProtocolState,ProtocolState> state = Automatons.createSynchronized(
                 Automatons.createSimple(
                         ProtocolState.CONNECTED));
         IConcurrentSet<SessionListener> listeners = new StrongConcurrentSet<SessionListener>();
         return new FrontendSessionExecutor(processor, volumeLookup, assignmentLookup, dispatchers, executor, session, state, listeners, scheduler);
     }
     
     public static <T extends Records.Request> T validate(Volume volume, T request) throws KeeperException {
         switch (request.opcode()) {
         case CREATE:
         case CREATE2:
         {
             // special case: root of a volume can't be sequential or ephemeral
             Records.CreateModeGetter create = (Records.CreateModeGetter) request;
             CreateMode mode = CreateMode.valueOf(create.getFlags());
             if (volume.getDescriptor().getRoot().toString().equals(create.getPath())
                     && (mode.contains(CreateFlag.SEQUENTIAL) || mode.contains(CreateFlag.EPHEMERAL))) {
                 throw new KeeperException.BadArgumentsException(create.getPath());
             }
             break;
         }
         case DELETE:
         {
             // special case: can't delete the parent of a volume
             ZNodeLabel.Path root = volume.getDescriptor().getRoot();
             ZNodeLabel.Path path = ZNodeLabel.Path.of(((Records.PathGetter) request).getPath());
            ZNodeLabel suffix = path.suffix(root.length());
             for (ZNodeLabel leaf: volume.getDescriptor().getLeaves()) {
                 if (leaf.toString().startsWith(suffix.toString())) {
                     int lastSlash = leaf.toString().lastIndexOf(ZNodeLabel.SLASH);
                     if ((lastSlash == -1) || (lastSlash == suffix.length())) {
                         throw new KeeperException.BadArgumentsException(path.toString());
                     }
                 }
             }
             break;
         }
         case MULTI:
         {
             // multi only supported if all operations are on the same volume
             for (Records.MultiOpRequest op: (IMultiRequest) request) {
                 ZNodeLabel.Path path = ZNodeLabel.Path.of(op.getPath());
                 if (! volume.getDescriptor().contains(path)) {
                     throw new KeeperException.BadArgumentsException(path.toString());
                 }
                 validate(volume, op);
             }
             break;
         }
         default:
             break;
         }
         return request;
     }
 
     protected static final Executor SAME_THREAD_EXECUTOR = MoreExecutors.sameThreadExecutor();
     
     protected final Logger logger;
     protected final ClientPeerConnectionExecutorsListener backends;
     protected final ShardingProcessor sharder;
     protected final SubmitProcessor submitter;
     protected final Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor;
     protected final FrontendSessionActor actor;
     protected final NotificationProcessor notifications;
 
     public FrontendSessionExecutor(
             Processors.UncheckedProcessor<Pair<Long, Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response>>, Message.ServerResponse<?>> processor,
             CachedFunction<ZNodeLabel.Path, Volume> volumes,
             CachedFunction<Identifier, Identifier> assignments,
             AsyncFunction<Identifier, ClientPeerConnectionDispatcher> dispatchers,
             Executor executor,
             Session session,
             Automaton<ProtocolState,ProtocolState> state,
             IConcurrentSet<SessionListener> listeners,
             ScheduledExecutorService scheduler) {
         super(session, state, listeners, scheduler);
         this.logger = LogManager.getLogger(getClass());
         this.processor = processor;
         this.backends = ClientPeerConnectionExecutorsListener.newInstance(this, dispatchers, executor);
         this.actor = new FrontendSessionActor(executor);
         this.sharder = new ShardingProcessor(volumes, executor);
         this.submitter = new SubmitProcessor(assignments, executor);
         this.notifications = new NotificationProcessor();
     }
     
     public NotificationProcessor notifications() {
         return notifications;
     }
     
     @Override
     public ListenableFuture<Message.ServerResponse<?>> submit(
             Message.ClientRequest<?> request) {
         timer.send(request);
         FrontendRequestTask task;
         switch (request.record().opcode()) {
         case PING:
         {
             // short circuit pings to keep connection alive
             return Futures.<Message.ServerResponse<?>>immediateFuture(
                     apply(Pair.create(Optional.<Operation.ProtocolRequest<?>>of(request), 
                                     (Records.Response) Records.newInstance(IPingResponse.class))));
         }
         case AUTH:
         case SET_WATCHES:
         {
             // unimplemented
             throw new RejectedExecutionException(new KeeperException.UnimplementedException());
         }
         case CLOSE_SESSION:
         {
             task = new FrontendDisconnectTask(
                     request,
                     LoggingPromise.create(
                             logger, 
                             SettableFuturePromise.<Message.ServerResponse<?>>create()));
             break;
         }
         default: 
         {
             task = new FrontendVolumeRequestTask(
                     request, 
                     LoggingPromise.create(
                             logger, 
                             SettableFuturePromise.<Message.ServerResponse<?>>create()));
             break;
         }
         }
         if (! actor.send(task)) {
             task.cancel(true);
         }
         return task;
     }
 
     @Override
     public Message.ServerResponse<?> apply(Pair<Optional<Operation.ProtocolRequest<?>>, Records.Response> input) {
         return processor.apply(Pair.create(session.id(), input));
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void onSuccess(ShardedResponseMessage<IWatcherEvent> result) {
         handleNotification((Message.ServerResponse<IWatcherEvent>) apply(
                 Pair.create( 
                         Optional.<Operation.ProtocolRequest<?>>absent(),
                         (Records.Response) result.record()))); 
     }
     
     protected abstract class FrontendRequestTask extends PromiseTask<Message.ClientRequest<?>, Message.ServerResponse<?>> implements Runnable {
 
         public FrontendRequestTask(
                 Message.ClientRequest<?> task,
                 Promise<Message.ServerResponse<?>> promise) {
             super(task, promise);
         }
         
         public abstract ListenableFuture<Records.Response> response();
 
         @Override
         public void run() {
             if (response().isDone()) {
                 if (this == actor.mailbox().peek()) {
                     actor.run();
                 }
             }
         }
     }
 
     protected class FrontendVolumeRequestTask extends FrontendRequestTask 
             implements Runnable {
     
         protected final VolumeTask volume;
         protected final ResponseTask response;
         
         public FrontendVolumeRequestTask(
                 Message.ClientRequest<?> task,
                 Promise<Message.ServerResponse<?>> promise) {
             super(task, promise);
             this.volume = new VolumeTask();
             this.response = new ResponseTask();
             
             addListener(this, SAME_THREAD_EXECUTOR);
         }
         
         public VolumeTask volume() {
             return volume;
         }
         
         public ResponseTask response() {
             return response;
         }
     
         @Override
         public void run() {
             if (isCancelled()) {
                 volume.cancel(false);
                 response.cancel(false);
             }
             super.run();
         }
     
         @Override
         protected synchronized Objects.ToStringHelper toString(Objects.ToStringHelper toString) {
             return super.toString(toString).add("volume", volume).add("response", response);
         }
         
         protected class VolumeTask extends ForwardingListenableFuture<Volume> implements AsyncFunction<CachedFunction<ZNodeLabel.Path, Volume>, Volume> {
     
             protected ListenableFuture<Volume> future;
             
             public VolumeTask() {
                 this.future = null;
             }
 
             @Override
             public synchronized boolean cancel(boolean mayInterruptIfRunning) {
                 if (future == null) {
                     return false;
                 } else {
                     return future.cancel(mayInterruptIfRunning);
                 }
             }
             
             @Override
             public synchronized boolean isDone() {
                 return (future != null) && future.isDone();
             }
             
             @Override
             public synchronized ListenableFuture<Volume> apply(CachedFunction<ZNodeLabel.Path, Volume> volumes) {
                 if (! isDone()) {
                     if (future == null) {
                         try {
                             switch (task.record().opcode()) {
                             case CREATE:
                             case CREATE2:
                             case DELETE:
                             case CHECK:
                             case EXISTS:
                             case GET_ACL:
                             case GET_CHILDREN:
                             case GET_CHILDREN2:
                             case GET_DATA:
                             case SET_ACL:
                             case SET_DATA:
                             case SYNC:
                             {
                                 ZNodeLabel.Path path = ZNodeLabel.Path.of(((Records.PathGetter) task.record()).getPath());
                                 future = volumes.apply(path);
                                 break;
                             }
                             case MULTI:
                             {
                                 // we're going to assume that all ops are on the same volume
                                 // so just pick the first path to lookup
                                 // (we will validate this request later)
                                 ZNodeLabel.Path path = null;
                                 for (Records.MultiOpRequest e: (IMultiRequest) task.record()) {
                                     path = ZNodeLabel.Path.of(e.getPath());
                                     break;
                                 }
                                 // empty multi is legal I guess?
                                 if (path == null) {
                                     future = Futures.immediateFuture(Volume.none());
                                 } else {
                                     future = volumes.apply(path);
                                 }
                                 break;
                             }
                             default:
                                 throw new AssertionError(String.valueOf(task));
                             }
                         } catch (Exception e) {
                             future = Futures.immediateFailedFuture(e);
                         }
                     }
                 }
                 return this;
             }
     
             @Override
             public synchronized String toString() {
                 Objects.ToStringHelper toString = Objects.toStringHelper(this);
                 if (isDone()) {
                     try {
                         toString.addValue(get());
                     } catch (Exception e) {
                         toString.addValue(e);
                     }
                 }
                 return toString.toString();
             }
             
             @Override
             protected ListenableFuture<Volume> delegate() {
                 return future;
             }
         }
         
         protected class ResponseTask extends ForwardingPromise<Records.Response> implements Runnable, AsyncFunction<ClientPeerConnectionExecutor, Records.Response> {
 
             protected final Promise<Records.Response> promise;
             protected ListenableFuture<ShardedResponseMessage<?>> future;
 
             public ResponseTask() {
                 this(LoggingPromise.create(logger, SettableFuturePromise.<Records.Response>create()));
             }
             
             public ResponseTask(Promise<Records.Response> promise) {
                 this.promise = promise;
                 this.future = null;
 
                 addListener(this, SAME_THREAD_EXECUTOR);
                 addListener(FrontendVolumeRequestTask.this, SAME_THREAD_EXECUTOR);
             }
             
             @Override
             public synchronized ListenableFuture<Records.Response> apply(ClientPeerConnectionExecutor connection) {
                 if (! isDone()) {
                     if (future == null) {
                         try {
                             Volume volume = volume().get();
                             try {
                                 validate(volume, task.record());
                                 future = connection.submit(ShardedRequestMessage.of(volume.getId(), task));
                                 future.addListener(this, SAME_THREAD_EXECUTOR);
                             } catch (KeeperException e) {
                                 set(new IErrorResponse(e.code()));
                             }
                         } catch (Exception e) {
                             setException(e);
                         }
                     }
                 }
                 return this;
             }
             
             @Override
             public synchronized void run() {
                 if (! isDone()) {
                     if ((future != null) && future.isDone()) {
                         if (future.isCancelled()) {
                             cancel(false);
                         } else {
                             // we won't see a notification until we see the response
                             // for setting a notification
                             notifications.handleRequest(task.record());
                             try {
                                 ShardedResponseMessage<?> response = future.get();
                                 Records.Response result;
                                 switch (response.record().opcode()) {
                                 case GET_CHILDREN:
                                 case GET_CHILDREN2:
                                 {
                                     Volume volume = volume().get();
                                     // special case: volume children won't be in the response
                                     ZNodeLabel.Path root = volume.getDescriptor().getRoot();
                                    ZNodeLabel.Path path = ZNodeLabel.Path.of(((Records.PathGetter) task.record()).getPath());
                                    ZNodeLabel suffix = path.suffix(root.length());
                                     List<String> children = null;
                                     for (ZNodeLabel leaf: volume.getDescriptor().getLeaves()) {
                                         if (leaf.toString().startsWith(suffix.toString())) {
                                             int lastSlash = leaf.toString().lastIndexOf(ZNodeLabel.SLASH);
                                             if ((lastSlash == -1) || (lastSlash == suffix.length())) {
                                                 if (children == null) {
                                                     List<String> volumeChildren = ((Records.ChildrenGetter) response.record()).getChildren();
                                                     children = Lists.newArrayListWithCapacity(volumeChildren.size() + volume.getDescriptor().getLeaves().size());
                                                     children.addAll(volumeChildren);
                                                 }
                                                 children.add((lastSlash == -1) ? leaf.toString() : leaf.toString().substring(suffix.length()));
                                             }
                                         }
                                     }
                                     if (children == null) {
                                         result = response.record();
                                     } else {
                                         if (response.record() instanceof Records.StatGetter) {
                                             result = new IGetChildren2Response(children, ((Records.StatGetter) response.record()).getStat());
                                         } else {
                                             result = new IGetChildrenResponse(children);
                                         }
                                     }
                                     break;
                                 }
                                 default:
                                 {
                                     result = response.record();
                                     break;
                                 }
                                 }
                                 set(result);
                             } catch (InterruptedException e) {
                                 throw new AssertionError(e);
                             } catch (ExecutionException e) {
                                 setException(e.getCause());
                             }
                         }
                     }
                 } else {
                     if (isCancelled()) {
                         if (future != null) {
                             future.cancel(false);
                         }
                     }
                 }
             }
             
             @Override
             protected Promise<Records.Response> delegate() {
                 return promise;
             }
         }
     }
 
     protected class FrontendDisconnectTask extends FrontendRequestTask 
             implements Runnable {
     
         protected final FlushTask flush;
         protected final ResponseTask response;
 
         public FrontendDisconnectTask(
                 Message.ClientRequest<?> task,
                 Promise<Message.ServerResponse<?>> promise) {
             super(task, promise);
             this.flush = new FlushTask();
             this.response = new ResponseTask();
             
             addListener(this, SAME_THREAD_EXECUTOR);
         }
         
         public FlushTask flush() {
             return flush;
         }
         
         @Override
         public ResponseTask response() {
             return response;
         }
     
         @Override
         public void run() {
             if (isCancelled()) {
                 flush.cancel(false);
                 response.cancel(false);
             }
             
             super.run();
         }
         
         protected class FlushTask extends ForwardingPromise<List<Volume>> implements Runnable, AsyncFunction<Map<Volume, SubmitProcessor.VolumeProcessor>, List<Volume>> {
 
             protected final Promise<List<Volume>> promise;
             protected List<ListenableFuture<Volume>> futures;
             
             public FlushTask() {
                 this(LoggingPromise.create(logger, SettableFuturePromise.<List<Volume>>create()));
             }
 
             public FlushTask(Promise<List<Volume>> promise) {
                 this.promise = promise;
                 this.futures = null;
                 
                 addListener(this, SAME_THREAD_EXECUTOR);
             }
 
             @Override
             public synchronized ListenableFuture<List<Volume>> apply(Map<Volume, SubmitProcessor.VolumeProcessor> volumes) {
                 if (! isDone()) {
                     if (futures == null) {
                         futures = Lists.newArrayListWithCapacity(volumes.size());
                         for (SubmitProcessor.VolumeProcessor p: volumes.values()) {
                             futures.add(p.flush());
                         }
                         for (ListenableFuture<Volume> future: futures) {
                             future.addListener(this, SAME_THREAD_EXECUTOR);
                         }
                     }
                 }
                 return this;
             }
             
             @Override
             public synchronized void run() {
                 if (! isDone()) {
                     if (futures != null) {
                         for (ListenableFuture<Volume> future: futures) {
                             if (! future.isDone()) {
                                 return;
                             }
                         }
                         try {
                             ImmutableList.Builder<Volume> results = ImmutableList.builder();
                             for (ListenableFuture<Volume> future: futures) {
                                 results.add(future.get());
                             }
                             set(results.build());
                         } catch (Exception e) {
                             setException(e);
                         }
                     }
                 } else {
                     if (isCancelled()) {
                         if (futures != null) {
                             for (ListenableFuture<Volume> future: futures) {
                                 future.cancel(false);
                             }
                         }
                     }
                 }
             }
             
             @Override
             protected Promise<List<Volume>> delegate() {
                 return promise;
             }
         }
         
         protected class ResponseTask extends ForwardingPromise<Records.Response> implements Runnable, AsyncFunction<Map<Identifier, ClientPeerConnectionExecutor>, Records.Response> {
 
             protected final Promise<Records.Response> promise;
             protected List<Pair<Identifier, ? extends ListenableFuture<? extends ShardedResponseMessage<?>>>> futures;
 
             public ResponseTask() {
                 this(LoggingPromise.create(logger, SettableFuturePromise.<Records.Response>create()));
             }
             
             public ResponseTask(Promise<Records.Response> promise) {
                 this.promise = promise;
                 this.futures = null;
                 
                 addListener(this, SAME_THREAD_EXECUTOR);
                 addListener(FrontendDisconnectTask.this, SAME_THREAD_EXECUTOR);
             }
 
             @Override
             public synchronized ListenableFuture<Records.Response> apply(Map<Identifier, ClientPeerConnectionExecutor> regions) {
                 if (! isDone()) {
                     if (futures == null) {
                         // special case - empty session
                         if (regions.isEmpty()) {
                             futures = ImmutableList.<Pair<Identifier, ? extends ListenableFuture<? extends ShardedResponseMessage<?>>>>of(
                                     Pair.create(
                                             Identifier.zero(), 
                                             Futures.immediateFuture(
                                                 ShardedResponseMessage.of(
                                                         Identifier.zero(), 
                                                         ProtocolResponseMessage.of(
                                                                 task.xid(), 
                                                                 0L,
                                                                 Records.Responses.getInstance().get(task.record().opcode()))))));
                         } else {
                             ShardedRequestMessage<?> request = ShardedRequestMessage.of(Identifier.zero(), task);
                             futures = Lists.newArrayListWithCapacity(regions.size());
                             for (Map.Entry<Identifier, ClientPeerConnectionExecutor> e: regions.entrySet()) {
                                 ListenableFuture<ShardedResponseMessage<?>> future = e.getValue().submit(request);
                                 futures.add(Pair.create(e.getKey(), future));
                             }
                         }
                         for (Pair<Identifier, ? extends ListenableFuture<? extends ShardedResponseMessage<?>>> future: futures) {
                             future.second().addListener(this, SAME_THREAD_EXECUTOR);
                         }
                     }
                 }
                 return this;
             }
             
             @Override
             public synchronized void run() {
                 if (! isDone()) {
                     if (futures != null) {
                         for (Pair<Identifier, ? extends ListenableFuture<? extends ShardedResponseMessage<?>>> future: futures) {
                             if (! future.second().isDone()) {
                                 return;
                             }
                         }
                         try {
                             Records.Response result = null;
                             for (Pair<Identifier, ? extends ListenableFuture<? extends ShardedResponseMessage<?>>> future: futures) {
                                 ShardedResponseMessage<?> response = future.second().get();
                                 if ((result == null) || ((response.record() instanceof Operation.Error) && !(result instanceof Operation.Error))) {
                                     result = response.record();
                                 }
                             }
                             assert (result != null);
                             set(result);
                         } catch (Exception e) {
                             setException(e);
                         }
                     }
                 } else {
                     if (isCancelled()) {
                         if (futures != null) {
                             for (Pair<Identifier, ? extends ListenableFuture<? extends ShardedResponseMessage<?>>> future: futures) {
                                 future.second().cancel(false);
                             }
                         }
                     }
                 }
             }
 
             @Override
             protected Promise<Records.Response> delegate() {
                 return promise;
             }
         }
     }
 
     protected class FrontendSessionActor extends ExecutedPeekingQueuedActor<FrontendRequestTask> {
 
         public FrontendSessionActor(
                 Executor executor) {
             super(executor, new ConcurrentLinkedQueue<FrontendRequestTask>(), FrontendSessionExecutor.this.logger);
         }
         
         public Queue<FrontendRequestTask> mailbox() {
             return mailbox;
         }
         
         @Override
         public boolean isReady() {
             FrontendRequestTask next = mailbox.peek();
             return ((next != null) && (next.response().isDone()));
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this).addValue(FrontendSessionExecutor.this).toString();
         }
         
         /**
          * Not thread safe
          */
         @Override
         protected synchronized boolean doSend(FrontendRequestTask message) {
             if (! mailbox.offer(message)) {
                 return false;
             }
             if (!sharder.send(message) || (!schedule() && (state() == State.TERMINATED))) {
                 mailbox.remove(message);
                 return false;
             }
             return true;
         }
 
         @Override
         protected synchronized boolean apply(FrontendRequestTask input) {
             if (input.response().isDone()) {
                 if (mailbox.remove(input)) {
                     if (! input.isDone()) {
                         try {
                             Records.Response response = input.response().get();
                             Message.ServerResponse<?> result = 
                                     FrontendSessionExecutor.this.apply( 
                                             Pair.create(
                                                     Optional.<Operation.ProtocolRequest<?>>of(input.task()), 
                                                     response));
                             input.set(result);
                         } catch (InterruptedException e) {
                             throw new AssertionError(e);
                         } catch (ExecutionException e) {
                             input.setException(e.getCause());
                         }
                     }
                     return true;
                 }
             }
             return false;
         }
         
         @Override
         protected synchronized void doStop() {
             FrontendRequestTask next;
             while ((next = mailbox.poll()) != null) {
                 next.cancel(true);
             }
             sharder.stop();
             submitter.stop();
             backends.stop();
         }
     }
     
     /**
      * Tracks session watches
      */
     protected class NotificationProcessor implements NotificationListener<ShardedResponseMessage<IWatcherEvent>> {
 
         protected final Set<String> dataWatches;
         protected final Set<String> childWatches;
         
         public NotificationProcessor() {
             this.dataWatches = Collections.synchronizedSet(
                     Collections.newSetFromMap(Maps.<String, Boolean>newHashMap()));
             this.childWatches = Collections.synchronizedSet(
                     Collections.newSetFromMap(Maps.<String, Boolean>newHashMap()));
         }
         
         public void handleRequest(Records.Request request) {
             switch (request.opcode()) {
             case GET_DATA:
             case EXISTS:
                 if (((Records.WatchGetter) request).getWatch()) {
                     dataWatches.add(((Records.PathGetter) request).getPath());
                 }
                 break;
             case GET_CHILDREN:
             case GET_CHILDREN2:
                 if (((Records.WatchGetter) request).getWatch()) {
                     childWatches.add(((Records.PathGetter) request).getPath());
                 }
                 break;
             default:
                 break;
             }
         }
         
         @Override
         public void handleNotification(
                 ShardedResponseMessage<IWatcherEvent> notification) {
             IWatcherEvent event = notification.record();
             Watcher.Event.EventType type = Watcher.Event.EventType.fromInt(event.getType());
             Set<String> watches = (type == Watcher.Event.EventType.NodeChildrenChanged) ? childWatches : dataWatches;
             if (watches.remove(event.getPath())) {
                 onSuccess(notification);
             }
         }
     }
 
     protected class ShardingProcessor extends ExecutedPeekingQueuedActor<FrontendRequestTask> {
 
         protected final CachedFunction<ZNodeLabel.Path, Volume> lookup;
         protected boolean waiting;
         
         public ShardingProcessor(
                 CachedFunction<ZNodeLabel.Path, Volume> lookup,
                 Executor executor) {
             super(executor, new ConcurrentLinkedQueue<FrontendRequestTask>(), FrontendSessionExecutor.this.logger);
             this.lookup = lookup;
             this.waiting = false;
         }
 
         @Override
         public synchronized boolean isReady() {
             if (waiting) {
                 FrontendVolumeRequestTask next = (FrontendVolumeRequestTask) mailbox.peek();
                 if (next.volume().isDone()) {
                     waiting = false;
                 }
             }
             return (!waiting && !mailbox.isEmpty());
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this).addValue(FrontendSessionExecutor.this.toString()).toString();
         }
 
         @Override
         protected synchronized boolean apply(FrontendRequestTask input) {
             if (input instanceof FrontendVolumeRequestTask) {
                 FrontendVolumeRequestTask task = (FrontendVolumeRequestTask) input;
                 if (task.volume().isDone()) {
                     if (waiting) {
                         waiting = false;
                     }
                 } else {
                     if (! waiting) {
                         waiting = true;
                         task.volume().apply(lookup).addListener(this, SAME_THREAD_EXECUTOR);
                         return apply(input);
                     }
                     return false;
                 }
             }
             if (mailbox.remove(input)) {
                 if (!submitter.send(input)) {
                     input.cancel(false);
                     stop();
                 }
             }
             return true;
         }
     }
 
     protected class SubmitProcessor extends ExecutedPeekingQueuedActor<FrontendRequestTask> {
 
         protected final CachedFunction<Identifier, Identifier> assignments;
         protected final ConcurrentMap<Volume, VolumeProcessor> processors;
         protected boolean waiting;
         
         public SubmitProcessor(
                 CachedFunction<Identifier, Identifier> assignments,
                 Executor executor) {
             super(executor, new ConcurrentLinkedQueue<FrontendRequestTask>(), FrontendSessionExecutor.this.logger);
             this.assignments = assignments;
             this.processors = new MapMaker().makeMap();
             this.waiting = false;
         }
 
         @Override
         public synchronized boolean isReady() {
             if (waiting) {
                 FrontendDisconnectTask next = (FrontendDisconnectTask) mailbox.peek();
                 if (next.flush().isDone()) {
                     waiting = false;
                 }
             }
             return (!waiting && !mailbox.isEmpty());
         }
 
         @Override
         public String toString() {
             return Objects.toStringHelper(this).addValue(FrontendSessionExecutor.this.toString()).toString();
         }
 
         @Override
         protected synchronized boolean apply(FrontendRequestTask input) {
             if (input instanceof FrontendDisconnectTask) {
                 FrontendDisconnectTask task = (FrontendDisconnectTask) input;
                 if (task.flush().isDone()) {
                     if (waiting) {
                         waiting = false;
                     }
                     if (mailbox.remove(input)) {
                         task.response().apply(backends.asCache());
                     }
                     return true;
                 } else {
                     if (! waiting) {
                         waiting = true;
                         task.flush().apply(processors).addListener(this, SAME_THREAD_EXECUTOR);
                         return apply(input);
                     }
                     return false;
                 }
             } else {
                 if (mailbox.remove(input)) {
                     try {
                         FrontendVolumeRequestTask task = (FrontendVolumeRequestTask) input;
                         Volume volume = task.volume().get();
                         VolumeProcessor processor = processors.get(volume);
                         if (processor == null) {
                             processor = new VolumeProcessor(volume);
                             processors.put(volume, processor);
                             ListenableFuture<ClientPeerConnectionExecutor> connection = 
                                     Futures.transform(
                                             assignments.apply(volume.getId()), 
                                             backends.asLookup(), 
                                             SAME_THREAD_EXECUTOR);
                             if (logger.isTraceEnabled()) {
                                 if (! connection.isDone()) {
                                     logger.trace("Waiting for connection for {}", volume);
                                 }
                             }
                             Futures.addCallback(connection, processor, SAME_THREAD_EXECUTOR);
                         }
                         if (!processor.send(task)) {
                             task.cancel(false);
                         }
                     } catch (Exception e) {
                         // FIXME
                         throw new UnsupportedOperationException(e);
                     }
                 }
             }
             return true;
         }
         
         @Override
         protected void doStop() {
             for (VolumeProcessor processor: processors.values()) {
                 processor.stop();
             }
             
             super.doStop();
         }
         
         protected class VolumeProcessor extends ExecutedPeekingQueuedActor<Promise<?>> implements FutureCallback<ClientPeerConnectionExecutor> {
 
             protected final Volume volume;
             protected volatile ClientPeerConnectionExecutor connection;
             
             public VolumeProcessor(
                     Volume volume) {
                 super(SubmitProcessor.this.executor, new ConcurrentLinkedQueue<Promise<?>>(), SubmitProcessor.this.logger);
                 this.volume = volume;
                 this.connection = null;
             }
             
             public ClientPeerConnectionExecutor connection() {
                 return connection;
             }
 
             public Volume volume() {
                 return volume;
             }
             
             public ListenableFuture<Volume> flush() {
                 FlushPromise task = new FlushPromise();
                 if (! send(task)) {
                     task.cancel(false);
                 }
                 return task;
             }
 
             @Override
             public boolean isReady() {
                 return ((connection != null) && !mailbox.isEmpty());
             }
 
             @Override
             public void onSuccess(ClientPeerConnectionExecutor result) {
                 this.connection = result;
                 run();
             }
 
             @Override
             public void onFailure(Throwable t) {
                 // FIXME
                 throw new AssertionError(t);
             }
 
             @Override
             public String toString() {
                 return Objects.toStringHelper(this)
                         .add("volume", volume)
                         .add("connection", connection)
                         .addValue(SubmitProcessor.this.toString()).toString();
             }
 
             @Override
             protected boolean apply(Promise<?> input) {
                 ClientPeerConnectionExecutor connection = this.connection;
                 if (connection == null) {
                     return false;
                 } else if (connection.state() == State.TERMINATED) {
                     this.connection = null;
                     // FIXME
                     throw new UnsupportedOperationException();
                 }
                 
                 if (mailbox.remove(input)) {
                     if (input instanceof FrontendVolumeRequestTask) {
                         FrontendVolumeRequestTask task = (FrontendVolumeRequestTask) input;
                         task.response().apply(connection);
                     } else if (input instanceof FlushPromise) {
                         ((FlushPromise) input).run();
                     } else {
                         throw new AssertionError(String.valueOf(input));
                     }
                 }
                 return true;
             }
 
             protected class FlushPromise extends ForwardingPromise<Volume> implements Runnable {
 
                 protected final Promise<Volume> promise;
                 
                 public FlushPromise() {
                     this(LoggingPromise.create(logger, SettableFuturePromise.<Volume>create()));
                 }
 
                 public FlushPromise(Promise<Volume> promise) {
                     this.promise = promise;
                 }
                 
                 @Override
                 public void run() {
                     set(volume());
                 }
                 
                 @Override
                 protected Promise<Volume> delegate() {
                     return promise;
                 }
             }
         }
     }
 }
