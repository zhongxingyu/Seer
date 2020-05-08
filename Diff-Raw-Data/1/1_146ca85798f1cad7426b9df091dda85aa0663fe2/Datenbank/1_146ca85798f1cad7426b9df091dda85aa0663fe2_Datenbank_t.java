 package datenbank;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.*;
 
 import flags.FlagDefinition;
 import flags.Flags;
 
 public class Datenbank {
 
   private String datenbank_name;
   private String datenbank_kurzname;
   private String user;
   private String db2_command_file;
   public String schemaName;
   private String dbCommandFlags;
   public String messagePath;
 
   private Connection connection;
   private Statement statement;
 
   // Tabellen
   public String wahlkreis() {
     return tabellenName("Wahlkreis");
   }
   
   public final static String kWahlkreisName = "Name";
   
   public String bundesland() {return tabellenName("Bundesland");}
   public final static String kBundeslandName = "Name";
 
   public String partei() {
     return tabellenName("Partei");
   }
   public final static String kID = "ID";
   public final static String kParteiKuerzel = "Kuerzel";
   public final static String kParteiName = "Name";
 
   public String kandidat() {
     return tabellenName("Kandidat");
   }
 
   public final static String kKandidatNachname = "Nachname";
   public final static String kKandidatVorname = "Vorname";
   public final static String kKandidatListenplatz = "Listenplatz";
   public final static String kKandidatDMParteiID = "DMParteiID";
   public final static String kKandidatDMWahlkreisID = "DMWahlkreisID";
 
   public String stimme() {
     return tabellenName("Stimme");
   }
   public final static String kStimmeJahr = "Jahr";
 
   public String wahlergebnis1() {
     return tabellenName("Wahlergebnis1");
   }
 
   public final static String kWahlergebnis1Anzahl = "Anzahl";
   public final static String kWahlergebnis1Jahr = "Jahr";
 
   public String wahlergebnis2() {
     return tabellenName("Wahlergebnis2");
   }
 
   public final static String kWahlergebnis2Anzahl = "Anzahl";
   public final static String kWahlergebnis2Jahr = "Jahr";
 
   // Temporary tables
   public String zweitStimmenNachBundesland() {
     return tabellenName("ZweitStimmenNachBundesland");
   }
 
   public String direktmandate() {
     return tabellenName("Direktmandate");
   }
 
   public String fuenfProzentParteien() {
     return tabellenName("FuenfProzentParteien");
   }
 
   public String dreiDirektMandatParteien() {
     return tabellenName("DreiDirektMandatParteien");
   }
 
   public String parteienImBundestag() {
     return tabellenName("ParteienImBundestag");
   }
   
   public String sitzeNachPartei() {
     return tabellenName("SitzeNachPartei");
   }
 
   public String sitzeNachLandeslisten() {
     return tabellenName("SitzeNachLandeslisten");
   }
 
   public final static String kForeignKeyParteiID = "ParteiID";
   public final static String kForeignKeyBundeslandID = "BundeslandID";
   public final static String kForeignKeyWahlbezirkID = "WahlbezirkID";
   public final static String kForeignKeyWahlkreisID = "WahlkreisID";
   public final static String kForeignKeyKandidatID = "KandidatID";
   
   public final static String kAnzahlStimmen = "AnzahlStimmen";
   public final static String kAnzahlSitze = "AnzahlSitze";
 
   public String zweitStimmenNachPartei() {
     return tabellenName("ZweitStimmenNachPartei");
   }
 
   public String tabellenName(String kurzname) {
     return schemaName + "." + kurzname;
   }
 
   public Datenbank(String name, String user, String pwd, String schemaName, String commandFile, String dbCommandFlags,
       String messagePath) {
     this.datenbank_kurzname = name;
     this.datenbank_name = "jdbc:db2:" + name;
     this.user = user;
     this.db2_command_file = commandFile;
     this.dbCommandFlags = dbCommandFlags;
     this.schemaName = schemaName;
     this.messagePath = messagePath;
     try {
       Class.forName("com.ibm.db2.jcc.DB2Driver");
       connection = DriverManager.getConnection(datenbank_name, user, pwd);
     } catch (SQLException e) {
       e.printStackTrace();
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
     }
   }
 
   public void disconnect() throws SQLException {
     connection.commit();
     connection.close();
   }
 
   public void truncate(String table) throws SQLException {
    connection.commit();
     executeUpdate("TRUNCATE TABLE " + table + " DROP STORAGE IMMEDIATE");
   }
 
   public ResultSet executeSQL(String sql_statement) throws SQLException {
     System.out.println(sql_statement);
     if (statement != null)
       statement.close();
     statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
     ResultSet result_set;
     try {
       result_set = statement.executeQuery(sql_statement + "\n");
     } catch (SQLException e) {
       if (e.getErrorCode() == -551) {
         System.out.println("User " + user + " does not have the necessary "
             + "priveleges to perform this action. You can change the priveleges" + " using this command: "
             + "GRANT  CREATETAB,BINDADD,CONNECT,CREATE_NOT_FENCED_ROUTINE,"
             + "IMPLICIT_SCHEMA,LOAD,CREATE_EXTERNAL_ROUTINE,QUIESCE_CONNECT " + "ON DATABASE  TO USER " + user + ";");
         System.exit(1);
       }
       throw new SQLException(e);
     }
     return result_set;
   }
 
   public void executeUpdate(String sql_statement) throws SQLException {
     System.out.println(sql_statement);
     Statement statement = connection.createStatement();
     statement.executeUpdate(sql_statement);
     statement.close();
   }
 
   public void executeDB2(String db2_statement) {
     File file = new File(db2_command_file);
     if (file.exists()) {
       file.delete();
     }
     FileWriter file_writer;
     try {
       file_writer = new FileWriter(file);
       file_writer.write("CONNECT TO " + datenbank_kurzname + ";\n" + db2_statement + ";\nCONNECT RESET;");
       file_writer.flush();
       System.out.println("cmd: " + db2_statement);
       String cmd = "db2cmd.exe " + dbCommandFlags + " " + db2_command_file;
       Process p = Runtime.getRuntime().exec(cmd);
       p.waitFor();
     } catch (IOException e) {
       e.printStackTrace();
       System.exit(1);
     } catch (InterruptedException e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   public String getTableShortName(String tableFullName) {
     return tableFullName.substring(tableFullName.indexOf(".") + 1);
   }
 
   public boolean tableIntegrityIsUnchecked(String tableName) throws SQLException {
     ResultSet rs = executeSQL("SELECT Status FROM SYSCAT.TABLES WHERE TABNAME='"
         + getTableShortName(tableName).toUpperCase() + "' AND TABSCHEMA='" + schemaName.toUpperCase() + "'");
     return rs.next();
   }
 
   public void setIntegrityChecked(String tableName) throws SQLException {
     if (tableIntegrityIsUnchecked(tableName)) {
       executeDB2("SET INTEGRITY FOR " + tableName + " IMMEDIATE CHECKED");
     }
   }
 
   public void load(String file, String columnNumbers, String[] columns, String tableName) throws SQLException {
     // The previous load might have failed. Abort load.
     final String abortLoadStmt = "LOAD FROM DUMMY OF DEL TERMINATE into " + tableName;
     executeDB2(abortLoadStmt);
     setIntegrityChecked(tableName);
 
     // Delete old content.
     try {
       truncate(tableName);
     } catch (SQLException e) {
       e.printStackTrace();
       System.exit(1);
     }
 
     String columnString = "";
     for (int i = 0; i < columns.length; i++) {
       columnString += columns[i];
       if (i != columns.length - 1)
         columnString += ", ";
     }
     final String loadStmt = "LOAD FROM \"" + file + "\" OF DEL MODIFIED BY COLDEL; " + "METHOD P " + columnNumbers
         + " SAVECOUNT 10000 " + "MESSAGES \"" + messagePath + "\" " + "INSERT INTO " + tableName + " (" + columnString
         + ")";
     executeDB2(loadStmt);
     setIntegrityChecked(tableName);
   }
 
   public boolean tableExists(String tableName) throws SQLException {
     String tableShortName = getTableShortName(tableName);
     ResultSet rs = executeSQL("SELECT * FROM SYSCAT.TABLES WHERE TABSCHEMA='" + schemaName.toUpperCase()
         + "' AND TABNAME='" + tableShortName.toUpperCase() + "'");
     return rs.next();
   }
 
   public void dropTable(String tableName) throws SQLException {
     executeUpdate("DROP TABLE " + tableName);
   }
 
   /**
    * 
    * @param columns
    *          List of columns the table should include. Format:
    *          "Column_1 type_1, [...] Column_n type_n"
    * @throws SQLException
    */
   public void createOrReplaceTemporaryTable(String tableName, String columns) throws SQLException {
     if (tableExists(tableName))
       dropTable(tableName);
     if (Flags.isTrue(FlagDefinition.kFlagMakeTemporaryTablesPermanent)) {
       executeUpdate("CREATE TABLE " + tableName + " (" + columns + ")");
     } else {
       executeUpdate("CREATE GLOBAL TEMPORARY TABLE " + tableName + " (" + columns + ") ON COMMIT PRESERVE ROWS");
     }
   }
 
   public Connection getConnection() {
     return connection;
   }
 
   public void printResultSet(ResultSet rs) throws SQLException {
     ResultSetMetaData metadata = rs.getMetaData();
     for (int i = 0; i < metadata.getColumnCount(); i++) {
       System.out.print(metadata.getColumnLabel(i + 1) + "\t");
     }
     System.out.println();
     while (rs.next()) {
       for (int i = 0; i < metadata.getColumnCount(); i++) {
         System.out.print(rs.getString(i + 1) + "\t");
       }
       System.out.println();
     }
   }
 
   public void printTable(String table) throws SQLException {
     printResultSet(executeSQL("SELECT * FROM " + table));
   }
 
 }
