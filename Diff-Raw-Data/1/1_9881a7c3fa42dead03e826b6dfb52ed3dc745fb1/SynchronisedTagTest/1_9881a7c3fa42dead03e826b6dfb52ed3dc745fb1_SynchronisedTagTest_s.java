 package afc.jsp.tag;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.servlet.jsp.JspContext;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.JspFragment;
 
 import junit.framework.TestCase;
 
 public class SynchronisedTagTest extends TestCase
 {
     private Object monitor;
     private SynchronisedTag tag;
     private StringJspWriter out;
     private MockJspContext ctx;
     
     @Override
     protected void setUp()
     {
         tag = new SynchronisedTag();
         out = new StringJspWriter();
         ctx = new MockJspContext(out);
         tag.setJspContext(ctx);
         monitor = new Object();
         tag.setMonitor(monitor);
     }
     
     /**
      * <p>The tag body and a parallel thread (t) created both try to synchronise upon tag's monitor. What should happen:
      * <ol>
      *  <li>t is started; t starts waiting for a notification that is issued by the tag body</li>
      *  <li>the tag body enters into the synchronised context upon monitor (implicitly)</li>
      *  <li>the tag body notifies t that the body is being executed</li>
      *  <li>in parallel:
      *      a) the tag body sets <tt>val</tt> to 1
      *      b) the thread tries to lock on monitor and then sets <tt>val</tt> to 2</li>
      *  <li>the test waits for both get finished</li>
      * </ol>
      * </p>
      * 
      * <p>
      * <ul>
      *  <li>if the tag body is executed in the synchronised context upon monitor then 4b) happens before 4a) and
      *      <tt>val</tt> must be equal to 2</li>
      *  <li>if the tag body is NOT executed in the synchronised context upon monitor then both 4a->4b and 4b->4a are possible,
      *      but since the tag body sleeps for a while after notifying t it is likely that 4b happens before 4a and
      *      <tt>val</tt> is equal to 1</li>
      * </ul>
      * </p>
      */
     public void testSynchronisedCall() throws Exception
     {
         final AtomicBoolean inTagBody = new AtomicBoolean();
         final AtomicInteger val = new AtomicInteger();
         
         final Thread t = new Thread() {
             @Override
             public void run()
             {
                 try {
                     while (!inTagBody.get()) {
                         synchronized (this) {
                             this.wait();
                         }
                     }
                     synchronized (monitor) {
                         // this should happen after tag body has been executed
                         val.set(2);
                     }
                 }
                 catch (InterruptedException ex) {
                     return;
                 }
             }
         };
         
         t.setDaemon(true); // just in case
         t.start();
         
         final JspFragment body = new JspFragment() {
             @Override
             public JspContext getJspContext()
             {
                 return ctx;
             }
 
             @Override
             public void invoke(final Writer out) throws JspException, IOException
             {
                 inTagBody.set(true);
                 synchronized (t) {
                     t.notify();
                 }
                 try {
                     Thread.sleep(50);
                 }
                 catch (InterruptedException ex) {
                     Thread.currentThread().interrupt(); // should never happen in normal case
                     return;
                 }
                 val.set(1);
             }
             
         };
         
         tag.setJspBody(body);
         
         try {
             tag.doTag();
             
             t.join(1000);
         }
         finally {
             if (t.isAlive()) {
                 t.interrupt();
                 fail();
             }
         }
         
        assertFalse(testFailed.get());
         assertEquals(2, val.get());
     }
 }
