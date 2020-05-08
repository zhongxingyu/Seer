 /*
  * Copyright 2013 paynes.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package cz.muni.rentalservice.handlers;
 
 import java.sql.CallableStatement;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import org.apache.ibatis.type.JdbcType;
 import org.apache.ibatis.type.MappedTypes;
 import org.apache.ibatis.type.TypeHandler;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 /**
  *
  * @author paynes
  */
 @MappedTypes(LocalDate.class)
 public final class LocalDateHandler implements TypeHandler<LocalDate> {
     
     /** Formatter, that is used to convert local date to string and back. */
     private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd");
 
     /**
      * {@inheritDoc }     
      */
     @Override
     public void setParameter(PreparedStatement ps, int i, LocalDate date, JdbcType jt) throws SQLException {
         if (date == null) {
             return;
        }     

        ps.setDate(i, new java.sql.Date(date.toDateTimeAtStartOfDay().getMillis()));

     }
 
     /**
      * {@inheritDoc }     
      */
     @Override
     public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
         if (rs == null) {
             return null;
         }
         
         final String dateValue = rs.getString(columnName);
         
         if (dateValue == null) {
             return null;
         }
         
         return FORMATTER.parseLocalDate(dateValue);
     }
 
     /**
      * {@inheritDoc }     
      */
     @Override
     public LocalDate getResult(ResultSet rs, int columnIndex) throws SQLException {
         if (rs == null) {
             return null;
         }
         
         final String dateValue = rs.getString(columnIndex);
         
         if (dateValue == null) {
             return null;
         }
         
         return FORMATTER.parseLocalDate(dateValue);
     }
 
     /**
      * {@inheritDoc }     
      */
     @Override
     public LocalDate getResult(CallableStatement cs, int i) throws SQLException {
         if (cs == null) {
             return null;
         }
         
         final String dateValue = cs.getString(i);
         
         if (dateValue == null) {
             return null;
         }
         
         return FORMATTER.parseLocalDate(dateValue);
     }    
 }
