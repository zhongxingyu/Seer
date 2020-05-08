 package web;
 
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import src.Crawler;
 import src.Util;
 
 
 public class StatisticReader extends HTMLReader {
 	private final static Logger logger = Logger.getLogger(Crawler.class .getName());
 	private final static String IDEAS_STATS = "ideas-stats";
 	private final static String OTHER_STATS = "other-stats";
 	private final static String IDEAS_IN_REVIEW = "tab-review";
 	private final static String IDEAS_IN_PROGRESS = "tab-progress";
 	private final static String IDEAS_COMPLETED = "tab-complete";
 	private final static String FRAME_TAG = "iframe";
 	private final static String FACEBOOK_STATS = "u_0_1";
 	private final static String TWITTER_URL_P = "https://cdn.api.twitter.com/1/urls/count.json?url=";
 	private final static String TWITTER_URL_S = "&callback=twttr.receiveCount";
 	private final static String LOGO = "logo";
 	private final static String EXPLANATION_TEXT = "client-txt";
 	private final static String TABS = "listing-nav";
 	private final static String IDEA_VOTES = "vote-number";
 	private final static String IDEA_COMMENTS_DATE = "comment-date comment-meta";
 	private final static String IDEA_COMMENTS_ID = "comment-list";
 	private final static String IDEA_COMMENTS_DESCRIPTION = "comment-content";
 	private final static String IDEA_COMMENT_AUTHOR_NAME = 	"comment-author-name";
 	private final static String IDEA_DESCRIPTION_CLASS = "entry-content";
 	private final static String IDEA_HREF_TAGS = "/a/ideas/tag/tags/";
 	private final static String HREF_ATTR = "href";
 	private final static String IDEA_SIMILAR_ID = "similar-idea-list";
 	
 	public StatisticReader() {
 		super();
 		prepareUserAgent();
 	}
 	
 	public HashMap<String,Object> getCommunityStatistic(String url)  
 	{
 		HashMap<String,Object> statistics = new HashMap<String,Object>();
 		statistics.put("ideas", null);
 		statistics.put("ideas_in_review", null);
 		statistics.put("ideas_in_progress", null);
 		statistics.put("ideas_completed", null);
 		statistics.put("comments", null);
 		statistics.put("votes", null);
 		statistics.put("members", null);
 		statistics.put("facebook", null);
 		statistics.put("twitter", null);
 		statistics.put("logo", null);
 		statistics.put("explanation_text", null);
 		statistics.put("tabs", null);
 		
 		String content;
 		String textElement;
 		try {
 			content = getUrlContent(Util.toURI(url));
 			Document doc = Jsoup.parse(content);		       
 			
 			Element ideasStats = doc.getElementById(IDEAS_STATS);
 			if (ideasStats != null) {
 				textElement = replaceThounsandSymbol(ideasStats.child(0).text());
 				if (isNumeric(textElement))
 					statistics.put("ideas", textElement);
 				else
 					throw new Exception("The ideas counter is not numeric. Community: " + url);
 			}
 	        	
 			Element otherStats = doc.getElementById(OTHER_STATS);
 			if (otherStats != null) {
 				Elements childrenStats = otherStats.children();
 				textElement = replaceThounsandSymbol(childrenStats.get(0).text());
 				if (isNumeric(textElement))
 					statistics.put("comments", textElement);
 				else
 					throw new Exception("The comments counter is not numeric. Community: " + url);
 				textElement = replaceThounsandSymbol(childrenStats.get(1).text());
 				if (isNumeric(textElement))
 					statistics.put("votes",textElement);
 				else
 					throw new Exception("The votes counter is not numeric. Community: " + url);
 				textElement = replaceThounsandSymbol(childrenStats.get(2).text());
 				if (isNumeric(textElement))
 					statistics.put("members", textElement);
 				else
 					throw new Exception("The members counter is not numeric. Community: " + url);
 			}
 			
 			Element ideasInReview = doc.getElementById(IDEAS_IN_REVIEW);
 			if (ideasInReview != null) {
 				textElement = ideasInReview.child(0).text();
 				textElement = textElement.replaceAll("[^0-9]+", " ");
 				textElement = textElement.trim();
 				if (isNumeric(textElement))
 					statistics.put("ideas_in_review",textElement);
 				else
 					throw new Exception("The ideas in review counter is not numeric. Community: " + url);
 			}
 			
 			Element ideasInProgress = doc.getElementById(IDEAS_IN_PROGRESS);
 			if (ideasInProgress != null) {
 				textElement = ideasInProgress.child(0).text();
 				textElement = textElement.replaceAll("[^0-9]+", " ");
 				textElement = textElement.trim();
 				if (isNumeric(textElement))
 					statistics.put("ideas_in_progress",textElement);
 				else
 					throw new Exception("The ideas in progress counter is not numeric. Community: " + url);
 			}
 			
 			Element ideasCompleted = doc.getElementById(IDEAS_COMPLETED);
 			if (ideasCompleted != null) {
 				textElement = ideasCompleted.child(0).text();
 				textElement = textElement.replaceAll("[^0-9]+", " ");
 				textElement = textElement.trim();
 				if (isNumeric(textElement))
 					statistics.put("ideas_completed",textElement);
 				else
 					throw new Exception("The ideas completed counter is not numeric. Community: " + url);
 			}
 			
 			/*Element logo = doc.getElementById(LOGO);
 			if (logo != null)
 				statistics.put("logo", "yes");*/
 			
 			Element explanation = doc.getElementById(EXPLANATION_TEXT);
 			if (explanation != null) {
 				textElement = explanation.text();
 				if (!textElement.isEmpty())
 					statistics.put("explanation_text", "yes");
 			}
 			
 			ArrayList<HashMap<String,String>> tabs = getTabsURL(doc);
 			if (tabs.isEmpty()) {
 				statistics.put("tabs", null);
 				statistics.put("status", "closed");
 			}
 			else {
 				statistics.put("tabs", tabs);
 				statistics.put("status", "active");
 			}
 			
 			HashMap<String,Object> auxStats = getSNCounters(doc,url);
 			statistics.put("facebook",auxStats.get("facebook"));
 			statistics.put("twitter",auxStats.get("twitter"));
 		} catch (Exception e) {
 			e.printStackTrace();
 			logger.log(Level.SEVERE,e.getMessage(),e);
 		}
 		
 		return statistics;
 	}
 	
 	public HashMap<String,Object> getIdeaStatistics(String communityURL,
 													String ideaURL) 
 	throws Exception {
 		HashMap<String,Object> statistics = new HashMap<String,Object>();
 		String ideaURLEncoded = URLEncoder.encode(ideaURL, "utf-8");
 		String fullURL = communityURL+ideaURLEncoded;
 		//String fullURL = "http://protools.ideascale.com/a/dtd/Freeze-tracks-feature/22400-3779";
 		statistics.put("description", null);
 		statistics.put("tags", null);
 		statistics.put("facebook", null);
 		statistics.put("twitter", null);
 		statistics.put("comments", null);
 		statistics.put("score", null);
 		
 		String content = getUrlContent(Util.toURI(communityURL+ideaURL));
 		//String content = getUrlContent(Util.toURI(fullURL));
 		Document doc = Jsoup.parse(content);	
 		
 		//Description
 		Elements desc = doc.getElementsByClass(IDEA_DESCRIPTION_CLASS);
 		String ideaDescription = "";
 		for (int i = 0; i < desc.size(); i++) 
 			ideaDescription += desc.get(i).text();
 		statistics.put("description", ideaDescription);
 		
 		//If the description is null the idea is inaccessible
 		if (!ideaDescription.isEmpty()) {	
 			//Tags
 			Elements tags = doc.getElementsByAttributeValueMatching(HREF_ATTR, IDEA_HREF_TAGS);
 			if (!tags.isEmpty()) {
 				String ideaTags = "";
 				int numTags = tags.size();
 				for (int i = 0; i < numTags; i++) {
 					if (i != (numTags - 1))
 						ideaTags += tags.get(i).text() + ", ";
 					else
 						ideaTags += tags.get(i).text();
 				}
 				statistics.put("tags", ideaTags);
 			}
 			else {
 				statistics.put("tags", null);
 			}
 			//Social Networks
 			HashMap<String,Object> auxStats = getIdeaSNCounters(doc,fullURL);
 			statistics.put("facebook", auxStats.get("facebook"));
 			statistics.put("twitter",auxStats.get("twitter"));
 			
 			//Get the comment counter and comments meta-info
 			Element comments = doc.getElementById(IDEA_COMMENTS_ID);
 			if (comments != null) {
 				statistics.put("comments", comments.children().size());
 				ArrayList<HashMap<String,String>> commentsMeta = new ArrayList<HashMap<String,String>>();
 				commentsMeta = getComments(comments,commentsMeta,"-1");
 				statistics.put("comments-meta", commentsMeta);
 			}
 			else {
 				statistics.put("comments", 0);
 			}
 			
 			//Get score
 			Element scoreElem = doc.getElementsByClass("vote-number").first();
 			if (scoreElem != null)
 				statistics.put("score", Integer.parseInt(scoreElem.text()));
 			else
 				statistics.put("score", 0);
 			
 			//Get votes meta-info
 			Element voteElem = doc.getElementById("vote-activity-list");
 			if (voteElem != null) {
 				ArrayList<HashMap<String,String>> votesMeta = new ArrayList<HashMap<String,String>>();
 				for (Element vote : voteElem.children()) {
 					HashMap<String,String> voteMeta = new HashMap<String,String>();
 					Elements voter = vote.getElementsByClass("voter");
 					if (voter.first().children().size() > 1) {
 						Element eAuthor = voter.first().child(1);
 						voteMeta.put("author-name", eAuthor.text());
 						String authorId = eAuthor.attr(HREF_ATTR);
 						authorId = authorId.substring(authorId.lastIndexOf("/")+1,authorId.length());
 						authorId = authorId.split("-")[0];
 						if (isNumeric(authorId))
 							voteMeta.put("author-id", authorId);
 						else
 							voteMeta.put("author-id", "-1");
 					}
 					else {
 						voteMeta.put("author-name", "Unsuscribed User");
 						voteMeta.put("author-id", "-1");
 					}
 					Element type = vote.getElementsByClass("vote").first().child(0);
 					if (type.getElementsByTag("strong").attr("class").equals("up"))
 						voteMeta.put("value", "1");
 					else if (type.getElementsByTag("strong").attr("class").equals("down"))
 						voteMeta.put("value", "-1");
 					else
 						throw new Exception("Couldn't understand vote value " + type.getElementsByTag("strong").text());
 					Element date = vote.getElementsByClass("vote").first().child(1);
 					voteMeta.put("date", date.text());
 					
 					votesMeta.add(voteMeta);
 				}
 				statistics.put("votes-meta", votesMeta);
 			}
 			
 			Element similarIdeas = doc.getElementById(IDEA_SIMILAR_ID);
 			if (similarIdeas != null)
 					statistics.put("similar", similarIdeas.children().size());	
 			else
 				statistics.put("similar", 0);
 		}
 		return statistics;
 	}
 	
 	private ArrayList<HashMap<String,String>> getComments(Element rootComments,
 														  ArrayList<HashMap<String,String>> commentsMeta,
 														  String parent) {
 		
 		for (Element comment : rootComments.children()) {
 			HashMap<String,String> commentMeta = new HashMap<String,String>();
 			String commentId = comment.attr("id").split("-")[1];
 			Elements childComments = comment.getElementsByClass("child-comments"); 
 			if (!childComments.isEmpty()) {
 				for (int i = 0; i < childComments.size(); i++)
 					getComments(childComments.get(i),commentsMeta,commentId);
 			}
 			commentMeta.put("id", commentId);
 			Elements commenter = comment.getElementsByClass(IDEA_COMMENT_AUTHOR_NAME);
 			if (commenter.first().children().size() > 0) {
 				Element eAuthor = commenter.first().child(0);
 				commentMeta.put("author-name", eAuthor.text());
 				String authorId = eAuthor.attr(HREF_ATTR);
 				authorId = authorId.substring(authorId.lastIndexOf("/")+1,authorId.length());
 				authorId = authorId.split("-")[0];
 				if (isNumeric(authorId))
 					commentMeta.put("author-id", authorId);
 				else
 					commentMeta.put("author-id", "-1");
 			}
 			else {
 				commentMeta.put("author-name", "Unsuscribed User");
 				commentMeta.put("author-id", "-1");
 			}
 			Element date = comment.getElementsByAttributeValueMatching("class",IDEA_COMMENTS_DATE).first();
 			commentMeta.put("date", date.text());
 			Elements commentDesc = comment.getElementsByClass(IDEA_COMMENTS_DESCRIPTION);
 			String commentContent = "";
 			for (int i = 0; i < commentDesc.size(); i++) 
 				commentContent += commentDesc.get(i).text();
 			commentMeta.put("description", commentContent);
 			commentMeta.put("parent", parent);
 			commentsMeta.add(commentMeta);
 			
 			commentMeta.put("author-type", "crowd");
             Element commenterVCard = comment.getElementsByAttributeValueMatching("class", "vcard").first();
 			String vCard = commenterVCard.attr("class");
 			if (vCard.contains("idea-submitter"))
 				commentMeta.put("author-type", "submitter");
 			else if (vCard.contains("moderator"))
 				commentMeta.put("author-type", "moderator");
 		}
 		
 		return commentsMeta;
 	}
 	
 	public ArrayList<HashMap<String,String>> getTabsURL(Document doc) throws Exception {
 		ArrayList<HashMap<String,String>> tabs = new ArrayList<HashMap<String,String>>();
 		
 		String numIdeas = "";
 		Element navTabs = doc.getElementById(TABS);
 		if (navTabs != null) {
 			for (Element li : navTabs.children()) {
 				Element aLink = li.child(0);
 				numIdeas = aLink.text().replaceAll("[^0-9]+", " ").trim();
 				if (Integer.parseInt(numIdeas) != 0) {   //Save only tabs whose list of ideas is not empty
 					HashMap<String,String> tab = new HashMap<String,String>();
 					tab.put("url", aLink.attr("href"));
 					tab.put("ideas", numIdeas);
 					tabs.add(tab);
 				}
 			}
 		}
 		
 		return tabs;
 	}
 	
     private String replaceThounsandSymbol(String str) {
     	return str.replace("K","000");
     }
 	
     public HashMap<String,Object> getSNCounters(Document doc, String url) 
     throws Exception  
 	{
     	HashMap<String,Object> snCounters = new HashMap<String,Object>();
 		snCounters.put("facebook", null);
 		snCounters.put("twitter", null);
     	Document docSN;
 		
 		Elements frameTag = doc.getElementsByTag(FRAME_TAG);
 		if (!frameTag.isEmpty()) {
 			Element facebookTag = frameTag.first();   //Should be the Facebook one. WARNING.
 			String urlSN = facebookTag.attr("src");
 			String content = getUrlContent(Util.toURI(urlSN));
 			docSN = Jsoup.parse(content);
 			Element facebookStats = docSN.getElementById(FACEBOOK_STATS);
 			if (facebookStats != null) {
 				String shared = facebookStats.text();
 				shared = shared.replaceAll("[^0-9]+", " ");
 				shared = shared.trim();
 				if (shared.isEmpty())
 					snCounters.put("facebook","0");						
 				else {
 					if (isNumeric(shared))
 						snCounters.put("facebook",shared);
 					else
 						snCounters.put("facebook","0");
 				}
 			}
 			
 			//Get Twitter counter
 			String twURL = TWITTER_URL_P + URLEncoder.encode(url, "utf-8") + 
 				    	   TWITTER_URL_S;
 			content = getUrlContent(twURL);
 			docSN = Jsoup.parse(content);
 			String textElement = docSN.getElementsByTag("body").text();
 			String twCounter = textElement.substring(textElement.indexOf(":") + 1, textElement.indexOf(","));
 			if (isNumeric(twCounter))
 				snCounters.put("twitter", twCounter);
 			else
 				snCounters.put("twitter", "0");
 		}
 		
     	return snCounters;
 	}
     
     public HashMap<String,Object> getIdeaSNCounters(Document doc, String url) 
     throws Exception  
 	{
     	HashMap<String,Object> snCounters = new HashMap<String,Object>();
 		snCounters.put("facebook", null);
 		snCounters.put("twitter", null);
 		Document docSN;
     	
 		String content, urlSN;
 		
 		Elements facebook = doc.getElementsByAttributeValue("class", "like");
 		if (!facebook.isEmpty()) {
 			Elements facebookTag = facebook.first().getElementsByTag(FRAME_TAG);
 			urlSN = facebookTag.first().attr("src");
 			urlSN = URLDecoder.decode(urlSN, "UTF-8");
 			content = getUrlContent(Util.toURI(urlSN));
 			docSN = Jsoup.parse(content);
 			Elements facebookStats = docSN.getElementsByClass("pluginCountTextDisconnected");
 			if (!facebookStats.isEmpty()) {
 				String shared = facebookStats.first().text();
 				shared = shared.replaceAll("[^0-9]+", " ");
 				shared = shared.trim();
 				if (shared.isEmpty())
 					snCounters.put("facebook","0");						
 				else {
 					if (isNumeric(shared))
 						snCounters.put("facebook",shared);
 					else
 						snCounters.put("facebook","0");
 				}
 			}
 			else {
 				Util.printMessage("Couldn't get the facebook counter of the idea: " + url, "severe", logger);
 			}
 		}
 		
 		//Get Twitter counter
		String twURL = TWITTER_URL_P + URLEncoder.encode(url, "utf-8") + 
			    	   TWITTER_URL_S;
 		content = getUrlContent(twURL);
 		docSN = Jsoup.parse(content);
 		String textElement = docSN.getElementsByTag("body").text();
 		String twCounter = textElement.substring(textElement.indexOf(":") + 1, textElement.indexOf(","));
 		if (isNumeric(twCounter))
 			snCounters.put("twitter", twCounter);
 		else
 			snCounters.put("twitter", "0");
 		
     	return snCounters;
 	}
     
     private boolean isNumeric(String num)
     {
     	try {
     		Integer.parseInt(num);
     	}
     	catch(NumberFormatException e) {
     		return false;
     	}
     	return true;
     }
 }
