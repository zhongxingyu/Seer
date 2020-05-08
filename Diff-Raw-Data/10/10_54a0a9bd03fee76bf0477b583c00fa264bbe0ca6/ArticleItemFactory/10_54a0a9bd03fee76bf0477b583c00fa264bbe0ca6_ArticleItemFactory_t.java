 package edu.sl.grabalyze.grabber.factory;
 
 import edu.sl.grabalyze.dao.ArticleDAO;
 import edu.sl.grabalyze.entity.Article;
 import edu.sl.grabalyze.grabber.factory.util.Distributor;
 import edu.sl.grabalyze.grabber.strategy.gazetaua.GazetaUaArticleStrategy;
 import edu.sl.grabalyze.grabber.strategy.GrabberStrategy;
 
 import java.util.*;
 
 public class ArticleItemFactory extends GrabberStrategies {
 
     private ArticleDAO articleDAO;
     private int count;
     private int offset;
 
     public ArticleItemFactory(int count, int offset) {
         this.count = count;
         this.offset = offset;
     }
 
     public void setArticleDAO(ArticleDAO articleDAO) {
         this.articleDAO = articleDAO;
     }
 
     public List<GrabberStrategy> createStrategies(int threads) {
         System.out.println("Requesting for article urls");
         List<GrabberStrategy> result = new ArrayList<>(count);
         List<Article> articles = articleDAO.getNotProcessedArticles(count, offset);
         List<List<Article>> distr = new Distributor<Article>(articles).distribute(threads);
         for (List<Article> list : distr) {
             HashMap<Long, String> map = new HashMap<>(count);
             for (Article a : list) {
                 map.put(a.getId(), a.getUrl());
             }
             GrabberStrategy strategy = getStrategyFactory().createItemStrategy(map);
             result.add(strategy);
         }
         System.out.println("Got " + articles.size() + " articles for " + threads + " workers.");
         if (articles.size() > 0)
            System.out.println("Dates:" + articles.get(articles.size()-1).getDate() + " to " + articles.get(0).getDate());
         return result;
     }
 }
