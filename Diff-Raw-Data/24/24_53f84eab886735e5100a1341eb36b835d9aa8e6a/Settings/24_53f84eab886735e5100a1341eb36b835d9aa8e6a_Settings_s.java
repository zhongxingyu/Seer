 /*
  * wePoker: Play poker with your friends, wherever you are!
  * Copyright (C) 2012, The AmbientTalk team.
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 2 of the License, or (at your
  * option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  */
 
 package edu.vub.at.nfcpoker.settings;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.Random;
 
 import edu.vub.at.nfcpoker.R;
 
import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.provider.Settings.Secure;
 
 
 public class Settings {
 	public static volatile String UUID;
 	public static volatile String nickname;
 	public static volatile int avatar;
 	public static volatile int gamesPlayed;
 	
 	private static final int nicknameTxtLength = 10000;
 	private static SharedPreferences settings;
 
 	public static void loadSettings(Context ctx) {
 		PreferenceManager.setDefaultValues(ctx, R.xml.preferences, false);
 		settings = PreferenceManager.getDefaultSharedPreferences(ctx);
 		UUID = Secure.getString(ctx.getContentResolver(), Secure.ANDROID_ID);
 		gamesPlayed = settings.getInt("gamesPlayed", 0);
 		nickname = settings.getString("nickname", "<random>");
 		if (nickname.equals("<random>")) {
 			nickname = getRandomNickName(ctx);
 			saveSettings(ctx);
 		}
 	}
 
 	public static void saveSettings(Context ctx) {
 		SharedPreferences.Editor editor = settings.edit();
 		editor.putString("nickname", nickname);
 		editor.putInt("gamesPlayed", gamesPlayed);
 		editor.commit();
 	}
 	
 	public static SharedPreferences getSharedPreferences() {
 		return settings;
 	}
 	
 	public static boolean isWifiDirectPreferred() {
 		return settings.getBoolean("prefer_wifi_direct", true);
 	}
 
 	private static String getRandomNickName(Context ctx) {
 		InputStreamReader inputStream = new InputStreamReader(ctx.getResources().openRawResource(R.raw.random_names));
 		BufferedReader br = new BufferedReader(inputStream);
 		Random r = new Random();
 		int desiredLine = r.nextInt(nicknameTxtLength);
 		String theLine = "Player";
 		int lineCtr = 0;
 		try {
 			while ((theLine = br.readLine()) != null)   {
 				if (lineCtr == desiredLine) {
 					break;
 				}
 				lineCtr++;
 			}
 		} catch (IOException e) { }
 		return theLine;
 	}
 }
