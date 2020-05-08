 package de.tum.in.sonar.collector.server;
 
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import redis.clients.jedis.BinaryJedis;
 import redis.clients.jedis.Jedis;
 import redis.clients.jedis.JedisPool;
 import redis.clients.util.SafeEncoder;
 import de.tum.in.sonar.collector.BundledSensorConfiguration;
 import de.tum.in.sonar.collector.ManagementService;
 import de.tum.in.sonar.collector.SensorConfiguration;
 import de.tum.in.sonar.collector.TimeSeriesQuery;
 import de.tum.in.sonar.collector.TransferableTimeSeriesPoint;
 import de.tum.in.sonar.collector.tsdb.Query;
 import de.tum.in.sonar.collector.tsdb.QueryException;
 import de.tum.in.sonar.collector.tsdb.TimeSeries;
 import de.tum.in.sonar.collector.tsdb.TimeSeriesDatabase;
 import de.tum.in.sonar.collector.tsdb.TimeSeriesPoint;
 import de.tum.in.sonar.collector.tsdb.UnresolvableException;
 
 public class ManagementServiceImpl implements ManagementService.Iface {
 
 	private static final Logger logger = LoggerFactory.getLogger(ManagementServiceImpl.class);
 
 	private TimeSeriesDatabase tsdb;
 
 	private JedisPool jedisPool = new JedisPool("srv2");
 
 	@Override
 	public List<TransferableTimeSeriesPoint> query(TimeSeriesQuery query) throws TException {
 
 		Query tsdbQuery = new Query(query.getSensor(), query.getStartTime(), query.getStopTime());
 		try {
 			TimeSeries timeSeries = tsdb.run(tsdbQuery);
 			List<TransferableTimeSeriesPoint> tsPoints = new ArrayList<TransferableTimeSeriesPoint>(100);
 
 			for (TimeSeriesPoint point : timeSeries) {
 				TransferableTimeSeriesPoint tsPoint = new TransferableTimeSeriesPoint();
 				tsPoints.add(tsPoint);
 
 				tsPoint.setTimestamp(point.getTimestamp());
 				tsPoint.setValue(point.getValue());
 				tsPoint.setLabels(point.getLabels());
 			}
 
 			return tsPoints;
 
 		} catch (QueryException e) {
 			logger.error("Error while executing query", e);
 		} catch (UnresolvableException e) {
 			logger.error("Error while mapping in query", e);
 		}
 
 		return new ArrayList<TransferableTimeSeriesPoint>();
 	}
 
 	// Redis Key Layout:
 	// :sensors -> [] - set of sensor names
 	// :hosts -> [] - set of hostnames
 	//
 	// :sensor:[name]:binary - binary for the sensor
 	// :sensor:[name]:config:[item] - configuration for the sensor
 	// :sensor:[name]:labels -> [] - set of labels
 	//
 	// :host:[hostname]:labels -> [] - set of labels
 	// :host:[hostname]:sensor:[sensorname] - enables or disables a sensor
 	// :host:[hostname]:sensor:[sensorname]/config:[item] - overrides a sensor
 	// configuration
 
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
 		return "srv2";
 	}
 
 	@Override
 	public ByteBuffer fetchSensor(String name) throws TException {
 		logger.debug("client fetches the sensor: " + name);
 
 		String key = key("sensor", name);
 		BinaryJedis jedis = new BinaryJedis(getRedisServer());
 		byte[] data = jedis.get(SafeEncoder.encode(key));
 		return ByteBuffer.wrap(data);
 	}
 
 	@Override
 	public void delSensor(String name) throws TException {
 		logger.debug("removing sensor: " + name);
 		Jedis jedis = jedisPool.getResource();
 
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
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public void deploySensor(String name, ByteBuffer binary) throws TException {
 		logger.debug("deploying sensor " + name);
 		Jedis jedis = jedisPool.getResource();
 
 		// Add sensor the the sensor set
 		jedis.sadd("sensors", name);
 
 		// Add sensor binary
 		String key = key("sensor", name, "binary");
 		jedis.set(SafeEncoder.encode(key), binary.array());
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public void setSensorLabels(String name, Set<String> labels) throws TException {
 		logger.debug("setting sensor labels: " + name);
 		Jedis jedis = jedisPool.getResource();
 
 		// Add labels to the key
 		String key = key("sensor", name, "labels");
 		jedis.sadd(key, labels.toArray(new String[] {}));
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public void addHost(String hostname) throws TException {
 		logger.debug("add host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
 		// Add hostname to the hosts list
 		String key = key("hosts");
 		jedis.sadd(key, hostname);
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public void delHost(String hostname) throws TException {
 		logger.debug("removing host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
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
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public void setHostLabels(String hostname, Set<String> labels) throws TException {
 		logger.debug("Setting labels for host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("host", hostname, "labels");
 		jedis.sadd(key, labels.toArray(new String[] {}));
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public Set<String> getLabels(String hostname) throws TException {
 		logger.debug("reading labels for host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
 		// Get all the labels in the key
 		String key = key("host", hostname, "labels");
 		if (!jedis.exists(key))
 			return Collections.emptySet();
 
 		Set<String> labels = jedis.smembers(key);
 
 		jedisPool.returnResource(jedis);
 		return labels;
 	}
 
 	@Override
 	public void setSensor(String hostname, String sensor, boolean activate) throws TException {
 		logger.debug("set sensor: " + sensor + " for th host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
 		// Update the sensor setting
 		String key = key("host", hostname, "sensor", sensor);
 		jedis.set(key, Boolean.toString(activate));
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public Set<String> getAllSensors() throws TException {
		logger.debug("get all available sensors");
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("sensors");
 		if (!jedis.exists(key))
 			return Collections.emptySet();
 
 		Set<String> sensors = jedis.smembers(key);
 
 		jedisPool.returnResource(jedis);
 		return sensors;
 	}
 
 	@Override
 	public boolean hasBinary(String sensor) throws TException {
 		logger.debug("ask for sensor binary: " + sensor);
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("sensor", sensor, "binary");
 		if (!jedis.exists(key))
 			return false;
 
 		byte[] data = jedis.get(SafeEncoder.encode(key));
 		boolean available = data.length > 64;
 
 		jedisPool.returnResource(jedis);
 		return available;
 	}
 
 	@Override
 	public Set<String> getSensors(String hostname) throws TException {
 		logger.debug("get all sensors for host: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("host", hostname, "sensor");
 		String query = key + ":*:true";
 		Set<String> sensorKeys = jedis.keys(query);
 
 		Set<String> sensors = new HashSet<String>();
 		for (String sensor : sensorKeys) {
 			sensor = sensor.replaceFirst(key, "");
 			sensor = sensor.replaceFirst(":true", "");
 
 			logger.debug("sensor found: " + sensor);
 			sensors.add(sensor);
 		}
 
 		jedisPool.returnResource(jedis);
 		return sensors;
 	}
 
 	@Override
 	public Set<String> getSensorLabels(String sensor) throws TException {
 		logger.debug("get sensor labels: " + sensor);
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("sensor", sensor, "labels");
 		if (!jedis.exists(key))
 			return Collections.emptySet();
 
 		Set<String> labels = jedis.smembers(key);
 
 		jedisPool.returnResource(jedis);
 		return labels;
 	}
 
 	@Override
 	public void setSensorConfiguration(String sensor, SensorConfiguration configuration) throws TException {
 		logger.debug("setting sensor configuration: " + sensor);
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("sensor", sensor, "config");
 		jedis.set(key(key, "interval"), Long.toString(configuration.getInterval()));
 
 		jedisPool.returnResource(jedis);
 	}
 
 	@Override
 	public Set<String> getAllHosts() throws TException {
 		logger.debug("get all hosts");
 		Jedis jedis = jedisPool.getResource();
 
 		String key = key("hosts");
 		if (!jedis.exists(key))
 			return Collections.emptySet();
 
 		Set<String> hostnames = jedis.smembers(key);
 
 		jedisPool.returnResource(jedis);
 		return hostnames;
 	}
 
 	public BundledSensorConfiguration getBundledSensorConfiguration(String sensor, String hostname) throws TException {
 		logger.debug("reading bundled sensor configuration for sensor: " + sensor + " and hostname: " + hostname);
 		Jedis jedis = jedisPool.getResource();
 
 		// Set default settings
 		BundledSensorConfiguration config = new BundledSensorConfiguration();
 		config.setSensor(sensor);
 		config.setHostname(hostname);
 
 		// Get the sensor activation state
 		String key = key("host", hostname, "sensor", sensor);
 		if (jedis.exists(key)) {
 			boolean status = Boolean.getBoolean(jedis.get(key));
 			config.setActive(status);
 		} else {
 			config.setActive(false);
 		}
 
 		// Get sensor configuration
 		SensorConfiguration sensorConfig = new SensorConfiguration();
 		key = key("sensor", sensor, "config");
 		if (jedis.exists(key))
 			sensorConfig.setInterval(Long.parseLong(key(key, "interval")));
 		config.setConfiguration(sensorConfig);
 
 		// Get all labels (aggregation of host and sensor)
 		key = key("sensor", sensor, "labels");
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
 
 		jedisPool.returnResource(jedis);
 		return config;
 	}
 
 	public void setTsdb(TimeSeriesDatabase tsdb) {
 		this.tsdb = tsdb;
 	}
 
 }
