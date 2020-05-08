 /**
  * Created with IntelliJ IDEA.
  * User: ryandao
  * Date: 8/25/13
  * Time: 3:00 PM
  * To change this template use File | Settings | File Templates.
  */
 
 import java.util.ArrayList;
import java.util.Arrays;
 
 public class Characters {
    ArrayList <String> sentences;
 
    public Characters(ArrayList <String> sentences) {
         this.sentences = sentences;
     }
 
    public ArrayList <ArrayList <String>> getWords() {
        ArrayList <ArrayList <String>> result = new ArrayList <ArrayList<String>>();
 
         for (int i = 0; i < sentences.length; i ++) {
            String sentence = sentences.get(i);
            String[] splitTokens = sentence.split(" ");
            result.add(new ArrayList <String> (Arrays.asList(splitTokens)));
         }
 
         return result;
     }
 }
