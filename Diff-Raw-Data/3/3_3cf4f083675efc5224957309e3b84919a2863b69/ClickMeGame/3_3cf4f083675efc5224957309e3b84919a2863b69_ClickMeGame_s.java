 package com.harcourtprogramming.clickme;
 
 import com.sun.faces.util.LRUMap;
 import java.util.HashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  *
  * @author Benedict
  */
 final class ClickMeGame
 {
 	protected class TimerDelegate implements Runnable
 	{
 		TimerDelegate()
 		{
 		}
 
 		@Override
 		public void run()
 		{
 			ClickMeGame.instance.newEntry();
 		}
 	}
 	
 	private final static ClickMeGame instance = new ClickMeGame();
 	
 	private static final int timeQuantum = 30;
 	private static final int historyQuanta = 24;
 	
 	private int currEntry = 0;
 	private ScheduledExecutorService scheduler =  Executors.newScheduledThreadPool(1);
 	/**
 	 * 
 	 */
 	@SuppressWarnings("unchecked")
 	private LRUMap<Integer,HashMap<String, Integer>> history = new LRUMap<Integer, HashMap<String, Integer>>(historyQuanta);
 	ScoreTable scores = new ScoreTable();
 	
 	public static ClickMeGame getInstance()
 	{
 		return instance;
 	}
 	
 	ClickMeGame()
 	{
 		for (currEntry = 0; currEntry < historyQuanta;)
 		{
 			++currEntry;
 			history.put(currEntry, new HashMap<String, Integer>());
 		}
 		
 		scheduler.scheduleAtFixedRate(new TimerDelegate(), timeQuantum, timeQuantum, TimeUnit.MINUTES);
 	}
 
 	@Override
 	protected void finalize() throws Throwable
 	{
 		scheduler.shutdownNow();		
 		super.finalize();
 	}
 	
 	final synchronized void newEntry()
 	{
 		++currEntry;
		history.put(currEntry, new HashMap<String, Integer>());
		scores.subtractAll(history.get(currEntry - 1).entrySet());
 	}
 	
 	public synchronized void addScore(String player, int score)
 	{
 		HashMap<String, Integer> latestScores = history.get(currEntry);
 		
 		if (latestScores.containsKey(player))
 		{
 			Integer s = latestScores.get(player);
 			latestScores.put(player, s + score);
 		}
 		else
 		{
 			latestScores.put(player, score);
 		}
 
 		scores.add(player, score);
 	}
 	
 	public synchronized ScoreTable getScores()
 	{
 		return scores.readOnlyCopy();
 	}
 }
