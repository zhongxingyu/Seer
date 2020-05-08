 package com.ngdb.web.pages;
 
 import com.google.common.collect.Collections2;
 import com.ngdb.Functions;
 import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.Population;
 import com.ngdb.entities.WishBoxFilter;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.entities.shop.Wish;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.Filter;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.Request;
 import org.hibernate.Session;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class WishBox {
 
     @Property
     private Wish wish;
 
     @Inject
     private com.ngdb.entities.WishBox wishBox;
 
     @Inject
     private Population population;
 
     @Persist
     private WishBoxFilter filter;
 
     @Property
     private Platform platform;
 
     @Property
     private Origin origin;
 
     @Inject
     private ReferenceService referenceService;
 
     @Inject
     private Session session;
 
     @Inject
     private Request request;
 
     void onActivate() {
         if (filter == null  || "true".equals(request.getParameter("display-all"))) {
             filter = new WishBoxFilter(wishBox, session);
             filter.filterByGames();
             filter.filterByOrigin(referenceService.findOriginByTitle("Japan"));
             filter.filterByPlatform(referenceService.findPlatformByName("AES"));
         }
     }
 
     boolean onActivate(String filterName, String value) {
         if (value == null) {
             onActivate();
             return true;
         }
         filter = new WishBoxFilter(wishBox, session);
        Filter filter = Filter.valueOf(Filter.class, filterName);
        switch (filter) {
        case byUser:
            User user = population.findById(Long.valueOf(value));
            this.filter.filterByUser(user);
            break;
         }
         return true;
     }
 
     public Collection<Article> getWishes() {
         return filter.getWishedArticles();
     }
 
     @Inject
     private ArticleFactory articleFactory;
 
     public String getMainPictureUrl() {
         wish = (Wish) session.load(Wish.class, wish.getId());
         return wish.getCover("small");
     }
 
     Object onActionFromClearFilters() {
         filter.clear();
         return this;
     }
 
     Object onActionFromSelectHardwares() {
         filter.filterByHardwares();
         return this;
     }
 
     Object onActionFromSelectGames() {
         filter.filterByGames();
         return this;
     }
 
     Object onActionFromSelectAccessories() {
         filter.filterByAccessories();
         return this;
     }
 
     public long getNumGames() {
         return filter.getNumGames();
     }
 
     public long getNumHardwares() {
         return filter.getNumHardwares();
     }
 
     public long getNumAccessories() {
         return filter.getNumAccessories();
     }
 
     public List<Platform> getPlatforms() {
         return referenceService.getPlatforms();
     }
 
     public boolean isWishInThisPlatform() {
         return getNumWishesInThisPlatform() > 0;
     }
 
     public boolean isFilteredByThisPlatform() {
         if (platform == null) {
             return false;
         }
         return filter.isFilteredBy(platform);
     }
 
     Object onActionFromFilterPlatform(Platform platform) {
         filter.filterByPlatform(platform);
         return this;
     }
 
     public int getNumWishesInThisPlatform() {
         return filter.getNumWishesInThisPlatform(platform);
     }
 
     public List<Origin> getOrigins() {
         return referenceService.getOrigins();
     }
 
     public boolean isFilteredByThisOrigin() {
         return filter.isFilteredBy(origin);
     }
 
     public boolean isWishInThisOrigin() {
         if (origin == null) {
             return false;
         }
         return getNumWishesInThisOrigin() > 0;
     }
 
     Object onActionFromFilterOrigin(Origin origin) {
         filter.filterByOrigin(origin);
         return this;
     }
 
     public int getNumWishesInThisOrigin() {
         return filter.getNumWishesInThisOrigin(origin);
     }
 
     public User getUser() {
         return filter.getFilteredUser();
     }
 
     public boolean isFilteredByGames() {
         return filter.isFilteredByGames();
     }
 
     public String getQueryLabel() {
         return filter.getQueryLabel();
     }
 
     public int getNumResults() {
         return getWishes().size();
     }
 
     public String getGameSelected() {
         return filter.isFilteredByGames() ? "selected":"";
     }
 
     public String getHardwareSelected() {
         return filter.isFilteredByHardwares() ? "selected":"";
     }
 
     public String getAccessorySelected() {
         return filter.isFilteredByAccessories() ? "selected":"";
     }
 }
