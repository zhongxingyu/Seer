 package org.javafunk.matchbox.implementations;
 
 import org.hamcrest.Matcher;
 import org.javafunk.matchbox.testclasses.Bean;
 import org.junit.Test;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.javafunk.matchbox.MatcherMatchers.matches;
 import static org.javafunk.matchbox.MatcherMatchers.mismatchesSampleWithMessage;
 import static org.javafunk.matchbox.ObjectMatchers.isBeanWithSameAttributesAs;
 import static org.javafunk.matchbox.testclasses.Bean.bean;
 
 public class IsBeanWithSameAttributesAsMatcherTest {
     @Test
     public void shouldMatchBeanWithSameProperties() {
         // Given
         Bean actual = bean("A", "B", "C", "D");
         Bean expected = bean("A", "B", "C", "D");
 
         // When
         Matcher<Bean> matcher = isBeanWithSameAttributesAs(expected);
 
         // Then
         assertThat(matcher, matches(actual));
     }
 
     @Test
     public void shouldMismatchBeanWithDifferentProperties() {
         // Given
         Bean actual = bean("A", "B", "C", "D");
         Bean expected = bean("A", "B", "C", "E");
 
         // When
         Matcher<Bean> matcher = isBeanWithSameAttributesAs(expected);
 
         // Then
        assertThat(matcher, mismatchesSampleWithMessage(actual,
                "got a Bean <Bean<attribute1=<A>, attribute2=<B>, attribute3=<C>, attribute4=<E>, >>\n" +
                 "Mismatch: expected property \"attribute4\" = \"E\"\n" +
                 "            actual property \"attribute4\" = \"D\""));
     }
 }
