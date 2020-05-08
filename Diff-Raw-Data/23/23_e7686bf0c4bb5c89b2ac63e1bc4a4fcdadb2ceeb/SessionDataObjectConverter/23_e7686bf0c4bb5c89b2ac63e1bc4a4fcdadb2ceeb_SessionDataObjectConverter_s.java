 /*
  *
  * This module intended to be used for session replication of Jetty via HBase
  * and later will be cached via Ehcache
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package com.smartitengineering.jetty.session.replication.impl.hbase;
 
 import com.smartitengineering.dao.impl.hbase.spi.ExecutorService;
 import com.smartitengineering.dao.impl.hbase.spi.impl.AbstractObjectRowConverter;
 import com.smartitengineering.jetty.session.replication.SessionData;
 import com.smartitengineering.jetty.session.replication.SessionDataId;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.util.Map;
 import org.apache.commons.io.input.ClassLoaderObjectInputStream;
 import org.apache.commons.lang.SerializationException;
 import org.apache.commons.lang.SerializationUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 
 /**
  *
  * @author imyousuf
  */
 public class SessionDataObjectConverter extends AbstractObjectRowConverter<SessionData, SessionDataId> {
 
   public static final byte[] FAMILY_SELF = Bytes.toBytes("self");
   public static final byte[] CELL_ROW_ID = Bytes.toBytes("rowId");
   public static final byte[] CELL_ACCESSED = Bytes.toBytes("accessed");
   public static final byte[] CELL_LAST_ACCESSED = Bytes.toBytes("lastAccessed");
   public static final byte[] CELL_MAX_IDLE_MS = Bytes.toBytes("maxIdleMs");
   public static final byte[] CELL_COOKIE_SET = Bytes.toBytes("cookieSet");
   public static final byte[] CELL_CREATED = Bytes.toBytes("created");
   public static final byte[] CELL_LAST_NODE = Bytes.toBytes("lastNode");
   public static final byte[] CELL_CANNONICAL_CONTEXT = Bytes.toBytes("cannonicalContext");
   public static final byte[] CELL_LAST_SAVED = Bytes.toBytes("lastSaved");
   public static final byte[] CELL_EXPIRY_TIME = Bytes.toBytes("expiryTime");
   public static final byte[] CELL_VIRTUAL_HOST = Bytes.toBytes("virtualHost");
   public static final byte[] CELL_ATTRIBUTE_MAP = Bytes.toBytes("attributes");
 
   @Override
   protected String[] getTablesToAttainLock() {
     return new String[]{getInfoProvider().getMainTableName()};
   }
 
   @Override
   protected void getPutForTable(SessionData instance, ExecutorService service, Put put) {
     if (instance == null) {
       return;
     }
     put.add(FAMILY_SELF, CELL_ACCESSED, Bytes.toBytes(instance.getAccessed()));
     put.add(FAMILY_SELF, CELL_COOKIE_SET, Bytes.toBytes(instance.getCookieSet()));
     put.add(FAMILY_SELF, CELL_CREATED, Bytes.toBytes(instance.getCreated()));
     put.add(FAMILY_SELF, CELL_EXPIRY_TIME, Bytes.toBytes(instance.getExpiryTime()));
     put.add(FAMILY_SELF, CELL_LAST_ACCESSED, Bytes.toBytes(instance.getLastAccessed()));
    if (StringUtils.isNotBlank(instance.getLastNode())) {
      put.add(FAMILY_SELF, CELL_LAST_NODE, Bytes.toBytes(instance.getLastNode()));
    }
     put.add(FAMILY_SELF, CELL_LAST_SAVED, Bytes.toBytes(instance.getLastSaved()));
     put.add(FAMILY_SELF, CELL_MAX_IDLE_MS, Bytes.toBytes(instance.getMaxIdleMs()));
     Map<String, Object> attrs = instance.getAttributeMap();
     if (attrs != null && !attrs.isEmpty()) {
       put.add(FAMILY_SELF, CELL_ATTRIBUTE_MAP, SerializationUtils.serialize((Serializable) attrs));
     }
   }
 
   @Override
   protected void getDeleteForTable(SessionData instance, ExecutorService service, Delete put) {
     //Nothing to do as whole row needs to be deleted
   }
 
   @Override
   public SessionData rowsToObject(Result startRow, ExecutorService executorService) {
     try {
      SessionData data = new SessionData(getInfoProvider().getIdFromRowId(startRow.getRow()), null);
       data.setAccessed(getLong(startRow, FAMILY_SELF, CELL_ACCESSED));
       data.setCookieSet(getLong(startRow, FAMILY_SELF, CELL_COOKIE_SET));
       data.setCreated(getLong(startRow, FAMILY_SELF, CELL_CREATED));
       data.setExpiryTime(getLong(startRow, FAMILY_SELF, CELL_EXPIRY_TIME));
       data.setLastAccessed(getLong(startRow, FAMILY_SELF, CELL_LAST_ACCESSED));
      data.setLastNode(getString(startRow, FAMILY_SELF, CELL_LAST_NODE));
       data.setLastSaved(getLong(startRow, FAMILY_SELF, CELL_LAST_SAVED));
       data.setMaxIdleMs(getLong(startRow, FAMILY_SELF, CELL_MAX_IDLE_MS));
       byte[] attrs = startRow.getValue(FAMILY_SELF, CELL_ATTRIBUTE_MAP);
       if (attrs != null) {
         data.setAttributeMap((Map) deserialize(attrs));
       }
       return data;
     }
     catch (Exception ex) {
       logger.error("Could convert row to session!", ex);
       throw new RuntimeException(ex);
     }
   }
 
   protected long getLong(Result rowData, byte[] family, byte[] qualifier) {
     byte[] val;
     val = rowData.getValue(family, qualifier);
     if (val == null) {
       return 0;
     }
     else {
       return Bytes.toLong(val);
     }
   }
 
   protected String getString(Result rowData, byte[] family, byte[] qualifier) {
     byte[] val;
     val = rowData.getValue(family, qualifier);
     if (val == null) {
       return null;
     }
     else {
       return Bytes.toString(val);
     }
   }
 
   private static Object deserialize(InputStream inputStream) {
     if (inputStream == null) {
       throw new IllegalArgumentException("The InputStream must not be null");
     }
     ObjectInputStream in = null;
     try {
       // stream closed in the finally
       in = new ClassLoaderObjectInputStream(Thread.currentThread().getContextClassLoader(), inputStream);
       return in.readObject();
 
     }
     catch (ClassNotFoundException ex) {
       throw new SerializationException(ex);
     }
     catch (IOException ex) {
       throw new SerializationException(ex);
     }
     finally {
       try {
         if (in != null) {
           in.close();
         }
       }
       catch (IOException ex) {
         // ignore close exception
       }
     }
   }
 
   private static Object deserialize(byte[] objectData) {
     if (objectData == null) {
       throw new IllegalArgumentException("The byte[] must not be null");
     }
     ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
     return deserialize(bais);
   }
 }
