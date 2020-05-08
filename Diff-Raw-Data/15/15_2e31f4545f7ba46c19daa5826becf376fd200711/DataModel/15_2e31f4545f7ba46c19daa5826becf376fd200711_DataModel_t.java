 package ww10.gui;
 
 import game.PublicGameInfo;
 import game.stats.BankrollObserver;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.locks.ReentrantLock;
 
 import javax.swing.SwingUtilities;
 
 import bots.prologbot.PrologBot;
 
 import com.biotools.meerkat.Action;
 import com.biotools.meerkat.Card;
 import com.biotools.meerkat.GameInfo;
 import com.biotools.meerkat.GameObserver;
 
 public class DataModel implements BankrollObserver, GameObserver {
 
 	private final ReentrantLock pauseLock = new ReentrantLock();
 
 	private List<String> playerNames;
 
 	private volatile int currentGamesPlayed;
 
 	//Bidirectional map
 	private HashMap<String, String> chosenNames = new HashMap<String, String>();
 	private HashMap<String, String> fixedNames = new HashMap<String, String>();
 
 	private final LinkedList<GameStats> bankrollWindow = new LinkedList<GameStats>();
 	//private final LinkedList<GameStats> plotTimes = new LinkedList<GameStats>();
 	private final LinkedList<MarkerStats> markerTimes = new LinkedList<MarkerStats>();
 
 	private int nbGamesInTotalProfit = 0;
 
 	private final Map<String, Double> currentTotalProfit = new HashMap<String, Double>();
 
 	private final Map<String, Long> currentRuntime = new HashMap<String, Long>();
 
 	private final long plotWindow = 180 * 1000;
 
 	private final long minBankrollWindow = 60*1000; //60s
 	
 	private final int maxBankRollSize = 10000;
 	
 	private long lastSubmitTime = System.currentTimeMillis();
 
 	private final HashMap<String, int[]> currentActionFreqs = new HashMap<String, int[]>();
 
 	private final HashMap<String, HashMap<String, Integer>> currentRulesUsed = new HashMap<String, HashMap<String, Integer>>();
 	
 	private final HashMap<String, Long> lastSubmits = new HashMap<String, Long>();
 
 	private GameInfo gameInfo;
 	
 	private boolean paused = false;
 	private boolean terminated = false;
 	
 	public DataModel() {
 	}
 
 	public synchronized List<String> getPlayerNames() {
 		return playerNames;
 	}
 
 	public synchronized int getCurrentGamesPlayed() {
 		return currentGamesPlayed;
 	}
 	
 
 	public synchronized String getChosenName(String fixedName) {
 		String chosenName = chosenNames.get(fixedName);
 		if (chosenName == null)
 			return fixedName;
 		else
 			return chosenName;
 	}
 	
 	public synchronized String getLongChosenName(String fixedName) {
 		String chosenName = chosenNames.get(fixedName);
 		if (chosenName == null)
 			return fixedName;
 		else
 			return chosenName + " (" + fixedName + ")";
 	}
 	
 	public synchronized String getFixedName(String chosenName){
 		return fixedNames.get(chosenName);
 	}
 	
 	public synchronized int getNbFolds(String player){
 		return currentActionFreqs.get(player)[0];
 	}
 	
 	public synchronized int getNbCalls(String player){
 		return currentActionFreqs.get(player)[1];
 	}
 	
 	public synchronized int getNbRaises(String player){
 		return currentActionFreqs.get(player)[2];
 	}
 	
 	public synchronized long getRunTime(String player)
 	{
 		return currentRuntime.get(player);
 	}
 	
 	public synchronized HashMap<String, Integer> getRulesUsed(String player){
 		return currentRulesUsed.get(player);
 	}
 	
 	public synchronized double getAvgProfit(String player){
 		return getAvgProfit().get(player);
 	}
 	
 	public synchronized int getLastSubmit(String player){
 		return (int) (System.currentTimeMillis() - lastSubmits.get(player));
 	}
 	
 	public synchronized int getLastSubmit()
 	{
 		return (int) (System.currentTimeMillis() - lastSubmitTime);
 	}
 
	public synchronized void refreshLastSubmit()
	{
		lastSubmitTime = System.currentTimeMillis();
	}

 	
 	private int[] markerLevelUse = new int[20];
 
 	public synchronized void onSubmit(final String fixedName, final String chosenName) {
 		String oldName = chosenNames.get(fixedName);
 		if(oldName != null && !oldName.equals(chosenName)){
 			fixedNames.remove(oldName);
 		}
 		
 		chosenNames.put(fixedName, chosenName);
 		fixedNames.put(chosenName, fixedName);
 		lastSubmitTime = System.currentTimeMillis();
 		lastSubmits.put(fixedName, lastSubmitTime);
 		MarkerStats stats = new MarkerStats(lastSubmitTime, getAvailableMarkerLevel());
 		markerTimes.add(stats);
 		/*SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				gui.averageProfitPanel.addMarker(fCurrentGamesPlayed, chosenName + " ingediend", offset);
 				gui.averageProfitPanel.setChosenName(fixedName, chosenName);
 				gui.actionPanel.setChosenName(fixedName, chosenName);
 			}
 		});*/
 		
 		
 	}
 
 	private int getAvailableMarkerLevel() {
 		int minValue = markerLevelUse[0];
 		int minIndex = 0;
 		for (int i = 1; i < markerLevelUse.length; i++) {
 			if (markerLevelUse[i] < minValue) {
 				minValue = markerLevelUse[i];
 				minIndex = i;
 			}
 		}
 		markerLevelUse[minIndex]++;
 		return minIndex;
 	}
 
 	@Override
 	public synchronized void gameStarted(int numSeatPermutations, int numGames, Set<String> playerNames) {
 		this.playerNames = Arrays.asList(playerNames.toArray(new String[playerNames.size()]));
 		Collections.sort(this.playerNames, new Comparator<String>() {
 
 			@Override
 			public int compare(String o1, String o2) {
 				try {
 					int id1 = Integer.parseInt(o1.replaceAll("#", ""));
 					int id2 = Integer.parseInt(o2.replaceAll("#", ""));
 					return id1 - id2;
 				} catch (NumberFormatException e) {
 					return o1.compareTo(o2);
 				}
 			}
 
 		});
 		
 		lastSubmitTime = System.currentTimeMillis();
 
 		for (String player : playerNames) {
 			currentTotalProfit.put(player, 0.0);
 			currentActionFreqs.put(player, new int[3]);
 			HashMap<String, Integer> rules = new HashMap<String, Integer>();
 			rules.put("Geen", 0);
 			currentRulesUsed.put(player, rules);
 			currentRuntime.put(player, 0L);
 			lastSubmits.put(player, System.currentTimeMillis());
 		}
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				//gui.initialize();
 				int framerate = 10;
 				int delay = 1000 / framerate;
 				int interval = 1000 / framerate;
 				final Timer timer = new Timer();
 				timer.scheduleAtFixedRate(new TimerTask() {
 					public void run() {
 						//TODO if table is deleted, terminate timer
 						if(terminated)
 						{
 							timer.cancel();
 						}
 						
 						pauseLock.lock();
 						pauseLock.unlock();
 						//						System.out.println("UPDATE PLOT");
 						if(!paused)
 						{
 							updatePlot();
 						}
 					}
 				}, delay, interval);
 			}
 		});
 
 	}
 	
 	public synchronized void gamePaused(){
 		paused = true;
 	}
 	
 	public synchronized void gameResumed(){
 		paused = false;
 	}
 	
 	public synchronized void gameTerminated(){
 		terminated = true;
 	}
 
 	/**
 	 * First try pause lock, then synchronize on this.
 	 */
 	@Override
 	public void updateBankroll(int seatpermutation, final Map<String, Double> playerDelta) {
 		pauseLock.lock();
 		pauseLock.unlock();
 		synchronized (this) {
 			currentGamesPlayed++;
 
 			//update stats
 
 			currentGameStats.time = System.currentTimeMillis();
 			currentGameStats.game = currentGamesPlayed;
 			currentGameStats.bankrollDeltas = playerDelta;
 
 			addGameStats(currentGameStats);
 			bankrollWindow.add(currentGameStats);
 
 			//		if (currentGamesPlayed % 1 == 0) {
 			//			updatePlot();
 			//		}			
 		}
 	}
 
 	public synchronized void updatePlot() {
 		
 		//remove old stats
 
 		// update bankroll
 		removeFromWindow();
 
 		// update plot
 
 		// TODO: Are these two useful?
 	//	removeDatapoints();
 
 		removeMarkers();
 
 
 
 		//plotTimes.add(currentGameStats);
 	}
 		
 	private int removeFromWindow() {
 		Iterator<GameStats> iter = bankrollWindow.iterator();
 		boolean done = false;
 		int nbRemoved = 0;
 		long currentTime = System.currentTimeMillis();
 		while (!done && iter.hasNext()) {
 			GameStats first = iter.next();
 			if (first.time < Math.min(lastSubmitTime, currentTime - minBankrollWindow)
 					|| bankrollWindow.size() > maxBankRollSize) {
 				nbRemoved++;
 				undoGameStats(first);
 				iter.remove();
 			} else {
 				done = true;
 			}
 		}
 		return nbRemoved;
 	}
 
 //	private int removeDatapoints() {
 //		Iterator<GameStats> iter = plotTimes.iterator();
 //		boolean done = false;
 //		int nbRemoved = 0;
 //		long currentTime = System.currentTimeMillis();
 //		while (!done && iter.hasNext()) {
 //			GameStats first = iter.next();
 //			if (first.time < currentTime - plotWindow) {
 //				nbRemoved++;
 //				iter.remove();
 //			} else {
 //				done = true;
 //			}
 //		}
 //		return nbRemoved;
 //	}
 
 	private int removeMarkers() {
 		Iterator<MarkerStats> iter = markerTimes.iterator();
 		boolean done = false;
 		int nbRemoved = 0;
 		long currentTime = System.currentTimeMillis();
 		while (!done && iter.hasNext()) {
 			MarkerStats first = iter.next();
 			if (first.time < currentTime - plotWindow) {
 				nbRemoved++;
 				markerLevelUse[first.level]--;
 				iter.remove();
 			} else {
 				done = true;
 			}
 		}
 		return nbRemoved;
 	}
 
 	private ConcurrentHashMap<String, Double> getAvgProfit() {
 		ConcurrentHashMap<String, Double> avgProfit = new ConcurrentHashMap<String, Double>();
 		for (String player : playerNames) {
 			double currentProfit = currentTotalProfit.get(player);
 			avgProfit.put(player, currentProfit / nbGamesInTotalProfit);
 		}
 		return avgProfit;
 	}
 	
 	
 
 	private void addGameStats(final GameStats stats) {
 		nbGamesInTotalProfit++;
 		for (String player : playerNames) {
 			//profit
 			double currentProfit = currentTotalProfit.get(player);
 			currentTotalProfit.put(player, currentProfit + stats.bankrollDeltas.get(player));
 			//actions
 			int[] prevFreq = currentActionFreqs.get(player);
 			int[] statFreq = stats.actionFreqs.get(player);
 			for (int i = 0; i < statFreq.length; i++) {
 				prevFreq[i] += statFreq[i];
 			}
 			//rules
 			HashMap<String, Integer> prevRules = currentRulesUsed.get(player);
 			HashMap<String, Integer> statsRules = stats.ruleCounts.get(player);
 			for (Entry<String, Integer> e : statsRules.entrySet()) {
 				Integer count = prevRules.get(e.getKey());
 				if (count == null) {
 					count = 0;
 				}
 				prevRules.put(e.getKey(), count + e.getValue());
 			}
 			// runtime 
 			long prevRuntime = currentRuntime.get(player);
 			long gameRuntime = stats.runtimes.get(player);
 			currentRuntime.put(player, prevRuntime + gameRuntime);
 		}
 	}
 
 	private void undoGameStats(final GameStats stats) {
 		nbGamesInTotalProfit--;
 		for (String player : playerNames) {
 			//profit
 			double currentProfit = currentTotalProfit.get(player);
 			currentTotalProfit.put(player, currentProfit - stats.bankrollDeltas.get(player));
 			//actions
 			int[] prevFreq = currentActionFreqs.get(player);
 			int[] statFreq = stats.actionFreqs.get(player);
 			for (int i = 0; i < statFreq.length; i++) {
 				prevFreq[i] -= statFreq[i];
 			}
 			//rules
 			HashMap<String, Integer> prevRules = currentRulesUsed.get(player);
 			HashMap<String, Integer> statsRules = stats.ruleCounts.get(player);
 			for (Entry<String, Integer> e : statsRules.entrySet()) {
 				Integer count = prevRules.get(e.getKey());
 				if (count == null) {
 					count = 0;
 				}
 				prevRules.put(e.getKey(), count - e.getValue());
 			}
 			// runtime 
 			long prevRuntime = currentRuntime.get(player);
 			long gameRuntime = stats.runtimes.get(player);
 			currentRuntime.put(player, prevRuntime - gameRuntime);
 		}
 	}
 
 	public synchronized float getGamesPerSecond() {
 		int nbGames = getCurrentBankrollGameWindow();
 		long time = getCurrentBankrollTimeWindow();
 		return (float) (nbGames / (time / 1000.0));
 	}
 
 	public synchronized int getCurrentBankrollGameWindow() {
 		if (bankrollWindow.isEmpty())
 			return 0;
 		return bankrollWindow.getLast().game - bankrollWindow.getFirst().game;
 	}
 
 	public synchronized long getCurrentBankrollTimeWindow() {
 		if (bankrollWindow.isEmpty())
 			return 0;
 		return bankrollWindow.getLast().time - bankrollWindow.getFirst().time;
 	}
 
 	private GameStats currentGameStats;
 
 	@Override
 	public void actionEvent(int pos, Action act) {
 		String player = gameInfo.getPlayerName(pos);
 		int[] freq = currentGameStats.actionFreqs.get(player);
 		switch (act.getType()) {
 		case Action.SMALL_BLIND:
 			break;
 		case Action.BIG_BLIND:
 			break;
 		case Action.CALL:
 			freq[1]++;
 			updateRuleEvalStats(player);
 			break;
 		case Action.RAISE:
 			freq[2]++;
 			updateRuleEvalStats(player);
 			break;
 		case Action.BET:
 			freq[2]++;
 			updateRuleEvalStats(player);
 			break;
 		case Action.CHECK:
 			freq[1]++;
 			updateRuleEvalStats(player);
 			break;
 		case Action.FOLD:
 			freq[0]++;
 			updateRuleEvalStats(player);
 			break;
 		case Action.MUCK:
 			break;
 		}
 
 	}
 
 	private void updateRuleEvalStats(String player) {
 		PublicGameInfo publicGameInfo = (PublicGameInfo) gameInfo;
 		PrologBot bot = (PrologBot) (publicGameInfo.getPlayer(player).getBot());
 		// rules
 		String lur = bot.getLastUsedRule();
 		if (lur == null) {
 			throw new IllegalStateException();
 		}
 		//		System.out.println(player + " used rule " + lur);
 		HashMap<String, Integer> ruleCounts = currentGameStats.ruleCounts.get(player);
 		Integer count = ruleCounts.get(lur);
 		if (count == null) {
 			count = 0;
 		}
 		ruleCounts.put(lur, count + 1);
 		// runtime
 		long actionRuntime = bot.getLastRuntime();
 		Long prevRuntime = currentGameStats.runtimes.get(player);
 		currentGameStats.runtimes.put(player, prevRuntime + actionRuntime);
 	}
 
 	@Override
 	public void dealHoleCardsEvent() {
 	}
 
 	@Override
 	public void gameOverEvent() {
 	}
 
 	@Override
 	public void gameStartEvent(GameInfo gameInfo) {
 		this.gameInfo = gameInfo;
 		currentGameStats = new GameStats();
 		for (String player : playerNames) {
 			currentGameStats.actionFreqs.put(player, new int[3]);
 			HashMap<String, Integer> rules = new HashMap<String, Integer>();
 			rules.put("Geen", 0);
 			currentGameStats.ruleCounts.put(player, rules);
 			currentGameStats.runtimes.put(player, 0L);
 		}
 	}
 
 	@Override
 	public void gameStateChanged() {
 	}
 
 	@Override
 	public void showdownEvent(int arg0, Card arg1, Card arg2) {
 	}
 
 	@Override
 	public void stageEvent(int arg0) {
 	}
 
 	@Override
 	public void winEvent(int arg0, double arg1, String arg2) {
 	}
 
 	public void togglePaused() {
 		if (pauseLock.isHeldByCurrentThread()) {
 			pauseLock.unlock();
 		} else {
 			pauseLock.lock();
 		}
 	}
 }
 
 final class GameStats {
 
 	long time;
 	int game;
 	Map<String, Double> bankrollDeltas;
 
 	final HashMap<String, int[]> actionFreqs = new HashMap<String, int[]>();
 	final HashMap<String, HashMap<String, Integer>> ruleCounts = new HashMap<String, HashMap<String, Integer>>();
 	final HashMap<String, Long> runtimes = new HashMap<String, Long>();
 
 	public GameStats() {
 
 	}
 }
 
 final class MarkerStats {
 
 	final long time;
 	final int level;
 
 	public MarkerStats(long time, int level) {
 		this.time = time;
 		this.level = level;
 	}
 
}
