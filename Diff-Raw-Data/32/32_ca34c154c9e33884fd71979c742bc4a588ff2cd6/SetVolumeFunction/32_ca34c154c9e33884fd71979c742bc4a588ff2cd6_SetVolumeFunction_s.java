 package teamwork.goodVibrations.functions;
 
 import teamwork.goodVibrations.Constants;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 
public class SetVolumeFunction implements Function {
 
   private static String TAG = "SetVolumeFunction";
  
	private AudioManager AM;
	public String name;
	private int volume;
	private boolean vibrate;
 
	public SetVolumeFunction(AudioManager tAM, String tName, int tVolume, boolean tVibrate)
   {
     AM = tAM;
     name = tName;
     volume = tVolume;
     vibrate = tVibrate;
   }
 
   public SetVolumeFunction(AudioManager tAM, Bundle b)
   {
     volume = b.getInt(Constants.INTENT_KEY_VOLUME);
     vibrate = b.getBoolean(Constants.INTENT_KEY_VIBRATE);
     AM = tAM;
   }
	
	public void execute()
	{
		AM.setStreamVolume(AudioManager.STREAM_RING, volume, AudioManager.FLAG_SHOW_UI);
		// TODO Handle Vibrate setting
		Log.d(TAG,"execute() - Setting to " + volume);
	}
 
 }
