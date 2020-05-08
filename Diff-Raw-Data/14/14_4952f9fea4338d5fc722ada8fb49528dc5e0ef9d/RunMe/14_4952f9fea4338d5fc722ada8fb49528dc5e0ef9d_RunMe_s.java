 package com.pzelnip.katas.anagramkata;
 
 import static com.pzelnip.katas.anagramkata.Anagrams.*;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 public final class RunMe {
 
     private static PrintStream out = System.out;
 
     private RunMe() {
     }
 
     static List<String> openWordList() {
         InputStream istream = RunMe.class.getResourceAsStream("/wordlist.txt");
         String result = new Scanner(istream).useDelimiter("\\A").next();
         return Arrays.asList(result.split("\\n"));
     }
 
     static void spitStats(final List<String> words) {
         long total = 0;
         int min = Integer.MAX_VALUE;
         String minword = "";
         int max = 0;
         String maxword = "";
         Map<Integer, Integer> frequencies = new HashMap<Integer, Integer>();
         for (String word : words) {
             int wordlen = word.length();
 
             Integer count = frequencies.get(wordlen);
             if (count == null) {
                 frequencies.put(wordlen, 1);
             } else {
                 frequencies.put(wordlen, count + 1);
             }
             total += wordlen;
             if (wordlen < min) {
                 min = wordlen;
                 minword = word;
             }
             if (wordlen > max) {
                 max = wordlen;
                 maxword = word;
             }
         }
 
        final int colwidth = 80;
         double avglen = (total * 1.0) / words.size();
         out.println("There are " + words.size() + " words");
         out.println("Average length: " + avglen);
         out.println(String.format("Max length: %s (%s)", maxword, max));
         out.println(String.format("Min length: %s (%s)", minword, min));
         out.println("Histogram:");
         for (int x = min; x <= max; x++) {
             Integer count = frequencies.get(x);
             out.print(String.format("%2d: ", x));
             if (count != null) {
                for (int y = 0; y < count / colwidth; y++) {
                    out.print("X");
                }
                if (count / colwidth < 1 && count != 0) {
                     out.print("X");
                 }
             }
             out.println();
         }
 
         out.print("Histogram raw counts: ");
         for (int x = min; x <= max; x++) {
             Integer count = frequencies.get(x);
             out.print(String.format("%s ", (count == null ? "0" : count)));
         }
         out.println();
     }
 
     public static void main(final String[] args) {
         List<String> lines = openWordList();
         spitStats(lines);
         Collection<Set<String>> anagrams = findAnagramsInList(lines);
         out.println("Found " + anagrams.size() + " groups of anagrams");
         Set<String> biggestGroup = new HashSet<String>();
         for (Set<String> group : anagrams) {
             out.println(group);
             if (group.size() > biggestGroup.size()) {
                 biggestGroup = group;
             }
         }
         out.println(String.format("Biggest group had %d elements (was %s)",
                 biggestGroup.size(), biggestGroup));
     }
 }
