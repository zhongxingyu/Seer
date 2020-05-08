 package testbw.analysis;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 public class DataAnalyzer {
 
 	// Datenbankverbindungsdaten
 	private Statement st = null;
 	private ResultSet rs = null;
 
 	public DataAnalyzer(Statement st, ResultSet rs) {
 		this.st = st;
 		this.rs = rs;
 	}
 
 	public void calculateTableWahlkreissieger(String tableName) throws SQLException {
 		//
 		st.executeUpdate("DROP TABLE IF EXISTS " + tableName + " CASCADE;");
 		st.executeUpdate("CREATE TABLE " + tableName + "(wahlkreis integer , max integer, kandnum integer ,  PRIMARY KEY (kandnum))WITH (OIDS=FALSE);");
 		st.executeUpdate("INSERT INTO " + tableName + " (SELECT wahlkreis, max, min(kandnum) AS kandnum 	FROM maxvoteskand 	GROUP BY wahlkreis, max ORDER BY max);");
 
 	}
 
 	/**
 	 * Returns seat distribution of the Bundestag.
 	 */
 	public ArrayList<ArrayList<String>> getSeatDistribution(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 		// Auswertung ---------------------------------------------------------
 		// --------------------------------------------------------------------
 
 		// temporäre Tabellen
 
 		// -- Auswertungsanfrage: Endergebnisse (wiederverwendbare Tabellen)
 
 		st.executeUpdate("CREATE OR REPLACE VIEW stimmenpropartei AS ( " + " SELECT partei, sum(anzahl) AS anzahl " + " FROM zweitstimmen " + " WHERE jahr = '" + jahrName + "' "
 				+ " GROUP BY partei);");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW maxvotes AS (" + "  SELECT wahlkreis, max(anzahl) AS max " + "  FROM erststimmen " + "  WHERE jahr = '" + jahrName + "' " + "  GROUP BY wahlkreis);");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW maxvoteskand AS (" + "	SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max "
 				+ "	FROM maxvotes m JOIN (SELECT * FROM erststimmen s WHERE s.jahr = '" + jahrName + "') e " + "	ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl);");
 
 		calculateTableWahlkreissieger("maxvotesuniquekand");
 
 		// -- Auswertungsanfrage: Endergebnisse (Zweitstimmen)
 
 		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmenergebnis AS ( "
 
 		+ "WITH divisoren AS ( " + "	SELECT GENERATE_SERIES(1, 2*(SELECT sitze FROM sitzeprojahr WHERE jahr = '" + jahrName + "')-1, 2) AS div), "
 
 		+ "itrergebnisse AS ( " + "	SELECT s.partei AS parteinum,  (s.anzahl / d.div::float8)+RANDOM() AS anzahl " + "	FROM divisoren d, stimmenpropartei s " + "	ORDER BY anzahl DESC "
 				+ "   LIMIT (SELECT sitze FROM sitzeprojahr WHERE jahr = '" + jahrName + "')), "
 
 				+ "unfiltered AS ( " + "	SELECT parteinum, COUNT(*) AS sitze " + "	FROM itrergebnisse " + "	GROUP BY parteinum), "
 
 				+ "filtered AS ( " + "SELECT * FROM unfiltered " + "WHERE sitze >= 0.05 * (SELECT SUM(sitze) FROM sitzeprojahr WHERE jahr = '" + jahrName + "')), "
 
 				+ "huerdedivisoren AS ( " + "	SELECT GENERATE_SERIES(1, 2*((SELECT SUM(sitze) FROM sitzeprojahr WHERE jahr = '" + jahrName
 				+ "') - (SELECT SUM(sitze) FROM filtered))::bigint, 2) AS div), "
 
 				+ "huerdeitrergebnisse AS ( " + "	SELECT f.parteinum AS parteinum,  (f.sitze / d.div::float8) AS anzahl " + "	FROM huerdedivisoren d, filtered f " + "	ORDER BY anzahl DESC "
 				+ "   LIMIT (((SELECT SUM(sitze) FROM sitzeprojahr WHERE jahr = '" + jahrName + "') - (SELECT SUM(sitze) FROM filtered))::bigint)), "
 
 				+ "neuezuweisungen AS ( " + "	SELECT parteinum, COUNT(*) AS sitze " + "	FROM huerdeitrergebnisse " + "	GROUP BY parteinum) "
 
 				+ "SELECT p.name AS parteiname, f.sitze + n.sitze AS sitze " + "FROM filtered f JOIN neuezuweisungen n ON f.parteinum = n.parteinum JOIN partei p ON f.parteinum = p.parteinummer);"
 
 		);
 
 		// -- Auswertungsanfrage: Endergebnisse (Erststimmen)
 		st.executeUpdate("CREATE OR REPLACE VIEW erststimmenergebnis AS (" + "WITH parteinsitze AS ( " + "SELECT partei, COUNT(*) AS sitze "
 				+ "FROM maxvotesuniquekand m JOIN (SELECT * FROM direktkandidat dk WHERE dk.jahr = '" + jahrName + "') d " + "ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis "
 				+ "GROUP BY partei) " +
 
 				"SELECT p.name AS parteiname, pn.sitze AS sitze " + "FROM parteinsitze pn join partei p " + "ON pn.partei = p.parteinummer);");
 
 		// Karten Info -------------------------------------------------------
 		// --------------------------------------------------------------------
 
 		st.executeUpdate("CREATE OR REPLACE VIEW parteibluebersichtzweitstimmen AS ( " + "WITH ergebnisparteienzweitstimmen AS ( " + "		SELECT z.parteiname, p.parteinummer "
 				+ "		FROM zweitstimmenergebnis z JOIN partei p " + "		ON z.parteiname = p.name), "
 
 				+ "bundeslandzweitstimmen AS ( " + "		SELECT b.abkuerzung AS bundesland, z.partei AS partei, z.anzahl AS anzahl " + "		FROM  (SELECT * FROM zweitstimmen WHERE jahr = '" + jahrName
 				+ "') z JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahrName + "') w  " + "		ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) "
 
 				+ "	SELECT p.name AS partei, b.bundesland AS bundesland, SUM(b.anzahl)::numeric AS anzahl " + "	FROM bundeslandzweitstimmen b JOIN partei p ON b.partei = p.parteinummer "
 				+ " WHERE b.partei IN (SELECT parteinummer FROM ergebnisparteienzweitstimmen) " + "	GROUP BY p.name, b.bundesland);");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW parteibluebersichterststimmen AS ( " + "WITH ergebnisparteienerststimmen AS ( " + "		SELECT e.parteiname, p.parteinummer "
 				+ "		FROM erststimmenergebnis e JOIN partei p " + "		ON e.parteiname = p.name), "
 
 				+ "bundeslanderststimmen AS ( " + "		SELECT b.abkuerzung AS bundesland, z.kandidatennummer AS kandidatennummer, z.anzahl AS anzahl "
 				+ "		FROM  (SELECT * FROM erststimmen WHERE jahr = '" + jahrName + "') z JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahrName + "') w  "
 				+ "		ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) "
 
 				+ "	SELECT p.name AS partei, b.bundesland AS bundesland, SUM(b.anzahl)::numeric AS anzahl " + "	FROM bundeslanderststimmen b JOIN (SELECT * FROM direktkandidat WHERE jahr = '"
 				+ jahrName + "') d " + "	ON b.kandidatennummer = d.kandidatennummer JOIN partei p ON d.partei = p.parteinummer"
 				+ " WHERE d.partei IN (SELECT parteinummer FROM ergebnisparteienerststimmen) " + "	GROUP BY p.name, b.bundesland);");
 
 		// Table meta info
 		List<String> tableNames = Arrays.asList("Zweitstimmenergebnis", "parteibluebersichtzweitstimmen", "Erststimmenergebnis", "parteibluebersichterststimmen");
 
 		List<List<String>> colNames = Arrays.asList(Arrays.asList("Parteiname", "Sitze"), Arrays.asList("Parteiname", "Bundesland", "Stimmen"), Arrays.asList("Parteiname", "Sitze"),
 				Arrays.asList("Parteiname", "Bundesland", "Stimmen"));
 
 		for (int i = 0; i < tableNames.size(); i++) {
 
 			ArrayList<String> header = new ArrayList<String>();
 			header.add(tableNames.get(i));
 			for (int j = 0; j < colNames.get(i).size(); j++) {
 				header.add(colNames.get(i).get(j));
 			}
 
 			result.add(header);
 			collectFromQuery(result, tableNames.get(i));
 		}
 
 		// System.out.println("\n---------------------------------");
 		//
 		// for (ArrayList<String> entry : result) {
 		// System.out.println("\n-------------");
 		// for (String entry2 : entry) {
 		// System.out.print(entry2 + "  ,  ");
 		//
 		// }
 		// }
 		// System.out.println("\n---------------------------------");
 
 		return result;
 	}
 
 	/**
 	 * Return a view of all Ueberhangsmandate
 	 */
 	public ArrayList<ArrayList<String>> getUeberhangsmandate(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 
 		// temporäre Tabellen
 
 		st.executeUpdate("DROP TABLE IF EXISTS ueberhangerststimmen CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS ueberhangzweitstimmen CASCADE;");
 
 		st.executeUpdate("CREATE TABLE ueberhangerststimmen(parteiname text,  bundesland text, mandate integer,  PRIMARY KEY (parteiname,bundesland))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE ueberhangzweitstimmen(parteiname text,  bundesland text, mandate integer,  PRIMARY KEY (parteiname,bundesland))WITH (OIDS=FALSE);");
 
 		st.executeUpdate("CREATE  OR REPLACE VIEW maxvotesuniquekand4 AS (SELECT wahlkreis, max, min(kandnum) AS kandnum 	FROM maxvoteskand 	GROUP BY wahlkreis, max ORDER BY max);");
 
 		// Ueberhangsmandate
 		st.executeUpdate("INSERT INTO ueberhangerststimmen ( "
 				+
 
 				// Also computed in getSeatDistribution -> use those results
 				/*
 				 * // groesste Stimmenanzahl fuer jeden Wahlkreis
 				 * "WITH maxvotes AS ( " +
 				 * "  SELECT wahlkreis, max(anzahl) AS max " +
 				 * "  FROM erststimmen " + "  WHERE jahr = '"+jahr+"' " +
 				 * "  GROUP BY wahlkreis), " +
 				 * 
 				 * // pro Wahlkreis, Kandidaten mit groessten Stimmenanzahl
 				 * "maxvoteskand AS ( " +
 				 * "	SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max "
 				 * +
 				 * "	FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '"
 				 * +jahr+"') e " +
 				 * "	ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl), " +
 				 * 
 				 * "maxvotesuniquekand AS ( " +
 				 * "	SELECT wahlkreis, max, min(kandnum) AS kandnum " +
 				 * "	FROM maxvoteskand " + "	GROUP BY wahlkreis, max), " +
 				 */
 
 				// in wahlkreis x won party y
 				"WITH parteigewinner AS ( " + "	SELECT m.wahlkreis AS wahlkreis, d.partei AS partei "
 				+ "	FROM maxvotesuniquekand4 m left outer join (SELECT * FROM direktkandidat dk WHERE dk.jahr = '" + jahrName + "') d "
 				+ "	ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis), "
 				+
 
 				// in bundesland z, wahlkreis x won party y
 				"blgewinner AS ( " + "	SELECT b.name AS bundesland,  p.wahlkreis AS wahlkreis,  p.partei AS partei " + "	FROM parteigewinner p JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahrName
 				+ "') w ON p.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) " +
 
 				"SELECT  p.name AS parteiname,b.bundesland AS bundesland, COUNT(*) AS mandate " + "FROM blgewinner b JOIN partei p ON b.partei = p.parteinummer " + "GROUP BY b.bundesland, p.name);");
 
 		st.executeUpdate("INSERT INTO ueberhangzweitstimmen ( "
 				+
 
 				// parteien aus dem zweitstimmenergebnis
 				"WITH ergebnisparteien AS ( "
 				+ "	SELECT z.parteiname, p.parteinummer "
 				+ "	FROM zweitstimmenergebnis z JOIN partei p "
 				+ "	ON z.parteiname = p.name), "
 				+
 
 				// iteratoren 1, 3, 5, ... max(sitze) fuer alle parteien im BT
 				"iterators AS ( "
 				+ "	SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM zweitstimmenergebnis), 2) AS iterator ), "
 				+
 
 				// iteratoren reduziert pro Partei
 				"parteiiterators AS ( "
 				+ " 	SELECT * "
 				+ "	FROM ergebnisparteien e, iterators i "
 				+ "	WHERE i.iterator <= 2 * (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = e.parteiname)), "
 				+
 
 				// anzahl stimmen pro "finalisten" partei und bundesland
 				"parteibluebersicht AS ( "
 				+ "	SELECT b.name AS bundesland, z.partei AS partei, SUM(z.anzahl)::numeric AS anzahl "
 				+ "	FROM  (SELECT * FROM zweitstimmen WHERE jahr = '"
 				+ jahrName
 				+ "' AND partei IN (SELECT parteinummer FROM ergebnisparteien)) z JOIN (SELECT * FROM wahlkreis WHERE jahr = '"
 				+ jahrName
 				+ "') w  "
 				+ "	ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer "
 				+ "	GROUP BY z.partei, b.name), "
 				+
 
 				// tree: partei x iterator x bundesland builds to result for
 				// iterator
 				"parteiiteratorbl AS ( "
 				+ "	SELECT p1.parteiname AS parteiname, p1.parteinummer AS parteinummer, p2.bundesland AS bundesland, (p2.anzahl::numeric / p1.iterator::numeric) + RANDOM() AS itrergebnis "
 				+ "	FROM parteiiterators p1 JOIN parteibluebersicht p2 " + "	ON p1.parteinummer = p2.partei " + "   ORDER BY partei ASC, itrergebnis DESC), " +
 
 				/*
 				 * // auswahl bundeslaender mit groessten zwischenergebnissen
 				 * "filtered AS ( " + "	SELECT * " +
 				 * "	FROM parteiiteratorbl p1 " + "	WHERE " +
 				 * "		(SELECT COUNT(*) FROM parteiiteratorbl p2 " +
 				 * "		WHERE p1.parteinummer = p2.parteinummer " +
 				 * "		AND p2.itrergebnis > p1.itrergebnis) " +
 				 * 
 				 * "		< (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p1.parteiname)) "
 				 * +
 				 * 
 				 * 
 				 * // aggregation
 				 * "SELECT parteiname, bundesland, COUNT(*) as mandate " +
 				 * "FROM filtered " +
 				 * "GROUP BY parteiname, parteinummer, bundesland); "
 				 */
 
 				"partitionen AS ( "
 				+ "	SELECT p.parteiname AS parteiname, p.bundesland AS bundesland, p.itrergebnis AS itrergebnis, ROW_NUMBER() OVER (PARTITION BY p.parteiname ORDER BY p.itrergebnis DESC) AS rn "
 				+ "	FROM parteiiteratorbl p), " +
 
 				"filter AS ( " + "	SELECT * FROM partitionen p " + "	WHERE p.rn <= (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p.parteiname)) " +
 
 				"SELECT f.parteiname AS parteiname, f.bundesland AS bundesland, COUNT(*) as mandate " + "FROM filter f " + "GROUP BY f.parteiname, f.bundesland);"
 
 		);
 
 		st.executeUpdate("CREATE OR REPLACE VIEW test AS ( SELECT e.bundesland AS bundesland, e.parteiname AS parteiname, e.mandate - z.mandate AS mandate FROM ueberhangerststimmen e , ueberhangzweitstimmen z "
 				+ "	WHERE e.bundesland = z.bundesland AND e.parteiname = z.parteiname )" + "");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW umandate AS ( " +
 
 		"WITH unfiltered AS ( SELECT e.bundesland AS bundesland, e.parteiname AS parteiname, e.mandate - z.mandate AS mandate FROM ueberhangerststimmen e , ueberhangzweitstimmen z "
 				+ "	WHERE e.bundesland = z.bundesland AND e.parteiname = z.parteiname) " +
 
 				"SELECT * FROM unfiltered  WHERE mandate > 0);"
 
 		);
 
 		List<String> tableNames = Arrays.asList("umandate");
 
 		List<List<String>> colNames = Arrays.asList(Arrays.asList("Bundesland", "Parteiname", "Ueberhangsmandate"));
 
 		for (int i = 0; i < tableNames.size(); i++) {
 
 			ArrayList<String> header = new ArrayList<String>();
 			header.add(tableNames.get(i));
 			for (int j = 0; j < colNames.get(i).size(); j++) {
 				header.add(colNames.get(i).get(j));
 			}
 
 			result.add(header);
 			collectFromQuery(result, tableNames.get(i));
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get the winners for every Wahlkreis. Dependend on getSeatdistribution -
 	 * SQL View maxvotesuniquekand
 	 */
 	public ArrayList<ArrayList<String>> getWahlkreissieger(String[] queryInput) throws SQLException, NumberFormatException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 
 		st.executeUpdate("CREATE  OR REPLACE VIEW maxvotesuniquekand2 AS (SELECT wahlkreis, max, min(kandnum) AS kandnum 	FROM maxvoteskand 	GROUP BY wahlkreis, max ORDER BY max);");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnernummern AS SELECT  mvuk.wahlkreis as wahlkreisnummer, d.politiker, d.partei , mvuk.max as anzahl FROM "
 				+ "maxvotesuniquekand2 mvuk , direktkandidat d  WHERE d.jahr = " + jahrName + " AND  d.kandidatennummer = mvuk.kandnum ");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT  w.name as wahlkreisname, p.name as politikername, pa.name as parteiname, esg.anzahl FROM "
 				+ "erststimmengewinnernummern esg  , wahlkreis w, partei pa,  politiker p WHERE  w.jahr = " + jahrName
 				+ " AND p.politikernummer = esg.politiker  AND pa.parteinummer = esg.partei  AND esg.wahlkreisnummer = w.wahlkreisnummer");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT  w.name as wahlkreisname, p.name as politikername, pa.name as parteiname, esg.anzahl FROM "
 				+ "erststimmengewinnernummern esg , wahlkreis w, partei pa,  politiker p WHERE w.jahr = " + jahrName
 				+ " AND p.politikernummer = esg.politiker  AND pa.parteinummer = esg.partei  AND esg.wahlkreisnummer = w.wahlkreisnummer");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmengewinner AS SELECT w.name as wahlkreisname, pa.name as parteiname, s1.anzahl FROM zweitstimmen s1 , wahlkreis w, partei pa WHERE s1.jahr = "
 				+ jahrName
 				+ " AND w.jahr = "
 				+ jahrName
 				+ " AND pa.parteinummer = s1.partei"
 				+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = ( SELECT max(s2.anzahl) FROM zweitstimmen s2 WHERE s2.jahr = " + jahrName + " AND s2.wahlkreis = w.wahlkreisnummer)");
 
 		List<String> tableNames = Arrays.asList("erststimmengewinner", "zweitstimmengewinner");
 
 		List<List<String>> colNames = Arrays.asList(Arrays.asList("Wahlkreis", "Kandidatennummer", "Partei", "Stimmen"), Arrays.asList("Wahlkreis", "Partei", "Stimmen"));
 
 		for (int i = 0; i < tableNames.size(); i++) {
 
 			ArrayList<String> header = new ArrayList<String>();
 			header.add(tableNames.get(i));
 			for (int j = 0; j < colNames.get(i).size(); j++) {
 				header.add(colNames.get(i).get(j));
 			}
 
 			result.add(header);
 			collectFromQuery(result, tableNames.get(i), "ORDER BY wahlkreisname ASC");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Return the members of the Bundestag. Dependend on getSeatdistribution-
 	 * SQL View maxvotesuniquekand ; getWahlkreissieger -
 	 * erststimmengewinnernummern ; getUeberhangmandate - ueberhangerststimmen,
 	 * ueberhangzweitstimmen
 	 */
 
 	public ArrayList<ArrayList<String>> getMembers(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 
 		st.executeUpdate("CREATE  OR REPLACE VIEW maxvotesuniquekand5 AS (SELECT wahlkreis, max, min(kandnum) AS kandnum 	FROM maxvoteskand 	GROUP BY wahlkreis, max ORDER BY max);");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW ueberhangerststimmen2 AS ( WITH parteigewinner AS ( 	SELECT m.wahlkreis AS wahlkreis, d.partei AS partei "
 				+ "	FROM maxvotesuniquekand5 m left outer join (SELECT * FROM direktkandidat dk WHERE dk.jahr = '" + jahrName + "') d "
 				+ "	ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis), " + "blgewinner AS ( " + "	SELECT b.name AS bundesland,  p.wahlkreis AS wahlkreis,  p.partei AS partei "
 				+ "	FROM parteigewinner p JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahrName + "') w ON p.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) "
 				+ "SELECT  p.name AS parteiname,b.bundesland AS bundesland, COUNT(*) AS mandate " + "FROM blgewinner b JOIN partei p ON b.partei = p.parteinummer " + "GROUP BY b.bundesland, p.name);");
 
		st.executeUpdate("CREATE OR REPLACE VIEW ueberhangzweitstimmen2  AS ( " + "WITH ergebnisparteien AS ( " + "	SELECT z.parteiname, p.parteinummer "
				+ "	FROM zweitstimmenergebnis z JOIN partei p " + "	ON z.parteiname = p.name), " + "iterators AS ( "
				+ "	SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM zweitstimmenergebnis), 2) AS iterator ), " + "parteiiterators AS ( " + " 	SELECT * " + "	FROM ergebnisparteien e, iterators i "
				+ "	WHERE i.iterator <= 2 * (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = e.parteiname)), " + "parteibluebersicht AS ( "
				+ "	SELECT b.name AS bundesland, z.partei AS partei, SUM(z.anzahl)::numeric AS anzahl " + "	FROM  (SELECT * FROM zweitstimmen WHERE jahr = '" + jahrName
 				+ "' AND partei IN (SELECT parteinummer FROM ergebnisparteien)) z JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahrName + "') w  "
 				+ "	ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer " + "	GROUP BY z.partei, b.name), " + "parteiiteratorbl AS ( "
 				+ "	SELECT p1.parteiname AS parteiname, p1.parteinummer AS parteinummer, p2.bundesland AS bundesland, (p2.anzahl::numeric / p1.iterator::numeric) + RANDOM() AS itrergebnis "
 				+ "	FROM parteiiterators p1 JOIN parteibluebersicht p2 " + "	ON p1.parteinummer = p2.partei " + "   ORDER BY partei ASC, itrergebnis DESC), " + "partitionen AS ( "
 				+ "	SELECT p.parteiname AS parteiname, p.bundesland AS bundesland, p.itrergebnis AS itrergebnis, ROW_NUMBER() OVER (PARTITION BY p.parteiname ORDER BY p.itrergebnis DESC) AS rn "
 				+ "	FROM parteiiteratorbl p), " +
 
 				"filter AS ( " + "	SELECT * FROM partitionen p " + "	WHERE p.rn <= (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p.parteiname)) " +
 
 				"SELECT f.parteiname AS parteiname, f.bundesland AS bundesland, COUNT(*) as mandate " + "FROM filter f " + "GROUP BY f.parteiname, f.bundesland);"
 
 		);
 
 		st.executeUpdate("CREATE OR REPLACE VIEW listenkanidatohnedirektkandidaten AS("
 				+ "SELECT lk.partei , lk.bundesland , lk.politiker , lk.listenplatz - "
 				+ "(SELECT count(*) FROM erststimmengewinnernummern esg,listenkandidat lk2 WHERE lk2.jahr = "
 				+ jahrName
 				+ " AND lk.bundesland = lk2.bundesland AND lk.partei = lk2.partei AND esg.politiker = lk2.politiker AND lk2.listenplatz <  lk.listenplatz )  as listenplatz FROM listenkandidat lk WHERE lk.jahr = "
 				+ jahrName + " AND lk.politiker NOT IN (SELECT esg.politiker FROM erststimmengewinnernummern esg ))");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW mitglieder AS("
 				+ "WITH zsdiff AS ( "
				+ "SELECT zs.parteiname , zs.bundesland, CASE WHEN es.mandate IS NULL THEN zs.mandate ELSE zs.mandate - es.mandate END as mandate FROM ueberhangerststimmen2 zs LEFT JOIN ueberhangerststimmen2 es ON es.parteiname =  zs.parteiname AND es.bundesland =  zs.bundesland )"
 				+ "(SELECT p.name as politikername, pa.name as parteiname FROM  listenkanidatohnedirektkandidaten lk, politiker p , partei pa , bundesland bl , zsdiff WHERE "
 				+ "zsdiff.bundesland = bl.name  AND bl.bundeslandnummer = lk.bundesland AND zsdiff.parteiname = pa.name  AND pa.parteinummer = lk.partei AND p.politikernummer = lk.politiker "
 				+ " AND lk.listenplatz <= zsdiff.mandate ) UNION (SELECT politikername , parteiname FROM erststimmengewinner ))");
 
 		// Insert Table for each party
 
 		List<String> tableNames = new ArrayList<String>();
 		List<List<String>> colNames = new ArrayList<List<String>>();
 		List<String> parteiNames = new ArrayList<String>();
 
 		// Insert Table for each party
 		rs = st.executeQuery("SELECT parteiname FROM mitglieder  GROUP BY parteiname ORDER BY parteiname ;");
 		while (rs.next()) {
 			parteiNames.add(rs.getString(1));
 		}
 		int parteiNummer = 0;
 		for (String partei : parteiNames) {
 			parteiNummer++;
 			st.executeUpdate("CREATE OR REPLACE VIEW mitglieder" + parteiNummer + " AS( SELECT * FROM mitglieder WHERE parteiname = '" + partei + "' )");
 			tableNames.add("mitglieder" + parteiNummer);
 		}
 
 		for (int i = 0; i < parteiNames.size(); i++) {
 			colNames.add(Arrays.asList("Politiker", "Partei"));
 		}
 
 		for (int i = 0; i < tableNames.size(); i++) {
 
 			ArrayList<String> header = new ArrayList<String>();
 			header.add(tableNames.get(i));
 			for (int j = 0; j < colNames.get(i).size(); j++) {
 				header.add(colNames.get(i).get(j));
 			}
 
 			result.add(header);
 			collectFromQuery(result, tableNames.get(i), "ORDER BY politikername");
 		}
 
 		return result;
 	}
 
 	/**
 	 * Return overview of the Wahlkreise
 	 */
 	public ArrayList<ArrayList<String>> getWahlkreisOverview(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 		String wahlkreis = Integer.toString(Integer.parseInt(queryInput[1]));
 
 		try {
 
 			st.executeUpdate("DROP TABLE IF EXISTS wahlbeteiligungabsolut CASCADE;");
 			st.executeUpdate("CREATE TABLE wahlbeteiligungabsolut( anzahl integer , PRIMARY KEY (anzahl))WITH (OIDS=FALSE);");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlkreisname AS  SELECT name FROM wahlkreis WHERE jahr = " + jahrName + " AND wahlkreisnummer = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlberechtigtewahlkreis AS  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis);
 
 			st.executeUpdate("INSERT INTO wahlbeteiligungabsolut (SELECT sum(anzahl) FROM erststimmen  WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis + ")");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativ AS SELECT"
 					+ " CAST( CAST( CAST( (SELECT * FROM wahlbeteiligungabsolut)::float/ ( SELECT * FROM wahlberechtigtewahlkreis)::float * 100 as decimal(10,0) ) as text) || CAST( ' %' as text) as text) ;");
 
 			// st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidat AS "
 			// +
 			// "SELECT p.name FROM erststimmengewinnernummern e , politiker p WHERE p.politikernummer = e.politiker AND e.wahlkreisnummer = "
 			// + wahlkreis + " ORDER BY RANDOM() LIMIT 1");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidat AS "
 					+ "SELECT p.name FROM maxvotesuniquekand e , politiker p, direktkandidat d WHERE p.politikernummer = d.politiker AND d.kandidatennummer = e.kandnum AND e.wahlkreis = " + wahlkreis
 					+ " ORDER BY RANDOM() LIMIT 1");
 
 			// &maxvotesuniquekand(wahlkreis integer , max integer, kandnum
 			// integer ,
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilabsolut AS SELECT p.name as name, zs.anzahl as anzahl  FROM  partei p,zweitstimmen zs WHERE zs.jahr = " + jahrName
 					+ " AND zs.partei =  p.parteinummer AND zs.wahlkreis = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilrelativ AS  SELECT pa.name as name, pa.anzahl::float/(SELECT * FROM wahlbeteiligungabsolut)::float  as anteil FROM parteienanteilabsolut pa ");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsolutvorjahr AS SELECT  sum(anzahl) FROM erststimmen  WHERE jahr = " + Integer.toString(Integer.parseInt(jahrName) - 4)
 					+ " AND wahlkreis = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilabsolutvorjahr AS  SELECT p.name as name, zs.anzahl as anzahl FROM zweitstimmen zs,partei p WHERE zs.jahr = "
 					+ Integer.toString(Integer.parseInt(jahrName) - 4) + " AND zs.partei =  p.parteinummer  AND zs.wahlkreis = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilrelativvorjahr AS  SELECT pa.name as name,  pa.anzahl::float/(SELECT * FROM wahlbeteiligungabsolutvorjahr)::float as anteil FROM parteienanteilabsolutvorjahr pa ");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilveraenderung AS "
 					+ "SELECT pa1.name as name , pa1.anteil::float-pa2.anteil::float as anzahl FROM parteienanteilrelativ pa2, parteienanteilrelativvorjahr pa1 WHERE pa1.name = pa2.name");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteil AS  SELECT pa.name as name, pa.anzahl as absolut , CAST( CAST( CAST(pr.anteil  *100 as decimal(10,2)) as text) || CAST( ' %' as text) as text)as relativ , CASE WHEN pv.anzahl >= 0 THEN  CAST(CAST( '+' as text) ||  CAST( CAST(  pv.anzahl*100 as decimal(10,2)) as text) || CAST( ' %' as text) as text) ELSE  CAST( CAST( CAST(  pv.anzahl*100 as decimal(10,2)) as text) || CAST( ' %' as text) as text) END as veraenderung FROM parteienanteilabsolut pa ,parteienanteilrelativ pr, parteienanteilveraenderung pv WHERE pa.name = pr.name AND pv.name = pr.name ORDER BY absolut DESC ");
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilohnevorjahr AS  SELECT pa.name as name, pa.anzahl as absolut , CAST( CAST( CAST(pr.anteil  *100 as decimal(10,2)) as text) || CAST( ' %' as text) as text)as relativ   FROM parteienanteilabsolut pa ,parteienanteilrelativ pr WHERE pa.name = pr.name ORDER BY absolut DESC ");
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		List<List<String>> tableNames;
 		List<String> valueNames;
 		List<List<String>> colNames;
 
 		if (Integer.parseInt(queryInput[0]) >= 2009) {
 			tableNames = Arrays.asList(Arrays.asList("wahlkreisname", "wahlberechtigtewahlkreis", "wahlbeteiligungabsolut", "wahlbeteiligungrelativ", "erststimmengewinnerkandidat"),
 					Arrays.asList("parteienanteil"));
 			valueNames = Arrays.asList("Wahlkreis", "Wahlberechtigte", "Absolute Wahlbeteiligung", "Relative Wahlbeteiligung", "Erststimmengewinner");
 			colNames = Arrays.asList(Arrays.asList("Wahlkreisergebnisse", ""), Arrays.asList("Partei", "Zweitstimmen", "Relativ", "Veränderung"));
 
 		} else {
 			tableNames = Arrays.asList(Arrays.asList("wahlkreisname", "wahlberechtigtewahlkreis", "wahlbeteiligungabsolut", "wahlbeteiligungrelativ", "erststimmengewinnerkandidat"),
 					Arrays.asList("parteienanteilohnevorjahr"));
 			valueNames = Arrays.asList("Wahlkreis", "Wahlberechtigte", "Absolute Wahlbeteiligung", "Relative Wahlbeteiligung", "Erststimmengewinner");
 			colNames = Arrays.asList(Arrays.asList("Wahlkreisergebnisse", ""), Arrays.asList("Partei", "Zweitstimmen", "Relativ"));
 
 		}
 
 		for (int i = 0; i < tableNames.size(); i++) {
 			for (int k = 0; k < tableNames.get(i).size(); k++) {
 
 				if (k == 0) {
 					ArrayList<String> header = new ArrayList<String>();
 					header.add("Wahlkreisoverview");
 
 					for (int l = 0; l < colNames.get(i).size(); l++) {
 						header.add(colNames.get(i).get(l));
 					}
 
 					result.add(header);
 					result.add(new ArrayList<String>());
 				}
 
 				rs = st.executeQuery("SELECT * FROM " + tableNames.get(i).get(k) + ";");
 				ResultSetMetaData meta = rs.getMetaData();
 				int anzFields = meta.getColumnCount();
 
 				if (i == 0)
 					result.get(result.size() - 1).add(valueNames.get(k));
 
 				while (rs.next()) {
 					for (int j = 0; j < anzFields; j++) {
 						result.get(result.size() - 1).add(rs.getString(j + 1));
 					}
 					// add delimiter
 					result.get(result.size() - 1).add("$$");
 
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Knappster Sieger - maxvotesuniquekand
 	 */
 	public ArrayList<ArrayList<String>> getKnappsterSieger(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 		String wahlkreis = Integer.toString(Integer.parseInt(queryInput[1]));
 		try {
 
 			calculateTableWahlkreissieger("maxvotesuniquekand3");
 
 			st.executeUpdate("DROP TABLE IF EXISTS knappsteabstaendenummern CASCADE;");
 			st.executeUpdate("CREATE TABLE knappsteabstaendenummern(wahlkreis integer , politiker integer, partei integer ,  differenz integer, anderepartei integer, PRIMARY KEY (wahlkreis,partei,politiker,anderepartei))WITH (OIDS=FALSE);");
 
 			st.executeUpdate("DROP TABLE IF EXISTS knappsteabstaendeverlierernummern CASCADE;");
 			st.executeUpdate("CREATE TABLE knappsteabstaendeverlierernummern(wahlkreis integer , politiker integer, partei integer ,  differenz integer, anderepartei integer, PRIMARY KEY (wahlkreis,partei,politiker,anderepartei))WITH (OIDS=FALSE);");
 
 			st.executeUpdate("INSERT INTO knappsteabstaendenummern SELECT s1.wahlkreis as wahlkreis, d.politiker, d.partei , min(s1.max - s2.anzahl)  AS differenz,  d2.partei as anderepartei"
 					+ " FROM maxvotesuniquekand3 s1,erststimmen s2 , direktkandidat d, direktkandidat d2 WHERE s2.jahr = "
 					+ jahrName
 					+ " AND d.jahr = "
 					+ jahrName
 					+ " AND d2.jahr = "
 					+ jahrName
 					+ " AND s1.max - s2.anzahl >= 0 AND s1.wahlkreis = s2.wahlkreis AND d.kandidatennummer != s2.kandidatennummer  "
 					+ " AND s1.kandnum = d.kandidatennummer AND s1.wahlkreis = d.wahlkreis AND s2.kandidatennummer = d2.kandidatennummer AND s2.wahlkreis = d2.wahlkreis  GROUP BY s1.wahlkreis, d.politiker, d.partei, d2.partei");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinnernummerntop10 AS  SELECT kg.wahlkreis, kg.politiker, kg.partei ,kg.differenz, kg.anderepartei  FROM knappsteabstaendenummern kg WHERE (SELECT count(*) FROM knappsteabstaendenummern kg2 WHERE kg2.partei = kg.partei AND kg2.differenz < kg.differenz) < 10 ");
 
 			st.executeUpdate("INSERT INTO knappsteabstaendeverlierernummern SELECT s1.wahlkreis, d.politiker,  d.partei , max(s1.anzahl - s2.max) AS differenz, d2.partei as anderepartei "
 					+ " FROM erststimmen s1,maxvotesuniquekand3 s2 , direktkandidat d , direktkandidat d2 WHERE s1.anzahl - s2.max < 0 AND s1.wahlkreis = s2.wahlkreis AND s1.kandidatennummer != d2.kandidatennummer AND  s1.jahr = "
 					+ jahrName + " AND d.jahr = " + jahrName
 					+ " AND s1.kandidatennummer = d.kandidatennummer AND s2.kandnum = d2.kandidatennummer GROUP BY s1.wahlkreis, d.politiker, d.partei, d2.partei");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinnernummerntop10insg AS  SELECT * FROM knappstegewinnernummerntop10 UNION SELECT kg.wahlkreis, kg.politiker, kg.partei ,kg.differenz, kg.anderepartei  FROM knappsteabstaendeverlierernummern kg WHERE (SELECT count(*) FROM knappsteabstaendeverlierernummern kg2 WHERE kg2.partei = kg.partei AND kg.differenz < kg2.differenz) < 10-(SELECT count(*) FROM knappsteabstaendenummern kg3 WHERE kg3.partei = kg.partei) ");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinner AS SELECT w.name as wname, p.name as pname, pa.name as paname, k10.differenz, pa2.name FROM wahlkreis w,knappstegewinnernummerntop10insg k10, politiker p, partei pa,partei pa2 WHERE w.jahr = "
 					+ jahrName
 					+ " AND p.politikernummer = k10.politiker AND pa.parteinummer = k10.partei AND pa2.parteinummer = k10.anderepartei  AND w.wahlkreisnummer = k10.wahlkreis ORDER BY pa.name , k10.differenz, w.name");
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		List<String> tableNames = Arrays.asList("knappstegewinner");
 
 		List<List<String>> colNames = Arrays.asList(Arrays.asList("Wahlkreis", "Politiker", "Partei", "Differenz", "Konkurrenzpartei"));
 		for (int i = 0; i < tableNames.size(); i++) {
 
 			ArrayList<String> header = new ArrayList<String>();
 			header.add(tableNames.get(i));
 			for (int j = 0; j < colNames.get(i).size(); j++) {
 				header.add(colNames.get(i).get(j));
 			}
 
 			result.add(header);
 			collectFromQuery(result, tableNames.get(i));
 		}
 
 		return result;
 	}
 
 	/**
 	 * Wahlkreis Overview Erststimmen
 	 */
 	public ArrayList<ArrayList<String>> getOverview(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 		String wahlkreis = Integer.toString(Integer.parseInt(queryInput[1]));
 
 		try {
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlkreisnameeinzelstimmen AS  SELECT name FROM wahlkreis WHERE jahr = " + jahrName + " AND wahlkreisnummer = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlberechtigtewahlkreiseinzelstimmen AS  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsoluteinzelstimmen AS SELECT  sum(anzahl) FROM (SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme WHERE jahr = "
 					+ jahrName + " AND wahlkreis  = " + wahlkreis + " GROUP BY wahlkreis, kandidatennummer,jahr) AS stimmen  WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis);
 
 			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativeinzelstimmen AS SELECT"
 					+ " CAST( CAST( CAST( (SELECT * FROM wahlbeteiligungabsoluteinzelstimmen)::float/ ( SELECT * FROM wahlberechtigtewahlkreiseinzelstimmen)::float * 100 as decimal(10,0) ) as text) || CAST( ' %' as text) as text) ;");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidateinzelstimmen AS "
 					+ "SELECT p.name FROM erststimmengewinnernummern e , politiker p WHERE p.politikernummer = e.politiker AND e.wahlkreisnummer = " + wahlkreis + " ORDER BY RANDOM() LIMIT 1");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilabsoluteinzelstimme AS  SELECT p.name as name, count(*) as anzahl FROM zweitstimme zs, partei p WHERE zs.jahr = " + jahrName
 					+ " AND zs.partei =  p.parteinummer  AND zs.wahlkreis = " + wahlkreis + " GROUP BY p.name");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilrelativeinzelstimmen AS  SELECT pa.name as name, CAST( CAST( CAST( pa.anzahl::float/(SELECT * FROM wahlbeteiligungabsoluteinzelstimmen)::float *100 as decimal(10,2)) as text) || CAST( ' %' as text) as text) as anteil FROM parteienanteilabsoluteinzelstimme pa ");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilabsolutvorjahreinzelstimmen AS  SELECT p.name as name, count(*) as anzahl FROM zweitstimme zs , partei p WHERE zs.jahr = "
 					+ Integer.toString(Integer.parseInt(jahrName) - 4) + " AND zs.partei =  p.parteinummer  AND zs.wahlkreis = " + wahlkreis + " GROUP BY p.name");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilveraenderungeinzelstimmen AS "
 					+ "SELECT pa1.name as name , pa1.anzahl-pa2.anzahl as anzahl FROM parteienanteilabsoluteinzelstimme pa2, parteienanteilabsolutvorjahreinzelstimmen pa1 WHERE pa1.name = pa2.name");
 
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteileinzelstimmen AS  SELECT pa.name as name, pa.anzahl as absolut , pr.anteil as relativ , pv.anzahl as veraenderung FROM parteienanteilabsoluteinzelstimme pa ,parteienanteilrelativeinzelstimmen pr, parteienanteilveraenderungeinzelstimmen pv WHERE pa.name = pr.name AND pv.name = pr.name ORDER BY absolut DESC ");
 			st.executeUpdate("CREATE OR REPLACE VIEW parteienanteilohnevorjahreinzelstimmen AS  SELECT pa.name as name, pa.anzahl as absolut , pr.anteil as relativ  FROM parteienanteilabsoluteinzelstimme pa ,parteienanteilrelativeinzelstimmen pr WHERE pa.name = pr.name ORDER BY absolut DESC ");
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		List<List<String>> tableNames;
 		List<String> valueNames;
 		List<List<String>> colNames;
 
 		if (Integer.parseInt(queryInput[0]) >= 2009) {
 			tableNames = Arrays.asList(Arrays.asList("wahlkreisnameeinzelstimmen", "wahlberechtigtewahlkreiseinzelstimmen", "wahlbeteiligungabsoluteinzelstimmen",
 					"wahlbeteiligungrelativeinzelstimmen", "erststimmengewinnerkandidateinzelstimmen"), Arrays.asList("parteienanteileinzelstimmen"));
 			valueNames = Arrays.asList("Wahlkreis", "Wahlberechtigte", "Absolute Wahlbeteiligung", "Relative Wahlbeteiligung", "Erststimmengewinner");
 			colNames = Arrays.asList(Arrays.asList("Wahlkreisergebnisse", ""), Arrays.asList("Partei", "Zweitstimmen", "Relativ", "Ver�nderung"));
 
 		} else {
 			tableNames = Arrays.asList(Arrays.asList("wahlkreisnameeinzelstimmen", "wahlberechtigtewahlkreiseinzelstimmen", "wahlbeteiligungabsoluteinzelstimmen",
 					"wahlbeteiligungrelativeinzelstimmen", "erststimmengewinnerkandidateinzelstimmen"), Arrays.asList("parteienanteilohnevorjahreinzelstimmen"));
 			valueNames = Arrays.asList("Wahlkreis", "Wahlberechtigte", "Absolute Wahlbeteiligung", "Relative Wahlbeteiligung", "Erststimmengewinner");
 			colNames = Arrays.asList(Arrays.asList("Wahlkreisergebnisse", ""), Arrays.asList("Partei", "Zweitstimmen", "Relativ"));
 
 		}
 
 		for (int i = 0; i < tableNames.size(); i++) {
 			for (int k = 0; k < tableNames.get(i).size(); k++) {
 
 				if (k == 0) {
 					ArrayList<String> header = new ArrayList<String>();
 					header.add("Wahlkreisoverview");
 
 					for (int l = 0; l < colNames.get(i).size(); l++) {
 						header.add(colNames.get(i).get(l));
 					}
 
 					result.add(header);
 					result.add(new ArrayList<String>());
 				}
 
 				rs = st.executeQuery("SELECT * FROM " + tableNames.get(i).get(k) + ";");
 				ResultSetMetaData meta = rs.getMetaData();
 				int anzFields = meta.getColumnCount();
 
 				if (i == 0)
 					result.get(result.size() - 1).add(valueNames.get(k));
 
 				while (rs.next()) {
 					for (int j = 0; j < anzFields; j++) {
 						result.get(result.size() - 1).add(rs.getString(j + 1));
 					}
 					// add delimiter
 					result.get(result.size() - 1).add("$$");
 
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Request vote forms (tables)
 	 */
 	public ArrayList<ArrayList<String>> requestVote(String[] queryInput) throws SQLException {
 
 		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
 		String jahrName = Integer.toString(Integer.parseInt(queryInput[0]));
 		String wahlkreis = Integer.toString(Integer.parseInt(queryInput[1]));
 
 		st.executeUpdate("CREATE OR REPLACE VIEW erststimmeliste AS ( SELECT p.name AS kandidatenname, t.name AS parteiname, p.politikernummer FROM politiker p, direktkandidat d , partei t WHERE d.jahr = "
 				+ jahrName + " AND p.politikernummer = d.politiker AND t.parteinummer = d.partei AND d.wahlkreis = " + wahlkreis + ")");
 
 		// st.executeUpdate("CREATE OR REPLACE VIEW erststimmeliste AS ( " +
 		// "SELECT p.name AS kandidatenname, t.name AS parteiname, p.politikernummer "
 		// + "FROM "
 		// + "	(SELECT * FROM direktkandidat WHERE jahr = " + jahrName +
 		// " AND wahlkreis = " + wahlkreis + ") d " +
 		// "	JOIN politiker p ON p.politikernummer = d.politiker "
 		// + "   JOIN partei t ON d.partei = t.parteinummer);");
 
 		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmeliste AS ( " + "SELECT l.listenplatz AS listenplatz, t.name AS parteiname, p.name AS kandidatenname, p.politikernummer " + "FROM "
 				+ "	(SELECT listenplatz, partei, politiker, bundesland " + "    FROM listenkandidat WHERE jahr = " + jahrName + ") l " + "	JOIN politiker p ON p.politikernummer = l.politiker "
 				+ "	JOIN (SELECT * FROM wahlkreis WHERE jahr = " + jahrName + ") w ON w.bundesland = l.bundesland " + "	JOIN partei t ON t.parteinummer = l.partei " + "WHERE wahlkreisnummer = "
 				+ wahlkreis + " " + "ORDER BY l.listenplatz);");
 
 		List<String> tableNames = Arrays.asList("erststimmeliste", "zweitstimmeliste");
 
 		List<List<String>> colNames = Arrays.asList(Arrays.asList("Kandidatenname", "Parteiname", "Politikernummer"), Arrays.asList("Listenplatz", "Parteiname", "Kandidatenname", "Politikernummer"));
 
 		for (int i = 0; i < tableNames.size(); i++) {
 
 			ArrayList<String> header = new ArrayList<String>();
 			header.add(tableNames.get(i));
 			for (int j = 0; j < colNames.get(i).size(); j++) {
 				header.add(colNames.get(i).get(j));
 			}
 
 			result.add(header);
 			collectFromQuery(result, tableNames.get(i));
 		}
 
 		return result;
 	}
 
 	/**
 	 * Submit vote forms (tables)
 	 */
 	public Boolean submitVote(String[] queryInput, ArrayList<ArrayList<String>> selections, Connection conn) throws SQLException {
 
 		String jahrName = Integer.toString((Integer.parseInt(queryInput[0]) + 781924902) ^ 781924902);
 
 		// Wahlkreis nr. where voting took place.
 		String wahlkreis = Integer.toString((Integer.parseInt(queryInput[1]) + 781924902) ^ 781924902);
 
 		// tan
 		int tan = (Integer.parseInt(queryInput[2]) + 781924902) ^ 781924902;
 
 		PreparedStatement updateStatement = null;
 		long random = (long) (1 + (Long.MAX_VALUE - 1) * Math.random());
 		updateStatement = conn.prepareStatement("UPDATE wahltan SET voting = ?  WHERE tan = ?;");
 		updateStatement.setLong(1, random);
 		updateStatement.setInt(2, tan);
 		updateStatement.executeUpdate();
 
 		updateStatement = conn.prepareStatement("SELECT  valid FROM wahltan WHERE tan = ? AND voting = ?;");
 		updateStatement.setInt(1, tan);
 		updateStatement.setLong(2, random);
 
 		rs = updateStatement.executeQuery();
 
 		ResultSetMetaData meta = rs.getMetaData();
 		int anzFields = meta.getColumnCount();
 		String validReturned = "";
 		if (anzFields == 1 && rs.next()) {
 			validReturned = rs.getString(1);
 		}
 
 		if (validReturned.equals("t") || validReturned.equals("TRUE") || validReturned.equals("true") || validReturned.equals("1")) {
 
 			if (selections.size() > 0) {
 				ArrayList<String> currentSelection = selections.get(0);
 				String erststimmeName = currentSelection.get(0);
 				String erststimmeParteiName = currentSelection.get(1);
 				String erststimmePolitiker = currentSelection.get(2);
 				int direktkandidatennummer = 0;
 				int erststimmeWahlkreis = 0;
 
 				currentSelection = selections.get(1);
 				String zweitstimmeName = currentSelection.get(0);
 				String zweitstimmeParteiName = currentSelection.get(1);
 				String zweitstimmePolitiker = currentSelection.get(2);
 				int zweitstimmeBundesland = 0;
 				int zweitstimmeWahlkreis = 0;
 				int zweitstimmePartei = 0;
 
 				// System.out.println("Updating: year: " + jahrName + ", wkNr: "
 				// + wahlkreis + ", name: " + erststimmeName + ", party: " +
 				// erststimmeParteiName + ", politicianNr " +
 				// erststimmePolitiker);
 				// System.out.println("Updating: year: " + jahrName + ", wkNr: "
 				// + wahlkreis + ", name: " + zweitstimmeName + ", party: " +
 				// zweitstimmeParteiName + ", politicianNr "
 				// + zweitstimmePolitiker);
 
 				if (!erststimmeName.equals("")) {
 
 					updateStatement = conn
 							.prepareStatement("SELECT d.kandidatennummer FROM direktkandidat d, partei pa WHERE d.jahr = ? AND d.politiker = ? AND d.partei = pa.parteinummer AND pa.name = ? AND d.wahlkreis = ?;");
 
 					updateStatement.setInt(1, Integer.parseInt(jahrName));
 					updateStatement.setInt(2, Integer.parseInt(erststimmePolitiker));
 					updateStatement.setString(3, erststimmeParteiName);
 					updateStatement.setInt(4, Integer.parseInt(wahlkreis));
 
 					rs = updateStatement.executeQuery();
 					meta = rs.getMetaData();
 					anzFields = meta.getColumnCount();
 				}
 
 				if (erststimmeName.equals("") || (anzFields == 1 && rs.next())) {
 
 					if (!erststimmeName.equals("")) {
 						direktkandidatennummer = Integer.parseInt(rs.getString(1));
 						erststimmeWahlkreis = Integer.parseInt(wahlkreis);
 					}
 					if (!erststimmeName.equals("")) {
 						updateStatement = conn.prepareStatement("SELECT w.bundesland, pa.parteinummer  FROM wahlkreis w, partei pa WHERE  w.jahr = ? AND  w.wahlkreisnummer = ? AND pa.name = ?;");
 						updateStatement.setInt(1, Integer.parseInt(jahrName));
 						updateStatement.setInt(2, Integer.parseInt(wahlkreis));
 						updateStatement.setString(3, zweitstimmeParteiName);
 						rs = updateStatement.executeQuery();
 						meta = rs.getMetaData();
 						anzFields = meta.getColumnCount();
 						System.out.println(anzFields);
 					}
 
 					if (zweitstimmeName.equals("") || anzFields == 2 && rs.next()) {
 						if (!erststimmeName.equals("")) {
 							zweitstimmeBundesland = Integer.parseInt(rs.getString(1));
 							zweitstimmePartei = Integer.parseInt(rs.getString(2));
 							zweitstimmeWahlkreis = Integer.parseInt(wahlkreis);
 						}
 						conn.setAutoCommit(false);
 						if (!erststimmeName.equals("")) {
 							updateStatement = conn.prepareStatement("INSERT INTO erststimme VALUES (?,?,?,?);");
 							updateStatement.setInt(1, Integer.parseInt(jahrName));
 							updateStatement.setInt(2, tan);
 							updateStatement.setInt(3, direktkandidatennummer);
 							updateStatement.setInt(4, erststimmeWahlkreis);
 						}
 						updateStatement.executeUpdate();
 						if (!erststimmeName.equals("")) {
 							updateStatement = conn.prepareStatement("INSERT INTO zweitstimme VALUES (?,?,?,?);");
 							updateStatement.setInt(1, Integer.parseInt(jahrName));
 							updateStatement.setInt(2, tan);
 							updateStatement.setInt(3, zweitstimmePartei);
 							updateStatement.setInt(4, zweitstimmeWahlkreis);
 							updateStatement.setInt(4, zweitstimmeBundesland);
 							updateStatement.executeUpdate();
 						}
 						updateStatement = conn.prepareStatement("UPDATE wahltan SET voting = ? , valid = false WHERE tan = ? ;");
 						updateStatement.setLong(1, 0);
 						updateStatement.setInt(2, tan);
 						updateStatement.executeUpdate();
 
 						conn.commit();
 						conn.setAutoCommit(true);
 
 						return true;
 					} else {
 						throw new SQLException("Interner Fehler");
 					}
 				} else {
 					throw new SQLException("Interner Fehler");
 				}
 			}
 
 		}
 		return false;
 	}
 
 	// Get data from ResultSet into required table format
 
 	private void collectFromQuery(ArrayList<ArrayList<String>> result, String tableName, String orderBy) throws SQLException {
 
 		result.add(new ArrayList<String>());
 		rs = st.executeQuery("SELECT * FROM " + tableName + " " + orderBy + ";");
 		ResultSetMetaData meta = rs.getMetaData();
 		int anzFields = meta.getColumnCount();
 		while (rs.next()) {
 			for (int i = 0; i < anzFields; i++) {
 				result.get(result.size() - 1).add(rs.getString(i + 1));
 			}
 			// add delimiter
 			result.get(result.size() - 1).add("$$");
 		}
 	}
 
 	private void collectFromQuery(ArrayList<ArrayList<String>> result, String tableName) throws SQLException {
 		collectFromQuery(result, tableName, "");
 	}
 
 }
