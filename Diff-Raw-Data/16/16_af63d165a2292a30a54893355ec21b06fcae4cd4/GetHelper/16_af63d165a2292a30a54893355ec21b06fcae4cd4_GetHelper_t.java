 package com.feedly.cassandra.dao;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 
 import me.prettyprint.hector.api.Serializer;
 import me.prettyprint.hector.api.beans.AbstractComposite.ComponentEquality;
 import me.prettyprint.hector.api.beans.DynamicComposite;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import com.feedly.cassandra.IKeyspaceFactory;
 import com.feedly.cassandra.entity.EntityMetadata;
 import com.feedly.cassandra.entity.PropertyMetadata;
 
 /*
  * helper class to do gets (reads by row key).
  */
 class GetHelper<K, V> extends LoadHelper<K, V>
 {
     GetHelper(EntityMetadata<V> meta, IKeyspaceFactory factory)
     {
         super(meta, factory);
     }
 
     public V get(K key, V value, GetOptions options)
     {
         switch(options.getColumnFilterStrategy())
         {
             case UNFILTERED:
                 return loadFromGet(key, null, null, null, null);
             case INCLUDES:
                 return get(key, value, options.getIncludes(), options.getExcludes());
             case RANGE:
                 return loadFromGet(key, value, null, propertyName(options.getStartColumn()), propertyName(options.getEndColumn()));
         }
         
         throw new IllegalStateException(); //never happens
     }
 
     private V get(K key, V value, Set<? extends Object> includes, Set<String> excludes)
     {
         _logger.debug("loading {}[{}]", _entityMeta.getFamilyName(), key);
 
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         List<byte[]> colNames = new ArrayList<byte[]>();
         List<PropertyMetadata> fullCollectionProperties = derivePartialColumns(colNames, includes, excludes);
 
         value = loadFromGet(key, value, colNames, null, null);
 
         if(fullCollectionProperties != null)
         {
             for(PropertyMetadata pm : fullCollectionProperties)
             {
                 DynamicComposite dc = new DynamicComposite();
                 dc.addComponent(0, pm.getName(), ComponentEquality.EQUAL);
                 byte[] colBytes = SER_COMPOSITE.toBytes(dc);
                 dc = new DynamicComposite();
                 dc.addComponent(0, pm.getName(), ComponentEquality.GREATER_THAN_EQUAL); // little strange, this really means the first value
                                                                                         // greater than...
                 byte[] colBytesEnd = SER_COMPOSITE.toBytes(dc);
 
                 value = loadFromGet(key, value, null, colBytes, colBytesEnd);
             }
         }
 
         return value;
     }
 
     public List<V> mget(Collection<K> keys)
     {
         return bulkLoadFromMultiGet(keys, null, null, null, null, false);
     }
 
     public List<V> mget(List<K> keys, List<V> values, GetOptions options)
     {
         if(keys == null)
             throw new IllegalArgumentException("keys parameter is null");
         if(values != null && keys.size() != values.size())
             throw new IllegalArgumentException("key and value list must be same size");
         
         switch(options.getColumnFilterStrategy())
         {
             case UNFILTERED:
                 return bulkLoadFromMultiGet(keys, values, null, null, null, true);
             case INCLUDES:
                 return mget(keys, values, options.getIncludes(), options.getExcludes());
             case RANGE:
                 return bulkLoadFromMultiGet(keys, values, null, propertyName(options.getStartColumn()), propertyName(options.getEndColumn()), true);
         }
 
         throw new IllegalStateException(); //never happens
     }
     
     private List<V> mget(List<K> keys, List<V> values, Set<? extends Object> includes, Set<String> excludes)
     {
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         if(includes != null && excludes != null)
             throw new IllegalArgumentException("either includes or excludes should be specified, not both");
 
         List<byte[]> colNames = new ArrayList<byte[]>();
         List<PropertyMetadata> fullCollectionProperties = derivePartialColumns(colNames, includes, excludes);
 
         values = bulkLoadFromMultiGet(keys, values, colNames, null, null, true);
 
         return addFullCollectionProperties(keys, values, fullCollectionProperties);
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private V loadFromGet(K key, V value, List<byte[]> cols, byte[] from, byte[] to)
     {
        _logger.debug("loading {}[{}]", _entityMeta.getFamilyName(), key);
 
         PropertyMetadata keyMeta = _entityMeta.getKeyMetadata();
         byte[] keyBytes = ((Serializer) keyMeta.getSerializer()).toBytes(key);
 
         SliceQuery<byte[], byte[], byte[]> query = buildSliceQuery(keyBytes);
 
         if(cols != null)
             query.setColumnNames(cols.toArray(new byte[cols.size()][]));
         else
             query.setRange(from, to, false, CassandraDaoBase.COL_RANGE_SIZE);
 
         return fromColumnSlice(key, value, keyMeta, keyBytes, query, query.execute().get(), to);
     }
 
 
 }
