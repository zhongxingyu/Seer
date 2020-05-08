 package com.psddev.dari.db;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.NoSuchElementException;
 import java.util.Set;
 import java.util.UUID;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.PaginatedResult;
 import com.psddev.dari.util.Settings;
 import com.psddev.dari.util.SparseSet;
 
 /**
  * Skeletal database implementation. A subclass must implement:
  *
  * <ul>
  * <li>{@link #openConnection}</li>
  * <li>{@link #closeConnection}</li>
  * <li>{@link #readLastUpdate}</li>
  * <li>{@link #readPartial}</li>
  * <li>{@link #doWrites}</li>
  * </ul>
  *
  * <p>If it supports read slaves, it should override:
  *
  * <ul>
  * <li>{@link #doOpenReadConnection}</li>
  * </ul>
  *
  * <p>It can override these to improve performance:
  *
  * <ul>
  * <li>{@link #readAll}</li>
  * <li>{@link #readAllGrouped}</li>
  * <li>{@link #readCount}</li>
  * <li>{@link #readFirst}</li>
  * <li>{@link #readIterable}</li>
  * <li>{@link #readPartialGrouped}</li>
  * <li>{@link #doIndexes}</li>
  * <li>{@link #deleteByQuery}</li>
  * </ul>
  *
  * <p>If it supports transactional writes, it should override:
  *
  * <ul>
  * <li>{@link #beginTransaction}</li>
  * <li>{@link #commitTransaction}</li>
  * <li>{@link #rollbackTransaction}</li>
  * <li>{@link #endTransaction}</li>
  * </ul>
  *
  * @param C Type of the implementation-specific connection object that's
  *        used for all database operations.
  */
 public abstract class AbstractDatabase<C> implements Database {
 
     public static final double DEFAULT_READ_TIMEOUT = 3.0;
     public static final String NULL_TYPE_QUERY_OPTION = "db.nullType";
     public static final String GROUPS_SUB_SETTING = "groups";
     public static final String READ_TIMEOUT_SUB_SETTING = "readTimeout";
     public static final String TRIGGER_EXTRA_PREFIX = "db.trigger.";
 
     private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDatabase.class);
 
     private volatile String name;
     private transient volatile DatabaseEnvironment environment;
     private volatile Set<String> groups;
     private volatile double readTimeout = DEFAULT_READ_TIMEOUT;
 
     private final transient ThreadLocal<Deque<Writes>> writesQueueLocal = new ThreadLocal<Deque<Writes>>();
 
     private static class Writes {
 
         public int depth;
         public final List<State> validates = new ArrayList<State>();
         public final List<State> saves = new ArrayList<State>();
         public final List<State> indexes = new ArrayList<State>();
         public final List<State> deletes = new ArrayList<State>();
 
         public void addToValidates(State state) {
             validates.remove(state);
             validates.add(state);
         }
 
         public void addToSaves(State state) {
             for (Iterator<State> i = saves.iterator(); i.hasNext(); ) {
                 State s = i.next();
                 if (s.equals(state)) {
                     i.remove();
                     state.getAtomicOperations().addAll(0, s.getAtomicOperations());
                 }
             }
             saves.add(state);
         }
 
         public void addToIndexes(State state) {
             for (Iterator<State> i = indexes.iterator(); i.hasNext(); ) {
                 State s = i.next();
                 if (s.equals(state)) {
                     i.remove();
                 }
             }
             indexes.add(state);
         }
 
         public void addToDeletes(State state) {
             deletes.remove(state);
             deletes.add(state);
         }
     }
 
     /**
      * Returns all {@linkplain ObjectType#getGroups groups} of types that
      * can be saved to this database.
      */
     public Set<String> getGroups() {
         if (groups == null) {
             groups = new SparseSet("+/");
         }
         return groups;
     }
 
     /**
      * Sets all {@linkplain ObjectType#getGroups groups} of types that
      * can be saved to this database.
      */
     public void setGroups(Set<String> groups) {
         this.groups = groups;
     }
 
     /**
      * Returns {@code true} if the given {@code types} represent all
      * types that can be saved to this database as indicated in
      * {@link #getGroups}.
      */
     public boolean isAllTypes(Collection<ObjectType> types) {
         Set<String> databaseGroups = getGroups();
         int allTypesCount = 0;
         for (ObjectType type : getEnvironment().getTypes()) {
             if (!(type.isAbstract() || type.isEmbedded())) {
                 for (String typeGroup : type.getGroups()) {
                     if (databaseGroups.contains(typeGroup)) {
                         ++ allTypesCount;
                         break;
                     }
                 }
             }
         }
         return types.size() == allTypesCount;
     }
 
     /**
      * Returns the read timeout, in seconds.
      *
      * @return May be less than or equal to {@code 0} to indicate
      *         no timeout.
      */
     public double getReadTimeout() {
         return readTimeout;
     }
 
     /**
      * Sets the read timeout, in seconds.
      *
      * @param readTimeout May be less than or equal to {@code 0} to
      *        indicate no timeout.
      */
     public void setReadTimeout(double readTimeout) {
         this.readTimeout = readTimeout;
     }
 
     /**
      * Opens an implementation-specific connection to the underlying
      * database. Once opened, the connection should be closed with
      * {@link #closeConnection}.
      *
      * @return Can't be {@code null}.
      */
     public abstract C openConnection();
 
     /**
      * Opens an implementation-specific connection to the underlying
      * database that can only be used for reading data. Once opened,
      * the connection should be closed with {@link #closeConnection}.
      *
      * @return Can't be {@code null}.
      */
     public final C openReadConnection() {
         return Database.Static.isIgnoreReadConnection() ?
                 openConnection() :
                 doOpenReadConnection();
     }
 
     /**
      * Called when this database needs to open an implementation-specific
      * connection to the underlying database that can only be used for
      * reading data. The default implementation calls
      * {@link #openConnection}.
      *
      * @return Can't be {@code null}.
      */
     protected C doOpenReadConnection() {
         return openConnection();
     }
 
     /**
      * Opens a connection that should be used to execute the given
      * {@code query}.
      */
     public C openQueryConnection(Query<?> query) {
        return query.isMaster() ?
                 openConnection() :
                 openReadConnection();
     }
 
     /**
      * Closes the given implementation-specific {@code connection}
      * to the underlying database.
      */
     public abstract void closeConnection(C connection);
 
     /** Returns {@code true} if the given {@code error} is recoverable. */
     protected boolean isRecoverableError(Exception error) {
         return false;
     }
 
     // --- Database support ---
 
     @Override
     public final synchronized void initialize(String settingsKey, Map<String, Object> settings) {
         String groupsPattern = ObjectUtils.to(String.class, settings.get(GROUPS_SUB_SETTING));
         setGroups(new SparseSet(ObjectUtils.isBlank(groupsPattern) ? "+/" : groupsPattern));
 
         Double readTimeout = ObjectUtils.to(Double.class, settings.get(READ_TIMEOUT_SUB_SETTING));
         if (readTimeout != null) {
             setReadTimeout(readTimeout);
         }
 
         doInitialize(settingsKey, settings);
     }
 
     /**
      * Called to initialize this database using the given {@code settings}.
      */
     protected abstract void doInitialize(String settingsKey, Map<String, Object> settings);
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public DatabaseEnvironment getEnvironment() {
         if (environment == null) {
             setEnvironment(new DatabaseEnvironment(this));
         }
         return environment;
     }
 
     @Override
     public void setEnvironment(DatabaseEnvironment environment) {
         this.environment = environment;
     }
 
     @Override
     public <T> List<T> readAll(Query<T> query) {
         return readPartial(query, 0L, MAXIMUM_LIMIT).getItems();
     }
 
     @Override
     public <T> List<Grouping<T>> readAllGrouped(Query<T> query, String... fields) {
         return readPartialGrouped(query, 0L, MAXIMUM_LIMIT, fields).getItems();
     }
 
     @Deprecated
     @Override
     public <T> List<T> readList(Query<T> query) {
         return readAll(query);
     }
 
     @Override
     public long readCount(Query<?> query) {
         return readPartial(query, 0L, 1).getCount();
     }
 
     @Override
     public <T> T readFirst(Query<T> query) {
         for (T item : readPartial(query, 0L, 1).getItems()) {
             return item;
         }
         return null;
     }
 
     @Override
     public <T> Iterable<T> readIterable(Query<T> query, int fetchSize) {
         return new ByIdIterable<T>(query, fetchSize);
     }
 
     private static class ByIdIterable<T> implements Iterable<T> {
 
         private final Query<T> query;
         private final int fetchSize;
 
         public ByIdIterable(Query<T> initialQuery, int initialFetchSize) {
             query = initialQuery;
             fetchSize = initialFetchSize;
         }
 
         @Override
         public Iterator<T> iterator() {
             return new ByIdIterator<T>(query, fetchSize);
         }
     }
 
     private static class ByIdIterator<T> implements Iterator<T> {
 
         private final Query<T> query;
         private final int fetchSize;
         private UUID lastObjectId;
         private PaginatedResult<T> result;
         private int index;
 
         public ByIdIterator(Query<T> query, int fetchSize) {
             if (!query.getSorters().isEmpty()) {
                 throw new IllegalArgumentException("Can't iterate over a query that has sorters!");
             }
 
             this.query = query.clone().sortAscending("_id");
             this.fetchSize = fetchSize > 0 ? fetchSize : 200;
         }
 
         @Override
         public boolean hasNext() {
             if (result != null && index >= result.getItems().size()) {
                 if (result.hasNext()) {
                     result = null;
                 } else {
                     return false;
                 }
             }
 
             if (result == null) {
                 Query<T> nextQuery = query.clone();
                 if (lastObjectId != null) {
                     nextQuery.and("_id > ?", lastObjectId);
                 }
 
                 result = nextQuery.select(0, fetchSize);
                 List<T> items = result.getItems();
 
                 int size = items.size();
                 if (size < 1) {
                     return false;
                 }
 
                 lastObjectId = State.getInstance(items.get(size - 1)).getId();
                 index = 0;
             }
 
             return true;
         }
 
         @Override
         public T next() {
             if (hasNext()) {
                 T object = result.getItems().get(index);
                 ++ index;
                 return object;
 
             } else {
                 throw new NoSuchElementException();
             }
         }
 
         @Override
         public void remove() {
             throw new UnsupportedOperationException();
         }
     }
 
     @Override
     public <T> PaginatedResult<Grouping<T>> readPartialGrouped(Query<T> query, long offset, int limit, String... fields) {
         Map<List<Object>, BasicGrouping<T>> groupingsMap = new LinkedHashMap<List<Object>, BasicGrouping<T>>();
         for (Object item : readIterable(query, 0)) {
 
             State itemState = State.getInstance(item);
             List<Object> keys = new ArrayList<Object>();
             if (fields != null) {
                 for (String field : fields) {
                     keys.add(itemState.getValue(field));
                 }
             }
 
             BasicGrouping<T> grouping = groupingsMap.get(keys);
             if (grouping == null) {
                 grouping = new BasicGrouping<T>(keys, query, fields);
                 groupingsMap.put(keys, grouping);
             }
 
             grouping.count += 1;
         }
 
         List<Grouping<T>> groupings = new ArrayList<Grouping<T>>(groupingsMap.values());
         return new PaginatedResult<Grouping<T>>(offset, limit, groupings);
     }
 
     /** Basic implementation of {@link Grouping}. */
     private class BasicGrouping<T> extends AbstractGrouping<T> {
 
         private long count;
 
         public BasicGrouping(List<Object> keys, Query<T> query, String[] fields) {
             super(keys, query, fields);
         }
 
         // --- AbstractGrouping support ---
 
         @Override
         protected Aggregate createAggregate(String field) {
             Aggregate aggregate = new Aggregate();
             Query<?> aggregateQuery = Query.fromQuery(query);
             List<Object> keys = getKeys();
 
             ITEM: for (Object item : readIterable(aggregateQuery, 0)) {
                 State itemState = State.getInstance(item);
                 Object value = itemState.getValue(field);
                 if (value == null) {
                     continue;
                 }
 
                 for (int i = 0, length = fields.length; i < length; ++ i) {
                     if (!ObjectUtils.equals(keys.get(i), itemState.getValue(fields[i]))) {
                         continue ITEM;
                     }
                 }
 
                 aggregate.setNonNullCount(aggregate.getNonNullCount() + 1);
 
                 if (ObjectUtils.compare(aggregate.getMaximum(), value, false) < 0) {
                     aggregate.setMaximum(value);
                 }
 
                 if (ObjectUtils.compare(aggregate.getMinimum(), value, true) > 0) {
                     aggregate.setMinimum(value);
                 }
 
                 Double valueDouble = ObjectUtils.to(Double.class, value);
                 if (valueDouble != null) {
                     aggregate.setSum(aggregate.getSum() + valueDouble);
                 }
             }
 
             return aggregate;
         }
 
         @Override
         public long getCount() {
             return count;
         }
     }
 
     @Deprecated
     @Override
     public Map<Object, Long> readGroupedCount(Query<?> query, String field) {
         Map<Object, Long> counts = new LinkedHashMap<Object, Long>();
         for (Grouping<?> grouping : readAllGrouped(query, field)) {
             counts.put(grouping.getKeys().get(0), grouping.getCount());
         }
         return counts;
     }
 
     private final Deque<Writes> getOrCreateWritesQueue() {
         Deque<Writes> writesQueue = writesQueueLocal.get();
         if (writesQueue == null) {
             writesQueue = new ArrayDeque<Writes>();
             writesQueueLocal.set(writesQueue);
         }
         return writesQueue;
     }
 
     @Override
     public final boolean beginWrites() {
         Deque<Writes> writesQueue = getOrCreateWritesQueue();
         if (writesQueue.isEmpty()) {
             writesQueue.addLast(new Writes());
             return true;
 
         } else {
             ++ writesQueue.peekLast().depth;
             return false;
         }
     }
 
     @Override
     public final void beginIsolatedWrites() {
         getOrCreateWritesQueue().addLast(new Writes());
     }
 
     @Override
     public final boolean commitWrites() {
         return doCommitWrites(true);
     }
 
     @Override
     public final boolean commitWritesEventually() {
         return doCommitWrites(false);
     }
 
     private boolean doCommitWrites(boolean isImmediate) {
         Deque<Writes> writesQueue = writesQueueLocal.get();
         if (writesQueue == null || writesQueue.isEmpty()) {
             throw new IllegalStateException("Can't commit writes that never began!");
         }
 
         Writes writes = writesQueue.peekLast();
         if (writes.depth > 0) {
             return false;
 
         } else {
             try {
                 write(writes.validates, writes.saves, writes.indexes, writes.deletes, isImmediate);
                 return true;
 
             } finally {
                 writes.validates.clear();
                 writes.saves.clear();
                 writes.indexes.clear();
                 writes.deletes.clear();
             }
         }
     }
 
     @Override
     public final boolean endWrites() {
         Deque<Writes> writesQueue = writesQueueLocal.get();
         if (writesQueue == null || writesQueue.isEmpty()) {
             throw new IllegalStateException("Can't end writes that never began!");
         }
 
         Writes writes = writesQueue.peekLast();
         if (writes.depth > 0) {
             -- writes.depth;
             return false;
 
         } else {
             writesQueue.removeLast();
             if (writesQueue.isEmpty()) {
                 writesQueueLocal.remove();
             }
             return true;
         }
     }
 
     private final Writes getCurrentWrites() {
         Deque<Writes> writesQueue = writesQueueLocal.get();
         return writesQueue != null ? writesQueue.peekLast() : null;
     }
 
     private enum Trigger {
 
         BEFORE_SAVE("beforeSave") {
             @Override
             protected void doExecute(Record record) {
                 record.beforeSave();
             }
         },
 
         BEFORE_DELETE("beforeDelete") {
             @Override
             protected void doExecute(Record record) {
                 record.beforeDelete();
             }
         };
 
         private final String name;
         private final String extraName;
 
         private Trigger(String name) {
             this.name = name;
             this.extraName = TRIGGER_EXTRA_PREFIX + name;
         }
 
         protected abstract void doExecute(Record record);
 
         @SuppressWarnings("unchecked")
         public final void execute(State state) {
             ObjectType type = state.getType();
 
             if (type == null) {
                 return;
             }
 
             // Global modifications.
             for (ObjectType modType : state.getDatabase().getEnvironment().getTypesByGroup(Modification.class.getName())) {
                 Class<?> modClass = modType.getObjectClass();
 
                 if (modClass != null &&
                         Modification.class.isAssignableFrom(modClass) &&
                         Modification.Static.getModifiedClasses((Class<? extends Modification<?>>) modClass).contains(Object.class)) {
                     executeForModClass(state, modClass);
                 }
             }
 
             // Type-specific modifications.
             for (String modClassName : type.getModificationClassNames()) {
                 Class<?> modClass = ObjectUtils.getClassByName(modClassName);
 
                 if (modClass != null) {
                     executeForModClass(state, modClass);
                 }
             }
 
             // Embedded objects.
             for (ObjectField field : type.getFields()) {
                 executeForValue(state.get(field.getInternalName()), field.isEmbedded());
             }
         }
 
         private void executeForModClass(State state, Class<?> modClass) {
             Object modObject;
 
             try {
                 modObject = state.as(modClass);
 
                 if (!(modObject instanceof Record)) {
                     return;
                 }
 
             } catch (Exception ex) {
                 return;
             }
 
             Record modRecord = (Record) modObject;
             State modState = modRecord.getState();
             Map<String, Object> extras = modState.getExtras();
             @SuppressWarnings("unchecked")
             Set<Class<?>> triggers = (Set<Class<?>>) extras.get(extraName);
 
             if (triggers == null) {
                 triggers = new HashSet<Class<?>>();
                 extras.put(extraName, triggers);
             }
 
             if (triggers.contains(modClass)) {
                 LOGGER.debug(
                         "Already triggered {} from [{}] on [{}]",
                         new Object[] { name, modClass.getName(), modState.getId() });
 
             } else {
                 LOGGER.debug(
                         "Triggering {} from [{}] on [{}]",
                         new Object[] { name, modClass.getName(), modState.getId() });
                 triggers.add(modClass);
                 doExecute(modRecord);
             }
         }
 
         private void executeForValue(Object value, boolean embedded) {
             if (value instanceof Map) {
                 value = ((Map<?, ?>) value).values();
             }
 
             if (value instanceof Iterable) {
                 for (Object item : (Iterable<?>) value) {
                     executeForValue(item, embedded);
                 }
 
             } else if (value instanceof Recordable) {
                 State valueState = ((Recordable) value).getState();
 
                 if (embedded) {
                     execute(valueState);
 
                 } else {
                     ObjectType valueType = valueState.getType();
 
                     if (valueType != null && valueType.isEmbedded()) {
                         execute(valueState);
                     }
                 }
             }
         }
     }
 
     @Override
     public final void save(State state) {
         checkState(state);
 
         ObjectType type = state.getType();
         if (type != null && !type.isConcrete()) {
             throw new IllegalStateException(String.format(
                     "Can't save a non-concrete object! (%s)",
                     type.getLabel()));
         }
 
         Trigger.BEFORE_SAVE.execute(state);
 
         Writes writes = getCurrentWrites();
         if (writes != null) {
             writes.addToValidates(state);
             writes.addToSaves(state);
 
         } else {
             List<State> wrapped = Arrays.asList(state);
             write(wrapped, wrapped, null, null, true);
         }
     }
 
     @Override
     public final void saveUnsafely(State state) {
         checkState(state);
 
         Writes writes = getCurrentWrites();
         if (writes != null) {
             writes.addToSaves(state);
 
         } else {
             write(null, Arrays.asList(state), null, null, true);
         }
     }
 
     @Override
     public final void index(State state) {
         checkState(state);
 
         Writes writes = getCurrentWrites();
         if (writes != null) {
             writes.addToIndexes(state);
 
         } else {
             write(null, null, Arrays.asList(state), null, true);
         }
     }
 
     // Checks to make sure that the given {@code state} is savable.
     private void checkState(State state) {
         if (state == null) {
             throw new IllegalArgumentException("State is required!");
         }
 
         if (state.isReferenceOnly()) {
             throw new IllegalArgumentException(String.format(
                     "Can't write a reference-only object! (%s)",
                     state.getId()));
         }
     }
 
     @Override
     public void indexAll(ObjectIndex index) {
     }
 
     @Override
     public final void delete(State state) {
         Trigger.BEFORE_DELETE.execute(state);
 
         Writes writes = getCurrentWrites();
         if (writes != null) {
             writes.addToDeletes(state);
 
         } else {
             write(null, null, null, Arrays.asList(state), true);
         }
     }
 
     @Override
     public void deleteByQuery(Query<?> query) {
         int batchSize = 200;
         try {
             beginWrites();
 
             int i = 0;
             for (Object item : readIterable(query, batchSize)) {
                 delete(State.getInstance(item));
                 ++ i;
                 if (i % batchSize == 0) {
                     commitWrites();
                 }
             }
 
             commitWrites();
         } finally {
             endWrites();
         }
     }
 
     // Performs the given write operations.
     private void write(
             List<State> validates,
             List<State> saves,
             List<State> indexes,
             List<State> deletes,
             boolean isImmediate) {
 
         boolean hasValidates = validates != null && !validates.isEmpty();
         boolean hasSaves = saves != null && !saves.isEmpty();
         boolean hasIndexes = indexes != null && !indexes.isEmpty();
         boolean hasDeletes = deletes != null && !deletes.isEmpty();
 
         if (!(hasValidates || hasSaves || hasIndexes || hasDeletes)) {
             return;
         }
 
         List<DistributedLock> locks = validate(validates, true);
 
         try {
             if (locks != null && !locks.isEmpty()) {
                 for (DistributedLock lock : locks) {
                     lock.lock();
                 }
                 validate(validates, false);
             }
 
             boolean isCommitted = false;
             Exception lastError = null;
 
             for (int i = 0, limit = Settings.getOrDefault(int.class, "dari/databaseWriteRetryLimit", 10); i < limit; ++ i) {
                 try {
                     C connection = openConnection();
 
                     try {
                         try {
                             beginTransaction(connection, isImmediate);
                             doWrites(connection, isImmediate, saves, indexes, deletes);
                             commitTransaction(connection, isImmediate);
                             isCommitted = true;
                             break;
 
                         } finally {
                             try {
                                 if (!isCommitted) {
                                     rollbackTransaction(connection, isImmediate);
                                 }
 
                             } finally {
                                 endTransaction(connection, isImmediate);
                             }
                         }
 
                     } finally {
                         closeConnection(connection);
                     }
 
                 } catch (Retry error) {
                     -- i;
 
                 } catch (Exception error) {
                     lastError = error;
 
                     if (error instanceof RecoverableDatabaseException ||
                             isRecoverableError(error)) {
                         try {
                             long initialPause = Settings.getOrDefault(long.class, "dari/databaseWriteRetryInitialPause", 10L);
                             long finalPause = Settings.getOrDefault(long.class, "dari/databaseWriteRetryFinalPause", 1000L);
                             double pauseJitter = Settings.getOrDefault(double.class, "dari/databaseWriteRetryPauseJitter", 0.5);
                             long pause = ObjectUtils.jitter(initialPause + (finalPause - initialPause) * i / (limit - 1), pauseJitter);
                             Thread.sleep(pause);
                             continue;
 
                         } catch (InterruptedException ex2) {
                         }
                     }
 
                     break;
                 }
             }
 
             if (!isCommitted) {
                 if (lastError instanceof DatabaseException) {
                     throw (DatabaseException) lastError;
 
                 } else if (isRecoverableError(lastError)) {
                     throw new RecoverableDatabaseException(this, lastError);
 
                 } else {
                     throw new DatabaseException(this, lastError);
                 }
             }
 
         } finally {
             if (locks != null && !locks.isEmpty()) {
                 for (DistributedLock lock : locks) {
                     try {
                         lock.unlock();
                     } catch (Throwable ex) {
                         if (LOGGER.isDebugEnabled()) {
                             LOGGER.debug("Can't unlock [" + lock + "]!", ex);
                         }
                     }
                 }
             }
         }
 
         if (hasSaves) {
             for (State state : saves) {
                 state.setStatus(StateStatus.SAVED);
             }
         }
         if (hasDeletes) {
             for (State state : deletes) {
                 state.setStatus(StateStatus.DELETED);
             }
         }
     }
 
     // Validates the given states and returns a list of locks that
     // should be used to enforce unique constraints.
     private List<DistributedLock> validate(List<State> states, boolean beforeLocks) {
         if (states == null || states.isEmpty()) {
             return null;
         }
 
         List<State> errors = null;
         Map<String, State> keys = null;
         DatabaseEnvironment environment = getEnvironment();
 
         for (State state : states) {
             if (beforeLocks) {
                 if (!state.validate()) {
                     if (errors == null) {
                         errors = new ArrayList<State>();
                     }
                     errors.add(state);
                 }
 
             } else {
                 state.clearAllErrors();
             }
 
             ObjectType type = state.getType();
             for (ObjectStruct struct : type != null ?
                     new ObjectStruct[] { type, environment } :
                     new ObjectStruct[] { environment }) {
 
                 for (ObjectIndex index : struct.getIndexes()) {
                     if (!index.isUnique()) {
                         continue;
                     }
 
                     Object[][] valuePermutations = index.getValuePermutations(state);
                     if (valuePermutations == null) {
                         continue;
                     }
 
                     String indexPrefix = index.getPrefix();
                     String indexName = index.getUniqueName();
                     List<String> fields = index.getFields();
 
                     for (int i = 0, ps = valuePermutations.length; i < ps; ++ i) {
                         Query<Object> duplicateQuery = Query.
                                 from(Object.class).
                                 where("id != ?", state.getId()).
                                 using(state.getDatabase()).
                                 referenceOnly();
 
                         StringBuilder keyBuilder = new StringBuilder();
                         keyBuilder.append(indexName);
 
                         Object[] values = valuePermutations[i];
                         for (int j = 0, vs = values.length; j < vs; ++ j) {
                             Object value = values[j];
                             keyBuilder.append('\0');
                             keyBuilder.append(value);
                             duplicateQuery.and(indexPrefix + fields.get(j) + " = ?", values[j]);
                         }
 
                         Object duplicate;
                         boolean ignore = Database.Static.isIgnoreReadConnection();
 
                         try {
                             Database.Static.setIgnoreReadConnection(true);
                             duplicate = duplicateQuery.first();
                         } finally {
                             Database.Static.setIgnoreReadConnection(ignore);
                         }
 
                         if (duplicate == null) {
                             if (!beforeLocks) {
                                 continue;
 
                             } else {
                                 if (keys == null) {
                                     keys = new HashMap<String, State>();
                                 }
 
                                 String key = keyBuilder.toString();
                                 duplicate = keys.get(key);
                                 if (duplicate == null) {
                                     keys.put(key, state);
                                     continue;
 
                                 } else if (state.equals(State.getInstance(duplicate))) {
                                     continue;
                                 }
                             }
                         }
 
                         if (errors == null) {
                             errors = new ArrayList<State>();
                         }
                         errors.add(state);
                         state.addError(
                                 state.getField(index.getField()),
                                 "Must be unique but duplicate at " +
                                 (State.getInstance(duplicate).getId()) +
                                 "!");
                     }
                 }
             }
         }
 
         if (errors != null && !errors.isEmpty()) {
             throw new ValidationException(errors);
         }
 
         if (keys == null || keys.isEmpty()) {
             return null;
 
         } else {
             List<DistributedLock> locks = new ArrayList<DistributedLock>();
             for (String key : keys.keySet()) {
                 locks.add(DistributedLock.Static.getInstance(this, key));
             }
             return locks;
         }
     }
 
     /**
      * Called by the write methods to begin a transaction.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void beginTransaction(C connection, boolean isImmediate) throws Exception {
     }
 
     /**
      * Called by the write methods to commit the transaction.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      * @param isImmediate If {@code true}, the saved data must be
      *        available for read immediately.
      */
     protected void commitTransaction(C connection, boolean isImmediate) throws Exception {
     }
 
     /**
      * Called by the write methods to roll back the transaction.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void rollbackTransaction(C connection, boolean isImmediate) throws Exception {
     }
 
     /**
      * Called by the write methods to end the transaction.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void endTransaction(C connection, boolean isImmediate) throws Exception {
     }
 
     /**
      * Called by the write methods to save, index, or delete the given states.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void doWrites(C connection, boolean isImmediate, List<State> saves, List<State> indexes, List<State> deletes) throws Exception {
         if (saves != null && !saves.isEmpty()) {
             doSaves(connection, isImmediate, saves);
         }
         if (indexes != null && !indexes.isEmpty()) {
             doIndexes(connection, isImmediate, indexes);
         }
         if (deletes != null && !deletes.isEmpty()) {
             doDeletes(connection, isImmediate, deletes);
         }
     }
 
     /**
      * Called by the write methods to save the given {@code states}.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void doSaves(C connection, boolean isImmediate, List<State> states) throws Exception {
     }
 
     /**
      * Called by the write methods to index the given {@code states}.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void doIndexes(C connection, boolean isImmediate, List<State> states) throws Exception {
     }
 
     /**
      * Called by the write methods to delete the given {@code states}.
      *
      * @param connection {@link #openConnection Implementation-specific
      *        connection} to the underlying database.
      */
     protected void doDeletes(C connection, boolean isImmediate, List<State> states) throws Exception {
     }
 
     @SuppressWarnings("serial")
     private static class Retry extends Error {
 
         @Override
         public Throwable fillInStackTrace() {
             return null;
         }
     }
 
     private static final Retry RETRY_INSTANCE = new Retry();
 
     /**
      * Retries the current writes. This method should only be called within
      * {@link #doWrites}, {@link #doSaves}, {@link #doIndexes}, or
      * {@link #doDeletes}.
      */
     protected void retryWrites() {
         throw RETRY_INSTANCE;
     }
 
     // --- Implementation helpers ---
 
     /**
      * Implementation helper method to create a previously saved object
      * with the given {@code id}, of the type represented by the given
      * {@code typeId}, and sets common state options based on the given
      * {@code query}. The ID parameters may be of any UUID-like object.
      */
     protected final <T> T createSavedObject(Object typeId, Object id, Query<T> query) {
         DatabaseEnvironment environment = getEnvironment();
         UUID typeIdUuid = ObjectUtils.to(UUID.class, typeId);
         UUID idUuid = ObjectUtils.to(UUID.class, id);
 
         if (typeIdUuid == null) {
             Object nullType = query.getOptions().get(NULL_TYPE_QUERY_OPTION);
             if (nullType != null) {
                 if (nullType instanceof ObjectType) {
                     typeIdUuid = ((ObjectType) nullType).getId();
                 } else if (nullType instanceof Class) {
                     typeIdUuid = environment.getTypeByClass((Class<?>) nullType).getId();
                 } else if (nullType instanceof UUID) {
                     typeIdUuid = (UUID) nullType;
                 } else if (nullType instanceof String) {
                     typeIdUuid = environment.getTypeByName((String) nullType).getId();
                 } else {
                     throw new IllegalArgumentException(String.format(
                             "Can't interpret [%s] as a type!", nullType));
                 }
             }
         }
 
         @SuppressWarnings("unchecked")
         T object = (T) environment.createObject(typeIdUuid, idUuid);
         State objectState = State.getInstance(object);
 
         if (idUuid != null) {
             objectState.setStatus(StateStatus.SAVED);
         }
 
         objectState.getExtras().put(Database.CREATOR_EXTRA, this);
         objectState.getExtras().put(Query.CREATOR_EXTRA, query);
 
         if (query != null) {
             objectState.setDatabase(query.getDatabase());
             objectState.setResolveToReferenceOnly(query.isResolveToReferenceOnly());
             objectState.setResolveUsingCache(query.isCache());
             objectState.setResolveUsingMaster(query.isMaster());
             objectState.setResolveInvisible(query.isResolveInvisible());
 
             if (query.isReferenceOnly()) {
                 objectState.setStatus(StateStatus.REFERENCE_ONLY);
             }
         }
 
         return object;
     }
 
     @SuppressWarnings("unchecked")
     protected final <T> T swapObjectType(Query<T> query, T object) {
         DatabaseEnvironment environment = getEnvironment();
         State state = State.getInstance(object);
         ObjectType type = state.getType();
 
         if (type != null) {
             Class<?> objectClass = type.getObjectClass();
 
             if (objectClass != null && !objectClass.isInstance(object)) {
                 State oldState = state;
                 object = (T) environment.createObject(state.getTypeId(), state.getId());
                 state = State.getInstance(object);
 
                 state.setStatus(oldState.getStatus());
                 state.setValues(oldState);
                 state.getExtras().putAll(oldState.getExtras());
             }
         }
 
         if (object instanceof ObjectType) {
             ObjectType asType = environment.getTypeById(((ObjectType) object).getId());
 
             if (asType != null && asType != object) {
                 return (T) asType.clone();
             }
         }
 
         return object;
     }
 
     // --- Object support ---
 
     @Override
     public String toString() {
         return getName();
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link #TRIGGER_EXTRA_PREFIX} instead. */
     @Deprecated
     public static final String BEFORE_SAVES_EXTRA = "db.beforeSaves";
 
     /** @deprecated Use {@link #READ_TIMEOUT_SUB_SETTING} instead. */
     @Deprecated
     public static final String READ_TIMEOUT_SETTING = READ_TIMEOUT_SUB_SETTING;
 }
