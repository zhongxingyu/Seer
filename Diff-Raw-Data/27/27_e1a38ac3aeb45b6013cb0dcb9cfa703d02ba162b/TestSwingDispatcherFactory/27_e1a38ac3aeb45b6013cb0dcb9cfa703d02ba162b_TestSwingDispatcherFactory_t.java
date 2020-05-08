 // Copyright (C) 2005 - 2008 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console.swingui;
 
 import junit.framework.TestCase;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 
 import javax.swing.SwingUtilities;
 
 import net.grinder.console.common.ErrorHandler;
 import net.grinder.testutility.RandomStubFactory;
 
 
 /**
  * Unit tests for {@link SwingDispatcherFactoryImplementation}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestSwingDispatcherFactory extends TestCase {
 
   private Runnable m_voidRunnable = new Runnable() { public void run() {} };
 
   public void testPropertyChangeListenerDispatch() throws Exception {
     final MyPropertyChangeListener listener = new MyPropertyChangeListener();
 
     final RandomStubFactory errorHandlerStubFactory =
       new RandomStubFactory(ErrorHandler.class);
     final ErrorHandler errorHandler =
       (ErrorHandler)errorHandlerStubFactory.getStub();
 
     final SwingDispatcherFactory swingDispatcherFactory =
       new SwingDispatcherFactoryImplementation(errorHandler);
 
     final PropertyChangeListener swingDispatchedListener =
       (PropertyChangeListener)swingDispatcherFactory.create(listener);
 
     final PropertyChangeEvent event =
       new PropertyChangeEvent(this, "my property", "before", "after");
 
     swingDispatchedListener.propertyChange(event);
 
     // Wait for a dummy event to be processed by the swing event
     // queue.
     SwingUtilities.invokeAndWait(m_voidRunnable);
 
     assertEquals(event, listener.getLastEvent());
     errorHandlerStubFactory.assertNoMoreCalls();
 
     final RuntimeException e = new RuntimeException("Problem");
     listener.setThrowException(e);
 
     swingDispatchedListener.propertyChange(event);
     SwingUtilities.invokeAndWait(m_voidRunnable);
 
     assertNull(listener.getLastEvent());
 
     errorHandlerStubFactory.assertSuccess("handleException", e);
     errorHandlerStubFactory.assertNoMoreCalls();
   }
 
   public void testDelegateWithMultipleInterfaces() throws Exception {
     final RandomStubFactory errorHandlerStubFactory =
       new RandomStubFactory(ErrorHandler.class);
     final ErrorHandler errorHandler =
       (ErrorHandler)errorHandlerStubFactory.getStub();
 
     final SwingDispatcherFactory swingDispatcherFactory =
       new SwingDispatcherFactoryImplementation(errorHandler);
 
     final Object proxy = swingDispatcherFactory.create(new FooImpl());
 
     assertTrue(proxy instanceof Foo);
     assertTrue(proxy instanceof Serializable);
   }
 
   public void testDelegateWithDuplicateInterfaces() throws Exception {
     final RandomStubFactory errorHandlerStubFactory =
       new RandomStubFactory(ErrorHandler.class);
     final ErrorHandler errorHandler =
       (ErrorHandler)errorHandlerStubFactory.getStub();
 
     final SwingDispatcherFactory swingDispatcherFactory =
       new SwingDispatcherFactoryImplementation(errorHandler);
 
     final Object proxy = swingDispatcherFactory.create(new FooFoo());
 
     assertTrue(proxy instanceof Foo);
     assertTrue(proxy instanceof PropertyChangeListener);
    assertFalse(proxy instanceof Bah);
   }
 
   private static final class MyPropertyChangeListener
     implements PropertyChangeListener {
 
     private PropertyChangeEvent m_propertyChangeEvent;
     private RuntimeException m_nextException;
 
     public void propertyChange(PropertyChangeEvent event) {
       if (m_nextException != null) {
         try {
           throw m_nextException;
         }
         finally {
           m_nextException = null;
         }
       }
 
       m_propertyChangeEvent = event;
     }
 
     public PropertyChangeEvent getLastEvent() {
       try {
         return m_propertyChangeEvent;
       }
       finally {
         m_propertyChangeEvent = null;
       }
     }
 
     public void setThrowException(RuntimeException e) {
       m_nextException = e;
     }
   }
 
   public interface Foo extends PropertyChangeListener { }
 
   public static class FooBase { }
 
   public static class FooImpl extends FooBase implements Foo, Serializable {
     public void propertyChange(PropertyChangeEvent e) { }
   }
 
  private interface Bah { }

   public static abstract class Foo2 implements Foo { }
 
  public static class FooFoo extends Foo2 implements Foo, Bah {
     public void propertyChange(PropertyChangeEvent e) { }
   }
 }
 
