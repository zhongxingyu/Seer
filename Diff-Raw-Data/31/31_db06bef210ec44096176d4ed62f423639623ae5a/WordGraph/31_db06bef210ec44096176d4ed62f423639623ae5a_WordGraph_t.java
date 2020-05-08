 package com.jayway.wordgraph;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Lists.newLinkedList;
 import static com.jayway.wordgraph.Collections3.reduce;
 import static java.util.Collections.max;
 import static java.util.Collections.singletonMap;
 import static java.util.Collections.sort;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 
 public class WordGraph {
     // @BEGIN_VERSION 4
     private static final Function<String, String> LOWER = new Function<String, String>() {
         public String apply(String from) {
             return from.toLowerCase();
         }
     };
 
     private static final Predicate<String> NO_EMPTY = new Predicate<String>() {
         public boolean apply(String input) {
             return input != null && input.length() > 0;
         }
     };
     // @END_VERSION 4
 
     // @BEGIN_VERSION 6
     private static final Function<Entry<String, Integer>, Map<String, Integer>> ENTRY_TO_MAP = new Function<Entry<String, Integer>, Map<String, Integer>>() {
         public Map<String, Integer> apply(Entry<String, Integer> from) {
             return singletonMap(from.getKey(), from.getValue());
         }
     };
 
     private static final Comparator<Map<String, Integer>> COMPARE_BY_VALUE = new Comparator<Map<String, Integer>>() {
         public int compare(Map<String, Integer> o1, Map<String, Integer> o2) {
             return o1.values().iterator().next().compareTo(o2.values().iterator().next());
         }
     };
     // @END_VERSION 6
 
     // @BEGIN_VERSION 9
     private static final Comparator<Map<String, Integer>> COMPARE_BY_KEY_LENGTH = new Comparator<Map<String, Integer>>() {
         public int compare(Map<String, Integer> o1, Map<String, Integer> o2) {
             Integer lengthFirst = o1.keySet().iterator().next().length();
             Integer lengthSecond = o2.keySet().iterator().next().length();
             return lengthFirst.compareTo(lengthSecond);
         }
     };
     // @END_VERSION 9
 
     public static void main(String[] args) throws IOException {
         if (args.length != 1) {
             System.err.println("Usage: WordGraph <filename>");
             System.exit(1);
         }
         // @BEGIN_VERSION_ONLY 3
         System.out.println(gatherWords(FileUtils.readFileToString(new File(args[0]))));
         // @END_VERSION_ONLY 3
         // @BEGIN_VERSION_ONLY 4
         System.out.println(countWords(gatherWords(FileUtils.readFileToString(new File(args[0])))));
         // @END_VERSION_ONLY 4
         // @BEGIN_VERSION_ONLY 5
         System.out.println(sortCountedWords(countWords(gatherWords(FileUtils.readFileToString(new File(args[0]))))));
         // @END_VERSION_ONLY 5
         // @BEGIN_VERSION 8
         System.out.println(histogram(sortCountedWords(countWords(gatherWords(FileUtils.readFileToString(new File(
                 args[0])))))));
         // @END_VERSION 8
     }
 
     // @BEGIN_VERSION 4
     public static Collection<String> gatherWords(String s) {
         String[] words = Pattern.compile("\\W+").split(s);
         return transform(filter(Arrays.asList(words), NO_EMPTY), LOWER);
     }
     // @END_VERSION 4
 
     // @BEGIN_VERSION 5
     public static Map<String, Integer> countWords(Collection<String> words) {
        Function2<Map<String, Integer>, String, Map<String, Integer>> count = new Function2<Map<String, Integer>, String, Map<String, Integer>>() {
             public Map<String, Integer> apply(Map<String, Integer> accum, String next) {
                 Integer value = accum.get(next);
                 if (value == null) {
                     value = 0;
                 }
                 accum.put(next, value + 1);
                 return accum;
             }
         };
         return reduce(count, new HashMap<String, Integer>(), words);
     }
     // @END_VERSION 5
 
     // @BEGIN_VERSION 6
     public static Collection<Map<String, Integer>> sortCountedWords(Map<String, Integer> words) {
         List<Map<String, Integer>> maps = newLinkedList(transform(words.entrySet(), ENTRY_TO_MAP));
         sort(maps, COMPARE_BY_VALUE);
         return maps;
     }
     // @END_VERSION 6
 
     // @BEGIN_VERSION 7
     public static String repeatStr(String s, int n) {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < n; i++) {
             sb.append(s);
         }
         return sb.toString();
     }
     // @END_VERSION 7
 
     // @BEGIN_VERSION 8
     public static String histogramEntry(Map<String, Integer> wordCount, int width) {
         String format = "%-" + width + "." + width + "s %s";
         Entry<String, Integer> entry = wordCount.entrySet().iterator().next();
         String word = entry.getKey();
         Integer count = entry.getValue();
         return String.format(format, word, repeatStr("#", count));
     }
     // @END_VERSION 8
 
     // @BEGIN_VERSION 9
     public static String histogram(Collection<Map<String, Integer>> input) {
         int maxWidth = max(input, COMPARE_BY_KEY_LENGTH).keySet().iterator().next().length();
         StringBuilder sb = new StringBuilder();
         String newline = System.getProperty("line.separator");
         for (Map<String, Integer> wordCount : input) {
             sb.append(histogramEntry(wordCount, maxWidth));
             sb.append(newline);
         }
         return sb.toString();
     }
     // @END_VERSION 9
 }
