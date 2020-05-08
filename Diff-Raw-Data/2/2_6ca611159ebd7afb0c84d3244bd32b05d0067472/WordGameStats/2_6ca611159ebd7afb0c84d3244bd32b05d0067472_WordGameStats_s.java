 package com.ifihada.anagramic;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.widget.TextView;
 
 class Stats implements java.io.Serializable
 {
 	private static final long serialVersionUID = 1906901039310054820L;
 	
 	public int gamesStarted;
 	public int gamesWon;
 	public int gamesAbandoned;
 	public int wordsFound;
 	public int wordsInvalid;
 	public long playTime;
 	
 	public int gamesByMode[];
 	public int gamesByDiff[];
 	
 	public int winsByMode[];
 	public int winsByDiff[];
 	
 	private static Stats instance;
 	private long _startTime;
 	
 	Stats()
 	{
 		this.gamesByMode = new int[WordGame.MODE_MAX];
 		this.gamesByDiff = new int[WordGame.DIFF_MAX];
 		this.winsByMode = new int[WordGame.MODE_MAX];
 		this.winsByDiff = new int[WordGame.DIFF_MAX];
 	}
 	
 	public static void reset()
 	{
 		Stats.instance = new Stats();
 	}
 	
 	public static Stats getInstance()
 	{
 		if (Stats.instance == null)
 			Stats.instance = new Stats(); 
 		return Stats.instance;
 	}
 	
 	private static final String StatFileName = "statistics.bin";
 	
 	public static void save(final Context ctx)
 	{
 		try {
 			// Just blindly assume everything will work!
 			final FileOutputStream fos = ctx.openFileOutput(Stats.StatFileName, Context.MODE_WORLD_READABLE);
 			final ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(Stats.getInstance());
 			oos.close();
 			fos.close();
 		} catch (final Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	public static void load(final Context ctx)
 	{
 		FileInputStream fis;
 		try {
 			fis = ctx.openFileInput(Stats.StatFileName);
 			final ObjectInputStream ois = new ObjectInputStream(fis);
 			final Stats s = (Stats) ois.readObject();
 			ois.close();
 			fis.close();
 			Stats.instance = s;
 		} catch (final Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	// --- collection calls
 	
 	public static void gameStart(final int mode, final int difficulty)
 	{
 		final Stats s = Stats.getInstance();
 		s.gamesStarted++;
 		s.gamesByMode[mode]++;
 		s.gamesByDiff[difficulty]++;
 	}
 	
 	public static void gameWin(final int mode, final int difficulty)
 	{
 		final Stats s = Stats.getInstance();
 		s.gamesWon++;
 		s.winsByMode[mode]++;
		s.winsByDiff[mode]++;
 	}
 	
 	public static void timerStart()
 	{
 		Stats.getInstance()._startTime = Util.getTimeSecs();
 	}
 	
 	public static void timerPause()
 	{
 		final Stats s = Stats.getInstance();
 		s.playTime += Util.getTimeSecs() - s._startTime;
 	}
 	
 	public static void giveUp()
 	{
 		Stats.getInstance().gamesAbandoned++;
 	}
 	
 	public static void foundWord()
 	{
 		Stats.getInstance().wordsFound++;
 	}
 	
 	public static void invalidWord()
 	{
 		Stats.getInstance().wordsInvalid++;
 	}
 }
 
 public class WordGameStats extends Activity
 {
 	@SuppressWarnings("unused")
 	private static final String TAG = "WordGameStats";
 	
 	@Override
     public void onCreate(final Bundle savedInstanceState)
     {
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         super.onCreate(savedInstanceState);
         setContentView(R.layout.stats);
         Util.setBackground(this);
         
         this.update();
         
     }
 	
 	private void updateGameStats(final int[] targets, final int[] wins, final int[] games)
 	{
 		for (int i = 0; i < targets.length; i++)
 		{
 			this.setText(targets[i], wins[i], games[i]);
 		}
 	}
 	
 	private void update()
 	{
 		final Stats s = Stats.getInstance();
 		this.setText(
 				R.id.StatsGamesPlayed,
 				s.gamesWon,
 				s.gamesStarted
 				);
 		
 		this.updateGameStats(
 				new int[] {
 					R.id.StatsBeginnerGamesPlayed,
 					R.id.StatsEasyGamesPlayed,
 					R.id.StatsMediumGamesPlayed,
 					R.id.StatsHardGamesPlayed,
 					R.id.StatsInsaneGamesPlayed
 				},
 				s.winsByDiff,
 				s.gamesByDiff
 				);
 
 		this.updateGameStats(
 				new int[] {
 					R.id.StatsConundrumGamesPlayed,
 					R.id.StatsTimedGamesPlayed,
 					R.id.StatsFreeGamesPlayed
 				},
 				s.winsByMode,
 				s.gamesByMode
 				);
 		
 		this.setTextTime(R.id.StatsTimePlayed, s.playTime);
 		this.setText(R.id.StatsGamesGivenUp, s.gamesAbandoned);
 		this.setText(R.id.StatsWordsFound, s.wordsFound);
 		this.setText(R.id.StatsWrongWordsFound, s.wordsInvalid);
 		
 	}
 	
 	private void setTextTime(final int id, long time)
 	{
 		final long hrs = time / 3600;
 		time -= hrs * 3600;
 		final long mins = time / 60;
 		this.setText(id, String.format("%dh %dm %ds", hrs, mins, time % 60));
 	}
 	
 	private void setText(final int id, final int i)
 	{
 		this.setText(id, String.format("%d", i));
 	}
 	
 	private void setText(final int id, final int i, final int j)
 	{
 		this.setText(id, String.format("%d / %d", i, j));
 	}
 	
 	private void setText(final int id, final String str)
 	{
 		final TextView vv = (TextView) this.findViewById(id);
 		vv.setText(str);		
 	}
 
 	public void onFinish(final View v)
 	{
 		this.finish();
 	}
 	
 	public void onReset(final View v)
 	{
 		Stats.reset();
 		this.update();
 	}
 }
