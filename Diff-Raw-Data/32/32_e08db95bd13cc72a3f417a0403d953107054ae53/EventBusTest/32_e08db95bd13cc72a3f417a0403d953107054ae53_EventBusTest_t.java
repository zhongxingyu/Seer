 package geomatico.events;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import geomatico.events.ExceptionEvent.Severity;
 import junit.framework.TestCase;
 
 import org.mockito.Mockito;
 
 public class EventBusTest extends TestCase {
 
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		EventBus.getInstance().removeAllHandlers();
	}

 	public void testSimpleEvent() {
 		ExceptionEventHandler handler = mock(ExceptionEventHandler.class);
 		Exception exception = mock(Exception.class);
 		ExceptionEvent event = new ExceptionEvent("", exception);
 
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler);
 		EventBus.getInstance().fireEvent(event);
 		verify(handler).exception(Severity.ERROR, "", exception);
 	}
 
 	public void testMultipleSubscriptionOneDispatch() {
 		ExceptionEventHandler handler = mock(ExceptionEventHandler.class);
 		Exception exception = mock(Exception.class);
 		ExceptionEvent event = new ExceptionEvent("", exception);
 
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler);
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler);
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler);
 
 		EventBus.getInstance().fireEvent(event);
 		verify(handler).exception(Severity.ERROR, "", exception);
 	}
 
 	public void testSeveralEvents() {
 		ExceptionEventHandler handler = mock(ExceptionEventHandler.class);
 		Exception exception = mock(Exception.class);
 		ExceptionEvent event = new ExceptionEvent("", exception);
 
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler);
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler);
 
 		EventBus.getInstance().fireEvent(event);
 		EventBus.getInstance().fireEvent(event);
 		EventBus.getInstance().fireEvent(event);
 		verify(handler, times(3)).exception(Severity.ERROR, "", exception);
 	}
 
 	public synchronized void testGarbageCollectedHandler() throws Exception {
 		ExceptionEventHandler handler1 = mock(ExceptionEventHandler.class);
 		ExceptionEventHandler handler2 = mock(ExceptionEventHandler.class);
 		Exception exception = mock(Exception.class);
 		ExceptionEvent event = new ExceptionEvent("", exception);
 
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler1);
 		EventBus.getInstance().addHandler(ExceptionEvent.class, handler2);
 
 		EventBus.getInstance().fireEvent(event);
 		verify(handler1).exception(Severity.ERROR, "", exception);
 		verify(handler2).exception(Severity.ERROR, "", exception);
 
 		Mockito.doThrow(RuntimeException.class)
 				.when(handler2)
 				.exception(any(Severity.class), anyString(),
 						any(Throwable.class));
 		handler2 = null;
 		Runtime.getRuntime().gc();
 
 		EventBus.getInstance().fireEvent(event);
 	}

	public void testFireNoHandled() throws Exception {
		// Just check that nothing happens, no exception is thrown
		EventBus.getInstance().fireEvent(
				new ExceptionEvent("", mock(Exception.class)));
	}
 }
