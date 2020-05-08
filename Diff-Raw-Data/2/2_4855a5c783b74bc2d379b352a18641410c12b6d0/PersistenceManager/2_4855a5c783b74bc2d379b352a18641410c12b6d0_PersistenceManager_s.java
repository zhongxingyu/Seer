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
 package se.chalmers.dat255.sleepfighter.persist;
 
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import net.engio.mbassy.listener.Handler;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.AlarmEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.model.IdProvider;
 import se.chalmers.dat255.sleepfighter.model.SnoozeConfig;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioConfig;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioSource;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfig;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet.ChallengeParamEvent;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeConfigSet.Event;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeParam;
 import se.chalmers.dat255.sleepfighter.model.challenge.ChallengeType;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterArea;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterAreaAlarm;
 import se.chalmers.dat255.sleepfighter.model.gps.GPSFilterAreaSet;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import android.content.Context;
 import android.util.Log;
 
 import com.google.common.collect.BiMap;
 import com.google.common.collect.HashBiMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.field.DataPersisterManager;
 import com.j256.ormlite.stmt.DeleteBuilder;
 import com.j256.ormlite.stmt.QueryBuilder;
 import com.j256.ormlite.stmt.Where;
 import com.j256.ormlite.table.TableUtils;
 
 /**
  * Handles all reads and writes to persistence.<br/>
  * There should be no reason to keep more than 1 instance of this object.
  * 
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 21, 2013
  */
 public class PersistenceManager {
 	private static final String TAG = PersistenceManager.class.getSimpleName();
 
 	private volatile OrmHelper ormHelper = null;
 
 	private Context context;
 
 	private static boolean init = false;
 
 	/**
 	 * Handles changes in alarm-list (the list itself, additions, deletions, etc).
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleListChange( AlarmList.Event evt ) {
 		switch ( evt.operation() ) {
 		case CLEAR:
 			this.clearAlarms();
 			break;
 
 		case ADD:
 			for ( Object elem : evt.elements() ) {
 				Debug.d("added alarm to database");
 				this.addAlarm( (Alarm) elem );
 			}
 			break;
 
 		case REMOVE:
 			for ( Object elem : evt.elements() ) {
 				this.removeAlarm( (Alarm) elem );
 			}
 			break;
 
 		case UPDATE:
 			Alarm old = (Alarm) evt.elements().iterator().next();
 			this.removeAlarm( old );
 			this.addAlarm( evt.source().get( evt.index() ) );
 			break;
 		}
 	}
 
 	/**
 	 * Handles a change in an alarm.
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleAlarmChange( AlarmEvent evt ) {
 		this.updateAlarm( evt.getAlarm(), evt );
 	}
 
 	/**
 	 * Handles a change in {@link ChallengeConfigSet}
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleChallengeChange( ChallengeConfigSet.Event evt ) {
 		this.updateChallenges( evt );
 	}
 
 	/**
 	 * Handles a change in {@link AudioConfig}
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleAudioConfigChange( AudioConfig.ChangeEvent evt ) {
 		this.updateAudioConfig( evt );
 	}
 	
 	/**
 	 * Handles a change in {@link SnoozeConfig}
 	 *
 	 * @param evt the event.
 	 */
 	@Handler
 	public void handleSnoozeConfigChange( SnoozeConfig.ChangeEvent evt ) {
 		this.updateSnoozeConfig( evt );
 	}
 
 	/**
 	 * Constructs the PersistenceManager.
 	 *
 	 * @param context android context.
 	 */
 	public PersistenceManager( Context context ) {
 		this.setContext( context );
 	}
 
 	/**
 	 * Sets the context to use.
 	 *
 	 * @param context android context.
 	 */
 	public void setContext( Context context ) {
 		this.context = context;
 	}
 
 	/**
 	 * Rebuilds all data-structures. Any data is lost.
 	 */
 	public void cleanStart() {
 		this.getHelper().rebuild();
 	}
 
 	/**
 	 * Clears the list of all alarms.
 	 *
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public void clearAlarms() throws PersistenceException {
 		this.clearTable( Alarm.class );
 
 		this.clearTable( AudioSource.class );
 		this.clearTable( AudioConfig.class );
 
 		this.clearTable( ChallengeConfigSet.class );
 		this.clearTable( ChallengeConfig.class );
 		this.clearTable( ChallengeParam.class );
 
 		this.clearTable(SnoozeConfig.class);
 
 		this.clearTable( GPSFilterAreaAlarm.class );
 	}
 
 	/**
 	 * Clears a DB table for given class.
 	 *
 	 * @param clazz the class to clear table for.
 	 */
 	private void clearTable( Class<?> clazz ) {
 		try {
 			TableUtils.clearTable( this.getHelper().getConnectionSource(), clazz );
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Fetches an AlarmsManager from database, it is sorted on ID.
 	 *
 	 * @return the fetched AlarmsManager.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public List<Alarm> fetchAlarms() throws PersistenceException {
 		try {
 			Debug.d("fetching alarms");
 			return this.joinFetched( this.makeAlarmQB().query() );
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Fetches an AlarmsManager from database, it is sorted on names.
 	 *
 	 * @return the fetched AlarmsManager.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public List<Alarm> fetchAlarmsSortedNames() throws PersistenceException {
 		try {
 			return this.joinFetched( this.makeAlarmQB().orderBy( "name", true ).query() );
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Fetches a single alarm from database by its id.
 	 *
 	 * @param id the ID of the alarm to fetch.
 	 * @return the fetched Alarm.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public Alarm fetchAlarmById( int id ) throws PersistenceException {
 		try {
 			List<Alarm> alarms = this.joinFetched( this.makeAlarmQB().where().idEq( id ).query() );
 			return alarms == null || alarms.size() == 0 ? null : alarms.get( 0 );
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Constructs a QueryBuilder (QB) for querying 0-many Alarm(s).
 	 *
 	 * @return the query builder.
 	 */
 	private QueryBuilder<Alarm, Integer> makeAlarmQB() {
 		return this.getHelper().getAlarmDao().queryBuilder();
 	}
 
 	/**
 	 * Performs "Joins" and fetches all to Alarm associated objects and sets to respective Alarm.
 	 *
 	 * @param alarms the list of alarms to fill in blanks for.
 	 * @return the passed argument, for fluid interface.
 	 */
 	private List<Alarm> joinFetched( final List<Alarm> alarms ) {
 		if ( alarms.size() == 0 ) {
 			return alarms;
 		}
 
 		OrmHelper helper = this.getHelper();
 
 		/*
 		 * Make lookup tables.
 		 * -------------------
 		 * Find all AudioSource:s present and make AudioSource.id -> index(Alarm) lookup table.
 		 * Make a AudioConfig.id -> index(Alarm) lookup table.
 		 */
 		Map<Integer, Integer> audioSourceLookup = Maps.newHashMap();
 		Map<Integer, Integer> audioConfigLookup = Maps.newHashMap();
 		Map<Integer, Integer> snoozeConfigLookup = Maps.newHashMap();
 		BiMap<Integer, Integer> challengeSetLookup = HashBiMap.create();
 
 		for ( int i = 0; i < alarms.size(); ++i ) {
 			Alarm alarm = alarms.get( i );
 
 			// Audio Source.
 			AudioSource source = alarm.getAudioSource();
 			if ( source != null ) {
 				audioSourceLookup.put( source.getId(), i );
 			}
 
 			// Audio Config.
 			audioConfigLookup.put( alarm.getAudioConfig().getId(), i );
 
 			// Snooze config
 			snoozeConfigLookup.put( alarm.getSnoozeConfig().getId(), i );
 
 			// Challenge related.
 			challengeSetLookup.put( alarm.getChallengeSet().getId(), i );
 		}
 
 		/*
 		 * Query for all tables.
 		 */
 		List<AudioSource> audioSourceList = this.queryInIds( helper.getAudioSourceDao(), AudioSource.ID_COLUMN, audioSourceLookup );
 		List<AudioConfig> audioConfigSetList = this.queryInIds( helper.getAudioConfigDao(), AudioConfig.ID_COLUMN, audioConfigLookup );
 		List<SnoozeConfig> snoozeConfigSetList = this.queryInIds( helper.getSnoozeConfigDao(), SnoozeConfig.ID_COLUMN, snoozeConfigLookup );
 
 		/*
 		 * Set all to respective Alarm object.
 		 */
 
 		// Set AudioSource to each alarm.
 		for ( AudioSource source : audioSourceList ) {
 			int alarmIndex = audioSourceLookup.get( source.getId() );
 			alarms.get( alarmIndex ).setFetched( source );
 		}
 
 		// Set AudioConfig to each alarm.
 		for ( AudioConfig config : audioConfigSetList ) {
 			int alarmIndex = audioConfigLookup.get( config.getId() );
 			alarms.get( alarmIndex ).setFetched( config );
 		}
 
 		for ( SnoozeConfig config : snoozeConfigSetList ) {
 			int alarmIndex = snoozeConfigLookup.get( config.getId() );
 			alarms.get( alarmIndex ).setFetched( config );
 		}
 
 		/*
 		 * Challenges...
 		 */
 		this.fetchJoinChallenge( alarms, challengeSetLookup );
 
 		return alarms;
 	}
 
 	/**
 	 * "Joins" in challenge set into list of alarms.
 	 *
 	 * @param alarms list of alarms.
 	 * @param challengeSetLookup the lookup challenge set id -> index in list of alarms.
 	 */
 	private void fetchJoinChallenge( List<Alarm> alarms, Map<Integer, Integer> challengeSetLookup ) {
 		OrmHelper helper = this.getHelper();
 
 		// 1) Read all sets and set them.
 		List<ChallengeConfigSet> challengeSetList = this.queryInIds( helper.getChallengeConfigSetDao(), ChallengeConfigSet.ID_COLUMN, challengeSetLookup );
 		for ( ChallengeConfigSet challengeSet : challengeSetList ) {
 			// Bind challenge config set to alarm.
 			int alarmIndex = challengeSetLookup.get( challengeSet.getId() );
 			alarms.get( alarmIndex ).setChallenges( challengeSet );
 		}
 
 		// 2) Read all challenge config:s and set to each set.
 		Map<Integer, ChallengeConfig> challengeConfigLookup = Maps.newHashMap();
 		PersistenceExceptionDao<ChallengeConfig, Integer> configDao = helper.getChallengeConfigDao();
 		List<ChallengeConfig> challengeConfigList = this.queryInIds( configDao, ChallengeConfig.SET_FOREIGN_COLUMN, challengeSetLookup );
 		for ( ChallengeConfig challengeConfig : challengeConfigList ) {
 			// Bind challenge config to set.
 			int alarmIndex = challengeSetLookup.get( challengeConfig.getSetId() );
 			alarms.get( alarmIndex ).getChallengeSet().putChallenge( challengeConfig );
 
 			// Add to challenge config lookup.
 			challengeConfigLookup.put( challengeConfig.getId(), challengeConfig );
 		}
 
 		// 3) Sanity fix. Find any missing ChallengeType:s and add them.
 		for ( ChallengeConfigSet challengeSet : challengeSetList ) {
			Set<ChallengeType> missingTypes = Sets.complementOf( challengeSet.getDefinedTypes() );
 
 			for ( ChallengeType type : missingTypes ) {
 				ChallengeConfig config =  new ChallengeConfig( type, false );
 				config.setFetchedSetId( challengeSet.getId() );
 
 				configDao.create( config );
 
 				challengeSet.putChallenge( config );
 			}
 		}
 
 		// 3) Finally read all parameters and set to each config.
 		List<ChallengeParam> challengeParamList = this.queryInIds( helper.getChallengeParamDao(), ChallengeParam.CHALLENGE_ID_COLUMN, challengeConfigLookup );
 		for ( ChallengeParam param : challengeParamList ) {
 			challengeConfigLookup.get( param.getId() ).setFetched( param );
 		}
 	}
 
 	/**
 	 * Helper for {@link #joinFetched(List)}, returns a list of items given a lookup table.
 	 *
 	 * @param dao the Domain Access Object for item type.
 	 * @param lookup the lookup table to get IDs from.
 	 * @return the list of items.
 	 */
 	private <T extends IdProvider> List<T> queryInIds( Dao<T, Integer> dao, String idColumn, Map<Integer, ?> lookup ) {
 		try {
 			return dao.queryBuilder().where().in( idColumn, lookup.keySet().toArray() ).query();
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Updates an alarm to database.
 	 *
 	 * @param alarm the alarm to update.
 	 * @param evt AlarmEvent that occurred, required to update foreign fields.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public void updateAlarm( Alarm alarm, AlarmEvent evt ) throws PersistenceException {
 		OrmHelper helper = this.getHelper();
 
 		boolean updateAlarmTable = false;
 
 		// First handle any updates to foreign fields that are set directly in Alarm.
 		switch ( evt.getModifiedField() ) {
 		case AUDIO_SOURCE:
 			updateAlarmTable = this.updateAudioSource( alarm.getAudioSource(), (AudioSource) evt.getOldValue() );
 			break;
 
 		default:
 			updateAlarmTable = true;
 			break;
 		}
 
 		if ( updateAlarmTable ) {
 			helper.getAlarmDao().update( alarm );
 		}
 	}
 
 	/**
 	 * Updates an Audio Config to database.
 	 *
 	 * @param ac the AudioConfig to update
 	 * @param evt ChangeEvent that occurred, required to update foreign fields.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public void updateAudioConfig( AudioConfig.ChangeEvent evt ) throws PersistenceException {
 		OrmHelper helper = this.getHelper();
 
 		helper.getAudioConfigDao().update( evt.getAudioConfig() );
 	}
 
 	/**
 	 * Updates a SnoozeConfig to database.
 	 *
 	 * @param ac the SnoozeConfig to update
 	 * @param evt SnoozeConfig that occurred, required to update foreign fields.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public void updateSnoozeConfig( SnoozeConfig.ChangeEvent evt ) throws PersistenceException {
 		OrmHelper helper = this.getHelper();
 
 		helper.getSnoozeConfigDao().update( evt.getSnoozeConfig() );
 	}
 
 	/**
 	 * Updates the audio source.
 	 *
 	 * @param source the audio source.
 	 * @param old the old source if any.
 	 * @return true if the alarm table must be updated as a result.
 	 */
 	private boolean updateAudioSource( AudioSource source, AudioSource old ) {
 		OrmHelper helper = this.getHelper();
 		PersistenceExceptionDao<AudioSource, Integer> asDao = helper.getAudioSourceDao();
 
 		if ( old != null ) {
 			if ( source == null ) {
 				asDao.delete( source );
 			} else {
 				source.setId( old.getId() );
 				asDao.update( source );
 				return false;
 			}
 		} else {
 			asDao.create( source );
 		}
 		return true;
 	}
 
 	/**
 	 * Updates the challenges.
 	 *
 	 * @param evt the event.
 	 */
 	private void updateChallenges( Event evt ) {
 		Log.d( TAG, evt.toString() );
 
 		OrmHelper helper = this.getHelper();
 		ChallengeConfigSet set = evt.getSet();
 
 		if ( evt instanceof ChallengeConfigSet.EnabledEvent ) {
 			// Handle change for isEnabled().
 			helper.getChallengeConfigSetDao().update( set );
 			return;
 		} else if ( evt instanceof ChallengeConfigSet.ChallengeEvent ) {
 			ChallengeConfig config = ((ChallengeConfigSet.ChallengeEnabledEvent) evt).getChallengeConfig();
 
 			if ( evt instanceof ChallengeConfigSet.ChallengeEnabledEvent ) {
 				// Handle change for isEnabled() for specific ChallengeConfig.
 				helper.getChallengeConfigDao().update( config );
 				return;
 			} else if ( evt instanceof ChallengeConfigSet.ChallengeParamEvent ) {
 				// Handle change for a specific challenge config parameter.
 				ChallengeParamEvent event = (ChallengeParamEvent) evt;
 				String key = event.getKey();
 
 				ChallengeParam param = new ChallengeParam( config.getId(), key, config.getParam( key ) );
 				helper.getChallengeParamDao().createOrUpdate( param );
 				return;
 			}
 		}
 
 		throw new AssertionError( "Do you know something we don't?" );
 	}
 
 	/**
 	 * Stores/adds an alarm to database.
 	 *
 	 * @param alarm the alarm to store.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public void addAlarm( Alarm alarm ) throws PersistenceException {
 		OrmHelper helper = this.getHelper();
 
 		// Handle audio source foreign object if present.
 		AudioSource audioSource = alarm.getAudioSource();
 		helper.getAudioSourceDao().create( audioSource );
 
 		// Handle audio config foreign object.
 		AudioConfig audioConfig = alarm.getAudioConfig();
 		helper.getAudioConfigDao().create( audioConfig );
 
 		// Handle snooze config foreign object.
 		SnoozeConfig snoozeConfig = alarm.getSnoozeConfig();
 		helper.getSnoozeConfigDao().create( snoozeConfig );
 
 		this.addChallengeSet( alarm );
 
 		// Finally persist alarm itself to DB.
 		helper.getAlarmDao().create( alarm );
 	}
 
 	/**
 	 * Stores/adds the challenge set of alarm when adding alarm to database.
 	 *
 	 * @param alarm the alarm to save challenge set for.
 	 */
 	private void addChallengeSet( Alarm alarm ) {
 		OrmHelper helper = this.getHelper();
 
 		// Insert set.
 		ChallengeConfigSet set = alarm.getChallengeSet();
 		helper.getChallengeConfigSetDao().create( set );
 
 		PersistenceExceptionDao<ChallengeConfig, Integer> challengeDao = helper.getChallengeConfigDao();
 		PersistenceExceptionDao<ChallengeParam, Integer> paramDao = helper.getChallengeParamDao();
 
 		for ( ChallengeConfig challenge : set.getConfigs() ) {
 			// Insert challenge.
 			challenge.setFetchedSetId( set.getId() );
 			challengeDao.create( challenge );
 
 			// Insert each param.
 			for ( Entry<String, String> entry : challenge.getParams().entrySet() ) {
 				ChallengeParam param = new ChallengeParam( challenge.getId(), entry.getKey(), entry.getValue() );
 				paramDao.create( param );
 			}
 		}
 	}
 
 	/**
 	 * Removes an alarm from database.
 	 *
 	 * @param alarm the alarm to remove.
 	 * @throws PersistenceException if some SQL error happens.
 	 */
 	public void removeAlarm( Alarm alarm ) throws PersistenceException {
 		OrmHelper helper = this.getHelper();
 
 		// Handle audio source foreign object if present.
 		AudioSource audioSource = alarm.getAudioSource();
 		if ( audioSource != null ) {
 			helper.getAudioSourceDao().delete( audioSource );
 		}
 
 		// Handle audio config foreign object.
 		AudioConfig audioConfig = alarm.getAudioConfig();
 		helper.getAudioConfigDao().delete( audioConfig );
 
 		// Handle snooze config foreign object.
 		SnoozeConfig snoozeConfig = alarm.getSnoozeConfig();
 		helper.getSnoozeConfigDao().delete(snoozeConfig);
 
 		// Handle challenge config set foreign object.
 		this.removeChallengeSet( alarm );
 
 		// Remove from ExludeArea junction table.
 		this.deleteGPSFilterAreaJunction( GPSFilterAreaAlarm.ALARM_ID_FIELD, alarm.getId() );
 
 		// Finally delete alarm itself from DB.
 		helper.getAlarmDao().delete( alarm );
 	}
 
 	/**
 	 * Removes the challenge set of alarm when removing alarm from database.
 	 *
 	 * @param alarm the alarm to remove challenge set for.
 	 */
 	private void removeChallengeSet( Alarm alarm ) {
 		OrmHelper helper = this.getHelper();
 
 		ChallengeConfigSet set = alarm.getChallengeSet();
 
 		// Compute list of ids to remove.
 		Collection<ChallengeConfig> challengeList = set.getConfigs();
 		List<Integer> removeIds = Lists.newArrayListWithCapacity( challengeList.size() );
 
 		for ( ChallengeConfig challenge : challengeList ) {
 			removeIds.add( challenge.getId() );
 		}
 
 		// Remove all challenge configs & params.
 		helper.getChallengeConfigDao().deleteIds( removeIds );
 		helper.getChallengeParamDao().deleteIds( removeIds );
 
 		// Finally remove set.
 		helper.getChallengeConfigSetDao().delete( set );
 	}
 
 	/**
 	 * Fetches all available GPSFilterArea:s.
 	 *
 	 * @return the list of GPSFilterArea:s.
 	 */
 	public GPSFilterAreaSet fetchGPSFilterAreas() {
 		OrmHelper helper = this.getHelper();
 		return new GPSFilterAreaSet( helper.getGPSFilterAreaDao().queryForAll() );
 	}
 
 	/**
 	 * Stores/updates an GPSFilterArea in database.
 	 *
 	 * @param area the GPSFilterArea to store/update.
 	 */
 	public void setGPSFilterArea( GPSFilterArea area ) {
 		OrmHelper helper = this.getHelper();
 		helper.getGPSFilterAreaDao().createOrUpdate( area );
 	}
 
 	/**
 	 * Deletes an GPSFilterArea from database. 
 	 *
 	 * @param area the area.
 	 */
 	public void deleteGPSFilterArea( GPSFilterArea area ) {
 		OrmHelper helper = this.getHelper();
 
 		this.deleteGPSFilterAreaJunction( GPSFilterAreaAlarm.EXCLUDE_AREA_ID_FIELD, area.getId() );
 
 		helper.getGPSFilterAreaDao().delete( area );
 	}
 
 	/**
 	 * Binds a GPSFilterArea to an Alarm.
 	 *
 	 * @param alarm the alarm.
 	 * @param area the area.
 	 */
 	public void bindGPSFilterArea( Alarm alarm, GPSFilterArea area ) {
 		OrmHelper helper = this.getHelper();
 		helper.getGPSFilterAreaAlarmDao().create( new GPSFilterAreaAlarm( alarm, area ) );
 	}
 
 	/**
 	 * Unbinds an GPSFilterArea from an alarm.
 	 *
 	 * @param alarm the alarm.
 	 * @param area the area.
 	 */
 	public void unbindGPSFilterArea( Alarm alarm, GPSFilterArea area ) {
 		OrmHelper helper = this.getHelper();
 		try {
 			DeleteBuilder<GPSFilterAreaAlarm, Integer> builder = helper.getGPSFilterAreaAlarmDao().deleteBuilder();
 			Where<GPSFilterAreaAlarm, Integer> where = builder.where()
 				.eq( GPSFilterAreaAlarm.ALARM_ID_FIELD, alarm.getId() ).and()
 				.eq( GPSFilterAreaAlarm.EXCLUDE_AREA_ID_FIELD, area.getId() );
 
 			builder.setWhere( where );
 			builder.delete();
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Removes a junction row for Alarm:GPSFilterArea relation.
 	 *
 	 * @param column the column that has the ID field of either Alarm or GPSFilterArea.
 	 * @param id the ID of Alarm or Alarm:GPSFilterArea
 	 */
 	private void deleteGPSFilterAreaJunction( String column, int id ) {
 		OrmHelper helper = this.getHelper();
 		try {
 			DeleteBuilder<GPSFilterAreaAlarm, Integer> builder = helper.getGPSFilterAreaAlarmDao().deleteBuilder();
 			Where<GPSFilterAreaAlarm, Integer> where = builder.where().eq( column, id );
 
 			builder.setWhere( where );
 			builder.delete();
 		} catch ( SQLException e ) {
 			throw new PersistenceException( e );
 		}
 	}
 
 	/**
 	 * Removes all gpsfilter areas (& junction table for alarms as a consequence).
 	 */
 	public void clearGPSFilterAreas() {
 		this.clearTable( GPSFilterArea.class );
 		this.clearTable( GPSFilterAreaAlarm.class );
 	}
 
 	/**
 	 * Releases any resources held such as the OrmHelper.
 	 */
 	public void release() {
 		if ( this.ormHelper != null ) {
 			OpenHelperManager.releaseHelper();
 			this.ormHelper = null;
 		}
 	}
 
 	/**
 	 * Returns the OrmHelper.
 	 *
 	 * @return the helper.
 	 */
 	public OrmHelper getHelper() {
 		if ( this.ormHelper == null ) {
 			if ( this.context == null ) {
 				throw new PersistenceException( "There is no helper set and context == null" );
 			}
 
 			this.loadHelper();
 		}
 
 		return this.ormHelper;
 	}
 
 	/**
 	 * Loads the OrmHelper.
 	 */
 	private void loadHelper() {
 		this.ormHelper = OpenHelperManager.getHelper( this.context, OrmHelper.class );
 
 		this.init();
 	}
 
 	/**
 	 * Initialization code goes here.
 	 */
 	private void init() {
 		// Run Once guard.
 		if ( init ) {
 			return;
 		}
 		init = true;
 
 		// Initialization code goes here.
 		DataPersisterManager.registerDataPersisters( BooleanArrayType.getSingleton() );
 	}
 }
