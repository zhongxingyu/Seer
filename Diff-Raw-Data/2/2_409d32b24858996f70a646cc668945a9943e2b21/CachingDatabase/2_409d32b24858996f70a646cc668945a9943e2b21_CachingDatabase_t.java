 package com.psddev.dari.db;
 
 import com.psddev.dari.util.ObjectUtils;
 import com.psddev.dari.util.PaginatedResult;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * Caches the results of read operations.
  *
  * <p>For example, given:</p>
  *
  * <blockquote><pre>{@literal
 CachingDatabase caching = new CachingDatabase();
 caching.setDelegate(Database.Static.getDefault());
 PaginatedResult<Article> result = Query.from(Article.class).using(caching).select(0, 5);
  * }</pre></blockquote>
  *
  * <p>These are some of the queries that won't trigger additional
  * reads in the delegate database:</p>
  * 
  * <ul>
  * <li>{@code Query.from(Article.class).using(caching).count()}</li>
  * <li>{@code Query.from(Article.class).using(caching).where("_id = ?", result.getItems().get(0));}</li>
  * </ul>
  *
  * <p>All methods are thread-safe.</p>
  */
 public class CachingDatabase extends ForwardingDatabase {
 
     private static final Object MISSING = new Object();
 
     private final ConcurrentMap<UUID, Object> objectCache = new ConcurrentHashMap<UUID, Object>();
     private final ConcurrentMap<UUID, Object> referenceCache = new ConcurrentHashMap<UUID, Object>();
     private final ConcurrentMap<Query<?>, List<?>> readAllCache = new ConcurrentHashMap<Query<?>, List<?>>();
     private final ConcurrentMap<Query<?>, Long> readCountCache = new ConcurrentHashMap<Query<?>, Long>();
     private final ConcurrentMap<Query<?>, Object> readFirstCache = new ConcurrentHashMap<Query<?>, Object>();
     private final ConcurrentMap<Query<?>, Map<Range, PaginatedResult<?>>> readPartialCache = new ConcurrentHashMap<Query<?>, Map<Range, PaginatedResult<?>>>();
 
     private static class Range {
 
         public final long offset;
         public final int limit;
 
         public Range(long offset, int limit) {
             this.offset = offset;
             this.limit = limit;
         }
 
         @Override
         public boolean equals(Object other) {
             if (this == other) {
                 return true;
 
             } else if (other instanceof Range) {
                 Range otherRange = (Range) other;
                 return offset == otherRange.offset &&
                         limit == otherRange.limit;
 
             } else {
                 return false;
             }
         }
 
         @Override
         public int hashCode() {
             return ObjectUtils.hashCode(offset, limit);
         }
     }
 
     /**
      * Returns the map of all objects cached so far.
      *
      * @return Never {@code null}. Mutable. Thread-safe.
      */
     public Map<UUID, Object> getObjectCache() {
         return objectCache;
     }
 
     /**
      * Returns the map of all object references cached so far.
      *
      * @return Never {@code null}. Mutable. Thread-safe.
      */
     public Map<UUID, Object> getReferenceCache() {
         return referenceCache;
     }
 
     // --- ForwardingDatabase support ---
 
     private List<Object> findIdOnlyQueryValues(Query<?> query) {
         if (query.getSorters().isEmpty()) {
             Predicate predicate = query.getPredicate();
 
             if (predicate instanceof ComparisonPredicate) {
                 ComparisonPredicate comparison = (ComparisonPredicate) predicate;
 
                 if (Query.ID_KEY.equals(comparison.getKey()) &&
                         PredicateParser.EQUALS_ANY_OPERATOR.equals(comparison.getOperator()) &&
                         comparison.findValueQuery() == null) {
                     return comparison.getValues();
                 }
             }
         }
         return null;
     }
 
     private Object findCachedObject(UUID id, Query<?> query) {
         Object object = objectCache.get(id);
 
         if (object == null && query.isReferenceOnly()) {
             return referenceCache != null ? referenceCache.get(id) : null;
 
         } else {
             return object;
         }
     }
 
     private void cacheObject(Object object) {
         State state = ((Recordable) object).getState();
         UUID id = state.getId();
 
         if (state.isReferenceOnly()) {
             referenceCache.put(id, object);
 
        } else if (!state.isResolveToReferenceOnly()) {
             objectCache.put(id, object);
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> List<T> readAll(Query<T> query) {
         if (query.as(QueryOptions.class).isDisabled()) {
             return super.readAll(query);
         }
 
         List<Object> all = new ArrayList<Object>();
         List<Object> values = findIdOnlyQueryValues(query);
 
         if (values != null) {
             List<Object> newValues = null;
 
             for (Object value : values) {
                 UUID valueId = ObjectUtils.to(UUID.class, value);
 
                 if (valueId != null) {
                     Object object = findCachedObject(valueId, query);
                     if (object != null) {
                         all.add(object);
                         continue;
                     }
                 }
 
                 if (newValues == null) {
                     newValues = new ArrayList<Object>();
                 }
                 newValues.add(value);
             }
 
             if (newValues == null) {
                 return (List<T>) all;
 
             } else {
                 query = query.clone();
                 query.setPredicate(PredicateParser.Static.parse("_id = ?", newValues));
             }
         }
 
         List<?> list = readAllCache.get(query);
 
         if (list == null) {
             list = super.readAll(query);
             readAllCache.put(query, list);
 
             for (Object item : list) {
                 cacheObject(item);
             }
         }
 
         all.addAll(list);
         return (List<T>) all;
     }
 
     @Override
     public long readCount(Query<?> query) {
         if (query.as(QueryOptions.class).isDisabled()) {
             return super.readCount(query);
         }
 
         Long count = readCountCache.get(query);
 
         if (count == null) {
             COUNT: {
                 if (readAllCache != null) {
                     List<?> list = readAllCache.get(query);
 
                     if (list != null) {
                         count = (long) list.size();
                         break COUNT;
                     }
                 }
 
                 if (readPartialCache != null) {
                     Map<Range, PaginatedResult<?>> subCache = readPartialCache.get(query);
 
                     if (subCache != null && !subCache.isEmpty()) {
                         count = subCache.values().iterator().next().getCount();
                         break COUNT;
                     }
                 }
 
                 count = super.readCount(query);
             }
 
             readCountCache.put(query, count);
         }
 
         return count;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> T readFirst(Query<T> query) {
         if (query.as(QueryOptions.class).isDisabled()) {
             return super.readFirst(query);
         }
 
         List<Object> values = findIdOnlyQueryValues(query);
 
         if (values != null) {
             for (Object value : values) {
                 UUID valueId = ObjectUtils.to(UUID.class, value);
 
                 if (valueId != null) {
                     Object object = findCachedObject(valueId, query);
 
                     if (object != null) {
                         return (T) object;
                     }
                 }
             }
         }
 
         Object first = readFirstCache.get(query);
 
         if (first == null) {
             first = super.readFirst(query);
             if (first == null) {
                 first = MISSING;
             } else {
                 cacheObject(first);
             }
             readFirstCache.put(query, first);
         }
 
         return first != MISSING ? (T) first : null;
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public <T> PaginatedResult<T> readPartial(Query<T> query, long offset, int limit) {
         if (query.as(QueryOptions.class).isDisabled()) {
             return super.readPartial(query, offset, limit);
         }
 
         Map<Range, PaginatedResult<?>> subCache = readPartialCache.get(query);
 
         if (subCache == null) {
             Map<Range, PaginatedResult<?>> newSubCache = new ConcurrentHashMap<Range, PaginatedResult<?>>();
             subCache = readPartialCache.putIfAbsent(query, newSubCache);
 
             if (subCache == null) {
                 subCache = newSubCache;
             }
         }
 
         Range range = new Range(offset, limit);
         PaginatedResult<?> result = subCache.get(range);
 
         if (result == null) {
             result = super.readPartial(query, offset, limit);
             subCache.put(range, result);
 
             for (Object item : result.getItems()) {
                 cacheObject(item);
             }
         }
 
         return (PaginatedResult<T>) result;
     }
 
     /** {@link Query} options for {@link CachingDatabase}. */
     @Modification.FieldInternalNamePrefix("caching.")
     public static class QueryOptions extends Modification<Query<?>> {
 
         private boolean disabled;
 
         /**
          * Returns {@code true} if the caching should be disabled when
          * running the query.
          */
         public boolean isDisabled() {
             Boolean old = ObjectUtils.to(Boolean.class, getOriginalObject().getOptions().get(IS_DISABLED_QUERY_OPTION));
             return old != null ? old : disabled;
         }
 
         /**
          * Sets whether the caching should be disabled when running
          * the query.
          */
         public void setDisabled(boolean disabled) {
             this.disabled = disabled;
         }
     }
 
     // --- Deprecated ---
 
     /** @deprecated Use {@link QueryOptions} instead. */
     @Deprecated
     public static final String IS_DISABLED_QUERY_OPTION = "caching.isDisabled";
 
     @Deprecated
     @Override
     public <T> List<T> readList(Query<T> query) {
         return readAll(query);
     }
 }
