 import java.util.Arrays;
 
 /*
  * Solution proposal to coursera Algorithms Part 2
  * Programming Assignment 1: WordNet
  */
 public class Outcast {
     // constructor takes a WordNet object
 
     private final WordNet wn;
 
     public Outcast(WordNet wordnet) {
         if (wordnet == null) throw new NullPointerException();
         wn = wordnet;
     }
 
     // given an array of WordNet nouns, return an outcast
     public String outcast(String[] nouns) {
         int[] d = new int[nouns.length];
         for (int i = 0; i < nouns.length - 1; i++) {
             for (int j = i + 1; j < nouns.length; j++) {
                 int dist=wn.distance(nouns[i], nouns[j]);
                 d[i]+=dist;
                 d[j]+=dist;
             }
         }
        int min=0;
         for(int i = 1 ; i < d.length ; i ++){
            min = Math.min(min,d[i]);
         }
        return nouns[min];
     }
 
     // for unit testing of this class (such as the one below)
     public static void main(String[] args) {
     }
 
 }
