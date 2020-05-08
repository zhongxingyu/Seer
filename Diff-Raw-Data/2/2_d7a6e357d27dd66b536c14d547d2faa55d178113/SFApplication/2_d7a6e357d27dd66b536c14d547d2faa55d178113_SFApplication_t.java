 package se.chalmers.dat255.sleepfighter;
 
 import se.chalmers.dat255.sleepfighter.activities.AlarmPlannerService;
 import se.chalmers.dat255.sleepfighter.model.AlarmList;
 import se.chalmers.dat255.sleepfighter.persist.PersistenceManager;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 import android.app.Application;
 
 /**
  * A custom implementation of Application for SleepFighter.
  */
 public class SFApplication extends Application {
 	private static final boolean CLEAN_START = false;
 
 	private AlarmList alarmList;
 	private MessageBus<Message> bus;
 
 	private PersistenceManager persistenceManager;
 
 	private AlarmPlannerService.ChangeHandler alarmPlanner;
 
 	private static SFApplication app;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		app = this;
 
 		this.persistenceManager = new PersistenceManager( this );
 	}
 
 	/**
 	 * Returns the one and only SFApplication in town.
 	 *
 	 * @return the application.
 	 */
 	public static final SFApplication get() {
 		return app;
 	}
 
 	/**
 	 * Returns the AlarmList for the application.<br/>
 	 * It is lazy loaded.
 	 * 
 	 * @return the AlarmList for the application
 	 */
 	public synchronized AlarmList getAlarms() {
 		if ( this.alarmList == null ) {
 			if ( CLEAN_START ) {
 				this.persistenceManager.cleanStart();
 			}
 
 			this.alarmList = this.getPersister().fetchAlarms();
 
 
 			this.alarmList.setMessageBus(this.getBus());
 			this.bus.subscribe( this.persistenceManager );
 
 			this.registerAlarmPlanner();
 		}
 
 		return alarmList;
 	}
 
 	private void registerAlarmPlanner() {
 		this.alarmPlanner = new AlarmPlannerService.ChangeHandler( this, this.alarmList );
 		this.bus.subscribe( this.alarmPlanner );
 	}
 
 	/**
 	 * Returns the default MessageBus for the application.
 	 * 
 	 * @return the default MessageBus for the application
 	 */
 	public synchronized MessageBus<Message> getBus() {
 		if ( this.bus == null ) {
 			this.bus = new MessageBus<Message>();
 		}
 		return bus;
 	}
 
 	/**
 	 * Returns the PersistenceManager for the application.
 	 *
	 * @return the persistence manager.
 	 */
 	public synchronized PersistenceManager getPersister() {
 		return this.persistenceManager;
 	}
 }
