 // %3512883119:de.hattrickorganizer.tools%
 package de.hattrickorganizer.tools;
 
 import de.hattrickorganizer.model.Lineup;
 import de.hattrickorganizer.model.Basics;
 import de.hattrickorganizer.model.Finanzen;
 import de.hattrickorganizer.model.HOModel;
 import de.hattrickorganizer.model.Liga;
 import de.hattrickorganizer.model.Spieler;
 import de.hattrickorganizer.model.Stadium;
 import de.hattrickorganizer.model.Team;
 import de.hattrickorganizer.model.Verein;
 import de.hattrickorganizer.model.XtraData;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import java.util.Properties;
 import java.util.Vector;
 
 
 /**
  * Importiert ein HRF-File und stellt die Werte bereit
  */
 public class HRFFileParser {
     //~ Static fields/initializers -----------------------------------------------------------------
 
     /** TODO Missing Parameter Documentation */
     private static final String ENTITY = "Entity";
 
     /** TODO Missing Parameter Documentation */
     private static final String BASICS = "[basics]";
 
     /** TODO Missing Parameter Documentation */
     private static final String LEAGUE = "[league]";
 
     /** TODO Missing Parameter Documentation */
     private static final String CLUB = "[club]";
 
     /** TODO Missing Parameter Documentation */
     private static final String TEAM = "[team]";
 
     /** TODO Missing Parameter Documentation */
     private static final String LINEUP = "[lineup]";
 
     /** TODO Missing Parameter Documentation */
     private static final String ECONOMY = "[economy]";
 
     /** TODO Missing Parameter Documentation */
     private static final String ARENA = "[arena]";
 
     /** TODO Missing Parameter Documentation */
     private static final String PLAYER = "[player]";
 
     /** TODO Missing Parameter Documentation */
     private static final String XTRA = "[xtra]";
 
     /** TODO Missing Parameter Documentation */
     private static final String LASTLINEUP = "[lastlineup]";
 
     //~ Instance fields ----------------------------------------------------------------------------
 
     private BufferedReader m_clReader;
 
     //~ Methods ------------------------------------------------------------------------------------
 
     /**
      * Datei einlesen und parsen
      *
      * @param datei TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     public final HOModel parse(File datei) {
         java.sql.Timestamp hrfdate = null;
 
         if (!datei.exists() || !datei.canRead()) {
             HOLogger.instance().log(getClass(),"Datei " + datei.getPath() + " nicht einlesbar!");
             return null;
         }
 
         try {
             final Vector<Properties> propertiesVector = new Vector<Properties>();
             Properties properties = null;
 
             //Datei als Datenstrom laden
             final FileInputStream fis = new FileInputStream(datei.getPath());
             final InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
             m_clReader = new BufferedReader(isr);
 
             //m_clReader = new BufferedReader(new InputStreamReader(new FileInputStream(datei.getPath())));		
             //Solange der Zeichenstrom nicht leer ist
             while (m_clReader.ready()) {
                 //Eine Zeile einlesen
                 final String zeile = m_clReader.readLine();
 
                 //Leere Zeile ist null! Wird auf leeren String gesetzt.
                 if ((zeile == null) || zeile.trim().equals("")) {
                     //Überspringen
                     continue;
                 }
 
                 //Neue Properties
                 if (zeile.startsWith("[")) {
                     //Alte Property vorhanden, dann dem Vector hinzufügen
                     if (properties != null) {
                         //Datum des HRFs
                         final Object entity = properties.get(HRFFileParser.ENTITY);
 
                         if ((entity != null)
                             && entity.toString().equalsIgnoreCase(HRFFileParser.BASICS)) {
                             final String datestring = properties.getProperty("date");
                             hrfdate = getDatumByString(datestring);
                         }
 
                         propertiesVector.add(properties);
                     }
 
                     //Neue Property erzeugen
                     properties = new Properties();
 
                     //Spieler?
                     if (zeile.startsWith("[player")) {
                         properties.setProperty(HRFFileParser.ENTITY, HRFFileParser.PLAYER);
                         properties.setProperty("id", zeile.substring(7, zeile.lastIndexOf(']')));
                     }
                     //Alle anderen
                     else {
                         properties.setProperty(HRFFileParser.ENTITY, zeile);
                     }
                 }
                 //Aktuelle Properties füllen
                 else {
                     final int indexGleichheitszeichen = zeile.indexOf('=');
 
                     //Eins vorhanden
                     if (indexGleichheitszeichen > 0) {
                         properties.setProperty(zeile.substring(0, indexGleichheitszeichen)
                                                     .toLowerCase(),
                                                zeile.substring(indexGleichheitszeichen + 1));
                     }
                 }
             }
 
             //Letzte Properties setzen
             if (properties != null) {
                 propertiesVector.add(properties);
             }
 
             //Dateizugriff beenden
             try {
                 m_clReader.close();
             } catch (IOException ioe) {
             }
 
             //### Debug
             //printProperties( propertiesVector );
             //Aus den gesammelten Properties ein HOModel machen
             return createHOModel(propertiesVector, hrfdate);
         } catch (Exception e) {
             HOLogger.instance().log(getClass(),"Fehler beim Parsen der Datei " + datei.getPath());
             HOLogger.instance().log(getClass(),e);
         }
 
         //Nur im Fehlerfall
         return null;
     }
 
     /**
      * TODO Missing Method Documentation
      *
      * @param date TODO Missing Method Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      */
     private java.sql.Timestamp getDatumByString(String date) {
         try {
             //Hattrick
             final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                                                                            java.util.Locale.GERMANY);
 
             return new java.sql.Timestamp(simpleFormat.parse(date).getTime());
         } catch (Exception e) {
             try {
                 //Hattrick
                 final java.text.SimpleDateFormat simpleFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                                                                                                java.util.Locale.GERMANY);
 
                 return new java.sql.Timestamp(simpleFormat.parse(date).getTime());
             } catch (Exception expc) {
                 HOLogger.instance().log(getClass(),e);
                 return new java.sql.Timestamp(System.currentTimeMillis());
             }
         }
     }
 
     /**
      * Erzeugt aus dem Vector mit Properties ein HOModel
      *
      * @param propertiesVector TODO Missing Constructuor Parameter Documentation
      * @param hrfdate TODO Missing Constructuor Parameter Documentation
      *
      * @return TODO Missing Return Method Documentation
      *
      * @throws Exception TODO Missing Constructuor Exception Documentation
      */
     private HOModel createHOModel(Vector<Properties> propertiesVector, java.sql.Timestamp hrfdate)
       throws Exception
     {
         final HOModel hoModel = new HOModel();
         int trainerID = -1;
 
         for (int i = 0; i < propertiesVector.size(); i++) {
             final Properties properties = (Properties) propertiesVector.get(i);
             final Object entity = properties.get(HRFFileParser.ENTITY);
 
             if (entity != null) {
                 //basics
                 if (entity.toString().equalsIgnoreCase(HRFFileParser.BASICS)) {
                     hoModel.setBasics(new Basics(properties));
                 }
                 //league
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.LEAGUE)) {
                     hoModel.setLiga(new Liga(properties));
                 }
                 //club
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.CLUB)) {
                     hoModel.setVerein(new Verein(properties));
                 }
                 //team
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.TEAM)) {
                     hoModel.setTeam(new Team(properties));
                 }
                 //lineup
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.LINEUP)) {
                     hoModel.setAufstellung(new Lineup(properties));
                 }
                 //economy
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.ECONOMY)) {
                     hoModel.setFinanzen(new Finanzen(properties));
                 }
                 //arena
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.ARENA)) {
                     hoModel.setStadium(new Stadium(properties));
                 }
                 //player
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.PLAYER)) {
                     hoModel.addSpieler(new Spieler(properties, hrfdate));
                 }
                 //Xtra
                 else if (entity.toString().equalsIgnoreCase(HRFFileParser.XTRA)) {
                     hoModel.setXtraDaten(new XtraData(properties));
                     trainerID = Integer.parseInt(properties.getProperty("trainerid", "-1").toString());
                 } else if (entity.toString().equalsIgnoreCase(HRFFileParser.LASTLINEUP)) {
                     hoModel.setLastAufstellung(new Lineup(properties));
                 }
                 //Unbekannt
                 else {
                     //Ignorieren!
                     HOLogger.instance().log(getClass(),"Unbekannte Entity: " + entity.toString());
                 }
             } else {
                 HOLogger.instance().log(getClass(),"Fehlerhafte Datei / Keine Entity gefunden");
                 return null;
             }
         }
 
         //Only keep trainerinformation for player equal to trainerID, rest is resetted . So later trainer could be found by searching for player having trainerType != -1
         if (trainerID > -1) {
             for (int i = 0;
                  (hoModel.getAllSpieler() != null) && (i < hoModel.getAllSpieler().size()); i++) {
                 if (((Spieler) hoModel.getAllSpieler().elementAt(i)).isTrainer()
                     && ((((Spieler) hoModel.getAllSpieler().elementAt(i))).getSpielerID() != trainerID)) {
                     (((Spieler) hoModel.getAllSpieler().elementAt(i))).setTrainer(-1);
                     (((Spieler) hoModel.getAllSpieler().elementAt(i))).setTrainerTyp(-1);
                 }
             }
         }
 
         return hoModel;
     }
 
     //--------------------Debug-------------------------------------------    
 
     /**
      * Debugfunktion: Ausgabe aller Properties
      *
      * @param propertiesVector TODO Missing Constructuor Parameter Documentation
      */
     /*private void printProperties(Vector<Properties> propertiesVector) {
         for (int i = 0; i < propertiesVector.size(); i++) {
             final Properties properties = (Properties) propertiesVector.get(i);
             final Enumeration<Object> keys = properties.keys();
             final Enumeration<Object> elements = properties.elements();
 
             while (keys.hasMoreElements() && elements.hasMoreElements()) {
                 final String key = keys.nextElement().toString();
                 final int blanks = 25 - key.length();
 
                 HOLogger.instance().log(getClass(),key);
 
                 for (int j = 0; j < blanks; j++) {
                     HOLogger.instance().log(getClass()," ");
                 }
 
                 HOLogger.instance().log(getClass(),":\t" + elements.nextElement().toString());
             }
 
         }
     }*/
 }
