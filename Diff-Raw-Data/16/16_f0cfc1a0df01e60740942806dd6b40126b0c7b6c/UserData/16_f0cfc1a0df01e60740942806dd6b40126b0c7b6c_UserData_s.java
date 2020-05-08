 package de.ovgu.swe_projekt.socialdoc;
 
 import android.content.SharedPreferences;
 import android.text.format.Time;
 
 /**
  * Created by Anne-Lena Simon on 09.06.13.
  */
 public class UserData {
 
     // the probandencode
     private String _probandenCode = "";
     // the times the alarm is supposed to happen at
     private int[] _userTimes = {-77,-77,-77,-77};
     // the exact date and time at last alarm, needed for creating the appropriate question
     private String _lastAlarm = "xx.xx.xxxx;77:77";
     // the time the last alarm happened at, needed for writing to csv
     String _timeAtLastAlarm = "77:77";
     // the time the last answer was given at, needed for creating the appropriate question
     String _dateAndTimeAtLastAnswer = "xx.xx.xxxx;77:77";
     // was the last question answered?
     private boolean _lastWasAnswered = false;
 
     public UserData() {
     }
 
     // import the user data from the settings
     public void importSavedUserData( SharedPreferences pref) {
         _probandenCode = pref.getString("Probandencode", "");
         _userTimes[0] = pref.getInt("time1", -77);
         _userTimes[1] = pref.getInt("time2", -77);
         _userTimes[2] = pref.getInt("time3", -77);
         _userTimes[3] = pref.getInt("time4", -77);
         _lastAlarm = pref.getString("lastAlarm", "xx.xx.xxxx;77:77");
         _timeAtLastAlarm =  pref.getString("timeAtLastAlarm", "77:77");
         _dateAndTimeAtLastAnswer =  pref.getString("dateAndTimeAtLastAnswer", "xx.xx.xxxx;77:77");
     }
 
     // set user data to default values (for new user)
     public void setUserDataToDefault() {
         _probandenCode = "";
         _userTimes[0] = -77;
         _userTimes[1] = -77;
         _userTimes[2] = -77;
         _userTimes[3] = -77;
         _lastAlarm = "xx.xx.xxxx;77:77";
         _timeAtLastAlarm = "77:77";
         _dateAndTimeAtLastAnswer = "xx.xx.xxxx;77:77";
         _lastWasAnswered = true;
     }
 
     // save the user data
     public void saveUserData( SharedPreferences pref) {
         SharedPreferences.Editor preferencesEditor = pref.edit();
         preferencesEditor.putString("Probandencode", _probandenCode);
         preferencesEditor.putString("lastAlarm", _lastAlarm);
         preferencesEditor.putString("timeAtLastAlarm", _timeAtLastAlarm);
         preferencesEditor.putString("dateAndTimeAtLastAnswer", _dateAndTimeAtLastAnswer);
         preferencesEditor.putInt("time1", _userTimes[0]);
         preferencesEditor.putInt("time2", _userTimes[1]);
         preferencesEditor.putInt("time3", _userTimes[2]);
         preferencesEditor.putInt("time4", _userTimes[3]);
         preferencesEditor.commit();
     }
 
     public String getProbandenCode() {
         return _probandenCode;
     }
     public void setProbandenCode(String probandenCode) {
         _probandenCode = probandenCode;
     }
 
     public int[] getUserTimes() {
         return _userTimes;
     }
     public void setUserTimes(int[] userTimes) {
         _userTimes = userTimes;
         // todo: set alarm to trigger at first possible user time
     }
 
     public String getLastAlarm() {
         return _lastAlarm;
     }
     public void setLastAlarm(Time lastAlarm) {
         _lastAlarm = lastAlarm.format("%d.%m.%Y;%H")+":00";
         _timeAtLastAlarm = lastAlarm.format("%H")+":00";
     }
 
     public String getTimeAtLastAlarm() {
         return _timeAtLastAlarm;
     }
     public String getTimeAtLastAnswer() {
         return _dateAndTimeAtLastAnswer;
     }
 
     public boolean wasLastQuestionAnswered() {
         return _lastWasAnswered;
     }
     public void setLastQuestionWasAnswered(boolean answered){
         _lastWasAnswered = answered;
     }
 }
