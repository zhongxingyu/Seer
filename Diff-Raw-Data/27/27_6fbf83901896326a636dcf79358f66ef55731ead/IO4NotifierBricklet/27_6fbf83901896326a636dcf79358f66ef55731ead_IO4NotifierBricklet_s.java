 package com.buschmais.tinkerforge4jenkins.core.notifier.io4;
 
 import static com.buschmais.tinkerforge4jenkins.core.BuildState.ABORTED;
 import static com.buschmais.tinkerforge4jenkins.core.BuildState.FAILURE;
 import static com.buschmais.tinkerforge4jenkins.core.BuildState.NOT_BUILT;
 import static com.buschmais.tinkerforge4jenkins.core.BuildState.UNKNOWN;
 import static com.buschmais.tinkerforge4jenkins.core.BuildState.UNSTABLE;
 
 import com.buschmais.tinkerforge4jenkins.core.JobState;
 import com.buschmais.tinkerforge4jenkins.core.notifier.common.AbstractNotifierDevice;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.IO4ConfigurationType;
 import com.buschmais.tinkerforge4jenkins.core.schema.configuration.v1.JobsType;
 import com.tinkerforge.BrickletIO4;
 
 /**
  * Implementation of a notifier device for IO4 bricklets.
  * 
  * @author dirk.mahler
  */
 public class IO4NotifierBricklet extends
 		AbstractNotifierDevice<BrickletIO4, IO4ConfigurationType> {
 
 	private static final int PIN_BUILDING = 8;
 	private static final int PIN_GREEN = 1;
 	private static final int PIN_YELLOW = 2;
 	private static final int PIN_RED = 4;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param uid
 	 *            The uid of the device.
 	 * @param device
 	 *            The device.
 	 */
 	protected IO4NotifierBricklet(String uid, BrickletIO4 device) {
 		super(uid, device);
		// configure the bricklet: output high on all pins
		getDevice().setConfiguration(
				(short) (PIN_RED + PIN_YELLOW + PIN_GREEN + PIN_BUILDING), 'o',
				true);
 	}
 
 	@Override
 	public void preUpdate() {
 	}
 
 	@Override
 	public void postUpdate() {
		JobsType filter = getConfiguration().getJobs();
 		int value = 0;
 		if (!filter(getJobsByBuildState(FAILURE), filter).isEmpty()) {
 			value = PIN_RED;
 			// red
 		} else if (!getJobsByBuildState(ABORTED, NOT_BUILT, UNKNOWN, UNSTABLE)
 				.isEmpty()) {
 			// yellow
 			value = PIN_YELLOW;
 		} else {
 			// green
 			value = PIN_GREEN;
 		}
		boolean building = false;
 		for (JobState jobState : filter(getJobStates().values(), filter)) {
 			if (jobState.isBuilding()) {
				building = true;
 			}
 		}
		if (building) {
			value = value + PIN_BUILDING;
		}
 		getDevice().setValue((short) value);
 	}
 
 	@Override
 	public void updateFailed(String message) {
 		getDevice().setValue(
 				(short) (PIN_RED + PIN_YELLOW + PIN_GREEN + PIN_BUILDING));
 	}
 }
