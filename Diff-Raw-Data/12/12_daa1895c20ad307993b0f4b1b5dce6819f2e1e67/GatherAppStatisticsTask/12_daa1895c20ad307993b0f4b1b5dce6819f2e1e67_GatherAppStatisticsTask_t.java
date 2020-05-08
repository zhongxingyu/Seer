 package net.sparkmuse.task;
 
 import com.google.code.twig.ObjectDatastore;
 import com.google.code.twig.FindCommand;
 import com.google.code.twig.Restriction;
 import com.google.inject.Inject;
 import com.google.inject.internal.Nullable;
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.QueryResultIterator;
 
 import net.sparkmuse.data.entity.*;
 import net.sparkmuse.data.util.AccessLevel;
 import org.apache.commons.lang.StringUtils;
 
 import java.util.List;
 
 /**
  * @author neteller
  * @created: Feb 20, 2011
  */
 public class GatherAppStatisticsTask extends Task {
 
   private final ObjectDatastore datastore;
 
   @Inject
   public GatherAppStatisticsTask(ObjectDatastore datastore) {
     super(datastore);
     this.datastore = datastore;
   }
 
   protected Cursor runTask(@Nullable Cursor cursor) {
     //no filter used for isUSER so we dont need an index
    int userCount = count(createUserCountQuery().restrictEntities(new ActiveUserRestriction()).returnAll().now());
     int sparkCount = createCountQuery(SparkVO.class).returnCount().now();
     int postCount = createCountQuery(Post.class).returnCount().now();
 
     datastore.store().instance(AppStat.newUserCount(previousCount(AppStat.Type.USERS) + userCount)).later();
     datastore.store().instance(AppStat.newSparkCount(previousCount(AppStat.Type.SPARKS) + sparkCount)).later();
     datastore.store().instance(AppStat.newPostCount(previousCount(AppStat.Type.POSTS) + postCount)).later();
 
     return null;
   }
 
   private int count(List<UserVO> userVOs) {
     return userVOs.size();
   }
 
   private int previousCount(AppStat.Type type) {
     QueryResultIterator<AppStat> statQueryResultIterator = datastore.find().type(AppStat.class)
         .addFilter("type", Query.FilterOperator.EQUAL, type.toString())
         .addSort("created", Query.SortDirection.DESCENDING)
         .fetchMaximum(1)
         .now();
     if (statQueryResultIterator.hasNext()) {
       return statQueryResultIterator.next().getCount();
     }
     else {
       return 0;
     }
   }
 
   private <T> FindCommand.RootFindCommand<T> createCountQuery(Class<T> type) {
     return sinceLastMigration(datastore.find().type(type));
   }
 
  private FindCommand.RootFindCommand<UserVO> createUserCountQuery() {
    FindCommand.RootFindCommand<UserVO> find = datastore.find().type(UserVO.class);
    final Migration lastMigration = lastMigration();
    if (null != lastMigration) { //has a value set for people elgible
      find.addFilter("firstLogin", Query.FilterOperator.GREATER_THAN, lastMigration.getStarted().getMillis());
    }
    return find;
  }

 
   private <T> FindCommand.RootFindCommand<T> sinceLastMigration(FindCommand.RootFindCommand<T> find) {
     final Migration lastMigration = lastMigration();
     if (null != lastMigration) { //has a value set for people elgible
       find.addFilter("created", Query.FilterOperator.GREATER_THAN, lastMigration.getStarted().getMillis());
     }
     return find;
   }
 
   private static final class ActiveUserRestriction implements Restriction<com.google.appengine.api.datastore.Entity> {
     public boolean allow(com.google.appengine.api.datastore.Entity candidate) {
       return StringUtils.equalsIgnoreCase(candidate.getProperty("accessLevel").toString(), AccessLevel.USER.toString());
     }
   }
 
 }
