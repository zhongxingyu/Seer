 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.common.Word;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/11/13
  * Time: 4:03 PM
  */
 
 //anagram class creation adds about 300ms to thread execution time
 
 public class Dictionary implements Iterable<String> {
     private Set<String> dictionary;
     private Map<String, HashSet<String>> anagrams;
     private SubstringDB substrings;
 
     private long timeToInit;
 
     public Dictionary(File dictionaryFile) throws FileNotFoundException {
         final long start = System.currentTimeMillis();
         final Scanner scanner = new Scanner(dictionaryFile);
 
         dictionary = new HashSet<String>();
         anagrams = new HashMap<String, HashSet<String>>();
         substrings = new SubstringDB();
 
         final int numCores = Runtime.getRuntime().availableProcessors();
 
         Thread t = new Thread(new Runnable() {
             public void run() {
                 while (scanner.hasNext()) {
                     String input = scanner.next().toUpperCase();
                     char[] charArr = input.toCharArray();
                     Arrays.sort(input.toCharArray());
                     String sortedString = new String(charArr);
                     if (!anagrams.containsKey(sortedString)) {
                         anagrams.put(sortedString, new HashSet<String>());
                     }

                     anagrams.get(sortedString).add(input);
                     dictionary.add(input);
                     substrings.add(input);
                 }
                 timeToInit = (System.currentTimeMillis() - start);
                 System.err.println("Processed dictionary file in " + timeToInit + "ms.");
             }
         });
 
         t.start();
     }
 
     public long getTimeToInit() {
         return timeToInit;
     }
 
     public boolean contains(Word w) {
         return contains(w.getWord());
     }
 
     public boolean contains(String s) {
         if (!dictionary.contains(s.toUpperCase())) {
             return false;
         }
         return true;
     }
 
     @Override
     public Iterator<String> iterator() {
         return dictionary.iterator();
     }
 }
