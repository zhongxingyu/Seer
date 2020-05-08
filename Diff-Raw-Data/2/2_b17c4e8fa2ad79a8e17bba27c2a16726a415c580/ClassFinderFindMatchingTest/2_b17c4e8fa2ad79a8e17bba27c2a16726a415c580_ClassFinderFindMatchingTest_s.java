 package org.dsf;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import static org.junit.Assert.*;
 
 @RunWith(JUnit4.class)
 public class ClassFinderFindMatchingTest {
 	final String encoding = ClassFinder.SUPPORTED_ENCODING;
 	
 	private ClassFinder classFinder;
 	
 	@Test
 	public void emptyPattern() {
 		Collection<String> result = classFinder.findMatching("");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(0, arr.length);
 	}
 	
 	@Test
 	public void whitespacePattern() {
 		Collection<String> result = classFinder.findMatching(" ");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(0, arr.length);
 	}
 	
 	@Test(expected = IllegalArgumentException.class)
 	public void nullPattern() {
 		classFinder.findMatching(null);
 	}
 	
 	@Test
 	public void oneCapitalChar() {
 		Collection<String> result = classFinder.findMatching("T");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(3, arr.length);
 		assertEquals("TestClass", arr[0]);
 		assertEquals("TestClassSecond", arr[1]);
 		assertEquals("TestClassThird", arr[2]);
 	}
 	
 	@Test
 	public void twoCapitalChars() {
 		Collection<String> result = classFinder.findMatching("TC");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(3, arr.length);
 		assertEquals("TestClass", arr[0]);
 		assertEquals("TestClassSecond", arr[1]);
 		assertEquals("TestClassThird", arr[2]);
 	}
 	
 	@Test
 	public void specifyingChars() {
 		Collection<String> result = classFinder.findMatching("TeCla");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(3, arr.length);
 		assertEquals("TestClass", arr[0]);
 		assertEquals("TestClassSecond", arr[1]);
 		assertEquals("TestClassThird", arr[2]);
 	}
 	
 	@Test
 	public void specifyingCharsNoMatch() {
 		Collection<String> result = classFinder.findMatching("TeCzz");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(0, arr.length);
 	}
 	
 	@Test
 	public void trailingWhitespace() {
 		Collection<String> result = classFinder.findMatching("TC ");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(1, arr.length);
 		assertEquals("TestClass", arr[0]);
 	}
 	
 	@Test
 	public void leadingWildcard() {
 		Collection<String> result = classFinder.findMatching("*B");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(2, arr.length);
 		assertEquals("FooBar", arr[0]);
 		assertEquals("FooBarBaz", arr[1]);
 	}
 	
 	@Test
 	public void trailingWildcard() {
 		Collection<String> result = classFinder.findMatching("TC*");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(3, arr.length);
 		assertEquals("TestClass", arr[0]);
 		assertEquals("TestClassSecond", arr[1]);
 		assertEquals("TestClassThird", arr[2]);
 	}
 	
 	@Test
 	public void middleWildcard() {
 		Collection<String> result = classFinder.findMatching("M*P");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(2, arr.length);
 		assertEquals("MyPrecious", arr[0]);
 		assertEquals("MySuperPrecious", arr[1]);
 	}
 	
 	@Test
 	public void middleWildcardSpecifyingChars() {
 		Collection<String> result = classFinder.findMatching("M*P*o");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(2, arr.length);
 		assertEquals("MyPrecious", arr[0]);
 		assertEquals("MySuperPrecious", arr[1]);
 	}
 	
 	@Test
 	public void ordering() throws Exception {
		InputStream in = new ByteArrayInputStream("zy.MyClass\nmy.MyClass2\nmy.MyClassa".getBytes(encoding));
 		ClassFinder finder = new ClassFinder(in);
 		Collection<String> result = finder.findMatching("MC");
 		String[] arr = result.toArray(new String[result.size()]);
 		assertEquals(3, arr.length);
 		assertEquals("MyClass", arr[0]);
 		assertEquals("MyClass2", arr[1]);
 		assertEquals("MyClassa", arr[2]);	
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		try (InputStream testStream = this.getClass().getClassLoader().getResourceAsStream("classNames1.txt")) {	
 			classFinder = new ClassFinder(testStream);	
 		} 
 	}
 }
