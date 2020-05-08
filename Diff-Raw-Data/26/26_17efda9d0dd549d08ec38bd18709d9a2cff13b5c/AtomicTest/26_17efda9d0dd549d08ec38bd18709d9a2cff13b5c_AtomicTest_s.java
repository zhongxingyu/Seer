 package org.agilewiki.pautil.atomic;
 
 import junit.framework.TestCase;
 import org.agilewiki.pactor.*;
 import org.agilewiki.pamailbox.DefaultMailboxFactoryImpl;
 
 public class AtomicTest extends TestCase {
    public void test() throws Exception {
         MailboxFactory mailboxFactory = new DefaultMailboxFactoryImpl();
         try {
            startReq(mailboxFactory.createAsyncMailbox()).pend();
         } finally {
             mailboxFactory.close();
         }
     }
 
    Request<Void> startReq(final Mailbox _mailbox) {
         return new RequestBase<Void>(_mailbox) {
             @Override
             public void processRequest(final ResponseProcessor<Void> _rp) throws Exception {
                 AtomicProcessorBase atomicProcessor = new FifoProcessor();
                 atomicProcessor.initialize(getMailbox().createAsyncMailbox());
                 _rp.processResponse(null);
             }
         };
     }
 }
 
 class AtomA implements Atomic<String> {
     public String msg;
 
     @Override
     public void process(final AtomicProcessorBase _processor, final ResponseProcessor<String> _rp)
             throws Exception {
         _rp.processResponse(msg);
     }
 
     @Override
     public boolean isExpired() {
         return false;
     }
 }
