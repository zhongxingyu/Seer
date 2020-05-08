 package edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Set;
 
 public class GoogleSynonymEngine {
 
   private static List<String> stoplist;
 
   public GoogleSynonymEngine() {
     try {
       loadStoplist();
     } catch (IOException e) {
       e.printStackTrace();
     }
   }
   
   public List<String> getSynonyms(String term) {
     List<String> synonyms = new ArrayList<String>();
     
     //File file = new File("model/google1/" + term + ".txt");
     //File file = new File("src/main/resources/model/google1/" + term + ".txt");
     try {
       URL url = new URL("file:src/main/resources/model/google1/" + term + ".txt");
       Scanner scanner = new Scanner(url.openStream());
       
       // Term frequency map
       Map<String, Integer> tmap = new HashMap<String, Integer>();
       // Document frequency map
       Map<String, Integer> dmap = new HashMap<String, Integer>();
       Map<String, Integer> tdmap = new HashMap<String, Integer>();
       
       while (scanner.hasNextLine()) {
         String[] tokens = scanner.nextLine().split(" ");
         
         Set<String> unique = new HashSet<String>();
         for (String token : tokens) {
           token = token.toLowerCase();
           if (!stoplist.contains(token)) {
             if (tmap.containsKey(token)) {
               tmap.put(token, tmap.get(token) + 1);
             } else {
               tmap.put(token, 1);
             }
             unique.add(token);
           }
         }
         
         for (String token : unique) {
           if (dmap.containsKey(token)) {
             dmap.put(token, dmap.get(token) + 1);
           } else {
             dmap.put(token, 1);
           }
         }
       }
       
       // Calculate score of TF * DF
       for (String token : tmap.keySet()) {
         tdmap.put(token, tmap.get(token) * dmap.get(token));
       }
 
       // Sort terms according to their score of TF * DF
       List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
               tdmap.entrySet());
       Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
         @Override
         public int compare(Entry<String, Integer> e0, Entry<String, Integer> e1) {
           return e1.getValue().compareTo(e0.getValue());
         }
       });
 
       // Pick the top 6 terms
       for (int i = 0; i < 6; i++) {
         if (!list.get(i).getKey().equals(term.toLowerCase())
                 && list.get(i).getValue() >= 4) {
           System.out.println("[GOOGLE] " + list.get(i).getKey() + " : " + list.get(i).getValue());
           synonyms.add(list.get(i).getKey());
         }
       }
       
     } catch (IOException e) {
      System.err.println("[MISSING] Term : " + term);
       return synonyms;
     }
     
     return synonyms;
   }
 
   /**
    * Load the stop list from 'stoplist.txt'.
    * @throws IOException 
    */
   public void loadStoplist() throws IOException {
     stoplist = new ArrayList<String>();
     // File file = new File("stoplist.txt");
     //File file = new File("src/main/resources/stoplist.txt");
     URL url = new URL("file:src/main/resources/stoplist.txt");
     Scanner scanner = new Scanner(url.openStream());
 
     while (scanner.hasNextLine()) {
       stoplist.add(scanner.nextLine());
     }
     scanner.close();
   }
 
   public static void main(String[] argv) {
 
     GoogleSynonymEngine engine = new GoogleSynonymEngine();
     // engine.getSynonyms("BARD1%20gene");
     engine.getSynonyms("NM23");
 
   }
 
 }
