 package org.smartreaction.boardgamegeek.business;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.select.Elements;
 import org.smartreaction.boardgamegeek.BoardGameGeekConstants;
 import org.smartreaction.boardgamegeek.model.GeekList;
 import org.smartreaction.boardgamegeek.model.GeekListComment;
 import org.smartreaction.boardgamegeek.model.GeekListDetail;
 import org.smartreaction.boardgamegeek.model.GeekListEntry;
 import org.smartreaction.boardgamegeek.services.BoardGameGeekService;
 import org.smartreaction.boardgamegeek.util.DateUtil;
 import org.smartreaction.boardgamegeek.xml.geeklist.Comment;
 import org.smartreaction.boardgamegeek.xml.geeklist.Geeklist;
 import org.smartreaction.boardgamegeek.xml.geeklist.Item;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.json.Json;
 import javax.json.JsonArray;
 import javax.json.JsonObject;
 import javax.json.JsonReader;
 import javax.xml.bind.JAXBException;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.zip.GZIPInputStream;
 
 @Stateless
 public class GeekListUtil
 {
     @EJB
     BoardGameGeekService boardGameGeekService;
 
     @EJB
     BoardGameCache boardGameCache;
 
     BoardGameGeekUtil boardGameGeekUtil;
 
     @PostConstruct
     public void setup()
     {
         boardGameGeekUtil = new BoardGameGeekUtil(boardGameCache);
     }
 
     public List<GeekList> getGeekLists(int page, String sort) throws IOException
     {
         List<GeekList> geekLists = new ArrayList<>();
 
         String url = BoardGameGeekConstants.BBG_WEBSITE + "/geeklist/module?ajax=1&domain=boardgame&nosession=1&objectid=0&objecttype=&pageid=" + page + "&showcontrols=1&showcount=12&sort=" + sort + "&tradelists=0&version=v2";
 
         URLConnection connection = new URL(url).openConnection();
         connection.setRequestProperty("Accept", "application/json, text/plain, */*");
         connection.setRequestProperty("Accept-Charset", "UTF-8");
         InputStream response = connection.getInputStream();
 
         if ("gzip".equals(connection.getContentEncoding())) {
             response = new GZIPInputStream(response);
         }
 
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response, Charset.forName("UTF-8")));
 
         try (JsonReader reader = Json.createReader(bufferedReader)) {
             JsonObject jsonObject = reader.readObject();
 
             JsonArray lists = jsonObject.getJsonArray("lists");
 
             for (int i = 0; i < lists.size(); i++) {
                 JsonObject item = lists.getJsonObject(i);
                 geekLists.add(getGeekList(item));
             }
         }
         catch (Exception e) {
             e.printStackTrace();
             for (String headerKey : connection.getHeaderFields().keySet()) {
                 System.out.println(headerKey + ": " + connection.getHeaderField(headerKey));
             }
         }
 
         return geekLists;
     }
 
     GeekList getGeekList(JsonObject item)
     {
         GeekList geekList = new GeekList();
         geekList.setThumbs(Integer.parseInt(item.getString("numpositive")));
         geekList.setTitle(item.getString("title"));
         String link = BoardGameGeekConstants.BBG_WEBSITE + item.getString("href");
         geekList.setId(getId(link));
         geekList.setLink(link);
         geekList.setCreator(item.getString("username"));
         geekList.setEntries(Integer.parseInt(item.getString("numitems")));
         return geekList;
     }
 
     private long getId(String link)
     {
         int start = link.indexOf("/geeklist/");
         return Long.valueOf(link.substring(start + 10, link.lastIndexOf("/")));
     }
 
     public GeekListDetail getGeekListDetail(long geekListId) throws IOException, JAXBException
     {
         Geeklist xmlGeekList = boardGameGeekService.getGeekList(geekListId);
 
         GeekListDetail detail = new GeekListDetail();
         detail.setGeekListId(geekListId);
         detail.setTitle(xmlGeekList.getTitle());
         detail.setDescription(boardGameGeekUtil.convertBoardGameGeekXmlText(xmlGeekList.getDescription()));
         detail.setUsername(xmlGeekList.getUsername());
         detail.setComments(getCommentsFromXmlComments(xmlGeekList.getComment()));
         detail.setEntries(getEntriesFromXmlItems(xmlGeekList.getItem()));
         detail.setThumbs(xmlGeekList.getThumbs().intValue());
 
         return detail;
     }
 
     private List<GeekListComment> getCommentsFromXmlComments(List<Comment> xmlComments)
     {
         List<GeekListComment> comments = new ArrayList<>(xmlComments.size());
         for (Comment xmlComment : xmlComments) {
             GeekListComment comment = new GeekListComment();
             comment.setComment(boardGameGeekUtil.convertBoardGameGeekXmlText(xmlComment.getContent()));
             comment.setUsername(xmlComment.getUsername());
             comment.setThumbs(xmlComment.getThumbs().intValue());
             SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
             try {
                 comment.setPostDate(sdf.parse(xmlComment.getPostdate()));
             }
             catch (ParseException e) {
                 e.printStackTrace();
             }
             comments.add(comment);
         }
         return comments;
     }
 
     private List<GeekListEntry> getEntriesFromXmlItems(List<Item> xmlItems)
     {
         List<GeekListEntry> entries = new ArrayList<>(xmlItems.size());
         for (Item xmlItem : xmlItems) {
             GeekListEntry entry = new GeekListEntry();
             entry.setGame(boardGameCache.getGame(xmlItem.getObjectid().longValue()));
             entry.setDescription(boardGameGeekUtil.convertBoardGameGeekXmlText(xmlItem.getBody()));
             entry.setComments(getCommentsFromXmlComments(xmlItem.getComment()));
             entry.setThumbs(xmlItem.getThumbs().intValue());
             entry.setUsername(xmlItem.getUsername());
             entry.setEntryId(xmlItem.getId().longValue());
             SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
             try {
                 entry.setPostDate(sdf.parse(xmlItem.getPostdate()));
             }
             catch (ParseException e) {
                 e.printStackTrace();
             }
             entries.add(entry);
         }
         return entries;
     }
 
     public GeekListDetail getGeekListDetailNew(long geekListId) throws IOException
     {
         GeekListDetail geekListDetail = new GeekListDetail();
         geekListDetail.setGeekListId(geekListId);
 
         String url = BoardGameGeekConstants.BBG_WEBSITE + "/geeklist/" + geekListId;
 
         Document document = Jsoup.connect(url).timeout(30000).get();
 
         Element titleElement = document.getElementsByClass("geeklist_title").first();
         geekListDetail.setTitle(titleElement.text());
 
         Element geekListSummary = document.getElementsByAttributeValue("data-objecttype", "geeklist").first();
         Element summaryContent = geekListSummary.getElementsByTag("dd").get(1);
 
         geekListDetail.setDescription(getGeekListDescription(summaryContent));
 
         geekListDetail.setThumbs(getThumbs(summaryContent, "recsbig"));
 
         List<GeekListEntry> entries = getGeekListEntries(document);
 
         geekListDetail.setEntries(entries);
 
         Element commentsElement = document.getElementById("morecomments_geeklist_" + geekListDetail.getGeekListId());
 
         if (commentsElement != null) {
             geekListDetail.setComments(getGeekListComments(commentsElement));
         }
 
         String nextPageLink = getNextPageLink(document);
         while (nextPageLink != null) {
             document = Jsoup.connect(BoardGameGeekConstants.BBG_WEBSITE + nextPageLink).timeout(30000).get();
 
             geekListDetail.getEntries().addAll(getGeekListEntries(document));
 
             nextPageLink = getNextPageLink(document);
         }
 
         return geekListDetail;
     }
 
     private String getNextPageLink(Document document)
     {
         String nextPageLink = null;
         Elements nextPageElements = document.getElementsByAttributeValue("title", "next page");
         if (!nextPageElements.isEmpty()) {
             Element nextPageElement = nextPageElements.get(0);
             nextPageLink = nextPageElement.attr("href");
         }
         return nextPageLink;
     }
 
     private List<GeekListEntry> getGeekListEntries(Document document)
     {
         Elements items = document.getElementsByAttributeValue("data-objecttype", "listitem");
 
         List<GeekListEntry> entries = new ArrayList<>(items.size());
 
         for (Element item : items) {
             entries.add(getGeekListEntry(item));
         }
         return entries;
     }
 
     private String getGeekListDescription(Element summaryContent)
     {
         StringBuilder sb = new StringBuilder();
 
         for (Node node : summaryContent.childNodes()) {
             if(!node.outerHtml().contains("\"recommend_block\"") && !node.outerHtml().contains("\"geeklist_tags\"")) {
                 sb.append(node.outerHtml());
             }
         }
 
         return boardGameGeekUtil.includeBoardGameGeekDomainInAbsoluteLinks(sb.toString());
     }
 
     private GeekListEntry getGeekListEntry(Element item)
     {
         GeekListEntry entry = new GeekListEntry();
 
         entry.setEntryId(Long.parseLong(item.attr("data-objectid")));
 
         Element itemTitleElement = item.getElementsByClass("geeklist_item_title").first();
 
         Elements gameLinks = itemTitleElement.getElementsByAttributeValueStarting("href", "/boardgame/");
 
         if (gameLinks.size() > 0) {
             String firstGameLink = gameLinks.first().attr("href");
             String linkMinusBoardGameText = firstGameLink.substring(11);
             String gameIdString = linkMinusBoardGameText.substring(0, linkMinusBoardGameText.indexOf("/"));
             try {
                 entry.setGame(boardGameCache.getGame(Long.parseLong(gameIdString)));
             }
             catch (NumberFormatException e) {
                 e.printStackTrace();
             }
         }
 
         Element itemDescriptionElement = item.getElementsByClass("doubleright").first();
         entry.setDescription(boardGameGeekUtil.includeBoardGameGeekDomainInAbsoluteLinks(itemDescriptionElement.html()));
 
         Element commentsElement = item.parent().getElementById("comments_" + entry.getEntryId());
 
         if (commentsElement != null) {
             entry.setComments(getGeekListComments(commentsElement));
         }
 
         Element thumbSection = item.children().get(1);
         entry.setThumbs(getThumbs(thumbSection));
 
         Element informationElement = item.children().get(1).getElementsByClass("information").get(0);
         Date postedDate = getPostedDate(informationElement);
         entry.setPostDate(postedDate);
 
         return entry;
     }
 
     private Date getPostedDate(Element informationElement)
     {
         Date postedDate = null;
        Element postingDateElement = informationElement.children().get(0).children().get(0);
        TextNode postedDateNode = (TextNode) postingDateElement.childNodes().get(1);
        String postedDateString = postedDateNode.text().substring(8);
         try {
             postedDate = DateUtil.getDateFromBggString(postedDateString);
         }
        catch (ParseException e) {
             e.printStackTrace();
         }
         return postedDate;
     }
 
     private List<GeekListComment> getGeekListComments(Element commentsElement)
     {
         Elements commentElements = commentsElement.getElementsByAttributeValueStarting("id", "comment_");
 
         List<GeekListComment> comments = new ArrayList<>(commentElements.size());
 
         for (Element commentElement : commentElements) {
             GeekListComment comment = getGeekListComment(commentElement);
 
             comments.add(comment);
         }
         return comments;
     }
 
     private GeekListComment getGeekListComment(Element commentElement)
     {
         GeekListComment comment = new GeekListComment();
 
         Elements children = commentElement.children();
 
         Element usernameAndComment = children.get(0);
 
         Element commentUser = usernameAndComment.children().get(0);
         String username = commentUser.getElementsByAttribute("data-username").first().attr("data-username");
         comment.setUsername(username);
 
         comment.setComment(boardGameGeekUtil.includeBoardGameGeekDomainInAbsoluteLinks(usernameAndComment.children().get(1).html()));
 
         Element thumbSection = children.get(1);
         comment.setThumbs(getThumbs(thumbSection));
 
         return comment;
     }
 
     private int getThumbs(Element thumbSection)
     {
         return getThumbs(thumbSection, "recs");
     }
 
     private int getThumbs(Element thumbSection, String recommendSection)
     {
         Element recs = thumbSection.getElementsByClass(recommendSection).get(0);
 
         int thumbs;
         try {
             thumbs = Integer.parseInt(recs.children().get(0).text());
         }
         catch (NumberFormatException e) {
             thumbs = 0;
         }
         return thumbs;
     }
 }
