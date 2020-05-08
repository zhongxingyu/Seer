 package com.mengruojun.common.dao;
 
 
 import com.mengruojun.common.domain.HistoryDataKBar;
 import com.mengruojun.common.domain.Instrument;
 import com.mengruojun.common.domain.TimeWindowType;
 import org.hibernate.jdbc.Work;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 /**
  * HistoryDataKBar Data Access Object (GenericDao) interface.
  *
  */
 public interface HistoryDataKBarDao extends GenericDao<HistoryDataKBar, Long> {
   public interface ResultSetWork {
     /**
      * Usually, in ouside, the code looks like:
      * <p/>
      * while (rs.next()) {
      * resultSetWork.doWork(rs);
      * }
      *
      * So it means the passed in rs object must hasNext.
      * @param rs is one instance of ResultSet
      */
     public void doWork(ResultSet rs) throws SQLException;
   }
   /**
    * find the specified HistoryDataKBar and return null if not found;
    * @param startTime
    * @param instrument
    * @param timeWindowType
    * @return
    */
   HistoryDataKBar find(long startTime, Instrument instrument, TimeWindowType timeWindowType);
 
   HistoryDataKBar getLatestBarForPeriod(Instrument instrument, TimeWindowType timeWindowType);
 
   /**
    * getBarsByOpenTimeRange
    * The openTimes of return bars are all more than or equal to openTimeFrom, but less than openTimeTo
    * @param instrument instrument
    * @param timeWindowType timeWindowType
    * @param openTimeFrom openTimeFrom
    * @param openTimeTo openTimeTo
    * @return a list of HistoryDataKBar
    */
   List<HistoryDataKBar> getBarsByOpenTimeRange(Instrument instrument, TimeWindowType timeWindowType, long openTimeFrom, long openTimeTo);
 
   /**
    * use jdbc mode to save bars in a batch. if get any exception, than, use hibernate saving one row by one row;
    * @param bars
    */
  void batchSave(List<HistoryDataKBar> bars) throws Exception;
 
   String getMysqlTimeZone();
 
   /**
    * work is a jdbc style to read data.
    * @param resultSetWork rs
    */
   public void readAll(final ResultSetWork resultSetWork);
 }
