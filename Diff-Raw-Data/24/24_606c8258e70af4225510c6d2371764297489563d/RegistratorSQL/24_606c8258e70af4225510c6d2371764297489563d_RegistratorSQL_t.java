 package ru.mail.jira.plugins;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.sql.DataSource;
 import org.apache.commons.dbcp.ConnectionFactory;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDataSource;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 import ru.mail.jira.plugins.structs.HrDate;
 import ru.mail.jira.plugins.structs.TimeOffBean;
 import ru.mail.jira.plugins.structs.TimePair;
 
 /**
  * This class processes all SQL operations of "arrival" plug-in.
  * 
  * @author Andrey Markelov
  */
 public class RegistratorSQL
 {
     private static DataSource dataSource;
 
     private static boolean isDriverInitialized = false;
 
     /**
      * Lock object for initialization datasource object.
      */
     private static final Object lock = new Object();
 
     /**
      * Logger.
      */
     private static Log log = LogFactory.getLog(RegistratorSQL.class);
 
     static
     {
         try
         {
             Class.forName(ArrivalConstants.DRIVER_CLASS).newInstance();
             isDriverInitialized = true;
         }
         catch (Exception e)
         {
             isDriverInitialized = false;
         }
     }
 
     public static synchronized void initDataSource(
         String host,
         String port,
         String dbName,
         String user,
         String password)
     {
         String jdbcUrl = String.format("jdbc:mysql://%s:%s/%s?useUnicode=yes&characterEncoding=UTF-8", host, port, dbName);
 
         GenericObjectPool connectionPool = new GenericObjectPool(null);
         connectionPool.setMinIdle(1);
         connectionPool.setMaxActive(4);
         connectionPool.setMaxWait(120000);
         connectionPool.setTestOnBorrow(true);
 
         ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(
             jdbcUrl,
             user,
             password);
 
         PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(
             connectionFactory,
             connectionPool,
             null,
             null,
             false,
             true);
         dataSource = new PoolingDataSource(poolableConnectionFactory.getPool()); 
     }
 
     public static boolean isDriverInitialized()
     {
         return isDriverInitialized;
     }
 
     public static void setDriverInitialized(boolean isDriverInitialized)
     {
         RegistratorSQL.isDriverInitialized = isDriverInitialized;
     }
 
     public RegistratorSQL(ConfigurationManager configurationManager)
     {
         synchronized (lock)
         {
             if (dataSource == null)
             {
                 initDataSource(
                     configurationManager.getDbHost(),
                     configurationManager.getDbPort(),
                     configurationManager.getDatabase(),
                     configurationManager.getUsername(),
                     configurationManager.getPassword());
             }
         }
     }
 
     public void addCancelTime(
         String user)
     throws SQLException
     {
         Calendar cal = Calendar.getInstance();
         cal.add(Calendar.DAY_OF_MONTH, 1);
         cal.set(Calendar.HOUR_OF_DAY, 5);
         cal.set(Calendar.MINUTE, 0);
         cal.set(Calendar.SECOND, 0);
         cal.set(Calendar.MILLISECOND, 0);
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.ADD_CANCEL_REG);
             pStmt.setString(1, user);
             pStmt.setTimestamp(2, new java.sql.Timestamp(cal.getTimeInMillis()));
             pStmt.executeUpdate();
         }
         catch (SQLException sqlex)
         {
             if (!sqlex.getSQLState().equals("23000")) //--> index_constraint
             {
                 throw sqlex;
             }
         }
         finally
         {
             close(null, pStmt, conn);
         }
     }
 
     /**
      * Add HR registered date.
      */
     public void addHrDate(HrDate hrDate)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.ADD_HRDATE);
             pStmt.setInt(1, hrDate.getYear());
             pStmt.setInt(2, hrDate.getMonth());
             pStmt.setDate(3, new java.sql.Date(hrDate.getHrDate().getTime()));
             pStmt.executeUpdate();
         }
         finally
         {
             close(null, pStmt, conn);
         }
     }
 
     private void close(ResultSet rs, PreparedStatement pStmt, Connection conn)
     {
         closeResultSet(rs);
         closePreparedStatement(pStmt);
         closeConnection(conn);
     }
 
     private void closeConnection(Connection conn)
     {
         if (conn != null)
         {
             try
             {
                 conn.close();
             }
             catch (SQLException e)
             {
                 log.error("Cannot close connection.", e);
             }
         }
     }
 
     private void closePreparedStatement(PreparedStatement pStmt)
     {
         if (pStmt != null)
         {
             try
             {
                 pStmt.close();
             }
             catch (SQLException e)
             {
                 log.error("Cannot close prepared statement", e);
             }
         }
     }
 
     private void closeResultSet(ResultSet rs)
     {
         if (rs != null)
         {
             try
             {
                 rs.close();
             }
             catch (SQLException e)
             {
                 log.error("Cannot close resultset", e);
             }
         }
     }
 
     public void deleteDelay(long delayId)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.DELETE_DELAY);
             pStmt.setLong(1, delayId);
             pStmt.executeUpdate();
         }
         finally
         {
             close(null, pStmt, conn);
         }
     }
 
     public Map<String, List<Date>> getAllArrivals(
         String[] users,
         Date startDate,
         Date endDate)
     throws SQLException
     {
         Map<String, List<Date>> result = new HashMap<String, List<Date>>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_USER_TIMES);
 
             List<Date> res;
             for (String user : users)
             {
                 res = new ArrayList<Date>();
                 pStmt.clearParameters();
                 pStmt.setString(1, user);
                 pStmt.setDate(2, new java.sql.Date(startDate.getTime()));
                 pStmt.setDate(3, new java.sql.Date(endDate.getTime()));
                 rs = pStmt.executeQuery();
                 while (rs.next())
                 {
                     res.add(rs.getTimestamp(1));
                 }
                 result.put(user, res);
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return result;
     }
 
     public Map<Date, Boolean> getCalendarRange(
         Date ldate,
         Date rdate)
     throws SQLException
     {
         Map<Date, Boolean> dayList = new HashMap<Date, Boolean>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_CALENDAR_RANGE);
             pStmt.setDate(1, new java.sql.Date(ldate.getTime()));
             pStmt.setDate(2, new java.sql.Date(rdate.getTime()));
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 dayList.put(rs.getDate(1), Boolean.parseBoolean(rs.getString(2)));
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return dayList;
     }
 
     /**
      * 
      */
     public Map<Integer, Boolean> getDayoff(Integer year, Integer month)
     throws SQLException
     {
         Map<Integer, Boolean> dayList = new HashMap<Integer, Boolean>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_CALENDAR);
             pStmt.setInt(1, year);
             pStmt.setInt(2, month);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 dayList.put(rs.getInt(1), Boolean.parseBoolean(rs.getString(2)));
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return dayList;
     }
 
     /**
      * Get HR registered date by year and month.
      */
     public HrDate getHrDate(int year, int month)
     throws SQLException
     {
         HrDate hrDate = null;
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_HRDATE);
             pStmt.setInt(1, year);
             pStmt.setInt(2, month);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 hrDate = new HrDate();
                 hrDate.setYear(rs.getInt(1));
                 hrDate.setMonth(rs.getInt(2));
                 hrDate.setHrDate(rs.getDate(3));
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return hrDate;
     }
 
     /**
      * Get last HR registered date.
      */
     public Date getLastHrDate()
     throws SQLException
     {
         Date hrDate = null;
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_LASTHRDATE);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 hrDate = rs.getDate(1);
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return hrDate;
     }
 
     /**
      * Get HR registered dates.
      */
     public List<HrDate> getLastHrDates()
     throws SQLException
     {
         List<HrDate> hrDates = new ArrayList<HrDate>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_HRDATES);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 HrDate hrDate = new HrDate();
                 hrDate.setYear(rs.getInt(1));
                 hrDate.setMonth(rs.getInt(2));
                 hrDate.setHrDate(rs.getDate(3));
                 hrDates.add(hrDate);
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return hrDates;
     }
 
     public TimePair getRegistrationTime(String username)
     throws SQLException
     {
         TimePair timePair = new TimePair();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.REG_TIME_QUERY);
             pStmt.setString(1, username);
             rs = pStmt.executeQuery();
             if (rs.next())
             {
                 timePair.setTimeOn(rs.getTimestamp(1));
             }
 
             closeResultSet(rs);
             closePreparedStatement(pStmt);
 
             pStmt = conn.prepareStatement(SqlQueries.GET_CANCEL_TIME);
             pStmt.setString(1, username);
             rs = pStmt.executeQuery();
             if (rs.next())
             {
                 timePair.setcTime(rs.getTimestamp(1));
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return timePair;
     }
 
     public List<TimeOffBean> getTimeOffList()
     throws SQLException
     {
         List<TimeOffBean> res = new ArrayList<TimeOffBean>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_DELAYS);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 TimeOffBean tb = new TimeOffBean();
                 tb.setId(rs.getLong(1));
                 tb.setUser(rs.getString(2));
                 tb.setReporter(rs.getString(3));
                 tb.setDelayDate(rs.getDate(4));
                 tb.setDelayTime(rs.getString(5));
                 tb.setAbsent(rs.getString(6));
                 tb.setComment(rs.getString(7));
                 res.add(tb);
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return res;
     }
 
     public List<TimeOffBean> getTimeOffRange(
         Date startDate,
         Date endDate)
     throws SQLException
     {
         List<TimeOffBean> res = new ArrayList<TimeOffBean>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_DELAYS);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 TimeOffBean tb = new TimeOffBean();
                 tb.setId(rs.getLong(1));
                 tb.setUser(rs.getString(2));
                 tb.setReporter(rs.getString(3));
                 tb.setDelayDate(rs.getDate(4));
                 tb.setDelayTime(rs.getString(5));
                 tb.setAbsent(rs.getString(6));
                 tb.setComment(rs.getString(7));
                 res.add(tb);
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return res;
     }
 
     public List<Date> getUserArrivals(
         String user,
         Date startDate,
         Date endDate)
     throws SQLException
     {
         List<Date> res = new ArrayList<Date>();
 
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_USER_TIMES);
             pStmt.setString(1, user);
             pStmt.setDate(2, new java.sql.Date(startDate.getTime()));
             pStmt.setDate(3, new java.sql.Date(endDate.getTime()));
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 res.add(rs.getTimestamp(1));
             }
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return res;
     }
 
     public List<Timestamp> getWeekReport(String username)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
            Calendar monday = ArrivalUtils.getMonday();
            Calendar sunday = Calendar.getInstance();
            sunday.setTimeInMillis(monday.getTimeInMillis());
            sunday.add(Calendar.DAY_OF_MONTH, 7);
             ArrayList<Timestamp> result = new ArrayList<Timestamp>();
 
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.WEEK_TIMES_QUERY);
             pStmt.setString(1, username);
            pStmt.setDate(2, new java.sql.Date(monday.getTimeInMillis()));
            pStmt.setDate(3, new java.sql.Date(sunday.getTimeInMillis()));
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 result.add(rs.getTimestamp(1));
             }
 
             return result;
         }
         finally
         {
             close(rs, pStmt, conn);
         }
     }
 
     public Timestamp register(String username)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         try
         {
             Timestamp registration = getRegistrationTime(username).getTimeOn();
             if (null != registration)
             {
                 return registration;
             }
 
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.REGISTER_TIME);
             pStmt.setString(1, username);
             pStmt.executeUpdate();
             return getRegistrationTime(username).getTimeOn();
         }
         finally
         {
             close(null, pStmt, conn);
         }
     }
 
     public boolean registerDelay(
         String user,
         String reporter,
         Date delayDate,
         String delayTime,
         String lateReason,
         String switchAbsent)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         ResultSet rs = null;
         try
         {
             conn = dataSource.getConnection();
 
             pStmt = conn.prepareStatement(SqlQueries.CHECK_DELAY);
             pStmt.setString(1, user);
             pStmt.setDate(2, new java.sql.Date(delayDate.getTime()));
             rs = pStmt.executeQuery();
             if (rs.next())
             {
                 return false;
             }
 
             closePreparedStatement(pStmt);
             pStmt = conn.prepareStatement(SqlQueries.REGISTER_DELAY);
             pStmt.setString(1, user);
             pStmt.setString(2, reporter);
             pStmt.setDate(3, new java.sql.Date(delayDate.getTime()));
             pStmt.setString(4, delayTime);
             pStmt.setString(5, switchAbsent);
             pStmt.setString(6, lateReason);
             pStmt.executeUpdate();
         }
         finally
         {
             close(rs, pStmt, conn);
         }
 
         return true;
     }
 
     public void saveCalendar(
         int year,
         int month,
         String[] checked,
         String[] unchecked)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         PreparedStatement pStmt2 = null;
         ResultSet rs = null;
         try
         {
             Map<Integer, Boolean> daysMap = new HashMap<Integer, Boolean>();
 
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.GET_CALENDAR);
             pStmt.setInt(1, year);
             pStmt.setInt(2, month);
             rs = pStmt.executeQuery();
             while (rs.next())
             {
                 daysMap.put(rs.getInt(1), rs.getBoolean(2));
             }
 
             closeResultSet(rs);
             closePreparedStatement(pStmt);
 
             Calendar cal = Calendar.getInstance();
 
             pStmt = conn.prepareStatement(SqlQueries.ADD_CALENDAR);
             pStmt2 = conn.prepareStatement(SqlQueries.UPDATE_CALENDAR);
             if (checked != null)
             {
                 for (String checkedDay : checked)
                 {
                     Integer icheckedDay = Integer.valueOf(checkedDay);
                     cal.set(year, month - 1, Integer.valueOf(checkedDay));
                     if (daysMap.containsKey(icheckedDay))
                     {
                         pStmt2.clearParameters();
                         pStmt2.setString(1, Boolean.TRUE.toString());
                         pStmt2.setDate(2, new java.sql.Date(cal.getTimeInMillis()));
                         pStmt2.executeUpdate();
                     }
                     else
                     {
                         pStmt.clearParameters();
                         pStmt.setDate(1, new java.sql.Date(cal.getTimeInMillis()));
                         pStmt.setString(2, Boolean.TRUE.toString());
                         pStmt.executeUpdate();
                     }
                 }
             }
 
             if (unchecked != null)
             {
                 for (String uncheckedDay : unchecked)
                 {
                     Integer iuncheckedDay = Integer.valueOf(uncheckedDay);
                     cal.set(year, month - 1, Integer.valueOf(uncheckedDay));
                     if (daysMap.containsKey(iuncheckedDay))
                     {
                         pStmt2.clearParameters();
                         pStmt2.setString(1, Boolean.FALSE.toString());
                         pStmt2.setDate(2, new java.sql.Date(cal.getTimeInMillis()));
                         pStmt2.executeUpdate();
                     }
                     else
                     {
                         pStmt.clearParameters();
                         pStmt.setDate(1, new java.sql.Date(cal.getTimeInMillis()));
                         pStmt.setString(2, Boolean.FALSE.toString());
                         pStmt.executeUpdate();
                     }
                 }
             }
         }
         finally
         {
             closePreparedStatement(pStmt2);
             close(rs, pStmt, conn);
         }
     }
 
     public void deleteHrDate(HrDate hrDate)
     throws SQLException
     {
         Connection conn = null;
         PreparedStatement pStmt = null;
         try
         {
             conn = dataSource.getConnection();
             pStmt = conn.prepareStatement(SqlQueries.DELETE_HRDATE);
             pStmt.setInt(1, hrDate.getYear());
             pStmt.setInt(2, hrDate.getMonth());
             pStmt.executeUpdate();
         }
         finally
         {
             close(null, pStmt, conn);
         }
     }
 }
