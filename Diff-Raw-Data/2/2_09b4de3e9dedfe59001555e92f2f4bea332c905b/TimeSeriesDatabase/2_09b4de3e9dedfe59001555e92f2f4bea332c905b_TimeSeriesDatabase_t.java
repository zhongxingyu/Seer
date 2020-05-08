 package de.tum.in.sonar.collector.tsdb;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import org.apache.hadoop.hbase.HColumnDescriptor;
 import org.apache.hadoop.hbase.HTableDescriptor;
 import org.apache.hadoop.hbase.KeyValue;
 import org.apache.hadoop.hbase.MasterNotRunningException;
 import org.apache.hadoop.hbase.ZooKeeperConnectionException;
 import org.apache.hadoop.hbase.client.HBaseAdmin;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.HTablePool;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.thrift.TDeserializer;
 import org.apache.thrift.TException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.tum.in.sonar.collector.Collector;
 import de.tum.in.sonar.collector.HBaseUtil;
 import de.tum.in.sonar.collector.tsdb.gen.CompactPoint;
 import de.tum.in.sonar.collector.tsdb.gen.CompactTimeseries;
 
 public class TimeSeriesDatabase extends Thread {
 
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
 
 		this.start();
 	}
 
 	private int appendToKey(byte[] key, int index, long value) {
 		return appendToKey(key, index, Bytes.toBytes(value));
 	}
 
 	private int appendToKey(byte[] key, int index, byte[] value) {
 		System.arraycopy(value, 0, key, index, value.length);
 		return value.length;
 	}
 
 	private int keyWidth(int labels) {
 		int keyWidth = Const.SENSOR_ID_WIDTH + Const.TIMESTAMP_WIDTH + Const.HOSTNAME_ID_WIDTH + Const.LABEL_ID_WIDTH * labels;
 		return keyWidth;
 	}
 
 	byte[] buildKey(MetricPoint point) throws UnresolvableException, InvalidLabelException {
 		int labels = 0;
 		if (point.getLabels() != null)
 			labels = point.getLabels().size();
 
 		int keyWidth = keyWidth(labels);
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
 		if (point.getLabels() != null)
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
 			tables.add(new InternalTableSchema(Const.FAMILY_TSDB_DATA, new String[] { Const.FAMILY_UID_FORWARD, Const.FAMILY_UID_BACKWARD }));
 
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
 
 			hbase.close();
 
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
 
 	private BlockingQueue<MetricPoint> queue = new LinkedBlockingQueue<MetricPoint>();
 
 	private ArrayList<Put> puts = new ArrayList<Put>();
 	private HTableInterface table = null;
 
 	public void run() {
 		while (true) {
 			try {
 				MetricPoint dataPoint = queue.take();
 
 				try {
 					byte[] key = buildKey(dataPoint);
 
 					if (table == null)
 						table = this.tsdbTablePool.getTable(Const.TABLE_TSDB);
 
 					try {
 						// Create a new row in this case
 						Put put = new Put(key);
 						byte[] secs = Bytes.toBytes(getSecondsInHour(dataPoint.getTimestamp()));
 						byte[] value = Bytes.toBytes(dataPoint.getValue());
 
 						put.add(Bytes.toBytes(Const.FAMILY_TSDB_DATA), secs, value);
 
 						// Add put to put list
 						puts.add(put);
 
 						// Check if there are enough elements for execution
 						if (puts.size() > 100) {
 							long time = System.currentTimeMillis();
 							table.put(puts);
 							time = System.currentTimeMillis() - time;
 							logger.debug("time required for put operations: " + time);
 							puts.clear();
 						}
 
 						scheduleCompaction(key);
 					} finally {
 						// table.close();
 					}
 
 				} catch (IOException e) {
 					logger.error("could not write tsdb to hbase", e);
 				} catch (UnresolvableException e) {
 					logger.error("could not create key for datapoint", e);
 				} catch (InvalidLabelException e) {
 					logger.error("invalid label used", e);
 				}
 
 			} catch (InterruptedException e) {
 				logger.error("error while taking item from queue", e);
 			}
 		}
 	}
 
 	public void writeData(MetricPoint dataPoint) {
 		try {
 			this.queue.put(dataPoint);
 		} catch (InterruptedException e) {
 			logger.error("error while putting item in queue", e);
 		}
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
 
 	private final byte[] createScannerStartKey(final Query query) throws UnresolvableException, InvalidLabelException {
 		// Create the start key for the scanner
 		byte[] startRow = new byte[keyWidth(0)];
 		int index = 0;
 
 		// Sensor field
 		appendToKey(startRow, index, Bytes.toBytes(sensorResolver.resolveName(query.getSensor())));
 		index += 8;
 
 		// Hostname field
 		if (query.getHostname() != null) {
 			appendToKey(startRow, index, Bytes.toBytes(hostnameResolver.resolveName(query.getHostname())));
 			index += 8;
 		} else {
 			index += 8;
 		}
 
 		// Start time field
 		appendToKey(startRow, index, getHourSinceEpoch(query.getStartTime()));
 		index += 8;
 
 		return startRow;
 	}
 
 	private final byte[] createScannerStopKey(final Query query) throws UnresolvableException, InvalidLabelException {
 		// Create the stop key for the scanner
 		byte[] stopRow = new byte[keyWidth(10)];
 		int index = 0;
 
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
 
 		return stopRow;
 	}
 
 	public TimeSeries run(Query query) throws QueryException, UnresolvableException {
 		HTableInterface table = null;
 		try {
 			logger.debug("start time: " + query.getStartTime());
 			logger.debug("stop time:  " + query.getStopTime());
 			logger.debug("hostname:   " + query.getHostname());
 			logger.debug("sensor:     " + query.getSensor());
 
 			// Get the table
 			table = this.tsdbTablePool.getTable(Const.TABLE_TSDB);
 
 			// New scanner
 			Scan scan = new Scan();
 			scan.setStartRow(createScannerStartKey(query));
 			scan.setStopRow(createScannerStopKey(query));
 
 			// Scan the table
 			ResultScanner scanner = table.getScanner(scan);
 
 			// Time series which holds all the fetched data
 			TimeSeries timeSeries = new TimeSeries();
 			long startTimestampHour = getHourSinceEpoch(query.getStartTime());
 			long stopTimestampHour = getHourSinceEpoch(query.getStopTime() + 1);
 
 			// Over-Estimate the number of elements to fetch
 			// Calculation is based on a 3 second logging interval including the overflow hours
 			long estimatedElementCount = (stopTimestampHour - startTimestampHour + 2) * 60 * 60 / 3;
 			// Calculate for 5 fetch rounds
 			int fetchCount = Math.max(1, (int) (estimatedElementCount / 5));
 
 			Result[] batch;
 			while ((batch = scanner.next(fetchCount)).length > 0) {
 				for (Result next : batch) {
 					byte[] rowKey = next.getRow();
 					long rowTimestampHours = Bytes.toLong(rowKey, Const.SENSOR_ID_WIDTH + Const.HOSTNAME_ID_WIDTH);
 
 					// New fragment for this row
 					TimeSeriesFragment fragment = timeSeries.newFragment();
 
 					NavigableMap<byte[], byte[]> familyMap = next.getFamilyMap(Bytes.toBytes(Const.FAMILY_TSDB_DATA));
 					for (byte[] key : familyMap.keySet()) {
 
 						// Found a compaction field
 						if (Bytes.toString(key).equals("data")) {
 							logger.debug("compaction field");
 
 							if (startTimestampHour <= rowTimestampHours && rowTimestampHours <= stopTimestampHour) {
 								logger.debug("segment");
 								fragment.addSegment(rowTimestampHours, familyMap.get(key));
 							} else {
 								logger.debug("partially");
 								TDeserializer deserializer = new TDeserializer();
 								CompactTimeseries ts = new CompactTimeseries();
 								deserializer.deserialize(ts, familyMap.get(key));
 
 								// TODO: Detect duplicated points! 
 								for (CompactPoint point : ts.getPoints()) {
 									long timestamp = rowTimestampHours + point.getTimestamp();
 									if (timestamp >= query.getStartTime() && timestamp <= query.getStopTime()) {
 										double value = Bytes.toDouble(familyMap.get(key));
 										TimeSeriesPoint tsPoint = new TimeSeriesPoint(timestamp, value);
 										fragment.addPoint(tsPoint);
 									}
 								}
 							}
 
 						} else { // Found a non compacted field
 							logger.debug("point");
 
 							long qualifier = Bytes.toLong(key);
 							long timestamp = rowTimestampHours + qualifier;
 							if (timestamp >= query.getStartTime() && timestamp <= query.getStopTime()) {
 								double value = Bytes.toDouble(familyMap.get(key));
 								TimeSeriesPoint tsPoint = new TimeSeriesPoint(timestamp, value);
 								fragment.addPoint(tsPoint);
 							}
 						}
 					}
 				}
 			}
 
 			scanner.close();
 			return timeSeries;
 
 		} catch (TException e) {
 			throw new QueryException(e);
 		} catch (IOException e) {
 			throw new QueryException(e);
 		} catch (InvalidLabelException e) {
 			throw new QueryException(e);
 		} finally {
 			if (table != null)
 				try {
 					table.close();
 				} catch (IOException e) {
 					logger.trace("could not close HBase table", e);
 				}
 		}
 	}
 
 	public Set<String> getSensorNames() throws QueryException {
 		HTableInterface table = null;
 		try {
 			Set<String> result = new HashSet<String>();
 
 			table = this.tsdbTablePool.getTable(Const.TABLE_UID);
 			Scan scan = new Scan();
 			ResultScanner scanner = table.getScanner(scan);
 
 			Result next;
 			while ((next = scanner.next()) != null) {
 				KeyValue value = next.getColumnLatest(Bytes.toBytes(Const.FAMILY_UID_FORWARD), Bytes.toBytes("sensor"));
 				if (value != null)
					result.add(Bytes.toString(value.getKey()));
 			}
 
 			scanner.close();
 
 			return result;
 		} catch (IOException e) {
 			throw new QueryException(e);
 		} finally {
 			if (table != null)
 				try {
 					table.close();
 				} catch (IOException e) {
 					logger.warn("error while closing table", e);
 				}
 		}
 	}
 }
