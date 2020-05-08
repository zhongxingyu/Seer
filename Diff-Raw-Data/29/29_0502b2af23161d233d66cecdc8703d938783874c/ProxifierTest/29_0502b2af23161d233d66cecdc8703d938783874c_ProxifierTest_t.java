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
		proxifier.proxify("net.vidageek.tellme.MyObject");
 	}
 
 	private	@Mock MessageQueue queue;
 
 	@Before
 	public void setUp() throws Exception {
 		MessageQueue.setDefault(queue);
 	}
 
 	@Test
 	public void proxifyAnnotatedMethodsToGenerateMessagesWhenTheyAreCalled() throws Throwable {
		// MUST be created here, otherwise the class is loaded before we replace its methods
		MyObject myObject = new MyObject();
 		myObject.annotatedMethod();
 
 		Method annotatedMethod = new Mirror().on(MyObject.class).reflect().method("annotatedMethod").withoutArgs();
 		verify(queue).addMessage(argThat(isAMessageTo(myObject).calling(annotatedMethod)));
 	}
 
 	@Test
 	public void doesNotProxifyNotAnnotatedMethods() throws Throwable {
		MyObject myObject = new MyObject();
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
