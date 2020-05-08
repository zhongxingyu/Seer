 package fr.kaddath.apps.fluxx.controller;
 
 import fr.kaddath.apps.fluxx.domain.AggregatedFeed;
 import fr.kaddath.apps.fluxx.domain.Fluxxer;
 import fr.kaddath.apps.fluxx.model.CollectionDataModel;
 import fr.kaddath.apps.fluxx.service.AggregatedFeedService;
 import java.io.Serializable;
 import java.util.List;
 import javax.annotation.ManagedBean;
 import javax.enterprise.context.SessionScoped;
 import javax.faces.model.DataModel;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 @ManagedBean
 @Named(value = "manageAggregatedFeed")
 @SessionScoped
 public class ManageAggregatedFeedBean extends ConnectedFluxxerBean implements Serializable {
 
     private static final String EDIT_AGGREGATEDFEED = "edit-aggregatedfeed";
     private static final String MANAGE_AGGREGATEDFEED = "manage-aggregatedfeed";
 
     private String name;
     private int numLastDay = 3;
 
     @Inject
     private AggregatedFeedService aggregatedFeedService;
 
     private transient CollectionDataModel userAggregatedFeedsDataModel;
 
     public String add() {
         aggregatedFeedService.addAggregatedFeed(getFluxxer(), name, numLastDay);
         reload();
         return MANAGE_AGGREGATEDFEED;
     }
 
     public String delete() {
         AggregatedFeed aggregatedFeed = getUserAggregatedFeeds().getRowData();
         Fluxxer fluxxer = getFluxxer();
         fluxxer.getAggregatedFeeds().remove(aggregatedFeed);
         userService.update(fluxxer);
         aggregatedFeedService.delete(aggregatedFeed);
         reload();
         return MANAGE_AGGREGATEDFEED;
     }
 
     private void reload() {
         buildUserAggregatedFeedsModel();
     }
 
     public List<AggregatedFeed> getAggregatedFeeds() {
         return getFluxxer().getAggregatedFeeds();
     }
 
     public DataModel<AggregatedFeed> getUserAggregatedFeeds() {
         if (userAggregatedFeedsDataModel == null) {
             buildUserAggregatedFeedsModel();
         }
 
         return userAggregatedFeedsDataModel.getDataModel();
     }
 
    private void buildUserAggregatedFeedsModel() {
         userAggregatedFeedsDataModel = new CollectionDataModel(getAggregatedFeeds());
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
 
 }
