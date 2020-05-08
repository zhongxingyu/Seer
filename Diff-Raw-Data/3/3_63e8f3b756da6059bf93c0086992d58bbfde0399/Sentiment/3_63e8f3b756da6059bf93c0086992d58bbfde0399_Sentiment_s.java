 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class Sentiment {
   private static Sentiment instance;
   private static String sentiment_file = "../resources/clean_senti_words.txt";
 
   private HashMap<String, Float> scores = new HashMap<String, Float>();
 
   private Sentiment() {}
 
   public static Sentiment get_instance() throws IOException {
     if (instance != null) {
       return instance;
     } else {
       instance = new Sentiment();
       instance.initialize();
       return instance;
     }
   }
 
   public Float score(String word) {
 	  return scores.get(clean_text(word));
   }
 
   private void initialize() throws IOException {
     BufferedReader reader = new BufferedReader(new FileReader(sentiment_file));
     String line;
     while ((line = reader.readLine()) != null) {
       String[] line_parts = line.split(" ");
       String word = line_parts[0];
       float score = Float.parseFloat(line_parts[1]);
       scores.put(word, score);
     }
     reader.close();
   }
 
   private String clean_text(String text) {
    // TODO downcase 
    return text;
   }
 }
 
