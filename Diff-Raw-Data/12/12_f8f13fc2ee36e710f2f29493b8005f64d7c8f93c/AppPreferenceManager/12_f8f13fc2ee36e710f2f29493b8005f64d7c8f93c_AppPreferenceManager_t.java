 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
 * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.preference;
 
 import se.toxbee.sleepfighter.utils.model.LocalizationProvider;
 import se.toxbee.sleepfighter.utils.prefs.PreferenceNode;
 
 /**
  * {@link AppPreferenceManager} is the central API for application wide preferences.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Dec 15, 2013
  */
 public final class AppPreferenceManager extends AppPreferenceNode {
 	public AppPreferenceManager( PreferenceNode b, LocalizationProvider lp ) {
 		super( b, null );
 
 		alarmControl = new AlarmControlPreferences( b );
 		display = new DisplayPreferences( p );
 		challenge = new ChallengeGlobalPreferences( p, lp );
 		locFilter = new LocationFilterPreferences( p );
 		weather = new WeatherPreferences( p );
 	}
 
 	public final DisplayPreferences display;
 	public final AlarmControlPreferences alarmControl;
 	public final LocationFilterPreferences locFilter;
 	public final ChallengeGlobalPreferences challenge;
 	public final WeatherPreferences weather;
 
 	/**
 	 * Returns whether or not proprietary code is allowed.<br/>
 	 * If this returns false NOTHING proprietary is allowed to run.
 	 *
 	 * @return
 	 */
 	public boolean isProprietaryAllowed() {
 		return p.getBoolean( "isProprietaryAllowed", true );
 	}
 
 	/**
 	 * Returns
 	 *
 	 * @return
 	 */
 	public boolean isOpenSourceOnly() {
 		return !this.isProprietaryAllowed();
 	}
 
 	/**
 	 * Sets the current versionCode in preferences.
 	 *
 	 * @param versionCode the current versionCode to set.
 	 */
 	public void versionCode( int versionCode ) {
 		p.setInt( "versionCode", versionCode );
 	}
 
 	/**
 	 * Returns the current versionCode in preferences.
 	 *
 	 * @return the current versionCode.
 	 */
 	public int versionCode() {
 		return p.getInt( "versionCode", -1 );
 	}
 }
