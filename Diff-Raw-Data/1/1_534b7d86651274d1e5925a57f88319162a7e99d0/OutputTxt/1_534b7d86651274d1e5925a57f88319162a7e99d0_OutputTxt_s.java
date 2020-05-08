 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.command;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.araqne.cron.AbstractTickTimer;
 import org.araqne.cron.TickService;
 import org.araqne.logdb.FileMover;
 import org.araqne.logdb.PartitionOutput;
 import org.araqne.logdb.PartitionPlaceholder;
 import org.araqne.logdb.QueryCommand;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.QueryStopReason;
 import org.araqne.logdb.Row;
 import org.araqne.logdb.RowBatch;
 import org.araqne.logdb.Strings;
 import org.araqne.logdb.TimeSpan;
 import org.araqne.logdb.writer.GzipLineWriterFactory;
 import org.araqne.logdb.writer.LineWriter;
 import org.araqne.logdb.writer.LineWriterFactory;
 import org.araqne.logdb.writer.PlainLineWriterFactory;
 import org.araqne.logdb.writer.RowOutputStreamWriterFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @since 1.6.7
  * @author darkluster
  * 
  */
 public class OutputTxt extends QueryCommand {
 	private final Logger logger = LoggerFactory.getLogger(OutputTxt.class.getName());
 	//private String[] fields;
 	private List<String> fields;
 	private String delimiter;
 	private String encoding;
 	private File f;
 	private String filePath;
 	private String tmpPath;
 	private boolean overwrite;
 	private boolean usePartition;
 	private boolean useGzip;
 	private List<PartitionPlaceholder> holders;
 	private boolean append;
 	private TimeSpan flushInterval;
 	private TickService tickService;
 	private boolean useRowFlush;
 
 	private Map<List<String>, PartitionOutput> outputs;
 	private FileMover mover;
 	private FlushTimer flushTimer = new FlushTimer();
 
 	private LineWriter writer;
 	private LineWriterFactory writerFactory;
 
 	@Deprecated
 	public OutputTxt(File f, String filePath, String tmpPath, boolean overwrite, String delimiter, 
 			List<String> fields, 	boolean useRowFlush, boolean useGzip, String encoding, boolean usePartition, List<PartitionPlaceholder> holders, 
 			boolean append, TimeSpan flushInterval, TickService tickService) {
 
 		try {
 			this.usePartition = usePartition;
 			this.useGzip = useGzip;
 			this.delimiter = delimiter;
 			this.encoding = encoding;
 			this.f = f;
 			this.filePath = filePath;
 			this.tmpPath = tmpPath;
 			this.overwrite = overwrite;
 		//	this.fields = fields.toArray(new String[0]);
 			this.append = append;
 			this.flushInterval = flushInterval;
 						
 			if (useRowFlush)
 				writerFactory = new RowOutputStreamWriterFactory(fields, encoding, append, delimiter);
 			
 			if (useGzip)
 				writerFactory = new GzipLineWriterFactory(fields, delimiter, encoding, append);
 			else
 				writerFactory = new PlainLineWriterFactory(fields, encoding, append, delimiter);
 
 			if (flushInterval != null)
 				tickService.addTimer(flushTimer);
 
 			if (!usePartition) {
 				String path = filePath;
 				if (tmpPath != null)
 					path = tmpPath;
 
 				this.writer = writerFactory.newWriter(path);
 			} else {
 				this.holders = holders;
 				this.outputs = new HashMap<List<String>, PartitionOutput>();
 			}
 
 		} catch (Throwable t) {
 			close();
 			throw new QueryParseException("io-error", -1);
 		}
 	}
 	
 	public OutputTxt( String filePath, String tmpPath, boolean overwrite, String delimiter,
 			List<String> fields, boolean useRowFlush,  boolean useGzip, String encoding, boolean usePartition, List<PartitionPlaceholder> holders,
 			boolean append, TimeSpan flushInterval, TickService tickService)  {
 		
 			this.usePartition = usePartition;
 			this.useGzip = useGzip;
 			this.delimiter = delimiter;
 			this.encoding = encoding;
 			this.filePath = filePath;
 			this.tmpPath = tmpPath;
 			this.overwrite = overwrite;
 			this.fields = fields;
 			//this.fields = fields.toArray(new String[0]);
 			this.append = append;
 			this.flushInterval = flushInterval;
 			this.useRowFlush = useRowFlush;
 			
 			if (flushInterval != null)
 				tickService.addTimer(flushTimer);
 	}
 
 	@Override
 	public String getName() {
 		return "outputtxt";
 	}
 
 	public File getTxtFile() {
 		return f;
 	}
 
 	public List<String> getFields() {
 		return fields;// Arrays.asList(fields);
 	}
 
 	public String getDelimiter() {
 		return delimiter;
 	}
 
 	@Override
 	public void onStart(){
 		File jsonFile = new File(filePath);
 		if (jsonFile.exists() && !overwrite  && !append)
 			throw new IllegalStateException("json file exists: " + jsonFile.getAbsolutePath());
 
 		if (!usePartition && jsonFile.getParentFile() != null)
 			jsonFile.getParentFile().mkdirs();
 		
 		this.f = jsonFile;
 		
 		try {
 		
 			if (useRowFlush)
 				writerFactory = new RowOutputStreamWriterFactory(fields, encoding, append, delimiter);
 			
 			if (useGzip)
 				writerFactory = new GzipLineWriterFactory(fields, delimiter, encoding, append);
 			else
 				writerFactory = new PlainLineWriterFactory(fields, encoding, append, delimiter);
 						
 			if (!usePartition) {
 				String path = filePath;
 				if (tmpPath != null)
 					path = tmpPath;
 
 				this.writer = writerFactory.newWriter(path);
 			} else {
 			//	this.holders = holders;
 				this.outputs = new HashMap<List<String>, PartitionOutput>();
 			}
 		}catch(QueryParseException t){
 			close();
 			throw t;
 		} catch (Throwable t) {
 			close();
 			Map<String, String> params = new HashMap<String, String> ();
 			params.put("msg", t.getMessage());
 			throw new QueryParseException("30406",  -1, -1, params);
 			//throw new QueryParseException("io-error", -1);
 		}
 	}
 	
 	@Override
 	public void onPush(RowBatch rowBatch) {
 		try {
 			if (rowBatch.selectedInUse) {
 				for (int i = 0; i < rowBatch.size; i++) {
 					int p = rowBatch.selected[i];
 					Row m = rowBatch.rows[p];
 
 					writeLog(m);
 				}
 			} else {
 				for (Row m : rowBatch.rows) {
 					writeLog(m);
 				}
 			}
 		} catch (Throwable t) {
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: cannot write log to txt file", t);
 
 			getQuery().stop(QueryStopReason.CommandFailure);
 		}
 
 		pushPipe(rowBatch);
 	}
 
 	@Override
 	public void onPush(Row m) {
 		try {
 			writeLog(m);
 		} catch (Throwable t) {
 			if (logger.isDebugEnabled())
 				logger.debug("araqne logdb: cannot write log to txt file", t);
 
 			getQuery().stop(QueryStopReason.CommandFailure);
 		}
 		pushPipe(m);
 	}
 
 	private void writeLog(Row m) throws IOException {
 		LineWriter writer = this.writer;
 		if (usePartition) {
 			Date date = m.getDate();
 			List<String> key = new ArrayList<String>(holders.size());
 			for (PartitionPlaceholder holder : holders)
 				key.add(holder.getKey(date));
 
 			PartitionOutput output = outputs.get(key);
 			if (output == null) {
 				output = new PartitionOutput(writerFactory, filePath, tmpPath, date, encoding);
 				outputs.put(key, output);
 
 				if (logger.isDebugEnabled())
 					logger.debug("araqne logdb: new partition found key [{}] tmpPath [{}] filePath [{}] date [{}]", new Object[] {
 							key, tmpPath, filePath, date });
 			}
 
 			writer = output.getWriter();
 		}
 
 		writer.write(m);
 	}
 
 	@Override
 	public boolean isReducer() {
 		return true;
 	}
 
 	@Override
 	public void onClose(QueryStopReason reason) {
 		close();
 		if (!append && reason == QueryStopReason.CommandFailure)
 			if (tmpPath != null)
 				new File(tmpPath).delete();
 			else
 				f.delete();
 	}
 
 	private void close() {
 		if (flushInterval != null && tickService != null) {
 			tickService.removeTimer(flushTimer);
 		}
 
 		if (!usePartition) {
 			try {
 				this.writer.close();
 				if (tmpPath != null) {
 					mover.move(tmpPath, filePath);
 				}
 			} catch (Throwable t) {
 				logger.error("araqne logdb: file move failed", t);
 			}
 		} else {
 			for (PartitionOutput output : outputs.values())
 				output.close();
 		}
 	}
 
 	@Override
 	public String toString() {
 		String overwriteOption = "";
 		if (overwrite)
 			overwriteOption = " overwrite=t";
 
 		String appendOption = "";
 		if (append)
 			appendOption = " append=t";
 
 		String compressionOption = "";
 		if (useGzip)
 			compressionOption = " gz=t";
 
 		String encodingOption = "";
 		if (encoding != null)
 			encodingOption = " encoding=" + encoding;
 
 		String delimiterOption = "";
 		if (!delimiter.equals(" "))
 			delimiterOption = " delimiter=" + delimiter;
 
 		String partitionOption = "";
 		if (usePartition)
 			partitionOption = " partition=t";
 
 		String tmpPathOption = "";
 		if (tmpPath != null)
 			tmpPathOption = " tmp=" + tmpPath;
 
 		String path = " " + filePath;
 
 		String fieldsOption = "";
 		//if (fields.length > 0)
 		if (fields.size() > 0)
 			fieldsOption = " " + Strings.join(getFields(), ", ");
 
 		return "outputtxt" + overwriteOption + appendOption + encodingOption + compressionOption + delimiterOption
 				+ partitionOption + tmpPathOption + path + fieldsOption;
 	}
 
 	private class FlushTimer extends AbstractTickTimer {
 
 		@Override
 		public int getInterval() {
 			return (int) flushInterval.getMillis();
 		}
 
 		@Override
 		public void onTick() {
 			try {
 				if (writer != null) {
 					writer.flush();
 				} else {
 
 				}
 			} catch (IOException e) {
 			}
 		}
 	}
 }
