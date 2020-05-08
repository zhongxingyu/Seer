 package se.chalmers.dryleafsoftware.androidrally.game;
 
 public class GameSettings {
 	
 	private final static int DEFAULT_HUMAN_PLAYERS = 1;
 	private final static int DEFAULT_BOTS = 7;
 	private final static int DEFAULT_HOURS_ROUND = 24;
 	private final static int DEFAULT_CARD_SECOND = 45;
 
 
 	private static int nbr_of_human_players = DEFAULT_HUMAN_PLAYERS;
 	private static int nbr_of_bots = DEFAULT_BOTS;
 	private static int hours_each_round = DEFAULT_HOURS_ROUND;
 	private static int	card_timer_seconds = DEFAULT_CARD_SECOND;
 	
 	private static final String DEFAULT_MAP = null;
 	private static String map = DEFAULT_MAP;
 	
	private static GameSettings currentSettings;
	
 	public GameSettings() {
 		 this(DEFAULT_HUMAN_PLAYERS, DEFAULT_BOTS);
 	}
 
 	public GameSettings(int humanPlayers, int bots) {
 		this(humanPlayers, bots, DEFAULT_HOURS_ROUND, DEFAULT_CARD_SECOND);
 
 	}
 
 	public GameSettings(int humanPlayer, int bots, int hoursEachRound,
 			int cardTimeSeconds) {
 		this(humanPlayer, bots, hoursEachRound, cardTimeSeconds, DEFAULT_MAP);
 
 	}
 
 	public GameSettings(int humanPlayer, int bots, int hoursEachRound,
 			int cardTimeSeconds, String map) {
 		GameSettings.nbr_of_human_players = humanPlayer;
 		GameSettings.nbr_of_bots = bots;
 		GameSettings.hours_each_round = hoursEachRound;
 		GameSettings.card_timer_seconds = cardTimeSeconds;
 		GameSettings.map = map;
 	}
 	
 	public int getNbrOfHumanPlayers() {
 		return GameSettings.nbr_of_human_players;
 	}
 
 	public int getNbrOfBots() {
 		return GameSettings.nbr_of_bots;
 	}
 
 	public int getHoursEachRound() {
 		return GameSettings.hours_each_round;
 	}
 
 	public int getCardTimerSeconds() {
 		return GameSettings.card_timer_seconds;
 	}
 
 	public String getMap() {
 		return GameSettings.map;
 	}
 
	public static void setCurrentSettings(GameSettings settings) {
		currentSettings = settings;
	}
	
 	public static GameSettings getCurrentSettings() {
		if (currentSettings == null) {
			currentSettings = new GameSettings();
		}
		return currentSettings;
 	}
 
 }
