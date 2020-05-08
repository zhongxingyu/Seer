 package net.sparkmuse.data.twig;
 
 import com.google.code.twig.ObjectDatastore;
 import com.google.code.twig.FindCommand;
 import com.google.code.twig.LoadCommand;
 import com.google.appengine.api.datastore.QueryResultIterator;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.Cursor;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.*;
 import com.google.inject.Inject;
 import net.sparkmuse.data.entity.Entity;
 import net.sparkmuse.data.entity.UserVO;
 import net.sparkmuse.data.paging.PageChangeRequest;
 import net.sparkmuse.common.Cache;
 import net.sparkmuse.common.CacheKeyFactory;
 
 import java.util.*;
 import java.util.concurrent.Future;
 
 import org.apache.commons.collections.CollectionUtils;
 
 /**
  * Created by IntelliJ IDEA.
  *
  * @author neteller
  * @created: Nov 22, 2010
  */
 public class DatastoreService {
 
   private final ObjectDatastore datastore;
   private final Cache cache;
 
   @Inject
   public DatastoreService(ObjectDatastore datastore, Cache cache) {
     this.datastore = datastore;
     this.cache = cache;
   }
 
   public ObjectDatastore getDatastore() {
     return this.datastore;
   }
   
   Cache getCache() {
     return cache;
   }
 
   /**
    * Attempts to get a user from the cache.  Failing that, it will lookup the user and add it to
    * the cache.
    *
    * @param id
    * @return
    */
   public UserVO getUser(final Long id) {
     UserVO cachedUser = cache.get(CacheKeyFactory.newUserKey(id));
     if (null != cachedUser) {
       return cachedUser;
     }
 
     return load(UserVO.class, id);
   }
 
   /**
    * Attempts to get a user from the cache.  Failing that, it will lookup the user and add it to
    * the cache.
    *
    * @param userIds
    * @return
    */
   public Map<Long, UserVO> getUsers(final Set<Long> userIds) {
     Set<Long> toQuery = Sets.newHashSet();
     Set<UserVO> cachedUsers = Sets.newHashSet();
     for (Long userId: userIds) {
       UserVO cachedUser = cache.get(CacheKeyFactory.newUserKey(userId));
       if (null != cachedUser) {
         cachedUsers.add(cachedUser);
       }
       else {
         toQuery.add(userId);
       }
     }
 
     final Map<Long, UserVO> usersMap = CollectionUtils.size(toQuery) > 0 ? loadAll(UserVO.class, toQuery) : Maps.<Long, UserVO>newHashMap();
     usersMap.putAll(Maps.uniqueIndex(cachedUsers, UserVO.asUserIds));
 
     return usersMap;
   }
 
   //READ COMMANDS
 
   public final <T extends Entity<T>> T only(FindCommand.RootFindCommand<T> findCommand) {
     final QueryResultIterator<T> resultsIterator = findCommand.now();
     if (resultsIterator.hasNext()) {
       final T toReturn = resultsIterator.next();
      Preconditions.checkState(!resultsIterator.hasNext(), "Only one result requested but more than one returned.");
       return After.read(toReturn, this);
     }
     else return null;
   }
 
   public final <U extends Entity<U>> U load(Class<U> entityClass, Long id) {
     final U u = datastore.load(entityClass, id);
     return After.read(u, this);
   }
 
   public final <I, U extends Entity<U>> Map<I, U> loadAll(Class<U> entityClass, Set<I> ids, Entity... parents) {
     final LoadCommand.MultipleTypedLoadCommand command = datastore.load().type(entityClass).ids(ids);
     for (Entity parent: parents) {
       command.parent(parent);
     }
 
     final Map<I, U> idsToModels = command.now();
     return After.read(idsToModels, this);
   }
 
   public final <U extends Entity<U>> List<U> all(FindCommand.RootFindCommand<U> findCommand) {
     final QueryResultIterator<U> resultIterator = findCommand.fetchNextBy(200).now();
     final List<U> toReturn = Lists.newArrayList(resultIterator);
     return After.read(toReturn, this);
   }
 
   public final <U extends Entity<U>> List<U> all(PageChangeRequest pageChangeRequest, FindCommand.RootFindCommand<U> findCommand) {
     final QueryResultIterator<U> resultIterator = pageChangeRequest.applyPaging(findCommand).now();
     final List<U> rawResults = Lists.newArrayList();
     for (int i = 0; i < pageChangeRequest.getState().pageSize() && resultIterator.hasNext(); i++) {
       rawResults.add(resultIterator.next());
     }
     List<U> results = After.read(rawResults, this);
     Cursor cursor = resultIterator.getCursor(); //get cursor before asking hasNext otherwise we bump the cursor forward one
     cursor = Cursor.fromWebSafeString(cursor.toWebSafeString()); //immutable
     pageChangeRequest.transition(resultIterator.hasNext(), cursor);
     return results;
   }
 
   //CREATE/UPDATES
 
   public final <U extends Entity<U>> U store(U entity) {
     if (null == entity) return null;
 
     //set the key on the model object
     return After.write(DatastoreUtils.store(entity, datastore), this);
   }
 
   public final <U extends Entity<U>> U update(U entity) {
     if (null == entity) return null;
 
     DatastoreUtils.associate(entity, datastore);
     datastore.update(entity);
 
     return After.write(entity, this);
   }
 
 }
