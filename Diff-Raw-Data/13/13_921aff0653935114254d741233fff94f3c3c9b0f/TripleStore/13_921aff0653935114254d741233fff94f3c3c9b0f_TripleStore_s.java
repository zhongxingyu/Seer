 package store;
 
 import format.Word;
 import java.util.*;
 
 /**
  * User: buren
  * Date: 10/1/13
  * Time: 5:03 PM
  */
 public class TripleStore {
     private List<Triple> pairList;
     private List<Triple> tripletList;
 
     public TripleStore() {
         this.pairList =    new ArrayList<Triple>();
         this.tripletList = new ArrayList<Triple>();
     }
 
     public List<Triple> getPairs(List<List<Word>> sentenceList) {
         for (List<Word> sentence : sentenceList){
             for (Word word : sentence) {
                 if (word.getDeprel().toLowerCase().equals("ss"))
                     pairList.add(new Triple(word, sentence.get(word.getHead())));
             }
         }
         return pairList;
     }
 
     public HashMap<String, List<Triple>> getAllPairs(List<List<Word>> sentenceList) {
         List<Triple> nonTripletpairList = new ArrayList<Triple>();
         this.tripletList = new ArrayList<Triple>();
         for (List<Word> sentence : sentenceList){
             HashMap<Integer, Triple> sentenceTriplets = new HashMap<Integer, Triple>();
             for (Word word : sentence) {
                 String deprel = word.getDeprel().toLowerCase();
                 Triple currentTriple = null;
                 if (sentenceTriplets.containsKey(word.getHead())){
                     currentTriple = sentenceTriplets.get(word.getHead());
                 }
                 if (deprel.equals("ss")){
                     if (currentTriple == null){
                         currentTriple = new Triple();
                     }
                     currentTriple.setSubject(word);
                     currentTriple.setVerb(sentence.get(word.getHead()));
                     sentenceTriplets.put(word.getHead(), currentTriple);
                 } else if (deprel.equals("oo")){
                     if (currentTriple == null) {
                         currentTriple = new Triple();
                     }
                     currentTriple.setwObject(word);
                     currentTriple.setVerb(sentence.get(word.getHead()));
                     sentenceTriplets.put(word.getHead(), currentTriple);
                 }
             }
             for (Triple sentenceTriple : sentenceTriplets.values()){
                 if (sentenceTriple.isTriplet())
                     tripletList.add(sentenceTriple);
                 else
                     nonTripletpairList.add(sentenceTriple);
             }
         }
         HashMap<String, List<Triple>> pairs = new HashMap<String, List<Triple>>();
         pairs.put("nonTripletpairs", nonTripletpairList);
         pairs.put("triplets", tripletList);
         return pairs;
     }
 
     public HashMap<Triple, Integer> getTripletsByFrequency(List<Triple> triples) {
      HashMap<Triple, Integer> frequencyTriplets = new HashMap<Triple, Integer>();
         for (Triple triple : triples) {
             if (frequencyTriplets.containsKey(triple)){
                int frequency = frequencyTriplets.get(triple) + 1;
                 frequencyTriplets.put(triple, frequency);
             } else {
                 frequencyTriplets.put(triple, 1);
             }
         }
         return frequencyTriplets;
     }
 
 
 
 
 }
