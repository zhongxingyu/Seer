 package com.tryrosberry.ololostfilm.logic.api;
 
 import com.tryrosberry.ololostfilm.logic.storage.ConstantStorage;
 import com.tryrosberry.ololostfilm.ui.models.NewsFeedItem;
 import com.tryrosberry.ololostfilm.ui.models.Serial;
 
 import org.htmlcleaner.ContentNode;
 import org.htmlcleaner.HtmlCleaner;
 import org.htmlcleaner.TagNode;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class HtmlParser {
 
     public HtmlParser(){}
 
     public static ArrayList<Serial> parseSerials(String response){
         return getSerials(parse(response, "a", "class", "bb_a"));
     }
 
     public static ArrayList<NewsFeedItem> parseNews(String response){
         return getNews(parse(response, "div", "class", "content_body"));
     }
 
     private static List<TagNode> parse(String response, String nodeName, String tagvalue, String CSSClassname) {
         TagNode rootNode = getRootNode(response);
         return getLinksByClass(rootNode, nodeName, tagvalue, CSSClassname);
     }
 
     private static ArrayList<Serial> getSerials(List<TagNode> nodes) {
         ArrayList<Serial> serials = new ArrayList<Serial>();
         for (TagNode node : nodes){
             serials.add(new Serial(node));
         }
         return serials;
     }
 
     private static ArrayList<NewsFeedItem> getNews(List<TagNode> nodes) {
         ArrayList<NewsFeedItem> newsFeed = new ArrayList<NewsFeedItem>();
         if(nodes.size() >= 1){
             TagNode newsNodes = nodes.get(0);
             List<TagNode> newsTagNodes = newsNodes.getChildTagList();
             List<TagNode> newsTagLinks = getLinksByClass(newsNodes, "a", "class", "a_full_news");
 
             for (int i = 0; i < newsTagNodes.size(); i++){
                 TagNode feedTagNode = newsTagNodes.get(i);
                 if(feedTagNode.getName().equals("h1")){
                     NewsFeedItem feedItem = new NewsFeedItem();
                     feedItem.title = getContent(feedTagNode);
                     feedItem.image = ConstantStorage.BASE_URL + HtmlParser.getLinksByClass(newsTagNodes.get(i+1),"img").get(0).getAttributeByName("src");
                     feedItem.description = getContent(newsTagNodes.get(i+2));
                     if(i < newsTagLinks.size())feedItem.link = ConstantStorage.BASE_URL + (newsTagLinks.get(i).getAttributeByName("href"));
                     newsFeed.add(feedItem);
                 }
 
             }
         }
 
         return newsFeed;
     }
 
     private static TagNode getRootNode(String response) {
         HtmlCleaner cleaner = null;
         try {
             cleaner = new HtmlCleaner();
         } catch (Exception e) {
             e.printStackTrace();
         }
 
         return cleaner.clean(response);
     }
 
     public static String getContent(TagNode node) {
         StringBuilder result = new StringBuilder();
         List<Object> items = node.getAllChildren();
         for (Object item : items) {
             if (item instanceof ContentNode) {
                String content = ((ContentNode) item).getContent();
                result.append(content);
             }
         }
         return result.toString();
     }
 
     private static List<TagNode> getLinksByClass(TagNode rootNode, String nodeName, String TagVal, String CSSClassname){
         List<TagNode> linkList = new ArrayList<TagNode>();
 
         //Выбираем все ссылки
         TagNode linkElements[] = rootNode.getElementsByName(nodeName, true);
         for (int i = 0; linkElements != null && i < linkElements.length; i++)
         {
             //получаем атрибут по имени
             String classType = linkElements[i].getAttributeByName(TagVal);
             //если атрибут есть и он эквивалентен искомому, то добавляем в список
             if (classType != null && classType.equals(CSSClassname))
             {
                 linkList.add(linkElements[i]);
             }
         }
 
         return linkList;
     }
 
     public static List<TagNode> getLinksByClass(TagNode rootNode, String nodeName){
         return rootNode.getElementListByName(nodeName, true);
     }
 
 }
