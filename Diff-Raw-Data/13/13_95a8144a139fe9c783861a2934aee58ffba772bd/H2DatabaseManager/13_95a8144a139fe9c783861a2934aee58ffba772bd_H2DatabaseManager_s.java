 /**
  *
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 
 package org.apache.maven.eventspy.h2;
 
 import org.h2.Driver;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 
 import javax.sql.DataSource;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 public class H2DatabaseManager {
     private static final Object LOCK = new Object();
     private File dbLocation;
     private DataSource cachedDataSource;
 
     public H2DatabaseManager(File dbLocation) {
         this.dbLocation = dbLocation;
     }
 
 
     public boolean doesDatabaseNotExist() {
         synchronized (LOCK) {
             return dbLocation.listFiles().length == 0;
         }
     }
 
     public void create() {
         synchronized (LOCK) {
            dbLocation.mkdirs();
             JdbcTemplate jdbc = new JdbcTemplate(load());
             jdbc.execute(readCreateScript());
         }
     }
 
     public DataSource load() {
         synchronized (LOCK) {
             if (cachedDataSource == null) {
                 DriverManagerDataSource dataSource = new DriverManagerDataSource();
                 dataSource.setDriverClassName(Driver.class.getName());
                 dataSource.setUrl("jdbc:h2:" + dbLocation.getAbsolutePath() + "/stats");
                 cachedDataSource = dataSource;
             }
             return cachedDataSource;
         }
     }
 
     private String readCreateScript() {
         StringBuilder builder = new StringBuilder();
         byte[] buffer = new byte[1024];
         int length = -1;
 
         InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("sql/create_table.sql");
         try {
             while ((length = input.read(buffer)) != -1) {
                 builder.append(new String(buffer, 0, length));
             }
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
         return builder.toString();
     }
 
     public void setDirectoryOfRepository(File directoryOfRepository) {
         this.dbLocation = directoryOfRepository;
     }
 }
