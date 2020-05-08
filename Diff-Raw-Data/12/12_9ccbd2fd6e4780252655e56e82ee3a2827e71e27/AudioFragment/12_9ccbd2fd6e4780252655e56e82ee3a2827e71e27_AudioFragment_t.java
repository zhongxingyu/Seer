 package mobi.cyann.deviltools;
 
 import mobi.cyann.deviltools.PreferenceListFragment.OnPreferenceAttachedListener;
 import mobi.cyann.deviltools.EqTuningPreference;
 import mobi.cyann.deviltools.preference.IntegerPreference;
 import mobi.cyann.deviltools.preference.StatusPreference;
 import mobi.cyann.deviltools.SysCommand;
 import android.app.ActivityManager;
 import android.app.ActivityManager.MemoryInfo;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.ListPreference;
 import android.preference.Preference.OnPreferenceChangeListener;
 import android.preference.PreferenceCategory;
 import android.preference.PreferenceManager;
 import android.preference.PreferenceScreen;
 import android.view.View;
 import android.widget.TextView;
 
 
 public class AudioFragment extends BasePreferenceFragment implements OnPreferenceChangeListener {
 	//private final static String LOG_TAG = "DevilTools.AudioActivity";
 	
 	public AudioFragment() {
 		super(R.layout.audio);
 	}
 
         private ContentResolver mContentResolver;
 	private static SharedPreferences preferences;
 
     	private StatusPreference mHeadphoneEq;
     	private EqTuningPreference mEq;
 
 	@Override
     	public void onCreate(Bundle savedInstanceState) {
         	super.onCreate(savedInstanceState);
 
 	preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
 
         PreferenceScreen prefSet = getPreferenceScreen();
         mContentResolver = getActivity().getApplicationContext().getContentResolver();
 
   	mHeadphoneEq = (StatusPreference) findPreference("key_headphone_eq");
	if (mHeadphoneEq != null)
 	mHeadphoneEq.setOnPreferenceChangeListener(this);
 	int eq_enabled = preferences.getInt("key_headphone_eq", 0);
 	boolean is_enabled = false;
 	if (eq_enabled == 1)
 	is_enabled = true;
 
   	mEq = (EqTuningPreference) findPreference("eq_tuning");
	if (mEq != null) {
	   if(EqTuningPreference.isSupported())
        	mEq.setEnabled(is_enabled);
	   else
        	mEq.setEnabled(false);
	}
 
     }
 	
     public boolean onPreferenceChange(Preference preference, Object objValue) {
 	int status;
 	boolean is_enabled;
 	if (preference == mHeadphoneEq) {
 	    status = (Integer) objValue;
 	    if(status == 1)
 	    	is_enabled = true;
 	    else
 	    	is_enabled = false;
 	    mEq.setEnabled(is_enabled);
             return true;
         }
 
         return false;
     }
 
 }
