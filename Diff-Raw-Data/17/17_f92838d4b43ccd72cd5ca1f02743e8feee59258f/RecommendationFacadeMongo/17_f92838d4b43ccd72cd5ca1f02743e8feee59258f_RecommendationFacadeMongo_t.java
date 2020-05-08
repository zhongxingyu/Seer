 package com.github.aprestaux.funreco.service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import com.github.aprestaux.funreco.api.Action;
 import com.github.aprestaux.funreco.api.Attributes;
 import com.github.aprestaux.funreco.api.Object;
 import com.github.aprestaux.funreco.api.Recommendation;
 import com.github.aprestaux.funreco.api.Recommendations;
 import com.github.aprestaux.funreco.api.RecommendedObject;
 import com.github.aprestaux.funreco.domain.DBAction;
 import com.github.aprestaux.funreco.domain.DBObject;
 import com.github.aprestaux.funreco.domain.DBProfile;
 import com.google.code.morphia.Datastore;
 
 /**
  * Implementation of RecommendationFacade using mongodb
  */
 public class RecommendationFacadeMongo implements RecommendationFacade {
     @Inject
     private Datastore datastore;
 
     @Override
     public void updateProfile(String id, Attributes attributes) {
         DBProfile dbProfile = findById(id);
 
         if (dbProfile == null) {
             dbProfile = new DBProfile(id);
         }
 
         dbProfile.setAttributes(attributes);
 
         datastore.save(dbProfile);
     }
 
     @Override
     public Attributes findProfile(String id) throws ProfileNotFoundException {
         return assertFindById(id).getAttributes();
     }
 
     @Override
     public Attributes findProfile(String email, String id) throws ProfileNotFoundException {
         DBProfile profile = findByEmail(email);
        
         return profile != null ? profile.getAttributes() : null;
     }
 
     @Override
     public void updateFriends(String id, List<String> friendIds) throws ProfileNotFoundException {
         DBProfile dbProfile = assertFindById(id);
 
         if (friendIds == null) {
             friendIds = new ArrayList<String>();
         }
 
         dbProfile.setFriendsIds(friendIds);
 
         datastore.save(dbProfile);
     }
 
     @Override
     public List<String> findFriends(String id) throws ProfileNotFoundException {
         return assertFindById(id).getFriendsIds();
     }
 
     @Override
     public void pushAction(String id, Action action) throws ProfileNotFoundException {
         DBProfile dbProfile = assertFindById(id);
 
         DBAction dbAction = new DBAction();
         dbAction.setProfile(dbProfile);
         dbAction.setDate(action.getDate() == null ? new Date() : action.getDate());
         dbAction.setObject(toDBObject(action.getObject()));
 
         datastore.save(dbAction);
     }
 
     @Override
     public List<Action> findActions(int offset, int limit) {
         List<DBAction> dbActions = datastore.find(DBAction.class).offset(offset).limit(limit).asList();
 
         return toActions(dbActions);
     }
     
     @Override
     public List<Action> findAllActions() {
         List<DBAction> dbActions = datastore.find(DBAction.class).asList();
 
         return toActions(dbActions);
     }
 
     @Override
     public List<Action> findActions(String id, int offset, int limit) {
         List<DBAction> dbActions = datastore.find(DBAction.class).filter("profile.externalId", id).offset(offset).limit(limit).asList();
         
         return toActions(dbActions);
     }
 
     @Override
     public int countActions() {
         return (int) datastore.find(DBAction.class).countAll();
     }
 
     @Override
     public Recommendations findDefaultRecommendations() {
     	List<DBAction> dbActions = datastore.find(DBAction.class).asList();
         
         return toRecommendations(dbActions);
     }
 
     @Override
     public Recommendations findRecommendations(String id) {
     	List<String> friendsIds = getFriendsIdsFor(id);
     	List<DBAction> dbActions = new ArrayList<DBAction>();
     	for(String friendId : friendsIds){
     		for(DBAction dbAction_tmp : datastore.find(DBAction.class).filter("profile.externalId", friendId).asList())
     			dbActions.add(dbAction_tmp);
     	}
        Recommendations reco = toRecommendations(dbActions);
         reco.setProfileId(friendsIds.get(0));
         return reco;
     }
 
     public Recommendations toRecommendations(List<DBAction> dbActions){
     	Map<String, RecommendedObject> recommendedObjects = new HashMap<String, RecommendedObject>();
         for(DBAction dbAction : dbActions){
         	Object object = toObject(dbAction.getObject());
         	String recomendorId = dbAction.getProfile().getExternalId();
         	if(recommendedObjects.containsKey(object.getId())){
         		recommendedObjects.get(object.getId()).getBy().add(recomendorId);
         	}
         	else{
         		List<String> initialRecommenderList = new ArrayList<String>();
         		initialRecommenderList.add(recomendorId);
         		RecommendedObject recommendedObject = new RecommendedObject();
         		recommendedObject.setBy(initialRecommenderList);
         		recommendedObject.setObject(object);
         		recommendedObjects.put(object.getId(), recommendedObject);
         	}
         }
         
         Recommendation recommendation = new Recommendation();
        recommendation.setQuery("");
         ArrayList<RecommendedObject> recommendedObjectList = new ArrayList<RecommendedObject>();
         recommendation.setObjects(recommendedObjectList);
         for(RecommendedObject recoObject : recommendedObjects.values()){
         	recommendation.addObject(recoObject);
         }
         
         Recommendations recommendations = new Recommendations();
         recommendations.addRecommendation(recommendation);
         
         return recommendations;
     }
     
     public List<Action> toActions(List<DBAction> dbActions){
         List<Action> actions = new ArrayList<Action>();
 
         for(DBAction dbAction : dbActions) {
             actions.add(toAction(dbAction));
         }
         
         return actions;
     }
 
     public Action toAction(DBAction dbAction){
         Action action = new Action();
 
         action.setDate(dbAction.getDate());
         action.setObject(toObject(dbAction.getObject()));
         
         return action;
     }
 
     public Object toObject(DBObject dbObject){
         if (dbObject == null) {
             return null;
         }
 
         Object object = new Object();
         object.setId(dbObject.getObjectId());
         object.getAttributes().putAll(dbObject.getObjectProperties());
         
         return object;
     }
 
     public DBObject toDBObject(Object object){
         if (object == null) {
             return null;
         }
 
         DBObject dbObject = new DBObject();
         dbObject.setObjectId(object.getId());
         dbObject.setObjectProperties(object.getAttributes());
 
         return dbObject;
     }
     
     private List<String> getFriendsIdsFor(String id){
     	DBProfile dbProfile = null;
     	try {
 			dbProfile = assertFindById(id);
 		} catch (ProfileNotFoundException e) {
 			e.printStackTrace();
 			return null;
 		}
     	
     	return dbProfile.getFriendsIds();
     }
 
     private DBProfile assertFindById(String id) throws ProfileNotFoundException {
         DBProfile dbProfile = findById(id);
 
         if (dbProfile == null) {
             throw new ProfileNotFoundException("No profile for id " + id);
         }
 
         return dbProfile;
     }
 
     private DBProfile findById(String id) {
         return datastore.find(DBProfile.class).filter("externalId", id).get();
     }
 
     private DBProfile findByEmail(String email) {
         return datastore.find(DBProfile.class).filter("email", email).get();
     }
 
     private DBObject findByObjectId(String id) {
         return datastore.find(DBObject.class).filter("id", id).get();
     }
 }
