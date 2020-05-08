 package database;
 
 public class TableDef {
 
   private String schemaName;
 
   public TableDef(String schemaName) {
     this.schemaName = schemaName;
   }
 
   public String buildCreateTableStatement() {
     String sql = "";
     for (String statement : getStatements())
       sql += statement;
     return sql;
   }
   
   public static String[] baseTables() {
   	return new String[] {
   			"BUNDESLAND",
   			"WAHLKREIS",
   			"WAHLBEZIRK",
   			"PARTEI",
   			"STIMME",
   			"KANDIDAT",
   			"ERSTSTIMMENNACHWAHLKREIS",
   			"ZWEITSTIMMENNACHWAHLKREIS",
   			"WAHLBERECHTIGTER",
   			"WAHLKREISDATEN"};
   }
 
   private String[] getStatements() {
     final String autoIncrementID = "BIGINT  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE )";
     return new String[] {
 
     // Bundesland
         "CREATE TABLE " + schemaName + ".BUNDESLAND ( ID BIGINT  NOT NULL , " + "NAME VARCHAR (255)  NOT NULL  , "
             + "CONSTRAINT CC1288606507352 PRIMARY KEY ( ID)  ) ;\n",
 
         // Wahlkreis
         "CREATE TABLE " + schemaName + ".WAHLKREIS ( ID BIGINT  NOT NULL , " + "BUNDESLANDID BIGINT  NOT NULL , "
             + "NAME VARCHAR (255)  , " + "CONSTRAINT CC1288606603901 PRIMARY KEY ( ID) , "
             + "CONSTRAINT CC1288606617285 FOREIGN KEY (BUNDESLANDID) REFERENCES " + schemaName
             + ".BUNDESLAND (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
             + "ORGANIZE BY DIMENSIONS ( BUNDESLANDID) ;\n",
 
         // Wahlbezirk
        "CREATE TABLE " + schemaName + ".WAHLBEZIRK " + "( ID BIGINT NOT NULL, "
             + "WAHLKREISID BIGINT  NOT NULL  , " + "CONSTRAINT CC1288606788792 PRIMARY KEY ( ID, WAHLKREISID) , "
             + "CONSTRAINT CC1288606799462 FOREIGN KEY (WAHLKREISID) REFERENCES " + schemaName
             + ".WAHLKREIS (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
             + "ORGANIZE BY DIMENSIONS ( WAHLKREISID) ;\n",
 
         // Partei
         "CREATE TABLE " + schemaName + ".PARTEI ( ID BIGINT  NOT NULL , " + "NAME VARCHAR (255) , "
             + "KUERZEL VARCHAR (63)  NOT NULL  , " + "CONSTRAINT CC1288606983948 PRIMARY KEY ( ID) );\n",
 
         // Kandidat
         "CREATE TABLE " + schemaName + ".KANDIDAT " + "( ID " + autoIncrementID + " , " + "PARTEIID BIGINT , "
             + "BUNDESLANDID BIGINT ," + "DMWAHLKREISID BIGINT," + "DMPARTEIID BIGINT,"
             + "NACHNAME VARCHAR (255)  NOT NULL , " + "VORNAME VARCHAR (255)  NOT NULL , " + "BERUF VARCHAR (255) , "
             + "GEBURTSDATUM DATE , " + "GEBURTSORT VARCHAR (255) ," + "ANSCHRIFT VARCHAR (2047)  , "
             + "LISTENPLATZ INTEGER ," + "CONSTRAINT CC1288607383356 PRIMARY KEY ( ID) , "
             + "CONSTRAINT CC1288607389830 FOREIGN KEY (PARTEIID) REFERENCES " + schemaName
             + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
             + "CONSTRAINT CC1288607385362 FOREIGN KEY (DMWAHLKREISID) REFERENCES " + schemaName
             + ".WAHLKREIS (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
             + "CONSTRAINT CC1288607388564 FOREIGN KEY (DMPARTEIID) REFERENCES " + schemaName
             + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
             + "CONSTRAINT CC1288607382330 FOREIGN KEY (BUNDESLANDID) REFERENCES " + schemaName
             + ".BUNDESLAND (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION ) "
             + "ORGANIZE BY DIMENSIONS ( PARTEIID, BUNDESLANDID) ;\n",
 
         // Stimme
         "CREATE TABLE " + schemaName + ".STIMME " + "( " + DB.kID + " " + autoIncrementID + " , "
             + DB.kForeignKeyKandidatID + " BIGINT, " + DB.kForeignKeyParteiID + " BIGINT, "
             + DB.kForeignKeyWahlbezirkID + " BIGINT  NOT NULL , " + DB.kForeignKeyWahlkreisID
             + " BIGINT NOT NULL , " + DB.kStimmeJahr + " INTEGER , "
             + "CONSTRAINT CC1288610679447 PRIMARY KEY ( " + DB.kID + ") , "
             + "CONSTRAINT CC1288610689165 FOREIGN KEY ( " + DB.kForeignKeyKandidatID + ") REFERENCES " + schemaName
             + ".KANDIDAT (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED ENABLE QUERY OPTIMIZATION, "
             + "CONSTRAINT CC1288610702435 FOREIGN KEY (PARTEIID) REFERENCES " + schemaName
             + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION ) "
             // +
             // "CONSTRAINT CC1288610713433 FOREIGN KEY (WAHLBEZIRKID, WAHLKREISID) REFERENCES "
             // + schemaName
             // +
             // ".WAHLBEZIRK (ID, WAHLKREISID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
             + "ORGANIZE BY DIMENSIONS ( KANDIDATID, WAHLBEZIRKID, WAHLKREISID) ;\n",
 
         // ERSTSTIMMENNACHWAHLKREIS
         "CREATE TABLE " + schemaName + ".ERSTSTIMMENNACHWAHLKREIS " + "("
         	  + "KANDIDATID BIGINT NOT NULL, "
             + "WAHLKREISID BIGINT  NOT NULL , "
             + "JAHR INTEGER NOT NULL, "
             + "ANZAHL INTEGER NOT NULL, "
             + "CONSTRAINT CC1288610976900 PRIMARY KEY ( KANDIDATID, WAHLKREISID, JAHR) , "
             //+ "CONSTRAINT CC1288610983160 FOREIGN KEY (KANDIDATID) REFERENCES " + schemaName
             //+ ".KANDIDAT (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
             + "CONSTRAINT CC1288610994045 FOREIGN KEY (WAHLKREISID) REFERENCES " + schemaName
             + ".WAHLKREIS (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
             + "ORGANIZE BY DIMENSIONS ( KANDIDATID, WAHLKREISID) ;\n" + "COMMENT ON TABLE " + schemaName
             + ".ERSTSTIMMENNACHWAHLKREIS IS 'Wahlergebnis 1. Stimme';\n",
 
         // ZWEITSTIMMENNACHWAHLKREIS
         "CREATE TABLE " + schemaName + ".ZWEITSTIMMENNACHWAHLKREIS " + "(" + "PARTEIID BIGINT  NOT NULL , "
             + "WAHLKREISID BIGINT  NOT NULL ," + "JAHR INTEGER  NOT NULL  , " + "ANZAHL INTEGER  NOT NULL, "
             + "CONSTRAINT CC1288610976900 PRIMARY KEY ( PARTEIID, WAHLKREISID, JAHR) , "
             + "CONSTRAINT CC1288610983160 FOREIGN KEY (PARTEIID) REFERENCES " + schemaName
             + ".PARTEI (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION , "
             + "CONSTRAINT CC1288610994045 FOREIGN KEY (WAHLKREISID) REFERENCES " + schemaName
             + ".WAHLKREIS (ID)  ON DELETE NO ACTION ON UPDATE NO ACTION ENFORCED  ENABLE QUERY OPTIMIZATION  ) "
             + "ORGANIZE BY DIMENSIONS ( PARTEIID, WAHLKREISID) ;\n" + "COMMENT ON TABLE " + schemaName
             + ".ZWEITSTIMMENNACHWAHLKREIS IS 'Wahlergebnis 2. Stimme';\n",
             
         // Wahlberechtigter
         "CREATE TABLE " + schemaName + ".Wahlberechtigter ("
         	  + "ID BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1, NO CACHE ), "
         	  + "WahlkreisID BIGINT REFERENCES " + schemaName + ".Wahlkreis, "
         	  + "WahlbezirkID BIGINT, "
         	  + "Gewaehlt INTEGER WITH DEFAULT 0);\n",
     
     		// Wahlkreisdaten
     		"CREATE TABLE " + schemaName + ".WahlkreisDaten ("
     		    + "WahlkreisID BIGINT NOT NULL PRIMARY KEY REFERENCES " + schemaName + ".Wahlkreis, "
     		    + "AnzahlWahlberechtigte BIGINT, "
     		    + "AnzahlUngueltigeErststimmen BIGINT, "
     		    + "AnzahlUngueltigeZweitstimmen BIGINT, "
     		    + "Jahr BIGINT);\n",
     
     		// SESSION IDs 
     		"CREATE TABLE " + schemaName + ".SessionIDs ( "
     				+ "ID VARCHAR (128) PRIMARY KEY NOT NULL);\n"
     		
     		};
   };
 
 }
