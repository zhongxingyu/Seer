 package com.mengruojun.common.dao.impl;
 
 import com.mengruojun.common.dao.HistoryDataKBarDao;
 import com.mengruojun.common.domain.HistoryDataKBar;
 import com.mengruojun.common.domain.Instrument;
 import com.mengruojun.common.domain.TimeWindowType;
 import org.apache.log4j.Logger;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.jdbc.Work;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.jdbc.core.BatchPreparedStatementSetter;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowCallbackHandler;
 import org.springframework.jdbc.support.JdbcAccessor;
 import org.springframework.orm.hibernate3.HibernateCallback;
 import org.springframework.stereotype.Repository;
 
 import javax.sql.DataSource;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 
 /**
  * This class interacts with Spring's HibernateTemplate to save/delete and
  * retrieve Role objects.
  *
  * @author <a href="mailto:bwnoll@gmail.com">Bryan Noll</a>
  */
 @Repository
 public class HistoryDataKBarDaoImpl extends GenericDaoHibernate<HistoryDataKBar, Long> implements HistoryDataKBarDao {
   Logger logger = Logger.getLogger(this.getClass());
 
   /**
    * Constructor to create a Generics-based version using Role as the entity
    */
   public HistoryDataKBarDaoImpl() {
     super(HistoryDataKBar.class);
   }
 
   //private HibernateTemplate hibernateTemplate;
   private JdbcTemplate jdbcTemplate;
 
   /**
    * @param dataSource the jdbc data source
    */
   @Autowired
   public final void setDataSource(@Qualifier("dataSource") DataSource dataSource) {
     if (jdbcTemplate == null || dataSource != ((JdbcAccessor) jdbcTemplate.getDataSource()).getDataSource()) {
       jdbcTemplate = new JdbcTemplate(dataSource);
     }
   }
 
 
   /**
    * | id   | version | closeTime     | currency1 | currency2 | askClose | askHigh | askLow | askOpen | askVolume | bidClose | bidHigh | bidLow  | bidOpen | bidVolume | openTime      | timeWindowType |
    */
   @Override
   public void readAll(final ResultSetWork resultSetWork) {
 
     final String sql = "SELECT id,version,closeTime,currency1,currency2,askClose,askHigh,askLow,askOpen,askVolume,bidClose,bidHigh,bidLow,bidOpen,bidVolume,openTime    ,timeWindowType  FROM HistoryDataKBar ORDER BY openTime ASC";
     this.getHibernateTemplate().executeFind(new HibernateCallback() {
       @Override
       public Object doInHibernate(Session session) throws HibernateException, SQLException {
         Map<Instrument, HistoryDataKBar> mdmMap;
 
         List<HistoryDataKBar> result = new ArrayList();
         session.doWork(new Work() {
           @Override
           public void execute(Connection conn) throws SQLException {
             PreparedStatement ps = null;
             ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             while (rs.next()) {
               resultSetWork.doWork(rs);
             }
             rs.close();
             ps.close();
           }
         });
         return result;
       }
     });
   }
 
   @Override
   public HistoryDataKBar find(long openTime, Instrument instrument, TimeWindowType timeWindowType) {
     List bars = getHibernateTemplate().find("from HistoryDataKBar where instrument.currency1 = ? and instrument.currency2 = ?" +
             "and openTime = ? and timeWindowType = ?", instrument.getCurrency1(), instrument.getCurrency2(), openTime, timeWindowType);
     if (bars.size() > 0) {
       return (HistoryDataKBar) bars.get(0);
     } else {
       return null;
     }
   }
 
   @Override
   public HistoryDataKBar getLatestBarForPeriod(final Instrument instrument, final TimeWindowType timeWindowType) {
     final String hql = "from HistoryDataKBar where instrument.currency1 = :currency1 and instrument.currency2 = :currency2 " +
             "and timeWindowType = :timeWindowType order by openTime desc ";
     HistoryDataKBar bar = (HistoryDataKBar) getHibernateTemplate().execute(
             new HibernateCallback() {
               public Object doInHibernate(Session session)
                       throws HibernateException, SQLException {
                 Query query = session.createQuery(hql);
                 query.setParameter("currency1", instrument.getCurrency1());
                 query.setParameter("currency2", instrument.getCurrency2());
                 query.setParameter("timeWindowType", timeWindowType);
                 query.setFirstResult(0);
                 query.setMaxResults(1);
                 return query.uniqueResult();
               }
             });
     return bar;
   }
 
   /**
    * getBarsByOpenTimeRange
    * The openTimes of return bars are all more than or equal to openTimeFrom, but less than openTimeTo
    *
    * @param instrument     instrument
    * @param timeWindowType timeWindowType
    * @param openTimeFrom   openTimeFrom
    * @param openTimeTo     openTimeTo
    * @return a list of HistoryDataKBar
    */
   @Override
   public List<HistoryDataKBar> getBarsByOpenTimeRange(final Instrument instrument, final TimeWindowType timeWindowType, final long openTimeFrom, final long openTimeTo) {
     List bars = getHibernateTemplate().find("from HistoryDataKBar where instrument.currency1 = ? and instrument.currency2 = ?" +
             "and openTime >= ? and openTime < ? and timeWindowType = ? order by openTime",
             instrument.getCurrency1(), instrument.getCurrency2(), openTimeFrom, openTimeTo, timeWindowType);
     return bars;
   }
 
   /**
    * Batch save rows -- method 3
    * Notice, the performance of this method is not tested. But it looks like the same as method1
    *
    * @param bars bars
    */
   private void bacthSave_jdbcTemplate_prepareStatement_batch(final List<HistoryDataKBar> bars)throws SQLException  {
     Connection conn = null;
     PreparedStatement ps = null;
     ResultSet rs = null;
     try {
       // 分批条数
       int preCount = 1000;
       String sql = "INSERT INTO HistoryDataKBar " +
               "( version,closeTime,currency1,currency2,askClose,askHigh,askLow,askOpen,askVolume,bidClose,bidHigh,bidLow,bidOpen,bidVolume,openTime,timeWindowType)" +
               "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
       conn = this.jdbcTemplate.getDataSource().getConnection();
       ps = conn.prepareStatement(sql);
       conn.setAutoCommit(false);
       for (int i = 0; i < bars.size(); i++) {
         HistoryDataKBar historyDataKBar = bars.get(i);
         ps.setInt(1, 0);
         ps.setLong(2, historyDataKBar.getCloseTime());
 
         ps.setString(3, historyDataKBar.getInstrument().getCurrency1().toString());
         ps.setString(4, historyDataKBar.getInstrument().getCurrency2().toString());
 
         ps.setDouble(5, historyDataKBar.getOhlc().getAskClose());
         ps.setDouble(6, historyDataKBar.getOhlc().getAskHigh());
         ps.setDouble(7, historyDataKBar.getOhlc().getAskLow());
         ps.setDouble(8, historyDataKBar.getOhlc().getAskOpen());
         ps.setDouble(9, historyDataKBar.getOhlc().getAskVolume());
 
         ps.setDouble(10, historyDataKBar.getOhlc().getBidClose());
         ps.setDouble(11, historyDataKBar.getOhlc().getBidHigh());
         ps.setDouble(12, historyDataKBar.getOhlc().getBidLow());
         ps.setDouble(13, historyDataKBar.getOhlc().getBidOpen());
         ps.setDouble(14, historyDataKBar.getOhlc().getBidVolume());
 
         ps.setLong(15, historyDataKBar.getOpenTime());
         ps.setString(16, historyDataKBar.getTimeWindowType().toString());
 
         ps.addBatch();
         if ((i % preCount) == 0) {
           ps.executeBatch();
         }
       }
     } catch (Exception e) {
       logger.error("数据出错,已进行回滚", e);
       if(conn !=null) conn.rollback();
     } finally {
       if(conn !=null){
         conn.commit();
         conn.close();
       }
       if(ps !=null ) ps.close();
     }
   }
 
   /**
    * Batch save rows -- method 2
    * Notice, the performance of this method is very good. It will spend  less 1s or so to save 10000 rows
    *
    * @param bars bars
    */
  private void bacthSave_jdbcTemplate_prepareStatement_longSql(final List<HistoryDataKBar> bars) throws Exception {
     if(bars.size()==0) return;
     Connection conn = null;
     PreparedStatement ps = null;
     ResultSet rs = null;
     try {
       String sql = "INSERT INTO HistoryDataKBar " +
               "( version,closeTime,currency1,currency2,askClose,askHigh,askLow,askOpen,askVolume," +
               "bidClose,bidHigh,bidLow,bidOpen,bidVolume,openTime,timeWindowType)" +
               "VALUES ";
       conn = this.jdbcTemplate.getDataSource().getConnection();
       ps = conn.prepareStatement(sql);
       conn.setAutoCommit(false);
 
       StringBuilder sb = new StringBuilder();
       sb.append(sql);
       for (int i = 0; i < bars.size(); i++) {
         HistoryDataKBar historyDataKBar = bars.get(i);
 
         if(i > 0) sb.append(",");
 
         sb.append("(");
         sb.append("0" + ",");                                          //1
         sb.append(historyDataKBar.getCloseTime()).append(",");         //2
         sb.append("'").append(historyDataKBar.getInstrument().getCurrency1().toString()).append("'").append(",");        //3
         sb.append("'").append(historyDataKBar.getInstrument().getCurrency2().toString()).append("'").append(",");     //4
 
         sb.append(historyDataKBar.getOhlc().getAskClose()).append(",");//5
         sb.append(historyDataKBar.getOhlc().getAskHigh()).append(",");//6
         sb.append(historyDataKBar.getOhlc().getAskLow()).append(",");//7
         sb.append(historyDataKBar.getOhlc().getAskOpen()).append(",");//8
         sb.append(historyDataKBar.getOhlc().getAskVolume()).append(",");//9
 
         sb.append(historyDataKBar.getOhlc().getBidClose()).append(",");//10
         sb.append(historyDataKBar.getOhlc().getBidHigh()).append(",");//11
         sb.append(historyDataKBar.getOhlc().getBidLow()).append(",");//12
         sb.append(historyDataKBar.getOhlc().getBidOpen()).append(",");//13
         sb.append(historyDataKBar.getOhlc().getBidVolume()).append(",");//14
 
 
         sb.append(historyDataKBar.getOpenTime()).append(",");//15
         sb.append("'").append(historyDataKBar.getTimeWindowType().toString()).append("'");//16
         sb.append(")");
 
       }
 
       ps.executeUpdate(sb.toString());
     } catch (Exception e) {
       logger.error("数据出错,已进行回滚", e);
       if(conn !=null) conn.rollback();
      throw e;
     } finally {
       if(conn !=null){
         conn.commit();
         conn.close();
       }
       if(ps !=null ) ps.close();
     }
 
   }
 
 
   /**
    * Batch save rows -- method 1
    * Notice, the performance of this method is not good. It will spend 8 minutes or so to save 10000 rows
    *
    * @param bars  bars
    */
   private void bacthSave_jdbcTemplate_BatchPreparedStatementSetter(final List<HistoryDataKBar> bars) {
     //id      | version | closeTime     | currency1 | currency2 | askClose | askHigh | askLow  | askOpen | askVolume | bidClose | bidHigh | bidLow  | bidOpen | bidVolume | openTime      | timeWindowType
     if (bars != null && bars.size() > 0) {
       String sql = "INSERT INTO HistoryDataKBar " +
               "( version,closeTime,currency1,currency2,askClose,askHigh,askLow,askOpen,askVolume,bidClose,bidHigh,bidLow,bidOpen,bidVolume,openTime,timeWindowType)" +
               "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
       this.jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
 
         @Override
         public void setValues(PreparedStatement ps, int i) throws SQLException {
           HistoryDataKBar historyDataKBar = bars.get(i);
           ps.setInt(1, 0);
           ps.setLong(2, historyDataKBar.getCloseTime());
 
           ps.setString(3, historyDataKBar.getInstrument().getCurrency1().toString());
           ps.setString(4, historyDataKBar.getInstrument().getCurrency2().toString());
 
           ps.setDouble(5, historyDataKBar.getOhlc().getAskClose());
           ps.setDouble(6, historyDataKBar.getOhlc().getAskHigh());
           ps.setDouble(7, historyDataKBar.getOhlc().getAskLow());
           ps.setDouble(8, historyDataKBar.getOhlc().getAskOpen());
           ps.setDouble(9, historyDataKBar.getOhlc().getAskVolume());
 
           ps.setDouble(10, historyDataKBar.getOhlc().getBidClose());
           ps.setDouble(11, historyDataKBar.getOhlc().getBidHigh());
           ps.setDouble(12, historyDataKBar.getOhlc().getBidLow());
           ps.setDouble(13, historyDataKBar.getOhlc().getBidOpen());
           ps.setDouble(14, historyDataKBar.getOhlc().getBidVolume());
 
           ps.setLong(15, historyDataKBar.getOpenTime());
           ps.setString(16, historyDataKBar.getTimeWindowType().toString());
         }
 
         @Override
         public int getBatchSize() {
           return bars.size();
         }
       });
 
     }
   }
 
   @Override
   /**
    * use jdbc mode to save bars in a batch. if get any exception, than, use hibernate saving one row by one row;
    */
  public void batchSave(final List<HistoryDataKBar> bars) throws Exception {
     //bacthSave_jdbcTemplate_BatchPreparedStatementSetter(bars);
     try{
 
      // bacthSave_jdbcTemplate_prepareStatement_batch(bars);
       bacthSave_jdbcTemplate_prepareStatement_longSql(bars);
     } catch (SQLException e){
       logger.error(e);
       throw e;
     }
   }
 
   @Override
   public String getMysqlTimeZone() {
 
     final StringBuffer timezone = new StringBuffer();
     jdbcTemplate.query("show variables LIKE 'time_zone'",
             new Object[]{},
             new RowCallbackHandler() {
               public void processRow(ResultSet rs) throws SQLException {
                 timezone.append(rs.getString(2));
               }
             });
 
     return timezone.toString();
 
 
   }
 }
