 package de.freinsberg.pomecaloco;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.opencv.core.Mat;
 import org.opencv.core.Scalar;
 
 import android.content.Context;
 import android.graphics.Color;
 import android.text.format.Time;
 import android.util.Log;
 import android.view.View;
 import android.widget.Chronometer;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class Race {
 	
 	private Mat mPreparedTrack;	
 	public static int ROUND_MODE = 1;
 	public static int TIMER_MODE = 2;	
 	public static int LEFT_LANE = 1;
 	public static int RIGHT_LANE = 2;
 	private boolean mLeftMovement = false;
 	private boolean mRightMovement = false;
 	private boolean mRaceStarted = false;
 	private boolean mTimeIsUp = false;
 	private DBHelper mDbHelper;
 	private int mMode;
 	private int mCount;
 	private int mCarStatus;
 	private int mActLeftRound;
 	private int mActRightRound;
 	private double mActLeftSpeed;
 	private double mActRightSpeed;
 	private double mOldBestTimeLeft;
 	private int[] mOldBestTimeLeftArray;
 	private int[] mOldBestTimeRightArray;
 	private double mOldBestTimeRight;
 	private String mActLeftRoundTime;		
 	private String mActRightRoundTime;
 	private String mBestTime;
 	private String mOldTimeLeft;
 	private String mOldTimeRight;
 	private String mTrackName;
 	private float mTrackLength;
 	private int mNumberOfPlayers;
 	private Pair<String,Integer> mBestTimeOnLane;
 	public List<Player> mPlayerArray = new ArrayList<Player>();
 	public MyTimer mRaceTimer;
 	public MillisecondChronometer mChronometer;	
 	private TextView mTimer;
 	private TextView mSpeedUpdater;
 	private TextView mBestTimeUpdater;
 	private List<String> mLeftTimes = new ArrayList<String>();
 	private List<String> mRightTimes = new ArrayList<String>();	
 	private static Race mInstance = new Race();	
 	
 	/**
 	 * Constructor: This private Constructor makes this Class a Singleton- Class.
 	 */
 	private Race() {}
 	
 	/**
 	 * This Function is used to get an Instance of the Race. So the Activities have the ability to use Methods of this class without creating an Object from it.
 	 * @return An Instance of a Race.
 	 */
 	public static Race getInstance(){
 		return mInstance;
 	};
 	
 	/**
 	 * This Method generates a new race with the given parameters count, mode, track, numberofplayers.
 	 * These parameters are mapped to the appropriate member variables to make them usable during a race.
 	 * @param count The Number of Round (Roundmode) or the Number of Minutes (Timermode) the race will have.
 	 * @param mode The gamemode for this race, wether Roundmode or Timermode.
 	 * @param track The track to drive on.
 	 * @param numberofplayers The Number of players on this track.
 	 */
 	@SuppressWarnings("unchecked")
 	public void newRace(Context context, int count, int mode, Object track, int carStatus, List<String> names) {				
 		mDbHelper = new DBHelper(context);
 		mDbHelper.openDB();
 		mPlayerArray.clear();		
 		mMode = mode;
 		mCount = count;
 		mTrackName = ((Pair<String, byte[]>) track).getL();
 		mTrackLength = mDbHelper.getTrackLength(mTrackName);
 		mCarStatus = carStatus;
 		createPlayer(names);
 		Log.i("debug","Neues Rennen:(1=Rundenrennen,2=Zeitrennen)Spielmodus"+mMode+", Counter: "+mCount+", Track: "+mTrackName+", NumberOfPlayers: "+mNumberOfPlayers+" erstellt");		
 	}
 
 	/**
 	 * This Method creates players for a race, the given parameter carstatus defines the lane for the player. 
 	 * The parameter names has to contain a List of names with name at position 0 in oneplayer mode or with name for left player at position 0 and right player at position 1 in twoplayer mode.   
 	 * @param carStatus The carstatus on the track.
 	 * @param names A List of names that is been assigned to the players.
 	 */
 	private void createPlayer(List<String> names) {
 		switch(mCarStatus){
 		case ObjectDetector.NO_CAR:			
 			break;
 		case ObjectDetector.RIGHT_CAR:			
 			mPlayerArray.add(new Player(names.get(0), RIGHT_LANE,mMode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));
 			break;
 		case ObjectDetector.LEFT_CAR:							
 			mPlayerArray.add(new Player(names.get(0), LEFT_LANE,mMode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
 			break;
 		case ObjectDetector.BOTH_CAR:
 			mPlayerArray.add(new Player(names.get(0), LEFT_LANE,mMode, ObjectDetector.getInstance().getCarColor(LEFT_LANE)));
 			mPlayerArray.add(new Player(names.get(1), RIGHT_LANE,mMode, ObjectDetector.getInstance().getCarColor(RIGHT_LANE)));			
 			break;	
 		default:
 			break;		
 		}
 	}	
 
 	/**
 	 * This Method sets variables to initialize the race. The given Context is used to initialize the MillisecondChronometer(Roundmode) with this context.
 	 * The parameter time is used to set the timer value(Timermode). 
 	 * @param c The Context of the actual activity
 	 * @param time The TextView that represents the Timer during Timermode.
 	 */
 	public void initRace(Context c, TextView time) {		
 		if(!mLeftTimes.isEmpty())
 			mLeftTimes.clear();
 		if(!mRightTimes.isEmpty())
 			mRightTimes.clear();
 		mOldBestTimeLeft = Double.MAX_VALUE;
 		mOldBestTimeRight = Double.MAX_VALUE;
 		mOldBestTimeLeftArray = new int[3];
 		mOldBestTimeRightArray = new int[3];
 		mRaceStarted = false;		
 		mTimer = time;
 		mActLeftRound = 0;
 		mActRightRound = 0;
 		mActLeftRoundTime = "00:00:00";
 		mActRightRoundTime = "00:00:00";
 		
 		if(mMode == TIMER_MODE)
 		{
 			mRaceTimer = new MyTimer(mCount*60000, 10, mTimer);
 			mOldTimeLeft = mRaceTimer.getCurrentTime();
 			mOldTimeRight = mRaceTimer.getCurrentTime();
 		}
 		else
 		{
 			mChronometer = ((MillisecondChronometer)((RaceActivity) c).findViewById(R.id.raceview_chronometer));
 			mOldTimeLeft = "00:00:00";
 			mOldTimeRight = "00:00:00";
 			((RaceActivity) c).findViewById(R.id.raceview_time_updater).setVisibility(View.GONE);
 			((RaceActivity) c).findViewById(R.id.raceview_chronometer).setVisibility(View.VISIBLE);
 		}		
 		//TODO Start Race
 		//TODO Rennen beginnt
 		//Timer läuft
 		//Es muss bei jeder Fahrzeugerkennung ein Zähler hochgezählt werden
 		// Bei Ende processResults()		
 	}
 	
 	/**
 	 * This Method starts the Timer (Timermode) or the Counter (Roundmode).
 	 * The truth value of mRaceStarted is set to TRUE.
 	 */
 	public void go() {
 		mRaceStarted = true;
 		if(mMode == TIMER_MODE)
 		{
 			if(mRaceTimer != null)
 				mRaceTimer.start();
 		}else if(mMode == ROUND_MODE){
 			if(mChronometer != null)
 				mChronometer.start();
 		}else
 			Log.i("debug", "Fehler beim starten des Timers, da kein korrekter Modus vorhanden ist.");			
 	}
 	
 	/**
 	 * This Method determines if there is a correct movement for the given lane and logical value of recognized.
 	 * For specific lane a correct movement happens when recognized was TRUE before it gets FALSE.  
 	 * When correct movement happens calcStatistics() is called.
 	 * @param lane The lane to find correct movement.
 	 * @param recognized The truth value of recognized object on the frame.
 	 * @return TRUE if there is a correct movement found, FALSE if no correct Movement is found.
 	 */
 	public boolean isCorrectMovement(int lane, boolean recognized) {
 		
 		if(lane == LEFT_LANE)
 		{
 			boolean correctmovement = false;
 			if(recognized) 
 			{
 				mLeftMovement = true;					
 			}
 			else 
 			{				
 				if (mLeftMovement)
 				{
 					mLeftMovement = false;	
 					correctmovement = true;
 					Log.i("debug", "Linkes movement!");
 					if(getGameMode() == TIMER_MODE)
 						calcStatistics(lane, mMode, mRaceTimer.getCurrentTime());
 					else
 						calcStatistics(lane, mMode, mChronometer.getTimeElapsedString());
 					return correctmovement;
 				}
 				else
 				{
 					mLeftMovement = false;					
 				}
 			}
 			return correctmovement;
 		}
 		else
 		{
 			boolean correctmovement = false;
 			if(recognized) 
 			{
 				mRightMovement = true;			
 			}			
 			else
 			{
 				if(mRightMovement)
 				{
 					mRightMovement = false;		
 					correctmovement = true;
 					Log.i("debug", "Rechtes movement!");
 					if(getGameMode() == TIMER_MODE)
 						calcStatistics(lane, mMode, mRaceTimer.getCurrentTime());
 					else
 						calcStatistics(lane, mMode, mChronometer.getTimeElapsedString());
 					return correctmovement;
 				}
 				else
 				{
 					mRightMovement = false;					
 				}
 			}
 			return correctmovement;			
 		}
 	}	
 	
 	/**
 	 * This Method is used to returns wether the race has been started or not.
 	 * @return TRUE if race has been started, FALSE if race has not been started, has been canceled or stopped.
 	 */
 	public boolean hasRaceBeenStarted(){
 		return mRaceStarted;
 	}
 	
 	/**
 	 * This Method sets the actual Roundcounter + 1 for a specific lane.
 	 * @param lane The lane on which the round has to be added.
 	 */
 	public void countRounds(int lane) {
 		
 		if(lane == LEFT_LANE)
 			mActLeftRound++;
 		else
 			mActRightRound++;	
 	}
 
 	/**
 	 * This Method is used to check if the race is over and gets the lane which matches the condition. 
 	 * During Roundmode this is implemented by a comparison of the actual round on every lane with the estimated rounds for the race.
 	 * During Timermode this is realised by checking which lane got more rounds after isFinished() from MyTimer class returned True.
 	 * @return Return the lane which ends the race first, 0 if race is still running.
 	 */
 	public int isOver() {
 		if(mMode == ROUND_MODE)
 		{
 			if(mActLeftRound == mCount)
 				return LEFT_LANE;
 			if (mActRightRound == mCount)
 				return RIGHT_LANE;
 		}
 		else if(mMode == TIMER_MODE)			
 		{
 			
 			if(mRaceTimer.isFinished())
 			{
 				if(mActLeftRound > mActRightRound)
 					return LEFT_LANE;
 				else
 					return RIGHT_LANE;
 			}				
 		}
 		return 0;		
 	}
 	
 	/**
 	 * This Method return the actual round according to the given lane
 	 * @param lane The lane for which the round is needed.
 	 * @return The value of the actual round.
 	 */
 	public int getCurrentRound(int lane) {		
 		if(lane == LEFT_LANE)			
 			return mActLeftRound;		
 		else
 			return mActRightRound;
 	}
 	
 	/**
 	 * This Method gets the current speed according to the given lane. 
 	 * @param lane The lane for which the speed is needed.
 	 * @return The double value of the speed.
 	 */
 	public double getCurrentSpeed (int lane) {
 		if(lane == LEFT_LANE)
 			return mActLeftSpeed;		
 		else
 			return mActRightSpeed;
 	}
 	
 	/**
 	 * This Method gets the raced track name.
 	 * @return The track name.
 	 */
 	public String getTrackName() {
 		return mTrackName;
 	}
 	
 	/**
 	 * This Method gets the game mode of this race.
 	 * @return The game mode(ROUNDMODE|TIMERMODE).
 	 */
 	public int getGameMode() {
 		return mMode;
 	}
 	
 	/**
 	 * This Method returns the Number of Players that are driving in this race according to the car status.	
 	 * @return The number of players, 0 if there are no Player in the race.
 	 */
 	public int getNumberOfPlayers() {
 		switch(mCarStatus){
 		case ObjectDetector.NO_CAR:	
 			return 0;			
 		case ObjectDetector.RIGHT_CAR:			
 			return 1;			
 		case ObjectDetector.LEFT_CAR:							
 			return 1;			
 		case ObjectDetector.BOTH_CAR:
 			return 2;	
 		default:
 			return 0;
 				
 		}
 	}
 	
 	/**
 	 * This Method gets the number of Rounds (ROUNDMODE) respectively the number of minutes (TIMERMODE).
 	 * @return The number.
 	 */
 	public int getCount() {
 		return mCount;
 	}
 	
 	/**
 	 * This Method gets the attempt for the given player.
 	 * @param p The Player for which the attempt is needed.
 	 * @return The attempt, 0 if there is no Player found for this lane.
 	 */
 	public int getAttempt(int lane){
 		for(Player p : mPlayerArray){
 			if(p.getLane() == lane)
 				return p.getAttempt();		
 		}
 		return 0;
 	}
 	
 	/**
 	 * This Method gets the driven meters during this race for the lane.
 	 * @param lane The lane for which the driven meters is needed.
 	 * @return The driven meters.
 	 */
 	public double getDrivenMeters(int lane){
 		
 		return calcDrivenMeters(lane);		
 	}
 	
 	/**
 	 * This Method gets the fastest round for the given lane.
 	 * @param lane The lane for which the fastest round is needed.
 	 * @return The fastest round after the race.
 	 */
 	public String getFastestRound(int lane){
 		if (lane == LEFT_LANE)
 			return mOldBestTimeLeftArray[0]+":"+mOldBestTimeLeftArray[1]+":"+mOldBestTimeLeftArray[2];
 		else
 			return mOldBestTimeRightArray[0]+":"+mOldBestTimeRightArray[1]+":"+mOldBestTimeRightArray[2];
 
 	}
 	
 	/**
 	 * This Method gets the average speed during the race according to the given lane.
 	 * @param lane The lane for which the avg speed is needed.
 	 * @return The average speed.
 	 */
 	public double getAvgSpeed(int lane){				
 				return calcAvgSpeed(lane);
 
 	}
 	
 	/**
 	 * This Method sets the best Time during a race into a Pair of the given String and the lane.
 	 * @param s The time to set to the pair.
 	 * @param lane The lane to set to the pair.
 	 */
 	private void setBestTime(String s, int lane){
 		
 		mBestTimeOnLane = new Pair<String, Integer>(s,lane);		
 	}
 	
 	/**
 	 * This Method gets the best time measured as a pair of the time string and the according lane. 
 	 * @return The Pair of the time string and the lane.
 	 */
 	public Pair<String,Integer> getBestTime(){	
 		
 		return mBestTimeOnLane;		
 	}
 	
 	/**
 	 * This Method is used to do store statistic stuff and calulations during the race.
 	 * It uses the given parameters lane, mode and time to set them to the accessed methods calcSpeedAndTime() and storeRoundTime().
 	 * @param lane The lane for which this method is needed.
 	 * @param mode The game mode.
 	 * @param time The current time, measured when a correct movement was recognized. 
 	 */
 	private void calcStatistics(int lane, int mode, String time){		
 		calcSpeedAndTime(lane, mode, time);
 		storeRoundTime(lane);		
 	}
 	
 	/**
 	 * This Method stores the measured actual round time according to the given lane into a List of Strings.
 	 * @param lane The lane for which the actual round time should be saved.
 	 */
 	private void storeRoundTime(int lane){
 		if(lane == LEFT_LANE)
 			mLeftTimes.add(mActLeftRoundTime);
 		else
 			mRightTimes.add(mActRightRoundTime);	
 	}
 	
 	/**
 	 * This Method calculates speed and round times based on the given time string. The time string is parsed to an integer array to work with it internally. 
 	 * Every actual given time is stored after time measuring to make continuos calculation possible. 
 	 * On every call of this function the member variables for actual round time and actual round speed is refreshed.
 	 * @param lane The lane for which the calculation is needed.
 	 * @param mode The game mode.
 	 * @param time The time string with the actual time.
 	 */
 	private void calcSpeedAndTime(int lane, int mode, String time) {		
 		
 		int[] _new = parseTimeString(time);
 		int[] curr= new int[3];
 		double _curr;
 		if(lane == LEFT_LANE)
 		{
 			int[] old = parseTimeString(mOldTimeLeft);
 			
 			if(mode == TIMER_MODE)
 			{				
 				curr[0] = old[0] - _new[0];
 				curr[1] = old[1] - _new[1];				
 				curr[2] = old[2] - _new[2];		
 			}
 			else
 			{			
 				curr[0] = _new[0] - old[0];
 				curr[1] = _new[1] - old[1];
 				curr[2] = _new[2] - old[2];				
 			}			
 			if(curr[1] <0){
 				curr[1] = curr[1] + 60;
 				curr[0] = curr[0] - 1;
 			}
 			if(curr[2] <0){
 				curr[2] = curr[2] + 100;
 				curr[1] = curr[1] - 1;
 			}
 			Log.i("debug", "Rundenzeit:"+curr[0]+":"+curr[1]+":"+curr[2]);
 			_curr = (curr[0]*60)+(curr[1])+ (curr[2] / 100.0); 
 			if(mOldBestTimeLeft > _curr){
 				mOldBestTimeLeft = _curr;
 				if(_curr < mOldBestTimeRight){
 					mOldBestTimeLeftArray = curr;
 					setBestTime(curr[0]+":"+curr[1]+":"+curr[2], lane);
 				}
 			}
 			Log.i("debug", "Rundenzeit Links: "+_curr);
 			mActLeftSpeed = mTrackLength/ _curr;
 			mActLeftRoundTime = (curr[0])+":"+(curr[1])+":"+(curr[2]); 			
 			mOldTimeLeft = time;
 			Log.i("debug","Alte Zeit links: "+mOldTimeLeft);
 		}
 		else
 		{
 			int[] old = parseTimeString(mOldTimeRight);
 			if(mode == TIMER_MODE)
 			{								
 				curr[0] = old[0] - _new[0];
 				curr[1] = old[1] - _new[1];
 				curr[2] = old[2] - _new[2];
 			}
 			else
 			{								
 				curr[0] = _new[0] - old[0];
 				curr[1] = _new[1] - old[1];
 				curr[2] = _new[2] - old[2];
 			}	
 			if(curr[1] <0){
 				curr[1] = curr[1] + 60;
 				curr[0] = curr[0] - 1;
 			}				
 			if(curr[2] <0){
 				curr[2] = curr[2] + 100;
 				curr[1] = curr[1] - 1;
 			}				
 			_curr = (curr[0]*60) + (curr[1]) + (curr[2] / 100.0); 
 			if(mOldBestTimeRight > _curr){
 				mOldBestTimeRight = _curr;
 				if(_curr < mOldBestTimeLeft){
 					mOldBestTimeRightArray = curr;
 					setBestTime(curr[0]+":"+curr[1]+":"+curr[2], lane);
 				}
 			}
 			Log.i("debug", "Rundenzeit Rechts: "+_curr);
 			mActRightSpeed = mTrackLength / _curr;
 			mActRightRoundTime = (curr[0])+":"+(curr[1])+":"+(curr[2]); 
 			mOldTimeRight = time;
 			Log.i("debug","Alte Zeit rechts: "+mOldTimeRight);
 		}		
 	}	
 	
 	private double calcDrivenMeters(int lane){
 		
 		double drivenMeters = 0;
 		if(lane == LEFT_LANE)
 			return drivenMeters = mActLeftRound * mTrackLength;
 		else
 			return drivenMeters = mActRightRound * mTrackLength;		
 	}
 	
 	private double calcAvgSpeed(int lane){
 		if(lane == LEFT_LANE){
 			if(mMode == ROUND_MODE)
 				return calcDrivenMeters(lane)/(mChronometer.getTimeElapsed()/1000);			
 			else
 				return calcDrivenMeters(lane)/(mCount*60);
 		}
 		else
 			if(mMode == ROUND_MODE)
 				return calcDrivenMeters(lane)/(mChronometer.getTimeElapsed()/1000);			
 			else
 				return calcDrivenMeters(lane)/(mCount*60);
 	}
 
 	/**
 	 * This Method parses a given String with format mm:ss:ms to an integer array with 3 values
 	 * @param time The time string to parse.
 	 * @return The integer array with minutes at [0], seconds at[1], millis at [2].
 	 */
 	private int[] parseTimeString(String time) {
 		int[] i = new int[3];
 		String[] split;
 		String s = time;
 		Log.i("debug","Time unparsed.."+s);
 		split = s.split(":");
 		i[0] = Integer.parseInt(split[0]);
 		i[1] = Integer.parseInt(split[1]);
 		i[2] = Integer.parseInt(split[2]);
 		return i;
 	}
 	
 	/**
 	 * This Method is used to put measured values into a database to use them after the race.
 	 */
 	public void processResults() {
 		Log.i("debug", "Processing Results.....");
 		
 		//Insert new Players into Database
 		switch(mCarStatus){
 		case ObjectDetector.NO_CAR:			
 			break;
 		case ObjectDetector.RIGHT_CAR:	
 			if(mDbHelper.isPlayerPresent(mPlayerArray.get(0).getName()))
 			{				
 				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(0).getName(), mTrackName, getGameMode()))
 				{				
 					mDbHelper.updatePlayerStats(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
 				}
 				else
 				{
 					mDbHelper.createPlayerTrack(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), 1, getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
 				}
 				break;
 			}
 			else				
 				break;
 		case ObjectDetector.LEFT_CAR:							
 			if(mDbHelper.isPlayerPresent(mPlayerArray.get(0).getName()))
 			{				
 				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(0).getName(), mTrackName, getGameMode()))
 				{				
 					mDbHelper.updatePlayerStats(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
 				}
 				else
 				{
 					mDbHelper.createPlayerTrack(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), 1, getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
 				}
 				break;
 			}
 			else				
 				break;
 		case ObjectDetector.BOTH_CAR:
 			if(mDbHelper.isPlayerPresent(mPlayerArray.get(0).getName()))
 			{				
 				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(0).getName(), mTrackName, getGameMode()))
 				{				
 					mDbHelper.updatePlayerStats(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
 				}
 				else
 				{
 					mDbHelper.createPlayerTrack(mPlayerArray.get(0).getName(), mTrackName, getGameMode(), 1, getFastestRound(LEFT_LANE), (float) getAvgSpeed(LEFT_LANE), (float) getDrivenMeters(LEFT_LANE));
 				}
 			}
 			if(mDbHelper.isPlayerPresent(mPlayerArray.get(1).getName()))
 			{				
 				if(mDbHelper.isPlayerTrackPresent(mPlayerArray.get(1).getName(), mTrackName, getGameMode()))
 				{				
 					mDbHelper.updatePlayerStats(mPlayerArray.get(1).getName(), mTrackName, getGameMode(), getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
 				}
 				else
 				{
 					mDbHelper.createPlayerTrack(mPlayerArray.get(1).getName(), mTrackName, getGameMode(), 1, getFastestRound(RIGHT_LANE), (float) getAvgSpeed(RIGHT_LANE), (float) getDrivenMeters(RIGHT_LANE));
 				}
 			}
 			break;	
 		default:
 			break;		
 		}
 		
 		Log.i("debug", "Rundenzeiten links: "+mLeftTimes);
 		Log.i("debug","Rundenzeiten rechts: "+mRightTimes);		
 		//Gibt das Ergebnis an Results-Klasse weiter.
 	}
 	
 	/**
 	 * This Method is used to stop the race timers, when the race is cancelled manually. 
 	 * The truth value of mRaceStarted is set to FALSE.
 	 */
 	public void cancel() {
 		mRaceStarted = false;
 		if(mMode == TIMER_MODE){
 			mRaceTimer.stop();
 			//mTimer.setText(mRaceTimer.getCurrentTime());
 			Log.i("debug", "Race cancelled! at:"+mRaceTimer.getCurrentTime());
 			
 		}
 		else
 		{
 			mChronometer.stop();
 			//mChronometer.setText(Long.toString(mChronometer.getTimeElapsed()));
 			Log.i("debug", "Race cancelled at:"+mChronometer.getTimeElapsed());
 			
 		}	
 	}
 	
 	/**
 	 * This Method is used, when the race is stopped properly. 
 	 * The Method processResults() is called.
 	 * The truth value of mRaceStarted is set to FALSE.
 	 */
 	public void stop(){		
 		if(mMode == TIMER_MODE){
 			for(Player p :mPlayerArray){
 				p.incAttempt();
 			}
			if(mRaceStarted)
				processResults();
 			mRaceStarted = false;
 		}
 		else
 		{
 			mChronometer.stop();
 			for(Player p :mPlayerArray){
 				p.incAttempt();
 			}
			if(mRaceStarted)
				processResults();	
 			mRaceStarted = false;
 		}	
 		
 	}
 	/**
 	 * This Method gets the players color as a Scalar according to the given lane. The value is get by using the method of the Player class.
 	 * @param lane The lane for which the color Scalar is needed.
 	 * @return null when mPlayerArray is empty for this lane, else the color of the player according to the lane.
 	 */
 	public Scalar getPlayerColor(int lane){
 		
 		for(Player p : mPlayerArray) {
 			if(p.getLane() == lane)
 				return p.getColor();
 		}		
 		return null;
 	}
 	
 	/**
 	 * This Method gets the players color as an integer-representative according to the given lane. The value is get by using the method of the Player class.
 	 * @param lane The lane for which the integer-representative is needed.
 	 * @return 0 when mPlayerArray is empty for this lane, else the colors integer-representative of the player according to the lane.
 	 */
 	public int getPlayerRGBColor(int lane){
 		
 		for(Player p : mPlayerArray) {
 			if(p.getLane() == lane)
 				return p.getRGBColor();
 		}		
 		return 0;
 	}
 	
 	/**
 	 * This Method gets the player name for the given index in the player array.
 	 * @param index The index of the needed player.
 	 * @returns null if PlayerArray is smaller that the index, this getter is Zero-Based, counting begins from zero (0,1..), first position gives the left Player, second position the right Player
 	 */
 	public String getPlayerName(int index){
 		if(mPlayerArray.size() > index)
 			return mPlayerArray.get(index).getName();
 		return null;
 	}
 	
 	/**
 	 * This Method gets a String that represents the Time when race is finished. When in a timer mode race, this String is set to "00:00:00", in round mode this String is set to the elapsed time on the Chronometer
 	 * @return
 	 */
 	public String getFinishedTime() {
 		String finishedTime;
 		if(mMode == TIMER_MODE)
 			finishedTime = "00:00:00";
 		else
 			finishedTime = mChronometer.getTimeElapsedString();		
 		return finishedTime;
 	}
 	
 	
 //	public void prepareRace(boolean twoplayer, int mode){
 //		
 //		if(twoplayer){
 //			new Player(LEFT_LANE, mode, Color_Car1);
 //			new Player(RIGHT_LANE, mode, Color_Car2);
 //		}else{
 //			new Player(LEFT_LANE, mode, Color_Car1);
 //		}
 //		//Hier wird angegeben wie viele Spieler das Rennen hat,außerdem welcher Spielmodus gewählt wurde.
 //		//
 //				
 //	}
 
 }
