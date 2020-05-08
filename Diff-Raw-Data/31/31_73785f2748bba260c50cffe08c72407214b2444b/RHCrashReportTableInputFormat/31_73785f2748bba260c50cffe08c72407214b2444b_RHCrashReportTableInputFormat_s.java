 /**
  * Copyright 2010 Mozilla Foundation
  *
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.godhuli.rhipe.hbase;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.text.DateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.text.SimpleDateFormat;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.conf.Configurable;
 import org.apache.hadoop.conf.Configuration;
 import org.apache.hadoop.hbase.HBaseConfiguration;
 import org.apache.hadoop.hbase.client.HTable;
 import org.apache.hadoop.hbase.client.Result;
 import org.apache.hadoop.hbase.client.ResultScanner;
 import org.apache.hadoop.hbase.client.Scan;
 import org.apache.hadoop.hbase.io.ImmutableBytesWritable;
 import org.apache.hadoop.hbase.mapreduce.TableSplit;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.apache.hadoop.hbase.util.Pair;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.JobContext;
 import org.apache.hadoop.mapreduce.RecordReader;
 import org.apache.hadoop.mapreduce.TaskAttemptContext;
 import org.apache.hadoop.util.StringUtils;
 
 import org.godhuli.rhipe.hbase.RHResult;
 import org.godhuli.rhipe.hbase.RHRaw;
 import java.text.ParseException;
 
 /**
  * Similar to TableInputFormat except this is meant for an array of Scan objects that can
  * be used to delimit row-key ranges.  This allows the usage of hashed dates to be prepended
  * to row keys so that hbase won't create hotspots based on dates, while minimizing the amount
  * of data that must be read during a MapReduce job for a given day.
  * 
  * Note: Only the first Scan object is used as a template.  The rest are only used for ranges.
  * @author Daniel Einspanjer
  * @author Xavier Stevens
  * @author Saptarshi Guha
  *
  */
 public class RHCrashReportTableInputFormat  extends org.apache.hadoop.mapreduce.InputFormat<RHRaw, RHResult> implements Configurable {
 
 	private final Log LOG = LogFactory.getLog(RHCrashReportTableInputFormat.class);
 
 
 	/** Job parameter that specifies the input table. */
 	public static final String INPUT_TABLE = "rhipe.hbase.tablename";
 	
 	/**
 	 * Base-64 encoded array of scanners.
 	 */
 	public static final String RHIPE_COLSPEC = "rhipe.hbase.colspec";
 	
 	private Configuration conf = null;
 	private HTable table = null;
 	private Scan[] scans = null;
 	private TableRecordReader trr = null;
 	
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.mapreduce.InputFormat#createRecordReader(org.apache.hadoop.mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)
 	 */
 	@Override
 	public RecordReader<RHRaw, RHResult> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
 		if (scans == null) {
 		    throw new IOException("No scans were provided");
 		}
 		if (table == null) {
 		    String tableName = this.conf.get(INPUT_TABLE);
 		    try {
 			setHTable(new HTable(HBaseConfiguration.create(conf), tableName));
 		    } catch (Exception e) {
 			LOG.error(StringUtils.stringifyException(e));
 		    }
 		    if (table == null) throw new IOException("No table was provided and could not reinitialize.");
 		}
 		if (trr == null) {
 			trr = new TableRecordReader();
 		}
 		
 		TableSplit tSplit = (TableSplit)split;
 		
 		Scan scan = new Scan(scans[0]);		
 		scan.setStartRow(tSplit.getStartRow());
 		scan.setStopRow(tSplit.getEndRow());
 		
 		trr.setScan(scan);
 		trr.setHTable(table);
 		trr.init();
 		
 		return trr;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.mapreduce.InputFormat#getSplits(org.apache.hadoop.mapreduce.JobContext)
 	 */
 	@Override
 	public List<InputSplit> getSplits(JobContext context) throws IOException, InterruptedException {
 		if (table == null) {
 			throw new IOException("No table was provided.");
 		}
 		
 		Pair<byte[][], byte[][]> keys = table.getStartEndKeys();
 		if (keys == null || keys.getFirst() == null || keys.getFirst().length == 0) {
 			throw new IOException("Expecting at least one region.");
 		}
 
 		Set<InputSplit> splits = new HashSet<InputSplit>();
 		for (int i = 0; i < keys.getFirst().length; i++) {
 			String regionLocation = table.getRegionLocation(keys.getFirst()[i]).getServerAddress().getHostname();
 			
 			for (Scan s : scans) {
 				byte[] startRow = s.getStartRow();
 				byte[] stopRow = s.getStopRow();
 				
 				// determine if the given start an stop key fall into the region
 				if ((startRow.length == 0 || keys.getSecond()[i].length == 0 || Bytes.compareTo(startRow, keys.getSecond()[i]) < 0) && 
 					 (stopRow.length == 0 || Bytes.compareTo(stopRow, keys.getFirst()[i]) > 0)) {
 					byte[] splitStart = startRow.length == 0 || Bytes.compareTo(keys.getFirst()[i], startRow) >= 0 ? keys.getFirst()[i]	: startRow;
 					byte[] splitStop = (stopRow.length == 0 || Bytes.compareTo(keys.getSecond()[i], stopRow) <= 0) 
 										&& keys.getSecond()[i].length > 0 ? keys.getSecond()[i] : stopRow;
 					InputSplit split = new TableSplit(table.getTableName(), splitStart, splitStop, regionLocation);
 					splits.add(split);
 				}
 			}
 		}
 		
 		return new ArrayList<InputSplit>(splits);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.conf.Configurable#getConf()
 	 */
 	@Override
 	public Configuration getConf() {
 		return conf;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.apache.hadoop.conf.Configurable#setConf(org.apache.hadoop.conf.Configuration)
 	 */
 	@Override
 	public void setConf(Configuration conf) {
 		this.conf = conf;
 		
 		String tableName = conf.get(INPUT_TABLE);
 		try {
 			setHTable(new HTable(HBaseConfiguration.create(conf), tableName));
 		} catch (Exception e) {
 			LOG.error(StringUtils.stringifyException(e));
 		}
 		
 		Scan[] scans = null;
 		if (conf.get(RHIPE_COLSPEC) != null) {
 			try {
			    DateFormat formatter = new SimpleDateFormat("yyyyMMdd");
 			    String[] cols = conf.get(RHIPE_COLSPEC).split(",");
 			    ArrayList<Pair<String,String>> l = new ArrayList<Pair<String,String>>(cols.length);
 			    Date d1 = (Date)formatter.parse(conf.get("rhipe.hbase.rowlim.start")); 
 			    Date d2 = (Date)formatter.parse(conf.get("rhipe.hbase.rowlim.end")); 
 			    Calendar c1=Calendar.getInstance(),c2=Calendar.getInstance();
 			    c1.setTime(d1);c2.setTime(d2);
 			    for(int i=0;i < cols.length;i++) {
 				String[] x = cols[i].split(":");
 				l.add(new Pair<String,String>(x[0],x[1]));
				// LOG.info("Added "+x[0]+":"+x[1]);
 			    }
 			    String[] x = conf.get("rhipe.hbase.mozilla.cacheblocks").split(":");
 			    if( conf.get("rhipe.hbase.mozilla.prefix")!=null && conf.get("rhipe.hbase.mozilla.prefix").equals("byteprefix")){
 				LOG.info("Using the byteprefix scanner");
 				scans = Util.generateBytePrefixScans(c1,c2,
								"yyyyMMdd",
 								    l,
 								    Integer.parseInt(x[0]),
 								    Integer.parseInt(x[1]) == 1? true: false);
 
 			    }else{
 				LOG.info("Using the hexprefix scanner");
 				scans = Util.generateHexPrefixScans(c1,c2,
								"yyMMdd",
 								    l,
 								    Integer.parseInt(x[0]),
 								    Integer.parseInt(x[1]) == 1? true: false);
 			    }
 			} catch (ParseException e) {
				LOG.error("An error occurred.", e);
 			}
 		} else {
 			scans = new Scan[] { new Scan() };
 		}
 		
 		setScans(scans);
 	}
 
 	/**
 	 * Allows subclasses to get the {@link HTable}.
 	 */
 	protected HTable getHTable() {
 		return this.table;
 	}
 
 	/**
 	 * Allows subclasses to set the {@link HTable}.
 	 * 
 	 * @param table
 	 *            The table to get the data from.
 	 */
 	protected void setHTable(HTable table) {
 		this.table = table;
 	}
 	
 	/**
 	 * @return
 	 */
 	public Scan[] getScans() {
 		return scans;
 	}
 
 	/**
 	 * @param scans The scans to use as boundaries.
 	 */
 	public void setScans(Scan[] scans) {
 		this.scans = scans;
 	}
 	
 	/**
 	 * Iterate over an HBase table data, return (ImmutableBytesWritable, Result)
 	 * pairs.
 	 */
 	protected class TableRecordReader extends RecordReader<RHRaw, RHResult> {
 
 		private ResultScanner scanner = null;
 		private Scan scan = null;
 		private HTable htable = null;
 		private byte[] lastRow = null;
 		private RHRaw key = null;
 		private Result _value = null;
 		private RHResult value = null;
 		private Result oldresult = null;
 		public void restart(byte[] firstRow) throws IOException {
 			Scan newScan = new Scan(scan);
 			newScan.setStartRow(firstRow);
 			this.scanner = this.htable.getScanner(newScan);
 		}
 
 		public void init() throws IOException {
 			restart(scan.getStartRow());
 		}
 
 		public void setHTable(HTable htable) {
 			this.htable = htable;
 		}
 
 		public void setScan(Scan scan) {
 			this.scan = scan;
 		}
 		public void close() {
 			this.scanner.close();
 		}
 
 		public RHRaw getCurrentKey() throws IOException, InterruptedException {
 			return key;
 		}
 
 		public RHResult getCurrentValue() throws IOException, InterruptedException {
 			return value;
 		}
 
 		public void initialize(InputSplit inputsplit, TaskAttemptContext context) throws IOException,
 				InterruptedException {
 		}
 		public boolean nextKeyValue() throws IOException, InterruptedException {
 			if (key == null)
 				key = new RHRaw();
 			if (value == null){
 			    value = new RHResult();
 			    oldresult = new Result();
 			}
 			try {
 			    oldresult  = this.scanner.next();
 			    if(oldresult != null) {
 				value.set(oldresult); 
 			    }
 			} catch (IOException e) {
 			    LOG.debug("recovered from " + StringUtils.stringifyException(e));
 			    restart(lastRow);
 			    scanner.next(); // skip presumed already mapped row
 			    oldresult = scanner.next();
 			    value.set(oldresult);
 			}
 			if (oldresult != null && oldresult.size() > 0) {
 			    byte[] _b = oldresult.getRow();
 			    key.set(_b);
 			    lastRow = _b;
 			    return true;
 			}
 			return false;
 		}
 
 		public float getProgress() {
 			// Depends on the total number of tuples
 			return 0;
 		}
 	}
 }
