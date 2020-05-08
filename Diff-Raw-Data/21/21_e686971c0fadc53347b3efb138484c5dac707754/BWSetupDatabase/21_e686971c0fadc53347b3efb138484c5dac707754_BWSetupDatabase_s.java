 package bundestagswahl.setup;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 public class BWSetupDatabase {
 
 	public static String wahlbewerber05Pfad = "csv\\wahlbewerber2005.csv";
 	public static String wahlbewerber09Pfad = "csv\\wahlbewerber2009.csv";
 	public static String ergebnis05Pfad = "csv\\StruktBtwkr2005.csv";
 	public static String ergebnis09Pfad = "csv\\StruktBtwkr2009.csv";
 
 	// Anpassungen in den CSV Dateien:
 	// Alle UTF-8 Format
 	// wahlergebniss2009.csv: VIOLETTEN zu DIE VIOLETTEN wie in kerg umbenannt
 	// Volksabst. zu Volksabstimmung wie in kerg umbenannt
 	// Tierschutzpartei -"-
 
 	public static void main(String[] args) {
 		System.out.println("Setup Database started");
 
 		SortedMap<String, String> bundeslaenderAbkuerzung = new TreeMap<String, String>();
 		// Bundesländer Nummerierung
 		bundeslaenderAbkuerzung.put("Baden-Württemberg", "BW");
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
 		bundeslaenderAbkuerzung.put("Thüringen", "TH");
 
 		try {
 			CSVReader readerWahlbewerber[] = new CSVReader[2];
 			readerWahlbewerber[0] = new CSVReader(new BufferedReader(
 					new InputStreamReader(new FileInputStream(
 							wahlbewerber05Pfad), "UTF-8")), ';');
 			readerWahlbewerber[1] = new CSVReader(new BufferedReader(
 					new InputStreamReader(new FileInputStream(
 							wahlbewerber09Pfad), "UTF-8")), ';');
 
 			CSVReader readerErgebnis[] = new CSVReader[2];
 			readerErgebnis[0] = new CSVReader(new BufferedReader(
 					new InputStreamReader(new FileInputStream(ergebnis05Pfad),
 							"UTF-8")), ';');
 			readerErgebnis[1] = new CSVReader(new BufferedReader(
 					new InputStreamReader(new FileInputStream(ergebnis09Pfad),
 							"UTF-8")), ';');
 
 			String[] readLineErgebnis;
 			String[] readLineWahlbewerber;
 
 			try {
 				Class.forName("org.postgresql.Driver");
 
 				// Datenbankverbindung aufbauen
 				String url = "jdbc:postgresql://localhost/Bundestagswahl?user=user&password=1234";
 				Connection conn;
 				Statement st;
 				ResultSet rs = null;
 				try {
 					conn = DriverManager.getConnection(url);
 					st = conn.createStatement();
 
 					// // Database löschen
 					// try {
 					// st.executeUpdate("DROP DATABASE \"Bundestagswahl\";");
 					// } catch (SQLException e) {
 					// e.printStackTrace();
 					// }
 					// st.executeUpdate("CREATE DATABASE \"Bundestagswahl\" WITH  OWNER = user ENCODING 'UNICODE' TABLESPACE = pg_default;");
 
 					// Tabellen löschen, Reihenfolge relevant---------
 					try {
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
 					} catch (SQLException e) {
 						e.printStackTrace();
 					}
 					// try {
 					// st.executeUpdate("DROP TABLE IF EXISTS stimmenpropartei CASCADE;");
 					// } catch (SQLException e) {
 					// e.printStackTrace();
 					// }
 					// Tabellen anlegen, Reihenfolge relevant-------------
 
 					st.executeUpdate("CREATE TABLE bundesland(bundeslandnummer integer , name text NOT NULL, abkuerzung text,  PRIMARY KEY (bundeslandnummer))WITH (OIDS=FALSE);");
 					st.executeUpdate("CREATE TABLE partei( parteinummer integer,  name text , PRIMARY KEY (parteinummer))WITH (OIDS=FALSE);");
 					st.executeUpdate("CREATE TABLE politiker(  politikernummer integer, name text , PRIMARY KEY (politikernummer))WITH (OIDS=FALSE);");
 					st.executeUpdate("CREATE TABLE wahlkreis( jahr integer, wahlkreisnummer integer, name text,  bundesland integer , PRIMARY KEY (jahr,wahlkreisnummer))WITH (OIDS=FALSE);");
 					st.executeUpdate("CREATE TABLE wahlberechtigte(jahr integer, wahlkreis integer UNIQUE,wahlberechtigte integer , PRIMARY KEY (jahr,wahlkreis))WITH (OIDS=FALSE);");
 					st.executeUpdate("CREATE TABLE direktkandidat(jahr integer,kandidatennummer integer UNIQUE, politiker integer, partei int, wahlkreis integer, PRIMARY KEY (jahr,kandidatennummer)) WITH ( OIDS=FALSE );");
 					st.executeUpdate("CREATE TABLE listenkandidat(jahr integer, partei integer, bundesland int, listenplatz integer, politiker integer, PRIMARY KEY (jahr,partei,bundesland,listenplatz)) WITH ( OIDS=FALSE );");
					st.executeUpdate("CREATE TABLE erststimme( jahr integer, stimmzettelnummer, kandidatennummer integer, wahlkreis integer, PRIMARY KEY (jahr,stimmzettelnummer))WITH (OIDS=FALSE);");
					st.executeUpdate("CREATE TABLE zweitstimme( jahr integer, stimmzettelnummer, partei integer, wahlkreis integer, bundesland integer, PRIMARY KEY (jahr,stimmzettelnummer))WITH (OIDS=FALSE);");
					st.executeUpdate("CREATE TABLE erststimmen( jahr integer, wahlkreis, kandidatennummer integer UNIQUE, anzahl integer, PRIMARY KEY (jahr,kandidatennummer))WITH (OIDS=FALSE);");
					st.executeUpdate("CREATE TABLE zweitstimmen( jahr integer, wahlkreis, partei integer UNIQUE, anzahl integer, PRIMARY KEY (jahr,partei))WITH (OIDS=FALSE);");
 
 					// Tabellen füllen-----------------------------------------
 
 					int bundeslandnummer;
 
 					// Tabelle Bundesländer füllen----------------------------
 
 					bundeslandnummer = 1;
 					for (Map.Entry<String, String> entry : bundeslaenderAbkuerzung
 							.entrySet()) {
 						st.executeUpdate("INSERT INTO bundesland VALUES ("
 								+ bundeslandnummer + ",'"
 								+ (String) entry.getKey() + "','"
 								+ (String) entry.getValue() + "');");
 						bundeslandnummer++;
 					}
 
 					// Wahlkreise anlegen---------------------------------
 
 					// Anfangszeilen überspringen
 					// Statische Daten und Ergebnisse eintragen---
 
 					HashSet<Partei> parteien = new HashSet<Partei>();
 					HashSet<Politiker> politiker = new HashSet<Politiker>();
 					HashSet<Direktkandidat> direktkandidaten = new HashSet<Direktkandidat>();
 					HashSet<Listenkandidat> listenkandidaten = new HashSet<Listenkandidat>();
 
 					int kandidatennummer = 1;
 
 					for (int jahr = 0; jahr < 2; jahr++) {
 						String jahrName = Integer.toString(2005 + jahr * 4);
 
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
 								bundeslandnummer = Integer
 										.parseInt(getQueryResult(st, rs,
 												"SELECT bundeslandnummer FROM bundesland WHERE name = '"
 														+ bundesland
 														+ "' OR abkuerzung = '"
 														+ bundesland + "' ;"));
 
 								// Neuen Wahlkreis hinzufügen
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
 
 						// Anfangszeilen überspringen
 
 						int skipLinesWahlbewerber = 1; // Anzahl Zeilen
 						for (int j = 1; j <= skipLinesWahlbewerber; j++) {
 							readerWahlbewerber[jahr].readNext();
 						}
 
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
 									wahlkreisnummer = readLineWahlbewerber[8]
 											.trim();
 									bundesland = readLineWahlbewerber[9].trim();
 									listenplatz = readLineWahlbewerber[10]
 											.trim();
 									politikername = readLineWahlbewerber[0];
 									String[] temp;
 									temp = politikername.split(",");
 									politikername = temp[0];
 									politikervorname = temp[1];
 									break;
 								case 1:
 									parteiname = readLineWahlbewerber[3].trim();
 									wahlkreisnummer = readLineWahlbewerber[4]
 											.trim();
 									bundesland = readLineWahlbewerber[5].trim();
 									listenplatz = readLineWahlbewerber[6]
 											.trim();
 									politikername = readLineWahlbewerber[0];
 									politikervorname = readLineWahlbewerber[1];
 									break;
 								}
 
 								// Politiker hinzufügen
 								if (!politikername.equals("")) {
 									boolean exists = false;
 									for (Politiker actPolitiker : politiker) {
 										if (actPolitiker.name
 												.equals(parteiname)) {
 											politikernummer = actPolitiker.politikernummer;
 											exists = true;
 											break;
 										}
 									}
 									if (!exists) {
 										politikernummer = politiker.size() + 1;
 										politiker.add(new Politiker(
 												politikernummer, politikername,
 												politikervorname));
 									}
 								}
 
 								// Parteien hinzufügen
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
 
 								// Direktkandidaten hinzufügen
 								if (!wahlkreisnummer.equals("")) {
 									direktkandidaten.add(new Direktkandidat(
 											2005 + jahr * 4, kandidatennummer,
 											parteinummer, politikernummer,
 											Integer.parseInt(wahlkreisnummer)));
 									kandidatennummer++;
 								}
 								// Listenkandidaten hinzufügen
 								if (!bundesland.equals("")) {
 									bundeslandnummer = Integer
 											.parseInt(getQueryResult(
 													st,
 													rs,
 													"SELECT bundeslandnummer FROM bundesland WHERE name = '"
 															+ bundesland
 															+ "' OR abkuerzung = '"
 															+ bundesland
 															+ "' ;"));
 
 									listenkandidaten.add(new Listenkandidat(
 											2005 + jahr * 4, parteinummer,
 											bundeslandnummer, Integer
 													.parseInt(listenplatz),
 											politikernummer));
 								}
 							}
 						}
 						// Ungültige und übrige Parteien auflösen
 					}
 
 					int parteinummer = parteien.size() + 1;
 					int politikernummer = politiker.size() + 1;
 					parteien.add(new Partei(0, "Ungültige"));
 					parteien.add(new Partei(parteinummer, "Übrige"));
 					politiker.add(new Politiker(politikernummer, "Politiker",
 							"Übriger "));
 
 					// Daten in Tabellen schreiben
 
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
 						// Direktkandidaten der Übrigen Parteien generieren
 						int anzahlWahlkreise = Integer.parseInt(getQueryResult(
 								st, rs,
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
 
 					st.close();
 					conn.close();
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		System.out.println("Setup Database finished");
 	}
 
 	public static String getQueryResult(Statement st, ResultSet rs, String query)
 			throws SQLException {
 		String returnString = "";
 		rs = st.executeQuery(query);
 		if (rs.next()) {
 			try {
 				returnString = rs.getString(1);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 		}
 		return returnString;
 	}
 }
