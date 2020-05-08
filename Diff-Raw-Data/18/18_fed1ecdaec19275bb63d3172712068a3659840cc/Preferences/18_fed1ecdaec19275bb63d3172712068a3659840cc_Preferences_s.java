 package com.normalexception.forum.rx8club.preferences;
 
 /************************************************************************
  * NormalException.net Software, and other contributors
  * http://www.normalexception.net
  * 
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
  * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
  * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
  * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  ************************************************************************/
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.Preference;
 import android.preference.PreferenceActivity;
 
 import com.normalexception.forum.rx8club.R;
 import com.normalexception.forum.rx8club.utils.PreferenceHelper;
 
 /**
  * Class used to set and save preferences
  */
 public class Preferences extends PreferenceActivity {
 	
 	/*
 	 * (non-Javadoc)
 	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
 	 */
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         getPreferenceManager().setSharedPreferencesName(
                 PreferenceHelper.PREFS_NAME);
        addPreferencesFromResource(R.xml.preferences); 
         
         Preference button = (Preference)findPreference("donate");
         button.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
             @Override
             public boolean onPreferenceClick(Preference arg0) { 
             	String donateUrl = "https://www.paypal.com/cgi-bin/webscr" +
             			"?cmd=_donations&business=XSEK8GC74RMMS&lc=US" +
             			"&currency_code=USD&bn=PP%2dDonations" +
             			"BF%3abtn_donateCC_LG%2egif%3aNonHosted";
             	Uri uri = Uri.parse(donateUrl);
             	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
             	startActivity(intent);
                 return true;
             }
         });
         
         try {
         	Preference version = (Preference)findPreference("version");
         	version.setSummary(
         			getPackageManager().getPackageInfo(
         					this.getPackageName(), 0).versionName);
         } catch (Exception e) {}
     }
 }
