 package org.cthul.matchers.fluent.value;
 
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.SelfDescribing;
 
 /**
  * Creates {@link MatchValue} from a plain value.
  * <p>
  * A MatchValueAdapter represents an aspect of some type, 
  * like the length of strings or each element of lists.
  * It can then be used to create {@link MatchValue}s that represent
  * this aspect of a concrete value.
  */
 public interface MatchValueAdapter<Value, Property> extends SelfDescribing {
     
     /**
      * Converts a plain value into a match value.
      * @param value source value
      * @return match value
      */
     MatchValue<Property> adapt(Object value);
     
     /**
      * Creates a match value that will have its elements derived from
      * the elements of the source match value.
      * @param value source value
      * @return match value
      */
     MatchValue<Property> adapt(MatchValue<? extends Value> value);
     
     /**
      * Creates a match value adapter that takes the match values from another
      * adapter and adapts them with this adapter.
      * @param <Value0> input type of resulting adapter
      * @param adapter source adapter
      * @return match value adapter
      */
     <Value0> MatchValueAdapter<Value0, Property> adapt(MatchValueAdapter<Value0, ? extends Value> adapter);
     
     /**
      * Returns a matcher that builds a {@link MatchValue} using this adapter,
      * and verifies it with the given matcher.
      * @param matcher the matcher
     * @return adapted matcher
      */
     Matcher<Value> adapt(Matcher<? super Property> matcher);
     
     /**
      * Equivalent to {@code adapter.adapt(this)};
      * @param <Property2>
      * @param adapter
     * @return match value adapter
      */
     <Property2> MatchValueAdapter<Value, Property2> get(MatchValueAdapter<? super Property, Property2> adapter);
     
     /**
      * Describes a producer that will have its output adapted by this adapter.
      * @param producer producer to be described
      * @param description description to append to
      */
     void describeProducer(SelfDescribing producer, Description description);
     
     /**
      * Describes a consumer that will receive the output of this adapter.
      * @param consumer consumer to be described
      * @param description description to append to
      */
     void describeConsumer(SelfDescribing consumer, Description description);
     
 }
