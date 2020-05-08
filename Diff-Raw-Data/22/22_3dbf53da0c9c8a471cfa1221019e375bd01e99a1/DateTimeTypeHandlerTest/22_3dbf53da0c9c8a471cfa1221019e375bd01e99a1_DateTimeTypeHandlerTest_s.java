 package com.twu.thoughtconf.dao;
 
 import org.apache.ibatis.type.JdbcType;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class DateTimeTypeHandlerTest {
     @Test
     public void testSetNonNullParameter() throws Exception {
         DateTimeTypeHandler handler = new DateTimeTypeHandler();
         int index = 1;
         DateTime dateTime = DateTime.now();
 
        PreparedStatement mock = mock(PreparedStatement.class);
        handler.setNonNullParameter(mock, index, dateTime, JdbcType.DATE);
 
        Date expectedDate = new Date(dateTime.toDate().getTime());
        verify(mock).setDate(index, expectedDate);
     }
 
 }
