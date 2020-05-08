 /**
  * DevilPreference.java
  * Jan 14, 2012 9:36:34 AM
  */
 package mobi.cyann.deviltools;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import mobi.cyann.deviltools.PreferenceListFragment.OnPreferenceAttachedListener;
 import mobi.cyann.deviltools.preference.IntegerPreference;
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
 
 /**
  * @DerTeufel1980
  *
  */
 public class MemoryFragment extends BasePreferenceFragment implements OnPreferenceChangeListener {
 	public MemoryFragment() {
 		super(R.layout.memory);
 	}
 	
 	private ListPreference mBigmem;
 	private ListPreference mZram;
 
         private ContentResolver mContentResolver;
 	private static SharedPreferences preferences;
 
     	private static final String BIGMEM_FILE_PATH = "/sys/kernel/bigmem/enable";
     	private static final String[] ZRAM_FILE_SIZE_PATH = new String[] {
     	"/sys/block/zram0/disksize",
     	"/sys/block/zram1/disksize",
     	"/sys/block/zram2/disksize",
     	"/sys/block/zram3/disksize",
 	};
 
     	private static final String[] ZRAM_FILE_RESET_PATH = new String[] {
     	"/sys/block/zram0/reset",
     	"/sys/block/zram1/reset",
     	"/sys/block/zram2/reset",
     	"/sys/block/zram3/reset",
 	};
 
     	private static final String[] ZRAM_FILE_PATH = new String[] {
     	"/dev/block/zram0",
     	"/dev/block/zram1",
     	"/dev/block/zram2",
     	"/dev/block/zram3",
 	};
 
 	@Override
     	public void onCreate(Bundle savedInstanceState) {
         	super.onCreate(savedInstanceState);
 
 	preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
 
         PreferenceScreen prefSet = getPreferenceScreen();
         mContentResolver = getActivity().getApplicationContext().getContentResolver();
 	SysCommand sc = SysCommand.getInstance();
 
 	// get current available ram
         MemoryInfo mi = new MemoryInfo();
         ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
         activityManager.getMemoryInfo(mi);
         long totalMegs = mi.totalMem / 1048576L;
 
         mBigmem = (ListPreference) findPreference("key_bigmem");
 	if (!Utils.fileExists(BIGMEM_FILE_PATH)) {
             PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("bigmem_category");
             category.removePreference(mBigmem);
             getPreferenceScreen().removePreference(category);
 	} else {
         mBigmem.setOnPreferenceChangeListener(this);
         mBigmem.setSummary(String.valueOf("Current Ram: " + totalMegs) + " MB");
 	}
 
         mZram = (ListPreference) findPreference("key_zram");
 	int num_devices = zram_num_devices();
 	if (num_devices < 1) {
             PreferenceCategory category = (PreferenceCategory) getPreferenceScreen().findPreference("zram_category");
             category.removePreference(mZram);
             getPreferenceScreen().removePreference(category);
 	} else {
         mZram.setOnPreferenceChangeListener(this);
 	String zramPercent = preferences.getString("key_zram", "0");
        mZram.setSummary(zramPercent + "%% of total availbale Ram are used for compressed Memory" + "\n" + "This is equivalent to " + String.valueOf(totalZramSize(zramPercent)) + " MB");
 
 	String command = zramCommand(zramPercent);
 	sc.suRun(command);
 	}
     }
 	
     public boolean onPreferenceChange(Preference preference, Object objValue) {
 	int index;
 	SysCommand sc = SysCommand.getInstance();
 	if (preference == mBigmem) {
             index = mBigmem.findIndexOfValue((String) objValue);
             mBigmem.setSummary("After Reboot: " + mBigmem.getEntries()[index]);
 	    sc.writeSysfs("/sys/kernel/bigmem/enable", ((String) objValue));
 	    sc.writeSysfs("/data/local/devil/bigmem", ((String) objValue));
 	    setPreferenceString(getString(R.string.key_bigmem), ((String) objValue));
             return true;
         } else if (preference == mZram) {
             String zramPercent = ((String) objValue);
             index = mZram.findIndexOfValue((String) objValue);
            mZram.setSummary(zramPercent + "%% of total availbale Ram are used for compressed Memory" + "\n" + "This is equivalent to " + String.valueOf(totalZramSize(zramPercent)) + " MB");
 	    setPreferenceString(getString(R.string.key_zram), zramPercent);
 	    String command = zramCommand(zramPercent);
 	    sc.suRun(command);
             return true;
         }
         return false;
     }
 
     public static void setPreferenceString(String key, String value) {
 	Editor ed = preferences.edit();
 	ed.putString(key, value);
 	ed.commit();
     }
 
     public static void setPreferenceInteger(String key, int value) {
 	Editor ed = preferences.edit();
 	ed.putInt(key, value);
 	ed.commit();
     }
 
     private static int zram_num_devices() {
         int count = 0;
         for (String filePath : ZRAM_FILE_SIZE_PATH) {
             if (Utils.fileExists(filePath)) {
                 count++;
             }
         }
 	return count;
     }
 
    private long totalZramSize(String percent) {
	String zramPercent = percent;
        MemoryInfo mi = new MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
	long totalzramSize = (mi.totalMem / 100) * Integer.parseInt(zramPercent) / 1048576L;
	return totalzramSize;
    }


     private String zramCommand(String percent) {
 	int num_devices = zram_num_devices();
 	String zramPercent = percent;
         MemoryInfo mi = new MemoryInfo();
         ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
         activityManager.getMemoryInfo(mi);
 	long zramSize = (mi.totalMem / num_devices / 100) * Integer.parseInt(zramPercent);
 	StringBuilder command = new StringBuilder();
         for (int i = 0; i < num_devices; i++) {
 		command.append("swapoff " + ZRAM_FILE_PATH[i] + "\n");
 		command.append("echo " + 1 + " > " + ZRAM_FILE_RESET_PATH[i] + "\n");
 		command.append("echo " + String.valueOf(zramSize) + " > " + ZRAM_FILE_SIZE_PATH[i] + "\n");
 		command.append("mkswap " + ZRAM_FILE_PATH[i] + "\n");
 		command.append("swapon " + ZRAM_FILE_PATH[i] + "\n");
         }
 	return command.toString();
     }
 }
