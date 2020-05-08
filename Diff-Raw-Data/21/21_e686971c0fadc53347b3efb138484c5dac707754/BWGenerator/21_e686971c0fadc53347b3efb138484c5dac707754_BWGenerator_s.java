 package bundestagswahl.generator;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.postgresql.copy.CopyManager;
 import org.postgresql.core.BaseConnection;
 
 import au.com.bytecode.opencsv.CSVReader;
 import au.com.bytecode.opencsv.CSVWriter;
 import bundestagswahl.setup.BWSetupDatabase;
 import bundestagswahl.setup.CopyProgressMonitor;
 
 public class BWGenerator {
 
 	// public static String progPfad = System.getProperty("user.dir");
 	// public static String homePfad = System.getProperty("user.home");
 
 	public static String ergebnis05Pfad = "csv\\kerg2005.csv";
 	public static String ergebnis09Pfad = "csv\\kerg2009.csv";
 
 	public static String erststimmen05Pfad = "csv\\erststimmen2005.csv";
 	public static String erststimmen09Pfad = "csv\\erststimmen2009.csv";
 	public static String zweitstimmen05Pfad = "csv\\zweitstimmen2005.csv";
 	public static String zweitstimmen09Pfad = "csv\\zweitstimmen2009.csv";
 
	public static boolean setupDatabase = false;// Datenbank neu aufsetzen
	public static boolean generateStimmen = false;// Stimmen CSV neu generieren
	public static boolean loadStimmen = false;// Stimmen neu in Datenbank laden
	public static boolean addConstraints = false;// Constraints hinzufügen
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		try {
 			Class.forName("org.postgresql.Driver");
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		// Datenbankverbindung aufbauen
 		String url = "jdbc:postgresql://localhost/Bundestagswahl?user=user&password=1234";
 		Connection conn;
 		Statement st;
 
 		ResultSet rs = null;
 
 		// Datenbank neu aufsetzen
 		if (setupDatabase) {
 			String[] init = { "" };
 			BWSetupDatabase.main(init);
 		}
 
 		// Stimmen pro Wahlkreis aus wkumrechnung200x.csv auslesen
 		// und in stimmen200x.csv Einzelstimmen schreiben
 
 		try {
 			try {
 				conn = DriverManager.getConnection(url);
 				st = conn.createStatement();
 				CSVReader readerErgebnis[] = new CSVReader[2];
 				CSVWriter writerErststimmen[] = new CSVWriter[2];
 				CSVWriter writerZweitstimmen[] = new CSVWriter[2];
 
 				if (generateStimmen) {
 					readerErgebnis[0] = new CSVReader(new BufferedReader(
 							new InputStreamReader(new FileInputStream(
 									ergebnis05Pfad), "UTF-8")), ';');
 					readerErgebnis[1] = new CSVReader(new BufferedReader(
 							new InputStreamReader(new FileInputStream(
 									ergebnis09Pfad), "UTF-8")), ';');
 
 					writerErststimmen[0] = new CSVWriter(new BufferedWriter(
 							new OutputStreamWriter(new FileOutputStream(
 									erststimmen05Pfad), "UTF-8")), ';');
 					writerErststimmen[1] = new CSVWriter(new BufferedWriter(
 							new OutputStreamWriter(new FileOutputStream(
 									erststimmen09Pfad), "UTF-8")), ';');
 
 					writerZweitstimmen[0] = new CSVWriter(new BufferedWriter(
 							new OutputStreamWriter(new FileOutputStream(
 									zweitstimmen05Pfad), "UTF-8")), ';');
 					writerZweitstimmen[1] = new CSVWriter(new BufferedWriter(
 							new OutputStreamWriter(new FileOutputStream(
 									zweitstimmen09Pfad), "UTF-8")), ';');
 				}
 
 				for (int jahr = 0; jahr < 2; jahr++) {
 					String jahrName = Integer.toString(2005 + jahr * 4);
 
 					if (generateStimmen) {
 						System.out.println("\nGenerating started");
 						String[] readLineErgebnis;
 
 						// Wahlberechtigte Tabelle leeren
 						st.executeUpdate("DELETE FROM wahlberechtigte;");
 
 						// kerg.csv------------------------------------
 
 						// Anfangszeilen überspringen und Header auslesen
 
 						readerErgebnis[jahr].readNext();
 						readerErgebnis[jahr].readNext();
 						readLineErgebnis = readerErgebnis[jahr].readNext();
 						readerErgebnis[jahr].readNext();
 						readerErgebnis[jahr].readNext();
 
 						// Beschriftung einlesen und Spalten Parteien zuordnen
 						HashMap<Integer, String> parteienSpalte = new HashMap<Integer, String>();
 						for (int i = 19; i < 132; i = i + 4) {
 							parteienSpalte.put(i, readLineErgebnis[i]);
 						}
 
 						// Benötige Variablen
 						int erststimmzettelnummer = 1;
 						int zweitstimmzettelnummer = 1;
 
 						int aktuelleKandidatennummer = 0;
 						int aktuelleParteinummer = 0;
 
 						while ((readLineErgebnis = readerErgebnis[jahr]
 								.readNext()) != null) {
 
 							if (!readLineErgebnis[0].trim().equals("")
 									&& !readLineErgebnis[2].trim().equals("")
 									&& !readLineErgebnis[2].equals("99")) {
 
 								int wahlkreisnummer = Integer
 										.parseInt(readLineErgebnis[0]);
 
 								String wahlkreisname = readLineErgebnis[1];
 								System.out.println("\n" + wahlkreisnummer
 										+ " - " + wahlkreisname);// Ladebalken
 
 								int wahlberechtigte = Integer
 										.parseInt(readLineErgebnis[3]);
 
 								System.out.println(wahlberechtigte);
 
 								int aktelleBundeslandnummer = Integer
 										.parseInt(getQueryResult(
 												st,
 												rs,
 												"SELECT bundesland FROM wahlkreis WHERE jahr = "
 														+ jahrName
 														+ " AND wahlkreisnummer = "
 														+ wahlkreisnummer + ";"));
 
 								// Wahlberechtigte einfgen
 								st.executeUpdate("INSERT INTO wahlberechtigte VALUES ("
 										+ jahrName
 										+ ","
 										+ wahlkreisnummer
 										+ "," + wahlberechtigte + ");");
 
 								for (int i = 19; i < 132; i = i + 4) {
 
 									System.out.print(".");// Ladebalken
 
 									String partei = parteienSpalte.get(i);
 
 									int erststimmenAnzahl = 0;
 									int zweitstimmenAnzahl = 0;
 
 									if (!readLineErgebnis[i].equals(""))
 										erststimmenAnzahl = Integer
 												.parseInt(readLineErgebnis[i]);
 
 									if (!readLineErgebnis[i + 2].equals(""))
 										zweitstimmenAnzahl = Integer
 												.parseInt(readLineErgebnis[i + 2]);
 
 									aktuelleParteinummer = Integer
 											.parseInt(getQueryResult(st, rs,
 													"SELECT parteinummer FROM partei WHERE name = '"
 															+ partei + "';"));
 
 									if (zweitstimmenAnzahl > 0) {
 
 										for (int j = 0; j < zweitstimmenAnzahl; j++) {
 											String[] writeLine = {
 													jahrName,
 													Integer.toString(zweitstimmzettelnummer),
 													Integer.toString(aktuelleParteinummer),
 													Integer.toString(wahlkreisnummer),
 													Integer.toString(aktelleBundeslandnummer) };
 
 											writerZweitstimmen[jahr]
 													.writeNext(writeLine);
 											zweitstimmzettelnummer++;
 										}
 									}
 
 									if (erststimmenAnzahl > 0) {
 										aktuelleKandidatennummer = Integer
 												.parseInt(getQueryResult(
 														st,
 														rs,
 														"SELECT kandidatennummer FROM direktkandidat WHERE jahr = "
 																+ jahrName
 																+ " AND wahlkreis = "
 																+ wahlkreisnummer
 																+ " AND partei = "
 																+ aktuelleParteinummer
 																+ ";"));
 
 										for (int j = 0; j < erststimmenAnzahl; j++) {
 											String[] writeLine = {
 													jahrName,
 													Integer.toString(erststimmzettelnummer),
 													Integer.toString(aktuelleKandidatennummer),
 													Integer.toString(wahlkreisnummer) };
 
 											writerErststimmen[jahr]
 													.writeNext(writeLine);
 											erststimmzettelnummer++;
 										}
 									}
 								}
 							}
 						}
 						writerZweitstimmen[jahr].close();
 
 						writerErststimmen[jahr].close();
 						readerErgebnis[jahr].close();
 
 						System.out.println("\nGenerating finished");
 					}
 
 					SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
 					if (loadStimmen) {
 
 						// Bulk Load der
 						// ErstStimmen----------------------------------------
 						CopyManager copyManager = new CopyManager(
 								(BaseConnection) conn);
 						String actPfad;
 						String progressString;
 						String talbeDestination;
 						for (int stimme = 0; stimme < 2; stimme++) {
 							System.out.println("\nCopying started: "
 									+ format.format(new Date()));
 
 							switch (stimme) {
 							case 0:
 								talbeDestination = "erststimme";
 								switch (jahr) {
 								case 0:
 									actPfad = erststimmen05Pfad;
 									progressString = "Erststimmen 2005 laden";
 									break;
 								default:
 									actPfad = erststimmen09Pfad;
 									progressString = "Erststimmen 2009 laden";
 									break;
 								}
 								break;
 							default:
 								talbeDestination = "zweitstimme";
 								switch (jahr) {
 								case 0:
 									actPfad = zweitstimmen05Pfad;
 									progressString = "Zweitstimmen 2005 laden";
 									break;
 								default:
 									actPfad = zweitstimmen09Pfad;
 									progressString = "Zweitstimmen 2009 laden";
 									break;
 								}
 								break;
 							}
 
 							System.out.println(talbeDestination + "   "
 									+ actPfad);
 							InputStream in = new BufferedInputStream(
 									CopyProgressMonitor.getCopyProgressMonitor(
 											actPfad, progressString));
 							copyManager.copyIn("COPY " + talbeDestination
 									+ " FROM STDIN WITH DELIMITER ';' CSV", in);
 
 							System.out.println("\nCopying finished");
 						}
 					}
 					if (addConstraints) {
 						System.out.println("\nAdding Constraints");
 
 						try {
 							st.executeUpdate("ALTER TABLE wahlberechtigte  ADD CONSTRAINT wahlkreis FOREIGN KEY (jahr,wahlkreis) REFERENCES wahlkreis(jahr,wahlkreisnummer);");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE direktkandidat  ADD CONSTRAINT politiker FOREIGN KEY (politiker) REFERENCES politiker;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE direktkandidat  ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE direktkandidat  ADD CONSTRAINT wahlkreis FOREIGN KEY (jahr,wahlkreis) REFERENCES wahlkreis(jahr,wahlkreisnummer);");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE listenkandidat  ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE listenkandidat  ADD CONSTRAINT bundesland FOREIGN KEY (bundesland) REFERENCES bundesland;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE listenkandidat  ADD CONSTRAINT politiker FOREIGN KEY (politiker) REFERENCES politiker;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE erststimme  ADD CONSTRAINT kandidatennummer FOREIGN KEY (kandidatennummer) REFERENCES direktkandidat(kandidatennummer);");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE zweitstimme  ADD CONSTRAINT bundesland FOREIGN  KEY (bundesland) REFERENCES bundesland;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE zweitstimme  ADD CONSTRAINT partei FOREIGN  KEY (partei) REFERENCES partei;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE erststimmen  ADD CONSTRAINT kandidatennummer FOREIGN  KEY (kandidatennummer) REFERENCES direktkandidat(kandidatennummer);");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						try {
 							st.executeUpdate("ALTER TABLE zweitstimmen  ADD CONSTRAINT partei FOREIGN  KEY (partei) REFERENCES partei;");
 						} catch (SQLException e) {
 							e.printStackTrace();
 						}
 						System.out.println("\nFinished");
 					}
 
 				}
 
 				// Stimmen aggregieren
 				System.out.println("\n Aggregate Stimmen");
 
 				try {
 					st.executeUpdate("DELETE FROM erststimmen;");
 					st.executeUpdate("DELETE FROM zweitstimmen;");
 					st.executeUpdate("INSERT INTO erststimmen SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr ORDER BY wahlkreis, anzahl;");
 					st.executeUpdate("INSERT INTO zweitstimmen SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr ORDER BY wahlkreis, anzahl;");
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 
 				printQueryResult(st, rs, "zweitstimmen");
 				printQueryResult(st, rs, "erststimmen");
 
 				System.out.println("\nFinished");
 
 				// Parameter fr Queries, ber UI auszuwhlen:
 				String jahrName = "2005";
 				int wahlkreis = 1;
 
 				// Q4: Wahlkreissieger (Q3 bentigt View aus Q4)
 				System.out.println("\n Q4: Wahlkreissieger");
 
 				try {
 					st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT  s1.wahlkreis, s1.kandidatennummer, s1.anzahl FROM erststimmen s1 , wahlkreis w WHERE s1.jahr = "
 							+ jahrName
 							+ " AND w.jahr = "
 							+ jahrName
 							+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = ( SELECT max(s2.anzahl) FROM erststimmen s2 WHERE s2.jahr = "
 							+ jahrName
 							+ " AND s2.wahlkreis = w.wahlkreisnummer)");
 					st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmengewinner AS SELECT s1.wahlkreis, s1.partei, s1.anzahl FROM zweitstimmen s1 , wahlkreis w WHERE s1.jahr = "
 							+ jahrName
 							+ " AND w.jahr = "
 							+ jahrName
 							+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = ( SELECT max(s2.anzahl) FROM zweitstimmen s2 WHERE s2.jahr = "
 							+ jahrName
 							+ " AND s2.wahlkreis = w.wahlkreisnummer)");
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 
 				printQueryResult(st, rs, "erststimmengewinner");
 				printQueryResult(st, rs, "zweitstimmengewinner");
 
 				System.out.println("\nFinished");
 
 				// Q3: Wahlkreisbersicht
 				System.out.println("\n Q3: Wahlkreisbersicht");
 
 				try {
 					st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsolut AS "
 							+ "SELECT sum(anzahl) FROM erststimmen WHERE jahr = "
 							+ jahrName + " AND wahlkreis = " + wahlkreis);
 
 					st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativ AS "
 							+ "SELECT (SELECT * FROM wahlbeteiligungabsolut) / (  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = "
 							+ jahrName
 							+ " AND wahlkreis = "
 							+ wahlkreis
 							+ " )::float ;");
 
 					st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidat AS "
 							+ "SELECT name FROM politiker p , direktkandidat d WHERE p.politikernummer = d.politiker AND d.kandidatennummer = (SELECT e.kandidatennummer FROM erststimmengewinner e ORDER BY RANDOM() LIMIT 1)");
 
 					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolut AS "
 							+ "SELECT p.parteinummer as parteinummer, (SELECT sum(zs.anzahl) FROM zweitstimmen zs WHERE zs.jahr = "
 							+ jahrName
 							+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");
 
 					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilrelativ AS "
 							+ "SELECT pa.parteinummer as parteinummer, pa.anzahl/(SELECT * FROM wahlbeteiligungabsolut) as anteil FROM parteinenanteilabsolut pa ");
 
 					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahr AS "
 							+ "SELECT p.parteinummer, (SELECT sum(zs.anzahl) FROM zweitstimmen zs WHERE zs.jahr = "
 							+ Integer.toString(Integer.parseInt(jahrName) - 4)
 							+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");
 
 					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilveraenderung AS "
 							+ "SELECT pa1.parteinummer as parteinummer , pa1.anzahl-pa2.anzahl as anzahl FROM parteinenanteilabsolutvorjahr pa2, parteinenanteilabsolut pa1 WHERE pa1.parteinummer = pa2.parteinummer");
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 
 				printQueryResult(st, rs, "wahlbeteiligungabsolut");
 				printQueryResult(st, rs, "wahlbeteiligungrelativ");
 				printQueryResult(st, rs, "erststimmengewinnerkandidat");
 				printQueryResult(st, rs, "parteinenanteilabsolut");
 				printQueryResult(st, rs, "parteinenanteilrelativ");
 				printQueryResult(st, rs, "parteinenanteilabsolutvorjahr");
 				printQueryResult(st, rs, "parteinenanteilveraenderung");
 
 				System.out.println("\nFinished");
 
 				// Q5: berhangmandate
 				System.out.println("\n Q5: berhangmandate");
 
 				System.out.println("\nFinished");
 
 				// Q6: Knappster Sieger
 				System.out.println("\n Q6: Knappster Sieger");
 
 				try {
 					st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinner AS "
 							+ "SELECT s1.wahlkreis, s1.kandidatennummer, d.partei , "
 							+ "( SELECT min(s1.anzahl - s2.anzahl) FROM erststimmen s2 WHERE jahr = "
 							+ jahrName
 							+ " AND s1.wahlkreis = s2.wahlkreis AND s1.kandidatennummer != s2.kandidatennummer) AS differenz"
 							+ " FROM erststimmen s1 , direktkandidat d WHERE s1.jahr = "
 							+ jahrName
 							+ " AND d.jahr = "
 							+ jahrName
 							+ " AND s1.kandidatennummer = d.kandidatennummer");
 
 					st.executeUpdate("CREATE OR REPLACE VIEW knappsteergebnisse AS "
 							+ "(SELECT * FROM knappstegewinner ) UNION "
 							+ "(SELECT s1.wahlkreis, s1.kandidatennummer, d.partei , "
 							+ " ( SELECT min( differenz ) FROM ( SELECT (s2.anzahl - s1.anzahl) As differenz FROM erststimmen s2 WHERE jahr = "
 							+ jahrName
 							+ " AND s2.wahlkreis = s1.wahlkreis AND ( s2.anzahl - s1.anzahl ) > 0 ) AS ergebnissedifferenzen )"
 							+ " FROM erststimmen s1 , direktkandidat d WHERE s1.jahr = "
 							+ jahrName
 							+ " AND d.jahr = "
 							+ jahrName
 							+ " AND s1.kandidatennummer = d.kandidatennummer AND d.partei NOT IN ( SELECT k.partei from knappstegewinner k) )");
 
 				} catch (SQLException e) {
 					e.printStackTrace();
 				}
 
 				printQueryResult(st, rs, "knappstegewinner");
 				printQueryResult(st, rs, "knappsteergebnisse");
 
 				System.out.println("\nFinished");
 
 				st.close();
 				conn.close();
 
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	/**
 	 * Gibt das erste Ergebnistupel der Abfrage zurck
 	 */
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
 
 	public static void printQueryResult(Statement st, ResultSet rs, String table)
 			throws SQLException {
 		String returnString = "";
 		rs = st.executeQuery("SELECT * FROM " + table + " ;");
 		System.out.println(" ");
 		System.out.println("------------------------------");
 		System.out.println(table + " :");
 		ResultSetMetaData meta = rs.getMetaData();
 		int anzFields = meta.getColumnCount();
 		while (rs.next()) {
 			try {
 				for (int i = 0; i < anzFields; i++) {
 					System.out.print(rs.getString(i + 1) + "  |  ");
 
 				}
 				System.out.print("\n");
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 			}
 		}
 		System.out.println("------------------------------");
 		System.out.println(" ");
 	}
 
 }
