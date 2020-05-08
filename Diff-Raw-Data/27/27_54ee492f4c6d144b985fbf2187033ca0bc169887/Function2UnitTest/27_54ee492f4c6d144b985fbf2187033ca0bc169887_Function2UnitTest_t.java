 /*
  Copyright (c) 2010, The Staccato-Commons Team
 
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; version 3 of the License.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.
  */
 package net.sf.staccatocommons.lang.function;
 
 import static net.sf.staccatocommons.lang.number.NumberTypes.*;
 import static org.junit.Assert.*;
 import net.sf.staccatocommons.defs.Applicable2;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.defs.function.Function2;
 import net.sf.staccatocommons.lang.Compare;
 import net.sf.staccatocommons.testing.junit.jmock.JUnit4MockObjectTestCase;
 
 import org.jmock.Expectations;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * 
  * Test for {@link AbstractFunction2}
  * 
  * @author flbulgarelli
  */
 public class Function2UnitTest extends JUnit4MockObjectTestCase {
 
 	AbstractFunction2<Integer, String, Character> function;
 	Applicable2<Integer, String, Character> applicable;
 
 	/** Instantiates both function and applicable */
 	@Before
 	public void setup() {
 		applicable = mock(Applicable2.class);
 		function = new AbstractFunction2<Integer, String, Character>() {
 			public Character apply(Integer arg1, String arg2) {
 				return applicable.apply(arg1, arg2);
 			}
 		};
 	}
 
 	/**
 	 * Test method for
 	 * {@link net.sf.staccatocommons.lang.function.AbstractFunction3#apply(java.lang.Object, java.lang.Object)}
 	 * and {@link AbstractFunction3#apply(Object, Object, Object)} .
 	 */
 	@Test
 	public void testApply() {
 		checking(new Expectations() {
 			{
 				exactly(2).of(applicable).apply(5, "foo");
 				will(returnValue('a'));
 			}
 		});
 		assertEquals('a', (char) function.apply(5).apply("foo"));
 		assertEquals('a', (char) function.apply(5, "foo"));
 	}
 
 	/**
 	 * Test method for
 	 * {@link net.sf.staccatocommons.lang.function.AbstractFunction3#toString()}.
 	 */
 	@Test
 	public void testToString() {
 		assertEquals("Function2", function.toString());
 	}
 
 	/** Test method for {@link AbstractFunction2#flip()} */
 	@Test
 	public void testFlip() throws Exception {
 		checking(new Expectations() {
 			{
 				exactly(2).of(applicable).apply(5, "hello");
 				will(returnValue('a'));
 			}
 		});
 		assertEquals(function.flip().apply("hello", 5), function.apply(5, "hello"));
 	}
 
 	/** Test for {@link AbstractFunction2#delayed(Object, Object)} */
 	@Test
 	public void testLazy() throws Exception {
 		Thunk<Character> lazy = function.delayed(5, "foo");
 		checking(new Expectations() {
 			{
				exactly(3).of(applicable).apply(5, "foo");
 				will(returnValue('a'));
 			}
 		});
 		assertEquals('a', (char) lazy.value());
 		assertEquals('a', (char) lazy.value());
 		assertEquals('a', (char) lazy.value());
 	}
 
 	/**
 	 * Test for {@link AbstractFunction#nullSafe()}
 	 */
 	@Test
 	public void testNullSafe() throws Exception {
 		Function2<Integer, Integer, Integer> add = Compare.max(integer());
 		assertNull(add.nullSafe().apply(null, 1));
 		assertNull(add.nullSafe().apply(1, null));
 		assertEquals((Integer) 10, add.apply(10, 5));
 	}
 }
