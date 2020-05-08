 /*
  * Copyright (c) Qatar Computing Research Institute, 2013.
  * Licensed under the MIT license <http://www.opensource.org/licenses/MIT>.
  */
 
 package qa.qcri.qnoise.util;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Strings;
 import qa.qcri.qnoise.sql.DBConfig;
 import qa.qcri.qnoise.sql.DBTools;
 import qa.qcri.qnoise.sql.SQLDialect;
 import qa.qcri.qnoise.sql.SQLDialectBase;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.Statement;
 
 /**
  * CSV common manipulation util.
  */
 public class CSVTools {
     /**
      * Loads CSV file into database.
      * @param file CSV file path.
      * @param dbConfig DB connection config.
      * @param tableName target import table name.
      * @param delimiter CSV delimiter.
      * @return the number of bytes loaded into the target database.
      */
     public static int load(
         String file,
         DBConfig dbConfig,
         String tableName,
         char delimiter
     ) throws Exception {
         Preconditions.checkNotNull(dbConfig);
         Preconditions.checkNotNull(file);
 
         Connection conn = null;
         Statement stat = null;
         BufferedReader reader = null;
         int result = 0;
         try {
             conn = DBTools.createConnection(dbConfig, true);
             stat = conn.createStatement();
             if (DBTools.existTable(dbConfig, tableName)) {
                 stat.executeUpdate("DROP TABLE " + tableName);
             }
 
             // create table based on the header
             reader = new BufferedReader(new FileReader(file));
             String header = reader.readLine();
             if (!Strings.isNullOrEmpty(header)) {
                 String[] columns = header.split(Character.toString(delimiter));
                 StringBuilder schemaSql = new StringBuilder("CREATE TABLE " + tableName + " (");
                 boolean isFirst = true;
                 for (String column : columns) {
                     if (!isFirst) {
                         schemaSql.append(",");
                     }
                     isFirst = false;
                     schemaSql.append(column).append(" VARCHAR(10240) ");
                 }
                 schemaSql.append(")");
                 stat.execute(schemaSql.toString());
 
                 SQLDialect dialect = dbConfig.getDialect();
                 SQLDialectBase dialectBase = SQLDialectBase.createDialectBaseInstance(dialect);
                 if (dialectBase.supportBulkLoad()) {
                     result = dialectBase.bulkLoad(dbConfig, tableName, file, delimiter);
                 } else {
                     throw new UnsupportedOperationException("Non-bulk loading is not yet implemented.");
                 }
             }
         } finally {
             try {
                 if (stat != null) {
                     stat.close();
                 }
 
                 if (conn != null) {
                     conn.close();
                 }
 
                 if (reader != null) {
                     reader.close();
                 }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
         }
         return result;
     }
 }
