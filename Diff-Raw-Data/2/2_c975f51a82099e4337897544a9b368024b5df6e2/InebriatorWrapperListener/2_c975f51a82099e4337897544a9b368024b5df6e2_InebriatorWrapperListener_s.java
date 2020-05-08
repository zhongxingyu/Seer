 package com.inebriator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.ws.rs.core.UriBuilder;
 
 import org.apache.commons.lang3.StringUtils;
 import org.glassfish.grizzly.http.server.HttpServer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.tanukisoftware.wrapper.WrapperListener;
 import org.tanukisoftware.wrapper.WrapperManager;
 
 import com.sleepycat.je.Database;
 import com.sleepycat.je.DatabaseConfig;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.EnvironmentConfig;
 import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
 import com.sun.jersey.api.core.PackagesResourceConfig;
 import com.sun.jersey.api.core.ResourceConfig;
 
 public class InebriatorWrapperListener implements WrapperListener {
 
 	private static final Logger LOG = LoggerFactory.getLogger(InebriatorWrapperListener.class);
 
 	private static final String CONFIG_PATH = "inebriator.properties";
 	private static final String HTTP_LISTEN_PORT_PROP_NAME = "inebriator.http.listen.port";
	private static final String SOLENOID_CONTROLLER_IMPL_PROP_NAME = "inebriator.solenoid.controller.impl";
 	private static final String POUR_MILLIS_PER_UNIT_PROP_NAME = "inebriator.pour.millis.per.unit";
 	private static final String AIR_FLUSH_DURATION_MILLIS_PROP_NAME = "inebriator.flush.air.duration";
 	private static final String WATER_FLUSH_DURATION_MILLIS_PROP_NAME = "inebriator.flush.water.duration";
 	private static final String SOLENOID_DEF_PREFIX = "inebriator.solenoid.";
 	private static final String PHIDGET_SERIAL_NUM_PREFIX = "inebriator.phidget.serial.";
 
 	// Jersey is shite. It doesn't want to let me inject stuff into Resources
 	public static Inebriator inebriator;
 
 	private Properties inebriatorProperties;
 	private SolenoidController solenoidController;
 	private Environment environment;
 	private Database drinksDb;
 	private int httpListenPort;
 	private Map<String, Solenoid> solenoidsByName;
 	private long pourMillisPerUnit;
 	private long airFlushDurationMillis;
 	private long waterFlushDurationMillis;
 
 	public static void main(String[] args) throws Exception {
 		WrapperManager.start(new InebriatorWrapperListener(), args);
 		LOG.info("Inebriator service is online.");
 		Thread.sleep(99999999999L);
 		LOG.warn("Inebriator service is exiting.");
 	}
 
 	@Override
 	public void controlEvent(int signal) {
 		LOG.info("Received signal {}, ignoring", signal);
 	}
 
 	@Override
 	public Integer start(String[] args) {
 		try {
 			this.inebriatorProperties = loadPropertiesFromClasspath(CONFIG_PATH);
 			this.solenoidController = getSolenoidController(inebriatorProperties);
 			this.environment = openEnvironment();
 			this.drinksDb = openDatabase();
 			this.httpListenPort = (int) getRequiredLongValue(inebriatorProperties, HTTP_LISTEN_PORT_PROP_NAME);
 			this.solenoidsByName = getSolenoidsByName(inebriatorProperties);
 			this.pourMillisPerUnit = getRequiredLongValue(inebriatorProperties, POUR_MILLIS_PER_UNIT_PROP_NAME);
 			this.airFlushDurationMillis = getRequiredLongValue(inebriatorProperties, AIR_FLUSH_DURATION_MILLIS_PROP_NAME);
 			this.waterFlushDurationMillis = getRequiredLongValue(inebriatorProperties, WATER_FLUSH_DURATION_MILLIS_PROP_NAME);
 			inebriator = new Inebriator(solenoidController, drinksDb, solenoidsByName, pourMillisPerUnit, airFlushDurationMillis, waterFlushDurationMillis);
 			URI baseUri = UriBuilder.fromUri("http://localhost/").port(httpListenPort).build();
 			Map<String, String> opts = new HashMap<String, String>();
 			opts.put("com.sun.jersey.config.property.packages", "com.inebriator.resources");
 			ResourceConfig resourceConfig = new PackagesResourceConfig("com.inebriator.resources");
 			HttpServer httpServer = GrizzlyServerFactory.createHttpServer(baseUri, resourceConfig);
 			httpServer.start();
 		} catch (Exception e) {
 			LOG.error("Startup failed", e);
 			return 1;
 		}
 
 //		tmpIntTest();
 		
 		return null;
 	}
 
 	@Override
 	public int stop(int exitCode) {
 
 		try {
 			solenoidController.disconnect();
 		} catch (Exception e) {
 			LOG.error("Error disconnecting from solenoid controller", e);
 			exitCode = exitCode == 0 ? 1 : exitCode;
 		}
 		
 		try {
 			drinksDb.close();
 		} catch (Exception e) {
 			LOG.error("Error closing drinks DB", e);
 			exitCode = exitCode == 0 ? 1 : exitCode;
 		}
 		
 		try {
 			environment.close();
 		} catch (Exception e) {
 			LOG.error("Error closing BDB environment", e);
 			exitCode = exitCode == 0 ? 1 : exitCode;
 		}
 
 		return exitCode;
 	}
 
 	private SolenoidController getSolenoidController(Properties properties) {
 		List<Integer> serialNumbers = new ArrayList<Integer>();
 		int index = 0;
 
 		for (;;) {
 			String s = properties.getProperty(PHIDGET_SERIAL_NUM_PREFIX + index);
 			if (StringUtils.isBlank(s)) {
 				break;
 			}
 
 			Integer serialNumber = Integer.parseInt(s);
 			serialNumbers.add(serialNumber);
 			index++;
 		}
 
 		String solenoidControllerImplType = getRequiredStringValue(properties, SOLENOID_CONTROLLER_IMPL_PROP_NAME);
 		SolenoidController controller;
 
 		if (solenoidControllerImplType.equals("mock")) {
 			LOG.warn("Using the mock solenoid controller -- no phidget commands will be sent");
 			controller = new MockSolenoidController();
 		} else if (solenoidControllerImplType.equals("phidget")) {
 			controller = new PhidgetSolenoidController(serialNumbers.toArray(new Integer[0]));
 		} else {
 			throw new RuntimeException("Unsupported " + SOLENOID_CONTROLLER_IMPL_PROP_NAME + " " + solenoidControllerImplType);
 		}
 
 		return controller;
 	}
 
 	private Environment openEnvironment() {
 		EnvironmentConfig environmentConfig = new EnvironmentConfig();
 		environmentConfig.setAllowCreate(true);
 		environmentConfig.setReadOnly(false);
 		File file = new File("../drinksdb");
 		return new Environment(file, environmentConfig);
 	}
 
 	private Database openDatabase() {
 		DatabaseConfig databaseConfig = new DatabaseConfig();
 		databaseConfig.setAllowCreate(true);
 		databaseConfig.setReadOnly(false);
 		databaseConfig.setTransactional(false);
 		
 		return environment.openDatabase(null, "DrinksDB", databaseConfig);
 	}
 
 	private static Properties loadPropertiesFromClasspath(String propFileName) throws IOException {
 		Properties props = new Properties();
 		InputStream inputStream = Inebriator.class.getClassLoader().getResourceAsStream(propFileName);
 
 		if (inputStream == null) {
 			throw new FileNotFoundException("Can't find file [" + propFileName + "] on the classpath");
 		}
 
 		props.load(inputStream);
 
 		LOG.debug("Loaded properties: {}", props);
 		return props;
 	}
 
 	private static String getRequiredStringValue(Properties properties, String name) {
 		String stringValue = properties.getProperty(name);
 
 		if (StringUtils.isBlank(stringValue)) {
 			throw new RuntimeException("Missing required property [" + name + "]");
 		}
 
 		return stringValue;
 	}
 
 	private static long getRequiredLongValue(Properties properties, String name) {
 		String stringValue = getRequiredStringValue(properties, name);
 		long intValue;
 
 		try {
 			intValue = Long.valueOf(stringValue);
 		} catch (NumberFormatException e) {
 			throw new RuntimeException("Can't parse [" + name + "] = [" + stringValue + "] as an integer");
 		}
 		
 		return intValue;
 	}
 	
 	private static Map<String, Solenoid> getSolenoidsByName(Properties properties) {
 		Map<String, Solenoid> solenoids = new HashMap<String, Solenoid>();
 
 		for (Object o : properties.keySet()) {
 			String key = (String) o;
 			if (key.startsWith(SOLENOID_DEF_PREFIX)) {
 				String name = key.replace(SOLENOID_DEF_PREFIX, "");
 				Solenoid solenoid = new Solenoid(properties.getProperty(key));
 				solenoids.put(name, solenoid);
 			}
 		}
 		
 		LOG.debug("Loaded solenoids: {}", solenoids);
 		return solenoids;
 	}
 }
