 package com.ngdb.entities;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.reference.Platform;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.hibernate.Query;
 import org.hibernate.Session;
 
 import javax.annotation.Nullable;
 import java.util.*;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static java.util.Collections.sort;
 
 public class Charts {
 
     @Inject
     private Session session;
 
     @Inject
     private ArticleFactory articleFactory;
 
     @Inject
     private Reviewer reviewer;
 
     public Collection<Top100Item> findTop100OfGamesInCollection(Platform platform) {
         String sqlQuery = "SELECT g.id, g.title, g.origin_title, COUNT(co.article_id) FROM CollectionObject co, ngdb.Game g WHERE g.id = co.article_id AND g.platform_short_name = '"+platform.getShortName()+"' GROUP BY co.article_id ORDER BY COUNT(co.article_id) DESC";
         return findTop100OfGames(sqlQuery);
     }
 
     public Collection<Top100Item> findTop100OfGamesInWishlist(Platform platform) {
         String sqlQuery = "SELECT g.id, g.title, g.origin_title, COUNT(w.article_id) FROM Wish w, ngdb.Game g WHERE g.id = w.article_id AND g.platform_short_name = '"+platform.getShortName()+"' GROUP BY w.article_id ORDER BY COUNT(w.article_id) DESC";
         return findTop100OfGames(sqlQuery);
     }
 
     private Collection<Top100Item> findTop100OfGames(String sqlQuery) {
         Query query = session.createSQLQuery(sqlQuery);
         query = query.setResultTransformer(new Top100ItemResultTransformer());
         return query.list();
     }
 
     public Collection<Top100Item> findTop100OfGamesWithRating() {
         Map<String, Double> marks = new HashMap<String, Double>();
         List<Game> allGamesWithReviews = articleFactory.findAllGamesWithReviews();
         Collection<Game> games = filterByNGH(allGamesWithReviews);
         List<Game> filteredGames = new ArrayList<Game>(orderByAverageMark(marks, games));
         return transform(filteredGames, fromGameToTop100Item(marks));
     }
 
     private Collection<Game> filterByNGH(Collection<Game> games) {
         final Set<String> nghs = new HashSet<String>();
         games = filter(games, new Predicate<Game>() {
             @Override
             public boolean apply(@Nullable Game game) {
                 String ngh = game.getNgh();
                 boolean addable = !nghs.contains(ngh);
                 nghs.add(ngh);
                 return addable;
             }
         });
         return games;
     }
 
     private List<Game> orderByAverageMark(final Map<String, Double> marks, Collection<Game> games) {
         List<Game> filteredGames = new ArrayList<Game>(games);
         sort(filteredGames, new Comparator<Game>() {
             @Override
             public int compare(Game game1, Game game2) {
                Double mark1 = getMark1(game1);
                Double mark2 = getMark2(game2);
                 return mark2.compareTo(mark1);
             }
 
            private Double getMark2(Game game2) {
                Double mark2 = reviewer.getAverageMarkOf(game2);
                if(mark2 == null) {
                    mark2 = reviewer.getAverageMarkOf(game2);
                    marks.put(game2.getNgh(), mark2);
                }
                return mark2;
            }

            private Double getMark1(Game game1) {
                Double mark1 = marks.get(game1.getNgh());
                if(mark1 == null) {
                    mark1= reviewer.getAverageMarkOf(game1);
                    marks.put(game1.getNgh(), mark1);
                 }
                return mark1;
             }
         });
         return filteredGames;
     }
 
     private Function<Game, Top100Item> fromGameToTop100Item(final Map<String, Double> marks) {
         return new Function<Game, Top100Item>() {
             private String rank;
             private double oldMark = 0;
             private Integer idx = 1;
 
             @Override
             public Top100Item apply(@Nullable Game input) {
                 Long id = input.getId();
                 String title = input.getTitle();
                 String originTitle = input.getOriginTitle();
                 Double mark = marks.get(input.getNgh());
                 if(mark == null) {
                     rank = "";
                    mark = Double.MAX_VALUE;
                 } else {
                     if (mark.equals(oldMark)) {
                         rank = ".";
                     } else {
                         rank = idx.toString() + ".";
                     }
                 }
                 idx++;
                 oldMark = mark;
                 return new Top100Item(id, title, originTitle, mark.toString(), rank);
             }
         };
     }
 
     public Collection<Top100ShopItem> findTop100OfGamesRecentlySold(Platform platform) {
         return findTop100OfGamesRecentlyInMarket(platform, true);
     }
 
     public Collection<Top100ShopItem> findTop100OfGamesRecentlyInShop(Platform platform) {
         return findTop100OfGamesRecentlyInMarket(platform, false);
     }
 
     private Collection<Top100ShopItem> findTop100OfGamesRecentlyInMarket(Platform platform, boolean sold) {
         int flag = sold?1:0;
         String sqlQuery = "SELECT g.id, g.title, g.origin_title, si.customCurrency, si.priceInCustomCurrency, si.id, si.seller_id, s.title state_title, si.modification_date FROM State s, ShopItem si, Game g WHERE s.id = si.state_id AND si.article_id = g.id AND sold = "+flag+" AND g.platform_short_name = '"+platform.getShortName()+"' ORDER BY si.MODIFICATION_DATE DESC";
         return findTop100OfShopItems(sqlQuery);
     }
 
     private Collection<Top100ShopItem> findTop100OfShopItems(String sqlQuery) {
         Query query = session.createSQLQuery(sqlQuery);
         query = query.setResultTransformer(new Top100ShopItemResultTransformer());
         return query.list();
     }
 
 }
