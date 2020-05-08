 package models.stats;
 
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TimeZone;
 
 import models.KTLCEdition;
 import models.KTLCRace;
 import models.Player;
 import models.TMEnvironment;
 import models.TMMap;
 
 /**
  * 
  * @author Toub
  */
 public class StatisticGeneral {
 	
 	/** The GeneralStatistic unique instance */
 	private static StatisticGeneral uniqueInstance;
 	/** The initialization tag */
 	private boolean initialized;
 	/** The creation date of the statistics */
 	private Date creationDate; 
 	
 	public int LENGTH_TOP;
 	public int RANK_INTEREST;
 	public int MIN_PERCENTAGE_PARTICIPATIONS;
 	public int MIN_NUMBER_MAPS_FOR_EPIC_FAIL;
 	
 	public KTLCEdition biggestKTLC;
 	public KTLCEdition smallestKTLC;
 	
 	public int stats_numberKTLCs;
 	public int stats_numberKTLC_TMU;
 	public int stats_numberSuperKTLC_TMU;
 	public int stats_numberKTLC_TM2;
 	public int stats_numberPlayers;
 	public int stats_numberPlayersPercentage;
 	public int stats_numberMaps;
 	public int stats_numberRuns;
 	
 	public double stats_averageNumberPlayersByKTLC;
 	public double stats_averageNumberMapsByKTLC;
 	
 	public HashMap<TMEnvironment, Integer> chart_numberMapsByEnviro;
 	public int[][] chart_numberPlayersByKTLC;
 	
 	public List<Rank> 		 	ranking_numberParticipation;
 	public List<Rank<Double>> 	ranking_bestAverageRank;
 	public List<Rank<Integer>>	ranking_numberMaps;
 	public List<Rank<int[]>> 	ranking_numberPodiumsKTLC;
 	public List<Rank<int[]>>	ranking_numberPodiumsRace;
 	public List<Rank<List<KTLCEdition>>> ranking_numberPerfect;
 	
 	public List<Rank<KTLCRace>> ranking_violentMaps;
 	public List<Rank<Integer>>	ranking_numberLastPlaceKTLC;
 	public List<Rank<Integer>>	ranking_numberLastPlaceRace;
 	public List<Rank<Double>> 	ranking_worstAverageRank;
 	public List<Rank<List<KTLCEdition>>> ranking_numberEpicFail;
 
 	/** Private constructor for implementation of the singleton pattern */
 	private StatisticGeneral() { 
 		initialized = false;
 	}
 	
 	/**
 	 * Get the unique statistics
 	 * @return the uniqueInstace statistic
 	 */
 	public static StatisticGeneral getUniqueInstance() {
 		if (uniqueInstance == null) {
 			// init the class
 			uniqueInstance = new StatisticGeneral();
 		}
 		return uniqueInstance;
 	}
 
 	public boolean isInitialized() {
 		return initialized;
 	}
 
 	public void setInitialized(boolean initialized) {
 		this.initialized = initialized;
 		if(initialized) {
 			creationDate = new Date();
 		}
 	}
 	
 	public Date getCreationDate() {
 		return creationDate;
 	}
  
 }
