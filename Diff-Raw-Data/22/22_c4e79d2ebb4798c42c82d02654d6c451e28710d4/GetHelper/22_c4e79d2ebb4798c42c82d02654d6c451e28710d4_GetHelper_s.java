 package com.feedly.cassandra.dao;
 
 import java.util.AbstractCollection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
 import me.prettyprint.hector.api.beans.CounterRow;
 import me.prettyprint.hector.api.beans.OrderedCounterRows;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.query.Query;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesCounterQuery;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceCounterQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import com.feedly.cassandra.EConsistencyLevel;
 import com.feedly.cassandra.IKeyspaceFactory;
 import com.feedly.cassandra.entity.EntityMetadata;
 import com.feedly.cassandra.entity.SimplePropertyMetadata;
 
 /*
  * helper class to do gets (reads by row key).
  */
 class GetHelper<K, V> extends LoadHelper<K, V>
 {
     GetHelper(EntityMetadata<V> meta, IKeyspaceFactory factory, int statsSize)
     {
         super(meta, factory, statsSize);
     }
 
     public V get(K key, V value, GetOptions options)
     {
         EConsistencyLevel c = options.getConsistencyLevel();
 
         V row = value;
         long startTiming = System.nanoTime();
         switch(options.getColumnFilterStrategy())
         {
             case UNFILTERED:
             case RANGE:
                 row = value;
                 byte[] start = null; 
                 byte[] end = null; 
 
                 if(options.getColumnFilterStrategy() == EColumnFilterStrategy.RANGE)
                 {
                     start = propertyName(options.getStartColumn(), ComponentEquality.EQUAL);
                     end = propertyName(options.getEndColumn(), ComponentEquality.GREATER_THAN_EQUAL);
                 }
                 
                 if(_entityMeta.hasNormalColumns())
                     row = loadFromGet(key, row, null, start, end, c); 
                 
                 if(_entityMeta.hasCounterColumns())
                     row = loadFromCounterGet(key, row, null, start, end, c);
                 break;
                 
             case INCLUDES:
                 row = get(key, value, options.getIncludes(), options.getExcludes(), c);
         }
 
         _stats.incrNumRows(1);
         _stats.incrNumOps(1);
         _stats.addRecentTiming(System.nanoTime() - startTiming);
         
         return row;
     }
 
     private V get(K key, V value, Set<? extends Object> includes, Set<String> excludes, EConsistencyLevel level)
     {
         _logger.debug("loading {}[{}]", _entityMeta.getFamilyName(), key);
 
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         List<byte[]> colNames = null, counterColNames = null;
         
         if(_entityMeta.hasCounterColumns())
             counterColNames = new ArrayList<byte[]>();
 
         if(_entityMeta.hasNormalColumns())
             colNames = new ArrayList<byte[]>();
             
         List<CollectionRange> ranges = derivePartialColumns(colNames, counterColNames, includes, excludes);
 
         if(colNames != null && !colNames.isEmpty())
             value = loadFromGet(key, value, colNames, null, null, level);
         
         if(counterColNames != null && !counterColNames.isEmpty())
             value = loadFromCounterGet(key, value, counterColNames, null, null, level);
                 
         if(ranges != null)
         {
             for(CollectionRange r : ranges)
             {
                 if(r.propertyMetadata().hasSimple())
                     value = loadFromGet(key, value, null, r.startBytes(), r.endBytes(), level);
 
                 if(r.propertyMetadata().hasCounter())
                     value = loadFromCounterGet(key, value, null, r.startBytes(), r.endBytes(), level);
             }
         }
 
         return value;
     }
 
     public List<V> mget(Collection<K> keys)
     {
         long startTime = System.nanoTime();
         List<V> values = null;
         
         if(_entityMeta.hasNormalColumns())
             values = bulkLoadFromMultiGet(keys, null, null, null, null, _entityMeta.hasCounterColumns(), null);
         
         if(_entityMeta.hasCounterColumns())
             values = bulkLoadFromMultiCounterGet(keys, values, null, null, null, _entityMeta.hasNormalColumns(), null);
         
         
         _stats.incrNumRows(keys.size());
         _stats.incrNumOps(1);
         _stats.addRecentTiming(System.nanoTime()-startTime);
         
         return values;
     }
 
     public List<V> mget(List<K> keys, List<V> values, GetOptions options)
     {
         long startTime = System.nanoTime();
 
         if(keys == null)
             throw new IllegalArgumentException("keys parameter is null");
         if(values != null && keys.size() != values.size())
             throw new IllegalArgumentException("key and value list must be same size");
         
         EConsistencyLevel c = options.getConsistencyLevel();
         switch(options.getColumnFilterStrategy())
         {
             case UNFILTERED:
             case RANGE:
                 byte[] start = null;
                 byte[] end = null;
                 if(options.getColumnFilterStrategy() == EColumnFilterStrategy.RANGE)
                 {
                     start = propertyName(options.getStartColumn(), ComponentEquality.EQUAL); 
                     end = propertyName(options.getEndColumn(), ComponentEquality.GREATER_THAN_EQUAL); 
                 }
                 
                 if(_entityMeta.hasNormalColumns())
                     values = bulkLoadFromMultiGet(keys, values, null, start, end, true, c);
                 
                 if(_entityMeta.hasCounterColumns())
                     values = bulkLoadFromMultiCounterGet(keys, values, null, start, end, true, c);
                 
                 break;
                 
             case INCLUDES:
                 values = mget(keys, values, options.getIncludes(), options.getExcludes(), c);
         }
 
         _stats.incrNumRows(keys.size());
         _stats.incrNumOps(1);
         _stats.addRecentTiming(System.nanoTime()-startTime);
         
         return values;
     }
     
     public Collection<V> mgetAll(GetAllOptions options)
     {
         _stats.incrNumOps(1);
         return new LazyLoadedCollection(options);
     }
     
     private List<V> mget(List<K> keys, List<V> values, Set<? extends Object> includes, Set<String> excludes, EConsistencyLevel level)
     {
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         if(includes == null && excludes == null)
             throw new IllegalArgumentException("includes or excludes should be specified");
 
         List<byte[]> colNames = null, counterColNames = null;
         
         if(_entityMeta.hasCounterColumns())
             counterColNames = new ArrayList<byte[]>();
 
         if(_entityMeta.hasNormalColumns())
             colNames = new ArrayList<byte[]>();
 
             
         List<CollectionRange> fullCollectionProperties = derivePartialColumns(colNames, counterColNames, includes, excludes);
 
 
         if(colNames != null && !colNames.isEmpty())
             values = bulkLoadFromMultiGet(keys, values, colNames, null, null, true, level);
         
         if(counterColNames != null && !counterColNames.isEmpty())
             values = bulkLoadFromMultiCounterGet(keys, values, counterColNames, null, null, true, level);
 
         return addCollectionRanges(keys, values, fullCollectionProperties, level);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private V loadFromGet(K key, V value, List<byte[]> cols, byte[] from, byte[] to, EConsistencyLevel level)
     {
         _logger.debug("loading {}[{}]", _entityMeta.getFamilyName(), key);
 
         SimplePropertyMetadata keyMeta = _entityMeta.getKeyMetadata();
         byte[] keyBytes = ((Serializer) keyMeta.getSerializer()).toBytes(key);
 
         SliceQuery<byte[], byte[], byte[]> query = buildSliceQuery(keyBytes, level);
 
         if(cols != null)
             query.setColumnNames(cols.toArray(new byte[cols.size()][]));
         else
             query.setRange(from, to, false, CassandraDaoBase.COL_RANGE_SIZE);
         
         _stats.incrNumCassandraOps(1);
         
         return fromColumnSlice(key, value, keyMeta, keyBytes, query, query.execute().get(), to, level);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private V loadFromCounterGet(K key, V value, List<byte[]> cols, byte[] from, byte[] to, EConsistencyLevel level)
     {
         _logger.debug("loading {}[{}]", _entityMeta.getCounterFamilyName(), key);
         
         SimplePropertyMetadata keyMeta = _entityMeta.getKeyMetadata();
         byte[] keyBytes = ((Serializer) keyMeta.getSerializer()).toBytes(key);
         
         SliceCounterQuery<byte[], byte[]> query = buildSliceCounterQuery(keyBytes, level);
         
         if(cols != null)
             query.setColumnNames(cols.toArray(new byte[cols.size()][]));
         else
             query.setRange(from, to, false, CassandraDaoBase.COL_RANGE_SIZE);
         
         _stats.incrNumCassandraOps(1);
 
         return fromCounterColumnSlice(key, value, keyMeta, keyBytes, query, query.execute().get(), to, level);
     }
     
     @SuppressWarnings({ "unchecked", "rawtypes" })
     private byte[] fetch(Query<?> q, byte[] start, GetOptions options, List<V> values)
     {
         long startTime = System.nanoTime();
         List<K> keys = null;
         List<CollectionRange> ranges = null;
         values.clear();
         byte[] endCol = null;
         RangeSlicesQuery<byte[], byte[], byte[]> query = q instanceof RangeSlicesQuery ? (RangeSlicesQuery) q : null;
         RangeSlicesCounterQuery<byte[], byte[]> cquery = query == null ?  (RangeSlicesCounterQuery) q : null;
         List<byte[]> colNames = new ArrayList<byte[]>();
         List<byte[]> counterColNames = new ArrayList<byte[]>();
         
         switch(options.getColumnFilterStrategy())
         {
             case INCLUDES:
                 Set<? extends Object> includes = options.getIncludes();
                 Set<String> excludes = options.getExcludes();
                 if(includes != null && excludes != null)
                     throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
                 if(includes == null && excludes == null)
                     throw new IllegalArgumentException("either includes or excludes should be specified");
 
                 ranges = derivePartialColumns(colNames, counterColNames, includes, excludes);
                 if(ranges != null)
                     keys = new ArrayList<K>();
                 
                 if(query != null)
                 {
                     if(colNames.isEmpty())
                         query.setReturnKeysOnly();
                     else
                         query.setColumnNames(colNames.toArray(new byte[colNames.size()][]));
                 }
                 else
                 {
                     if(counterColNames.isEmpty())
                         cquery.setReturnKeysOnly();
                     else
                         cquery.setColumnNames(counterColNames.toArray(new byte[counterColNames.size()][]));
                 }
                 break;
                 
             case RANGE:
                 byte[] startCol = propertyName(options.getStartColumn(), ComponentEquality.EQUAL);
                 endCol = propertyName(options.getEndColumn(), ComponentEquality.GREATER_THAN_EQUAL); 
                 if(query != null)
                     query.setRange(startCol, endCol, false, CassandraDaoBase.ROW_RANGE_SIZE);
                 else
                     cquery.setRange(startCol, endCol, false, CassandraDaoBase.ROW_RANGE_SIZE);
                 break;
             
             case UNFILTERED:
                 if(query != null)
                     query.setRange(null, null, false, CassandraDaoBase.ROW_RANGE_SIZE);
                 else
                     cquery.setRange(null, null, false, CassandraDaoBase.ROW_RANGE_SIZE);
                 break;
         }
         
         if(keys == null && _entityMeta.hasCounterColumns())
             keys = new ArrayList<K>();
         
         if(query != null)
             query.setKeys(start, null);
         else
             cquery.setKeys(start, null);
 
         QueryResult<OrderedRows<byte[], byte[], byte[]>> result = null;
         QueryResult<OrderedCounterRows<byte[], byte[]>> cresult = null;
         
         if(query != null)
             result = query.execute();
         else
             cresult = cquery.execute();
         
         _stats.incrNumCassandraOps(1);
         
         SimplePropertyMetadata keyMeta = _entityMeta.getKeyMetadata();
         boolean checkStart = true;
 
         Iterable<?> rows = result != null ? result.get() : cresult.get();
         byte[] keyBytes = null;
         for(Object rowObj : rows)
         {
             Row<byte[],byte[],byte[]> row = null;
             CounterRow<byte[],byte[]> crow = null;
             
             if(result != null)
                 row = (Row) rowObj;
             else
                 crow = (CounterRow) rowObj;
             
             keyBytes = row != null ? row.getKey() : crow.getKey();
             if(checkStart)
             {
                 checkStart = false;
                 if(start != null && Arrays.equals(start, keyBytes))
                     continue;
             }
             
             K key = (K) ((Serializer) keyMeta.getSerializer()).fromBytes(keyBytes);
 
             
             V value;
             if(row != null)
                 value = fromColumnSlice(key, null, keyMeta, keyBytes, null, row.getColumnSlice(), endCol, options.getConsistencyLevel());
             else
                 value = fromCounterColumnSlice(key, null, keyMeta, keyBytes, null, crow.getColumnSlice(), endCol, options.getConsistencyLevel());
             
             if(value != null)
             {
                 if(keys != null)
                     keys.add(key);
                 values.add(value);
             }
         }
         
         int cnt = result != null ? result.get().getCount() : cresult.get().getCount();
 
         _stats.addRecentTiming(System.nanoTime() - startTime);
         if(cnt == 0 || (start != null && cnt == 1))
             return null;
         
         _stats.incrNumRows(values.size());
         
         if(ranges != null)
         {
             addCollectionRanges(keys, values, ranges, options.getConsistencyLevel());
         }
 
         return cnt == 0 ? null : keyBytes;
     }
 
     
     private class LazyLoadedIterator implements Iterator<V>
     {
         private List<V> _current;
         private Iterator<V> _currentIter;
         private byte[] _lastKeyOfBatch;
         private byte[] _lastKeyOfCounterBatch;
         private V _next;
         private int _iteratedCnt = 0;
         private final GetAllOptions _options;
         RangeSlicesQuery<byte[], byte[], byte[]> _query;
         RangeSlicesCounterQuery<byte[], byte[]> _counterQuery;
         private final LazyLoadedCollection _parent;
         
         public LazyLoadedIterator(LazyLoadedCollection parent, 
                                   List<V> first,
                                   byte[] lastKeyOfBatch,
                                   byte[] lastKeyOfCounterBatch,
                                   RangeSlicesQuery<byte[], byte[], byte[]> query,
                                   RangeSlicesCounterQuery<byte[], byte[]> counterQuery,
                                   GetAllOptions options)
         {
             _parent = parent;
             _current = new ArrayList<V>(first);
             _lastKeyOfBatch = lastKeyOfBatch;
             _lastKeyOfCounterBatch = lastKeyOfCounterBatch;
             _query = query;
             _counterQuery = counterQuery;
             _currentIter = _current.iterator();
             _next = _currentIter.next();
             _options = options;
         }
 
 
         @Override
         public boolean hasNext()
         {
             return _next != null;
         }
 
         @Override
         public V next()
         {
             if(_next == null)
                 throw new NoSuchElementException();
             
             V rv = _next;
             
             _iteratedCnt++;
             
             if(_iteratedCnt == _options.getMaxRows())
             {
                 _current = null;
                 _currentIter = null;
                 _next = null;
             }
             else
             {
                 if(_currentIter.hasNext())
                     _next = _currentIter.next();
                 else if(_current.size() == 0) //minus one for key boundary overlap
                 {
                     _current = null;
                     _currentIter = null;
                     _next = null;
                 }
                 else 
                 {
                     
                     if(_query != null)
                     {
                         do 
                         {
                             _lastKeyOfBatch = fetch(_query, _lastKeyOfBatch, _options, _current);
                         } while(_lastKeyOfBatch != null && _current.isEmpty());
                     }
                     else
                     {
                         do 
                         {
                             _lastKeyOfCounterBatch = fetch(_counterQuery, _lastKeyOfCounterBatch, _options, _current);
                         } while(_lastKeyOfCounterBatch != null && _current.isEmpty());
                     }
                     
                     if(_current.isEmpty())
                     {
                         _current = null;
                         _currentIter = null;
                         _next = null;
                     }
                     else
                     {
                         _currentIter = _current.iterator();
                         _next = _currentIter.next();
                     }
                 }
             }
             
             if(_next == null)
                 _parent.setSize(_iteratedCnt); //we have a row count, notify parent so subsequent calls to size() don't have to fetch all rows
             
             return rv;
         }
 
         @Override
         public void remove()
         {
             throw new UnsupportedOperationException();
         }
     }
     
     private class LazyLoadedCollection extends AbstractCollection<V>
     {
         RangeSlicesQuery<byte[], byte[], byte[]> _query;
         RangeSlicesCounterQuery<byte[], byte[]> _counterQuery;
         private GetAllOptions _options;
         private List<V> _all = null; //if it is known all rows have been fetched, this field is set
         private List<V> _first = new ArrayList<V>();
         private byte[] _lastKeyOfBatch; //last key of the _first rows
         private byte[] _lastKeyOfCounterBatch; //last key of the _first rows
         private int _size = -1;
         
         public LazyLoadedCollection(GetAllOptions options)
         {
             boolean gettingNormalColumns = options.gettingNormalColumns();
             if(gettingNormalColumns && !_entityMeta.hasNormalColumns())
             {
                 _logger.warn("family {} has no normal columns, assuming caller meant to retrieve counters", _entityMeta.getCounterFamilyName());
                 gettingNormalColumns = false;
             }
             else if(!gettingNormalColumns && !_entityMeta.hasCounterColumns())
             {
                 _logger.warn("family {} has no counter columns, assuming caller meant to retrieve normal columns", _entityMeta.getCounterFamilyName());
                 gettingNormalColumns = true;
             }
             
             if(gettingNormalColumns)
             {
                 _query = HFactory.createRangeSlicesQuery(_keyspaceFactory.createKeyspace(options.getConsistencyLevel()), SER_BYTES, SER_BYTES, SER_BYTES);
                 _query.setColumnFamily(_entityMeta.getFamilyName());
                 do
                 {
                    _lastKeyOfBatch = fetch(_query, null, options, _first); 
                 } while(_lastKeyOfBatch != null && _first.isEmpty());
             }
             else
             {
                 _counterQuery = HFactory.createRangeSlicesCounterQuery(_keyspaceFactory.createKeyspace(options.getConsistencyLevel()), SER_BYTES, SER_BYTES);
                 _counterQuery.setColumnFamily(_entityMeta.getCounterFamilyName());
                 do
                 {
                     _lastKeyOfCounterBatch = fetch(_counterQuery, null, options, _first);
                 } while(_lastKeyOfCounterBatch != null && _first.isEmpty());
             }
             
             if(_first.isEmpty())
                 _all = _first; //no data
             else if(_lastKeyOfBatch == null && _lastKeyOfCounterBatch == null)
                 _all = _first; //all data has been read
             
             _options = options;
         }
 
         //override, don't want to invoke size, just to check if empty
         @Override
         public boolean isEmpty()
         {
             return _first.isEmpty();
         }
         
         //can aggressively fetch and retain all values, use with caution
         @Override
         public int size()
         {
             if(_size >= 0)
                 return _size;
             if(_all == null)
             {
                 Iterator<V> iter = iterator();
                 _all = new ArrayList<V>();
                 while(iter.hasNext() && _all.size() < _options.getMaxRows())
                     _all.add(iter.next());
             }
             
             return _all.size();
         }
         
         @Override
         public java.util.Iterator<V> iterator()
         {
             if(_all != null)
                 return _all.iterator();
             
             return new LazyLoadedIterator(this, _first, _lastKeyOfBatch, _lastKeyOfCounterBatch, _query, _counterQuery, _options);
         }
         
         void setSize(int size)
         {
             _size = size;
         }
     }
 }
