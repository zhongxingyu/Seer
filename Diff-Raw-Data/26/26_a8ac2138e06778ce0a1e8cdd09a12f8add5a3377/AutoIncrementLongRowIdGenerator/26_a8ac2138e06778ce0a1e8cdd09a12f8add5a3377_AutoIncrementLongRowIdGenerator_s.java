 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
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
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.hbase.autoincrement;
 
 import com.sun.jersey.spi.resource.Singleton;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import org.apache.commons.lang.mutable.MutableLong;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.RowLock;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author imyousuf
  */
 @Path("/{tableName}")
 @Singleton
 public class AutoIncrementLongRowIdGenerator {
 
   private Configuration hbaseConfiguration;
   private final Map<String, MutableLong> tableCurrentMax = new HashMap<String, MutableLong>();
   private final ObjectMapper objectMapper = new ObjectMapper();
   private HTablePool pool;
   protected final Logger logger = LoggerFactory.getLogger(getClass());
 
   public Configuration getHbaseConfiguration() {
     if (hbaseConfiguration == null) {
       hbaseConfiguration = HBaseConfiguration.create();
     }
     return hbaseConfiguration;
   }
 
   public HTablePool getPool() {
     if (pool == null) {
       pool = new HTablePool(getHbaseConfiguration(), 200);
     }
     return pool;
   }
 
   @POST
   @Produces(MediaType.APPLICATION_JSON)
   public Response get(@PathParam("tableName") String tableName, @QueryParam("retry") @DefaultValue("3") int retry)
       throws IOException {
     MutableLong mutableLong = tableCurrentMax.get(tableName);
     final HTableInterface table;
     table = getPool().getTable(tableName);
     if (mutableLong == null) {
       synchronized (tableCurrentMax) {
         if (!tableCurrentMax.containsKey(tableName)) {
           mutableLong = new MutableLong(-1l);
           tableCurrentMax.put(tableName, mutableLong);
         }
         else {
           mutableLong = tableCurrentMax.get(tableName);
         }
       }
       synchronized (mutableLong) {
         if (mutableLong.longValue() < 0) {
           Scan scan = new Scan();
           ResultScanner scanner = table.getScanner(scan);
           try {
             Result result[];
             result = scanner.next(1);
             if (result != null && result.length > 0) {
               mutableLong.setValue(Bytes.toLong(result[0].getRow()));
             }
             else {
               mutableLong.setValue(Long.MAX_VALUE);
             }
           }
           finally {
             if (scanner != null) {
               try {
                 scanner.close();
               }
               catch (Exception ex) {
                 logger.warn("Could not close scanner!", ex);
               }
             }
           }
         }
       }
     }
     final IdConfig config;
     synchronized (mutableLong) {
       mutableLong.add(-1l);
       boolean rowExists = table.exists(new Get(Bytes.toBytes(mutableLong.longValue())));
       while (rowExists) {
         mutableLong.add(-1l);
         rowExists = table.exists(new Get(Bytes.toBytes(mutableLong.longValue())));
       }
       final long id = mutableLong.longValue();
       RowLock lock;
       int retryCount = 0;
       do {
         try {
           lock = table.lockRow(Bytes.toBytes(id));
         }
         catch (Exception ex) {
           lock = null;
           logger.warn("Could not attain lock, retrying!", ex);
           retryCount++;
         }
       }
       while (lock == null && retryCount < retry);
       if (lock == null) {
         return Response.status(Response.Status.SERVICE_UNAVAILABLE).build();
       }
       config = new IdConfig(id, lock.getLockId());
     }
     ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
     objectMapper.writeValue(outputStream, config);
     return Response.ok(outputStream.toByteArray()).build();
 
   }
 }
