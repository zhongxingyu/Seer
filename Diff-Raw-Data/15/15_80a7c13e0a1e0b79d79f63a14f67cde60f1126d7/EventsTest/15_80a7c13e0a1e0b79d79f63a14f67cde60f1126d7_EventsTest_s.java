 package ru.vmsoftware.events;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import ru.vmsoftware.events.annotations.Listener;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 /**
  * @author Vyacheslav Mayorov
  * @since 2013-29-04
  */
 public class EventsTest {
 
     private static final String EMITTER_ID = "abc";
 
     static class ListenerBase {
         boolean getInvokedAndReset() {
             boolean res = invoked;
             invoked = false;
             return res;
         }
 
         boolean invoked = false;
     }
 
     static abstract class ParentListener extends ListenerBase {
 
         @Listener(emitterClass = EventsTest.class, event = "A")
         void parentListener() {
             invoked = true;
         }
     }
 
     enum TestEnum {
         A
     }
 
     enum TestEnum2 {
         A
     }
 
    @Mock(answer = Answers.CALLS_REAL_METHODS)
     private DefaultEventManager manager;
 

     private EventManager restoreManager;
 
     @Before
     public void setUp() throws Exception {
         MockitoAnnotations.initMocks(this);
         restoreManager = Events.manager;
         Events.manager = manager;
     }
 
     @After
     public void tearDown() throws Exception {
         Events.manager = restoreManager;
     }
 
     @Test
     public void testInitShouldAddListenersFromParentClass() throws Exception {
        testListener(new ParentListener() {
        }, TestEnum.A, true);
     }
 
     @Test
     public void testInitShouldAddGlobalListenerForAnyEvent() throws Exception {
         testListener(new ListenerBase() {
             @Listener
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum2.A, true);
     }
 
     @Test
     public void testInitShouldAddGlobalListener() throws Exception {
         testListener(new ListenerBase() {
             @Listener(eventClass = TestEnum.class)
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum.A, true);
     }
 
     @Test
     public void testInitShouldAddListenerByField() throws Exception {
         testListener(new ListenerBase() {
             @Listener(field = "thisEmitter", eventClass = TestEnum.class)
             void listenerMethod() {
                 invoked = true;
             }
 
             Object thisEmitter = EventsTest.this;
         }, TestEnum.A, false);
     }
 
     @Test
     public void testInitShouldAddListenerByEmitterClass() throws Exception {
         testListener(new ListenerBase() {
             @Listener(emitterClass = EventsTest.class, eventClass = TestEnum.class)
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum.A, false);
     }
 
     @Test
     public void testInitShouldAddListenerBySourcePattern() throws Exception {
         testListener(new ListenerBase() {
             @Listener(emitterClass = EventsTest.class, eventClass = TestEnum.class)
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum.A, false);
     }
 
     @Test
     public void testInitShouldComposeEmitterTypeAndPattern() throws Exception {
         testListener(new ListenerBase() {
             @Listener(emitterClass = EventsTest.class, emitter = EMITTER_ID)
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum2.A, false);
     }
 
     @Test
     public void testInitShouldComposeEventTypeAndPattern() throws Exception {
         testListener(new ListenerBase() {
             @Listener(eventClass = TestEnum2.class, event = "A")
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum2.A, TestEnum.A, false);
     }
 
     private static class ContainerBase extends ListenerBase implements TagContainer<Object> {
         private ContainerBase(String tag, Object value) {
             this.tag = tag;
             this.value = value;
         }
 
         public Object getByTag(String tag) {
             return this.tag.equals(tag) ? value : null;
         }
 
         private String tag;
         private Object value;
     }
 
     @Test
     public void testInitShouldAddListenerByTag() throws Exception {
         testListener(new ContainerBase("abc", EventsTest.this) {
             @Listener(tag = "abc")
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum.A, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInitShouldThrowExceptionIfObjectDoesntImplementTagContainer() throws Exception {
         testListener(new ListenerBase() {
             @Listener(tag = "abc")
             void listenerMethod() {
                 invoked = true;
             }
         }, TestEnum.A, false);
     }
 
     @Test
     public void testInitShouldAddListenerFieldByTag() throws Exception {
         testListener(new ListenerBase() {
             @Listener(field = "container", tag = "abc")
             void listenerMethod() {
                 invoked = true;
             }
 
             ContainerBase container = new ContainerBase("abc", EventsTest.this);
         }, TestEnum.A, false);
     }
 
     @Test(expected = IllegalArgumentException.class)
     public void testInitShouldThrowExceptionIfFieldDoesntImplementTagContainer() throws Exception {
         testListener(new ListenerBase() {
             @Listener(field = "container", tag = "abc")
             void listenerMethod() {
                 invoked = true;
             }
 
             ListenerBase container = new ListenerBase();
         }, TestEnum.A, false);
     }
 
     @Test
     public void testEmitter() throws Exception {
         final Emitter emitter = Events.emitter(this);
         Mockito.verify(manager).emitter(this);
     }
 
     private void testListener(ListenerBase listener, Object event, boolean isGlobal) {
         testListener(listener, event, event, isGlobal);
     }
 
     private void testListener(ListenerBase listener, Object event, Object event2, boolean isGlobal) {
         Events.init(listener);
         Events.emit(this, event);
         assertThat(listener.getInvokedAndReset()).isTrue();
         Events.emit(EMITTER_ID, event2);
         assertThat(listener.getInvokedAndReset()).isEqualTo(isGlobal);
     }
 
     public String toString() {
         return EMITTER_ID;
     }
 
 
 }
