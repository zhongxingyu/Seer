 package se.mockachino;
 
 import org.junit.Test;
 import se.mockachino.exceptions.UsageError;
 import se.mockachino.matchers.Matchers;
 import se.mockachino.order.OrderingContext;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotSame;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 import static se.mockachino.Settings.spyOn;
 import static se.mockachino.matchers.Matchers.any;
 import static se.mockachino.matchers.Matchers.anyInt;
 import static se.mockachino.matchers.Matchers.eq;
 import static se.mockachino.matchers.Matchers.notEq;
 import static se.mockachino.matchers.Matchers.notSame;
 import static se.mockachino.matchers.Matchers.regexp;
 import static se.mockachino.matchers.Matchers.same;
 import static se.mockachino.matchers.Matchers.type;
 
 public class MockachinoTest {
 	
 	@Test
 	public void testSimpleTest() {
 		List mock = Mockachino.mock(List.class);
 
 		Mockachino.stubReturn(123).on(mock).size();
 		Mockachino.stubReturn(100).on(mock).indexOf("Hello");
 		Mockachino.stubReturn(200).on(mock).indexOf("World");
 		Mockachino.stubReturn(300).on(mock).indexOf("Test");
 
 		assertEquals(123, mock.size());
 		assertEquals(100, mock.indexOf("Hello"));
 		assertEquals(200, mock.indexOf("World"));
 		assertEquals(300, mock.indexOf("Test"));
 
 		Mockachino.mock(List.class, spyOn(new ArrayList()));
 
 		foo(mock);
 
 		Mockachino.verifyOnce().on(mock).add(1, regexp("Hell.*"));
 		Mockachino.verifyNever().on(mock).add(1, regexp("Helll.*"));
 	}
 
 	private void foo(List mock) {
 		mock.add(1, "Hello");
 		mock.add(2, "World");
 	}
 
 	@Test
 	public void testSpy() {
 		List mock = Mockachino.mock(List.class, spyOn(new ArrayList()));
 
 		mock.add("Hello");
 		mock.add("World");
 
 		assertEquals(2, mock.size());
 		Mockachino.verifyOnce().on(mock).add("Hello");
 		Mockachino.verifyOnce().on(mock).add("World");
 		assertEquals(true, mock.remove("Hello"));
 		assertEquals(false, mock.remove("Foo"));
 		assertEquals(1, mock.size());
 	}
 
 	@Test
 	public void testException() {
 		try {
 			List mock = Mockachino.mock(List.class);
 			Mockachino.stubThrow(new ArrayIndexOutOfBoundsException("Hello")).on(mock).add(type(Object.class));
 			mock.add("");
 			fail("Should have thrown an exception");
 		} catch (Exception e) {
 			assertEquals("Hello", e.getMessage());
 		}
 	}
 
 	@Test
 	public void testSpy2() {
 		List list = new LinkedList();
 		List spy = Mockachino.mock(List.class, spyOn(list));
 
 		Mockachino.stubReturn("foo").on(spy).get(0);
 
 		assertEquals("foo", spy.get(0));
 	}
 
 	@Test
 	public void testInlineMock() {
 		List mockA = Mockachino.mock(List.class);
 		List mockB = Mockachino.mock(List.class);
 
 		Mockachino.stubReturn(mockB).on(mockA).get(anyInt());
 
 		assertSame(mockB, mockA.get(0));
 	}
 
 	@Test
 	public void testInlineMock2() {
 		List<List> mock = Mockachino.mock(List.class);
 
 		Mockachino.stubReturn(Mockachino.mock(List.class)).on(mock).get(anyInt());
 
 		List inlinedMock = mock.get(0);
 
 		// Do some stubbing on the inlined mock to match that it works.
 		Mockachino.stubReturn("Foo").on(inlinedMock).get(123);
 		assertEquals("Foo", inlinedMock.get(123));
 		assertEquals(null, inlinedMock.get(124));
 	}
 
 	@Test
 	public void testSpyString() {
 		String s = "Hello world";
 		CharSequence mock = Mockachino.mock(CharSequence.class, spyOn(s));
 
 		Mockachino.stubReturn("Jello").on(mock).subSequence(0, 5);
 
 		assertEquals("Hell", mock.subSequence(0, 4));
 		assertEquals("Jello", mock.subSequence(0, 5));
 
 		Mockachino.verifyExactly(1).on(mock).subSequence(0, 4);
 		Mockachino.verifyExactly(1).on(mock).subSequence(eq(0), eq(4));
 	}
 
 	@Test
 	public void testSpyMock() {
 		List mock = Mockachino.mock(List.class);
 		List spy = Mockachino.mock(List.class, spyOn(mock));
 
 		Mockachino.stubReturn("Hello").on(mock).get(123);
 		Mockachino.stubReturn("World").on(spy).get(123);
 		Mockachino.stubReturn("Foo").on(mock).get(456);
 
 		Mockachino.verifyNever().on(mock).get(type(int.class));
 		Mockachino.verifyNever().on(spy).get(type(int.class));
 
 		assertEquals("Hello", mock.get(123));
 		Mockachino.verifyOnce().on(mock).get(123);
 		Mockachino.verifyNever().on(spy).get(anyInt());
 
 		assertEquals("Foo", spy.get(456));
 		Mockachino.verifyOnce().on(mock).get(456);
 		Mockachino.verifyOnce().on(spy).get(456);
 
 		assertEquals("World", spy.get(123));
 		Mockachino.verifyOnce().on(spy).get(123);
 	}
 
 	@Test
 	public void testBadMatcher() {
 		try {
 			List mock = Mockachino.mock(List.class);
 			anyInt();
 			Mockachino.stubReturn("Hello").on(mock).get(0);
 
 			fail("Not supposed to accept bad match usage");
 		} catch (Exception e) {
 
 		}
 	}
 
 	@Test
 	public void testOverwriteStub() {
 		List mock = Mockachino.mock(List.class);
 		Mockachino.stubReturn("A").on(mock).get(0);
 		Mockachino.stubReturn("B").on(mock).get(0);
 		assertEquals("B", mock.get(0));
 
 	}
 
 	private static interface Foo {
 		String bar(String s);
 		int bar(int i);
 	}
 
 	@Test
 	public void testMatcherType() {
 		Foo mock = Mockachino.mock(Foo.class);
 		Mockachino.stubReturn("World").on(mock).bar("Hello");
 		Mockachino.stubReturn(123).on(mock).bar(anyInt());
 
 		assertEquals("World", mock.bar("Hello"));
 		assertEquals(123, mock.bar(789));
 
 		Mockachino.verifyOnce().on(mock).bar("Hello");
 		Mockachino.verifyOnce().on(mock).bar(type(String.class));
 		Mockachino.verifyOnce().on(mock).bar(eq("Hello"));
 		Mockachino.verifyOnce().on(mock).bar(same("Hello"));
 		Mockachino.verifyOnce().on(mock).bar((String) notSame(null));
 		Mockachino.verifyOnce().on(mock).bar(Matchers.<String>notEq(null));
 
 		Mockachino.verifyOnce().on(mock).bar(789);
 		Mockachino.verifyOnce().on(mock).bar(type(Integer.class));
 		Mockachino.verifyOnce().on(mock).bar(eq("Hello"));
 		Mockachino.verifyOnce().on(mock).bar(same("Hello"));
 		Mockachino.verifyOnce().on(mock).bar((String) notSame(null));
 		Mockachino.verifyOnce().on(mock).bar((String) notEq(null));
 
 		assertEquals(null, mock.bar("A"));
 	}
 
 	@Test
 	public void testVerifyInOrderDuplicate() {
 		List mock = Mockachino.mock(List.class);
 		mock.add("Hello");
 		mock.add("World");
 		mock.add("Hello");
 
 		OrderingContext order = Mockachino.newOrdering();
 		order.verify().on(mock).add("Hello");
 		order.verify().on(mock).add("World");
 		order.verify().on(mock).add("Hello");
 	}
 
 	@Test
 	public void testAnswer() {
 		List mock = Mockachino.mock(List.class);
 		Mockachino.stubAnswer(new CallHandler() {
 			@Override
 			public Object invoke(Object self, MethodCall call) {
 				Object obj = call.getArguments()[0];
 				return obj.toString() + obj;
 			}
 		}).on(mock).get(anyInt());
 
 		for (int i = 0; i < 100; i++) {
 			String s = "" + i;
 			String s2 = s + s;
 			assertEquals(s2, mock.get(i));
 		}
 		OrderingContext order = Mockachino.newOrdering();
 		for (int i = 0; i < 100; i++) {
 			order.verify().on(mock).get(i);
 		}
 
 		Mockachino.verifyExactly(100).on(mock).get(anyInt());
 
 	}
 
 	@Test
 	public void testNotAMock() {
 		try {
 			Throwable notAMock = new Throwable();
 			Mockachino.verifyExactly(1).on(notAMock).printStackTrace();
 			fail("Should fail");
 		} catch (UsageError e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testBadListener() {
 		List mock = Mockachino.mock(List.class);
 
 		final AtomicBoolean wasCalled = new AtomicBoolean();
 		Mockachino.observeWith(new CallHandler() {
 			@Override
 			public Object invoke(Object obj, MethodCall call) {
 				wasCalled.set(true);
 				throw new RuntimeException("Never shown");
 			}
 		}).on(mock).add(any(Object.class));
 
 		assertFalse(wasCalled.get());
 		mock.add("Hello world");
 		assertTrue(wasCalled.get());
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage1() {
 		List mock = Mockachino.mock(List.class);
 		Mockachino.observeWith(null).on(mock);
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage2() {
 		List mock = Mockachino.mock(List.class);
 		Mockachino.stubThrow(null).on(mock);
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage3() {
 		List mock = Mockachino.mock(List.class);
 		Mockachino.stubAnswer(null).on(mock);
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage4() {
 		Mockachino.newOrdering().verify().on(null);
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage5() {
 		Mockachino.mock(null);
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage6() {
 		Mockachino.spy(List.class, spyOn(null));
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage7() {
 		Mockachino.mock(List.class, null);
 	}
 
 	@Test(expected = UsageError.class)
 	public void testBadUsage8() {
 		Mockachino.getData("Hello world");
 	}
 
 	@Test
 	public void testReset() {
 		List mock = Mockachino.mock(List.class);
 		mock.add("Hello");
 		mock.remove("Hello");
 		mock.add("World");
 
 		Mockachino.verifyExactly(2).on(mock).add(any(Object.class));
 		Mockachino.getData(mock).resetCalls();
 		Mockachino.verifyExactly(0).on(mock).add(any(Object.class));
 	}
 
 	@Test
 	public void testEquals() {
 		MockContext context = new MockContext();
 		List mock1 = context.mock(List.class);
 		List mock2 = context.mock(List.class);
 
 		assertEquals("Mock:List:1", mock1.toString());
 		assertEquals("Mock:List:2", mock2.toString());
 		assertTrue(mock1.hashCode() != 0);
 		assertTrue(mock2.hashCode() != 0);
 		assertNotSame(mock1.hashCode(), mock2.hashCode());
 		assertTrue(mock1.equals(mock1));
 		assertFalse(mock1.equals(mock2));
 		assertFalse(mock1.equals(null));
 	}
 }
 
