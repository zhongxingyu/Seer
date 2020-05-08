 package com.cellent.springockito.spy;
 
import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
import com.cellent.spring.utils.junit_spring.impl.MockitoApplicationContext;
 import com.cellent.spring.utils.junit_spring.support.ToItselfDelegatingBean;
 
 /**
  * Demonstration Test to show the spy-Functionality of Mockito - it is really well documented in its Javadoc, so doing this
  * is really as easy as knowing the keyword "spy".
  * 
  * @author bjoern
  * 
  */
 public class MockitoSpyDemonstrationTest {
 
 	/**
 	 * Class under Test.
 	 */
 	private ToItselfDelegatingBean toItselfDelegatingBean;
 
 	/**
 	 * Init class under Test.
 	 */
 	@Before
 	public void init() {
 		ToItselfDelegatingBean tmp = new ToItselfDelegatingBean();
 		toItselfDelegatingBean = Mockito.spy(tmp);
 	}
 
 	/**
 	 * Test that the delegating Method is really overwritten by a Mockito Spy.
 	 * 
 	 * The Blocks "setup", "when" and "then" are meant in the semantics of the Groovy testing framework spock. I think
 	 * keeping this structure makes tests better readable and understandable.
 	 */
 	@Test
 	public void test() {
 		String expectedString;
 		{ // setup
 			expectedString = "Test123";
 			Mockito.doReturn(expectedString).when(toItselfDelegatingBean).myDelegatingMethod();
 			// The next String cannot work, because the real method is called then leading to an exception
 			// Mockitos Javadoc gives a good picture in this concern.
 			// Mockito.when(toItselfDelegatingBean.myDelegatingMethod()).thenReturn(expectedString);
 		}
 		String result;
 		{ // when
 			result = toItselfDelegatingBean.getResultString();
 		}
 		{ // then
 			assertEquals(expectedString, result);
 		}
 	}
 
	/**
	 * There already exists a Method {@link MockitoApplicationContext#postProcessBean(Object)}. Problem is, that binds
	 * Autowiring and afterPropertiesSet together. In another project, afterPropertiesSet did a lot of things. I wanted to
	 * test only parts of these and I wanted to use a Mockito spy as well es mocked delegates in that part (which was
	 * itself a method). So I had to admit that I need to be more fine-grained in here.
	 */
	@Test
	public void testCreateSpy() {
		assertTrue(toItselfDelegatingBean instanceof ToItselfDelegatingBean);
		assertFalse(toItselfDelegatingBean.getClass().equals(ToItselfDelegatingBean.class));
	}
 }
