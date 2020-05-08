 package org.cthul.matchers.fluent.value;
 
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.SelfDescribing;
 
 /**
  * A value that can be matched. 
  * <p>
  * Can consist of multiple elements that will be matched individually.
  * The MatchValue tracks which elements matched successfully and is
  * able to provide messages that describe its state.
  * @param <Value> element type
  */
 public interface MatchValue<Value> extends SelfDescribing {
     
     /**
      * Applies the matcher.
      * @param matcher the matcher
      * @return true iff value is valid
      */
     boolean matches(ElementMatcher<Value> matcher);
     
     /**
      * Returns whether the value is valid.
      * @return true iff value is valid
      */
     boolean matched();
     
     /**
      * Describes the value.
      * @param description 
      */
     @Override
     void describeTo(Description description);
     
     /**
      * Describes the value's type.
      * @param description 
      */
     void describeValueType(Description description);
 
     /**
      * Describes what was expected of the value.
      * Assumes the match value is in an invalid state.
      * @param description 
      */
     void describeExpected(ExpectationDescription description);
     
     /**
      * Describes how the match failed.
      * Assumes the match value is in an invalid state.
      * @param description 
      */
     void describeMismatch(Description description);
     
     /**
     * Equivalent to {@code adapter.adapt(this)}.
      * @param <Property>
      * @param adapter
      * @return adapted match value
      */
     <Property> MatchValue<Property> get(MatchValueAdapter<? super Value, Property> adapter);
     
     
     /**
      * A single element of a {@link MatchValue}.
      * <p>
      * If two elements are equal, they represent the same underlying value.
      * @param <Value> value type
      */
     interface Element<Value> {
         
         Value value();
         
     }
     
     /**
      * A Hamcrest Matcher that accepts {@link Element}s of a type.
      * @param <Value> value type
      * @see org.cthul.matchers.fluent.value.ElementMatcher
      */
     interface ElementMatcher<Value> extends Matcher<Element<?>> {
         
         /**
          * Generates a description that explains what would have been expected
          * of an element to be matched successfully.
          * @param e element that was rejected
          * @param description the description to append to
          */
         void describeExpected(Element<?> e, ExpectationDescription description);
         
     }
     
     /**
      * A description that consists of multiple expectation descriptions,
      * which will be combined for the end result.
      * Can be expected to remove duplicates.
      */
     interface ExpectationDescription extends Description {
         
         /**
          * 
          * @param index
          * @param expected 
          */
         void addExpected(int index, SelfDescribing expected);
         
     }
 }
