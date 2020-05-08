 package com.vaadin.addon.sqlcontainer;
 
 import java.io.Serializable;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.EventObject;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.vaadin.addon.sqlcontainer.query.Filter;
 import com.vaadin.addon.sqlcontainer.query.FilteringMode;
 import com.vaadin.addon.sqlcontainer.query.OrderBy;
 import com.vaadin.addon.sqlcontainer.query.QueryDelegate;
 import com.vaadin.addon.sqlcontainer.query.TableQuery;
 import com.vaadin.addon.sqlcontainer.query.generator.MSSQLGenerator;
 import com.vaadin.addon.sqlcontainer.query.generator.OracleGenerator;
 import com.vaadin.data.Container;
 import com.vaadin.data.Item;
 import com.vaadin.data.Property;
 
 public class SQLContainer implements Container, Container.Filterable,
         Container.Indexed, Container.Sortable, Container.ItemSetChangeNotifier {
     private static final long serialVersionUID = -3863564310693712511L;
 
     /* Filtering mode setting. Default mode = INCLUSIVE */
     private FilteringMode currentFilteringMode = FilteringMode.FILTERING_MODE_INCLUSIVE;
 
     /* Query delegate and related settings */
     private QueryDelegate delegate;
     private boolean autoCommit = false;
 
     /* Page length = number of items fetched in one query */
     public static final int DEFAULT_PAGE_LENGTH = 100;
     private int pageLength = DEFAULT_PAGE_LENGTH;
 
     /* Number of items to cache = CACHE_RATIO x pageLength */
     public static final int CACHE_RATIO = 2;
 
     /* Item and index caches */
     private Map<Integer, RowId> itemIndexes = new HashMap<Integer, RowId>();
     private CacheMap<RowId, RowItem> cachedItems = new CacheMap<RowId, RowItem>();
 
     /* Container properties = column names and data types */
     private List<String> propertyIds = new ArrayList<String>();
     private Map<String, Class<?>> propertyTypes = new HashMap<String, Class<?>>();
 
     /* Filters (WHERE) and sorters (ORDER BY) */
     private List<Filter> filters = new ArrayList<Filter>();
     private List<OrderBy> sorters = new ArrayList<OrderBy>();
 
     /*
      * Size = total number of items available in the data source using the
      * current query, filters and sorters.
      */
     private int size;
 
     /*
      * Do not update size from data source if it has been updated in the last n
      * milliseconds
      */
     private final int sizeValidMilliSeconds = 10000;
     private boolean sizeDirty = true;
     private Date sizeUpdated = new Date();
 
     /* Currently fetched page number */
     private int currentOffset;
 
     /* Listeners */
     private LinkedList<Container.ItemSetChangeListener> itemSetChangeListeners;
 
     /* Temporary storage for items to be removed and added */
     private Map<RowId, RowItem> removedItems = new HashMap<RowId, RowItem>();
     private List<RowItem> addedItems = new ArrayList<RowItem>();
     private List<RowItem> modifiedItems = new ArrayList<RowItem>();
 
     /**
      * Prevent instantiation without a QueryDelegate.
      */
     @SuppressWarnings("unused")
     private SQLContainer() {
     }
 
     /**
      * Creates and initializes SQLContainer using the given QueryDelegate
      * 
      * @param delegate
      *            QueryDelegate implementation
      * @throws SQLException
      */
     public SQLContainer(QueryDelegate delegate) throws SQLException {
         if (delegate == null) {
             throw new IllegalArgumentException(
                     "QueryDelegate must not be null.");
         }
         this.delegate = delegate;
         getPropertyIds();
         cachedItems.setCacheLimit(CACHE_RATIO * getPageLength());
     }
 
     /**************************************/
     /** Methods from interface Container **/
     /**************************************/
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#addItem()
      */
     public Object addItem() throws UnsupportedOperationException {
         Object emptyKey[] = new Object[delegate.getPrimaryKeyColumns().size()];
         TemporaryRowId itemId = new TemporaryRowId(emptyKey);
         // Create new empty column properties for the row item.
         List<ColumnProperty> itemProperties = new ArrayList<ColumnProperty>();
         for (String propertyId : propertyIds) {
             /* Default settings for new item properties. */
             boolean readOnly = false;
             boolean allowReadOnlyChange = true;
             boolean nullable = true;
             /* Try to fetch column properties from an existing item. */
             boolean dataFromItem = false;
             if (!cachedItems.isEmpty()) {
                 try {
                     RowItem item = cachedItems.get(cachedItems.keySet()
                             .iterator().next());
                     ColumnProperty cp = (ColumnProperty) item
                             .getItemProperty("propertyId");
                     allowReadOnlyChange = cp.isReadOnlyChangeAllowed();
                     readOnly = cp.isReadOnly();
                     nullable = cp.isNullable();
                     dataFromItem = true;
                 } catch (Exception e) {
                     /* Failed to fetch data from existing item. */
                 }
             }
             /*
              * If data fetching failed, we can still check if the property in
              * question is a primary key.
              */
             if (!dataFromItem
                     && delegate.getPrimaryKeyColumns().contains(propertyId)) {
                 readOnly = true;
                 allowReadOnlyChange = false;
                 nullable = false;
             }
             itemProperties.add(new ColumnProperty(propertyId, readOnly,
                     allowReadOnlyChange, nullable, null, getType(propertyId)));
         }
         addedItems.add(new RowItem(this, itemId, itemProperties));
         fireContentsChange();
         return itemId;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#containsId(java.lang.Object)
      */
     public boolean containsId(Object itemId) {
         if (cachedItems.containsKey(itemId)) {
             return true;
         } else {
             for (RowItem item : addedItems) {
                 if (item.getId().equals(itemId)) {
                     return itemPassesFilters(item);
                 }
             }
         }
 
         if (removedItems.containsKey(itemId)) {
             return false;
         }
 
         try {
             return delegate.containsRowWithKey(((RowId) itemId).getId());
         } catch (Exception e) {
             /* Query failed, just return false. */
         }
         return false;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#getContainerProperty(java.lang.Object,
      * java.lang.Object)
      */
     public Property getContainerProperty(Object itemId, Object propertyId) {
         Item item = getItem(itemId);
         if (item == null) {
             return null;
         }
         return item.getItemProperty(propertyId);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#getContainerPropertyIds()
      */
     public Collection<?> getContainerPropertyIds() {
         return Collections.unmodifiableCollection(propertyIds);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#getItem(java.lang.Object)
      */
     public Item getItem(Object itemId) {
         if (!cachedItems.containsKey(itemId)) {
             int index = indexOfId(itemId);
             if (index >= size) {
                 // The index is in the added items
                 int offset = index - size;
                 RowItem item = addedItems.get(offset);
                 if (itemPassesFilters(item)) {
                     return item;
                 } else {
                     return null;
                 }
             } else {
                 // load the item into cache
                 updateOffsetAndCache(index);
             }
         }
         return cachedItems.get(itemId);
     }
 
     /**
      * NOTE! Do not use this method if in any way avoidable. This method doesn't
      * (and cannot) use lazy loading, which means that all rows in the database
      * will be loaded into memory.
      * 
      * {@inheritDoc}
      */
     public Collection<?> getItemIds() {
         updateCount();
         ArrayList<RowId> ids = new ArrayList<RowId>();
         ResultSet rs = null;
         try {
             // Load ALL rows :(
             rs = delegate.getResults(0, 0);
             List<String> pKeys = delegate.getPrimaryKeyColumns();
             while (rs.next()) {
                 /* Generate itemId for the row based on primary key(s) */
                 Object[] itemId = new Object[pKeys.size()];
                 for (int i = 0; i < pKeys.size(); i++) {
                     itemId[i] = rs.getObject(pKeys.get(i));
                 }
                 RowId id = new RowId(itemId);
                 if (!removedItems.containsKey(id)) {
                     ids.add(id);
                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException("Failed to fetch item indexes.", e);
         }
         for (RowItem item : getFilteredAddedItems()) {
             ids.add(item.getId());
         }
         return Collections.unmodifiableCollection(ids);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#getType(java.lang.Object)
      */
     public Class<?> getType(Object propertyId) {
         if (!propertyIds.contains(propertyId)) {
             return null;
         }
         return propertyTypes.get(propertyId);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#size()
      */
     public int size() {
         updateCount();
         return size + sizeOfAddedItems() - removedItems.size();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#removeItem(java.lang.Object)
      */
     public boolean removeItem(Object itemId)
             throws UnsupportedOperationException {
         if (!containsId(itemId)) {
             return false;
         }
 
         for (RowItem item : addedItems) {
             if (item.getId().equals(itemId)) {
                 addedItems.remove(item);
                 fireContentsChange();
                 return true;
             }
         }
 
         /* If auto commit mode is enabled, the row will be instantly removed. */
         if (autoCommit) {
             Item i = getItem(itemId);
             if (i == null) {
                 return false;
             }
             try {
                 delegate.commit();
                 delegate.beginTransaction();
                 boolean success = delegate.removeRow((RowItem) i);
                 delegate.commit();
                 refresh();
                 return success;
             } catch (SQLException e) {
                 try {
                     delegate.rollback();
                 } catch (SQLException ee) {
                     /* Nothing can be done here */
                 }
                 return false;
             }
         } else {
             removedItems.put((RowId) itemId, (RowItem) getItem(itemId));
             cachedItems.remove(itemId);
             refresh();
             return true;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#removeAllItems()
      */
     public boolean removeAllItems() throws UnsupportedOperationException {
         /*
          * If auto commit mode is enabled, all the rows will be instantly
          * removed. Note that they are still removed within one transaction so
          * if any removal fails, all actions will be rolled back.
          */
         if (autoCommit) {
             try {
                 delegate.commit();
                 delegate.beginTransaction();
                 boolean success = true;
                 for (Object id : getItemIds()) {
                     if (!delegate.removeRow((RowItem) getItem(id))) {
                         success = false;
                     }
                 }
                 if (success) {
                     delegate.commit();
                     refresh();
                 } else {
                     delegate.rollback();
                 }
                 return success;
             } catch (SQLException e) {
                 try {
                     delegate.rollback();
                 } catch (SQLException ee) {
                     /* Nothing can be done here */
                 }
                 return false;
             }
         } else {
             for (Object id : getItemIds()) {
                 removedItems.put((RowId) id, (RowItem) getItem(id));
                 cachedItems.remove(id);
             }
             refresh();
             return true;
         }
     }
 
     /*************************************************/
     /** Methods from interface Container.Filterable **/
     /*************************************************/
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.data.Container.Filterable#addContainerFilter(java.lang.Object,
      * java.lang.String, boolean, boolean)
      */
     public void addContainerFilter(Object propertyId, String filterString,
             boolean ignoreCase, boolean onlyMatchPrefix) {
         if (propertyId == null || !propertyIds.contains(propertyId)) {
             return;
         }
         /* Generate Filter -object */
         Filter.ComparisonType ct = Filter.ComparisonType.CONTAINS;
         if (onlyMatchPrefix) {
             ct = Filter.ComparisonType.STARTS_WITH;
         }
         Filter f = new Filter((String) propertyId, ct, filterString);
         f.setCaseSensitive(!ignoreCase);
         filters.add(f);
         refresh();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Filterable#removeAllContainerFilters()
      */
     public void removeAllContainerFilters() {
         filters.clear();
         refresh();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.data.Container.Filterable#removeContainerFilters(java.lang
      * .Object)
      */
     public void removeContainerFilters(Object propertyId) {
         List<Filter> toRemove = new ArrayList<Filter>();
         for (Filter f : filters) {
             if (f.getColumn().equals(propertyId)) {
                 toRemove.add(f);
             }
         }
         if (!toRemove.isEmpty()) {
             for (Filter f : toRemove) {
                 filters.remove(f);
             }
             refresh();
         }
     }
 
     /**********************************************/
     /** Methods from interface Container.Indexed **/
     /**********************************************/
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Indexed#indexOfId(java.lang.Object)
      */
     public int indexOfId(Object itemId) {
         // First check if the id is in the added items
         for (int ix = 0; ix < addedItems.size(); ix++) {
             RowItem item = addedItems.get(ix);
             if (item.getId().equals(itemId)) {
                 if (itemPassesFilters(item)) {
                     updateCount();
                     return size + ix;
                 } else {
                     return -1;
                 }
             }
         }
 
         if (!containsId(itemId)) {
             return -1;
         }
         if (cachedItems.isEmpty()) {
             getPage();
         }
         int size = size();
         boolean wrappedAround = false;
         while (!wrappedAround) {
             for (Integer i : itemIndexes.keySet()) {
                 if (itemIndexes.get(i).equals(itemId)) {
                     return i;
                 }
             }
             // load in the next page.
             int nextIndex = currentOffset
                     + (int) (pageLength * CACHE_RATIO * 1.5f);
             if (nextIndex >= size) {
                 // Wrap around.
                 wrappedAround = true;
                 // this will load from index 0 forward, since
                 // updateOffsetAndCache loads from index - pageLength *
                 // CACHE_RATIO / 2.
                 nextIndex = pageLength * CACHE_RATIO / 2;
             }
             updateOffsetAndCache(nextIndex);
         }
         return -1;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Indexed#getIdByIndex(int)
      */
     public Object getIdByIndex(int index) {
         if (index < 0 || index > size() - 1) {
             return null;
         }
         if (index < size) {
             if (itemIndexes.keySet().contains(index)) {
                 return itemIndexes.get(index);
             }
             updateOffsetAndCache(index);
             return itemIndexes.get(index);
         } else {
             // The index is in the added items
             int offset = index - size;
             return addedItems.get(offset).getId();
         }
     }
 
     /**********************************************/
     /** Methods from interface Container.Ordered **/
     /**********************************************/
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#nextItemId(java.lang.Object)
      */
     public Object nextItemId(Object itemId) {
         return getIdByIndex(indexOfId(itemId) + 1);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#prevItemId(java.lang.Object)
      */
     public Object prevItemId(Object itemId) {
         return getIdByIndex(indexOfId(itemId) - 1);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#firstItemId()
      */
     public Object firstItemId() {
         updateCount();
         if (size == 0) {
             if (addedItems.isEmpty()) {
                 return null;
             } else {
                 int ix = -1;
                 do {
                     ix++;
                 } while (!itemPassesFilters(addedItems.get(ix))
                         && ix < addedItems.size());
                 if (ix < addedItems.size()) {
                     return addedItems.get(ix).getId();
                 }
             }
         }
         if (!itemIndexes.containsKey(0)) {
             updateOffsetAndCache(0);
         }
         return itemIndexes.get(0);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#lastItemId()
      */
     public Object lastItemId() {
         if (addedItems.isEmpty()) {
             int lastIx = size() - 1;
             if (!itemIndexes.containsKey(lastIx)) {
                 updateOffsetAndCache(size - pageLength * CACHE_RATIO / 2);
             }
             return itemIndexes.get(lastIx);
         } else {
             int ix = addedItems.size();
             do {
                 ix--;
             } while (!itemPassesFilters(addedItems.get(ix)) && ix >= 0);
             if (ix >= 0) {
                 return addedItems.get(ix).getId();
             } else {
                 return null;
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#isFirstId(java.lang.Object)
      */
     public boolean isFirstId(Object itemId) {
         return firstItemId().equals(itemId);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#isLastId(java.lang.Object)
      */
     public boolean isLastId(Object itemId) {
         return lastItemId().equals(itemId);
     }
 
     /***********************************************/
     /** Methods from interface Container.Sortable **/
     /***********************************************/
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Sortable#sort(java.lang.Object[],
      * boolean[])
      */
     public void sort(Object[] propertyId, boolean[] ascending) {
         sorters.clear();
         if (propertyId == null || propertyId.length == 0) {
             refresh();
             return;
         }
         /* Generate OrderBy -objects */
         boolean asc = true;
         for (int i = 0; i < propertyId.length; i++) {
             /* Check that the property id is valid */
             if (propertyId[i] instanceof String
                     && propertyIds.contains(propertyId[i])) {
                 try {
                     asc = ascending[i];
                 } catch (Exception e) {
                 }
                 sorters.add(new OrderBy((String) propertyId[i], asc));
             }
         }
         refresh();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Sortable#getSortableContainerPropertyIds()
      */
     public Collection<?> getSortableContainerPropertyIds() {
         return getContainerPropertyIds();
     }
 
     /**************************************/
     /** Methods specific to SQLContainer **/
     /**************************************/
 
     /**
      * Sets the current filtering mode of the container. Possible values are
      * <code>FilteringMode.FILTERING_MODE_INCLUSIVE</code> and
      * <code>FilteringMode.FILTERING_MODE_EXCLUSIVE</code>.
      * 
      * With other inputs this method defaults to
      * <code>FilteringMode.FILTERING_MODE_INCLUSIVE</code>.
      * 
      * @param filteringMode
      *            Filtering mode to set.
      */
     public void setFilteringMode(FilteringMode filteringMode) {
         currentFilteringMode = filteringMode == FilteringMode.FILTERING_MODE_EXCLUSIVE ? FilteringMode.FILTERING_MODE_EXCLUSIVE
                 : FilteringMode.FILTERING_MODE_INCLUSIVE;
         refresh();
     }
 
     /**
      * Refreshes the container - clears all caches and resets size and offset.
      * Does NOT remove sorting or filtering rules!
      */
     public void refresh() {
         sizeDirty = true;
         currentOffset = 0;
         cachedItems.clear();
         itemIndexes.clear();
         fireContentsChange();
     }
 
     /**
      * Returns modify state of the container.
      * 
      * @return true if contents of this container have been modified
      */
     public boolean isModified() {
         return !removedItems.isEmpty() || !addedItems.isEmpty()
                 || !modifiedItems.isEmpty();
     }
 
     /**
      * Set auto commit mode enabled or disabled. Auto commit mode means that all
      * changes made to items of this container will be immediately written to
      * the underlying data source.
      * 
      * @param autoCommitEnabled
      *            true to enable auto commit mode
      */
     public void setAutoCommit(boolean autoCommitEnabled) {
         autoCommit = autoCommitEnabled;
     }
 
     /**
      * Returns status of the auto commit mode.
      * 
      * @return true if auto commit mode is enabled
      */
     public boolean isAutoCommit() {
         return autoCommit;
     }
 
     /**
      * Returns the currently set page length.
      * 
      * @return current page length
      */
     public int getPageLength() {
         return pageLength;
     }
 
     /**
      * Sets the page length used in lazy fetching of items from the data source.
      * Also resets the cache size to match the new page length.
      * 
      * @param pageLength
      *            new page length
      */
     public void setPageLength(int pageLength) {
         this.pageLength = pageLength > 0 ? pageLength : DEFAULT_PAGE_LENGTH;
         cachedItems.setCacheLimit(CACHE_RATIO * getPageLength());
     }
 
     /**
      * Adds the given Filter to this container and refreshes the container
      * contents with the new filtering rules.
      * 
      * Note that filter.getColumn() must return a column name that exists in
      * this container.
      * 
      * @param filter
      *            Filter to be added to the set of filters of this container
      */
     public void addFilter(Filter filter) {
         if (filter == null) {
             return;
         }
         if (!propertyIds.contains(filter.getColumn())) {
             throw new IllegalArgumentException(
                     "The column given for sorting does not exist in this container.");
         }
         filters.add(filter);
         refresh();
     }
 
     /**
      * Adds the given OrderBy to this container and refreshes the container
      * contents with the new sorting rules.
      * 
      * Note that orderBy.getColumn() must return a column name that exists in
      * this container.
      * 
      * @param orderBy
      *            OrderBy to be added to the container sorting rules
      */
     public void addOrderBy(OrderBy orderBy) {
         if (orderBy == null) {
             return;
         }
         if (!propertyIds.contains(orderBy.getColumn())) {
             throw new IllegalArgumentException(
                     "The column given for sorting does not exist in this container.");
         }
         sorters.add(orderBy);
         refresh();
     }
 
     /**
      * Commits all the changes, additions and removals made to the items of this
      * container.
      * 
      * @throws UnsupportedOperationException
      * @throws SQLException
      */
     public void commit() throws UnsupportedOperationException, SQLException {
         try {
             delegate.beginTransaction();
             /* Perform buffered deletions */
             for (RowItem item : removedItems.values()) {
                 if (!delegate.removeRow(item)) {
                     throw new SQLException("Removal failed for row with ID: "
                             + item.getId());
                 }
             }
             for (RowItem item : modifiedItems) {
                 delegate.storeRow(item);
             }
             for (RowItem item : addedItems) {
                 delegate.storeRow(item);
             }
             delegate.commit();
             removedItems.clear();
             addedItems.clear();
             modifiedItems.clear();
             refresh();
         } catch (SQLException e) {
             delegate.rollback();
             throw e;
         }
     }
 
     /**
      * Rolls back all the changes, additions and removals made to the items of
      * this container.
      * 
      * @throws UnsupportedOperationException
      * @throws SQLException
      */
     public void rollback() throws UnsupportedOperationException, SQLException {
         /* Discard removed, added and modified items */
         removedItems.clear();
         addedItems.clear();
         modifiedItems.clear();
         /*
          * Refresh container to clear item cache ,container size etc. which may
          * contain modifications
          */
         refresh();
     }
 
     /**
      * Notifies this container that a property in the given item has been
      * modified. The change will be buffered or made instantaneously depending
      * on auto commit mode.
      * 
      * @param changedItem
      *            item that has a modified property
      */
     void itemChangeNotification(RowItem changedItem) {
         if (autoCommit) {
             try {
                 delegate.commit();
                 delegate.beginTransaction();
                 delegate.storeRow(changedItem);
                 delegate.commit();
             } catch (SQLException e) {
                 try {
                     delegate.rollback();
                 } catch (SQLException ee) {
                     /* Nothing can be done here */
                 }
             }
         } else {
             if (!(changedItem.getId() instanceof TemporaryRowId)
                     && !modifiedItems.contains(changedItem)) {
                 modifiedItems.add(changedItem);
             }
         }
     }
 
     /**
      * Determines a new offset for updating the row cache. The offset is
      * calculated from the given index, and will be fixed to match the start of
      * a page, based on the value of pageLength.
      * 
      * @param index
      *            Index of the item that was requested, but not found in cache
      */
     private void updateOffsetAndCache(int index) {
         if (itemIndexes.containsKey(index)) {
             return;
         }
         currentOffset = index - (pageLength * CACHE_RATIO) / 2;
         if (currentOffset < 0) {
             currentOffset = 0;
         }
         getPage();
     }
 
     /**
      * Fetches new count of rows from the data source, if needed.
      */
     private void updateCount() {
         if (!sizeDirty
                 && new Date().getTime() < sizeUpdated.getTime()
                         + sizeValidMilliSeconds) {
             return;
         }
         try {
             try {
                 delegate.setFilters(filters, currentFilteringMode);
             } catch (UnsupportedOperationException e) {
                 /* The query delegate doesn't support filtering. */
             }
             try {
                 delegate.setOrderBy(sorters);
             } catch (UnsupportedOperationException e) {
                 /* The query delegate doesn't support filtering. */
             }
             int newSize = delegate.getCount();
             if (newSize != size) {
                 size = newSize;
                 refresh();
             }
             sizeUpdated = new Date();
             sizeDirty = false;
         } catch (SQLException e) {
             throw new RuntimeException("Failed to update item set size.", e);
         }
     }
 
     /**
      * Fetches property id's (column names and their types) from the data
      * source.
      * 
      * Note that if the table contains (or query returns) no items, it is
      * impossible to determine the data types of the columns.
      */
     private void getPropertyIds() {
         propertyIds.clear();
         propertyTypes.clear();
         delegate.setFilters(null);
         delegate.setOrderBy(null);
         ResultSet rs = null;
         ResultSetMetaData rsmd = null;
         try {
             rs = delegate.getResults(0, 1);
             boolean resultExists = rs.next();
             rsmd = rs.getMetaData();
            Object o = null;
             for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                 if (!isColumnIdentifierValid(rsmd.getColumnName(i))) {
                     continue;
                 }
                 propertyIds.add(rsmd.getColumnName(i));
                 if (resultExists) {
                    o = rs.getObject(i);
                 }
                propertyTypes.put(rsmd.getColumnName(i),
                        o == null ? new Object().getClass() : o.getClass());
             }
         } catch (SQLException e) {
             throw new RuntimeException("Failed to fetch property id's.", e);
         }
     }
 
     /**
      * Fetches a page from the data source based on the values of pageLenght and
      * currentOffset. Also updates the set of primary keys, used in
      * identification of RowItems.
      */
     private void getPage() {
         updateCount();
         ResultSet rs = null;
         ResultSetMetaData rsmd = null;
         // Clear the caches so that we don't fill up memory with (possibly)
         // millions of records
         cachedItems.clear();
         itemIndexes.clear();
         try {
             try {
                 delegate.setOrderBy(sorters);
             } catch (UnsupportedOperationException e) {
                 /* The query delegate doesn't support sorting. */
                 /* No need to do anything. */
             }
             rs = delegate.getResults(currentOffset, pageLength * CACHE_RATIO);
             rsmd = rs.getMetaData();
             List<String> pKeys = delegate.getPrimaryKeyColumns();
             if (pKeys == null || pKeys.isEmpty()) {
                 throw new IllegalStateException("No primary key column(s) set.");
             }
             /* Create new items and column properties */
             ColumnProperty cp = null;
             int rowCount = currentOffset;
             if (!delegate.implementationRespectsPagingLimits()) {
                 rowCount = currentOffset = 0;
                 setPageLength(size);
             }
             while (rs.next()) {
                 List<ColumnProperty> itemProperties = new ArrayList<ColumnProperty>();
                 /* Generate row itemId based on primary key(s) */
                 Object[] itemId = new Object[pKeys.size()];
                 for (int i = 0; i < pKeys.size(); i++) {
                     itemId[i] = rs.getObject(pKeys.get(i));
                 }
                 RowId id = new RowId(itemId);
                 if (!removedItems.containsKey(id)) {
                     for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                         if (!isColumnIdentifierValid(rsmd.getColumnName(i))) {
                             continue;
                         }
                         /* Determine column meta data */
                         boolean nullable = rsmd.isNullable(i) == ResultSetMetaData.columnNullable;
                         boolean allowReadOnlyChange = !rsmd.isReadOnly(i)
                                 && !rsmd.isAutoIncrement(i);
                         boolean readOnly = rsmd.isReadOnly(i)
                                 || rsmd.isAutoIncrement(i);
                         Object value = rs.getObject(i);
                         if (value != null) {
                             cp = new ColumnProperty(rsmd.getColumnName(i),
                                     readOnly, allowReadOnlyChange, nullable,
                                     value, value.getClass());
                         } else {
                             Class<?> colType = Object.class;
                             for (String propName : propertyTypes.keySet()) {
                                 if (propName.equals(rsmd.getColumnName(i))) {
                                     colType = propertyTypes.get(propName);
                                     break;
                                 }
                             }
                             cp = new ColumnProperty(rsmd.getColumnName(i),
                                     readOnly, allowReadOnlyChange, nullable,
                                     null, colType);
                         }
                         itemProperties.add(cp);
                     }
                     /* Cache item */
                     itemIndexes.put(rowCount, id);
                     cachedItems.put(id, new RowItem(this, id, itemProperties));
                     rowCount++;
                 }
             }
         } catch (SQLException e) {
             throw new RuntimeException("Failed to fetch page.", e);
         }
     }
 
     private int sizeOfAddedItems() {
         return getFilteredAddedItems().size();
     }
 
     private List<RowItem> getFilteredAddedItems() {
         ArrayList<RowItem> filtered = new ArrayList<RowItem>(addedItems);
         if (filters != null && !filters.isEmpty()) {
             for (RowItem item : addedItems) {
                 if (!itemPassesFilters(item)) {
                     filtered.remove(item);
                 }
             }
         }
         return filtered;
     }
 
     private boolean itemPassesFilters(RowItem item) {
         for (Filter filter : filters) {
             if (!filter.passes(item.getItemProperty(filter.getColumn())
                     .getValue())) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Checks is the given column identifier valid to be used with SQLContainer.
      * Currently the only non-valid identifier is "rownum" when MSSQL or Oracle
      * is used. This is due to the way the SELECT queries are constructed in
      * order to implement paging in these databases.
      * 
      * @param identifier
      *            Column identifier
      * @return true if the identifier is valid
      */
     private boolean isColumnIdentifierValid(String identifier) {
         if (identifier.equalsIgnoreCase("rownum")
                 && delegate instanceof TableQuery) {
             TableQuery tq = (TableQuery) delegate;
             if (tq.getSqlGenerator() instanceof MSSQLGenerator
                     || tq.getSqlGenerator() instanceof OracleGenerator) {
                 return false;
             }
         }
         return true;
     }
 
     /************************************/
     /** UNSUPPORTED CONTAINER FEATURES **/
     /************************************/
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#addContainerProperty(java.lang.Object,
      * java.lang.Class, java.lang.Object)
      */
     public boolean addContainerProperty(Object propertyId, Class<?> type,
             Object defaultValue) throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#removeContainerProperty(java.lang.Object)
      */
     public boolean removeContainerProperty(Object propertyId)
             throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container#addItem(java.lang.Object)
      */
     public Item addItem(Object itemId) throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#addItemAfter(java.lang.Object,
      * java.lang.Object)
      */
     public Item addItemAfter(Object previousItemId, Object newItemId)
             throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Indexed#addItemAt(int, java.lang.Object)
      */
     public Item addItemAt(int index, Object newItemId)
             throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Indexed#addItemAt(int)
      */
     public Object addItemAt(int index) throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.vaadin.data.Container.Ordered#addItemAfter(java.lang.Object)
      */
     public Object addItemAfter(Object previousItemId)
             throws UnsupportedOperationException {
         throw new UnsupportedOperationException();
     }
 
     /******************************************/
     /** ITEMSETCHANGENOTIFIER IMPLEMENTATION **/
     /******************************************/
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.data.Container.ItemSetChangeNotifier#addListener(com.vaadin
      * .data.Container.ItemSetChangeListener)
      */
     public void addListener(Container.ItemSetChangeListener listener) {
         if (itemSetChangeListeners == null) {
             itemSetChangeListeners = new LinkedList<Container.ItemSetChangeListener>();
         }
         itemSetChangeListeners.add(listener);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.data.Container.ItemSetChangeNotifier#removeListener(com.vaadin
      * .data.Container.ItemSetChangeListener)
      */
     public void removeListener(Container.ItemSetChangeListener listener) {
         if (itemSetChangeListeners != null) {
             itemSetChangeListeners.remove(listener);
         }
     }
 
     protected void fireContentsChange() {
         if (itemSetChangeListeners != null) {
             final Object[] l = itemSetChangeListeners.toArray();
             final Container.ItemSetChangeEvent event = new SQLContainer.ItemSetChangeEvent(
                     this);
             for (int i = 0; i < l.length; i++) {
                 ((Container.ItemSetChangeListener) l[i])
                         .containerItemSetChange(event);
             }
         }
     }
 
     /**
      * Simple ItemSetChangeEvent implementation.
      */
     public class ItemSetChangeEvent extends EventObject implements
             Container.ItemSetChangeEvent, Serializable {
         private static final long serialVersionUID = -8215550776139145987L;
 
         private ItemSetChangeEvent(SQLContainer source) {
             super(source);
         }
 
         public Container getContainer() {
             return (Container) getSource();
         }
     }
 
     /**
      * CacheMap extends LinkedHashMap, adding the possibility to adjust maximum
      * number of items.
      * 
      * In SQLContainer this is used for RowItem -cache. Cache size will be two
      * times the page length parameter of the container.
      */
     public class CacheMap<K, V> extends LinkedHashMap<K, V> {
         private static final long serialVersionUID = 679999766473555231L;
         private int cacheLimit = CACHE_RATIO * DEFAULT_PAGE_LENGTH;
 
         @Override
         protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
             return size() > cacheLimit;
         }
 
         public void setCacheLimit(int limit) {
             cacheLimit = limit > 0 ? limit : DEFAULT_PAGE_LENGTH;
         }
 
         public int getCacheLimit() {
             return cacheLimit;
         }
     };
 }
