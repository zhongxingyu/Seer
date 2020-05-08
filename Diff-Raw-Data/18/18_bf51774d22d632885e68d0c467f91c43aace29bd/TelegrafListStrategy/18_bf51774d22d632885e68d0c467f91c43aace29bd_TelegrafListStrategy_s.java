 package edu.sl.grabalyze.grabber.strategy.telegraf;
 
 import edu.sl.grabalyze.dao.ArticleDAO;
 import edu.sl.grabalyze.entity.Article;
 import edu.sl.grabalyze.grabber.strategy.GrabberStrategy;
 import edu.sl.grabalyze.grabber.strategy.utils.Extractor;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class TelegrafListStrategy implements GrabberStrategy {
 
     private static final String HOST = "http://telegraf.com.ua";
     private static final String NEWS_LINK = HOST + "/date/%s/page/%d/";
 
     private static final String CONTENT_EXIST = "divh5";
     private static final String START_STRING = "titleBlockForNews";
    private static final String END_STRING = "page-numbers current";
 
     private static final String LIST_ITEM = "categoryLatestNewsTextHeight";
 
     private static final long DAY = 1000 * 60 * 60 * 24;
 
     private final SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
 
     private Date dateTo, dateFrom, startDate;
     private int page;
     private int pageCount;
 
     private ArticleDAO articleDAO;
 
     public TelegrafListStrategy(Date from, Date to) {
         this.dateFrom = new Date(from.getTime());
         this.startDate = new Date(from.getTime());
         this.dateTo = new Date(to.getTime());
         page = 1;
         pageCount = -1;
     }
 
     public void setArticleDAO(ArticleDAO articleDAO) {
         this.articleDAO = articleDAO;
     }
 
     @Override
     public void processHtml(String html) {
         if (html == null || html.isEmpty() || !html.contains(CONTENT_EXIST)) {
             System.out.println("Page is empty");
             dateFrom.setTime(dateFrom.getTime() + DAY);
             page = 1;
             pageCount = -1;
             return;
         }
         String content = html.substring(html.indexOf(START_STRING), html.indexOf(END_STRING));
         if (pageCount == -1) {
             if (!html.contains(END_STRING)) {
                 pageCount = 1;
             } else {
                 Extractor extractor = new Extractor();
                 extractor.setString("Страница 1 из ", "</span>");
                 String countStr = extractor.extractValue(content);
                pageCount = Integer.valueOf(countStr);
             }
         }
 
         List<Article> result = new ArrayList<Article>();
         Extractor ex = new Extractor();
         ex.setIndex(0);
         while (true) {
             ex.setIndex(content.indexOf(LIST_ITEM, ex.getIndex()));
             if (ex.getIndex() == -1)
                 break;
 
             Article art = new Article();
 
             ex.setString("<a href=\"", "\">");
             art.setUrl(ex.extractValue(content));
 
             ex.setString("<div class=\"divh5\">", "</div>");
             art.setTitle(ex.extractValue(content));
 
             ex.setString("class=\"addToRead\" id=\"", "\" title");
             art.setId(Long.valueOf(ex.extractValue(content)));
 
             art.setCategoryCode(art.getUrl().replaceAll(HOST + "/", "")
                     .replaceAll("/" + art.getId() + ".*", ""));
 
             art.setDate(dateFrom);
 
             result.add(art);
         }
 
         articleDAO.batchInsert(result);
     }
 
     @Override
     public double getProgress() {
         return (1.0 * (dateFrom.getTime() - startDate.getTime()) + 1.0 * (page - 1) / pageCount * DAY)
                 / (dateTo.getTime() - startDate.getTime() + DAY);
     }
 
     @Override
     public String nextUrl() {
         if (pageCount == -1)
             return getUrl();
         else {
             if (page > pageCount) {
                 dateFrom.setTime(dateFrom.getTime() + DAY);
                 pageCount = -1;
                 page = 1;
                 return nextUrl();
             } else {
                 return getUrl();
             }
         }
     }
 
     @Override
     public boolean hasUrl() {
         return dateFrom.before(dateTo) || dateFrom.equals(dateTo) && (page <= pageCount || pageCount == -1);
     }
 
     private String getUrl() {
         return String.format(NEWS_LINK, format.format(dateFrom), page++);
     }
 }
