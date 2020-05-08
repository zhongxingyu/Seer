 package controllers.stats;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import play.i18n.Messages;
 
 import controllers.Players;
 
 import models.KTLCEdition;
 import models.KTLCRace;
 import models.KTLCRaceResult;
 import models.KTLCResult;
 import models.Player;
 import models.TMEnvironment;
 import models.TMMap;
 import models.stats.Rank;
 import models.stats.StatisticConfig;
 import models.stats.StatisticGeneral;
 import models.stats.StatisticMapper;
 import models.stats.StatisticPlayer;
 import models.stats.StatsEntry;
 
 /**
  * This class is used to generate general statistics regarding the whole KTLC history.
  * @author Toub
  */
 public class StatisticsGenerator {
 	/** The configuration for the statistics calculations */
 	private static StatisticConfig config;
 	
 	/**
 	 * Update all the statistics, should be called only using administrator board... 
 	 * @return the elapsed time in milliseconds
 	 */
 	public static Long updateAllGeneralStatistics() {
 		Long startTime = System.nanoTime();
 		
		StatisticGeneral stats = StatisticGeneral.getUniqueInstance();
		stats.reset();
		
 		List<KTLCEdition> ktlcs = KTLCEdition.find("order by date asc").fetch();
 		List<Player> players = Player.find("order by name asc").fetch();
 		List<TMMap> maps = TMMap.find("order by environment desc").fetch();
 		
 		if (!(ktlcs.isEmpty() || players.isEmpty() || maps.isEmpty())) {
 			config = StatisticConfig.loadStatsConfig();
 			
 			stats.LENGTH_TOP = config.getLengthTop();
 			stats.RANK_INTEREST = config.getRankInterest();
 			stats.MIN_PERCENTAGE_PARTICIPATIONS = config.getMinPercentageParticipations();
 			stats.MIN_NUMBER_MAPS_FOR_EPIC_FAIL = config.getMinNumberMapsForEpicFail();
 			
 			// Stats		
 			stats.stats_numberKTLCs = ktlcs.size();
 			stats.stats_numberKTLC_TMU = calcNumberKTLCTMU(ktlcs);
 			stats.stats_numberSuperKTLC_TMU = calcNumberSuperKTLCTMU(ktlcs);
 			stats.stats_numberKTLC_TM2 = calcNumberKTLCTM2(ktlcs);
 			stats.stats_numberPlayers = players.size();
 			stats.stats_numberPlayersPercentage = calcNumberPlayerByPercentage(players, config.getMinPercentageParticipations());
 			stats.stats_numberMaps = maps.size();
 			stats.stats_numberRuns = calcTotalNumberRuns();
 			stats.stats_averageNumberPlayersByKTLC = calcAverageNumberPlayersByKTLC(ktlcs);
 			stats.stats_averageNumberMapsByKTLC = calcAverageNumberMapsByKTLC(ktlcs);
 			
 			// charts
 			stats.chart_numberMapsByEnviro = calcMapsByEnviro(maps);
 			stats.chart_numberPlayersByKTLC = calcNumberPlayersByKTLC(ktlcs);
 			
 			// particular KTLC
 			stats.smallestKTLC = calcMinNumberPlayers(ktlcs);
 			stats.biggestKTLC = calcMaxNumberPlayers(ktlcs);
 			
 			// rankings - hall of fame
 			stats.ranking_numberParticipation = calcRankingParticipatioRatio(players, config.getLengthTop());
 			stats.ranking_bestAverageRank = calcRankingBestAverageRank(players, config.getLengthTop(), config.getMinPercentageParticipations());
 			stats.ranking_numberMaps = calcRankingNumberMaps(players, config.getLengthTop());
 			stats.ranking_numberPodiumsKTLC = calcRankingNumberPodiumsKTLC(players, config.getLengthTop(), config.getRankInterest());
 			stats.ranking_numberPodiumsRace = calcRankingNumberPodiumsRace(players, config.getLengthTop(), config.getRankInterest());
 			stats.ranking_numberPerfect = calcRankingNumberPerfect();
 			
 			// ranking - hall of shame
 			List<KTLCRace> races = KTLCRace.findAll();
 			stats.ranking_violentMaps = calcRankingViolentMaps(races, config.getLengthTop());
 			stats.ranking_numberLastPlaceKTLC = calcRankingNumberlastPlaceKTLC(players, config.getLengthTop(), config.getMinPercentageParticipations());
 			stats.ranking_numberLastPlaceRace = calcRankingNumberlastPlaceRace(players, config.getLengthTop());
 			stats.ranking_worstAverageRank = calcRankingWorstAverageRank(players, config.getLengthTop(), config.getMinPercentageParticipations());
 			stats.ranking_numberEpicFail = calcRankingNumberEpicFail(config.getMinNumberMapsForEpicFail());
 			
 			// change the status
 			stats.setInitialized(true);
 		}
 		
 		Long endTime = System.nanoTime();
 		
 		return Math.abs(endTime - startTime) / 1000000;
 	}
 	
 	/**
 	 * Generate the statistics for the player
 	 * @param mapper the player to study
 	 * @param minPercentage the minPercentage to use for comparison, actually not used for stats
 	 * @return
 	 */
 	public static StatisticPlayer generateStatisticsPlayer(Player player, int minPercentage) {		
 		StatisticPlayer stats = new StatisticPlayer();
 		
 		// set the parameters from the config
 		config = StatisticConfig.loadStatsConfig();
 		stats.RANK_LIMIT = config.getRankLimit();
 		stats.MIN_PERCENTAGE = minPercentage;
 
 		// set the stats
 		stats.player = player;
 		
 		// all the ktlcs
 		List<KTLCEdition> ktlcs = KTLCEdition.find("order by date asc").fetch();
 		// the results of the player on the ktlcs he played
 		List<KTLCResult> ktlcResults = KTLCResult.findByPlayer(player);
 		// the results of the player for all the maps he played
 		List<KTLCRaceResult> raceResults = KTLCRaceResult.findByPlayer(player);
 		// the editions of the ktlcs on which the player played
 		List<KTLCEdition> ktlcPlayed = new ArrayList<KTLCEdition>(ktlcResults.size());
 		for (KTLCResult result : ktlcResults) { ktlcPlayed.add(result.ktlc); }
 		
 		//participations
 		stats.playedKTLCs = new StatsEntry(ktlcResults.size(), ktlcs.size());
 		stats.playedKTLC_TMU = new StatsEntry(calcNumberKTLCTMU(ktlcPlayed), calcNumberKTLCTMU(ktlcs));
 		stats.playedSuperKTLC_TMU = new StatsEntry(calcNumberSuperKTLCTMU(ktlcPlayed), calcNumberSuperKTLCTMU(ktlcs));
 		stats.playedKTLC_TM2 = new StatsEntry(calcNumberKTLCTM2(ktlcPlayed), calcNumberKTLCTM2(ktlcs));
 		
 		// averages
 		stats.averageRank = averageRankByPlayer(player);
 		stats.averageNumberOpponents = calcAverageNumberOpponents(ktlcPlayed);
 		
 		//others
 		stats.numberPlayedRaces = calcNumberMapsPlayed(ktlcResults);
 		stats.numberPlayedRuns = calcTotalNumberRuns(player);
 		stats.numberLastPlaceKTLC = calcNumberLastPlaceKTLC(ktlcResults);
 		stats.numberLastPlaceRace = calcNumberlastPlaceRace(raceResults);
 		stats.perfects = calcPerfects(ktlcResults);
 		stats.epicFails = calcEpicFails(ktlcResults, config.getMinNumberMapsForEpicFail());
 		stats.longestPodiumSerie = calcLongestPodiumSeries(ktlcResults);
 		
 		// charts
 		stats.chart_ranksByKTLCs = ktlcResultsByRanksByPlayer(player, config.getRankLimit());
 		stats.numberPodiumsKTLC = new StatsEntry(
 				stats.chart_ranksByKTLCs[0] + 
 				stats.chart_ranksByKTLCs[1] + 
 				stats.chart_ranksByKTLCs[2], 
 				stats.playedKTLCs.value);
 		
 		stats.chart_ranksByRaces = raceResultsByRanksByPlayer(player, config.getRankLimit());
 		stats.numberPodiumsRace = new StatsEntry(
 				stats.chart_ranksByRaces[0] + 
 				stats.chart_ranksByRaces[1] + 
 				stats.chart_ranksByRaces[2], 
 				stats.numberPlayedRaces);
 		
 		stats.chart_averageRankByEnviro = calcAverageRankByEnviro(raceResults);
 		stats.chart_numberPodiumsByEnviro = calcNumberPodiumsByEnviro(raceResults);
 		
 		// best enviros
 		stats.bestEnviroFromAvgRank = calcBestEnvirofromAvgRank(stats.chart_averageRankByEnviro);
 		stats.bestEviroFromPodiums = calcBestEnviroFromPodiums(stats.chart_numberPodiumsByEnviro);
 		
 		return stats;
 	}
 	
 	/**
 	 * Compare all the statistics of two players and assign to each attribute a tag "better", 
 	 * "worse" or "equal", directly inserted in a HasMap with the name of the attribute as key.
 	 * Used for efficient styling of comparison
 	 * @param origin the player statistics of the origin player
 	 * @param target the player statistics of the target player
 	 * @return HashMap with value for each statistic parameter, with "better" if origin is better,
 	 * "worse" if target is better and "equal" if the values are equal
 	 */
 	public static HashMap<String,String> compareStatsPlayers(StatisticPlayer origin, StatisticPlayer target) {
 		HashMap<String, String> comparison = new HashMap<String, String>();
 		
 		comparison.put("playedKTLCs.value", compareValues(origin.playedKTLCs.value, target.playedKTLCs.value, true));
 		comparison.put("playedKTLC_TMU.value", compareValues(origin.playedKTLC_TMU.value, target.playedKTLC_TMU.value, true));
 		comparison.put("playedSuperKTLC_TMU.value", compareValues(origin.playedSuperKTLC_TMU.value, target.playedSuperKTLC_TMU.value, true));
 		comparison.put("playedKTLC_TM2.value", compareValues(origin.playedKTLC_TM2.value, target.playedKTLC_TM2.value, true));
 		comparison.put("playedKTLCs.ratio", compareValues(origin.playedKTLCs.ratio, target.playedKTLCs.ratio, true));
 		comparison.put("playedKTLC_TMU.ratio", compareValues(origin.playedKTLC_TMU.ratio, target.playedKTLC_TMU.ratio, true));
 		comparison.put("playedSuperKTLC_TMU.ratio", compareValues(origin.playedSuperKTLC_TMU.ratio, target.playedSuperKTLC_TMU.ratio, true));
 		comparison.put("playedKTLC_TM2.ratio", compareValues(origin.playedKTLC_TM2.ratio, target.playedKTLC_TM2.ratio, true));
 		
 		comparison.put("numberPlayedRaces", compareValues(origin.numberPlayedRaces, target.numberPlayedRaces, true));
 		comparison.put("numberPlayedRuns", compareValues(origin.numberPlayedRuns, target.numberPlayedRuns, true));
 		comparison.put("perfects", compareValues(origin.perfects.size(), target.perfects.size(), true));
 		comparison.put("epicFails", compareValues(origin.epicFails.size(), target.epicFails.size(), false));
 		comparison.put("longestPodiumSerie", compareValues(origin.longestPodiumSerie.size(), target.longestPodiumSerie.size(), true));
 		
 		comparison.put("numberLastPlaceRace.value", compareValues(origin.numberLastPlaceRace.value, target.numberLastPlaceRace.value, false));
 		comparison.put("numberLastPlaceKTLC.value", compareValues(origin.numberLastPlaceKTLC.value, target.numberLastPlaceKTLC.value, false));
 		comparison.put("numberPodiumsRace.value", compareValues(origin.numberPodiumsRace.value, target.numberPodiumsRace.value, true));
 		comparison.put("numberPodiumsKTLC.value", compareValues(origin.numberPodiumsKTLC.value, target.numberPodiumsKTLC.value, true));
 		comparison.put("numberLastPlaceRace.ratio", compareValues(origin.numberLastPlaceRace.ratio, target.numberLastPlaceRace.ratio, false));
 		comparison.put("numberLastPlaceKTLC.ratio", compareValues(origin.numberLastPlaceKTLC.ratio, target.numberLastPlaceKTLC.ratio, false));
 		comparison.put("numberPodiumsRace.ratio", compareValues(origin.numberPodiumsRace.ratio, target.numberPodiumsRace.ratio, true));
 		comparison.put("numberPodiumsKTLC.ratio", compareValues(origin.numberPodiumsKTLC.ratio, target.numberPodiumsKTLC.ratio, true));
 		
 		comparison.put("averageRank", compareValues(origin.averageRank, target.averageRank, false));
 		comparison.put("averageNumberOpponents", compareValues(origin.averageNumberOpponents, target.averageNumberOpponents, true));
 		
 		comparison.put("bestEnviroFromAvgRank", compareValues(
 				origin.chart_averageRankByEnviro.get(origin.bestEnviroFromAvgRank.get(0))[0],
 				target.chart_averageRankByEnviro.get(target.bestEnviroFromAvgRank.get(0))[0], false));
 		
 		comparison.put("bestEviroFromPodiums", compareValues(
 				origin.bestEviroFromPodiums != null ? origin.chart_numberPodiumsByEnviro.get(origin.bestEviroFromPodiums.get(0))[3]: 0,
 				target.bestEviroFromPodiums != null ? target.chart_numberPodiumsByEnviro.get(target.bestEviroFromPodiums.get(0))[3]: 0, true));		
 		return comparison;
 	}
 	
 	/**
 	 * Generate the statistics for the mapper
 	 * @param mapper the mapper to study
 	 * @param minPercentage the minPercentage to use for comparison, actually not used for stats
 	 * @return
 	 */
 	public static StatisticMapper generateStatisticsMapper(Player mapper, int minPercentage) {		
 		StatisticMapper stats = new StatisticMapper();
 
 		// set the stats
 		stats.mapper = mapper;
 		stats.MIN_PERCENTAGE = minPercentage;
 		
 		List<TMMap> maps = TMMap.findByPlayer(mapper);
 		List<KTLCRace> races = new ArrayList<KTLCRace>(maps.size());
 		for (TMMap map : maps) { races.add(KTLCRace.findByMap(map)); }
 		
 		stats.chart_numberMapsByEnviro = calcMapsByEnviro(maps);
 		
 		stats.createdMaps = new StatsEntry(maps.size(), TMMap.findAll().size());
 		stats.distinctPlayersOnMaps = calcNumberDistinctPlayersOnMaps(races);
 		stats.numberRunsOnMaps = calcNumberRunsOnMaps(races);
 		stats.numberDistinctKTLCsAsMapper = calcNumberDistinctKTLCsAsMapper(races);
 		
 		stats.averageNumberPlayersOnMaps = calcAverageNumberPlayersOnMaps(races);
 		
 		stats.favoriteMappingEnviros = calcFavoriteMappingEnviro(stats.chart_numberMapsByEnviro);
 		
 		return stats;
 	}
 	
 	/**
 	 * Compare all the statistics of two mappers and assign to each attribute a tag "better", 
 	 * "worse" or "equal", directly inserted in a HasMap with the name of the attribute as key.
 	 * Used for efficient styling of comparison
 	 * @param origin the player statistics of the origin mapper
 	 * @param target the player statistics of the target mapper
 	 * @return HashMap with value for each statistic parameter, with "better" if origin is better,
 	 * "worse" if target is better and "equal" if the values are equal
 	 */
 	public static HashMap<String,String> compareStatsMappers(StatisticMapper origin, StatisticMapper target) {
 		HashMap<String, String> comparison = new HashMap<String, String>();
 		
 		comparison.put("createdMaps.value", compareValues(origin.createdMaps.value, target.createdMaps.value, true));
 		comparison.put("distinctPlayersOnMaps.value", compareValues(origin.distinctPlayersOnMaps.value, target.distinctPlayersOnMaps.value, true));
 		comparison.put("createdMaps.ratio", compareValues(origin.createdMaps.ratio, target.createdMaps.ratio, true));
 		comparison.put("distinctPlayersOnMaps.ratio", compareValues(origin.distinctPlayersOnMaps.ratio, target.distinctPlayersOnMaps.ratio, true));
 		
 		comparison.put("numberRunsOnMaps", compareValues(origin.numberRunsOnMaps, target.numberRunsOnMaps, true));
 		comparison.put("numberDistinctKTLCsAsMapper", compareValues(origin.numberDistinctKTLCsAsMapper, target.numberDistinctKTLCsAsMapper, true));
 		comparison.put("averageNumberPlayersOnMaps", compareValues(origin.averageNumberPlayersOnMaps, target.averageNumberPlayersOnMaps, true));
 		
 		comparison.put("favoriteMappingEnviros", compareValues(
 				origin.chart_numberMapsByEnviro.get(origin.favoriteMappingEnviros.get(0)),
 				target.chart_numberMapsByEnviro.get(target.favoriteMappingEnviros.get(0)), true));
 		
 		return comparison;
 	}
 	
 	/**
 	 * Calculate the size of the longest series of KTLC where the player finished on the podium
 	 * @param ktlcResults the results of the player
 	 * @return the list of KTLC Editions of the longest series
 	 */
 	private static List<KTLCEdition> calcLongestPodiumSeries(List<KTLCResult> ktlcResults) {
 		List<KTLCEdition> podiums = new ArrayList<KTLCEdition>();
 		List<KTLCEdition> currentSerie = new ArrayList<KTLCEdition>();
 		
 		// sort the results of the ktlc by date
 		Collections.sort(ktlcResults, new Comparator<KTLCResult>() {
 	            @Override
 	            public int compare(final KTLCResult entry1, final KTLCResult entry2) {
 	                final Date dateKTLC1 = entry1.ktlc.date;
 	                final Date dateKTLC2 = entry2.ktlc.date;
 	                return -1*dateKTLC1.compareTo(dateKTLC2);
 	            }
 	        });
 		
 		for (KTLCResult result : ktlcResults) {
 			// if podium, add the ktlc to the current serie
 			if (result.rank <= 3) {
 				currentSerie.add(result.ktlc);
 			} else {
 				// start a new serie and save the current
 				if (currentSerie.size() > podiums.size()) {					
 					podiums = currentSerie;
 				}
 				currentSerie = new ArrayList<KTLCEdition>();
 			}
 		}
 		
 		return podiums;
 	}
 
 	/**
 	 * Calculate the number of last places in races for the player
 	 * @param results the race results of the player
 	 * @return a stats entry with the number of last places in races
 	 */
 	private static StatsEntry calcNumberlastPlaceRace(List<KTLCRaceResult> results) {
 		
 		int numberLastPlaces = 0;
 		
 		for (KTLCRaceResult result : results) {
 			int numberPlayers = result.race.results.size();
 			if (result.rank == numberPlayers) {
 				numberLastPlaces++;
 			}
 		}	
 		return new StatsEntry(numberLastPlaces, results.size());		
 	}
 	
 	/**
 	 * Calculate the number of last places in KTLCs for the player
 	 * @param results the ktlcs results of the player
 	 * @return a StatsEntry with the number of last places in ktlcs
 	 */
 	private static StatsEntry calcNumberLastPlaceKTLC(List<KTLCResult> results) {
 		
 		int numberLastPlaces = 0;
 		
 		for (KTLCResult result : results) {
 			int numberPlayers = result.ktlc.results.size();
 			if (result.rank == numberPlayers) {
 				numberLastPlaces++;
 			}
 		}	
 		return new StatsEntry(numberLastPlaces, results.size());		
 	}
 	
 	/**
 	 * Calculate the average number of opponent of the player
 	 * @param ktlcs the KTLCs at which the player participated
 	 * @return the average number of opponents
 	 */
 	private static double calcAverageNumberOpponents(List<KTLCEdition> ktlcs) {
 		int numberOpponents = 0;
 		for (KTLCEdition ktlc : ktlcs) {
 			numberOpponents += (ktlc.results.size() - 1); // remove the player himself
 		}
 		
 		return numberOpponents / (double)ktlcs.size();
 	}
 
 	/**
 	 * Calculate the best environment with respect to the number of podiums
 	 * @param the hashmaps with the number of podiums by environment
 	 * @return the environments (several if equalities)
 	 */
 	private static List<TMEnvironment> calcBestEnviroFromPodiums(HashMap<TMEnvironment, int[]> numberPodiumsByEnviro) {
 		List<TMEnvironment> bestEnviros = null;
 		
 		int maxPodium = Integer.MIN_VALUE;
 		for (TMEnvironment enviro : numberPodiumsByEnviro.keySet()) {
 			// 1st value = #1st places, 2nd value = #2nd places, 3rd value = #3rd places, 4th value = total of podiums
 			int numberPodiums = numberPodiumsByEnviro.get(enviro)[3];
 			if (numberPodiums > maxPodium && numberPodiums != 0) {
 				maxPodium = numberPodiums;
 				bestEnviros = new ArrayList<TMEnvironment>();
 				bestEnviros.add(enviro);
 			} else if (numberPodiums == maxPodium  && numberPodiums != 0) {
 				bestEnviros.add(enviro);
 			}
 		}
 		
 		return bestEnviros;
 	}
 
 	/**
 	 * Calculate the best environment with respect to the average rank
 	 * @param the hashmaps with the average ranking by environment
 	 * @return the environments (several if equalities)
 	 */
 	private static List<TMEnvironment> calcBestEnvirofromAvgRank(HashMap<TMEnvironment, Double[]> averageRankByEnviro) {
 		List<TMEnvironment> bestEnviros = null;
 		
 		double bestAVG = Double.MAX_VALUE;
 		for (TMEnvironment enviro : averageRankByEnviro.keySet()) {
 			// 1st value = average, 2nd value = numberMaps in double...
 			double avg = averageRankByEnviro.get(enviro)[0];
 			if (avg < bestAVG && avg != 0) {
 				bestAVG = avg;
 				bestEnviros = new ArrayList<TMEnvironment>();
 				bestEnviros.add(enviro);
 			} else if (avg == bestAVG  && avg != 0) {
 				bestEnviros.add(enviro);
 			}
 		}
 		
 		return bestEnviros;
 	}
 
 	/**
 	 * Calculate the average rank by environment
 	 * @param raceResults the results by races
 	 * @return a HashMap with an array of double for each environment. Double[0] = average rank on the enviro, Double[1] = numberMaps played on the enviro
 	 */
 	private static HashMap<TMEnvironment, Double[]> calcAverageRankByEnviro(List<KTLCRaceResult> raceResults) {
 		HashMap<TMEnvironment, Integer> countedMaps = new HashMap<TMEnvironment, Integer>();
 		HashMap<TMEnvironment, Integer> sumRanks = new HashMap<TMEnvironment, Integer>();
 				
 		for (KTLCRaceResult result : raceResults) {
 			TMEnvironment enviro = result.race.map.environment;
 			int numberMaps = 1;
 			int currentSum = result.rank;
 			
 			if (countedMaps.containsKey(enviro)) {
 				numberMaps += countedMaps.get(enviro);
 				currentSum += sumRanks.get(enviro);
 			}
 			
 			countedMaps.put(enviro, numberMaps);
 			sumRanks.put(enviro, currentSum);			
 		}
 		
 		HashMap<TMEnvironment, Double[]> average = new HashMap<TMEnvironment, Double[]>();
 		for (TMEnvironment enviro : TMEnvironment.values()) {
 			// 1st value = average, 2nd value = numberMaps in double...
 			Double[] resultOfEnviro = {0.0, 0.0};
 			if (countedMaps.containsKey(enviro)) {
 				resultOfEnviro[0] = sumRanks.get(enviro) / (double) countedMaps.get(enviro);
 				resultOfEnviro[1] = (double) countedMaps.get(enviro);
 			}
 			average.put(enviro, resultOfEnviro);
 		}
 		
 		return average;
 	}
 	
 	/**
 	 * Calculate the number of podiums by environment
 	 * @param raceResults the results by races of the player
 	 * @return an hashMap with an array of int by environment. int[0-2] = number of podiums (1st, 2nd, 3rd ranks) on the enviro, int[4] = numberMaps played on the enviro
 	 */
 	private static HashMap<TMEnvironment, int[]> calcNumberPodiumsByEnviro(List<KTLCRaceResult> raceResults) {
 		HashMap<TMEnvironment, Integer> countedMaps = new HashMap<TMEnvironment, Integer>();
 		HashMap<TMEnvironment, int[]> sumRanks = new HashMap<TMEnvironment, int[]>();
 				
 		for (KTLCRaceResult result : raceResults) {
 			TMEnvironment enviro = result.race.map.environment;
 			int numberMaps = 1;
 			int[] currentSum = {0, 0, 0};
 			
 			if (countedMaps.containsKey(enviro)) {
 				numberMaps += countedMaps.get(enviro);
 				currentSum = sumRanks.get(enviro);
 			}
 			
 			// on the podium
 			if (result.rank <= 3) {
 				currentSum[result.rank - 1] += 1;
 			}
 			
 			countedMaps.put(enviro, numberMaps);
 			sumRanks.put(enviro, currentSum);			
 		}
 		
 		HashMap<TMEnvironment, int[]> numberPodiums = new HashMap<TMEnvironment, int[]>();
 		for (TMEnvironment enviro : TMEnvironment.values()) {
 			// 1st value = #1st places, 2nd value = #2nd places, 3rd value = #3rd places, 4th value = total of podiums
 			int[] resultOfEnviro = {0, 0, 0, 0};
 			if (countedMaps.containsKey(enviro)) {
 				resultOfEnviro[0] = sumRanks.get(enviro)[0];
 				resultOfEnviro[1] = sumRanks.get(enviro)[1];
 				resultOfEnviro[2] = sumRanks.get(enviro)[2];
 				resultOfEnviro[3] = sumRanks.get(enviro)[0] + sumRanks.get(enviro)[1] + sumRanks.get(enviro)[2];
 			}
 			numberPodiums.put(enviro, resultOfEnviro);
 		}
 		
 		return numberPodiums;
 	}
 
 	/**
 	 * Calculate the favorite environment of the mapper
 	 * @param mapsByEnviro the HashMap with the number of maps by environment
 	 * @return the environment with the highest number of maps (several if equalities)
 	 */
 	private static List<TMEnvironment> calcFavoriteMappingEnviro(HashMap<TMEnvironment, Integer> mapsByEnviro) {
 		int maxNumberMaps = 0;
 		List<TMEnvironment> maxEnviro = new ArrayList<TMEnvironment>();
 		
 		for (TMEnvironment enviro : mapsByEnviro.keySet()) {
 			if (mapsByEnviro.get(enviro) > maxNumberMaps) {
 				maxEnviro = new ArrayList<TMEnvironment>();
 				maxEnviro.add(enviro);
 				maxNumberMaps = mapsByEnviro.get(enviro);
 			} else if (mapsByEnviro.get(enviro) == maxNumberMaps) {
 				maxEnviro.add(enviro);
 			}
 		}
 		
 		return maxEnviro;
 	}
 
 	/**
 	 * Calculate the number of distinct players that played on the maps of the mapper
 	 * @param races the list of the races where the maps were played
 	 * @return a StatsEntry with the number of players
 	 */
 	private static StatsEntry calcNumberDistinctPlayersOnMaps(List<KTLCRace> races) {
 		Set<Player> players = new HashSet<Player>();
 		for (KTLCRace race : races) {
 			for (KTLCRaceResult result : race.results) {
 				players.add(Player.findByLogin(result.login.name));
 			}
 		}
 		return new StatsEntry(players.size(), Player.findAll().size());
 	}
 	
 	/**
 	 * Calculate the average number of players that palyed on the map of the mapper
 	 * @param races the list of the races where the maps were played
 	 * @return the average number of players
 	 */
 	private static double calcAverageNumberPlayersOnMaps(List<KTLCRace> races) {
 		double sum = 0;
 		for (KTLCRace race : races) {
 			sum += race.results.size();			
 		}
 		
 		return sum / (double)races.size();
 	}
 	
 	/**
 	 * Calculate the number of runs played on the maps of the mappers
 	 * @param races the list of the races where the maps were played
 	 * @return the total number of runs
 	 */
 	private static int calcNumberRunsOnMaps(List<KTLCRace> races) {
 		int count = 0;
 		for (KTLCRace race : races) {
 			for (KTLCRaceResult result : race.results) {
 				if (result.rank == 1) {
 					count += result.roundsCount;
 				}
 			}	
 		}
 		
 		return count;
 	}
 	
 	/**
 	 * Calculate the number of distinct KTLC where the player provided maps
 	 * @param races the list of the races where the maps were played
 	 * @return the total number of ktlcs
 	 */
 	private static int calcNumberDistinctKTLCsAsMapper(List<KTLCRace> races) {
 		Set<Integer> ktlcs = new HashSet<Integer>();
 		for (KTLCRace race : races) {
 			ktlcs.add(race.ktlc.number);
 		}
 		
 		return ktlcs.size();
 	}
 
 	/**
 	 * Calculate the number of players that played at least x % of the KTLCS
 	 * @param players the list of players
 	 * @param percentage the percentage required
 	 * @return
 	 */
 	private static int calcNumberPlayerByPercentage(List<Player> players, int percentage) {
 		int result = 0;
 		int minKTLC = (int)(KTLCEdition.findAll().size() * percentage * 0.01);
 		
 		for (Player player : players) {
 			if (KTLCResult.findByPlayer(player).size() >= minKTLC) {
 				result++;
 			}
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * Calculate the number of maps played by the player
 	 * @param results the list of results by KTLC of the player
 	 * @return
 	 */
 	private static int calcNumberMapsPlayed(List<KTLCResult> results) {
 		int count = 0;
 		for (KTLCResult result : results) {
 			count += result.nbRaces;
 		}
 		return count;
 	}
 	
 	/**
 	 * Calculate the evolution of the number of players by KTLC
 	 * @param ktlcs the list of KTLC Editions
 	 * @return
 	 */
 	private static int[][] calcNumberPlayersByKTLC(List<KTLCEdition> ktlcs) {
 		int[][] playersByKTLC = new int[ktlcs.size()][2];
 		for (int i = 0; i < ktlcs.size(); i++) {
 			playersByKTLC[i][0] = ktlcs.get(i).number;
 			playersByKTLC[i][1] = ktlcs.get(i).results.size();
 		}
 		
 		return playersByKTLC;
 	}
 	
 	/**
 	 * Count the number of maps by TMEnvironment
 	 * @param maps the list of maps that have to be used
 	 * @return a HashMap with the environment as keys and count as value
 	 */
 	private static HashMap<TMEnvironment, Integer> calcMapsByEnviro(List<TMMap> maps) {
 		HashMap<TMEnvironment, Integer> count = new HashMap<TMEnvironment, Integer>();
 
 		for (TMMap map : maps) {
 			if (count.containsKey(map.environment)) {
 				int currentCount = count.get(map.environment);
 				count.put(map.environment, currentCount + 1);
 			} else {
 				count.put(map.environment, 1);
 			}
 		}
 		
 		// add the remaining enviros
 		for (TMEnvironment enviro : TMEnvironment.values()) {
 			if (!count.containsKey(enviro)) {
 				count.put(enviro, 0);
 			}
 		}
 		
 		return count;
 	}
 	
 	/**
 	 * Calculate the number of KTLC TMU that have been played
 	 * @param ktlcs the list of KTLC Editions
 	 * @return
 	 */
 	private static int calcNumberKTLCTMU(List<KTLCEdition> ktlcs) {
 		int count = 0;
 		for (KTLCEdition ktlc : ktlcs) {
 			KTLCRace firstRace = ktlc.races.get(0);
 			if (TMEnvironment.getEnvironmentsTMU().contains(firstRace.map.environment)) {
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * Calculate the number of Super KTLC TMU that have been played
 	 * @param ktlcs the list of KTLC Editions
 	 * @return
 	 */
 	private static int calcNumberSuperKTLCTMU(List<KTLCEdition> ktlcs) {
 		int count = 0;
 		
 		for (KTLCEdition ktlc : ktlcs) {
 			if (ktlc.races.size() == TMEnvironment.getEnvironmentsTMU().size()) {
 				int distinctEnviro = 0;
 				for (TMEnvironment enviro : TMEnvironment.getEnvironmentsTMU()) {
 					for (KTLCRace race : ktlc.races) {
 						if (race.map.environment.equals(enviro)) {
 							distinctEnviro++;
 						}
 					}
 				}
 				if (distinctEnviro == TMEnvironment.getEnvironmentsTMU().size()) {
 					count++;
 				}
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * Calculate the number of KTLC TM2
 	 * @param ktlcs the list of KTLC Editions
 	 * @return
 	 */
 	private static int calcNumberKTLCTM2(List<KTLCEdition> ktlcs) {
 		int count = 0;
 		for (KTLCEdition ktlc : ktlcs) {
 			KTLCRace firstRace = ktlc.races.get(0);
 			if (TMEnvironment.getEnvironmentsTM2().contains(firstRace.map.environment)) {
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	/**
 	 * Calculate the average number of players for a list of KTLCs
 	 * @param ktlcs the list of KTLC Editions
 	 * @return the average number of players
 	 */
 	private static double calcAverageNumberPlayersByKTLC(List<KTLCEdition> ktlcs) {
 		int count = 0;
 		for (KTLCEdition ktlc : ktlcs) {
 			count += ktlc.results.size();
 		}
 		return (double)count / (double)ktlcs.size();
 	}
 	
 	/**
 	 * Calculate the average number of maps for a list of KTLCs
 	 * @param ktlcs the list of KTLC Editions
 	 * @return the average number of maps
 	 */
 	private static double calcAverageNumberMapsByKTLC(List<KTLCEdition> ktlcs) {
 		int count = 0;
 		for (KTLCEdition ktlc : ktlcs) {
 			count += ktlc.races.size();
 		}
 		return (double)count / (double)ktlcs.size();
 	}
 	
 	/**
 	 * Find the ktlc with the smallest number of players in a list of KTLCs
 	 * @param ktlcs the list of KTLC Editions
 	 * @return the smallest KTLCEdition
 	 */
 	private static KTLCEdition calcMinNumberPlayers(List<KTLCEdition> ktlcs) {
 		KTLCEdition minNumberPlayersKTLC = null;
 		int min = Integer.MAX_VALUE;
 		for (KTLCEdition ktlc : ktlcs) {
 			if (ktlc.results.size() < min) {
 				min = ktlc.results.size();
 				minNumberPlayersKTLC = ktlc;
 			}
 		}
 		return minNumberPlayersKTLC;
 	}
 	
 	/**
 	 * Find the ktlc with the biggest number of players in a list of KTLCs
 	 * @param ktlcs the list of KTLC Editions
 	 * @return the biggest KTLCEdition
 	 */
 	private static KTLCEdition calcMaxNumberPlayers(List<KTLCEdition> ktlcs) {
 		KTLCEdition maxNumberPlayersKTLC = null;
 		int max = Integer.MIN_VALUE;
 		for (KTLCEdition ktlc : ktlcs) {
 			if (ktlc.results.size() > max) {
 				max = ktlc.results.size();
 				maxNumberPlayersKTLC = ktlc;
 			}
 		}
 		return maxNumberPlayersKTLC;
 	}
 	
 	/**
 	 * Calculate the number of played runs by getting the results of the winner of each maps.
 	 * The number of runs is the sum of the number of round played by each winner
 	 * @return the total number of runs
 	 */
 	private static int calcTotalNumberRuns() {
 		// get only the results from the winner of each race
 		List<KTLCRaceResult> results = KTLCRaceResult.find("rank = 1").fetch();
 	
 		int count = 0;
 		for (KTLCRaceResult result : results) {
 			count += result.roundsCount;
 		}		
 		return count;
 	}
 	
 	/**
 	 * Calculate the number of played runs by the player
 	 * The number of runs is the sum of the number of round played by each winner
 	 * @param player
 	 * @return the total number of runs
 	 */
 	private static int calcTotalNumberRuns(Player player) {
 		// get only the results from the winner of each race
 		List<KTLCRaceResult> results = KTLCRaceResult.findByPlayer(player);
 	
 		int count = 0;
 		for (KTLCRaceResult result : results) {
 			count += result.roundsCount;
 		}		
 		return count;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the Participation Ratio of a list of players, 
 	 * on ALL the KTLCs.
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @return the list of size lengthTop of the best players with their values
 	 */
 	private static List<Rank> calcRankingParticipatioRatio(List<Player> players, int lengthTop) {
 		List<Rank> ranking = new ArrayList<Rank>(lengthTop);
 		int numberKTLCs = KTLCEdition.findAll().size();
 		
 		// calculate the participation ratio for each player
 		Double[][] participationRatiosByPlayerID = new Double[players.size()][2];
 		int currentIndex = 0;
 		for (Player player : players) {
 			int numberParticipation = KTLCResult.findByPlayer(player).size();
 			participationRatiosByPlayerID[currentIndex][0] = (double)player.id;
 			participationRatiosByPlayerID[currentIndex][1] = (double)numberParticipation / (double)numberKTLCs;
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING participation rate
 		Arrays.sort(participationRatiosByPlayerID, new Comparator<Double[]>() {
             @Override
             public int compare(final Double[] entry1, final Double[] entry2) {
                 final Double ratio1 = entry1[1];
                 final Double ratio2 = entry2[1];
                 return ratio1.compareTo(ratio2);
             }
         });
 		
 		// Add the best results to the ranking create the structure for holding results
 		for (int i = 0; i < lengthTop; i++) {
 			int index = players.size() - 1 - i;
 			
 			Player player = Player.findById(participationRatiosByPlayerID[index][0].longValue());
 			Double ratio = participationRatiosByPlayerID[index][1];
 			Integer participation = KTLCResult.findByPlayer(player).size();
 			
 			Rank r = new Rank(player, participation, ratio);
 			ranking.add(r);
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the Best Average Rank of a list of players, 
 	 * on ALL the KTLCs.
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @param minPercentage the minimal percentage that the players should have played to be considered
 	 * @return the list of size lengthTop of the best players with their values
 	 */
 	private static List<Rank<Double>> calcRankingBestAverageRank(List<Player> players, int lengthTop, int MinPercentage) {
 		List<Rank<Double>> ranking = new ArrayList<Rank<Double>>(lengthTop);
 		int numberKTLC = KTLCEdition.findAll().size();
 		
 		// calculate the number of maps for each player
 		double[][] averageRankByPlayerID = new double[players.size()][2];
 		
 		int currentIndex = 0;
 		for (Player player: players) {
 			averageRankByPlayerID[currentIndex][0] = player.id.doubleValue();
 			averageRankByPlayerID[currentIndex][1] = averageRankByPlayer(player);
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING participation rate
 		Arrays.sort(averageRankByPlayerID, new Comparator<double[]>() {
             @Override
             public int compare(final double[] entry1, final double[] entry2) {
                 final Double averageRank1 = entry1[1];
                 final Double averageRank2 = entry2[1];
                 return averageRank1.compareTo(averageRank2);
             }
         });
 		
 		// create the structure for holding results,
 		// but only for players with at least MIN_PARTICIPATION
 		int currentCount = 0;
 		for (int i = 0; i < players.size(); i++) {
 			Player player = Player.findById((long)averageRankByPlayerID[i][0]);
 			double value = averageRankByPlayerID[i][1];
 			int numberParticipation = KTLCResult.findByPlayer(player).size();
 			
 			if (numberParticipation >= (MinPercentage*0.01*numberKTLC)) {
 				Rank<Double> r = new Rank<Double>(player, numberParticipation, -1, value);
 				ranking.add(currentCount, r);
 				
 				currentCount++;
 				if (currentCount == lengthTop) { break; }
 			}
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the Worst Average Rank of a list of players, 
 	 * on ALL the KTLCs.
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @param minPercentage the minimal percentage that the players should have played to be considered
 	 * @return the list of size lengthTop of the best players with their values
 	 */
 	private static List<Rank<Double>> calcRankingWorstAverageRank(List<Player> players, int lengthTop, int MinPercentage) {
 		List<Rank<Double>> ranking = new ArrayList<Rank<Double>>(lengthTop);
 		int numberKTLC = KTLCEdition.findAll().size();
 		
 		// calculate the number of maps for each player
 		double[][] averageRankByPlayerID = new double[players.size()][2];
 		
 		int currentIndex = 0;
 		for (Player player: players) {
 			averageRankByPlayerID[currentIndex][0] = player.id.doubleValue();
 			averageRankByPlayerID[currentIndex][1] = averageRankByPlayer(player);
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING participation rate
 		Arrays.sort(averageRankByPlayerID, new Comparator<double[]>() {
             @Override
             public int compare(final double[] entry1, final double[] entry2) {
                 final Double averageRank1 = entry1[1];
                 final Double averageRank2 = entry2[1];
                 return averageRank1.compareTo(averageRank2);
             }
         });
 		
 		// create the structure for holding results,
 		// but only for players with at least MIN_PARTICIPATION
 		int currentCount = 0;
 		for (int i = players.size() - 1; i >= 0; i--) {
 			Player player = Player.findById((long)averageRankByPlayerID[i][0]);
 			double value = averageRankByPlayerID[i][1];
 			int numberParticipation = KTLCResult.findByPlayer(player).size();
 			
 			if (numberParticipation >= (MinPercentage*0.01*numberKTLC)) {
 				Rank<Double> r = new Rank<Double>(player, numberParticipation, -1, value);
 				ranking.add(currentCount, r);
 				
 				currentCount++;
 				if (currentCount == lengthTop) { break; }
 			}
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the Number of map created by the players from the list, 
 	 * on ALL the KTLCs.
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @return the list of size lengthTop of the best players with their values
 	 */
 	private static List<Rank<Integer>> calcRankingNumberMaps(List<Player> players, int lengthTop) {
 		List<Rank<Integer>> ranking = new ArrayList<Rank<Integer>>(lengthTop);
 		
 		// calculate the number of maps for each player
 		int[][] numberMapsByPlayerID = new int[players.size()][2];
 		
 		int currentIndex = 0;
 		for (Player player : players) {
 			numberMapsByPlayerID[currentIndex][0] = player.id.intValue();
 			numberMapsByPlayerID[currentIndex][1] = TMMap.findByPlayer(player).size();
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING participation rate
 		Arrays.sort(numberMapsByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 final Integer numberMaps1 = entry1[1];
                 final Integer numberMaps2 = entry2[1];
                 return numberMaps1.compareTo(numberMaps2);
             }
         });
 		
 		// create the structure for holding results
 		for (int i = 0; i < lengthTop; i++) {
 			int index = players.size() - 1 - i;
 			
 			Player player = Player.findById((long)numberMapsByPlayerID[index][0]);
 			int numberMaps = numberMapsByPlayerID[index][1];
 			
 			// numberParticipation & ratio are not used nor calculated
 			Rank r = new Rank<Integer>(player, -1, -1, numberMaps);
 			ranking.add(i, r);
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the Number of podiums performed during the KTLCs by the 
 	 * players from the list, on ALL the KTLCs.
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @param rankInterest the last rank that should be considered (from 1st place to rankInterest)
 	 * @return the list of size lengthTop of the best players with their values
 	 */
 	private static List<Rank<int[]>> calcRankingNumberPodiumsKTLC(List<Player> players, int lengthTop, int rankInterest) {
 		List<Rank<int[]>> ranking = new ArrayList<Rank<int[]>>(lengthTop);
 		
 		// calculate the number of podiums by KTLC for each player
 		int[][] numberPodiumByPlayerID = new int[players.size()][rankInterest + 1];
 		
 		int currentIndex = 0;
 		for (Player player : players) {
 			// store the player id in the first cell
 			numberPodiumByPlayerID[currentIndex][0] = player.id.intValue();
 			// store the results in the remaining cells
 			List<KTLCResult> results = KTLCResult.findByPlayer(player);
 			if (!results.isEmpty()) {
 				for (KTLCResult result : results) {
 					if (result.rank <= rankInterest) {
 						numberPodiumByPlayerID[currentIndex][result.rank]++;
 					}
 				}
 			}
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING number of podium
 		Arrays.sort(numberPodiumByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 final Integer numPodium1 = entry1[1] + entry1[2] + entry1[3];
                 final Integer numPodium2 = entry2[1] + entry2[2] + entry2[3];
                 return numPodium1.compareTo(numPodium2);
             }
         });
 		
 		// create the structure for holding results
 		for (int i = 0; i < lengthTop; i++) {			
 			int index = players.size() - 1 - i;
 			
 			Player player = Player.findById((long)numberPodiumByPlayerID[index][0]);
 			int participation = KTLCResult.findByPlayer(player).size();
 			
 			int[] podiums = Arrays.copyOfRange(numberPodiumByPlayerID[index], 1, rankInterest + 1);
 			int totalPodium = podiums[0] + podiums[1] + podiums[2];
 			double ratio = (double)totalPodium / (double)participation;
 			
 			Rank<int[]> r = new Rank<int[]>(player, participation, ratio, podiums);
 			ranking.add(i, r);
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the Number of podiums performed during the races of the KTLCs
 	 * by the players from the list, on ALL the KTLCs.
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @param rankInterest the last rank that should be considered (from 1st place to rankInterest)
 	 * @return the list of size lengthTop of the best players with their values
 	 */
 	private static List<Rank<int[]>> calcRankingNumberPodiumsRace(List<Player> players, int lengthTop, int rankInterest) {
 		List<Rank<int[]>> ranking = new ArrayList<Rank<int[]>>(lengthTop);
 		
 		// calculate the number of podiums by race for each player
 		int[][] numberPodiumByPlayerID = new int[players.size()][rankInterest + 1];
 		
 		int currentIndex = 0;
 		for (Player player : players) {
 			// store the player id in the first cell
 			numberPodiumByPlayerID[currentIndex][0] = player.id.intValue();
 			// store the results in the remaining cells
 			List<KTLCRaceResult> results = KTLCRaceResult.findByPlayer(player);
 			if (!results.isEmpty()) {
 				for (KTLCRaceResult result : results) {
 					if (result.rank <= rankInterest) {
 						numberPodiumByPlayerID[currentIndex][result.rank]++;
 					}
 				}
 			}
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING number of podium
 		Arrays.sort(numberPodiumByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 final Integer numPodium1 = entry1[1] + entry1[2] + entry1[3];
                 final Integer numPodium2 = entry2[1] + entry2[2] + entry2[3];
                 return numPodium1.compareTo(numPodium2);
             }
         });
 		
 		// create the structure for holding results
 		for (int i = 0; i < lengthTop; i++) {
 			int index = players.size() - 1 - i;
 			
 			Player player = Player.findById((long)numberPodiumByPlayerID[index][0]);
 			int participation = KTLCRaceResult.findByPlayer(player).size();
 			
 			int[] podiums = Arrays.copyOfRange(numberPodiumByPlayerID[index], 1, rankInterest + 1);
 			int totalPodium = podiums[0] + podiums[1] + podiums[2];
 			double ratio = (double)totalPodium / (double)participation;
 			
 			Rank<int[]> r = new Rank<int[]>(player, participation, ratio, podiums);
 			ranking.add(i, r);
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking regarding the maps which eliminated the highest number of player, 
 	 * on ALL the KTLCs.
 	 * @param races the list of races
 	 * @param lengthTop the size of the top
 	 * @return the list of size lengthTop of the most violent races with their values
 	 */
 	private static List<Rank<KTLCRace>> calcRankingViolentMaps(List<KTLCRace> races, int lengthTop) {
 		List<Rank<KTLCRace>> ranking = new ArrayList<Rank<KTLCRace>>(lengthTop);
 		
 		//calculate the number of elimination for each races
 		int[][] numberEliminationFirstLapByRaceID = new int[races.size()][2];
 		
 		int currentIndex = 0;
 		for (KTLCRace race: races) {
 			numberEliminationFirstLapByRaceID[currentIndex][0] = race.id.intValue();
 			// count the number of giving up or elimination
 			int countElimination = 0;
 			for (KTLCRaceResult result : race.results) {
 				if (result.roundsCount == 1) {
 					countElimination++;
 				}
 			}
 			// retract one as 1 player at least should have finished the map if only one round has been played
 			if (countElimination == race.results.size()) {
 				countElimination-=1;
 			}
 			
 			numberEliminationFirstLapByRaceID[currentIndex][1] = countElimination;
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING elimination
 		Arrays.sort(numberEliminationFirstLapByRaceID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 final Integer numberElimination1 = entry1[1];
                 final Integer numberElimination2 = entry2[1];
                 return numberElimination1.compareTo(numberElimination2);
             }
         });
 	
 		// create the structure for holding results,
 		int currentCount = 0;
 		for (int i = races.size() - 1; i >= 0; i--) {			
 			KTLCRace race = KTLCRace.findById((long)numberEliminationFirstLapByRaceID[i][0]);
 			Player author = Player.findByLogin(race.map.login.name);
 			// the number of elimination
 			int participation = numberEliminationFirstLapByRaceID[i][1];
 
 			Rank<KTLCRace> r = new Rank<KTLCRace>(author, participation, -1, race);
 			ranking.add(currentCount, r);
 			
 			currentCount++;
 			if (currentCount == lengthTop) { break; }
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking for the player that finished at the last place of a race
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @return the list of size lengthTop of players with the highest number of last places
 	 */
 	private static List<Rank<Integer>> calcRankingNumberlastPlaceRace(List<Player> players, int lengthTop) {
 		List<Rank<Integer>> ranking = new ArrayList<Rank<Integer>>(lengthTop);
 		
 		// calculate the number of podiums by race for each player
 		int[][] numberLastPlaceByPlayerID = new int[players.size()][2];
 		
 		int currentIndex = 0;
 		for (Player player : players) {
 			// store the player id in the first cell
 			numberLastPlaceByPlayerID[currentIndex][0] = player.id.intValue();
 			// store the results in the remaining cell
 			List<KTLCRaceResult> results = KTLCRaceResult.findByPlayer(player);
 			if (!results.isEmpty()) {
 				for (KTLCRaceResult result : results) {
 					int numberPlayers = result.race.results.size();
 					if (result.rank == numberPlayers) {
 						numberLastPlaceByPlayerID[currentIndex][1]++;
 					}
 				}
 			}
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING number of last place
 		Arrays.sort(numberLastPlaceByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 final Integer numberLastPlace1 = entry1[1];
                 final Integer numberLastPlace2 = entry2[1];
                 return numberLastPlace1.compareTo(numberLastPlace2);
             }
         });
 		
 		// create the structure for holding results
 		int currentCount = 0;
 		for (int i = players.size() - 1; i >= 0; i--) {	
 			Player player = Player.findById((long)numberLastPlaceByPlayerID[i][0]);
 			int participation = KTLCRaceResult.findByPlayer(player).size();
 			
 			Integer numberLastPlace = numberLastPlaceByPlayerID[i][1];
 			double ratio = (double)numberLastPlace / (double)participation;
 			
 			Rank<Integer> r = new Rank<Integer>(player, participation, ratio, numberLastPlace);
 			ranking.add(currentCount, r);
 			
 			currentCount++;
 			if (currentCount == lengthTop) { break; }
 		}
 		
 		return ranking;		
 	}
 	
 	/**
 	 * Calculate the ranking for the player that finished at the last place of a KTLC
 	 * @param players the list of players
 	 * @param lengthTop the size of the top
 	 * @param minPercentage the minimal percentage that the players should have played to be considered
 	 * @return the list of players of size lengthTop with the highest number of last places
 	 */
 	private static List<Rank<Integer>> calcRankingNumberlastPlaceKTLC(List<Player> players, int lengthTop, int minPercentage) {
 		List<Rank<Integer>> ranking = new ArrayList<Rank<Integer>>(lengthTop);
 		int numberKTLC = KTLCEdition.findAll().size();
 		
 		// calculate the number of podiums by race for each player
 		int[][] numberLastPlaceByPlayerID = new int[players.size()][2];
 		
 		int currentIndex = 0;
 		for (Player player : players) {
 			// store the player id in the first cell
 			numberLastPlaceByPlayerID[currentIndex][0] = player.id.intValue();
 			// store the results in the remaining cell
 			List<KTLCResult> results = KTLCResult.findByPlayer(player);
 			if (!results.isEmpty()) {
 				for (KTLCResult result : results) {
 					int numberPlayers = result.ktlc.results.size();
 					if (result.rank == numberPlayers) {
 						numberLastPlaceByPlayerID[currentIndex][1]++;
 					}
 				}
 			}
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING number of last place
 		Arrays.sort(numberLastPlaceByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 final Integer numberLastPlace1 = entry1[1];
                 final Integer numberLastPlace2 = entry2[1];
                 return numberLastPlace1.compareTo(numberLastPlace2);
             }
         });
 		
 		// create the structure for holding results,
 		// but only for players with at least minPercentage of participation
 		int currentCount = 0;
 		for (int i = players.size() - 1; i >= 0; i--) {			
 			Player player = Player.findById((long)numberLastPlaceByPlayerID[i][0]);
 			int participation = KTLCResult.findByPlayer(player).size();
 			
 			Integer numberLastPlace = numberLastPlaceByPlayerID[i][1];
 			double ratio = (double)numberLastPlace / (double)participation;
 			
 			if (participation >= (minPercentage*0.01*numberKTLC)) {
 				Rank<Integer> r = new Rank<Integer>(player, participation, ratio, numberLastPlace);
 				ranking.add(currentCount, r);
 				
 				currentCount++;
 				if (currentCount == lengthTop) { break; }
 			}			
 		}
 		
 		return ranking;		
 	}
 	
 	/**
 	 * Calculate the ranking for the player that made a "perfect" during a KTLC (finish all the map
 	 * at first place)
 	 * @return the list of players that performed perfects
 	 */
 	private static List<Rank<List<KTLCEdition>>> calcRankingNumberPerfect() {
 		List<Rank<List<KTLCEdition>>> ranking = new ArrayList<Rank<List<KTLCEdition>>>();
 		HashMap<Long, List<KTLCEdition>> listPerfectByPlayerID = new HashMap<Long, List<KTLCEdition>>(); 
 		
 		// find all the results of ktlc where a player finished first and with an rank average of 1, 
 		// which means that the player did win all the maps that he played
 		List<KTLCResult> results = KTLCResult.find("rank = 1 and rankAvg = 1").fetch(); 
 		
 		for (KTLCResult result : results) {
 			// if the player did win all the maps
 			if (result.nbRaces == result.ktlc.races.size()) {
 				Long playerID = Player.findByLogin(result.login.name).id;
 				List<KTLCEdition> editions;
 				if (listPerfectByPlayerID.containsKey(playerID)) {
 					editions = listPerfectByPlayerID.get(playerID);
 				} else {
 					editions = new ArrayList<KTLCEdition>();
 				}
 				//add the ktlc to the list of perfect of the player
 				editions.add(result.ktlc);
 				listPerfectByPlayerID.put(playerID, editions);
 			}
 		}
 		
 		// at this step, we know the players that really did a perfect and at which editions. Now, 
 		// we have to sort them to make the ranking. The sorting has to be done by number of ktlc, 
 		// and if equality, by editions...
 		int[][] numberPerfectByPlayerID = new int[listPerfectByPlayerID.keySet().size()][3];
 		int currentIndex = 0;
 		for (Long playerID : listPerfectByPlayerID.keySet()) {
 			Player player = Player.findById(playerID);
 			numberPerfectByPlayerID[currentIndex][0] = playerID.intValue();							// the player id
 			numberPerfectByPlayerID[currentIndex][1] = listPerfectByPlayerID.get(playerID).size();	// the number of perfect
 			numberPerfectByPlayerID[currentIndex][2] = KTLCResult.findByPlayer(player).size(); 		// the number of ktlc participated
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING number of perfect
 		Arrays.sort(numberPerfectByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 int numPerfect1 = entry1[1];
                 int numKTLC1 = entry1[2];
                 int numPerfect2 = entry2[1];
                 int numKTLC2 = entry2[2];
                 
                 if (numPerfect1 > numPerfect2) {
                 	return 1;
                 } else if (numPerfect1 < numPerfect2) {
                 	return -1;
                 } else {
                 	if (numKTLC1 > numKTLC2) {
                 		return -1;
                 	} else if (numKTLC1 < numKTLC2) {
                 		return 1;
                 	} else {
                 		return 0;
                 	}
                 }
             }
         });
 		
 		// create the structure for holding results
 		for (int i = 0; i < listPerfectByPlayerID.keySet().size(); i++) {
 			int index = listPerfectByPlayerID.keySet().size() - 1 - i;
 			
 			Player player = Player.findById((long)numberPerfectByPlayerID[index][0]);
 			int participation = numberPerfectByPlayerID[index][2];
 			List<KTLCEdition> editions = listPerfectByPlayerID.get((long)numberPerfectByPlayerID[index][0]);
 			
 			Rank<List<KTLCEdition>> r = new Rank<List<KTLCEdition>>(player, participation, -1, editions);
 			ranking.add(i, r);
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate the ranking for the player that made a "epic fail" during a KTLC (eliminated at the 
 	 * first lap of each race of a KTLC)
 	 * @param minNumberMaps the minimal number of maps that the player should have played during a KTLC to be considered
 	 * @return the list of players that performed epic fails
 	 */
 	private static List<Rank<List<KTLCEdition>>> calcRankingNumberEpicFail(int minNumberMaps) {
 		List<Rank<List<KTLCEdition>>> ranking = new ArrayList<Rank<List<KTLCEdition>>>();
 		HashMap<Long, List<KTLCEdition>> listEpicFailByPlayerID = new HashMap<Long, List<KTLCEdition>>(); 
 		
 		// find all the ktlc where a player finished last on all the maps (eliminated at 1st round)
 		List<KTLCEdition> ktlcs = KTLCEdition.findAll();
 		for (KTLCEdition ktlc : ktlcs) {
 			HashMap<Player, Integer> playersEliminatedAt1stLap = new HashMap<Player, Integer>();
 			
 			for (KTLCRace race : ktlc.races) {
 				List<KTLCRaceResult> results = race.results;
 				for (KTLCRaceResult result : results) {
 					// the players that has been eliminated at first lap
 					if (result.roundsCount == 1) {
 						Player player = Player.findByLogin(result.login.name);
 						if (playersEliminatedAt1stLap.containsKey(player)) {
 							int value = playersEliminatedAt1stLap.get(player);
 							playersEliminatedAt1stLap.put(player, value + 1);
 						} else {
 							playersEliminatedAt1stLap.put(player, 1);
 						}
 					}
 				}
 			}
 			
 			for (Player player : playersEliminatedAt1stLap.keySet()) {
 				int numberMapsPlayedByPlayer = KTLCResult.findByKTLCAndPlayer(ktlc, player).nbRaces;
 				
 				// if the player has been eliminated at first lap on all the maps AND played at lest x Maps,
 				// then it's a Epic Fail
 				if (playersEliminatedAt1stLap.get(player) == numberMapsPlayedByPlayer 
 						&& playersEliminatedAt1stLap.get(player) >= minNumberMaps) {
 					List<KTLCEdition> editions;
 					if (listEpicFailByPlayerID.containsKey(player.id)) {
 						editions = listEpicFailByPlayerID.get(player.id);
 					} else {
 						editions = new ArrayList<KTLCEdition>();
 					}
 					//add the ktlc to the list of epic fial of the player
 					editions.add(ktlc);
 					listEpicFailByPlayerID.put(player.id, editions);
 				}
 			}
 		}
 		
 		// at this step, we know the players that really did a perfect and at which editions. Now, 
 		// we have to sort them to make the ranking. The sorting has to be done by number of ktlc, 
 		// and if equality, by editions...
 		int[][] numberEpicFailByPlayerID = new int[listEpicFailByPlayerID.keySet().size()][3];
 		int currentIndex = 0;
 		for (Long playerID : listEpicFailByPlayerID.keySet()) {
 			Player player = Player.findById(playerID);
 			numberEpicFailByPlayerID[currentIndex][0] = playerID.intValue();							// the player id
 			numberEpicFailByPlayerID[currentIndex][1] = listEpicFailByPlayerID.get(playerID).size();	// the number of epic fail
 			numberEpicFailByPlayerID[currentIndex][2] = KTLCResult.findByPlayer(player).size(); 		// the number of ktlc participated
 			currentIndex++;
 		}
 		
 		// sort the array by ASCENDING number of perfect
 		Arrays.sort(numberEpicFailByPlayerID, new Comparator<int[]>() {
             @Override
             public int compare(final int[] entry1, final int[] entry2) {
                 int numPerfect1 = entry1[1];
                 int numKTLC1 = entry1[2];
                 int numPerfect2 = entry2[1];
                 int numKTLC2 = entry2[2];
                 
                 if (numPerfect1 > numPerfect2) {
                 	return 1;
                 } else if (numPerfect1 < numPerfect2) {
                 	return -1;
                 } else {
                 	if (numKTLC1 > numKTLC2) {
                 		return -1;
                 	} else if (numKTLC1 < numKTLC2) {
                 		return 1;
                 	} else {
                 		return 0;
                 	}
                 }
             }
         });
 		
 		// create the structure for holding results		
 		for (int i = 0; i < listEpicFailByPlayerID.keySet().size(); i++) {
 			int index = listEpicFailByPlayerID.keySet().size() - 1 - i;
 			
 			Player player = Player.findById((long)numberEpicFailByPlayerID[index][0]);
 			int participation = numberEpicFailByPlayerID[index][2];
 			List<KTLCEdition> editions = listEpicFailByPlayerID.get((long)numberEpicFailByPlayerID[index][0]);
 			
 			//if (participation >= (MIN_PERCENTAGE_PARTICIPATIONS*0.01*numberKTLC)) {
 				Rank<List<KTLCEdition>> r = new Rank<List<KTLCEdition>>(player, participation, -1, editions);
 				ranking.add(i, r);
 			//}
 		}
 		
 		return ranking;
 	}
 	
 	/**
 	 * Calculate for a particular player his number of results by ranks for the KTLCs.
 	 * @param player
 	 * @return an array of rankLimit values, corresponding to the number of 1st, 2nd, 3rd, ... rankLimit-th places.
 	 */
 	private static int[] ktlcResultsByRanksByPlayer(Player player, int rankLimit) {
 		List<KTLCResult> results = KTLCResult.findByPlayer(player);
 		int[] podiums = new int[rankLimit];
 		
 		if (!results.isEmpty()) {
 			for (KTLCResult result : results) {
 				if (result.rank <= rankLimit) {
 					podiums[result.rank - 1]++;
 				}
 			}
 		}
 		
 		return podiums;
 	}
 	
 	/**
 	 * Calculate for a particular player his number of results by ranks for the races.
 	 * @param player
 	 * @return an array of rankLimit values, corresponding to the number of 1st, 2nd, 3rd, ... rankLimit-th places.
 	 */
 	private static int[] raceResultsByRanksByPlayer(Player player, int rankLimit) {
 		List<KTLCRaceResult> results = KTLCRaceResult.findByPlayer(player);
 		int[] podiums = new int[rankLimit];
 		
 		if (!results.isEmpty()) {
 			for (KTLCRaceResult result : results) {
 				if (result.rank <= rankLimit) {
 					podiums[result.rank - 1]++;
 				}
 			}
 		}
 		
 		return podiums;
 	}
 	
 	/**
 	 * Calculate for a particular player his average rank
 	 * @param player
 	 * @return Double.MAX_VALUE if never played
 	 */
 	private static double averageRankByPlayer(Player player) {
 		List<KTLCResult> results = KTLCResult.findByPlayer(player);
 		if (!results.isEmpty()) {
 			int rank = 0;
 			for (KTLCResult result : results) {
 				rank += result.rank;
 			}
 			return (double)rank / (double)results.size();
 		} else {
 			return Double.MAX_VALUE;
 		}
 	}
 	
 	/**
 	 * Calculate the number of "perfect" of a player, regarding its results
 	 * @return the list of KTLCs where the player performed perfects
 	 */
 	private static List<KTLCEdition> calcPerfects(List<KTLCResult> results) {
 		List<KTLCEdition> editions = new ArrayList<KTLCEdition>();
 		
 		for (KTLCResult result : results) {
 			// if the player did win all the maps
 			if (result.rankAvg == 1.0 && result.nbRaces == result.ktlc.races.size()) {
 				editions.add(result.ktlc);
 			}
 		}
 		
 		return editions;
 	}
 	
 	/**
 	 * Calculate the number of "epic fail" of a player, regarding its results
 	 * @param minNumberMaps the minimal number of maps that the player should have played during a KTLC to be considered
 	 * @return the list of players that performed epic fails
 	 */
 	private static List<KTLCEdition> calcEpicFails(List<KTLCResult> results, int minNumberMaps) {
 		List<KTLCEdition> editions = new ArrayList<KTLCEdition>();
 		
 		// find all the ktlc where a player finished last on all the maps (eliminated at 1st round)
 		for (KTLCResult ktlcResult : results) {
 			int numberElimiation = 0;
 			for (KTLCRace race : ktlcResult.ktlc.races) {
 				for (KTLCRaceResult raceResult : race.results) {
 					// if its the good player and if he did only 1 lap
 					if (raceResult.login.equals(ktlcResult.login) && raceResult.roundsCount == 1) {
 						numberElimiation++;
 					}
 				}
 			}
 			
 			// if the player has been eliminated at first lap on all the maps AND played at lest x Maps,
 			// then it's a Epic Fail
 			if (numberElimiation >= minNumberMaps && ktlcResult.nbRaces == numberElimiation) {
 				editions.add(ktlcResult.ktlc);
 			}
 		}		
 		return editions;
 	}
 	
 	/**
 	 * Compare two integer values and tell if its better, worse or equal textually, use to compare 
 	 * stats and assign correct style. Some time its better to have a bigger value.
 	 * @param v1
 	 * @param v2
 	 * @param bigIsBetter invert the logic
 	 * @return 
 	 */
 	private static String compareValues(int v1, int v2, boolean bigIsBetter) {
 		if(bigIsBetter) {
 			if (v1 > v2) {
 				return "better";
 			} else if (v1 < v2) {
 				return "worse";
 			} else {
 				return "equal";
 			}
 		} else {
 			if (v1 > v2) {
 				return "worse";
 			} else if (v1 < v2) {
 				return "better";
 			} else {
 				return "equal";
 			}	
 		}
 	}
 	
 	/**
 	 * Compare two double values and tell if its better, worse or equal textually, use to compare 
 	 * stats and assign correct style. Some time its better to have a bigger value.
 	 * @param v1
 	 * @param v2
 	 * @param bigIsBetter invert the logic
 	 * @return 
 	 */
 	private static String compareValues(double v1, double v2,  boolean bigIsBetter) {
 		if(bigIsBetter) {
 			if (v1 > v2) {
 				return "better";
 			} else if (v1 < v2) {
 				return "worse";
 			} else {
 				return "equal";
 			}
 		} else {
 			if (v1 > v2) {
 				return "worse";
 			} else if (v1 < v2) {
 				return "better";
 			} else {
 				return "equal";
 			}	
 		}
 	}
 }
