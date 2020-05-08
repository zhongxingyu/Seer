 package edu.uw.zookeeper.safari.backend;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.WeakHashMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ScheduledExecutorService;
 
 import net.engio.mbassy.common.IConcurrentSet;
 import net.engio.mbassy.common.StrongConcurrentSet;
 import com.google.common.base.Function;
 import com.google.common.base.Objects;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 
 import edu.uw.zookeeper.common.LoggingPromise;
 import edu.uw.zookeeper.common.Promise;
 import edu.uw.zookeeper.common.TimeValue;
 import edu.uw.zookeeper.data.ZNodeLabel;
 import edu.uw.zookeeper.protocol.ConnectMessage;
 import edu.uw.zookeeper.protocol.Message;
 import edu.uw.zookeeper.protocol.Operation;
 import edu.uw.zookeeper.protocol.ProtocolConnection;
 import edu.uw.zookeeper.protocol.SessionListener;
 import edu.uw.zookeeper.protocol.client.ConnectTask;
 import edu.uw.zookeeper.protocol.client.PendingQueueClientExecutor;
 import edu.uw.zookeeper.protocol.proto.OpCodeXid;
 import edu.uw.zookeeper.protocol.proto.Records;
 import edu.uw.zookeeper.safari.Identifier;
 import edu.uw.zookeeper.safari.common.CachedFunction;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedRequestMessage;
 import edu.uw.zookeeper.safari.peer.protocol.ShardedResponseMessage;
 
 public class ShardedClientExecutor<C extends ProtocolConnection<? super Message.ClientSession,? extends Operation.Response,?,?,?>> extends PendingQueueClientExecutor<ShardedRequestMessage<?>, ShardedResponseMessage<?>, ShardedClientExecutor.ShardedRequestTask, C> {
 
     public static <C extends ProtocolConnection<? super Message.ClientSession,? extends Operation.Response,?,?,?>> ShardedClientExecutor<C> newInstance(
             Function<ZNodeLabel.Path, Identifier> lookup,
             CachedFunction<Identifier, OperationPrefixTranslator> translator,
             ConnectMessage.Request request,
             C connection,
             ScheduledExecutorService executor) {
         return newInstance(
                 lookup,
                 translator,
                 ConnectTask.connect(connection, request),
                 connection,
                 TimeValue.milliseconds(request.getTimeOut()),
                 executor,
                 new StrongConcurrentSet<SessionListener>());
     }
 
     public static <C extends ProtocolConnection<? super Message.ClientSession,? extends Operation.Response,?,?,?>> ShardedClientExecutor<C> newInstance(
             Function<ZNodeLabel.Path, Identifier> lookup,
             CachedFunction<Identifier, OperationPrefixTranslator> translator,
             ListenableFuture<ConnectMessage.Response> session,
             C connection,
             TimeValue timeOut,
             ScheduledExecutorService executor,
             IConcurrentSet<SessionListener> listeners) {
         return new ShardedClientExecutor<C>(
                 lookup,
                 translator,
                 session, 
                 connection,
                 timeOut,
                 executor,
                 listeners);
     }
 
     protected final Function<ZNodeLabel.Path, Identifier> lookup;
     protected final CachedFunction<Identifier, OperationPrefixTranslator> translator;
     // not thread-safe
     protected final Set<ListenableFuture<OperationPrefixTranslator>> futures;
     
     protected ShardedClientExecutor(
             Function<ZNodeLabel.Path, Identifier> lookup,
             CachedFunction<Identifier, OperationPrefixTranslator> translator,
             ListenableFuture<ConnectMessage.Response> session,
             C connection,
             TimeValue timeOut,
             ScheduledExecutorService executor,
             IConcurrentSet<SessionListener> listeners) {
         super(session, connection, timeOut, executor, listeners);
         this.lookup = lookup;
         this.translator = translator;
         this.futures = Collections.newSetFromMap(
                 new WeakHashMap<ListenableFuture<OperationPrefixTranslator>, Boolean>());
     }
 
     @Override
     public ListenableFuture<ShardedResponseMessage<?>> submit(
             ShardedRequestMessage<?> request, Promise<ShardedResponseMessage<?>> promise) {
         ListenableFuture<OperationPrefixTranslator> future;
         try {
             future = translator.apply(request.getIdentifier());
         } catch (Exception e) {
             return Futures.immediateFailedFuture(e);
         }
         ShardedRequestTask task = new ShardedRequestTask(
                 future,
                 request, 
                 LoggingPromise.create(logger, promise));
         if (! send(task)) {
             task.cancel(true);
         }
         return task;
     }
 
     @Override
     public void handleConnectionRead(Operation.Response response) {
        if (response instanceof Message.ServerResponse) {
            ShardedResponseMessage<?> unsharded;
            Message.ServerResponse<?> message = (Message.ServerResponse<?>) response;
            if (message.record() instanceof Records.PathGetter) {
                Identifier shard = lookup.apply(ZNodeLabel.Path.of(((Records.PathGetter) message.record()).getPath()));
                assert (shard != null);
                OperationPrefixTranslator translator = this.translator.cached().apply(shard);
                assert (translator != null);
                unsharded = ShardedResponseMessage.of(shard, translator.apply(message));
            } else {
                unsharded = ShardedResponseMessage.of(Identifier.zero(), message);
            }
            response = unsharded;
         }
         synchronized (pending) {
            super.handleConnectionRead(response);
             runPending();
         }
     }
     
     @Override
     protected void doRun() throws Exception {
         ShardedRequestTask next;
         while ((next = mailbox.peek()) != null) {
             if (next.isReady()) {
                 mailbox.remove(next);
                 if (!apply(next)) {
                     break;
                 }
             } else {
                 if (futures.add(next.translator())) {
                     next.translator().addListener(this, SAME_THREAD_EXECUTOR);
                 }
                 break;
             }
         }
     }
     
     @Override
     protected boolean apply(ShardedRequestTask input) {
         assert (input.translator().isDone());
         if (! input.isDone()) {
             if (state() != State.TERMINATED) {
                 Message.ClientRequest<?> sharded = null;
                 try {
                     sharded = input.shard();
                 } catch (ExecutionException e) {
                     // we add failed requests to the pending queue
                     // to ensure that the future is set with respect
                     // to request ordering
                     FailedRequestTask failed = new FailedRequestTask(
                             new ShardedRequestException(input.task(), e.getCause()),
                             input.task().xid(),
                             this,
                             input.promise());
                     pending.add(failed);
                     runPending();
                     sharded = null;
                 } catch (InterruptedException e) {
                     throw new AssertionError(e);
                 }
                 if (sharded != null) {
                     write(sharded, input.promise());
                 }
             } else {
                 input.cancel(true);
             }
         }
         
         return (state() != State.TERMINATED);
     }
     
     @Override
     protected void runExit() {
         if (state.compareAndSet(State.RUNNING, State.WAITING)) {
             ShardedRequestTask next = mailbox.peek();
             if ((next != null) && (next.isReady())) {
                 schedule();
             }
         }
     }
     
     protected void runPending() {
         synchronized (pending) {
             Iterator<PendingTask<ShardedResponseMessage<?>>> itr = pending.iterator();
             while (itr.hasNext()) {
                 PendingTask<ShardedResponseMessage<?>> next = itr.next();
                 if (next instanceof Runnable) {
                     itr.remove();
                     ((Runnable) next).run();
                 } else {
                     break;
                 }
             }
         }
     }
     
     public static class ShardedRequestException extends Exception {
     
         private static final long serialVersionUID = -6523271323477641540L;
         
         private final ShardedRequestMessage<?> request;
         
         public ShardedRequestException(ShardedRequestMessage<?> request, Throwable cause) {
             super(String.format("%s failed", request), cause);
             this.request = request;
         }
         
         public ShardedRequestMessage<?> request() {
             return request;
         }
     }
 
     protected static class ShardedRequestTask extends PendingQueueClientExecutor.RequestTask<ShardedRequestMessage<?>, ShardedResponseMessage<?>> {
 
         protected final ListenableFuture<OperationPrefixTranslator> translator;
         
         public ShardedRequestTask(
                 ListenableFuture<OperationPrefixTranslator> translator,
                 ShardedRequestMessage<?> task,
                 Promise<ShardedResponseMessage<?>> promise) {
             super(task, promise);
             assert(task.xid() != OpCodeXid.PING.xid());
             this.translator = translator;
         }
         
         public boolean isReady() {
             return translator.isDone();
         }
         
         public ListenableFuture<OperationPrefixTranslator> translator() {
             return translator;
         }
         
         public Message.ClientRequest<?> shard() throws InterruptedException, ExecutionException {
             return translator.get().apply(task.getRequest());
         }
 
         @Override
         protected Objects.ToStringHelper toString(Objects.ToStringHelper toString) {
             return super.toString(toString.add("translator", translator));
         }
     }
     
     protected static class FailedRequestTask extends PendingQueueClientExecutor.PendingTask<ShardedResponseMessage<?>> implements Runnable {
 
         protected final Exception failure;
         
         public FailedRequestTask(
                 Exception failure,
                 int xid,
                 FutureCallback<? super PendingTask<ShardedResponseMessage<?>>> callback,
                 Promise<ShardedResponseMessage<?>> promise) {
             super(xid, callback, promise);
             this.failure = failure;
         }
 
         @Override
         public void run() {
             setException(failure);
         }
 
         @Override
         protected Objects.ToStringHelper toString(Objects.ToStringHelper toString) {
             return super.toString(toString.add("failure", failure));
         }
     }
 }
