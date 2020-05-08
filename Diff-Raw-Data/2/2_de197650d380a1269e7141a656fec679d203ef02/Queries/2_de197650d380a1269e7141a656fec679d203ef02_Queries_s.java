 package de.htw.queries;
 
 import business.model.KursMitDetails;
 import business.model.Sportangebot;
 import business.model.TerminDetails;
 import business.model.ontology.KoerperlicheEinschraenkungen;
 import business.model.ontology.Ziele;
 import de.htw.datenbankverbindung.DAOFactory;
 import de.htw.datenbankverbindung.KursDAO;
 import de.htw.datenbankverbindung.TerminDAO;
 import de.htw.gui.Choices;
 import de.htw.gui.TimeFrameChooser.TimeFrame;
 import de.htw.ontologieverbindung.OntoUtil;
 import de.htw.ontologieverbindung.OntolgyConnection;
 import org.semanticweb.owlapi.model.OWLClass;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 public class Queries {
 
     private static OntolgyConnection ontolgy   = OntolgyConnection.getInstance();
     private static Connection        database  = DAOFactory.getInstance()
             .getConnection();
     private static KursDAO           kursDao   = new KursDAO();
     private static TerminDAO         terminDAO = new TerminDAO();
 
     /**
      * Gibt die Menge der Sportarten zurück, die sowohl in der Onto als auch in
      * der DB sind zurück. </br> Warum? Onto gibt wegen OpenWorld Konzept nicht
      * nur Sportarten zurück sondern auch verschiedene Äquivalenzklassen etc...
      * DB hingegen enthält wesentlich mehr Sportarten als die Ontology. Es ist
      * also auch ein kleiner Test was wir überhaupt abdecken.
      *
      * @return
      */
     public static Map<String, Sportangebot> querySport() {
         Set<OWLClass> fetchedClasses = ontolgy.doQuery(Sportangebot.ONTO_CLASS);
         String query = "SELECT " + Sportangebot.ID + ", " + Sportangebot.NAME
                 + " FROM " + Sportangebot.TABLE;
         ResultSet results = null;
         Map<String, Sportangebot> sportClasses = new HashMap<String, Sportangebot>();
         try {
             results = database.createStatement().executeQuery(query);
             while (results.next()) {
                 Integer sportID = results.getInt(Sportangebot.ID);
                 String sportName = results.getString(Sportangebot.NAME);
                 for (OWLClass sportclass : fetchedClasses) {
                     if (OntoUtil.getShortForm(sportclass).equals(sportName)) {
                         sportClasses.put(sportName, new Sportangebot(sportID,
                                                                      sportName));
                         break;
                     }
                 }
             }
         }
         catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return sportClasses;
     }
 
     /**
      * Gibt die Einzelsportarten zurück die sowohl in der Onto als auch in
      * inputClasses enthalten sind.
      *
      * @param inputClasses
      * @return
      */
     public static Map<String, Sportangebot> queryEinzelsport(
             Map<String, Sportangebot> inputClasses) {
         String query = "Einzelsport "
                 + createAccepableClassesCondition_Ontology(inputClasses);
         Set<OWLClass> fetchedClasses = ontolgy.doQuery(query);
 
         return removeUnacceptableClasses(inputClasses, fetchedClasses);
     }
 
     /**
      * Gibt die Teamsportarten zurück die sowohl in der Onto als auch in
      * inputClasses enthalten sind.
      *
      * @param inputClasses
      * @return
      */
     public static Map<String, Sportangebot> queryTeamsport(
             Map<String, Sportangebot> inputClasses) {
         String query = "Teamsport "
                 + createAccepableClassesCondition_Ontology(inputClasses);
         Set<OWLClass> fetchedClasses = ontolgy.doQuery(query);
 
         return removeUnacceptableClasses(inputClasses, fetchedClasses);
     }
 
     /**
      * Filtert die Sportarten aus inputClasses raus, die man NICHT mit den
      * angegebenen Einschränkungen machen kann.
      *
      * @param inputClasses
      * @param koerperlicheEinschraenkungen
      * @return
      */
     public static Map<String, Sportangebot> queryFilterKoerperlicheEinschraenkungen(
             Map<String, Sportangebot> inputClasses,
             KoerperlicheEinschraenkungen... koerperlicheEinschraenkungen) {
         StringBuilder query = new StringBuilder("Sport ");
         for (KoerperlicheEinschraenkungen k : koerperlicheEinschraenkungen) {
             query.append("and not (ungeeignetBei some ");
             query.append(k.getName());
             query.append(") ");
         }
         query.append(createAccepableClassesCondition_Ontology(inputClasses));
 
         Set<OWLClass> fetchedClasses = ontolgy.doQuery(query.toString());
         return removeUnacceptableClasses(inputClasses, fetchedClasses);
     }
 
     /**
      * Filter welche der Sportarten ein oder mehrere Ziele verfolgen
      *
      * @param inputClasses
      * @param ziele
      * @return
      */
     public static Map<String, Sportangebot> queryZiele(
             Map<String, Sportangebot> inputClasses, Ziele... ziele) {
         StringBuilder query = new StringBuilder("Sport ");
         for (Ziele ziel : ziele) {
             query.append(" and hatZiel some ");
             query.append(ziel.getName());
         }
         query.append(createAccepableClassesCondition_Ontology(inputClasses));
 
         Set<OWLClass> fetchedClasses = ontolgy.doQuery(query.toString());
         return removeUnacceptableClasses(inputClasses, fetchedClasses);
     }
 
     /**
      * Filtert die Sportarten nach den Angegeben Attributen.
      *
      * @param inputClasses
      * @param kampfsport
      * @param exotisch
      * @param koerperbetont
      * @param wassersport
      * @return
      */
     public static Map<String, Sportangebot> query4Attributes(
             Map<String, Sportangebot> inputClasses, Choices kampfsport,
             Choices exotisch, Choices koerperbetont, Choices wassersport) {
 
         String queryBegin = "Sport";
         StringBuilder query = new StringBuilder(queryBegin);
 
         if (kampfsport != Choices.EGAL) {
             query.append(" and istKampfsport only "
                                  + kampfsport.getOntoEquivalent());
         }
 
         if (exotisch != Choices.EGAL) {
             query.append(" and istExotisch only "
                                  + exotisch.getOntoEquivalent());
         }
 
         if (koerperbetont != Choices.EGAL) {
             query.append(" and istKoerperkontakt only "
                                  + koerperbetont.getOntoEquivalent());
         }
 
         if (wassersport != Choices.EGAL) {
             query.append(" and istWassersport only "
                                  + wassersport.getOntoEquivalent());
         }
 
        if (query.equals(queryBegin)) {
             return inputClasses;
         } else {
             query.append(createAccepableClassesCondition_Ontology(inputClasses));
 
             Set<OWLClass> fetchedClasses = ontolgy.doQuery(query.toString());
             return removeUnacceptableClasses(inputClasses, fetchedClasses);
         }
     }
 
     /**
      * Gibt die Sportarten zurück die in inputClasses stehen und höchsten
      * maxPrice kosten.
      *
      * @param inputClasses
      * @param maxPrice
      * @return
      */
     public static Map<String, Sportangebot> queryPrice(
             Map<String, Sportangebot> inputClasses, double maxPrice) {
         String query = "SELECT DISTINCT Sportangebot.name FROM "
                 + " Sportangebot, Kurs WHERE "
                 + " Sportangebot.idSportangebot = Kurs.idSportangebot "
                 + "AND kosten <= " + maxPrice
                 + createAccepableClassesCondition_DB(inputClasses);
 
         return queryDB(inputClasses, query);
     }
 
     /**
      * Filtert ob eine SPortart innen oder außen ist
      *
      * @param inputClasses
      * @param indoor
      * @return
      */
     public static Map<String, Sportangebot> queryIndoor(
             Map<String, Sportangebot> inputClasses, boolean indoor) {
         int indoor_db = indoor ? 1 : 0;
         String query = "SELECT DISTINCT Sportangebot.name FROM "
                 + " Sportangebot, Kurs, Ort WHERE "
                 + " Sportangebot.idSportangebot = Kurs.idSportangebot "
                 + " AND Kurs.idOrt = Ort.idOrt " + " AND Ort.innen = "
                 + indoor_db + createAccepableClassesCondition_DB(inputClasses);
 
         return queryDB(inputClasses, query);
 
     }
 
     public static Map<String, Sportangebot> queryTimeFrames(Map<String, Sportangebot> inputClasses,
                                                             List<TimeFrame> timeFrames) {
         String query = "SELECT DISTINCT Sportangebot.name FROM Sportangebot, Kurs k, Termine t WHERE k.idKurs = t.idKurs and Sportangebot" +
                 ".idSportangebot = k.idSportangebot and (";
         Boolean firstOneDone = false;
         SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
         for (TimeFrame tf : timeFrames) {
             if (!firstOneDone) {
                 firstOneDone = true;
             } else {
                 query += " or ";
             }
             query += "((t.anfangszeit >= Time(\"" + sdf.format(tf.getStartTime()) + "\") and t.endzeit <= TIME(\"" +
                     sdf.format(tf.getEndTime()) + "\") and idWochentag = " + tf.getDay().getId() + "))";
         }
         query += ")" + createAccepableClassesCondition_DB(inputClasses);
         return queryDB(inputClasses, query);
     }
 
     /**
      * führt eine Query auf der DB aus und filtert die nicht akzeptierbaren
      * Resultate aus inputClasses
      *
      * @param inputClasses
      * @param query
      * @return
      */
     private static Map<String, Sportangebot> queryDB(
             Map<String, Sportangebot> inputClasses, String query) {
         System.out.println("DB: " + query);
         ResultSet results = null;
         try {
             results = database.createStatement().executeQuery(query);
         }
         catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return removeUnacceptableClasses(inputClasses, results);
     }
 
     /**
      * Filtert die Sportarten aus originalClasses raus, die sich nicht in
      * acceptableClasses befinden. </br> Diese Methode wird für Queries auf der
      * Onotlogie benutzt um eine Menge aus Sportangeboten zu erstellen.
      *
      * @param sportClasses
      * @param acceptableClasses
      * @return gefiltertes sportClasses
      */
     private static Map<String, Sportangebot> removeUnacceptableClasses(
             Map<String, Sportangebot> originalClasses,
             Set<OWLClass> acceptableClasses) {
         Map<String, Sportangebot> sportangebote = new HashMap<String, Sportangebot>();
         for (OWLClass fetchedClass : acceptableClasses) {
             String sportName = OntoUtil.getShortForm(fetchedClass);
             if (originalClasses.containsKey(sportName)) {
                 sportangebote.put(sportName, originalClasses.get(sportName));
             }
         }
         return sportangebote;
     }
 
     /**
      * Filtert die Sportarten aus originalClasses raus, die sich nicht in
      * acceptableClasses befinden. </br> Diese Methode wird für Queries auf der
      * Datenbank benutzt um eine Menge aus Sportangeboten zu erstellen.
      *
      * @param sportClasses
      * @param acceptableClasses
      * @return gefiltertes sportClasses
      */
     private static Map<String, Sportangebot> removeUnacceptableClasses(
             Map<String, Sportangebot> originalClasses,
             ResultSet acceptableClasses) {
         if (acceptableClasses == null) {
             return originalClasses;
         }
 
         Map<String, Sportangebot> sportangebote = new HashMap<String, Sportangebot>();
         try {
             while (acceptableClasses.next()) {
                 String sportName = acceptableClasses
                         .getString("Sportangebot.name");
                 sportangebote.put(sportName, originalClasses.get(sportName));
             }
         }
         catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         return sportangebote;
     }
 
     /**
      * Condition Anhang an die DB-Query </br> Select * from xy where ... </br>
      * AND ( Sportangebot.name = 'Basketball'</br> OR Sportangebot.name =
      * 'Volleyball'</br> ... )
      *
      * @param inputClasses
      * @return
      */
     private static String createAccepableClassesCondition_DB(
             Map<String, Sportangebot> inputClasses) {
         StringBuilder condition = new StringBuilder();
         boolean first = true;
         for (String sportName : inputClasses.keySet()) {
             if (first) {
                 condition.append(" AND ( ");
                 first = false;
             } else {
                 condition.append(" OR ");
             }
             condition.append(" Sportangebot.name = '");
             condition.append(sportName);
             condition.append("'" + System.lineSeparator());
         }
         condition.append(" )");
 
         return condition.toString();
     }
 
     /**
      * Condition Anhang an die Onto-Query </br> bla and (Basketball or
      * Volleyball or ... )
      *
      * @param inputClasses
      * @return
      */
     private static String createAccepableClassesCondition_Ontology(
             Map<String, Sportangebot> inputClasses) {
         //in case inputClasses is empty
         if (inputClasses.size() == 0) {
             return " and not Sport";
         }
         StringBuilder condition = new StringBuilder(" and (");
         int i = 0;
         for (String sportangebot : inputClasses.keySet()) {
             condition.append(" " + sportangebot);
             if (i < inputClasses.size() - 1) {
                 condition.append(" or ");
             }
             i++;
         }
         condition.append(")");
         return condition.toString();
     }
 
     public static String getDetailString(Sportangebot sport) {
         List<KursMitDetails> kurse = new ArrayList<KursMitDetails>();
         kurse = kursDao.findAllKurseByIdSportangebot(sport.getIdSportangebot());
         return printKursMitDetails(kurse);
     }
     
     
 
 
     private static String printKursMitDetails(List<KursMitDetails> kurse) {
         String ausgaben = "";
         List<TerminDetails> termine = new ArrayList<TerminDetails>();
         String termine_details = "";
 
         for (int i = 0; i < kurse.size(); i++) {
         	KursMitDetails kurs = kurse.get(i);
             termine = terminDAO.findAllTerminByIdKurs(kurs.getIdKurs());
             termine_details = printTermineMitDetails(termine);
             ausgaben += kurs.toString().replaceAll("\t", " ") + "<br/>" + termine_details;
             if (i != kurse.size() - 1){
             	 ausgaben += "<hr><br/>";	
             }
            
         }
         ausgaben += "<br/>";
         return ausgaben;
     }
 
     private static String printTermineMitDetails(List<TerminDetails> termine) {
         String ausgaben = "<ul>";
         for (TerminDetails termin : termine) {
             ausgaben += "<li>" + termin.toString() + "</li>" ;
         }
         ausgaben += "</ul>";
         return ausgaben;
     }
 
 }
