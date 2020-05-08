 package com.twu.thoughtconf.dao;
 
 import org.apache.ibatis.type.JdbcType;
 import org.joda.time.DateTime;
 import org.junit.Test;
 
 import java.sql.Date;
 import java.sql.PreparedStatement;
import java.sql.Timestamp;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 public class DateTimeTypeHandlerTest {
     @Test
     public void testSetNonNullParameter() throws Exception {
         DateTimeTypeHandler handler = new DateTimeTypeHandler();
         int index = 1;
         DateTime dateTime = DateTime.now();
 
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        handler.setNonNullParameter(preparedStatement, index, dateTime, JdbcType.DATE);
 
        verify(preparedStatement).setTimestamp(index, new Timestamp(dateTime.toDate().getTime()));
     }
 
 }
