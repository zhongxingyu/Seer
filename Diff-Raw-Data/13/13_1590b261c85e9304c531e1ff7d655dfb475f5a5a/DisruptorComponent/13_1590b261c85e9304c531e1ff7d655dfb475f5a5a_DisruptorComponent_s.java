 /*
  * Copyright 2012 Riccardo Sirchia
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.camel.component.disruptor;
 
 import com.lmax.disruptor.RingBuffer;
 import com.lmax.disruptor.dsl.Disruptor;
 import com.lmax.disruptor.dsl.ProducerType;
 import java.util.*;
 import java.util.concurrent.*;
 import org.apache.camel.Endpoint;
 import org.apache.camel.Exchange;
 import org.apache.camel.impl.DefaultComponent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * TODO: documentation
  * Parameters:
  * - bufferSize: size of the ringbuffer (will be rounded up to nearest power of 2), default 1024
  * - concurrentConsumers: number of concurrent threads processing exchanges, default 1
  * - multipleConsumers: whether multiple consumers or Publish-Subscribe style multicast is supported, default false
  */
 public class DisruptorComponent extends DefaultComponent {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(DisruptorComponent.class);
 
     public static final int DEFAULT_BUFFER_SIZE = 1024;
     public static final int MAX_CONCURRENT_CONSUMERS = 500;
 
 
     protected int bufferSize = DEFAULT_BUFFER_SIZE;
     //for SEDA compatibility only
     protected int queueSize = -1;
 
     protected int defaultConcurrentConsumers = 1;
     private boolean defaultMultipleConsumers = false;
     private ProducerType defaultProducerType = ProducerType.MULTI;
     private DisruptorWaitStrategy defaultWaitStrategy = DisruptorWaitStrategy.BLOCKING;
 
     private final Map<String, DisruptorReference> disruptors = new ConcurrentHashMap<String, DisruptorReference>();
 
     @Override
     protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
         int concurrentConsumers = getAndRemoveParameter(parameters, "concurrentConsumers", Integer.class, defaultConcurrentConsumers);
         boolean limitConcurrentConsumers = getAndRemoveParameter(parameters, "limitConcurrentConsumers", Boolean.class, true);
         if (limitConcurrentConsumers && concurrentConsumers > MAX_CONCURRENT_CONSUMERS) {
             throw new IllegalArgumentException("The limitConcurrentConsumers flag in set to true. ConcurrentConsumers cannot be set at a value greater than "
                     + MAX_CONCURRENT_CONSUMERS + " was " + concurrentConsumers);
         }
 
         if (concurrentConsumers < 0) {
             throw new IllegalArgumentException("concurrentConsumers found to be " + concurrentConsumers +
                     ", must be greater than 0");
         }
 
         int size = 0;
         if (parameters.containsKey("size")) {
             size = getAndRemoveParameter(parameters, "size", int.class);
             if (size <= 0) {
                 throw new IllegalArgumentException("size found to be " + size + ", must be greater than 0");
             }
         }
 
         //TODO actually use pollTimeout parameter?
         parameters.remove("pollTimeout");
 
         DisruptorWaitStrategy waitStrategy = getAndRemoveParameter(parameters, "waitStrategy", DisruptorWaitStrategy.class, defaultWaitStrategy);
 
         ProducerType producerType = getAndRemoveParameter(parameters, "producerType", ProducerType.class, defaultProducerType);
 
         boolean multipleConsumers = getAndRemoveParameter(parameters, "multipleConsumers", boolean.class, defaultMultipleConsumers);
 
         DisruptorReference disruptorReference = getOrCreateDisruptor(uri, size, defaultProducerType, waitStrategy);
         DisruptorEndpoint disruptorEndpoint = new DisruptorEndpoint(uri, this, disruptorReference, concurrentConsumers, multipleConsumers);
         disruptorEndpoint.configureProperties(parameters);
 
         return disruptorEndpoint;
     }
 
     private synchronized DisruptorReference getOrCreateDisruptor(String uri, int size, ProducerType producerType, DisruptorWaitStrategy waitStrategy)
             throws Exception {
         String key = getDisruptorKey(uri);
 
         int sizeToUse;
         if (size > 0) {
             sizeToUse = size;
         } else if (bufferSize > 0) {
             sizeToUse = bufferSize;
         } else if (queueSize > 0) {
             sizeToUse = queueSize;
         } else {
             sizeToUse = DEFAULT_BUFFER_SIZE;
         }
         DisruptorReference ref = getDisruptors().get(key);
         if (ref == null) {
             ref = new DisruptorReference(this, uri, powerOfTwo(sizeToUse), producerType, waitStrategy);
             getDisruptors().put(key, ref);
         }
 
         if (ref.hasNullReference()) {
             ref.createDisruptor();
             ref.start();
         }
         return ref;
     }
 
     public static int powerOfTwo(int size) {
         size--;
         size |= size >> 1;
         size |= size >> 2;
         size |= size >> 4;
         size |= size >> 8;
         size |= size >> 16;
         size++;
         return size;
     }
 
     public String getDisruptorKey(String uri) {
         if (uri.contains("?")) {
             // strip parameters
             uri = uri.substring(0, uri.indexOf('?'));
         }
         return uri;
     }
 
     @Override
     protected void doStop() throws Exception {
         getDisruptors().clear();
         super.doStop();
     }
 
     public Map<String, DisruptorReference> getDisruptors() {
         return disruptors;
     }
 
     public int getDefaultConcurrentConsumers() {
         return defaultConcurrentConsumers;
     }
 
     public void setDefaultConcurrentConsumers(int defaultConcurrentConsumers) {
         this.defaultConcurrentConsumers = defaultConcurrentConsumers;
     }
 
     public boolean isDefaultMultipleConsumers() {
         return defaultMultipleConsumers;
     }
 
     public void setDefaultMultipleConsumers(boolean defaultMultipleConsumers) {
         this.defaultMultipleConsumers = defaultMultipleConsumers;
     }
 
     public ProducerType getDefaultProducerType() {
         return defaultProducerType;
     }
 
     public void setDefaultProducerType(ProducerType defaultProducerType) {
         this.defaultProducerType = defaultProducerType;
     }
 
     public DisruptorWaitStrategy getDefaultWaitStrategy() {
         return defaultWaitStrategy;
     }
 
     public void setDefaultWaitStrategy(DisruptorWaitStrategy defaultWaitStrategy) {
         this.defaultWaitStrategy = defaultWaitStrategy;
     }
 
     //TODO test queueSize in unit test
     @Deprecated
     public void setQueueSize(int size) {
         LOGGER.warn("Using deprecated queueSize parameter for SEDA compatibility, use bufferSize instead");
         queueSize = size;
     }
 
     @Deprecated
     public int getQueueSize() {
         LOGGER.warn("Using deprecated queueSize parameter for SEDA compatibility, use bufferSize instead");
         return queueSize;
     }
 
     //TODO test bufferSize in unit test?
     public void setBufferSize(int size) {
         bufferSize = size;
     }
 
     public int getBufferSize() {
         return bufferSize;
     }
     /**
      * Holder for Disruptor references.
      * <p/>
      * This is used to keep track of the usages of the Disruptors, so we know when a Disruptor is no longer in use, and
      * can safely be discarded.
      */
     // TODO: Is this class life cycle inline with an Endpoint? Should it be bound to an endpoint?
     public static final class DisruptorReference {
         private final Set<DisruptorEndpoint> endpoints = Collections.newSetFromMap(new WeakHashMap<DisruptorEndpoint, Boolean>(4));
         private final DisruptorComponent component;
         private final String uri;
 
         private Disruptor<ExchangeEvent> disruptor;
 
         private final DelayedExecutor delayedExecutor = new DelayedExecutor();
 
         private ExecutorService executor;
 
         private final ProducerType producerType;
 
         private final int size;
 
         private final DisruptorWaitStrategy waitStrategy;
 
         private LifecycleAwareExchangeEventHandler[] handlers;
 
         private Queue<Exchange> temporaryExchangeBuffer;
 
         private DisruptorReference(DisruptorComponent component, String uri, int size, ProducerType producerType, DisruptorWaitStrategy waitStrategy)
                 throws Exception {
             this.component = component;
             this.uri = uri;
             this.size = size;
             this.producerType = producerType;
             this.waitStrategy = waitStrategy;
             temporaryExchangeBuffer = new ArrayBlockingQueue<Exchange>(size);
             reconfigure();
         }
 
         public void createDisruptor() throws Exception {
             disruptor = new Disruptor<ExchangeEvent>(ExchangeEventFactory.INSTANCE, size, delayedExecutor, producerType,
                     waitStrategy.createWaitStrategyInstance());
         }
 
         public int getCount() {
             return endpoints.size();
         }
 
         public boolean hasNullReference() {
             return disruptor == null;
         }
 
         public void publish(Exchange exchange) {
             RingBuffer<ExchangeEvent> ringBuffer = disruptor.getRingBuffer();
 
             long sequence = ringBuffer.next();
             ringBuffer.get(sequence).setExchange(exchange);
             ringBuffer.publish(sequence);
         }
 
         public void reconfigure() throws Exception {
             /*
                 TODO handle reconfigure correctly with a full buffer and producers blocking
                 Instead of blocking until a fre spot on the ringbuffer is avaiable, they now
                 probably get an exception
              */
             shutdown();
 
             createDisruptor();
 
             ArrayList<LifecycleAwareExchangeEventHandler> eventHandlers = new ArrayList<LifecycleAwareExchangeEventHandler>();
 
             for (DisruptorEndpoint endpoint : endpoints) {
                 Collection<LifecycleAwareExchangeEventHandler> consumerEventHandlers = endpoint.createConsumerEventHandlers();
 
                 if (consumerEventHandlers != null) {
                     eventHandlers.addAll(consumerEventHandlers);
                 }
             }
 
             handleEventsWith(eventHandlers.toArray(new LifecycleAwareExchangeEventHandler[eventHandlers.size()]));
 
             start();
         }
 
         private void start() {
             disruptor.start();
 
             if (executor != null) {
                 //and use our delayed executor to really really execute the event handlers now
                 delayedExecutor.executeDelayedCommands(executor);
             }
 
             //make sure all event handlers are correctly started before we continue
             for (LifecycleAwareExchangeEventHandler handler : handlers) {
                 boolean eventHandlerStarted = false;
                 while (!eventHandlerStarted) {
                     try {
                         //The disruptor start command executed above should have triggered a start signal to all
                         //event processors which, in their death, should notify our event handlers. They respond by
                         //switching a latch and we want to await that latch here to make sure they are started.
                         if (!handler.awaitStarted(10, TimeUnit.SECONDS)) {
                             //we wait for a relatively long, but limited amount of time to prevent an application using
                             //this component from hanging indefinitely
                             //Please report a bug if you can repruduce this
                             LOGGER.error("Disruptor/event handler failed to start properly, PLEASE REPORT");
                         }
                         eventHandlerStarted = true;
                     } catch (InterruptedException e) {
                         //just retry
                     }
                 }
             }
 
             //now empty out all buffered Exchange if we had any
             List<Exchange> exchanges = new ArrayList<Exchange>(temporaryExchangeBuffer.size());
             while (!temporaryExchangeBuffer.isEmpty()) {
                 exchanges.add(temporaryExchangeBuffer.remove());
             }
             //and offer them again to our new ringbuffer
             for (Exchange exchange : exchanges) {
                 publish(exchange);
             }
         }
 
         private void shutdown() {
             if (disruptor != null) {
                 //check if we had a blocking event handler to keep an empty disruptor 'busy'
                if (handlers != null && handlers.length == 1 && handlers[0] instanceof BlockingExhangeEventHandler) {
                     //yes we did, unblock it so we can get rid of our backlog empty its pending exchanged in our temporary buffer
                    BlockingExhangeEventHandler blockingExhangeEventHandler = (DisruptorReference.BlockingExhangeEventHandler) handlers[0];
                    blockingExhangeEventHandler.unblock();
                 }
 
                 disruptor.shutdown();
 
                 //they have already been given a trigger to halt when they are done by shutting down the disruptor
                 //we do however want to await their completion before they are scheduled to process events from the new
                 for (LifecycleAwareExchangeEventHandler eventHandler : handlers) {
                     boolean eventHandlerFinished = false;
                     //the disruptor is now empty and all consumers are either done or busy processing their last exchange
                     while (!eventHandlerFinished) {
                         try {
                             //The disruptor shutdown command executed above should have triggered a halt signal to all
                             //event processors which, in their death, should notify our event handlers. They respond by
                             //switching a latch and we want to await that latch here to make sure they are done.
                             if (!eventHandler.awaitStopped(10, TimeUnit.SECONDS)) {
                                 //we wait for a relatively long, but limited amount of time to prevent an application using
                                 //this component from hanging indefinitely
                                 //Please report a bug if you can repruduce this
                                 LOGGER.error("Disruptor/event handler failed to shut down properly, PLEASE REPORT");
                             }
                             eventHandlerFinished = true;
                         } catch (InterruptedException e) {
                             //just retry
                         }
                     }
                 }
 
                 handlers = new LifecycleAwareExchangeEventHandler[0];
 
                 disruptor = null;
             }
 
             if (executor != null) {
                 component.getCamelContext().getExecutorServiceManager().shutdown(executor);
                 executor = null;
             }
         }
 
         private void handleEventsWith(LifecycleAwareExchangeEventHandler[] newHandlers) {
             if (newHandlers == null || newHandlers.length == 0) {
                 handlers = new LifecycleAwareExchangeEventHandler[1];
                handlers[0] = new BlockingExhangeEventHandler();
             } else {
                 handlers = newHandlers;
             }
             resizeThreadPoolExecutor(handlers.length);
             disruptor.handleEventsWith(handlers);
         }
 
         public long remainingCapacity() {
             return disruptor.getRingBuffer().remainingCapacity();
         }
 
         public int getBufferSize() {
             return disruptor.getRingBuffer().getBufferSize();
         }
 
         public void addEndpoint(DisruptorEndpoint disruptorEndpoint) {
             endpoints.add(disruptorEndpoint);
         }
 
         public void removeEndpoint(DisruptorEndpoint disruptorEndpoint) {
             if (getCount() == 1) {
                 this.shutdown();
             }
             endpoints.remove(disruptorEndpoint);
         }
 
         public int size() {
             if (disruptor != null) {
                 return (int) (disruptor.getRingBuffer().getBufferSize() - disruptor.getRingBuffer().remainingCapacity()) + temporaryExchangeBuffer.size();
             }
             return temporaryExchangeBuffer.size();
         }
 
         private void resizeThreadPoolExecutor(int newSize) {
             if (executor == null && newSize > 0) {
                 //no thread pool executor yet, create a new one
                 executor = component.getCamelContext().getExecutorServiceManager().newFixedThreadPool(this, uri,
                         newSize);
             } else if (executor != null && newSize <= 0) {
                 //we need to shut down our executor
                 component.getCamelContext().getExecutorServiceManager().shutdown(executor);
                 executor = null;
             } else if (executor instanceof ThreadPoolExecutor) {
                 //our thread pool executor is of type ThreadPoolExecutor, we know how to resize it
                 ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
                 threadPoolExecutor.setCorePoolSize(newSize);
                 threadPoolExecutor.setMaximumPoolSize(newSize);
             } else if (newSize > 0) {
                 //hmmm...no idea what kind of executor this is...just kill it and start fresh
                 component.getCamelContext().getExecutorServiceManager().shutdown(executor);
 
                 executor = component.getCamelContext().getExecutorServiceManager().newFixedThreadPool(this, uri,
                         newSize);
             }
         }
 
        private class BlockingExhangeEventHandler extends AbstractLifecycleAwareExchangeEventHandler {
             private CountDownLatch blockingLatch = new CountDownLatch(1);
 
             @Override
             public void onEvent(ExchangeEvent event, long sequence, boolean endOfBatch) throws Exception {
                 blockingLatch.await();
                 Exchange exchange = event.getExchange();
 
                 if (exchange.getProperty(DisruptorEndpoint.DISRUPTOR_IGNORE_EXCHANGE, false, boolean.class)) {
                     // Property was set and it was set to true, so don't process Exchange.
                     LOGGER.trace("Ignoring exchange {}", exchange);
                     return;
                 } else {
                     temporaryExchangeBuffer.offer(exchange);
                 }
             }
 
             public void unblock() {
                 blockingLatch.countDown();
             }
         }
     }
 
     /**
      * When a consumer is added or removed, we need to create a new Disruptor due to its static configuration. However, we
      * would like to reuse our thread pool executor and only add or remove the threads we need. On a reconfiguraion of the
      * Disruptor, we need to atomically swap the current RingBuffer with a new and fully configured one in order to keep
      * the producers operational without the risk of losing messages. Configuration of a RingBuffer by the Disruptor's
      * start method has a side effect that immediately starts execution of the event processors (consumers) on the
      * Executor passed as a constructor argument which is stored in a final field. In order to be able to delay actual
      * execution of the event processors until the event processors of the previous RingBuffer are done processing and the
      * thread pool executor has been resized to match the new consumer count, we delay their execution using this class.
      */
     public static class DelayedExecutor implements Executor {
 
         private static Queue<Runnable> delayedCommands = new LinkedList<Runnable>();
 
         @Override
         public void execute(Runnable command) {
             delayedCommands.offer(command);
         }
 
         public void executeDelayedCommands(Executor actualExecutor) {
             Runnable command;
 
             while ((command = delayedCommands.poll()) != null) {
                 actualExecutor.execute(command);
             }
         }
     }
 }
