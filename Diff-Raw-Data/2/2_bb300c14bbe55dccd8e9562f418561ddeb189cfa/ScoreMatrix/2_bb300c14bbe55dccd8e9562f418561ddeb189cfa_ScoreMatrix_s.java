 package common;
 
 import common.interfaces.IMatchScoreMatrix;
 import edu.princeton.cs.introcs.In;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Operates with scoring matrix
  *
  * Author: Oleg Yasnev
  */
 public class ScoreMatrix implements IMatchScoreMatrix {
     /**
      * Scoring map. Key is two concatenated nucleotides
      */
     protected Map<String, Integer> map;
 
     /**
      * Constructor.
      * Reads scoring matrix from a given file
      * @param filename file with scoring matrix
      */
     public ScoreMatrix(String filename) {
         readFromFile(filename);
     }
 
     public int getScore(char ch1, char ch2) {
        return map.get(String.valueOf(ch1) + String.valueOf(ch2));
     }
 
     /**
      * Reads scoring matrix from a given file
      * @param filename file with scoring matrix
      */
     protected void readFromFile(String filename) {
         In in = new In(filename);
 
         String str = in.readLine();
 
         // Parse header
         String[] symbols = str.trim().split("\\s+");
 
         map = new HashMap<String, Integer>(symbols.length * 2);
         // length is twice because AB and BA are two records in map
 
         // Read score and fill map
         while (!in.isEmpty()) {
             // read header of current line
             char curSymbol = in.readString().toUpperCase().charAt(0);
             // add scores for combination row + count
             for (String symbol : symbols) {
                 int score = in.readInt();
                 map.put(symbol + curSymbol, score);
             }
         }
 
         in.close();
     }
 }
