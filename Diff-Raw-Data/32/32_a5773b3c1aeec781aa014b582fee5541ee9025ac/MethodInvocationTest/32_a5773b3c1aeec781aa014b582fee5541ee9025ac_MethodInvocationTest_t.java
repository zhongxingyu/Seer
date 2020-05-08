 package org.kalibro.core.concurrent;
 
 import static org.junit.Assert.*;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.kalibro.KalibroTestCase;
 
 public class MethodInvocationTest extends KalibroTestCase {
 
 	private static final String STRING = "My string";
 
 	private Method method;
 	private MethodInvocation invocation;
 
 	@Before
 	public void setUp() throws Exception {
 		method = String.class.getMethod("substring", int.class);
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
	public void shouldInvoke() throws Throwable {
 		createInvocation(3);
 		invocation.invoke();
 		assertEquals("string", invocation.getResult());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
 	public void shouldRetrieveIfInvocationWasDone() {
 		createInvocation(4);
 		assertFalse(invocation.done());
 		invocation.invoke();
 		assertTrue(invocation.done());
 	}
 
 	@Test(timeout = UNIT_TIMEOUT)
	public void shouldThrowInvocationErrorWhenRetrievingResult() {
 		createInvocation(-1);
 		invocation.invoke();
		checkException(new Task() {

			@Override
			protected void perform() throws Throwable {
				invocation.getResult();
			}
		}, InvocationTargetException.class);
 	}
 
 	private void createInvocation(int index) {
 		invocation = new MethodInvocation(STRING, method, index);
 	}
 }
