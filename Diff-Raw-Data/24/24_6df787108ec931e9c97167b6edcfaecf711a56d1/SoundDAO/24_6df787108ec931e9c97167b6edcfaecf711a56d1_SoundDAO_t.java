 package com.sound.dao;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bson.types.ObjectId;
 
 import com.github.jmkgreen.morphia.Morphia;
 import com.github.jmkgreen.morphia.query.Query;
 import com.mongodb.Mongo;
 import com.sound.constant.Constant;
 import com.sound.model.Sound;
 import com.sound.model.Tag;
 import com.sound.model.User;
 import com.sound.model.enums.SoundState;
 import com.sound.morphia.extension.BaseDAO;
 
 public class SoundDAO extends BaseDAO<Sound, ObjectId> {
 
   public SoundDAO(Mongo mongo, Morphia morphia, String dbName) {
     super(mongo, morphia, dbName);
   }
 
   public List<Sound> findByKeyWord(String keyWord, Integer start, Integer range) {
     Query<Sound> query = createQuery();
 
     query.or(query.criteria("profile.name").containsIgnoreCase(keyWord)).or(
         query.criteria("profile.description").containsIgnoreCase(keyWord));
 
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.PRIVATE.getStatusId());
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
 
     query.offset(start).limit(range).order("-profile.priority, -profile.priorityUpdatedDate, -records.createdTime");
 
     return this.find(query).asList();
   }
 
   public List<Sound> findByTag(User curUser, List<Tag> tags, Integer start, Integer range) {
     Query<Sound> query = createQuery();
 
     query.criteria("tags").hasAnyOf(tags);
     
     if (null != curUser)
     {
       query.criteria("profile.owner").notEqual(curUser);
     }
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.PRIVATE.getStatusId());
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
 
     query.offset(start).limit(range).order("-profile.priority, -profile.priorityUpdatedDate, -records.createdTime");
 
     return this.find(query).asList();
   }
   
   public List<Sound> getUserSound(User user, User curUser, Integer start, Integer range) {
     Query<Sound> query = createQuery();
     query.criteria("records.owner").equal(user);
 
     List<Integer> status = new ArrayList<Integer>();
     if (!user.equals(curUser)) {
       status.add(SoundState.PRIVATE.getStatusId());
     }
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
 
     if (curUser.getUserRoles().contains(Constant.USER_ROLE_OBJ))
     {
       query.offset(start).limit(range).order("-records.createdTime");
     }
     else
     {
       query.offset(start).limit(range).order("-profile.priority, -profile.priorityUpdatedDate, -records.createdTime");
     }
 
     return this.find(query).asList();
   }
 
   public List<Sound> getUsersSound(List<User> users, User curUser, Integer start, Integer range) {
     Query<Sound> query = createQuery();
     query.criteria("records.owner").hasAnyOf(users);
     query.criteria("records.owner").notEqual(curUser);
 
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.PRIVATE.getStatusId());
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
 
     query.offset(start).limit(range).order("-profile.priority, -profile.priorityUpdatedDate, -records.createdTime");
 
     return this.find(query).asList();
   }
 
   public long getUsersSoundCount(List<User> users, Date startTime) {
     Query<Sound> query = createQuery();
     query.criteria("records.owner").hasAnyOf(users);
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.PRIVATE.getStatusId());
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
     query.criteria("records.createdTime").greaterThan(startTime);
 
     return this.count(query);
   }
   
   public long getUserCreatedCount(User user, Date startTime) {
     Query<Sound> query = createQuery();
     query.criteria("records.owner").equal(user);
     query.criteria("records.type").equal(Constant.SOUND_RECORD_CREATE);
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
     query.criteria("records.createdTime").greaterThan(startTime);
 
     return this.count(query);
   }
 
   public List<Sound> getRecommendSoundsByUser(User user, Integer start, Integer range) {
     Query<Sound> query = createQuery();
     query.criteria("profile.owner").notEqual(user);
     query.criteria("likes.owner").equal(user);
     query.criteria("records.owner").equal(user);
 
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.PRIVATE.getStatusId());
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
     query.offset(start).limit(range).order("-records.createdTime");
 
     return this.find(query).asList();
   }
 
   public List<Sound> getSoundByTags(User recommendTo, Set<Tag> tags, int start, Integer range) {
     Query<Sound> query = createQuery();
     query.criteria("tags").in(tags);
     query.criteria("profile.owner").notEqual(recommendTo);
     query.criteria("records.owner").notEqual(recommendTo);
 
     List<Integer> status = new ArrayList<Integer>();
     status.add(SoundState.PRIVATE.getStatusId());
     status.add(SoundState.DELETE.getStatusId());
     query.criteria("profile.status").hasNoneOf(status);
     query.criteria("soundData").exists();
 
     query.offset(start).limit(range).order("-profile.createdTime");
 
     return this.find(query).asList();
   }
   
   public List<Sound> getSoundsByCreatedTime(Map<String, Object> cratiaries, Date startTime){
     Query<Sound> query = createQuery();
     for (String key : cratiaries.keySet()) {
       query.field(key).equal(cratiaries.get(key));
     }
     
     query.criteria("profile.createdTime").greaterThan(startTime);
     return this.find(query).asList();
   }
 
   public List<Sound> getSoundsByIds(List<ObjectId> soundIds){
     Query<Sound> query = createQuery();
     query.criteria("_id").in(soundIds);
     
     return this.find(query).asList();
   }
  
  public List<Sound> findTopOnes(Integer number, Map<String, List<Object>> exclude) {
    Query<Sound> query = createQuery();

    for (String key : exclude.keySet()) {
      query.field(key).hasNoneOf(exclude.get(key));
    }

    List<Integer> status = new ArrayList<Integer>();
    status.add(SoundState.PRIVATE.getStatusId());
    status.add(SoundState.DELETE.getStatusId());
    query.criteria("profile.status").hasNoneOf(status);
    query.criteria("soundData").exists();
    
    query.offset(0).limit(number);

    return this.find(query).asList();
  }
 }
