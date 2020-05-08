 
 import java.util.List;
 import java.util.Map;
 
 import spellcheck.WordCheck;
 import wordnet.Dictionary;
 
 
 public class Main {
 
 	private static Dictionary dict;
 
 
 	public static void main(String[] args){
 		// TODO Auto-generated method stub\
		String str = "acreo";
 		dict = new Dictionary();
 		WordCheck wc = new WordCheck(dict);
 		if(! dict.hasWord(str)){
 			Map<String, Integer> map = wc.getCorrect(str);
 			for (String string : map.keySet()) {
 				System.out.println(string+"  <"+ map.get(string) +">");
 			}
 		} else {
 			System.out.println("Correct");
 		}
 
 	}
 
 }
