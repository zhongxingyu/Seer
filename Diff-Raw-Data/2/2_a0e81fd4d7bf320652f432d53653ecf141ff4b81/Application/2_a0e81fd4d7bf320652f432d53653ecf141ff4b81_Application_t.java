 package controllers;
 
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Semaphore;
 
 import model.BallotEntry;
 import model.Constituency;
 import model.DataCache;
 import model.Party;
 import model.VoteAggregate;
 import play.mvc.Controller;
 import play.mvc.Result;
 import views.html.bundestagmembers;
 import views.html.constituencyballot;
 import views.html.constituencyoverview;
 import views.html.constituencyoverview_json;
 import views.html.constituencywinners;
 import views.html.excessmandates;
 import views.html.narrowwinners;
 import views.html.narrowwinners_json;
 import views.html.overview;
 import views.html.seatdistribution;
 import views.html.ballotcode;
 import views.html.message;
 
 import common.DataManagerThread;
 import common.db.DBConnect;
 
 public class Application extends Controller {
 
 	/**
 	 * The interval time of updating the data cache (in seconds) [10min = 600s]
 	 * 
 	 */
 	private static final int CACHE_UPDATE_INTERVAL = 600;
 
 	/**
 	 * The thread for managing data
 	 */
 	private static DataManagerThread dataManager = null;
 
 	/**
 	 * Connection to the database
 	 */
 	private static DBConnect db = null;
 
 	/**
 	 * Read GET-parameter "cache" to determine if cache should be used
 	 */
 	private static boolean use_cache() {
 		Map<String, String[]> query = request().queryString();
 		if (query.get("cache") != null && query.get("cache").length > 0)
 			if (query.get("cache")[0].equals("false")
 					|| query.get("cache")[0].equals("0"))
 				return false;
 			else
 				return true;
 		else
 			return true;
 	}
 
 	/**
 	 * Index
 	 */
 	public static Result index() {
 		initializeDataManager();
 		return redirect("/ueberblick/deutschland");
 	}
 
 	/**
 	 * Overview
 	 */
 	public static Result overview() {
 		initializeDataManager();
 		List<VoteAggregate> votes = DataCache.getVoteAggregates(use_cache());
 		return ok(overview
 				.render("Übersicht Deutschland", votes));
 	}
 
 	/**
 	 * Constituency Overview
 	 */
 	public static Result constituencyoverview() {
 		initializeDataManager();
 		List<Constituency> constituencies = DataCache
 				.getConstituencies(use_cache());
 		if (!constituencies.get(0).getName().equals("--- bitte waehlen ---"))
 			constituencies.add(0, new Constituency(0, "--- bitte waehlen ---"));
 		return ok(constituencyoverview.render("Übersicht Wahlkreise",
 				constituencies));
 	}
 
 	public static Result constituencyoverview_json(int constituency) {
 		initializeDataManager();
 		if (constituency == 0) {
 			return badRequest("no constituency ID was provided");
 		}
 		return ok(constituencyoverview_json.render(
 				DataCache.getPartyFirstVotes(constituency, use_cache()),
 				DataCache.getPartySecondVotes(constituency, use_cache()),
 				DataCache.getConstituencyInfo(constituency, use_cache())));
 	}
 
 	/**
 	 * Constituency winners
 	 */
 	public static Result constituencywinners() {
 		initializeDataManager();
 		return ok(constituencywinners.render("Wahlkreissieger",
 				DataCache.getConstituencyWinners(use_cache())));
 	}
 
 	/**
 	 * Narrow Winners and Losers
 	 */
 	public static Result narrowwinners() {
 		initializeDataManager();
 		List<Party> parties = DataCache.getParties(use_cache());
 		if (!parties.get(0).getName().equals("--- bitte waehlen ---"))
 			parties.add(0, new Party(0, "--- bitte waehlen ---"));
 		return ok(narrowwinners.render("Knappe Gewinner", parties));
 	}
 
 	public static Result narrowwinners_json(int party) {
 		initializeDataManager();
 		if (party == 0) {
 			return badRequest("no party ID was provided");
 		}
 		return ok(narrowwinners_json.render(DataCache.getNarrowWinners(party,
 				use_cache())));
 	}
 
 	public static Result narrowlosers_json(int party) {
 		initializeDataManager();
 		if (party == 0) {
 			return badRequest("no party ID was provided");
 		}
 		return ok(narrowwinners_json.render(DataCache.getNarrowLosers(party,
 				use_cache())));
 	}
 
 	/**
 	 * Seat distribution
 	 */
 	public static Result seatdistribution() {
 		initializeDataManager();
 		return ok(seatdistribution.render("Sitzverteilung",
 				DataCache.getSeatAggregates(use_cache())));
 	}
 
 	/**
 	 * Excess mandates
 	 */
 	public static Result excessmandates() {
 		initializeDataManager();
 		return ok(excessmandates.render("Überhangmandate",
 				DataCache.getExcessMandates(use_cache())));
 	}
 
 	/**
 	 * Members of the bundestag
 	 */
 	public static Result bundestagmembers() {
 		initializeDataManager();
 		return ok(bundestagmembers.render("Bundestagsmitglieder",
 				DataCache.getBundestagMembers(use_cache())));
 	}
 
 	/**
 	 * Enter ballot code
 	 */
 	public static Result ballotCode() {
 		initializeDataManager();
 		if (session("codecount") != null
 				&& Integer.parseInt(session("codecount")) > 3)
			return ok(message.render("Zu viele Fehlversuche"));
 		return ok(ballotcode.render("Stimmzettel - Code eingeben"));
 	}
 
 	/**
 	 * Constituency ballot
 	 */
 	public static Result constituencyBallot() {
 		initializeDataManager();
 		if (session("codecount") != null
 				&& Integer.parseInt(session("codecount")) > 3)
 			return ok(message
 					.render("Zu viele Fehlversuche bei der Eingabe des Codes. Bitte kontaktieren Sie die Wahlhelfer!"));
 		final Map<String, String[]> values = request().body()
 				.asFormUrlEncoded();
 		String ballotcode = values.get("ballotcode")[0];
 		int constituency = db.checkBallotCode(ballotcode);
 		if (constituency < 1) {
 			if (session("codecount") == null) {
 				session("codecount", "1");
 			} else {
 				session("codecount",
 						(Integer.parseInt(session("codecount")) + 1) + "");
 			}
 			return ok(message
 					.render("Code ist ungültig! Bitte versuchen Sie es erneut."));
 		}
 		List<BallotEntry> ballot = DataCache.getBallot(constituency,
 				use_cache());
 		return ok(constituencyballot.render("Stimmzettel", ballot, ballotcode));
 	}
 
 	/**
 	 * process ballot
 	 */
 	public static Result processBallot() {
 		initializeDataManager();
 		final Map<String, String[]> values = request().body()
 				.asFormUrlEncoded();
 		int candidateid, partyid;
 		String ballotcode;
 		try {
 			String candidate = values.get("candidate")[0];
 			String party = values.get("party")[0];
 			ballotcode = values.get("ballotcode")[0];
 			candidateid = Integer.parseInt(candidate);
 			partyid = Integer.parseInt(party);
 			if (db.insertBallot(ballotcode, candidateid, partyid)) {
 				return ok(message.render("Sie haben erfolgreich abgestimmt!"));
 			} else {
 				System.out.println("insertBallot fehlgeschalgen");
 				return ok(message
 						.render("Fehler bei der Abgabe! Bitte kontaktieren Sie die Wahlhelfer."));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 			return ok(message
 					.render("Fehler bei der Abgabe!  Bitte kontaktieren Sie die Wahlhelfer."));
 		}
 	}
 
 	/**
 	 * Initialize the data manager thread
 	 */
 	private static void initializeDataManager() {
 		if (Application.dataManager == null) {
 			// Create the database connection
 			db = new DBConnect();
 
 			// Interrupt the current data manager, if one exists
 			if (Application.dataManager != null)
 				Application.dataManager.interrupt();
 			/*
 			 * Create and aquire a semaphore, because the data cache is empty.
 			 * The data manager will release the semaphore as soon as the data
 			 * cache was updated.
 			 */
 			Semaphore semaphore = new Semaphore(0);
 			Application.dataManager = new DataManagerThread(db,
 					Application.CACHE_UPDATE_INTERVAL, semaphore);
 			Application.dataManager.start();
 			try {
 				semaphore.acquire();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 	}
 }
