 /*
  * Copyright 2013 Venkat Malladi.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package me.malladi.dashcricket;
 
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.preference.PreferenceManager;
 
 import com.google.android.apps.dashclock.api.DashClockExtension;
 import com.google.android.apps.dashclock.api.ExtensionData;
 
 /**
  * A DashClock extension for fetching live cricket scores.
  */
 public class DashCricket extends DashClockExtension {
 	public static final String PREF_LIVE_SCORE_ID = "live_score_id";
 	public static final String PREF_LIVE_SCORE_SUMMARY = "live_score_summary";
 
 	@Override
 	protected void onInitialize(boolean isReconnect) {
 		super.onInitialize(isReconnect);
 		setUpdateWhenScreenOn(true);
 	}
 
 	@Override
 	protected void onUpdateData(int reason) {
 		Util.debug("onUpdateData(" + reason + ")");
 
 		// Check whether the network is available.
        NetworkInfo networkInfo = ((ConnectivityManager) getSystemService(
        		Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
    		Util.debug("not connected to the network");
            return;
        }
 
        // Fetch live scores from the network.
 		LiveScore[] liveScores = Util.getLiveScores();
 		if (liveScores == null) {
 			return;
 		} else if (liveScores.length == 0){
    		Util.debug("no matches in progress");
 			publishUpdate(new ExtensionData().visible(false));
 		}
 
 		// Publish the preferred live score.
 		SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
 		String liveScoreId = sharedPref.getString(PREF_LIVE_SCORE_ID, "NONE");
 		Util.debug(PREF_LIVE_SCORE_ID + " " + liveScoreId);
 		for (LiveScore liveScore : liveScores) {
 			if (liveScoreId.equals(liveScore.id)) {
 				publishUpdate(new ExtensionData()
 						.visible(true)
 						.icon(R.drawable.ic_launcher)
 						.status(liveScore.getStatus())
 						.expandedTitle(liveScore.getExpandedTitle())
 						.expandedBody(liveScore.getExpandedBody())
 						.clickIntent(new Intent(Intent.ACTION_VIEW,
 								Uri.parse(liveScore.liveScorecardLink))));
 				Util.debug("showing scores for match " + liveScore.id);
 				return;
 			}
 		}
 
 		// Allow the user to pick a live score.
 		publishUpdate(new ExtensionData()
 				.visible(true)
 				.icon(R.drawable.ic_launcher)
 				.status(getString(R.string.configure_status, liveScores.length))
 				.expandedTitle(getString(R.string.configure_title))
 				.expandedBody(getString(R.string.configure_body))
 				.clickIntent(new Intent().setClassName(
 						"me.malladi.dashcricket",
 						"me.malladi.dashcricket.DashCricketSettingsActivity")));
 		Util.debug("need to configure");
 	}
 }
