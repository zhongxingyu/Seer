 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 
 public class PhraseParserImpl implements PhraseParser {
 
 	LinkedList<String> des;
 	ArrayList<HashMap<String, Integer>> maps;
 	
	public PhraseParserImpl(Webpage w){
 		des=new LinkedList<String>();
 		maps=new ArrayList<HashMap<String, Integer>>();
		parse(w);
 	}
 	
	private void parse(Webpage w) {
 		// TODO Auto-generated method stub
 		LinkedList<CourseDescription> cdes=w.getCourseDescriptions();
 		for(CourseDescription c:cdes){
 			des.add(c.getDescription());
 		}
 		generateMap(1);
 		generateMap(2);
 		generateMap(3);
 		generateMap(4);
 	}
 	
	private void generateMap(int length){
 		HashMap<String,Integer> map=new HashMap<String, Integer>();
 		ArrayList<String> phrases=generatePhrases(length);
 		for(String s: phrases){
 			if(map.containsKey(s))
 				map.put(s, map.get(s)+1);
 			else map.put(s, 1);
 		}
 		maps.add(map);
 	}
 	
	private ArrayList<String> generatePhrases(int length){
 		ArrayList<String> result=new ArrayList<String>();
 		ArrayList<String> temp=new ArrayList<String>();
 		for(String s: des){
 			String[] array=s.split("\\.|,");
 			for(String ss: array){
 				temp.add(ss.trim());
 			}
 		}
 		for(String s: temp){
 			String[] array=s.split(" ");
 			int index=0;
 			while(array.length-index>length){
 				StringBuilder phrase=new StringBuilder();
 				for(int i=index; i<index+length; i++)
 					phrase.append(array[i]+" ");
 				result.add(phrase.toString());
 				index++;
 			}
 		}
 		return result;
 	}
 	
 
 	@Override
 	public HashMap<String, Integer> getPhrase(int length) {
 		// TODO Auto-generated method stub
 		return maps.get(length-1);
 	}
 
 	@Override
 	public String getDescription(String title) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
