 package se.mockachino;
 
 import org.junit.Test;
 import se.mockachino.exceptions.VerificationError;
import se.mockachino.matchers.Matchers;
 import se.mockachino.matchers.matcher.Matcher;
 
 import java.io.DataOutput;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 import static se.mockachino.matchers.Matchers.and;
 import static se.mockachino.matchers.Matchers.any;
 import static se.mockachino.matchers.Matchers.anyBoolean;
 import static se.mockachino.matchers.Matchers.anyByte;
 import static se.mockachino.matchers.Matchers.anyDouble;
 import static se.mockachino.matchers.Matchers.anyFloat;
 import static se.mockachino.matchers.Matchers.anyInt;
 import static se.mockachino.matchers.Matchers.anyLong;
 import static se.mockachino.matchers.Matchers.anyShort;
 import static se.mockachino.matchers.Matchers.eq;
 import static se.mockachino.matchers.Matchers.mEq;
 import static se.mockachino.matchers.Matchers.mType;
 import static se.mockachino.matchers.Matchers.match;
 import static se.mockachino.matchers.Matchers.matchEq;
 import static se.mockachino.matchers.Matchers.notEq;
 import static se.mockachino.matchers.Matchers.or;
 import static se.mockachino.matchers.Matchers.type;
 
 public class MatchersTest {
 	@Test
 	public void testInt() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeInt(123);
 		Mockachino.verifyOnce().on(mock).writeInt(123);
 		Mockachino.verifyOnce().on(mock).writeInt(anyInt());
 	}
 
 	@Test
 	public void testLong() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeLong(123);
 		Mockachino.verifyOnce().on(mock).writeLong(123);
 		Mockachino.verifyOnce().on(mock).writeLong(anyLong());
 	}
 
 	@Test
 	public void testDouble() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeDouble(123.0);
 		Mockachino.verifyOnce().on(mock).writeDouble(123.0);
 		Mockachino.verifyOnce().on(mock).writeDouble(anyDouble());
 	}
 
 	@Test
 	public void testFloat() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeFloat(123.0f);
 		Mockachino.verifyOnce().on(mock).writeFloat(123.0f);
 		Mockachino.verifyOnce().on(mock).writeFloat(anyFloat());
 	}
 
 	@Test
 	public void testShort() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeShort(123);
 		Mockachino.verifyOnce().on(mock).writeShort(123);
 		Mockachino.verifyOnce().on(mock).writeShort(anyShort());
 	}
 
 	@Test
 	public void testByte() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeByte(123);
 		Mockachino.verifyOnce().on(mock).writeByte(123);
 		Mockachino.verifyOnce().on(mock).writeByte(anyByte());
 	}
 
 	@Test
 	public void testBoolean() throws IOException {
 		DataOutput mock = Mockachino.mock(DataOutput.class);
 		mock.writeBoolean(true);
 		Mockachino.verifyOnce().on(mock).writeBoolean(true);
 		Mockachino.verifyOnce().on(mock).writeBoolean(anyBoolean());
 	}
 
 	@Test
 	public void testComplex() {
 		Comparator mock = Mockachino.mock(Comparator.class);
 		mock.compare("Hello", "World");
 		mock.compare("Foo", "Bar");
 		mock.compare("Foo", null);
 
 		Mockachino.verifyOnce().on(mock).compare("Hello", "World");
 		Mockachino.verifyNever().on(mock).compare("Hello", "Worldz");
 		Mockachino.verifyNever().on(mock).compare("Helloz", "World");
 		Mockachino.verifyExactly(2).on(mock).compare(type(Object.class), type(Object.class));
 		Mockachino.verifyExactly(3).on(mock).compare(any(Object.class), any(Object.class));
 		Mockachino.verifyOnce().on(mock).compare("Foo", null);
 		Mockachino.verifyOnce().on(mock).compare("Foo", type(Object.class));
 		Mockachino.verifyExactly(2).on(mock).compare(type(String.class), type(String.class));
 		Mockachino.verifyExactly(2).on(mock).compare(type(String.class), type(Object.class));
 		Mockachino.verifyOnce().on(mock).compare(type(String.class), eq(null));
 		Mockachino.verifyOnce().on(mock).compare(notEq(null), eq(null));
 		
 	}
 
 	@Test
 	public void testAnd() {
 		Comparator mock = Mockachino.mock(Comparator.class);
 		mock.compare("Hello", "World");
 		mock.compare("Foo", "Bar");
 		mock.compare("Foo", null);
 
 		Mockachino.verifyExactly(2).on(mock).compare(
 				and(
 						matchEq("Foo", Object.class), mType(Object.class)),
 				or(matchEq("Bar", Object.class), mEq(null)));
 	}
 
 	@Test
 	public void testCustom() {
 		List mock = Mockachino.mock(List.class);
 		Matcher<Integer> myMatcher = new Matcher<Integer>() {
 			@Override
 			public boolean matches(Integer value) {
 				return true;
 			}
 
 			@Override
 			public Class<Integer> getType() {
 				return Integer.class;
 			}
 		};
 
 		Mockachino.stubReturn("Foo").on(mock).get(match(myMatcher));
 		assertEquals("Foo", mock.get(1));
 	}
 
 	@Test
 	public void testArrayEquals() {
 		List<String[]> mock = Mockachino.mock(List.class);
 
 		mock.add(new String[]{"Hello", "World"});
 		Mockachino.verifyExactly(1).on(mock).add(new String[]{"Hello", "World"});
 	}
 
 	@Test
 	public void testArrayEqualsFail() {
 		List<String[]> mock = Mockachino.mock(List.class);
 
 		mock.add(new String[]{"Hello", "World"});
 		Mockachino.verifyExactly(0).on(mock).add(new String[]{"Hello", "World2"});
 	}
 
 	@Test
 	public void testArrayEqualsFail2() {
 		List<Object> mock = Mockachino.mock(List.class);
 
 		mock.add(new String[]{"Hello", "World"});
 		Mockachino.verifyExactly(0).on(mock).add(Arrays.asList("Hello", "World"));
 	}
 
 	@Test
 	public void testComplexEquals() {
 		List<String[][]> mock = Mockachino.mock(List.class);
 
 		mock.add(new String[][]{new String[]{"Hello"}, new String[]{"World"}});
 		try {
 			Mockachino.verifyExactly(2).on(mock).add(new String[][]{new String[]{"Hello"}, new String[]{"World"}});
 			fail("Should fail");
 		} catch (VerificationError e) {
 			e.printStackTrace();
 		}
 	}
 
    private static interface MockInterface {
        void foo(long a);
    }

    @Test
    public void testMatcherConversion() {
        MockInterface mock = Mockachino.mock(MockInterface.class);
        mock.foo(123L);
        Mockachino.verifyOnce().on(mock).foo(Matchers.eq(123));
    }

 }
