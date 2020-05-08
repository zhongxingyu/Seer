 package teamwork.goodVibrations.functions;
 
 import teamwork.goodVibrations.Constants;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.util.Log;
 
 public class SetVolumeFunction extends Function
 {
 
   private static String TAG = "SetVolumeFunction";
 
   private AudioManager AM;
   private int volume;
   private boolean vibrate;
   private byte volumeTypes;
   
   public SetVolumeFunction(AudioManager tAM, Bundle b, int newID)
   {
     volume = b.getInt(Constants.INTENT_KEY_VOLUME);
     Log.d(TAG, "Volume: " + volume);
     vibrate = b.getBoolean(Constants.INTENT_KEY_VIBRATE);
     name = b.getString(Constants.INTENT_KEY_NAME);
     volumeTypes = b.getByte(Constants.INTENT_KEY_VOLUME_TYPES);
     AM = tAM;
     id = newID;
     type = Function.FunctionType.RING_VOLUME;
   }
   
   public SetVolumeFunction(AudioManager tAM, String s)
   {
     // TODO Redo string parsing
     AM = tAM;
     String[] categories = s.split(Constants.CATEGORY_DELIM);
     name = categories[0];
     id = new Integer(categories[1]).intValue();
     volume = new Integer(categories[2]).intValue();
     vibrate = new Boolean(categories[3]).booleanValue();
    volumeTypes = new Byte(categories[4]).byteValue();
     type = Function.FunctionType.RING_VOLUME;
   }
 
   @Override
   public void execute()
   {
     Log.d(TAG, "EXECUTING " + name);
     Log.d(TAG,"Volume " + volume);
     if(volume > 0)
     {
       AM.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
     }
     if(volume == 0 && vibrate)
     {
       AM.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
     }
     
     byte flags = AudioManager.FLAG_SHOW_UI | AudioManager.FLAG_ALLOW_RINGER_MODES | AudioManager.FLAG_VIBRATE;
     
     if((volumeTypes & (byte)1) != 0)
     {
       Log.d(TAG,"RING" + scaleVolume(AudioManager.STREAM_RING));
       AM.setStreamVolume(AudioManager.STREAM_RING, scaleVolume(AudioManager.STREAM_RING), flags);
     }
     if((volumeTypes & (byte)2) != 0)
     {
       Log.d(TAG,"MUSIC" + scaleVolume(AudioManager.STREAM_MUSIC));
       AM.setStreamVolume(AudioManager.STREAM_MUSIC, scaleVolume(AudioManager.STREAM_MUSIC), flags);
     }
     if((volumeTypes & (byte)4) != 0)
     {
       Log.d(TAG,"ALARM" + scaleVolume(AudioManager.STREAM_ALARM));
       AM.setStreamVolume(AudioManager.STREAM_ALARM,scaleVolume(AudioManager.STREAM_ALARM),flags);
     }
     if((volumeTypes & (byte)8) != 0)
     {
       Log.d(TAG,"NOTIFICATION" + scaleVolume(AudioManager.STREAM_NOTIFICATION));
       AM.setStreamVolume(AudioManager.STREAM_NOTIFICATION,scaleVolume(AudioManager.STREAM_NOTIFICATION),flags);
     }
 
     /*  
      * // TODO Implement a sifalent setting if(volume == 0 && silent) {
      * AM.setRingerMode(AudioManager.RINGER_MODE_SILENT); }
      */
     if(vibrate)
     {
       AM.setVibrateSetting(AudioManager.STREAM_RING, AudioManager.VIBRATE_SETTING_ON);
     }
     else
     {
       AM.setVibrateSetting(AudioManager.STREAM_RING, AudioManager.VIBRATE_SETTING_OFF);
     }
 
     Log.d(TAG, "execute() - Setting to " + volume);
   }
   
   private int scaleVolume(int stream)
   {
     float maxVol = (float) AM.getStreamMaxVolume(stream);
     return (int) Math.round(maxVol * (volume / 100.0));
   }
 
   @Override
   public String getInternalSaveString()
   {
     //name
     //id
     //volume
     //vibrate
    //volumeTypes
     
     String saveString = new String();
     saveString = name + Constants.CATEGORY_DELIM;
     saveString += id  + Constants.CATEGORY_DELIM;
     saveString += new Integer(volume).toString();
     saveString += Constants.CATEGORY_DELIM;
    saveString += new Boolean(vibrate).toString();
    saveString += Constants.CATEGORY_DELIM;
    saveString += new Byte(volumeTypes).toString();
     
     return saveString;
   }
 }
