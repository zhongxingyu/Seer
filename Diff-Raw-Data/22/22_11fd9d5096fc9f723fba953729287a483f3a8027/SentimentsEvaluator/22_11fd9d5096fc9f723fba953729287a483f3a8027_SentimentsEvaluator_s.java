 package loyalitymonitor;
 
 import org.apache.pig.EvalFunc;
 import org.apache.pig.data.Tuple;
 import org.apache.pig.data.TupleFactory;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 public class SentimentsEvaluator extends EvalFunc<Integer> {
     private Map<String, Integer> sentiments = null;
 
     private void loadSentimentsMap() throws IOException {
         Map<String, Integer> _sentiments = new HashMap<String, Integer>();
 
         BufferedReader in = new BufferedReader(new FileReader("sentiments"));
         String s = null;
         while ((s = in.readLine()) != null) {
             String[] word = s.split("\t");
             // put greate -> 5
             _sentiments.put(word[0], Integer.parseInt(word[1]));
         }
         this.sentiments = _sentiments;
     }
 
     public void setSentiments(Map<String, Integer> dic) {
         this.sentiments = dic;
     }
 
     @Override
     public Integer exec(Tuple input) throws IOException {
         if(sentiments == null) {
             loadSentimentsMap();
         }
         if (input == null || input.size() == 0) {
             return null;
         }
 
         String inputLine = (String)input.get(0);
         String[] splitInputLine = inputLine.split("\\s");
         int tweetSentimentScore = 0;
         for(String s : splitInputLine) {
             // remove all punctuation symbols
             s = s.replaceAll("(\\w+)\\p{Punct}(\\s|$)", "$1$2");
             if( sentiments.containsKey(s) ) {
                tweetSentimentScore += sentiments.get(s);
             }
         }
 
         return tweetSentimentScore;
     }
 }
