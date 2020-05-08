 package queries;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import database.DB;
 
 public abstract class Query {
 
 	protected String headline;
   protected final int kCurrentElectionYear = 2009;
   protected final int kPreviousElectionYear = 2005;
 	
 	protected DB db;
 	
 	String withQuery = "";
 	
 	public Query(String headline) {
 		this.headline = headline;
 	}
 
 	public void setDatabase(DB db) {
 		this.db = db;
 	}
 	
 	public String replaceUmlaute(String input) {
 		input = input.replace("", "&uuml;");
 		input = input.replace("", "&Uuml;");
 		input = input.replace("", "&ouml;");
 		input = input.replace("", "&Ouml;");
 		input = input.replace("", "&auml;");
 		input = input.replace("", "&Auml;");
 		input = input.replace("", "&szlig;");
 		input = input.replace("", "&eacute;");
 		input = input.replace("", "&egrave;");
 		
 		return input;
 	}
 
 	public String generateHtmlOutput() {
 		try {
 			final long startTime = System.currentTimeMillis();
 			ResultSet resultSet = doQuery();
 			String body = generateBody(resultSet);
 			body = replaceUmlaute(body);
 			String html = "<html>";
 			html += "<head>" + getHtmlHeader() + "</head>";
 			html += "<body><div class=\"container\"><h1>" + headline + "</h1><hr/>";
 			html += "<div class=\"span-6\">" + getNavigation() + "</div>";
 			html += "<div class=\"span-18 last\">" + body + "</div>";
 			html += "<hr/><p>Die Berechnung hat " + (System.currentTimeMillis() - startTime) + " Millisekunden gedauert.<p>";
 			html += "</div></body></html>";
 			return html;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	private String getNavigation() {
 		String navigation = "<h3>Navigation</h3>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q1\">Q1: Sitzverteilung</a><br/>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q2\">Q2: Abgeordnete</a><br/>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q3\">Q3: Wahlkreisinfo</a><br/>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q4\">Q4: Wahlkreisergebnisse</a><br/>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q5\">Q5: berhangsmandate</a><br/>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q6\">Q6: Knappste Sieger</a><br/>";
 		navigation += "<a href=\"/WahlWebsite/ShowResult?query=Q7\">Q7: Wahlkreisinfo (Einzelstimmen)</a><br/>";
 		return navigation;
 	}
 
 	private String getHtmlHeader() {
 		String header = "";
 		header += "<link rel=\"stylesheet\" href=\"css/screen.css\" type=\"text/css\" media=\"screen, projection\">";
 		header += "<link rel=\"stylesheet\" href=\"css/custom.css\" type=\"text/css\" media=\"screen, projection\">";
 		return header;
 	}
 	
 	protected abstract ResultSet doQuery() throws SQLException;
 	
 	protected abstract String generateBody(ResultSet resultSet) throws SQLException;
 
 	protected String stmtZweitStimmenNachBundesland() {
 		return ""
 			+ "SELECT w2." + DB.kForeignKeyParteiID + ", wk." + DB.kForeignKeyBundeslandID + ", "
 			 			 + "sum(w2." + DB.kWahlergebnis2Anzahl + ") as " + DB.kAnzahlStimmen + " "
 			+ "FROM " + db.zweitStimmenNachWahlkreis() + " w2" + ", " + db.wahlkreis() + " wk" + " "
 			+ "WHERE w2." + DB.kForeignKeyWahlkreisID + " = wk." + DB.kID + " "
 			+   "AND w2." + DB.kJahr + " = " + kCurrentElectionYear + " "
 			+ "GROUP BY " + "wk." + DB.kForeignKeyBundeslandID + ", w2." + DB.kForeignKeyParteiID;
 	}
 	
 	protected String createZweitStimmenNachBundeslandTable() throws SQLException {
 		db.createFilledTemporaryTable(db.zweitStimmenNachBundesland(), DB.kForeignKeyParteiID + " BIGINT, "
 		 			+ DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT", stmtZweitStimmenNachBundesland());
 	  return db.zweitStimmenNachBundesland();
 	}
 	
 	protected String stmtZweitStimmenNachPartei(String zweitStimmenNachBundeslandTable) {
 		return ""
 			+ "SELECT " + DB.kForeignKeyParteiID + ", SUM(" + DB.kAnzahlStimmen + ") AS " + DB.kAnzahlStimmen + " "
 			+ "FROM " + zweitStimmenNachBundeslandTable + " "
 			+ "GROUP BY " + DB.kForeignKeyParteiID;
 	}
 
 	protected String createZweitStimmenNachParteiTable(String zweitStimmenNachBundeslandTable) throws SQLException {
 		db.createFilledTemporaryTable(db.zweitStimmenNachPartei(), DB.kForeignKeyParteiID
         + " BIGINT, " + DB.kAnzahlStimmen + " BIGINT", stmtZweitStimmenNachPartei(zweitStimmenNachBundeslandTable));
 		return db.zweitStimmenNachPartei();
 	}
 
 	protected String createDirektmandateTable() throws SQLException {
 		return createDirektmandateTable(db.erstStimmenNachWahlkreis());
 	}
 	
 	protected String stmtMaxErststimmenNachWahlkreis(String erstStimmenNachWahlkreisTable) {
 		return "" +
   	"SELECT k." + DB.kKandidatDMWahlkreisID + " AS " + DB.kForeignKeyWahlkreisID + ", " + 
   				 "MAX(v." + DB.kAnzahl + ") AS " + DB.kMaxStimmen + " " + 
   	"FROM " + erstStimmenNachWahlkreisTable + " v, " + db.kandidat() + " k " + 
   	"WHERE v." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " +
   		"AND v." + DB.kJahr + " = " + kCurrentElectionYear + " " +
   	"GROUP BY k." + DB.kKandidatDMWahlkreisID;
 	}
 	
 	protected String stmtDirektmandateNummer(String maxErstStimmenNachWahlkreisTable,
 			String erstStimmenNachWahlkreisTable) {
 		return ""
 			+ "SELECT e." + DB.kForeignKeyKandidatID + ", e." + DB.kForeignKeyWahlkreisID
 				+ ", Row_Number() OVER (PARTITION BY e." + DB.kForeignKeyWahlkreisID + ") AS " + DB.kNummer + " "
 			+ "FROM " + maxErstStimmenNachWahlkreisTable + " m, " + erstStimmenNachWahlkreisTable + " e "
 			+ "WHERE e." + DB.kForeignKeyWahlkreisID + " = m." + DB.kForeignKeyWahlkreisID + " "
 				+ "AND	m." + DB.kMaxStimmen + " = e." + DB.kAnzahl + " AND e." + DB.kJahr + " = " + kCurrentElectionYear;
 	}
 	
 	protected String stmtDirektmandateMaxNummer(String direktmandateNummerTable) {
 		return ""
 			+ "SELECT " + DB.kForeignKeyWahlkreisID + ", MAX(" + DB.kNummer + ") AS " + DB.kMaxNummer + " "
 			+ "FROM " + direktmandateNummerTable + " "
 			+ "GROUP BY " + DB.kForeignKeyWahlkreisID;
 	}
 
 	protected String stmtDirektmandate(String direkmandateNummerTable, String direktMandateMaxNummerTable) {
 		return ""
 			+ "SELECT n." + DB.kForeignKeyKandidatID + ", k." + DB.kForeignKeyParteiID + ", "
 				+ "k." + DB.kKandidatDMWahlkreisID + " "
 			+ "FROM " + direkmandateNummerTable + " n, " + direktMandateMaxNummerTable + " mn, " + db.zufallsZahlen() + " z, "
 				+ db.kandidat() + " k "
 			+ "WHERE n." + DB.kForeignKeyWahlkreisID + " = mn." + DB.kForeignKeyWahlkreisID + " "
 				+ "AND k." + DB.kID + " = n." + DB.kForeignKeyKandidatID + " "
 				+ "AND z." + DB.kZeile + " = mod(n." + DB.kForeignKeyWahlkreisID + ", "
 					+ "(SELECT Count(*) FROM " + db.zufallsZahlen()+ ")) "
 				+ "AND n." + DB.kNummer + " = mod(z." + DB.kZahl + ", mn." + DB.kMaxNummer + ") + 1";
 	}
 	
 	protected String createDirektmandateTable(String erstStimmenNachWahlkreisTable) throws SQLException {
 		db.createFilledTemporaryTable(db.direktmandate(), DB.kForeignKeyKandidatID + " BIGINT, "
         	+ DB.kForeignKeyParteiID + " BIGINT, " + DB.kKandidatDMWahlkreisID + " BIGINT",
         "WITH " + db.maxErststimmenNachWahlkreis() 
     			+ " AS ( " + stmtMaxErststimmenNachWahlkreis(erstStimmenNachWahlkreisTable) + "), "
   			+ db.direktMandateNummer() + " AS ( "
   				+ stmtDirektmandateNummer(db.maxErststimmenNachWahlkreis(), erstStimmenNachWahlkreisTable) + "), "
   			+ db.direktMandateMaxNummer() + " AS ( "
   				+ stmtDirektmandateMaxNummer(db.direktMandateNummer()) + ") "
     		+	stmtDirektmandate(db.direktMandateNummer(), db.direktMandateMaxNummer()));
     return db.direktmandate();
 	}
 
 	protected String stmtFuenfProzentParteien(String zweitStimmenNachBundeslandTable,
 			String zweitStimmenNachParteiTable) {
 		return ""
 			+ "SELECT p." + DB.kID + " as " + DB.kForeignKeyParteiID + " "
 			+ "FROM " + db.partei() + " p, " + db.zweitStimmenNachWahlkreis() + " v "
 	    + "WHERE v." + DB.kForeignKeyParteiID + " = p." + DB.kID + " "
	    	+ "AND v." + DB.kJahr + "=" + kCurrentElectionYear + " "
 	    + "GROUP BY p." + DB.kID + " "
 	    + "HAVING CAST(SUM(v." + DB.kWahlergebnis2Anzahl + ") AS FLOAT)" + " / "
 	    	+ "(SELECT SUM(" + DB.kAnzahlStimmen + ") "	+ "FROM " + zweitStimmenNachBundeslandTable + ")"
 	    	+ " >= 0.05";
 	}
 	
 	protected String createFuenfProzentParteienTable(String zweitStimmenNachBundeslandTable,
 			String zweitStimmenNachParteiTable) throws SQLException {
 		db.createFilledTemporaryTable(db.fuenfProzentParteien(), DB.kForeignKeyParteiID + " BIGINT",
 				stmtFuenfProzentParteien(zweitStimmenNachBundeslandTable, zweitStimmenNachParteiTable));
     return db.fuenfProzentParteien();
 	}
 
 	
 	protected String stmtDreiDirektmandateParteien(String direktMandateTable) {
 		return "SELECT dm."
     	+ DB.kForeignKeyParteiID + " FROM " + direktMandateTable + " dm " + " GROUP BY dm."
     	+ DB.kForeignKeyParteiID + " HAVING COUNT(*) >= 3";
 	}
 	protected String createDreiDirektmandateParteienTable(String direktMandateTable) throws SQLException {
 		db.createFilledTemporaryTable(db.dreiDirektMandatParteien(), DB.kForeignKeyParteiID + " BIGINT",
 				stmtDreiDirektmandateParteien(direktMandateTable));
     return db.dreiDirektMandatParteien();
 	}
 	
 	protected String stmtParteienImBundestag(String fuenfProzentParteienTable, String dreiDirektMandateTable) {
 		return  " SELECT * FROM " + fuenfProzentParteienTable + " UNION " + " SELECT * FROM " + dreiDirektMandateTable;
 	}
 	
 	protected String createParteienImBundestagTable(String fuenfProzentParteienTable, String dreiDirektMandateTable) throws SQLException {
 		db.createFilledTemporaryTable(db.parteienImBundestag(), DB.kForeignKeyParteiID + " BIGINT",
 				stmtParteienImBundestag(fuenfProzentParteienTable, dreiDirektMandateTable));
     return db.parteienImBundestag();
 	}
 	
 	protected String stmtDivisoren() {
 		return ""
 			+ "SELECT (ROW_NUMBER() OVER (order by w." + DB.kID + ")  - 0.5) as Wert "
 			+ "FROM " + db.wahlkreis() + " w "
 			+ "UNION "
 			+ "SELECT (ROW_NUMBER() OVER (order by w." + DB.kID + ") + (SELECT COUNT(*) FROM " + db.wahlkreis() + ") - 0.5) AS Wert "
 			+ "FROM " + db.wahlkreis() + " w";
 	}
 	
 	protected String stmtZugriffsreihenfolgeSitzeNachPartei(String parteienImBundestagTable,
 			String zweitStimmenNachParteiTable, String DivisorenTable) {
 		return ""
 			+ "SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kAnzahlStimmen + ", "
 			+ "(z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, "
 			+ "ROW_NUMBER() OVER (ORDER BY (z." + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang "
 			+ "FROM " + parteienImBundestagTable + " p, " + zweitStimmenNachParteiTable + " z, " + DivisorenTable + " d "
 			+ "WHERE p." + DB.kForeignKeyParteiID + " = z."
 		  + DB.kForeignKeyParteiID + " "
 		  + "ORDER BY DivWert desc";
 	}
 	
 	protected String stmtSitzeNachParteiTable(String zweitStimmenNachParteiTable, String parteienImBundestagTable,
 			String zugriffsreihenfolgeSitzeNachParteiTable) {
 		return ""
 		+ "SELECT " + DB.kForeignKeyParteiID + ", COUNT(Rang) as " + DB.kAnzahlSitze + " "
 		+ "FROM " + zugriffsreihenfolgeSitzeNachParteiTable + " "
 		+ "WHERE Rang <= 598 "
     + "GROUP BY " + DB.kForeignKeyParteiID;
 	}
 	
 	protected String createSitzeNachPartei(String zweitStimmenNachParteiTable, String parteienImBundestagTable) throws SQLException {
 		db.createFilledTemporaryTable(db.sitzeNachPartei(), DB.kForeignKeyParteiID + " BIGINT, "
         + DB.kAnzahlSitze + " BIGINT",
         "WITH " + db.divisoren() + " AS (" + stmtDivisoren() + "), "
         + db.zugriffsreihenfolgeSitzeNachPartei() + " AS ("
          + stmtZugriffsreihenfolgeSitzeNachPartei(parteienImBundestagTable, zweitStimmenNachParteiTable, db.divisoren())
          + ") "
         + stmtSitzeNachParteiTable(
         		zweitStimmenNachParteiTable, parteienImBundestagTable, db.zugriffsreihenfolgeSitzeNachPartei()));
     return db.sitzeNachPartei();
 	}
 	
 	protected String stmtZugriffsreihenfolgeSitzeNachLandeslisten(String parteienImBundestagTable,
 			String zweitStimmenNachBundeslandTable, String divisorenTable) {
 		return ""
 		+ "SELECT p." + DB.kForeignKeyParteiID + ", z." + DB.kForeignKeyBundeslandID + ", z." + DB.kAnzahlStimmen + ", "
 		  		 + "(z." + DB.kAnzahlStimmen + " / d.wert) as DivWert, "
 		  		 + "ROW_NUMBER() OVER (PARTITION BY p." + DB.kForeignKeyParteiID + " "
 		  		 	 + "ORDER BY (z." + DB.kAnzahlStimmen + " / d.wert) DESC) as Rang "
 		+ "FROM " + parteienImBundestagTable + " p, " + zweitStimmenNachBundeslandTable + " z, " + divisorenTable + " d "
 		+ "WHERE p." + DB.kForeignKeyParteiID + " = z." + DB.kForeignKeyParteiID + " "
 		+ "ORDER BY " + DB.kForeignKeyParteiID + ", DivWert DESC";
 	}
 	
 	protected String stmtSitzeNachLandeslisten(String parteienImBundestagTable, 
 			String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable,
 			String zugriffsreihenfolgeSitzeNachLandeslistenTable) {
 		return ""
 			+ "SELECT z." + DB.kForeignKeyParteiID + ", " + DB.kForeignKeyBundeslandID 
 					 + ", COUNT(Rang) as " + DB.kAnzahlSitze + " "
 	    + "FROM " + zugriffsreihenfolgeSitzeNachLandeslistenTable +" z, " + sitzeNachParteiTable + " s "
 	    + "WHERE z." + DB.kForeignKeyParteiID + " = s." + DB.kForeignKeyParteiID + " "
 	    	+ "AND z.Rang <= s." + DB.kAnzahlSitze + " "
 	    + "GROUP BY z." + DB.kForeignKeyParteiID + ", z." + DB.kForeignKeyBundeslandID + ", s." + DB.kForeignKeyParteiID;
 	}
 	
 	protected String createSitzeNachLandeslistenTable(String parteienImBundestagTable, 
 			String zweitStimmenNachBundeslandTable, String sitzeNachParteiTable) throws SQLException {
 		db.createFilledTemporaryTable(db.sitzeNachLandeslisten(), DB.kForeignKeyParteiID
         + " BIGINT, " + DB.kForeignKeyBundeslandID + " BIGINT, " + DB.kAnzahlSitze + " BIGINT",
         "WITH " + db.divisoren() + " AS (" + stmtDivisoren() + "), "
     		+ db.zugriffsreihenfolgeSitzeNachLandeslisten() + " AS ("
     			+ stmtZugriffsreihenfolgeSitzeNachLandeslisten(
     					parteienImBundestagTable, zweitStimmenNachBundeslandTable, db.divisoren()) + ") "
     		+ stmtSitzeNachLandeslisten(parteienImBundestagTable, zweitStimmenNachBundeslandTable, sitzeNachParteiTable, 
     				db.zugriffsreihenfolgeSitzeNachLandeslisten()));
     return db.sitzeNachLandeslisten();
 	}	
 
 	protected String stmtDirektMandateProParteiUndBundesland(String direktMandateTable) {
 		return "" +
 			"SELECT k." + DB.kForeignKeyParteiID + ", w." + DB.kForeignKeyBundeslandID + ", COUNT(*) AS AnzahlDirektmandate " +
 			"FROM " + direktMandateTable + " dm, " + db.kandidat() + " k, " + db.wahlkreis() + " w " +
 			"WHERE dm." + DB.kForeignKeyKandidatID + " = k." + DB.kID + " " + 
 			"AND w." + DB.kID + " = k." + DB.kKandidatDMWahlkreisID + " " + 
 			"GROUP BY k." + DB.kForeignKeyParteiID + ", w." + DB.kForeignKeyBundeslandID;
 	}
 	
 	protected String stmtUeberhangsmandate(String direktMandateTable, String sitzeNachLandeslistenTable,
 			String direktMandateProParteiUndBundeslandTable) {
 		return ""
 			+ "SELECT b." + DB.kID + " AS " + DB.kForeignKeyBundeslandID + ", b." + DB.kBundeslandName + ", "
 				+ "p." + DB.kID + " AS " + DB.kForeignKeyParteiID + ", p." + DB.kParteiKuerzel + ", "
 				+ "dmpb.AnzahlDirektmandate - s." + DB.kAnzahlSitze + " AS " + DB.kAnzahlUeberhangsmandate + " "
 			+ "FROM " + direktMandateProParteiUndBundeslandTable + " dmpb, " + sitzeNachLandeslistenTable + " s, "
 				+ db.partei() + " p, " + db.bundesland() + " b "
 			+ "WHERE dmpb." + DB.kForeignKeyBundeslandID + " = s." + DB.kForeignKeyBundeslandID + " "
 				+ "AND dmpb." + DB.kForeignKeyParteiID + " = s." + DB.kForeignKeyParteiID + " "
 				+ "AND dmpb." + DB.kForeignKeyParteiID + " = p." + DB.kID + " "
 				+ "AND dmpb." + DB.kForeignKeyBundeslandID + " = b." + DB.kID + " "
 				+ "AND dmpb.AnzahlDirektmandate - s." + DB.kAnzahlSitze + " > 0";
 	}
 
 	protected String createUeberhangsmandateTable(String direktMandateTable, String sitzeNachLandeslistenTable) throws SQLException {
 		db.createFilledTemporaryTable(db.ueberhangsMandate(), DB.kForeignKeyBundeslandID + " BIGINT, " +
 				DB.kBundeslandName + " VARCHAR(255), " + 
 				DB.kForeignKeyParteiID + " BIGINT, " +
 				DB.kParteiKuerzel + " VARCHAR(64), " +
 				DB.kAnzahlUeberhangsmandate + " BIGINT ", 
 
 				"WITH " + db.direktMandateProParteiUndBundesland() + " AS ("
 				+ stmtDirektMandateProParteiUndBundesland(direktMandateTable) + ") "
 			+ stmtUeberhangsmandate(direktMandateTable, sitzeNachLandeslistenTable,
 					db.direktMandateProParteiUndBundesland()));
 		return db.ueberhangsMandate();
 	}
 	
 	protected String stmtMaxZweitStimmenNachWahlkreis() {
 		return "" +
 			"SELECT we." + DB.kForeignKeyWahlkreisID + ", MAX(we." + DB.kWahlergebnis2Anzahl + ") AS " + DB.kMaxStimmen + " " +
 			"FROM " + db.zweitStimmenNachWahlkreis() + " we " +
 			"WHERE we." + DB.kJahr + " = " + kCurrentElectionYear + " " +
 			"GROUP BY " + DB.kForeignKeyWahlkreisID;
 	}
 	
 	protected String stmtGewinnerErststimmen(String maxErstStimmenNachWahlkreisTable) {
 		return "" +
 			"SELECT we." + DB.kForeignKeyWahlkreisID + ", we." + DB.kForeignKeyKandidatID + " " + 
 			"FROM " + db.erstStimmenNachWahlkreis() + " we, " + maxErstStimmenNachWahlkreisTable + " ms " + 
 			"WHERE we." + DB.kForeignKeyWahlkreisID + " = ms.WahlkreisID " + 
 				"AND we." + DB.kAnzahl + " = ms." + DB.kMaxStimmen + " " +
 				"AND we." + DB.kJahr + " = " + kCurrentElectionYear;
 	}
 	
 	protected String stmtGewinnerZweitStimmen(String maxZweitStimmenNachWahlkreisTable) {
 		return "" +
 			"SELECT we." + DB.kForeignKeyWahlkreisID + ", we." + DB.kForeignKeyParteiID + " " + 
 			"FROM " + db.zweitStimmenNachWahlkreis() + " we, " + maxZweitStimmenNachWahlkreisTable + " ms " + 
 			"WHERE we." + DB.kForeignKeyWahlkreisID + " = ms.WahlkreisID " + 
 				"AND we." + DB.kWahlergebnis2Anzahl + " = ms." + DB.kMaxStimmen + " " +
 				"AND we." + DB.kJahr + " = " + kCurrentElectionYear; 
 	}
 	
 	protected String stmtWahlkreissieger(String gewinnerErstStimmenTable, String gewinnerZweitStimmenTable) {
 		return "" +
 			"SELECT g1.WahlkreisID, wk." + DB.kForeignKeyBundeslandID + ", " +
 				"p1." + DB.kParteiKuerzel + " AS P1, p2." + DB.kParteiKuerzel + " AS P2 " +
 			"FROM " + gewinnerErstStimmenTable + " g1, " + gewinnerZweitStimmenTable + " g2, " + db.partei() + " p1, " + 
 				db.partei() + " p2, " + db.kandidat() + " k, " + db.wahlkreis() + " wk " + 
 			"WHERE g1.WahlkreisID = g2.WahlkreisID " + 
 				"AND g1.KandidatID = k." + DB.kID + " " +
 				"AND k.ParteiID = p1." + DB.kID + " " +
 				"AND g2.ParteiID = p2." + DB.kID + " " + 
 				"AND wk." + DB.kID + " = g1." + DB.kForeignKeyWahlkreisID;
 	}
 	
 	public String createWahlkreissiegerTable() throws SQLException {
 		db.createFilledTemporaryTable(db.wahlkreisSieger(), DB.kForeignKeyWahlkreisID + " BIGINT, " +
 				DB.kForeignKeyBundeslandID + " BIGINT, " + "P1 VARCHAR(64), " + "P2 VARCHAR(64)",
 				"WITH "
 				+ db.maxZweitStimmenNachWahlkreis() + " AS (" + stmtMaxZweitStimmenNachWahlkreis() + "), "
 				+ db.maxErststimmenNachWahlkreis() + " AS ("
 					+ stmtMaxErststimmenNachWahlkreis(db.erstStimmenNachWahlkreis()) + "), "
 				+ db.gewinnerZweitstimmen() + " AS (" + stmtGewinnerZweitStimmen(db.maxZweitStimmenNachWahlkreis()) + "), "
 				+ db.gewinnerErststimmen() + " AS (" + stmtGewinnerErststimmen(db.maxErststimmenNachWahlkreis()) + ") "
 				+ stmtWahlkreissieger(db.gewinnerErststimmen(), db.gewinnerZweitstimmen()));
 		return db.wahlkreisSieger();
 	}
 }
