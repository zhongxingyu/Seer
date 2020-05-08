 package edu.usc.chla.vpicu.explorer;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.tomcat.jdbc.pool.DataSource;
 
 public class H2Provider extends BaseProvider {
 
   private static final String H2_LIMIT = "H2 Limit";
 
   public H2Provider() {
     super();
   }
 
   public H2Provider(File dbfile, String user, String pass) {
     DataSource ds = new DataSource();
     ds.setDriverClassName("org.h2.Driver");
     String dbpath = "";
     if (dbfile != null) {
       if (dbfile.getPath().endsWith(".h2.db"))
         dbpath = dbfile.getPath().replaceAll(".h2.db$", "");
       else
         dbpath = dbfile.getPath();
     }
     ds.setUrl("jdbc:h2:file:" + dbpath);
     ds.setUsername(user);
     ds.setPassword(pass);
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
         sample.get(H2_LIMIT));
     return sql;
   }
 
   @Override
   public Map<String, Object> createDefaultSampleParams() {
     Map<String, Object> params = new HashMap<String, Object>();
     params.put(H2_LIMIT, 1000);
     return params;
   }
 
   @Override
   public Map<String, Object> createDefaultOccurrenceParams() {
     return Collections.emptyMap();
   }
 
 }
