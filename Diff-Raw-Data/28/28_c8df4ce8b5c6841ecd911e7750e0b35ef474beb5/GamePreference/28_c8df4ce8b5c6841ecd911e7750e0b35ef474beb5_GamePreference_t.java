 package tw.h4.game.escape;
 
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 
 public class GamePreference {
 	private static final String PREFIX = "tw.h4.game.escape.";
 	private static final String PREF_NAME = "tw.h4.game.escape_preferences";
 	private static final String EVENT_TIME = PREFIX + "EVENT_TIME";
 	private static final String EVENT_TYPE = PREFIX + "EVENT_TYPE";
 	private static final String PREV_RECORD_TIME = PREFIX + "PREV_RECORD_TIME";
 	private static final String PREV_ELAPSED_TIME = PREFIX
 	        + "PREV_ELAPSED_TIME";
 	private static final String TIME_SPEED = PREFIX + "TIME_SPEED";
 
 	private SharedPreferences pref;
 	private static GamePreference mInstance = null;
 
 	private GamePreference(Context context) {
 		pref = context.getApplicationContext().getSharedPreferences(PREF_NAME,
 		        Context.MODE_PRIVATE);
 	}
 
 	public static GamePreference getInstance(Context context) {
 		if (null == mInstance) {
 			synchronized (GamePreference.class) {
 				mInstance = new GamePreference(context);
 			}
 		}
 		return mInstance;
 	}
 
 	public void setEventTime(long time) {
 		Editor editor = pref.edit();
 		editor.putLong(EVENT_TIME, time);
 		editor.commit();
 	}
 
 	public long getEventTime() {
 		return pref.getLong(EVENT_TIME, -1);
 	}
 
 	public void setPrevRecordTime(long time) {
 		Editor editor = pref.edit();
 		editor.putLong(PREV_RECORD_TIME, time);
 		editor.commit();
		Debugger.d("GamePreference", "record time::" + time);
 	}
 
 	public long getPrevRecordTime() {
 		return pref.getLong(PREV_RECORD_TIME, -1);
 	}
 
 	public void setPrevElapsedTime(long time) {
 		Editor editor = pref.edit();
 		editor.putLong(PREV_ELAPSED_TIME, time);
 		editor.commit();
        Debugger.d("GamePreference", "elapsed time::" + time);
 	}
 
 	public long getPrevElapsedTime() {
 		return pref.getLong(PREV_ELAPSED_TIME, -1);
 	}
 
 	public void setTimeSpeed(int speed) {
 		Editor editor = pref.edit();
 		editor.putInt(TIME_SPEED, speed);
 		editor.commit();
 	}
 
 	public int getTimeSpeed() {
 		return pref.getInt(TIME_SPEED, 1);
 	}
 
 	public void setEventType(int type) {
 		Editor editor = pref.edit();
 		editor.putInt(EVENT_TYPE, type);
 		editor.commit();
 	}
 
 	public int getEventType() {
 		return pref.getInt(EVENT_TYPE, 0);
 	}
 
 	public void removeEvent() {
 		Editor editor = pref.edit();
 		editor.remove(EVENT_TIME);
 		editor.remove(EVENT_TYPE);
 		editor.commit();
 	}
 }
