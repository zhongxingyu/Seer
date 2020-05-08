 package com.aconex.scrutineer.jdbc;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.Iterator;
 
 import com.aconex.scrutineer.IdAndVersion;
 import org.apache.commons.lang.NotImplementedException;
 
 public class IdAndVersionResultSetIterator implements Iterator<IdAndVersion> {
 
     private final ResultSet resultSet;
 
     private IdAndVersion current;
     private final int columnClass;
 
     public IdAndVersionResultSetIterator(ResultSet resultSet) {
         this.resultSet = resultSet;
         try {
             this.columnClass = resultSet.getMetaData().getColumnType(2);
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
         nextRow();
     }
 
     @Override
     public boolean hasNext() {
         return current != null;
     }
 
     @Override
     public IdAndVersion next() {
         try {
             return current;
         } finally {
             nextRow();
         }
     }
 
     @Override
     public void remove() {
         throw new NotImplementedException();
     }
 
     // TODO talk to Leon about this Cyclomatic Complexity for checkstyle
     //CHECKSTYLE:OFF
    @SuppressWarnings("PMD.NcssMethodCount")
     private long getVersionValueAnLong() throws SQLException {
         switch (this.columnClass) {
             case Types.TIMESTAMP:
                 return resultSet.getTimestamp(2).getTime();
 
             case Types.BIGINT:
             case Types.INTEGER:
                 return resultSet.getLong(2);
             default:
                 throw new UnsupportedOperationException(String.format("Do not know how to handle version column type (java.sql.Type value=%d", columnClass));
         }
     }
    //CHECKSTYLE:ON
 
    @SuppressWarnings("PMD.NcssMethodCount")
     private void nextRow() {
         try {
             if (resultSet.next()) {
                 current = new IdAndVersion(resultSet.getString(1), getVersionValueAnLong());
             } else {
                 current = null;
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     ResultSet getResultSet() {
         return resultSet;
     }
 }
