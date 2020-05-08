 package org.cmu.fastcode.yelpscrapper;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  * This class is responsible for getting list of all business links
  * on business page
  *
  * @author Fengjiang.
  *         Created Nov 15, 2012.
  */
 public class BizListGetter {
 	
 	/**
 	 * This method is responsible for getting all biz lists for one
 	 * biz-list page
 	 * 
 	 * input: URL of biz-list page
 	 * return: List of URL of each biz link
 	 * @throws IOException 
 	 * */
 	static List<String> getBizLinkLists(String bizPageUrl) throws IOException{
 		List<String> bizLinkLists = new ArrayList<String>();
 		Document doc = Jsoup.connect(bizPageUrl).get();
 		Elements bizListElements = doc.getElementsByClass("itemheading");
 		for(Element e : bizListElements){
 			bizLinkLists.add(e.select("a[href*=/biz]").first().attr("abs:href"));
 		}
 		return bizLinkLists;
 	}
 	
 	static String getNextPageBizList(String bizPageUrl) throws IOException{
		Document doc = Jsoup.connect(bizPageUrl).timeout(0).get();
 		return doc.getElementById("pager_page_next").attr("abs:href");
 	}
 	
 	
 	/**
 	 * -class:reviewclearfix
 	 *     -class:review_comment ieSucks
 	 * */
 	static Map<Integer, String> getReviewRaitingPair(String url) throws IOException{
 		Map<Integer, String> map = new HashMap<Integer, String>();
 		Document doc = Jsoup.connect(url).timeout(0).get();
 		
 		return map;
 	}
 	
 	public static void main(String[] args) throws IOException{
 		String url = "http://www.yelp.com/search?find_desc=&find_loc=New+York%2C+NY&ns=1#find_desc=restaurant&show_filters=1&start=80";
 		System.out.println(getNextPageBizList(url));
 	}
 }
