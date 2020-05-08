 /**
  * Copyright 2013 Bjørn Remseth (la3lma@gmail.com)
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package no.rmz.blobee.rpc.client;
 
 import no.rmz.blobee.rpc.methods.MethodSignatureResolver;
 import no.rmz.blobee.rpc.methods.ResolverImpl;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import com.google.protobuf.Descriptors.MethodDescriptor;
 import com.google.protobuf.Descriptors.ServiceDescriptor;
 import com.google.protobuf.Message;
 import com.google.protobuf.RpcCallback;
 import com.google.protobuf.RpcChannel;
 import com.google.protobuf.RpcController;
 import com.google.protobuf.Service;
 import edu.umd.cs.findbugs.annotations.SuppressWarnings;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.rmz.blobee.controllers.RpcClientController;
 import no.rmz.blobee.controllers.RpcClientControllerImpl;
 import no.rmz.blobee.protobuf.MethodTypeException;
 import no.rmz.blobee.protobuf.TypeExctractor;
 import no.rmz.blobee.rpc.peer.RemoteExecutionContext;
 import no.rmz.blobee.rpc.peer.wireprotocol.OutgoingRpcAdapter;
 import no.rmz.blobee.rpc.peer.wireprotocol.WireFactory;
 import org.jboss.netty.channel.Channel;
 
 public final class RpcClientImpl implements RpcClient {
 
     private static final Logger log =
             Logger.getLogger(RpcClientImpl.class.getName());
     private static final int MILLIS_TO_SLEEP_BETWEEN_ATTEMPTS = 20;
     private static final int NUM_OF_TIMES_BEFORE_FAILING = 200;
     private static final int MAX_CAPACITY_FOR_INPUT_BUFFER = 10000;
     private static final int TIME_TO_WAIT_WHEN_QUEUE_IS_EMPTY_IN_MILLIS = 50;
     public static final int MAXIMUM_TCP_PORT_NUMBER = 65535;
     private final int capacity;
     private final BlockingQueue<RpcClientSideInvocation> incoming;
     private volatile boolean running = false;
     // XXX Should this also be a concurrent hash?  Yes.  Also, a lot
     //     (perhaps all) of the synchronization over invocations
     //     should be removed.
     private final Map<Long, RpcClientSideInvocation> invocations =
             new TreeMap<>();
     private OutgoingRpcAdapter wire;
     private long nextIndex;
     private Channel channel;
     private final Object mutationMonitor = new Object();
     private final Object runLock = new Object();
     private final MethodSignatureResolver resolver;
 
     @Override
     public void returnCall(
             final RemoteExecutionContext dc,
             final Message message) {
         checkNotNull(dc);
         checkNotNull(message);
         synchronized (invocations) {
             final long rpcIndex = dc.getRpcIndex();
             final RpcClientSideInvocation invocation =
                     invocations.get(rpcIndex);
 
             if (invocation == null) {
                 log.log(Level.FINEST,
                         "Attempt to return  nonexistant invocation: "
                         + rpcIndex + " with message message " + message);
                 return;
             }
 
             // Deliver the result then disable the controller
             // so it can be reused.
             final RpcCallback<Message> done = invocation.getDone();
 
             // XXX If we add a flag to the return value saying
             //     "noReturnValue", then "done.run(message)" will
             //     only be invoked if there is actually a return
             //     value.  This can be used to close down a sequence
             //     of multiple return values.
             done.run(message);
 
             if (!dc.isMultiReturn()) {
                 deactivateInvocation(dc.getRpcIndex());
             }
         }
     }
 
     @Override
     public void terminateMultiSequence(long rpcIndex) {
         checkArgument(rpcIndex >= 0);
         synchronized (invocations) {
 
             final RpcClientSideInvocation invocation =
                     invocations.get(rpcIndex);
 
             if (invocation == null) {
                 log.log(Level.FINEST,
                         "Attempt to terminate  nonexistant  multiinvocation sequence: "
                         + rpcIndex);
                 return;
             }
             deactivateInvocation(rpcIndex);
 
         }
     }
 
     private void deactivateInvocation(final Long index) {
         synchronized (invocations) {
             final RpcClientSideInvocation invocation =
                     invocations.get(index);
             if (invocation == null) {
                 throw new IllegalStateException(
                         "Couldn't find call stub for invocation " + index);
             }
 
 
             invocations.remove(index);
             if (invocations.containsKey(index)) {
                 log.info("Removal of index did not succeed for index " + index);
             }
             invocation.getController().setActive(false);
 
         }
     }
     private final Runnable incomingDispatcher = new Runnable() {
         @Override
         public void run() {
             while (running) {
                 sendFirstAvailableOutgoingInvocation();
             }
 
             try {
                 runningLock.lock();
                 noLongerRunning.signal();
             }
             finally {
                 runningLock.unlock();
             }
         }
     };
 
     private void sendFirstAvailableOutgoingInvocation() {
         try {
             final RpcClientSideInvocation invocation =
                     incoming.poll(TIME_TO_WAIT_WHEN_QUEUE_IS_EMPTY_IN_MILLIS,
                     TimeUnit.MILLISECONDS);
             // If nothing there, then just return to the busy-waiting loop.
             if (invocation == null) {
                 return;
             }
 
             // If the invocation is cancelled already, don't even bother
             // sending it over the wire, just forget about it.
             if (invocation.getController().isCanceled()) {
                 return;
             }
 
             if (listener != null) {
                 listener.listenToInvocation(invocation);
             }
 
             final Long currentIndex = nextIndex++;

             final RpcClientController rcci =
                     (RpcClientController) invocation.getController();
 
            // Avoid leaking memory for no-return invocations.
            if (!rcci.isNoReturn()) {
                synchronized (invocations) {
                    invocations.put(currentIndex, invocation);
                }
            }

             rcci.setClientAndIndex(this, currentIndex);
             sendInvocation(invocation, currentIndex);
         }
         catch (InterruptedException ex) {
             log.warning("Something went south");
         }
     }
 
     public RpcClientImpl(
             final int capacity) {
         this(capacity, new ResolverImpl());
     }
     private final Lock runningLock;
     private final Condition noLongerRunning;
 
     public RpcClientImpl(
             final int capacity,
             final MethodSignatureResolver resolver) {
 
         checkArgument(0 < capacity && capacity < MAX_CAPACITY_FOR_INPUT_BUFFER);
         this.capacity = capacity;
         this.incoming =
                 new ArrayBlockingQueue<>(capacity);
         this.resolver = checkNotNull(resolver);
         this.runningLock = new ReentrantLock();
         this.noLongerRunning = runningLock.newCondition();
     }
 
     public void setChannel(final Channel channel) {
         synchronized (mutationMonitor) {
             if (this.channel != null) {
                 throw new IllegalStateException(
                         "Can't set channel since channel is already set");
             }
             this.channel = checkNotNull(channel);
             this.wire = WireFactory.getWireForChannel(channel);
         }
     }
 
     public void start(
             final Channel channel,
             final ChannelShutdownCleaner channelCleanupRunnable) {
         checkNotNull(channel);
         checkNotNull(channelCleanupRunnable);
         synchronized (runLock) {
             setChannel(channel);
 
             // This runnable will start, then just wait until
             // the channel stops, then the bootstrap will be instructed
             // to reease external resources, and with that the client
             // is completely halted.
             final Runnable channelCleanup = new Runnable() {
                 @Override
                 public void run() {
                     // Wait until the connection is
                     // closed or the connection attempt fails.
                     channel.getCloseFuture().awaitUninterruptibly();
                     // Then do whatever cleanup is necessary
                     channelCleanupRunnable.shutdownHook();
                 }
             };
 
             running = true;
             final Thread thread = new Thread(channelCleanup, "client shutdown cleaner");
             thread.start();
 
             final Thread dispatcherThread =
                     new Thread(incomingDispatcher, "Incoming dispatcher for client");
             dispatcherThread.start();
         }
     }
 
     @Override
     public RpcChannel newClientRpcChannel() {
         return new RpcChannel() {
             @Override
             public void callMethod(
                     final MethodDescriptor method,
                     final RpcController controller,
                     final Message request,
                     final Message responsePrototype,
                     final RpcCallback<Message> done) {
 
                 checkNotNull(method);
                 checkNotNull(controller);
                 checkNotNull(request);
                 checkNotNull(responsePrototype);
                 checkNotNull(done);
 
                 // Binding the controller to this invocation.
                 if (controller instanceof RpcClientController) {
                     final RpcClientController ctrl =
                             (RpcClientController) controller;
                     if (ctrl.isActive()) {
                         throw new IllegalStateException(
                                 "Attempt to activate already active controller");
                     } else {
                         ctrl.setActive(running);
                     }
                 }
 
                 final RpcClientSideInvocation invocation =
                         new RpcClientSideInvocation(
                         method, controller,
                         request, responsePrototype, done);
 
                 // XXX
                 // Busy-wait to add to in-queue
                 for (int i = 0; i < NUM_OF_TIMES_BEFORE_FAILING; i++) {
 
                     if (incoming.offer(invocation)) {
                         return;
                     }
                     try {
                         Thread.sleep(MILLIS_TO_SLEEP_BETWEEN_ATTEMPTS);
                     }
                     catch (InterruptedException ex) {
                         log.info("Ignoring interruption " + ex);
                     }
                 }
                 throw new RuntimeException("Couldn't add to queue");
             }
         };
     }
 
     @Override
     public BlobeeRpcController newController() {
         return new RpcClientControllerImpl();
     }
 
     @Override
     public void failInvocation(final long rpcIndex, final String errorMessage) {
         checkNotNull(errorMessage);
         checkArgument(rpcIndex >= 0);
         final RpcClientSideInvocation invocation;
         synchronized (invocations) {
             invocation = invocations.get(rpcIndex);
         }
         if (invocation == null) {
             log.log(Level.FINEST,
                     "Attempt to fail nonexistant invocation: "
                     + rpcIndex
                     + " with error message "
                     + errorMessage);
             return;
         }
 
         checkNotNull(invocation);
         invocation.getController().setFailed(errorMessage);
         deactivateInvocation(rpcIndex);
     }
 
     @Override
     public void cancelInvocation(final long rpcIndex) {
         checkArgument(rpcIndex >= 0);
 
         synchronized (invocations) {
             final RpcClientSideInvocation invocation =
                     invocations.get(rpcIndex);
 
             if (invocation == null) {
                 log.log(Level.FINEST,
                         "Attempt to cancel nonexistant invocation: "
                         + rpcIndex);
                 return;
             }
         }
 
         wire.sendCancelMessage(rpcIndex);
 
         deactivateInvocation(rpcIndex);
     }
 
     @Override
     public RpcClient start() {
         return this;
     }
     private RpcClientSideInvocationListener listener;
 
     @Override
     public RpcClient addInvocationListener(
             final RpcClientSideInvocationListener listener) {
         checkNotNull(listener);
         this.listener = listener;
         return this;
     }
 
     @Override
     public MethodSignatureResolver getResolver() {
         return resolver;
     }
 
     @Override
     public RpcClient addProtobuferRpcInterface(final Object instance) {
 
         if (!( instance instanceof com.google.protobuf.Service )) {
             throw new IllegalArgumentException(
                     "Expected a class extending com.google.protobuf.Service");
         }
 
         final Service service = (Service) instance;
 
         final ServiceDescriptor descriptor = service.getDescriptorForType();
         final List<MethodDescriptor> methods = descriptor.getMethods();
         for (final MethodDescriptor md : methods) {
             try {
 
                 final Message inputType = TypeExctractor.getReqestPrototype(service, md);
                 final Message outputType = TypeExctractor.getResponsePrototype(service, md);
 
                 // The lines above were made in desperation snce I couldn1t get
                 // these two lines to work.  If I made some stupid mistake,
                 // and someone can get the two lines below to work, I can
                 // remove the entire TypeExtractor class and be very happy
                 // about that :-)
                 // final MessageLite outputType = md.getOutputType().toProto().getDefaultInstance();
                 // final MessageLite inputType = md.getInputType().toProto().getDefaultInstance();
 
                 resolver.addTypes(md, inputType, outputType);
             }
             catch (MethodTypeException ex) {
                 /// XXXX Something  more severe should happen here
                 Logger.getLogger(
                         RpcClientImpl.class.getName()).log(
                         Level.SEVERE, null, ex);
             }
         }
 
         return this;
     }
 
     @Override
     public RpcClient addInterface(final Class serviceDefinition) {
         checkNotNull(serviceDefinition);
 
         Method newReflectiveService = null;
         for (final Method m : serviceDefinition.getMethods()) {
             if (m.getName().equals("newReflectiveService")) {
                 newReflectiveService = m;
                 break;
             }
         }
 
         if (newReflectiveService == null) {
             throw new IllegalStateException(
                     "class " + serviceDefinition + " is not a service defining class");
         }
         final Object instance;
         try {
             instance = newReflectiveService.invoke(null, (Object) null);
         }
         catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
             throw new RuntimeException(ex);
         }
 
         addProtobuferRpcInterface(instance);
 
         return this;
     }
 
     @Override
     @SuppressWarnings("WA_AWAIT_NOT_IN_LOOP")
     public void stop() {
         running = false;
 
         try {
             runningLock.lock();
             noLongerRunning.await();
         }
         catch (InterruptedException ex) {
             throw new RuntimeException(ex); // XXXX
         }
         finally {
             runningLock.unlock();
         }
 
         if (channel.isOpen() && channel.isBound()) {
             try {
                 log.info("about to close stuff");
                 /// XXXX
                 // For some reason this fails, and the catch below doesn't work.
                 //       channel.close();
             }
             catch (Throwable e) {
                 log.log(Level.SEVERE,
                         "Something went wrong when closing channel:  "
                         + channel, e);
             }
         }
     }
 
     private void sendInvocation(
             final RpcClientSideInvocation invocation,
             final Long rpcIndex) {
         checkNotNull(invocation);
         checkArgument(rpcIndex >= 0);
         // Then creating the protobuf representation of
         // the invocation, in preparation of sending it
         // down the wire.
 
         final MethodDescriptor md = invocation.getMethod();
         final String methodName = md.getFullName();
         final String inputType = md.getInputType().getFullName();
         final String outputType = md.getOutputType().getFullName();
         final Message request = invocation.getRequest();
         final RpcClientController controller = invocation.getController();
         final boolean multiReturn = controller.isMultiReturn();
         final boolean noReturn = controller.isNoReturn();
 
         wire.sendInvocation(
                 methodName,
                 inputType, outputType, rpcIndex, request,
                 multiReturn, noReturn);
     }
 }
