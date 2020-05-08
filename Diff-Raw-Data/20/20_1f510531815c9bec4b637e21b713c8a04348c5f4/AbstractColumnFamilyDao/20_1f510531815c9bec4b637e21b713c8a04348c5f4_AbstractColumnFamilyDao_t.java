 /*******************************************************************************
  * Copyright (c) 2006-2010 eBay Inc. All Rights Reserved.
  * Licensed under the Apache License, Version 2.0 (the "License"); 
  * you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *******************************************************************************/
 package org.ebayopensource.turmeric.utils.cassandra.dao;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import me.prettyprint.cassandra.serializers.BytesArraySerializer;
 import me.prettyprint.cassandra.serializers.LongSerializer;
 import me.prettyprint.cassandra.serializers.ObjectSerializer;
 import me.prettyprint.cassandra.serializers.SerializerTypeInferer;
 import me.prettyprint.cassandra.serializers.StringSerializer;
 import me.prettyprint.hector.api.Keyspace;
 import me.prettyprint.hector.api.beans.ColumnSlice;
 import me.prettyprint.hector.api.beans.HColumn;
 import me.prettyprint.hector.api.beans.OrderedRows;
 import me.prettyprint.hector.api.beans.Row;
 import me.prettyprint.hector.api.beans.Rows;
 import me.prettyprint.hector.api.factory.HFactory;
 import me.prettyprint.hector.api.mutation.Mutator;
 import me.prettyprint.hector.api.query.MultigetSliceQuery;
 import me.prettyprint.hector.api.query.QueryResult;
 import me.prettyprint.hector.api.query.RangeSlicesQuery;
 import me.prettyprint.hector.api.query.SliceQuery;
 
 import org.ebayopensource.turmeric.utils.cassandra.hector.HectorHelper;
 import org.ebayopensource.turmeric.utils.cassandra.hector.HectorManager;
 
 /**
  * The Class AbstractColumnFamilyDao.
  * 
  * @author jamuguerza
  * @param <KeyType>
  *           the generic type
  * @param <T>
  *           the generic type
  */
 public abstract class AbstractColumnFamilyDao<KeyType, T> {
 
    /** The persistent class. */
    protected final Class<T> persistentClass;
 
    /** The key type class. */
    protected final Class<KeyType> keyTypeClass;
 
    /** The key space. */
    protected final Keyspace keySpace;
 
    /** The column family name. */
    protected final String columnFamilyName;
 
    /** The all column names. */
    protected final String[] allColumnNames;
 
    /**
     * Instantiates a new abstract column family dao.
     * 
     * @param clusterName
     *           the clusterName
     * @param host
     *           the host
     * @param s_keyspace
     *           the s_keyspace
     * @param keyTypeClass
     *           the key type class
     * @param persistentClass
     *           the persistent class
     * @param columnFamilyName
     *           the column family name
     */
    public AbstractColumnFamilyDao(final String clusterName, final String host, final String s_keyspace,
             final Class<KeyType> keyTypeClass, final Class<T> persistentClass, final String columnFamilyName) {
       this.keySpace = new HectorManager().getKeyspace(clusterName, host, s_keyspace, columnFamilyName, false, null,
                keyTypeClass);
       this.keyTypeClass = keyTypeClass;
       this.persistentClass = persistentClass;
       this.columnFamilyName = columnFamilyName;
       this.allColumnNames = HectorHelper.getAllColumnNames(persistentClass);
 
    }
 
    /**
     * Save.
     * 
     * @param key
     *           the key
     * @param model
     *           the model
     */
    public void save(KeyType key, T model) {
 
       Mutator<Object> mutator = HFactory.createMutator(keySpace, SerializerTypeInferer.getSerializer(keyTypeClass));
       for (HColumn<?, ?> column : HectorHelper.getColumns(model)) {
          mutator.addInsertion(key, columnFamilyName, column);
       }
 
       mutator.execute();
    }
 
    /**
     * Find.
     * 
     * @param key
     *           the key
     * @return the t
     */
    public T find(KeyType key) {
       SliceQuery<Object, String, byte[]> query = HFactory.createSliceQuery(keySpace,
                SerializerTypeInferer.getSerializer(keyTypeClass), StringSerializer.get(), BytesArraySerializer.get());
 
      int columnQuerySize = 100;
       QueryResult<ColumnSlice<String, byte[]>> result = query.setColumnFamily(columnFamilyName).setKey(key)
       // .setColumnNames(allColumnNames).execute();
 
               .setRange("", "", false, columnQuerySize).execute();
 
       try {
          if (result.get().getColumns().isEmpty()) {
             return null;
          }
       } catch (Exception e) {
          return null;
       }
 
       try {
          Constructor<?>[] constructorsT = persistentClass.getConstructors();
          T t = null;
          if (constructorsT.length > 1) {
             if (constructorsT[0].getParameterTypes().length == 1
                      && constructorsT[0].getParameterTypes()[0].isAssignableFrom(keyTypeClass)) {
                t = (T) constructorsT[0].newInstance(keyTypeClass);
             } else {
                t = (T) constructorsT[0].newInstance();
             }
          } else {
             if (constructorsT[0].getParameterTypes().length == 1
                      && (constructorsT[0].getParameterTypes()[0].isAssignableFrom(keyTypeClass) || Object.class
                               .equals(constructorsT[0].getParameterTypes()[0]))) {
                t = (T) constructorsT[0].newInstance(keyTypeClass);
             } else {
                t = (T) constructorsT[0].newInstance();
             }
          }
 
          HectorHelper.populateEntity(t, key, result);
          return t;
       } catch (Exception e) {
          throw new RuntimeException("Error creating persistent class", e);
       }
    }
 
    /**
     * Delete.
     * 
     * @param key
     *           the key
     * @see http://wiki.apache.org/cassandra/DistributedDeletes
     */
    public void delete(KeyType key) {
       Mutator<Object> mutator = HFactory.createMutator(keySpace, SerializerTypeInferer.getSerializer(keyTypeClass));
       mutator.delete(key, columnFamilyName, null, SerializerTypeInferer.getSerializer(keyTypeClass));
    }
 
    /**
     * Gets the keys.
     * 
     * @return the keys
     */
    public Set<KeyType> getKeys() {
       int rows = 0;
       int pagination = 50;
       Set<KeyType> rowKeys = new HashSet<KeyType>();
 
       Row<Object, String, byte[]> lastRow = null;
 
       do {
          RangeSlicesQuery<Object, String, byte[]> rangeSlicesQuery = HFactory
                   .createRangeSlicesQuery(keySpace, SerializerTypeInferer.getSerializer(keyTypeClass),
                            StringSerializer.get(), BytesArraySerializer.get());
          rangeSlicesQuery.setColumnFamily(columnFamilyName);
          if (lastRow != null) {
             rangeSlicesQuery.setKeys(lastRow.getKey(), "");
          } else {
             rangeSlicesQuery.setKeys(null, null);
          }
          rangeSlicesQuery.setReturnKeysOnly();
          rangeSlicesQuery.setRange(null, null, false, keyTypeClass.getDeclaredFields().length);
          rangeSlicesQuery.setRowCount(pagination);
          QueryResult<OrderedRows<Object, String, byte[]>> result = rangeSlicesQuery.execute();
          OrderedRows<Object, String, byte[]> orderedRows = result.get();
          rows = orderedRows.getCount();
 
          for (Row<Object, String, byte[]> row : orderedRows) {
             if (!row.getColumnSlice().getColumns().isEmpty()) {
                rowKeys.add((KeyType) row.getKey());
             }
          }
 
          lastRow = orderedRows.peekLast();
 
       } while (rows == pagination);
 
       return rowKeys;
 
    }
 
    /**
     * Find items.
     * 
     * @param keys
     *           the keys
     * @param rangeFrom
     *           the range from
     * @param rangeTo
     *           the range to
     * @return the sets the
     */
    public Set<T> findItems(final List<KeyType> keys, final String rangeFrom, final String rangeTo) {
 
       Set<T> items = new HashSet<T>();
 
       MultigetSliceQuery<Object, String, byte[]> multigetSliceQuery = HFactory.createMultigetSliceQuery(keySpace,
                SerializerTypeInferer.getSerializer(keyTypeClass), StringSerializer.get(), BytesArraySerializer.get());
 
       multigetSliceQuery.setColumnFamily(columnFamilyName);
       multigetSliceQuery.setKeys(keys.toArray());
       multigetSliceQuery.setRange(rangeFrom, rangeTo, false, 50);
 
       QueryResult<Rows<Object, String, byte[]>> result = multigetSliceQuery.execute();
 
       for (Row<Object, String, byte[]> row : result.get()) {
          if (!row.getColumnSlice().getColumns().isEmpty()) {
             items.add((T) row.getColumnSlice());
          }
       }
 
       return items;
    }
 
    /**
     * Find items.
     * 
     * @param keys
     *           the keys
     * @param rangeFrom
     *           the range from
     * @param rangeTo
     *           the range to
     * @return the sets the
     */
    public Map<KeyType, Map<Long, String>> findItems(final List<KeyType> keys, final Long rangeFrom, final Long rangeTo) {
 
       Map<KeyType, Map<Long, String>> items = new HashMap<KeyType, Map<Long, String>>();
 
       MultigetSliceQuery<Object, Long, byte[]> multigetSliceQuery = HFactory.createMultigetSliceQuery(keySpace,
                SerializerTypeInferer.getSerializer(keyTypeClass), LongSerializer.get(), BytesArraySerializer.get());
 
       multigetSliceQuery.setColumnFamily(columnFamilyName);
       multigetSliceQuery.setKeys(keys.toArray());
       multigetSliceQuery.setRange(rangeFrom, rangeTo, false, 50);
 
       QueryResult<Rows<Object, Long, byte[]>> result = multigetSliceQuery.execute();
 
       for (Row<Object, Long, byte[]> row : result.get()) {
          if (!row.getColumnSlice().getColumns().isEmpty()) {
             HashMap<Long, String> columnHashMap = new HashMap<Long, String>();
 
             ColumnSlice<Long, byte[]> col = row.getColumnSlice();
             List<HColumn<Long, byte[]>> columns = col.getColumns();
             for (HColumn<Long, byte[]> hColumn : columns) {
                columnHashMap.put(hColumn.getName(), hColumn.getValue().toString());
             }
 
             items.put((KeyType) row.getKey(), columnHashMap);
          }
       }
 
       return items;
    }
 
    public Map<KeyType, Map<Long, Object>> findItemsWithObjectColumnValues(final List<KeyType> keys,
             final Long rangeFrom, final Long rangeTo) {
 
       Map<KeyType, Map<Long, Object>> items = new HashMap<KeyType, Map<Long, Object>>();
 
       MultigetSliceQuery<Object, Long, Object> multigetSliceQuery = HFactory.createMultigetSliceQuery(keySpace,
                SerializerTypeInferer.getSerializer(keyTypeClass), LongSerializer.get(), ObjectSerializer.get());
 
       multigetSliceQuery.setColumnFamily(columnFamilyName);
       multigetSliceQuery.setKeys(keys.toArray());
       multigetSliceQuery.setRange(rangeFrom, rangeTo, false, 50);
 
       QueryResult<Rows<Object, Long, Object>> result = multigetSliceQuery.execute();
 
       for (Row<Object, Long, Object> row : result.get()) {
          if (!row.getColumnSlice().getColumns().isEmpty()) {
             HashMap<Long, Object> columnHashMap = new HashMap<Long, Object>();
 
             ColumnSlice<Long, Object> col = row.getColumnSlice();
             List<HColumn<Long, Object>> columns = col.getColumns();
             for (HColumn<Long, Object> hColumn : columns) {
                columnHashMap.put(hColumn.getName(), hColumn.getValue());
             }
 
             items.put((KeyType) row.getKey(), columnHashMap);
          }
       }
 
       return items;
    }
 
    public Map<KeyType, Map<Long, String>> findItemsWithStringColumnValues(final List<KeyType> keys,
             final Long rangeFrom, final Long rangeTo) {
 
       Map<KeyType, Map<Long, String>> items = new HashMap<KeyType, Map<Long, String>>();
 
       MultigetSliceQuery<Object, Long, String> multigetSliceQuery = HFactory.createMultigetSliceQuery(keySpace,
                SerializerTypeInferer.getSerializer(keyTypeClass), LongSerializer.get(), StringSerializer.get());
 
       multigetSliceQuery.setColumnFamily(columnFamilyName);
       multigetSliceQuery.setKeys(keys.toArray());
       multigetSliceQuery.setRange(rangeFrom, rangeTo, false, 50);
 
       QueryResult<Rows<Object, Long, String>> result = multigetSliceQuery.execute();
 
       for (Row<Object, Long, String> row : result.get()) {
          if (!row.getColumnSlice().getColumns().isEmpty()) {
             HashMap<Long, String> columnHashMap = new HashMap<Long, String>();
 
             ColumnSlice<Long, String> col = row.getColumnSlice();
             List<HColumn<Long, String>> columns = col.getColumns();
             for (HColumn<Long, String> hColumn : columns) {
                columnHashMap.put(hColumn.getName(), hColumn.getValue());
             }
 
             items.put((KeyType) row.getKey(), columnHashMap);
          }
       }
 
       return items;
    }
 
    /**
     * Contains.
     * 
     * @param key
     *           the key
     * @return true, if successful
     * @see http://wiki.apache.org/cassandra/DistributedDeletes
     */
    public boolean containsKey(KeyType key) {
       RangeSlicesQuery<Object, String, byte[]> rangeSlicesQuery = HFactory.createRangeSlicesQuery(keySpace,
                SerializerTypeInferer.getSerializer(keyTypeClass), StringSerializer.get(), BytesArraySerializer.get());
       rangeSlicesQuery.setColumnFamily(columnFamilyName);
       rangeSlicesQuery.setKeys(key, key);
       rangeSlicesQuery.setReturnKeysOnly();
       rangeSlicesQuery.setRange("", "", false, 1);
       rangeSlicesQuery.setRowCount(1);
       QueryResult<OrderedRows<Object, String, byte[]>> result = rangeSlicesQuery.execute();
       OrderedRows<Object, String, byte[]> orderedRows = result.get();
 
       return (!orderedRows.getList().isEmpty() && !orderedRows.getByKey(key).getColumnSlice().getColumns().isEmpty());
    }
 
 }
