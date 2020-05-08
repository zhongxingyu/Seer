 package model;
 
 import gui.components.LikeBox;
 import gui.components.PersonAgeComboBox;
 import gui.steps.DistanceStep;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.management.InstanceAlreadyExistsException;
 import javax.swing.SwingUtilities;
 
 import org.semanticweb.owlapi.model.OWLOntologyCreationException;
 import org.semanticweb.owlapi.model.OWLOntologyStorageException;
 
 import ontologyAndDB.OntToDbConnection;
 import ontologyAndDB.exception.OWLConnectionUnknownTypeException;
 import ontologyAndDB.exception.OntologyConnectionDataPropertyException;
 import ontologyAndDB.exception.OntologyConnectionIndividualAreadyExistsException;
 import ontologyAndDB.exception.OntologyConnectionUnknowClassException;
 import ontologyAndDB.exception.ViewDoesntExistsException;
 
 import model.steps.BudgetStepModel;
 import model.steps.DistanceStepModel;
 import model.steps.DurationStepModel;
 import model.steps.EventCategoryStepModel;
 import model.steps.GenreSelectionStepModel;
 import model.steps.KindOfEventSelectionStepModel;
 import model.steps.OriginLocationStepModel;
 import model.steps.PersonAgeStepModel;
 import model.steps.PersonDescriptionStepModel;
 import model.steps.SchoolLocationStepModel;
 import model.steps.TimeRangeStepModel;
 import model.steps.VoyageMethodStepModel;
 
 /**
  * Helper Class used in the model class
  */
 public class EventCollector {
 	private static PersonDescriptionStepModel personDescriptionStep = PersonDescriptionStepModel
 			.getInstance();
 	private static TimeRangeStepModel timeRangeStep = TimeRangeStepModel
 			.getInstance();
 	private static DurationStepModel durationStep = DurationStepModel
 			.getInstance();
 	private static SchoolLocationStepModel schoolLocationStep = SchoolLocationStepModel
 			.getInstance();
 	private static PersonAgeStepModel personAgeStep = PersonAgeStepModel
 			.getInstance();
 	private static VoyageMethodStepModel voyageMethodStep = VoyageMethodStepModel
 			.getInstance();
 	private static DistanceStepModel distanceStep = DistanceStepModel
 			.getInstance();
 	private static OriginLocationStepModel originLocationStep = OriginLocationStepModel
 			.getInstance();
 	private static KindOfEventSelectionStepModel kindOfEventSelectionStepModel = KindOfEventSelectionStepModel
 			.getInstance();
 	private static EventCategoryStepModel eventCategoryStepModel = EventCategoryStepModel
 			.getInstance();
 	private static GenreSelectionStepModel genreSelectionStepModel = GenreSelectionStepModel
 			.getInstance();
 	private static BudgetStepModel budgetStepModel = BudgetStepModel
 			.getInstance();
 	private OntToDbConnection ontToDbConnection;
 	private ArrayList<Integer> eventIDs = new ArrayList<Integer>();
 	private static EventCollector instance = null;
 
 	private EventCollector() {
 		ontToDbConnection = OntToDbConnection.getInstance();
 		ontToDbConnection.removeAllIndividuals();
 	}
 
 	public static EventCollector getInstance() {
 		if (instance == null) {
 			instance = new EventCollector();
 		}
 		return instance;
 	}
 
 	/**
 	 * looks up the asked data from all step models and returns a list of events
 	 */
 	public HashMap<String, String>[] getEvents() {
 		return getDatabaseData(eventIDs);
 	}
 
 	public HashMap<String, String>[] getDatabaseData(ArrayList<Integer> eventIDs) {
 		if (eventIDs != null && !eventIDs.isEmpty()) {
 			try {
 				final ResultSet rs = ontToDbConnection
 						.getDataFromDbByEvent_Id(eventIDs);
 				ArrayList<HashMap<String, String>> list = null;
 				if (rs != null) {
 
 					final int columns = rs.getMetaData().getColumnCount();
 					list = new ArrayList<HashMap<String, String>>();
 
 					while (rs.next()) {
 
 						final HashMap<String, String> tmprow = new HashMap<String, String>();
 
 						for (int i = 0; i < columns; i++) {
 							final String key = rs.getMetaData().getColumnLabel(
 									i + 1);
 							tmprow.put(key, rs.getString(key));
 						}
 
 						list.add(tmprow);
 					}
 				}
 				return list.toArray(new HashMap[0]);
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 		}
 		return null;
 	}
 
 	public void setEventIDs() {
 		// ontToDbConnection.removeAllIndividuals();
 		ArrayList<Integer> tmpEventIDs = new ArrayList<Integer>();
 
 		// Holiday-View-Setzen
		setHolidayView();
 
 		// Distance view setzen...
		setDistanceView();
		
		ontToDbConnection.reopenOntology();
 
 		// Duration-Ontologie
 		Set<Integer> durationSet = null;
 		durationSet = new HashSet<Integer>(
 				ontToDbConnection
 						.getInvidualsFromOntologieClassByReasoner(durationStep
 								.getDuration()));
 
 		// Ontologie Class Restrictions for Culture Events
 		Set<Integer> cultureEvents = null;
 		if (!kindOfEventSelectionStepModel.getCultureStatus().equals(
 				KindOfEventSelectionStepModel.DONTLIKE)) {
 			cultureEvents = calculateCultureEvents();
 		}
 
 		// Ontologie Class Restrictions for LeisureTimeEvents
 		Set<Integer> leisureTimeEvents = null;
 		if (!kindOfEventSelectionStepModel.getLeisureTimeStatus().equals(
 				KindOfEventSelectionStepModel.DONTLIKE)) {
 			leisureTimeEvents = calculateLeisureTimeEvents();
 		}
 
 		// Ontologie Class Restrictions for SportEvents
 		Set<Integer> sportEvents = null;
 		if (!kindOfEventSelectionStepModel.getSportStatus().equals(
 				KindOfEventSelectionStepModel.DONTLIKE)) {
 			sportEvents = calculateSportEvents();
 		}
 
 		// duration && (sport || culture || leisuretime)
 
 		final HashSet<Integer> tmp = new HashSet<Integer>();
 		if (cultureEvents != null && !cultureEvents.isEmpty()) {
 			tmp.addAll(cultureEvents);
 		}
 		if (sportEvents != null && !sportEvents.isEmpty()) {
 			tmp.addAll(sportEvents);
 		}
 		if (leisureTimeEvents != null && !leisureTimeEvents.isEmpty()) {
 			tmp.addAll(leisureTimeEvents);
 		}
 
 		durationSet.retainAll(tmp);
 
 		this.eventIDs = new ArrayList<Integer>(
 				calulateBudgetRestriction(durationSet));
 	}
 
 	private HashSet<Integer> calulateBudgetRestriction(Set<Integer> eventSet) {
 		final HashSet<Integer> eventIdsWithBudgetRestriction = new HashSet<Integer>();
 		final double budget = Double.parseDouble(budgetStepModel.getBudget());
 		boolean childPriceNeeded = false;
 		int childs = 0;
 		int normalPersons =0;
 		final String[] personAges = personAgeStep.getAgesIncludingOrderingPerson();
 		
 		for(int i=0; i< personAges.length;i++){
 			if(personAges[i].equals(PersonAgeComboBox.CHILD)){
 				childPriceNeeded=true;
 				childs++;
 			}
 		}
 
 		for (Integer eventId : eventSet) {
 			// check if the BudgetRestriction for this event is fullfilled
 			final ArrayList<Integer> tmp = new ArrayList<Integer>();
 			tmp.add(eventId);
 			ResultSet rs = ontToDbConnection.getDataFromDbByEvent_Id(tmp);
 			double childPrice = 0;
 			double normalPrice = 0;
 			try {
 				while (rs.next()) {
 					if (childPriceNeeded) {
 						Double d = rs.getDouble("kinder");
 						if (d != null) {
 							
 							childPrice = Math.round(d);
 						}
 					}
 					Double d = rs.getDouble("erwachsene");
 					if (d != null) {
 						normalPrice = Math.round(d);
 					}
 				}
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
 
 			if ((childs * childPrice) + (personAges.length - childs)
 					* normalPrice <= budget) {
 				eventIdsWithBudgetRestriction.add(eventId);
 			}
 		}
 
 		return eventIdsWithBudgetRestriction;
 	}
 
 	private Set<Integer> calculateLeisureTimeEvents() {
 		// leisureTime && (festivity || ...|| ...)
 		Set<Integer> leisureTimeEvents = null;
 		leisureTimeEvents = new HashSet<Integer>(
 				ontToDbConnection
 						.getInvidualsFromOntologieClassByReasoner("LeisureTimeEvent"));
 
 		final HashMap<String, String> leisureTimeEventCategories = eventCategoryStepModel
 				.getLeisureTimeCategories();
 		final Iterator<String> it = leisureTimeEventCategories.keySet()
 				.iterator();
 		final ArrayList<String> leisureTimeCategoryClassNames = new ArrayList<String>();
 		while (it.hasNext()) {
 			final String leisureTimeCategory = it.next();
 			final String status = leisureTimeEventCategories
 					.get(leisureTimeCategory);
 			if (!status.equals(LikeBox.DONTLIKE)) {
 				leisureTimeCategoryClassNames.add(leisureTimeCategory);
 			}
 		}
 		Set<Integer> sportCategoryEvents = null;
 		sportCategoryEvents = new HashSet<Integer>(
 				ontToDbConnection
 						.getIndividualUnionOverClassesByReasoner(leisureTimeCategoryClassNames));
 		leisureTimeEvents.retainAll(sportCategoryEvents);
 
 		return leisureTimeEvents;
 	}
 
 	private Set<Integer> calculateSportEvents() {
 		// sport && (running || motorsport|| ...)
 		Set<Integer> sportEvents = null;
 		sportEvents = new HashSet<Integer>(
 				ontToDbConnection
 						.getInvidualsFromOntologieClassByReasoner("SportEvents"));
 
 		final HashMap<String, String> sportEventCategories = eventCategoryStepModel
 				.getSportCategories();
 		final Iterator<String> it = sportEventCategories.keySet().iterator();
 		final ArrayList<String> sportCategoryClassNames = new ArrayList<String>();
 		while (it.hasNext()) {
 			final String sportCategory = it.next();
 			final String status = sportEventCategories.get(sportCategory);
 			if (!status.equals(LikeBox.DONTLIKE)) {
 				sportCategoryClassNames.add(sportCategory);
 			}
 		}
 		Set<Integer> sportCategoryEvents = null;
 		sportCategoryEvents = new HashSet<Integer>(
 				ontToDbConnection
 						.getIndividualUnionOverClassesByReasoner(sportCategoryClassNames));
 		sportEvents.retainAll(sportCategoryEvents);
 
 		return sportEvents;
 	}
 
 	private Set<Integer> calculateCultureEvents() {
 		// culture && ((cinema && (genre1 || genre2)) || concert&&(genre ||
 		// genre ...) || theatre&&(genre || genre ...))
 
 		final Set<Integer> tmp = new HashSet<Integer>();
 		Set<Integer> cultureEventsSet = null;
 		cultureEventsSet = new HashSet<Integer>(
 				ontToDbConnection
 						.getInvidualsFromOntologieClassByReasoner("CultureEvent"));
 
 		final HashMap<String, String> culturEventCategories = eventCategoryStepModel
 				.getCultureCategories();
 
 		final Iterator<String> it = culturEventCategories.keySet().iterator();
 		Set<Integer> cinemaEvents = new HashSet<Integer>();
 		Set<Integer> concertEvents = new HashSet<Integer>();
 		Set<Integer> theatreEvents = new HashSet<Integer>();
 
 		while (it.hasNext()) {
 			final String currentCategory = it.next();
 			final String status = culturEventCategories.get(currentCategory);
 			if (!status.equals(LikeBox.DONTLIKE)) {
 				if (currentCategory.equals("CinemaEvent")) {
 					cinemaEvents = new HashSet<Integer>(
 							ontToDbConnection
 									.getInvidualsFromOntologieClassByReasoner("CinemaEvent"));
 					// here we now that cinema events are marked as "LIKE"
 					// so we have to care about genres
 					final ArrayList<String> cinemaGenreClassNames = new ArrayList<String>();
 					Set<Integer> cinemaGenreEvents = null;
 					// iterate through the genres that are selected...
 					final HashMap<String, String> cinemaGenresLikeStatusMap = genreSelectionStepModel
 							.getCinemaGenres();
 					final Iterator<String> cinemaGenreKeys = cinemaGenresLikeStatusMap
 							.keySet().iterator();
 					while (cinemaGenreKeys.hasNext()) {
 						final String cinemaGenre = cinemaGenreKeys.next();
 						final String likeStatus = cinemaGenresLikeStatusMap
 								.get(cinemaGenre);
 						if (!likeStatus.equals(LikeBox.DONTLIKE)) {
 							cinemaGenreClassNames.add(cinemaGenre);
 						}
 					}
 					cinemaGenreEvents = new HashSet<Integer>(
 							ontToDbConnection
 									.getIndividualUnionOverClassesByReasoner(cinemaGenreClassNames));
 					// cinema && (genre 1 || genre 2...)
 					cinemaEvents.retainAll(cinemaGenreEvents);
 
 				}
 				else if (currentCategory.equals("ConcertEvent")) {
 					// here we now that concert events are marked as "LIKE"
 					// so we have to care about genres
 					concertEvents = new HashSet<Integer>(
 							ontToDbConnection
 									.getInvidualsFromOntologieClassByReasoner("ConcertEvent"));
 					final ArrayList<String> concertGenreClassNames = new ArrayList<String>();
 					Set<Integer> concertGenreEvents = null;
 					// iterate through the genres that are selected...
 					final HashMap<String, String> concertGenresLikeStatusMap = genreSelectionStepModel
 							.getConcertGenres();
 					final Iterator<String> concertGenreKeys = concertGenresLikeStatusMap
 							.keySet().iterator();
 					while (concertGenreKeys.hasNext()) {
 						final String concertGenre = concertGenreKeys.next();
 						final String likeStatus = concertGenresLikeStatusMap
 								.get(concertGenre);
 						if (!likeStatus.equals(LikeBox.DONTLIKE)) {
 							concertGenreClassNames.add(concertGenre);
 						}
 					}
 					concertGenreEvents = new HashSet<Integer>(
 							ontToDbConnection
 									.getIndividualUnionOverClassesByReasoner(concertGenreClassNames));
 					// concert && (genre 1 || genre 2...)
 					concertEvents.retainAll(concertGenreEvents);
 				}
 				else if (currentCategory.equals("TheatreEvent")) {
 					// here we now that theatre events are marked as "LIKE"
 					// so we have to care about genres
 					theatreEvents = new HashSet<Integer>(
 							ontToDbConnection
 									.getInvidualsFromOntologieClassByReasoner("TheatreEvent"));
 					final ArrayList<String> theatreGenreClassNames = new ArrayList<String>();
 					Set<Integer> theatreGenreEvents = null;
 					// iterate through the genres that are selected...
 					final HashMap<String, String> theatreGenresLikeStatusMap = genreSelectionStepModel
 							.getTheatreGenres();
 					final Iterator<String> theatreGenreKeys = theatreGenresLikeStatusMap
 							.keySet().iterator();
 					while (theatreGenreKeys.hasNext()) {
 						final String theatreGenre = theatreGenreKeys.next();
 						final String likeStatus = theatreGenresLikeStatusMap
 								.get(theatreGenre);
 						if (!likeStatus.equals(LikeBox.DONTLIKE)) {
 							theatreGenreClassNames.add(theatreGenre);
 						}
 					}
 					theatreGenreEvents = new HashSet<Integer>(
 							ontToDbConnection
 									.getIndividualUnionOverClassesByReasoner(theatreGenreClassNames));
 					// theatre && (genre 1 || genre 2...)
 					theatreEvents.retainAll(theatreGenreEvents);
 				}
 				else{
 					final ArrayList<Integer> miscCultureEvents = new ArrayList<Integer>(ontToDbConnection
 							.getInvidualsFromOntologieClassByReasoner(currentCategory));							
 					
 					tmp.addAll(miscCultureEvents);
 				}
 				
 			}
 		}
 
 		// cinema &&(...) || concert && (...) || theatre&&(...)
 		tmp.addAll(cinemaEvents);
 		tmp.addAll(concertEvents);
 		tmp.addAll(theatreEvents);
 
 		// culture && (...)
 		cultureEventsSet.retainAll(tmp);
 
 		return cultureEventsSet;
 	}
 
 	public void setHolidayView() {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 
 		ontToDbConnection.setHolidayView(
 				sdf.format(timeRangeStep.getFromDate().getTime()),
 				sdf.format(timeRangeStep.getToDate().getTime()));
 	}
 
 	public void setDistanceView() {
 		ontToDbConnection.removeAllIndividuals();
 
 		final String distanceUnit = distanceStep.getUnit();
 		ArrayList<String> reachableCities = null;
 		try {
 			if (distanceUnit.equals(DistanceStepModel.DISTANCE_UNTI)) {
 
 				reachableCities = distanceStep
 						.getReachableCitiesByDistance(Double
 								.parseDouble(distanceStep.getDistance()));
 			} else {
 				reachableCities = distanceStep
 						.getReachableCitiesByTime(60 * 60 * 1000 * Double
 								.parseDouble(distanceStep.getDistance()));
 			}
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 		}
 
 		if (reachableCities != null) {
 			ontToDbConnection.setDistanceView(reachableCities);
 		}
 
 		ontToDbConnection.fillOntWithEventsFromDistanceView();
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				ontToDbConnection.InfereceAndSaveOntology();
 			}
 		});
 		
 	}
 
 }
