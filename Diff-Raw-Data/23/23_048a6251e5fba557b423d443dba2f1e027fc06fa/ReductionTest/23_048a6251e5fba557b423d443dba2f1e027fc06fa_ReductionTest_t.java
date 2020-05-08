 package com.nearinfinity.java8.samples;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.*;
 import java.util.function.Function;
 
 import static java.util.stream.Collectors.toList;
 import static java.util.stream.Collectors.toStringJoiner;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class ReductionTest {
 
     private List<Integer> _numbers;
     private List<String> _names;
 
     @Before
     public void setUp() {
         _numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
         _names = Arrays.asList("Bob", "Tom", "Jeff", "Scott", "Jennifer", "Steve");
     }
 
     @Test
     public void testSumOfSquaresReducer() {
         assertThat(_numbers.stream().map(value -> value * value).sum(), is(385L));
     }
 
     @Test
     public void testAverageOfSquaresReducer() {
         OptionalDouble result = _numbers.stream().map(value -> value * value).average();
         assertThat(result.getAsDouble(), is(38.5));
     }
 
     @Test
     public void testFindLongestNameReducer() {
         Optional<String> longestName = _names.stream().reduce((name1, name2) ->
                name1.length() >= name2.length() ? name1 : name2);
         assertThat(longestName.get(), is("Jennifer"));
     }
 
     @Test
     public void testReducingNamesWithToStringJoinerCollector() {
         StringJoiner names = _names.stream().map(String::toLowerCase).collect(toStringJoiner(", "));
         String joinedLowerCaseNames = names.toString();
         assertThat(joinedLowerCaseNames, is("bob, tom, jeff, scott, jennifer, steve"));
     }
 
     @Test
     public void testGreetEachNameAndCollectIntoNewList() {
         List<String> greetings = _names.stream()
                 .map(name -> "Hello " + name)
                 .collect(toList());
         assertThat(greetings, is(Arrays.asList(
                 "Hello Bob", "Hello Tom", "Hello Jeff", "Hello Scott", "Hello Jennifer", "Hello Steve")));
     }

    @Test
    public void testDoublingNumbersAndTurningIntoArray() {
        int[] doubled = _numbers.stream()
                .map(value -> value * 2)
                .toArray();
        assertThat(doubled, is(new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20}));
    }

    // TODO Doesn't seem right to have to cast like below in order to make it work, but if
    //      don't do cast I get a compilation error at the moment. Must be something I don't know...
    @Test
    public void testDoublingNumbersAndCollectingIntoList() {
        List<Integer> doubled = _numbers.stream()
                .map((Function<? super Integer, ? extends Integer>) value -> value * 2)
                .collect(toList());
        System.out.println("doubled = " + doubled);
    }
 }
