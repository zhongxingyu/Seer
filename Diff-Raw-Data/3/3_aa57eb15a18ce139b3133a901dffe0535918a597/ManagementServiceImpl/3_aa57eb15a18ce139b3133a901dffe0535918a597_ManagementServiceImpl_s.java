 package de.tum.in.sonar.collector.server;
 
 import java.io.IOException;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import redis.clients.jedis.BinaryJedis;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 import redis.clients.jedis.JedisPoolConfig;
 import redis.clients.util.SafeEncoder;
 import de.tum.in.sonar.collector.BundledSensorConfiguration;
 import de.tum.in.sonar.collector.LogMessage;
 import de.tum.in.sonar.collector.LogsQuery;
 import de.tum.in.sonar.collector.ManagementService;
 import de.tum.in.sonar.collector.Parameter;
 import de.tum.in.sonar.collector.SensorConfiguration;
 import de.tum.in.sonar.collector.TimeSeriesQuery;
 import de.tum.in.sonar.collector.TransferableTimeSeriesPoint;
 import de.tum.in.sonar.collector.log.LogDatabase;
 import de.tum.in.sonar.collector.tsdb.InvalidLabelException;
 import de.tum.in.sonar.collector.tsdb.Query;
 import de.tum.in.sonar.collector.tsdb.QueryException;
 import de.tum.in.sonar.collector.tsdb.TimeSeries;
 import de.tum.in.sonar.collector.tsdb.TimeSeriesDatabase;
 import de.tum.in.sonar.collector.tsdb.TimeSeriesPoint;
 import de.tum.in.sonar.collector.tsdb.UnresolvableException;
 
 public class ManagementServiceImpl implements ManagementService.Iface {
 
 	private static final Logger logger = LoggerFactory.getLogger(ManagementServiceImpl.class);
 
 	private TimeSeriesDatabase tsdb;
 
 	private JedisPool jedisPool;
 
 	private LogDatabase logdb;
 
 	private SensorlistCache sensorlistCache = new SensorlistCache();
 
 	public ManagementServiceImpl() {
 		JedisPoolConfig config = new JedisPoolConfig();
 		config.setMaxActive(200);
 		config.setMinIdle(10);
 		config.setTestOnBorrow(false);
 
 		this.jedisPool = new JedisPool(config, getRedisServer());
 	}
 
 	@Override
 	public List<TransferableTimeSeriesPoint> query(TimeSeriesQuery query) throws TException {
 
 		Query tsdbQuery = new Query(query.getSensor(), query.getStartTime(), query.getStopTime());
 		tsdbQuery.setHostname(query.hostname);
 		try {
 			TimeSeries timeSeries = tsdb.run(tsdbQuery);
 			List<TransferableTimeSeriesPoint> tsPoints = new ArrayList<TransferableTimeSeriesPoint>(100);
 
 			for (TimeSeriesPoint point : timeSeries) {
 				TransferableTimeSeriesPoint tsPoint = new TransferableTimeSeriesPoint();
 				tsPoints.add(tsPoint);
 
 				tsPoint.setTimestamp(point.getTimestamp());
 				tsPoint.setValue(point.getValue());
 
 				if (point.getLabels() != null) {
 					Set<String> labels = new HashSet<String>();
 					Collections.addAll(labels, point.getLabels());
 					tsPoint.setLabels(labels);
 				}
 			}
 
 			return tsPoints;
 
 		} catch (QueryException e) {
 			logger.error("Error while executing query", e);
 		} catch (UnresolvableException e) {
 			logger.trace("Error while mapping in query", e);
 		}
 
 		return new ArrayList<TransferableTimeSeriesPoint>();
 	}
 
 	// Redis Key Layout:
 	// :sensors -> [] - set of sensor names
 	// :hosts -> [] - set of hostnames
 	//
 	// :sensor:[name]:binary - binary for the sensor
 	// :sensor:[name]:md5 - MD5 value of the binary
 	// :sensor:[name]:config:[item] - configuration for the sensor
 	// :sensor:[name]:labels -> [] - set of labels
 	//
 	// :host:[hostname]:labels -> [] - set of labels
 	// :host:[hostname]:extends -> other hostname - reference another host
 	// :host:[hostname]:sensor:[sensorname] - enables or disables a sensor
 	// :host:[hostname]:sensor:[sensorname]/config:[item] - overrides a sensor
 	// configuration
 	// TODO: add inheritence to the documentation
 
 	private String key(String... args) {
 		StringBuilder builder = new StringBuilder();
 
 		for (int i = 0; i < args.length; i++) {
 			builder.append(args[i]);
 			if ((i + 1) < args.length)
 				builder.append(":");
 		}
 
 		return builder.toString();
 	}
 
 	private final String getRedisServer() {
 		Properties prop = new Properties();
 		try {
 			prop.load(ManagementServiceImpl.class.getResourceAsStream("/redis.properties"));
 			String server = (String) prop.get("server");
 			return server;
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		return null;
 	}
 
 	@Override
 	public ByteBuffer fetchSensor(String name) throws TException {
 		logger.debug("client fetches the sensor: " + name);
 
 		String key = key("sensor", name, "binary");
 		BinaryJedis jedis = null;
 		try {
 			jedis = new BinaryJedis(getRedisServer());
 			if (!jedis.exists(SafeEncoder.encode(key)))
 				return ByteBuffer.wrap(new byte[] {});
 
 			byte[] data = jedis.get(SafeEncoder.encode(key));
 			return ByteBuffer.wrap(data);
 		} finally {
 			if (jedis != null)
 				jedis.disconnect();
 		}
 	}
 
 	@Override
 	public void delSensor(String name) throws TException {
 		logger.debug("removing sensor: " + name);
 		Jedis jedis = jedisPool.getResource();
 
 		try {
 			// Remove from the sensors set
 			logger.debug("removing from sensors list");
 			jedis.srem("sensors", name);
 
 			// Remove all keys from the sensor segment
 			String query = key("sensor", name);
 			Set<String> keys = jedis.keys(query + ":*");
 			for (String key : keys) {
 				logger.debug("removing key: " + key);
 				jedis.del(key);
 			}
 
 			// Remove the sensor from all the host configurations
 			query = "host:" + "*" + ":sensor:" + name + ":" + "*";
 			keys = jedis.keys(query + ":*");
 			for (String key : keys) {
 				logger.debug("removing key: " + key);
 				jedis.del(key);
 			}
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public String sensorHash(String name) throws TException {
 		logger.debug("sensor hash for " + name);
 		Jedis jedis = jedisPool.getResource();
 
 		try {
 			String key = key("sensor", name, "md5");
 			String md5 = "";
 			if (jedis.exists(key)) {
 				md5 = jedis.get(key);
 				logger.debug("returning sensor hash: " + md5);
 			}
 
 			return md5;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void deploySensor(String name, ByteBuffer binary) throws TException {
 		logger.debug("deploying sensor " + name);
 		Jedis jedis = jedisPool.getResource();
 
 		try {
 			// Add sensor the the sensor set
 			jedis.sadd("sensors", name);
 
 			// Add sensor binary
 			String key = key("sensor", name, "binary");
 			jedis.set(SafeEncoder.encode(key), binary.array());
 
 			// Set MD5
 			try {
 				MessageDigest md = MessageDigest.getInstance("MD5");
 				byte[] md5 = md.digest(binary.array());
 				BigInteger bigInt = new BigInteger(1, md5);
 				String smd5 = bigInt.toString(16);
 				logger.debug("deploy md5: " + smd5);
 
 				key = key("sensor", name, "md5");
 				jedis.set(key, smd5);
 			} catch (NoSuchAlgorithmException e) {
 				logger.error("Could not create MD5 for sensor binary", e);
 			}
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void setSensorLabels(String name, Set<String> labels) throws TException {
 		logger.debug("setting sensor labels: " + name);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			// Add labels to the key
 			String key = key("sensor", name, "labels");
 			jedis.sadd(key, labels.toArray(new String[] {}));
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void addHost(String hostname) throws TException {
 		logger.debug("add host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			// Add hostname to the hosts list
 			String key = key("hosts");
 			if (!jedis.sismember(key, hostname))
 				jedis.sadd(key, hostname);
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void delHost(String hostname) throws TException {
 		logger.debug("removing host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			// Remove from the hosts set
 			logger.debug("removing from hosts list");
 			jedis.srem("hosts", hostname);
 
 			// Remove all keys from the host segment
 			String query = key("host", hostname);
 			Set<String> keys = jedis.keys(query + ":*");
 			for (String key : keys) {
 				logger.debug("removing key: " + key);
 				jedis.del(key);
 			}
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void addHostExtension(String hostname, String virtualHostName) throws TException {
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("host", hostname, "extends");
 
 			// TODO: This has to be null instead of '-1'
 			// If extends host is not selected -1 is passed and the reference is removed
 			if (virtualHostName.equalsIgnoreCase("-1"))
 				jedis.del(key);
 			else
 				jedis.set(key, virtualHostName);
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public String getHostExtension(String hostname) throws TException {
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("host", hostname, "extends");
 			if (jedis.exists(key))
 				return jedis.get(key);
 
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 
 		return null;
 	}
 
 	@Override
 	public void setHostLabels(String hostname, Set<String> labels) throws TException {
 		logger.debug("Setting labels for host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("host", hostname, "labels");
 			if (jedis.exists(key))
 				jedis.del(key);
 			jedis.sadd(key, labels.toArray(new String[] {}));
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public Set<String> getLabels(String hostname) throws TException {
 		logger.debug("reading labels for host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			// Get all the labels in the key
 			String key = key("host", hostname, "labels");
 			if (!jedis.exists(key))
 				return Collections.emptySet();
 
 			Set<String> labels = jedis.smembers(key);
 			return labels;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void setSensor(String hostname, String sensor, boolean activate) throws TException {
 		logger.debug("set sensor: " + sensor + " for th host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			// Update the sensor setting
 			String key = key("host", hostname, "sensor", sensor);
 			jedis.set(key, Boolean.toString(activate));
 
 			jedis.save();
 
 			// Invalidate cache
 			sensorlistCache.invalidateAll();
 
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public Set<String> getAllSensors() throws TException {
 		logger.debug("get all sensors");
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("sensors");
 			if (!jedis.exists(key))
 				return Collections.emptySet();
 
 			Set<String> sensors = jedis.smembers(key);
 			return sensors;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public boolean hasBinary(String sensor) throws TException {
 		logger.debug("ask for sensor binary: " + sensor);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("sensor", sensor, "binary");
 			if (!jedis.exists(key))
 				return false;
 
 			byte[] data = jedis.get(SafeEncoder.encode(key));
 			boolean available = data.length > 64;
 			return available;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public Set<String> getSensors(String hostname) throws TException {
 		logger.debug("get all sensors for host: " + hostname);
 
 		// Check cache
 		if (sensorlistCache.get(hostname) != null)
 			return sensorlistCache.get(hostname);
 
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("host", hostname, "sensor");
 			String query = key + ":*";
 			logger.debug("finding sensors with query: " + query);
 			Set<String> sensorKeys = jedis.keys(query);
 
 			Set<String> sensors = new HashSet<String>();
 			for (String sensorKey : sensorKeys) {
 				// Check state of the sensor
 				String value = jedis.get(sensorKey);
 				logger.debug("checking sensor key: " + sensorKey + " - " + value);
 
 				if (value.equals("true")) {
 					String sensor = sensorKey.replaceFirst(key + ":", "");
 					logger.debug("sensor found: " + sensor);
 					sensors.add(sensor);
 				}
 			}
 
 			// Get the sensors from extensions
 			String extendedKey = key("host", hostname, "extends");
 			if (jedis.exists(extendedKey)) {
 				String virtualHost = jedis.get(extendedKey);
 				sensors.addAll(getSensors(virtualHost));
 			}
 
 			// Add data to cache
 			sensorlistCache.put(hostname, sensors);
 
 			return sensors;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public Set<String> getSensorLabels(String sensor) throws TException {
 		logger.debug("get sensor labels: " + sensor);
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("sensor", sensor, "labels");
 			if (!jedis.exists(key))
 				return Collections.emptySet();
 
 			Set<String> labels = jedis.smembers(key);
 			return labels;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public void setSensorConfiguration(String sensor, SensorConfiguration configuration) throws TException {
 		logger.debug("setting sensor configuration: " + sensor);
 
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("sensor", sensor, "config");
 
 			if (configuration.getInterval() != 0)
 				jedis.set(key(key, "interval"), Long.toString(configuration.getInterval()));
 
 			// if None is selected in the GUI
 			// TODO: Replace '-1' by null
 			if (configuration.getSensorExtends() == null || configuration.getSensorExtends().equals("-1"))
 				jedis.del(key(key, "extends"));
 			else
 				jedis.set(key(key, "extends"), configuration.getSensorExtends());
 
 			if (configuration.getParameters() != null) {
 				key = key(key, "properties");
 				for (Parameter param : configuration.getParameters()) {
 					jedis.set(key(key, param.key), param.value);
 				}
 			}
 
 			jedis.save();
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	@Override
 	public Set<String> getAllHosts() throws TException {
 		logger.debug("get all hosts");
 
 		Jedis jedis = jedisPool.getResource();
 		try {
 			String key = key("hosts");
 			if (!jedis.exists(key))
 				return Collections.emptySet();
 
 			Set<String> hostnames = jedis.smembers(key);
 			return hostnames;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	private boolean getSensorActiveState(String hostname, String sensor, Jedis jedis) {
 		logger.debug("get sensor active state");
 
 		// If this host does not extends any other host return the status
 		String extendedKey = key("host", hostname, "extends");
 		boolean sensorActive = false;
 
 		if (jedis.exists(extendedKey)) {
 			String virtualHost = jedis.get(extendedKey);
 			sensorActive = getSensorActiveState(virtualHost, sensor, jedis);
 		} else {
 			String key = key("host", hostname, "sensor", sensor);
 			if (jedis.exists(key))
 				sensorActive = Boolean.parseBoolean(jedis.get(key));
 		}
 
 		return sensorActive;
 	}
 
 	public BundledSensorConfiguration getBundledSensorConfiguration(String sensor, String hostname) throws TException {
 		logger.debug("reading bundled sensor configuration for sensor: " + sensor + " and hostname: " + hostname);
 
 		Jedis jedis = jedisPool.getResource();
 		try {
 			// Set default settings
 			BundledSensorConfiguration config = new BundledSensorConfiguration();
 			config.setSensor(sensor);
 			config.setHostname(hostname);
 
 			// Get the sensor activation state
 			config.setActive(getSensorActiveState(hostname, sensor, jedis));
 
 			// Get sensor configuration
 			SensorConfiguration sensorConfig = getSensorConfiguration(sensor);
 			config.setConfiguration(sensorConfig);
 
 			// Get all labels (aggregation of host and sensor)
 			String key = key("sensor", sensor, "labels");
 			Set<String> sensorLabels = null;
 			if (jedis.exists(key))
 				sensorLabels = jedis.smembers(key);
 			else
 				sensorLabels = Collections.emptySet();
 
 			key = key("host", hostname, "labels");
 			Set<String> hostLabels = null;
 			if (jedis.exists(key))
 				hostLabels = jedis.smembers(key);
 			else
 				hostLabels = Collections.emptySet();
 
 			Set<String> allLabels = new HashSet<String>();
 			allLabels.addAll(sensorLabels);
 			allLabels.addAll(hostLabels);
 			config.setLabels(allLabels);
 
 			return config;
 		} finally {
 			jedisPool.returnResource(jedis);
 		}
 	}
 
 	public void setTsdb(TimeSeriesDatabase tsdb) {
 		this.tsdb = tsdb;
 	}
 
 	public void setLogdb(LogDatabase logdb) {
 		this.logdb = logdb;
 	}
 
 	public List<LogMessage> queryLogs(LogsQuery query) throws TException {
 		List<LogMessage> logMessages = null;
 		try {
 			logMessages = logdb.run(query);
 			return logMessages;
 		} catch (QueryException e) {
 			logger.error("Error while executing query", e);
 		} catch (UnresolvableException e) {
 			logger.error("Error while executing query", e);
 		} catch (InvalidLabelException e) {
 			logger.error("Error while executing query", e);
 		}
 		return new ArrayList<LogMessage>();
 	}
 
 	public SensorConfiguration getSensorConfiguration(String sensor) throws TException {
 		logger.debug("reading sensor configuration for sensor: " + sensor);
 
 		Jedis jedis = jedisPool.getResource();
 		try {
 			SensorConfiguration sensorConfig = new SensorConfiguration();
 			String key = key("sensor", sensor, "config");
 
 			if (jedis.exists(key(key, "interval")))
 				sensorConfig.setInterval(Long.parseLong(jedis.get(key(key, "interval"))));
 			else
 				sensorConfig.setInterval(0);
 
 			// Get the properties
 			// If the sensor extends a virtual sensor, add these parameters too
 			if (jedis.exists(key(key, "extends"))) {
 				String virtualSensor = jedis.get(key(key, "extends"));
 				sensorConfig.setSensorExtends(virtualSensor);
 			} else
 				sensorConfig.setSensorExtends(null);
 
 			// Get the properties
 			sensorConfig.setParameters(getSensorConfigParameters(sensor));
 
 			return sensorConfig;
 		} finally {
 			jedisPool.returnResourceObject(jedis);
 		}
 	}
 
 	private List<Parameter> getSensorConfigParameters(String sensor) {
 		logger.debug("reading sensor parameters for sensor: " + sensor);
 
 		Jedis jedis = jedisPool.getResource();
 		List<Parameter> parameterList = new ArrayList<Parameter>();
 		try {
 			String key = key("sensor", sensor, "config");
 
 			// Add all inherited properties (goes up recursively)
 			if (jedis.exists(key(key, "extends"))) {
 				String virtualSensor = jedis.get(key(key, "extends"));
 				List<Parameter> extendParams = getSensorConfigParameters(virtualSensor);
 				parameterList.addAll(extendParams);
 			}
 
 			// Add the private properties
 			key = key("sensor", sensor, "config", "properties");
 			Set<String> parameters = jedis.keys(key + ":*");
 			for (String name : parameters) {
 				String value = jedis.get(name);
 				String parameterKey = name.substring(name.lastIndexOf(":") + 1);
 
 				// Build parameter
 				Parameter param = new Parameter();
 				param.setKey(parameterKey);
 				param.setValue(value);
 				param.setExtendSensor(sensor);
 
 				// Override inherited properties
 				deleteParameterFromList(parameterKey, parameterList);
 				parameterList.add(param);
 			}
 
 			return parameterList;
 		} finally {
 			jedisPool.returnResourceObject(jedis);
 		}
 	}
 
 	private void deleteParameterFromList(String key, List<Parameter> paramList) {
 		Iterator<Parameter> iter = paramList.iterator();
 		while (iter.hasNext()) {
 			Parameter temp = iter.next();
 			if (temp.getKey().equalsIgnoreCase(key)) {
 				iter.remove();
 			}
 		}
 	}
 
 	public Set<String> getSensorNames() throws TException {
 		Set<String> result = new HashSet<String>();
 		try {
 			result.addAll(tsdb.getSensorNames());
 		} catch (QueryException e) {
 			logger.error("Error while getting the list of configured sensors", e);
 		}
 		return result;
 	}
 }
