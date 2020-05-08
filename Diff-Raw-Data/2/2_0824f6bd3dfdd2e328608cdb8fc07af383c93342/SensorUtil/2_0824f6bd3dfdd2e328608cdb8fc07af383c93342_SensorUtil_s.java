 package org.concord.sensor.applet;
 
 import java.applet.Applet;
 import java.awt.EventQueue;
 import java.util.Arrays;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.concord.sensor.ExperimentConfig;
 import org.concord.sensor.SensorConfig;
 import org.concord.sensor.SensorRequest;
 import org.concord.sensor.applet.exception.ConfigureDeviceException;
 import org.concord.sensor.applet.exception.CreateDeviceException;
 import org.concord.sensor.applet.exception.SensorAppletException;
 import org.concord.sensor.device.SensorDevice;
 import org.concord.sensor.device.impl.DeviceConfigImpl;
 import org.concord.sensor.device.impl.DeviceID;
 import org.concord.sensor.device.impl.JavaDeviceFactory;
 import org.concord.sensor.device.impl.SensorConfigImpl;
 import org.concord.sensor.impl.ExperimentRequestImpl;
 import org.concord.sensor.impl.Range;
 import org.concord.sensor.impl.SensorRequestImpl;
 import org.concord.sensor.impl.SensorUtilJava;
 
 public class SensorUtil {
 	private static final Logger logger = Logger.getLogger(SensorUtil.class.getName());
 
 	private Applet applet;
 	private JavaDeviceFactory deviceFactory;
 	private SensorDevice device;
 	private ExperimentConfig actualConfig;
 	private boolean deviceIsRunning = false;
 	private boolean deviceIsAttached = false;
 	private boolean deviceIsCollectable = false;
 	private ScheduledExecutorService executor;
 	private ScheduledExecutorService jsBridgeExecutor;
 	private ScheduledFuture<?> collectionTask;
 
 	private String deviceType;
 
 	private SensorRequest[] sensors;
 	private SensorRequest[] configuredSensors;
 
 	public SensorUtil(Applet applet, String deviceType) {
 		this.applet = applet;
 		this.deviceType = deviceType;
 		this.deviceFactory = new JavaDeviceFactory();
 		this.executor = Executors.newSingleThreadScheduledExecutor();
 		this.jsBridgeExecutor = Executors.newSingleThreadScheduledExecutor();
 	}
 
 	public boolean isRunning() {
 		return deviceIsRunning;
 	}
 
 	public void stopDeviceError(final JavascriptDataBridge jsBridge) {
 		stopDevice();
 		// Figure out what went wrong...
 		// Do we still have a device?
 		try {
 			if (isDeviceAttached()) {
 				ExperimentConfig config = getDeviceConfig();
 				if (config == null) {
 					notifyDeviceUnplugged(jsBridge);
 					return;
 				}
 				if (config.getSensorConfigs() == null || config.getSensorConfigs().length != sensors.length) {
 					notifySensorUnplugged(jsBridge);
 					return;
 				}
 				// TODO See if we can figure out which sensor(s) got unplugged
 				System.out.println("Somehow we didn't detect a connection error!");
 				configureDevice(sensors, true);
 			} else {
 				notifyDeviceUnplugged(jsBridge);
 			}
 		} catch (ConfigureDeviceException e) {
 			notifyDeviceUnplugged(jsBridge);
 		}
 	}
 	
 	private void notifyDeviceUnplugged(final JavascriptDataBridge jsBridge) {
 		jsBridgeExecutor.schedule(new Runnable() {
 			public void run() {
 				System.err.println("Notifying device was unplugged.");
 				jsBridge.notifyDeviceUnplugged();
 			}
 		}, 0, TimeUnit.MILLISECONDS);
 	}
 	
 	private void notifySensorUnplugged(final JavascriptDataBridge jsBridge) {
 		jsBridgeExecutor.schedule(new Runnable() {
 			public void run() {
 				System.err.println("Notifying sensor was unplugged.");
 				jsBridge.notifySensorUnplugged();
 			}
 		}, 0, TimeUnit.MILLISECONDS);
 	}
 
 	public void stopDevice() {
 		if (device != null && deviceIsRunning) {
 			if (collectionTask != null && !collectionTask.isDone()) {
 				collectionTask.cancel(false);
 			}
 			collectionTask = null;
 			Runnable r = new Runnable() {
 				public void run() {
 					logger.fine("Stopping device: " + Thread.currentThread().getName());
 					deviceIsRunning = false;
 					device.stop(true);
 				}
 			};
 
 			if (!executeAndWait(r)) {
 				// try closing and re-opening the device
 				System.err.println("Stopping had errors! Closing and re-opening.");
 				tearDownDevice();
 			}
 		}
 	}
 
 	private void reopenDevice(boolean skipExecutor) {
 		Runnable r = new Runnable() {
 			public void run() {
 				device.close();
 				device.open(getOpenString(getDeviceId(deviceType)));
 			}
 		};
 		if (skipExecutor) {
 			r.run();
 		} else {
 			executeAndWait(r);
 		}
 	}
 
 	private int numErrors = 0;
 	public void startDevice(final JavascriptDataBridge jsBridge) throws CreateDeviceException, ConfigureDeviceException {
 		if (deviceIsRunning) { return; }
 
 		if (device == null) {
 			setupDevice(sensors);
 		}
 		
 		if (isDeviceAttached()) {
 			configureDevice(sensors, true);
 	
 			Runnable start = new Runnable() {
 				public void run() {
 					deviceIsRunning = device.start();
 					System.out.println("started device");
 				}
 			};
 			executor.schedule(start, 0, TimeUnit.MILLISECONDS);
 	
 			final float[] data = new float[1024];
 			Runnable r = new Runnable() {
 				public void run() {
 					try {
 						final int numSamples = device.read(data, 0, sensors.length, null);
 						if (numSamples > 0) {
 							final float[] dataCopy = new float[numSamples * sensors.length];
 							System.arraycopy(data, 0, dataCopy, 0, numSamples * sensors.length);
 							jsBridgeExecutor.schedule(new Runnable() {
 								public void run() {
 									jsBridge.handleData(numSamples, sensors.length, dataCopy);
 								}
 							}, 0, TimeUnit.MILLISECONDS);
 
 							numErrors = 0;
 						} else {
 							// some devices (ex: GoIO) report -1 samples to indicate an error, or
 							// will just report 0 samples continuously after being unplugged
 							numErrors++;
 						}
 					} catch (Exception e) {
 						numErrors++;
 						logger.log(Level.SEVERE, "Error reading data from device!", e);
 					}
 					if (numErrors >= 5) {
 						numErrors = 0;
 						logger.severe("Too many collection errors! Stopping device.");
 						EventQueue.invokeLater(new Runnable(){
 							public void run() {
 								stopDeviceError(jsBridge);
 							}
 						});
 					}
 				}
 			};
 			numErrors = 0;
 			double interval = Math.floor(actualConfig.getDataReadPeriod() * 1000);
 			collectionTask = executor.scheduleAtFixedRate(r, 10, (long) interval, TimeUnit.MILLISECONDS);
 		}
 	}
 
 	public float[] readSingleValue(JavascriptDataBridge jsBridge, boolean allAttachedSensors) throws SensorAppletException {
 		// TODO There's probably a more efficient way of doing this.
 		// GoIO devices, for instance, support one-shot data collection.
 		// Perhaps other devices do as well?
 		if (deviceIsRunning) { return null; }
 
 		if (device == null) {
 			setupDevice(null);
 		}
 
 		if (allAttachedSensors) {
 			configureDevice(null, false);
 		} else {
 			configureDevice(sensors, false);
 		}
 
 		int numSensorsTmp = 1;
 		if (allAttachedSensors || sensors == null) {
 			ExperimentConfig config = getDeviceConfig();
 			if (config == null) { return null; }
 			final SensorConfig[] configs = config.getSensorConfigs();
 			numSensorsTmp = configs.length;
 			if (configs == null || numSensorsTmp < 1) { return null; }
 		} else {
 			numSensorsTmp = sensors.length;
 		}
 		final int numSensors = numSensorsTmp;
 
 		Runnable start = new Runnable() {
 			public void run() {
 				deviceIsRunning = device.start();
 				System.out.println("started device");
 			}
 		};
 		executor.schedule(start, 0, TimeUnit.MILLISECONDS);
 
 		final float[] buffer = new float[1024];
 		final float[] data = new float[numSensorsTmp];
 		Runnable r = new Runnable() {
 			public void run() {
 				int numCollected = 0;
 				while (numErrors < 5 && numCollected < 1) {
 					try {
 						final int numSamples = device.read(buffer, 0, numSensors, null);
 						if (numSamples > 0) {
 							// read just the first value
 							synchronized (data) {
 								System.arraycopy(buffer, 0, data, 0, numSensors);
 							}
 							numCollected++;
 							numErrors = 0;
 						} else {
 							// some devices (ex: GoIO) report -1 samples to indicate an error, or
 							// will just report 0 samples continuously after being unplugged
 							numErrors++;
 						}
 					} catch (Exception e) {
 						numErrors++;
 						logger.log(Level.SEVERE, "Error reading data from device!", e);
 					}
 				}
 				if (numErrors >= 5) {
 					logger.severe("Too many collection errors while getting single value! Stopping device.");
 				}
 			}
 		};
 		try {
 			numErrors = 0;
 			executeAndWait(r);
 			synchronized (data) {
 				System.out.println("Sync");
 			}
 			return data;
 		} finally {
 			if (numErrors >= 5) {
 				stopDeviceError(jsBridge);
 			} else {
 			    stopDevice();
 			}
 		}
 	}
 
 	public void setupDevice(SensorRequest[] sensors) throws CreateDeviceException, ConfigureDeviceException {
 		this.sensors = sensors;
 
 		if (device == null) {
 			createDevice();
 		}
 
 		configureDevice(sensors, true);
 	}
 
 	private ExperimentConfig reportedConfig;
 	private long reportedConfigLoadedAt = 0;
 
 	public ExperimentConfig getDeviceConfig() throws ConfigureDeviceException {
 		reportedConfig = null;
 		if (device != null && (reportedConfig == null || (System.currentTimeMillis() - reportedConfigLoadedAt) > 1000)) {
 			Runnable r = new Runnable() {
 				public void run() {
 					logger.fine("Getting device config: " + Thread.currentThread().getName());
 					// Check what is attached, this isn't necessary if you know what you want
 					// to be attached. But sometimes you want the user to see what is attached
 					reportedConfig = device.getCurrentConfig();
 					reportedConfigLoadedAt = System.currentTimeMillis();
 				}
 			};
 
 			executeAndWaitConfigure(r);
 		}
 		return reportedConfig;
 	}
 
 	public boolean isDeviceAttached() {
 		if (device != null) {
 			Runnable r = new Runnable() {
 				public void run() {
 					logger.fine("Checking attached: " + Thread.currentThread().getName());
 					// TODO Auto-generated method stub
 					deviceIsAttached = device.isAttached();
 					if (!deviceIsAttached) {
 						// try re-opening the device
 						try {
 							reopenDevice(true);
 							deviceIsAttached = device.isAttached();
 						} catch (Exception e) {
 							deviceIsAttached = false;
 						}
 					}
 				}
 			};
 
 			executeAndWait(r);
 		} else {
 			logger.info("Device was null. Trying to open...");
 			try {
 				setupDevice(sensors);
 				deviceIsAttached = isDeviceAttached();
 			} catch (SensorAppletException e) {
 				deviceIsAttached = false;
 			}
 
 		}
 		return deviceIsAttached;
 	}
 
 	public boolean isCollectable() {
 		logger.fine("Checking for sensor interface: " + deviceType);
 		if (sensors != null && sensors.length > 0 && deviceIsCollectable) {
 			return deviceIsCollectable;
 		}
 		try {
 			if (device == null) {
 				setupDevice(null);
 			}
 			if (isDeviceAttached()) {
 				logger.fine("Device reported as attached.");
 				ExperimentConfig config = getDeviceConfig();
 				if (config != null) {
 					System.err.println("Interface is connected. Here's the config: ");
 					SensorUtilJava.printExperimentConfig(config);
 					deviceIsCollectable = true;
 					return deviceIsCollectable;
 				} else {
 					logger.fine("No config!");
 				}
 			} else {
 				logger.fine("device says not attached");
 			}
 		} catch (SensorAppletException e) {
 			//
 			e.printStackTrace();
 		}
 		deviceIsCollectable = false;
 		return deviceIsCollectable;
 	}
 
 	public void tearDownDevice() {
 		stopDevice();
 		if (device == null) {
 			return;
 		}
 		Runnable r = new Runnable() {
 			public void run() {
 				logger.fine("Closing device: " + Thread.currentThread().getName());
 				if (device != null) {
 					deviceFactory.destroyDevice(device);
 					device = null;
 					deviceIsRunning = false;
 					logger.fine("Device shut down");
 				}
 			}
 		};
 		executeAndWait(r);
 	}
 
 	public void destroy() {
 		tearDownDevice();
 		executor.shutdown();
 		try {
 			executor.awaitTermination(5, TimeUnit.SECONDS);
 			System.err.println("Shutdown completed. All tasks terminated: " + executor.isTerminated());
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		executor = null;
 		applet = null;
 		deviceFactory = null;
 	}
 
 	private void createDevice() throws CreateDeviceException {
 		Runnable r = new Runnable() {
 			public void run() {
 				logger.fine("Creating device: " + Thread.currentThread().getName());
 				int deviceId = getDeviceId(deviceType);
 				device = deviceFactory.createDevice(new DeviceConfigImpl(deviceId, getOpenString(deviceId)));
 			}
 		};
 
 		executeAndWaitCreate(r);
 	}
 
 	private void configureDevice(final SensorRequest[] sensors, boolean force) throws ConfigureDeviceException {
 		if ((!force) && configuredSensors == sensors) { return; }
 		Runnable r = new Runnable() {
 			public void run() {
 				logger.fine("Configuring device: " + Thread.currentThread().getName());
 				// Check what is attached, this isn't necessary if you know what you want
 				// to be attached.  But sometimes you want the user to see what is attached
 //				ExperimentConfig currentConfig = device.getCurrentConfig();
 //				System.out.println("Current sensor config:");
 //				if (currentConfig == null) {
 //					System.out.println("  IS NULL");
 //				} else {
 //					SensorUtilJava.printExperimentConfig(currentConfig);
 //				}
 
 
 				ExperimentRequestImpl request = new ExperimentRequestImpl();
 				if (!configureExperimentRequest(request, sensors)) {
 					System.out.println("Couldn't configure experiment request!");
 					return;
 				}
 
 				actualConfig = device.configure(request);
 				System.out.println("Config to be used:");
 				if (actualConfig == null) {
 					System.out.println("IS ALSO NULL <-- BAD!");
 					deviceIsCollectable = false;
 				} else {
 					SensorUtilJava.printExperimentConfig(actualConfig);
 					configuredSensors = sensors;
 					deviceIsCollectable = true;
 				}
 			}
 
 		};
 		executeAndWaitConfigure(r);
 	}
 
 	private boolean configureExperimentRequest(ExperimentRequestImpl experiment, SensorRequest[] sensors) {
 		float minPeriod = Float.MAX_VALUE;
 		if (sensors == null || sensors.length == 0) {
 			sensors = getSensorsFromCurrentConfig();
 			if (sensors == null || sensors.length == 0) {
 				return false;
 			}
 		}
 		for (SensorRequest sensor : sensors) {
 			float period = getPeriod(sensor);
 			if (period < minPeriod) {
 				minPeriod = period;
 			}
 		}
 		System.out.println("Configured min period: " + minPeriod);
 		experiment.setPeriod(minPeriod);
 		experiment.setNumberOfSamples(-1);
 
 		experiment.setSensorRequests(sensors);
 		return true;
 	}
 
 	// This should only be called from within the executor thread!!!
 	private SensorRequest[] getSensorsFromCurrentConfig() {
 		ExperimentConfig deviceConfig = device.getCurrentConfig();
 		if (deviceConfig == null || deviceConfig.getSensorConfigs() == null) {
 			return null;
 		}
 		SensorConfig[] configs = deviceConfig.getSensorConfigs();
 		SensorRequest[] reqs = new SensorRequest[configs.length];
 		for (int i = 0; i < configs.length; i++) {
 			SensorConfigImpl config = (SensorConfigImpl) configs[i];
 			SensorRequestImpl sensorReq = new SensorRequestImpl();
 			Range r = config.getValueRange();
 			if (r == null) { r = new Range(-10000f, 10000f); }
 			configureSensorRequest(sensorReq, 1, r.minimum, r.maximum, config.getPort(), config.getStepSize(), config.getType());
 			reqs[i] = sensorReq;
 		}
 		return reqs;
 	}
 
 	private float getPeriod(SensorRequest sensor) {
 		switch (sensor.getType()) {
 		case SensorConfig.QUANTITY_CO2_GAS:
 		case SensorConfig.QUANTITY_OXYGEN_GAS:
 			return 1.0f;
 		case SensorConfig.QUANTITY_FORCE:
 		case SensorConfig.QUANTITY_DISTANCE:
 			return 0.05f;
 		case SensorConfig.QUANTITY_TEMPERATURE:
 		case SensorConfig.QUANTITY_LIGHT:
 		case SensorConfig.QUANTITY_PH:
 		default:
 			return 0.1f;
 		}
 	}
 
 	private int getDeviceId(String id) {
 		logger.fine("Requested device of: " + id);
 		if (id.equals("golink") || id.equals("goio")) {
 			return DeviceID.VERNIER_GO_LINK_JNA;
 		} else if (id.equals("labquest")) {
 			return DeviceID.VERNIER_LAB_QUEST;
 		} else if (id.equals("manual")) {
 			try {
 				return Integer.parseInt(applet.getParameter("deviceId"));
 			} catch (NumberFormatException e) {
 				logger.severe("Invalid 'deviceId' param: " + applet.getParameter("deviceId"));
 			}
 		}
 		return DeviceID.PSEUDO_DEVICE;
 	}
 
 	private String getOpenString(int deviceId) {
 		switch (deviceId) {
 		case DeviceID.VERNIER_GO_LINK_JNA:
 		case DeviceID.VERNIER_LAB_QUEST:
 			return null;
 		default:
 			return applet.getParameter("openString");
 		}
 	}
 
 	public static SensorRequestImpl getSensorRequest(String type) {
 		type = type.toLowerCase();
 		SensorRequestImpl sensor = new SensorRequestImpl();
 
 		if (type.equals("light")) {
 			configureSensorRequest(sensor, 0, 0.0f, 4000.0f, 0, 0.1f, SensorConfig.QUANTITY_LIGHT);
 		} else if (type.equals("position") || type.equals("distance")) {
 			configureSensorRequest(sensor, -2, 0.0f, 4.0f, 0, 0.1f, SensorConfig.QUANTITY_DISTANCE);
 		} else if (type.equals("co2") || type.equals("carbon dioxide")) {
 			configureSensorRequest(sensor, 1, 0.0f, 5000.0f, 0, 20.0f, SensorConfig.QUANTITY_CO2_GAS);
 		} else if (type.equals("force") || type.equals("force 5n")) {
 			configureSensorRequest(sensor, -2, -4.0f, 4.0f, 0, 0.01f, SensorConfig.QUANTITY_FORCE);
 		} else if (type.equals("force 50n")) {
 			configureSensorRequest(sensor, -1, -40.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_FORCE);
 		} else if (type.equals("o2") || type.equals("oxygen")) {
 			configureSensorRequest(sensor, -2, 0.0f, 100.0f, 0, 0.01f, SensorConfig.QUANTITY_OXYGEN_GAS);
 		} else if (type.equals("ph")) {
 			configureSensorRequest(sensor, -1, 0.0f, 14.0f, 0, 0.1f, SensorConfig.QUANTITY_PH);
 		} else if (type.equals("manual")) {
 			// return an unconfigured sensor request
 		} else {
 			// fall back to temperature
 			configureSensorRequest(sensor, -1, 0.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_TEMPERATURE);
 		}
 
 		return sensor;
 	}
 
 	private static void configureSensorRequest(SensorRequestImpl sensor, int precision, float min, float max, int port, float step, int type) {
 		sensor.setDisplayPrecision(precision);
 		sensor.setRequiredMin(min);
 		sensor.setRequiredMax(max);
 		sensor.setPort(port);
 		sensor.setStepSize(step);
 		sensor.setType(type);
 	}
 
 	private void executeAndWaitCreate(Runnable r) throws CreateDeviceException {
 		ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 		try {
 			task.get();
 		} catch (InterruptedException e) {
 			throw new CreateDeviceException("Exception creating device", e);
 		} catch (ExecutionException e) {
 			throw new CreateDeviceException("Exception creating device", e.getCause());
 		} catch (IllegalMonitorStateException e) {
 			throw new CreateDeviceException("Exception creating device", e);
 		}
 	}
 
 	private void executeAndWaitConfigure(Runnable r) throws ConfigureDeviceException {
 		ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 		try {
 			task.get();
 		} catch (InterruptedException e) {
 			throw new ConfigureDeviceException("Exception configuring device", e);
 		} catch (ExecutionException e) {
 			throw new ConfigureDeviceException("Exception configuring device", e.getCause());
 		} catch (IllegalMonitorStateException e) {
 			throw new ConfigureDeviceException("Exception configuring device", e);
 		}
 	}
 
 	private boolean executeAndWait(final Runnable r) {
 		ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 		try {
 			task.get();
 			return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public boolean isActualConfigValid() {
 		if (reconfigureNextTime) {
 			reconfigureNextTime = false;
 			try {
 				configureDevice(sensors, true);
 			} catch (ConfigureDeviceException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				return false;
 			}
 		}
 		if (actualConfig == null) { return false; }
 		if (sensors == null) { return true; }
 		SensorConfig[] actuals = actualConfig.getSensorConfigs();
		if (actuals.length != sensors.length) { return false; }
 		int[] actualTypes = new int[actuals.length];
 		int[] reqTypes = new int[sensors.length];
 		for (int i = 0; i < actuals.length; i++) {
 			int aType = actuals[i].getType();
 			actualTypes[i] = aType;
 			int rType = sensors[i].getType();
 			reqTypes[i] = rType;
 			System.err.println("Recording types: " + aType + ", " + rType);
 		}
 		Arrays.sort(actualTypes);
 		Arrays.sort(reqTypes);
 		System.err.println("Comparing sensor arrays: " + Arrays.toString(actualTypes) + ", " + Arrays.toString(reqTypes));
 		return Arrays.equals(actualTypes, reqTypes);
 	}
 
 	private boolean reconfigureNextTime = false;
 	public void reconfigureNextTime() {
 		reconfigureNextTime = true;
 	}
 }
