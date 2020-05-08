 package fr.kaddath.apps.fluxx.controller;
 
 import fr.kaddath.apps.fluxx.comparator.FeedsComparator;
 import fr.kaddath.apps.fluxx.domain.AggregatedFeed;
 import fr.kaddath.apps.fluxx.domain.Feed;
 import fr.kaddath.apps.fluxx.model.CollectionDataModel;
 import fr.kaddath.apps.fluxx.service.AggregatedFeedService;
 import fr.kaddath.apps.fluxx.service.FeedFetcherService;
 import fr.kaddath.apps.fluxx.service.FeedService;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Logger;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.bean.ManagedBean;
 import javax.inject.Named;
 import javax.faces.context.FacesContext;
 import javax.faces.model.DataModel;
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletRequest;
 
 @Named(value = "editAggregatedFeed")
 @ManagedBean
 @SessionScoped
 public class EditAggregatedFeedBean implements Serializable {
 
     private static final FeedsComparator FEEDS_COMPARATOR = new FeedsComparator();
 
     @Inject
     private FeedService feedService;
 
     @Inject
     private FeedFetcherService feedFetcherService;
 
     @Inject
     private AggregatedFeedService aggregatedFeedService;
 
     @Inject
     private ManageAggregatedFeedBean aggregatedFeedBean;
 
     private static final String EDIT_AGGREGATEDFEED = "edit-aggregatedfeed";
 
     private String filter;
     private String id;
     private String newFeedUrl;
     private String name;
     private int numLastDay = 3;
     private transient CollectionDataModel feedsDataModel;
     private transient CollectionDataModel availableFeedsDataModel;
     private transient CollectionDataModel aggregatedFeedsDataModel;
     private List<Feed> availableListFeeds;
     private AggregatedFeed currentAggregatedFeed;
 
     private static final Logger LOG = Logger.getLogger(EditAggregatedFeedBean.class.getName());
 
     public String edit() {
         currentAggregatedFeed = aggregatedFeedBean.getUserAggregatedFeeds().getRowData();
         filter = currentAggregatedFeed.getName();
         reload();
         return EDIT_AGGREGATEDFEED;
     }
 
     public String addFeed() {
         Feed feed = getAvailableFeeds().getRowData();
         return addFeed(feed);
     }
 
     private String addFeed(Feed feed) {
         currentAggregatedFeed = aggregatedFeedService.addFeedToAggregatedFeed(feed, currentAggregatedFeed);
         reload();
        aggregatedFeedBean.buildUserAggregatedFeedsModel();
         return EDIT_AGGREGATEDFEED;
     }
 
     public String removeFeed() {
         currentAggregatedFeed.getFeeds().remove(getAggregatedFeeds().getRowData());
         currentAggregatedFeed = aggregatedFeedService.merge(currentAggregatedFeed);
         reload();
         return EDIT_AGGREGATEDFEED;
     }
 
     public String addNewFeed() {
         try {
             Feed newFeed = feedFetcherService.add(newFeedUrl);
             return addFeed(newFeed);
         } catch (Exception ex) {
             return "add-feed-failure";
         }
     }
 
     public String update() {
         currentAggregatedFeed.setName(name);
         currentAggregatedFeed.setNumLastDay(numLastDay);
         currentAggregatedFeed = aggregatedFeedService.merge(currentAggregatedFeed);
         return EDIT_AGGREGATEDFEED;
     }
 
     public String filter() {
         LOG.info("filter feeds with : "+filter);
         buildAvailableFeedsDataModel(filter);
         return EDIT_AGGREGATEDFEED;
     }
 
     private void reload() {
         buildInformations();
         buildAvailableFeedsDataModel();
         buildFeedsDataModel();
         buildAggregatedFeedsDataModel();
     }
 
     private void buildInformations() {
         this.id = currentAggregatedFeed.getAggregatedFeedId();
         this.numLastDay = currentAggregatedFeed.getNumLastDay();
         this.name = currentAggregatedFeed.getName();
     }
 
     public DataModel<Feed> getFeeds() {
         if (feedsDataModel == null) {
             buildFeedsDataModel();
         }
         return feedsDataModel.getDataModel();
     }
 
     private void buildFeedsDataModel() {
         this.feedsDataModel = new CollectionDataModel(currentAggregatedFeed.getFeeds());
     }
 
     public DataModel<Feed> getAvailableFeeds() {
         if (availableFeedsDataModel == null) {
             buildAvailableFeedsDataModel();
         }
         return availableFeedsDataModel.getDataModel();
     }
 
     private void buildAvailableFeedsDataModel() {
         LOG.info("filter:"+filter);
        buildAvailableFeedsDataModel(filter);
     }
 
     private void buildAvailableFeedsDataModel(String filter) {
         List<Feed> feeds = feedService.findAvailableFeedsByAggregatedFeedWithFilter(currentAggregatedFeed, filter);
         Collections.sort(feeds, FEEDS_COMPARATOR);
         this.availableFeedsDataModel = new CollectionDataModel(feeds);
     }
 
     public DataModel<Feed> getAggregatedFeeds() {
         if (aggregatedFeedsDataModel == null) {
             buildAggregatedFeedsDataModel();
         }
         return aggregatedFeedsDataModel.getDataModel();
     }
 
     private void buildAggregatedFeedsDataModel() {
         this.aggregatedFeedsDataModel = new CollectionDataModel(currentAggregatedFeed.getFeeds());
     }
 
     public String getId() {
         return id;
     }
 
     public String getUrl() {
         return aggregatedFeedService.createUrl((HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest(), currentAggregatedFeed);
     }
 
     public List<Feed> getAvailableListFeeds() {
         return availableListFeeds;
     }
 
     public String getNewFeedUrl() {
         return newFeedUrl;
     }
 
     public void setNewFeedUrl(String newFeedUrl) {
         this.newFeedUrl = newFeedUrl;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public int getNumLastDay() {
         return numLastDay;
     }
 
     public void setNumLastDay(int numLastDay) {
         this.numLastDay = numLastDay;
     }
 
     public String getFilter() {
         return filter;
     }
 
     public void setFilter(String filter) {
         this.filter = filter;
     }
     
 }
