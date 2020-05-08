 package org.agilewiki.pamailbox;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.agilewiki.pactor.Actor;
 import org.agilewiki.pactor.ExceptionHandler;
 import org.agilewiki.pactor.Mailbox;
 import org.agilewiki.pactor.MailboxFactory;
 import org.agilewiki.pactor.ResponseProcessor;
 import org.agilewiki.pactor._Request;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MailboxImpl implements Mailbox, Runnable, MessageSource {
 
     private static Logger LOG = LoggerFactory.getLogger(MailboxImpl.class);
 
     private final _MailboxFactory mailboxFactory;
     private final MessageQueue inbox;
     private final AtomicBoolean running = new AtomicBoolean();
     private final boolean commandeeringDisabled; //todo: disable commandeering when true
     private final boolean messageBuffering;
     private final Runnable messageProcessor;
 
     /** Send buffer */
     private Map<MessageSource, List<Message>> sendBuffer;
 
     private ExceptionHandler exceptionHandler;
     private Message currentMessage;
 
     /** messageQueue can be null to use the default queue implementation. */
     public MailboxImpl(final boolean _mayBlock,
             final boolean _disableMessageBuffering,
             final Runnable _messageProcessor, final _MailboxFactory factory,
             final MessageQueue messageQueue) {
         commandeeringDisabled = _mayBlock;
         messageBuffering = !_disableMessageBuffering;
         messageProcessor = _messageProcessor;
         running.set(messageProcessor != null);
         this.mailboxFactory = factory;
         this.inbox = messageQueue;
     }
 
     @Override
     public final boolean isEmpty() {
         return !inbox.isNonEmpty();
     }
 
     /**
      * does nothing until message buffering is implemented.
      * @throws Exception
      */
     @Override
     public final void flush() throws Exception {
         if (sendBuffer != null) {
             final Iterator<Entry<MessageSource, List<Message>>> iter = sendBuffer
                     .entrySet().iterator();
             while (iter.hasNext()) {
                 final Entry<MessageSource, List<Message>> entry = iter.next();
                 iter.remove();
                 final MessageSource target = entry.getKey();
                 final List<Message> messages = entry.getValue();
                 target.addUnbufferedMessages(messages);
             }
         }
     }
 
     @Override
     public final <A extends Actor> void signal(final _Request<Void, A> request,
             final A targetActor) throws Exception {
         final Message message = inbox.createMessage(null, targetActor, null,
                 request, null, EventResponseProcessor.SINGLETON);
         // No source mean never local
         addMessage(null, message, false);
     }
 
     /**
      * Same as signal(Request) until buffered message are implemented.
      */
     @Override
     public final <A extends Actor> void signal(final _Request<Void, A> request,
             final Mailbox source, final A targetActor) throws Exception {
         final MailboxImpl sourceMailbox = (MailboxImpl) source;
         if (!sourceMailbox.running.get())
             throw new IllegalStateException(
                     "A valid source mailbox can not be idle");
         //todo Buffer events the same way send buffers requests.
         final Message message = inbox.createMessage(sourceMailbox, targetActor,
                 sourceMailbox.currentMessage, request,
                 sourceMailbox.exceptionHandler,
                 EventResponseProcessor.SINGLETON);
         addMessage(sourceMailbox, message, this == source);
     }
 
     @Override
     public final <E, A extends Actor> void send(final _Request<E, A> request,
             final Mailbox source, final A targetActor,
             final ResponseProcessor<E> responseProcessor) throws Exception {
         final MailboxImpl sourceMailbox = (MailboxImpl) source;
         if (!sourceMailbox.running.get())
             throw new IllegalStateException(
                     "A valid source mailbox can not be idle");
         final Message message = inbox.createMessage(sourceMailbox, targetActor,
                 sourceMailbox.currentMessage, request,
                 sourceMailbox.exceptionHandler, responseProcessor);
         addMessage(sourceMailbox, message, this == source);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public final <E, A extends Actor> E call(final _Request<E, A> request,
             final A targetActor) throws Exception {
         final Caller caller = new Caller();
         final Message message = inbox.createMessage(caller, targetActor, null,
                 request, null,
                 (ResponseProcessor<E>) DummyResponseProcessor.SINGLETON);
         // Using a Caller means never local
         // TODO what if another actor with the same mailbox is called by accident?
         // Don't we get a deadlock?
         addMessage(null, message, false);
         return (E) caller.call();
     }
 
     @Override
     public final ExceptionHandler setExceptionHandler(
             final ExceptionHandler handler) {
         if (!running.get())
             throw new IllegalStateException(
                     "Attempt to set an exception handler on an idle mailbox");
         final ExceptionHandler rv = this.exceptionHandler;
         this.exceptionHandler = handler;
         return rv;
     }
 
     private void addMessage(final MailboxImpl sourceMailbox,
             final Message message, final boolean local) throws Exception {
         // sourceMailbox is either null, or running ...
         if ((sourceMailbox == null) || local
                 || !sourceMailbox.buffer(message, this)) {
             addUnbufferedMessage(message, local);
         }
     }
 
     /** Adds a message to the queue. */
     private void addUnbufferedMessage(final Message message, final boolean local)
             throws Exception {
         inbox.offer(local, message);
         afterAdd();
     }
 
     /** Adds messages to the queue. */
     @Override
     public void addUnbufferedMessages(final Iterable<Message> messages)
             throws Exception {
         inbox.offer(messages);
         afterAdd();
     }
 
     /** Should be called after adding some message(s) to the queue. */
     private void afterAdd() throws Exception {
         if (running.compareAndSet(false, true)) {
             if (inbox.isNonEmpty())
                 mailboxFactory.submit(this);
             else
                 running.set(false);
         } else if (messageProcessor != null)
             messageProcessor.run();
     }
 
     /**
      * Returns true, if the message could be buffered before sending.
      * @param message Message to send-buffer
      * @return true, if buffered
      */
     @Override
     public boolean buffer(final Message message, final MessageSource target) {
         if (messageBuffering) {
             List<Message> buffer;
             if (sendBuffer == null) {
                 sendBuffer = new HashMap<MessageSource, List<Message>>();
                 buffer = null;
             } else {
                 buffer = sendBuffer.get(target);
             }
             if (buffer == null) {
                 buffer = new ArrayList<Message>();
                 sendBuffer.put(target, buffer);
             }
             buffer.add(message);
         }
         return false;
     }
 
     @Override
     public final void run() {
         if (messageProcessor != null)
             while (true) {
                 final Message message = inbox.poll();
                 if (message == null)
                    return;
                 if (message.isResponsePending())
                     processRequestMessage(message);
                 else
                     processResponseMessage(message);
             }
         else
             while (true) {
                 final Message message = inbox.poll();
                 if (message == null) {
                     running.set(false);
                     // If inbox.isNonEmpty() was ever to throw an Exception,
                     // we should still be in a consistent state, since there
                     // was no unprocessed message, and running was set to false.
                     if (inbox.isNonEmpty()) {
                         if (!running.compareAndSet(false, true))
                             return;
                         continue;
                     }
                 }
                 if (message.isResponsePending())
                     processRequestMessage(message);
                 else
                     processResponseMessage(message);
             }
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private void processRequestMessage(final Message message) {
         exceptionHandler = null; //NOPMD
         currentMessage = message;
         final _Request<?, Actor> request = message.getRequest();
         try {
             request.processRequest(message.getTargetActor(),
                     new ResponseProcessor() {
                         @Override
                         public void processResponse(final Object response)
                                 throws Exception {
                             if (!message.isResponsePending())
                                 return;
                             message.setResponse(response);
                             if (message.getResponseProcessor() != EventResponseProcessor.SINGLETON) {
                                 message.getMessageSource().incomingResponse(
                                         message, MailboxImpl.this);
                             } else if (response instanceof Throwable) {
                                 LOG.warn("Uncaught throwable",
                                         (Throwable) response);
                             }
                         }
                     });
         } catch (final Throwable t) {
             processThrowable(t);
         }
     }
 
     private void processThrowable(final Throwable t) {
         if (!currentMessage.isResponsePending())
             return;
         final Message message = currentMessage;
         if (exceptionHandler != null) {
             try {
                 exceptionHandler.processException(t);
             } catch (final Throwable u) {
                 LOG.error("Exception handler unable to process throwable "
                         + exceptionHandler.getClass().getName(), t);
                 if (!(message.getResponseProcessor() instanceof EventResponseProcessor)) {
                     if (!message.isResponsePending())
                         return;
                     currentMessage.setResponse(u);
                     message.getMessageSource().incomingResponse(message,
                             MailboxImpl.this);
                 } else {
                     LOG.error("Thrown by exception handler and uncaught "
                             + exceptionHandler.getClass().getName(), t);
                 }
             }
         } else {
             if (!message.isResponsePending())
                 return;
             currentMessage.setResponse(t);
             if (!(message.getResponseProcessor() instanceof EventResponseProcessor))
                 message.getMessageSource().incomingResponse(message,
                         MailboxImpl.this);
             else {
                 LOG.warn("Uncaught throwable", t);
             }
         }
     }
 
     @SuppressWarnings("unchecked")
     private void processResponseMessage(final Message message) {
         final Object response = message.getResponse();
         exceptionHandler = message.getSourceExceptionHandler();
         currentMessage = message.getOldMessage();
         if (response instanceof Throwable) {
             processThrowable((Throwable) response);
             return;
         }
         @SuppressWarnings("rawtypes")
         final ResponseProcessor responseProcessor = message
                 .getResponseProcessor();
         try {
             responseProcessor.processResponse(response);
         } catch (final Throwable t) {
             processThrowable(t);
         }
     }
 
     @Override
     public final void incomingResponse(final Message message,
             final Mailbox responseSource) {
 //        final MailboxImpl sourceMailbox = (MailboxImpl) responseSource;
 //        if (!sourceMailbox.running.get())
 //            throw new IllegalStateException(
 //                    "A valid source mailbox can not be idle");
         try {
             // TODO Currently, we don't buffer responses
             addMessage(null, message, this == responseSource);
         } catch (final Throwable t) {
             LOG.error("unable to add response message", t);
         }
     }
 
     @Override
     public final MailboxFactory getMailboxFactory() {
         return mailboxFactory;
     }
 }
