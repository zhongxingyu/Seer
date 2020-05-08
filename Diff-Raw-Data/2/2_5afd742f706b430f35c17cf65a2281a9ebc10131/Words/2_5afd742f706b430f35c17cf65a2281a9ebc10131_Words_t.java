 package cryptograms;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 
 /**
  *
  * @author Charlie
  */
 public class Words {
     
     ArrayList<String> allWords = new ArrayList<String>();
     
     public Words() {
         try {
             String word;
             InputStream file = getClass().getResourceAsStream("2of4brif.txt");
             BufferedReader reader = new BufferedReader(new InputStreamReader(file));
             while ((word = reader.readLine()) != null) {
                 allWords.add(word);
             }
         } catch (IOException ex) {
             System.out.println("Exception: " + ex.getMessage());
         }
     }
     
     public String[] getWordsOfLength(int len) {
         ArrayList<String> foundWords = new ArrayList<String>();
         for (String word : allWords) {
             if (word.length() == len)
                 foundWords.add(word);
         }
        return foundWords.toArray(new String[foundWords.size()]);
     }
 }
