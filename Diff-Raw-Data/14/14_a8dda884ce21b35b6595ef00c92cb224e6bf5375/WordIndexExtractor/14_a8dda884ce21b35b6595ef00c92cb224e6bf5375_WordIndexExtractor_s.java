 package nl.tudelft.in4325.a2.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class WordIndexExtractor {
 
     /**
      * Extracts information about a word from the entry in the index file.
      * @param stringValue word in index
      *
      * @return - Map, where the key is the document ID and the value is the
      *         number of times that the word occurs within this document.
      */
     public Map<String, Integer> extractWordFrequencies(String stringValue) {
        String docs[] = stringValue.split(" ");
 
         Map<String, Integer> docNumberOfOccurrences = new HashMap<String, Integer>();
 
        for (int i = 1; i < docs.length; i += 2) {
            docNumberOfOccurrences.put(docs[i], Integer.valueOf(docs[i + 1]));
         }
         return docNumberOfOccurrences;
     }
     
 }
