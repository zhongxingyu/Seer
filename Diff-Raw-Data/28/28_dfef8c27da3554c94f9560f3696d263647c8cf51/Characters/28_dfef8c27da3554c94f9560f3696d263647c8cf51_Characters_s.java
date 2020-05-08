 /**
  * Created with IntelliJ IDEA.
  * User: ryandao
  * Date: 8/25/13
  * Time: 3:00 PM
  * To change this template use File | Settings | File Templates.
  */
 
 import java.util.ArrayList;
 
 public class Characters {
    String[] sentences;
 
    public Characters(String[] sentences) {
         this.sentences = sentences;
     }
 
    public ArrayList<String[]> getWords() {
        ArrayList <String[]> result = new ArrayList <String[]>();
 
         for (int i = 0; i < sentences.length; i ++) {
            String sentence = sentences[i];
            result.add(sentence.split(" "));
         }
 
         return result;
     }
 }
