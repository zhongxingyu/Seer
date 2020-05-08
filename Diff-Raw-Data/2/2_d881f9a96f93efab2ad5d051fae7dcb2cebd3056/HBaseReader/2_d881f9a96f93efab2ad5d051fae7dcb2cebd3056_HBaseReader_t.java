 package ypf412.hbase.test.worker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.CountDownLatch;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.Get;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.util.Bytes;
 
 import ypf412.hbase.test.worker.Constants.Read;
 
 /**
  * HBase Test Writer Class: SCAN_BY_COLUMN_FAMILY || GET_BY_COLUMN_FAMILY || SCAN_BY_COLUMN || GET_BY_COLUMN
  * 
  * @author jiuling.ypf
  *
  */
 public class HBaseReader {
 	
 	private static final Log LOG = LogFactory.getLog(HBaseReader.class);
 	
 	private static final Configuration conf = HBaseConfiguration.create();
 	
 	private HTable[] hTables;
 	
 	private String dataDir = "/home/lz/tt/DATA/RAWED/taobao_acookie";
 	
 	private int threadNum = 1;
 	
 	private String tableName = "test";
 	
 	private String columnFamily = "t";
 	
 	private Read readType;
 	
 	public HBaseReader(String dataDir, int threadNum, String tableName, String columnFamily, Read readType) {
 		this.dataDir = dataDir;
 		this.threadNum = threadNum;
 		this.tableName = tableName;
 		this.columnFamily = columnFamily;
 		this.readType = readType;
 		
 		initHTables();
 	}
 	
 	/**
 	 * init HBase tables for every thread
 	 * 
 	 * @return
 	 */
 	private boolean initHTables() {
 		try {
 			hTables = new HTable[threadNum];
 			for (int i = 0; i < threadNum; i++) {
 				hTables[i] = new HTable(conf, tableName);
 			}
 		} catch (IOException e) {
 			LOG.error("initHTables error, e=" + e.getStackTrace());
 			return false;
 		}
 		return true;
 	}
 	
 	public void startWorkers() {
 		LOG.info("---------- HBase reader begin to work ----------");
 		CountDownLatch countDownLatch = new CountDownLatch(threadNum);
 		Counter[] counter = new Counter[threadNum];
 		for (int shard = 0; shard < threadNum; shard++)
 			counter[shard] = new Counter();
 		try {
 			for (int shard = 0; shard < threadNum; shard++) {
 				Thread shardThread = new Thread(new ShardRead(shard, countDownLatch, counter[shard]));
 				shardThread.setDaemon(true);
 				shardThread.start();
 			}
 		} catch (Exception e) {
 			LOG.error("failed to create shard thread", e);
 			throw new RuntimeException(e);
 		}
 		try {
 			countDownLatch.await();
 		} catch (InterruptedException e) {
 			LOG.error("worker thread is interrupted", e);
 		}
 		
 		closeHTables();
 		
 		printStat(counter);
 		
 		LOG.info("---------- HBase reader finish to work ----------");
 	}
 	
 	private void printStat(Counter[] counter) {
 		Counter result = new Counter();
 		for (int shard = 0; shard < threadNum; shard++) {
 			System.out.println("======>thread: " + shard);
 			counter[shard].printStat();
 			result.countA += counter[shard].countA;
 			result.countB += counter[shard].countB;
 			result.countC += counter[shard].countC;
 			result.countD += counter[shard].countD;
 			result.countE += counter[shard].countE;
 			result.countAN += counter[shard].countAN;
 			result.countBN += counter[shard].countBN;
 			result.countCN += counter[shard].countCN;
 			result.countDN += counter[shard].countDN;
 			result.countEN += counter[shard].countEN;
 			result.totalRecord += counter[shard].totalRecord;
 			result.totalTime += counter[shard].totalTime;
 			result.totalRecordN += counter[shard].totalRecordN;
			result.totalTimeN += counter[shard].totalTimeN;
 			result.countSuccess += counter[shard].countSuccess;
 			result.countNull += counter[shard].countNull;
 		}
 		
 		System.out.println("======>all threads: ");
 		result.printStat();
 	}
 	
 	private void closeHTables() {
 		try {
 			for (int i = 0; i < threadNum; i++) {
 				hTables[i].close();
 			}
 		} catch (IOException e) {
 			LOG.error("closeHTables error, e=" + e.getStackTrace());
 		}
 	}
 	
 	private class ShardRead implements Runnable {
 		
 		private int shardNum;
 		private CountDownLatch countDownLatch;
 		private Counter counter;
 		private List<File> fileList;
 		private final Random rand = new Random();
 
 
 		public ShardRead(int shardNum, CountDownLatch countDownLatch, Counter counter) throws Exception {
 			this.shardNum = shardNum;
 			this.countDownLatch = countDownLatch;
 			this.counter = counter;
 			this.fileList = loadFiles();
 		}
 
 		public void run() {
 			for (File file : fileList) {
 				if (readType == Read.SCAN_BY_COLUMN_FAMILY || readType == Read.SCAN_BY_COLUMN) {
 					scanFromHBaseDB(shardNum, file, readType);
 				} else if (readType == Read.GET_BY_COLUMN_FAMILY || readType == Read.GET_BY_COLUMN) {
 					getFromHBaseDB(shardNum, file, readType);
 				}
 			}
 			countDownLatch.countDown();
 		}
 		
 		private List<File> loadFiles() {
 			List<File> fileList = new ArrayList<File>();
 			File dir = new File(dataDir);
 			if (dir.exists() && dir.isDirectory()) {
 				File[] allFiles = dir.listFiles();
 				for (int i=0; i<allFiles.length; i++) {
 					if (i % threadNum == shardNum)
 						fileList.add(allFiles[i]);
 				}
 			} else {
 				LOG.error("parameter dataDir: " + dataDir + " is not a directory");
 			}
 			return fileList;
 		}
 
 		private void scanFromHBaseDB(int shardNum, File file, Read readType) {
 			Scan scan = new Scan();
 			if (readType == Read.SCAN_BY_COLUMN_FAMILY)
 				scan.addFamily(Bytes.toBytes(columnFamily));
 			else {
 				List<String> columnList = new ArrayList<String>();
 				int i = 0;
 				while(i < 10) {
 					String columnName = "col" + rand.nextInt(30);
 					if (!columnList.contains(columnName)) {
 						scan.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
 						columnList.add(columnName);
 						i++;
 					}
 				}
 			}
 			scan.setCaching(0);
 			byte[] bShard = { (byte)shardNum };
 			byte[] startRow = Bytes.add(bShard, Bytes.toBytes(file.getAbsolutePath()));
 			byte[] bShardN = { (byte)(shardNum + 1) };
 			byte[] stopRow = bShardN;
 			scan.setStartRow(startRow);
 			scan.setStopRow(stopRow);
 			ResultScanner rs = null;
 			long start = System.currentTimeMillis();
 			try {
 				rs = hTables[shardNum].getScanner(scan);
 			} catch (IOException e) {
 				LOG.error("get scanner error", e);
 			}
 			Result rr = new Result();
 			while (rr != null) {
 				start = System.currentTimeMillis();
 				try {
 					rr = rs.next();
 				} catch (IOException e) {
 					LOG.error("result scanner next error", e);
 				}
 				if (rr != null && !rr.isEmpty()) {
 					counter.add(System.currentTimeMillis() - start);
 				}
 			}
 			rs.close();
 		}
 
 		private byte[] getRowKeyForGet(int shardNum, File file) {
 			int lineNum = rand.nextInt(250000);
 			byte[] bShard = { (byte)shardNum };
 			byte[] bFile = Bytes.toBytes(file.getAbsolutePath());
 			byte[] bLine = Bytes.toBytes(lineNum);
 			byte[] rowKey = Bytes.add(bShard, bFile, bLine);
 			return rowKey;
 		}
 		
 		private void getFromHBaseDB(int shardNum, File file, Read readType) {
 			final int lineNumPerFile = 200000;
 			long start = System.currentTimeMillis();
 			for (int i = 0; i < lineNumPerFile; i++) {
 				byte[] rowKey = getRowKeyForGet(shardNum, file);
 				Get get = new Get(rowKey);
 				if (readType == Read.GET_BY_COLUMN_FAMILY)
 					get.addFamily(Bytes.toBytes(columnFamily));
 				else {
 					List<String> columnList = new ArrayList<String>();
 					int j = 0;
 					while(j < 10) {
 						String columnName = "col" + rand.nextInt(50);
 						if (!columnList.contains(columnName)) {
 							get.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(columnName));
 							columnList.add(columnName);
 							j++;
 						}
 					}
 				}
 				start = System.currentTimeMillis();
 				try {
 					Result result = hTables[shardNum].get(get);
 					if (result != null && !result.isEmpty())
 						counter.add((System.currentTimeMillis() - start));
 					else
 						counter.addN((System.currentTimeMillis() - start));
 				} catch (IOException e) {
 					counter.addN((System.currentTimeMillis() - start));
 				}
 			}
 		}
 		
 	}
 	
 	/**
 	 * static Counter class
 	 * @author jiuling.ypf
 	 *
 	 */
 	class Counter{
 		// process record number
 		public long totalRecord;
 		
 		// process total time
 		public long totalTime;
 		
 		// process record number
 		public long totalRecordN;
 		
 		// process total time
 		public long totalTimeN;
 		
 		// < 1 ms
 		public long countA;
 		
 		// 1 ~ 2ms
 		public long countB;
 		
 		// 2 ~ 10 ms 
 		public long countC;
 		
 		// 10 ~ 20 ms
 		public long countD;
 		
 		// > 20 ms
 		public long countE;
 
 		// < 1 ms
 		public long countAN;
 		
 		// 1 ~ 2ms
 		public long countBN;
 		
 		// 2 ~ 10 ms 
 		public long countCN;
 		
 		// 10 ~ 20 ms
 		public long countDN;
 		
 		// > 20 ms
 		public long countEN;
 
 		
 		// success count
 		public long countSuccess;
 		
 		// null result count
 		public long countNull;
 		
 		// put time
 		public void add(long t) {
 			totalRecord++;
 			totalTime += t;
 			
 			if(t < 1) {
 				countA++;
 			}
 			else if(t < 2) {
 				countB++;
 			}
 			else if(t < 10) {
 				countC++;
 			}
 			else if(t < 20) {
 				countD++;
 			}
 			else{
 				countE++;
 			}
 			
 			countSuccess++;
 		}
 		
 		// put time
 		public void addN(long t) {
 			totalRecordN++;
 			totalTimeN += t;
 			
 			if(t < 1) {
 				countAN ++;
 			}
 			else if(t < 2) {
 				countBN ++;
 			}
 			else if(t< 10) {
 				countCN ++;
 			}
 			else if(t<20) {
 				countDN ++;
 			}
 			else{
 				countEN++;
 			}
 			
 			countNull++;
 		}
 		
 		// print statistic log
 		public void printStat(){
 			System.out.println("----------------------- success stat---------------------------------");
 			System.out.println("totalRecord = " + totalRecord + ", totalTime = " + totalTime + ", hit = " + countSuccess);
 			if(totalRecord != 0){
 				System.out.println("average time per line = "
 						+ ((double)totalTime / (double)totalRecord));
 				System.out.println("average qps = "
 						+ ((double)(totalRecord * 1000) / (double)totalTime));
 			}
 			System.out.println("< 1ms = " + countA + ", 1~2 ms = " + countB
 					+ ", 2~10 ms = " + countC + ", 10~20ms = " + countD + ", > 20ms = " + countE);
 			System.out.println("----------------------- success stat---------------------------------");
 			
 			System.out.println("----------------------- null stat---------------------------------");
 			System.out.println("totalRecord = " + totalRecordN + ", totalTime = " + totalTimeN + ", hit = " + countNull);
 			if(totalRecordN != 0){
 				System.out.println("average time per line = "
 						+ ((double)totalTimeN / (double)totalRecordN));
 				System.out.println("average qps = "
 						+ ((double)(totalRecordN * 1000) / (double)totalTimeN));
 			}
 			System.out.println("< 1ms = " + countAN + ", 1~2 ms = " + countBN
 					+ ", 2~10 ms = " + countCN + ", 10~20ms = " + countDN + ", > 20ms = " + countEN);
 			System.out.println("----------------------- null stat---------------------------------");
 		}
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		if (args.length != 5) {
 			System.err
 					.println("Usage: ypf412.hbase.test.worker.HBaseReader [dataDir] [threadNum] [tableName] [columnFamily] [readType(scan_by_column_family|get_by_column_family|scan_by_column|get_by_column)]");
 			System.exit(1);
 		}
 		
 		String dataDir = args[0];
 		int threadNum = Integer.parseInt(args[1]);
 		String tableName = args[2];
 		String columnFamily = args[3];
 		Read readType = null;
 		if (args[4].equalsIgnoreCase("scan_by_column_family"))
 			readType = Read.SCAN_BY_COLUMN_FAMILY;
 		else if (args[4].equalsIgnoreCase("get_by_column_family"))
 			readType = Read.GET_BY_COLUMN_FAMILY;
 		else if (args[4].equalsIgnoreCase("scan_by_column"))
 			readType = Read.SCAN_BY_COLUMN;
 		else if (args[4].equalsIgnoreCase("get_by_column"))
 			readType = Read.GET_BY_COLUMN;
 		else {
 			System.err.println("invalid read type: " + args[4]);
 			System.exit(1);
 		}
 		HBaseReader reader = new HBaseReader(dataDir, threadNum, tableName, columnFamily, readType);
 		reader.startWorkers();
 	}
 
 }
