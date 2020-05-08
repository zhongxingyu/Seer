 /*
  * Copyright (c) 2013 David Hartveld
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 package com.hartveld.queryable.optional;
 
 import static org.hamcrest.Matchers.instanceOf;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.sameInstance;
 import static org.junit.Assert.assertThat;
 
 import com.hartveld.queryable.interactive.Enumerable;
 import com.hartveld.queryable.interactive.Enumerables;
 import com.hartveld.queryable.reactive.Observable;
 import java.util.NoSuchElementException;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.junit.Before;
 import org.junit.Test;
 
 public class SomeTest {
 
 	private Object input;
 	private Object expected;
 
 	private Some<Object> some;
 
 	@Before
 	public void setUp() {
 		this.input = new Object();
 		this.expected = new Object();
 
 		this.some = new Some<>(this.input);
 	}
 
 	@Test
 	public void testThatHasValueReturnsTrue() {
 		assertThat(
 				"hasValue() incorrectly returns false",
 				some.hasValue(), is(true)
 		);
 	}
 
 	@Test
 	public void testThatGetValueThrowsNoSuchElementException() {
 		assertThat(
 				"getValue() did not return expected instance",
 				some.getValue(), is(sameInstance(input))
 		);
 	}
 
 	@Test
 	public void testThatFlatMapReturnsThisInstance() {
 		final Optional<Object> result = some.flatMap(x -> Optional.some(expected));
 
 		assertThatGetValueReturnsExpectedValue(result);
 	}
 
 	@Test
 	public void testThatFlatMapThrowsExceptionForNonOptionalReturnValue() {
 		boolean exceptionWasThrown = false;
 
 		try {
 			some.flatMap(x -> Enumerables.empty());
 		} catch (final IllegalArgumentException e) {
 			exceptionWasThrown = true;
 		}
 
 		assertThat(
 				"No IllegalArgumentException was thrown",
 				exceptionWasThrown, is(true)
 		);
 	}
 
 	@Test
 	public void testThatMapReturnsThisInstance() {
 		final Optional<Object> result = some.map(x -> expected);
 
 		assertThatGetValueReturnsExpectedValue(result);
 	}
 
 	@Test
 	public void testThatReduceReturnsResultOfAccumulator() {
 		final Object id = new Object();
 
 		final Optional<Object> result = some.reduce(id, (x, y) -> expected);
 
 		assertThatGetValueReturnsExpectedValue(result);
 	}
 
 	@Test
 	public void testThatReduceCallsAccumulatorWithIdParameter() {
 		final Object id = new Object();
 
 		final Optional<Object> result = some.reduce(
 				id,
 				(x, y) -> {
 					assertThat(x, is(sameInstance(id)));
 					return new Object();
 				}
 		);
 	}
 
 	@Test
 	public void testThatReduceCallsAccumulatorWithInpuParameter() {
 		final Object id = new Object();
 
 		final Optional<Object> result = some.reduce(
 				id,
 				(x, y) -> {
 					assertThat(y, is(sameInstance(input)));
 					return new Object();
 				}
 		);
 	}
 
 	@Test
 	public void testThatFilterReturnsNoneIfPredicateTestReturnsFalse() {
 		final Optional<Object> result = some.filter(x -> false);
 
 		assertThat(
 				"result should be instance of None",
 				result, is(instanceOf(None.class))
 		);
 	}
 
 	@Test
 	public void testThatFilterReturnsThisInstanceIfPredicateTestReturnsTrue() {
 		final Optional<Object> result = some.filter(x -> true);
 
 		assertThatResultIsThisInstance(result);
 	}
 
 	@Test
 	public void testThatPeekReturnsThisInstance() {
 		final Optional<Object> result = some.peek(x -> { });
 
 		assertThatResultIsThisInstance(result);
 	}
 
 	@Test
	public void testThatAsEnumerableReturnsEmptyEnumerable() {
 		final Enumerable<Object> enumerable = some.asEnumerable();
 
 		int count = 0;
 
 		for (final Object o : enumerable) {
 			count++;
 		}
 
 		assertThat(
 				"count should be 1",
 				count, is(1)
 		);
 	}
 
 	@Test
	public void testThatAsObservableReturnsEmptyObservable() {
 		final Observable<Object> observable = some.asObservable();
 
 		final AtomicInteger count = new AtomicInteger();
 
 		observable.subscribe(o -> count.incrementAndGet());
 
 		assertThat(
 				"count should be 1",
 				count.get(), is(1)
 		);
 	}
 
 	private void assertThatGetValueReturnsExpectedValue(final Optional<Object> result) throws NoSuchElementException {
 		assertThat(
 				"getValue() did not return expected instance",
 				result.getValue(), is(sameInstance(expected))
 		);
 	}
 
 	private void assertThatResultIsThisInstance(final Optional<Object> result) {
 		assertThat(
 				"result should be 'this' instance",
 				result, is(sameInstance(some))
 		);
 	}
 
 }
