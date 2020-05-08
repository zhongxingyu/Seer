 package de.tum.in.sonar.collector.tsdb;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.NavigableMap;
 import java.util.Set;
 
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.tum.in.sonar.collector.Collector;
 import de.tum.in.sonar.collector.HBaseUtil;
 
 public class TimeSeriesDatabase {
 
 	private static Logger logger = LoggerFactory.getLogger(Collector.class);
 
 	private HBaseUtil hbaseUtil;
 
 	private IdResolver labelResolver;
 	private IdResolver hostnameResolver;
 	private IdResolver sensorResolver;
 
 	private HTablePool tsdbTablePool;
 
 	private CompactionQueue compactionQueue;
 
 	public TimeSeriesDatabase() {
 		this.labelResolver = new IdResolver("label");
 		this.hostnameResolver = new IdResolver("hostname");
 		this.sensorResolver = new IdResolver("sensor");
 
 		this.tsdbTablePool = new HTablePool();
 
 		this.compactionQueue = new CompactionQueue();
 		this.compactionQueue.start();
 	}
 
 	private int appendToKey(byte[] key, int index, long value) {
 		return appendToKey(key, index, Bytes.toBytes(value));
 	}
 
 	private int appendToKey(byte[] key, int index, byte[] value) {
 		System.arraycopy(value, 0, key, index, value.length);
 		return value.length;
 	}
 
 	private int keyWidth(int labels) {
 		int keyWidth = Const.SENSOR_ID_WIDTH + Const.TIMESTAMP_WIDTH + Const.HOSTNAME_WIDTH + Const.LABEL_ID_WIDTH
 				* labels;
 
 		return keyWidth;
 	}
 
 	byte[] buildKey(MetricPoint point) throws UnresolvableException, InvalidLabelException {
 		int keyWidth = keyWidth(point.getLabels().size());
 		byte[] key = new byte[keyWidth];
 
 		int index = 0;
 		// Sensor
 		appendToKey(key, index, sensorResolver.resolveName(point.getSensor()));
 		index += 8;
 
 		// Hostname
 		appendToKey(key, index, hostnameResolver.resolveName(point.getHostname()));
 		index += 8;
 
 		// Hour
 		appendToKey(key, index, getHourSinceEpoch(point.getTimestamp()));
 		index += 8;
 
 		// Labels
 		for (String label : point.getLabels()) {
 			appendToKey(key, index, labelResolver.resolveName(label));
 			index += 8;
 		}
 
 		return key;
 	}
 
 	public void setupTables() throws TableCreationException {
 
 		try {
 			HBaseAdmin hbase = new HBaseAdmin(hbaseUtil.getConfig());
 
 			// Table layout
 			Set<InternalTableSchema> tables = new HashSet<InternalTableSchema>();
 			tables.add(new InternalTableSchema(Const.TABLE_TSDB, new String[] { Const.FAMILY_TSDB_DATA }));
 			tables.add(new InternalTableSchema(Const.TABLE_UID, new String[] { "forward", "backward" }));
 			tables.add(new InternalTableSchema(Const.FAMILY_TSDB_DATA, new String[] { Const.FAMILY_UID_FORWARD,
 					Const.FAMILY_UID_BACKWARD }));
 
 			// Remove all existing table from the set
 			HTableDescriptor tableDescriptors[] = hbase.listTables();
 			for (HTableDescriptor desc : tableDescriptors) {
 				String name = Bytes.toString(desc.getName());
 				tables.remove(new InternalTableSchema(name, new String[] {}));
 
 				logger.info("HBase table found: " + name);
 			}
 
 			// Create all remaining tables
 			for (InternalTableSchema internalDesc : tables) {
 
 				logger.debug("Creating HBase table: " + internalDesc.getName());
 
 				HTableDescriptor desc = new HTableDescriptor(internalDesc.getName());
 				for (String family : internalDesc.getFamilies()) {
 					HColumnDescriptor meta = new HColumnDescriptor(family.getBytes());
 					meta.setMaxVersions(internalDesc.getVersions());
 					desc.addFamily(meta);
 				}
 
 				hbase.createTable(desc);
 			}
 
 		} catch (MasterNotRunningException e) {
 			throw new TableCreationException(e);
 		} catch (ZooKeeperConnectionException e) {
 			throw new TableCreationException(e);
 		} catch (IOException e) {
 			throw new TableCreationException(e);
 		}
 	}
 
 	private void scheduleCompaction(byte[] row) {
 		this.compactionQueue.schedule(row);
 	}
 
 	public void writeData(MetricPoint dataPoint) {
 
 		try {
 			byte[] key = buildKey(dataPoint);
 
 			HTableInterface table = this.tsdbTablePool.getTable(Const.TABLE_TSDB);
 
 			// Create a new row in this case
 			Put put = new Put(key);
 			byte[] secs = Bytes.toBytes(getSecondsInHour(dataPoint.getTimestamp()));
 			byte[] value = Bytes.toBytes(dataPoint.getValue());
 
 			put.add(Bytes.toBytes(Const.FAMILY_TSDB_DATA), secs, value);
 			table.put(put);
 
 			scheduleCompaction(key);
 
 		} catch (IOException e) {
 			logger.error("could not write tsdb to hbase", e);
 		} catch (UnresolvableException e) {
 			logger.error("could not create key for datapoint", e);
 		} catch (InvalidLabelException e) {
 			logger.error("invalid label used", e);
 		}
 
 		logger.debug("writing data ");
 	}
 
 	public void setHbaseUtil(HBaseUtil hbaseUtil) {
 		this.hbaseUtil = hbaseUtil;
 
 		this.sensorResolver.setHbaseUtil(hbaseUtil);
 		this.hostnameResolver.setHbaseUtil(hbaseUtil);
 		this.labelResolver.setHbaseUtil(hbaseUtil);
 
 		this.compactionQueue.setHbaseUtil(hbaseUtil);
 	}
 
 	long getHourSinceEpoch(long timestamp) {
 		long hourSinceEpoch = timestamp - (timestamp % 3600);
 		return hourSinceEpoch;
 	}
 
 	long getSecondsInHour(long timestamp) {
 		long hourSinceEpoch = getHourSinceEpoch(timestamp);
 		long offset = (timestamp - hourSinceEpoch);
 		return offset;
 	}
 
 	public String getHexString(byte[] b) {
 		String result = "";
 		for (int i = 0; i < b.length; i++) {
 			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1) + " ";
 
 			if ((i + 1) % 8 == 0)
 				result += " - ";
 		}
 		return result;
 	}
 
 	public TimeSeries run(Query query) throws QueryException, UnresolvableException {
 
 		try {
 			logger.debug("start: " + query.getStartTime());
 			logger.debug("stop: " + query.getStopTime());
 			logger.debug("hostname: " + query.getHostname());
 			logger.debug("sensor: " + query.getSensor());
 
 			HTableInterface table = this.tsdbTablePool.getTable(Const.TABLE_TSDB);
 			Scan scan = new Scan();
 
 			// Empty start row
 			// ===============================================================
			byte[] startRow = new byte[keyWidth(10)];
 			int index = 0;
 
 			// Add sensor
 			appendToKey(startRow, index, Bytes.toBytes(sensorResolver.resolveName(query.getSensor())));
 			index += 8;
 
 			// Add hostname
 			if (query.getHostname() != null) {
 				logger.debug("Using hostname in query");
 				appendToKey(startRow, index, Bytes.toBytes(hostnameResolver.resolveName(query.getHostname())));
 				index += 8;
 			} else {
 				index += 8;
 			}
 
 			// Add start time
 			appendToKey(startRow, index, getHourSinceEpoch(query.getStartTime()));
 			index += 8;
 
 			// Define start row now
 			scan.setStartRow(startRow);
 			logger.info("start: " + getHexString(startRow));
 
 			// Empty stop row
 			// ===============================================================
 			byte[] stopRow = new byte[keyWidth(10)];
 
 			index = 0;
 
 			// Add sensor
 			appendToKey(stopRow, index, Bytes.toBytes(sensorResolver.resolveName(query.getSensor())));
 			index += 8;
 
 			// Add hostname
 			if (query.getHostname() != null) {
 				logger.debug("Using hostname in query");
 				appendToKey(stopRow, index, Bytes.toBytes(hostnameResolver.resolveName(query.getHostname())));
 				index += 8;
 			} else {
 				index += 8;
 			}
 
 			// Add stop time
 			appendToKey(stopRow, index, getHourSinceEpoch(query.getStopTime()));
 			index += 8;
 
 			// Fill the remaining digits with ones
 			for (; index < stopRow.length; index++)
 				stopRow[index] = (byte) 0xFF;
 
 			// Define stop row
 			scan.setStopRow(stopRow);
 			logger.info("stop:  " + getHexString(stopRow));
 
 			// Rows finished
 			// ===============================================================
 
 			// Load the scanner
 			logger.debug("Starting table scanner on query");
 			ResultScanner scanner = table.getScanner(scan);
 
 			TimeSeries timeSeries = new TimeSeries();
 
 			Result next;
 			while ((next = scanner.next()) != null) {
 				byte[] rowKey = next.getRow();
 				long timestampHours = Bytes.toLong(rowKey, Const.SENSOR_ID_WIDTH + 8);
 				long hostname = Bytes.toLong(rowKey, Const.SENSOR_ID_WIDTH);
 				logger.info("hostname: " + hostname);
 
 				TimeSeriesFragment fragment = timeSeries.newFragment();
 
 				NavigableMap<byte[], byte[]> familyMap = next.getFamilyMap(Bytes.toBytes(Const.FAMILY_TSDB_DATA));
 
 				for (byte[] key : familyMap.keySet()) {
 
 					if (Bytes.toString(key).equals("data")) {
 						try {
 							fragment.addSegment(timestampHours, familyMap.get(key));
 						} catch (TException e) {
 							e.printStackTrace();
 						}
 
 					} else {
 						long quali = Bytes.toLong(key);
 						double value = Bytes.toDouble(familyMap.get(key));
 
 						TimeSeriesPoint p = new TimeSeriesPoint();
 						p.setTimestamp(timestampHours + quali);
 						p.setValue(value);
 						fragment.addPoint(p);
 					}
 				}
 			}
 
 			return timeSeries;
 
 		} catch (IOException e) {
 			throw new QueryException(e);
 		} catch (InvalidLabelException e) {
 			throw new QueryException(e);
 		}
 	}
 
 	public Set<String> getSensorNames() throws QueryException {
 		try {
 			Set<String> result = new HashSet<String>();
 			HBaseUtil util = new HBaseUtil();
 
 			HTable table = new HTable(util.getConfig(), Const.TABLE_UID);
 			Scan scan = new Scan();
 			ResultScanner scanner = table.getScanner(scan);
 
 			Result next;
 			while ((next = scanner.next()) != null) {
 				NavigableMap<byte[], byte[]> familyMap = next.getFamilyMap(Bytes.toBytes(Const.FAMILY_UID_FORWARD));
 
 				for (byte[] key : familyMap.keySet()) {
 					// when the data in the sensor is not null, that row key is
 					// the sensor
 					if (Bytes.toString(key).equals("sensor")) {
 						if (null != familyMap.get(key)) {
 							result.add(Bytes.toString(next.getRow()));
 						}
 					}
 				}
 			}
 			return result;
 		} catch (IOException e) {
 			throw new QueryException(e);
 		}
 	}
 }
