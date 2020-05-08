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
 	private ScheduledExecutorService executor;
 	private ScheduledFuture<?> collectionTask;
 
 	public SensorUtil(Applet applet) {
 		this.applet = applet;
 		this.deviceFactory = new JavaDeviceFactory();
 		this.executor = Executors.newSingleThreadScheduledExecutor();
 	}
 
 
 	public void stopDevice() {
 		if (device != null && deviceIsRunning) {
 			if (!collectionTask.isDone()) {
 				collectionTask.cancel(false);
 			}
 			collectionTask = null;
 			Runnable r = new Runnable() {
 				public void run() {
 					device.stop(true);
 					deviceIsRunning = false;
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
 	}
 
 	public void startDevice(final JavascriptDataBridge jsBridge) {
 		if (deviceIsRunning) { return; }
 
 		if (device == null) {
 			setupDevice();
 		}
 		
 		configureDevice();
 
 		Runnable start = new Runnable() {
 			public void run() {
 				deviceIsRunning = device.start();		
 				System.out.println("started device");
 			}
 		};
 		executor.schedule(start, 0, TimeUnit.MILLISECONDS);
 
 		final float [] data = new float [1024];
 		Runnable r = new Runnable() {
 			public void run() {
 				try {
 					final int numSamples = device.read(data, 0, 1, null);
 					if(numSamples > 0) {
 						final float[] dataCopy = Arrays.copyOfRange(data, 0, numSamples);
 						executor.schedule(new Runnable() {
 							public void run() {
 								jsBridge.handleData(numSamples, dataCopy);
 							}
 						}, 0, TimeUnit.MILLISECONDS);
 					}
 				} catch (Exception e) {
 					logger.log(Level.SEVERE, "Error reading data from device!", e);
 				}
 			}
 		};
 		double interval = Math.floor(actualConfig.getDataReadPeriod()*1000);
 		collectionTask = executor.scheduleAtFixedRate(r, 10, (long)interval, TimeUnit.MILLISECONDS);
 	}
 
 	public void setupDevice() {
 		tearDownDevice();
 
 		createDevice();
 		configureDevice();
 	}
 
 	public void tearDownDevice() {
 		Runnable r = new Runnable() {
 			public void run() {
 				if(device != null){
 					device.close();
 					device = null;
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
 
 	private void createDevice() {
 		Runnable r = new Runnable() {
 			public void run() {
 				int deviceId = getDeviceId();
 				device = deviceFactory.createDevice(new DeviceConfigImpl(deviceId, getOpenString(deviceId)));
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
 
 	private void configureDevice() {
 		Runnable r = new Runnable() {
 			public void run() {
 				// Check what is attached, this isn't necessary if you know what you want
 				// to be attached.  But sometimes you want the user to see what is attached
 				ExperimentConfig currentConfig = device.getCurrentConfig();
 				System.out.println("Current sensor config:");
 				if (currentConfig == null) {
 					System.out.println("  IS NULL");
 				} else {
 					SensorUtilJava.printExperimentConfig(currentConfig);
 				} 
 
 
 				ExperimentRequestImpl request = new ExperimentRequestImpl();
 
 				SensorRequest sensor = getSensorRequest(request);
 
 				request.setSensorRequests(new SensorRequest [] { sensor });
 
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
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	private int getDeviceId() {
 		String id = applet.getParameter("device");
 		logger.info("Got device of: " + id);
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
 
 	private SensorRequest getSensorRequest(ExperimentRequestImpl experiment) {
 		String type = applet.getParameter("probeType");
 		logger.info("Got probeType of: " + type);
 		if (type == null) { type = "temperature"; }
 		type = type.toLowerCase();
 
 		SensorRequestImpl sensor = new SensorRequestImpl();
 
 		if (type.equals("light")) {
 			experiment.setPeriod(0.1f);
 			configureSensorRequest(sensor, 0, 0.0f, 4000.0f, 0, 0.1f, SensorConfig.QUANTITY_LIGHT);
 		} else if (type.equals("position") || type.equals("distance")) {
 			experiment.setPeriod(0.1f);
 			configureSensorRequest(sensor, -2, 0.0f, 4.0f, 0, 0.1f, SensorConfig.QUANTITY_DISTANCE);
 		} else if (type.equals("co2")) {
 			experiment.setPeriod(1.0f);
 			configureSensorRequest(sensor, 1, 0.0f, 5000.0f, 0, 20.0f, SensorConfig.QUANTITY_CO2_GAS);
 		} else if (type.equals("force") || type.equals("force 5n")) {
 			experiment.setPeriod(0.01f);
 			configureSensorRequest(sensor, -2, -4.0f, 4.0f, 0, 0.01f, SensorConfig.QUANTITY_FORCE);
 		} else if (type.equals("force 50n")) {
 			experiment.setPeriod(0.01f);
 			configureSensorRequest(sensor, -1, -40.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_FORCE);
 		} else if (type.equals("manual")) {
 			try {
 				experiment.setPeriod(Float.parseFloat(applet.getParameter("period")));
 				configureSensorRequest(sensor,
 						Integer.parseInt(applet.getParameter("precision")),
 						Float.parseFloat(applet.getParameter("min")),
 						Float.parseFloat(applet.getParameter("max")),
 						Integer.parseInt(applet.getParameter("sensorPort")),
 						Float.parseFloat(applet.getParameter("stepSize")),
 						Integer.parseInt(applet.getParameter("sensorType"))
 						);
 			} catch (NumberFormatException e) {
 				logger.severe("One or more manual configuration params was incorrect or unspecified!\n" + 
 						"period: " + applet.getParameter("period") + "\n" + 
 						"precision: " + applet.getParameter("precision") + "\n" + 
 						"min: " + applet.getParameter("min") + "\n" + 
 						"max: " + applet.getParameter("max") + "\n" + 
 						"sensorPort: " + applet.getParameter("sensorPort") + "\n" + 
 						"stepSize: " + applet.getParameter("stepSize") + "\n" + 
 						"sensorType: " + applet.getParameter("sensorType")
 						);
 				// fall back to temperature
 				experiment.setPeriod(0.1f);
 				configureSensorRequest(sensor, -1, 0.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_TEMPERATURE);
 			}
 		} else {
 			// fall back to temperature
 			experiment.setPeriod(0.1f);
 			configureSensorRequest(sensor, -1, 0.0f, 40.0f, 0, 0.1f, SensorConfig.QUANTITY_TEMPERATURE);
 		}
 		experiment.setNumberOfSamples(-1);
 
 		return sensor;
 	}
 
 	private void configureSensorRequest(SensorRequestImpl sensor, int precision, float min, float max, int port, float step, int type) {
 		sensor.setDisplayPrecision(precision);
 		sensor.setRequiredMin(min);
 		sensor.setRequiredMax(max);
 		sensor.setPort(port);
 		sensor.setStepSize(step);
 		sensor.setType(type);
 	}
 }
