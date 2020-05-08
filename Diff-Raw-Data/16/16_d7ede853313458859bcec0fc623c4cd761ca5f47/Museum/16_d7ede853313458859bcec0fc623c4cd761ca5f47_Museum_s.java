 package com.ngdb.web.pages;
 
 import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.MuseumFilter;
 import com.ngdb.entities.Population;
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.element.Tag;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.Publisher;
 import com.ngdb.entities.reference.ReferenceService;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.Filter;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.ioc.Messages;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.Request;
 import org.hibernate.Session;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class Museum {
 
     @Property
     private Platform platform;
 
     @Property
     private Origin origin;
 
     @Property
     private Publisher publisher;
 
     @Inject
     private ReferenceService referenceService;
 
     @Persist
     private MuseumFilter filter;
 
     @Inject
     private ArticleFactory articleFactory;
 
     @Inject
     private Request request;
 
     @Inject
     private Population population;
 
     @Inject
     private Session session;
 
     @Inject
     private Messages messages;
 
     void onActivate() {
         if (filter == null || "true".equals(request.getParameter("display-all"))) {
             filter = new MuseumFilter(articleFactory, session);
             filter.filterByGames();
             filter.filterByOrigin(referenceService.findOriginByTitle("Japan"));
             filter.filterByPlatform(referenceService.findPlatformByName("AES"));
         }
         filter.initializeWithUrlParameters(request, referenceService, population);
     }
 
     boolean onActivate(User user) {
         filter = new MuseumFilter(articleFactory, session);
         filter.filterByUser(user);
         return true;
     }
 
     boolean onActivate(String filterName, String value) {
         if (value == null) {
             onActivate();
             return true;
         }
        filter = new MuseumFilter(articleFactory, session);
         Filter filter = Filter.valueOf(Filter.class, filterName);
         switch (filter) {
         case byOrigin:
             this.filter.filterByOrigin(referenceService.findOriginByTitle(value));
             break;
         case byPlatform:
             this.filter.filterByPlatform(referenceService.findPlatformByName(value));
             break;
         case byPublisher:
             this.filter.filterByPublisher(referenceService.findPublisherBy(Long.valueOf(value)));
             break;
         case byTag:
             this.filter.filterByTag(value);
             break;
         case byNgh:
             this.filter.filterByNgh(value);
             break;
         case byReleaseDate:
             this.filter.filterByReleaseDate(toReleaseDate(value));
             break;
         }
         return true;
     }
 
     private Date toReleaseDate(String value) {
         try {
             return new SimpleDateFormat("yyyy-MM-dd").parse(value);
         } catch (ParseException e) {
             return new Date();
         }
     }
 
     public List<Article> getArticles() {
         return filter.getArticles();
     }
 
     Object onActionFromClearFilters() {
         filter.clear();
         return this;
     }
 
     public String getFilterUrl() {
         return filter.getFilterUrl();
     }
 
     public int getNumResults() {
         return getArticles().size();
     }
 
     public long getNumHardwares() {
         return filter.getNumHardwares();
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
 
     public long getNumAccessories() {
         return filter.getNumAccessories();
     }
 
     public List<Platform> getPlatforms() {
         return referenceService.getPlatforms();
     }
 
     public boolean isArticleInThisPlatform() {
         return getNumArticlesInThisPlatform() > 0;
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
 
     public int getNumArticlesInThisPlatform() {
         return filter.getNumArticlesInThisPlatform(platform);
     }
 
     public List<Origin> getOrigins() {
         return referenceService.getOrigins();
     }
 
     public boolean isFilteredByThisOrigin() {
         return filter.isFilteredBy(origin);
     }
 
     public boolean isArticleInThisOrigin() {
         if (origin == null) {
             return false;
         }
         return filter.getNumArticlesInThisOrigin(origin) > 0;
     }
 
     Object onActionFromFilterOrigin(Origin origin) {
         filter.filterByOrigin(origin);
         return this;
     }
 
     public int getNumArticlesInThisOrigin() {
         return filter.getNumArticlesInThisOrigin(origin);
     }
 
     public boolean isArticleInThisPublisher() {
         return filter.getNumArticlesInThisPublisher(publisher) > 0;
     }
 
     public boolean isFilteredByThisPublisher() {
         if (publisher == null) {
             return false;
         }
         return filter.isFilteredBy(publisher);
     }
 
     Object onActionFromFilterPublisher(Publisher publisher) {
         filter.filterByPublisher(publisher);
         return this;
     }
 
     public List<Publisher> getPublishers() {
         return referenceService.getPublishers();
     }
 
     public User getUser() {
         return filter.getFilteredUser();
     }
 
     public boolean isFilteredByGames() {
         return filter.isFilteredByGames();
     }
 
     public int getNumArticlesInThisPublisher() {
         return filter.getNumArticlesInThisPublisher(publisher);
     }
 
     public String getTag() {
         return filter.getFilteredTag();
     }
 
     public Date getReleaseDate() {
         return filter.getFilteredReleaseDate();
     }
 
     public String getQueryLabel() {
         return filter.getQueryLabel();
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
 
     public String getTitle() {
         return messages.format("museum.usermuseum",filter.getFilteredUser().getLogin());
     }
 
 }
