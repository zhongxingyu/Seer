 package org.vsegda.dao;
 
 import net.sf.jsr107cache.Cache;
 import net.sf.jsr107cache.CacheException;
 import net.sf.jsr107cache.CacheFactory;
 import net.sf.jsr107cache.CacheManager;
 import org.vsegda.data.DataItem;
 import org.vsegda.data.DataStream;
 import org.vsegda.factory.PM;
 import org.vsegda.shared.DataStreamMode;
 import org.vsegda.util.TimeInstant;
 import org.vsegda.util.TimePeriod;
 import org.vsegda.util.TimePeriodUnit;
 
 import javax.jdo.Query;
 import java.io.Serializable;
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * @author Roman Elizarov
  */
 public class DataItemDAO {
     private static final Logger log = Logger.getLogger(DataItemDAO.class.getName());
 
     public static final TimeInstant DEFAULT_SINCE = TimeInstant.valueOf(TimePeriod.valueOf(-1, TimePeriodUnit.WEEK));
     public static final int DEFAULT_LAST = 2500;
 
     private static final int MAX_CACHED_LIST_SIZE = (int)(1.5 * DEFAULT_LAST);
     private static final Cache LIST_CACHE;
 
     static {
         try {
             CacheFactory cf = CacheManager.getInstance().getCacheFactory();
             LIST_CACHE = cf.createCache(Collections.emptyMap());
         } catch (CacheException e) {
             throw new ExceptionInInitializerError(e);
         }
     }
 
     private DataItemDAO() {} // do not create
 
     public static void persistDataItem(DataItem dataItem) {
         PM.instance().makePersistent(dataItem);
         ListEntry entry = (ListEntry) LIST_CACHE.get(dataItem.getStreamId());
         if (entry != null) {
             entry.items.add(dataItem);
             int size = entry.items.size();
             if (size >= 2 && DataItem.ORDER_BY_TIME.compare(entry.items.get(size - 1), entry.items.get(size - 2)) < 0)
                 Collections.sort(entry.items, DataItem.ORDER_BY_TIME);
             if (size > MAX_CACHED_LIST_SIZE)
                 entry.items.subList(0, size - DEFAULT_LAST).clear();
             LIST_CACHE.put(dataItem.getStreamId(), entry);
         }
     }
 
     public static void persistDataItems(List<DataItem> items) {
         for (DataItem item : items)
             persistDataItem(item);
     }
 
     public static void removeDataItems(DataStream stream, List<DataItem> items) {
         PM.instance().deletePersistentAll(items);
         // just kill cache completely for the stream in this case
         LIST_CACHE.remove(stream.getStreamId());
     }
 
     public static DataItem findLastDataItem(DataStream stream) {
         List<DataItem> items = listDataItems(stream, null, 1, null);
         return items.isEmpty() ? new DataItem(stream, Double.NaN, 0) : items.get(0);
     }
 
     public static List<DataItem> listDataItems(DataStream stream, TimeInstant since, int last, int first, ReqFlags reqFlags) {
         List<DataItem> list = listDataItems(stream, since, last + first, reqFlags);
         return list.subList(0, Math.min(last, list.size()));
     }
 
     /**
      * Returns a list of last data items in ASCENDING order.
      */
    public static List<DataItem> listDataItems(DataStream stream, TimeInstant since, int last, ReqFlags reqFlags) {
         if (stream.getMode() == DataStreamMode.LAST)
            last = 1;
         ListEntry entry = (ListEntry) LIST_CACHE.get(stream.getStreamId());
         List<DataItem> items = null;
         if (entry != null) {
             int start = 0;
             int size = entry.items.size();
             if (since != null) {
                 while (start < size && entry.items.get(start).getTimeMillis() < since.time())
                     start++;
             }
            if (entry.complete || start > 0 || size >= last) // return from cache
                items = entry.items.subList(Math.max(start, size - last), size);
         }
         // perform query if not found in cache
         if (items == null)
            items = performItemsQuery(stream.getStreamId(), since, last, false);
         // always assume "hasNext" when since was set
        if (since != null && items.size() < last && reqFlags != null)
             reqFlags.setHasMore();
        if (items.size() >= last && reqFlags != null)
             reqFlags.setHasNext();
         return fillStream(stream, items);
     }
 
     @SuppressWarnings({"unchecked"})
     private static List<DataItem> performItemsQuery(long streamId, TimeInstant since, int n, boolean forceCacheUpdate) {
         Query query = PM.instance().newQuery(DataItem.class);
         String queryFilter = "streamId == id";
         String queryParams = "long id";
         Map<String, Object> queryArgs = new HashMap<String, Object>();
         queryArgs.put("id", streamId);
         if (since != null) {
             queryFilter += " && timeMillis >= since";
             queryParams += ", long since";
             queryArgs.put("since", since.time());
         }
         query.setFilter(queryFilter);
         query.declareParameters(queryParams);
         query.setOrdering("timeMillis desc");
         query.setRange(0, n);
         query.getFetchPlan().setFetchSize(n);
         List<DataItem> items = new ArrayList<DataItem>((Collection<DataItem>)query.executeWithMap(queryArgs));
         Collections.reverse(items); // turn descending into ascending order
         // update cache if needed
         ListEntry oldCacheEntry = (ListEntry) LIST_CACHE.get(streamId);
         if (forceCacheUpdate || oldCacheEntry == null || oldCacheEntry.items.size() <= items.size()) {
             List<DataItem> cacheItems = items.size() <= MAX_CACHED_LIST_SIZE ? items :
                     new ArrayList<DataItem>(items.subList(items.size() - MAX_CACHED_LIST_SIZE, items.size()));
            LIST_CACHE.put(streamId, new ListEntry(cacheItems, since == null && cacheItems.size() < n));
         }
         return items;
     }
 
     private static List<DataItem> fillStream(DataStream stream, List<DataItem> items) {
         for (DataItem item : items)
             item.setStream(stream);
         return items;
     }
 
     public static void refreshCache(long streamId) {
         performItemsQuery(streamId, null, DEFAULT_LAST, true);
     }
 
     @SuppressWarnings({"unchecked"})
     public static DataItem findFirstDataItem(DataStream stream) {
         Query query = PM.instance().newQuery(DataItem.class);
         query.setFilter("streamId == id");
         query.setOrdering("timeMillis asc");
         query.declareParameters("long id");
         query.setRange(0, 1);
         Collection<DataItem> items = (Collection<DataItem>) query.execute(stream.getStreamId());
         if (items.isEmpty())
             return null;
         DataItem item = items.iterator().next();
         item.setStream(stream);
         return item;
     }
 
     private static class ListEntry implements Serializable {
         private static final long serialVersionUID = 4488592054732566662L;
 
         List<DataItem> items;
         boolean complete;
 
         ListEntry(List<DataItem> items, boolean complete) {
             this.items = items;
             this.complete = complete;
         }
     }
 }
