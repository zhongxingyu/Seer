 package de.tum.in.sonar.collector.tsdb;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.NavigableMap;
 import java.util.concurrent.DelayQueue;
 
 import org.apache.hadoop.hbase.client.Delete;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Put;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.thrift.TDeserializer;
 import org.apache.thrift.TException;
 import org.apache.thrift.TSerializer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.tum.in.sonar.collector.HBaseUtil;
 import de.tum.in.sonar.collector.tsdb.gen.CompactPoint;
 import de.tum.in.sonar.collector.tsdb.gen.CompactTimeseries;
 
 class CompactionQueue extends Thread {
 
 	// Logger
 	private Logger logger = LoggerFactory.getLogger(CompactionQueue.class);
 
 	// the delay time 30 minutes currently
 	private static final long TIME_DELAY = 5 * 60 * 1000;
 
 	private DelayQueue<RowKeyJob> delayQueue = new DelayQueue<RowKeyJob>();
 
 	private HTable table = null;
 
 	public void schedule(byte[] key) {
 		RowKeyJob rowKeyJob = new RowKeyJob(TIME_DELAY, key);
 		if (delayQueue.contains(rowKeyJob))
 			delayQueue.remove(rowKeyJob);
 
 		delayQueue.add(rowKeyJob);
 	}
 
	private void compact(byte[] key) throws IOException, TException, InterruptedException {
 		Get get = new Get(key);
 		Result result = table.get(get);
		NavigableMap<byte[], byte[]> familyMap = result.getFamilyMap(Bytes.toBytes(Const.FAMILY_TSDB_DATA));
 
 		List<CompactPoint> points = new ArrayList<CompactPoint>();
 		List<Delete> deletes = new ArrayList<Delete>();
 
 		CompactTimeseries ts = null;
 		TSerializer serializer = new TSerializer();
 		TDeserializer deserializer = new TDeserializer();
 
 		for (byte[] quali : familyMap.keySet()) {
 			if (Bytes.toString(quali).equals("data")) {
 				logger.info("Deserializing...");
 				byte[] data = familyMap.get(quali);
 				ts = new CompactTimeseries();
 				deserializer.deserialize(ts, data);
 				continue;
 			}
 
 			CompactPoint point = new CompactPoint();
 			points.add(point);
 			point.setTimestamp(Bytes.toLong(quali));
 			point.setValue(Bytes.toLong(familyMap.get(quali)));
 
 			Delete del = new Delete(key);
 			del.deleteColumn(Bytes.toBytes(Const.FAMILY_TSDB_DATA), quali);
 			deletes.add(del);
 		}
 
 		if (ts == null)
 			ts = new CompactTimeseries();
 
 		for (CompactPoint element : points)
 			ts.addToPoints(element);
 
 		byte[] buffer = serializer.serialize(ts);
 
 		// Updating HBase
 		Put put = new Put(key);
		put.add(Bytes.toBytes(Const.FAMILY_TSDB_DATA), Bytes.toBytes("data"), buffer);
 		table.put(put);
 		table.delete(deletes);
 		table.flushCommits();
 	}
 
 	private boolean getCompactionCell(final byte[] row) throws IOException {
 		Get get = new Get(row);
		get.addColumn(Bytes.toBytes(Const.FAMILY_TSDB_DATA), Bytes.toBytes("data"));
 		Result result = table.get(get);
		return result.isEmpty();
 	}
 
 	private long getLastModified(final byte[] row) throws IOException {
 		Get get = new Get(row);
 		get.addFamily(Bytes.toBytes("data"));
 		Result result = table.get(get);
 
 		List<Long> timestampList = new ArrayList<Long>();
		Map<Long, byte[]> timestamps = result.getMap().get(Bytes.toBytes(Const.FAMILY_TSDB_DATA))
 				.get(Bytes.toBytes("data"));
 		timestampList.addAll(timestamps.keySet());
 
 		Collections.sort(timestampList);
 		long maxTimestamp = timestampList.get(timestampList.size() - 1);
 
 		return maxTimestamp;
 	}
 
 	@Override
 	public void run() {
 		logger.info("Starting compaction thread...");
 
 		while (true) {
 
 			try {
 				RowKeyJob data = delayQueue.take();
 				boolean exists = getCompactionCell(data.getRowKey());
 
 				// Compaction cell does not exist
 				if (exists == false) {
 					compact(data.getRowKey());
 				} else {
 
 					// Check age of the last insert (compaction or TSD value)
 					long timestamp = getLastModified(data.getRowKey());
 					long delta = System.currentTimeMillis() - timestamp;
 					if (delta > TIME_DELAY) {
 						// Compaction
 						compact(data.getRowKey());
 					} else {
 						// Reschedule
 						logger.debug("reschedule compaction because compaction field changed");
 						delayQueue.add(data);
 					}
 
 				}
			} catch (IOException | TException | InterruptedException e) {
 				logger.error("Error while compacting", e);
 			}
 		}
 	}
 
 	void setHbaseUtil(HBaseUtil hbaseUtil) {
 		try {
 			table = new HTable(hbaseUtil.getConfig(), Const.TABLE_TSDB);
 		} catch (IOException e) {
 			logger.error("could not open HBase table", e);
 		}
 	}
 }
