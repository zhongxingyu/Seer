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
 
 import static org.junit.Assert.*;
 import net.sf.staccatocommons.defs.Applicable3;
 import net.sf.staccatocommons.defs.Thunk;
 import net.sf.staccatocommons.testing.junit.jmock.JUnit4MockObjectTestCase;
 
 import org.jmock.Expectations;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * 
  * Test for {@link AbstractFunction3}
  * 
  * @author flbulgarelli
  * 
  */
 public class Function3UnitTest extends JUnit4MockObjectTestCase {
 
 	AbstractFunction3<Integer, String, Boolean, Character> function;
 	Applicable3<Integer, String, Boolean, Character> applicable;
 
 	/** Instantiates both function and applicable */
 	@Before
 	public void setup() {
 		applicable = mock(Applicable3.class);
 		function = new AbstractFunction3<Integer, String, Boolean, Character>() {
 			public Character apply(Integer arg1, String arg2, Boolean arg3) {
 				return applicable.apply(arg1, arg2, arg3);
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
 				exactly(3).of(applicable).apply(5, "foo", true);
 				will(returnValue('a'));
 			}
 		});
 		assertEquals('a', (char) function.apply(5, "foo", true));
 		assertEquals('a', (char) function.apply(5, "foo").apply(true));
 		assertEquals('a', (char) function.apply(5).apply("foo", true));
 	}
 
 	/**
 	 * Test method for
 	 * {@link net.sf.staccatocommons.lang.function.AbstractFunction3#toString()}.
 	 */
 	@Test
 	public void testToString() {
 		assertEquals("Function3", function.toString());
 	}
 
 	/** Test for {@link AbstractFunction3#delayed(Object, Object, Object)} */
 	@Test
 	public void testLazy() throws Exception {
 		Thunk<Character> lazy = function.delayed(5, "foo", true);
 		checking(new Expectations() {
 			{
				one(applicable).apply(5, "foo", true);
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
 		AbstractFunction3<Integer, Integer, Integer, Integer> add = new AbstractFunction3<Integer, Integer, Integer, Integer>() {
 			public Integer apply(Integer arg1, Integer arg2, Integer arg3) {
 				return arg1 + arg2 + arg3;
 			}
 		};
 		assertNull(add.nullSafe().apply(null, 1, 2));
 		assertNull(add.nullSafe().apply(1, null, 2));
 		assertNull(add.nullSafe().apply(1, 6, null));
 		assertEquals((Integer) 25, add.apply(10, 5, 10));
 	}
 }
