 package net.codjo.mad.server.handler.select;
 import java.sql.ResultSet;
 import java.sql.Types;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 public class GetterTest {
 
     @Test
     public void test_constructor_index() throws Exception {
         Getter getter = new Getter(0) {
             @Override
             public String get(Object bean) throws Exception {
                 return null;
             }
         };
         ResultSet resultSet = Mockito.mock(ResultSet.class);
 
         getter.get(resultSet);
 
        Mockito.verify(resultSet, Mockito.times(1)).getString(0);
     }
 
 
     @Test
     public void test_constructor_index_date() throws Exception {
         Getter getter = new Getter(0, Types.DATE) {
             @Override
             public String get(Object bean) throws Exception {
                 return null;
             }
         };
         ResultSet resultSet = Mockito.mock(ResultSet.class);
 
         getter.get(resultSet);
 
         Mockito.verify(resultSet, Mockito.times(1)).getDate(0);
     }
 
 
     @Test
     public void test_constructor_index_timestamp() throws Exception {
         Getter getter = new Getter(0, Types.TIMESTAMP) {
             @Override
             public String get(Object bean) throws Exception {
                 return null;
             }
         };
         ResultSet resultSet = Mockito.mock(ResultSet.class);
 
         getter.get(resultSet);
 
         Mockito.verify(resultSet, Mockito.times(1)).getTimestamp(0);
     }
 
 
     @Test
     public void test_constructor_name() throws Exception {
         Getter getter = new Getter("MY_COL") {
             @Override
             public String get(Object bean) throws Exception {
                 return null;
             }
         };
         ResultSet resultSet = Mockito.mock(ResultSet.class);
 
         getter.get(resultSet);
 
        Mockito.verify(resultSet, Mockito.times(1)).getString("MY_COL");
     }
 
 
     @Test
     public void test_constructor_name_date() throws Exception {
         Getter getter = new Getter("MY_COL", Types.DATE) {
             @Override
             public String get(Object bean) throws Exception {
                 return null;
             }
         };
         ResultSet resultSet = Mockito.mock(ResultSet.class);
 
         getter.get(resultSet);
 
         Mockito.verify(resultSet, Mockito.times(1)).getDate("MY_COL");
     }
 
 
     @Test
     public void test_constructor_name_timestamp() throws Exception {
         Getter getter = new Getter("MY_COL", Types.TIMESTAMP) {
             @Override
             public String get(Object bean) throws Exception {
                 return null;
             }
         };
         ResultSet resultSet = Mockito.mock(ResultSet.class);
 
         getter.get(resultSet);
 
         Mockito.verify(resultSet, Mockito.times(1)).getTimestamp("MY_COL");
     }
 }
