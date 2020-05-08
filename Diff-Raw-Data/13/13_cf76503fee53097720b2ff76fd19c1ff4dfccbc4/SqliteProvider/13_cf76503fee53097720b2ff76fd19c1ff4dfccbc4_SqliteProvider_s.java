 package edu.usc.chla.vpicu.explorer;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.tomcat.jdbc.pool.DataSource;
 
 public class SqliteProvider extends BaseProvider {
 
   private static final String SQLITE_LIMIT = "SQLite Limit";
 
   public SqliteProvider() {
     super();
   }
 
   public SqliteProvider(File dbfile) {
     DataSource ds = new DataSource();
     ds.setDriverClassName("org.sqlite.JDBC");
     ds.setUrl("jdbc:sqlite:" + dbfile.getAbsolutePath());
     setDataSource(ds);
   }
 
   @Override
   public String getOccurrenceQuery(String occurrenceTable,
       String occurrenceIdCol, String lookupTable, String lookupIdCol,
       String labelCol, Map<String, Object> sample) {
     String sql = MessageFormat.format("SELECT DISTINCT a.{1}, l.{4}, a.count FROM ("
         + "   SELECT {1}, COUNT(*) AS count"
         + "   FROM {0}"
         + "   GROUP BY {1}) a"
         + " INNER JOIN {2} l"
         + " ORDER BY a.count DESC",
         occurrenceTable, occurrenceIdCol, lookupTable, lookupIdCol, labelCol);
     return sql;
   }
 
   @Override
   public String getOccurrenceQuery(String occurrenceTable,
       String occurrenceIdCol, String labelCol, Map<String, Object> sample) {
     String sql = MessageFormat.format("SELECT {1}, {2}, COUNT(*) AS count"
         + " FROM {0}"
         + " GROUP BY {1}, {2}"
         + " ORDER BY count DESC",
         occurrenceTable, occurrenceIdCol, labelCol);
     return sql;
   }
 
   @Override
   public String getSampleQuery(String occurrenceTable, String occurrenceIdCol,
       String occurrenceValueCol, Map<String, Object> sample) {
     String sql = MessageFormat.format("SELECT {2}, COUNT(*) AS count FROM ("
         + "   SELECT {2} FROM {0} WHERE {1} = ? LIMIT {3,number,#}) a"
         + " GROUP BY {2}"
         + " ORDER BY {2}",
         occurrenceTable, occurrenceIdCol, occurrenceValueCol,
         sample.get(SQLITE_LIMIT));
     return sql;
   }
 
   @Override
   public Map<String, Object> createDefaultSampleParams() {
     Map<String, Object> params = new HashMap<String, Object>();
     params.put(SQLITE_LIMIT, 1000);
     return params;
   }
 
   @Override
   public Map<String, Object> createDefaultOccurrenceParams() {
     return Collections.emptyMap();
   }
 
 }
