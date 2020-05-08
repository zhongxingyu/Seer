 package com.vikinghammer.nfl.scoreboard.model;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Random;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 import org.codehaus.jackson.annotate.JsonProperty;
 
 import com.vikinghammer.nfl.scoreboard.date.LocalTimeCalculator;
 
 @JsonIgnoreProperties({ "possessionTeam", "seasonType", "season", 
 	"weekNumber", "dateTime_monthday", "dateTime_daytime", "dateTime_date",
 	"preview", "recap", "awayTeamScores", "homeTeamScores", "gameKey",
	"game_top_performer"
 	})
 public class Game {
 	
 	public static final String GAME_ID = "GAME_ID";
 	
 	private long internalId = (new Random()).nextLong();
 
 	@JsonProperty("id")
 	private String gameId;
 	
 	private String status;
 	
 	private String label;
 	private String stadium;
 	private String tvNetwork;
 	
 	@JsonProperty("dateTime")
 	private String dateTime;
 	
 	@JsonProperty("dateTime_time")
 	private String dateTime_time;
 	
 	private transient Calendar localTime = null;
 	
 	private String weather;
 	private String temperatures;
 	private String conditions;
 	
 	private int periodCount;
 	
 	@JsonProperty("insideTwenty")
 	private boolean insideTwenty;
 	
 	@JsonProperty("isFinal")
 	private boolean isFinal;
 	
 	@JsonProperty("isHalftime")
 	private boolean isHalftime;
 	
 	@JsonProperty("isInProgress")
 	private boolean isInProgress;
 	
 	@JsonProperty("isPreGame")
 	private boolean isPreGame;
 	
 	@JsonProperty("crossedRedZone")
 	private boolean isCrossedRedZone;
 	
 	@JsonProperty("hasJustScored")
 	private boolean isJustScored;
 	
 	private int down;
 	private String gameClock;
 	private String hasPossession;
 	private int quarter;
 	private int yardsToGo;
 	private int yardsFromGoal;
 	
 	private TeamInfo winner;
 	
 	private TeamInfo awayTeam;
 	private String awayTeamRecord;
 	private int awayScore;
 	private int awayQ1Score;
 	private int awayQ2Score;
 	private int awayQ3Score;
 	private int awayQ4Score;
 	private int awayOtScore;
 	private int awayTeamTimeoutsRemaining;
 	
 	private TeamInfo homeTeam;
 	private String homeTeamRecord;
 	private int homeScore;
 	private int homeQ1Score;
 	private int homeQ2Score;
 	private int homeQ3Score;
 	private int homeQ4Score;
 	private int homeOtScore;
 	private int homeTeamTimeoutsRemaining;
 	
 	public long getInternalId() {
 		return internalId;
 	}
 
 	public String getGameId() {
 		return gameId;
 	}
 
 	public String getStatus() {
 		return status;
 	}
 	
 	public String getLabel() {
 		return label;
 	}
 	public String getStadium() {
 		return stadium;
 	}
 	public String getTvNetwork() {
 		return tvNetwork;
 	}
 	
 	public Calendar getLocalTime() {
 		if (localTime == null) {
 			String[] timePieces = dateTime_time.split(" ");
 			if (timePieces.length >= 2) {
 				LocalTimeCalculator calculator = new LocalTimeCalculator(timePieces[0], timePieces[1]);
 				localTime = calculator.getLocalTime();
 				
 				try {
 					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
 					String[] datePieces = dateTime.split("T");
 					Date date = sdf.parse(datePieces[0]);
 					Calendar dateCal = Calendar.getInstance();
 					dateCal.setTime(date);
 					
 					localTime.set(Calendar.YEAR, dateCal.get(Calendar.YEAR));
 					localTime.set(Calendar.MONTH, dateCal.get(Calendar.MONTH));
 					localTime.set(Calendar.DAY_OF_MONTH, dateCal.get(Calendar.DAY_OF_MONTH));
 				} catch (Exception e) {
 					
 				}
 				
 			} else {
 				return Calendar.getInstance();
 			}
 		}
 		return localTime;
 	}
 	
 	public String getWeather() {
 		return weather;
 	}
 	public String getTemperatures() {
 		return temperatures;
 	}
 	public String getConditions() {
 		return conditions;
 	}
 	
 	public int getPeriodCount() {
 		return periodCount;
 	}
 	
 	public boolean isInsideTwenty() {
 		return insideTwenty;
 	}
 	public boolean isFinal() {
 		return isFinal;
 	}
 	public boolean isHalftime() {
 		return isHalftime;
 	}
 	public boolean isInProgress() {
 		return isInProgress;
 	}
 	public boolean isPreGame() {
 		return isPreGame;
 	}
 	public boolean isCrossedRedZone() {
 		return isCrossedRedZone;
 	}
 	public boolean isJustScored() {
 		return isJustScored;
 	}
 	
 	public TeamInfo getWinner() {
 		return winner;
 	}
 	
 	public TeamInfo getAwayTeam() {
 		return awayTeam;
 	}
 	public String getAwayTeamRecord() {
 		return awayTeamRecord;
 	}
 	public int getAwayScore() {
 		return awayScore;
 	}
 	public int getAwayQ1Score() {
 		return awayQ1Score;
 	}
 	public int getAwayQ2Score() {
 		return awayQ2Score;
 	}
 	public int getAwayQ3Score() {
 		return awayQ3Score;
 	}
 	public int getAwayQ4Score() {
 		return awayQ4Score;
 	}
 	public int getAwayOtScore() {
 		return awayOtScore;
 	}
 	public int getAwayTeamTimeoutsRemaining() {
 		return awayTeamTimeoutsRemaining;
 	}
 	
 	public TeamInfo getHomeTeam() {
 		return homeTeam;
 	}
 	public String getHomeTeamRecord() {
 		return homeTeamRecord;
 	}
 	public int getHomeScore() {
 		return homeScore;
 	}
 	public int getHomeQ1Score() {
 		return homeQ1Score;
 	}
 	public int getHomeQ2Score() {
 		return homeQ2Score;
 	}
 	public int getHomeQ3Score() {
 		return homeQ3Score;
 	}
 	public int getHomeQ4Score() {
 		return homeQ4Score;
 	}
 	public int getHomeOtScore() {
 		return homeOtScore;
 	}
 	public int getHomeTeamTimeoutsRemaining() {
 		return homeTeamTimeoutsRemaining;
 	}
 	
 	public int getDown() {
 		return down;
 	}
 	public String getDownName() {
 		return translateNumberToName(down);
 	}
 	public String getGameClock() {
 		return gameClock;
 	}
 	public String getHasPossession() {
 		return hasPossession;
 	}
 	public int getQuarter() {
 		return quarter;
 	}
 	public String getQuarterName() {
 		if (5 == quarter) {
 			return "OT";
 		} else {
 			return translateNumberToName(quarter);
 		}
 	}
 	public int getYardsToGo() {
 		return yardsToGo;
 	}
 	public int getYardsFromGoal() {
 		return yardsFromGoal;
 	}
 	
 	private String translateNumberToName(int number) {
 		switch(number) {
 		case 1:
 			return "1st";
 		case 2:
 			return "2nd";
 		case 3:
 			return "3rd";
 		case 4:
 			return "4th";
 		default:
 			return String.format("%s", number);
 		}
 	}
 }
