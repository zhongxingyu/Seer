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
 
 import junit.framework.TestCase;
 import static org.mockito.Mockito.*;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 public class DatabaseColumnFieldSizeResolverTest extends TestCase {
     private DataSource dataSource;
     private Connection connection;
     private DatabaseMetaData dbMetaData;
     private ResultSet resultSet;
     private DatabaseColumnFieldSizeResolver resolver;
 
     protected void setUp() throws Exception {
         super.setUp();
         dataSource = mock(DataSource.class);
         connection = mock(Connection.class);
         dbMetaData = mock(DatabaseMetaData.class);
         resultSet = mock(ResultSet.class);
 
         resolver = new DatabaseColumnFieldSizeResolver();
         resolver.setDataSource(dataSource);
     }
 
     public void test_isResolvable() {
         assertTrue(resolver.isResolvable("schema.table.column"));
         assertFalse(resolver.isResolvable("notValid"));
         assertTrue(resolver.isResolvable("table.column"));
     }
 
     public void test_resolveLength_NoEnoughInfo() throws SQLException {
         try {
             resolver.resolveLength("noEnoughData");
             fail();
         } catch (IllegalArgumentException err) {
            assertEquals("Database column information given in a bad format. A valid format is schema.table.column or table.column", err.getMessage());
         }
 
         verifyZeroInteractions(dataSource, connection);
     }
 
     public void test_resolveLength_NoSchema() throws SQLException {
         when(dataSource.getConnection()).thenReturn(connection);
         when(connection.getMetaData()).thenReturn(dbMetaData);
         when(dbMetaData.getColumns(null, null, "TABLE", "COLUMN")).thenReturn(resultSet);
         when(resultSet.next()).thenReturn(true);
         when(resultSet.getInt("COLUMN_SIZE")).thenReturn(1);
 
         assertEquals(1, resolver.resolveLength("table.column"));
 
         verify(connection).close();
     }
 
     public void test_resolveLength() throws SQLException {
         when(dataSource.getConnection()).thenReturn(connection);
         when(connection.getMetaData()).thenReturn(dbMetaData);
         when(dbMetaData.getColumns(null, "SCHEMA", "TABLE", "COLUMN")).thenReturn(resultSet);
         when(resultSet.next()).thenReturn(true);
         when(resultSet.getInt("COLUMN_SIZE")).thenReturn(1);
 
         assertEquals(1, resolver.resolveLength("schema.table.column"));
 
         verify(connection).close();
     }
 
     public void test_resolveLength_CouldNotFindColumn() throws SQLException {
         when(dataSource.getConnection()).thenReturn(connection);
         when(connection.getMetaData()).thenReturn(dbMetaData);
         when(dbMetaData.getColumns(null, "SCHEMA", "TABLE", "COLUMN")).thenReturn(resultSet);
         when(resultSet.next()).thenReturn(false);
 
         try {
             resolver.resolveLength("schema.table.column");
             fail();
         } catch (IllegalArgumentException err) {
             assertEquals("Could not find column schema.table.column", err.getMessage());
         }
 
         verify(connection).close();
     }
 
     public void test_resolveLength_CouldNotGetConnection() throws SQLException {
         SQLException realError = new SQLException("");
         when(dataSource.getConnection()).thenThrow(realError);
 
         try {
             resolver.resolveLength("schema.table.column");
             fail();
         } catch (RuntimeException err) {
             assertEquals(realError, err.getCause());
         }
 
         verifyZeroInteractions(connection);
     }
 
 
 }
