 /*
  * This program is part of Zenoss Core, an open source monitoring platform.
  * Copyright (C) 2010, Zenoss Inc.
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License version 2 as published by
  * the Free Software Foundation.
  * 
  * For complete information please visit: http://www.zenoss.com/oss/
  */
 package org.zenoss.zep.dao.impl;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.dao.DuplicateKeyException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.transaction.support.TransactionSynchronizationAdapter;
 import org.springframework.transaction.support.TransactionSynchronizationManager;
 import org.zenoss.zep.dao.DaoCache;
 
 public class DaoCacheImpl implements DaoCache {
 
     private static final int DEFAULT_MAX_CACHE_ENTRIES = 500;
     @SuppressWarnings("unused")
     private static final Logger logger = LoggerFactory
             .getLogger(DaoCacheImpl.class);
 
     private final JdbcTemplate template;
     private final DaoTableStringLruCache eventClassCache = new DaoTableStringLruCache(
             "event_class", DEFAULT_MAX_CACHE_ENTRIES);
     private final DaoTableStringLruCache eventClassKeyCache = new DaoTableStringLruCache(
             "event_class_key", DEFAULT_MAX_CACHE_ENTRIES);
     private final DaoTableStringLruCache monitorCache = new DaoTableStringLruCache(
             "monitor", DEFAULT_MAX_CACHE_ENTRIES);
     private final DaoTableStringLruCache agentCache = new DaoTableStringLruCache(
             "agent", DEFAULT_MAX_CACHE_ENTRIES);
     private final DaoTableStringLruCache groupCache = new DaoTableStringLruCache(
             "event_group", DEFAULT_MAX_CACHE_ENTRIES);
     private final DaoTableStringLruCache eventKeyCache = new DaoTableStringLruCache(
             "event_key", DEFAULT_MAX_CACHE_ENTRIES);
 
     public DaoCacheImpl(JdbcTemplate template) {
         this.template = template;
     }
 
     public void init() {
     }
 
     private <T> int getIdFromName(final DaoTableCache<T> cache, final T name) {
         Integer cached = cache.getCache().getIdFromName(name);
         if (cached == null) {
             final int id = cache.insertOrSelect(template, name);
             TransactionSynchronizationManager
                     .registerSynchronization(new TransactionSynchronizationAdapter() {
                         @Override
                         public void afterCommit() {
                             cache.getCache().cache(name, id);
                         }
                     });
             cached = id;
         }
         return cached;
     }
 
     private <T> T getNameFromId(final DaoTableCache<T> cache, final int id) {
         T cached = cache.getCache().getNameFromId(id);
         if (cached == null) {
            final T name = cache.findNameFromId(template, id);
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(
                        new TransactionSynchronizationAdapter() {
                            @Override
                            public void afterCommit() {
                                cache.getCache().cache(name, id);
                            }
                        });
            }
            else {
                // Not part of a transaction - can safely cache
                cache.getCache().cache(name, id);
            }
            cached = name;
         }
         return cached;
     }
 
     @Override
     @Transactional
     public int getEventClassId(String eventClass) {
         return getIdFromName(this.eventClassCache, eventClass);
     }
 
     @Override
     public String getEventClassFromId(int id) {
         return getNameFromId(this.eventClassCache, id);
     }
 
     @Override
     @Transactional
     public int getEventClassKeyId(String eventClassKey) {
         return getIdFromName(this.eventClassKeyCache, eventClassKey);
     }
 
     @Override
     public String getEventClassKeyFromId(int id) {
         return getNameFromId(this.eventClassKeyCache, id);
     }
 
     @Override
     @Transactional
     public int getMonitorId(String monitor) {
         return getIdFromName(this.monitorCache, monitor);
     }
 
     @Override
     public String getMonitorFromId(int id) {
         return getNameFromId(this.monitorCache, id);
     }
 
     @Override
     @Transactional
     public int getAgentId(String agent) {
         return getIdFromName(this.agentCache, agent);
     }
 
     @Override
     public String getAgentFromId(int id) {
         return getNameFromId(this.agentCache, id);
     }
 
     @Override
     @Transactional
     public int getEventGroupId(String eventGroup) {
         return getIdFromName(this.groupCache, eventGroup);
     }
 
     @Override
     public String getEventGroupFromId(int id) {
         return getNameFromId(this.groupCache, id);
     }
 
     @Override
     @Transactional
     public int getEventKeyId(String eventKey) {
         return getIdFromName(this.eventKeyCache, eventKey);
     }
 
     @Override
     public String getEventKeyFromId(int id) {
         return getNameFromId(this.eventKeyCache, id);
     }
 
     private static class BiMap<T> {
         private final Map<T, Integer> nameToIdMap;
         private final Map<Integer, T> idToNameMap;
         private final int maxCache;
 
         /**
          * Creates a BiMap which is optionally bounded by the specified size.
          * 
          * @param maxCache
          *            The maximum size of the maps to keep. If this is greater
          *            than zero, then this BiMap will function as a LRU cache.
          */
         public BiMap(int maxCache) {
             this.nameToIdMap = new HashMap<T, Integer>();
             if (maxCache <= 0) {
                 this.idToNameMap = new HashMap<Integer, T>();
             } else {
                 this.idToNameMap = new LinkedHashMap<Integer, T>();
             }
             this.maxCache = maxCache;
         }
 
         public synchronized Integer getIdFromName(T name) {
             return this.nameToIdMap.get(name);
         }
 
         public synchronized T getNameFromId(int id) {
             return this.idToNameMap.get(id);
         }
 
         public synchronized void cache(T name, int id) {
             this.idToNameMap.put(id, name);
             this.nameToIdMap.put(name, id);
             if (maxCache > 0 && idToNameMap.size() > maxCache) {
                 final Iterator<Map.Entry<Integer, T>> it = this.idToNameMap
                         .entrySet().iterator();
                 final Map.Entry<Integer, T> lru = it.next();
                 it.remove();
                 this.nameToIdMap.remove(lru.getValue());
             }
         }
     }
 
     private static abstract class DaoTableCache<T> {
         protected final String tableName;
         protected final BiMap<T> cache;
         static final String COLUMN_ID = "id";
         static final String COLUMN_NAME = "name";
 
         public DaoTableCache(String tableName, BiMap<T> cache) {
             this.tableName = tableName;
             this.cache = cache;
         }
 
         @SuppressWarnings("unused")
         public void init(JdbcTemplate template) {
             /* Default is to not prime cache */
         }
 
         public BiMap<T> getCache() {
             return cache;
         }
 
         public abstract int insertOrSelect(final JdbcTemplate template, T name);
 
         public abstract T findNameFromId(final JdbcTemplate template, int id);
     }
 
     private static class DaoTableStringLruCache extends DaoTableCache<String> {
         public DaoTableStringLruCache(String tableName, final int limit) {
             super(tableName, new BiMap<String>(limit));
         }
 
         @Override
         public int insertOrSelect(JdbcTemplate template, String name) {
             try {
                 final SimpleJdbcInsert insert = new SimpleJdbcInsert(template)
                         .withTableName(this.tableName);
                 final Map<String, Object> args = Collections
                         .<String, Object> singletonMap(COLUMN_NAME, name);
                 return insert.usingColumns(COLUMN_NAME)
                         .usingGeneratedKeyColumns(COLUMN_ID)
                         .executeAndReturnKey(args).intValue();
             } catch (DuplicateKeyException e) {
                 return new SimpleJdbcTemplate(template).queryForInt("SELECT "
                         + COLUMN_ID + " FROM " + tableName + " WHERE "
                         + COLUMN_NAME + "=?", name);
             }
         }
 
         @Override
         public String findNameFromId(JdbcTemplate template, int id) {
             return template.queryForObject("SELECT " + COLUMN_NAME + " FROM "
                     + this.tableName + " WHERE " + COLUMN_ID + "=?",
                     String.class, id);
         }
     }
 }
