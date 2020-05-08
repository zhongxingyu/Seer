 package org.jdryad.com.rabbitmq;
 
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 import com.google.common.util.concurrent.ThreadFactoryBuilder;
 
 import com.rabbitmq.client.Channel;
 import com.rabbitmq.client.Connection;
 import com.rabbitmq.client.ConnectionFactory;
 import com.rabbitmq.client.DefaultConsumer;
 import com.rabbitmq.client.Envelope;
 import com.rabbitmq.client.MessageProperties;
 import com.rabbitmq.client.AMQP.BasicProperties;
 
 import java.io.IOException;
 import java.util.concurrent.Executor;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.ThreadFactory;
 
 import org.jdryad.com.Communicator;
 import org.jdryad.com.HostID;
 import org.jdryad.com.Message;
 import org.jdryad.com.MessageMarshaller;
 import org.jdryad.com.MessageMarshallerFactory;
 import org.jdryad.com.MessageType;
 import org.jdryad.com.Reactor;
 import org.jdryad.common.Pair;
 
 /**
  * </p>
  * An implementation of <code>Communicator</code> that internally uses RabbitMQ
  * for implementing the communication protocol.
  *
  * A basic documentation for rabbit mq can be found in
  * <a href="http://www.rabbitmq.com/api-guide.html">APIGuide</a> and
  * <a href="http://www.infoq.com/articles/AMQP-RabbitMQ">InfoQ</code>
  *
  * </p>
  * The basic protocol is as follows.
  * <ul>
  * <li>1. We create a default exchange by name <b>jDAG</b>
  * <li>2. Every host will create a quque  and attach to the jDAG exchange with
  *        the binding key set to the host name
  * <li>3. If we want to send data to that host we will just publish to the
  *       exchange jDAG with the routing key set to host name.
  * </ul>
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public class RabbitMQCommunicator implements Communicator
 {
     private static final String CONST_FIXED_EXCHANGE_TYPE = "fixed";
 
     private final RabbitMQConfiguration myCOnfiguration;
 
     private final LockableChannel myChannel;
 
     private final MessageMarshallerFactory myMarshallerFasctory;
 
     private final ExecutorService myCommExecutor;
 
     private final Connection myActiveConnection;
 
     private final Multimap<MessageType, Pair<Reactor, Executor>>
         myMessageToReactorMap;
 
     /**
      * Wraps a <code>Reactor</code> so that it can be scheduled on a
      * Executor.
      */
     private static class ExecutableReactor implements Runnable
     {
         private final Reactor myReactor;
 
         private final Message myMessage;
 
         /**
          * CTOR
          */
         public ExecutableReactor(Message message, Reactor reactor)
         {
             myMessage = message;
             myReactor = reactor;
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void run()
         {
             myReactor.process(myMessage);
         }
     }
 
     /**
      * An implementation of <code>Consumer</code> that invokes appropriate
      * <code>Reactor</code> based on the incoming message type
      */
     private class ReactorConsumer extends DefaultConsumer
     {
         /**
          * CTOR
          */
         public ReactorConsumer(Channel channel)
         {
             super(channel);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void handleDelivery(String consumerTag, Envelope envelope,
                 BasicProperties properties, byte[] body) throws IOException
         {
             MessageMarshaller marshaller =
                 myMarshallerFasctory.makeMarshaller();
             Message m = marshaller.unmarshal(body);
 
             synchronized(myMessageToReactorMap) {
                 for (Pair<Reactor, Executor> reactorExecPair :
                         myMessageToReactorMap.get(m.getMessageType()))
                 {
                     ExecutableReactor reactor =
                         new ExecutableReactor(m, reactorExecPair.getFirst());
                     if (reactorExecPair.getSecond() != null) {
                         reactorExecPair.getSecond().execute(reactor);
                     }
                     else {
                         myCommExecutor.execute(reactor);
                     }
                 }
             }
         }
     }
 
     /**
      * A simple type that ensures that only a single thread access
      * the channel at a single time.
      */
     private static class LockableChannel
     {
         private final Semaphore myLock;
         private final Channel myChannel;
 
         /**
          * CTOR
          */
         public LockableChannel(Channel channel)
         {
             myChannel = channel;
             myLock = new Semaphore(1);
         }
 
         /**
          * Returns the value of channel
          */
         public Channel getRabbitMQChannel()
         {
             return myChannel;
         }
 
         /** Locks the channel for use */
         public void lock() throws InterruptedException
         {
             myLock.acquire();
         }
 
         /** UnLocks the channel after use */
         public void unlock() throws InterruptedException
         {
             myLock.release();
         }
     }
 
     /**
      * CTOR
      */
     public RabbitMQCommunicator(RabbitMQConfiguration config,
                                 MessageMarshallerFactory mmFactory)
     {
         myCOnfiguration = config;
         myMarshallerFasctory = mmFactory;
         ConnectionFactory factory = new ConnectionFactory();
         factory.setHost(config.getHost());
         factory.setPort(config.getPort());
         factory.setUsername(config.getUserName());
         factory.setPassword(config.getPassword());
         factory.setVirtualHost(config.getVirtualHost());
 
         myMessageToReactorMap =
             HashMultimap.<MessageType, Pair<Reactor, Executor>>create();
 
         ThreadFactoryBuilder tfBuilder = new ThreadFactoryBuilder();
         ThreadFactory namedFactory =
             tfBuilder.setNameFormat(RabbitMQCommunicator.class.getSimpleName())
                      .build();
         myCommExecutor =
             Executors.newSingleThreadExecutor(namedFactory);
 
         try {
             myActiveConnection = factory.newConnection();
             Channel c = myActiveConnection.createChannel();
             myChannel = new LockableChannel(c);
         }
         catch (IOException e) {
              throw new RuntimeException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void attachReactor(MessageType type, Reactor r)
     {
         synchronized (myMessageToReactorMap) {
             myMessageToReactorMap.put(type,
                                        new Pair<Reactor, Executor>(r, null));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void attachReactor(MessageType type, Reactor r, Executor e)
     {
         synchronized (myMessageToReactorMap) {
             myMessageToReactorMap.put(type,
                                        new Pair<Reactor, Executor>(r, e));
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void sendMessage(HostID host, Message m)
     {
         MessageMarshaller marshaller = myMarshallerFasctory.makeMarshaller();
         byte[] payload = marshaller.marshal(m);
 
         try {
             myChannel.lock();
             myChannel.getRabbitMQChannel().basicPublish(
                     myCOnfiguration.getGlobalExchange(), host.getIdentifier(),
                     MessageProperties.MINIMAL_PERSISTENT_BASIC, payload);
             myChannel.unlock();
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void start()
     {
 
         try {
             myChannel.lock();
             if (myCOnfiguration.shouldDeclareGlobalExchange()) {
                 // The global exchange should be durable.
                 myChannel.getRabbitMQChannel().exchangeDeclare(
                     myCOnfiguration.getGlobalExchange(),
                     CONST_FIXED_EXCHANGE_TYPE,
                     true);
             }
 
             myChannel.getRabbitMQChannel().queueDeclare(
                 myCOnfiguration.getHostName(),
                 true,
                 true,
                 false,
                 null);
 
             // Now attach the new quuqe with the default exchange and the
             // routing key is same as the quque name.
             myChannel.getRabbitMQChannel().queueBind(
                 myCOnfiguration.getHostName(),
                 myCOnfiguration.getGlobalExchange(),
                 myCOnfiguration.getHostName());
 
             myChannel.getRabbitMQChannel().basicConsume(myCOnfiguration.getHostName(),
                                                 new ReactorConsumer(
                                                     myChannel.getRabbitMQChannel()));
             myChannel.unlock();
         }
         catch (InterruptedException e) {
              throw new RuntimeException(e);
         }
         catch (IOException e) {
              throw new RuntimeException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void stop()
     {
         try {
             myChannel.lock();
             myActiveConnection.close();
             myChannel.unlock();
         }
         catch (InterruptedException e) {
              throw new RuntimeException(e);
         }
         catch (IOException e) {
              throw new RuntimeException(e);
         }
         myCommExecutor.shutdown();
     }

    /**
     * {@inheritDoc}
     */
    @Override
    public HostID getMyHostID()
    {
        return new HostID(myCOnfiguration.getHostName());
    }
 }
