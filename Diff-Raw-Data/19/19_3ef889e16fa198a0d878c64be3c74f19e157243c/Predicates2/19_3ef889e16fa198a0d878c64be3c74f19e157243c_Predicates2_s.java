 /* Copyright 2013 Jonatan Jönsson
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package se.softhouse.common.guavaextensions;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Predicates.alwaysTrue;
 
 import java.util.List;
 
 import javax.annotation.Nonnull;
 
 import se.softhouse.common.strings.Describables;
 
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 
 /**
  * Additional implementations of the {@link Predicate} interface, as a complement to
  * {@link Predicates}
  */
 public final class Predicates2
 {
 	private Predicates2()
 	{
 	}
 
 	/**
 	 * Returns a predicate that throws an {@link IllegalArgumentException} if any element in a list
 	 * doesn't return true for {@code elementLimiter.apply(Object)}. Useful for input validation
 	 * where you already have a {@link Predicate} for the element type and want to check a list of
 	 * such elements.
 	 * Without {@link #listPredicate(Predicate)} it would look something like:
 	 * 
 	 * <pre class="prettyprint">
 	 * <code class="language-java">
 	 * List&lt;Integer&gt; numbers = Arrays.asList(1, -1, -2);
 	 * Optional&lt;Integer&gt; invalidNumber = Iterables.tryFind(numbers, Predicates.not(ABOVE_ZERO));
 	 * if(invalidNumber.isPresent())
 	 *   throw new IllegalArgumentException("'" + invalidNumber.get() + "' is not " + ABOVE_ZERO);
 	 * </code>
 	 * </pre>
 	 * 
 	 * With {@link #listPredicate(Predicate)}:
 	 * 
 	 * <pre class="prettyprint">
 	 * <code class="language-java">
 	 * Predicates2.listPredicate(ABOVE_ZERO).apply(numbers);
 	 * </code>
 	 * </pre>
 	 * 
 	 * As this breaks the general contract of {@link Predicate#apply(Object)} it should be
 	 * considered useful for validating arguments only.
 	 * The detail message only mentions the first element that didn't return true.
 	 */
 	public static <E> Predicate<List<? extends E>> listPredicate(Predicate<E> elementLimiter)
 	{
 		checkNotNull(elementLimiter);
 		if(elementLimiter == alwaysTrue())
 			return alwaysTrue();
 		return new ListPredicate<E>(elementLimiter);
 	}
 
 	private static final class ListPredicate<E> implements Predicate<List<? extends E>>
 	{
 		private final Predicate<E> elementLimiter;
 
 		private ListPredicate(Predicate<E> elementLimiter)
 		{
 			this.elementLimiter = elementLimiter;
 		}
 
 		@Override
 		public boolean apply(@Nonnull List<? extends E> values)
 		{
 			for(E value : values)
 			{
 				if(!elementLimiter.apply(value))
 					throw Describables.illegalArgument(Describables.format("'%s' is not %s", value, elementLimiter));
 			}
 			return true;
 		}
 
 		@Override
 		public String toString()
 		{
 			return elementLimiter.toString();
 		}
 	}
 
 	/**
 	 * Works just like {@link Predicates#and(Predicate, Predicate)} except that if {@code first} is
 	 * {@link Predicates#alwaysTrue()} {@code second} is returned directly (or vice versa). This has
 	 * the potential to make {@link Predicate#toString()} look a bit nicer for the resulting
 	 * {@link Predicate}.
 	 */
	// A predicate for ? super T is also a Predicate for T
	@SuppressWarnings("unchecked")
	public static <T> Predicate<T> and(Predicate<? super T> first, Predicate<? super T> second)
 	{
 		if(first == alwaysTrue())
			return (Predicate<T>) checkNotNull(second);
 		else if(second == alwaysTrue())
			return (Predicate<T>) checkNotNull(first);
		return Predicates.and(first, second);
 	}
 }
