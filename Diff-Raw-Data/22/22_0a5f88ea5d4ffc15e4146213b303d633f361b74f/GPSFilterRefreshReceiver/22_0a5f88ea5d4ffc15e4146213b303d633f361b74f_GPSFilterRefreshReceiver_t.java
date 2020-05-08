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
 package se.chalmers.dat255.sleepfighter.receiver;
 
 import org.joda.time.DateTime;
 
 import se.chalmers.dat255.sleepfighter.SFApplication;
 import se.chalmers.dat255.sleepfighter.gps.GPSFilterLocationRetriever;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.model.AlarmTimestamp;
 import se.chalmers.dat255.sleepfighter.preference.GlobalPreferencesManager;
 import se.chalmers.dat255.sleepfighter.utils.android.AlarmWakeLocker;
 import android.app.AlarmManager;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.location.Criteria;
 import android.location.LocationManager;
 import android.os.Bundle;
 
 /**
  * GPSFilterRefreshReceiver is responsible for simply scheduling and requesting updates,<br/>
  * and receiving responses to said requests.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Oct 10, 2013
  */
 public class GPSFilterRefreshReceiver extends BroadcastReceiver {
 	private static final String FROM_ALARM_MANAGER = "from_alarm_manager";
 	private static final String REQUESTING_SINGLE = "requesting_single";
 
 	private static final int MINUTE_TO_MS_FACTOR = 60000;
 
 	@Override
 	public void onReceive( Context context, Intent intent ) {
 		AlarmWakeLocker.acquire( context );
 
 		if ( intent.getBooleanExtra( FROM_ALARM_MANAGER, false ) ) {
 			// We got this from AlarmManager, we need to make a single request.
 			// Make pending intent.
 			Bundle bundle = new Bundle();
 			bundle.putBoolean( FROM_ALARM_MANAGER, true );
 			PendingIntent pi = makePi( context, bundle );
 
 			// Make the request.
 			GPSFilterLocationRetriever locRet = getLocRet( context );
 			if ( locRet == null ) {
 				AlarmWakeLocker.release();
 				return;
 			}
 
 			locRet.requestSingleUpdate( context, pi );
 		} else {
 			// Get refresh interval.
 			GlobalPreferencesManager manager = SFApplication.get().getPrefs();
 			int interval = manager.getLocationRefreshInterval();
 
 			boolean reqSingle = intent.getBooleanExtra( REQUESTING_SINGLE, false );
 
 			if ( interval == 0 ) {
 				if ( !reqSingle ) {
 					unscheduleUpdates( context );
 				}
 
 				AlarmWakeLocker.release();
 				return;
 			}
 
 			// Check if next update will be after alarm (too late).
 			long now = new DateTime().getMillis();
 			AlarmList list = SFApplication.get().getAlarms();
 			AlarmTimestamp at = list.getEarliestAlarm( now );
			if ( at.getMillis() + (long)interval * MINUTE_TO_MS_FACTOR > now ) {
 				if ( !reqSingle ) {
 					unscheduleUpdates( context );
 				}
 
 				AlarmWakeLocker.release();
 				return;
 			}
 
 			if ( reqSingle ) {
 				// We've made our single requests and interval updates are on, start requesting them.
 				Bundle bundle = new Bundle();
 				PendingIntent pi = makePi( context, bundle );
 				getLocRet( context ).requestUpdates( context, pi, interval, manager.getLocationMinDistance() );
 			}
 		}
 	}
 
 
 	/**
 	 * Schedules for a fix.
 	 *
 	 * @param context android context.
 	 * @param alarmTime the time of earliest alarm in Unix time.
 	 */
 	public static void scheduleFix( Context context, long alarmTime ) {
 		SFApplication app = SFApplication.get();
 		GlobalPreferencesManager manager = app.getPrefs();
 
 		// Make sure we're allowed to make refreshes and that there are any areas to refresh for.
 		if ( !(manager.isLocationFilterEnabled() && app.getPersister().fetchGPSFilterAreas().hasEnabledAndValid()) ) {
 			// No areas, unschedule instead.
 			unscheduleUpdates( context );
 			return;
 		}
 
 		// Figure out Unix time of when to make the first fix.
 		int frtd = manager.getLocationFRTD();
 
 		// If first request time delta = 0 then it is disabled.
 		if ( frtd == 0 ) {
 			return;
 		}
 
		long unixTime = alarmTime + (long)frtd * MINUTE_TO_MS_FACTOR;
 
 		// Make pending intent.
 		Bundle bundle = new Bundle();
 		bundle.putBoolean( FROM_ALARM_MANAGER, true );
 		PendingIntent pi = makePi( context, bundle );
 
 		// Schedule.
 		getAm( context ).set( AlarmManager.RTC_WAKEUP, unixTime, pi );
 	}
 
 	/**
 	 * Unschedules fixes if any.
 	 *
 	 * @param context android context.
 	 */
 	public static void unscheduleFix( Context context ) {
 		getAm( context ).cancel( makePi( context, null ) );
 		unscheduleUpdates( context );
 	}
 
 	private static GPSFilterLocationRetriever getLocRet( Context context ) {
 		Criteria criteria = new Criteria();
 		criteria.setAccuracy( Criteria.ACCURACY_FINE );
 
 		GPSFilterLocationRetriever locRet = new GPSFilterLocationRetriever( criteria );
 
 		return locRet.getBestProvider( context ) == null ? null : locRet;
 	}
 
 	private static void unscheduleUpdates( Context context ) {
 		getLM( context ).removeUpdates( makePi( context, null ) );
 	}
 
 	private static PendingIntent makePi( Context context, Bundle extras ) {
 		Intent intent = new Intent( context, GPSFilterRefreshReceiver.class );
 
 		if ( extras != null ) {
 			intent.putExtras( extras );
 		}
 
 		PendingIntent pi = PendingIntent.getBroadcast( context, -1, intent, PendingIntent.FLAG_UPDATE_CURRENT );
 		return pi;
 	}
 
 	private static AlarmManager getAm( Context context ) {
 		return (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
 	}
 
 	private static LocationManager getLM( Context context ) {
 		return (LocationManager) context.getSystemService( Context.LOCATION_SERVICE );
 	}
 }
