 package pegasus.eventbus.amqp;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pegasus.eventbus.amqp.AmqpMessageBus.UnacceptedMessage;
 
 import pegasus.eventbus.client.Envelope;
 import pegasus.eventbus.client.EnvelopeHandler;
 import pegasus.eventbus.client.EventResult;
 
 /**
  * Watches a Queue for new messages on a background thread, calling the EnvelopeHandler when new messages arrive.
  * 
  * @author Ken Baltrinic (Berico Technologies)
  */
 class QueueListener implements Runnable {
 
     protected final Logger         LOG;
 
     private final AmqpEventManager amqpEventManager;
     private final String           queueName;
     private final String           threadName;
     private EnvelopeHandler        envelopeHandler;
 
     private volatile boolean       currentlyListening;
     private volatile boolean       continueListening;
 
     private Thread                 backgroundThread;
 
     /**
      * Start up an new Queue Listener bound on the supplied queue name, with the provided EnvelopeHander dealing with new messages.
      * 
      * @param queueName
      *            Name of the Queue to watch.
      * @param envelopeHandler
      *            EnvelopeHandler that deals with new messages.
      * @param amqpEventManager
      *            TODO
      */
     public QueueListener(AmqpEventManager amqpEventManager, String queueName, EnvelopeHandler envelopeHandler) {
 
         this.amqpEventManager = amqpEventManager;
         // Custom Logger for Each Queue Listener.
        //TODO: Need to add tests to assert that this logger name is always valid (i.e. queue names with . and any other illegal chars are correctly mangled.)
        LOG = LoggerFactory.getLogger(String.format("%s$>%s", this.getClass().getName(), queueName.replace('.', '_')));
 
         this.queueName = queueName;
         this.threadName = "Listener for queue: " + queueName;
         this.envelopeHandler = envelopeHandler;
     }
 
     /**
      * Begin listening for messages on the Queue.
      */
     public void beginListening() {
 
         LOG.debug("QueueListener commanded to start on a new thread.");
 
         if (backgroundThread != null)
             return;
 
         continueListening = true;
 
         backgroundThread = new Thread(this);
         backgroundThread.setName(threadName);
         backgroundThread.start();
     }
 
     /**
      * Executed on a separate thread.
      */
     @Override
     public void run() {
 
         LOG.info("Starting to listen on thread [{}].", Thread.currentThread().getName());
 
         currentlyListening = true;
         while (continueListening) {
 
             try {
                 UnacceptedMessage message;
                 synchronized (this) {
 
                     LOG.trace("Getting next message for queue [{}]", queueName);
 
                     // see not in StopListening() as to why we are
                     // synchronizing here.
                     message = this.amqpEventManager.getAmqpMessageBus().getNextMessageFrom(queueName);
                 }
                 if (message == null) {
 
                     LOG.debug("No messages received.  Waiting 50ms.");
 
                     try {
                         Thread.sleep(50);
 
                     } catch (InterruptedException e) {
 
                         LOG.debug("Thread [{}] interrupted in method AmqpEventManager$QueueListener.run().", threadName);
 
                         break;
                     } finally {
                         // ?
                     }
                     continue;
                 }
 
                 LOG.debug("Message received.");
 
                 EventResult result;
 
                 Envelope envelope = message.getEnvelope();
 
                 try {
 
                     LOG.trace("Handling envelope.");
 
                     result = envelopeHandler.handleEnvelope(envelope);
                 } catch (Exception e) {
 
                     result = EventResult.Failed;
 
                     String id;
 
                     try {
 
                         id = envelope.getId().toString();
 
                     } catch (Exception ee) {
 
                         id = "<message id not available>";
                     }
 
                     LOG.error("Envelope handler of type " + envelopeHandler.getClass().getCanonicalName() + " on queue " + queueName + " threw exception of type " + e.getClass().getCanonicalName()
                             + " handling message " + id, e);
                 }
 
                 LOG.trace("Determining how to handle EventResult [{}]", result);
 
                 switch (result) {
                     case Handled:
 
                         LOG.trace("Accepting Message [{}]", message.getAcceptanceToken());
 
                         this.amqpEventManager.getAmqpMessageBus().acceptMessage(message);
 
                         break;
                     case Failed:
 
                         LOG.trace("Rejecting Message [{}]", message.getAcceptanceToken());
 
                         this.amqpEventManager.getAmqpMessageBus().rejectMessage(message, false);
 
                         break;
                     case Retry:
 
                         LOG.trace("Retrying Message [{}]", message.getAcceptanceToken());
 
                         this.amqpEventManager.getAmqpMessageBus().rejectMessage(message, true);
 
                         break;
                 }
             } catch (Exception e) {
 
                 LOG.error("Envelope handler of type " + envelopeHandler.getClass().getCanonicalName() + " on queue " + queueName + " threw exception of type " + e.getClass().getCanonicalName()
                         + " while retrieving next message.");
             }
         }
         currentlyListening = false;
         backgroundThread = null;
 
         LOG.info("Stopped listening on thread [" + threadName + "].");
     }
 
     /**
      * Command the QueueListener to stop listening on the queue, thereby stopping the background thread.
      */
     public void StopListening() {
         continueListening = false;
 
         LOG.debug("Interrupting thread [" + threadName + "].");
 
         // This is a bit screwy but the
         // RpcTest.getResponseToShouldReceiveResponsesToResposnesToSentEvent
         // test will
         // usually hang if interrupt() is called because the timing of the
         // test is such that the interrupt gets called
         // while the AMQP-client.channel.basicGet() is blocking because
         // getBasic apparently fails to handle the interrupt correctly.
         // Therefore we synchronize on the listener here and when calling
         // getNextMessageFrom so that we are sure never
         // to call interrupt while in the middle of a basicGet();
         synchronized (this) {
 
             if (backgroundThread == null) {
 
                 LOG.debug("backgroundThread was null for thread [" + threadName + "].");
 
             } else {
 
                 backgroundThread.interrupt();
             }
         }
 
     }
 
     /**
      * Is the QueueListener currently monitoring the Queue?
      * 
      * @return true is it is monitoring queue.
      */
     public boolean isCurrentlyListening() {
         return currentlyListening;
     }
 }
