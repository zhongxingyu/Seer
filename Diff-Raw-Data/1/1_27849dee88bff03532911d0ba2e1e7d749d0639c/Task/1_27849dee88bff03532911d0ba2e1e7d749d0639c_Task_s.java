 package net.sparkmuse.task;
 
 import net.sparkmuse.data.twig.BatchDatastoreService;
 import net.sparkmuse.data.entity.Entity;
 import net.sparkmuse.data.entity.Migration;
 import net.sparkmuse.common.Cache;
 import com.google.common.base.Function;
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.Query;
 import com.google.inject.internal.Nullable;
 import com.google.code.twig.FindCommand;
 import com.google.code.twig.ObjectDatastore;
 
 import java.util.List;
 
 import play.Logger;
 import org.joda.time.DateTime;
 
 /**
  * Base class for tasks.
  *
  * @author neteller
  * @created: Jan 22, 2011
  */
 public abstract class Task<T extends Entity> {
 
   private final Cache cache;
   private final BatchDatastoreService batchService;
   private final ObjectDatastore datastore;
   private Cursor lastCursor;
 
   public Task(Cache cache, BatchDatastoreService batchService, ObjectDatastore datastore) {
     this.cache = cache;
     this.batchService = batchService;
     this.datastore = datastore;
   }
 
   public boolean isComplete() {
     return lastCursor == null;
   }
 
   protected abstract String getTaskName();
 
   protected abstract T transform(T t);
 
   protected abstract FindCommand.RootFindCommand<T> find(boolean isNew);
 
 
   public Cursor execute(@Nullable Cursor cursor) {
     if (null == cursor) storeBegin();
     lastCursor = batchService.transform(find(null == cursor), createTransformer(), cursor);
     if (isComplete()) storeEnd();
     return lastCursor;
   }
 
   public void storeBegin() {
     Logger.info("Beginning task [" + this.getClass() + "].");
 
     final Migration migration = new Migration(getTaskName(), Migration.State.STARTED);
 
     datastore.store(migration);
   }
 
   public void storeEnd() {
     Logger.info("Completed task [" + this.getClass() + "].");
 
     final Migration migration = currentMigration();
     migration.setEnded(new DateTime());
 
    if (null == datastore.associatedKey(migration)) datastore.associate(migration);
     datastore.update(migration);
   }
 
   protected Migration currentMigration() {
     return datastore.find().type(Migration.class)
         .addFilter("state", Query.FilterOperator.EQUAL, Migration.State.STARTED.toString())
         .addFilter("taskName", Query.FilterOperator.EQUAL, getTaskName())
         .fetchMaximum(1)
         .returnAll()
         .now()
         .get(0);
   }
 
   protected Migration lastMigration() {
     final List<Migration> migrationList = datastore.find().type(Migration.class)
         .addFilter("state", Query.FilterOperator.EQUAL, Migration.State.COMPLETED.toString())
         .addFilter("taskName", Query.FilterOperator.EQUAL, getTaskName())
         .addSort("started", Query.SortDirection.DESCENDING)
         .fetchMaximum(1)
         .returnAll()
         .now();
     return migrationList.size() > 0 ? migrationList.get(0) : null;
   }
 
   private Function<T, T> createTransformer() {
     return new Function<T, T>() {
       public T apply(T t) {
         final T entity = transform(t);
         cache.delete(entity);
         return entity;
       }
     };
   }
 
 }
