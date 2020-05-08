 package database;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import flags.FlagDefinition;
 import flags.FlagErrorException;
 import flags.Flags;
 
 public class DB {
 
   private String datenbank_name;
   private String datenbank_kurzname;
   private String user;
   private String db2_command_file;
   public String schemaName;
   private String dbCommandFlags;
   public String messagePath;
   public boolean MQTmode = false;
 
   private Connection connection;
   private Statement statement;
 
   // Tabellen
   public String wahlbezirk() {
     return tabellenName("Wahlbezirk");
   }
 
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
   public final static String kKandidatBeruf = "Beruf";
   public final static String kKandidatAnschrift = "Anschrift";
   public final static String kKandidatListenplatz = "Listenplatz";
   public final static String kKandidatDMParteiID = "DMParteiID";
   public final static String kKandidatDMWahlkreisID = "DMWahlkreisID";
 
   public String stimme() {
     return tabellenName("Stimme");
   }
   public final static String kStimmeJahr = "Jahr";
 
   public String erstStimmenNachWahlkreis() {
     return tabellenName("ErstStimmenNachWahlkreis");
   }
 
   public final static String kAnzahl = "Anzahl";
   public final static String kWahlergebnis1Jahr = "Jahr";
 
   public String zweitStimmenNachWahlkreis() {
     return tabellenName("ZweitStimmenNachWahlkreis");
   }
 
   public final static String kWahlergebnis2Anzahl = "Anzahl";
   public final static String kWahlergebnis2Jahr = "Jahr";
 
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
   
   public String ueberhangsMandate() {
 		return tabellenName("Ueberhangsmandate");
 	}
   
   public String wahlberechtigter() {
 		return tabellenName("Wahlberechtigter");
 	}
   
   public final static String kWahlberechtigterGewaehlt = "gewaehlt";
   
   public String sessionIDs() {
   	return tabellenName("SessionIDs");
   }
   
   public final static String kForeignKeyParteiID = "ParteiID";
   public final static String kForeignKeyBundeslandID = "BundeslandID";
   public final static String kForeignKeyWahlbezirkID = "WahlbezirkID";
   public final static String kForeignKeyWahlkreisID = "WahlkreisID";
   public final static String kForeignKeyKandidatID = "KandidatID";
   
   public final static String kAnzahlStimmen = "AnzahlStimmen";
   public final static String kAnzahlSitze = "AnzahlSitze";
   public final static String kMaxStimmen = "MaxStimmen";
   
   public String wahlkreisDaten() {
   	return tabellenName("WahlkreisDaten");
   }
   public final static String kAnzahlWahlberechtigte = "AnzahlWahlberechtigte";
   public final static String kUngueltigeErststimmen = "AnzahlUngueltigeErststimmen";
   public final static String kUngueltigeZweitstimmen = "AnzahlUngueltigeZweitstimmen";
   
   public final static String kAnzahlUeberhangsmandate = "AnzahlUeberhangsmandate";
   
   public final static String kJahr = "Jahr";
   public final static String kZeile = "Zeile";
   public final static String kZahl = "Zahl";
   public final static String kNummer = "Nummer";
   public final static String kMaxNummer = "kMaxNummer";
 
   public String zweitStimmenNachPartei() {
     return tabellenName("ZweitStimmenNachPartei");
   }
 
   public String maxErststimmenNachWahlkreis() {
   	return tabellenName("MaxErststimmenNachWahlkreis");
   }
   
   public String zugriffsreihenfolgeSitzeNachPartei() {
   	return tabellenName("ZugriffsreihenfolgeSitzeNachPartei");
   }
   
   public String divisoren() {
   	return tabellenName("Divisoren");
   }
   
   public String zugriffsreihenfolgeSitzeNachLandeslisten() {
   	return tabellenName("ZugriffsreihenfolgeSitzeNachLandeslisten");
   }
   
   public String direktMandateProParteiUndBundesland() {
   	return tabellenName("DirektMandateProParteiUndBundesland");
   }
   
   public String maxZweitStimmenNachWahlkreis() {
   	return tabellenName("MaxZweitStimmenNachWahlkreis");
   }
   
   public String wahlkreisSieger() {
   	return tabellenName("WahlkreisSieger");
   }
   
   public String gewinnerErststimmen() {
   	return tabellenName("GewinnerErststimmen");
   }
   
   public String gewinnerZweitstimmen() {
   	return tabellenName("GewinnerZweitstimmen");
   }
   
   public String zufallsZahlen() {
   	return tabellenName("ZufallsZahlen");
   }
   
   public String direktMandateNummer() {
   	return tabellenName("DirektmandateNummer");
   }
   
   public String direktMandateMaxNummer() {
   	return tabellenName("DirektmandateMaxNummer");
   }
   
   public boolean isBaseTable(String kurzname) {
   	for (String table : TableDef.baseTables()) {
   		if (table.equalsIgnoreCase(kurzname)) {
   			return true;
   		}
   	}
   	return false;
   }
   
   public String tabellenName(String kurzname) {
   	if (MQTmode && !isBaseTable(kurzname)) {
   		return schemaName + ".MQT" + kurzname;
   	} else {
   		return schemaName + "." + kurzname;
   	}
   }
   
   public static DB getDatabaseByFlags() throws FlagErrorException {
     final String dbName = Flags.getFlagValue(FlagDefinition.kFlagDbName);
     final String dbUser = Flags.getFlagValue(FlagDefinition.kFlagDbUser);
     final String dbPwd = Flags.getFlagValue(FlagDefinition.kFlagDbPwd);
     final String dbSchemaName = Flags.getFlagValue(FlagDefinition.kFlagDbSchemaName);
     final String dbCommandFile = Flags.getFlagValue(FlagDefinition.kFlagDbCommandFile);
     final String dbCommandFlags = Flags.getFlagValue(FlagDefinition.kFlagDbCommandFlags);
     final String logFile = Flags.getFlagValue(FlagDefinition.kFlagLogFile);
 
     return new DB(dbName, dbUser, dbPwd, dbSchemaName, dbCommandFile, dbCommandFlags, logFile);
   }
 
   public void deleteSQLLog() {
   	try {
   		String sqlLogFile = Flags.getFlagValue(FlagDefinition.kFlagLogSQLFile);
 			if (!sqlLogFile.isEmpty()) {
 				new FileWriter(sqlLogFile, false);
 			}
 		} catch (FlagErrorException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
   }
   
   public DB(String name, String user, String pwd, String schemaName, String commandFile, String dbCommandFlags,
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
   
   public String prettyPrintSQL(String sql_statement) {
   	String endSign = "##END##";
   	String result = sql_statement + endSign;
   	
   	// New line before and after union
   	result = result.replaceAll(" UNION ", "\n\nUNION\n\n");
   	
   	// Line break for with tables
   	String kNoBracket = "[^\\(\\)]";
   	String kNoOrOneBracket = "(?:" + kNoBracket + "|\\(" + kNoBracket + "*\\))";
   	String kAtMostTwoBrackets = "(?:" + kNoOrOneBracket + "|\\(" + kNoOrOneBracket + "*\\))";
   	String kAtMostThreeBrackets = "(?:" + kAtMostTwoBrackets + "|\\(" + kAtMostTwoBrackets + "*\\))";
   	result = result.replaceAll("([A-Za-z]+ AS \\(" + kAtMostThreeBrackets + "*\\))", "\n\n$1");
   	
   	String kSelectEtc = "SELECT|FROM|WHERE|GROUP BY|HAVING|UNION|ORDER BY";
   	// Indent Sub Select clauses
   	result = result.replaceAll("\\(SELECT", "( SELECT");
   	for (int i = 0; i < 10; i++) {
 	  	result = result.replaceAll("( AS \\(" + kAtMostThreeBrackets + "*)" + "( |\n)(" + kSelectEtc + ")", "$1\n\t$3");
   	}
   	
   	// Line break for last SELECT FROM WHERE Clause
   	for (int i = 0; i < 10; i++) {
 	  	result = result.replaceAll(" (" + kSelectEtc + ")(" + kAtMostThreeBrackets + "*" + endSign + ")", "\n$1$2");
   	}
   	
 
 
   	
   	result = result.replaceAll(" ([A-Za-z]*) (BIGINT|VARCHAR|INTEGER)", "\n$1 $2");
   	result = result.replaceAll("\\(([A-Za-z]*) (BIGINT|VARCHAR|INTEGER)", "(\n$1 $2");
   	result = result.replaceAll("CREATE", "\n\nCREATE");
   	result = result.replaceAll(" CONSTRAINT", "\nCONSTRAINT");
   	result = result.replaceAll(" ORGANIZE", "\nORGANIZE");
   	
   	result = result.replaceAll(schemaName + "\\.", "");
   	result = result.replaceAll("  ", " ");
   	result = result.replaceAll(" ,", ",");
   	//result = result.replaceAll("(\n[^\n]{60,}) ", "$1\n");
   	
   	// New Line for AND
   	result = result.replaceAll("\n(\t?)(\t?)WHERE([^\n]*?)AND", "\n$1$2WHERE$3\n$1$2\tAND");
   	for (int i = 1; i < 10; i++) 
   		result = result.replaceAll("\n(\t?)(\t?)AND([^\n]*?)AND", "\n$1$2AND$3\n$1$2AND");
   	
   	// Line breaks of full lines (More than 63 characters)  	
   	for (int i = 5; i < 30; i += 5)
   		result = result.replaceAll("\n(\t?)(\t?)((?:" + kSelectEtc + "|AND)[^\n\t]{" + (55 - i) + ",55}) ([^\n]{" + i + ",})",
   				"\n$1$2$3\n\t$1$2$4");
   	for (int i = 5; i < 30; i += 5)
   		result = result.replaceAll("\n(\t?)(\t?)([^\n\t]{" + (60 - i) + ",60}) ([^\n]{" + i + ",})",
   				"\n$1$2$3\n$1$2$4");
   	
   	result = result.replaceAll("\n\n\n", "\n\n");
   	// Remove END SIGN
   	result = result.replaceAll(endSign, "");
   	return result;
   }
 
   public ResultSet executeSQL(String sql_statement) throws SQLException {
   	logSQL(sql_statement);
   	
     System.out.println(prettyPrintSQL(sql_statement));
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
   
   public void logSQL(String sql_statement) {
   	try {
   		String sqlLogFile = Flags.getFlagValue(FlagDefinition.kFlagLogSQLFile);
 			if (!sqlLogFile.isEmpty() &&
 					!sql_statement.contains("TRUNCATE") &&
 					!sql_statement.contains("SYSCAT")) {
 				FileWriter fileWriterSQLlog = new FileWriter(sqlLogFile, true);
 				fileWriterSQLlog.write(prettyPrintSQL(sql_statement) + "\n\n");
 				fileWriterSQLlog.close();
 			}
 		} catch (FlagErrorException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
   }
 
   public void executeUpdate(String sql_statement) throws SQLException {
   	logSQL(sql_statement);
 
     System.out.println(prettyPrintSQL(sql_statement));
     if (!Flags.isTrue(FlagDefinition.kFlagSimulateSQLUpdate)) {
 	    Statement statement = connection.createStatement();
 	    statement.executeUpdate(sql_statement);
 	    statement.close();
     }
   }
 
   public void executeDB2(String db2_statement) {
   	if (Flags.isTrue(FlagDefinition.kFlagSimulateSQLUpdate)) {
   		System.out.println("\n\ncmd:\n" + prettyPrintSQL(db2_statement));
   		return;
   	}
     File file = new File(db2_command_file);
     if (file.exists()) {
       file.delete();
     }
     FileWriter file_writer;
     try {
       file_writer = new FileWriter(file);
       file_writer.write("CONNECT TO " + datenbank_kurzname + ";\n" + db2_statement + ";\nCONNECT RESET;");
       file_writer.flush();
       System.out.println("cmd: " + prettyPrintSQL(db2_statement));
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
         + getTableShortName(tableName).toUpperCase() + "' AND TABSCHEMA='" + schemaName.toUpperCase()
         + "' AND STATUS<>'N'");
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
   	createOrReplaceTable(tableName, columns, Flags.isTrue(FlagDefinition.kFlagMakeTemporaryTablesPermanent));
   }
  
   public void createOrReplaceTable(String tableName, String columns, boolean permanentTable) throws SQLException { 	
     if (tableExists(tableName)) {
     	truncate(tableName);
     	return;
     }
       
     else if (permanentTable) {
       executeUpdate("CREATE TABLE " + tableName + " (" + columns + ")");
     } else {
       executeUpdate("CREATE GLOBAL TEMPORARY TABLE " + tableName + " (" + columns + ") ON COMMIT PRESERVE ROWS");
     }
   }
 
   
   public void createFilledTemporaryTable(String tableName, String columns, String sql_statement) throws SQLException {	
   	if (MQTmode) {
   		createOrUpdateMQT(tableName, columns, sql_statement);
   	} else {
 	  	createOrReplaceTemporaryTable(tableName, columns);
 	  	executeUpdate("INSERT INTO " + tableName + "(" + columnsWithoutType(columns) + ") " + sql_statement);
   	}
   }
 
 	private String columnsWithoutType(String columns) {
 		return columns.replaceAll("([^ ]+) [^ ,]+ *(,|$)", "$1$2");
 	}
 
   private void createOrUpdateMQT(String tableName, String columns, String sql_statement) throws SQLException {
 		if (!tableExists(tableName)) {
 			createMQT(tableName, columns, sql_statement);
 		}
 		setIntegrityChecked(tableName);
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
   
   public void createMQT(String tableName, String columnList, String query) throws SQLException {
   	executeUpdate("CREATE TABLE " + tableName + " (" + columnsWithoutType(columnList) + ") AS (" + query + ") "
   		+ "DATA INITIALLY DEFERRED "
   		+ "REFRESH DEFERRED "
   		+ "DISABLE QUERY OPTIMIZATION "
   		+ "MAINTAINED BY USER");
   }
   
 	public String stmtZweitstimmenNachWahlkreis() {
 		return ""
 		+ "SELECT s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
 		+ "FROM " + stimme() + " s "
 		+ "WHERE " + DB.kForeignKeyParteiID + " IS NOT NULL "
 		+ "GROUP BY s." + DB.kForeignKeyParteiID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr;
 	}
   
 	public String stmtErststimmenNachWahlkreis() {
 		return ""
 		+ "SELECT s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", " + DB.kJahr + ", COUNT(*) "
 		+ "FROM " + stimme() + " s "
 		+ "WHERE " + DB.kForeignKeyKandidatID + " IS NOT NULL "
 		+ "GROUP BY s." + DB.kForeignKeyKandidatID + ", s." + DB.kForeignKeyWahlkreisID + ", s." + DB.kJahr;
 	}
   
 	public String updateZweitstimmenNachWahlkreisTable() throws SQLException {
 		createFilledTemporaryTable(zweitStimmenNachWahlkreis(), DB.kForeignKeyParteiID + " BIGINT, "
 				+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " INTEGER, " + DB.kAnzahl + " INTEGER" , 
 				stmtZweitstimmenNachWahlkreis());
 		return zweitStimmenNachWahlkreis();
 	}
 	
 	public String updateErststimmenNachWahlkreisTable() throws SQLException {
 		createFilledTemporaryTable(erstStimmenNachWahlkreis(), DB.kForeignKeyKandidatID + " BIGINT, "
 				+ DB.kForeignKeyWahlkreisID + " BIGINT, " + DB.kJahr + " INTEGER, " + DB.kAnzahl + " INTEGER" , 
 				stmtErststimmenNachWahlkreis());
 		return erstStimmenNachWahlkreis();
 	}
 }
