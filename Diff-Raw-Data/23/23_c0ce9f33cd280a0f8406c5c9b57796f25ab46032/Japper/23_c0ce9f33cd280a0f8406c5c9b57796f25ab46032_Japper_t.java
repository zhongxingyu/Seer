 package org.dt.japper;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /*
  * Copyright (c) 2012, David Sykes and Tomasz Orzechowski 
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  * 
  * - Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  * 
  * - Neither the name David Sykes nor Tomasz Orzechowski may be used to endorse
  * or promote products derived from this software without specific prior written
  * permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE. 
  * 
  * @author David Sykes
  * 
  * 
  */
 
 
 /**
  * This is basically a rip-off of the .NET Dapper mini-ORM
  * http://code.google.com/p/dapper-dot-net/
  * 
  * The idea is that the conversion from SQL query to object is "magic"
  * No configuration, annotations, or anything. It should just work
  * 
  * We assume SQL is far better at doing the data stuff than anything we could
  * come up with, and we just worry about munging a result set into a nice object
  * for us to work with
  * 
  */
 public class Japper {
   
   private static final Log log = LogFactory.getLog(Japper.class);
   
   
   private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<Connection>();
   
   public static Connection setThreadConnection(Connection conn) {
     threadConnection.set(conn);
     return conn;
   }
   
   public static Connection getThreadConnection() { return threadConnection.get(); }
   
   
   public static <T> List<T> query(Class<T> resultType, String sql, Object...params) {
     return query(threadConnection.get(), resultType, sql, params);
   }
   
   
   public static <T> List<T> query(Connection conn, Class<T> resultType, String sql, Object...params) {
     Profile profile = new Profile(resultType, sql);
     
     List<T> result = new ArrayList<T>();
     
     PreparedStatement ps = null;
     try {
       ps = prepareSql(profile, conn, sql, params);
       
       profile.startQuery();
       ResultSetMetaData metaData = ps.getMetaData();
       logMetaData(metaData);
       
       ResultSet rs = ps.executeQuery();
       profile.stopQuery();
       
       profile.startMap();
       Mapper<T> mapper = getMapper(resultType, sql, metaData);
       profile.stopMapperCreation();
       
       while (rs.next()) {
         profile.startMapRow();
         result.add( mapper.map(rs) );
         profile.stopMapRow();
       }
       profile.stopMap();
 
       profile.end();
       profile.log();
       
       return result;
     }
     catch (SQLException sqlEx) {
       throw new JapperException(sqlEx);
     }
     finally {
       try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
     }
   }
   
  
  public static int execute(String sql, Object...params) {
    return execute(threadConnection.get(), sql, params);
  }
  
  public static int execute(Connection conn, String sql, Object...params) {
    Profile profile = new Profile(int.class, sql);
    
    PreparedStatement ps = null;
    try {
      ps = prepareSql(profile, conn, sql, params);
      return ps.executeUpdate();
    }
    catch (SQLException sqlEx) {
      throw new JapperException(sqlEx);
    }
    finally {
      try { if (ps != null) ps.close(); } catch (SQLException ignored) {}
    }
  }
  
  
  
   private static PreparedStatement prepareSql(Profile profile, Connection conn, String sql, Object...params) throws SQLException {
     profile.startPrep();
     ParameterParser parser = new ParameterParser(sql).parse();
     
     profile.setSql(parser.getSql());
     PreparedStatement ps = conn.prepareStatement(parser.getSql());
     
     /*
      * For some testing over a remote connection, we want to make sure the query results
      * get loaded in a single round-trip
      * 200 is a good size for these tests
      * 
      * TODO think a bit more about whether this is a good default, or whether this
      * needs to be a parameter
      */
     ps.setFetchSize(500); 
     
     if (params != null && params.length > 0) {
       if (params.length % 2 != 0) throw new IllegalArgumentException("Mismatched param/value pairs!");
       
       for (int i = 0; i < params.length; i+=2) {
         String name = (String) params[i];
         Object value = params[i+1];
         
         profile.setParam( name, (value == null ? "(null)" : value.toString()) );
         
         List<Integer> indexes = parser.getIndexes(name);
         if (indexes != null) {
           setParameter(ps, value, indexes);
         }
       }
     }
     
     profile.stopPrep();
     return ps;
   }
   
   private static void setParameter(PreparedStatement ps, Object value, List<Integer> indexes) throws SQLException {
     for (int index : indexes) {
       setParameter(ps, value, index);
     }
   }
 
   private static void setParameter(PreparedStatement ps, Object value, int index) throws SQLException {
     if (value == null) {
       ps.setNull(index, Types.CHAR);
     }
     else if (value instanceof String) {
       ps.setString(index, (String) value);
     }
     else if (value instanceof BigDecimal) {
       ps.setBigDecimal(index, (BigDecimal) value);
     }
     else if (value instanceof Integer) {
       ps.setInt(index, (Integer) value);
     }
     else if (value instanceof Integer) {
       ps.setInt(index, (Integer) value);
     }
     else if (value instanceof Long) {
       ps.setLong(index, (Long) value);
     }
     else if (value instanceof Double) {
       ps.setDouble(index, (Double) value);
     }
     else if (value instanceof Float) {
       ps.setFloat(index, (Float) value);
     }
     else if (value instanceof Date) {
       ps.setDate(index, new java.sql.Date(((Date) value).getTime()));
     }
     else if (value instanceof Timestamp) {
       ps.setTimestamp(index, (Timestamp) value);
     }
   }
 
   /**
    * Get a Mapper implementation that can be used to map the results of the given SQL query
    * with the given meta data to the given type
    * 
    * @param resultType the type to map to
    * @param sql the query whose results we are going to map
    * @param metaData the meta data of the results of the query
    * @return a Mapper implementation
    * @throws SQLException 
    */
   public static <T> Mapper<T> getMapper(Class<T> resultType, String sql, ResultSetMetaData metaData) throws SQLException {
     // TODO Once we have a better feel for what the performance overhead of the code generation
     // is, and whether it provides a benefit for large result sets and/or multiple invocations of 
     // the same query, we can add some decision code in here to decide when to use the default,
     // reflective Mapper implementation, and when to generate a specific one dynamically.
 
     // For now we leave it up to the caller. Code generation is on by default, and can be disabled
     // via a specific comment inside the SQL query
     
     if (canUseDynamicMapper(sql)) {
       // Use a dynamically generated, optimized mapper object
       return DynamicMapper.get(resultType, sql, metaData);
     }
 
     // this is the code to create the default, reflective Mapper
     return new DefaultMapper<T>(resultType, metaData);
   }
   
 
   /**
    * Check to see if the caller put in the Japper hint to disable code gen
    * in the query
    * 
    * @param sql the sql check
    * @return true if a dynamically generated mapper can be used, false otherwise
    */
   private static boolean canUseDynamicMapper(String sql) {
     return !sql.contains("/*-codeGen*/");
   }
   
   
   
   private static void logMetaData(ResultSetMetaData metaData) throws SQLException {
     if (!log.isDebugEnabled()) return;
     
     log.debug("ResultSet Meta Data:");
     for (int i = 1; i <= metaData.getColumnCount(); i++) {
       log.debug("Column "+i+": "+metaData.getTableName(i)+"."+metaData.getColumnName(i)+" as "+metaData.getColumnLabel(i)+", type "+metaData.getColumnTypeName(i)+" ("+metaData.getColumnType(i)+")");
     }
   }
 
   
   private static class Profile {
     private Class<?> type;
     
     private long globalStart;
     private long globalEnd;
     
     private long prepStart;
     private long prepEnd;
     
     private long queryStart;
     private long queryEnd;
     
     private long mapStart;
     private long mapEnd;
     
     private long mapperCreationStart;
     private long mapperCreationEnd;
     
     
     private int rowCount;
     private long rowStart;
     private long mapFirstRowStart;
     private long mapFirstRowEnd;
     private long mappingTotal;
     
     private String originalSql;
     private String sql;
     
     private List<String> names = new ArrayList<String>();
     private List<String> values = new ArrayList<String>();
     
     public Profile(Class<?> type, String originalSql) {
       this.type = type;
       this.originalSql = originalSql;
       globalStart = System.nanoTime();
     }
     
     public void setSql(String sql) { this.sql = sql; }
     
     public void setParam(String name, String value) {
       names.add(name);
       values.add(value);
     }
     
     public void startPrep() { prepStart = System.nanoTime(); }
     
     public void stopPrep() { prepEnd = System.nanoTime(); }
     
     public void startQuery() { queryStart = System.nanoTime(); }
     
     public void stopQuery() { queryEnd = System.nanoTime(); }
     
     public void startMap() { mapStart = mapperCreationStart = System.nanoTime(); }
     
     public void stopMapperCreation() { mapperCreationEnd = System.nanoTime(); }
     
     public void startMapRow() { rowStart = System.nanoTime();  }
     
     public void stopMapRow() {
       long rowEnd = System.nanoTime();
       rowCount++;
       
       if (rowCount == 1) {
         mapFirstRowStart = rowStart;
         mapFirstRowEnd = rowEnd;
       }
       
       mappingTotal += (rowEnd - rowStart);
     }
     
     public void stopMap() { mapEnd = System.nanoTime(); }
 
     public void end() { globalEnd = System.nanoTime(); }
     
     public void log() {
       log.info("query("+type.getName()+"):");
       log.info("  original: "+originalSql);
       log.info("  executed: "+sql);
       if (log.isDebugEnabled() && !names.isEmpty()) {
         for (int i = 0; i < names.size(); i++) {
           log.debug("  "+names.get(i)+" = "+values.get(i));
         }
       }
       log.info("  preparation: "+nicify(prepStart, prepEnd));
       log.info("        query: "+nicify(queryStart, queryEnd));
       log.info("          map: "+nicify(mapStart, mapEnd));
       log.info("                     row count: "+rowCount);
       log.info("               mapper creation: "+nicify(mapperCreationStart, mapperCreationEnd));
       log.info("                     first row: "+nicify(mapFirstRowStart, mapFirstRowEnd));
       log.info("                      avg. row: "+nicify((long) avgPerRow()));
       log.info("Total: "+nicify(globalStart, globalEnd));
     }
 
     private static final long MILLI_THRESHOLD = 10000000L;
     private static final long MICRO_THRESHOLD = 10000L;
     
     private String nicify(long start, long end) { return nicify(end - start); }
     
     private String nicify(long duration) {
       if (duration > MILLI_THRESHOLD) {
         return Long.toString(duration / 1000000L)+"ms";
       }
       
       if (duration > MICRO_THRESHOLD) {
         return Long.toString(duration / 1000L)+"us";
       }
       
       return Long.toString(duration) + "ns";
     }
     
     private double avgPerRow() {
       return (double) mappingTotal / (double) rowCount;
     }
   }
 }
