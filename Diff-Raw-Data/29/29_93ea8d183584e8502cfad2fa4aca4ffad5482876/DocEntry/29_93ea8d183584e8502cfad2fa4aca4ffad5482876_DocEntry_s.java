 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.Map.Entry;
 
 public class DocEntry {
 
     private String docId;
     private Boolean spam;
     private HashMap<String, Integer> wordCounts;
     private HashMap<String, Double> wordWeights; // tf-idf weights
     private double vectorLength; // actually, this may not be needed
 
     public DocEntry() {
         this(null, null);
     }
 
     public DocEntry(String docId, Boolean spam) {
         this.docId = docId;
         this.spam = spam;
         this.wordCounts = new HashMap<String, Integer>();
         this.wordWeights = new HashMap<String, Double>();
     }
 
     public String getDocId() {
         return docId;
     }
 
     /**
      * @throws NullPointerException if we don't know yet if document is spam or not
      */
     public boolean isSpam() {
         return spam;
     }
 
     public int getCount(String term) {
         return wordCounts.get(term);
     }
 
     public void incCount(String word) {
         incCount(word, 1);
     }
 
     public void incCount(String word, int inc) {
         if (wordCounts.get(word) == null) {
             wordCounts.put(word, inc);
         } else {
             int count = wordCounts.get(word);
             wordCounts.put(word, count + inc);
         }
     }
 
     public Set<Map.Entry<String, Integer>> getWordCounts() {
         return wordCounts.entrySet();
     }
 
     public Set<String> getWords() {
         return wordCounts.keySet();
     }
 
     /**
      * Adds the word weights of the other DocEntry to this DocEntry.
      *
      * @param other DocEntry whose word weights we want to add
      */
     public void addWordWeights(DocEntry other) {
         for (String word : other.wordWeights.keySet()) {
             Double weight = other.wordWeights.get(word);
             if (wordWeights.get(word) != null) {
                 weight += wordWeights.get(word);
             }
 
             wordWeights.put(word, weight);
         }
     }
 
     /**
      * For phase 2.
      *
      * Computes the map with the tf-idf weights for each word in the doc, using a map that contains
      * the df values for each term.
      *
      * @param docFrequencyMap df values for each term
      * @param numDocs N in the tf-idf formula
      */
     public void computeWordWeights(HashMap<String, Integer> docFrequencyMap, int numDocs) {
         if (wordCounts.isEmpty()) {
             throw new RuntimeException("The wordCounts map is empty!");
         }
 
         vectorLength = 0;
 
         for (String term : wordCounts.keySet()) {
             double tf_idf = (1 + Math.log10(wordCounts.get(term)))
                     * Math.log10(numDocs / (double) docFrequencyMap.get(term));
             wordWeights.put(term, tf_idf);
             vectorLength += tf_idf * tf_idf;
         }
 
         vectorLength = Math.sqrt(vectorLength);
     }
 
     /**
      * For phase 2.
      *
      * Computes the distance between 2 documents,
      *
      * @param other
      * @return
      */
     public double getDistance(DocEntry other) {
         double dist = 0;
 
         Set<String> words = new HashSet<String>(wordCounts.keySet());
         words.addAll(other.wordCounts.keySet());
 
         for (String word : words) {
             double x1 = 0;
             double x2 = 0;
 
             if (wordWeights.containsKey(word)) {
                 x1 = wordWeights.get(word);
             }
             if (other.wordWeights.containsKey(word)) {
                 x2 = other.wordWeights.get(word);
             }
 
             dist += (x1 - x2) * (x1 - x2);
         }
 
         dist = Math.sqrt(dist);
 
         return dist;
     }
     /*
      * Returns the String representation of the DocEntry instance.
      */
     public String toString() {
         return docId;
     }
 }
