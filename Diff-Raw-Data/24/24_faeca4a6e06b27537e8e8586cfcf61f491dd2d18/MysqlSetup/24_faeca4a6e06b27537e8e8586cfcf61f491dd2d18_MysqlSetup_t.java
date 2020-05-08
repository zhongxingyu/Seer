 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.lite.jdbc.mysql;
 
 import com.google.common.collect.ImmutableMap;
 
 import org.sakaiproject.nakamura.lite.storage.jdbc.JDBCStorageClientPool;
 
 public class MysqlSetup {
 
     private static JDBCStorageClientPool clientPool = createClientPool();
 
     public synchronized static JDBCStorageClientPool createClientPool() {
         try {
             JDBCStorageClientPool connectionPool = new JDBCStorageClientPool();
             connectionPool
                     .activate(ImmutableMap
                             .of(JDBCStorageClientPool.CONNECTION_URL,
                                     (Object) "jdbc:mysql://127.0.0.1:3306/sakai22?useUnicode=true&amp;characterEncoding=UTF-8",
                                     JDBCStorageClientPool.JDBC_DRIVER, "com.mysql.jdbc.Driver",
                                    "username", "sakai22", "password", "sakai22","store-base-dir", "target/store"));
             return connectionPool;
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     public static JDBCStorageClientPool getClientPool() {
         return clientPool;
     }
 
 }
