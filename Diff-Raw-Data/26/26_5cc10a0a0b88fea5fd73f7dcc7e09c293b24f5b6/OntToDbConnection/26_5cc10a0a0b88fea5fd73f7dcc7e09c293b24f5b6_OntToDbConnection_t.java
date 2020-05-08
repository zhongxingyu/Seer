 package ontologyAndDB;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Set;
 
 import model.steps.DistanceStepModel;
 import ontologyAndDB.exception.OWLConnectionUnknownTypeException;
 import ontologyAndDB.exception.OntologyConnectionDataPropertyException;
 import ontologyAndDB.exception.OntologyConnectionIndividualAreadyExistsException;
 import ontologyAndDB.exception.OntologyConnectionUnknowClassException;
 import ontologyAndDB.exception.ViewDoesntExistsException;
 
 import org.semanticweb.owlapi.model.OWLClass;
 import org.semanticweb.owlapi.model.OWLIndividual;
 import org.semanticweb.owlapi.model.OWLNamedIndividual;
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyStorageException;
 import org.semanticweb.owlapi.reasoner.NodeSet;
 import org.semanticweb.owlapi.util.OWLEntityRemover;
 
 
 
 public class OntToDbConnection {
 	
 	private static OntToDbConnection instance;
 	
 	private 	DBConnection 		dbCon;
 	private		OntologyConnection  ontCon;
 	private 	boolean				reachCitiesViewIsSet;
 	private		boolean				holidayViewIsSet;
 	
 	private final String HOLIDAY_VIEW_NAME ="HolidayView";
 	private final String REACHABLE_CITIES_VIEW_NAME ="ReachableCitiesView";
 	public static String THEATRE_GENRE = "TheatreGenre";
 	public static String Cinema_GENRE = "CinemaGenre";
 	public static String CONCERT_GENRE = "ConcertGenre";
 	
 	
 	/////////////////////////////////////////////////// Constructor //////////////////////////////////////////////////////
 	
	public static OntToDbConnection getInstance() throws OWLOntologyCreationException 
 	{
		if(instance == null)
		{
			instance=new OntToDbConnection();
			instance.openOntology("evntologie_latest.owl");
		}
		
		return instance;
 	}
 	
 	
 	private OntToDbConnection(){
 		 dbCon = new DBConnection();
 		 ontCon = new OntologyConnection();
 		 holidayViewIsSet=false;
 		 reachCitiesViewIsSet=false;
 	}
 	
 	/////////////////////////////////////////////////// Ontologie - Manipulation- Methods////////////////////////////////
 	
 	public void removeAllIndividuals (){
 		ontCon.removeAllIndividuals();
 	}
 	
 	public void openOntology(String ontologyFilePath) throws OWLOntologyCreationException{
 		ontCon.openOntology(ontologyFilePath);
 	}
 	public void InfereceAndSaveToFile(String owlFilePath) throws OWLOntologyCreationException, OWLOntologyStorageException{
 		ontCon.preAndSave(owlFilePath);
 	}
 	
 	public void InfereceAndSaveOntology() throws OWLOntologyCreationException, OWLOntologyStorageException{
 		ontCon.preAndSave();
 	}
 
 	public void fillOntWithAllEvents() throws SQLException, OntologyConnectionDataPropertyException, OWLConnectionUnknownTypeException, OntologyConnectionIndividualAreadyExistsException, OntologyConnectionUnknowClassException{	
 	  ResultSet rs = dbCon.executeQuery("Select * from \"Event\"");
 	  fillOntWithEvents(rs);
 	}
 	
 	public void fillOntWithEventsUntilNumber(int eventNumber) throws SQLException, OntologyConnectionDataPropertyException, OWLConnectionUnknownTypeException, OntologyConnectionIndividualAreadyExistsException, OntologyConnectionUnknowClassException{	
 		  ResultSet rs = dbCon.executeQuery("Select * from \"Event\" WHERE event_id < "+ eventNumber);
 		  fillOntWithEvents(rs);
 		}
 	
 	public void fillOntWithEventsFromHolidayView () throws SQLException, OntologyConnectionDataPropertyException, OWLConnectionUnknownTypeException, OntologyConnectionIndividualAreadyExistsException, OntologyConnectionUnknowClassException, ViewDoesntExistsException{
 		if (!holidayViewIsSet)
 				throw new ViewDoesntExistsException(HOLIDAY_VIEW_NAME + " doesnt exist yet");
 		ResultSet rs = dbCon.executeQuery("SELECT * FROM \""+HOLIDAY_VIEW_NAME+"\"");
 		 fillOntWithEvents(rs);
 	}
 	
 	private void fillOntWithEvents(ResultSet dataBaseEvents) throws SQLException, OntologyConnectionDataPropertyException, OWLConnectionUnknownTypeException, OntologyConnectionIndividualAreadyExistsException, OntologyConnectionUnknowClassException{	
 		  ResultSet rs2;
 		  ResultSet rs3;
 		  ResultSet rs4;
 		  while(dataBaseEvents.next()){
 			  // Individual erzeugen
 			  OWLNamedIndividual individ = ontCon.createIndividual(String.valueOf(dataBaseEvents.getInt("event_id")));
 			  // data propertie : Kinderbetreeung
 			  ontCon.setObjectPropertieToIndividual(individ, "hasChildCare", dataBaseEvents.getBoolean("kinderbetreuung"));
 			  // data propertie: childfriendly
 			  ontCon.setObjectPropertieToIndividual(individ, "isChildFriendly", dataBaseEvents.getBoolean("kinderfreundlich"));
 			  // data propertie : hasconcreteDuration 
 			  long timeDiff = dataBaseEvents.getTimestamp("enddatum").getTime() - dataBaseEvents.getTimestamp("startdatum").getTime();
 			  int durationInDays = (int)(timeDiff / 1000 / 3600 / 24) ;
 			  ontCon.setObjectPropertieToIndividual(individ, "hasConcreteDuration", durationInDays);
 			  // hinzufgen zur passenden Event-Klasse
 			  rs2 = (dbCon.executeQuery("select bezeichnung from \"Kategorie\" where kategorie_id="+  dataBaseEvents.getInt("kategorie") ));
 			  rs2.next();
 			  String eventKategorie = rs2.getString(1);
 			  ontCon.addIndividualToClass(individ, eventKategorie);
 			  // falls genre gesetzt ist , hinzufgen zur Genre-Klasse
 			  rs3 = (dbCon.executeQuery("select genre from \"Event_Genre\" where event=" + String.valueOf(dataBaseEvents.getInt("event_id") )));
 			  if (rs3.next()){
 			  int genreID = rs3.getInt(1);
 			  rs4 = (dbCon.executeQuery("select bezeichnung from \"Genre\" where genre_id=" + String.valueOf(genreID )));
 			  rs4.next();
 				  String genreKategorie = rs4.getString(1);
 				  ontCon.addIndividualToClass(individ, genreKategorie);
 			  }
 			  ontCon.saveOntologie();
 		  }	
 		}
 	
 	////////////////////////////////// Distance-Methods /////////////////////////////////////////////////////////////
 	
 	/**
 	 * Returns the Cities which occur in the DB
 	 * @throws SQLException 
 	 */
 	public ArrayList<String> getCitiesFromDB () throws SQLException{
 		ArrayList<String> cities = new ArrayList<String>();
 		ResultSet rs = dbCon.executeQuery("Select Distinct ort from \"Event\"");
 		while(rs.next()){
 			cities.add(rs.getString("ort"));
 		}
 		return cities;
 	}
 	
 	/**
 	 * Creates a view in the DB with all reachable Events
 	 * @param reachableCities all Cities that are reachable
 	 * @throws SQLException 
 	 */
 	public void setDistanceView (ArrayList<String> reachableCities) throws SQLException{
 		String sqlInStat = reachableCities.toString().replace("[","").replace("]","").trim();
 		String sqlStatement =  " SELECT * FROM \"Event\" WHERE ort IN ("+sqlInStat+"(" ;
 		dbCon.createView(REACHABLE_CITIES_VIEW_NAME, sqlStatement);
 		reachCitiesViewIsSet = true;
 		
 	}
 	
 	//////////////////////////////////////////////////////////// Time-Methods//////////////////////////////////////////
 	
 	public void setHolidayView (String startDate, String endDate) throws SQLException, ViewDoesntExistsException{
 		if ( !reachCitiesViewIsSet )
 			throw new ViewDoesntExistsException(REACHABLE_CITIES_VIEW_NAME+" hasnt been created yet");
 		String sqlStatement ="";
 		//TODO Implementieren der Zeitberecchnung und des passenden SQL statements
 		dbCon.createView(HOLIDAY_VIEW_NAME, sqlStatement);
 		holidayViewIsSet =true;
 	}
 	
 	//////////////////////////////////////////Reasoner Based Methods ////////////////////////////////////////////////////////
 	
 	/**
 	 *  Returns the direct Subclases of a Class
 	 * @param className the Name of the superclass
 	 * @return Non rekursive list of subclasses
 	 * @throws OntologyConnectionUnknowClassException 
 	 */
 	public ArrayList<String> getSubClassesOfClassByReasoner(String className) throws OntologyConnectionUnknowClassException{
 		return ontCon.getClassNamesOnly(ontCon.getSubClassesByReasoner(className));
 	}
 	
 	public ArrayList<String> getSuperClassesOfClassByReasoner ( String className) throws OntologyConnectionUnknowClassException{
 		return ontCon.getSuperClassesByReasoner(className);
 		
 	}
 	
 	public ArrayList<Integer> getInvidualsFromOntologieClassByReasoner (String className){
 		return ontCon.getEventIdsByClassByReasoner(className);
 	}
   
 	public ArrayList<Integer> getIndividualUnionOverClassesByReasoner (ArrayList<String> classNames) {
 		ArrayList<Integer> individs = new  ArrayList<Integer>();
 		for (String s : classNames){
 			individs.addAll(this.getInvidualsFromOntologieClassByReasoner(s));
 		}
 		return individs;
 	}
 	
 	public ArrayList<Integer> getIndividualIntersectionOverClassesByReasoner ( ArrayList<String> classNames) throws Exception {
 		if (classNames.size()<2)
 			throw new Exception("2 ore more Classes needed to make an intersection");
 		ArrayList<Integer> list1 ;
 		ArrayList<Integer> list2 ;
 		ArrayList<Integer> list3 = this.getInvidualsFromOntologieClassByReasoner(classNames.get(0));
 		for ( int i = 1; i < classNames.size(); i++ ){
 			list1 = list3;
 			list3 = new ArrayList<Integer>();
 			list2 = this.getInvidualsFromOntologieClassByReasoner(classNames.get(i));
 			for (Integer s : list1){
 				if (list2.contains(s))
 					list3.add(s);
 			}
 		}
 		return list3;
 	} 
 	
 	//////////////////////////////////////////Ontology Based Queries ////////////////////////////////////////////////////////
 	
 	public ArrayList<String> getSuperClassesOfClassFromOntology(String className) throws OntologyConnectionUnknowClassException{
 		return ontCon.getSuperClassesByClassFromOntology(className);
 	}
 	
 	public ArrayList<Integer> getEventIdsByClassByOntology (String className){
 		return ontCon.getEventIdsByClassByOntology(className);
 	}
 	
 	public  ArrayList<String> getSubClassesOfClassByOntology(String className) throws OntologyConnectionUnknowClassException{
 		return ontCon.getSubClassesByClassFromOntology(className);
 	}
 	//////////////////////////////////////////////////Database Methods//////////////////////////////////////////////////////////////////
 	
 		
 	public ResultSet getDataFromDbByEvent_Id ( ArrayList<Integer> eventIDs) throws SQLException{
 		String s = new String(" ");
 		int i ;
 		for ( i=0  ; i < eventIDs.size()-1;i++){
 			s = s.concat(String.valueOf(eventIDs.get(i))+"," );
 		}
 		s = s.concat(String.valueOf(eventIDs.get(i)));
 		return  dbCon.executeQuery("Select * from \"Event\" where \"event_id\" in (" + s + ")");
 	}
 	
 	public void disconnectFromDB (){
 		dbCon.disconnect();
 	}
 	
 	public ArrayList<Integer> EventsFromDBByMaxCosts ( int numberGrownUp , int numberChilden , int numberReducedCost , int MaxSum , ArrayList<Integer> searchField) throws SQLException{
 		ArrayList<Integer> events = new ArrayList<Integer>();
 		ResultSet rs = dbCon.executeQuery("Select * from \"Event\" where \"event_id\" in (" + searchField + ")");
 		while(rs.next()){
 			int event_id = rs.getInt("event_id");
 			ResultSet rs2 = dbCon.executeQuery("SELECT * FROM \"Preisliste\" WHERE event = " + event_id);
 			if ( rs2.getInt("kinder")*numberChilden + rs2.getInt("erwachsene")*numberChilden +rs2.getInt("ermaessigt")*numberReducedCost < MaxSum )
 				events.add(event_id);
 		}
 		return events;
 	}
 	
 	private ResultSet executeQuery (String sqlStatement)throws SQLException{		
 		return dbCon.executeQuery(sqlStatement);
 	}
 	
 
 	
 }
