 package com.github.ryhmrt.mssqldiff.csv;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * {@link SchemaCsvReader}の実装クラス
  */
 public class SchemaCsvReaderMssqlImpl implements SchemaCsvReader {
 
     /** スキーマ情報取得のSQL */
     private static final String SQL =
         "SELECT" + "\n" +
         "  o.xtype AS objectType," + "\n" + "\n" +
         "  o.name AS tableName," + "\n" +
         "  c.name AS columnName," + "\n" +
        "  t.type AS columnType," + "\n" +
         "  CASE" + "\n" +
         "    WHEN c.length = -1 THEN c.length" + "\n" +
         "    WHEN t.name = 'nvarchar' THEN c.length / 2" + "\n" +
         "    WHEN t.name = 'varchar' THEN c.length / 2" + "\n" +
         "    WHEN t.name = 'nchar' THEN c.length / 2" + "\n" +
         "    WHEN t.name = 'char' THEN c.length / 2" + "\n" +
         "    WHEN t.name = 'ntext' THEN -1" + "\n" +
         "    ELSE c.length" + "\n" +
         "  END AS [length]," + "\n" +
         "  CASE WHEN EXISTS (SELECT NULL FROM sysconstraints AS r INNER JOIN sysobjects AS ro ON ro.id = r.constid INNER JOIN sysindexes AS i ON i.name = ro.name INNER JOIN sysindexkeys AS ik ON ik.id = i.id AND ik.indid = i.indid WHERE ik.id = c.id AND ik.colid = c.colid AND ro.xtype = 'PK') THEN 1 ELSE 0 END AS [pk]," + "\n" +
         "  (c.status & 128) / 128 AS [identity]," + "\n" +
         "  (c.status & 8) / 8 AS [nullable]," + "\n" +
         "  (SELECT TOP 1 text FROM syscomments AS m INNER JOIN sysconstraints AS r ON m.id = r.constid INNER JOIN sysobjects AS ro ON ro.id = r.constid WHERE r.id = c.id AND r.colid = c.colid AND ro.xtype = 'D') AS defaultValue," + "\n" +
         "  (SELECT TOP 1 ex.value FROM sys.extended_properties AS ex WHERE ex.major_id = o.id AND ex.minor_id = 0 AND ex.name = 'MS_Description' ) AS tableDescription," + "\n" +
         "  (SELECT TOP 1 ex.value FROM sys.extended_properties AS ex WHERE ex.major_id = c.id AND ex.minor_id = c.colid AND ex.name = 'MS_Description' ) AS columnDescription," + "\n" +
         "  p.userName," + "\n" +
         "  p.canSelect," + "\n" +
         "  p.canInsert," + "\n" +
         "  p.canUpdate," + "\n" +
         "  p.canDelete" + "\n" +
         "FROM sysobjects AS o" + "\n" +
         "INNER JOIN syscolumns AS c ON o.id = c.id" + "\n" +
         "INNER JOIN systypes AS t ON t.xusertype = c.xusertype" + "\n" +
         "LEFT OUTER JOIN (" + "\n" +
         "  SELECT" + "\n" +
         "    id," + "\n" +
         "    user_name(user_id) AS userName," + "\n" +
         "    CASE WHEN EXISTS(SELECT NULL FROM sys.database_permissions WHERE major_id = id  AND minor_id = 0 AND class = 1 AND state = 'G' AND type = 'SL') THEN 1 ELSE 0 END AS canSelect," + "\n" +
         "    CASE WHEN EXISTS(SELECT NULL FROM sys.database_permissions WHERE major_id = id  AND minor_id = 0 AND class = 1 AND state = 'G' AND type = 'IN') THEN 1 ELSE 0 END AS canInsert," + "\n" +
         "    CASE WHEN EXISTS(SELECT NULL FROM sys.database_permissions WHERE major_id = id  AND minor_id = 0 AND class = 1 AND state = 'G' AND type = 'UP') THEN 1 ELSE 0 END AS canUpdate," + "\n" +
         "    CASE WHEN EXISTS(SELECT NULL FROM sys.database_permissions WHERE major_id = id  AND minor_id = 0 AND class = 1 AND state = 'G' AND type = 'DL') THEN 1 ELSE 0 END AS canDelete" + "\n" +
         "  FROM (SELECT distinct major_id AS id, grantee_principal_id AS user_id FROM sys.database_permissions WHERE class = 1 AND minor_id = 0 AND state = 'G' AND type IN ('SL', 'IN', 'UP', 'DL')) AS mst" + "\n" +
         ") AS p ON p.id = o.id" + "\n" +
         "WHERE o.xtype IN ('U', 'V')" + "\n" +
         "ORDER BY o.xtype, o.name, c.colorder, p.userName";
 
     private static final String DRIVER = "net.sourceforge.jtds.jdbc.Driver";
     
     /** 接続ホスト名 */
     private String host;
     /** 接続ユーザ名 */
     private String user;
     /** 接続パスワード */
     private String pass;
     /** 接続DB名 */
     private String dbname;
 
     @Override
     public List<SchemaCsv> read() {
         List<SchemaCsv> result = new ArrayList<SchemaCsv>();
         Connection con = getConnection();
         try {
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SQL);
             try {
                 while (rs.next()) {
                     result.add(getSchemaCsv(rs));
                 }
             } finally {
                 rs.close();
             }
         } catch (SQLException e) {
             throw new RuntimeException(e);
         } finally {
             try {
                 con.close();
             } catch (SQLException e) {
                 // ignore
             }
         }
         return result;
     }
 
     /**
      * {@link ResultSet}のカレントレコードからデータを取得し、{@link SchemaCsv}を生成する
      * @param rs データ読込元の{@link ResultSet}
      * @return {@link ResultSet}から生成した{@link SchemaCsv}
      * @throws SQLException {@link ResultSet}からデータを取得する際に発生したエラーをそのまま返却
      */
     private SchemaCsv getSchemaCsv(ResultSet rs) throws SQLException {
         SchemaCsv csv = new SchemaCsv();
         csv.setObjectType(rs.getString("objectType"));
         csv.setTableName(rs.getString("tableName"));
         csv.setColumnName(rs.getString("columnName"));
         csv.setColumnType(rs.getString("columnType"));
         csv.setLength(rs.getInt("length"));
         csv.setPk(rs.getBoolean("pk"));
         csv.setIdentity(rs.getBoolean("identity"));
         csv.setNullable(rs.getBoolean("nullable"));
         csv.setDefaultValue(rs.getString("defaultValue"));
         csv.setTableDescription(rs.getString("tableDescription"));
         csv.setColumnDescription(rs.getString("columnDescription"));
         csv.setUserName(rs.getString("userName"));
         csv.setCanSelect(rs.getBoolean("canSelect"));
         csv.setCanInsert(rs.getBoolean("canInsert"));
         csv.setCanUpdate(rs.getBoolean("canUpdate"));
         csv.setCanDelete(rs.getBoolean("canDelete"));
         return csv;
     }
 
     /**
      * @return SQLコネクション
      */
     private Connection getConnection() {
         try {
             Class.forName(DRIVER);
             return DriverManager.getConnection("jdbc:jtds:sqlserver://" + host + "/" + dbname, user, pass);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
     
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public String getPass() {
         return pass;
     }
 
     public void setPass(String pass) {
         this.pass = pass;
     }
 
     public String getDbname() {
         return dbname;
     }
 
     public void setDbname(String dbname) {
         this.dbname = dbname;
     }
 }
