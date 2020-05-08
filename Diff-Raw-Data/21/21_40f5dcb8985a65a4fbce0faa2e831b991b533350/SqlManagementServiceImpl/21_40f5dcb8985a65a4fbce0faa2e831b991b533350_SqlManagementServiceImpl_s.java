 package de.tum.in.sonar.collector.server;
 
 import java.beans.PropertyVetoException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.math.BigInteger;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.apache.thrift.TException;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 import de.tum.in.sonar.collector.BundledSensorConfiguration;
 import de.tum.in.sonar.collector.LogMessage;
 import de.tum.in.sonar.collector.LogsQuery;
 import de.tum.in.sonar.collector.ManagementService;
 import de.tum.in.sonar.collector.Parameter;
 import de.tum.in.sonar.collector.SensorConfiguration;
 import de.tum.in.sonar.collector.SensorType;
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
 
 public class SqlManagementServiceImpl implements ManagementService.Iface {
 
 	private static final Logger logger = Logger.getLogger(SqlManagementServiceImpl.class);
 
 	private TimeSeriesDatabase tsdb;
 
 	private LogDatabase logdb;
 
 	private ComboPooledDataSource cpds;
 
 	private SensorlistCache sensorlistCache = new SensorlistCache();
 
 	public SqlManagementServiceImpl() throws PropertyVetoException {
 		cpds = new ComboPooledDataSource();
 		cpds.setDriverClass("com.mysql.jdbc.Driver");
 		cpds.setJdbcUrl("jdbc:mysql://localhost/sonar");
 		cpds.setUser("root");
 		cpds.setPassword("root");
 		cpds.setMinPoolSize(1);
 		cpds.setAcquireIncrement(5);
 		cpds.setMaxPoolSize(50);
 	}
 
 	@Override
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
 
 	private void assertNext(ResultSet res) throws SQLException {
 		if (!res.next()) {
 			logger.error("Result set is expected to have one row");
 			throw new NullPointerException();
 		}
 	}
 
 	@Override
 	public ByteBuffer fetchSensor(String name) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			// Fetch the ZIP BLOB of the sensor
 			PreparedStatement st = con.prepareStatement("select zip from sensors where name = ?");
 			st.setString(1, name);
 			ResultSet res = st.executeQuery();
 			assertNext(res);
 
 			// Copy stuff
 			InputStream in = res.getBlob(1).getBinaryStream();
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			byte[] buffer = new byte[128];
 			int len = 0;
 			while ((len = in.read(buffer)) > 0) {
 				out.write(buffer, 0, len);
 			}
 
 			// Return binary
 			return ByteBuffer.wrap(out.toByteArray());
 
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} catch (IOException e) {
 			logger.warn("IOException while copying sensor binary");
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public String sensorHash(String name) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("select md5 from sensors where name = ?");
 			st.setString(1, name);
 			ResultSet res = st.executeQuery();
 			assertNext(res);
 			return res.getString(1);
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public void deploySensor(String name, ByteBuffer file) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			// Build input stream from buffer
 			ByteArrayInputStream in = new ByteArrayInputStream(file.array());
 
 			// Build query
 			PreparedStatement st = con.prepareStatement("update sensors set zip = ?, md5=? where name = ?");
 			st.setBlob(1, in);
 			st.setString(3, name);
 
 			// Set MD5
 			try {
 				MessageDigest md = MessageDigest.getInstance("MD5");
 				byte[] md5 = md.digest(file.array());
 				BigInteger bigInt = new BigInteger(1, md5);
 				String smd5 = bigInt.toString(16);
 				st.setString(2, smd5);
 				logger.info("deploy md5: " + smd5);
 			} catch (NoSuchAlgorithmException e) {
 				logger.error("Could not create MD5 for sensor binary", e);
 			}
 
 			// Run query
 			st.executeUpdate();
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public Set<String> getAllSensors() throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			PreparedStatement st = con.prepareStatement("select name from sensors");
 			ResultSet res = st.executeQuery();
 			Set<String> names = new HashSet<String>();
 			while (res.next()) {
 				names.add(res.getString(1));
 			}
 
 			return names;
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	@Override
 	public boolean hasBinary(String name) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("select zip from sensors where name = ? and zip is not NULL");
 			st.setString(1, name);
 			ResultSet res = st.executeQuery();
 			return res.next();
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public Set<String> getSensorLabels(String sensor) throws TException {
 		return new HashSet<String>();
 	}
 
 	@Override
 	public void delSensor(String name) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			Integer idSensor = fetchSensorId(con, name);
 			if (idSensor != null) {
 				PreparedStatement st = con.prepareStatement("delete from sensors where name = ?");
 				st.setString(1, name);
 				st.execute();
 
 				PreparedStatement stp = con.prepareStatement("delete from params where SENSOR_ID = ?");
 				stp.setInt(1, idSensor);
 				stp.execute();
 			}
 
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public void setSensorLabels(String sensor, Set<String> labels) throws TException {
 		// pass
 	}
 
 	@Override
 	public void setSensorConfiguration(String name, SensorConfiguration configuration) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			// Get rid of old sensors
 			delSensor(name);
 
 			// Insert new sensor
 			PreparedStatement st = con.prepareStatement(
 					"insert into sensors (name, interv, type, extends) values (?,?,?,?)",
 					Statement.RETURN_GENERATED_KEYS);
 
 			st.setString(1, name);
 			st.setLong(2, configuration.getInterval());
 			st.setString(4, configuration.getSensorExtends());
 			if (configuration.getSensorType() == null)
 				st.setNull(3, java.sql.Types.INTEGER);
 			else
 				st.setInt(3, configuration.getSensorType().getValue());
 			st.executeUpdate();
 
 			// Get generated ID for the new sensor
 			ResultSet keys = st.getGeneratedKeys();
 			if (!keys.next()) {
 				logger.error("Could not get ID of new sensor");
 				return;
 			}
 			int id = keys.getInt(1);
 
 			// Insert parameters of the new sensor
 			if (configuration.getParameters() != null) {
 				for (Parameter param : configuration.getParameters()) {
 					PreparedStatement stParam = con
 							.prepareStatement("insert into params (SENSOR_ID, param, value, extend) values (?,?,?,?)");
 					stParam.setInt(1, id);
 					stParam.setString(2, param.getKey());
 					stParam.setString(3, param.getValue());
 					stParam.setString(4, param.getExtendSensor());
 					stParam.execute();
 				}
 			}
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public SensorConfiguration getSensorConfiguration(String name) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			// Sensor configuration
 			SensorConfiguration config = new SensorConfiguration();
 
 			// Fetch basic sensor configuration
 			PreparedStatement st = con.prepareStatement("select id, interv, type, extends from sensors where name = ?");
 			st.setString(1, name);
 			ResultSet res = st.executeQuery();
 			assertNext(res);
 
 			config.setInterval(res.getLong(2));
 			config.setSensorType(SensorType.findByValue(res.getInt(3)));
 			config.setSensorExtends(res.getString(4));
 
 			// Fetch sensor ID
 			int idSensor = fetchSensorId(con, name);
 
 			// Fetch parameters for this sensor
 			st = con.prepareStatement("select param, value, extend from params where SENSOR_ID = ? ");
 			st.setInt(1, idSensor);
 			res = st.executeQuery();
 			while (res.next()) {
 				Parameter param = new Parameter();
 				param.setKey(res.getString(1));
 				param.setValue(res.getString(2));
 				param.setExtendSensor(res.getString(3));
 				config.addToParameters(param);
 			}
 
 			return config;
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	@Override
 	public Set<String> getSensorNames() throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("select name from sensors");
 			ResultSet res = st.executeQuery();
 			Set<String> sensors = new HashSet<String>();
 			while (res.next()) {
 				sensors.add(res.getString(1));
 			}
 			return sensors;
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public void updateSensorConfiguration(String name, SensorConfiguration configuration, Set<String> labels)
 			throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			// Update central sensor settings
 			PreparedStatement st = con
 					.prepareStatement("update sensors set interv = ?, type = ?, extends = ? where name = ?");
 			st.setLong(1, configuration.getInterval());
 			st.setString(3, configuration.getSensorExtends());
 			st.setString(4, name);
 			if (configuration.getSensorType() == null)
 				st.setNull(2, java.sql.Types.INTEGER);
 			else
 				st.setInt(2, configuration.getSensorType().getValue());
 			st.executeUpdate();
 
 			// Fetch sensor id
 			int idSensor = fetchSensorId(con, name);
 
 			// Delete all parameters
 			st = con.prepareStatement("delete from params where SENSOR_ID = ?");
 			st.setInt(1, idSensor);
 			st.execute();
 
 			// Update sensor parameters
 			if (configuration.getParameters() != null) {
 				for (Parameter param : configuration.getParameters()) {
 					PreparedStatement stParam = con
 							.prepareStatement("insert into params (SENSOR_ID, param, value, extend) values (?,?,?,?)");
 					stParam.setInt(1, idSensor);
 					stParam.setString(2, param.getKey());
 					stParam.setString(3, param.getValue());
 					stParam.setString(4, param.getExtendSensor());
 					st.execute();
 				}
 			}
 		} catch (SQLException e) {
 			logger.error("SQL exception while fetching all sensor names", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public void addHost(String hostname) throws TException {
 		Connection con = null;
 		try {
			// Get rid of existing hosts
			delHost(hostname);

 			// Insert new hosts
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("insert into hosts (name) values(?)");
 			st.setString(1, hostname);
 			st.execute();
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public void addHostExtension(String hostname, String virtualHostName) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("update hosts set extends = ? where name = ?");
 			st.setString(1, virtualHostName);
 			st.setString(2, hostname);
 			st.execute();
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 					// pass
 				}
 		}
 	}
 
 	@Override
 	public String getHostExtension(String hostname) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("select extends from hosts where name = ?");
 			st.setString(1, hostname);
 			ResultSet res = st.executeQuery();
 			assertNext(res);
 			return res.getString(1);
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	@Override
 	public Set<String> getAllHosts() throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			PreparedStatement st = con.prepareStatement("select name from hosts");
 			ResultSet res = st.executeQuery();
 			Set<String> hosts = new HashSet<String>();
 
 			while (res.next()) {
 				hosts.add(res.getString(1));
 			}
 
 			return hosts;
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	@Override
 	public void delHost(String hostname) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 			PreparedStatement st = con.prepareStatement("delete from hosts where name = ?");
 			st.setString(1, hostname);
 			st.execute();
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	@Override
 	public void setHostLabels(String hostname, Set<String> labels) throws TException {
 		// pass
 	}
 
 	@Override
 	public Set<String> getLabels(String hostname) throws TException {
 		return new HashSet<String>();
 	}
 
 	private Integer fetchSensorId(Connection con, String name) throws SQLException {
 		PreparedStatement st = con.prepareStatement("select id from sensors where name = ?");
 		st.setString(1, name);
 
 		ResultSet res = st.executeQuery();
 		if (!res.next())
 			return null;
 
 		return res.getInt(1);
 	}
 
 	private Integer fetchHostId(Connection con, String name) throws SQLException {
 		PreparedStatement st = con.prepareStatement("select id from hosts where name = ?");
 		st.setString(1, name);
 
 		ResultSet res = st.executeQuery();
 		if (!res.next())
 			return null;
 
 		return res.getInt(1);
 	}
 
 	@Override
 	public void setSensor(String hostname, String sensor, boolean activate) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			int idSensor = fetchSensorId(con, sensor);
 			int idHost = fetchHostId(con, hostname);
 
 			if (activate) {
 				PreparedStatement st = con.prepareStatement("insert into host2sensors values (?,?)");
 				st.setInt(1, idHost);
 				st.setInt(2, idSensor);
 				st.execute();
 			} else {
 				PreparedStatement st = con.prepareStatement("delete from host2sensors where HOST_ID=? and SENSOR_ID=?");
 				st.setInt(1, idHost);
 				st.setInt(2, idSensor);
 				st.execute();
 			}
 
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	@Override
 	public Set<String> getSensors(String hostname) throws TException {
 		// Check cache
 		if (sensorlistCache.get(hostname) != null)
 			return sensorlistCache.get(hostname);
 
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			// List with all sensors
 			Set<String> sensors = new HashSet<String>();
 
 			// Add all sensors registered for this host
 			Integer idHost = fetchHostId(con, hostname);
 			if (idHost != null) {
 				PreparedStatement st = con
 						.prepareStatement("select sensors.name from host2sensors join sensors on sensors.ID = host2sensors.SENSOR_ID where HOST_ID = ?");
 				st.setInt(1, idHost);
 				ResultSet res = st.executeQuery();
 				while (res.next()) {
 					sensors.add(res.getString(1));
 				}
 
 				// Add all sensors from extension
 				String extension = getHostExtension(hostname);
 				if (extension != null && !extension.equals("-1"))
 					sensors.addAll(getSensors(extension));
 			}
 
 			// If nothing was found so far
 			// Check matching hostname expressions
 			if (sensors.isEmpty()) {
 				// Query sensors by
 				Set<String> hosts = getAllHosts();
 				for (String host : hosts) {
 
 					// Do not check identical hostname
 					if (host.equals(hostname))
 						continue;
 
 					boolean match = Pattern.matches(host, hostname);
 					if (match) {
 						// Add those sensors to the sensor list
 						sensors.addAll(getSensors(host));
 					}
 				}
 			}
 
 			// Add data to cache
 			sensorlistCache.put(hostname, sensors);
 
 			return sensors;
 
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	private boolean getSensorActiveState(Connection con, String hostname, String sensor) throws SQLException {
 		int idHost = fetchHostId(con, hostname);
 		int idSensor = fetchSensorId(con, sensor);
 
 		PreparedStatement st = con.prepareStatement("select * from host2sensors where HOST_ID=? and SENSOR_ID=?");
 		st.setInt(1, idHost);
 		st.setInt(2, idSensor);
 		ResultSet res = st.executeQuery();
 		return res.next();
 	}
 
 	@Override
 	public BundledSensorConfiguration getBundledSensorConfiguration(String sensor, String hostname) throws TException {
 		Connection con = null;
 		try {
 			con = cpds.getConnection();
 
 			BundledSensorConfiguration config = new BundledSensorConfiguration();
 
 			// Basic settings
 			config.setSensor(sensor);
 			config.setHostname(hostname);
 
 			// Get the sensor activation state
 			config.setActive(getSensorActiveState(con, hostname, sensor));
 
 			// Get sensor configuration
 			SensorConfiguration sensorConfig = getSensorConfiguration(sensor);
 			config.setConfiguration(sensorConfig);
 
 			// Get all labels (aggregation of host and sensor)
 			Set<String> hostLabels = this.getLabels(hostname);
 			Set<String> sensorLabels = this.getSensorLabels(sensor);
 
 			Set<String> allLabels = new HashSet<String>();
 			allLabels.addAll(sensorLabels);
 			allLabels.addAll(hostLabels);
 			config.setLabels(allLabels);
 
 			return config;
 		} catch (SQLException e) {
 			logger.error("SQL exception", e);
 			throw new TException(e);
 		} finally {
 			if (con != null)
 				try {
 					con.close();
 				} catch (SQLException e) {
 				}
 		}
 	}
 
 	public void setTsdb(TimeSeriesDatabase tsdb) {
 		this.tsdb = tsdb;
 	}
 
 	public void setLogdb(LogDatabase logdb) {
 		this.logdb = logdb;
 	}
 }
