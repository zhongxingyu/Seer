 package com.twu.thoughtconf.dao;
 
 import org.apache.commons.lang.NotImplementedException;
 import org.apache.ibatis.type.BaseTypeHandler;
 import org.apache.ibatis.type.JdbcType;
 import org.joda.time.DateTime;
 
 import java.sql.*;
 
 // Responsible for mapping a java Date to a joda DateTime
 public class DateTimeTypeHandler extends BaseTypeHandler<DateTime> {
 
 
     @Override
     public void setNonNullParameter(PreparedStatement preparedStatement, int i, DateTime dateTime, JdbcType jdbcType) throws SQLException {
//        preparedStatement.setDate(i, new Date(dateTime.toDate().getTime()));
          preparedStatement.setTimestamp(i, new Timestamp(dateTime.toDate().getTime()));
     }
 
     @Override
     public DateTime getNullableResult(ResultSet resultSet, String columnName) throws SQLException {
         Timestamp date = resultSet.getTimestamp(columnName);
         return new DateTime(date);
     }
 
     @Override
     public DateTime getNullableResult(ResultSet resultSet, int i) throws SQLException {
         throw new NotImplementedException();
     }
 
     @Override
 
     public DateTime getNullableResult(CallableStatement callableStatement, int i) throws SQLException {
         throw new NotImplementedException();
     }
 }
