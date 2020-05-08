 /**
  * Copyright (C) 2012 Ness Computing, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.nesscomputing.jdbc;
 
import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.TimeZone;
 
 import javax.sql.DataSource;
 
 import org.skife.jdbi.v2.DBI;
 import org.skife.jdbi.v2.Handle;
 import org.skife.jdbi.v2.tweak.HandleCallback;
 import org.skife.jdbi.v2.util.StringMapper;
 
 import com.nesscomputing.logging.Log;
 
 /**
  * Check that a pool is ready for use.
  *
  */
 class DatabaseChecker {
     private static final TimeZone UTC = TimeZone.getTimeZone("UTC");
 
     private static final Log LOG = Log.findLog();
 
     static void checkPool(final DataSource pool) throws SQLException {
         // Right now just check that the DB and JVM are UTC.  This caused me infinite pain
         // and as such we will refuse to set up such a misconfigured system.
 
         final DBI dbi = new DBI(pool);
 
         final String timeZone = dbi.withHandle(new HandleCallback<String>() {
 
             @Override
             public String withHandle(final Handle handle) throws Exception
             {
                 final String productName = handle.getConnection().getMetaData().getDatabaseProductName();
                 // right now, we only care about PG
                 if (!"POSTGRESQL".equalsIgnoreCase(productName)) {
                     return "UTC";
                 }
                 return handle.createQuery("SHOW timezone").map(StringMapper.FIRST).first();
             }
         });
 
         if (!"UTC".equalsIgnoreCase(timeZone)) {
             throw new IllegalStateException(String.format("Postgres database time zone must be set to UTC but is %s. Please fix your database configuration.", timeZone));
         }
     }
 
    /**
     * Check that the connection works, before a pool is created.  That way if the URL is wrong we get a proper error message
     * rather than "timeout while waiting for acquire" from C3P0.
     */
    static void checkConnection(DataSource unpooledDataSource) throws SQLException
    {
        Connection connection = unpooledDataSource.getConnection();
        connection.close();
    }

     static void fixUTCTimezone()
     {
         if (!TimeZone.getDefault().equals(UTC)) {
             LOG.warn("JVM timezone is not UTC! Switching to UTC to ensure correct timestamps on Postgres!");
             TimeZone.setDefault(UTC);
         }
     }
 }
