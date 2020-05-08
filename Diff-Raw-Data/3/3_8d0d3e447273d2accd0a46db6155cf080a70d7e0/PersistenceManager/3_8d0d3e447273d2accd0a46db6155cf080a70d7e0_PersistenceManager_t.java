 package se.chalmers.dat255.sleepfighter.persist;
 
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 
 import net.engio.mbassy.listener.Handler;
 import se.chalmers.dat255.sleepfighter.model.Alarm;
 import se.chalmers.dat255.sleepfighter.model.Alarm.AlarmEvent;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.model.IdProvider;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioConfig;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioSource;
 import android.content.Context;
 
 import com.google.common.collect.Maps;
 import com.j256.ormlite.android.apptools.OpenHelperManager;
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.field.DataPersisterManager;
 import com.j256.ormlite.stmt.QueryBuilder;
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
 	private final static String TAG = PersistenceManager.class.getSimpleName();
 
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
 	public AlarmList fetchAlarms() throws PersistenceException {
 		try {
 			return new AlarmList( this.joinFetched( this.makeAlarmQB().query() ) );
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
 	public AlarmList fetchAlarmsSortedNames() throws PersistenceException {
 		try {
 			return new AlarmList( this.joinFetched( this.makeAlarmQB().orderBy( "name", true ).query() ) );
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
 		OrmHelper helper = this.getHelper();
 
 		/*
 		 * Make lookup tables.
 		 * -------------------
 		 * Find all AudioSource:s present and make AudioSource.id -> index(Alarm) lookup table.
 		 * Make a AudioConfig.id -> index(Alarm) lookup table.
 		 */
 		Map<Integer, Integer> audioSourceLookup = Maps.newHashMap();
 		Map<Integer, Integer> audioConfigLookup = Maps.newHashMap();
 		for ( int i = 0; i < alarms.size(); ++i ) {
 			Alarm alarm = alarms.get( i );
 
 			AudioSource source = alarm.getAudioSource();
 			if ( source != null ) {
 				audioSourceLookup.put( source.getId(), i );
 			}
 
 			if(source != null){
 			audioConfigLookup.put( alarm.getAudioConfig().getId(), i );
 			}
 		}
 
 		/*
 		 * Query for all tables.
 		 */
 		List<AudioSource> audioSourceList = this.queryInIds( helper.getAudioSourceDao(), audioSourceLookup );
 		List<AudioConfig> audioConfigList = this.queryInIds( helper.getAudioConfigDao(), audioConfigLookup );
 
 		/*
 		 * Set all to respective Alarm object.
 		 */
 
 		// Set AudioSource to each alarm.
 		for ( AudioSource source : audioSourceList ) {
 			int alarmIndex = audioSourceLookup.get( source.getId() );
 			alarms.get( alarmIndex ).setFetched( source );
 		}
 
 		// Set AudioConfig to each alarm.
 		for ( AudioConfig config : audioConfigList ) {
 			int alarmIndex = audioConfigLookup.get( config.getId() );
 			alarms.get( alarmIndex ).setFetched( config );
 		}
 
 		return alarms;
 	}
 
 	/**
 	 * Helper for {@link #joinFetched(List)}, returns a list of items given a lookup table.
 	 *
 	 * @param dao the Domain Access Object for item type.
 	 * @param lookup the lookup table to get IDs from.
 	 * @return the list of items.
 	 */
 	private <T extends IdProvider> List<T> queryInIds( Dao<T, Integer> dao, Map<Integer, Integer> lookup ) {
 		try {
 			return dao.queryBuilder().where().in( AudioSource.ID_COLUMN, lookup.keySet().toArray() ).query();
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
 
 		// First handle any updates to foreign fields that are set directly in Alarm.
 		switch ( evt.getModifiedField() ) {
 		case AUDIO_SOURCE:
 			PersistenceExceptionDao<AudioSource, Integer> asDao = helper.getAudioSourceDao();
 			AudioSource audioSource = alarm.getAudioSource();
 
 			if ( evt.getOldValue() != null ) {
 				AudioSource old = (AudioSource) evt.getOldValue();
 				audioSource.setId( old.getId() );
 				asDao.update( audioSource );
 			} else {
 				asDao.create( alarm.getAudioSource() );
 			}
 			break;
 
 		/*
 		 * TODO: Will AudioConfig really be directly set via Alarm#setAudioConfig?
 		 * It can never be null so... Maybe use own event?
 		 */
 		case AUDIO_CONFIG:
 			helper.getAudioConfigDao().update( alarm.getAudioConfig() );
 			break;
 
 		default:
 			break;
 		}
		helper.getAlarmDao().update( alarm );
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
 
 		// Finally persist alarm itself to DB.
 		helper.getAlarmDao().create( alarm );
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
 
 		// Finally delete alarm itself from DB.
 		helper.getAlarmDao().delete( alarm );
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
