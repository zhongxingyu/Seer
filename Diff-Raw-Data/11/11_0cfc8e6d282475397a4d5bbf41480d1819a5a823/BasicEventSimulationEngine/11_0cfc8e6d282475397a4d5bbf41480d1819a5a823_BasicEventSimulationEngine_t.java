 package net.sf.openrocket.simulation;
 
 import java.util.Iterator;
 
 import net.sf.openrocket.aerodynamics.Warning;
 import net.sf.openrocket.l10n.Translator;
 import net.sf.openrocket.logging.LogHelper;
 import net.sf.openrocket.motor.Motor;
 import net.sf.openrocket.motor.MotorId;
 import net.sf.openrocket.motor.MotorInstance;
 import net.sf.openrocket.motor.MotorInstanceConfiguration;
 import net.sf.openrocket.rocketcomponent.Configuration;
 import net.sf.openrocket.rocketcomponent.DeploymentConfiguration;
 import net.sf.openrocket.rocketcomponent.MotorConfiguration;
 import net.sf.openrocket.rocketcomponent.MotorMount;
 import net.sf.openrocket.rocketcomponent.RecoveryDevice;
 import net.sf.openrocket.rocketcomponent.RocketComponent;
 import net.sf.openrocket.rocketcomponent.Stage;
 import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
 import net.sf.openrocket.simulation.exception.MotorIgnitionException;
 import net.sf.openrocket.simulation.exception.SimulationException;
 import net.sf.openrocket.simulation.exception.SimulationLaunchException;
 import net.sf.openrocket.simulation.listeners.SimulationListenerHelper;
 import net.sf.openrocket.startup.Application;
 import net.sf.openrocket.unit.UnitGroup;
 import net.sf.openrocket.util.Coordinate;
 import net.sf.openrocket.util.MathUtil;
 import net.sf.openrocket.util.Pair;
 import net.sf.openrocket.util.SimpleStack;
 
 
 public class BasicEventSimulationEngine implements SimulationEngine {
 
 	private static final Translator trans = Application.getTranslator();
 	private static final LogHelper log = Application.getLogger();
 
 	// TODO: MEDIUM: Allow selecting steppers
 	private SimulationStepper flightStepper = new RK4SimulationStepper();
 	private SimulationStepper landingStepper = new BasicLandingStepper();
 	private SimulationStepper tumbleStepper = new BasicTumbleStepper();
 
	// Constant holding 30 degress in radians.  This is the AOA condition
	// necessary to transistion to tumbling.
	private final static double AOA_TUMBLE_CONDITION = Math.PI / 3.0;
	
 	private SimulationStepper currentStepper;
 
 	private SimulationStatus status;
 	
 	private String flightConfigurationId;
 
 	private SimpleStack<SimulationStatus> stages = new SimpleStack<SimulationStatus>();
 
 
 	@Override
 	public FlightData simulate(SimulationConditions simulationConditions) throws SimulationException {
 
 		// Set up flight data
 		FlightData flightData = new FlightData();
 
 		// Set up rocket configuration
 		Configuration configuration = setupConfiguration(simulationConditions);
 		flightConfigurationId = configuration.getFlightConfigurationID();
 		MotorInstanceConfiguration motorConfiguration = setupMotorConfiguration(configuration);
 		if (motorConfiguration.getMotorIDs().isEmpty()) {
 			throw new MotorIgnitionException("No motors defined in the simulation.");
 		}
 
 		status = new SimulationStatus(configuration, motorConfiguration, simulationConditions);
 		status.getEventQueue().add(new FlightEvent(FlightEvent.Type.LAUNCH, 0, simulationConditions.getRocket()));
 		{
 			// main sustainer stage
 			RocketComponent sustainer = configuration.getRocket().getChild(0);
 			status.setFlightData( new FlightDataBranch( sustainer.getName(), FlightDataType.TYPE_TIME));
 		}
 		stages.add(status);
 
 		SimulationListenerHelper.fireStartSimulation(status);
 
 		while( true ) {
 			if (stages.size() == 0 ) {
 				break;
 			}
 			SimulationStatus stageStatus = stages.pop();
 			if ( stageStatus == null ) {
 				break;
 			}
 			status = stageStatus;
 			FlightDataBranch dataBranch = simulateLoop();
 			flightData.addBranch(dataBranch);
 			flightData.getWarningSet().addAll( status.getWarnings() );
 		}
 		
 		SimulationListenerHelper.fireEndSimulation(status, null);
 
 		configuration.release();
 
 		if (!flightData.getWarningSet().isEmpty()) {
 			log.info("Warnings at the end of simulation:  " + flightData.getWarningSet());
 		}
 
 		return flightData;
 	}
 	
 	private FlightDataBranch simulateLoop() throws SimulationException {
 
 		// Initialize the simulation
 		currentStepper = flightStepper;
 		status = currentStepper.initialize(status);
 
 		// Get originating position (in case listener has modified launch position)
 		Coordinate origin = status.getRocketPosition();
 		Coordinate originVelocity = status.getRocketVelocity();
 
 		try {
 			double maxAlt = Double.NEGATIVE_INFINITY;
 
 			// Start the simulation
 			while (handleEvents()) {
 
 				// Take the step
 				double oldAlt = status.getRocketPosition().z;
 
 				if (SimulationListenerHelper.firePreStep(status)) {
 					// Step at most to the next event
 					double maxStepTime = Double.MAX_VALUE;
 					FlightEvent nextEvent = status.getEventQueue().peek();
 					if (nextEvent != null) {
 						maxStepTime = MathUtil.max(nextEvent.getTime() - status.getSimulationTime(), 0.001);
 					}
 					log.verbose("BasicEventSimulationEngine: Taking simulation step at t=" + status.getSimulationTime());
 					currentStepper.step(status, maxStepTime);
 				}
 				SimulationListenerHelper.firePostStep(status);
 
 
 				// Check for NaN values in the simulation status
 				checkNaN();
 
 				// Add altitude event
 				addEvent(new FlightEvent(FlightEvent.Type.ALTITUDE, status.getSimulationTime(),
 						status.getConfiguration().getRocket(),
 						new Pair<Double, Double>(oldAlt, status.getRocketPosition().z)));
 
 				if (status.getRocketPosition().z > maxAlt) {
 					maxAlt = status.getRocketPosition().z;
 				}
 
 
 				// Position relative to start location
 				Coordinate relativePosition = status.getRocketPosition().sub(origin);
 
 				// Add appropriate events
 				if (!status.isLiftoff()) {
 
 					// Avoid sinking into ground before liftoff
 					if (relativePosition.z < 0) {
 						status.setRocketPosition(origin);
 						status.setRocketVelocity(originVelocity);
 					}
 					// Detect lift-off
 					if (relativePosition.z > 0.02) {
 						addEvent(new FlightEvent(FlightEvent.Type.LIFTOFF, status.getSimulationTime()));
 					}
 
 				} else {
 
 					// Check ground hit after liftoff
 					if (status.getRocketPosition().z < 0) {
 						status.setRocketPosition(status.getRocketPosition().setZ(0));
 						addEvent(new FlightEvent(FlightEvent.Type.GROUND_HIT, status.getSimulationTime()));
 						addEvent(new FlightEvent(FlightEvent.Type.SIMULATION_END, status.getSimulationTime()));
 					}
 
 				}
 
 				// Check for launch guide clearance
 				if (!status.isLaunchRodCleared() &&
 						relativePosition.length() > status.getSimulationConditions().getLaunchRodLength()) {
 					addEvent(new FlightEvent(FlightEvent.Type.LAUNCHROD, status.getSimulationTime(), null));
 				}
 
 
 				// Check for apogee
 				if (!status.isApogeeReached() && status.getRocketPosition().z < maxAlt - 0.01) {
 					addEvent(new FlightEvent(FlightEvent.Type.APOGEE, status.getSimulationTime(),
 							status.getConfiguration().getRocket()));
 				}
 
 
 				// Check for burnt out motors
 				for (MotorId motorId : status.getMotorConfiguration().getMotorIDs()) {
 					MotorInstance motor = status.getMotorConfiguration().getMotorInstance(motorId);
 					if (!motor.isActive() && status.addBurntOutMotor(motorId)) {
 						addEvent(new FlightEvent(FlightEvent.Type.BURNOUT, status.getSimulationTime(),
 								(RocketComponent) status.getMotorConfiguration().getMotorMount(motorId), motorId));
 					}
 				}
 				
 				// Check for Tumbling
 				// Conditions for transision are:
 				//  apogee reached
 				// and is not already tumbling
 				// and not stable (cg > cp)
 				// and aoa > 30
 
 				if (status.isApogeeReached() && !status.isTumbling() ) {
 					double cp = status.getFlightData().getLast(FlightDataType.TYPE_CP_LOCATION);
 					double cg = status.getFlightData().getLast(FlightDataType.TYPE_CG_LOCATION);
 					double aoa = status.getFlightData().getLast(FlightDataType.TYPE_AOA);
					if( cg > cp && aoa > AOA_TUMBLE_CONDITION ) {
 						addEvent( new FlightEvent(FlightEvent.Type.TUMBLE,status.getSimulationTime()));
						status.setTumbling(true);
 					}
 
 				}
 
 			}
 
 		} catch (SimulationException e) {
 			SimulationListenerHelper.fireEndSimulation(status, e);
 			throw e;
 		}
 
 		return status.getFlightData();
 	}
 
 	/**
 	 * Create a rocket configuration from the launch conditions.
 	 *
 	 * @param simulation	the launch conditions.
 	 * @return				a rocket configuration with all stages attached.
 	 */
 	private Configuration setupConfiguration(SimulationConditions simulation) {
 		Configuration configuration = new Configuration(simulation.getRocket());
 		configuration.setAllStages();
 		configuration.setFlightConfigurationID(simulation.getMotorConfigurationID());
 
 		return configuration;
 	}
 
 
 
 	/**
 	 * Create a new motor instance configuration for the rocket configuration.
 	 *
 	 * @param configuration		the rocket configuration.
 	 * @return					a new motor instance configuration with all motors in place.
 	 */
 	private MotorInstanceConfiguration setupMotorConfiguration(Configuration configuration) {
 		MotorInstanceConfiguration motors = new MotorInstanceConfiguration();
 		final String motorId = configuration.getFlightConfigurationID();
 
 		Iterator<MotorMount> iterator = configuration.motorIterator();
 		while (iterator.hasNext()) {
 			MotorMount mount = iterator.next();
 			RocketComponent component = (RocketComponent) mount;
 			MotorConfiguration motorConfig = mount.getFlightConfiguration(motorId);
 			MotorConfiguration defaultConfig = mount.getDefaultFlightConfiguration();
 			Motor motor = motorConfig.getMotor();
 
 			if (motor != null) {
 				Coordinate[] positions = component.toAbsolute(mount.getMotorPosition(motorId));
 				for (int i = 0; i < positions.length; i++) {
 					Coordinate position = positions[i];
 					MotorId id = new MotorId(component.getID(), i + 1);
 					motors.addMotor(id, motorConfig, defaultConfig, mount, position);
 				}
 			}
 		}
 		return motors;
 	}
 
 	/**
 	 * Handles events occurring during the flight from the event queue.
 	 * Each event that has occurred before or at the current simulation time is
 	 * processed.  Suitable events are also added to the flight data.
 	 */
 	private boolean handleEvents() throws SimulationException {
 		boolean ret = true;
 		FlightEvent event;
 
 		log.verbose("HandleEvents: current branch = " + status.getFlightData().getBranchName() );
 		log.verbose("EventQueue = " + status.getEventQueue().toString());
 		for (event = nextEvent(); event != null; event = nextEvent()) {
 
 			// Ignore events for components that are no longer attached to the rocket
 			if (event.getSource() != null && event.getSource().getParent() != null &&
 					!status.getConfiguration().isStageActive(event.getSource().getStageNumber())) {
 				continue;
 			}
 
 			// Call simulation listeners, allow aborting event handling
 			if (!SimulationListenerHelper.fireHandleFlightEvent(status, event)) {
 				continue;
 			}
 
 			if (event.getType() != FlightEvent.Type.ALTITUDE) {
 				log.verbose("BasicEventSimulationEngine:  Handling event " + event);
 			}
 
 			if (event.getType() == FlightEvent.Type.IGNITION) {
 				MotorMount mount = (MotorMount) event.getSource();
 				MotorId motorId = (MotorId) event.getData();
 				MotorInstance instance = status.getMotorConfiguration().getMotorInstance(motorId);
 				if (!SimulationListenerHelper.fireMotorIgnition(status, motorId, mount, instance)) {
 					continue;
 				}
 			}
 
 			if (event.getType() == FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT) {
 				RecoveryDevice device = (RecoveryDevice) event.getSource();
 				if (!SimulationListenerHelper.fireRecoveryDeviceDeployment(status, device)) {
 					continue;
 				}
 			}
 
 
 
 			// Check for motor ignition events, add ignition events to queue
 			for (MotorId id : status.getMotorConfiguration().getMotorIDs()) {
 				MotorConfiguration.IgnitionEvent ignitionEvent = status.getMotorConfiguration().getMotorIgnitionEvent(id);
 				MotorMount mount = status.getMotorConfiguration().getMotorMount(id);
 				RocketComponent component = (RocketComponent) mount;
 
 				if (ignitionEvent.isActivationEvent(event, component)) {
 					double ignitionDelay = status.getMotorConfiguration().getMotorIgnitionDelay(id);
 					addEvent(new FlightEvent(FlightEvent.Type.IGNITION,
 							status.getSimulationTime() + ignitionDelay,
 							component, id));
 				}
 			}
 
 
 			// Check for stage separation event
 			for (int stageNo : status.getConfiguration().getActiveStages()) {
 				if (stageNo == 0)
 					continue;
 
 				Stage stage = (Stage) status.getConfiguration().getRocket().getChild(stageNo);
 				StageSeparationConfiguration separationConfig = stage.getFlightConfiguration(flightConfigurationId);
 				if ( separationConfig == null ) {
 					separationConfig = stage.getDefaultFlightConfiguration();
 				}
 				if (separationConfig.getSeparationEvent().isSeparationEvent(event, stage)) {
 					addEvent(new FlightEvent(FlightEvent.Type.STAGE_SEPARATION,
 							event.getTime() + separationConfig.getSeparationDelay(), stage));
 				}
 			}
 
 
 			// Check for recovery device deployment, add events to queue
 			Iterator<RocketComponent> rci = status.getConfiguration().iterator();
 			while (rci.hasNext()) {
 				RocketComponent c = rci.next();
 				if (!(c instanceof RecoveryDevice))
 					continue;
 				DeploymentConfiguration deployConfig = ((RecoveryDevice) c).getFlightConfiguration(flightConfigurationId);
 				if ( deployConfig == null ) {
 				   deployConfig = ((RecoveryDevice) c).getDefaultFlightConfiguration();
 				}
 				if (deployConfig.isActivationEvent(event, c)) {
 					// Delay event by at least 1ms to allow stage separation to occur first
 					addEvent(new FlightEvent(FlightEvent.Type.RECOVERY_DEVICE_DEPLOYMENT,
 							event.getTime() + Math.max(0.001, deployConfig.getDeployDelay()), c));
 				}
 			}
 
 
 			// Handle event
 			switch (event.getType()) {
 
 			case LAUNCH: {
 				status.getFlightData().addEvent(event);
 				break;
 			}
 
 			case IGNITION: {
 				// Ignite the motor
 				MotorId motorId = (MotorId) event.getData();
 				MotorInstanceConfiguration config = status.getMotorConfiguration();
 				config.setMotorIgnitionTime(motorId, event.getTime());
 				status.setMotorIgnited(true);
 				status.getFlightData().addEvent(event);
 
 				break;
 			}
 
 			case LIFTOFF: {
 				// Mark lift-off as occurred
 				status.setLiftoff(true);
 				status.getFlightData().addEvent(event);
 				break;
 			}
 
 			case LAUNCHROD: {
 				// Mark launch rod as cleared
 				status.setLaunchRodCleared(true);
 				status.getFlightData().addEvent(event);
 				break;
 			}
 
 			case BURNOUT: {
 				// If motor burnout occurs without lift-off, abort
 				if (!status.isLiftoff()) {
 					throw new SimulationLaunchException("Motor burnout without liftoff.");
 				}
 				// Add ejection charge event
 				String id = status.getConfiguration().getFlightConfigurationID();
 				MotorMount mount = (MotorMount) event.getSource();
 				double delay = mount.getMotorDelay(id);
 				if (delay != Motor.PLUGGED) {
 					addEvent(new FlightEvent(FlightEvent.Type.EJECTION_CHARGE, status.getSimulationTime() + delay,
 							event.getSource(), event.getData()));
 				}
 				status.getFlightData().addEvent(event);
 				break;
 			}
 
 			case EJECTION_CHARGE: {
 				status.getFlightData().addEvent(event);
 				break;
 			}
 
 			case STAGE_SEPARATION: {
 				// Record the event.
 				status.getFlightData().addEvent(event);
 
 				RocketComponent stage = event.getSource();
 				int n = stage.getStageNumber();
 
 				// Prepare the booster status for simulation.
 				SimulationStatus boosterStatus = new SimulationStatus(status);
 				boosterStatus.setFlightData(new FlightDataBranch( stage.getName(), FlightDataType.TYPE_TIME));
 				
 				stages.add(boosterStatus);
 				
 				// Mark the status as having dropped the booster
 				status.getConfiguration().setToStage(n - 1);
 
 				// Mark the booster status as only having the booster.
 				boosterStatus.getConfiguration().setOnlyStage(n);
 				break;
 			}
 
 			case APOGEE:
 				// Mark apogee as reached
 				status.setApogeeReached(true);
 				status.getFlightData().addEvent(event);
 				break;
 
 			case RECOVERY_DEVICE_DEPLOYMENT:
 				RocketComponent c = event.getSource();
 				int n = c.getStageNumber();
 				// Ignore event if stage not active
 				if (status.getConfiguration().isStageActive(n)) {
 					// TODO: HIGH: Check stage activeness for other events as well?
 
 					// Check whether any motor in the active stages is active anymore
 					for (MotorId motorId : status.getMotorConfiguration().getMotorIDs()) {
 						int stage = ((RocketComponent) status.getMotorConfiguration().
 								getMotorMount(motorId)).getStageNumber();
 						if (!status.getConfiguration().isStageActive(stage))
 							continue;
 						if (!status.getMotorConfiguration().getMotorInstance(motorId).isActive())
 							continue;
 						status.getWarnings().add(Warning.RECOVERY_DEPLOYMENT_WHILE_BURNING);
 					}
 
 					// Check for launch rod
 					if (!status.isLaunchRodCleared()) {
 						status.getWarnings().add(Warning.RECOVERY_LAUNCH_ROD);
 					}
 
 					// Check current velocity
 					if (status.getRocketVelocity().length() > 20) {
 						// TODO: LOW: Custom warning.
 						status.getWarnings().add(Warning.fromString(trans.get("Warning.RECOVERY_HIGH_SPEED") +
 								" ("
 								+ UnitGroup.UNITS_VELOCITY.toStringUnit(status.getRocketVelocity().length())
 								+ ")."));
 					}
 
 					status.setLiftoff(true);
 					status.getDeployedRecoveryDevices().add((RecoveryDevice) c);
 
 					this.currentStepper = this.landingStepper;
 					this.status = currentStepper.initialize(status);
 
 					status.getFlightData().addEvent(event);
 				}
 				break;
 
 			case GROUND_HIT:
 				status.getFlightData().addEvent(event);
 				break;
 
 			case SIMULATION_END:
 				ret = false;
 				status.getFlightData().addEvent(event);
 				break;
 
 			case ALTITUDE:
 				break;
 				
 			case TUMBLE:
 				this.currentStepper = this.tumbleStepper;
 				this.status = currentStepper.initialize(status);
 				status.getFlightData().addEvent(event);
 				break;
 			}
 
 		}
 
 
 		// If no motor has ignited, abort
 		if (!status.isMotorIgnited()) {
 			throw new MotorIgnitionException("No motors ignited.");
 		}
 
 		return ret;
 	}
 
 	/**
 	 * Add a flight event to the event queue unless a listener aborts adding it.
 	 *
 	 * @param event		the event to add to the queue.
 	 */
 	private void addEvent(FlightEvent event) throws SimulationException {
 		if (SimulationListenerHelper.fireAddFlightEvent(status, event)) {
 			status.getEventQueue().add(event);
 		}
 	}
 
 
 
 	/**
 	 * Return the next flight event to handle, or null if no more events should be handled.
 	 * This method jumps the simulation time forward in case no motors have been ignited.
 	 * The flight event is removed from the event queue.
 	 *
 	 * @return			the flight event to handle, or null
 	 */
 	private FlightEvent nextEvent() {
 		EventQueue queue = status.getEventQueue();
 		FlightEvent event = queue.peek();
 		if (event == null)
 			return null;
 
 		// Jump to event if no motors have been ignited
 		if (!status.isMotorIgnited() && event.getTime() > status.getSimulationTime()) {
 			status.setSimulationTime(event.getTime());
 		}
 		if (event.getTime() <= status.getSimulationTime()) {
 			return queue.poll();
 		} else {
 			return null;
 		}
 	}
 
 
 
 	private void checkNaN() throws SimulationException {
 		double d = 0;
 		boolean b = false;
 		d += status.getSimulationTime();
 		d += status.getPreviousTimeStep();
 		b |= status.getRocketPosition().isNaN();
 		b |= status.getRocketVelocity().isNaN();
 		b |= status.getRocketOrientationQuaternion().isNaN();
 		b |= status.getRocketRotationVelocity().isNaN();
 		d += status.getEffectiveLaunchRodLength();
 
 		if (Double.isNaN(d) || b) {
 			log.error("Simulation resulted in NaN value:" +
 					" simulationTime=" + status.getSimulationTime() +
 					" previousTimeStep=" + status.getPreviousTimeStep() +
 					" rocketPosition=" + status.getRocketPosition() +
 					" rocketVelocity=" + status.getRocketVelocity() +
 					" rocketOrientationQuaternion=" + status.getRocketOrientationQuaternion() +
 					" rocketRotationVelocity=" + status.getRocketRotationVelocity() +
 					" effectiveLaunchRodLength=" + status.getEffectiveLaunchRodLength());
 			throw new SimulationException("Simulation resulted in not-a-number (NaN) value, please report a bug.");
 		}
 	}
 
 
 }
