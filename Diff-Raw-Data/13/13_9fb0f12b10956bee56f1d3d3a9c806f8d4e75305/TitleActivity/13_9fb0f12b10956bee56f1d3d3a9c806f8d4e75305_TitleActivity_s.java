 package net.honeybadgerlabs.adventurequest;
 
 import android.app.AlarmManager;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.SystemClock;
 import android.preference.PreferenceManager;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.view.ViewPager;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import java.lang.Math;
 import java.util.List;
 import java.util.Random;
 import java.util.Vector;
 
 public class TitleActivity extends FragmentActivity {
   private static final String TAG         = "TitleActivity";
   private static final int LEVEL_BASE     = 300000;
   private static final int ANIMATION_STEP = 25;
 
   public static final String ACTION_SUCCESS = "QuestSuccess";
   public static final String ACTION_FAILURE = "QuestFailure";
 
   private SharedPreferences settings;
   private Random rng = new Random();
   private CountDownTimer timer;
   private AlarmManager alarm;
   private PendingIntent successIntent;
   private PendingIntent failureIntent;
   private PagerAdapter adapter;
   private QuestAdapter archives;
   private StatAdapter attributes;
   private ProgressBar expBar;
 
   private int charLevel;
   private int charXP;
   private String questDescription;
   private long questEnd;
   private long questFailEnd;
   private int questXP;
   private int questStatus;
 
   public interface UpdateListener {
     public void onUpdate(String description, int status, long end);
   }
 
   private List<UpdateListener> listeners;
 
   private class LoadArchivesTask extends AsyncTask<Void, Void, Void> {
     protected Void doInBackground(Void... dummy) {
       archives.load();
       return null;
     }
 
     protected void onPreExecute() {
       archives.clear();
       archives.setNotifyOnChange(false);
     }
 
     protected void onPostExecute(Void dummy) {
       archives.setNotifyOnChange(true);
       archives.notifyDataSetChanged();
 
       Log.d(TAG, "Loaded " + archives.getCount() + " quests from archives");
     }
   }
 
   private class LoadProfileTask extends AsyncTask<Void, Void, Void> {
     protected Void doInBackground(Void... dummy) {
       attributes.load();
       return null;
     }
 
     protected void onPreExecute() {
       attributes.clear();
       attributes.setNotifyOnChange(false);
     }
 
     protected void onPostExecute(Void dummy) {
       attributes.setNotifyOnChange(true);
       attributes.notifyDataSetChanged();
     }
   }
 
   @Override public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.title);
 
     expBar = (ProgressBar) findViewById(R.id.stat_experience);
 
     settings = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
     alarm = (AlarmManager) getSystemService(ALARM_SERVICE);
     successIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_SUCCESS, null, this, CompleteReceiver.class), 0);
     failureIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_FAILURE, null, this, CompleteReceiver.class), 0);
 
     List<Fragment> fragments = new Vector<Fragment>();
     fragments.add(Fragment.instantiate(this, CurrentFragment.class.getName()));
     fragments.add(Fragment.instantiate(this, ProfileFragment.class.getName()));
     fragments.add(Fragment.instantiate(this, HistoryFragment.class.getName()));
 
     adapter = new PagerAdapter(this, getSupportFragmentManager(), fragments);
     ((ViewPager) findViewById(R.id.pager)).setAdapter(adapter);
 
     listeners = new Vector<UpdateListener>();
     archives = new QuestAdapter(this);
     attributes = new StatAdapter(this);
   }
 
   @Override public void onResume() {
     super.onResume();
 
     loadGame();
     updateDisplay(true);
     setNotify(false);
 
     if (questStatus == Quest.STATUS_PROGRESS) startTimer();
   }
 
   @Override public void onPause() {
     super.onPause();
 
     saveGame();
     if (timer != null) timer.cancel();
     setNotify(true);
   }
 
   public void setNotify(boolean notify) {
     SharedPreferences.Editor editor = settings.edit();
     editor.putBoolean("notify", notify);
     editor.commit();
 
     if (notify == false) {
       ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(CompleteReceiver.ID_NOTIFICATION);
     }
   }
 
   public void onAction(View v) {
     if (questStatus == Quest.STATUS_PROGRESS) {
       abandonQuest();
     } else {
       beginQuest();
     }
   }
 
   public void onSpendClick(View v) {
     if (charLevel > attributes.totalPoints()) {
       levelUp();
     }
   }
 
   public void addUpdateListener(UpdateListener listener) {
     listeners.add(listener);
     updateDisplay(false);
   }
 
   public void removeUpdateListener(UpdateListener listener) {
     listeners.remove(listener);
   }
 
   public void levelUp() {
     updatePointsButton();
 
     FragmentManager fm = getSupportFragmentManager();
     LevelUpDialog dialog = new LevelUpDialog(charLevel);
     dialog.show(fm, "fragment_level_up");
   }
 
   public void addStat(String name) {
     attributes.addStat(name);
     updatePointsButton();
   }
 
   public int getCharLevel() {
     return charLevel;
   }
 
   public QuestAdapter getArchiveAdapter() {
     return archives;
   }
 
   public StatAdapter getStatAdapter() {
     return attributes;
   }
 
   private void loadGame() {
     charLevel = settings.getInt("char_level", 1);
     charXP    = settings.getInt("char_xp", 0);
 
     questDescription = settings.getString("quest_desc", getString(R.string.welcome));
     questStatus      = settings.getInt("quest_status", Quest.STATUS_NONE);
     questEnd         = settings.getLong("quest_end", 0);
     questFailEnd     = settings.getLong("quest_fail_end", 0);
     questXP          = settings.getInt("quest_xp", 0);
 
     new LoadArchivesTask().execute();
     new LoadProfileTask().execute();
   }
 
   private void saveGame() {
     SharedPreferences.Editor editor = settings.edit();
 
     editor.putInt("char_level", charLevel);
     editor.putInt("char_xp", charXP);
     editor.putString("quest_desc", questDescription);
     editor.putInt("quest_status", questStatus);
     editor.putLong("quest_end", questEnd);
     editor.putLong("quest_fail_end", questFailEnd);
     editor.putInt("quest_xp", questXP);
 
     editor.commit();
 
     archives.save();
     attributes.save();
   }
 
   private void updatePointsButton() {
     ((ProfileFragment) adapter.getItem(1)).updateButton();
   }
 
   private void setText(int id, String text) {
     ((TextView) findViewById(id)).setText(text);
   }
 
   private void setText(int id, int text) {
     ((TextView) findViewById(id)).setText(text);
   }
 
   private void updateDisplay(boolean instantXP) {
     if (instantXP) {
       setText(R.id.stat_level, String.format(getString(R.string.stat_level), charLevel));
       expBar.setProgress(charXP);
     }
 
     for (UpdateListener listener : listeners) {
       listener.onUpdate(questDescription, questStatus, questEnd);
     }
   }
 
   private long timeToLevel(int currentLevel) {
     if (currentLevel > 60) {
       return timeToLevel(60) + 864000000 * (currentLevel - 60);
     } else {
       return LEVEL_BASE * (long) (Math.pow(1.16, currentLevel));
     }
   }
 
   private void startTimer() {
     long time = (questFailEnd > 0 ? questFailEnd : questEnd) - SystemClock.elapsedRealtime();
 
     if (time > 0) {
       timer = new CountDownTimer(time, 100) {
         public void onTick(long millisUntilFinished) {
           updateDisplay(false);
         }
         public void onFinish() {
           completeQuest();
         }
       }.start();
 
       // set alarm for notification
       if (questFailEnd > 0) {
         alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, questFailEnd, failureIntent);
       } else {
         alarm.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, questEnd, successIntent);
       }
     } else {
       completeQuest();
     }
   }
 
   private String randomStringFromArray(String name) {
     int id = getResources().getIdentifier(name, "array", getPackageName());
 
     if (id == 0) {
       return "{" + name + "}";
     } else {
       String[] array = getResources().getStringArray(id);
       return array[rng.nextInt(array.length)];
     }
   }
 
   private String generateQuestDescription() {
     String quest = randomStringFromArray("quest_base");
 
     while (quest.indexOf("[") > -1) {
       int start = quest.indexOf("[");
       int end   = quest.indexOf("]");
 
       String type = quest.substring(start + 1, end);
       String replace = randomStringFromArray("quest_" + type);
 
       quest = quest.substring(0, start) + replace + quest.substring(end + 1, quest.length());
     }
 
     return quest;
   }
 
   private void beginQuest() {
     double factor = rng.nextDouble() / 5.0 + 0.2;
     long time = (long) ((double) timeToLevel(charLevel) * factor);
     long start = SystemClock.elapsedRealtime();
 
     questDescription = generateQuestDescription();
     questEnd = start + time;
     questXP  = (int) (factor * 100);
 
     if (rng.nextFloat() < 0.10) {
       double early = rng.nextGaussian() * 0.25 + 0.60;
       if (early > 1.0) early = 0.99;
 
       questFailEnd = start + (long) ((double) time * early);
     } else {
       questFailEnd = 0;
     }
 
     questStatus = Quest.STATUS_PROGRESS;
     updateDisplay(false);
 
     startTimer();
   }
 
   private void completeQuest() {
     if (questEnd > 0) {
       if (questFailEnd == 0) {
         if (charXP + questXP >= 100) {
           int before = 100 - charXP;
           int after = questXP - before;
 
           charLevel++;
           charXP += questXP - 100;
 
           levelUp();
           animateLevelUp(before, after);
         } else {
           animateXPGain(questXP);
           charXP += questXP;
         }
 
         Log.d(TAG, "Quest complete!");
         questStatus = Quest.STATUS_COMPLETE;
       } else {
         Log.d(TAG, "Quest failed!");
         questStatus = Quest.STATUS_FAILED;
       }
     }
 
     archives.insert(new Quest(questDescription, questStatus), 0);
 
     questEnd = 0;
     questFailEnd = 0;
     updateDisplay(false);
   }
 
   private void abandonQuest() {
     int loss = questXP / 8;
     if (loss > charXP) loss = charXP;
 
     animateXPLoss(loss);
     charXP -= loss;
 
     questEnd = 0;
     questFailEnd = 0;
     questXP = 0;
     questStatus = Quest.STATUS_ABANDON;
     timer.cancel();
 
     archives.insert(new Quest(questDescription, questStatus), 0);
 
     updateDisplay(false);
   }
 
   private void animateXP(int count, final int diff) {
     new CountDownTimer(count * ANIMATION_STEP, ANIMATION_STEP) {
       public void onTick(long millisUntilFinished) {
         expBar.incrementProgressBy(diff);
       }
 
       public void onFinish() {
         expBar.setProgress(charXP);
       }
     }.start();
   }
 
   private void animateXPGain(int gain) {
     Log.d(TAG, "Gain " + gain + " XP");
     animateXP(gain, 1);
   }
 
   private void animateXPLoss(int loss) {
     animateXP(loss, -1);
   }
 
   private void animateLevelUp(int before, final int after) {
     new CountDownTimer(before * ANIMATION_STEP, ANIMATION_STEP) {
       public void onTick(long millisUntilFinished) {
         expBar.incrementProgressBy(1);
       }
 
       public void onFinish() {
         setText(R.id.stat_level, String.format(getString(R.string.stat_level), charLevel));
 
         expBar.setProgress(0);
         animateXPGain(after);
       }
     }.start();
   }
 }
