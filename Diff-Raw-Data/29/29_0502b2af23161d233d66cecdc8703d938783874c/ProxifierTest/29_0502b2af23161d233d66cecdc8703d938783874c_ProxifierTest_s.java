 package net.vidageek.tellme;
 
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.verify;
 
 import java.lang.reflect.Method;
 
 import net.vidageek.mirror.dsl.Mirror;
 import net.vidageek.tellme.messaging.MessageQueue;
 import net.vidageek.tellme.testing.MessageMatcher;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 
 @RunWith(MockitoJUnitRunner.class)
 public class ProxifierTest {
 
 	@BeforeClass
 	public static void proxifyMyObject() {
 		Proxifier proxifier = new Proxifier();
		proxifier.proxify("net.vidageek.tellme.MyObject", "target/test-classes");
 	}
 
 	private	@Mock MessageQueue queue;
	private MyObject myObject;
 
 	@Before
 	public void setUp() throws Exception {
 		MessageQueue.setDefault(queue);
		myObject = new MyObject();
 	}
 
	// FIXME make this pass on the first run (ClassLoader problem)
 	@Test
 	public void proxifyAnnotatedMethodsToGenerateMessagesWhenTheyAreCalled() throws Throwable {
 		myObject.annotatedMethod();
 
 		Method annotatedMethod = new Mirror().on(MyObject.class).reflect().method("annotatedMethod").withoutArgs();
 		verify(queue).addMessage(argThat(isAMessageTo(myObject).calling(annotatedMethod)));
 	}
 
 	@Test
 	public void doesNotProxifyNotAnnotatedMethods() throws Throwable {
 		myObject.notAnnotatedMethod();
 
 		Method notAnnotatedMethod = new Mirror().on(MyObject.class).reflect().method("notAnnotatedMethod").withoutArgs();
 		verify(queue, never()).addMessage(argThat(isAMessageTo(myObject).calling(notAnnotatedMethod)));
 	}
 
 	private MessageMatcher isAMessageTo(Object destination) {
 		return new MessageMatcher(destination);
 	}
 }
 
 class MyObject {
 	@Async
 	public void annotatedMethod() {}
 	public void notAnnotatedMethod() {}
 }
