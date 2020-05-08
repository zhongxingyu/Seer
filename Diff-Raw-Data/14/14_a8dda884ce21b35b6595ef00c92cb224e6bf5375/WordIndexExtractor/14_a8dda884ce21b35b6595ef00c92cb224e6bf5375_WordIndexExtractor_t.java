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
        String content = stringValue.substring(stringValue.indexOf("<") + 1, stringValue.lastIndexOf(">"));

        String docs[] = content.split(">;");
 
         Map<String, Integer> docNumberOfOccurrences = new HashMap<String, Integer>();
 
        for (String doc : docs) {
            if (doc.trim().isEmpty())
                continue;

            int numberOfOccurrences = doc.substring(doc.indexOf("<") + 1).split(",").length;
            String docId = doc.substring(0, doc.indexOf("<")).trim();
            docNumberOfOccurrences.put(docId, numberOfOccurrences);
         }
         return docNumberOfOccurrences;
     }
     
 }
