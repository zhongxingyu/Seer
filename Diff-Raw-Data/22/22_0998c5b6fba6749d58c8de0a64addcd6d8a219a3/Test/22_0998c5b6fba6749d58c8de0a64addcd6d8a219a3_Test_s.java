 /**
  * 
  */
 package com.vikasing.nicetext;
 
 import java.util.Set;
 
 import org.crow.utils.HtmlUtils;
 
 
 /**
  * @author vikasing
  *
  */
 public class Test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		HTMLHelper htmlHelper = new HTMLHelper();
 
 		HtmlUtils htmlUtils = new HtmlUtils();
		Set<String> urlSet = htmlUtils.getURLsFromHTML("https://news.ycombinator.com/news");
 		for (String url : urlSet) {
			System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
 			String text = htmlHelper.getText(url).getNiceText();
 			System.out.println(text);
		}
/*		NGramExtracter nExtracter = new NGramExtracter();
 		Map<String, SortedSet<Entry<String, Integer>>> nGramMap = nExtracter.getNGrams(text);
 		SortedSet<Entry<String, Integer>> bigrams = nGramMap.get("bi");
 		for (Entry<String, Integer> entry : bigrams) {
 			if (entry.getValue()>1) {
 				System.out.println(entry.getKey() +" "+entry.getValue());
 			}
 		}
 		SortedSet<Entry<String, Integer>> trigrams = nGramMap.get("tri");
 		for (Entry<String, Integer> entry : trigrams) {
 			if (entry.getValue()>1) {
 				System.out.println(entry.getKey() +" "+entry.getValue());
 			}
 		}
 		
 		SortedSet<Entry<String, Integer>> monograms = nGramMap.get("mono");
 		for (Entry<String, Integer> entry : monograms) {
 			if (entry.getValue()>1) {
 				System.out.println(entry.getKey() +" "+entry.getValue());
 			}
 		}*/
 	}
 
 }
