 package projectswop20102011.externalsystem;
 
 import be.kuleuven.cs.swop.api.IEmergencyDispatchApi;
 import be.kuleuven.cs.swop.api.ITime;
 import be.kuleuven.cs.swop.events.AbstractEventGenerator;
 import be.kuleuven.cs.swop.events.Time;
 import be.kuleuven.cs.swop.external.ExternalSystemException;
 import be.kuleuven.cs.swop.external.IExternalSystem;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class TimeAheadAdapter extends AbstractEventGenerator {
 
 	/**
 	 * A variabele registering the externalSystem of this timeAheadController.
 	 */
 	private final IExternalSystem externalSystem;
 
 	/**
 	 * Creates a new instance of a TimeAheadAdapter with the given parameter.
 	 * @param api
 	 *		The IEmergencyDispatchApi of the TimeAheadAdapter
 	 * @effect The IEmergencyDispatchApi of this TimeAheadAdapter is set to the given IEmergencyDispatchApi.
 	 *		|super(api)
 	 * @post The IExternalSystem of this TimeAheadAdapter is set to the given IExternalSystem
 	 *		|this.externalSystem == externalSystem
 	 */
 	public TimeAheadAdapter(IEmergencyDispatchApi api, IExternalSystem externalSystem) {
 		super(api);
 		this.externalSystem = externalSystem;
 	}
 
 	/**
 	 * Returns the externalSystem of this TimeAheadAdapter.
 	 * @return The externalSystem of this TimeAheadAdapter.
 	 */
 	private IExternalSystem getExternalSystem() {
 		return externalSystem;
 	}
 
 	/**
 	 * Execute the events that happened in the externalSystem from worldTime to worldTime + seconds
 	 * @param worldTime
 	 *		The startime.
 	 * @param seconds
 	 *		The amount of time that has elapsed.
 	 */
 	public void timeChange(long worldTime, long seconds) {
 		int s = (int) (seconds + worldTime);
 		int hours = (s / 3600);
 		int minutes = ((s - (3600 * hours)) / 60);
		if (hours != 0 || minutes != 0) {
			ITime time = new Time(hours, minutes);
			executeEventsUntil(time);
		}
 	}
 
 	/**
 	 * Execute all events in the external system that happened until the given time.
 	 * @param itime
 	 *		The new time.
 	 */
 	@Override
 	protected void executeEventsUntil(ITime itime) {
 		try {
 			getExternalSystem().notifyTimeChanged(itime);
 		} catch (ExternalSystemException ex) {
 			Logger.getLogger(TimeAheadAdapter.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (IllegalArgumentException ex) {
 			Logger.getLogger(TimeAheadAdapter.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 }
