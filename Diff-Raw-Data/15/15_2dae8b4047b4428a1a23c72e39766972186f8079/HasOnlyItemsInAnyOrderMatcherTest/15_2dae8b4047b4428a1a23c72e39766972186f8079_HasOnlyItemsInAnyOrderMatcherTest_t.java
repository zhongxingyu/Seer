 package org.javafunk.matchbox.implementations;
 
 import org.hamcrest.Matcher;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.javafunk.funk.Literals.iterable;
 import static org.javafunk.funk.Literals.iterableWith;
 import static org.javafunk.matchbox.IterableMatchers.hasOnlyItemsInAnyOrder;
 import static org.javafunk.matchbox.MatcherMatchers.matches;
 import static org.javafunk.matchbox.MatcherMatchers.mismatchesSampleWithMessage;
 
 public class HasOnlyItemsInAnyOrderMatcherTest {
     @Test
     public void shouldReportOnMismatchedItemsInHasOnlyItemsInAnyOrder() {
         // Given
         Iterable<Integer> actual = iterableWith(1, 2, 5, 6);
         Iterable<Integer> expected = iterableWith(1, 2, 3, 4);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, mismatchesSampleWithMessage(actual, "got <1>, <2>, <5>, <6>\n" +
                 "expected but didn't get <3>, <4>\n" +
                 "got but didn't expect <5>, <6>"));
     }
 
     @Test
     public void shouldReportOnTooManyItemsInHasOnlyItemsInAnyOrder() {
         // Given
         Iterable<Integer> actual = iterableWith(1, 2, 5, 6);
         Iterable<Integer> expected = iterableWith(1, 2);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, mismatchesSampleWithMessage(actual, "got <1>, <2>, <5>, <6>\n" +
                 "got collection with size <4> rather than <2>\n" +
                 "got but didn't expect <5>, <6>"));
     }
 
     @Test
     public void shouldReportOnTooFewItemsInHasOnlyItemsInAnyOrder() {
         // Given
         Iterable<Integer> actual = iterableWith(1, 2);
         Iterable<Integer> expected = iterableWith(1, 2, 4, 5);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, mismatchesSampleWithMessage(actual, "got <1>, <2>\n" +
                 "got collection with size <2> rather than <4>\n" +
                 "expected but didn't get <4>, <5>"));
     }
 
     @Test
     public void shouldMatchIdenticalIterablesInHasOnlyItemsInAnyOrder() {
         // Given
         Iterable<Integer> actual = iterableWith(1, 2, 3);
         Iterable<Integer> expected = iterableWith(1, 2, 3);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, matches(actual));
     }
 
     @Test
     public void shouldMatchIterablesDifferingOnlyInOrderInHasOnlyItemsInAnyOrder() {
         // Given
         Iterable<Integer> actual = iterableWith(1, 2, 3);
         Iterable<Integer> expected = iterableWith(1, 3, 2);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, matches(actual));
     }
 
     @Test
     public void shouldMismatchIterablesDifferingOnlyInItemCountInHasOnlyItemsInAnyOrder() {
         // Given
         Iterable<Integer> actual = iterableWith(1, 2, 2, 3);
         Iterable<Integer> expected = iterableWith(1, 3, 3, 2);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, mismatchesSampleWithMessage(actual, "got <1>, <2>, <2>, <3>\n" +
                 "expected but didn't get <3>\n" +
                 "got but didn't expect <2>"));
     }
 
 
     @Test
     public void shouldReportWhenActualItemsIsEmpty() {
         // Given
         Iterable<Integer> actual = iterable();
         Iterable<Integer> expected = iterableWith(1, 2, 3, 4);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, mismatchesSampleWithMessage(actual, "got empty collection"));
     }
 
     @Test
     public void shouldReportWhenActualItemsIsNull() {
         // Given
         Iterable<Integer> actual = null;
         Iterable<Integer> expected = iterableWith(1, 2, 3, 4);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, mismatchesSampleWithMessage(actual, "was null"));
     }
 
     @Test
     public void shouldReportWhenExpectedItemsIsEmpty() {
         // Given
         Iterable<Integer> expected = iterable();
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
        assertThat(matcher.toString(), is("empty collection"));
     }
 
     @Test
     public void shouldMatchWhenExpectedAndActualItemsAreEmpty() {
         // Given
         Iterable<Integer> actual = iterable();
         Iterable<Integer> expected = iterable();
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher, matches(actual));
     }
 
     @Test
     public void shouldReportItemsFoundInExpectedIterable() {
         // Given
         Iterable<Integer> expected = iterableWith(1, 2, 3, 4);
 
         // When
         Matcher<Iterable<Integer>> matcher = hasOnlyItemsInAnyOrder(expected);
 
         // Then
         assertThat(matcher.toString(), is("Collection with size <4> containing exactly items <1>, <2>, <3>, <4> in any order."));
     }
 }
