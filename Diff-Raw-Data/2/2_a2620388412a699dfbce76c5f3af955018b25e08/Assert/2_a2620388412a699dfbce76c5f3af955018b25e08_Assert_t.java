 /******************************************************************************
  * Copyright 2010 Alexander Kiel                                              *
  *                                                                            *
  * Licensed under the Apache License, Version 2.0 (the "License");            *
  * you may not use this file except in compliance with the License.           *
  * You may obtain a copy of the License at                                    *
  *                                                                            *
  *     http://www.apache.org/licenses/LICENSE-2.0                             *
  *                                                                            *
  * Unless required by applicable law or agreed to in writing, software        *
  * distributed under the License is distributed on an "AS IS" BASIS,          *
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.   *
  * See the License for the specific language governing permissions and        *
  * limitations under the License.                                             *
  ******************************************************************************/
 
 package net.alexanderkiel.junit;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 /**
  * @author Alexander Kiel
  * @version $Id$
  */
 public class Assert {
 
 	private Assert() {
 	}
 
 	/**
 	 * Asserts that the equals and hashCode methods of the class under test behave normally.
 	 * <p/>
 	 * This method tests six things: <ul> <li>that equals is reflexive, <li>that a comparison with {@code null} returns
 	 * {@code false}, <li>that a comparison with another class returns {@code false}, <li>that equals is symmetric,
 	 * <li>that hashCode of the two equal instances returns the same value and <li>that hashCode of the two different
 	 * instances returns different values </ul>
 	 * <p/>
 	 * This method needs three non-null instances of the class under test, whereas the first two should be equal and the
 	 * last should not be equal to any of the first two.
 	 *
 	 * @param foo1 the first of two equal instances
 	 * @param foo2 the second of two equal instances
 	 * @param bar  an instance not equals to any of the first two
 	 * @param <T>  the type of the class under test
 	 * @see Object#equals(Object)
 	 * @see Object#hashCode()
 	 */
 	@SuppressWarnings({"ObjectEqualsNull", "SimplifiableJUnitAssertion", "ProhibitedExceptionCaught"})
 	public static <T> void assertBasicEqualsAndHashCodeBehavior(T foo1, T foo2, T bar) {
 		assertNotNull("foo1 is not null", foo1);
 		assertNotNull("foo1 is not null", foo2);
 		assertNotNull("bar is not null", bar);
 
 		assertTrue("equals is reflexive", foo1.equals(foo1));
 
 		try {
 			assertFalse("not equals null", foo1.equals(null));
 		} catch (NullPointerException e) {
 			fail("equals doesn't throw NullPointerExceptions");
 		}
 
 		assertFalse("not equals instances other classes", foo1.equals(new Object()));
 
 		assertTrue("equals is symmetric", foo1.equals(foo2));
 		assertTrue("equals is symmetric", foo2.equals(foo1));
 
 		assertFalse("equals is symmetric", foo2.equals(bar));
 		assertFalse("equals is symmetric", bar.equals(foo2));
 
 		assertTrue("hash code of equal objects is equal too", foo1.hashCode() == foo2.hashCode());
 		assertTrue("hash code of unequal objects is not equal", foo2.hashCode() != bar.hashCode());
 	}
 
	public static void assertExceptionMessageEquals(String expectedMessage, Throwable throwable) {
 		assertEquals("exception message", expectedMessage, throwable.getMessage());
 	}
 }
