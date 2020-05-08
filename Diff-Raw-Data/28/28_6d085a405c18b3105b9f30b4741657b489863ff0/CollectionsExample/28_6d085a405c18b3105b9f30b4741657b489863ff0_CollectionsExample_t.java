 package com.jaxjug.guava;
 
 import java.util.List;
 
 import com.google.common.base.CharMatcher;
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Predicate;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.HashMultiset;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.ListMultimap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Multiset;
 
 public class CollectionsExample {
 
 	public CollectionsExample() {
 		@SuppressWarnings("unused")
 		ImmutableList<String> emptyList = ImmutableList.of();
 		
 		@SuppressWarnings("unused")
 		ImmutableList<String> builderMethod = ImmutableList.<String>builder()
 				.add("one")
 				.add("two")
 				.add("three")
 				.build();
 		
 	}
 	
 	private Iterable<String> split(String data) { 
 		return Splitter.on(CharMatcher.JAVA_LETTER_OR_DIGIT.negate())
 				.trimResults()
 				.omitEmptyStrings()
 				.split(data);
 	}
 
 	public Multiset<String> multiset(String data) {
 		Iterable<String> words = split(data);
 		Multiset<String> wordInstances = HashMultiset.create(words);
 		for (String word : wordInstances.elementSet()) {
 			System.out.println(word 
 					+ ": " 
 					+ wordInstances.count(word));
 		}
 		return wordInstances;
 	}
 
 	public ListMultimap<String, Integer> multimap(String data) {
 		Iterable<String> words = split(data);
 		ListMultimap<String, Integer> wordLocations = ArrayListMultimap.create();
 		
 		int i = 0;
 		for (String word : words) { 
 			wordLocations.put(word, i);
 			i += 1;
 		}
 		
 		for (String word : wordLocations.keySet()) {
 			List<Integer> locations = wordLocations.get(word);
 			System.out.println(word + ":" + locations);
 		}
 		
 		return wordLocations;
 	}
 
 	public Multiset<Integer> getAllUpercaseStringLengthsImperative(Iterable<String> src) {
 		Multiset<Integer> lengths = HashMultiset.create();
 		for (String element : src) { 
 			if (CharMatcher.JAVA_UPPER_CASE.matchesAllOf(element)) {
 				lengths.add(element.length());
 			}
 		}
 		return lengths;
 	}
 	
 	public Iterable<String> getAllUppercaseImperative(Iterable<String> src) { 
 		List<String> caps = Lists.newArrayList();
 		for (String element : src) { 
 			if (CharMatcher.JAVA_UPPER_CASE.matchesAllOf(element)) {
 				caps.add(element);
 			}
 		}
 		return caps;
 	}
 	
 	Predicate<String> allCaps = new Predicate<String>() {
 		@Override
 		public boolean apply(String input) {
 			return CharMatcher.JAVA_UPPER_CASE.matchesAllOf(input);
 		}};
 		
     Function<String, Integer> stringLengths = new Function<String, Integer>() { 
     	@Override
     	public Integer apply(String input) { 
     		return input.length();
     	}
     };
     
     public Iterable<String> getAllUppercase(Iterable<String> src) { 
     	return Iterables.filter(src, allCaps);
     }
 	
 	public Multiset<Integer> getAllUppercaseStringLengths(Iterable<String> src) {
 		return HashMultiset.create(Iterables.transform(Iterables.filter(src, allCaps), stringLengths));
 	}
 	
 	public Iterable<Integer> getAllUppercaseStringLengthsFluent(Iterable<String> src) { 
		return FluentIterable
			.from(src)
			.filter(allCaps)
			.transform(stringLengths);
 	}
 	
 	public static Function<String, String> quote = new Function<String, String>() {
 		@Override
 		public String apply(String input) {
 			return "\"" + input + "\"";
 		}
 	};
 	
 	Function<String, Integer> quoteAndGetLength = 
 		Functions.compose(stringLengths, quote);
 }
