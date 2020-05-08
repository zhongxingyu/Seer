 package csv;
 
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import utils.Tuple;
 
 import database.DB;
 import flags.FlagDefinition;
 import flags.FlagErrorException;
 import flags.Flags;
 
 public class CsvParser {
 
   final int kMaxBewohnerProBezirk = 2500;
   final int kInvalidID = -1;
   final int kPartyDoesNotExist = -1;
   final String kParteilosName = "UNABHNGIGE";
   final int kParteilosID = 99;
   final String kOtherPartiesName = "brige";
   final int kOtherPartyID = 99;
   final int kCurrentElectionYear = 2009;
   final int kPreviousElectionYear = 2005;
   String stimmenFile;
   String erststimmenAggregiertFile;
   String zweitstimmenAggregiertFile;
   String wahlberechtigteFile;
 
   private DB datenbank;
 
   public CsvParser(DB datenbank, String stimmenFile, String erstStimmenAggregiertFile,
       String zweitStimmenAggregiertFile, String wahlberechtigteFile) {
     this.datenbank = datenbank;
     this.stimmenFile = stimmenFile;
     this.erststimmenAggregiertFile = erstStimmenAggregiertFile;
     this.zweitstimmenAggregiertFile = zweitStimmenAggregiertFile;
     this.wahlberechtigteFile = wahlberechtigteFile;
   }
 
   private int getKandidat(int wahlkreisID, int parteiID) throws SQLException {
     final int kKeineParteiID = 99;
     String parteiIDString;
     if (parteiID == kKeineParteiID) {
       parteiIDString = " is NULL";
     } else {
       parteiIDString = "=" + parteiID;
     }
     final String sql = "SELECT " + DB.kID + " " + "FROM " + datenbank.kandidat() + " " + "WHERE "
         + DB.kKandidatDMWahlkreisID + "=" + wahlkreisID + " " + "AND " + DB.kKandidatDMParteiID
         + parteiIDString;
     ResultSet resultSet = datenbank.executeSQL(sql);
     if (resultSet.next()) {
 	    final int result = resultSet.getInt(DB.kID);
 	    resultSet.close();
 	    return result;
     } else {
     	return kInvalidID;
     }
   }
 
   private String getBundesland(int wahlkreisID) throws SQLException {
     String sql = ""
     	  + "SELECT " + DB.kBundeslandName + " "
     	  + "FROM " + datenbank.bundesland() + " "
     	  + "WHERE " + DB.kID + "=" + "("
     	  	+ "SELECT " + DB.kForeignKeyBundeslandID + " "
     	  	+ "FROM " + datenbank.wahlkreis() + " "
     	  	+ "WHERE " + DB.kID + "=" + wahlkreisID + ")";
     ResultSet resultSet = datenbank.executeSQL(sql);
     resultSet.next();
     final String result = resultSet.getString(DB.kBundeslandName);
     resultSet.close();
     return result;
   }
   
   private int getParty(String name) throws SQLException {
     String sql = ""
   	  + "SELECT " + DB.kID + " "
   	  + "FROM " + datenbank.partei() + " "
   	  + "WHERE " + DB.kParteiKuerzel + "='" + name + "'";
 	  ResultSet resultSet = datenbank.executeSQL(sql);
 	  if (resultSet.next()) {
 		  final int result = resultSet.getInt(DB.kID);
 		  resultSet.close();
 		  return result; 	
 	  } else {
 	  	return kPartyDoesNotExist;
 	  }
   }
   
 	public void createVotes(String kandidatID, String parteiID, int wahlbezirkID, int wahlkreisID, int year, 
 			int numVotes, FileWriter fileWriterStimmen) {
 		for (int i = 0; i < numVotes; i++) {
 			doppelStimme(year, wahlkreisID, wahlbezirkID, kandidatID, parteiID, fileWriterStimmen);
 		}
 	}
 	
 	public int getInteger(String[] tokens, int index) {
 		if (index >= tokens.length) {
 			return 0;
 		} else {
 			return Integer.parseInt(0 + tokens[index]);
 		}
 	}
   
   public void parseKergCsv(String csvFile) throws IOException, SQLException, FlagErrorException {
   	final int skipCountBeforeParties = 2;
   	final int skipCountAfterParties = 2;
   	
 		final int kPosWahlkreisID = 0;
 		final int kPosGehoertZu = 2;
 		final int kPosWahlberechtigte = 3;
 		final int kUngueltigePos = 11;
 		final int kPartiesStart = 19;
 		
 		final int kOffsetErststimmen = 0;
 		final int kOffsetErststimmenVorperiode = 1;
 		final int kOffsetZweitstimmen = 2;
 		final int kOffsetZweitstimmenVorperiode = 3;
 		
     FileReader fileReader = new FileReader(csvFile);
     LineNumberReader line_number_reader = new LineNumberReader(fileReader);
     FileWriter fileWriterStimmen = new FileWriter(stimmenFile);
     FileWriter fileWriterErststimmenAggregiert = new FileWriter(erststimmenAggregiertFile);
     FileWriter fileWriterZweitstimmenAggregiert = new FileWriter(zweitstimmenAggregiertFile);
     FileWriter fileWriterWahlberechtigte = new FileWriter(wahlberechtigteFile);
     
     for (int i = 0; i < skipCountBeforeParties; i++) line_number_reader.readLine();
   	String next_line = line_number_reader.readLine(); 
   	String[] parties = next_line.split(";");
   	int[] partyIDs = new int[parties.length];
   	for (int i = 0; i < parties.length; i++) {
   		partyIDs[i] = getParty(parties[i]);
   	}
   	for (int i = 0; i < skipCountAfterParties; i++) line_number_reader.readLine();
   	
   	while ((next_line = line_number_reader.readLine()) != null) {
       String[] tokens = next_line.split(";");
       String gehoertZu = tokens.length > kPosGehoertZu ? tokens[kPosGehoertZu] : "";
       
       // Some rows only contain irrelevant information.
       if (gehoertZu.equals("") || gehoertZu.equals("99")) continue;
       
       int wahlkreisID = Integer.parseInt(tokens[kPosWahlkreisID]);
       
       if (!Flags.getFlagValue(FlagDefinition.kFlagAllowedWahlkreisIDs).equals("")
       		&& (Flags.getFlagValue(FlagDefinition.kFlagAllowedWahlkreisIDs) + ",").indexOf(wahlkreisID + ",") < 0) {
       	continue;
       }
       
       int wahlberechtigte = Integer.parseInt(tokens[kPosWahlberechtigte]);
       createWaehlberechtigte(wahlkreisID, wahlberechtigte, fileWriterWahlberechtigte);
       
       Queue<Tuple<Integer, Integer>> erststimmenQueue = new LinkedList<Tuple<Integer,Integer>>();
       Queue<Tuple<Integer, Integer>> zweitstimmenQueue = new LinkedList<Tuple<Integer,Integer>>();
       
       // Ungueltige Stimmen
       int ungueltigeErststimmen = getInteger(tokens, kUngueltigePos + kOffsetErststimmen);
      erststimmenQueue.add(new Tuple<Integer, Integer>(kInvalidID, ungueltigeErststimmen));
       int ungueltigeZweitstimmen = getInteger(tokens, kUngueltigePos + kOffsetZweitstimmen);
      zweitstimmenQueue.add(new Tuple<Integer, Integer>(kInvalidID, ungueltigeZweitstimmen));
       
       // Fill the queue.
       for (int i = 0; i < 29; i++) {
       	int column = kPartiesStart + 4*i;
       	int parteiID = getParty(parties[column]);
       	int anzahlErststimmen = getInteger(tokens, column + kOffsetErststimmen);
       	int anzahlZweitstimmen = getInteger(tokens, column + kOffsetZweitstimmen);
       	erststimmenQueue.add(new Tuple<Integer, Integer>(getKandidat(wahlkreisID, parteiID), anzahlErststimmen));
       	zweitstimmenQueue.add(new Tuple<Integer, Integer>(parteiID, anzahlZweitstimmen));
       	
       	// Write aggregated votes.
         writeZweitstimmenAggregiert(kCurrentElectionYear, wahlkreisID, parteiID, anzahlZweitstimmen,
         		fileWriterZweitstimmenAggregiert);
         writeErststimmenAggregiert(kCurrentElectionYear, wahlkreisID, parteiID, anzahlErststimmen,
         		fileWriterErststimmenAggregiert);
       	int anzahlErststimmenVorperiode = getInteger(tokens, column + kOffsetErststimmenVorperiode);
       	int anzahlZweitstimmenVorperiode = getInteger(tokens, column + kOffsetZweitstimmenVorperiode);  
       	writeZweitstimmenAggregiert(kPreviousElectionYear, wahlkreisID, parteiID, anzahlZweitstimmenVorperiode,
       			fileWriterZweitstimmenAggregiert);
       	writeErststimmenAggregiert(kPreviousElectionYear, wahlkreisID, parteiID, anzahlErststimmenVorperiode,
       			fileWriterErststimmenAggregiert);
 
       }
 
       Tuple<Integer, Integer> erststimmenTuple;
       Tuple<Integer, Integer> zweitstimmenTuple;
       int kandidatID = -1;
       int parteiID = -1;
       int remainingErststimmen = 0;
       int remainingZweitstimmen = 0;
       int currentWahlbezirk = 0;
       int remainingVotes = 0;
       while (!erststimmenQueue.isEmpty()) {
       	assert(!zweitstimmenQueue.isEmpty());
       	
       	while (!erststimmenQueue.isEmpty() && remainingErststimmen <= 0) {
       		erststimmenTuple = erststimmenQueue.poll();
       		kandidatID = erststimmenTuple.first;
       		remainingErststimmen = erststimmenTuple.second;
       	}
       	while (!zweitstimmenQueue.isEmpty() && remainingZweitstimmen <= 0) {
       		zweitstimmenTuple = zweitstimmenQueue.poll();
       		parteiID = zweitstimmenTuple.first;
       		remainingZweitstimmen = zweitstimmenTuple.second;
       	}
       	
       	if (remainingVotes <= 0) {
       		currentWahlbezirk++;
       		remainingVotes = 2500;
       	}
       	
       	int numVotes = Math.min(Math.min(remainingErststimmen, remainingZweitstimmen), remainingVotes);
      	createVotes((kandidatID == kInvalidID ? "" : "" + kandidatID),
      			(parteiID == kInvalidID ? "" : "" + parteiID), currentWahlbezirk, wahlkreisID, kCurrentElectionYear,
       			numVotes, fileWriterStimmen);
       	
       	remainingErststimmen -= numVotes;
       	remainingZweitstimmen -= numVotes;
       	remainingVotes -= numVotes;
       	
       }
       assert(zweitstimmenQueue.isEmpty());
   	}
   	
     fileWriterStimmen.flush();
     fileWriterErststimmenAggregiert.flush();
     fileWriterZweitstimmenAggregiert.flush();
     fileWriterWahlberechtigte.flush();
    fileWriterStimmen.close();
    fileWriterErststimmenAggregiert.close();
    fileWriterZweitstimmenAggregiert.close();
    fileWriterWahlberechtigte.close();
   }
 
   /**
    * Creates the single votes based on the overall election results.
    * 
    * @param csvFile
    *          A file containing the election results.
    * @param bundesland
    *          If not null, only votes for this Bundesland will be created.
    * @param messagePath
    *          Location where the db2 messages will be stored.
    * @throws IOException
    * @throws SQLException
    */
   public void parseVotes(String csvFile, String bundesland, String messagePath) throws IOException, SQLException {
     final int kSpalteWahlkreisID = 0;
     final int kSpalteParteiID = 1;
     final int kSpalteErststimmen = 2;
     final int kSpalteZweitstimmen = 3;
     final int kSpalteJahr = 4;
 
     FileWriter fileWriterStimmen = new FileWriter(stimmenFile);
     FileWriter fileWriterErststimmenAggregiert = new FileWriter(erststimmenAggregiertFile);
     FileWriter fileWriterZweitstimmenAggregiert = new FileWriter(zweitstimmenAggregiertFile);
 
     // These two values are necessary for inventing the right Wahlbezirke.
     int lastWahlbezirk = -1;
     int currentWahlkreis = -1;
 
     FileReader fileReader = new FileReader(csvFile);
     LineNumberReader line_number_reader = new LineNumberReader(fileReader);
     String next_line = line_number_reader.readLine(); // Skip Header line.
     while ((next_line = line_number_reader.readLine()) != null) {
       String[] tokens = next_line.split(";");
       assert (tokens.length == 5);
 
       final int jahr = Integer.parseInt(tokens[kSpalteJahr]);
       if (jahr == 2009) { // TODO: support 2005 as well
 
         final int wahlkreisID = Integer.parseInt(tokens[kSpalteWahlkreisID]);
         if (wahlkreisID != currentWahlkreis) {
           lastWahlbezirk = 0;
           currentWahlkreis = wahlkreisID;
         }
 
         if (bundesland == null || bundesland.equals(getBundesland(wahlkreisID))) {
           final int parteiId = Integer.parseInt(tokens[kSpalteParteiID]);
           final int anzahlErststimmen = Integer.parseInt("0" + tokens[kSpalteErststimmen]);
           final int anzahlZweitstimmen = Integer.parseInt("0" + tokens[kSpalteZweitstimmen]);
           final int anzahlWahlbezirke = Math.max(anzahlErststimmen, anzahlZweitstimmen) / kMaxBewohnerProBezirk;
 
           writeStimmen(jahr, wahlkreisID, parteiId, anzahlErststimmen, anzahlZweitstimmen, lastWahlbezirk,
               anzahlWahlbezirke, fileWriterStimmen);
 
           writeErststimmenAggregiert(jahr, wahlkreisID, parteiId, anzahlErststimmen, fileWriterErststimmenAggregiert);
           writeZweitstimmenAggregiert(jahr, wahlkreisID, parteiId, anzahlZweitstimmen, fileWriterZweitstimmenAggregiert);
           fileWriterStimmen.flush();
           fileWriterErststimmenAggregiert.flush();
           fileWriterZweitstimmenAggregiert.flush();
           lastWahlbezirk += anzahlWahlbezirke;
         }
       }
 
     }
     fileWriterStimmen.close();
     System.out.println("Stimmen files have been created.");
   }
 
   public void createWaehlberechtigte(int wahlkreisID, int numWahlberechtigte, FileWriter fileWriterWahlberechtigte) throws IOException {
   	for (int i = 0; i < numWahlberechtigte; i++) {
   		fileWriterWahlberechtigte.write(wahlkreisID + "\n");
   	}
   }
   
   private void doppelStimme(int jahr, int wahlkreisID, int bezirkID, String votedForErstimmeID,
       String votedForZweitstimmeID, FileWriter fileWriter) {
     String csvLine = jahr + ";" + wahlkreisID + ";" + bezirkID + ";" + votedForErstimmeID + ";" + votedForZweitstimmeID;
     try {
       fileWriter.write(csvLine + "\n");
     } catch (IOException e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   private void writeStimmen(int jahr, int wahlkreisID, int parteiID, int anzahlErststimmen, int anzahlZweitstimmen,
       int lastWahlbezirk, int anzahlWahlbezirke, FileWriter fileWriter) {
     int kandidatID = kInvalidID;
     try {
       if (anzahlErststimmen > 0)
         kandidatID = getKandidat(wahlkreisID, parteiID);
     } catch (SQLException e) {
       System.out.println("No candidate could be found.");
       e.printStackTrace();
       System.exit(1);
     }
 
     int remainingErststimmen = anzahlErststimmen; // # processed Erststimmen.
     int remainingZweitstimmen = anzahlZweitstimmen; // # processed Zweitstimmen.
     for (int i = 0; i < anzahlWahlbezirke; i++) {
       int anzahlErststimmenBezirk = Math.min(kMaxBewohnerProBezirk, remainingErststimmen);
       remainingErststimmen -= anzahlErststimmenBezirk;
       int anzahlZweitstimmenBezirk = Math.min(kMaxBewohnerProBezirk, remainingZweitstimmen);
       remainingZweitstimmen -= anzahlZweitstimmenBezirk;
 
       final int wahlbezirk = i + lastWahlbezirk + 1;
       System.out.println(wahlkreisID + ";" + wahlbezirk + ";" + kandidatID + ":" + anzahlErststimmenBezirk + ";"
           + parteiID + ":" + anzahlZweitstimmenBezirk);
       final int anzahlStimmenBezirk = Math.max(anzahlErststimmenBezirk, anzahlZweitstimmenBezirk);
       for (int j = 0; j < anzahlStimmenBezirk; j++) {
         final String stimmeKandidatID = (j < anzahlErststimmenBezirk) ? Integer.toString(kandidatID) : "";
         final String stimmeParteiID = (j < anzahlZweitstimmenBezirk) ? Integer.toString(parteiID) : "";
         doppelStimme(jahr, wahlkreisID, wahlbezirk, stimmeKandidatID, stimmeParteiID, fileWriter);
       }
     }
     assert (remainingErststimmen == 0);
     assert (remainingZweitstimmen == 0);
   }
 
   private void writeErststimmenAggregiert(int jahr, int wahlkreisID, int parteiID, int anzahlErststimmen,
       FileWriter fileWriter) {
     if (anzahlErststimmen == 0)
       return;
     try {
       final int kandidatID = getKandidat(wahlkreisID, parteiID);
       String csvLine = jahr + ";" + wahlkreisID + ";" + anzahlErststimmen + ";" + kandidatID;
       fileWriter.write(csvLine + "\n");
     } catch (SQLException e) {
       e.printStackTrace();
       System.exit(1);
     } catch (IOException e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   private void writeZweitstimmenAggregiert(int jahr, int wahlkreisID, int parteiID, int anzahlZweitstimmen,
       FileWriter fileWriter) {
     if (anzahlZweitstimmen == 0)
       return;
     try {
       String csvLine = jahr + ";" + wahlkreisID + ";" + anzahlZweitstimmen + ";" + parteiID;
       fileWriter.write(csvLine + "\n");
     } catch (IOException e) {
       e.printStackTrace();
       System.exit(1);
     }
   }
 
   public void importVotes() throws SQLException {
     final String[] columnsStimmen = { DB.kStimmeJahr, DB.kForeignKeyWahlkreisID,
         DB.kForeignKeyWahlbezirkID, DB.kForeignKeyKandidatID, DB.kForeignKeyParteiID };
     datenbank.load(stimmenFile, "(1, 2, 3, 4, 5)", columnsStimmen, datenbank.stimme());
     System.out.println("Stimmen have been imported to the database");
   }
 
   public void runImports(String dataFolder, String messagePfad) {
     final int kFile = 0;
     final int kColumnNumber = 1;
     final int kTableName = 2;
     final int kColumnName = 3;
     final String[][] kImportTripel = {
         { "Bundeslaender.csv", "(1, 2)", datenbank.bundesland(),
             " (" + DB.kID + ", " + DB.kBundeslandName + ")" },
         {
             "Wahlkreise.csv",
             "(1, 2, 3)",
             datenbank.wahlkreis(),
             " (" + DB.kID + ", " + DB.kWahlkreisName + ", " + DB.kForeignKeyBundeslandID
                 + ")" },
         { "Parteien.csv", "(3, 1, 2)", datenbank.partei(),
             " (" + DB.kID + ", " + DB.kParteiKuerzel + ", " + DB.kParteiName + ")" },
         {
             "Kandidaten.csv",
             "(1, 2, 3, 4, 5, 3, 6)",
             datenbank.kandidat(),
             " (" + DB.kKandidatNachname + ", " + DB.kKandidatVorname + ", " + DB.kForeignKeyParteiID
                 + ", " + DB.kForeignKeyBundeslandID + ", " + DB.kKandidatListenplatz + ", "
                 + DB.kKandidatDMParteiID + ", " + DB.kKandidatDMWahlkreisID + ")" }, };
 
     for (int i = 0; i < Math.min(4, kImportTripel.length); i++) {
 
       System.out.println("Import: " + kImportTripel[i][kFile]);
       final String datei_pfad = dataFolder + kImportTripel[i][kFile];
       String sql = "IMPORT FROM \"" + datei_pfad + "\" OF DEL MODIFIED BY COLDEL; METHOD P "
           + kImportTripel[i][kColumnNumber] + " SKIPCOUNT 1 MESSAGES \"" + messagePfad + "\" INSERT INTO "
           + kImportTripel[i][kTableName] + kImportTripel[i][kColumnName];
       System.out.println(sql);
       datenbank.executeDB2(sql);
     }
     System.out.println("Import has been completed.");
   }
 
   public void importAggregatedVotes() throws SQLException {
     final String[] columnsErststimmenAggregiert = { DB.kWahlergebnis1Jahr, DB.kForeignKeyWahlkreisID,
         DB.kWahlergebnis1Anzahl, DB.kForeignKeyKandidatID };
     datenbank.load(erststimmenAggregiertFile, "(1, 2, 3, 4)", columnsErststimmenAggregiert, datenbank.wahlergebnis1());
     System.out.println("ErststimmenAggregiert have been imported to the database");
 
     final String[] columnsZweitstimmenAggregiert = { DB.kWahlergebnis2Jahr, DB.kForeignKeyWahlkreisID,
         DB.kWahlergebnis2Anzahl, DB.kForeignKeyParteiID };
     datenbank
         .load(zweitstimmenAggregiertFile, "(1, 2, 3, 4)", columnsZweitstimmenAggregiert, datenbank.wahlergebnis2());
     System.out.println("ZweitstimmenAggregiert have been imported to the database");
   }
 }
