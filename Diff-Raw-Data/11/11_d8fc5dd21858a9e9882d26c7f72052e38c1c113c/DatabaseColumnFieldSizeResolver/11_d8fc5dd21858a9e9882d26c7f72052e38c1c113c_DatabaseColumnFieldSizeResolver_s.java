 /**
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
 package kramer;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.MessageFormat;
 
 
 public class DatabaseColumnFieldSizeResolver implements FieldSizeResolver {
     private static final String COULD_NOT_FIND_COLUMN = "Could not find column {0}";
     private DataSource dataSource;
 
     public int resolveLength(String fieldInfo) {
         Connection connection = null;
         try {
             String schema = null;
             String table;
             String column;
             String parts[] = fieldInfo.toUpperCase().split("\\.");
 
             if (parts.length == 2) {
                 table = parts[0];
                 column = parts[1];
             } else if (parts.length == 3) {
                 schema = parts[0];
                 table = parts[1];
                 column = parts[2];
             } else {
                throw new IllegalArgumentException("Database column information given in a bad format. A valid format is schema.table.column or table.column");
             }
 
             connection = dataSource.getConnection();
             ResultSet resultSet = connection.getMetaData().getColumns(null, schema, table, column);
             if (resultSet.next())
                 return resultSet.getInt("COLUMN_SIZE");
             else {
                 throw new IllegalArgumentException(MessageFormat.format(COULD_NOT_FIND_COLUMN, fieldInfo));
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
 
                 }
             }
         }
     }
 
     public boolean isResolvable(String fieldInfo) {
         String[] parts = fieldInfo.split("\\.");
         return parts.length == 3 || parts.length == 2;
     }
 
     public void setDataSource(DataSource dataSource) {
         this.dataSource = dataSource;
     }
 }
