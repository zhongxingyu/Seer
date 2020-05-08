 package testbw.setup;
 
 import java.sql.*;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import au.com.bytecode.opencsv.CSVReader;
 import testbw.util.DBManager;
 import testbw.util.InputDirectory;
 
 public class BWSetupDatabase {
 
 	// Input CSV Dateien ------------------------------------------------------
 	private static String wahlbewerber05Pfad = InputDirectory.wahlbewerber05Pfad;
 	private static String wahlbewerber09Pfad = InputDirectory.wahlbewerber09Pfad;
 	private static String daten05Pfad = InputDirectory.daten05Pfad;
 	private static String daten09Pfad = InputDirectory.daten09Pfad;
 
 	// Datenbankverbindungsdaten ----------------------------------------------
 	//private Connection conn = null;
 	private Statement st = null;
 	private ResultSet rs = null;
 	private DBManager manager = null;
 
 	public BWSetupDatabase(Statement st, DBManager manager){
 		//this.conn = conn;
 		this.st = st;
 		this.manager = manager;
 	}
 
 	/**
 	 * Legt Tabellen, Trigger, und weitere statische Daten an.
 	 */
 	public void setupDatabase() throws FileNotFoundException, IOException, SQLException  {
 
 		// Setup Bundeslaender ------------------------------------------------
 		SortedMap<String, String> bundeslaenderAbkuerzung = new TreeMap<String, String>();
 		bundeslaenderAbkuerzung.put("Baden-Wrttemberg", "BW");
 		bundeslaenderAbkuerzung.put("Bayern", "BY");
 		bundeslaenderAbkuerzung.put("Berlin", "BE");
 		bundeslaenderAbkuerzung.put("Brandenburg", "BB");
 		bundeslaenderAbkuerzung.put("Bremen", "HB");
 		bundeslaenderAbkuerzung.put("Hamburg", "HH");
 		bundeslaenderAbkuerzung.put("Hessen", "HE");
 		bundeslaenderAbkuerzung.put("Mecklenburg-Vorpommern", "MV");
 		bundeslaenderAbkuerzung.put("Niedersachsen", "NI");
 		bundeslaenderAbkuerzung.put("Nordrhein-Westfalen", "NW");
 		bundeslaenderAbkuerzung.put("Rheinland-Pfalz", "RP");
 		bundeslaenderAbkuerzung.put("Saarland", "SL");
 		bundeslaenderAbkuerzung.put("Sachsen", "SN");
 		bundeslaenderAbkuerzung.put("Sachsen-Anhalt", "ST");
 		bundeslaenderAbkuerzung.put("Schleswig-Holstein", "SH");
 		bundeslaenderAbkuerzung.put("Thringen", "TH");
 
 		// Verbindung zu CSV Daten aufbauen -----------------------------------
 		CSVReader readerWahlbewerber[] = new CSVReader[2];
 		CSVReader readerErgebnis[] = new CSVReader[2];
 
 
 		readerWahlbewerber[0] = new CSVReader(new BufferedReader(
 				new InputStreamReader(new FileInputStream(
 						wahlbewerber05Pfad), "UTF-8")), ';');
 		readerWahlbewerber[1] = new CSVReader(new BufferedReader(
 				new InputStreamReader(new FileInputStream(
 						wahlbewerber09Pfad), "UTF-8")), ';');
 
 		readerErgebnis[0] = new CSVReader(new BufferedReader(
 				new InputStreamReader(new FileInputStream(daten05Pfad),
 						"UTF-8")), ';');
 		readerErgebnis[1] = new CSVReader(new BufferedReader(
 				new InputStreamReader(new FileInputStream(daten09Pfad),
 						"UTF-8")), ';');
 
 		String[] readLineErgebnis;
 		String[] readLineWahlbewerber;
 
 		// Tabellen loeschen, Reihenfolge relevant-------------------------
 		st.executeUpdate("DROP TABLE IF EXISTS erststimmen CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS zweitstimmen CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS erststimme CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS zweitstimme CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS listenkandidat CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS direktkandidat CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS wahlberechtigte CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS wahlkreis CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS politiker CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS partei CASCADE;");
 		st.executeUpdate("DROP TABLE IF EXISTS bundesland CASCADE;");
 
 		// Tabellen anlegen, Reihenfolge relevant--------------------------
 		st.executeUpdate("CREATE TABLE bundesland(bundeslandnummer integer , name text NOT NULL, abkuerzung text,  PRIMARY KEY (bundeslandnummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE partei( parteinummer integer,  name text , PRIMARY KEY (parteinummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE politiker(  politikernummer integer, name text , PRIMARY KEY (politikernummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE wahlkreis( jahr integer, wahlkreisnummer integer, name text,  bundesland integer , PRIMARY KEY (jahr,wahlkreisnummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE wahlberechtigte(jahr integer, wahlkreis integer UNIQUE,wahlberechtigte integer , PRIMARY KEY (jahr,wahlkreis))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE direktkandidat(jahr integer,kandidatennummer integer UNIQUE, politiker integer, partei int, wahlkreis integer, PRIMARY KEY (jahr,kandidatennummer)) WITH ( OIDS=FALSE );");
 		st.executeUpdate("CREATE TABLE listenkandidat(jahr integer, partei integer, bundesland int, listenplatz integer, politiker integer, PRIMARY KEY (jahr,partei,bundesland,listenplatz)) WITH ( OIDS=FALSE );");
 		st.executeUpdate("CREATE TABLE erststimme( jahr integer, stimmzettelnummer integer, kandidatennummer integer, wahlkreis integer, PRIMARY KEY (jahr,stimmzettelnummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE zweitstimme( jahr integer, stimmzettelnummer integer, partei integer, wahlkreis integer, bundesland integer, PRIMARY KEY (jahr,stimmzettelnummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE erststimmen( jahr integer, wahlkreis integer, kandidatennummer integer UNIQUE, anzahl integer, PRIMARY KEY (jahr,kandidatennummer))WITH (OIDS=FALSE);");
 		st.executeUpdate("CREATE TABLE zweitstimmen( jahr integer, wahlkreis integer, partei integer, anzahl integer, PRIMARY KEY (jahr,partei,wahlkreis))WITH (OIDS=FALSE);");
 
 		// Tabelle Bundeslaender fuellen-----------------------------------
 		int bundeslandnummer;
 		bundeslandnummer = 1;
 		for (Map.Entry<String, String> entry : bundeslaenderAbkuerzung
 				.entrySet()) {
 			st.executeUpdate("INSERT INTO bundesland VALUES ("
 					+ bundeslandnummer + ",'"
 					+ (String) entry.getKey() + "','"
 					+ (String) entry.getValue() + "');");
 			bundeslandnummer++;
 		}
 
 		// Wahlkreise anlegen----------------------------------------------
 		// Anfangszeilen Ueberspringen
 		// Statische Daten und Ergebnisse eintragen
 		HashSet<Partei> parteien = new HashSet<Partei>();
 		HashSet<Politiker> politiker = new HashSet<Politiker>();
 		HashSet<Direktkandidat> direktkandidaten = new HashSet<Direktkandidat>();
 		HashSet<Listenkandidat> listenkandidaten = new HashSet<Listenkandidat>();
 
 		int kandidatennummer = 1;
 
 		for (int jahr = 0; jahr < 2; jahr++) {
 			String jahrName = Integer.toString(2005 + jahr * 4);
 
 			// Read Ergebnis ----------------------------------------------
 			while ((readLineErgebnis = readerErgebnis[jahr]
 					.readNext()) != null) {
 				// In Datei stehen Wahlkreise vor Bundeslandnamen
 				// Deswegen zwischenspeichern der Wahlkreise
 				if (!readLineErgebnis[0].trim().equals("")
 						&& !readLineErgebnis[1].trim().equals("0")
 						&& !readLineErgebnis[1].trim().equals("")
 						&& !readLineErgebnis[2].trim().equals("")
 						&& !(Integer.parseInt(readLineErgebnis[1]) > 900)) {
 
 					String bundesland = readLineErgebnis[0];
 					bundeslandnummer = Integer.parseInt(manager.getQueryResult(
 							//st, 
 							rs,
 							"SELECT bundeslandnummer FROM bundesland WHERE name = '"
 									+ bundesland
 									+ "' OR abkuerzung = '"
 									+ bundesland + "' ;"));
 
 					// Neuen Wahlkreis hinzufuegen
 					int wahlkreisnummer = Integer
 							.parseInt(readLineErgebnis[1]);
 					String wahlkreisname = readLineErgebnis[2];
 					st.executeUpdate("INSERT INTO wahlkreis VALUES ("
 							+ jahrName
 							+ ","
 							+ wahlkreisnummer
 							+ ",'"
 							+ wahlkreisname
 							+ "',"
 							+ bundeslandnummer + ");");
 				}
 			}
 			readerErgebnis[jahr].close();
 
 			// Anfangszeilen ueberspringen
 			int skipLinesWahlbewerber = 1; // Anzahl Zeilen
 			for (int j = 1; j <= skipLinesWahlbewerber; j++) {
 				readerWahlbewerber[jahr].readNext();
 			}
 
 			// Read Wahlbewerber-------------------------------------------
 			while ((readLineWahlbewerber = readerWahlbewerber[jahr]
 					.readNext()) != null) {
 				if (readLineWahlbewerber.length >= 1) {
 
 					int parteinummer = 0;
 					int politikernummer = 0;
 					String parteiname = "";
 					String wahlkreisnummer = "";
 					String bundesland = "";
 					String listenplatz = "";
 					String politikername = "";
 					String politikervorname = "";
 
 					switch (jahr) {
 					case 0:
 						parteiname = readLineWahlbewerber[7].trim();
 						wahlkreisnummer = readLineWahlbewerber[8].trim();
 						bundesland = readLineWahlbewerber[9].trim();
 						listenplatz = readLineWahlbewerber[10].trim();
 						politikername = readLineWahlbewerber[0];
 						String[] temp;
 						temp = politikername.split(",");
 						politikername = temp[0];
 						politikervorname = temp[1];
 						break;
 					case 1:
 						parteiname = readLineWahlbewerber[3].trim();
 						wahlkreisnummer = readLineWahlbewerber[4].trim();
 						bundesland = readLineWahlbewerber[5].trim();
 						listenplatz = readLineWahlbewerber[6].trim();
 						politikername = readLineWahlbewerber[0];
 						politikervorname = readLineWahlbewerber[1];
 						break;
 					}
 
 					// Politiker hinzufuegen
 					if (!politikername.equals("")) {
 						boolean exists = false;
 						for (Politiker actPolitiker : politiker) {
 							if (actPolitiker.name.equals(parteiname)) {
 								politikernummer = actPolitiker.politikernummer;
 								exists = true;
 								break;
 							}
 						}
 						if (!exists) {
 							politikernummer = politiker.size() + 1;
 							politiker.add(new Politiker(politikernummer, 
 									politikername, politikervorname));
 						}
 					}
 
 					// Parteien hinzufuegen
 					if (!parteiname.equals("")) {
 						boolean exists = false;
 						for (Partei actPartei : parteien) {
 							if (actPartei.name.equals(parteiname)) {
 								parteinummer = actPartei.parteinummer;
 								exists = true;
 								break;
 							}
 						}
 						if (!exists) {
 							parteinummer = parteien.size() + 1;
 							Partei partei = new Partei(
 									parteinummer, parteiname);
 							parteien.add(partei);
 						}
 					}
 
 					// Direktkandidaten hinzufuegen
 					if (!wahlkreisnummer.equals("")) {
 						direktkandidaten.add(new Direktkandidat(
 								2005 + jahr * 4, kandidatennummer,
 								parteinummer, politikernummer,
 								Integer.parseInt(wahlkreisnummer)));
 						kandidatennummer++;
 					}
 
 					// Listenkandidaten hinzufuegen
 					if (!bundesland.equals("")) {
 						bundeslandnummer = Integer.parseInt(manager.getQueryResult(
 								//st, 
 								rs,
 								"SELECT bundeslandnummer FROM bundesland WHERE name = '"
 										+ bundesland
 										+ "' OR abkuerzung = '"
 										+ bundesland
 										+ "' ;"));
 
 						listenkandidaten.add(new Listenkandidat(
 								2005 + jahr * 4, parteinummer,
 								bundeslandnummer, Integer.parseInt(listenplatz),
 								politikernummer));
 					}
 				}
 			} // end read Wahlbewerber
 
 		} // end for each jahr
 
 		// Ungltige und brige Parteien auflsen -------------------------
 		int parteinummer = parteien.size() + 1;
 		int politikernummer = politiker.size() + 1;
 		parteien.add(new Partei(0, "Ungltige"));
 		parteien.add(new Partei(parteinummer, "brige"));
 		politiker.add(new Politiker(politikernummer, "Politiker", "briger"));
 
 		// Daten in Tabellen schreiben ------------------------------------
 		Iterator<Politiker> itrPolitiker = politiker.iterator();
 		while (itrPolitiker.hasNext()) {
 			st.executeUpdate("INSERT INTO politiker VALUES ("
 					+ itrPolitiker.next() + ");");
 		}
 
 		Iterator<Partei> itrPartei = parteien.iterator();
 		while (itrPartei.hasNext()) {
 			st.executeUpdate("INSERT INTO partei VALUES ("
 					+ itrPartei.next() + ");");
 		}
 
 		Iterator<Direktkandidat> itrDirektkandidat = direktkandidaten
 				.iterator();
 		while (itrDirektkandidat.hasNext()) {
 			st.executeUpdate("INSERT INTO direktkandidat VALUES ("
 					+ itrDirektkandidat.next() + ");");
 		}
 
 		Iterator<Listenkandidat> itrListenkandidat = listenkandidaten
 				.iterator();
 		while (itrListenkandidat.hasNext()) {
 			st.executeUpdate("INSERT INTO listenkandidat VALUES ("
 					+ itrListenkandidat.next() + ");");
 		}
 
 		for (int jahr = 0; jahr < 2; jahr++) {
 			String jahrName = Integer.toString(2005 + jahr * 4);
 			// Direktkandidaten der brigen Parteien generieren
 			int anzahlWahlkreise = Integer.parseInt(manager.getQueryResult(
 					//st, 
 					rs,
 					"SELECT count(*) FROM wahlkreis WHERE jahr = "
 							+ jahrName + ";"));
 			for (int j = 0; j < anzahlWahlkreise; j++) {
 				st.executeUpdate("INSERT INTO direktkandidat VALUES ("
 						+ jahrName
 						+ ","
 						+ kandidatennummer
 						+ ","
 						+ politikernummer
 						+ ","
 						+ parteinummer
 						+ "," + (j + 1) + ");");
 				kandidatennummer++;
 			}
 		}
 
 
 		// --------------------------------------------------------------------
 		// --------------------------------------------------------------------
 
 
 		//-- SitzeProJahr
 		//-- Typ: vordefiniert
 		//-- Verweist auf: -
 		st.executeUpdate("DROP TABLE IF EXISTS sitzeprojahr CASCADE;");
 		st.executeUpdate("CREATE TABLE sitzeprojahr ( jahr INTEGER PRIMARY KEY, sitze INTEGER NOT NULL);");
 		st.executeUpdate("INSERT INTO sitzeprojahr VALUES ('2013', '700');");
		st.executeUpdate("INSERT INTO sitzeprojahr VALUES ('2005', '598');");
		st.executeUpdate("INSERT INTO sitzeprojahr VALUES ('2008', '598');");
 
 		//-- Divisoren
 		//-- Typ: vordefiniert
 		//-- Verweist auf: -
 		st.executeUpdate("DROP TABLE IF EXISTS divisoren CASCADE;");
 		st.executeUpdate("CREATE TABLE divisoren ( div INTEGER PRIMARY KEY);");
 
 
 		//-- ItrErgebnisse
 		//-- Typ: vordefiniert
 		//-- Verweist auf: -
 		st.executeUpdate("DROP TABLE IF EXISTS itrergebnisse CASCADE;");
 		st.executeUpdate("CREATE TABLE itrergebnisse ( partei CHARACTER VARYING NOT NULL, anzahl NUMERIC NOT NULL, PRIMARY KEY(partei, anzahl));");
 
 		// Nun Teil der Auswertung wegen der Abhaengigkeit an 'stimmenpropartei', die nicht im Voraus berechnet werden darf.
 		/*//-- Trigger Divisoren -> ItrErgebnisse
 		//-- Typ: vordefiniert?
 		//-- Verweist auf: stimmenpropartei
 		st.executeUpdate("DROP TRIGGER IF EXISTS berechne_ItrErgebnisse ON divisoren CASCADE;");
 		st.executeUpdate("DROP FUNCTION IF EXISTS berechneitr() CASCADE;");
 		st.executeUpdate("CREATE OR REPLACE FUNCTION berechneitr() RETURNS trigger AS $$ " +
 				"BEGIN " +
 				"  INSERT INTO itrergebnisse (SELECT partei, (anzahl / NEW.div::float8) AS anzahl FROM stimmenpropartei); " +
 				"  RETURN NEW; " +
 				"END; " + 
 				"$$ LANGUAGE plpgsql;");
 
 		st.executeUpdate("CREATE TRIGGER berechne_ItrErgebnisse " +
 				"AFTER INSERT ON divisoren " +
 				"FOR EACH ROW " + 
 				"EXECUTE PROCEDURE berechneitr();");
 
 
 		//-- Load: Divisoren
 		//-- Typ: Vorberechnung?
 		//-- Verweist auf: divisoren, sitzeprojahr
 		st.executeUpdate("INSERT INTO divisoren ( SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM sitzeprojahr), 2));");
 		 */
 	}
 }
