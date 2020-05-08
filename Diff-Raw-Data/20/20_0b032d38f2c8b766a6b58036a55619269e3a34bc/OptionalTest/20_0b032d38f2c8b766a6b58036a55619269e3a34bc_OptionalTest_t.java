 package com.nearinfinity.java8.samples;
 
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Optional;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class OptionalTest {
 
     @Test
     public void testOptionalOf_WithNonNullValue() {
         Optional<String> value = Optional.of("some value");
         assertThat(value.isPresent(), is(true));
         assertThat(value.get(), is("some value"));
     }
 
     @Test(expected = NullPointerException.class)
     public void testOptionalOf_WithNullValue() {
         String str = null;
         Optional.of(str);
     }
 
     @Test
     public void testOptional_Empty() {
         Optional<Integer> o = Optional.empty();
         assertThat(o.isPresent(), is(false));
         assertThat(o.orElse(42), is(42));
     }
 
     @Test
     public void testOptional_IfPresent() {
         List<Integer> results = new ArrayList<>();
         Optional<Integer> second= Optional.of(3);
        second.ifPresent(results::add);  // must operate via side-effects, unfortunately...
         assertThat(results, is(Arrays.asList(3)));
     }
 
     @Test
     public void testOptional_OrElse() {
         Optional<Integer> o = Optional.empty();
         assertThat(o.orElse(42), is(42));
     }
 
     @Test
     public void testOptional_Get() {
         Optional<Integer> o = Optional.of(42);
         assertThat(o.get(), is(42));
     }
 
     @Test(expected = NoSuchElementException.class)
     public void testOptional_Get_WhenNoValue() {
         Optional<Integer> o = Optional.empty();
         o.get();
     }
 }
