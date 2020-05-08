 package org.concord.sensor.applet;
 
 import java.applet.Applet;
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
 import org.concord.sensor.impl.ExperimentRequestImpl;
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
 	private ScheduledFuture<?> collectionTask;
 
 	private String deviceType;
 
 	private SensorRequest[] sensors;
 
 	public SensorUtil(Applet applet, String deviceType) {
 		this.applet = applet;
 		this.deviceType = deviceType;
 		this.deviceFactory = new JavaDeviceFactory();
 		this.executor = Executors.newSingleThreadScheduledExecutor();
 	}
 
     public boolean isRunning() {
     	return deviceIsRunning;
     }
 	
 	public void stopDevice() {
		if (collectionTask != null && !collectionTask.isDone()) {
			collectionTask.cancel(false);
		}
		collectionTask = null;
		
 		if (device != null && deviceIsRunning) {
 			Runnable r = new Runnable() {
 				public void run() {
 					logger.info("Stopping device: " + Thread.currentThread().getName());
 					device.stop(true);
					System.out.println("stopped device");
 				}
 			};
 
 			ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 			try {
 				task.get();
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (ExecutionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}

		deviceIsRunning = false;
 	}
 
 	public void startDevice(final JavascriptDataBridge jsBridge) throws CreateDeviceException, ConfigureDeviceException {
 		if (deviceIsRunning) { return; }
 
 		if (device == null) {
 			setupDevice(sensors);
 		}
 		
 		configureDevice(sensors);
 
 		Runnable start = new Runnable() {
 			public void run() {
 				deviceIsRunning = device.start();		
 				System.out.println("started device");
 			}
 		};
 		executor.schedule(start, 0, TimeUnit.MILLISECONDS);
 
 		final float [] data = new float [1024];
 		Runnable r = new Runnable() {
 			private int numErrors = 0;
 			public void run() {
 				try {
 					final int numSamples = device.read(data, 0, sensors.length, null);
 					if(numSamples > 0) {
 						final float[] dataCopy = new float[numSamples*sensors.length];
 						System.arraycopy(data, 0, dataCopy, 0, numSamples*sensors.length);
 						executor.schedule(new Runnable() {
 							public void run() {
 								jsBridge.handleData(numSamples, sensors.length, dataCopy);
 							}
 						}, 0, TimeUnit.MILLISECONDS);
 					}
 					numErrors = 0;
 				} catch (Exception e) {
 					numErrors++;
 					logger.log(Level.SEVERE, "Error reading data from device!", e);
 				}
 				if (numErrors >= 5) {
 					logger.severe("Too many collection errors! Stopping device.");
 					stopDevice();
 				}
 			}
 		};
 		double interval = Math.floor(actualConfig.getDataReadPeriod()*1000);
 		collectionTask = executor.scheduleAtFixedRate(r, 10, (long)interval, TimeUnit.MILLISECONDS);
 	}
 
 	public void setupDevice(SensorRequest[] sensors) throws CreateDeviceException, ConfigureDeviceException {
 		this.sensors = sensors;
 		
 		if (device == null) {
 		  createDevice();
 		}
 		
 		if (sensors.length > 0) {
 		    configureDevice(sensors);
 		}
 	}
 	
 	private ExperimentConfig reportedConfig;
 	public ExperimentConfig getDeviceConfig() throws ConfigureDeviceException {
 		reportedConfig = null;
 		if (device != null) {
 			Runnable r = new Runnable() {
 				public void run() {
 					logger.info("Getting device config: " + Thread.currentThread().getName());
 					// Check what is attached, this isn't necessary if you know what you want
 					// to be attached.  But sometimes you want the user to see what is attached
 					reportedConfig = device.getCurrentConfig();
 				}
 			};
 
 			ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 			try {
 				task.get();
 			} catch (InterruptedException e) {
 				throw new ConfigureDeviceException("Exception configuring device", e);
 			} catch (ExecutionException e) {
 				throw new ConfigureDeviceException("Exception configuring device", e.getCause());
 			}
 		}
 		return reportedConfig;
 	}
 	
 	public boolean isDeviceAttached() {
 		if (device != null) {
 			Runnable r = new Runnable() {
 				public void run() {
 					logger.info("Checking attached: " + Thread.currentThread().getName());
 					// TODO Auto-generated method stub
 					deviceIsAttached = device.isAttached();
 				}
 			};
 
 			ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 			try {
 				task.get();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return deviceIsAttached;
 	}
 	
 	public boolean isCollectable() {
 		logger.info("Checking for sensor interface: " + deviceType);
 		if (sensors != null && sensors.length > 0 && deviceIsCollectable) {
 			return deviceIsCollectable;
 		}
 		try {
 			if (sensors == null || sensors.length == 0) {
 			    setupDevice(new SensorRequest[] {});
 			}
 			if (isDeviceAttached()) {
 				logger.info("Device reported as attached.");
 				ExperimentConfig config = getDeviceConfig();
 				if (config != null) {
 					logger.info("Interface is connected. Here's the config: ");
 					SensorUtilJava.printExperimentConfig(config);
 					deviceIsCollectable = true;
 					return deviceIsCollectable;
 				}
 			} else {
 				logger.info("device says not attached");
 			}
 		} catch (SensorAppletException e) {
 			//
 		}
 		deviceIsCollectable = false;
 		return deviceIsCollectable;
 	}
 
 	public void tearDownDevice() {
 		stopDevice();
 		Runnable r = new Runnable() {
 			public void run() {
 				logger.info("Closing device: " + Thread.currentThread().getName());
 				if(device != null){
 					device.close();
 					device = null;
 					logger.info("Device shut down");
 				}
 			}
 		};
 
 		ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 		try {
 			task.get();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void destroy() {
 		tearDownDevice();
 		executor.shutdown();
 		try {
 			executor.awaitTermination(5, TimeUnit.SECONDS);
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
 				logger.info("Creating device: " + Thread.currentThread().getName());
 				int deviceId = getDeviceId(deviceType);
 				device = deviceFactory.createDevice(new DeviceConfigImpl(deviceId, getOpenString(deviceId)));
 			}
 		};
 
 		ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 		try {
 			task.get();
 		} catch (InterruptedException e) {
 			throw new CreateDeviceException("Exception creating device", e);
 		} catch (ExecutionException e) {
 			throw new CreateDeviceException("Exception creating device", e.getCause());
 		}
 	}
 
 	private void configureDevice(final SensorRequest[] sensors) throws ConfigureDeviceException {
 		Runnable r = new Runnable() {
 			public void run() {
 				logger.info("Configuring device: " + Thread.currentThread().getName());
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
 				configureExperimentRequest(request, sensors);
 
 				request.setSensorRequests(sensors);
 
 				actualConfig = device.configure(request);
 				System.out.println("Config to be used:");
 				if (actualConfig == null) {
 					System.out.println("IS ALSO NULL <-- BAD!");
 				} else {
 					SensorUtilJava.printExperimentConfig(actualConfig);
 				}
 			}
 
 		};
 
 		ScheduledFuture<?> task = executor.schedule(r, 0, TimeUnit.MILLISECONDS);
 		try {
 			task.get();
 		} catch (InterruptedException e) {
 			throw new ConfigureDeviceException("Exception configuring device", e);
 		} catch (ExecutionException e) {
 			throw new ConfigureDeviceException("Exception configuring device", e.getCause());
 		}
 	}
 
 	private void configureExperimentRequest(ExperimentRequestImpl experiment, SensorRequest[] sensors) {
 		float minPeriod = Float.MAX_VALUE;
 		for (SensorRequest sensor : sensors) {
 			float period = getPeriod(sensor);
 			if (period < minPeriod) {
 				minPeriod = period;
 			}
 		}
 		experiment.setPeriod(minPeriod);
 		experiment.setNumberOfSamples(-1);
 	}
 	
 	private float getPeriod(SensorRequest sensor) {
 		switch (sensor.getType()) {
 		case SensorConfig.QUANTITY_CO2_GAS:
 		case SensorConfig.QUANTITY_OXYGEN_GAS:
 			return 1.0f;
 		case SensorConfig.QUANTITY_FORCE:
 			return 0.01f;
 		case SensorConfig.QUANTITY_TEMPERATURE:
 		case SensorConfig.QUANTITY_DISTANCE:
 		case SensorConfig.QUANTITY_LIGHT:
 		case SensorConfig.QUANTITY_PH:
 		default:
 			return 0.1f;
 		}
 	}
 
 	private int getDeviceId(String id) {
 		logger.info("Requested device of: " + id);
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
 }
