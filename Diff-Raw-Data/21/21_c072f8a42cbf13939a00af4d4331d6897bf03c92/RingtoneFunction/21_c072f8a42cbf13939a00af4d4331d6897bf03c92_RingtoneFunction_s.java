 package teamwork.goodVibrations.functions;
 
 import teamwork.goodVibrations.Constants;
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.RingtoneManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class RingtoneFunction extends Function
 {
   private static String TAG = "RingtoneFunction";
 
   private Uri mUri;
   private boolean vibrate;
   private AudioManager AM;
   private byte toneTypes;
   Context mC;
 
   public RingtoneFunction(Context c, Bundle b, int newID)
   {
     Log.d(TAG, "RingtoneFunction() Constructor");
     mC = c;
     AM = (AudioManager) mC.getSystemService(Context.AUDIO_SERVICE);
     mUri = b.getParcelable(Constants.INTENT_KEY_URI);
     vibrate = b.getBoolean(Constants.INTENT_KEY_VIBRATE);
     name = b.getString(Constants.INTENT_KEY_NAME);
     toneTypes = b.getByte(Constants.INTENT_KEY_TONE_TYPES);
     id = newID;
     type = Function.FunctionType.RINGTONE;
   }
   
   public RingtoneFunction(Context c, String s)
   {
     // TODO Redo string parsing
     mC = c;
     type = Function.FunctionType.RINGTONE;
     String [] categories = s.split(Constants.CATEGORY_DELIM);
     name = categories[0];
     id = new Integer(categories[1]).intValue();
     mUri = Uri.parse(categories[2]);
     vibrate = new Boolean(categories[3]).booleanValue();

   }
 
   // this is used to regenerate your object. All Parcelables must have a CREATOR
   // that implements these two methods
   public static final Parcelable.Creator<RingtoneFunction> CREATOR = new Parcelable.Creator<RingtoneFunction>()
   {
     public RingtoneFunction createFromParcel(Parcel in)
     {
       return new RingtoneFunction(in);
     }
 
     public RingtoneFunction[] newArray(int size)
     {
       return new RingtoneFunction[size];
     }
   };
 
   private RingtoneFunction(Parcel in)
   {
     name = in.readString();
     id = in.readInt();
   }
 
   public void execute()
   {
 
     Log.d(TAG, "execute() - Setting Ringtone to " + mUri);
 
     // Toast.makeText(mC, "executing()", Toast.LENGTH_LONG).show();
 
     try
     {
       // RingtoneManager.getRingtone(mC, mUri).play();
       // Log.d(TAG,"Ringtone playing");
       if((toneTypes & (byte)1) != 0)
       {
         RingtoneManager.setActualDefaultRingtoneUri(mC, RingtoneManager.TYPE_RINGTONE, mUri);
       }
       if((toneTypes & (byte)2) != 0)
       {
         RingtoneManager.setActualDefaultRingtoneUri(mC, RingtoneManager.TYPE_ALARM, mUri);
       }
       if((toneTypes & (byte)4) != 0)
       {
         RingtoneManager.setActualDefaultRingtoneUri(mC, RingtoneManager.TYPE_NOTIFICATION, mUri);
       }
     }
     catch(Exception e)
     {
       Log.d(TAG, "Error executing set ringtone");
       // error handling goes here -- also, use something other than Throwable
     }
 
     if(vibrate)
     {
       AM.setVibrateSetting(AudioManager.STREAM_RING, AudioManager.VIBRATE_SETTING_ON);
     }
     else
     {
       AM.setVibrateSetting(AudioManager.STREAM_RING, AudioManager.VIBRATE_SETTING_OFF);
     }
 
     /*
      * try { RingtoneManager.setActualDefaultRingtoneUri(c,
      * RingtoneManager.TYPE_NOTIFICATION, nuri); } catch (Throwable t) { //
      * error handling goes here -- also, use something other than Throwable }
      */
 
     /*
      * AudioManager AM =
      * (AudioManager)c.getSystemService(Context.AUDIO_SERVICE);
      * AM.setRingerMode(AudioManager.RINGER_MODE_NORMAL); for (int volumeType:
      * (new int[] {
      * AudioManager.STREAM_SYSTEM,AudioManager.STREAM_RING,AudioManager
      * .STREAM_NOTIFICATION, AudioManager.STREAM_ALARM } )) { //int maxVolume =
      * AM.getStreamMaxVolume(volumeType); int vol =
      * AM.getStreamVolume(AudioManager.STREAM_RING);
      * AM.setStreamVolume(volumeType, vol, AudioManager.FLAG_PLAY_SOUND |
      * AudioManager.FLAG_VIBRATE); AM.setStreamMute(volumeType, false);
      * AM.setVibrateSetting(volumeType,AudioManager.VIBRATE_SETTING_ON); }
      */
   }
 
   @Override
   public String getInternalSaveString()
   {
     String saveString = new String();
     saveString = name + Constants.CATEGORY_DELIM;
     saveString += id  + Constants.CATEGORY_DELIM;
     saveString += mUri.toString();
     saveString += Constants.CATEGORY_DELIM;    
    saveString += new Boolean(vibrate).toString(); 
     
     return saveString;
   }
 
 }
